package jas.spawner.modern.spawner;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.global.BiomeBlacklist;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.Counter.SpawnCounter;
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
	public static SpawnCounter spawnCounter = new SpawnCounter();

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
		final int entityTypeCap = creatureType.maxNumberOfCreature * countInfo.eligibleChunkLocations().size() / 256;
		int globalEntityTypeCount = countInfo.getGlobalEntityTypeCount(creatureType.typeID);
		if (globalEntityTypeCount > entityTypeCap) {
			return;
		}
		ChunkCoordinates serverOriginPoint = worldServer.getSpawnPoint();
		List<ChunkCoordIntPair> eligibleChunksForSpawning = new ArrayList<ChunkCoordIntPair>(
				countInfo.eligibleChunkLocations());
		Collections.shuffle(eligibleChunksForSpawning);
		labelChunkStart: for (ChunkCoordIntPair chunkCoord : eligibleChunksForSpawning) {
			ChunkStat chunkStat = countInfo.getChunkStat(chunkCoord);
			if (chunkStat.isEdge) {
				continue;
			}
			countInfo.resetEntitiesSpawnedThisLoop();
			for (int numLocAttempts = 0; numLocAttempts < creatureType.iterationsPerChunk; ++numLocAttempts) {
				IEntityLivingData entitylivingdata = null;
				ChunkPosition startSpawningPoint = creatureType.getRandomSpawningPointInChunk(worldServer,
						chunkCoord.chunkXPos, chunkCoord.chunkZPos);
				
				SpawnListEntry spawnlistentry = null;
				Class<? extends EntityLiving> livingToSpawn = null;
				LivingHandler handler = null;
				countInfo.resetEntitiesPackCount();
				
				// CreatureType
				if (!creatureType.canSpawnHere(worldServer, countInfo, startSpawningPoint)) {
					continue;
				}
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
						break;
					}

					if (isNearPlayerOrOrigin(worldServer, serverOriginPoint, spawningPoint.chunkPosX,
							spawningPoint.chunkPosY, spawningPoint.chunkPosZ)) {
						continue;
					}

					// Set SpawnList Specific attributes, set only for outer loop (when SpawnListEntry == null), is done
					// in inner loop after creatureType.canSpawnHere for performance reasons
					// (regsitry.getSpawnListEntryToSpawn is not cheap)
					if (spawnlistentry == null) {
						spawnlistentry = biomeSpawnListRegistry.getSpawnListEntryToSpawn(worldServer, creatureType,
								startSpawningPoint.chunkPosX, startSpawningPoint.chunkPosY,
								startSpawningPoint.chunkPosZ);
						if (spawnlistentry == null) {
							break;
						}
						Tags tags = new Tags(worldServer, countInfo, startSpawningPoint.chunkPosX,
								startSpawningPoint.chunkPosY, startSpawningPoint.chunkPosZ);
						livingToSpawn = livingHandlerRegistry.getRandomEntity(spawnlistentry.livingGroupID,
								worldServer.rand, tags);
						if (livingToSpawn == null) {
							break;
						}
						handler = livingHandlerRegistry.getLivingHandler(spawnlistentry.livingGroupID);
					}

					// LivingCap
					{
						int globalEntityClassCount = countInfo.getGlobalEntityClassCount(livingToSpawn);
						int livingCap = handler.getLivingCap();

						if (livingCap > 0 && globalEntityClassCount >= livingCap) {
							spawnlistentry = null;
							break;
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

					if (spawnlistentry.getLivingHandler().getCanSpawnHere(entityliving, spawnlistentry, countInfo)) {
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
						
						// Living PackSize
						if (countInfo.getEntitiesSpawnedThisLoop() >= spawnlistentry.packSize) {
							continue labelChunkStart;
						}
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
			CountInfo countInfo = CustomSpawner.spawnCounter.countEntities(world);
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
						if (spawnListEntry.getLivingHandler().getCanSpawnHere(entityliving, spawnListEntry, countInfo)) {
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
						}
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
