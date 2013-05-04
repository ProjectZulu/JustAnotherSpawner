package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.Map.Entry;

import net.minecraft.world.World;

import com.google.common.collect.ListMultimap;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsDespawning extends OptionalSettingsBase {
    public OptionalSettingsDespawning(String parseableString) {
        super(parseableString.replace("}", ""));
    }

    @Override
    protected final void parseString() {
        if (stringParsed || parseableString.equals("")) {
            return;
        }
        stringParsed = true;

        /* Set default Paramters that are assumed to be Present */
        valueCache.put(Key.spawnRate.key, 40);
        valueCache.put(Key.blockRangeX.key, 3);
        valueCache.put(Key.blockRangeY.key, 3);
        valueCache.put(Key.blockRangeZ.key, 3);

        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase(Key.despawn.key)) {
                    isEnabled = true;
                    isInverted = false;
                } else if (masterParts[i].equalsIgnoreCase(Key.notDespawn.key)) {
                    isEnabled = true;
                    isInverted = true;
                } else {
                    JASLog.severe("Optional Settings Error expected %s from within %s", Key.despawn.key, masterParts[i]);
                    break;
                }
            } else {
                String[] childParts = masterParts[i].split(",");
                Operand operand = Operand.OR;
                if (childParts[0].startsWith("&")) {
                    operand = Operand.AND;
                    childParts[0] = childParts[0].substring(1);
                } else if (childParts[0].startsWith("|")) {
                    childParts[0] = childParts[0].substring(1);
                }

                Key key = Key.getKeybyString(childParts[0]);
                switch (key) {
                case light:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseLight(childParts)), operand);
                    break;
                case block:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseBlock(childParts)), operand);
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
                case sky:
                case noSky:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseSky(childParts)), operand);
                    break;
                case despawnAge:
                    OptionalParser.parseDespawnAge(childParts, valueCache);
                    break;
                case maxSpawnRange:
                    OptionalParser.parseMaxSpawnRange(childParts, valueCache);
                    break;
                case minSpawnHeight:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseMinSpawnHeight(childParts)), operand);
                    break;
                case maxSpawnHeight:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseMaxSpawnHeight(childParts)), operand);
                    break;
                default:
                    JASLog.warning("Did Not Recognize a valid Despawn properties from %s.", masterParts[i]);
                    break;
                }
            }
        }
    }

    public int getRate() {
        parseString();
        return (Integer) valueCache.get(Key.spawnRate.key);
    }

    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord) {
        boolean outcome = true;
        for (int i = 0; i < parsedChainable.size(); i++) {
            TypeValuePair typeValuePair = parsedChainable.get(i);
            if (i != 0) {
                if (operandvalue.get(i) == Operand.AND && outcome == true) {
                    continue;
                } else if (operandvalue.get(i) == Operand.OR && outcome == false) {
                    return false;
                }
            }

            Key key = typeValuePair.getType();
            switch (key) {
            case light:
                int[] lightLevels = (int[]) typeValuePair.getValue();
                int lightLevel = world.getBlockLightValue(xCoord, yCoord, zCoord);
                outcome = lightLevel > lightLevels[1] || lightLevel < lightLevels[0];
                break;
            case block:
                outcome = isValidBlock(world, xCoord, yCoord, zCoord, typeValuePair);
                break;
            case sky:
                boolean sky = (Boolean) typeValuePair.getValue();
                boolean canSeeSky = canBlockSeeTheSky(world, xCoord, yCoord, zCoord);
                outcome = sky ? !canSeeSky : canSeeSky;
                break;
            case minSpawnHeight:
                Integer minSpawnHeight = (Integer) typeValuePair.getValue();
                outcome = yCoord < minSpawnHeight ? true : false;
                break;
            case maxSpawnHeight:
                Integer maxSpawnHeight = (Integer) typeValuePair.getValue();
                outcome = yCoord > maxSpawnHeight ? true : false;
                break;
            default:
                break;
            }
        }
        return outcome;
    }

    /**
     * Represents Restriction on isValidBlock.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    private boolean isValidBlock(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair) {
        @SuppressWarnings("unchecked")
        ListMultimap<Integer, Integer> iDMetas = (ListMultimap<Integer, Integer>) typeValuePair.getValue();
        int xRange = (Integer) valueCache.get(Key.blockRangeX.key);
        int yRange = (Integer) valueCache.get(Key.blockRangeY.key);
        int zRange = (Integer) valueCache.get(Key.blockRangeZ.key);
        for (int i = -xRange; i <= xRange; i++) {
            for (int k = -zRange; k <= zRange; k++) {
                for (int j = -yRange; j <= yRange; j++) {
                    for (Entry<Integer, Integer> iDMetaEntry : iDMetas.entries()) {
                        int blockID = world.getBlockId(xCoord + i, yCoord + j, zCoord + k);
                        int meta = world.getBlockMetadata(xCoord + i, yCoord + j, zCoord + k);
                        if (blockID == iDMetaEntry.getKey() && meta == iDMetaEntry.getValue()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isValidAge(int currentAge, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.despawnAge);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return currentAge > defaultCutoff;
    }

    /**
     * 
     * @param playerDistance Distance to the playe rin [m^2]
     * @param defaultCutoff Default Range in [m]
     * @return
     */
    public boolean isMaxDistance(int playerDistance, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.maxSpawnRange);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return playerDistance > defaultCutoff * defaultCutoff;
    }
}
