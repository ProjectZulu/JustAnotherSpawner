package jas.common.spawner.creature.handler.parsing.settings;

import jas.common.spawner.creature.handler.parsing.keys.Key;

import java.util.EnumSet;

public class OptionalSpawnListSpawning extends OptionalSettingsBase {

    public OptionalSpawnListSpawning(String parseableString) {
        super(parseableString, EnumSet.of(Key.spawn));
    }
}
