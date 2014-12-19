package jas.modern.spawner.creature.handler;

import jas.modern.GsonHelper;
import jas.modern.JASLog;
import jas.modern.spawner.TagConverter;
import jas.modern.spawner.creature.handler.parsing.keys.Key;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettingsDespawning;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettingsPostSpawning;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettingsSpawning;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
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
		// Hack to provide backwards compatability: read contents from LivingGroups and move them to LivingHandler
		public static HashMap<String, List<String>> livingGroupContents = new HashMap<String, List<String>>();

		public final String FILE_VERSION = "2.0";
		public final String FILE_VERSION_KEY = "FILE_VERSION";
		public final String HANDLERS_KEY = "LIVING_HANDLERS";
		public final String STATS_KEY = "Type-Enabled";
		@Deprecated
		final String TAGS_KEY = "Tags";
		public final String SPAWN_TAG_KEY = "Spawn Tag";
		public final String DESPAWN_KEY = "Despawn Tags";
		public final String INSTANT_DESPAWN_KEY = "InstantDespawn Tags";
		public final String POSTSPAWN_KEY = "PostSpawn Tags";
		public final String ENTITY_EXP_KEY = "Entity Tags";

		public final String MIN_DESPAWN_RANGE_KEY = "Min Despawn Range";
		public final String MAX_DESPAWN_RANGE_KEY = "Max Despawn Range";
		public final String ENTITY_CAP_KEY = "Entity Cap";
		public final String DESPAWN_AGE_KEY = "Despawn Age";
		public final String DESPAWN_RATE_KEY = "Despawn Rate";
		public final String SPAWN_OPERAND_KEY = "Spawn Operand";

		public final String CONTENTS_KEY = "Contents";
		private String currentVersion;

		@Override
		public JsonElement serialize(LivingHandlerSaveObject src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			JsonObject livingHandlers = new JsonObject();
			for (LivingHandlerBuilder builder : src.handlerIdToBuilder.get().values()) {
				JsonObject handler = new JsonObject();
				handler.addProperty(STATS_KEY,
						builder.getCreatureTypeId().concat("-").concat(Boolean.toString(builder.getShouldSpawn())));

				if (builder.getSpawnOperand().isPresent()) {
					handler.addProperty(SPAWN_OPERAND_KEY, builder.getSpawnOperand().get().toString());
				}

				if (!"".equals(builder.getEntityExpression())) {
					handler.addProperty(ENTITY_EXP_KEY, builder.getEntityExpression());
				}
				
				if (!"".equals(builder.getSpawnExpression())) {
					handler.addProperty(SPAWN_TAG_KEY, builder.getSpawnExpression());
				}

				if (!"".equals(builder.getDespawnExpression())) {
					handler.addProperty(DESPAWN_KEY, builder.getDespawnExpression());
				}

				if (!"".equals(builder.getInstantDespawnExpression())) {
					handler.addProperty(INSTANT_DESPAWN_KEY, builder.getInstantDespawnExpression());
				}
				
				if (!"".equals(builder.getPostSpawnExpression())) {
					handler.addProperty(POSTSPAWN_KEY, builder.getPostSpawnExpression());
				}

				if (builder.getMinDespawnRange().isPresent()) {
					handler.addProperty(MIN_DESPAWN_RANGE_KEY, builder.getMinDespawnRange().get());
				}

				if (builder.getMaxDespawnRange().isPresent()) {
					handler.addProperty(MAX_DESPAWN_RANGE_KEY, builder.getMaxDespawnRange().get());
				}

				if (builder.getEntityCap().isPresent()) {
					handler.addProperty(ENTITY_CAP_KEY, builder.getEntityCap().get());
				}

				if (builder.getDespawnAge().isPresent()) {
					handler.addProperty(DESPAWN_AGE_KEY, builder.getDespawnAge().get());
				}

				if (builder.getDespawnRate().isPresent()) {
					handler.addProperty(DESPAWN_RATE_KEY, builder.getDespawnRate().get());
				}
				
				JsonArray contents = new JsonArray();
				for (String content : builder.contents) {
					contents.add(new JsonPrimitive(content));
				}
				handler.add(CONTENTS_KEY, contents);

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
			currentVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
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
			String creatureTypeId = stats.split("-")[0];
			boolean shouldSpawn = Boolean.parseBoolean(stats.split("-")[1].trim());
			LivingHandlerBuilder builder = new LivingHandlerBuilder(handlerId, creatureTypeId)
					.setShouldSpawn(shouldSpawn);
			if (currentVersion.equals("1.0")) {
				final String optionalParameters = GsonHelper.getMemberOrDefault(handler, TAGS_KEY, "");
				String[] parts = optionalParameters.split("\\{");
				for (String string : optionalParameters.split("\\{")) {
					String parsed = string.replace("}", "");
					String titletag = parsed.split("\\:", 2)[0].toLowerCase();
					TagConverter conv = null;
					if (Key.spawn.keyParser.isMatch(titletag)) {
						conv = new TagConverter(parsed);
						if (!conv.expression.trim().equals("")) {
							builder.setSpawnExpression(conv.expression, Optional.of(conv.operand));
						}
					} else if (Key.despawn.keyParser.isMatch(titletag)) {
						conv = new TagConverter(parsed);
						if (!conv.expression.trim().equals("")) {
							builder.setDespawnExpression(conv.expression);
						}
					} else if (Key.postspawn.keyParser.isMatch(titletag)) {
						conv = new TagConverter(parsed);
						if (!conv.expression.trim().equals("")) {
							builder.setPostSpawnExpression(conv.expression);
						}
					}
					if (conv != null) {
						if (conv.despawnAge.isPresent()) {
							builder.setDespawnAge(conv.despawnAge.get());
						}
						if (conv.entityCap.isPresent()) {
							builder.setEntityCap(conv.entityCap.get());
						}
						if (conv.maxSpawnRange.isPresent()) {
							builder.setMaxDespawnRange(conv.maxSpawnRange.get());
						}
						if (conv.minDespawnRage.isPresent()) {
							builder.setMinDespawnRange(conv.minDespawnRage.get());
						}
						if (conv.despawnRate.isPresent()) {
							builder.setDespawnRate(conv.despawnRate.get());
						}
					}
				}
			} else {
				String spawnTag = GsonHelper.getMemberOrDefault(handler, SPAWN_TAG_KEY, "");
				String spawnOperand = GsonHelper.getMemberOrDefault(handler, SPAWN_OPERAND_KEY, "");
				builder.setSpawnExpression(spawnTag,
						Optional.of("OR".equalsIgnoreCase(spawnOperand) ? Operand.OR : Operand.AND));
				String despawnTag = GsonHelper.getMemberOrDefault(handler, DESPAWN_KEY, "");
				builder.setDespawnExpression(despawnTag);
				String instantdespawnTag = GsonHelper.getMemberOrDefault(handler, INSTANT_DESPAWN_KEY, "");
				builder.setInstantDespawnExpression(instantdespawnTag);
				String postspawnTag = GsonHelper.getMemberOrDefault(handler, POSTSPAWN_KEY, "");
				builder.setPostSpawnExpression(postspawnTag);

				int minDespawnRange = GsonHelper.getMemberOrDefault(handler, MIN_DESPAWN_RANGE_KEY, -1);
				builder.setMinDespawnRange(minDespawnRange);
				int maxDespawnRange = GsonHelper.getMemberOrDefault(handler, MAX_DESPAWN_RANGE_KEY, -1);
				builder.setMaxDespawnRange(maxDespawnRange);
				int entityCap = GsonHelper.getMemberOrDefault(handler, ENTITY_CAP_KEY, -1);
				builder.setEntityCap(entityCap);
				int despawnAge = GsonHelper.getMemberOrDefault(handler, DESPAWN_AGE_KEY, -1);
				builder.setDespawnAge(despawnAge);
				int despawnRate = GsonHelper.getMemberOrDefault(handler, DESPAWN_RATE_KEY, -1);
				builder.setDespawnRate(despawnRate);
			}
			builder.setEntityExpression(GsonHelper.getMemberOrDefault(handler, ENTITY_EXP_KEY, ""));
			
			JsonArray contents = GsonHelper.getMemberOrDefault(handler, CONTENTS_KEY, getDefaultArray(handlerId));
			for (JsonElement jsonElement : contents) {
				String content = GsonHelper.getAsOrDefault(jsonElement, "");
				if (content != null && !content.trim().equals("")) {
					builder.contents.add(content);
				}
			}
			return builder;
		}

		private JsonArray getDefaultArray(String handlerID) {
			JsonArray jsonArray = new JsonArray();
			if (currentVersion.equals("1.0")) {
				List<String> contents = livingGroupContents.get(handlerID);
				if (contents != null) {
					for (String content : contents) {
						jsonArray.add(new JsonPrimitive(content));
					}
				}
			}
			return jsonArray;
		}
	}
}
