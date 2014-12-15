package jas.refactor.biome.list;

import net.minecraft.util.ChunkCoordinates;

public class EntitySpawn {
	private String entityToSpawn;
	private ChunkCoordinates spawnPosition;

	public void setEntity(String entityToSpawn) {
		this.entityToSpawn = entityToSpawn;
	}

	public String getEntity() {
		return entityToSpawn;
	}

	public void setPos(ChunkCoordinates spawnPosition) {
		this.spawnPosition = spawnPosition;
	}

	public ChunkCoordinates getPos() {
		return spawnPosition;
	}
}