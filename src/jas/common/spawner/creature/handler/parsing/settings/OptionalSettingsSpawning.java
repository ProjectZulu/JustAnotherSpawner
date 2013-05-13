package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsSpawning extends OptionalSettingsBase {

    public OptionalSettingsSpawning(String parseableString) {
        super(parseableString.replace("}", ""), EnumSet.of(Key.spawn, Key.light, Key.block, Key.blockFoot,
                Key.spawnRange, Key.sky, Key.entityCap, Key.minSpawnHeight, Key.maxSpawnHeight, Key.liquid, Key.opaque,
                Key.normal, Key.solidSide, Key.difficulty));
    }

    public Integer getEntityCap() {
        return (Integer) valueCache.get(Key.entityCap.key);
    }
}
