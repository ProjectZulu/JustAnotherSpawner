package jas.common.spawner.creature.handler;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Represent the readable Tags for OptionalSettings
 */
public enum Key {
    /* Category Tags */
    spawn("spawn"), despawn("despawn"), notSpawn("!spawn"), notDespawn("!despawn"),

    /* Properties */
    enabled("enabled"), sky("sky"), noSky("!sky"), block("block"), light("light"), spawnRange("spawnRange"), minLightLevel(
            "minLightLevel"), maxLightLevel("maxLightLevel"), spawnRate("spawnRate"), blockRange("blockRange"), blockRangeX(
            "blockRangeX"), blockRangeY("blockRangeY"), blockRangeZ("blockRangeZ"), blockList("blocks"), metaList(
            "meta"), material("material"),
    /**/
    UNKNOWN("");

    public final String key;
    private static final HashMap<String, Key> lookupEnum = new HashMap<String, Key>();
    static {
        for (Key key : EnumSet.allOf(Key.class))
            lookupEnum.put(key.key.toUpperCase(), key);
    }

    Key(String key) {
        this.key = key;
    }

    /**
     * Gets the Key associated with the String. Is not case Sensitive
     * 
     * @param string
     * @return
     */
    public static Key getKeybyString(String string) {
        Key value = lookupEnum.get(string.toUpperCase());
        if (value != null) {
            return value;
        } else {
            return UNKNOWN;
        }
    }
}
