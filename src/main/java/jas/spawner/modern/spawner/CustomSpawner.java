package jas.spawner.modern.spawner;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.global.BiomeBlacklist;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.entry.SpawnListEntry;
import jas.spawner.modern.spawner.creature.handler.LivingHandler;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.eventhandler.Event.Result;

public class CustomSpawner {

	public static CountInfo determineCountInfo(World world) {
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = CustomSpawner.determineChunksForSpawnering(
				world, JustAnotherSpawner.globalSettings().chunkSpawnDistance);
		EntityCounter creatureTypeCount = new EntityCounter();
		EntityCounter creatureCount = new EntityCounter();
		CustomSpawner.countEntityInChunks(world, creatureTypeCount, creatureCount);
		return new CountInfo(eligibleChunksForSpawning, creatureTypeCount, creatureCount);
	}

	/**
	 * Populates eligibleChunksForSpawning with All Valid Chunks. Unlike its vanilla counterpart
	 * {@link SpawnerAnimals#findChunksForSpawning} this does not spawn a Creature.
	 * 
	 * @param worldServer
	 * @param par1
	 *            should Spawn spawnHostileMobs
	 * @param par2
	 *            should Spawn spawnPeacefulMobs
	 * @param par3
	 *            worldInfo.getWorldTotalTime() % 400L == 0L
	 */
	public static final HashMap<ChunkCoordIntPair, ChunkStat> determineChunksForSpawnering(World worldServer,
			int chunkDistance) {
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = new HashMap<ChunkCoordIntPair, ChunkStat>();
		for (int i = 0; i < worldServer.playerEntities.size(); ++i) {
			EntityPlayer entityplayer = (EntityPlayer) worldServer.playerEntities.get(i);
			int posX = MathHelper.floor_double(entityplayer.posX / 16.0D);
			int posZ = MathHelper.floor_double(entityplayer.posZ / 16.0D);

			for (int xOffset = -chunkDistance; xOffset <= chunkDistance; ++xOffset) {
				for (int zOffset = -chunkDistance; zOffset <= chunkDistance; ++zOffset) {
					boolean flag3 = xOffset == -chunkDistance || xOffset == chunkDistance || zOffset == -chunkDistance
							|| zOffset == chunkDistance;
					ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(xOffset + posX, zOffset + posZ);
					ChunkStat chunkStat = new ChunkStat(flag3);
					Chunk chunk = worldServer.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos,
							chunkcoordintpair.chunkZPos);
					for (@SuppressWarnings("rawtypes")
					List entityLists : chunk.entityLists) {
						for (Object object : entityLists) {
							if (object == null || !(object instanceof EntityLiving)) {
								continue;
							}
							EntityLiving entity = (EntityLiving) object;
							List<LivingHandler> livingHandlers = MVELProfile.worldSettings()
									.livingHandlerRegistry()
									.getLivingHandlers((Class<? extends EntityLiving>) entity.getClass());
							Set<String> livingTypes = getApplicableLivingTypes(livingHandlers);
							chunkStat.entityClassCount.incrementOrPutIfAbsent(entity.getClass().getSimpleName(), 1);
							for (String creatureTypeID : livingTypes) {
								chunkStat.entityTypeCount.incrementOrPutIfAbsent(creatureTypeID, 1);
							}
						}
					}

					eligibleChunksForSpawning.put(chunkcoordintpair, chunkStat);
				}
			}
		}
		return eligibleChunksForSpawning;
	}

	/**
	 * Count and Cache the Amount of Loaded Entities
	 * 
	 * @param worldServer
	 */
	public static void countEntityInChunks(World worldServer, EntityCounter creatureType, EntityCounter creatureCount) {
		for (Entity entity : getLoadedEntities(worldServer)) {
			if (entity == null) {
				continue;
			}
			@SuppressWarnings("unchecked")
			List<LivingHandler> livingHandlers = MVELProfile.worldSettings().livingHandlerRegistry()
					.getLivingHandlers((Class<? extends EntityLiving>) entity.getClass());
			Set<String> livingTypes = getApplicableLivingTypes(livingHandlers);
			creatureCount.incrementOrPutIfAbsent(entity.getClass().getSimpleName(), 1);
			for (String creatureTypeID : livingTypes) {
				creatureType.incrementOrPutIfAbsent(creatureTypeID, 1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Entity> getLoadedEntities(World world) {
		List<Entity> entities = new ArrayList<Entity>();
		if (JustAnotherSpawner.globalSettings().chunkCountDistance <= 0) {
			return world.loadedEntityList;
		}

		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForCounting = CustomSpawner.determineChunksForSpawnering(
				world, JustAnotherSpawner.globalSettings().chunkCountDistance);
		for (ChunkCoordIntPair pair : eligibleChunksForCounting.keySet()) {
			Chunk chunk = world.getChunkFromChunkCoords(pair.chunkXPos, pair.chunkZPos);
			if (chunk != null && chunk.entityLists != null) {
				for (int i = 0; i < chunk.entityLists.length; i++) {
					@SuppressWarnings("rawtypes")
					List loadedEntities = chunk.entityLists[i];
					for (int j = 0; j < loadedEntities.size(); j++) {
						Entity entity = (Entity) loadedEntities.get(j);
						if (entity == null) {
							continue;
						}
						entities.add(entity);
					}
				}
			}
		}
		return entities;
	}

	/**
	 * Helper method get the number of unique livingTypes from the applicable livinghandlers
	 * 
	 * Used when counting entities such that entities with the same type twice are counted once. i.e. is Skeleton is in
	 * three groups two which are MONSTER one which is AMBIENT, it counts once as a monster and once as an ambient
	 */
	private static Set<String> getApplicableLivingTypes(Collection<LivingHandler> livingHandlers) {
		Set<String> livingTypes = new HashSet<String>();
		for (LivingHandler livingHandler : livingHandlers) {
			if (livingHandler != null) {
				livingTypes.add(livingHandler.creatureTypeID);
			}
		}
		return livingTypes;
	}

	/**
	 * Performs Actual Creature Spawning inside eligibleChunks. {@link determineChunksForSpawnering} needs to be run to
	 * populate eligibleChunksForSpawning with spawnable chunks
	 * 
	 * @param creatureType
	 *            CreatureType spawnList that is being Spawned
	 */
	public static final void spawnCreaturesInChunks(WorldServer worldServer,
			LivingHandlerRegistry livingHandlerRegistry, BiomeSpawnListRegistry biomeSpawnListRegistry,
			CreatureType creatureType, BiomeBlacklist blacklist, CountInfo countInfo) {
		ChunkCoordinates serverOriginPoint = worldServer.getSpawnPoint();

		List<ChunkCoordIntPair> eligibleChunksForSpawning = new ArrayList<ChunkCoordIntPair>(
				countInfo.eligibleChunkLocations());
		Collections.shuffle(eligibleChunksForSpawning);
		
		final int entityTypeCap = creatureType.maxNumberOfCreature * countInfo.eligibleChunkLocations().size() / 256;
		int globalEntityTypeCount = countInfo.getGlobalEntityTypeCount(creatureType.typeID);
		if (globalEntityTypeCount > entityTypeCap) {
			return;
		}
		for (ChunkCoordIntPair chunkCoord : eligibleChunksForSpawning) {
			ChunkStat chunkStat = countInfo.getChunkStat(chunkCoord);
			if (chunkStat.isEdge) {
				continue;
			}
			// TODO: CreatureType.passiveSpawnAttempts
			countInfo.resetEntitiesSpawnedThisLoop();
			for (int numLocAttempts = 0; numLocAttempts < creatureType.iterationsPerChunk; ++numLocAttempts) {
				IEntityLivingData entitylivingdata = null;
				ChunkPosition startSpawningPoint = creatureType.getRandomSpawningPointInChunk(worldServer,
						chunkCoord.chunkXPos, chunkCoord.chunkZPos);
				SpawnListEntry spawnlistentry = biomeSpawnListRegistry.getSpawnListEntryToSpawn(worldServer,
						creatureType, startSpawningPoint.chunkPosX, startSpawningPoint.chunkPosY, startSpawningPoint.chunkPosZ);
				if (spawnlistentry == null) {
					continue;
				}
				Tags tags = new Tags(worldServer, countInfo, startSpawningPoint.chunkPosX,
						startSpawningPoint.chunkPosY, startSpawningPoint.chunkPosZ);
				Class<? extends EntityLiving> livingToSpawn = livingHandlerRegistry.getRandomEntity(
						spawnlistentry.livingGroupID, worldServer.rand, tags);
				if (livingToSpawn == null) {
					continue;
				}
				LivingHandler handler = livingHandlerRegistry.getLivingHandler(spawnlistentry.livingGroupID);
				countInfo.resetEntitiesPackCount();
				for (int numEntAttempts = 0; numEntAttempts < creatureType.iterationsPerPack; ++numEntAttempts) {
					// Randomized on Each Attempt, but horizontally to allow a 'Pack' to spawn near each other
					final int horVar = 6;
					final int verVar = 1;
					ChunkPosition spawningPoint = new ChunkPosition(
						startSpawningPoint.chunkPosX + worldServer.rand.nextInt(horVar) - worldServer.rand.nextInt(horVar),
						startSpawningPoint.chunkPosY + worldServer.rand.nextInt(verVar) - worldServer.rand.nextInt(verVar),
						startSpawningPoint.chunkPosZ + worldServer.rand.nextInt(horVar) - worldServer.rand.nextInt(horVar));
					// Biome BlackList
					if (blacklist.isBlacklisted(worldServer.getBiomeGenForCoords(spawningPoint.chunkPosX,
							spawningPoint.chunkPosY))) {
						continue;
					}

					if (isNearPlayerOrOrigin(worldServer, serverOriginPoint, spawningPoint.chunkPosX,
							spawningPoint.chunkPosY, spawningPoint.chunkPosZ)) {
						continue;
					}

					// CreatureType
					if (!creatureType.canSpawnHere(worldServer, countInfo, spawningPoint)) {
						continue;
					}

					// LivingCap and PackSize
					{
						int globalEntityClassCount = countInfo.getGlobalEntityClassCount(livingToSpawn);
						int livingCap = handler.getLivingCap();

						if (livingCap > 0 && globalEntityClassCount >= livingCap) {
							spawnlistentry = null;
							continue;
						}
						if (countInfo.getEntitiesSpawnedThisLoop() >= spawnlistentry.packSize) {
							continue;
						}
					}

					/* Spawn is Centered Version of blockSpawn such that entity is not placed in Corner */
					float spawnX = spawningPoint.chunkPosX + 0.5F;
					float spawnY = spawningPoint.chunkPosY;
					float spawnZ = spawningPoint.chunkPosZ + 0.5F;
					EntityLiving entityliving;
					try {
						entityliving = livingToSpawn.getConstructor(new Class[] { World.class }).newInstance(
								new Object[] { worldServer });
					} catch (Exception exception) {
						exception.printStackTrace();
						return;
					}
					entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ, worldServer.rand.nextFloat() * 360.0F,
							0.0F);

					Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, worldServer, spawnX, spawnY,
							spawnZ);
					if (canSpawn == Result.ALLOW
							|| (canSpawn == Result.DEFAULT && spawnlistentry.getLivingHandler().getCanSpawnHere(
									entityliving, spawnlistentry, countInfo))) {
						if (canSpawn == Result.ALLOW) {
							JASLog.log()
									.warning(
											"JAS is spawning entity %s, irrespective of JAS LH & SLE conditions as part of LivingSpawnEvent compatabillity.",
											(String) EntityList.classToStringMapping.get(entityliving.getClass()));
						}

						worldServer.spawnEntityInWorld(entityliving);
						if (!ForgeEventFactory.doSpecialSpawn(entityliving, worldServer, spawnX, spawnY, spawnZ)) {
							entitylivingdata = entityliving.onSpawnWithEgg(entitylivingdata);
						}
						JASLog.log().logSpawn(
								false,
								(String) EntityList.classToStringMapping.get(entityliving.getClass()),
								spawnlistentry.getLivingHandler().creatureTypeID,
								(int) entityliving.posX,
								(int) entityliving.posY,
								(int) entityliving.posZ,
								BiomeHelper.getPackageName(entityliving.worldObj.getBiomeGenForCoords(
										(int) entityliving.posX, (int) entityliving.posZ)));
						spawnlistentry.getLivingHandler().postSpawnEntity(entityliving, spawnlistentry, countInfo);
						countInfo.countSpawn(entityliving, creatureType.typeID);
					}
				}
			}
		}
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

	/**
	 * Called during chunk generation to spawn initial creatures.
	 */
	public static void performWorldGenSpawning(World world, CreatureType creatureType,
			LivingHandlerRegistry livingHandlerRegistry, BiomeGenBase biome, int par2, int par3, int par4, int par5,
			Random random) {
		if (random.nextFloat() < creatureType.chunkSpawnChance) {
			int j1 = par2 + random.nextInt(par4);
			int k1 = par3 + random.nextInt(par5);
			int l1 = j1;
			int i2 = k1;
			int topHeight = world.getTopSolidOrLiquidBlock(j1, k1);
			BiomeSpawnListRegistry biomeSpawnListRegistry = MVELProfile.worldSettings().biomeSpawnListRegistry();
			SpawnListEntry spawnListEntry = biomeSpawnListRegistry.getSpawnListEntryToSpawn(world, creatureType, j1,
					topHeight, k1);
			IEntityLivingData entitylivingdata = null;
			if (spawnListEntry == null) {
				JASLog.log().debug(Level.INFO, "Entity not Spawned due to Empty %s List", creatureType.typeID);
				return;
			} else {
				JASLog.log().debug(Level.INFO, "Evaluating if We Should spawn entity group %s",
						spawnListEntry.livingGroupID);
			}
			int i1 = spawnListEntry.minChunkPack
					+ random.nextInt(1 + spawnListEntry.maxChunkPack - spawnListEntry.minChunkPack);
			CountInfo countInfo = CustomSpawner.determineCountInfo(world);
			for (int j2 = 0; j2 < i1; ++j2) {
				boolean flag = false;
				Tags tags = new Tags(world, countInfo, j1, topHeight, k1);
				Class<? extends EntityLiving> livingToSpawn = livingHandlerRegistry.getRandomEntity(
						spawnListEntry.livingGroupID, world.rand, tags);
				if (livingToSpawn == null) {
					JASLog.log().severe("No EntityClasses appear to exist in %s", spawnListEntry.toString());
					continue;
				}
				for (int k2 = 0; !flag && k2 < 4; ++k2) {
					int l2 = world.getTopSolidOrLiquidBlock(j1, k1);
					if (creatureType.canSpawnAtLocation(world, new Tags(world, countInfo, j1, l2, k1), j1, l2, k1)) {
						float f = j1 + 0.5F;
						float f1 = l2;
						float f2 = k1 + 0.5F;
						EntityLiving entityliving;

						try {
							entityliving = livingToSpawn.getConstructor(new Class[] { World.class }).newInstance(
									new Object[] { world });
						} catch (Exception exception) {
							exception.printStackTrace();
							continue;
						}

						entityliving.setLocationAndAngles(f, f1, f2, random.nextFloat() * 360.0F, 0.0F);
						JASLog.log().logSpawn(
								true,
								(String) EntityList.classToStringMapping.get(entityliving.getClass()),
								spawnListEntry.getLivingHandler().creatureTypeID,
								(int) entityliving.posX,
								(int) entityliving.posY,
								(int) entityliving.posZ,
								BiomeHelper.getPackageName(entityliving.worldObj.getBiomeGenForCoords(
										(int) entityliving.posX, (int) entityliving.posZ)));
						world.spawnEntityInWorld(entityliving);
						if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, f, f1, f2)) {
							entitylivingdata = entityliving.onSpawnWithEgg(entitylivingdata);
						}
						spawnListEntry.getLivingHandler().postSpawnEntity(entityliving, spawnListEntry, countInfo);
						countInfo.countSpawn(entityliving, creatureType.typeID);
						flag = true;
					} else {
						JASLog.log().debug(Level.INFO,
								"Entity not Spawned due to invalid creatureType location. Creature Type was %s",
								creatureType.typeID);
					}

					j1 += random.nextInt(5) - random.nextInt(5);

					for (k1 += random.nextInt(5) - random.nextInt(5); j1 < par2 || j1 >= par2 + par4 || k1 < par3
							|| k1 >= par3 + par4; k1 = i2 + random.nextInt(5) - random.nextInt(5)) {
						j1 = l1 + random.nextInt(5) - random.nextInt(5);
					}
				}
			}
		}
	}
}
