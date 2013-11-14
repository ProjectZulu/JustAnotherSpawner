package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.creature.handler.parsing.ParsingHelper;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.ImmutableMultimap;

public class KeyParserBiome extends KeyParserBase {

    private enum BiomeType {
        MAPPING, ATTRIBUTE, GROUP;
    }

    public KeyParserBiome(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {

        boolean isInverted = false;
        if (isInverted(parseable)) {
            isInverted = true;
        }

        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);
        TypeValuePair typeValue = null;

        final int lengthOnlyName = 2;
        final int lengthWithSearch = 3;
        final int lengthFullForm = 4;
        if (pieces.length == lengthOnlyName || pieces.length == lengthWithSearch || pieces.length == lengthFullForm) {
            int rangeX, rangeZ;
            rangeX = rangeZ = 0;
            if (pieces.length == lengthWithSearch || pieces.length == lengthFullForm) {
                String[] rangePieces = pieces[1].split("/");
                if (rangePieces.length == 3) {
                    rangeX = ParsingHelper.parseFilteredInteger(rangePieces[0], 0, key.key + "SearchRangeX");
                    rangeZ = ParsingHelper.parseFilteredInteger(rangePieces[2], 0, key.key + "SearchRangeZ");
                } else if (rangePieces.length == 2) {
                    rangeX = ParsingHelper.parseFilteredInteger(rangePieces[0], 0, key.key + "SearchRangeX");
                    rangeZ = ParsingHelper.parseFilteredInteger(rangePieces[1], 0, key.key + "SearchRangeZ");
                } else if (rangePieces.length == 1) {
                    rangeX = ParsingHelper.parseFilteredInteger(rangePieces[0], 0, key.key + "SearchRangeIso");
                    rangeZ = rangeX;
                }
            }
            String biomeName;
            if (pieces.length == lengthFullForm) {
                biomeName = pieces[3];
            } else if (pieces.length == lengthWithSearch) {
                biomeName = pieces[2];
            } else {
                biomeName = pieces[1];
            }

            if (biomeName.length() > 2 && biomeName.charAt(2) == '|' && !biomeName.startsWith("A|")
                    && !biomeName.startsWith("G|")) {
                JASLog.severe("Error Parsing %s. | operator detected. Only valid prefixed are A| or G|.", key.key);
                return false;
            }
            if (pieces.length == lengthFullForm) {
                String[] offsetPieces = pieces[2].split("/");
                int offsetX = 0;
                int offsetZ = 0;
                if (offsetPieces.length == 3) {
                    offsetX = ParsingHelper.parseFilteredInteger(offsetPieces[0], 0, key.key + "OffsetX");
                    offsetZ = ParsingHelper.parseFilteredInteger(offsetPieces[2], 0, key.key + "OffsetZ");
                } else if (offsetPieces.length == 2) {
                    offsetX = ParsingHelper.parseFilteredInteger(offsetPieces[0], 0, key.key + "OffsetX");
                    offsetZ = ParsingHelper.parseFilteredInteger(offsetPieces[1], 0, key.key + "OffsetZ");
                } else {
                    JASLog.severe("Error Parsing Range of %s. Invalid Offset Argument Length of %s.", key.key,
                            offsetPieces.length);
                }
                typeValue = new TypeValuePair(key, new Object[] { isInverted, rangeX, rangeZ, offsetX, offsetZ,
                        biomeName });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, rangeX, rangeZ, biomeName });
            }
        } else {
            JASLog.severe("Error Parsing %s Block Parameter. Invalid Argument Length of %s.", key.key, pieces.length);
            return false;
        }

        if (typeValue != null && typeValue.getValue() != null) {
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

        Object[] values = (Object[]) typeValuePair.getValue();
        boolean isInverted = (Boolean) values[0];

        final int shortFormLength = 4;
        final int formWithOffsetLength = 6;
        if (values.length == shortFormLength || values.length == formWithOffsetLength) {
            int rangeX = (Integer) values[1];
            int rangeZ = (Integer) values[2];
            int offsetX, offsetZ;
            offsetX = offsetZ = 0;
            if (values.length == formWithOffsetLength) {
                offsetX = (Integer) values[3];
                offsetZ = (Integer) values[4];
            }
            String biomeName;
            if (values.length == formWithOffsetLength) {
                biomeName = (String) values[5];
            } else {
                biomeName = (String) values[3];
            }
            BiomeType type = BiomeType.MAPPING;
            if (biomeName.startsWith("A|")) {
                type = BiomeType.ATTRIBUTE;
                biomeName = biomeName.substring(2);
            } else if (biomeName.startsWith("G|")) {
                type = BiomeType.GROUP;
                biomeName = biomeName.substring(2);
            }

            BiomeGroupRegistry registry = JustAnotherSpawner.worldSettings().biomeGroupRegistry();
            ImmutableMultimap<String, String> packgToBiomeGroupID = ImmutableMultimap.of();
            if (type == BiomeType.GROUP) {
                // Cache Group as our only current public access is a copy method. This is already changed in DEV14
                // builds so is a temporary evil TODO
                packgToBiomeGroupID = registry.getPackgNameToGroupIDList();
            }

            for (int i = -rangeX; i <= rangeX; i++) {
                for (int k = -rangeZ; k <= rangeZ; k++) {
                    BiomeGenBase biome = world.getBiomeGenForCoords(xCoord + offsetX + i, zCoord + offsetZ + k);
                    boolean isBiome = false;
                    switch (type) {
                    case MAPPING: {
                        isBiome = biomeName.equals(registry.biomePckgToMapping.get(BiomeHelper.getPackageName(biome)));
                        break;
                    }
                    case ATTRIBUTE: {
                        List<String> attributeIDs = registry.packgNameToAttributeIDList.get(BiomeHelper
                                .getPackageName(biome));
                        if (attributeIDs.contains(biomeName)) {
                            isBiome = true;
                        }
                        break;
                    }
                    case GROUP: {
                        if (packgToBiomeGroupID.get(BiomeHelper.getPackageName(biome)).contains(biomeName)) {
                            isBiome = true;
                        }
                        break;
                    }
                    }

                    if (!isInverted && isBiome || isInverted && !isBiome) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}