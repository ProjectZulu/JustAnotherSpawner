package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.ArrayList;

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
                    OptionalParser.parseLight(childParts, valueCache);
                    break;
                case block:
                    OptionalParser.parseBlock(childParts, valueCache);
                    break;
                case blockRange:
                    OptionalParser.parseBlockRange(childParts, valueCache);
                    break;
                case spawnRange:
                    OptionalParser.parseSpawnRange(childParts, valueCache);
                    break;
                case spawnRate:
                    OptionalParser.parseSpawnRate(childParts, valueCache);
                    break;
                default:
                    JASLog.warning("Did Not Recognize a valid Despawn properties from %s.", masterParts[i]);
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

    /**
     * Checks if the Distance to
     * 
     * @param playerDistance Distance Squared to Nearest Player
     * @return True to Continue as Normal, False to Interrupt, Null Use Global Check
     */
    public Boolean isValidDistance(int playerDistance) {
        parseString();
        Integer distanceToPlayer = (Integer) valueCache.get(Key.spawnRange);
        return distanceToPlayer != null ? playerDistance > distanceToPlayer * distanceToPlayer : null;
    }
}
