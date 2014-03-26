package jas.common;

import java.io.File;

import com.google.gson.annotations.SerializedName;

public class WorldGlobalSettings {
    @SerializedName("Min_Despawn_Distance")
    public final int despawnDist;
    @SerializedName("Instant_Despawn_Distance")
    public final int maxDespawnDist;
    @SerializedName("Min_Despawn_Time")
    public final int minDespawnTime;

    public WorldGlobalSettings() {
        maxDespawnDist = 32;
        despawnDist = 128;
        minDespawnTime = 600;
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/WorldGlobalProperties.cfg");
    }
}
