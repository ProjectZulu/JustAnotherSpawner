package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.OptionalParser;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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

        Operand operand = getOperand(pieces);

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
    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord, TypeValuePair typeValuePair,
            HashMap<String, Object> valueCache) {
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
        ListMultimap<Integer, Integer> iDMetas = (ListMultimap<Integer, Integer>) typeValuePair.getValue();
        Integer xRange = (Integer) valueCache.get(Key.blockRangeX.key);
        Integer yRange = (Integer) valueCache.get(Key.blockRangeY.key);
        Integer zRange = (Integer) valueCache.get(Key.blockRangeZ.key);

        xRange = xRange == null ? OptionalSettingsBase.defaultBlockRange : xRange;
        yRange = yRange == null ? OptionalSettingsBase.defaultBlockRange : yRange;
        zRange = zRange == null ? OptionalSettingsBase.defaultBlockRange : zRange;

        for (int i = -xRange; i <= xRange; i++) {
            for (int k = -zRange; k <= zRange; k++) {
                for (int j = -yRange; j <= yRange; j++) {
                    for (Entry<Integer, Integer> iDMetaEntry : iDMetas.entries()) {
                        int blockID = world.getBlockId(xCoord + i, yCoord + j, zCoord + k);
                        int meta = world.getBlockMetadata(xCoord + i, yCoord + j, zCoord + k);
                        if (blockID == iDMetaEntry.getKey() && meta == iDMetaEntry.getValue()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
