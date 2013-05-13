package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class KeyParserSolidSide extends KeyParserBase {

    public KeyParserSolidSide(Key key) {
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

        if (pieces.length == 5 || pieces.length == 8) {
            int side = ParsingHelper.parseInteger(pieces[1], 0, "side");
            int rangeX = ParsingHelper.parseInteger(pieces[2], 0, "normalBlockRangeX");
            int rangeY = ParsingHelper.parseInteger(pieces[3], 0, "normalBlockRangeY");
            int rangeZ = ParsingHelper.parseInteger(pieces[4], 0, "normalBlockRangeZ");
            if (pieces.length == 7) {
                int offsetX = ParsingHelper.parseInteger(pieces[5], 0, "normalOffsetX");
                int offsetY = ParsingHelper.parseInteger(pieces[6], 0, "normalOffsetY");
                int offsetZ = ParsingHelper.parseInteger(pieces[7], 0, "normalOffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, side, rangeX, rangeY, rangeZ, offsetX,
                        offsetY, offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, side, rangeX, rangeY, rangeZ });
            }
        } else if (pieces.length == 3 || pieces.length == 6) {
            int side = ParsingHelper.parseInteger(pieces[1], 0, "side");
            int range = ParsingHelper.parseInteger(pieces[2], 0, "normalBlockRange");
            if (pieces.length == 6) {
                int offsetX = ParsingHelper.parseInteger(pieces[3], 0, "normalOffsetX");
                int offsetY = ParsingHelper.parseInteger(pieces[4], 0, "normalOffsetY");
                int offsetZ = ParsingHelper.parseInteger(pieces[5], 0, "normalOffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, side, range, offsetX, offsetY, offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, side, range });
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
        int side = (Integer) values[1];
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        int rangeX = 0;
        int rangeY = 0;
        int rangeZ = 0;

        if (values.length == 5 || values.length == 8) {
            rangeX = (Integer) values[2];
            rangeY = (Integer) values[3];
            rangeZ = (Integer) values[4];
            if (values.length == 8) {
                offsetX = (Integer) values[5];
                offsetY = (Integer) values[6];
                offsetZ = (Integer) values[7];
            }
        } else if (values.length == 3 || values.length == 6) {
            rangeX = (Integer) values[2];
            rangeY = rangeX;
            rangeZ = rangeX;
            if (values.length == 6) {
                offsetX = (Integer) values[3];
                offsetY = (Integer) values[4];
                offsetZ = (Integer) values[5];
            }
        }

        for (int i = -rangeX; i <= rangeX; i++) {
            for (int k = -rangeZ; k <= rangeZ; k++) {
                for (int j = -rangeY; j <= rangeY; j++) {
                    boolean isSolid = world.isBlockSolidOnSide(xCoord + offsetX + i, yCoord + offsetY + j, zCoord
                            + offsetZ + k, ForgeDirection.getOrientation(side));
                    if (!isInverted && isSolid || isInverted && !isSolid) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}