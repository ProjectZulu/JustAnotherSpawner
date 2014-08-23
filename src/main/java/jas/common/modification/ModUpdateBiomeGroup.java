package jas.common.modification;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Optional;

import jas.common.ImportedSpawnList;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.entry.SpawnListEntryBuilder;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class ModUpdateBiomeGroup extends BaseModification {
	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;
	private String prevBiomeGroupId;

	public ModUpdateBiomeGroup(String groupName, ArrayList<String> contents) {
		this(groupName, groupName, null, contents);
	}

	public ModUpdateBiomeGroup(String prevBiomeGroupId, String groupName, String configName, ArrayList<String> contents) {
		this.prevBiomeGroupId = prevBiomeGroupId;
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.updateBiomeGroup(prevBiomeGroupId, groupName, configName.get(), contents);
		} else {
			registry.updateBiomeGroup(prevBiomeGroupId, groupName, contents);
		}
	}
}
