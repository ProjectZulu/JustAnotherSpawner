package jas.legacy.world;

import jas.modern.DefaultProps;

import java.io.File;

import com.google.gson.annotations.SerializedName;

public class WorldGlobalSettings {
    @SerializedName("FILE_VERSION")
    public final String fileVersion;
    @SerializedName("Min_Despawn_Distance")
    public final int despawnDist;
    @SerializedName("Instant_Despawn_Distance")
    public final int maxDespawnDist;
    @SerializedName("Min_Despawn_Time")
    public final int minDespawnTime;

    public WorldGlobalSettings() {
        fileVersion = "1.0";
        maxDespawnDist = 128;
        despawnDist = 32;
        minDespawnTime = 600;
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/WorldGlobalProperties.cfg");
    }
}
