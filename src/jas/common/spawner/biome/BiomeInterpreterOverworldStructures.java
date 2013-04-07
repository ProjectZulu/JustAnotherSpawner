package jas.common.spawner.biome;

import jas.api.BiomeInterpreter;
import jas.common.spawner.creature.handler.ReflectionHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;

public class BiomeInterpreterOverworldStructures implements BiomeInterpreter {

    public static final String STRONGHOLD_KEY = "Stronghold";
    public static final String MINESHAFT_KEY = "Mineshaft";

    @Override
    public Collection<String> getStructureKeys() {
        return Arrays.asList(STRONGHOLD_KEY, MINESHAFT_KEY);
    }

    @Override
    public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
        return Collections.emptyList();
    }

    @Override
    public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
        ChunkProviderGenerate chunkProviderGenerate = BiomeInterpreterHelper.getInnerChunkProvider(world,
                ChunkProviderGenerate.class);
        if (chunkProviderGenerate != null) {
            MapGenStructure strongholdGen = getStructureGen(MapGenStronghold.class, chunkProviderGenerate,
                    "strongholdGenerator", "field_73225_u");
            String stronghold = isLocationStructure(strongholdGen, STRONGHOLD_KEY, xCoord, yCoord, zCoord);
            if (stronghold != null) {
                return stronghold;
            }

            MapGenStructure mineshaftGen = getStructureGen(MapGenMineshaft.class, chunkProviderGenerate,
                    "mineshaftGenerator", "field_73223_w");
            String mineshaft = isLocationStructure(mineshaftGen, MINESHAFT_KEY, xCoord, yCoord, zCoord);
            if (mineshaft != null) {
                return mineshaft;
            }
        }
        return null;
    }

    private MapGenStructure getStructureGen(Class<? extends MapGenStructure> fieldClass, Object containerInstance,
            String fieldName, String obfName) {
        MapGenStructure structure;
        try {
            structure = ReflectionHelper.getCatchableFieldFromReflection(obfName, containerInstance, fieldClass);
        } catch (NoSuchFieldException e) {
            structure = ReflectionHelper.getFieldFromReflection(fieldName, containerInstance, fieldClass);
        }
        return structure;
    }

    private String isLocationStructure(MapGenStructure structure, String structureKey, int xCoord, int yCoord,
            int zCoord) {
        return structure != null ? structure.hasStructureAt(xCoord, yCoord, zCoord) ? structureKey : null : null;
    }

    @Override
    public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
        return world.getWorldInfo().getDimension() == 0;
    }

}
