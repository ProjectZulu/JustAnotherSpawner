package jas.spawner.modern.spawner.creature.handler.parsing.keys;

import jas.spawner.modern.spawner.creature.handler.parsing.TypeValuePair;

import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class KeyParserTorchLight extends KeyParserRange {

    public KeyParserTorchLight(Key key) {
        super(key);
    }

    @Override
    int getCurrent(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
        return world.getSavedLightValue(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
    }

	@Override
	public String toExpression(int minRange, int maxRange) {
		return new StringBuilder().append("lgcy.torchlight(").append(minRange).append(",").append(maxRange).append(")")
				.toString();
	}
}