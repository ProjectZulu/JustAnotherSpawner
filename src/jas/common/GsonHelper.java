package jas.common;

import jas.common.FileUtilities.OptionalCloseable;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonHelper {

    public static Gson createGson() {
        return new GsonBuilder().setVersion(DefaultProps.GSON_VERSION).setPrettyPrinting().create();
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
            return object.newInstance();
        } catch (Exception e) {
            JASLog.severe("This should never be possible. Failed to instantiate class %s.", object);
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
}
