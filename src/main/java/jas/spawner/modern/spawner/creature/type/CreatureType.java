package jas.spawner.modern.spawner.creature.type;

import jas.common.helper.MVELHelper;
import jas.common.helper.VanillaHelper;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandler;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import org.mvel2.MVEL;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

// TODO: Large Constructor could probably use Factory OR String optionalParameters to consolidate unused properties
public class CreatureType {
	public final String typeID;
	public final int spawnRate;
	public final int maxNumberOfCreature;
	public final float chunkSpawnChance;
	public final Material spawnMedium;
	public final String spawnExpression;

	public final int iterationsPerChunk;
	public final int iterationsPerPack;

	private Optional<Serializable> compSpawnExpression = Optional.absent();
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
		this.spawnExpression = builder.getSpawnExpression();
		this.compSpawnExpression = !spawnExpression.trim().equals("") ? Optional.of(MVEL
				.compileExpression(spawnExpression)) : Optional.<Serializable> absent();
		this.iterationsPerChunk = builder.getIterationsPerChunk();
		this.iterationsPerPack = builder.getIterationsPerPack();
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
	public BlockPos getRandomSpawningPointInChunk(World world, int chunkX, int chunkZ) {
		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		int xCoord = chunkX * 16 + world.rand.nextInt(16);
		int zCoord = chunkZ * 16 + world.rand.nextInt(16);
		int yCoord = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
		return new BlockPos(xCoord, yCoord, zCoord);
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
		Block block = VanillaHelper.getBlock(world, xCoord, yCoord, zCoord);
		if (spawnMedium == Material.air) {
			return !VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).isNormalCube()
					&& (VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).getMaterial() == spawnMedium || (!VanillaHelper
							.getBlock(world, xCoord, yCoord, zCoord).getMaterial().blocksMovement() && !VanillaHelper
							.getBlock(world, xCoord, yCoord, zCoord).getMaterial().isLiquid()));
		} else {
			return !VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).isNormalCube()
					&& VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).getMaterial() == spawnMedium;
		}
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
	public boolean canSpawnAtLocation(World world, Tags tags, int xCoord, int yCoord, int zCoord) {
		if (compSpawnExpression.isPresent()) {
			return !MVELHelper.executeExpression(compSpawnExpression.get(), tags,
					"Error processing spawnExpression compiled expression for " + typeID + ": " + spawnExpression);
		} else {
			if (spawnMedium == Material.water) {
				return VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).getMaterial().isLiquid()
						&& VanillaHelper.getBlock(world, xCoord, yCoord - 1, zCoord).getMaterial().isLiquid()
						&& !VanillaHelper.getBlock(world, xCoord, yCoord + 1, zCoord).isNormalCube();
			} else if (!VanillaHelper.doesBlockHaveSolidTopSurface(world, xCoord, yCoord - 1, zCoord)) {
				return false;
			} else {
				Block l = VanillaHelper.getBlock(world, xCoord, yCoord - 1, zCoord);
				boolean spawnBlock = (l != null && canCreatureSpawn(l, world, xCoord, yCoord - 1, zCoord));
				return spawnBlock && l != Blocks.bedrock
						&& !VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).isNormalCube()
						&& !VanillaHelper.getBlock(world, xCoord, yCoord, zCoord).getMaterial().isLiquid()
						&& !VanillaHelper.getBlock(world, xCoord, yCoord + 1, zCoord).isNormalCube();
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
		BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);
		IBlockState state = VanillaHelper.getBlockState(world, pos);
		if (block instanceof BlockSlab) {
			return (block.isFullBlock() || state.getValue(BlockSlab.HALF_PROP) == BlockSlab.EnumBlockHalf.TOP);
		} else if (block instanceof BlockStairs) {
			return state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
		}
		return block.isSideSolid(world, pos, EnumFacing.UP);
	}

	public static File getFile(File configDirectory, String saveName) {
		return new File(configDirectory, saveName + "/" + "CreatureType.cfg");
	}

	public boolean canSpawnHere(World worldServer, CountInfo countInfo, BlockPos spawningPoint) {
		Tags tags = new Tags(worldServer, countInfo, spawningPoint.getX(), spawningPoint.getY(), spawningPoint.getZ());
		// Max of Type: Moved back to beggining of CustomSpawner for performance
		final int entityTypeCap = this.maxNumberOfCreature * countInfo.eligibleChunkLocations().size() / 256;
		int globalEntityTypeCount = countInfo.getGlobalEntityTypeCount(this.typeID);
		if (globalEntityTypeCount > entityTypeCap) {
			return false;
		}

		// BiomeCap
		ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(MathHelper.floor_double(spawningPoint.getX() / 16.0D),
				MathHelper.floor_double(spawningPoint.getZ() / 16.0D));
		int biomeCap = this
				.getChunkCap(worldServer.getChunkFromChunkCoords(chunkCoord.chunkXPos, chunkCoord.chunkZPos));
		if (biomeCap > -1 && countInfo.getClodEntityCount(chunkCoord, this.typeID) >= biomeCap) {
			return false;
		}

		// Valid Medium
		if (!this.isValidMedium(worldServer, spawningPoint.getX(), spawningPoint.getY(), spawningPoint.getZ())) {
			return false;
		}

		// {spawn} Tag
		if (!this.canSpawnAtLocation(worldServer, tags, spawningPoint.getX(), spawningPoint.getY(),
				spawningPoint.getZ())) {
			return false;
		}
		return true;
	}
}
