package jas.common.global;

import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.DefaultProps;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;

import java.io.File;
import java.util.TreeMap;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.gson.Gson;

public class BiomeBlacklist {
    public final String FILE_VERSION = "1.0";
    private boolean[] blacklist;

    public BiomeBlacklist(File configDirectory) {
        Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { BiomeBlacklistSaveObject.class },
                new Object[] { new BiomeBlacklistSaveObject.BlacklistSerializer() });

        File blackListFile = new File(configDirectory, DefaultProps.GLOBALSETTINGSDIR + "BiomeBlacklist.cfg");
        /* Read Blacklist */
        {
            BiomeBlacklistSaveObject optBlackList = GsonHelper.readOrCreateFromGson(
                    FileUtilities.createReader(blackListFile, false), BiomeBlacklistSaveObject.class, gson);

            /* Create Numeric Blacklist */
            TreeMap<String, Boolean> namedBlacklist = optBlackList.getBlacklist();
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
            BiomeBlacklistSaveObject saveObject = new BiomeBlacklistSaveObject(blacklist);
            GsonHelper.writeToGson(FileUtilities.createWriter(blackListFile, true), saveObject, gson);
        }
    }

    public boolean isBlacklisted(BiomeGenBase biome) {
        if (biome == null || biome.biomeID < 0 || biome.biomeID >= blacklist.length) {
            return false;
        }
        return blacklist[biome.biomeID];
    }
}
