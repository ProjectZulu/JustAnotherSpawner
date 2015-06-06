package jas.spawner.modern.spawner.tags;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.biome.BiomeGenBase;

public interface WorldFunctions {
	public int lightAt(int coordX, int coordY, int coordZ);

	public int torchlightAt(int coordX, int coordY, int coordZ);

	public String blockNameAt(Integer offsetX, Integer offsetY, Integer offsetZ);

	public Block blockAt(int coordX, int coordY, int coordZ);

	public BiomeGenBase biomeAt(int coordX, int coordZ);

	public Block biomeTop(int coordX, int coordZ);

	public Block biomeFiller(int coordX, int coordZ);

	public Material materialAt(int coordX, int coordY, int coordZ);

	public ChunkCoordinates originPos();

	public boolean skyVisibleAt(int coordX, int coordY, int coordZ);

	public int originDis(int coordX, int coordY, int coordZ);

	public int dimension();

	public long totalTime();

	public long timeOfDay();
	
	public boolean isClearWeather();
}
