package jas.spawner.refactor.biome.list;

import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.SpawnSettings.BiomeSettings;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;

import java.util.Collection;
import java.util.HashSet;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table.Cell;

public class BiomeSpawnList {
	private ImmutableListMultimap<String, SpawnListEntry> locMappingToSLE;

	// Stored in Memory for QuickAccesss Time: {LocMapping, LivingTypeID} -> Collection<SpawnListEntry>
	private ImmutableTable<String, String, ImmutableCollection<SpawnListEntry>> spawnTable;

	// {JAS_ENTITY_NAME Collection(SpawnListEntries_ThatEntityIsContainedIn }
	private ImmutableMultimap<String, SpawnListEntry> spawnLookupByEntity;

	public Multimap<String, SpawnListEntry> locMappingToSLE() {
		return locMappingToSLE;
	}

	public ImmutableMultimap<String, SpawnListEntry> spawnLookupByEntity() {
		return spawnLookupByEntity;
	}

	public BiomeSpawnList(Collection<SpawnListEntry> spawnableEntries) {
		loadFromConfig(spawnableEntries);
	}

	public ImmutableCollection<SpawnListEntry> getSpawnList(World world, BiomeSettings biomeSettings,
			BiomeGenBase biome, LivingType livingType) {
		String jasName = biomeSettings.biomeMappings().keyToMapping().get(BiomeHelper.getPackageName(biome));
		ImmutableCollection<SpawnListEntry> spawnList = spawnTable.get(jasName, livingType.livingTypeID);
		return spawnList;
	}
	
	public void loadFromConfig(Collection<SpawnListEntry> mapsBuilder) {
		Builder<String, SpawnListEntry> reverseLocMapping = ImmutableListMultimap.<String, SpawnListEntry> builder();
		HashMultimap<String, SpawnListEntry> entNameToSLE = HashMultimap.<String, SpawnListEntry> create();

		HashBasedTable<String, String, Collection<SpawnListEntry>> spawnTabeleBuilder = HashBasedTable
				.<String, String, Collection<SpawnListEntry>> create();
		for (SpawnListEntry entry : mapsBuilder) {
			for (String entityMapping : entry.entityMappings) {
				entNameToSLE.put(entityMapping, entry);
			}
			for (String locMapping : entry.locMappings) {
				reverseLocMapping.put(locMapping, entry);
			}
			for (String locMapping : entry.locMappings) {
				Collection<SpawnListEntry> set = spawnTabeleBuilder.get(locMapping, entry.livingTypeIDs);
				if (set == null) {
					set = new HashSet<SpawnListEntryBuilder.SpawnListEntry>();
					for (String livingTypeID : entry.livingTypeIDs) {
						spawnTabeleBuilder.put(locMapping, livingTypeID, set);
					}
				}
				set.add(entry);
			}
		}
		
		ImmutableTable.Builder<String, String, ImmutableCollection<SpawnListEntry>> immutableMapBuilder = ImmutableTable
				.<String, String, ImmutableCollection<SpawnListEntry>> builder();
		for (Cell<String, String, Collection<SpawnListEntry>> spawnListEntry : spawnTabeleBuilder.cellSet()) {
			immutableMapBuilder.put(spawnListEntry.getRowKey(), spawnListEntry.getColumnKey(), ImmutableList
					.<SpawnListEntry> builder().addAll(spawnListEntry.getValue()).build());
		}
		spawnTable = immutableMapBuilder.build();
		
		locMappingToSLE = reverseLocMapping.build();
		spawnLookupByEntity = ImmutableMultimap.<String, SpawnListEntry> builder().putAll(entNameToSLE).build();
	}
}
