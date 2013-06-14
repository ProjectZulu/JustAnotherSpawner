package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserRandom extends KeyParserBase {

    public KeyParserRandom(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);

        if (pieces.length == 4) {
            int randInt = ParsingHelper.parseFilteredInteger(pieces[1], 16, "RandomRange " + key.key);
            int randOffset = ParsingHelper.parseFilteredInteger(pieces[2], 16, "RandomOffset " + key.key);
            int maximum = ParsingHelper.parseFilteredInteger(pieces[3], -1, "Maximum " + key.key);
            TypeValuePair typeValue = new TypeValuePair(key, new Object[] { isInverted(pieces[0]), randInt, randOffset,
                    maximum });
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        } else {
            JASLog.severe("Error Parsing %s Parameter. Invalid Argument Length.", key.key);
            return false;
        }
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        Object[] values = (Object[]) typeValuePair.getValue();
        boolean isInverted = (Boolean) values[0];

        int randInt = (Integer) values[1];
        int randOffset = (Integer) values[2];
        int maximum = (Integer) values[3];

        boolean isValid = !(world.rand.nextInt(randInt) + randOffset <= maximum);
        return isInverted ? isValid : !isValid;
    }
}
