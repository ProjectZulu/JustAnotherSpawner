package jas.common.spawner.creature.handler;

import net.minecraft.world.World;

/**
 * For style see {@link OptionalSettings}
 */
public abstract class OptionalSettingsBase extends OptionalSettings {

    public OptionalSettingsBase(String parseableString) {
        super(parseableString);
    }

    @Override
    public boolean isOptionalEnabled() {
        parseString();
        return valueCache.get(Key.enabled.key) != null;
    }

    @Override
    public boolean isInverted() {
        parseString();
        return !(Boolean) valueCache.get(Key.enabled.key);
    }

    /**
     * Represents Restriction on LightLevel.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    public boolean isValidLightLevel(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        int lightLevel = world.getBlockLightValue(xCoord, yCoord, zCoord);
        return lightLevel > (Integer) valueCache.get(Key.maxLightLevel.key)
                || lightLevel < (Integer) valueCache.get(Key.minLightLevel.key);
    }

    public boolean isValidSky(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        if (valueCache.get(Key.sky.key) == null) {
            return true;
        } else if ((Boolean) valueCache.get(Key.sky.key)) {
            return !world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
        } else {
            return world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
        }
    }

    /**
     * Checks if the Distance to
     * 
     * @param playerDistance Distance Squared to Nearest Player
     * @return True to Continue as Normal, False to Interrupt, Null Use Global Check
     */
    public Boolean isValidDistance(int playerDistance) {
        parseString();
        Integer distanceToPlayer = (Integer) valueCache.get(Key.spawnRange);
        return distanceToPlayer != null ? playerDistance > distanceToPlayer * distanceToPlayer : null;
    }
}
