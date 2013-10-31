package jas.common.spawner.biome;

import jas.common.WorldProperties;
import jas.common.config.BiomeCapConfiguration;
import jas.common.spawner.biome.group.BiomeHelper;

import java.io.File;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class BiomeCaps {

    /* Package name to Cap */
    private Integer[] biomeCaps;
    private boolean isCapEnabled = false;
    public static final int DEFAULT_CAP = 20;

    private WorldProperties worldProperties;

    public BiomeCaps(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    public void loadFromConfig(File configDirectory) {
        BiomeCapConfiguration config = new BiomeCapConfiguration(configDirectory, worldProperties);
        config.load();
        isCapEnabled = config.getIsEnabled(false).getBoolean(false);
        Integer[] caps = new Integer[BiomeGenBase.biomeList.length];
        for (int i = 0; i < caps.length; i++) {
            BiomeGenBase biome = BiomeGenBase.biomeList[i];
            if (biome == null) {
                continue;
            }
            caps[i] = config.getCap(BiomeHelper.getPackageName(biome), DEFAULT_CAP).getInt(DEFAULT_CAP);
        }
        config.save();
        biomeCaps = caps;
    }

    public void saveToConfig(File configDirectory) {
        BiomeCapConfiguration config = new BiomeCapConfiguration(configDirectory, worldProperties);
        config.load();
        config.getIsEnabled(isCapEnabled).set(isCapEnabled);
        for (int i = 0; i < biomeCaps.length; i++) {
            BiomeGenBase biome = BiomeGenBase.biomeList[i];
            if (biome == null) {
                continue;
            }
            config.getCap(BiomeHelper.getPackageName(biome), biomeCaps[i]).set(biomeCaps[i]);
        }
        config.save();
    }

    public int getChunkCap(Chunk chunk) {
        if (chunk == null || !isCapEnabled) {
            return -1;
        }
        int chunkCap = 0;
        int counter = 0;
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; i++) {
            int biomeID = biomeArray[i] & 255;
            Integer columnCap = biomeCaps[biomeID];
            if (columnCap != null) {
                chunkCap += columnCap;
                counter++;
            }
        }
        return counter > 0 ? chunkCap / counter : -1;
    }
}
