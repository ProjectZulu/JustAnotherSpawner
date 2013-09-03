package jas.common;

import jas.common.spawner.biome.group.BiomeHelper;

import java.io.File;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;

public class BiomeBlacklist {

    private boolean[] blacklist;

    public BiomeBlacklist(File configDirectory) {
        Configuration config = new Configuration(new File(configDirectory, DefaultProps.GLOBALSETTINGSDIR
                + "GlobalProperties.cfg"));
        config.load();
        blacklist = new boolean[BiomeGenBase.biomeList.length];
        for (int biomeID = 0; biomeID < blacklist.length; biomeID++) {
            BiomeGenBase biome = BiomeGenBase.biomeList[biomeID];
            if (biome != null
                    && config.get("properties.biomeblacklist", BiomeHelper.getPackageName(biome), false).getBoolean(
                            false)) {
                blacklist[biomeID] = true;
            } else {
                blacklist[biomeID] = false;
            }
        }
        config.save();
    }

    public boolean isBlacklisted(BiomeGenBase biome) {
        if (biome == null || biome.biomeID < 0 || biome.biomeID >= blacklist.length) {
            return false;
        }
        return blacklist[biome.biomeID];
    }
}
