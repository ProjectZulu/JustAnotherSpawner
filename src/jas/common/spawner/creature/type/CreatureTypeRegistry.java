package jas.common.spawner.creature.type;

import jas.common.DefaultProps;
import jas.common.JASLog;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.block.material.Material;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;

public enum CreatureTypeRegistry {
    INSTANCE;
    /** Hashmap containing Creature Types. */
    private final HashMap<String, CreatureType> types = new HashMap<String, CreatureType>();

    /**
     * Default Category Keys. Note that 'NONE' is not a Type but the absence of, i.e. null. This is NOT placed inside
     * the types Hashmap which should not contain null entries
     */
    public static final String NONE = "NONE";
    public static final String CREATURE = "CREATURE";
    public static final String MONSTER = "MONSTER";
    public static final String AMBIENT = "AMBIENT";
    public static final String WATERCREATURE = "WATERCREATURE";
    public static final String UNDERGROUND = "UNDERGROUND";
    public static final String OPENSKY = "OPENSKY";

    public void addSpawnCategory(CreatureType creatureType) {
        if (types.containsKey(creatureType.typeID.toUpperCase())) {
            throw new IllegalArgumentException("Creature Type " + creatureType.typeID
                    + " already exists. Brace for crash Landing!");
        }
        JASLog.info("Category Added %s", creatureType.typeID);
        types.put(creatureType.typeID.toUpperCase(), creatureType);
    }

    public Iterator<CreatureType> getCreatureTypes() {
        return types.values().iterator();
    }

    public CreatureType getCreatureType(String typeID) {
        return types.get(typeID.toUpperCase());
    }

    private CreatureTypeRegistry() {
        addSpawnCategory(new CreatureTypeOpensky(CREATURE, 10, Material.air, 400, true));
        addSpawnCategory(new CreatureType(MONSTER, 70, Material.air, 1, false));
        addSpawnCategory(new CreatureType(AMBIENT, 15, Material.air, 1, false));
        addSpawnCategory(new CreatureType(WATERCREATURE, 15, Material.water, 1, false));
        
        addSpawnCategory(new CreatureTypeUnderground(UNDERGROUND, 10, Material.air, 1, false));
        addSpawnCategory(new CreatureTypeOpensky(OPENSKY, 10, Material.air, 1, false));
    }

    public void initializeFromConfig(File configDirectory, MinecraftServer minecraftServer) {
        Configuration masterConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "Master/" + "CreatureType" + ".cfg"));
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + minecraftServer.worldServers[0].getWorldInfo().getWorldName() + "/" + "CreatureType" + ".cfg"));
        masterConfig.load();
        worldConfig.load();
        for (String typeID : types.keySet()) {
            CreatureType creatureType = getTypeFromConfig(worldConfig, typeID,
                    getTypeFromConfig(masterConfig, typeID, types.get(typeID)));
            types.put(typeID, creatureType);
        }
        masterConfig.save();
        worldConfig.save();
    }

    private CreatureType getTypeFromConfig(Configuration config, String typeID, CreatureType defaultSettings) {
        int spawnRate = config.get("LivingType." + typeID, "Spawn Rate", defaultSettings.spawnRate).getInt();
        int maxNumberOfCreature = config.get("LivingType." + typeID, "Creature Spawn Cap",
                defaultSettings.maxNumberOfCreature).getInt();
        boolean chunkSpawning = config.get("LivingType." + typeID, "Do Chunk Spawning", defaultSettings.chunkSpawning)
                .getBoolean(defaultSettings.chunkSpawning);
        return defaultSettings.create(typeID, maxNumberOfCreature, defaultSettings.spawnMedium, spawnRate,
                chunkSpawning);
    }
}