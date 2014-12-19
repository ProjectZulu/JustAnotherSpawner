package jas.modern.spawner.creature.handler.parsing.keys;

import jas.modern.JASLog;
import jas.modern.spawner.creature.handler.parsing.NBTWriter;
import jas.modern.spawner.creature.handler.parsing.TypeValuePair;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class KeyParserWriteNBT extends KeyParserBase {

    public KeyParserWriteNBT(Key key) {
        super(key, false, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",", 2);
        Operand operand = parseOperand(pieces);

        if (pieces.length > 1) {
            TypeValuePair typeValue = new TypeValuePair(key, pieces[1]);
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
        String nbtOperation = (String) typeValuePair.getValue();
        try {
            NBTTagCompound entityNBT = new NBTTagCompound();
            entity.writeToNBT(entityNBT);
            new NBTWriter(nbtOperation.split(",")).writeToNBT(entityNBT);
            entity.readFromNBT(entityNBT);
            return false;
        } catch (IllegalFormatException e) {
            JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
            return true;
        } catch (IllegalArgumentException e) {
            JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
            return true;
        }
    }
    
	@Override
	public String toExpression(String parseable) {
		ArrayList<TypeValuePair> parsedChainable = new ArrayList<TypeValuePair>();
		ArrayList<Operand> operandvalue = new ArrayList<OptionalSettings.Operand>();
		boolean parsedSuccessfully = parseChainable(parseable, parsedChainable, operandvalue);
		String nbtOperation = (String) parsedChainable.get(0).getValue();

		String[] nbtOperations = nbtOperation.split(",");
		StringBuilder expBuilder = new StringBuilder(5 + nbtOperations.length * 3);
		expBuilder.append("writenbt({");
		for (int i = 0; i < nbtOperations.length; i++) {
			if (i != 0) {
				expBuilder.append(",");
			}
			expBuilder.append("'").append(nbtOperations[i]).append("'");
		}
		expBuilder.append("})");
		return expBuilder.toString();
	}
}
