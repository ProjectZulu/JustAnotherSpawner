package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

public class OptionalSettingsCreatureTypeSpawn extends OptionalSettingsBase {

    public OptionalSettingsCreatureTypeSpawn(String parseableString) {
        super(parseableString.replace("}", ""), EnumSet.of(Key.spawn, Key.light, Key.block, Key.blockFoot,
                Key.spawnRange, Key.sky, Key.minSpawnHeight, Key.maxSpawnHeight, Key.liquid, Key.opaque, Key.normal,
                Key.solidSide, Key.difficulty, Key.torchLight, Key.ground, Key.top, Key.fill));
    }
}