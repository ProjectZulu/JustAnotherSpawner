package jas.common.spawner.creature.handler;

import jas.common.GsonHelper;
import jas.common.JASLog;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
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

public class LivingHandlerSaveObject {

    private Optional<HashMap<String, LivingHandlerBuilder>> handlerIdToBuilder;
    
    private LivingHandlerSaveObject() {
        handlerIdToBuilder = Optional.absent();
    }

    public LivingHandlerSaveObject(Collection<LivingHandlerBuilder> handlers) {
        HashMap<String, LivingHandlerBuilder> map = new HashMap<String, LivingHandlerBuilder>();
        for (LivingHandlerBuilder livingHandlerBuilder : handlers) {
            map.put(livingHandlerBuilder.getHandlerId(), livingHandlerBuilder);
        }
        handlerIdToBuilder = Optional.of(map);
    }

    public Optional<Collection<LivingHandlerBuilder>> getHandlers() {
        return handlerIdToBuilder.isPresent() ? Optional.of(handlerIdToBuilder.get().values()) : Optional
                .<Collection<LivingHandlerBuilder>> absent();
    }

    public static class Serializer implements JsonSerializer<LivingHandlerSaveObject>,
            JsonDeserializer<LivingHandlerSaveObject> {
        public final String FILE_VERSION = "1.0";
        public final String FILE_VERSION_KEY = "FILE_VERSION";
        public final String HANDLERS_KEY = "LIVING_HANDLERS";
        public final String STATS_KEY = "Type-Enabled";
        public final String TAGS_KEY = "Tags";

        @Override
        public JsonElement serialize(LivingHandlerSaveObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
            JsonObject livingHandlers = new JsonObject();
            for (LivingHandlerBuilder builder : src.handlerIdToBuilder.get().values()) {
                JsonObject handler = new JsonObject();
                handler.addProperty(STATS_KEY,
                        builder.getCreatureTypeId().concat("-").concat(Boolean.toString(builder.getShouldSpawn())));
                if (!"".equals(builder.getOptionalParameters())) {
                    handler.addProperty(TAGS_KEY, builder.getOptionalParameters());
                }
                livingHandlers.add(builder.getHandlerId(), handler);
            }
            endObject.add(HANDLERS_KEY, livingHandlers);
            return endObject;
        }

        @Override
        public LivingHandlerSaveObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            LivingHandlerSaveObject saveObject = new LivingHandlerSaveObject();
            JsonObject endObject = GsonHelper.getAsJsonObject(json);
            String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
            JsonElement handlerElement = endObject.get(HANDLERS_KEY);
            if (handlerElement != null && handlerElement.isJsonObject()) {
                JsonObject handlesr = handlerElement.getAsJsonObject();
                saveObject.handlerIdToBuilder = Optional.of(new HashMap<String, LivingHandlerBuilder>());
                for (Entry<String, JsonElement> entry : handlesr.entrySet()) {
                    String handlerId = entry.getKey();
                    JsonObject handler = GsonHelper.getAsJsonObject(entry.getValue());
                    LivingHandlerBuilder builder = getBuilder(handler, handlerId);
                    saveObject.handlerIdToBuilder.get().put(builder.getHandlerId(), builder);
                }
            }
            return saveObject;
        }

        private LivingHandlerBuilder getBuilder(JsonObject handler, String handlerId) {
            String stats = GsonHelper.getMemberOrDefault(handler, STATS_KEY, "NONE-true");
            if (stats.split("-").length != 2) {
                JASLog.log().severe("Error parsing LivingHandler %s stats data: %s is an invalid format", handlerId,
                        stats);
                stats = "NONE-true";
            }
            String tags = GsonHelper.getMemberOrDefault(handler, TAGS_KEY, "");
            String creatureTypeId = stats.split("-")[0];
            boolean shouldSpawn = Boolean.parseBoolean(stats.split("-")[1].trim());
            LivingHandlerBuilder builder = new LivingHandlerBuilder(handlerId, creatureTypeId).setShouldSpawn(
                    shouldSpawn).setOptionalParameters(tags);
            return builder;
        }
    }
}
