package jas.common;

import jas.common.config.LivingConfiguration;
import jas.common.config.SaveConfiguration;

import java.io.File;
import java.io.IOException;

import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public final class WorldProperties {

    /* Functional Universal Directory Settings, marks the Way the System should Sort */
    public boolean savedUniversalDirectory;
    /* Placeholder used to determine if the current directory needs to be deleted and changed */
    public boolean loadedUniversalDirectory;

    /* Functional Universal Directory Settings, marks the Way the System should Sort */
    public boolean savedSortCreatureByBiome;
    /* Placeholder used to determine if the current directory needs to be deleted and changed */
    public boolean loadedSortCreatureByBiome;

    public int despawnDist = 32;
    public int maxDespawnDist = 128;
    public int minDespawnTime = 600;

    public String saveName = "";
    public String importName = "";

    public WorldProperties(File configDirectory, World world) {
        loadWorldSaveConfiguration(configDirectory, world);
        importDefaultFiles(configDirectory);
        loadFileSaveConfiguration(configDirectory);
        loadWorldProperties(configDirectory);
    }

    /**
     * Load data related to how and where the files are desired to be saved
     */
    private void loadWorldSaveConfiguration(File configDirectory, World world) {
        SaveConfiguration configuration = new SaveConfiguration(configDirectory);
        String curWorldName = world.getWorldInfo().getWorldName();
        configuration.load();

        /* Load Save Use Import_Name */
        Property importProp = configuration.getImportName();
        importName = importProp.getString();

        /* Load Save Use Global Save_Name */
        Property defaultsaveProp = configuration.getDefaultSaveName();
        saveName = defaultsaveProp.getString().replace("{$world}", curWorldName);

        /* Load Save Use Actual Save_Name */
        Property saveProp = configuration.getLocalSaveName(curWorldName, saveName);
        saveName = saveProp.getString().trim().equals("") ? "default" : saveProp.getString();

        /* Load Save Sort Creature By Biome */
        boolean globalSortByBiome = JustAnotherSpawner.globalSettings().globalSortCreatureByBiome;
        Property sortByProp = configuration.getLocalSortByCreature(curWorldName, globalSortByBiome);
        loadedSortCreatureByBiome = sortByProp.getBoolean(globalSortByBiome);

        /* Load Save/Use Universal Entity Directory */
        Property useUniProp = configuration.getLocalSortUseUniversal(curWorldName, false);
        loadedUniversalDirectory = useUniProp.getBoolean(false);
        configuration.save();
    }

    private void importDefaultFiles(File modConfigDirectoryFile) {
        if (importName.trim().equals("")) {
            return;
        }
        File worldFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + saveName);
        File importFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + importName);
        if (worldFolder.exists() || !importFolder.exists()) {
            return;
        }
        try {
            FileUtilities.copy(importFolder, worldFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load data related to how the files to be read were actually saved / formatted
     */
    private void loadFileSaveConfiguration(File configDirectory) {
        LivingConfiguration livingTempSettings = new LivingConfiguration(configDirectory, "temporarySaveSettings", this);
        livingTempSettings.load();
        savedSortCreatureByBiome = livingTempSettings.getSavedSortByBiome(loadedSortCreatureByBiome).getBoolean(
                loadedSortCreatureByBiome);
        savedUniversalDirectory = livingTempSettings.getSavedUseUniversalConfig(loadedUniversalDirectory).getBoolean(
                loadedUniversalDirectory);
        livingTempSettings.save();
    }

    private void loadWorldProperties(File configDirectory) {
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + saveName + "/" + "WorldGlobalProperties" + ".cfg"));
        worldConfig.load();
        despawnDist = worldConfig.get("Properties.Spawning", "Min Despawn Distance", despawnDist).getInt(despawnDist);
        worldConfig.save();
    }

    public void saveCurrentToConfig(File configDirectory, World world) {
        saveWorldSaveConfiguration(configDirectory, world);
        saveFileSaveConfiguration(configDirectory);
        saveWorldProperties(configDirectory);
    }

    private void saveWorldSaveConfiguration(File configDirectory, World world) {
        SaveConfiguration configuration = new SaveConfiguration(configDirectory);
        String curWorldName = world.getWorldInfo().getWorldName();
        configuration.load();

        /* Load Save Use Import_Name */
        Property importProp = configuration.getImportName();
        importProp.set(importName);

        /* Load Save Use Global Save_Name */
        Property defaultsaveProp = configuration.getDefaultSaveName();
        defaultsaveProp.set(saveName);

        /* Load Save Use Actual Save_Name */
        Property saveProp = configuration.getLocalSaveName(curWorldName, saveName);
        saveProp.set(saveName);

        /* Load Save Sort Creature By Biome */
        boolean globalSortByBiome = JustAnotherSpawner.globalSettings().globalSortCreatureByBiome;
        Property sortByProp = configuration.getLocalSortByCreature(curWorldName, globalSortByBiome);
        sortByProp.set(loadedSortCreatureByBiome);

        /* Load Save/Use Universal Entity Directory */
        Property useUniProp = configuration.getLocalSortUseUniversal(curWorldName, false);
        useUniProp.set(loadedUniversalDirectory);

        configuration.save();
    }

    private void saveFileSaveConfiguration(File configDirectory) {
        LivingConfiguration livingTempSettings = new LivingConfiguration(configDirectory, "temporarySaveSettings", this);
        livingTempSettings.load();
        Property savedSortCreatureByBiomeProp = livingTempSettings.getSavedSortByBiome(loadedSortCreatureByBiome);
        savedSortCreatureByBiomeProp.set(savedSortCreatureByBiome);
        Property universalDirectoryProp = livingTempSettings.getSavedUseUniversalConfig(loadedUniversalDirectory);
        universalDirectoryProp.set(savedUniversalDirectory);
        livingTempSettings.save();
    }

    private void saveWorldProperties(File configDirectory) {
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + saveName + "/" + "WorldGlobalProperties" + ".cfg"));
        worldConfig.load();
        Property despawnDistProp = worldConfig.get("Properties.Spawning", "Min Despawn Distance", despawnDist);
        despawnDistProp.set(despawnDist);
        worldConfig.save();
    }
}
