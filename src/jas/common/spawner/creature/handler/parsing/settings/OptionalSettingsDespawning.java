package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsDespawning extends OptionalSettingsBase {
    public OptionalSettingsDespawning(String parseableString) {
        super(parseableString.replace("}", ""), EnumSet.of(Key.despawn, Key.light, Key.block, Key.blockFoot,
                Key.blockRange, Key.spawnRange, Key.spawnRate, Key.sky, Key.despawnAge, Key.maxSpawnRange,
                Key.minSpawnHeight, Key.maxSpawnHeight, Key.liquid, Key.opaque, Key.normal, Key.solidSide,
                Key.difficulty, Key.torchLight, Key.ground, Key.top, Key.fill, Key.origin, Key.players, Key.entities,
                Key.random));
    }

    public int getRate() {
        parseString();
        Integer rate = (Integer) valueCache.get(Key.spawnRate.key);
        return rate != null ? rate : 40;
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
        Integer tempCutoff = (Integer) valueCache.get(Key.maxSpawnRange.key);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return playerDistance > defaultCutoff * defaultCutoff;
    }
}
