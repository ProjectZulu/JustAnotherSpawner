package jas.common.modification;

import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;

import java.util.ArrayList;

import com.google.common.base.Optional;

public class ModAddLivingGroup extends BaseModification {

	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;

	public ModAddLivingGroup(String groupName, ArrayList<String> contents) {
		this(groupName, null, contents);
	}

	public ModAddLivingGroup(String groupName, String configName, ArrayList<String> contents) {
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(LivingGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.addLivingGroup(new LivingGroup(groupName, configName.get(), contents));
		} else {
			registry.addLivingGroup(new LivingGroup(groupName, contents));
		}
	}
}
