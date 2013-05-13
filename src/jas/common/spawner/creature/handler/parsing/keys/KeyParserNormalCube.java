package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.World;

public class KeyParserNormalCube extends KeyParserBase {

    public KeyParserNormalCube(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");
        Operand operand = getOperand(pieces);

        boolean isInverted = false;
        if (isInverted(parseable)) {
            isInverted = true;
        }

        TypeValuePair typeValue = null;

        if (pieces.length == 4 || pieces.length == 7) {
            int rangeX = ParsingHelper.parseInteger(pieces[1], 0, "normalBlockRangeX");
            int rangeY = ParsingHelper.parseInteger(pieces[2], 0, "normalBlockRangeY");
            int rangeZ = ParsingHelper.parseInteger(pieces[3], 0, "normalBlockRangeZ");
            if (pieces.length == 7) {
                int offsetX = ParsingHelper.parseInteger(pieces[4], 0, "normalOffsetX");
                int offsetY = ParsingHelper.parseInteger(pieces[5], 0, "normalOffsetY");
                int offsetZ = ParsingHelper.parseInteger(pieces[6], 0, "normalOffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, rangeX, rangeY, rangeZ, offsetX, offsetY,
                        offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, rangeX, rangeY, rangeZ });
            }
        } else if (pieces.length == 2 || pieces.length == 5) {
            int range = ParsingHelper.parseInteger(pieces[1], 0, "normalBlockRange");
            if (pieces.length == 5) {
                int offsetX = ParsingHelper.parseInteger(pieces[2], 0, "normalOffsetX");
                int offsetY = ParsingHelper.parseInteger(pieces[3], 0, "normalOffsetY");
                int offsetZ = ParsingHelper.parseInteger(pieces[4], 0, "normalOffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, range, offsetX, offsetY, offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, range });
            }
        } else {
            JASLog.severe("Error Parsing %s Block Parameter. Invalid Argument Length of %s.", key.key, pieces.length);
        }

        if (typeValue != null && typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        }
        return false;
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {

        Object[] values = (Object[]) typeValuePair.getValue();
        boolean isInverted = (Boolean) values[0];
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        int rangeX = 0;
        int rangeY = 0;
        int rangeZ = 0;

        if (values.length == 4 || values.length == 7) {
            rangeX = (Integer) values[1];
            rangeY = (Integer) values[2];
            rangeZ = (Integer) values[3];
            if (values.length == 7) {
                offsetX = (Integer) values[4];
                offsetY = (Integer) values[5];
                offsetZ = (Integer) values[6];
            }
        } else if (values.length == 2 || values.length == 5) {
            rangeX = (Integer) values[1];
            rangeY = rangeX;
            rangeZ = rangeX;
            if (values.length == 5) {
                offsetX = (Integer) values[2];
                offsetY = (Integer) values[3];
                offsetZ = (Integer) values[4];
            }
        }

        for (int i = -rangeX; i <= rangeX; i++) {
            for (int k = -rangeZ; k <= rangeZ; k++) {
                for (int j = rangeY; j <= rangeY; j++) {
                    boolean isNormal = world.isBlockNormalCube(xCoord + offsetX + i, yCoord + offsetY + j, zCoord
                            + offsetZ + k);
                    if (!isInverted && isNormal || isInverted && !isNormal) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}