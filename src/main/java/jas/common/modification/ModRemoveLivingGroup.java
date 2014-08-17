package jas.common.modification;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;

public class ModRemoveLivingGroup extends BaseModification {

	public final String groupName;

	public ModRemoveLivingGroup(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public void applyModification(LivingGroupRegistry registry) {
		registry.removeLivingGroup(groupName);
	}
}
