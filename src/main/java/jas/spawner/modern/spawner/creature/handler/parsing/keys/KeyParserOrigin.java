package jas.spawner.modern.spawner.creature.handler.parsing.keys;

import jas.spawner.modern.spawner.creature.handler.parsing.TypeValuePair;

import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserOrigin extends KeyParserRange {

    public KeyParserOrigin(Key key) {
        super(key);
    }

    @Override
    int getCurrent(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
        return (int) Math.sqrt(world.getSpawnPoint().getDistanceSquared(xCoord, yCoord, zCoord));
    }
    
	@Override
	public String toExpression(int minRange, int maxRange) {
		return new StringBuilder().append("lgcy.origin(").append(minRange).append(",").append(maxRange).append(")")
				.toString();
	}

}