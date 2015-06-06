package jas.spawner.refactor;

import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.SpawnSettings.BiomeSettings;
import jas.spawner.refactor.SpawnSettings.LivingSettings;
import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.biome.list.BiomeSpawnList;
import jas.spawner.refactor.biome.list.SpawnEntryGenerator;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configsloader.BiomeSpawnListLoader;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.entities.Group.Parser.LivingContext;
import jas.spawner.refactor.entities.Group.Parser.LocationContext;
import jas.spawner.refactor.entities.Group.Parser.ResultsBuilder;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class BiomeSpawnLists {
	private BiomeSpawnList spawnList;

	public BiomeSpawnList getSpawnList() {
		return spawnList;
	}

	public BiomeSpawnLists(World world, ConfigLoader loader, WorldProperties worldProperties,
			LivingSettings livingsettings, BiomeSettings biomeSettings, LivingTypes livingTypes,
			ImportedSpawnList importedSpawnList) {
		loadFromConfig(world, loader, worldProperties, livingsettings, biomeSettings, livingTypes, importedSpawnList);
	}

	public void loadFromConfig(World world, ConfigLoader loader, WorldProperties worldProperties,
			LivingSettings livingsettings, BiomeSettings biomeSettings, LivingTypes livingTypes,
			ImportedSpawnList importedSpawnList) {
		HashSet<SpawnListEntryBuilder> mapsBuilder = new HashSet<SpawnListEntryBuilder>();
		HashSet<String> saveFilesProcessed = new HashSet<String>();
		for (Entry<String, LoadedFile<BiomeSpawnListLoader>> entry : loader.biomeSpawnListLoaders.entrySet()) {
			if (entry.getValue().saveObject.getBuilders().isEmpty()) {
				saveFilesProcessed.add(entry.getKey());
			} else {
				for (SpawnListEntryBuilder builder : entry.getValue().saveObject.getBuilders()) {
					saveFilesProcessed.add(builder.getModID());
					mapsBuilder.add(builder);
				}
			}
		}

		/**
		 * Default Entries:
		 * 
		 * @0: SpawnListEntry are created for each new LivingMapping & BiomeGroup pairs
		 * 
		 * @1: FOREACH newMapping, SpawnListEntry created for EVERY LivingHandler even if file even if processed
		 * @2: OTHERWISE SpawnListEntry for each LivingHandler if file was NOT processed
		 */
		SpawnEntryGenerator spawnGenerator = new SpawnEntryGenerator(importedSpawnList, livingTypes);

		for (String livingMapping : livingsettings.livingMappings().mappingToKey().keySet()) {
			LivingHandler livingHandler = livingsettings.livingHandlers().getLivingHandler(livingMapping);
			Optional<String> livingHandlerID = livingHandler != null ? Optional.of(livingHandler.livingHandlerID)
					: Optional.<String> absent();
			if (saveFilesProcessed.contains(getSaveFileName(worldProperties, livingMapping))
					&& livingsettings.livingMappings().newMappings().contains(livingMapping)) {
				for (String newMapping : biomeSettings.biomeMappings().newMappings()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, newMapping, livingMapping,
							livingHandlerID, livingsettings.livingMappings(), biomeSettings.biomeMappings());
					mapsBuilder.add(sle);
				}
			} else {
				for (BiomeGroup group : biomeSettings.biomeGroups().iDToGroup().values()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, group, livingMapping,
							livingHandlerID, livingsettings.livingMappings(), biomeSettings.biomeMappings());
					mapsBuilder.add(sle);
				}
			}
		}
		LocationContext biomeContext = new LocationContext(biomeSettings.biomeMappings(),
				biomeSettings.dictionaryGroups(), biomeSettings.biomeAttributes(), biomeSettings.biomeGroups());
		LivingContext livingContext = new LivingContext(livingsettings.livingMappings(), null,
				livingsettings.livingAttributes(), livingsettings.livingHandlers());
		ImmutableSet.Builder<SpawnListEntry> mappingBuilder = ImmutableSet.<SpawnListEntry> builder();
		for (SpawnListEntryBuilder builder : mapsBuilder) {
			// Parse SpawnableLocations
			ResultsBuilder locResult = new MVELExpression<ResultsBuilder>(builder.getLocContent()).evaluate(
					biomeContext, "");
			builder.setLocResults(locResult.resultMappings);
			// Parse SpawnableEntitiies
			ResultsBuilder entResult = new MVELExpression<ResultsBuilder>(builder.getEntContent()).evaluate(
					livingContext, "");
			builder.setEntResults(entResult.resultMappings);
			mappingBuilder.add(builder.build());
		}
		spawnList = new BiomeSpawnList(mappingBuilder.build());
	}

	private String getSaveFileName(WorldProperties worldProperties, String entityGroupID) {
		boolean universalCFG = worldProperties.getSavedFileConfiguration().universalDirectory;
		if (universalCFG) {
			return "Universal";
		} else {
			return LivingHelper.guessModID(entityGroupID);
		}
	}

	public void saveToConfig(WorldProperties worldProperties, ConfigLoader loader) {
		loader.biomeSpawnListLoaders = new HashMap<String, ConfigLoader.LoadedFile<BiomeSpawnListLoader>>();
		Multimap<String, SpawnListEntryBuilder> fileNameToSLE = HashMultimap.create();
		for (SpawnListEntry sle : spawnList.locMappingToSLE().values()) {
			fileNameToSLE.put(sle.modID, new SpawnListEntryBuilder(sle));
		}
		for (String fileName : fileNameToSLE.keySet()) {
			loader.biomeSpawnListLoaders.put(fileName, new LoadedFile<BiomeSpawnListLoader>(new BiomeSpawnListLoader(
					fileNameToSLE.get(fileName), worldProperties.getFolderConfiguration().sortCreatureByBiome)));
		}
	}

	public Collection<String> livingTypesForEntity(World world, Entity entity, LivingSettings livingSettings,
			BiomeSettings biomeSettings) {
		BiomeGenBase biome = world.getBiomeGenForCoords((int) entity.posX, (int) entity.posZ);
		String pckgeName = BiomeHelper.getPackageName(biome);
		String jasBiomeName = biomeSettings.biomeMappings().keyToMapping().get(pckgeName);

		String jasLivingMapping = livingSettings.livingMappings().keyToMapping().get(entity.getClass());

		Set<String> livingTypes = new HashSet<String>();
		for (SpawnListEntry spawnEntry : spawnList.locMappingToSLE().get(jasBiomeName)) {
			if (spawnEntry.entityMappings.contains(jasLivingMapping)) {
				livingTypes.add(spawnEntry.livingTypeID);
			}
		}
		return livingTypes;
	}
}
