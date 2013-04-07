package jas.common.spawner.biome;

import jas.api.BiomeInterpreter;
import jas.common.JASLog;
import jas.common.spawner.creature.handler.ReflectionHelper;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.structure.MapGenNetherBridge;

public class BiomeInterpreterNether implements BiomeInterpreter {

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
        ChunkProviderHell chunkProviderHell = BiomeInterpreterHelper.getInnerChunkProvider(world,
                ChunkProviderHell.class);
        if (biome instanceof BiomeGenHell && chunkProviderHell != null) {
            MapGenNetherBridge genNetherBridge;
            try {
                genNetherBridge = ReflectionHelper.getCatchableFieldFromReflection("field_73172_c", chunkProviderHell,
                        MapGenNetherBridge.class);
            } catch (NoSuchFieldException e) {
                genNetherBridge = ReflectionHelper.getFieldFromReflection("genNetherBridge", chunkProviderHell,
                        MapGenNetherBridge.class);
            }
            if (genNetherBridge != null && genNetherBridge.hasStructureAt(xCoord, yCoord, zCoord)) {
                return "NetherBridge";
            }
        } else {
            JASLog.info("Biome or Chunkprovider is wrong, %s, %s", biome, chunkProviderHell);
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
