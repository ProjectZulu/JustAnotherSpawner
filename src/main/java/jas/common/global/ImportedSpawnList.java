package jas.common.global;


import jas.common.JASLog;
import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.DefaultProps;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.Gson;

/**
 * Used to Temporarily Store Vanilla Spawn List Information before Clearin it
 * 
 * Information is stored so that JAS can read defaults Vanilla Information is cleared so that it does not spawn anything
 */
public class ImportedSpawnList {

    private SpawnList[] spawnLists = new SpawnList[BiomeGenBase.getBiomeGenArray().length];

    private static class SpawnList {
        public Multimap<EnumCreatureType, SpawnListEntry> spawnLists = ArrayListMultimap.create();
    }

    @SuppressWarnings("unchecked")
    public ImportedSpawnList(BiomeBlacklist blacklist, boolean clearVanilla) {
        JASLog.log().info("Importing ".concat(clearVanilla ? "and clearing " : "").concat("vanilla spawn lists."));
        for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++) {
            BiomeGenBase biome = BiomeGenBase.getBiomeGenArray()[i];
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
    
	public void exportImportedSpawnlistToFile(File configDirectory) {
		TreeMap<String, TreeMap<String, Collection<String>>> importedSpawnLists = new TreeMap<String, TreeMap<String, Collection<String>>>();
		for (int i = 0; i < spawnLists.length; i++) {
			SpawnList spawnList = spawnLists[i];
			if (spawnList != null) {
				TreeMap<String, Collection<String>> typeTpEntry = importedSpawnLists.get(BiomeHelper
						.getPackageName(BiomeGenBase.getBiomeGenArray()[i]));
				if (typeTpEntry == null) {
					typeTpEntry = new TreeMap<String, Collection<String>>();
					importedSpawnLists.put(BiomeHelper.getPackageName(BiomeGenBase.getBiomeGenArray()[i]), typeTpEntry);
				}
				for (EnumCreatureType creatureType : spawnList.spawnLists.keySet()) {
					Collection<String> entries = typeTpEntry.get(creatureType.toString());
					if (entries == null) {
						entries = new ArrayList<String>();
						typeTpEntry.put(creatureType.toString(), entries);
					}
					for (SpawnListEntry spawnEntry : spawnList.spawnLists.get(creatureType)) {
						String value = spawnEntry.entityClass.getName() + ": [" + spawnEntry.itemWeight + ", "
								+ spawnEntry.minGroupCount + "->" + spawnEntry.maxGroupCount + "]";
						entries.add(value);
					}
				}
			}
		}

		Gson gson = GsonHelper.createGson(true);
		File importedSettingsFile = new File(configDirectory, DefaultProps.MODDIR + "ImportedSpawnlists.cfg");
		GsonHelper.writeToGson(FileUtilities.createWriter(importedSettingsFile, true), importedSpawnLists, gson);
	}
}
