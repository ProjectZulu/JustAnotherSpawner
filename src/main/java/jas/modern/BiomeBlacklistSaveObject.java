package jas.modern;

import jas.modern.spawner.biome.group.BiomeHelper;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BiomeBlacklistSaveObject {
    private TreeMap<String, Boolean> namedBlacklist;

    public TreeMap<String, Boolean> getBlacklist() {
        return namedBlacklist;
    }

    /** For Serialization */
    private BiomeBlacklistSaveObject() {
        namedBlacklist = new TreeMap<String, Boolean>();
    }

    public BiomeBlacklistSaveObject(boolean[] rawBlacklist) {
        namedBlacklist = new TreeMap<String, Boolean>();
        for (int biomeID = 0; biomeID < rawBlacklist.length; biomeID++) {
            BiomeGenBase biome = BiomeGenBase.getBiomeGenArray()[biomeID];
            if (biome == null) {
                continue;
            }
            namedBlacklist.put(BiomeHelper.getPackageName(biome), rawBlacklist[biomeID]);
        }
    }

    public static class BlacklistSerializer implements JsonSerializer<BiomeBlacklistSaveObject>,
            JsonDeserializer<BiomeBlacklistSaveObject> {
        private final String FILE_VERSION = "1.0";
        private final String FILE_VERSION_KEY = "FILE_VERSION";
        private final String BLACKLIST_KEY = "BLACKLIST";

        @Override
        public JsonElement serialize(BiomeBlacklistSaveObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
            JsonObject blacklistObj = new JsonObject();
            for (Entry<String, Boolean> entry : src.namedBlacklist.entrySet()) {
                blacklistObj.addProperty(entry.getKey(), entry.getValue());
            }
            endObject.add(BLACKLIST_KEY, blacklistObj);
            return endObject;
        }

        @Override
        public BiomeBlacklistSaveObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            BiomeBlacklistSaveObject saveObject = new BiomeBlacklistSaveObject();
            JsonObject object = json.getAsJsonObject();
            String fileVersion = object.get(FILE_VERSION_KEY).getAsString();
            JsonObject blacklist = object.get(BLACKLIST_KEY).getAsJsonObject();
            for (Entry<String, JsonElement> entry : blacklist.entrySet()) {
                String biomeName = entry.getKey();
                Boolean isBlacklisted = entry.getValue().getAsBoolean();
                saveObject.namedBlacklist.put(biomeName, isBlacklisted != null ? isBlacklisted : false);
            }
            return saveObject;
        }
    }
}
