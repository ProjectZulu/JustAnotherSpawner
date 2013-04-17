package jas.common.spawner;

import jas.common.JASLog;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeEventFactory;

public class CustomSpawner {
    /** The 17x17 area around the player where mobs can spawn */
    private static ConcurrentHashMap<ChunkCoordIntPair, Boolean> eligibleChunksForSpawning = new ConcurrentHashMap<ChunkCoordIntPair, Boolean>();

    private static ConcurrentHashMap<String, CountableInt> creatureTypeCount = new ConcurrentHashMap<String, CountableInt>();
    private static ConcurrentHashMap<String, CountableInt> creatureCount = new ConcurrentHashMap<String, CountableInt>();

    private static class CountableInt {
        int value = 1;

        CountableInt(int startValue) {
            value = startValue;
        }

        public int increment() {
            return ++value;
        }

        public int get() {
            return value;
        }
    }

    /**
     * Populates eligibleChunksForSpawning with All Valid Chunks. Unlike its vanilla counterpart
     * {@link SpawnerAnimals#findChunksForSpawning} this does not spawn a Creature.
     * 
     * @param worldServer
     * @param par1 should Spawn spawnHostileMobs
     * @param par2 should Spawn spawnPeacefulMobs
     * @param par3 worldInfo.getWorldTotalTime() % 400L == 0L
     */
    public static final void determineChunksForSpawnering(WorldServer worldServer) {
        eligibleChunksForSpawning.clear();
        int i;
        int j;
        for (i = 0; i < worldServer.playerEntities.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) worldServer.playerEntities.get(i);
            int k = MathHelper.floor_double(entityplayer.posX / 16.0D);
            j = MathHelper.floor_double(entityplayer.posZ / 16.0D);
            byte b0 = 8;

            for (int l = -b0; l <= b0; ++l) {
                for (int i1 = -b0; i1 <= b0; ++i1) {
                    boolean flag3 = l == -b0 || l == b0 || i1 == -b0 || i1 == b0;
                    ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(l + k, i1 + j);

                    if (!flag3) {
                        eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(false));
                    } else if (!eligibleChunksForSpawning.containsKey(chunkcoordintpair)) {
                        eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(true));
                    }
                }
            }
        }
    }

    /**
     * Count and Cache the Amount of Loaded Entities
     * 
     * @param worldServer
     */
    public static void countEntityInChunks(WorldServer worldServer) {
        creatureTypeCount.clear();
        creatureCount.clear();
        @SuppressWarnings("unchecked")
        Iterator<? extends Entity> creatureIterator = worldServer.loadedEntityList.iterator();
        while (creatureIterator.hasNext()) {
            Entity entity = creatureIterator.next();
            LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(entity.getClass());
            if (livingHandler != null) {
                incrementOrPutIfAbsent(livingHandler.creatureTypeID, creatureTypeCount, 1);
                incrementOrPutIfAbsent(entity.getClass().getSimpleName(), creatureCount, 1);
            }
        }
    }

    /**
     * Gets or Puts if absent a CountableInt from the provided Map.
     * 
     * @param key Key for Value inside the hash we want to get
     * @param countingHash Hash that is being used for Counting
     * @param defaultValue Default Value CountableInt is initialized to
     * @return CountableInt that has not been iterated
     */
    private static CountableInt getOrPutIfAbsent(String key, ConcurrentHashMap<String, CountableInt> countingHash,
            int defaultValue) {
        CountableInt count = countingHash.get(key);
        if (count == null) {
            count = new CountableInt(defaultValue);
            countingHash.put(key, count);
        }
        return count;
    }

    /**
     * Gets or Puts if absent a CountableInt from the provided Map.
     * 
     * @param key Key for Value inside the hash we want to iterate
     * @param countingHash Hash that is being used for Counting
     * @param defaultValue Default Value CountableInt is initialized to
     * @return CountableInt that has been iterated
     */
    private static CountableInt incrementOrPutIfAbsent(String key,
            ConcurrentHashMap<String, CountableInt> countingHash, int defaultValue) {
        CountableInt count = countingHash.get(key);
        if (count == null) {
            count = new CountableInt(defaultValue);
            countingHash.put(key, count);
        } else {
            count.increment();
        }
        return count;
    }

    /**
     * Performs Actual Creature Spawning inside eligibleChunks. {@link determineChunksForSpawnering} needs to be run to
     * populate eligibleChunksForSpawning with spawnable chunks
     * 
     * @param creatureType CreatureType spawnList that is being Spawned
     */
    public static final void spawnCreaturesInChunks(WorldServer worldServer, CreatureType creatureType) {
        ChunkCoordinates chunkcoordinates = worldServer.getSpawnPoint();

        CountableInt typeCount = getOrPutIfAbsent(creatureType.typeID, creatureTypeCount, 0);
        int entityTypeCap = creatureType.maxNumberOfCreature * eligibleChunksForSpawning.size() / 256;
        if (typeCount.get() <= entityTypeCap) {
            Iterator<ChunkCoordIntPair> iterator = eligibleChunksForSpawning.keySet().iterator();
            ArrayList<ChunkCoordIntPair> tmp = new ArrayList<ChunkCoordIntPair>(eligibleChunksForSpawning.keySet());
            Collections.shuffle(tmp);
            iterator = tmp.iterator();
            labelChunkStart:

            while (iterator.hasNext()) {
                ChunkCoordIntPair chunkCoord = iterator.next();
                if (!eligibleChunksForSpawning.get(chunkCoord).booleanValue()) {
                    ChunkPosition chunkposition = creatureType.getRandomSpawningPointInChunk(worldServer,
                            chunkCoord.chunkXPos, chunkCoord.chunkZPos);
                    int k1 = chunkposition.x;
                    int l1 = chunkposition.y;
                    int i2 = chunkposition.z;

                    if (creatureType.isValidMedium(worldServer, k1, l1, i2)) {
                        int j2 = 0;
                        for (int k2 = 0; k2 < 3; ++k2) {
                            int blockSpawnX = k1;
                            int blockSpawnY = l1;
                            int blockSpawnZ = i2;
                            byte variance = 6;
                            SpawnListEntry spawnlistentry = null;
                            CountableInt livingCount = null;
                            int livingCap = 0;
                            for (int k3 = 0; k3 < 4; ++k3) {
                                blockSpawnX += worldServer.rand.nextInt(variance) - worldServer.rand.nextInt(variance);
                                blockSpawnY += worldServer.rand.nextInt(1) - worldServer.rand.nextInt(1);
                                blockSpawnZ += worldServer.rand.nextInt(variance) - worldServer.rand.nextInt(variance);
                                if (creatureType.canSpawnAtLocation(worldServer, blockSpawnX, blockSpawnY, blockSpawnZ)) {
                                    /* Spawn is Centered Version of blockSpawn such that entity is not placed in Corner */
                                    float spawnX = blockSpawnX + 0.5F;
                                    float spawnY = blockSpawnY;
                                    float spawnZ = blockSpawnZ + 0.5F;

                                    if (worldServer.getClosestPlayer(spawnX, spawnY, spawnZ, 24.0D) == null) {
                                        float xOffset = spawnX - chunkcoordinates.posX;
                                        float yOffset = spawnY - chunkcoordinates.posY;
                                        float zOffset = spawnZ - chunkcoordinates.posZ;
                                        float sqOffset = xOffset * xOffset + yOffset * yOffset + zOffset * zOffset;

                                        if (sqOffset < 576.0F) {
                                            continue;
                                        }

                                        if (spawnlistentry == null) {
                                            spawnlistentry = creatureType.getSpawnListEntryToSpawn(worldServer,
                                                    blockSpawnX, blockSpawnY, blockSpawnZ);
                                            if (spawnlistentry == null) {
                                                continue;
                                            }
                                            livingCount = getOrPutIfAbsent(spawnlistentry.livingClass.getSimpleName(),
                                                    CustomSpawner.creatureCount, 0);
                                            livingCap = CreatureHandlerRegistry.INSTANCE.getLivingHandler(
                                                    spawnlistentry.livingClass).getLivingCap();

                                            if (typeCount.get() > entityTypeCap) {
                                                return;
                                            }

                                            if (livingCap > 0 && livingCount.get() >= livingCap) {
                                                spawnlistentry = null;
                                                continue;
                                            }
                                        }

                                        EntityLiving entityliving;
                                        try {
                                            entityliving = spawnlistentry.getLivingHandler().entityClass
                                                    .getConstructor(new Class[] { World.class }).newInstance(
                                                            new Object[] { worldServer });
                                        } catch (Exception exception) {
                                            exception.printStackTrace();
                                            return;
                                        }

                                        entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ,
                                                worldServer.rand.nextFloat() * 360.0F, 0.0F);

                                        Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, worldServer,
                                                spawnX, spawnY, spawnZ);
                                        JASLog.info("Can Spawn %s", canSpawn);
                                        if (canSpawn == Result.ALLOW
                                                || (canSpawn == Result.DEFAULT && spawnlistentry.getLivingHandler()
                                                        .getCanSpawnHere(entityliving))) {
                                            ++j2;
                                            JASLog.info("Spawning Creature %s at %s, %s, %s ",
                                                    entityliving.getEntityName(), entityliving.posX, entityliving.posY,
                                                    entityliving.posZ);
                                            worldServer.spawnEntityInWorld(entityliving);
                                            creatureSpecificInit(entityliving, worldServer, spawnX, spawnY, spawnZ);
                                            typeCount.increment();
                                            livingCount.increment();

                                            if (j2 >= spawnlistentry.packSize) {
                                                continue labelChunkStart;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static final int countCreatureType(WorldServer worldServer, CreatureType creatureType) {
        int count = 0;
        @SuppressWarnings("unchecked")
        Iterator<? extends Entity> creatureIterator = worldServer.loadedEntityList.iterator();
        while (creatureIterator.hasNext()) {
            Entity entity = creatureIterator.next();
            if (creatureType.isEntityOfType(entity)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Used to Trigger Special Creature Conditions, such as Skeletons Riding Spiders
     */
    private static void creatureSpecificInit(EntityLiving entityLiving, World par1World, float xCoord, float yCoord,
            float zCoord) {
        if (ForgeEventFactory.doSpecialSpawn(entityLiving, par1World, xCoord, yCoord, zCoord)) {
            return;
        }
        entityLiving.initCreature();
    }

    /**
     * Called during chunk generation to spawn initial creatures.
     */
    public static void performWorldGenSpawning(World world, CreatureType creatureType, BiomeGenBase biome, int par2,
            int par3, int par4, int par5, Random random) {
        while (random.nextFloat() < biome.getSpawningChance()) {
            int j1 = par2 + random.nextInt(par4);
            int k1 = par3 + random.nextInt(par5);
            int l1 = j1;
            int i2 = k1;
            SpawnListEntry spawnListEntry = creatureType.getSpawnListEntryToSpawn(world,
                    BiomeHelper.getPackageName(biome), j1, world.getTopSolidOrLiquidBlock(j1, k1), k1);
            if (spawnListEntry == null) {
                JASLog.debug(Level.INFO, "Entity not Spawned due to Empty %s List", creatureType.typeID);
                return;
            } else {
                JASLog.debug(Level.INFO, "Evaluating if We Should spawn %s", spawnListEntry.livingClass.getSimpleName());
            }
            int i1 = spawnListEntry.minChunkPack
                    + random.nextInt(1 + spawnListEntry.maxChunkPack - spawnListEntry.minChunkPack);
            for (int j2 = 0; j2 < i1; ++j2) {
                boolean flag = false;

                for (int k2 = 0; !flag && k2 < 4; ++k2) {
                    int l2 = world.getTopSolidOrLiquidBlock(j1, k1);

                    if (creatureType.canSpawnAtLocation(world, j1, l2, k1)) {
                        float f = j1 + 0.5F;
                        float f1 = l2;
                        float f2 = k1 + 0.5F;
                        EntityLiving entityliving;

                        try {
                            entityliving = spawnListEntry.getLivingHandler().entityClass.getConstructor(
                                    new Class[] { World.class }).newInstance(new Object[] { world });
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            continue;
                        }

                        entityliving.setLocationAndAngles(f, f1, f2, random.nextFloat() * 360.0F, 0.0F);
                        JASLog.info("Chunk Spawned %s at %s, %s, %s ", entityliving.getEntityName(), entityliving.posX,
                                entityliving.posY, entityliving.posZ);
                        world.spawnEntityInWorld(entityliving);
                        creatureSpecificInit(entityliving, world, f, f1, f2);
                        flag = true;
                    } else {
                        JASLog.debug(Level.INFO, "Entity not Spawned due to invalid creatureType location",
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
