package jas.compatability.tf;

import jas.api.StructureInterpreter;
import jas.common.ReflectionHelper;
import jas.common.spawner.biome.structure.StructureInterpreterHelper;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.MapGenTFMajorFeature;
import twilightforest.world.TFWorld;

public class StructureInterpreterTwilightForest implements StructureInterpreter {

    private HashMap<String, Integer> featureNameToID = new HashMap<String, Integer>();

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<String> getStructureKeys() {
        Collection<String> collection = new ArrayList();
        for (int i = 0; i < TFFeature.featureList.length; i++) {
            if (TFFeature.featureList[i] == null) {
                continue;
            }
            List spawnableMonsterList = ReflectionHelper.getFieldFromReflection("spawnableMonsterLists",
                    TFFeature.featureList[i], List.class);
            if (spawnableMonsterList != null) {
                for (int j = 0; j < spawnableMonsterList.size(); j++) {
                    featureNameToID.put(TFFeature.featureList[i].name, i);
                    collection.add(TFFeature.featureList[i].name + "_" + j);
                }
            }
        }
        return collection;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
        Collection<SpawnListEntry> collection = new ArrayList();

        String[] keyParts = structureKey.split("_");
        String featureName = keyParts[0];
        int index = ParsingHelper.parseFilteredInteger(keyParts[1], -1, structureKey);
        TFFeature feature = TFFeature.featureList[featureNameToID.get(featureName)];
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            collection.addAll(feature.getSpawnableList(creatureType, index));
        }

        return collection;
    }

    @Override
    public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
        ChunkProviderTwilightForest chunkProviderTwilightForest = StructureInterpreterHelper.getInnerChunkProvider(world,
                ChunkProviderTwilightForest.class);
        if (chunkProviderTwilightForest != null) {
            TFFeature nearestFeature = TFFeature.getNearestFeatureIncludeMore(xCoord >> 4, zCoord >> 4, world);

            if (nearestFeature != TFFeature.nothing) {
                MapGenTFMajorFeature mapGenTFMajorFeature;
                mapGenTFMajorFeature = ReflectionHelper.getFieldFromReflection("majorFeatureGenerator",
                        chunkProviderTwilightForest, MapGenTFMajorFeature.class);

                int spawnListIndex = mapGenTFMajorFeature.getSpawnListIndexAt(xCoord, yCoord, zCoord);
                if (spawnListIndex >= 0) {
                    return nearestFeature.name + "_" + Integer.toString(spawnListIndex);
                }
            }

            if ((yCoord < TFWorld.SEALEVEL)) {
                return TFFeature.underground.name + "_0";
            }
        }
        return null;
    }

    @Override
    public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
        return world.provider.dimensionId == TwilightForestMod.dimensionID;
    }
}
