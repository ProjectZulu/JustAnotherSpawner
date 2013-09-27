package jas.common.spawner.creature.handler.parsing.keys;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserDimension extends KeyParserIntArray {

    public KeyParserDimension(Key key) {
        super(key);
    }

    @Override
    public int argAmount() {
        return 1;
    }

    @Override
    public boolean isValid(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord, int[] args) {
        return world.provider.dimensionId == args[0];
    }
}