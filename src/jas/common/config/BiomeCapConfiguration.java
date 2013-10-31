package jas.common.config;

import jas.common.DefaultProps;
import jas.common.WorldProperties;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class BiomeCapConfiguration extends Configuration {

    public static final String ConfigName = "BiomeCaps.cfg";
    public static final String BaseCategory = "biomecaps";
    public static final String CapCategory = BaseCategory + ".caps";

    public BiomeCapConfiguration(File configDirectory, WorldProperties worldProperties) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName + "/" + ConfigName));
    }

    public Property getIsEnabled(boolean defaultValue) {
        return get(BaseCategory, "isenabled", defaultValue);
    }

    public Property getCap(String packageName, int defaultCap) {
        return get(CapCategory, packageName, defaultCap);
    }
}
