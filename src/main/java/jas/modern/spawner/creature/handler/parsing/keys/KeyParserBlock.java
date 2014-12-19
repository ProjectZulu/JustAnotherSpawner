package jas.modern.spawner.creature.handler.parsing.keys;

import jas.modern.spawner.creature.handler.parsing.OptionalParser;
import jas.modern.spawner.creature.handler.parsing.TypeValuePair;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettingsBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import com.google.common.collect.ListMultimap;

public class KeyParserBlock extends KeyParserBase {

    public KeyParserBlock(Key key) {
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
        return isValidBlock(world, xCoord, yCoord, zCoord, typeValuePair, valueCache);
    }

    /**
     * Represents Restriction on isValidBlock.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    private boolean isValidBlock(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
        @SuppressWarnings("unchecked")
        ListMultimap<String, Integer> iDMetas = (ListMultimap<String, Integer>) typeValuePair.getValue();
        Integer xRange = (Integer) valueCache.get(Key.blockRangeX.key);
        Integer yRange = (Integer) valueCache.get(Key.blockRangeY.key);
        Integer zRange = (Integer) valueCache.get(Key.blockRangeZ.key);

        xRange = xRange == null ? OptionalSettingsBase.defaultBlockRange : xRange;
        yRange = yRange == null ? OptionalSettingsBase.defaultBlockRange : yRange;
        zRange = zRange == null ? OptionalSettingsBase.defaultBlockRange : zRange;

        for (String blockKey : iDMetas.keySet()) {
            Block searchBlock = Block.getBlockFromName(blockKey);
            if (searchBlock == null) {
                continue;
            }
            List<Integer> metas = iDMetas.get(blockKey);
            for (Integer metaValue : metas) {
                for (int i = -xRange; i <= xRange; i++) {
                    for (int k = -zRange; k <= zRange; k++) {
                        for (int j = -yRange; j <= yRange; j++) {
                            Block blockID = world.getBlock(xCoord + i, yCoord + j, zCoord + k);
                            int meta = world.getBlockMetadata(xCoord + i, yCoord + j, zCoord + k);
                            if (blockID == searchBlock && metaValue.equals(meta)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
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
			expBuilder.append("block({'").append(blockKey).append("'},");

			List<Integer> metas = output.get(blockKey);
			if (!metas.isEmpty()) {
				expBuilder.append("{");
				Iterator<Integer> metaIter = metas.iterator();
				while (metaIter.hasNext()) {
					Integer meta = metaIter.next();
					expBuilder.append(meta);
					if (metaIter.hasNext()) {
						expBuilder.append(",");
					}
				}
				expBuilder.append("},");
			}
			expBuilder.append("{3,3,3},").append("{0,0,0})");
		}
		return expBuilder.toString();
	}
}
