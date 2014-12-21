package jas.common.spawner.biome.structure;

import jas.api.StructureInterpreter;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.MapGenNetherBridge;

public class StructureInterpreterNether implements StructureInterpreter {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<String> getStructureKeys() {
        Collection<String> collection = new ArrayList();
        collection.add("NetherBridge");
        return collection;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
        Collection<SpawnListEntry> collection = new ArrayList();
        if (structureKey.equals("NetherBridge")) {
            collection.add(new SpawnListEntry(EntityBlaze.class, 10, 2, 3));
            collection.add(new SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
            collection.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
            collection.add(new SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
        }
        return collection;
    }

    @Override
    public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
		BiomeGenBase biome = world.getBiomeGenForCoords(xCoord, zCoord);
		ChunkProviderServer chunkprovider = (ChunkProviderServer) world.getChunkProvider();
		if (chunkprovider.currentChunkProvider instanceof ChunkProviderHell) {
			ChunkProviderHell chunkProviderHell = (ChunkProviderHell) chunkprovider.currentChunkProvider;
			if (biome instanceof BiomeGenHell && chunkProviderHell != null) {
				MapGenNetherBridge genNetherBridge = chunkProviderHell.genNetherBridge;
				if (genNetherBridge != null && genNetherBridge.hasStructureAt(xCoord, yCoord, zCoord)) {
					return "NetherBridge";
				}
			}
		}
		return null;
    }

    @Override
    public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
        if (biomeGenBase.biomeName.equals(BiomeGenBase.hell.biomeName)) {
            return true;
        }
        return false;
    }
}
