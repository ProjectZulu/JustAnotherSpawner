package jas.common.config;

import jas.common.DefaultProps;
import jas.common.Properties;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class LivingConfiguration extends Configuration {

    public final String filename;

    /**
     * 
     * @param configDirectory
     * @param fileName Filename for this Configuration, typically modID
     */
    public LivingConfiguration(File configDirectory, String fileName) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + Properties.saveName + "/"
                + DefaultProps.ENTITYSUBDIR + fileName + ".cfg"));
        this.filename = fileName;
    }

    public Property getLivingHandler(String mobName, String value) {
        return this.get("CreatureSettings.LivingHandler", mobName, value);
    }
}
