package jas.common.modification;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;

public class ModRemoveBiomeGroup extends BaseModification {

	public final String groupName;

	public ModRemoveBiomeGroup(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		registry.removeBiomeGroup(groupName);
	}
}
