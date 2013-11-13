package jas.common;

import java.io.File;

import net.minecraft.world.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class WorldProperties {

    public boolean loadedUniversalDirectory() {
        return folderConfiguration.getCurrentWorldStats(curWorldName).universalDirectory;
    }

    public boolean loadedSortCreatureByBiome() {
        return folderConfiguration.getCurrentWorldStats(curWorldName).sortCreatureByBiome;
    }

    public String saveName() {
        return folderConfiguration.getCurrentWorldStats(curWorldName).saveName;
    }

    public String importName() {
        return folderConfiguration.importName;
    }

    private String curWorldName;
    private FolderConfiguration folderConfiguration;
    private SavedFolderConfiguration savedConfguration;
    private WorldGlobalSettings worldGlobalProperties;

    public WorldGlobalSettings getGlobal() {
        return worldGlobalProperties;
    }

    public SavedFolderConfiguration getSavedFileConfiguration() {
        return savedConfguration;
    }

    public void setSavedUniversalDirectory(boolean value) {
        savedConfguration = new SavedFolderConfiguration(value, savedConfguration.sortCreatureByBiome);
    }

    public void setSavedSortCreatureByBiome(boolean value) {
        savedConfguration = new SavedFolderConfiguration(savedConfguration.universalDirectory, value);
    }

    public WorldProperties() {
    }

    public void loadFromConfig(File configDirectory, World world) {
        curWorldName = world.getWorldInfo().getWorldName();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        folderConfiguration = GsonHelper.readFromGson(
                FileUtilities.createReader(FolderConfiguration.getFile(configDirectory), false),
                FolderConfiguration.class, gson);
        folderConfiguration.getCurrentWorldStats(world.getWorldInfo().getWorldName());

        importFilesIfNeccesary(configDirectory);

        savedConfguration = GsonHelper.readFromGson(
                FileUtilities.createReader(SavedFolderConfiguration.getFile(configDirectory, saveName()), false),
                SavedFolderConfiguration.class, gson);

        worldGlobalProperties = GsonHelper.readFromGson(
                FileUtilities.createReader(WorldGlobalSettings.getFile(configDirectory, saveName()), false),
                WorldGlobalSettings.class, gson);
    }

    public void saveToConfig(File configDirectory) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        GsonHelper.writeToGson(FileUtilities.createWriter(FolderConfiguration.getFile(configDirectory), true),
                folderConfiguration, gson);
        GsonHelper.writeToGson(
                FileUtilities.createWriter(SavedFolderConfiguration.getFile(configDirectory, saveName()), true),
                savedConfguration, gson);
        GsonHelper.writeToGson(FileUtilities.createWriter(WorldGlobalSettings.getFile(configDirectory, saveName()), true),
                worldGlobalProperties, gson);
    }

    private void importFilesIfNeccesary(File modConfigDirectoryFile) {
        if (importName().trim().equals("")) {
            return;
        }
        File worldFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + saveName());
        File importFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + importName());
        if (worldFolder.exists() || !importFolder.exists()) {
            return;
        }
        FileUtilities.copy(importFolder, worldFolder);
    }
}
