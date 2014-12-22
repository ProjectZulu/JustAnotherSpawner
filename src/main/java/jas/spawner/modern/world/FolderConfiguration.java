package jas.spawner.modern.world;

import jas.common.JustAnotherSpawner;
import jas.spawner.modern.DefaultProps;

import java.io.File;
import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

public class FolderConfiguration {
    public final String IMPORTANT = "# Folder names are case sensitive if OS allows it. Beware invalid OS characters.";
    @SerializedName("FILE_VERSION")
    public final String fileVersion = "1.0";
    @SerializedName("DefaultProfile")
    private WorldStats defaultStats;
    @SerializedName("WorldProfiles")
    private HashMap<String, WorldStats> worldNameToStats;
    private transient WorldStats currentStats;

    public FolderConfiguration() {
        defaultStats = new WorldStats();
        worldNameToStats = new HashMap<String, WorldStats>();
    }

    public static class WorldStats {
        /* Marks the folders should be saved */
        @SerializedName("Save_Folder_Name")
        public final String saveName;
        /* Marks how the Entity CFG settings should be saved */
        @SerializedName("Use_Universal_Entity_CFG")
        public final Boolean universalDirectory;
        /* Marks how the Entity CFG settings should be saved */
        @SerializedName("Sort_Creature_By_Biome")
        public final Boolean sortCreatureByBiome;

        public WorldStats() {
            this("DEFAULT");
        }

        public WorldStats(String saveName) {
            this.saveName = saveName;
            this.universalDirectory = false;
            this.sortCreatureByBiome = JustAnotherSpawner.globalSettings().globalSortCreatureByBiome;
        }
    }

    public WorldStats getCurrentWorldStats(String curWorldName) {
        if (currentStats != null) {
            return currentStats;
        }
        WorldStats stats = worldNameToStats.get(curWorldName);
        if (stats == null) {
            return currentStats = defaultStats;
        }
        return currentStats = stats;
    }

    public static File getFile(File configDirectory) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + "SaveConfigGson.cfg");
    }
}
