package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import net.minecraft.world.World;

public class OptionalSettingsDespawning {

    public final String parseableString;
    private boolean stringParsed = false;

    /* Internal Cache to Store Values */
    private HashMap<String, Object> valueCache = new HashMap<String, Object>();

    public static final String enabledKey = "overrideDespawn";
    public static final String minLightLevelKey = "minLightLevel";
    public static final String maxLightLevelKey = "maxLightLevel";
    public static final String spawnRateKey = "spawnRate";

    public static final String blockRangeKey = "blockRange";
    public static final String blockRangeXKey = "blockRangeXKey";
    public static final String blockRangeYKey = "blockRangeYKey";
    public static final String blockRangeZKey = "blockRangeZKey";
    public static final String blocksKey = "blocksKey";
    public static final String metaKey = "metasKey";

    public OptionalSettingsDespawning(String parseableString) {
        this.parseableString = parseableString.replace("}", "");
    }

    private void parseString() {
        if (stringParsed) {
            return;
        }
        stringParsed = true;
        /* Set default Paramters that are assumed to be Present */
        valueCache.put(minLightLevelKey, -1);
        valueCache.put(maxLightLevelKey, -1);
        valueCache.put(blocksKey, new ArrayList<Integer>());
        valueCache.put(metaKey, new ArrayList<Integer>());
        valueCache.put(spawnRateKey, 40);
        valueCache.put(blockRangeXKey, 3);
        valueCache.put(blockRangeYKey, 3);
        valueCache.put(blockRangeZKey, 3);

        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase("despawn")) {
                    valueCache.put(enabledKey, Boolean.TRUE);
                } else {
                    JASLog.severe("Optional Settings Error expected deSpawn from within %s", masterParts[i]);
                }
            } else {
                String[] childParts = masterParts[i].split(",");
                if (childParts[0].equalsIgnoreCase("light")) {
                    if (childParts.length == 3) {
                        valueCache.put(minLightLevelKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(minLightLevelKey), "minLightLevel"));
                        valueCache.put(maxLightLevelKey, ParsingHelper.parseInteger(childParts[2],
                                (Integer) valueCache.get(maxLightLevelKey), "maxLightLevel"));
                    } else {
                        JASLog.severe("Error Parsing deSpawn Light Parameter. Invalid Length");
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
                } else if (childParts[0].equalsIgnoreCase(blockRangeKey)) {
                    if (childParts.length == 4) {
                        valueCache.put(blockRangeXKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(blockRangeXKey), "blockRangeX"));
                        valueCache.put(blockRangeYKey, ParsingHelper.parseInteger(childParts[2],
                                (Integer) valueCache.get(blockRangeYKey), "blockRangeY"));
                        valueCache.put(blockRangeZKey, ParsingHelper.parseInteger(childParts[3],
                                (Integer) valueCache.get(blockRangeZKey), "blockRangeZ"));
                    } else if (childParts.length == 2) {
                        valueCache.put(blockRangeXKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(blockRangeXKey), "blockRangeX"));
                        valueCache.put(blockRangeYKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(blockRangeYKey), "blockRangeY"));
                        valueCache.put(blockRangeZKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(blockRangeZKey), "blockRangeZ"));
                    } else {
                        JASLog.severe("Error Parsing deSpawn block search range Parameter. Invalid Length");
                    }
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                } else if (childParts[0].equalsIgnoreCase(spawnRateKey)) {
                    if (childParts.length == 2) {
                        valueCache.put(spawnRateKey, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(spawnRateKey), "spawnRateKey"));
                    } else {
                        JASLog.severe("Error Parsing deSpawn spawn rate Parameter. Invalid Length");
                    }
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                } else {
                    JASLog.warning("Did Not Recognize any valid deSpawn properties from %s.", masterParts[i]);
                }
            }
        }
    }

    public boolean isOptionalEnabled() {
        parseString();
        return valueCache.get(enabledKey) != null;
    }

    public int getRate() {
        parseString();
        return (Integer) valueCache.get(spawnRateKey);
    }

    /**
     * Represents Restriction on LightLevel.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    public boolean isValidLightLevel(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        int lightLevel = world.getBlockLightValue(xCoord, yCoord, zCoord);
        return lightLevel > (Integer) valueCache.get(maxLightLevelKey)
                || lightLevel < (Integer) valueCache.get(minLightLevelKey);
    }

    /**
     * Represents Restriction on isValidBlock.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    @SuppressWarnings("unchecked")
    public boolean isValidBlock(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        ArrayList<Integer> blockIDlist = (ArrayList<Integer>) valueCache.get(blocksKey);
        ArrayList<Integer> metaIDList = (ArrayList<Integer>) valueCache.get(metaKey);
        if (blockIDlist.isEmpty()) {
            return true;
        }

        int xRange = (Integer) valueCache.get(blockRangeXKey);
        int yRange = (Integer) valueCache.get(blockRangeYKey);
        int zRange = (Integer) valueCache.get(blockRangeZKey);
        for (int i = -xRange; i <= xRange; i++) {
            for (int k = -zRange; k <= zRange; k++) {
                for (int j = -yRange; j <= yRange; j++) {
                    for (int m = 0; m < blockIDlist.size(); m++) {
                        int blockID = world.getBlockId(xCoord + i, yCoord + j, zCoord + k);
                        int meta = world.getBlockMetadata(xCoord + i, yCoord + j, zCoord + k);
                        if (blockID == blockIDlist.get(m) && meta == metaIDList.get(m)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
