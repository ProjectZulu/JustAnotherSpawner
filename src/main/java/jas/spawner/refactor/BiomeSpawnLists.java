package jas.spawner.refactor;

import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.biome.BiomeAttributes;
import jas.spawner.refactor.biome.BiomeDictionaryGroups;
import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.biome.BiomeGroups;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.biome.list.BiomeSpawnList;
import jas.spawner.refactor.biome.list.SpawnEntryGenerator;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configsloader.BiomeSpawnListLoader;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.entities.Group.Parser.ExpressionContext;
import jas.spawner.refactor.entities.Group.Parser.ResultsBuilder;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.entities.LivingAttributes;
import jas.spawner.refactor.entities.LivingMappings;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class BiomeSpawnLists {
	private BiomeMappings biomeMappings;
	private BiomeAttributes biomeAttributes;
	private BiomeGroups biomeGroups;
	private BiomeDictionaryGroups dictionaryGroups;

	private LivingTypes livingTypes;
	private LivingHandlers livingHandlers;
	// StructureHandlerRegistry
	private LivingMappings livingMappings; // This may come from LivingHandlers depending on if it needs it
	private BiomeSpawnList spawnList;
	public BiomeSpawnList getSpawnList() {
		return spawnList;
	}

	public BiomeSpawnLists(World world, WorldProperties worldProperties, LivingMappings livingMappings, ConfigLoader loader,
			ImportedSpawnList importedSpawnList) {
		loadFromConfig(world, loader, worldProperties, livingMappings, importedSpawnList);
	}

	public void loadFromConfig(World world, ConfigLoader loader, WorldProperties worldProperties, LivingMappings livingMappings,
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

		 * @1: FOREACH newMapping, SpawnListEntry created for EVERY LivingHandler even if file even if processed
		 * @2: OTHERWISE SpawnListEntry for each LivingHandler if file was NOT processed
		 */
		SpawnEntryGenerator spawnGenerator = new SpawnEntryGenerator(importedSpawnList, livingTypes);

		for (String livingMapping : livingMappings.mappingToKey().keySet()) {
			LivingHandler livingHandler = livingHandlers.getLivingHandler(livingMapping);
			Optional<String> livingHandlerID = livingHandler != null ? Optional.of(livingHandler.livingHandlerID)
					: Optional.<String> absent();
			if (saveFilesProcessed.contains(getSaveFileName(worldProperties, livingMapping))
					&& livingMappings.newMappings().contains(livingMapping)) {
				for (String newMapping : biomeMappings.newMappings()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, newMapping, livingMapping,
							livingHandlerID, livingMappings, biomeMappings);
					mapsBuilder.add(sle);
				}
			} else {
				for (BiomeGroup group : biomeGroups.iDToGroup().values()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, group, livingMapping,
							livingHandlerID, livingMappings, biomeMappings);
					mapsBuilder.add(sle);
				}
			}
		}
		ExpressionContext context = new ExpressionContext(biomeMappings, dictionaryGroups, biomeAttributes, biomeGroups);
		ImmutableSet.Builder<SpawnListEntry> mappingBuilder = ImmutableSet.<SpawnListEntry> builder();
		for (SpawnListEntryBuilder builder : mapsBuilder) {
			// Parse SpawnableLocations
			ResultsBuilder locResult = new MVELExpression<ResultsBuilder>(builder.getLocContent())
					.evaluate(context, "");
			builder.setLocResults(locResult.resultMappings);
			// Parse SpawnableEntitiies
			ResultsBuilder entResult = new MVELExpression<ResultsBuilder>(builder.getEntContent())
					.evaluate(context, "");
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

	public Collection<String> livingTypesForEntity(World world, Entity entity) {
		BiomeGenBase biome = world.getBiomeGenForCoords((int) entity.posX, (int) entity.posZ);
		String pckgeName = BiomeHelper.getPackageName(biome);
		String jasBiomeName = biomeMappings.keyToMapping().get(pckgeName);

		String jasLivingMapping = livingMappings.keyToMapping().get(entity.getClass());

		Set<String> livingTypes = new HashSet<String>();
		for (SpawnListEntry spawnEntry : spawnList.locMappingToSLE().get(jasBiomeName)) {
			if (spawnEntry.entityMappings.contains(jasLivingMapping)) {
				livingTypes.add(spawnEntry.livingTypeID);
			}
		}
		return livingTypes;
	}
}
