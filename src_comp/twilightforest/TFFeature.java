package twilightforest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnListEntry;

public class TFFeature {
    public static final TFFeature[] featureList = new TFFeature[256];
    public static final TFFeature nothing = new TFFeature();
    public static final TFFeature underground = new TFFeature();
    public String name;

    public static TFFeature getNearestFeatureIncludeMore(int chunkX, int chunkZ, World world) {
        return null;
    }

    public List<SpawnListEntry> getSpawnableList(EnumCreatureType par1EnumCreatureType, int index) {
        return new ArrayList<SpawnListEntry>();
    }
}
