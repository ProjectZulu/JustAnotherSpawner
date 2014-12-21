package jas.common.spawner.biome.structure;

import jas.api.StructureInterpreter;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.biome.BiomeGenSwamp;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;

public class StructureInterpreterSwamp implements StructureInterpreter {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<String> getStructureKeys() {
        Collection<String> collection = new ArrayList();
        collection.add("WitchHut");
        return collection;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
        Collection<SpawnListEntry> collection = new ArrayList();
        if (structureKey.equals("WitchHut")) {
            collection.add(new SpawnListEntry(EntityWitch.class, 1, 1, 1));
        }
        return collection;
    }

    @Override
    public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
		BiomeGenBase biome = world.getBiomeGenForCoords(xCoord, zCoord);
		ChunkProviderServer chunkprovider = (ChunkProviderServer) world.getChunkProvider();
		if (chunkprovider.currentChunkProvider instanceof ChunkProviderGenerate) {
			ChunkProviderGenerate chunkProviderGenerate = (ChunkProviderGenerate) chunkprovider.currentChunkProvider;
			if (biome instanceof BiomeGenSwamp && chunkProviderGenerate != null) {
				MapGenScatteredFeature mapGenScatteredFeature = chunkProviderGenerate.scatteredFeatureGenerator;
				if (mapGenScatteredFeature != null && mapGenScatteredFeature.hasStructureAt(xCoord, yCoord, zCoord)) {
					return "WitchHut";
				}
			}
		}
		return null;
    }

    @Override
    public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
        return biomeGenBase.biomeName.equals(BiomeGenBase.swampland.biomeName);
    }
}
