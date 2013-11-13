package jas.common;

import jas.common.FileUtilities.OptionalCloseable;

import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;

public class GsonHelper {

    public static <T> T readFromGson(OptionalCloseable<FileReader> reader, Class<T> object, Gson gson) {
        if (reader.isPresent()) {
            T instance = gson.fromJson(reader.get(), object);
            reader.close();
            return instance;
        } else {
            try {
                return object.newInstance();
            } catch (Exception e) {
                JASLog.severe("This should never be possible. Failed to instantiate class %s.", object);
                e.printStackTrace();
                return null;
            }
        }
    }

    public static <T> void writeToGson(OptionalCloseable<FileWriter> writer, T object, Gson gson) {
        if (writer.isPresent()) {
            gson.toJson(object, writer.get());
            writer.close();
        }
    }
}
