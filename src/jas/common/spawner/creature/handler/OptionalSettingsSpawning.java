package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.util.Map.Entry;

import net.minecraft.world.World;

import com.google.common.collect.ListMultimap;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsSpawning extends OptionalSettingsBase {

    public OptionalSettingsSpawning(String parseableString) {
        super(parseableString.replace("}", ""));
    }

    @Override
    protected final void parseString() {
        if (stringParsed || parseableString.equals("")) {
            return;
        }
        stringParsed = true;
        String[] masterParts = parseableString.split(":");
        for (int i = 0; i < masterParts.length; i++) {
            if (i == 0) {
                if (masterParts[i].equalsIgnoreCase(Key.spawn.key)) {
                    isEnabled = true;
                    isInverted = false;
                } else if (masterParts[i].equalsIgnoreCase(Key.notSpawn.key)) {
                    isEnabled = true;
                    isInverted = true;
                } else {
                    JASLog.severe("Optional Settings Error expected spawn from %s", masterParts[i]);
                    break;
                }
            } else {
                String[] childParts = masterParts[i].split(",");
                Operand operand = Operand.OR;
                if (childParts[0].startsWith("&")) {
                    operand = Operand.AND;
                    childParts[0] = childParts[0].substring(1);
                } else if (childParts[0].startsWith("|")) {
                    childParts[0] = childParts[0].substring(1);
                }

                Key key = Key.getKeybyString(childParts[0]);
                switch (key) {
                case light:
                    addParsedChainable(new TypeValuePair(Key.light, OptionalParser.parseLight(childParts)), operand);
                    break;
                case block:
                    addParsedChainable(new TypeValuePair(Key.block, OptionalParser.parseBlock(childParts)), operand);
                    break;
                case spawnRange:
                    OptionalParser.parseSpawnRange(childParts, valueCache);
                    break;
                case sky:
                case noSky:
                    addParsedChainable(new TypeValuePair(Key.sky, OptionalParser.parseSky(childParts)), operand);
                    break;
                case material:
                    // TODO: Add Material Tag ? Air or Water? What is the point?
                    JASLog.info("Material Tag is not implemented yet. Have some %s", Math.PI);
                    break;
                case entityCap:
                    OptionalParser.parseEntityCap(childParts, valueCache);
                    break;
                case minSpawnHeight:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseMinSpawnHeight(childParts)), operand);
                    break;
                case maxSpawnHeight:
                    addParsedChainable(new TypeValuePair(key, OptionalParser.parseMaxSpawnHeight(childParts)), operand);
                    break;
                default:
                    JASLog.severe("Could Not Recognize any valid Spawn properties from %s", masterParts[i]);
                    break;
                }
            }
        }
    }

    public Integer getEntityCap() {
        return (Integer) valueCache.get(Key.entityCap.key);
    }

    public boolean isValidLocation(World world, int xCoord, int yCoord, int zCoord) {
        boolean outcome = true;
        for (int i = 0; i < parsedChainable.size(); i++) {
            TypeValuePair typeValuePair = parsedChainable.get(i);
            if (i != 0) {
                if (operandvalue.get(i) == Operand.AND && outcome == true) {
                    continue;
                } else if (operandvalue.get(i) == Operand.OR && outcome == false) {
                    return false;
                }
            }

            Key key = typeValuePair.getType();
            switch (key) {
            case light:
                int[] lightLevels = (int[]) typeValuePair.getValue();
                int lightLevel = world.getBlockLightValue(xCoord, yCoord, zCoord);
                outcome = lightLevel > lightLevels[1] || lightLevel < lightLevels[0];
                break;
            case block:
                @SuppressWarnings("unchecked")
                ListMultimap<Integer, Integer> iDMetas = (ListMultimap<Integer, Integer>) typeValuePair.getValue();
                int blockID = world.getBlockId(xCoord, yCoord - 1, zCoord);
                int meta = world.getBlockMetadata(xCoord, yCoord - 1, zCoord);
                boolean foundMatch = false;
                for (Entry<Integer, Integer> iDMetaEntry : iDMetas.entries()) {
                    if (blockID == iDMetaEntry.getKey() && meta == iDMetaEntry.getValue()) {
                        foundMatch = true;
                        break;
                    }
                }
                outcome = foundMatch ? false : true;
                break;
            case sky:
                boolean sky = (Boolean) typeValuePair.getValue();
                boolean canSeeSky = canBlockSeeTheSky(world, xCoord, yCoord, zCoord);
                outcome = sky ? !canSeeSky : canSeeSky;
                break;
            case minSpawnHeight:
                Integer minSpawnHeight = (Integer) typeValuePair.getValue();
                outcome = yCoord < minSpawnHeight ? true : false;
                break;
            case maxSpawnHeight:
                Integer maxSpawnHeight = (Integer) typeValuePair.getValue();
                outcome = yCoord > maxSpawnHeight ? true : false;
                break;
            default:
                break;
            }
        }
        return outcome;
    }
}
