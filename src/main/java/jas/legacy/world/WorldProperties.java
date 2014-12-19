package jas.legacy.world;

import jas.legacy.world.FolderConfiguration.WorldStats;
import jas.modern.DefaultProps;
import jas.modern.FileUtilities;
import jas.modern.GsonHelper;

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
        folderConfiguration = GsonHelper.readOrCreateFromGson(
                FileUtilities.createReader(FolderConfiguration.getFile(configDirectory), false),
                FolderConfiguration.class, gson);
        folderConfiguration.getCurrentWorldStats(world.getWorldInfo().getWorldName());
        
        savedConfguration = GsonHelper.readOrCreateFromGson(
                FileUtilities.createReader(
                        SavedFolderConfiguration.getFile(configDirectory, getFolderConfiguration().saveName), false),
                SavedFolderConfiguration.class, gson);

        worldGlobalProperties = GsonHelper.readOrCreateFromGson(
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
}
