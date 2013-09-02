package jas.common.config;

import jas.common.DefaultProps;
import jas.common.WorldProperties;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class StructureConfiguration extends Configuration {
    public static final String StructureConfigName = "StructureSpawns.cfg";

    public StructureConfiguration(File configDirectory, WorldProperties worldProperties) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName + "/"
                + StructureConfigName));
    }

    public Property getStructureSpawns(String structureKey, String entityList) {
        return this.get("CreatureSettings.SpawnList", structureKey, entityList);
    }

}
