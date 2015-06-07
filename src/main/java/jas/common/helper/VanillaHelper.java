package jas.common.helper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructure;

/**
 * Layer between JAS code and its vanilla calls. Generally common calls. One of or trivial calls may still be maintained
 * in calling code directly or where calling VanillaHelepr would be onerous or confusing.
 * 
 * Logic is still done in calling code, this is more or less maps directly to a vanilla function.
 * 
 * Intent is for when changes to vanilla happen ONLY this layer should need to be altered for most interactions.
 */
public class VanillaHelper {

	public static BlockPos posObj(int posX, int posZ) {
		return new BlockPos(posX, 0, posZ);
	}

	public static BlockPos convert(int posX, int posY, int posZ) {
		return new BlockPos(posX, posY, posZ);
	}

	public static IBlockState getState(IBlockAccess world, int posX, int posY, int posZ) {
		return getState(world, convert(posX, posY, posZ));
	}

	public static IBlockState getState(IBlockAccess world, BlockPos blockPos) {
		return world.getBlockState(blockPos);
	}

	public static Block getBlock(IBlockAccess world, int posX, int posY, int posZ) {
		return getBlock(world, convert(posX, posY, posZ));
	}

	public static Block getBlock(IBlockAccess world, BlockPos blockPos) {
		return getBlock(getBlockState(world, blockPos));
	}

	public static Block getBlock(IBlockState blockState) {
		return blockState.getBlock();
	}

	public static IBlockState getBlockState(IBlockAccess world, int posX, int posY, int posZ) {
		return getBlockState(world, convert(posX, posY, posZ));
	}

	public static IBlockState getBlockState(IBlockAccess world, BlockPos blockPos) {
		return world.getBlockState(blockPos);
	}

	public static BiomeGenBase getBiomeForCoords(IBlockAccess world, int posX, int posZ) {
		return world.getBiomeGenForCoords(convert(posX, 0, posZ));
	}

	public static BiomeGenBase getBiomeForCoords(IBlockAccess world, BlockPos blockPos) {
		return world.getBiomeGenForCoords(blockPos);
	}

	public static BlockPos getTopSolidOrLiquidBlock(World world, int posX, int posZ) {
		return getTopSolidOrLiquidBlock(world, convert(posX, 0, posZ));
	}

	public static BlockPos getTopSolidOrLiquidBlock(World world, BlockPos blockPos) {
		return world.func_175672_r(blockPos);
	}

	public static int getDimensionID(World world) {
		return world.provider.getDimensionId();
	}

	public static int getBlockMeta(World world, int posX, int posY, int posZ) {
		return getBlockMeta(world, convert(posX, posY, posZ));
	}

	public static int getBlockMeta(World world, BlockPos blockPos) {
		IBlockState blockState = getState(world, blockPos);
		return getBlockMeta(getBlock(blockState), blockState);
	}

	public static int getBlockMeta(IBlockState blockState) {
		return getBlockMeta(getBlock(blockState), blockState);
	}

	public static int getBlockMeta(Block block, IBlockState blockState) {
		return block.getMetaFromState(blockState);
	}

	public static boolean canBlockSeeSky(World world, int posX, int posY, int posZ) {
		return canBlockSeeSky(world, convert(posX, posY, posZ));
	}

	public static boolean canBlockSeeSky(World world, BlockPos pos) {
		return world.canSeeSky(pos);
	}

	public static boolean isStructureAt(MapGenStructure structure, int posX, int posY, int posZ) {
		return structure.func_175795_b(convert(posX, 0, posZ));
	}

	public static boolean isStructureAt(MapGenStructure structure, BlockPos blockPos) {
		return structure == null ? false : structure.func_175795_b(blockPos);
	}

	public static boolean isOpaque(IBlockAccess world, int posX, int posY, int posZ) {
		return isOpaque(world, convert(posX, posY, posZ));
	}

	public static boolean isOpaque(IBlockAccess world, BlockPos pos) {
		return isOpaque(getBlock(world, pos));
	}

	public static boolean isOpaque(Block block) {
		return block.getMaterial().isOpaque();
	}

