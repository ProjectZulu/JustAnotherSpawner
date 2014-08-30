package jas.common.spawner;

import jas.common.spawner.CountInfo.ChunkStat;
import jas.common.spawner.creature.type.CreatureType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

public final class CountInfo {
	private HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning;
	private EntityCounter globalCreatureTypeCount;
	private EntityCounter globalCreatureClassCount;

	private int entitiesSpawnedThisLoop; // Count of entities spawned in each chunk-spawn loop

	public static class ChunkStat {
		public final boolean isEdge;
		public final EntityCounter entityClassCount = new EntityCounter();
		public final EntityCounter entityTypeCount = new EntityCounter();

		public ChunkStat(boolean isEdge) {
			this.isEdge = isEdge;
		}
	}

	public CountInfo(HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning,
			EntityCounter globalCreatureTypeCount, EntityCounter globalCreatureClassCount) {
		this.eligibleChunksForSpawning = eligibleChunksForSpawning;
		this.globalCreatureTypeCount = globalCreatureTypeCount;
		this.globalCreatureClassCount = globalCreatureClassCount;
	}

	public void countSpawn(Entity creature, String creatureType) {
		int posX = MathHelper.floor_double(creature.posX / 16.0D);
		int posZ = MathHelper.floor_double(creature.posZ / 16.0D);

		ChunkCoordIntPair pair = new ChunkCoordIntPair(posX, posZ);
		ChunkStat chunkStat = eligibleChunksForSpawning.get(pair);
		if (chunkStat != null) {
			chunkStat.entityClassCount.incrementOrPutIfAbsent(creatureType, 1);
			chunkStat.entityClassCount.incrementOrPutIfAbsent(creature.getClass().getSimpleName(), 1);
		}
		globalCreatureTypeCount.incrementOrPutIfAbsent(creatureType, 1);
		globalCreatureClassCount.incrementOrPutIfAbsent(creature.getClass().getSimpleName(), 1);
		++entitiesSpawnedThisLoop;
	}

	public ChunkStat getChunkStat(ChunkCoordIntPair location) {
		return eligibleChunksForSpawning.get(location);
	}

	public Set<ChunkCoordIntPair> eligibleChunkLocations() {
		return eligibleChunksForSpawning.keySet();
	}

	public int getLocalEntityTypeCount(ChunkCoordIntPair location, String entityType) {
		return eligibleChunksForSpawning.get(location).entityTypeCount.getOrPutIfAbsent(entityType, 0).get();
	}

	public int getLocalEntityClassCount(ChunkCoordIntPair location, Class<?> entityClass) {
		return eligibleChunksForSpawning.get(location).entityClassCount
				.getOrPutIfAbsent(entityClass.getSimpleName(), 0).get();
	}

	public int getGlobalEntityTypeCount(String entityType) {
		return globalCreatureTypeCount.getOrPutIfAbsent(entityType, 0).get();
	}

	public int getGlobalEntityClassCount(Class<?> entityClass) {
		return globalCreatureClassCount.getOrPutIfAbsent(entityClass.getSimpleName(), 0).get();
	}

	public int getEntitiesSpawnedThisLoop() {
		return entitiesSpawnedThisLoop;
	}

	public void resetEntitiesSpawnedThisLoop() {
		entitiesSpawnedThisLoop = 0;
	}

	/**
	 * Counts Entities off
	 */
	public int getClodEntityCount(ChunkCoordIntPair chunkCoord, String creatureTypeID) {
		// ClodSize could be customizable, but other values in testing didn't seem to work well
		final int clodSize = 2;
		int entityTotal = 0;
		int chunksActiallyCounted = 0;
		for (int i = -clodSize; i <= clodSize; i++) {
			for (int k = -clodSize; k <= clodSize; k++) {
				ChunkCoordIntPair coord = new ChunkCoordIntPair(chunkCoord.chunkXPos + i, chunkCoord.chunkZPos + k);
				ChunkStat chunkStat = eligibleChunksForSpawning.get(coord);
				if (chunkStat != null) {
					entityTotal += chunkStat.entityTypeCount.getOrPutIfAbsent(creatureTypeID, 0).get();
					chunksActiallyCounted++;
				}
			}
		}
		return (int) ((entityTotal) * (2f * clodSize + 1) * (2f * clodSize + 1) / chunksActiallyCounted);
	}
}