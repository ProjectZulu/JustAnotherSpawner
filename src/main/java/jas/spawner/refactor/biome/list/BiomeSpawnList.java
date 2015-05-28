package jas.spawner.refactor.biome.list;

import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;

public class BiomeSpawnList {
	private ImmutableListMultimap<String, SpawnListEntry> locMappingToSLE;
	// Stored in Memory for QuickAccesss Time: {LocMapping, LivingTypeID} -> Collection<SpawnListEntry>
	private ImmutableTable<String, String, Collection<SpawnListEntry>> spawnTable;

	public Multimap<String, SpawnListEntry> locMappingToSLE() {
		return locMappingToSLE;
	}

	public BiomeSpawnList(Collection<SpawnListEntry> spawnableEntries) {
		loadFromConfig(spawnableEntries);
	}

	public void loadFromConfig(Collection<SpawnListEntry> mapsBuilder) {
		Builder<String, SpawnListEntry> reverseMapping = ImmutableListMultimap.<String, SpawnListEntry> builder();
		HashBasedTable<String, String, Collection<SpawnListEntry>> spawnTabeleBuilder = HashBasedTable
				.<String, String, Collection<SpawnListEntry>> create();
		for (SpawnListEntry entry : mapsBuilder) {
			for (String locMapping : entry.locMappings) {
				reverseMapping.put(locMapping, entry);
			}
			for (String locMapping : entry.locMappings) {
				Collection<SpawnListEntry> set = spawnTabeleBuilder.get(locMapping, entry.livingTypeID);
				if (set == null) {
					set = new HashSet<SpawnListEntryBuilder.SpawnListEntry>();
					spawnTabeleBuilder.put(locMapping, entry.livingTypeID, set);
				}
				set.add(entry);
			}
		}
		spawnTable = ImmutableTable.<String, String, Collection<SpawnListEntry>> builder().putAll(spawnTabeleBuilder)
				.build();
		locMappingToSLE = reverseMapping.build();
	}
}
