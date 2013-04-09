package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class OptionalSettingsSpawning {

    public final String parseableString;
    private boolean stringParsed = false;

    /* Internal Cache to Store Values */
    private HashMap<String, Object> valueCache = new HashMap<String, Object>();

    public static final String overrideSpawnKey = "overrideSpawn";
    public static final String minLightLevelKey = "minLightLevel";
    public static final String maxLightLevelKey = "maxLightLevel";
    public static final String blocksKey = "blocksKey";
    public static final String metaKey = "metasKey";

    public OptionalSettingsSpawning(String parseableString) {
        this.parseableString = parseableString.replace("}", "");
    }

    private void parseString() {
        if (stringParsed) {
            return;
        }
        stringParsed = true;

        /* Set default Paramters that are assumed to be Present */
        valueCache.put(minLightLevelKey, 0);
        valueCache.put(maxLightLevelKey, 15);
        valueCache.put(blocksKey, new ArrayList<Integer>());
        valueCache.put(metaKey, new ArrayList<Integer>());

        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase("spawn")) {
                    valueCache.put(overrideSpawnKey, Boolean.TRUE);
                } else {
                    JASLog.severe("Optional Settings Error expected spawn from %s", masterParts[i]);
                }
            } else {
                String[] childParts = masterParts[i].split(",");
                if (childParts[0].equalsIgnoreCase("light")) {
                    if (!parseableString.equals("")) {
                        JASLog.info("light detected");
                    }
                    if (childParts.length == 3) {
                        valueCache.put(minLightLevelKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(minLightLevelKey), "minLightLevel"));
                        valueCache.put(maxLightLevelKey, ParsingHelper.parseInteger(childParts[2],
                                (Integer) valueCache.get(maxLightLevelKey), "maxLightLevel"));
                    } else {
                        JASLog.severe("Error Parsing Spawn Light Parameter. Invalid Length");
                    }
                } else if (childParts[0].equalsIgnoreCase("block")) {
                    ArrayList<Integer> blockList = new ArrayList<Integer>();
                    ArrayList<Integer> metaList = new ArrayList<Integer>();
                    for (int j = 1; j < childParts.length; j++) {
                        int minID = -1;
                        int maxID = -1;
                        int minMeta = 0;
                        int maxMeta = 0;
                        /* Parse Scenario: 2>4-1>2 ADDS (Block,Meta)(2,1)(2,2)(3,1)(3,2)(4,1)(4,2) */
                        String[] idMetaParts = childParts[j].split("-");
                        for (int k = 0; k < idMetaParts.length; k++) {
                            String[] rangeParts = idMetaParts[k].split(">");
                            if (k == 0) {
                                for (int l = 0; l < rangeParts.length; l++) {
                                    if (l == 0) {
                                        minID = ParsingHelper.parseInteger(rangeParts[l], minID, "parseMinBlockID");
                                    } else if (l == 1) {
                                        maxID = ParsingHelper.parseInteger(rangeParts[l], maxID, "parseMaxBlockID");
                                    } else {
                                        JASLog.warning("Block entry %s contains too many > elements.", childParts[j]);
                                    }
                                }
                            } else if (k == 1) {
                                for (int l = 0; l < rangeParts.length; l++) {
                                    if (l == 0) {
                                        minMeta = ParsingHelper.parseInteger(rangeParts[l], minID, "parseMinMetaID");
                                    } else if (l == 1) {
                                        maxMeta = ParsingHelper.parseInteger(rangeParts[l], minID, "parseMaxMetaID");
                                    } else {
                                        JASLog.warning("Block entry %s contains too many > elements.", childParts[j]);
                                    }
                                }
                            } else {
                                JASLog.warning("Block entry %s contains too many - elements.", childParts[j]);
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
                    valueCache.put(blocksKey, blockList);
                    valueCache.put(metaKey, metaList);
                } else if (childParts[0].equalsIgnoreCase("material")) {
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                } else {
                    JASLog.severe("Could Not Recognize any valid Spawn properties from %s", masterParts[i]);
                }
            }
        }
    }

    public boolean overrideLocationCheck() {
        parseString();
        return valueCache.get(overrideSpawnKey) != null;
    }

    public boolean isValidLightLevel(int lightLevel) {
        parseString();
        return lightLevel > (Integer) valueCache.get(maxLightLevelKey)
                || lightLevel < (Integer) valueCache.get(minLightLevelKey);
    }

    @SuppressWarnings("unchecked")
    public boolean isValidBlock(int blockID, int meta) {
        parseString();
        ArrayList<Integer> blockIDlist = (ArrayList<Integer>) valueCache.get(blocksKey);
        ArrayList<Integer> metaIDList = (ArrayList<Integer>) valueCache.get(metaKey);

        for (int i = 0; i < blockIDlist.size(); i++) {
            if (blockID == blockIDlist.get(i) && meta == metaIDList.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
