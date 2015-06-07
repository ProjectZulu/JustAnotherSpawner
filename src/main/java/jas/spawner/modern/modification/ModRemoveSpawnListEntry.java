package jas.spawner.modern.modification;

import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;

public class ModRemoveSpawnListEntry extends BaseModification {

	private String livingGroupId;
	private String biomeGroupId;

	public ModRemoveSpawnListEntry(String livingGroupId, String biomeGroupId) {
		this.livingGroupId = livingGroupId;
		this.biomeGroupId = biomeGroupId;
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		registry.removeSpawnListEntry(livingGroupId, biomeGroupId);
	}
}