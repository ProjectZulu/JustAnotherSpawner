package jas.common;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.block.material.Material;

public enum CreatureTypeRegistry {
    INSTANCE;
    private final HashMap<String, CreatureType> types = new HashMap<String, CreatureType>();

    /**
     * Default Category Keys. Note that 'NONE' is not a Type but the absence of, i.e. null
     */
    public static final String NONE = "NONE";
    public static final String CREATURE = "CREATURE";
    public static final String MONSTER = "MONSTER";
    public static final String AMBIENT = "AMBIENT";

    public void addSpawnCategory(String categoryKey, CreatureType creatureType) {
        if (types.containsKey(categoryKey.toUpperCase())) {
            throw new IllegalArgumentException("Creature Type " + categoryKey + " already exists. Brace for crash Landing!");
        }
        types.put(categoryKey.toUpperCase(), creatureType);
    }
    
    public Iterator<CreatureType> getCreatureTypes() {
        return types.values().iterator();
    }

    public CreatureType getCreatureType(String typeID){
        return types.get(typeID.toUpperCase());
    }
    
    private CreatureTypeRegistry() {
        addSpawnCategory(CREATURE, new CreatureType(CREATURE, 10, Material.air, 1400, true));
        addSpawnCategory(MONSTER, new CreatureType(MONSTER, 70, Material.air, 1, true));
//        addSpawnCategory(AMBIENT, new CreatureType(NONE, 15, Material.air, 10, true));
    }
}