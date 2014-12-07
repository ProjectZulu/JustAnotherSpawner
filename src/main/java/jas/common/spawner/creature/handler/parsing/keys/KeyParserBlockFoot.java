package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.OptionalParser;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import com.google.common.collect.ListMultimap;

public class KeyParserBlockFoot extends KeyParserBase {

    public KeyParserBlockFoot(Key key) {
        super(key, false, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");

        Operand operand = parseOperand(pieces);

        TypeValuePair typeValue = new TypeValuePair(key, OptionalParser.parseBlock(pieces));

        if (typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        }
        return false;
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        @SuppressWarnings("unchecked")
        ListMultimap<String, Integer> iDMetas = (ListMultimap<String, Integer>) typeValuePair.getValue();
        Block blockID = world.getBlock(xCoord, yCoord - 1, zCoord);
        int meta = world.getBlockMetadata(xCoord, yCoord - 1, zCoord);
        boolean foundMatch = false;
        for (String blockKey : iDMetas.keySet()) {
            Block searchBlock = Block.getBlockFromName(blockKey);
            if (searchBlock == null) {
                continue;
            }
            List<Integer> metas = iDMetas.get(blockKey);
            for (Integer metaValue : metas) {
                if (blockID == searchBlock && metaValue.equals(meta)) {
                    foundMatch = true;
                    break;
                }
            }
        }
        return foundMatch ? false : true;
    }

	@Override
	public String toExpression(String parseable) {
		String[] pieces = parseable.split(",");
		ListMultimap<String, Integer> output = OptionalParser.parseBlock(pieces);
		StringBuilder expBuilder = new StringBuilder(12 + output.values().size() * 3);
		Integer xRange = 3;
		Integer yRange = 3;
		Integer zRange = 3;
		Iterator<String> iter = output.keys().iterator();
		while (iter.hasNext()) {
			String blockKey = iter.next();
			expBuilder.append("blockFoot({'").append(blockKey).append("'}");

			List<Integer> metas = output.get(blockKey);
			if (!metas.isEmpty()) {
				expBuilder.append(",{");
				Iterator<Integer> metaIter = metas.iterator();
				while (metaIter.hasNext()) {
					Integer meta = metaIter.next();
					expBuilder.append(meta);
					if (metaIter.hasNext()) {
						expBuilder.append(",");
					}
				}
				expBuilder.append("}");
			}
			expBuilder.append(")");
		}
		return expBuilder.toString();
	}
}