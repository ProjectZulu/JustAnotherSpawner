package jas.common;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.block.material.Material;

public enum CreatureTypeRegistry {
    INSTANCE;
    private final HashMap<String, CreatureType> types = new HashMap<String, CreatureType>();

    /** Default Category Keys */
    public static final String CREATURE = "CREATURE";
    public static final String MONSTER = "MONSTER";
    public static final String AMBIENT = "AMBIENT";

    public void addSpawnCategory(String categoryKey, CreatureType creatureType) {
        if (types.containsKey(categoryKey)) {
            throw new IllegalArgumentException("Creature Type " + categoryKey + " already exists. Brace for crash Landing!");
        }
        types.put(categoryKey, creatureType);
    }
    
    public Iterator<CreatureType> getCreatureTypes() {
        return types.values().iterator();
    }

    private CreatureTypeRegistry() {
        addSpawnCategory(CREATURE, new CreatureType(10, Material.air, 40, true));
        addSpawnCategory(MONSTER, new CreatureType(70, Material.air, 10, true));
        addSpawnCategory(AMBIENT, new CreatureType(15, Material.air, 20, true));
    }
}