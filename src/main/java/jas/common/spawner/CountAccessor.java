package jas.common.spawner;

import com.google.common.collect.ImmutableBiMap;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

/**
 * Accessor to public expose the World object for users to provide them as unchanging interface as possible. As a bonus
 * this provides stability to other tagas that utilize for calls instead of accessing count directly.
 */
public class CountAccessor {
	private CountInfo info;
	private Tags parent;

	public CountAccessor(CountInfo info, Tags parent) {
		this.info = info;
		this.parent = parent;
	}

	private ChunkCoordIntPair blockPosToChunkPos() {
		int posX = MathHelper.floor_double(parent.posX / 16.0D);
		int posZ = MathHelper.floor_double(parent.posZ / 16.0D);
		return new ChunkCoordIntPair(posX, posZ);
	}

	public int getLocalEntityTypeCount(String entityType) {
		if (JustAnotherSpawner.worldSettings().creatureTypeRegistry().getCreatureType(entityType) == null) {
			JASLog.log().severe("CreatureType %s does not exist. getLocalEntityTypeCount will return 0.", entityType);
		}
		return info.getLocalEntityTypeCount(blockPosToChunkPos(), entityType);
	}

	public int getLocalEntityClassCount(ChunkCoordIntPair location, String entityJasName) {
		ImmutableBiMap<String, Class<? extends EntityLiving>> mapping = JustAnotherSpawner.worldSettings()
				.livingGroupRegistry().JASNametoEntityClass;
		Class<?> entityClass = mapping.get(entityJasName);
		return entityClass != null ? info.getLocalEntityClassCount(blockPosToChunkPos(), entityClass) : 0;
	}

	public int getGlobalEntityTypeCount(String entityType) {
		if (JustAnotherSpawner.worldSettings().creatureTypeRegistry().getCreatureType(entityType) == null) {
			JASLog.log().severe("CreatureType %s does not exist. getLocalEntityTypeCount will return 0.", entityType);
		}
		return info.getGlobalEntityTypeCount(entityType);
	}

	public int getGlobalEntityClassCount(ChunkCoordIntPair location, String entityJasName) {
		ImmutableBiMap<String, Class<? extends EntityLiving>> mapping = JustAnotherSpawner.worldSettings()
				.livingGroupRegistry().JASNametoEntityClass;
		Class<?> entityClass = mapping.get(entityJasName);
		return entityClass != null ? info.getGlobalEntityClassCount(entityClass) : 0;
	}

	public int entitiesSpawnedThisLoop() {
		return info.getEntitiesSpawnedThisLoop();
	}

	public int entitiesSpawnedThisPack() {
		return info.getEntityPackCount();
	}

	public int clodCount(String entityType) {
		if (JustAnotherSpawner.worldSettings().creatureTypeRegistry().getCreatureType(entityType) == null) {
			JASLog.log().severe("CreatureType %s does not exist. clodCount will return 0.", entityType);
		}
		return info.getClodEntityCount(blockPosToChunkPos(), entityType);
	}
}
