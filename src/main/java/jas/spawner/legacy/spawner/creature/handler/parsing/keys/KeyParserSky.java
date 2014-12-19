package jas.spawner.legacy.spawner.creature.handler.parsing.keys;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserSky extends KeyParserBoolean {

    public KeyParserSky(Key key) {
        super(key);
    }

    @Override
    public boolean getValue(EntityLiving entity, World world, int xCoord, int yCoord, int zCoord) {
        return world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
    }
}