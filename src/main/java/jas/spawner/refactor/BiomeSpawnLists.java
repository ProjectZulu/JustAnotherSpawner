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
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.Group.Parser.ExpressionContext;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.CharMatcher;

public class BiomeSpawnLists {
	private BiomeMappings biomeMappings;
	private BiomeAttributes biomeAttributes;
	private BiomeGroups biomeGroups;
	private BiomeDictionaryGroups dictionaryGroups;

	private LivingTypes livingTypes;
	private LivingHandlers livingHandlers;
	// StructureHandlerRegistry

	private BiomeSpawnList spawnList;

	public BiomeSpawnList getSpawnList() {
		return spawnList;
	}
	
	public BiomeSpawnLists(World world, WorldProperties worldProperties, ConfigLoader loader,
			ImportedSpawnList importedSpawnList) {
		loadFromConfig(world, loader, worldProperties, importedSpawnList);
	}

	public void loadFromConfig(World world, ConfigLoader loader, WorldProperties worldProperties,
			ImportedSpawnList importedSpawnList) {
		livingHandlers = new LivingHandlers(this);
		livingTypes = new LivingTypes();
		biomeMappings = new BiomeMappings(loader);
		dictionaryGroups = new BiomeDictionaryGroups(biomeMappings);
		biomeAttributes = new BiomeAttributes(loader, biomeMappings, dictionaryGroups);
		biomeGroups = new BiomeGroups(loader, biomeMappings, dictionaryGroups, biomeAttributes);

		ImmutableMapGroupsBuilder<SpawnListEntryBuilder> mapsBuilder = new ImmutableMapGroupsBuilder<SpawnListEntryBuilder>(
				BiomeSpawnList.key);
		HashSet<String> saveFilesProcessed = new HashSet<String>();
		for (Entry<String, LoadedFile<BiomeSpawnListLoader>> entry : loader.biomeSpawnListLoaders.entrySet()) {
			if (entry.getValue().saveObject.getBuilders().isEmpty()) {
				saveFilesProcessed.add(entry.getKey());
			} else {
				for (SpawnListEntryBuilder builder : entry.getValue().saveObject.getBuilders()) {
					saveFilesProcessed.add(getSaveFileName(worldProperties, builder.getLivingHandlerID()));
					mapsBuilder.addGroup(builder);
				}
			}
		}

		/**
		 * Default Entries:
		 * 
		 * @0: SpawnListEntry are created for LivingHandler & BiomeGroup pairs
		 * @1: FOREACH newMapping, SpawnListEntry created for EVERY LivingHandler even if file even if processed
		 * @2: OTHERWISE SpawnListEntry for each LivingHandler if file was NOT processed
		 */
		SpawnEntryGenerator spawnGenerator = new SpawnEntryGenerator(importedSpawnList, livingTypes);
		for (LivingHandler handler : livingHandlers.iDToGroup().values()) {
			if (saveFilesProcessed.contains(getSaveFileName(worldProperties, handler.livingHandlerID))
					&& !biomeMappings.newMappings().contains(handler.livingHandlerID)) {
				for (String newMapping : biomeMappings.newMappings()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, newMapping, handler,
							livingHandlers.livingMappings(), biomeMappings);
					mapsBuilder.addGroup(sle);
				}
			} else {
				for (BiomeGroup group : biomeGroups.iDToGroup().values()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, group, handler,
							livingHandlers.livingMappings(), biomeMappings);
					mapsBuilder.addGroup(sle);

				}
			}
		}
		ExpressionContext context = new ExpressionContext(biomeMappings, dictionaryGroups, biomeAttributes, biomeGroups);
		ImmutableMapGroupsBuilder<SpawnListEntry> mappingBuilder = new ImmutableMapGroupsBuilder<SpawnListEntry>(
				BiomeSpawnList.key);
		for (SpawnListEntryBuilder builder : mapsBuilder.iDToGroup().values()) {
			Group.Parser.parseGroupContents(builder, context);
			mappingBuilder.addGroup(builder.build());
		}
		spawnList = new BiomeSpawnList(mappingBuilder);
	}

	private String getSaveFileName(WorldProperties worldProperties, String entityGroupID) {
		boolean universalCFG = worldProperties.getSavedFileConfiguration().universalDirectory;
		if (universalCFG) {
			return "Universal";
		} else {
			String modID;
			String[] mobNameParts = entityGroupID.split("\\.");
			if (mobNameParts.length >= 2) {
				String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
				modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
			} else {
				modID = "Vanilla";
			}
			return modID;
		}
	}

	public Collection<String> livingTypesForEntity(World world, Entity entity) {
		BiomeGenBase biome = world.getBiomeGenForCoords((int) entity.posX, (int) entity.posZ);
		String pckgeName = BiomeHelper.getPackageName(biome);
		String jasBiomeName = biomeMappings.keyToMapping().get(pckgeName);

		Collection<String> livingTypes = new HashSet<String>();
		for (String spawnListEntryID : spawnList.mappingToID().get(jasBiomeName)) {
			SpawnListEntry spawnEntry = spawnList.iDToGroup().get(spawnListEntryID);
			// This doesn't work we have no way to check if SpawnListEntry corresponds to Entity entity
			// livingTypes.add(spawnEntry.livingTypeID);
		}
		return livingTypes;
	}
}
