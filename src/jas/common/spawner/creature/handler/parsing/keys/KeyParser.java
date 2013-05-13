package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.World;

public abstract class KeyParser {

    public enum KeyType {
        CHAINABLE, VALUE, PARENT, NONE;
    }

    public abstract boolean isInvertable();

    public abstract boolean isMatch(String title);

    public abstract KeyType getKeyType();

    public abstract boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue);

    public abstract boolean parseValue(String parseable, HashMap<String, Object> valueCache);

    public abstract boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache);
}
