package jas.common.spawner;

import jas.common.BiomeBlacklist;
import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.EntityCounter.CountableInt;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingData;
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
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeEventFactory;

public class CustomSpawner {

    /**
     * Populates eligibleChunksForSpawning with All Valid Chunks. Unlike its vanilla counterpart
     * {@link SpawnerAnimals#findChunksForSpawning} this does not spawn a Creature.
     * 
     * @param worldServer
     * @param par1 should Spawn spawnHostileMobs
     * @param par2 should Spawn spawnPeacefulMobs
     * @param par3 worldInfo.getWorldTotalTime() % 400L == 0L
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

                    Chunk chunk = worldServer.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos,
                            chunkcoordintpair.chunkZPos);
                    int count = 0;
                    for (@SuppressWarnings("rawtypes")
                    List entityLists : chunk.entityLists) {
                        for (Object object : entityLists) {
                            if (object instanceof EntityLiving) {
                                count++;
                            }
                        }
                    }
                    
                    if (!flag3) {
                        eligibleChunksForSpawning.put(chunkcoordintpair, new ChunkStat(false, count));
                    } else if (!eligibleChunksForSpawning.containsKey(chunkcoordintpair)) {
                        eligibleChunksForSpawning.put(chunkcoordintpair, new ChunkStat(true, count));
                    }
                }
            }
        }
        return eligibleChunksForSpawning;
    }

    public static class ChunkStat {
        public final boolean isEdge;
        public final int entityCount;

        public ChunkStat(boolean isEdge, int entityCount) {
            this.isEdge = isEdge;
            this.entityCount = entityCount;
        }
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
            List<LivingHandler> livingHandlers = JustAnotherSpawner.worldSettings().livingHandlerRegistry()
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
     * @param creatureType CreatureType spawnList that is being Spawned
     */
    public static final void spawnCreaturesInChunks(WorldServer worldServer,
            LivingHandlerRegistry livingHandlerRegistry, LivingGroupRegistry livingGroupRegistry,
            CreatureType creatureType, HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning,
            EntityCounter creatureTypeCount, EntityCounter creatureCount, BiomeBlacklist blacklist) {
        ChunkCoordinates chunkcoordinates = worldServer.getSpawnPoint();

        CountableInt typeCount = creatureTypeCount.getOrPutIfAbsent(creatureType.typeID, 0);
        int entityTypeCap = creatureType.maxNumberOfCreature * eligibleChunksForSpawning.size() / 256;
        if (typeCount.get() <= entityTypeCap) {
            Iterator<ChunkCoordIntPair> iterator = eligibleChunksForSpawning.keySet().iterator();
            ArrayList<ChunkCoordIntPair> tmp = new ArrayList<ChunkCoordIntPair>(eligibleChunksForSpawning.keySet());
            Collections.shuffle(tmp);
            iterator = tmp.iterator();
            labelChunkStart:

            while (iterator.hasNext()) {
                ChunkCoordIntPair chunkCoord = iterator.next();
                ChunkStat chunkStat = eligibleChunksForSpawning.get(chunkCoord);
                if (!chunkStat.isEdge) {
                    int biomeCap = creatureType.getChunkCap(worldServer.getChunkFromChunkCoords(chunkCoord.chunkXPos,
                            chunkCoord.chunkZPos));
                    if (biomeCap > -1 && chunkStat.entityCount >= biomeCap) {
                        continue;
                    }
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
                            Class<? extends EntityLiving> livingToSpawn = null;
                            EntityLivingData entitylivingdata = null;
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

                                    if (blacklist.isBlacklisted(worldServer.getBiomeGenForCoords(blockSpawnX,
                                            blockSpawnZ))) {
                                        continue labelChunkStart;
                                    }

                                    if (worldServer.getClosestPlayer(spawnX, spawnY, spawnZ, 24.0D) == null) {
                                        float xOffset = spawnX - chunkcoordinates.posX;
                                        float yOffset = spawnY - chunkcoordinates.posY;
                                        float zOffset = spawnZ - chunkcoordinates.posZ;
                                        float sqOffset = xOffset * xOffset + yOffset * yOffset + zOffset * zOffset;

                                        if (sqOffset < 576.0F) {
                                            continue;
                                        }

                                        if (spawnlistentry == null) {
                                            BiomeSpawnListRegistry biomeSpawnListRegistry = JustAnotherSpawner
                                                    .worldSettings().biomeSpawnListRegistry();
                                            spawnlistentry = biomeSpawnListRegistry.getSpawnListEntryToSpawn(
                                                    worldServer, creatureType, blockSpawnX, blockSpawnY, blockSpawnZ);
                                            if (spawnlistentry == null) {
                                                continue;
                                            }
                                            livingToSpawn = livingGroupRegistry.getRandomEntity(
                                                    spawnlistentry.livingGroupID, worldServer.rand);
                                            if (livingToSpawn == null) {
                                                spawnlistentry = null;
                                                continue;
                                            }

                                            LivingHandler handler = livingHandlerRegistry
                                                    .getLivingHandler(spawnlistentry.livingGroupID);
                                            livingCount = creatureCount.getOrPutIfAbsent(livingToSpawn.getSimpleName(),
                                                    0);
                                            livingCap = handler.getLivingCap();

                                            if (typeCount.get() > entityTypeCap) {
                                                return;
                                            }

                                            if (livingCap > 0 && livingCount.get() >= livingCap) {
                                                spawnlistentry = null;
                                                continue;
                                            }
                                        } else {
                                            livingToSpawn = livingGroupRegistry.getRandomEntity(
                                                    spawnlistentry.livingGroupID, worldServer.rand);
                                            if (livingToSpawn == null) {
                                                spawnlistentry = null;
                                                continue;
                                            }
                                            livingCount = creatureCount.getOrPutIfAbsent(livingToSpawn.getSimpleName(),
                                                    0);
                                        }

                                        EntityLiving entityliving;
                                        try {
                                            entityliving = livingToSpawn.getConstructor(new Class[] { World.class })
                                                    .newInstance(new Object[] { worldServer });
                                        } catch (Exception exception) {
                                            exception.printStackTrace();
                                            return;
                                        }

                                        entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ,
                                                worldServer.rand.nextFloat() * 360.0F, 0.0F);

                                        Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, worldServer,
                                                spawnX, spawnY, spawnZ);
                                        if (canSpawn == Result.ALLOW
                                                || (canSpawn == Result.DEFAULT && spawnlistentry.getLivingHandler()
                                                        .getCanSpawnHere(entityliving, spawnlistentry))) {
                                            ++j2;
                                            worldServer.spawnEntityInWorld(entityliving);
                                            if (!ForgeEventFactory.doSpecialSpawn(entityliving, worldServer, spawnX,
                                                    spawnY, spawnZ)) {
                                                entitylivingdata = entityliving.onSpawnWithEgg(entitylivingdata);
                                            }
                                            JASLog.logSpawn(false, (String) EntityList.classToStringMapping
                                                    .get(entityliving.getClass()),
                                                    spawnlistentry.getLivingHandler().creatureTypeID,
                                                    (int) entityliving.posX, (int) entityliving.posY,
                                                    (int) entityliving.posZ, BiomeHelper
                                                            .getPackageName(entityliving.worldObj.getBiomeGenForCoords(
                                                                    (int) entityliving.posX, (int) entityliving.posZ)));
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

    /**
     * Called during chunk generation to spawn initial creatures.
     */
    public static void performWorldGenSpawning(World world, CreatureType creatureType,
            LivingGroupRegistry livingGroupRegistry, BiomeGenBase biome, int par2, int par3, int par4, int par5,
            Random random) {
        if (random.nextFloat() < creatureType.chunkSpawnChance) {
            int j1 = par2 + random.nextInt(par4);
            int k1 = par3 + random.nextInt(par5);
            int l1 = j1;
            int i2 = k1;
            BiomeSpawnListRegistry biomeSpawnListRegistry = JustAnotherSpawner.worldSettings().biomeSpawnListRegistry();
            SpawnListEntry spawnListEntry = biomeSpawnListRegistry.getSpawnListEntryToSpawn(world, creatureType, j1,
                    world.getTopSolidOrLiquidBlock(j1, k1), k1);
            EntityLivingData entitylivingdata = null;
            if (spawnListEntry == null) {
                JASLog.debug(Level.INFO, "Entity not Spawned due to Empty %s List", creatureType.typeID);
                return;
            } else {
                JASLog.debug(Level.INFO, "Evaluating if We Should spawn entity group %s", spawnListEntry.livingGroupID);
            }
            int i1 = spawnListEntry.minChunkPack
                    + random.nextInt(1 + spawnListEntry.maxChunkPack - spawnListEntry.minChunkPack);
            for (int j2 = 0; j2 < i1; ++j2) {
                boolean flag = false;
                Class<? extends EntityLiving> livingToSpawn = livingGroupRegistry.getRandomEntity(
                        spawnListEntry.livingGroupID, world.rand);
                if (livingToSpawn == null) {
                    JASLog.severe("No EntityClasses appear to exist in %s", spawnListEntry.toString());
                    continue;
                }
                for (int k2 = 0; !flag && k2 < 4; ++k2) {
                    int l2 = world.getTopSolidOrLiquidBlock(j1, k1);

                    if (creatureType.canSpawnAtLocation(world, j1, l2, k1)) {
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
                        JASLog.logSpawn(true, (String) EntityList.classToStringMapping.get(entityliving.getClass()),
                                spawnListEntry.getLivingHandler().creatureTypeID, (int) entityliving.posX,
                                (int) entityliving.posY, (int) entityliving.posZ, BiomeHelper
                                        .getPackageName(entityliving.worldObj.getBiomeGenForCoords(
                                                (int) entityliving.posX, (int) entityliving.posZ)));
                        world.spawnEntityInWorld(entityliving);
                        if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, f, f1, f2)) {
                            entitylivingdata = entityliving.onSpawnWithEgg(entitylivingdata);
                        }
                        flag = true;
                    } else {
                        JASLog.debug(Level.INFO,
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
