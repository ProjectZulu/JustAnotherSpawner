package jas.common;

import jas.common.FileUtilities.OptionalCloseable;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GsonHelper {

    public static Gson createGson(boolean prettyPrinting) {
        return createGson(prettyPrinting, new Type[0], new Object[0]);
    }

    public static Gson createGson(boolean prettyPrinting, Type[] types, Object[] adapters) {
        if (types.length != adapters.length) {
            throw new IllegalArgumentException("Type adapters mismatched arument length");
        }
        GsonBuilder builder = new GsonBuilder().setVersion(DefaultProps.GSON_VERSION);
        if (prettyPrinting) {
            builder.setPrettyPrinting();
        }
        for (int i = 0; i < adapters.length; i++) {
            builder.registerTypeAdapter(types[i], adapters[i]);
        }
        return builder.create();
    }

    public static <T> T readFromGson(OptionalCloseable<FileReader> reader, Class<T> object, Gson gson) {
        if (reader.isPresent()) {
            T instance = gson.fromJson(reader.get(), object);
            reader.close();
            if (instance != null) {
                return instance;
            }
        }
        try {
            Constructor<T> constructor = object.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            JASLog.log().severe("This should never be possible. Failed to instantiate class %s.", object);
            e.printStackTrace();
            return null;
        }
    }

    public static <T> Optional<T> readFromGson(OptionalCloseable<FileReader> reader, Type type, Gson gson) {
        if (reader.isPresent()) {
            T instance = gson.fromJson(reader.get(), type);
            reader.close();
            if (instance != null) {
                return Optional.of(instance);
            }
        }
        return Optional.absent();
    }

    public static <T> void writeToGson(OptionalCloseable<FileWriter> writer, T object, Gson gson) {
        if (writer.isPresent()) {
            gson.toJson(object, writer.get());
            writer.close();
        }
    }

    public static <T> void writeToGson(OptionalCloseable<FileWriter> writer, T object, Type type, Gson gson) {
        if (writer.isPresent()) {
            gson.toJson(object, type, writer.get());
            writer.close();
        }
    }

    /**
     * Helper for unwrapping JsonElements, returns empty JSON is provided element is not a JsonObject
     */
    public static JsonObject getAsJsonObject(JsonElement element) {
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }

    /**
     * Helper for unwrapping JsonObject members, returns default value if desired member is absent or an invalid type
     */
    public static String getMemberOrDefault(JsonObject jsonObject, String memberName, String defaultIfAbsent) {
        JsonElement element = jsonObject.get(memberName);
        if (element != null && element.isJsonPrimitive()) {
            JsonPrimitive memberPrimitive = element.getAsJsonPrimitive();
            if (memberPrimitive.isString()) {
                return memberPrimitive.getAsString();
            }
        }
        return defaultIfAbsent;
    }

    /**
     * Helper for unwrapping JsonObject members, returns default value if desired member is absent or an invalid type
     */
    public static int getMemberOrDefault(JsonObject jsonObject, String memberName, int defaultIfAbsent) {
        JsonElement element = jsonObject.get(memberName);
        if (element != null && element.isJsonPrimitive()) {
            JsonPrimitive memberPrimitive = element.getAsJsonPrimitive();
            if (memberPrimitive.isNumber()) {
                return memberPrimitive.getAsInt();
            }
        }
        return defaultIfAbsent;
    }

    /**
     * Helper for unwrapping JsonObject members, returns default value if desired member is absent or an invalid type
     */
    public static boolean getMemberOrDefault(JsonObject jsonObject, String memberName, boolean defaultIfAbsent) {
        JsonElement element = jsonObject.get(memberName);
        if (element != null && element.isJsonPrimitive()) {
            JsonPrimitive memberPrimitive = element.getAsJsonPrimitive();
            if (memberPrimitive.isBoolean()) {
                return memberPrimitive.getAsBoolean();
            }
        }
        return defaultIfAbsent;
    }

    /**
     * Helper for unwrapping JsonObject members, returns default value if desired member is absent or an invalid type
     */
    public static JsonObject getMemberOrDefault(JsonObject jsonObject, String memberName, JsonObject defaultIfAbsent) {
        JsonElement element = jsonObject.get(memberName);
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return defaultIfAbsent;
    }

    /**
     * Helper for unwrapping JsonObject members, returns default value if desired member is absent or an invalid type
     */
    public static JsonArray getMemberOrDefault(JsonObject jsonObject, String memberName, JsonArray defaultIfAbsent) {
        JsonElement element = jsonObject.get(memberName);
        if (element != null && element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        return defaultIfAbsent;
    }

}
