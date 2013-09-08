package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public abstract class KeyParserIntArray extends KeyParserBase {

    public KeyParserIntArray(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");

        Operand operand = parseOperand(pieces);
        int argAmount = argAmount();
        if (pieces.length == 1 + argAmount) {
            int[] args = new int[argAmount];
            for (int i = 0; i < argAmount; i++) {
                args[i] = ParsingHelper.parseFilteredInteger(pieces[i + 1], 0, i + " " + key.key);
            }
            TypeValuePair typeValue = new TypeValuePair(key, new Object[] { isInverted(pieces[0]), args });
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        } else {
            JASLog.severe("Error Parsing %s Parameter. Invalid Argument Length.", key.key);
            return false;
        }
    }

    public abstract int argAmount();

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        Object[] values = (Object[]) typeValuePair.getValue();
        boolean isInverted = (Boolean) values[0];
        int[] args = (int[]) values[1];

        boolean isValid = isValid(world, entity, xCoord, yCoord, zCoord, args);
        return isInverted ? isValid : !isValid;
    }

    public abstract boolean isValid(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord, int[] args);
}
