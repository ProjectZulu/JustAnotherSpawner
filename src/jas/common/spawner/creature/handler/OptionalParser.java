package jas.common.spawner.creature.handler;

import jas.common.JASLog;
import jas.common.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class OptionalParser {

    /**
     * Parses the Light Tag.
     * 
     * Format [0] Tag, [1] MinLightLevel, [2] MaxLightLevel.
     * 
     * @param values Values to be Used for Parsing
     * @param valueCache Cache used by OptionalSettings to hold values
     */
    public static void parseLight(String[] values, HashMap<String, Object> valueCache) {
        if (values.length == 3) {
            valueCache.put(Key.minLightLevel.key, ParsingHelper.parseInteger(values[1],
                    (Integer) valueCache.get(Key.minLightLevel.key), Key.minLightLevel.key));
            valueCache.put(Key.maxLightLevel.key, ParsingHelper.parseInteger(values[2],
                    (Integer) valueCache.get(Key.maxLightLevel.key), Key.maxLightLevel.key));
        } else {
            JASLog.severe("Error Parsing deSpawn Light Parameter. Invalid Argument Length.");
        }
    }

    /**
     * Parses the Block Tag.
     * 
     * @param values Values to be Used for Parsing
     * @param valueCache Cache used by OptionalSettings to hold values
     */
    public static void parseBlock(String[] values, HashMap<String, Object> valueCache) {
        ArrayList<Integer> blockList = new ArrayList<Integer>();
        ArrayList<Integer> metaList = new ArrayList<Integer>();
        for (int j = 1; j < values.length; j++) {
            int minID = -1;
            int maxID = -1;
            int minMeta = 0;
            int maxMeta = 0;
            /* Parse Scenario: 2>4-1>2 ADDS (Block,Meta)(2,1)(2,2)(3,1)(3,2)(4,1)(4,2) */
            String[] idMetaParts = values[j].split("-");
            for (int k = 0; k < idMetaParts.length; k++) {
                String[] rangeParts = idMetaParts[k].split(">");
                if (k == 0) {
                    for (int l = 0; l < rangeParts.length; l++) {
                        if (l == 0) {
                            minID = ParsingHelper.parseInteger(rangeParts[l], minID, "parseMinBlockID");
                        } else if (l == 1) {
                            maxID = ParsingHelper.parseInteger(rangeParts[l], maxID, "parseMaxBlockID");
                        } else {
                            JASLog.warning("Block entry %s contains too many > elements.", values[j]);
                        }
                    }
                } else if (k == 1) {
                    for (int l = 0; l < rangeParts.length; l++) {
                        if (l == 0) {
                            minMeta = ParsingHelper.parseInteger(rangeParts[l], minID, "parseMinMetaID");
                        } else if (l == 1) {
                            maxMeta = ParsingHelper.parseInteger(rangeParts[l], minID, "parseMaxMetaID");
                        } else {
                            JASLog.warning("Block entry %s contains too many > elements.", values[j]);
                        }
                    }
                } else {
                    JASLog.warning("Block entry %s contains too many - elements.", values[j]);
                }
            }

            /* Gaurantee Max > Min. Auxillary Purpose: Gaurantees max is not -1 if only min is Set */
            maxID = minID > maxID ? minID : maxID;
            maxMeta = minMeta > maxMeta ? minMeta : maxMeta;

            for (int id = minID; id <= maxID; id++) {
                for (int meta = minMeta; meta <= maxMeta; meta++) {
                    if (id != -1) {
                        JASLog.debug(Level.INFO, "Would be adding (%s,%s)", id, meta);
                        blockList.add(id);
                        metaList.add(meta);
                    }
                }
            }
        }
        valueCache.put(Key.blockList.key, blockList);
        valueCache.put(Key.metaList.key, metaList);
    }

    /**
     * Parses the BlockRange Tag.
     * 
     * @param values Values to be Used for Parsing
     * @param valueCache Cache used by OptionalSettings to hold values
     */
    public static void parseBlockRange(String[] values, HashMap<String, Object> valueCache) {
        if (values.length == 4) {
            valueCache
                    .put(Key.blockRangeX.key, ParsingHelper.parseInteger(values[1],
                            (Integer) valueCache.get(Key.blockRangeX.key), "blockRangeX"));

            valueCache
                    .put(Key.blockRangeY.key, ParsingHelper.parseInteger(values[2],
                            (Integer) valueCache.get(Key.blockRangeY.key), "blockRangeY"));
            valueCache
                    .put(Key.blockRangeZ.key, ParsingHelper.parseInteger(values[3],
                            (Integer) valueCache.get(Key.blockRangeZ.key), "blockRangeZ"));
        } else if (values.length == 2) {
            valueCache
                    .put(Key.blockRangeX.key, ParsingHelper.parseInteger(values[1],
                            (Integer) valueCache.get(Key.blockRangeX.key), "blockRangeX"));
            valueCache
                    .put(Key.blockRangeY.key, ParsingHelper.parseInteger(values[1],
                            (Integer) valueCache.get(Key.blockRangeY.key), "blockRangeY"));
            valueCache
                    .put(Key.blockRangeZ.key, ParsingHelper.parseInteger(values[1],
                            (Integer) valueCache.get(Key.blockRangeZ.key), "blockRangeZ"));
        } else {
            JASLog.severe("Error Parsing deSpawn block search range Parameter. Invalid Argument Length.");
        }
    }

    /**
     * Parses the SpawnRate Tag.
     * 
     * @param values Values to be Used for Parsing
     * @param valueCache Cache used by OptionalSettings to hold values
     */
    public static void parseSpawnRate(String[] values, HashMap<String, Object> valueCache) {
        if (values.length == 2) {
            valueCache.put(Key.spawnRate.key, ParsingHelper.parseInteger(values[1],
                    (Integer) valueCache.get(Key.spawnRate.key), Key.spawnRate.key));
        } else {
            JASLog.severe("Error Parsing deSpawn spawn rate Parameter. Invalid Argument Length.");
        }
    }

    /**
     * Parses the SpawnRange Tag.
     * 
     * @param values Values to be Used for Parsing
     * @param valueCache Cache used by OptionalSettings to hold values
     */
    public static void parseSpawnRange(String[] values, HashMap<String, Object> valueCache) {
        if (values.length == 2) {
            valueCache.put(Key.spawnRange.key,
                    ParsingHelper.parseInteger(values[1], Properties.despawnDist, Key.spawnRange.key));
        } else {
            JASLog.severe("Error Parsing spawnRange parameter. Invalid Argument Length.");
        }
    }

    public static void parseSky(String[] values, HashMap<String, Object> valueCache) {
        if (values.length == 1) {
            if (Key.sky.key.equalsIgnoreCase(values[0])) {
                valueCache.put(Key.sky.key, Boolean.TRUE);
            } else {
                valueCache.put(Key.sky.key, Boolean.FALSE);
            }
        } else {
            JASLog.severe("Error Parsing Needs Sky parameter. Invalid Argument Length.");
        }
    }
}
