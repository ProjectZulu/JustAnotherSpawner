package jas.modern.spawner.creature.handler.parsing.keys;

import jas.api.ITameable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.world.World;

public class KeyParserIsTamed extends KeyParserBoolean {

    public KeyParserIsTamed(Key key) {
        super(key);
    }

    @Override
    public boolean getValue(EntityLiving entity, World world, int xCoord, int yCoord, int zCoord) {
        if (entity instanceof ITameable) {
            return ((ITameable) entity).isTamed();
        } else if (entity instanceof EntityTameable) {
            return ((EntityTameable) entity).isTamed();
        }
        return false;
    }

	@Override
	public String toExpression(String parseable) {
		return "isTamed()";
	}
}