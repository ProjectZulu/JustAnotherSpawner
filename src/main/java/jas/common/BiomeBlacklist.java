package jas.common;

import jas.common.spawner.biome.group.BiomeHelper;

import java.io.File;
import java.lang.reflect.Type;
import java.util.TreeMap;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BiomeBlacklist {

    private boolean[] blacklist;

    public BiomeBlacklist(File configDirectory) {
        Type blacklistType = new TypeToken<TreeMap<String, Boolean>>() {
        }.getType();

        Gson gson = GsonHelper.createGson(true);
        File blackListFile = new File(configDirectory, DefaultProps.GLOBALSETTINGSDIR + "BiomeBlacklist.cfg");
        /* Read Blacklist */
        {
            Optional<TreeMap<String, Boolean>> optBlackList = GsonHelper.readFromGson(
                    FileUtilities.createReader(blackListFile, false), blacklistType, gson);
            if (!optBlackList.isPresent()) {
                optBlackList = Optional.of(new TreeMap<String, Boolean>());
            }

            /* Create Numeric Blacklist */
            TreeMap<String, Boolean> namedBlacklist = optBlackList.get();
            blacklist = new boolean[BiomeGenBase.getBiomeGenArray().length];
            for (int biomeID = 0; biomeID < blacklist.length; biomeID++) {
                BiomeGenBase biome = BiomeGenBase.getBiomeGenArray()[biomeID];
                if (biome == null) {
                    blacklist[biomeID] = false;
                    continue;
                }
                Boolean isBlacklisted = namedBlacklist.get(BiomeHelper.getPackageName(biome));
                blacklist[biomeID] = isBlacklisted != null ? isBlacklisted : false;
            }
        }
        {
            /* Write Blacklist */
            TreeMap<String, Boolean> namedBlacklist = new TreeMap<String, Boolean>();
            for (int biomeID = 0; biomeID < blacklist.length; biomeID++) {
                BiomeGenBase biome = BiomeGenBase.getBiomeGenArray()[biomeID];
                if (biome == null) {
                    continue;
                }
                namedBlacklist.put(BiomeHelper.getPackageName(biome), blacklist[biomeID]);
            }
            GsonHelper
                    .writeToGson(FileUtilities.createWriter(blackListFile, true), namedBlacklist, blacklistType, gson);
        }
    }

    public boolean isBlacklisted(BiomeGenBase biome) {
        if (biome == null || biome.biomeID < 0 || biome.biomeID >= blacklist.length) {
            return false;
        }
        return blacklist[biome.biomeID];
    }
}
