package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;
import java.util.logging.Level;

public class OptionalSettingsSpawning extends OptionalSettings {
    public OptionalSettingsSpawning(String parseableString) {
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

        if (parseableString.equals("")) {
            return;
        }
        
        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase(Key.spawn.key)) {
                    valueCache.put(Key.enabled.key, Boolean.TRUE);
                } else {
                    JASLog.severe("Optional Settings Error expected spawn from %s", masterParts[i]);
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
                        JASLog.severe("Error Parsing Spawn Light Parameter. Invalid Length");
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

                case material:
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                    break;

                default:
                    JASLog.severe("Could Not Recognize any valid Spawn properties from %s", masterParts[i]);
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

    public boolean isValidLightLevel(int lightLevel) {
        parseString();
        return lightLevel > (Integer) valueCache.get(Key.maxLightLevel.key)
                || lightLevel < (Integer) valueCache.get(Key.minLightLevel.key);
    }

    @SuppressWarnings("unchecked")
    public boolean isValidBlock(int blockID, int meta) {
        parseString();
        ArrayList<Integer> blockIDlist = (ArrayList<Integer>) valueCache.get(Key.blockList.key);
        ArrayList<Integer> metaIDList = (ArrayList<Integer>) valueCache.get(Key.metaList.key);

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
