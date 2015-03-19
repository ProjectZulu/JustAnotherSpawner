package jas.spawner.refactor.biome.list;

import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.entities.Group.ReversibleGroups;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;

public class BiomeSpawnList implements ReversibleGroups {
	private ImmutableMap<String, SpawnListEntry> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;
	// Stored in Memory for QuickAccesss Time: {BiomeMapping, LivingTypeID} -> Collection<SpawnListEntry>
	private ImmutableTable<String, String, Collection<SpawnListEntry>> spawnTable;
	/**
	 * Contains Mapping between B.Mapping/StructureID, LivingType to valid SpawnListEntry
	 * 
	 * Used to create constant access time
	 */
	private transient ImmutableTable<String, String, Set<SpawnListEntry>> validSpawnListEntries;

	public final static String key = "L|";

	@Override
	public String key() {
		return key;
	}

	@Override
	public Map<String, SpawnListEntry> iDToGroup() {
		return iDToGroup;
	}

	@Override
	public Multimap<String, String> mappingToID() {
		return mappingToGroupID;
	}

	public BiomeSpawnList(ImmutableMapGroupsBuilder<SpawnListEntry> mapsBuilder) {
		loadFromConfig(mapsBuilder);
	}

	public void loadFromConfig(ImmutableMapGroupsBuilder<SpawnListEntry> mapsBuilder) {
		Builder<String, String> reverseMapping = ImmutableListMultimap.<String, String> builder();
		HashBasedTable<String, String, Collection<SpawnListEntry>> spawnTabeleBuilder = HashBasedTable
				.<String, String, Collection<SpawnListEntry>> create();
		for (SpawnListEntry entry : mapsBuilder.iDToGroup().values()) {
			for (String mapping : entry.results()) {
				reverseMapping.put(mapping, entry.iD());
			}
			for (String biomeMapping : entry.results()) {
				Collection<SpawnListEntry> set = spawnTabeleBuilder.get(biomeMapping, entry.livingTypeID);
				if (set == null) {
					set = new HashSet<SpawnListEntryBuilder.SpawnListEntry>();
					spawnTabeleBuilder.put(biomeMapping, entry.livingTypeID, set);
				}
				set.add(entry);
			}
		}
		spawnTable = ImmutableTable.<String, String, Collection<SpawnListEntry>> builder().putAll(spawnTabeleBuilder)
				.build();
		mappingToGroupID = reverseMapping.build();
		iDToGroup = mapsBuilder.build();
	}
}
