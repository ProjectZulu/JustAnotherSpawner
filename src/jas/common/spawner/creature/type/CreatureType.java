package jas.common.spawner.creature.type;

import static net.minecraftforge.common.ForgeDirection.UP;
import jas.common.JASLog;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.biome.structure.BiomeHandler;
import jas.common.spawner.biome.structure.BiomeHandlerRegistry;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStep;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.Configuration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ListMultimap;

//TODO: Large Constructor could probably use Factory OR String optionalParameters to consolidate unused properties
public class CreatureType {
    public final String typeID;
    public final int spawnRate;
    public final int maxNumberOfCreature;
    public final boolean chunkSpawning;
    public final Material spawnMedium;
    private final ListMultimap<String, SpawnListEntry> groupNameToSpawnEntry = ArrayListMultimap.create();

    public CreatureType(String typeID, int maxNumberOfCreature, Material spawnMedium, int spawnRate,
            boolean chunkSpawning) {
        this.typeID = typeID;
        this.maxNumberOfCreature = maxNumberOfCreature;
        this.spawnMedium = spawnMedium;
        this.spawnRate = spawnRate;
        this.chunkSpawning = chunkSpawning;
    }

    public final CreatureType maxNumberOfCreatureTo(int maxNumberOfCreature) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    public final CreatureType spawnRateTo(int spawnRate) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    public final CreatureType chunkSpawningTo(boolean chunkSpawning) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    /**
     * Used internally to create a new Instance of CreatureType. MUST be Overriden by Subclasses so that they are not
     * replaced with Parent. Used to Allow subclasses to Include their own Logic, but maintain same data structure.
     * 
     * Should create a new instance of class using parameters provided in the constructor.
     * 
     * @param typeID
     * @param maxNumberOfCreature
     * @param spawnMedium
     * @param spawnRate
     * @param chunkSpawning
     */
    protected CreatureType constructInstance(String typeID, int maxNumberOfCreature, Material spawnMedium,
            int spawnRate, boolean chunkSpawning) {
        return new CreatureType(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    /**
     * Adds a SpawnlistEntry to the corresponding SpawnList using the biomeName as key
     * 
     * @param pckgName
     * @param spawnListEntry
     */
    public void addSpawn(SpawnListEntry spawnListEntry) {
        groupNameToSpawnEntry.get(spawnListEntry.pckgName).add(spawnListEntry);
    }

    /**
     * Removes the SpawnListEntry defined by biomeName and LivingClass from the CreatureType.
     * 
     * @param pckgName
     * @param livingClass
     * @return
     */
    public boolean removeSpawn(String groupName, Class<? extends EntityLiving> livingClass) {
        Iterator<SpawnListEntry> iterator = groupNameToSpawnEntry.get(groupName).iterator();
        while (iterator.hasNext()) {
            SpawnListEntry spawnListEntry = iterator.next();
            if (livingClass.equals(spawnListEntry.livingClass) && spawnListEntry.pckgName.equals(groupName)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Get All Spawns For all Biomes that are of Type CreatureType
     * 
     * @return
     */
    public Collection<SpawnListEntry> getAllSpawns() {
        return groupNameToSpawnEntry.values();
    }

    /**
     * Performs Remove and Add Spawn operation
     * 
     * @param pckgName
     * @param livingClass
     */
    public void updateOrAddSpawn(SpawnListEntry spawnListEntry) {
        ListIterator<SpawnListEntry> iterator = groupNameToSpawnEntry.get(spawnListEntry.pckgName).listIterator();
        while (iterator.hasNext()) {
            SpawnListEntry listEntry = iterator.next();
            if (listEntry.livingClass.equals(spawnListEntry.livingClass)
                    && listEntry.pckgName.equals(spawnListEntry.pckgName)) {
                iterator.set(spawnListEntry);
                return;
            }
        }
        addSpawn(spawnListEntry);
    }

    /**
     * Resets All Spawn Lists. This is used on World Change
     */
    public void resetSpawns() {
        groupNameToSpawnEntry.clear();
    }

    /**
     * Called by CustomSpawner for a Passive Spawn Entity
     * 
     * @param world
     * @param xCoord Random xCoordinate nearby to Where Creature will spawn
     * @param yCoord Random yCoordinate nearby to Where Creature will spawn
     * @param zCoord Random zCoordinate nearby to Where Creature will spawn
     * @return Creature to Spawn
     */
    @SuppressWarnings("unchecked")
    public SpawnListEntry getSpawnListEntryToSpawn(World world, int xCoord, int yCoord, int zCoord) {
        Collection<SpawnListEntry> structureSpawnList = getStructureSpawnList(world, xCoord, yCoord, zCoord);
        if (!structureSpawnList.isEmpty()) {
            JASLog.debug(Level.INFO, "Structure SpawnListEntry found for ChunkSpawning at %s, %s, %s", xCoord, yCoord,
                    zCoord);
            SpawnListEntry spawnListEntry = (SpawnListEntry) WeightedRandom.getRandomItem(world.rand,
                    structureSpawnList);
            return isEntityOfType(spawnListEntry.livingClass) ? spawnListEntry : null;
        }
        BiomeGenBase biomegenbase = world.getBiomeGenForCoords(xCoord, zCoord);
        if (biomegenbase != null) {
            ImmutableCollection<String> groupIDList = BiomeGroupRegistry.INSTANCE.getPackgNameToGroupIDList().get(
                    BiomeHelper.getPackageName(biomegenbase));
            return getRandomEntry(world.rand, groupIDList);
        }
        return null;
    }

    /**
     * Called by CustomSpawner for a Chunk Spawn Entity
     * 
     * @param pckgName Name of Biome SpawnList to get CreatureType From
     * @return
     */
    public SpawnListEntry getSpawnListEntryToSpawn(World world, String packgName, int xCoord, int yCoord, int zCoord) {
        Collection<SpawnListEntry> structureSpawnList = getStructureSpawnList(world, xCoord, yCoord, zCoord);
        if (!structureSpawnList.isEmpty()) {
            JASLog.debug(Level.INFO, "Structure SpawnListEntry found for ChunkSpawning at %s, %s, %s", xCoord, yCoord,
                    zCoord);
            SpawnListEntry spawnListEntry = (SpawnListEntry) WeightedRandom.getRandomItem(world.rand,
                    structureSpawnList);
            return isEntityOfType(spawnListEntry.livingClass) ? spawnListEntry : null;
        }

        BiomeGenBase biomegenbase = world.getBiomeGenForCoords(xCoord, zCoord);
        if (biomegenbase != null) {
            ImmutableCollection<String> groupIDList = BiomeGroupRegistry.INSTANCE.getPackgNameToGroupIDList().get(
                    BiomeHelper.getPackageName(biomegenbase));
            return getRandomEntry(world.rand, groupIDList);
        }
        return null;
    }

    private Collection<SpawnListEntry> getStructureSpawnList(World world, int xCoord, int yCoord, int zCoord) {
        Iterator<BiomeHandler> iterator = BiomeHandlerRegistry.INSTANCE.getHandlers();
        while (iterator.hasNext()) {
            BiomeHandler handler = iterator.next();
            if (handler.doesHandlerApply(world, xCoord, yCoord, zCoord)) {
                Collection<SpawnListEntry> spawnEntryList = handler
                        .getStructureSpawnList(world, xCoord, yCoord, zCoord);
                if (!spawnEntryList.isEmpty()) {
                    return spawnEntryList;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Equivalent to WeightedRandom.getRandomItem but implemented for List of Lists
     * 
     * @param random
     * @param spawnList
     * @param weightList
     * @return
     */
    private SpawnListEntry getRandomEntry(Random random, ImmutableCollection<String> groupIDList) {
        int totalWeight = 0;
        for (String groupID : groupIDList) {
            for (SpawnListEntry spawnListEntry : groupNameToSpawnEntry.get(groupID)) {
                totalWeight += spawnListEntry.itemWeight;
            }
        }

        if (totalWeight <= 0) {
            return null;
        } else {
            int selectedWeight = random.nextInt(totalWeight);
            SpawnListEntry resultEntry = null;

            for (String groupID : groupIDList) {
                for (SpawnListEntry spawnListEntry : groupNameToSpawnEntry.get(groupID)) {
                    resultEntry = spawnListEntry;
                    selectedWeight -= spawnListEntry.itemWeight;
                    if (selectedWeight <= 0) {
                        return resultEntry;
                    }
                }
            }
            return resultEntry;
        }
    }

    /**
     * Called by CustomSpawner to get the base coordinate to spawn an Entity
     * 
     * @param world
     * @param xCoord
     * @param zCoord
     * @return
     */
    public ChunkPosition getRandomSpawningPointInChunk(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int xCoord = chunkX * 16 + world.rand.nextInt(16);
        int zCoord = chunkZ * 16 + world.rand.nextInt(16);
        int yCoord = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(xCoord, yCoord, zCoord);
    }

    /**
     * Entity Bases Type Check. Used to Evalue Type of Entity if it exists in the World
     * 
     * @param entity Entity that is being Checked
     * @return
     */
    public boolean isEntityOfType(Entity entity) {
        LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(entity.getClass());
        return livingHandler != null ? livingHandler.isEntityOfType(entity, this) : false;
    }

    /**
     * Class Bases Type Check. Used to Evalue Type of Entity before it exists in the World
     * 
     * @param entity
     * @return
     */
    public boolean isEntityOfType(Class<? extends EntityLiving> entity) {
        LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(entity);
        return livingHandler != null ? livingHandler.isEntityOfType(entity, this) : false;
    }

    /**
     * Called by CustomSpawner to determine if the Chunk Postion to be spawned at is a valid Type
     * 
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    public boolean isValidMedium(World world, int xCoord, int yCoord, int zCoord) {
        return !world.isBlockNormalCube(xCoord, yCoord, zCoord)
                && world.getBlockMaterial(xCoord, yCoord, zCoord) == spawnMedium;
    }

    /**
     * Called by CustomSpawner the location is valid for determining if the Chunk Postion is a valid location to spawn
     * 
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    public boolean canSpawnAtLocation(World world, int xCoord, int yCoord, int zCoord) {
        if (spawnMedium == Material.water) {
            return world.getBlockMaterial(xCoord, yCoord, zCoord).isLiquid()
                    && world.getBlockMaterial(xCoord, yCoord - 1, zCoord).isLiquid()
                    && !world.isBlockNormalCube(xCoord, yCoord + 1, zCoord);
        } else if (!world.doesBlockHaveSolidTopSurface(xCoord, yCoord - 1, zCoord)) {
            return false;
        } else {
            int l = world.getBlockId(xCoord, yCoord - 1, zCoord);
            boolean spawnBlock = (Block.blocksList[l] != null && canCreatureSpawn(Block.blocksList[l], world, xCoord,
                    yCoord - 1, zCoord));
            return spawnBlock && l != Block.bedrock.blockID && !world.isBlockNormalCube(xCoord, yCoord, zCoord)
                    && !world.getBlockMaterial(xCoord, yCoord, zCoord).isLiquid()
                    && !world.isBlockNormalCube(xCoord, yCoord + 1, zCoord);
        }
    }

    /**
     * Creates a new instance of creature types from configuration using itself as the default
     * 
     * @param config
     * @return
     */
    public CreatureType createFromConfig(Configuration config) {
        int resultSpawnRate = config.get("LivingType." + typeID, "Spawn Rate", spawnRate).getInt();
        int resultMaxNumberOfCreature = config.get("LivingType." + typeID, "Creature Spawn Cap", maxNumberOfCreature)
                .getInt();
        boolean resultChunkSpawning = config.get("LivingType." + typeID, "Do Chunk Spawning", chunkSpawning)
                .getBoolean(chunkSpawning);
        return this.maxNumberOfCreatureTo(resultMaxNumberOfCreature).spawnRateTo(resultSpawnRate)
                .chunkSpawningTo(resultChunkSpawning);
    }

    /*
     * TODO: Does not Belong Here. Possible Block Helper Class. Ideally Mods should be able to Register a Block. Similar
     * to Proposed Entity Registry or BiomeInterpreter. How will end-users fix issue? Does End User Need to?
     */
    /**
     * Custom Implementation of canCreatureSpawnMethod which Required EnumCreatureType. Cannot be Overrident.
     * 
     * @param block
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    private boolean canCreatureSpawn(Block block, World world, int xCoord, int yCoord, int zCoord) {
        int meta = world.getBlockMetadata(xCoord, yCoord, zCoord);
        if (block instanceof BlockStep) {
            return (((meta & 8) == 8) || block.isOpaqueCube());
        } else if (block instanceof BlockStairs) {
            return ((meta & 4) != 0);
        }
        return block.isBlockSolidOnSide(world, xCoord, yCoord, zCoord, UP);
    }
}
