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
        return isEnabled;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    /**
     * Checks if the Distance to
     * 
     * @param playerDistance Distance to the playe rin [m^2]
     * @param defaultCutoff Default Range in [m]
     * @return True to Continue as Normal, False to Interrupt
     */
    public boolean isMidDistance(int playerDistance, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.spawnRange);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return playerDistance > defaultCutoff * defaultCutoff;
    }
    
    protected boolean canBlockSeeTheSky(World world, int xCoord, int yCoord, int zCoord) {
        int blockHeight = world.getTopSolidOrLiquidBlock(xCoord, zCoord);
        return blockHeight <= yCoord;
    }
}
