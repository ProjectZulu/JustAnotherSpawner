package jas.spawner.modern.spawner;

import jas.spawner.modern.spawner.tags.TagsWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Accessor to public expose the World object for users to provide them as unchanging interface as possible. As a bonus
 * this provides stability to other tagas that utilize for calls instead of accessing world directly.
 */
public class WorldAccessor implements TagsWorld {
	private World world;

	public WorldAccessor(World world) {
		this.world = world;
	}

	public int lightAt(int coordX, int coordY, int coordZ) {
		return world.getBlockLightValue(coordX, coordY, coordZ);
	}

	public int torchlightAt(int coordX, int coordY, int coordZ) {
		return world.getSavedLightValue(EnumSkyBlock.Block, coordX, coordY, coordZ);
	}

	public String blockNameAt(Integer offsetX, Integer offsetY, Integer offsetZ) {
		return Block.blockRegistry.getNameForObject(blockAt(offsetX, offsetY, offsetZ));
	}

	public Block blockAt(int coordX, int coordY, int coordZ) {
		return world.getBlock(coordX, coordY, coordZ);
	}

	public BiomeGenBase biomeAt(int coordX, int coordZ) {
		return world.getBiomeGenForCoords(coordX, coordZ);
	}

	public Block biomeTop(int coordX, int coordZ) {
		return biomeAt(coordX, coordZ).topBlock;
	}

	public Block biomeFiller(int coordX, int coordZ) {
		return biomeAt(coordX, coordZ).fillerBlock;
	}

	public Material materialAt(int coordX, int coordY, int coordZ) {
		return world.getBlock(coordX, coordY, coordZ).getMaterial();
	}

	public ChunkCoordinates originPos() {
		return world.getSpawnPoint();
	}

	public boolean skyVisibleAt(int coordX, int coordY, int coordZ) {
		return world.canBlockSeeTheSky(coordX, coordY, coordZ);
	}

	public int originDis(int coordX, int coordY, int coordZ) {
		return (int) Math.sqrt(originPos().getDistanceSquared(coordX, coordY, coordZ));
	}

	public int dimension() {
		return world.provider.dimensionId;
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
