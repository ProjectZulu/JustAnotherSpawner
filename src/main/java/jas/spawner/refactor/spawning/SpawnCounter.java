package jas.spawner.refactor.spawning;

import jas.common.JustAnotherSpawner;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.EntityCounter;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.Counter;
import jas.spawner.refactor.BiomeSpawnLists;
import jas.spawner.refactor.ExperimentalProfile;
import jas.spawner.refactor.LivingHandlers;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

public class SpawnCounter implements Counter {
	private BiomeSpawnLists biomeSpawnLists;

	public SpawnCounter(BiomeSpawnLists biomeSpawnLists) {
		this.biomeSpawnLists = biomeSpawnLists;
	}

	public CountInfo countEntities(World world) {
		return countEntities(world, JustAnotherSpawner.globalSettings().chunkSpawnDistance,
				JustAnotherSpawner.globalSettings().chunkCountDistance);
	}

	@Override
	public CountInfo countEntities(World world, int spawnChunkDistance, int countChunkDistance) {
		// JustAnotherSpawner.globalSettings().chunkSpawnDistance
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = determineChunksForSpawning(world,
				spawnChunkDistance);
		return countInfo(world, eligibleChunksForSpawning, countChunkDistance);
	}

	private HashMap<ChunkCoordIntPair, ChunkStat> determineChunksForSpawning(World world, int chunkDistance) {
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = new HashMap<ChunkCoordIntPair, ChunkStat>();
		for (int i = 0; i < world.playerEntities.size(); ++i) {
			EntityPlayer entityplayer = (EntityPlayer) world.playerEntities.get(i);
			int posX = MathHelper.floor_double(entityplayer.posX / 16.0D);
			int posZ = MathHelper.floor_double(entityplayer.posZ / 16.0D);

			for (int xOffset = -chunkDistance; xOffset <= chunkDistance; ++xOffset) {
				for (int zOffset = -chunkDistance; zOffset <= chunkDistance; ++zOffset) {
					boolean flag3 = xOffset == -chunkDistance || xOffset == chunkDistance || zOffset == -chunkDistance
							|| zOffset == chunkDistance;
					ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(xOffset + posX, zOffset + posZ);
					eligibleChunksForSpawning.put(chunkcoordintpair, new ChunkStat(flag3));
				}
			}
		}
		return eligibleChunksForSpawning;
	}

	private CountInfo countInfo(World world, HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning,
			int countChunkDistance) {
		EntityCounter creatureTypeCount = new EntityCounter();
		EntityCounter creatureCount = new EntityCounter();

		for (Object object : world.loadedEntityList) {
			Entity entity = (Entity) object;
			EntityPlayer player = world.getClosestPlayerToEntity(entity, countChunkDistance * 16);
			if (isPlayerClose(world, entity, countChunkDistance * 16)) {
				ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(MathHelper.floor_double(entity.posX / 16.0D),
						MathHelper.floor_double(entity.posZ / 16.0D));
				//LivingType will be stored in Entity along with LH
//				biomeSpawnLists.getSpawnList().getSpawnListEntries(, livingType);
//				Collection<LivingHandler> livingHandlers = livingHandlerRegistry
//						.getLivingHandler((Class<? extends EntityLiving>) entity.getClass());
				Set<String> livingTypes = getApplicableLivingTypes(null /*livingHandlers*/);

				creatureCount.incrementOrPutIfAbsent(entity.getClass().getSimpleName(), 1);
				for (String creatureTypeID : livingTypes) {
					creatureTypeCount.incrementOrPutIfAbsent(creatureTypeID, 1);
				}

				ChunkStat chunkStat = eligibleChunksForSpawning.get(chunkPos);
				if (chunkStat != null) {
					chunkStat.entityClassCount.incrementOrPutIfAbsent(entity.getClass().getSimpleName(), 1);
					for (String creatureTypeID : livingTypes) {
						chunkStat.entityTypeCount.incrementOrPutIfAbsent(creatureTypeID, 1);
					}
				}
			}
		}
		return new CountInfo(eligibleChunksForSpawning, creatureTypeCount, creatureCount);
	}

	private Set<String> getApplicableLivingTypes(Collection<LivingHandler> livingHandlers) {
		//TODO
		Set<String> livingTypes = new HashSet<String>();
		for (LivingHandler livingHandler : livingHandlers) {
			if (livingHandler != null) {
//				livingTypes.add(livingHandler.creatureTypeID);
			}
		}
		return livingTypes;
	}

	public Collection<Entity> countLoadedEntities(World world) {
		return countLoadedEntities(world, JustAnotherSpawner.globalSettings().chunkCountDistance);
	}

	@Override
	public Collection<Entity> countLoadedEntities(World world, int chunkDistance) {
		List<Entity> entities = new ArrayList<Entity>();
		if (chunkDistance <= 0) {
			return world.loadedEntityList;
		}

		for (Object object : world.loadedEntityList) {
			Entity entity = (Entity) object;
			if (isPlayerClose(world, entity, chunkDistance * 16)) {
				entities.add(entity);
			}
		}
		return entities;
	}

	private boolean isPlayerClose(World world, Entity entity, double cutoffDist) {
		for (int i = 0; i < world.playerEntities.size(); ++i) {
			EntityPlayer player = (EntityPlayer) world.playerEntities.get(i);
			double xDist = player.posX - entity.posX;
			double zDist = player.posZ - entity.posZ;
			double curDist = (xDist * xDist + zDist * zDist);
			if ((cutoffDist < 0.0D || curDist < cutoffDist * cutoffDist)) {
				return true;
			}
		}
		return false;
	}
}
