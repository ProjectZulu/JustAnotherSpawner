package jas.common;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
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
    private LivingHandlerRegistry livingHandlerRegistry;
    private StructureHandlerRegistry structureHandlerRegistry;
    private BiomeSpawnListRegistry biomeSpawnListRegistry;
    private LivingGroupRegistry livingGroupRegistry;

    private ImportedSpawnList importedSpawnList;

    protected WorldSettings(File modConfigDirectoryFile, World world, ImportedSpawnList importedSpawnList) {
        this.importedSpawnList = importedSpawnList;
        loadWorldSettings(modConfigDirectoryFile, world);
    }

    public void saveWorldSettings(File configDirectory, World world) {
        if (worldProperties.savedUniversalDirectory != worldProperties.loadedUniversalDirectory
                || worldProperties.savedSortCreatureByBiome != worldProperties.loadedSortCreatureByBiome) {
            worldProperties.savedUniversalDirectory = worldProperties.loadedUniversalDirectory;
            worldProperties.savedSortCreatureByBiome = worldProperties.loadedSortCreatureByBiome;
            File entityFolder = new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName
                    + "/" + DefaultProps.ENTITYSUBDIR);
            for (File file : entityFolder.listFiles()) {
                file.delete();
            }
        }
        worldProperties.saveCurrentToConfig(configDirectory, world);
        biomeGroupRegistry.saveToConfig(configDirectory);
        livingGroupRegistry.saveToConfig(configDirectory);
        creatureTypeRegistry.saveCurrentToConfig(configDirectory);
        livingHandlerRegistry.saveToConfig(configDirectory);
        structureHandlerRegistry.saveCurrentToConfig(configDirectory);
        biomeSpawnListRegistry.saveToConfig(configDirectory);
    }

    public void loadWorldSettings(File modConfigDirectoryFile, World world) {
        worldProperties = new WorldProperties(modConfigDirectoryFile, world);

        biomeGroupRegistry = new BiomeGroupRegistry(worldProperties);
        biomeGroupRegistry.loadFromConfig(modConfigDirectoryFile);

        livingGroupRegistry = new LivingGroupRegistry(worldProperties);
        livingGroupRegistry.loadFromConfig(modConfigDirectoryFile);

        creatureTypeRegistry = new CreatureTypeRegistry(biomeGroupRegistry, worldProperties);
        creatureTypeRegistry.initializeFromConfig(modConfigDirectoryFile);

        livingHandlerRegistry = new LivingHandlerRegistry(livingGroupRegistry, creatureTypeRegistry, worldProperties);
        livingHandlerRegistry.loadFromConfig(modConfigDirectoryFile, world, importedSpawnList);

        structureHandlerRegistry = new StructureHandlerRegistry(livingHandlerRegistry, worldProperties);
        structureHandlerRegistry.setupHandlers(modConfigDirectoryFile, world);

        biomeSpawnListRegistry = new BiomeSpawnListRegistry(worldProperties, biomeGroupRegistry, livingGroupRegistry,
                creatureTypeRegistry, livingHandlerRegistry, structureHandlerRegistry);
        biomeSpawnListRegistry.loadFromConfig(modConfigDirectoryFile, importedSpawnList);
        saveWorldSettings(modConfigDirectoryFile, world);
    }

    public WorldProperties worldProperties() {
        return worldProperties;
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
}