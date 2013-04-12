package jas.compatability;

import jas.common.spawner.biome.structure.BiomeHandlerRegistry;
import jas.compatability.tf.BiomeInterpreterTwilightForest;
import cpw.mods.fml.common.Loader;

public class CompatabilityManager {

    public static void addCompatabilityResources() {
        if (Loader.isModLoaded("TwilightForest")) {
            BiomeHandlerRegistry.INSTANCE.registerInterpreter(new BiomeInterpreterTwilightForest());
        }
    }
}
