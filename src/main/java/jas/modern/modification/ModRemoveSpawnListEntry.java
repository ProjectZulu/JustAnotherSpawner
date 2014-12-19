package jas.modern.modification;

import jas.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.modern.spawner.creature.entry.SpawnListEntryBuilder;

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