package jas.common;

import jas.common.FolderConfiguration.WorldStats;

import java.io.File;

import net.minecraft.world.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class WorldProperties {

    private String curWorldName;
    private FolderConfiguration folderConfiguration;
    private SavedFolderConfiguration savedConfguration;
    private WorldGlobalSettings worldGlobalProperties;

    public WorldStats getFolderConfiguration() {
        return folderConfiguration.getCurrentWorldStats(curWorldName);
    }

    public String importName() {
        return folderConfiguration.importName;
    }

    public WorldGlobalSettings getGlobal() {
        return worldGlobalProperties;
    }

    public SavedFolderConfiguration getSavedFileConfiguration() {
        return savedConfguration;
    }

    public void setSavedUniversalDirectory(boolean value) {
        savedConfguration = new SavedFolderConfiguration(value);
    }

    public WorldProperties() {
    }

    public void loadFromConfig(File configDirectory, World world) {
        curWorldName = world.getWorldInfo().getWorldName();
        Gson gson = new GsonBuilder().setVersion(DefaultProps.GSON_VERSION).setPrettyPrinting().create();
        folderConfiguration = GsonHelper.readFromGson(
                FileUtilities.createReader(FolderConfiguration.getFile(configDirectory), false),
                FolderConfiguration.class, gson);
        folderConfiguration.getCurrentWorldStats(world.getWorldInfo().getWorldName());

        importFilesIfNeccesary(configDirectory);

        savedConfguration = GsonHelper.readFromGson(
                FileUtilities.createReader(
                        SavedFolderConfiguration.getFile(configDirectory, getFolderConfiguration().saveName), false),
                SavedFolderConfiguration.class, gson);

        worldGlobalProperties = GsonHelper.readFromGson(
                FileUtilities.createReader(
                        WorldGlobalSettings.getFile(configDirectory, getFolderConfiguration().saveName), false),
                WorldGlobalSettings.class, gson);
    }

    public void saveToConfig(File configDirectory) {
        Gson gson = new GsonBuilder().setVersion(DefaultProps.GSON_VERSION).setPrettyPrinting().create();
        GsonHelper.writeToGson(FileUtilities.createWriter(FolderConfiguration.getFile(configDirectory), true),
                folderConfiguration, gson);
        GsonHelper.writeToGson(
                FileUtilities.createWriter(
                        SavedFolderConfiguration.getFile(configDirectory, getFolderConfiguration().saveName), true),
                savedConfguration, gson);
        GsonHelper.writeToGson(
                FileUtilities.createWriter(
                        WorldGlobalSettings.getFile(configDirectory, getFolderConfiguration().saveName), true),
                worldGlobalProperties, gson);
    }

    private void importFilesIfNeccesary(File modConfigDirectoryFile) {
        if (importName().trim().equals("")) {
            return;
        }
        File worldFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR
                + getFolderConfiguration().saveName);
        File importFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + importName());
        if (worldFolder.exists() || !importFolder.exists()) {
            return;
        }
        FileUtilities.copy(importFolder, worldFolder);
    }
}
