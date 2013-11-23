package jas.common.spawner.creature.type;

import jas.common.spawner.creature.handler.LivingHandlerBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LivingTypeSerializer implements JsonSerializer<HashMap<String, LivingHandlerBuilder>>,
        JsonDeserializer<HashMap<String, LivingHandlerBuilder>> {

    @Override
    public JsonElement serialize(HashMap<String, LivingHandlerBuilder> object, Type type,
            JsonSerializationContext context) {
        JsonObject map = new JsonObject();
        for (LivingHandlerBuilder builder : object.values()) {
            JsonObject handlerEntry = new JsonObject();
            JsonPrimitive settings = new JsonPrimitive(builder.getCreatureTypeId().concat("-")
                    .concat(Boolean.toString(builder.getShouldSpawn())));
            handlerEntry.add("Type-Enabled", settings);
            handlerEntry.add("Tags", new JsonPrimitive(builder.getOptionalParameters()));
            map.add(builder.getHandlerId(), handlerEntry);
        }
        return map;
    }

    @Override
    public HashMap<String, LivingHandlerBuilder> deserialize(JsonElement element, Type type,
            JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, LivingHandlerBuilder> handlers = new HashMap<String, LivingHandlerBuilder>();
        JsonObject map = element.getAsJsonObject();
        for (Entry<String, JsonElement> entry : map.entrySet()) {
            JsonObject handlerEntry = entry.getValue().getAsJsonObject();
            String handlerId = entry.getKey();
            if (handlerId != null && !handlerId.equals("") && handlerEntry != null) {
                LivingHandlerBuilder builder = getBuilder(handlerEntry, handlerId);
                handlers.put(builder.getHandlerId(), builder);
            }
        }
        return handlers;
    }

    private LivingHandlerBuilder getBuilder(JsonObject handlerEntry, String handlerId) {
        JsonPrimitive settings = handlerEntry.getAsJsonPrimitive("Type-Enabled");
        LivingHandlerBuilder builder = new LivingHandlerBuilder(handlerId);
        if (settings != null) {
            String[] parts = settings.getAsString().split("-");
            builder.setCreatureTypeId(parts[0]);
            if (parts.length == 2) {
                builder.setShouldSpawn(Boolean.parseBoolean(parts[1]));
            }
        }
        JsonPrimitive tags = handlerEntry.getAsJsonPrimitive("Tags");
        if (tags != null) {
            builder.setOptionalParameters(tags.getAsString());
        }
        return builder;
    }
}
