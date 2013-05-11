package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsSpawning extends OptionalSettingsBase {

    public OptionalSettingsSpawning(String parseableString) {
        super(parseableString.replace("}", ""));
        validKeys = EnumSet.of(Key.spawn, Key.light, Key.block, Key.blockFoot, Key.spawnRange, Key.sky, Key.entityCap,
                Key.minSpawnHeight, Key.maxSpawnHeight);
        parseString();
    }

    public Integer getEntityCap() {
        return (Integer) valueCache.get(Key.entityCap.key);
    }
}
