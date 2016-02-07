package jas.spawner.refactor.structure;

import jas.api.StructureInterpreter;
import jas.common.JASLog;
import jas.spawner.refactor.LivingHelper;
import jas.spawner.refactor.LivingTypes;
import jas.spawner.refactor.SpawnSettings.LivingSettings;
import jas.spawner.refactor.WorldProperties;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.structure.StructureHandlerBuilder.StructureHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.world.World;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class StructureHandlers {
	private static final ArrayList<StructureInterpreter> structureInterpreters = new ArrayList<StructureInterpreter>();

	public static void registerInterpreter(StructureInterpreter structureInterpreter) {
		structureInterpreters.add(structureInterpreter);
	}

	private ImmutableList<StructureHandler> structureHandlers;

	public ImmutableList<StructureHandler> handlers() {
		return structureHandlers;
	}

	public StructureHandlers(World world, ConfigLoader loader, LivingSettings livingSettings) {
		loadFromConfig(world, loader, livingSettings);
	}

	public void loadFromConfig(World world, ConfigLoader loader, LivingSettings livingSettings) {
		ArrayList<StructureHandlerBuilder> structureHandlers = new ArrayList<StructureHandlerBuilder>();
		for (StructureInterpreter interpreter : structureInterpreters) {
			structureHandlers.add(new StructureHandlerBuilder(interpreter));
		}
		HashMap<String, Collection<SpawnListEntryBuilder>> structureKeyToSpawnList = loader.structureHandlerLoader.saveObject
				.locKeyToSpawnlist();
		for (StructureHandlerBuilder structureHandlerBuilder : structureHandlers) {
			JASLog.log().info("Starting to load and configure Structure %s SpawnListEntry data",
					structureHandlerBuilder);
			for (String structureKey : structureHandlerBuilder.getStructureKeys()) {
				Collection<SpawnListEntryBuilder> spawnList = structureKeyToSpawnList.get(structureKey);
				if (spawnList == null) {
					spawnList = new ArrayList<SpawnListEntryBuilder>();
					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : structureHandlerBuilder.interpreter
							.getStructureSpawnList(structureKey)) {
						String jasName = livingSettings.livingMappings().keyToMapping().get(spawnListEntry.entityClass);
						LivingHandler livingHandler = livingSettings.livingHandlers().getLivingHandler(jasName);
						SpawnListEntryBuilder builder = new SpawnListEntryBuilder(LivingHelper.guessModID(jasName),
								livingHandler != null ? livingHandler.livingHandlerID : null,
								LivingHelper.guessVanillaLivingType(world, spawnListEntry.entityClass), "Builder().A("
										+ structureKey + ")", "Builder().A(" + jasName + ")");
						StringBuilder sb = new StringBuilder();
						sb.append(spawnListEntry.minGroupCount).append("+ util.rand(1 + ")
								.append(spawnListEntry.maxGroupCount).append(" - ")
								.append(spawnListEntry.minGroupCount);
						builder.setWeight(spawnListEntry.itemWeight).setChunkPackSize(sb.toString());
						spawnList.add(builder);
					}
				}
				for (SpawnListEntryBuilder spawnBuilder : spawnList) {
					if (!spawnBuilder.getLivingTypeIDs().contains(LivingTypes.NONE)) {
						SpawnListEntry spawnEntry = spawnBuilder.build();
						structureHandlerBuilder.addSpawnList(structureKey, spawnEntry);
					} else {
						// Reminder: We ignore the NONE type for any spawnlist (structure or biome)
						JASLog.log().debug(Level.INFO,
								"Not Generating Structure %s SpawnList entries for %s. CreatureTypeID: %s",
								structureKey, spawnBuilder.getEntContent(), spawnBuilder.getLivingTypeIDs());
					}
				}
			}
		}

		Builder<StructureHandler> builder = ImmutableList.builder();
		for (StructureHandlerBuilder structureHandlerBuilder : structureHandlers) {
			builder.add(structureHandlerBuilder.build());
		}
		this.structureHandlers = builder.build();
	}

	public void saveToConfig(WorldProperties worldProperties, ConfigLoader configLoader) {
		configLoader.structureHandlerLoader = new LoadedFile(new StructureHandlerLoader(
				worldProperties.getFolderConfiguration().sortCreatureByBiome, structureHandlers));
	}
}