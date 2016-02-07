package jas.spawner.refactor;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.global.BiomeBlacklist;
import jas.common.helper.MVELHelper;
import jas.spawner.modern.EntityProperties;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.EntityCounter;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.SpawnSettings.BiomeSettings;
import jas.spawner.refactor.SpawnSettings.LivingSettings;
import jas.spawner.refactor.SpawnerHelper.Counter.SpawnCounter;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.despawn.DespawnRuleBuilder.DespawnRule;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.mvel.MVELExpression;
import jas.spawner.refactor.structure.StructureHandlerBuilder.StructureHandler;
import jas.spawner.refactor.structure.StructureHandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;

import org.apache.logging.log4j.Level;

import com.google.common.base.Optional;

import cpw.mods.fml.common.eventhandler.Event.Result;

/**
 * Common Spawner Processes
 */
public class SpawnerHelper {
	
	// TODO create interface for different spawners
	public interface SpawnerLogic {
		public Counter counter(World world);

		public void spawnCycle(World worldServer, CountInfo countInfo, LivingType creatureType);
	}

	public static SpawnCounter counter = new SpawnCounter();

	// public static Despawner despawn;
	// public static Spawner spawner;

	public static interface Counter {
		
		public CountInfo countEntities(World world);

		public CountInfo countEntities(World world, int spawnChunkDistance, int countChunkDistance);

		public Collection<Entity> countLoadedEntities(World world, int chunkDistance);

		public static final class SpawnCounter implements Counter {

			@Override
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
						Collection<String> livingTypes = ExperimentalProfile.worldSettings().getSpawnSettings(world)
								.biomeGroupRegistry()
								.livingTypesForEntity(world, entity, spawnSettings.livingSettings());
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
	}

	// public static CountInfo countEntities(World world) {
	// return SpawnerLogic.counter.countEntities(world);
	// }

	public static void despawnEntity(EntityLiving entity, Tags tags, DespawnRule despawnRule) {
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
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
							entity.getCommandSenderName(), entityProps.getAge(), rate);
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
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
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

