package jas.spawner.legacy.spawner.creature.type;

import jas.spawner.legacy.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.legacy.spawner.creature.handler.LivingHandler;
import jas.spawner.legacy.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.legacy.spawner.creature.handler.parsing.keys.Key;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettingsCreatureTypeSpawn;
import jas.spawner.modern.DefaultProps;

import java.io.File;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableMap;

//TODO: Large Constructor could probably use Factory OR String optionalParameters to consolidate unused properties
public class CreatureType {
    public final String typeID;
    public final int spawnRate;
    public final int maxNumberOfCreature;
    public final float chunkSpawnChance;
    public final Material spawnMedium;
    public final String optionalParameters;
    protected OptionalSettingsCreatureTypeSpawn spawning;
    public final BiomeGroupRegistry biomeGroupRegistry;

    public final int defaultBiomeCap;
    public final ImmutableMap<Integer, Integer> biomeCaps;

    public CreatureType(BiomeGroupRegistry biomeGroupRegistry, CreatureTypeBuilder builder) {
        this.biomeGroupRegistry = biomeGroupRegistry;
        this.typeID = builder.typeID;
        this.maxNumberOfCreature = builder.maxNumberOfCreature;
        this.spawnRate = builder.spawnRate;
        this.spawnMedium = builder.getSpawnMedium();
        this.chunkSpawnChance = builder.getChunkSpawnChance();
        this.defaultBiomeCap = builder.getDefaultBiomeCap();
        Map<Integer, Integer> biomeCaps = CreatureTypeBuilder.capMapMappingToBiomeId(builder.getBiomeCaps(),
                biomeGroupRegistry.biomeMappingToPckg(), biomeGroupRegistry.pckgNameToBiomeID());
        this.biomeCaps = ImmutableMap.<Integer, Integer> builder().putAll(biomeCaps).build();
        this.optionalParameters = builder.getOptionalParameters();
        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsCreatureTypeSpawn(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsCreatureTypeSpawn("") : spawning;
    }

    public boolean isReady(WorldServer world) {
        return world.getWorldInfo().getWorldTotalTime() % spawnRate == 0L;
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
    public boolean isEntityOfType(LivingHandlerRegistry livingHandlerRegistry, Entity entity) {
        if (entity instanceof EntityLiving) {
            return isEntityOfType(livingHandlerRegistry, ((EntityLiving) entity).getClass());
        }
        return false;
    }

    /**
     * Class Bases Type Check. Used to Evalue Type of Entity before it exists in the World
     * 
     * @param entity
     * @return
     */
    public boolean isEntityOfType(LivingHandlerRegistry livingHandlerRegistry, Class<? extends EntityLiving> entity) {
        for (LivingHandler handler : livingHandlerRegistry.getLivingHandlers(entity)) {
            if (handler.creatureTypeID.equals(this.typeID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEntityOfType(LivingHandlerRegistry livingHandlerRegistry, String groupID) {
        LivingHandler handler = livingHandlerRegistry.getLivingHandler(groupID);
        return handler != null ? handler.creatureTypeID.equals(this.typeID) : false;
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
        return !world.getBlock(xCoord, yCoord, zCoord).isNormalCube()
                && world.getBlock(xCoord, yCoord, zCoord).getMaterial() == spawnMedium;
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
        boolean canSpawn = true;
        if (spawning.isOptionalEnabled()) {
            canSpawn = !spawning.isInverted();
            if (!spawning.isValidLocation(world, null, xCoord, yCoord, zCoord)) {
                canSpawn = spawning.isInverted();
            }

            return canSpawn;
        } else {
            if (spawnMedium == Material.water) {
                return world.getBlock(xCoord, yCoord, zCoord).getMaterial().isLiquid()
                        && world.getBlock(xCoord, yCoord - 1, zCoord).getMaterial().isLiquid()
                        && !world.getBlock(xCoord, yCoord + 1, zCoord).isNormalCube();
            } else if (!World.doesBlockHaveSolidTopSurface(world, xCoord, yCoord - 1, zCoord)) {
                return false;
            } else {
                Block l = world.getBlock(xCoord, yCoord - 1, zCoord);
                boolean spawnBlock = (l != null && canCreatureSpawn(l, world, xCoord, yCoord - 1, zCoord));
                return spawnBlock && l != Blocks.bedrock && !world.getBlock(xCoord, yCoord, zCoord).isBlockNormalCube()
                        && !world.getBlock(xCoord, yCoord, zCoord).getMaterial().isLiquid()
                        && !world.getBlock(xCoord, yCoord + 1, zCoord).isBlockNormalCube();
            }
        }
    }

    public int getChunkCap(Chunk chunk) {
        if (chunk == null || defaultBiomeCap <= 0) {
            return -1;
        }
        int chunkCap = 0;
        int counter = 0;
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; i++) {
            int biomeID = biomeArray[i] & 255;
            Integer columnCap = biomeCaps.get(biomeID);
            columnCap = columnCap != null ? columnCap : defaultBiomeCap;
            chunkCap += columnCap;
            counter++;
        }
        return counter > 0 ? chunkCap / counter : -1;
    }

    /*
     * TODO: Does not Belong Here. Possible Block Helper Class. Ideally Mods should be able to Register a Block. Similar
     * to Proposed Entity Registry or StructureInterpreter. How will end-users fix issue? Does End User Need to?
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
        if (block instanceof BlockSlab) {
            return (((meta & 8) == 8) || block.func_149730_j());
        } else if (block instanceof BlockStairs) {
            return ((meta & 4) != 0);
        }
        return block.isSideSolid(world, xCoord, yCoord, zCoord, ForgeDirection.UP);
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + "CreatureType.cfg");
    }
}
