package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;
import java.util.logging.Level;

import net.minecraft.world.World;

public class OptionalSettingsDespawning extends OptionalSettings {
    public OptionalSettingsDespawning(String parseableString) {
        super(parseableString.replace("}", ""));
    }

    @Override
    protected final void parseString() {
        if (stringParsed) {
            return;
        }
        stringParsed = true;
        /* Set default Paramters that are assumed to be Present */
        valueCache.put(Key.minLightLevel.key, 16); // Light < Min means Spawn
        valueCache.put(Key.maxLightLevel.key, -1); // Light > Max means Spawn
        valueCache.put(Key.blockList.key, new ArrayList<Integer>());
        valueCache.put(Key.metaList.key, new ArrayList<Integer>());
        valueCache.put(Key.spawnRate.key, 40);
        valueCache.put(Key.blockRangeX.key, 3);
        valueCache.put(Key.blockRangeY.key, 3);
        valueCache.put(Key.blockRangeZ.key, 3);

        if (parseableString.equals("")) {
            return;
        }

        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase(Key.despawn.key)) {
                    valueCache.put(Key.enabled.key, Boolean.TRUE);
                } else {
                    JASLog.severe("Optional Settings Error expected %s from within %s", Key.despawn.key, masterParts[i]);
                    break;
                }
            } else {
                String[] childParts = masterParts[i].split(",");

                switch (Key.getKeybyString(childParts[0])) {
                case light:
                    if (childParts.length == 3) {
                        valueCache.put(
                                Key.minLightLevel.key,
                                ParsingHelper.parseInteger(childParts[1],
                                        (Integer) valueCache.get(Key.minLightLevel.key), Key.minLightLevel.key));
                        valueCache.put(
                                Key.maxLightLevel.key,
                                ParsingHelper.parseInteger(childParts[2],
                                        (Integer) valueCache.get(Key.maxLightLevel.key), Key.maxLightLevel.key));
                    } else {
                        JASLog.severe("Error Parsing deSpawn Light Parameter. Invalid Length");
                    }
                    break;

                case block:
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
                    valueCache.put(Key.blockList.key, blockList);
                    valueCache.put(Key.metaList.key, metaList);
                    break;

                case blockRange:
                    if (childParts.length == 4) {
                        valueCache.put(Key.blockRangeX.key, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(Key.blockRangeX.key), "blockRangeX"));
                        valueCache.put(Key.blockRangeY.key, ParsingHelper.parseInteger(childParts[2],
                                (Integer) valueCache.get(Key.blockRangeY.key), "blockRangeY"));
                        valueCache.put(Key.blockRangeZ.key, ParsingHelper.parseInteger(childParts[3],
                                (Integer) valueCache.get(Key.blockRangeZ.key), "blockRangeZ"));
                    } else if (childParts.length == 2) {
                        valueCache.put(Key.blockRangeX.key, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(Key.blockRangeX.key), "blockRangeX"));
                        valueCache.put(Key.blockRangeY.key, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(Key.blockRangeY.key), "blockRangeY"));
                        valueCache.put(Key.blockRangeZ.key, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(Key.blockRangeZ.key), "blockRangeZ"));
                    } else {
                        JASLog.severe("Error Parsing deSpawn block search range Parameter. Invalid Length");
                    }
                    break;
                case spawnRate:
                    if (childParts.length == 2) {
                        valueCache.put(Key.spawnRate.key, ParsingHelper.parseInteger(childParts[1],
                                (Integer) valueCache.get(Key.spawnRate.key), Key.spawnRate.key));
                    } else {
                        JASLog.severe("Error Parsing deSpawn spawn rate Parameter. Invalid Length");
                    }
                    break;
                default:
                    JASLog.warning("Did Not Recognize a valid deSpawn properties from %s.", masterParts[i]);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isOptionalEnabled() {
        parseString();
        return valueCache.get(Key.enabled.key) != null;
    }

    public int getRate() {
        parseString();
        return (Integer) valueCache.get(Key.spawnRate.key);
    }

    /**
     * Represents Restriction on LightLevel.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    public boolean isValidLightLevel(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        int lightLevel = world.getBlockLightValue(xCoord, yCoord, zCoord);
        return lightLevel > (Integer) valueCache.get(Key.maxLightLevel.key)
                || lightLevel < (Integer) valueCache.get(Key.minLightLevel.key);
    }

    /**
     * Represents Restriction on isValidBlock.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    @SuppressWarnings("unchecked")
    public boolean isValidBlock(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        ArrayList<Integer> blockIDlist = (ArrayList<Integer>) valueCache.get(Key.blockList.key);
        ArrayList<Integer> metaIDList = (ArrayList<Integer>) valueCache.get(Key.metaList.key);
        if (blockIDlist.isEmpty()) {
            return true;
        }

        int xRange = (Integer) valueCache.get(Key.blockRangeX.key);
        int yRange = (Integer) valueCache.get(Key.blockRangeY.key);
        int zRange = (Integer) valueCache.get(Key.blockRangeZ.key);
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
