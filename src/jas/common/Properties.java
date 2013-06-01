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
    public static String saveName = "";
    public static String importName = "";

    /**
     * Load Global Properties
     * 
     * @param configDirectory
     */
    public static void loadProperties(File configDirectory) {
        Configuration config = new Configuration(
                new File(configDirectory, DefaultProps.MODDIR + "GlobalProperties.cfg"));
        config.load();
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

    public static void loadWorldSaveConfiguration(File configDirectory, MinecraftServer minecraftServer) {
        Configuration worldGloablConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "SaveConfig.cfg"));
        worldGloablConfig.load();
        Property saveProp = worldGloablConfig.get("Save_Configuration."
                + minecraftServer.worldServers[0].getWorldInfo().getWorldName(), "Save_Name",
                minecraftServer.worldServers[0].getWorldInfo().getWorldName(),
                "Folder name to look for and generate CFG files");
        saveName = saveProp.getString().equals("") ? "default" : saveProp.getString();

        Property importProp = worldGloablConfig.get("Save_Configuration", "Import_Name", "",
                "Folder name to Copy Missing Files From");
        importName = importProp.getString();

        worldGloablConfig.save();
    }

    
    /**
     * Load World Specific Global Properties
     * 
     * @param configDirectory
     */
    public static void loadWorldProperties(File configDirectory, MinecraftServer minecraftServer) {
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + saveName + "/" + "WorldGlobalProperties" + ".cfg"));
        worldConfig.load();
        despawnDist = worldConfig.get("Properties.Spawning", "Min Despawn Distance", despawnDist).getInt(despawnDist);
        worldConfig.save();
    }
}