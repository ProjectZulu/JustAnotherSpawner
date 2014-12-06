package jas.gui;

import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.units.DisplayUnit;
import jas.gui.utilities.GsonHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DisplayRegistrySaveObject {
    private Optional<List<DisplayUnit>> displays;

    /** Serialization */
    private DisplayRegistrySaveObject() {
        displays = Optional.absent();
    }

    public DisplayRegistrySaveObject(Collection<DisplayUnit> displays) {
        List<DisplayUnit> displayList = new ArrayList<DisplayUnit>(displays);
        this.displays = Optional.of(displayList);
    }

    public Optional<List<DisplayUnit>> getDisplays() {
        return displays;
    }

    public static class Serializer implements JsonSerializer<DisplayRegistrySaveObject>,
            JsonDeserializer<DisplayRegistrySaveObject> {
        public final String FILE_VERSION_KEY = "FILE_VERSION";
        public final String FILE_VERSION = "1.0";
        public final String DISPLAYS_KEY = "DISPLAYS";
        private final String DISPLAY_TYPE_KEY = "TYPE";

        /** Only Present during Deserialization */
        private DisplayUnitFactory displayFactory;

        public Serializer(DisplayUnitFactory displayFactory) {
            this.displayFactory = displayFactory;
        }

        @Override
        public JsonElement serialize(DisplayRegistrySaveObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
            JsonArray displaysObject = new JsonArray();
            if (src.displays.isPresent()) {
                for (DisplayUnit display : src.displays.get()) {
                    JsonObject displayObject = new JsonObject();
                    displayObject.addProperty(DISPLAY_TYPE_KEY, display.getType());
                    display.saveCustomData(displayObject);
                    displaysObject.add(displayObject);
                }
            }
            endObject.add(DISPLAYS_KEY, displaysObject);
            return endObject;
        }

        @Override
        public DisplayRegistrySaveObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            DisplayRegistrySaveObject saveObject = new DisplayRegistrySaveObject();
            JsonObject endObject = json.getAsJsonObject();
            String currentVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);

            JsonElement displaysElement = endObject.get(DISPLAYS_KEY);
            if (displaysElement != null && displaysElement.isJsonArray()) {
                JsonArray displaysObject = displaysElement.getAsJsonArray();
                List<DisplayUnit> displays = new ArrayList<DisplayUnit>();
                for (JsonElement entry : displaysObject) {
                    JsonObject displayObject = GsonHelper.getAsJsonObject(entry);
                    DisplayUnit display = displayFactory.createDisplay(displayObject.get(DISPLAY_TYPE_KEY)
                            .getAsString(), displayObject);
                    if (display != null) {
                        displays.add(display);
                    }
                }
                saveObject.displays = Optional.of(displays);
            }
            return saveObject;
        }
    }
}
