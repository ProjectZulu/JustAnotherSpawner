package jas.spawner.refactor.biome.list;

import net.minecraft.util.BlockPos;


public class EntitySpawn {
	private String entityToSpawn;
	private BlockPos spawnPosition;

	public void setEntity(String entityToSpawn) {
		this.entityToSpawn = entityToSpawn;
	}

	public String getEntity() {
		return entityToSpawn;
	}

	public void setPos(BlockPos spawnPosition) {
		this.spawnPosition = spawnPosition;
	}

	public BlockPos getPos() {
		return spawnPosition;
	}
}