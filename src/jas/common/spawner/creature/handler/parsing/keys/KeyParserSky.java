package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.OptionalParser;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.World;

public class KeyParserSky extends KeyParserBase {

    public KeyParserSky(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String[] parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        Operand operand = getOperand(parseable);
        TypeValuePair typeValue = new TypeValuePair(key, OptionalParser.parseSky(parseable));

        if (typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        }
        return false;
    }

    @Override
    public boolean parseValue(String[] parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
        boolean sky = (Boolean) typeValuePair.getValue();
        boolean canSeeSky = canBlockSeeTheSky(world, xCoord, yCoord, zCoord);
        return sky ? !canSeeSky : canSeeSky;
    }

    protected boolean canBlockSeeTheSky(World world, int xCoord, int yCoord, int zCoord) {
        int blockHeight = world.getTopSolidOrLiquidBlock(xCoord, zCoord);
        return blockHeight <= yCoord;
    }
}
