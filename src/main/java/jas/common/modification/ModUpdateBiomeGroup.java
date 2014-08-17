package jas.common.modification;

import java.util.ArrayList;

import com.google.common.base.Optional;

import jas.common.spawner.biome.group.BiomeGroupRegistry;

public class ModUpdateBiomeGroup extends BaseModification {
	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;

	public ModUpdateBiomeGroup(String groupName, ArrayList<String> contents) {
		this(groupName, null, contents);
	}

	public ModUpdateBiomeGroup(String groupName, String configName, ArrayList<String> contents) {
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.updateBiomeGroup(groupName, configName.get(), contents);
		} else {
			registry.updateBiomeGroup(groupName, contents);
		}
	}
}
