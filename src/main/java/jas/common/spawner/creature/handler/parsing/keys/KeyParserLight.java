package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.TypeValuePair;

import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserLight extends KeyParserRange {

    public KeyParserLight(Key key) {
        super(key);
    }

    @Override
    int getCurrent(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
        return world.getBlockLightValue(xCoord, yCoord, zCoord);
    }
}