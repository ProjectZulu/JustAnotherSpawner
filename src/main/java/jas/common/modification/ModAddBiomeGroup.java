package jas.common.modification;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.base.Optional;

public class ModAddBiomeGroup extends BaseModification {

	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;

	public ModAddBiomeGroup(String groupName, ArrayList<String> contents) {
		this(groupName, null, contents);
	}

	public ModAddBiomeGroup(String groupName, String configName, ArrayList<String> contents) {
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.addBiomeGroup(groupName, configName.get(), contents);
		} else {
			registry.addBiomeGroup(groupName, contents);
		}
	}
}
