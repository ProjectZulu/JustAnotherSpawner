package jas.common.spawner.creature.type;

import jas.common.JASLog;
import jas.common.config.EntityCategoryConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.block.material.Material;

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

    public void updateCreatureType(String typeID, int spawnRate, int maxNumberOfCreature, boolean chunkSpawning) {
        CreatureType creatureType = types.get(typeID);
        types.put(typeID, creatureType.maxNumberOfCreatureTo(maxNumberOfCreature).spawnRateTo(spawnRate)
                .chunkSpawningTo(chunkSpawning));
    }

    private CreatureTypeRegistry() {
        addSpawnCategory(new CreatureType(CREATURE, 10, Material.air, 400, true,
                "{spawn:!solidside,1,0,[0/-1/0]:liquid,0:normal,0:normal,0,[0/1/0]:!opaque,0,[0/-1/0]:!sky}"));
        addSpawnCategory(new CreatureTypeMonster(MONSTER, 70, Material.air, 1, false));
        addSpawnCategory(new CreatureType(AMBIENT, 15, Material.air, 1, false));
        addSpawnCategory(new CreatureType(WATERCREATURE, 15, Material.water, 1, false,
                "{spawn:!liquid,0:!liquid,0,[0/-1/0]:normal,0,[0/1/0]}"));
        addSpawnCategory(new CreatureType(UNDERGROUND, 10, Material.air, 1, false,
                "{spawn:!solidside,1,0,[0/-1/0]:liquid,0:normal,0:normal,0,[0/1/0]:!opaque,0,[0/-1/0]:sky}"));
        addSpawnCategory(new CreatureType(OPENSKY, 10, Material.air, 1, false,
                "{spawn:!solidside,1,0,[0/-1/0]:liquid,0:normal,0:normal,0,[0/1/0]:!opaque,0,[0/-1/0]:!sky}"));
    }

    public void initializeFromConfig(File configDirectory) {
        EntityCategoryConfiguration config = new EntityCategoryConfiguration(configDirectory);
        config.load();
        String customNames = config.getCustomCategories("").getString().toUpperCase();
        for (String typeID : types.keySet()) {
            CreatureType creatureType = types.get(typeID).createFromConfig(config);
            types.put(typeID, creatureType);
        }
        String[] pieces = customNames.split(",");
        for (String categoryName : pieces) {
            if (isValidName(categoryName)) {
                types.put(categoryName,
                        new CreatureType(categoryName, 10, Material.air, 1, false).createFromConfig(config));
            }
        }
        config.save();
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            return false;
        }

        if (name.equalsIgnoreCase(NONE) || name.equalsIgnoreCase(CREATURE) || name.equalsIgnoreCase(MONSTER)
                || name.equalsIgnoreCase(AMBIENT) || name.equalsIgnoreCase(WATERCREATURE)
                || name.equalsIgnoreCase(UNDERGROUND) || name.equalsIgnoreCase(OPENSKY)) {
            JASLog.info("Ignoring Custom Category name %s. Name is reserved.", name);
            return false;
        }
        return true;
    }

    /**
     * Used to save the currently loaded settings into the Configuration Files
     * 
     * If config settings are already present, they will be overwritten
     */
    public void saveToConfig(File configDirectory) {
        EntityCategoryConfiguration config = new EntityCategoryConfiguration(configDirectory);
        config.load();

        /* Save Extra Categories */
        String nameString = "";
        Iterator<String> typeNames = types.keySet().iterator();
        while (typeNames.hasNext()) {
            String typeName = typeNames.next();
            if (isReservedName(typeName)) {
                continue;
            }
            typeName = nameString.concat(typeName);
            if (typeNames.hasNext()) {
                typeName = nameString.concat(",");
            }
        }
        config.getCustomCategories(nameString).set(nameString);

        /* Save All Category Settings */
        for (CreatureType entry : types.values()) {
            entry.saveToConfig(config);
        }
        config.save();
    }

    private boolean isReservedName(String name) {
        if (name.equalsIgnoreCase(NONE) || name.equalsIgnoreCase(CREATURE) || name.equalsIgnoreCase(MONSTER)
                || name.equalsIgnoreCase(AMBIENT) || name.equalsIgnoreCase(WATERCREATURE)
                || name.equalsIgnoreCase(UNDERGROUND) || name.equalsIgnoreCase(OPENSKY)) {
            return true;
        }
        return false;
    }
}