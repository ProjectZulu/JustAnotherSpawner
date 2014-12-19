package jas.modern.spawner.creature.handler.parsing.keys;

import jas.modern.JASLog;
import jas.modern.spawner.creature.handler.parsing.ParsingHelper;
import jas.modern.spawner.creature.handler.parsing.TypeValuePair;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserLocation extends KeyParserBase {

    public KeyParserLocation(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);

        if (pieces.length == 7) {
            int targetX = ParsingHelper.parseFilteredInteger(pieces[1], 0, "targetX " + key.key);
            int targetY = ParsingHelper.parseFilteredInteger(pieces[2], 0, "targetY " + key.key);
            int targetZ = ParsingHelper.parseFilteredInteger(pieces[3], 0, "targetZ " + key.key);

            int varX = ParsingHelper.parseFilteredInteger(pieces[4], 1, "varX " + key.key);
            int varY = ParsingHelper.parseFilteredInteger(pieces[5], 1, "varY " + key.key);
            int varZ = ParsingHelper.parseFilteredInteger(pieces[6], 1, "varZ " + key.key);
            TypeValuePair typeValue = new TypeValuePair(key, new Object[] { isInverted(pieces[0]), targetX, targetY,
                    targetZ, varX, varY, varZ });
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        } else {
            JASLog.log().severe("Error Parsing %s Parameter. Invalid Argument Length.", key.key);
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
		int targetX = (Integer) values[1];
		int targetY = (Integer) values[2];
		int targetZ = (Integer) values[3];

		int varX = (Integer) values[4];
		int varY = (Integer) values[5];
		int varZ = (Integer) values[6];

		boolean isValid = false;
		if (isWithinTargetRange(xCoord, targetX, varX) && isWithinTargetRange(yCoord, targetY, varY)
				&& isWithinTargetRange(zCoord, targetZ, varZ)) {
			isValid = true;
		}
		return isInverted ? isValid : !isValid;
	}

    private boolean isWithinTargetRange(int current, int target, int targetVariance) {
        int maxRange = target + targetVariance;
        int minRange = target - targetVariance;

        boolean isValid = !(current <= maxRange && current >= minRange);
        if (minRange <= maxRange) {
            isValid = (current <= maxRange && current >= minRange);
        } else {
            isValid = !(current < minRange && current > maxRange);
        }
        return isValid;
	}

	@Override
	public String toExpression(String parseable) {
		ArrayList<TypeValuePair> parsedChainable = new ArrayList<TypeValuePair>();
		ArrayList<Operand> operandvalue = new ArrayList<OptionalSettings.Operand>();
		boolean parsedSuccessfully = parseChainable(parseable, parsedChainable, operandvalue);
		Object[] values = (Object[]) parsedChainable.get(0).getValue();

		int targetX = (Integer) values[1];
		int targetY = (Integer) values[2];
		int targetZ = (Integer) values[3];

		int varX = (Integer) values[4];
		int varY = (Integer) values[5];
		int varZ = (Integer) values[6];

		StringBuilder expBuilder = new StringBuilder(15);
		expBuilder.append("lgcy.location(");
		expBuilder.append("{").append(targetX).append(",").append(targetY).append(",").append(targetZ).append("}");
		expBuilder.append(",{").append(varX).append(",").append(varY).append(",").append(varZ).append("}");
		expBuilder.append(")");
		return expBuilder.toString();
	}
}