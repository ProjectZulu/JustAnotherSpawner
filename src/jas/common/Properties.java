package jas.common;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Properties {
    public static boolean debugMode = false;
    public static int spawnerTickSpacing = 0;
    public static boolean globalSortCreatureByBiome = true;

    /* Functional Universal Directory Settings, marks the Way the System should Sort */
    public static boolean universalDirectory = false;
    /* Placeholder used to determine if the current directory needs to be deleted and changed */
    public static boolean loadedUniversalDirectory = false;

    /* Functional Universal Directory Settings, marks the Way the System should Sort */
    public static boolean savedSortCreatureByBiome = true;
    /* Placeholder used to determine if the current directory needs to be deleted and changed */
    public static boolean loadedSortCreatureByBiome = true;

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

        globalSortCreatureByBiome = config.get("Properties.Spawning", "Sort Creature By Biome",
                globalSortCreatureByBiome).getBoolean(globalSortCreatureByBiome);
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
        String curWorldName = minecraftServer.worldServers[0].getWorldInfo().getWorldName();
        worldGloablConfig.load();

        /* Load Save Use Import_Name */
        Property importProp = worldGloablConfig.get("Save_Configuration", "Import_Name", "",
                "Folder name to Copy Missing Files From. Case Sensitive if OS allows. Beware invalid OS characters.");
        importName = importProp.getString();

        /* Load Save Use Global Save_Name */
        Property defaultsaveProp = worldGloablConfig
                .get("Save_Configuration",
                        "Default Save_Name",
                        "{$world}",
                        "Default name used for Save_Name. {$world} is replaced by world name. Case Sensitive if OS allows. Beware invalid OS characters.");
        saveName = defaultsaveProp.getString().replace("{$world}", curWorldName);

        /* Load Save Use Actual Save_Name */
        Property saveProp = worldGloablConfig
                .get("Save_Configuration." + curWorldName, "Save_Name", saveName,
                        "Folder name to look for and generate CFG files. Case Sensitive if OS allows. Beware invalid OS characters.");
        saveName = saveProp.getString().trim().equals("") ? "default" : saveProp.getString();

        /* Load Save Sort Creature By Biome */
        loadedSortCreatureByBiome = worldGloablConfig
                .get("Save_Configuration." + curWorldName, "Sort Creature By Biome - Setting",
                        globalSortCreatureByBiome,
                        "Determines if Entity CFGs are sorted internally by Entity or Biome. Change from TRUE to FALSE to alter sorting.")
                .getBoolean(globalSortCreatureByBiome);

        Property sortProperty = worldGloablConfig.get("Save_Configuration." + curWorldName + ".save data - ignore me",
                "Sort Creature By Biome - Memory", globalSortCreatureByBiome,
                "DO NOT TOUCH. Internally used to remember how the Configuration file was actually saved.");
        savedSortCreatureByBiome = sortProperty.getBoolean(globalSortCreatureByBiome);
        sortProperty.set(loadedSortCreatureByBiome);

        /* Load Save Use Universal Entity Directory */
        loadedUniversalDirectory = worldGloablConfig.get("Save_Configuration." + curWorldName,
                "Universal Entity CFG - Settings", false,
                "Specifies if the User wants the Entity CFG to Combined into a Universal CFG.").getBoolean(false);
        Property univerProp = worldGloablConfig.get("Save_Configuration." + curWorldName + ".save data - ignore me",
                "Universal Entity CFG - Memory", loadedUniversalDirectory,
                "DO NOT TOUCH. Internally used to remember how the Configuration directory was actually saved.");
        universalDirectory = univerProp.getBoolean(loadedUniversalDirectory);
        univerProp.set(loadedUniversalDirectory);

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