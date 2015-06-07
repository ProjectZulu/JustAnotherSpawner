package jas.spawner.refactor.structure;

import jas.api.StructureInterpreter;
import jas.common.JASLog;
import jas.common.helper.VanillaHelper;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.World;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

public class StructureHandlerBuilder {
	public final StructureInterpreter interpreter;

	private Set<String> structureKeys;
	private SetMultimap<String, SpawnListEntry> structureKeysToSpawnList;
	/* Tracks valid entries that are currently ignored (weight ==0 or type==NONE) but need to be kept in the config */
	private SetMultimap<String, SpawnListEntry> structureKeysToDisabledpawnList;

	public StructureHandlerBuilder(StructureInterpreter interpreter) {
		this.interpreter = interpreter;
		this.structureKeys = new HashSet<String>(interpreter.getStructureKeys());
		this.structureKeysToSpawnList = HashMultimap.create();
		this.structureKeysToDisabledpawnList = HashMultimap.create();
	}

	public StructureHandlerBuilder(StructureHandler handler) {
		this.interpreter = handler.interpreter;
		this.structureKeys = new HashSet<String>(handler.structureKeys);
		this.structureKeysToSpawnList = HashMultimap.create();
		this.structureKeysToSpawnList.putAll(handler.structureKeysToSpawnList);
		this.structureKeysToDisabledpawnList = HashMultimap.create();
		this.structureKeysToDisabledpawnList.putAll(handler.structureKeysToDisabledpawnList);
	}

	public static class StructureHandler {
		public final StructureInterpreter interpreter;
		public ImmutableSet<String> structureKeys;
		public ImmutableSetMultimap<String, SpawnListEntry> structureKeysToSpawnList;
		/* Tracks valid entries that are currently ignore (weight ==0 or type==NONE) but need to be kept in the config */
		public ImmutableSetMultimap<String, SpawnListEntry> structureKeysToDisabledpawnList;

		public StructureHandler(StructureHandlerBuilder buidler) {
			this.interpreter = buidler.interpreter;
			this.structureKeys = ImmutableSet.<String> builder().addAll(buidler.structureKeys).build();
			this.structureKeysToSpawnList = ImmutableSetMultimap.<String, SpawnListEntry> builder()
					.putAll(buidler.structureKeysToSpawnList).build();
			this.structureKeysToDisabledpawnList = ImmutableSetMultimap.<String, SpawnListEntry> builder()
					.putAll(buidler.structureKeysToDisabledpawnList).build();
		}

		/**
		 * Gets the spawnList for the worlds coordinates provided.
		 * 
		 * @return Collection of JAS SpawnListEntries that should be spawn. Return Empty list if none.
		 */
		public Collection<SpawnListEntry> getStructureSpawnList(World world, int xCoord, int yCoord, int zCoord) {
			Optional<String> structureKey = getStructureAt(world, xCoord, yCoord, zCoord);
			if (structureKey.isPresent()) {
				return structureKeysToSpawnList.get(structureKey.get());
			}
			return Collections.emptyList();
		}

		public Optional<String> getStructureAt(World world, int xCoord, int yCoord, int zCoord) {
			String structureKey = interpreter.areCoordsStructure(world, xCoord, yCoord, zCoord);
			return structureKey == null ? Optional.of(structureKey) : Optional.<String> absent();
		}

		public boolean doesHandlerApply(World world, int xCoord, int yCoord, int zCoord) {
			return interpreter.shouldUseHandler(world, VanillaHelper.getBiomeForCoords(world, xCoord, zCoord));
		}

		public Set<String> getStructureKeys() {
			return structureKeys;
		}
	}

	public StructureHandler build() {
		return new StructureHandler(this);
	}

	public StructureHandlerBuilder putStuctureSpawnList(String structureKey, Collection<SpawnListEntry> spawnList) {
		this.structureKeysToSpawnList.removeAll(structureKey);
		this.structureKeysToDisabledpawnList.removeAll(structureKey);
		for (SpawnListEntry spawnListEntry : spawnList) {
			addSpawnList(structureKey, spawnListEntry);
		}
		return this;
	}

	public StructureHandlerBuilder setStructureKeysToDisabledpawnList(
			ImmutableSetMultimap<String, SpawnListEntry> structureKeysToDisabledpawnList) {
		this.structureKeysToDisabledpawnList = HashMultimap.create();
		if (structureKeysToDisabledpawnList != null) {
			this.structureKeysToDisabledpawnList.putAll(structureKeysToDisabledpawnList);
		}
		return this;
	}

	public StructureHandlerBuilder addSpawnList(String structureKey, SpawnListEntry spawnListEntry) {
		if (spawnListEntry.weight > 0) {
			JASLog.log().logSpawnListEntry(spawnListEntry.entityContents, "Structure: " + structureKey, true,
					"of type " + spawnListEntry.livingTypeID);
			structureKeysToSpawnList.put(structureKey, spawnListEntry);
		} else {
			JASLog.log().logSpawnListEntry(spawnListEntry.entityContents, "Structure: " + structureKey, false,
					String.format("due to Weight %s", spawnListEntry.weight));
			structureKeysToDisabledpawnList.put(structureKey, spawnListEntry);
		}
		return this;
	}

	public Set<String> getStructureKeys() {
		return structureKeys;
	}
}
