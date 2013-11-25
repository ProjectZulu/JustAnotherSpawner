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
        this.worldProperties = new WorldProperties();
        this.biomeGroupRegistry = new BiomeGroupRegistry(worldProperties);
        this.livingGroupRegistry = new LivingGroupRegistry(worldProperties);
        this.creatureTypeRegistry = new CreatureTypeRegistry(biomeGroupRegistry, worldProperties);
        this.livingHandlerRegistry = new LivingHandlerRegistry(livingGroupRegistry, creatureTypeRegistry,
                worldProperties);
        structureHandlerRegistry = new StructureHandlerRegistry(livingHandlerRegistry, worldProperties);
        loadWorldSettings(modConfigDirectoryFile, world);
    }

    public void saveWorldSettings(File configDirectory, World world) {
        if (worldProperties.getSavedFileConfiguration().universalDirectory != worldProperties.getFolderConfiguration().universalDirectory
                || worldProperties.getSavedFileConfiguration().sortCreatureByBiome != worldProperties
                        .getFolderConfiguration().sortCreatureByBiome) {
            worldProperties.setSavedUniversalDirectory(worldProperties.getFolderConfiguration().universalDirectory);
            worldProperties.setSavedSortCreatureByBiome(worldProperties.getFolderConfiguration().sortCreatureByBiome);
            File entityFolder = new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                    + worldProperties.getFolderConfiguration().saveName + "/" + DefaultProps.ENTITYSUBDIR);
            for (File file : entityFolder.listFiles()) {
                file.delete();
            }
        }
        worldProperties.saveToConfig(configDirectory);
        biomeGroupRegistry.saveToConfig(configDirectory);
        livingGroupRegistry.saveToConfig(configDirectory);
        creatureTypeRegistry.saveCurrentToConfig(configDirectory);
        livingHandlerRegistry.saveToConfig(configDirectory);
        structureHandlerRegistry.saveCurrentToConfig(configDirectory);
        biomeSpawnListRegistry.saveToConfig(configDirectory);
    }

    public void loadWorldSettings(File modConfigDirectoryFile, World world) {
        worldProperties.loadFromConfig(modConfigDirectoryFile, world);
        biomeGroupRegistry.loadFromConfig(modConfigDirectoryFile);
        livingGroupRegistry.loadFromConfig(modConfigDirectoryFile);
        creatureTypeRegistry.loadFromConfig(modConfigDirectoryFile);
        livingHandlerRegistry.loadFromConfig(modConfigDirectoryFile, world);
        structureHandlerRegistry.loadFromConfig(modConfigDirectoryFile, world);

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