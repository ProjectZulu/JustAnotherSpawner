package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.keys.Key;
import jas.common.spawner.creature.handler.parsing.keys.KeyParser.KeyType;

import java.util.EnumSet;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

/**
 * For style see {@link OptionalSettings}
 */
public abstract class OptionalSettingsBase extends OptionalSettings {

    public static int defaultBlockRange = 3;
    public static int defaultSpawnRate = 40;

    public OptionalSettingsBase(String parseableString, EnumSet<Key> validKeys) {
        super(parseableString, validKeys);
        parseString();
    }

    @Override
    public boolean isOptionalEnabled() {
        return isEnabled;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    @Override
    protected final void parseString() {
        if (stringParsed || parseableString.equals("")) {
            return;
        }
        stringParsed = true;
        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                for (Key key : validKeys) {
                    if (key.keyParser == null || key.keyParser.getKeyType() != KeyType.PARENT) {
                        continue;
                    }
                    if (key.keyParser.isMatch(masterParts[i])) {
                        isEnabled = true;
                        if (key.keyParser.isInvertable() && masterParts[i].startsWith("!")) {
                            isInverted = true;
                        } else {
                            isInverted = false;
                        }
                    }
                }
            } else {
                String[] childParts = masterParts[i].split(",", 2);
                boolean foundMatch = false;
                for (Key key : validKeys) {
                    if (key.keyParser == null) {
                        continue;
                    }
                    if (key.keyParser.isMatch(childParts[0])) {
                        foundMatch = true;
                        if (key.keyParser.getKeyType() == KeyType.CHAINABLE) {
                            if (!key.keyParser.parseChainable(masterParts[i], parsedChainable, operandvalue)) {
                                JASLog.severe("Failed to Parse Chainable from %s", masterParts[i]);
                            }
                        } else if (key.keyParser.getKeyType() == KeyType.VALUE) {
                            if (!key.keyParser.parseValue(masterParts[i], valueCache)) {
                                JASLog.severe("Failed to Parse Value from %s", masterParts[i]);
                            }
                        }
                        break;
                    }
                }
                if (!foundMatch) {
                    JASLog.severe("Could Not Recognize any valid Spawn properties from %s", masterParts[i]);
                }
            }
        }
    }

    /**
     * 
     * @param world World being evaluated
     * @param entity Entity being processed. May be null where not applicable.
     * @param xCoord X coord location in the world
     * @param yCoord Y coord location in the world
     * @param zCoord Z coord location in the world
     * @return
     */
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord) {
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
            outcome = key.keyParser.isValidLocation(world, entity, xCoord, yCoord, zCoord, typeValuePair, valueCache);
        }
        return outcome;
    }

    /**
     * Checks if the Distance to
     * 
     * @param playerDistance Distance to the playe rin [m^2]
     * @param defaultCutoff Default Range in [m]
     * @return True to Continue as Normal, False to Interrupt
     */
    public boolean isMidDistance(int playerDistance, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.spawnRange);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return playerDistance > defaultCutoff * defaultCutoff;
    }

    protected boolean canBlockSeeTheSky(World world, int xCoord, int yCoord, int zCoord) {
        int blockHeight = world.getTopSolidOrLiquidBlock(xCoord, zCoord);
        return blockHeight <= yCoord;
    }
}
