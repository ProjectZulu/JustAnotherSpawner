package jas.common.spawner.biome;

import jas.common.spawner.creature.handler.ReflectionHelper;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenSwamp;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.feature.MapGenScatteredFeature;

public class BiomeInterpreterSwamp implements BiomeInterpreter {

    @Override
    public Collection<String> getStructureKeys() {
        Collection<String> collection = new ArrayList<>();
        collection.add("WitchHut");
        return collection;
    }

    @Override
    public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
        Collection<SpawnListEntry> collection = new ArrayList<>();
        if (structureKey.equals("WitchHut")) {
            collection.add(new SpawnListEntry(EntityWitch.class, 1, 1, 1));
        }
        return collection;
    }

    @Override
    // TODO: Compile List of Fields we could make public for Coremod
    public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
        BiomeGenBase biome = world.getBiomeGenForCoords(xCoord, zCoord);
        IChunkProvider chunkprovider = world.getChunkProvider();
        if (biome instanceof BiomeGenSwamp && chunkprovider instanceof ChunkProviderGenerate) {
            ChunkProviderGenerate chunkProviderGenerate = (ChunkProviderGenerate) world.getChunkProvider();
            if (ReflectionHelper.isUnObfuscated(ChunkProviderGenerate.class, "ChunkProviderGenerate")) {
                MapGenScatteredFeature mapGenScatteredFeature = ReflectionHelper.getFieldFromReflection(
                        "MapGenScatteredFeature", chunkProviderGenerate, MapGenScatteredFeature.class);
                if (mapGenScatteredFeature != null) {

                }
            }
            // chunkProviderGenerate.scatteredFeatureGenerator
            return "WitchHut";
        }
        return null;
    }

    @Override
    public boolean isValidBiome(BiomeGenBase biomeGenBase) {
        return biomeGenBase.equals(BiomeGenBase.swampland);
    }
}
