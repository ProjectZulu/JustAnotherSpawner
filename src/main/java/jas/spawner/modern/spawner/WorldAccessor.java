package jas.spawner.modern.spawner;

import jas.common.helper.VanillaHelper;
import jas.spawner.modern.spawner.tags.WorldFunctions;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Accessor to public expose the World object for users to provide them as unchanging interface as possible. As a bonus
 * this provides stability to other tagas that utilize for calls instead of accessing world directly.
 */
public class WorldAccessor implements WorldFunctions {
	private World world;

	public WorldAccessor(World world) {
		this.world = world;
	}

	public int lightAt(int coordX, int coordY, int coordZ) {
		return VanillaHelper.getLight(world, coordX, coordY, coordZ);
	}

	public int torchlightAt(int coordX, int coordY, int coordZ) {
		return VanillaHelper.getLightFor(world, EnumSkyBlock.BLOCK, coordX, coordY, coordZ);
	}

	public String blockNameAt(Integer offsetX, Integer offsetY, Integer offsetZ) {
		return VanillaHelper.getNameForBlock(world, offsetX, offsetY, offsetZ);
	}

	public Block blockAt(int coordX, int coordY, int coordZ) {
		return VanillaHelper.getBlock(world, coordX, coordY, coordZ);
	}

	public BiomeGenBase biomeAt(int coordX, int coordZ) {
		return VanillaHelper.getBiomeForCoords(world, coordX, coordZ);
	}

	public Block biomeTop(int coordX, int coordZ) {
		return VanillaHelper.getBlock(biomeAt(coordX, coordZ).topBlock);
	}

	public Block biomeFiller(int coordX, int coordZ) {
		return VanillaHelper.getBlock(biomeAt(coordX, coordZ).fillerBlock);
	}

	public Material materialAt(int coordX, int coordY, int coordZ) {
		return VanillaHelper.getBlock(world, coordX, coordY, coordZ).getMaterial();
	}

	public BlockPos originPos() {
		return world.getSpawnPoint();
	}

	public boolean skyVisibleAt(int coordX, int coordY, int coordZ) {
		return VanillaHelper.canBlockSeeSky(world, coordX, coordY, coordZ);
	}

	public int originDis(int coordX, int coordY, int coordZ) {
		return (int) Math.sqrt(originPos().distanceSq(coordX, coordY, coordZ));
	}

	public int dimension() {
		return VanillaHelper.getDimensionID(world);
	}

	public long totalTime() {
		return world.getWorldInfo().getWorldTotalTime();
	}

	public long timeOfDay() {
		return world.getWorldInfo().getWorldTime();
	}

	@Override
	public boolean isClearWeather() {
		return !world.isRaining();
	}
}
