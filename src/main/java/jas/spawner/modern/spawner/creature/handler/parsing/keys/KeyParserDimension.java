package jas.spawner.modern.spawner.creature.handler.parsing.keys;

import jas.common.helper.VanillaHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.ParsingHelper;
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
        return VanillaHelper.getDimensionID(world) == args[0];
    }
    

	@Override
	public String toExpression(String parseable) {
		String[] pieces = parseable.split(",");
		int argAmount = argAmount();
		if (pieces.length == 1 + argAmount) {
			int[] args = new int[argAmount];
			for (int i = 0; i < argAmount; i++) {
				args[i] = ParsingHelper.parseFilteredInteger(pieces[i + 1], 0, i + " " + key.key);
			}
			return new StringBuilder().append("lgcy.dimension(").append(args[0]).append(")").toString();
		}
		return "";
	}
}