package jas.common;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Properties {
    public static boolean debugMode = false;
    public static int spawnerTickSpacing = 0;
    public static boolean sortCreatureByBiome = true;

    public static boolean turnGameruleSpawningOff = false;
    public static boolean emptyVanillaSpawnLists = false;

    public static int despawnDist = 32;
    public static int maxDespawnDist = 128;
    public static int minDespawnTime = 600;

    /**
     * Load Global Properties
     * 
     * @param configDirectory
     */
    public static void loadProperties(File configDirectory) {
        Configuration config = new Configuration(
                new File(configDirectory, DefaultProps.MODDIR + "GlobalProperties.cfg"));
        config.load();
        debugMode = config.get("Properties.Logging", "Debug Mode", debugMode).getBoolean(debugMode);

        turnGameruleSpawningOff = config.get("Properties.Vanilla Controls", "Gamerule doSpawning Off on Start",
                turnGameruleSpawningOff).getBoolean(turnGameruleSpawningOff);
        emptyVanillaSpawnLists = config.get("Properties.Vanilla Controls", "Empty Vanilla SpawnLists on Start",
                emptyVanillaSpawnLists).getBoolean(emptyVanillaSpawnLists);

        sortCreatureByBiome = config.get("Properties.Spawning", "Sort Creature By Biome", sortCreatureByBiome)
                .getBoolean(sortCreatureByBiome);
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

    /**
     * Load World Specific Global Properties
     * 
     * @param configDirectory
     */
    public static void loadWorldProperties(File configDirectory, MinecraftServer minecraftServer) {
        Configuration masterConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "Master/" + "WorldGlobalProperties" + ".cfg"));
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + minecraftServer.worldServers[0].getWorldInfo().getWorldName() + "/" + "WorldGlobalProperties"
                + ".cfg"));
        masterConfig.load();
        worldConfig.load();

        despawnDist = masterConfig.get("Properties.Spawning", "Min Despawn Distance", despawnDist).getInt(despawnDist);
        despawnDist = worldConfig.get("Properties.Spawning", "Min Despawn Distance", despawnDist).getInt(despawnDist);

        masterConfig.save();
        worldConfig.save();
    }
}