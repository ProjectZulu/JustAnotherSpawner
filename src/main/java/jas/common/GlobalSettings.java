package jas.common;

import com.google.gson.annotations.SerializedName;

public class GlobalSettings {
    public String FILE_VERSION = "1.0";
    @SerializedName("___VANILLA COMPATABILITY___")
    public final String VANILLA_COMMENT = "These options are used to disable the vanilla spawning system in a friendly way";
    @SerializedName("Turn gamerule spawning off on start")
    public boolean turnGameruleSpawningOff = false;
    @SerializedName("Empty vanilla spawnlists")
    public boolean emptyVanillaSpawnLists = true;

    @SerializedName("___SPAWNER SETTINGS___")
    public final String SPAWNING_COMMENT = "These options set properties of the spawner";
    @SerializedName("Spawner Tick Spacing")
    public int spawnerTickSpacing = 0;
    @SerializedName("Distance (in Chunks) to perform entity spawning")
    public int chunkSpawnDistance = 8;
    @SerializedName("Distance (in Chunks) to perform entity counting")
    public int chunkCountDistance = 8;

    @SerializedName("___GLOBAL SETTINGS___")
    public final String GLOBAL_COMMENT = "These are Global properties to set their World-Specific counterparts";
    @SerializedName("Sort entities by biome")
    public boolean globalSortCreatureByBiome = true;

    public GlobalSettings() {
        spawnerTickSpacing = 0;
        globalSortCreatureByBiome = true;
        turnGameruleSpawningOff = false;
        emptyVanillaSpawnLists = true;
        chunkSpawnDistance = 8;
        chunkCountDistance = 8;
    }
}