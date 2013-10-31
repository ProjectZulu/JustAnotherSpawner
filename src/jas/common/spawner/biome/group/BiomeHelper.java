package jas.common.spawner.biome.group;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeHelper {
    // This is hacky, but neccesary, as It is what I get for relying on String names in an Integer ID system.
    private static String[] packageNames = new String[BiomeGenBase.biomeList.length];

    /**
     * Convert a BiomeGenBase biomeName to a universal package Biome Name. This is to combat developers who are using
     * identical biome names.
     * 
     * @return BiomeName in the form Package+Class+BiomeName
     */
    public static String getPackageName(BiomeGenBase biome) {
        String packageName = packageNames[biome.biomeID];
        if (packageName != null) {
            return packageName;
        } else {
            packageName = biome.getClass().getName() + "." + biome.biomeName;
            packageNames[biome.biomeID] = packageName;
            return packageName;
        }
    }

    /**
     * Gets the a shortform of the package name. Usually used for display purposes to users when uniqueness is not
     * required.
     * 
     * @return BiomeName in the form Package+Class+BiomeName
     */
    public static String getShortPackageName(BiomeGenBase biome) {
        String[] currentParts = getPackageName(biome).split("\\.");
        String prefix = currentParts.length > 1 ? currentParts[0] : "DUPLICATE";
        return prefix + "." + biome.biomeName;
    }
}
