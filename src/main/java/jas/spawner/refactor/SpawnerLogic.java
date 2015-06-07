package jas.spawner.refactor;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.helper.MVELHelper;
import jas.spawner.modern.EntityProperties;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.EntityCounter;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.refactor.SpawnerLogic.Counter.SpawnCounter;
import jas.spawner.refactor.despawn.DespawnRuleBuilder.DespawnRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import org.apache.logging.log4j.Level;

/**
 * Common Spawner Processes
 */
public class SpawnerLogic {

	public static SpawnCounter counter;
//	public static Despawner despawn;
//	public static Spawner spawner;

	public static interface Counter {
		public CountInfo countEntities(World world, int spawnChunkDistance, int countChunkDistance);

		public Collection<Entity> countLoadedEntities(World world, int chunkDistance);

		public static final class SpawnCounter implements Counter {

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
							boolean flag3 = xOffset == -chunkDistance || xOffset == chunkDistance
									|| zOffset == -chunkDistance || zOffset == chunkDistance;
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
						ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(
								MathHelper.floor_double(entity.posX / 16.0D),
								MathHelper.floor_double(entity.posZ / 16.0D));
						SpawnSettings spawnSettings = ExperimentalProfile.worldSettings().getSpawnSettings(world);
						Collection<String> livingTypes = ExperimentalProfile
								.worldSettings()
								.getSpawnSettings(world)
								.biomeGroupRegistry()
								.livingTypesForEntity(world, entity, spawnSettings.livingSettings(),
										spawnSettings.biomeSettings());
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

			// private Set<String> getApplicableLivingTypes(Collection<LivingHandler> livingHandlers) {
			// Set<String> livingTypes = new HashSet<String>();
			// for (LivingHandler livingHandler : livingHandlers) {
			// if (livingHandler != null) {
			// livingTypes.add(livingHandler.creatureTypeID);
			// }
			// }
			// return livingTypes;
			// }

			public Collection<Entity> countLoadedEntities(World world) {
				return countLoadedEntities(world, JustAnotherSpawner.globalSettings().chunkCountDistance);
			}

			@Override
			public Collection<Entity> countLoadedEntities(World world, int chunkDistance) {
				List<Entity> entities = new ArrayList<Entity>();
				// chunkDistance == JustAnotherSpawner.globalSettings().chunkCountDistance
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
	}

//	public static CountInfo countEntities(World world) {
//		return SpawnerLogic.counter.countEntities(world);
//	}

	public static void despawnEntity(EntityLiving entity, Tags tags, DespawnRule despawnRule) {
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.getEntityBoundingBox().minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		if (entityplayer != null) {
			double d0 = entityplayer.posX - entity.posX;
			double d1 = entityplayer.posY - entity.posY;
			double d2 = entityplayer.posZ - entity.posZ;
			double playerDistance = d0 * d0 + d1 * d1 + d2 * d2;

			EntityProperties entityProps = (EntityProperties) entity
					.getExtendedProperties(EntityProperties.JAS_PROPERTIES);
			entityProps.incrementAge(60);

			boolean canDespawn;
			if (despawnRule.canDspwn.isPresent()) {
				canDespawn = !MVELHelper.executeExpression(despawnRule.canDspwn.get().compiled.get(), tags,
						"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
								+ despawnRule.canDspwn.get().expression);
			} else {
				canDespawn = LivingHelper.canDespawn(entity);
			}

			if (canDespawn == false) {
				entityProps.resetAge();
				return;
			}

			boolean canInstantDespawn;
			if (despawnRule.shouldInstantDspwn.isPresent()) {
				canInstantDespawn = !MVELHelper.executeExpression(despawnRule.shouldInstantDspwn.get().compiled.get(),
						tags, "Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
								+ despawnRule.shouldInstantDspwn.get().expression);
			} else {
				Integer maxRange = ExperimentalProfile.worldSettings().worldProperties().getGlobal().maxDespawnDist;
				canInstantDespawn = playerDistance > maxRange * maxRange;
			}

			if (canInstantDespawn) {
				entity.setDead();
			} else {
				boolean dieOfAge;
				final int rate = 40; // Value from Vanilla
				if (despawnRule.dieOfAge.isPresent()) {
					dieOfAge = !MVELHelper.executeExpression(despawnRule.dieOfAge.get().compiled.get(), tags,
							"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
									+ despawnRule.dieOfAge.get().expression);
				} else {
					Integer minRange = ExperimentalProfile.worldSettings().worldProperties().getGlobal().despawnDist;
					Integer minAge = ExperimentalProfile.worldSettings().worldProperties().getGlobal().minDespawnTime;
					boolean isOfAge = entityProps.getAge() > minAge;
					boolean validDistance = playerDistance > minRange * minRange;
					dieOfAge = isOfAge && entity.worldObj.rand.nextInt(1 + rate / 3) == 0 && validDistance;
				}

				boolean resetAge;
				if (despawnRule.resetAge.isPresent()) {
					resetAge = !MVELHelper.executeExpression(despawnRule.resetAge.get().compiled.get(), tags,
							"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
									+ despawnRule.resetAge.get().expression);
				} else {
					Integer minRange = ExperimentalProfile.worldSettings().worldProperties().getGlobal().despawnDist;
					Integer minAge = ExperimentalProfile.worldSettings().worldProperties().getGlobal().minDespawnTime;
					boolean validDistance = playerDistance > minRange * minRange;
					resetAge = !(playerDistance > minRange * minRange);
				}

				if (dieOfAge) {
					JASLog.log().debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s",
							entity.getName(), entityProps.getAge(), rate);
					entity.setDead();
				} else if (resetAge) {
					entityProps.resetAge();
				}
			}
		}
	}

	public static boolean willEntityDespawn(EntityLiving entity, Tags tags, DespawnRule despawnRule) {
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.getEntityBoundingBox().minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		if (entityplayer != null) {
			double d0 = entityplayer.posX - entity.posX;
			double d1 = entityplayer.posY - entity.posY;
			double d2 = entityplayer.posZ - entity.posZ;
			double playerDistance = d0 * d0 + d1 * d1 + d2 * d2;

			EntityProperties entityProps = (EntityProperties) entity
					.getExtendedProperties(EntityProperties.JAS_PROPERTIES);
			entityProps.incrementAge(60);
			boolean canDespawn;
			if (despawnRule.canDspwn.isPresent()) {
				canDespawn = !MVELHelper.executeExpression(despawnRule.canDspwn.get().compiled.get(), tags,
						"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
								+ despawnRule.canDspwn.get().expression);
			} else {
				canDespawn = LivingHelper.canDespawn(entity);
			}
			if (canDespawn == false) {
				return false;
			}
			// Other Despawn logic is ignored; entity is assumed to eventually be able to age and despawn
		}
		return true;
	}
}
