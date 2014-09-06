package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserMaxHeight extends KeyParserIntArray {

	public KeyParserMaxHeight(Key key) {
		super(key);
	}

	@Override
	public int argAmount() {
		return 1;
	}

	@Override
	public boolean isValid(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord, int[] args) {
		return yCoord > args[0];
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
			return new StringBuilder().append("lgcy.height(").append("args[0]").append(",").append("256").append(")")
					.toString();
		}
		return "";
	}
}