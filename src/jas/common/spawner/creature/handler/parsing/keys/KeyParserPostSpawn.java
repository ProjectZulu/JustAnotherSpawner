package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserPostSpawn extends KeyParserBase {

    public KeyParserPostSpawn(Key key) {
        super(key, true, KeyType.PARENT);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }
}