	public static boolean doesBlockHaveSolidTopSurface(IBlockAccess blockAccess, int posX, int posY, int posZ) {
		return doesBlockHaveSolidTopSurface(blockAccess, convert(posX, posY, posZ));
	}

	public static boolean doesBlockHaveSolidTopSurface(IBlockAccess blockAccess, BlockPos blockPos) {
		return isSolidSide(blockAccess, blockPos, EnumFacing.UP);
	}

	public static boolean isSolidSide(IBlockAccess world, int posX, int posY, int posZ, EnumFacing side) {
		return isSolidSide(world, convert(posX, posY, posZ), side);
	}

	public static boolean isSolidSide(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return isSolidSide(getBlock(world, pos), world, pos, side);
	}

	public static boolean isSolidSide(Block block, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return block.isSideSolid(world, pos, side);
	}

	public static boolean isNormal(IBlockAccess world, int posX, int posY, int posZ) {
		return isNormal(world, convert(posX, posY, posZ));
	}

	public static boolean isNormal(IBlockAccess world, BlockPos pos) {
		return isNormal(getBlock(world, pos));
	}

	public static boolean isNormal(Block block) {
		return block.isNormalCube();
	}

	public static boolean isLiquid(IBlockAccess world, int posX, int posY, int posZ) {
		return isNormal(world, convert(posX, posY, posZ));
	}

	public static boolean isLiquid(IBlockAccess world, BlockPos pos) {
		return isNormal(getBlock(world, pos));
	}

	public static boolean isLiquid(Block block) {
		return block.getMaterial().isLiquid();
	}

	public static int getLight(World world, int coordX, int coordY, int coordZ) {
		return getLight(world, VanillaHelper.convert(coordX, coordY, coordZ));
	}

	public static int getLight(World world, BlockPos pos) {
		return world.getLight(pos, true);
	}

	public static int getLightFor(World world, EnumSkyBlock block, int posX, int posY, int posZ) {
		return getLightFor(world, block, VanillaHelper.convert(posX, posY, posZ));
	}

	public static int getLightFor(World world, EnumSkyBlock block, BlockPos pos) {
		return world.getLightFor(EnumSkyBlock.BLOCK, pos);
	}

	public static Chunk getChunkFromChunkCoord(World world, int chunkX, int chunkZ) {
		return world.getChunkFromChunkCoords(chunkX, chunkZ);
	}

	public static Chunk getChunkFromBlockCoord(World world, int posX, int posZ) {
		return getChunkFromBlockCoord(world, convert(posX, 0, posZ));
	}

	public static Chunk getChunkFromBlockCoord(World world, BlockPos pos) {
		return world.getChunkFromBlockCoords(pos);
	}

	public static boolean isFoliage(World world, BlockPos pos) {
		return isFoliage(getBlock(world, pos), world, pos);
	}

	public static boolean isFoliage(Block block, World world, int posX, int posY, int posZ) {
		return isFoliage(block, world, convert(posX, posY, posZ));
	}

	public static boolean isFoliage(Block block, World world, BlockPos pos) {
		return block.isFoliage(world, pos);
	}

	public static boolean isWood(World world, BlockPos pos) {
		return isWood(getBlock(world, pos), world, pos);
	}

	public static boolean isWood(Block block, World world, int posX, int posY, int posZ) {
		return isWood(block, world, convert(posX, posY, posZ));
	}

	public static boolean isWood(Block block, World world, BlockPos pos) {
		return block.isWood(world, pos);
	}

	public static AxisAlignedBB getBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
		return AxisAlignedBB.fromBounds(x1, y1, z1, x2, y2, z2);
	}

	public static String getNameForBlock(World world, int posX, int posY, int posZ) {
		return getNameForBlock(world, getBlock(world, posX, posY, posZ));
	}

	public static String getNameForBlock(World world, Block block) {
		return ((ResourceLocation) Block.blockRegistry.getNameForObject(block)).toString();
	}

	public static Material getBlockMaterial(World world, int posX, int posY, int posZ) {
		return getBlockMaterial(world, convert(posX, posY, posZ));
	}

	public static Material getBlockMaterial(World world, BlockPos pos) {
		return getBlockMaterial(getBlock(world, pos));
	}

	public static Material getBlockMaterial(Block block) {
		return block.getMaterial();
	}
}
