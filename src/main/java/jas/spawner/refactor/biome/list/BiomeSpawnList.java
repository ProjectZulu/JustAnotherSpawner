package jas.spawner.refactor.biome.list;

import jas.spawner.refactor.biome.BiomeAttributes;
import jas.spawner.refactor.biome.BiomeDictionaryGroups;
import jas.spawner.refactor.biome.BiomeGroups;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configloader.BiomeSpawnListLoader;
import jas.spawner.refactor.configloader.ConfigLoader;
import jas.spawner.refactor.configloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.Group.ReversibleGroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;

public class BiomeSpawnList implements ReversibleGroups {
	private ImmutableMap<String, SpawnListEntry> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;

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
		for (SpawnListEntry entry : mapsBuilder.iDToGroup().values()) {
			for (String mapping : entry.results()) {
				reverseMapping.put(mapping, entry.iD());
			}
		}
		mappingToGroupID = reverseMapping.build();
		iDToGroup = mapsBuilder.build();
	}

	public List<SpawnListEntry> getSpawnListEntries(String mapping, String livingType) {
		List<SpawnListEntry> entries = new ArrayList<SpawnListEntry>();
		for (String groupID : mappingToGroupID.get(mapping)) {
			SpawnListEntry entry = iDToGroup.get(groupID);
			if (livingType.equals(entry.livingTypeID) && entry.weight.isPresent()) {
				entries.add(entry);
			}
		}
		return entries;
	}
}
