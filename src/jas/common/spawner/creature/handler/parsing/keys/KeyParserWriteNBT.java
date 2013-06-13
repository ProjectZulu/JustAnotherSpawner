package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.NBTWriter;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

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
        String nbtOperation = (String) typeValuePair.getValue();
        JASLog.info("XXX: %s", nbtOperation);
        try {
            NBTTagCompound entityNBT = new NBTTagCompound();
            entity.writeToNBT(entityNBT);
            new NBTWriter(nbtOperation).writeToNBT(entityNBT);
            entity.readFromNBT(entityNBT);
            return false;
        } catch (IllegalFormatException e) {
            JASLog.severe("Skipping NBT Write due to %s", e.getMessage());
            return true;
        } catch (IllegalArgumentException e) {
            JASLog.severe("Skipping NBT Write due to %s", e.getMessage());
            return true;
        }
    }
}
