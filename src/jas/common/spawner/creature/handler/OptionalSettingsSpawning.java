package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsSpawning extends OptionalSettingsBase {
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
                } else if (masterParts[i].equalsIgnoreCase(Key.notSpawn.key)) {
                    valueCache.put(Key.enabled.key, Boolean.FALSE);
                } else {
                    JASLog.severe("Optional Settings Error expected spawn from %s", masterParts[i]);
                    break;
                }
            } else {
                String[] childParts = masterParts[i].split(",");

                switch (Key.getKeybyString(childParts[0])) {
                case light:
                    OptionalParser.parseLight(childParts, valueCache);
                    break;
                case block:
                    OptionalParser.parseBlock(childParts, valueCache);
                    break;
                case spawnRange:
                    OptionalParser.parseSpawnRange(childParts, valueCache);
                    break;
                case sky:
                case noSky:
                    OptionalParser.parseSky(childParts, valueCache);
                    break;
                case material:
                    // TODO: Add Material Tag ? Air or Water? What is the point?
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                    break;
                case entityCap:
                    OptionalParser.parseEntityCap(childParts, valueCache);
                    break;
                default:
                    JASLog.severe("Could Not Recognize any valid Spawn properties from %s", masterParts[i]);
                    break;
                }
            }
        }
    }

    public Integer getEntityCap() {
        return (Integer) valueCache.get(Key.entityCap.key);
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

}
