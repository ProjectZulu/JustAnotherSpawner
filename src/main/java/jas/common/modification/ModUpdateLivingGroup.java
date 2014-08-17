package jas.common.modification;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;

import java.util.ArrayList;

import com.google.common.base.Optional;

public class ModUpdateLivingGroup extends BaseModification {
	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;

	public ModUpdateLivingGroup(String groupName, ArrayList<String> contents) {
		this(groupName, null, contents);
	}

	public ModUpdateLivingGroup(String groupName, String configName, ArrayList<String> contents) {
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(LivingGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.updateLivingGroup(new LivingGroup(groupName, configName.get(), contents));
		} else {
			registry.updateLivingGroup(new LivingGroup(groupName, contents));
		}
	}
}