	public static void spawnCycle(World world, CountInfo countInfo, LivingType livingType, BiomeBlacklist blacklist,
			BiomeSettings biomeSettings, LivingSettings livingSettings, BiomeSpawnLists biomesSpawns,
			StructureHandlers structureSpawns) {
		ChunkCoordinates serverOriginPoint = world.getSpawnPoint();
		List<ChunkCoordIntPair> eligibleChunksForSpawning = new ArrayList<ChunkCoordIntPair>(
				countInfo.eligibleChunkLocations());
		Collections.shuffle(eligibleChunksForSpawning);
		labelChunkStart: for (ChunkCoordIntPair chunkCoord : eligibleChunksForSpawning) {
			ChunkStat chunkStat = countInfo.getChunkStat(chunkCoord);
			if (chunkStat.isEdge) {
				continue;
			}
			countInfo.resetEntitiesSpawnedThisLoop();
			for (int numLocAttempts = 0; numLocAttempts < livingType.iterationsPerChunk; ++numLocAttempts) {
				IEntityLivingData entitylivingdata = null;
				ChunkPosition startSpawningPoint = getRandomSpawningPointInChunk(world, chunkCoord.chunkXPos,
						chunkCoord.chunkZPos);

				SpawnListEntry spawnlistentry = null;
				Class<? extends EntityLiving> livingToSpawn = null;
				LivingHandler handler = null;
				countInfo.resetEntitiesPackCount();

				if (livingType.quickCheck.isPresent()) {
					Tags tags = new Tags(world, countInfo, startSpawningPoint.chunkPosX, startSpawningPoint.chunkPosY,
							startSpawningPoint.chunkPosZ);

					Optional<Boolean> quickCheckCanSpawn = MVELExpression.execute(livingType.quickCheck, tags,
							"Error processing spawnExpression compiled expression for " + livingType.livingTypeID
									+ ": " + livingType.quickCheck.expression);
					if (quickCheckCanSpawn.isPresent() && !quickCheckCanSpawn.get()) {
						continue;
					}
				}

				for (int numEntAttempts = 0; numEntAttempts < livingType.iterationsPerPack; ++numEntAttempts) {
					// Randomized on Each Attempt, but horizontally to allow a 'Pack' to spawn near each other
					final int horVar = 10;
					final int verVar = 3;
					ChunkPosition spawningPoint = new ChunkPosition(startSpawningPoint.chunkPosX
							+ world.rand.nextInt(horVar) - world.rand.nextInt(horVar), startSpawningPoint.chunkPosY
							+ world.rand.nextInt(verVar) - world.rand.nextInt(verVar), startSpawningPoint.chunkPosZ
							+ world.rand.nextInt(horVar) - world.rand.nextInt(horVar));
					// Biome BlackList
					if (blacklist.isBlacklisted(world.getBiomeGenForCoords(spawningPoint.chunkPosX,
							spawningPoint.chunkPosY))) {
						break;
					}

					if (isNearPlayerOrOrigin(world, serverOriginPoint, spawningPoint.chunkPosX,
							spawningPoint.chunkPosY, spawningPoint.chunkPosZ)) {
						continue;
					}

					// Set SpawnList Specific attributes, set only for outer loop (when SpawnListEntry == null), is done
					// in inner loop after creatureType.canSpawnHere for performance reasons
					// (regsitry.getSpawnListEntryToSpawn is not cheap)
					if (spawnlistentry == null) {
						spawnlistentry = getRandomSpawnListEntryToSpawn(world, structureSpawns, biomesSpawns,
								biomeSettings, livingType, spawningPoint.chunkPosX, spawningPoint.chunkPosY,
								spawningPoint.chunkPosZ);
						if (spawnlistentry == null) {
							break;
						}
						int randomElement = spawnlistentry.entityMappings.size();
						String livingMappingToSpawn_JASName = spawnlistentry.entityMappings.get(randomElement);
						String livingMappingToSpawn_FMLName = livingSettings.livingMappings().mappingToKey()
								.get(livingMappingToSpawn_JASName);
						livingToSpawn = (Class<? extends EntityLiving>) EntityList.stringToClassMapping
								.get(livingMappingToSpawn_FMLName);
					}

					// LivingCap > TODO Ensure this can be accomplished via the CanSpawnExpression
					// {
					// int globalEntityClassCount = countInfo.getGlobalEntityClassCount(livingToSpawn);
					// int livingCap = handler.getLivingCap();
					//
					// if (livingCap > 0 && globalEntityClassCount >= livingCap) {
					// spawnlistentry = null;
					// break;
					// }
					// }

					/* Spawn is Centered Version of blockSpawn such that entity is not placed in Corner */
					float spawnX = spawningPoint.chunkPosX + 0.5F;
					float spawnY = spawningPoint.chunkPosY;
					float spawnZ = spawningPoint.chunkPosZ + 0.5F;
					EntityLiving entityliving;
					try {
						entityliving = livingToSpawn.getConstructor(new Class[] { World.class }).newInstance(
								new Object[] { world });
					} catch (Exception exception) {
						exception.printStackTrace();
						return;
					}
					entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);
					if (canSpawn(entityliving, spawnlistentry, countInfo, livingType, spawningPoint.chunkPosX,
							spawningPoint.chunkPosY, spawningPoint.chunkPosZ)) {
						world.spawnEntityInWorld(entityliving);
						if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, spawnX, spawnY, spawnZ)) {
							entitylivingdata = entityliving.onSpawnWithEgg(entitylivingdata);
						}
						JASLog.log().logSpawn(
								false,
								(String) EntityList.classToStringMapping.get(entityliving.getClass()),
								livingType.livingTypeID,
								(int) entityliving.posX,
								(int) entityliving.posY,
								(int) entityliving.posZ,
								BiomeHelper.getPackageName(entityliving.worldObj.getBiomeGenForCoords(
										(int) entityliving.posX, (int) entityliving.posZ)));
						Tags tags = new Tags(entityliving.worldObj, countInfo, spawningPoint.chunkPosX,
								spawningPoint.chunkPosY, spawningPoint.chunkPosZ, entityliving);
						if (spawnlistentry.postSpawn.isPresent()) {
							MVELExpression.execute(spawnlistentry.postSpawn.get(), tags,
									"Error processing compiled handler postSpawn expression for "
											+ livingType.livingTypeID + ": "
											+ spawnlistentry.postSpawn.get().expression);
						}
						countInfo.countSpawn(entityliving, livingType.livingTypeID);

						// Living PackSize
						Optional<Integer> packSize = MVELExpression.execute(spawnlistentry.passivePackSize, tags,
								"Error processing compiled handler postSpawn expression for " + livingType.livingTypeID
										+ ": " + spawnlistentry.postSpawn.get().expression);

						if (!packSize.isPresent() || countInfo.getEntitiesSpawnedThisLoop() >= packSize.get()) {
							continue labelChunkStart;
						}
					}
				}
			}
		}
	}

	private static ChunkPosition getRandomSpawningPointInChunk(World world, int chunkX, int chunkZ) {
		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		int xCoord = chunkX * 16 + world.rand.nextInt(16);
		int zCoord = chunkZ * 16 + world.rand.nextInt(16);
		int yCoord = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
		return new ChunkPosition(xCoord, yCoord, zCoord);
	}

	private static boolean isNearPlayerOrOrigin(World world, ChunkCoordinates serverSpawnPoint, int originX,
			int originY, int originZ) {
		if (world.getClosestPlayer(originX, originY, originZ, 24.0D) == null) {
			float xOffset = originX - serverSpawnPoint.posX;
			float yOffset = originY - serverSpawnPoint.posY;
			float zOffset = originZ - serverSpawnPoint.posZ;
			float sqOffset = xOffset * xOffset + yOffset * yOffset + zOffset * zOffset;

			if (sqOffset < 576.0F) {
				return true;
			}
			return false;
		}
		return true;
	}

	private static SpawnListEntry getRandomSpawnListEntryToSpawn(World world, StructureHandlers structures,
			BiomeSpawnLists spawnLists, BiomeSettings biomeSettings, LivingType livingType, int xCoord, int yCoord,
			int zCoord) {
		Collection<SpawnListEntry> spawnList = getSpawnList(world, structures, spawnLists, biomeSettings, livingType,
				xCoord, yCoord, zCoord);
		int totalWeight = 0;
		for (SpawnListEntry entry : spawnList) {
			totalWeight += entry.weight;
		}
		if (totalWeight <= 0) {
			return null;
		} else {
			int selectedWeight = world.rand.nextInt(totalWeight) + 1;
			SpawnListEntry resultEntry = null;

			for (SpawnListEntry spawnListEntry : spawnList) {
				resultEntry = spawnListEntry;
				selectedWeight -= spawnListEntry.weight;
				if (selectedWeight <= 0) {
					return resultEntry;
				}
			}
			return resultEntry;
		}
	}

	private static Collection<SpawnListEntry> getSpawnList(World world, StructureHandlers structures,
			BiomeSpawnLists spawnLists, BiomeSettings biomeSettings, LivingType livingType, int xCoord, int yCoord,
			int zCoord) {
		for (StructureHandler handler : structures.handlers()) {
			Collection<SpawnListEntry> spawnList = handler.getStructureSpawnList(world, xCoord, yCoord, zCoord);
			if (!spawnList.isEmpty()) {
				return spawnList;
			}
		}
		return spawnLists.getSpawnList(world, biomeSettings, world.getBiomeGenForCoords(xCoord, zCoord), livingType);
	}

	private static boolean canSpawn(EntityLiving entity, SpawnListEntry sle, CountInfo countInfo,
			LivingType livingType, int xCoord, int yCoord, int zCoord) {
		Tags tags = new Tags(entity.worldObj, countInfo, xCoord, yCoord, zCoord, entity);
		Optional<Boolean> canSpawnType = MVELExpression.execute(livingType.canSpawn, tags,
				"Error processing spawnExpression compiled expression for " + livingType.livingTypeID + ": "
						+ livingType.quickCheck.expression);
		if (canSpawnType.isPresent() && canSpawnType.get()) {
			Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, entity.worldObj, (int) entity.posX,
					(int) entity.posY, (int) entity.posZ);
			Optional<Boolean> canSpawnSLE = Optional.absent();
			if (sle.canSpawn.isPresent()) {
				canSpawnSLE = MVELExpression.execute(sle.canSpawn.get(), tags,
						"Error processing spawnExpression compiled expression for " + livingType.livingTypeID + ": "
								+ livingType.quickCheck.expression);
			}
			if (!canSpawnSLE.isPresent()) {
				return canSpawn != Result.DENY;
			} else {
				return canSpawnSLE.get();
			}
		} else {
			return false;
		}
	}
}