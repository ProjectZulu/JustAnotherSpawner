package jas.common.config;

import jas.common.DefaultProps;
import jas.common.Properties;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class StructureConfiguration extends Configuration {
    public static final String StructureConfigName = "StructureSpawns.cfg";

    public StructureConfiguration(File configDirectory) {
        super(
                new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + Properties.saveName + "/"
                        + StructureConfigName));
    }

    public Property getStructureSpawns(String structureKey, String entityList) {
        return this.get("CreatureSettings.SpawnList", structureKey, entityList);
    }

}
