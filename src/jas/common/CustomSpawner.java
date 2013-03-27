package jas.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

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
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeEventFactory;

public class CustomSpawner {
    /** The 17x17 area around the player where mobs can spawn */
    private static HashMap<ChunkCoordIntPair, Boolean> eligibleChunksForSpawning = new HashMap<ChunkCoordIntPair, Boolean>();

    /**
     * Populates eligibleChunksForSpawning with All Valid Chunks. Unlike its vanilla counterpart
     * {@link SpawnerAnimals#findChunksForSpawning} this does not spawn a Creature.
     * 
     * @param worldServer
     * @param par1 should Spawn spawnHostileMobs
     * @param par2 should Spawn spawnPeacefulMobs
     * @param par3 worldInfo.getWorldTotalTime() % 400L == 0L
     */
    public static final void determineChunksForSpawnering(WorldServer par0WorldServer, boolean par1, boolean par2,
            boolean par3) {
        eligibleChunksForSpawning.clear();
        int i;
        int j;
        for (i = 0; i < par0WorldServer.playerEntities.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) par0WorldServer.playerEntities.get(i);
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
     * Performs Actual Creature Spawning inside eligibleChunks. {@link determineChunksForSpawnering} needs to be run to
     * populate eligibleChunksForSpawning with spawnable chunks
     * 
     * @param creatureType CreatureType spawnList that is being Spawned
     */
    //TODO: Why does this return integer? Debugging? What is the value?
    public static final int spawnCreaturesInChunks(WorldServer worldServer, CreatureType creatureType) {
        int i = 0;
        ChunkCoordinates chunkcoordinates = worldServer.getSpawnPoint();
        if (countCreatureType(worldServer, creatureType) <= creatureType.maxNumberOfCreature
                * eligibleChunksForSpawning.size() / 256) {
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
                        int k2 = 0;
                        while (k2 < 3) { //TODO: This Screams For Loop
                            int l2 = k1;
                            int i3 = l1;
                            int j3 = i2;
                            byte b1 = 6;
                            SpawnListEntry spawnlistentry = null;
                            int k3 = 0;
                            while (true) {
                                if (k3 < 4) { //TODO: This Screams For Loop
                                    labelInside: {
                                        l2 += worldServer.rand.nextInt(b1) - worldServer.rand.nextInt(b1);
                                        i3 += worldServer.rand.nextInt(1) - worldServer.rand.nextInt(1);
                                        j3 += worldServer.rand.nextInt(b1) - worldServer.rand.nextInt(b1);
                                        if (creatureType.canSpawnAtLocation(worldServer, l2, i3, j3)) {
                                            float f = l2 + 0.5F;
                                            float f1 = i3;
                                            float f2 = j3 + 0.5F;

                                            if (worldServer.getClosestPlayer(f, f1, f2, 24.0D) == null) {
                                                float f3 = f - chunkcoordinates.posX;
                                                float f4 = f1 - chunkcoordinates.posY;
                                                float f5 = f2 - chunkcoordinates.posZ;
                                                float f6 = f3 * f3 + f4 * f4 + f5 * f5;

                                                if (f6 >= 576.0F) {
                                                    if (spawnlistentry == null) {
                                                        spawnlistentry = creatureType.getSpawnListEntry(worldServer, l2, i3, j3);
                                                        if (spawnlistentry == null) {
                                                            break labelInside; //TODO: Coulnd't This be Continue?
                                                        }
                                                    }

                                                    EntityLiving entityliving;

                                                    try {
                                                        entityliving = (EntityLiving) spawnlistentry.entityClass
                                                                .getConstructor(new Class[] { World.class })
                                                                .newInstance(new Object[] { worldServer });
                                                    } catch (Exception exception) {
                                                        exception.printStackTrace();
                                                        return i;
                                                    }

                                                    entityliving.setLocationAndAngles(f, f1, f2,
                                                            worldServer.rand.nextFloat() * 360.0F, 0.0F);

                                                    Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving,
                                                            worldServer, f, f1, f2);
                                                    if (canSpawn == Result.ALLOW
                                                            || (canSpawn == Result.DEFAULT && entityliving
                                                                    .getCanSpawnHere())) {
                                                        ++j2;
                                                        worldServer.spawnEntityInWorld(entityliving);
                                                        creatureSpecificInit(entityliving, worldServer, f, f1, f2);

                                                        if (j2 >= entityliving.getMaxSpawnedInChunk()) {
                                                            continue labelChunkStart;
                                                        }
                                                    }

                                                    i += j2;

                                                }
                                            }
                                        }
                                        ++k3;
                                        continue;
                                    }
                                }
                                ++k2;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return i;
    }
    
    private static final int countCreatureType(WorldServer worldServer, CreatureType creatureType){
        int count = 0;
        @SuppressWarnings("unchecked")
        Iterator<? extends Entity> creatureIterator = worldServer.loadedEntityList.iterator();
        while (creatureIterator.hasNext()) {
            Entity entity = creatureIterator.next();
            if(creatureType.isEntityOfType(entity)){
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
}
