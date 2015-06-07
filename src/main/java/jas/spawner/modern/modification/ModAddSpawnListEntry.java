package jas.spawner.modern.modification;

import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.entry.SpawnListEntryBuilder;

public class ModAddSpawnListEntry extends BaseModification {

	private SpawnListEntryBuilder builder;

	public ModAddSpawnListEntry(String livingGroupId, String biomeGroupId) {
		builder = new SpawnListEntryBuilder(livingGroupId, biomeGroupId);
	}

	public ModAddSpawnListEntry(String livingGroupId, String biomeGroupId, int spawnWeight, int packSize,
			int minChunkPack, int maxChunkPack) {
		this(livingGroupId, biomeGroupId, spawnWeight, packSize, minChunkPack, maxChunkPack, "");
	}

	public ModAddSpawnListEntry(String livingGroupId, String biomeGroupId, int spawnWeight, int packSize,
			int minChunkPack, int maxChunkPack, String optionalParameters) {
		builder = new SpawnListEntryBuilder(livingGroupId, biomeGroupId);
		builder.setWeight(spawnWeight);
		builder.setPackSize(packSize);
		builder.setMinChunkPack(packSize);
		builder.setMaxChunkPack(packSize);
//		builder.setOptionalParameters(optionalParameters);
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		registry.addSpawnListEntry(builder);
	}
}