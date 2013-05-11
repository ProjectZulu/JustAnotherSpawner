package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsDespawning extends OptionalSettingsBase {
    public OptionalSettingsDespawning(String parseableString) {
        super(parseableString.replace("}", ""));
        validKeys = EnumSet.of(Key.despawn, Key.light, Key.block, Key.blockFoot, Key.blockRange, Key.spawnRange,
                Key.spawnRate, Key.sky, Key.despawnAge, Key.maxSpawnRange, Key.minSpawnHeight, Key.maxSpawnHeight);
        parseString();
    }

    public int getRate() {
        parseString();
        return (Integer) valueCache.get(Key.spawnRate.key);
    }

    public boolean isValidAge(int currentAge, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.despawnAge);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return currentAge > defaultCutoff;
    }

    /**
     * 
     * @param playerDistance Distance to the playe rin [m^2]
     * @param defaultCutoff Default Range in [m]
     * @return
     */
    public boolean isMaxDistance(int playerDistance, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.maxSpawnRange);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return playerDistance > defaultCutoff * defaultCutoff;
    }
}
