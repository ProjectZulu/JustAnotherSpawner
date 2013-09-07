package jas.common;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.BiomeHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;

import net.minecraft.world.World;

/**
 * Do not store references to anything accessed in a static way
 */
public final class WorldSettings {
    private WorldProperties worldProperties;
    private BiomeGroupRegistry biomeGroupRegistry;
    private CreatureTypeRegistry creatureTypeRegistry;
    private CreatureHandlerRegistry creatureHandlerRegistry;
    private BiomeHandlerRegistry biomeHandlerRegistry;
    private BiomeSpawnListRegistry biomeSpawnListRegistry;

    private ImportedSpawnList importedSpawnList;

    protected WorldSettings(File modConfigDirectoryFile, World world, ImportedSpawnList importedSpawnList) {
        this.importedSpawnList = importedSpawnList;
        loadWorldSettings(modConfigDirectoryFile, world);
    }

    public void saveWorldSettings(File configDirectory) {
        biomeGroupRegistry.saveCurrentToConfig(configDirectory);
        creatureTypeRegistry.saveCurrentToConfig(configDirectory);
        creatureHandlerRegistry.saveCurrentToConfig(configDirectory);
        biomeHandlerRegistry.saveCurrentToConfig(configDirectory);
        biomeSpawnListRegistry.saveToConfig(configDirectory);
    }

    public void loadWorldSettings(File modConfigDirectoryFile, World world) {
        worldProperties = new WorldProperties(modConfigDirectoryFile, world);

        biomeGroupRegistry = new BiomeGroupRegistry(worldProperties);
        biomeGroupRegistry.createBiomeGroups(modConfigDirectoryFile);

        creatureTypeRegistry = new CreatureTypeRegistry(biomeGroupRegistry, worldProperties);
        creatureTypeRegistry.initializeFromConfig(modConfigDirectoryFile);

        creatureHandlerRegistry = new CreatureHandlerRegistry(biomeGroupRegistry, creatureTypeRegistry, worldProperties);
        creatureHandlerRegistry.serverStartup(modConfigDirectoryFile, world, importedSpawnList);

        biomeHandlerRegistry = new BiomeHandlerRegistry(creatureHandlerRegistry, worldProperties);
        biomeHandlerRegistry.setupHandlers(modConfigDirectoryFile, world);

        biomeSpawnListRegistry = new BiomeSpawnListRegistry(worldProperties, biomeGroupRegistry, creatureTypeRegistry,
                creatureHandlerRegistry, biomeHandlerRegistry);
        biomeSpawnListRegistry.loadFromConfig(modConfigDirectoryFile, importedSpawnList);
    }

    public WorldProperties worldProperties() {
        return worldProperties;
    }

    public BiomeGroupRegistry biomeGroupRegistry() {
        return biomeGroupRegistry;
    }

    public CreatureTypeRegistry creatureTypeRegistry() {
        return creatureTypeRegistry;
    }

    public CreatureHandlerRegistry creatureHandlerRegistry() {
        return creatureHandlerRegistry;
    }

    public BiomeHandlerRegistry biomeHandlerRegistry() {
        return biomeHandlerRegistry;
    }

    public BiomeSpawnListRegistry biomeSpawnListRegistry() {
        return biomeSpawnListRegistry;
    }
}
