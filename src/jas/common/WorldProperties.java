package jas.common;

import jas.common.config.LivingConfiguration;

import java.io.File;
import java.io.IOException;

import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public final class WorldProperties {

    /* Functional Universal Directory Settings, marks the Way the System should Sort */
    public boolean universalDirectory;
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
        Configuration worldGloablConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "SaveConfig.cfg"));
        String curWorldName = world.getWorldInfo().getWorldName();
        worldGloablConfig.load();

        /* Load Save Use Import_Name */
        Property importProp = worldGloablConfig.get("Save_Configuration", "Import_Name", "",
                "Folder name to Copy Missing Files From. Case Sensitive if OS allows. Beware invalid OS characters.");
        importName = importProp.getString();

        /* Load Save Use Global Save_Name */
        Property defaultsaveProp = worldGloablConfig
                .get("Save_Configuration",
                        "Default Save_Name",
                        "{$world}",
                        "Default name used for Save_Name. {$world} is replaced by world name. Case Sensitive if OS allows. Beware invalid OS characters.");
        saveName = defaultsaveProp.getString().replace("{$world}", curWorldName);

        /* Load Save Use Actual Save_Name */
        Property saveProp = worldGloablConfig
                .get("Save_Configuration." + curWorldName, "Save_Name", saveName,
                        "Folder name to look for and generate CFG files. Case Sensitive if OS allows. Beware invalid OS characters.");
        saveName = saveProp.getString().trim().equals("") ? "default" : saveProp.getString();

        /* Load Save Sort Creature By Biome */
        loadedSortCreatureByBiome = worldGloablConfig
                .get("Save_Configuration." + curWorldName, "Sort Creature By Biome - Setting",
                        JustAnotherSpawner.globalSettings().globalSortCreatureByBiome,
                        "Determines if Entity CFGs are sorted internally by Entity or Biome. Change from TRUE to FALSE to alter sorting.")
                .getBoolean(JustAnotherSpawner.globalSettings().globalSortCreatureByBiome);

        /* Load Save/Use Universal Entity Directory */
        loadedUniversalDirectory = worldGloablConfig.get("Save_Configuration." + curWorldName,
                "Universal Entity CFG - Settings", false,
                "Specifies if the User wants the Entity CFG to Combined into a Universal CFG.").getBoolean(false);
        worldGloablConfig.save();
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
        universalDirectory = livingTempSettings.getSavedUseUniversalConfig(loadedUniversalDirectory).getBoolean(
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

    public void saveCurrentToConfig(File configDirectory) {
        saveWorldSaveConfiguration(configDirectory);
        saveFileSaveConfiguration(configDirectory);
        saveWorldProperties(configDirectory);
    }

    private void saveWorldSaveConfiguration(File configDirectory) {
        Configuration worldGloablConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "SaveConfig.cfg"));
        String curWorldName = "Doesn't Matter as we are overwriting saved data";
        worldGloablConfig.load();

        /* Load Save Use Import_Name */
        Property importProp = worldGloablConfig.get("Save_Configuration", "Import_Name", "",
                "Folder name to Copy Missing Files From. Case Sensitive if OS allows. Beware invalid OS characters.");
        importProp.set(importName);

        /* Load Save Use Global Save_Name */
        Property defaultsaveProp = worldGloablConfig
                .get("Save_Configuration",
                        "Default Save_Name",
                        "{$world}",
                        "Default name used for Save_Name. {$world} is replaced by world name. Case Sensitive if OS allows. Beware invalid OS characters.");
        defaultsaveProp.set(saveName);

        /* Load Save Use Actual Save_Name */
        Property saveProp = worldGloablConfig
                .get("Save_Configuration." + curWorldName, "Save_Name", saveName,
                        "Folder name to look for and generate CFG files. Case Sensitive if OS allows. Beware invalid OS characters.");
        saveProp.set(saveName);

        /* Load Save Sort Creature By Biome */
        Property loadedSortCreatureByBiomeProp = worldGloablConfig
                .get("Save_Configuration." + curWorldName, "Sort Creature By Biome - Setting",
                        JustAnotherSpawner.globalSettings().globalSortCreatureByBiome,
                        "Determines if Entity CFGs are sorted internally by Entity or Biome. Change from TRUE to FALSE to alter sorting.");
        loadedSortCreatureByBiomeProp.set(loadedSortCreatureByBiome);

        /* Load Save/Use Universal Entity Directory */
        Property loadedUniversalDirectoryProp = worldGloablConfig.get("Save_Configuration." + curWorldName,
                "Universal Entity CFG - Settings", false,
                "Specifies if the User wants the Entity CFG to Combined into a Universal CFG.");
        loadedUniversalDirectoryProp.set(loadedUniversalDirectory);
        worldGloablConfig.save();
    }

    private void saveFileSaveConfiguration(File configDirectory) {
        LivingConfiguration livingTempSettings = new LivingConfiguration(configDirectory, "temporarySaveSettings", this);
        livingTempSettings.load();
        Property savedSortCreatureByBiomeProp = livingTempSettings.getSavedSortByBiome(loadedSortCreatureByBiome);
        savedSortCreatureByBiomeProp.set(savedSortCreatureByBiome);
        Property universalDirectoryProp = livingTempSettings.getSavedUseUniversalConfig(loadedUniversalDirectory);
        universalDirectoryProp.set(universalDirectory);
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
