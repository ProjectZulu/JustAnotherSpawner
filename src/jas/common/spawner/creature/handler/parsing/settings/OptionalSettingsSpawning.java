package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

/**
 * For style see {@link OptionalSettings}
 */
public class OptionalSettingsSpawning extends OptionalSettingsBase {

    public OptionalSettingsSpawning(String parseableString) {
        super(parseableString.replace("}", ""), EnumSet.of(Key.spawn, Key.light, Key.block, Key.blockRange,
                Key.blockFoot, Key.spawnRange, Key.sky, Key.entityCap, Key.minSpawnHeight, Key.maxSpawnHeight,
                Key.liquid, Key.opaque, Key.normal, Key.solidSide, Key.difficulty, Key.torchLight, Key.ground, Key.top,
                Key.fill, Key.modspawn, Key.origin, Key.players, Key.entities, Key.random, Key.writenbt));
    }

    public Integer getEntityCap() {
        return (Integer) valueCache.get(Key.entityCap.key);
    }
}
