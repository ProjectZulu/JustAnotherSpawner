package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

public class OptionalSettingsPostSpawning extends OptionalSettingsBase {

    public OptionalSettingsPostSpawning(String parseableString) {
        super(parseableString.replace("}", ""), EnumSet.of(Key.postspawn, Key.light, Key.block, Key.blockRange,
                Key.blockFoot, Key.spawnRange, Key.sky, Key.entityCap, Key.minSpawnHeight, Key.maxSpawnHeight,
                Key.liquid, Key.opaque, Key.normal, Key.solidSide, Key.difficulty, Key.torchLight, Key.ground, Key.top,
                Key.fill, Key.modspawn, Key.origin, Key.players, Key.entities, Key.random, Key.writenbt, Key.istamed,
                Key.istameable, Key.location, Key.dimension, Key.biome));
    }
}
