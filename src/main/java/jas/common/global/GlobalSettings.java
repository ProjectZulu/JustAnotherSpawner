package jas.common.global;

import com.google.gson.annotations.SerializedName;

public class GlobalSettings {
	public static transient String profileMVEL = "MVEL";
	public static transient String profileTAGS = "BASIC";

	public String FILE_VERSION = "1.0";
    @SerializedName("___GLOBAL SETTINGS___")
    public final String GLOBAL_COMMENT = "These are Global properties to set their World-Specific counterparts";
//    @SerializedName("Spawning Profile")
//    public String spawningProfile = profileMVEL;
    @SerializedName("Sort entities by biome")
    public boolean globalSortCreatureByBiome = true;

    @SerializedName("___VANILLA COMPATABILITY___")
    public final String VANILLA_COMMENT = "These options are used to disable the vanilla spawning system in a friendly way";
    @SerializedName("Turn gamerule spawning off on start")
    public boolean turnGameruleSpawningOff;
    @SerializedName("Empty vanilla spawnlists")
    public boolean emptyVanillaSpawnLists;
    @SerializedName("Disable Vanilla Chunk Spawning")
    public boolean disabledVanillaChunkSpawning;

    @SerializedName("___SPAWNER SETTINGS___")
    public final String SPAWNING_COMMENT = "These options set properties of the spawner";
    @SerializedName("Spawner Tick Spacing")
    public int spawnerTickSpacing = 0;
    @SerializedName("Distance (in Chunks) to perform entity spawning")
    public int chunkSpawnDistance = 8;
    @SerializedName("Distance (in Chunks) to perform entity counting")
    public int chunkCountDistance = 8;
    @SerializedName("Generate Zero-Weight Spawn Entries")
    public boolean shouldGenerateZeroSpawnEntries;
    
    @SerializedName("___IMC SETTINGS___")
    public final String IMC_COMMENT = "These options are for enabling inter-mod communications. You only need to enable these if another mod instructs you to do so.";
    @SerializedName("Enable 'AddEligibleChunkForSpawning' Event")
    public boolean enableEventAddEligibleChunkForSpawning;
    @SerializedName("Enable 'StartSpawnCreaturesInChunks' Event")
    public boolean enableEventStartSpawnCreaturesInChunks;
    @SerializedName("Add a 'IS_JAS_SPAWNED' boolean NBT tag on all entites spawned with JAS")
    public boolean enableIsJasSpawnedEntityDataTag;
    
    
    public GlobalSettings() {
        spawnerTickSpacing = 0;
        globalSortCreatureByBiome = true;
        shouldGenerateZeroSpawnEntries = true;
        turnGameruleSpawningOff = false;
        emptyVanillaSpawnLists = true;
        disabledVanillaChunkSpawning = true;
        chunkSpawnDistance = 8;
        chunkCountDistance = 8;
        enableEventAddEligibleChunkForSpawning = false;
        enableEventStartSpawnCreaturesInChunks = false;
        enableIsJasSpawnedEntityDataTag = false;
//        spawningProfile = profileMVEL;
    }
}