package jas.modern.spawner.creature.handler.parsing.keys;

import jas.modern.spawner.creature.handler.parsing.TypeValuePair;

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

	@Override
	public String toExpression(int minRange, int maxRange) {
		return new StringBuilder().append("lgcy.light(").append(minRange).append(",").append(maxRange).append(")")
				.toString();
	}
}