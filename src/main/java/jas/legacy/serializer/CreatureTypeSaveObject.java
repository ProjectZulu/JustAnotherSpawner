package jas.legacy.serializer;

import jas.legacy.GsonHelper;
import jas.legacy.spawner.creature.type.CreatureType;
import jas.legacy.spawner.creature.type.CreatureTypeBuilder;
import jas.legacy.spawner.creature.type.CreatureTypeRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CreatureTypeSaveObject {
    private Optional<TreeMap<String, CreatureTypeBuilder>> types;

    private CreatureTypeSaveObject() {
        types = Optional.absent();
    }

    public CreatureTypeSaveObject(CreatureTypeRegistry registry) {
        TreeMap<String, CreatureTypeBuilder> types = new TreeMap<String, CreatureTypeBuilder>();
        java.util.Iterator<CreatureType> iterator = registry.getCreatureTypes();
        while (iterator.hasNext()) {
            CreatureType type = iterator.next();
            types.put(type.typeID, new CreatureTypeBuilder(type));
        }
        this.types = Optional.of(types);
    }

    public Optional<Collection<CreatureTypeBuilder>> getTypes() {
        return types.isPresent() ? Optional.of(types.get().values()) : Optional
                .<Collection<CreatureTypeBuilder>> absent();
    }

    public static class CreatureTypeSaveObjectSerializer implements JsonSerializer<CreatureTypeSaveObject>,
            JsonDeserializer<CreatureTypeSaveObject> {
        public final String FILE_VERSION = "1.0";
        public final String FILE_VERSION_KEY = "FILE_VERSION";
        public final String TYPE_KEY = "TYPES";

        public final String SPAWN_RATE_KEY = "Spawn Rate";
        public final String MAX_CREATURE_KEY = "Spawn Cap";
        public final String CHUNK_CHANCE_KEY = "Chunk Spawn Chance";
        public final String SPAWN_MEDIUM_KEY = "Spawn Medium";
        public final String OPTIONAL_PARAM_KEY = "Tags";
        public final String DEFAULT_BIOME_CAP_KEY = "Default Biome Cap";
        public final String MAPPING_TO_CAP = "Biome Caps";

        @Override
        public JsonElement serialize(CreatureTypeSaveObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
            JsonObject types = new JsonObject();
            for (CreatureTypeBuilder type : src.types.get().values()) {
                JsonObject entry = new JsonObject();
                entry.addProperty(SPAWN_RATE_KEY, type.spawnRate);
                entry.addProperty(MAX_CREATURE_KEY, type.maxNumberOfCreature);
                entry.addProperty(CHUNK_CHANCE_KEY, type.getChunkSpawnChance());
                entry.addProperty(SPAWN_MEDIUM_KEY, type.getRawSpawnMedium());
                entry.addProperty(OPTIONAL_PARAM_KEY, type.getOptionalParameters());
                entry.addProperty(DEFAULT_BIOME_CAP_KEY, type.getDefaultBiomeCap());
                JsonObject biomeCaps = new JsonObject();
                for (Entry<String, Integer> capEntry : type.getBiomeCaps().entrySet()) {
                    biomeCaps.addProperty(capEntry.getKey(), capEntry.getValue());
                }
                entry.add(MAPPING_TO_CAP, biomeCaps);
                types.add(type.typeID, entry);
            }
            endObject.add(TYPE_KEY, types);
            return endObject;
        }

        @Override
        public CreatureTypeSaveObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            CreatureTypeSaveObject saveObject = new CreatureTypeSaveObject();
            JsonObject endObject = GsonHelper.getAsJsonObject(json);
            String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
            JsonElement jsonElement = endObject.get(TYPE_KEY);
            if (jsonElement != null && jsonElement.isJsonObject()) {
                JsonObject types = jsonElement.getAsJsonObject();
                TreeMap<String, CreatureTypeBuilder> typeMap = new TreeMap<String, CreatureTypeBuilder>();
                for (Entry<String, JsonElement> entry : types.entrySet()) {
                    JsonObject builderObject = GsonHelper.getAsJsonObject(entry.getValue());
                    CreatureTypeBuilder builder = new CreatureTypeBuilder(entry.getKey(),
                            GsonHelper.getMemberOrDefault(builderObject, SPAWN_RATE_KEY, 1),
                            GsonHelper.getMemberOrDefault(builderObject, MAX_CREATURE_KEY, 10));
                    builder.withChanceToChunkSpawn(GsonHelper.getMemberOrDefault(builderObject, CHUNK_CHANCE_KEY, 0f));
                    builder.setRawMedium(GsonHelper.getMemberOrDefault(builderObject, SPAWN_MEDIUM_KEY, "air"));
                    builder.withOptionalParameters(GsonHelper.getMemberOrDefault(builderObject, OPTIONAL_PARAM_KEY, ""));
                    builder.withDefaultBiomeCap(GsonHelper.getMemberOrDefault(builderObject, DEFAULT_BIOME_CAP_KEY, -1));
                    JsonObject caps = GsonHelper.getMemberOrDefault(builderObject, MAPPING_TO_CAP, new JsonObject());
                    for (Entry<String, JsonElement> capEntry : caps.entrySet()) {
                        builder.withBiomeCap(capEntry.getKey(),
                                GsonHelper.getAsOrDefault(builderObject, builder.getDefaultBiomeCap()));
                        if (capEntry.getValue().isJsonPrimitive()
                                && capEntry.getValue().getAsJsonPrimitive().isNumber()) {
                            builder.withBiomeCap(capEntry.getKey(), capEntry.getValue().getAsJsonPrimitive().getAsInt());
                        }
                    }
                    typeMap.put(builder.typeID, builder);
                }
                saveObject.types = Optional.of(typeMap);
            }
            return saveObject;
        }
    }
}
