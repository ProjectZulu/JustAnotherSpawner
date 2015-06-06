package jas.spawner.modern.world;

import jas.common.JustAnotherSpawner;
import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.DefaultProps;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.eventspawn.EventSpawnRegistry;
import jas.spawner.modern.modification.ModLoadConfig;
import jas.spawner.modern.modification.Modification;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.structure.StructureHandlerRegistry;
import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Do not store references to anything accessed in a static way
 */
public final class WorldSettings {
	private WorldProperties worldProperties;
	private EventSpawnRegistry eventSpawnRegistry;
	private BiomeGroupRegistry biomeGroupRegistry;
	private CreatureTypeRegistry creatureTypeRegistry;
	private LivingHandlerRegistry livingHandlerRegistry;
	private StructureHandlerRegistry structureHandlerRegistry;
	private BiomeSpawnListRegistry biomeSpawnListRegistry;
	private LivingGroupRegistry livingGroupRegistry;
	
	private ImportedSpawnList importedSpawnList;

	public WorldSettings(File configDirectory, World world, ImportedSpawnList importedSpawnList) {
		this.importedSpawnList = importedSpawnList;
		this.worldProperties = new WorldProperties();
		this.eventSpawnRegistry = new EventSpawnRegistry(worldProperties);
		this.biomeGroupRegistry = new BiomeGroupRegistry(worldProperties);
		this.livingGroupRegistry = new LivingGroupRegistry(worldProperties);
		this.creatureTypeRegistry = new CreatureTypeRegistry(biomeGroupRegistry, worldProperties);
		this.livingHandlerRegistry = new LivingHandlerRegistry(livingGroupRegistry, creatureTypeRegistry,
				worldProperties);
		structureHandlerRegistry = new StructureHandlerRegistry(livingHandlerRegistry, worldProperties);
		loadWorldSettings(configDirectory, world);
	}

	public void saveWorldSettings(File configDirectory, World world) {
		if (worldProperties.getSavedFileConfiguration().universalDirectory != worldProperties.getFolderConfiguration().universalDirectory) {
			worldProperties.setSavedUniversalDirectory(worldProperties.getFolderConfiguration().universalDirectory);
			File entityFolder = new File(configDirectory, worldProperties.getFolderConfiguration().saveName + "/"
					+ DefaultProps.ENTITYHANDLERDIR);
			File[] entityFileList = entityFolder.listFiles();
			if (entityFileList != null) {
				for (File file : entityFolder.listFiles()) {
					file.delete();
				}
			}

			File spawnFolder = new File(configDirectory, worldProperties.getFolderConfiguration().saveName + "/"
					+ DefaultProps.ENTITYSPAWNRDIR);
			File[] spawnFileList = entityFolder.listFiles();
			if (spawnFileList != null) {
				for (File file : spawnFolder.listFiles()) {
					file.delete();
				}
			}
		}
		worldProperties.saveToConfig(configDirectory);
		eventSpawnRegistry.saveToConfig(configDirectory);
		biomeGroupRegistry.saveToConfig(configDirectory);
		livingGroupRegistry.saveToConfig(configDirectory);
		creatureTypeRegistry.saveCurrentToConfig(configDirectory);
		livingHandlerRegistry.saveToConfig(configDirectory);
		structureHandlerRegistry.saveCurrentToConfig(configDirectory);
		biomeSpawnListRegistry.saveToConfig(configDirectory);
	}

	public void loadWorldSettings(File modConfigDirectoryFile, World world) {
		worldProperties.loadFromConfig(modConfigDirectoryFile, world);
		eventSpawnRegistry.loadFromConfig(modConfigDirectoryFile);
		biomeGroupRegistry.loadFromConfig(modConfigDirectoryFile);
		livingGroupRegistry.loadFromConfig(modConfigDirectoryFile);
		creatureTypeRegistry.loadFromConfig(modConfigDirectoryFile);
		livingHandlerRegistry.loadFromConfig(modConfigDirectoryFile, world, importedSpawnList);
		structureHandlerRegistry.loadFromConfig(modConfigDirectoryFile, world);

		biomeSpawnListRegistry = new BiomeSpawnListRegistry(worldProperties, biomeGroupRegistry, livingGroupRegistry,
				creatureTypeRegistry, livingHandlerRegistry, structureHandlerRegistry);
		biomeSpawnListRegistry.loadFromConfig(modConfigDirectoryFile, importedSpawnList);
	}

	public WorldProperties worldProperties() {
		return worldProperties;
	}
	
	public EventSpawnRegistry eventSpawnRegistry() {
		return eventSpawnRegistry;
	}

	public BiomeGroupRegistry biomeGroupRegistry() {
		return biomeGroupRegistry;
	}

	public LivingGroupRegistry livingGroupRegistry() {
		return livingGroupRegistry;
	}

	public CreatureTypeRegistry creatureTypeRegistry() {
		return creatureTypeRegistry;
	}

	public LivingHandlerRegistry livingHandlerRegistry() {
		return livingHandlerRegistry;
	}

	public StructureHandlerRegistry structureHandlerRegistry() {
		return structureHandlerRegistry;
	}

	public BiomeSpawnListRegistry biomeSpawnListRegistry() {
		return biomeSpawnListRegistry;
	}

	private Queue<Modification> modifications = new LinkedList<Modification>();

	public synchronized void applyChanges() {
		if(!modifications.isEmpty()) {
			while (!modifications.isEmpty()) {
				Modification modification = modifications.poll();
				modification.applyModification(biomeGroupRegistry);
				modification.applyModification(livingGroupRegistry);
				modification.applyModification(creatureTypeRegistry);
				modification.applyModification(livingHandlerRegistry);
				modification.applyModification(structureHandlerRegistry);
				modification.applyModification(biomeSpawnListRegistry);
				modification.applyModification(this);
			}
			File profileDir = new File(JustAnotherSpawner.getModConfigDirectory(), DefaultProps.MODDIR
					+ DefaultProps.WORLDSETTINGSDIR + MVELProfile.PROFILE_FOLDER);

			this.saveWorldSettings(profileDir, MinecraftServer.getServer().worldServers[0]);
			this.loadWorldSettings(profileDir, MinecraftServer.getServer().worldServers[0]);

			//TODO? update: To ensure changes propagate, such as disabled entities are removed from SpawnListRegistry
			// Better than relying on save/load to clear out trash
			//biomeGroupRegistry.reprocess();
			//livingGroupRegistry.regen();
			//creatureTypeRegistry.regen();
			//livingHandlerRegistry.regen();
			//structureHandlerRegistry.regen();
			//structureHandlerRegistry.regen();
		}
	}

	public synchronized void addChange(Modification modification) {
		modifications.add(modification);
	}
}