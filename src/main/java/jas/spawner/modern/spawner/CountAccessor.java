package jas.spawner.modern.spawner;

import jas.common.JASLog;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.tags.CountFunctions;
import jas.spawner.modern.spawner.tags.BaseFunctions;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

import com.google.common.collect.ImmutableBiMap;

/**
 * Accessor to public expose the World object for users to provide them as unchanging interface as possible. As a bonus
 * this provides stability to other tagas that utilize for calls instead of accessing count directly.
 */
public class CountAccessor implements CountFunctions {
	private CountInfo info;
	private BaseFunctions parent;

	public CountAccessor(CountInfo info, BaseFunctions parent) {
		this.info = info;
		this.parent = parent;
	}

	private ChunkCoordIntPair blockPosToChunkPos() {
		int posX = MathHelper.floor_double(parent.posX() / 16.0D);
		int posZ = MathHelper.floor_double(parent.posZ() / 16.0D);
		return new ChunkCoordIntPair(posX, posZ);
	}

	public int getLocalEntityTypeCount(String entityType) {
		if (MVELProfile.worldSettings().creatureTypeRegistry().getCreatureType(entityType) == null) {
			JASLog.log().severe("CreatureType %s does not exist. getLocalEntityTypeCount will return 0.", entityType);
		}
		return info.getLocalEntityTypeCount(blockPosToChunkPos(), entityType);
	}

	public int getLocalEntityClassCount(String entityJasName) {
		ImmutableBiMap<String, Class<? extends EntityLiving>> mapping = MVELProfile.worldSettings()
				.livingGroupRegistry().JASNametoEntityClass;
		Class<?> entityClass = mapping.get(entityJasName);
		return entityClass != null ? info.getLocalEntityClassCount(blockPosToChunkPos(), entityClass) : 0;
	}

	public int getGlobalEntityTypeCount(String entityType) {
		if (MVELProfile.worldSettings().creatureTypeRegistry().getCreatureType(entityType) == null) {
			JASLog.log().severe("CreatureType %s does not exist. getLocalEntityTypeCount will return 0.", entityType);
		}
		return info.getGlobalEntityTypeCount(entityType);
	}

	public int getGlobalEntityClassCount(String entityJasName) {
		ImmutableBiMap<String, Class<? extends EntityLiving>> mapping = MVELProfile.worldSettings()
				.livingGroupRegistry().JASNametoEntityClass;
		Class<?> entityClass = mapping.get(entityJasName);
		return entityClass != null ? info.getGlobalEntityClassCount(entityClass) : 0;
	}

	public int entitiesSpawnedThisChunk() {
		return info.getEntitiesSpawnedThisLoop();
	}

	public int entitiesSpawnedThisPack() {
		return info.getEntityPackCount();
	}

	public int clodCount(String entityType) {
		if (MVELProfile.worldSettings().creatureTypeRegistry().getCreatureType(entityType) == null) {
			JASLog.log().severe("CreatureType %s does not exist. clodCount will return 0.", entityType);
		}
		return info.getClodEntityCount(blockPosToChunkPos(), entityType);
	}
}
