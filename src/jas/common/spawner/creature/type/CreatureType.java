package jas.common.spawner.creature.type;

import static net.minecraftforge.common.ForgeDirection.UP;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStep;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.Configuration;

//TODO: Large Constructor could probably use Factory
public class CreatureType {
    public final String typeID;
    public final int spawnRate;
    public final int maxNumberOfCreature;
    public final boolean chunkSpawning;
    public final Material spawnMedium;
    private final HashMap<String, Collection<SpawnListEntry>> biomeNameToSpawnEntry = new HashMap<String, Collection<SpawnListEntry>>();

    public CreatureType(String typeID, int maxNumberOfCreature, Material spawnMedium, int spawnRate,
            boolean chunkSpawning) {
        this.typeID = typeID;
        this.maxNumberOfCreature = maxNumberOfCreature;
        this.spawnMedium = spawnMedium;
        this.spawnRate = spawnRate;
        this.chunkSpawning = chunkSpawning;
    }

    /**
     * Create a new Instance of CreatureType. Used to Allow subclasses to Include their own Logic
     * 
     * @param typeID
     * @param maxNumberOfCreature
     * @param spawnMedium
     * @param spawnRate
     * @param chunkSpawning
     * @param needSky
     */
    // TODO: Should This be moved into a Factory of Sorts?
    protected CreatureType create(String typeID, int maxNumberOfCreature, Material spawnMedium, int spawnRate,
            boolean chunkSpawning) {
        return new CreatureType(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    /**
     * Adds a SpawnlistEntry to the corresponding SpawnList using the biomeName as key
     * 
     * @param biomeName
     * @param spawnListEntry
     */
    public void addSpawn(SpawnListEntry spawnListEntry) {
        if (biomeNameToSpawnEntry.get(spawnListEntry.biomeName) == null) {
            biomeNameToSpawnEntry.put(spawnListEntry.biomeName, new ArrayList<SpawnListEntry>());
        }
        biomeNameToSpawnEntry.get(spawnListEntry.biomeName).add(spawnListEntry);
    }

    /**
     * Resets All Spawn Lists. This is used on World Change
     */
    public void resetSpawns() {
        biomeNameToSpawnEntry.clear();
    }

    /**
     * Called by CustomSpawner to get a creature dependent on the World Location
     * 
     * @param world
     * @param xCoord Random xCoordinate nearby to Where Creature will spawn
     * @param yCoord Random yCoordinate nearby to Where Creature will spawn
     * @param zCoord Random zCoordinate nearby to Where Creature will spawn
     * @return Creature to Spawn
     */
    public SpawnListEntry getSpawnListEntry(World world, int xCoord, int yCoord, int zCoord) {
        BiomeGenBase biomegenbase = world.getBiomeGenForCoords(xCoord, zCoord);
        if (biomegenbase != null) {
            Collection<SpawnListEntry> spawnEntryList = biomeNameToSpawnEntry.get(biomegenbase.biomeName);
            return spawnEntryList != null ? (SpawnListEntry) WeightedRandom.getRandomItem(world.rand, spawnEntryList)
                    : null;
        }
        return null;
    }

    /**
     * Called by CustomSpawner to get a creature dependent on the World Biome
     * 
     * @param biomeName Name of Biome SpawnList to get CreatureType From
     * @return
     */
    public SpawnListEntry getSpawnListEntry(World world, String biomeName) {
        Collection<SpawnListEntry> spawnEntryList = biomeNameToSpawnEntry.get(biomeName);
        return spawnEntryList != null ? (SpawnListEntry) WeightedRandom.getRandomItem(world.rand, spawnEntryList)
                : null;
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
     * 
     * @param entity Entity that is being Checked
     * @return
     */
    public boolean isEntityOfType(Entity entity) {
        LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(entity.getClass());
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
     * @param config
     * @return
     */
    public CreatureType createFromConfig(Configuration config) {
        int resultSpawnRate = config.get("LivingType." + typeID, "Spawn Rate", spawnRate).getInt();
        int resultMaxNumberOfCreature = config.get("LivingType." + typeID, "Creature Spawn Cap", maxNumberOfCreature)
                .getInt();
        boolean resultChunkSpawning = config.get("LivingType." + typeID, "Do Chunk Spawning", chunkSpawning)
                .getBoolean(chunkSpawning);
        return this.create(typeID, resultMaxNumberOfCreature, spawnMedium, resultSpawnRate, resultChunkSpawning);
    }
    
    /*
     * TODO: Does not Belong Here. Possible Block Helper Class. Ideally Mods should be able to Register a Block. Similar
     * to Proposed Entity Registry. How will end-users fix issue? Does End User Need to?
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
