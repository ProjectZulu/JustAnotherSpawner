package jas.common;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Properties {
    public static boolean debugMode = false;
    public static int spawnerTickSpacing = 0;

    public static void loadProperties(File configDirectory) {
        Configuration config = new Configuration(
                new File(configDirectory, DefaultProps.MODDIR + "GlobalProperties.cfg"));
        config.load();
        debugMode = config.get("Properties.Logging", "Debug Mode", debugMode).getBoolean(debugMode);
        Property resultTickSpacing = config.get("Properties.Spawning", "Spawner Tick Spacing", spawnerTickSpacing);
        if (resultTickSpacing.getInt(spawnerTickSpacing) < 0) {
            JASLog.severe(
                    "Error with spawnerTickSpacing is %s. spawnerTickSpacing cannot be less than zero. Setting to 0.",
                    resultTickSpacing.getInt(spawnerTickSpacing));
            resultTickSpacing.set(spawnerTickSpacing);
        } else {
            spawnerTickSpacing = resultTickSpacing.getInt(spawnerTickSpacing);
        }
        config.save();
    }
}
