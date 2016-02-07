package jas.spawner.refactor.spawning;

import jas.common.JASLog;
import jas.common.global.BiomeBlacklist;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.BiomeSpawnLists;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.SpawnSettings.BiomeSettings;
import jas.spawner.refactor.SpawnSettings.LivingSettings;
import jas.spawner.refactor.SpawnerHelper.Counter;
import jas.spawner.refactor.SpawnerHelper.SpawnerLogic;
import jas.spawner.refactor.SpawnerHelper.Counter.SpawnCounter;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.mvel.MVELExpression;
import jas.spawner.refactor.structure.StructureHandlers;
import jas.spawner.refactor.structure.StructureHandlerBuilder.StructureHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;

import com.google.common.base.Optional;

import cpw.mods.fml.common.eventhandler.Event.Result;

public class WorldSpawningLogic implements SpawnerLogic {
	@Override
	public Counter counter(World world) {
		return new SpawnCounter();
	}

	@Override
	public void spawnCycle(World worldServer, CountInfo countInfo, LivingType creatureType) {

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
