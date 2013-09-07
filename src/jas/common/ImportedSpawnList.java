package jas.common;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Used to Temporarily Store Vanilla Spawn List Information before Clearin it
 * 
 * Information is stored so that JAS can read defaults Vanilla Information is cleared so that it does not spawn anything
 */
public class ImportedSpawnList {

    private SpawnList[] spawnLists = new SpawnList[BiomeGenBase.biomeList.length];

    private static class SpawnList {
        public Multimap<EnumCreatureType, SpawnListEntry> spawnLists = ArrayListMultimap.create();
    }

    @SuppressWarnings("unchecked")
    public ImportedSpawnList(BiomeBlacklist blacklist, boolean clearVanilla) {
        JASLog.info("Importing ".concat(clearVanilla ? "and clearing " : "").concat("vanilla spawn lists."));
        for (int i = 0; i < BiomeGenBase.biomeList.length; i++) {
            BiomeGenBase biome = BiomeGenBase.biomeList[i];
            if (biome == null) {
                continue;
            }
            spawnLists[i] = new SpawnList();
            for (EnumCreatureType type : EnumCreatureType.values()) {
                if (biome.getSpawnableList(type) != null) {
                    spawnLists[i].spawnLists.get(type).addAll(biome.getSpawnableList(type));
                    if (clearVanilla && !blacklist.isBlacklisted(biome)) {
                        biome.getSpawnableList(type).clear();
                    }
                }
            }
        }
    }

    public void clearImportedSpawnList() {
        spawnLists = new SpawnList[BiomeGenBase.biomeList.length];
    }

    public Collection<SpawnListEntry> getSpawnableCreatureList(BiomeGenBase biome, EnumCreatureType creatureType) {
        if (spawnLists[biome.biomeID] != null) {
            return spawnLists[biome.biomeID].spawnLists.get(creatureType);
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<SpawnListEntry> getSpawnableCreatureList(int biomeID, EnumCreatureType creatureType) {
        if (spawnLists[biomeID] != null) {
            return spawnLists[biomeID].spawnLists.get(creatureType);
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<SpawnListEntry> getSpawnableCreatureList(BiomeGenBase biome) {
        if (spawnLists[biome.biomeID] != null) {
            return spawnLists[biome.biomeID].spawnLists.values();
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<SpawnListEntry> getSpawnableCreatureList(int biomeID) {
        if (spawnLists[biomeID] != null) {
            return spawnLists[biomeID].spawnLists.values();
        } else {
            return Collections.emptyList();
        }
    }
}
