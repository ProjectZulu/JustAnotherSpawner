package jas.common;

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
        maxDespawnDist = 32;
        despawnDist = 128;
        minDespawnTime = 600;
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/WorldGlobalProperties.cfg");
    }
}
