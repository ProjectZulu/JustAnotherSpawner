package jas.modern.modification;

import jas.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.modern.spawner.creature.entry.SpawnListEntryBuilder;

public class ModUpdateSpawnListEntry extends BaseModification {

	private String prevLivingGroupId;
	private String prevBiomeGroupId;
	private SpawnListEntryBuilder builder;

	public ModUpdateSpawnListEntry(String prevLivingGroupId, String prevBiomeGroupId, String livingGroupId,
			String biomeGroupId) {
		this.prevLivingGroupId = prevLivingGroupId;
		this.prevBiomeGroupId = prevBiomeGroupId;
		builder = new SpawnListEntryBuilder(livingGroupId, biomeGroupId);
	}

	public ModUpdateSpawnListEntry(String prevLivingGroupId, String prevBiomeGroupId, String livingGroupId,
			String biomeGroupId, int spawnWeight, int packSize, int minChunkPack, int maxChunkPack) {
		this(prevLivingGroupId, prevBiomeGroupId, livingGroupId, biomeGroupId, spawnWeight, packSize, minChunkPack,
				maxChunkPack, "");
	}

	public ModUpdateSpawnListEntry(String prevLivingGroupId, String prevBiomeGroupId, String livingGroupId,
			String biomeGroupId, int spawnWeight, int packSize, int minChunkPack, int maxChunkPack,
			String optionalParameters) {
		this.prevLivingGroupId = prevLivingGroupId;
		this.prevBiomeGroupId = prevBiomeGroupId;
		builder = new SpawnListEntryBuilder(livingGroupId, biomeGroupId);
		builder.setWeight(spawnWeight);
		builder.setPackSize(packSize);
		builder.setMinChunkPack(packSize);
		builder.setMaxChunkPack(packSize);
//		builder.setOptionalParameters(optionalParameters);
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		registry.updateSpawnListEntry(prevLivingGroupId, prevBiomeGroupId, builder);
	}
}