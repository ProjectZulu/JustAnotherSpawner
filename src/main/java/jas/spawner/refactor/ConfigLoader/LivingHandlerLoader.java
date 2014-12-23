package jas.spawner.refactor.configloader;

import jas.common.JASLog;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.spawner.TagConverter;
import jas.spawner.modern.spawner.creature.handler.parsing.keys.Key;
import jas.spawner.refactor.configloader.ConfigLoader.VersionedFile;
import jas.spawner.refactor.entities.LivingHandlerBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

public class LivingHandlerLoader implements VersionedFile {
	private String version;
	private Optional<HashMap<String, LivingHandlerBuilder>> handlerIdToBuilder;

	private LivingHandlerLoader() {
		handlerIdToBuilder = Optional.absent();
		version = Serializer.FILE_VERSION;
	}

	public LivingHandlerLoader(Collection<LivingHandlerBuilder> handlers) {
		HashMap<String, LivingHandlerBuilder> map = new HashMap<String, LivingHandlerBuilder>();
		for (LivingHandlerBuilder livingHandlerBuilder : handlers) {
			map.put(livingHandlerBuilder.getLivingHandlerID(), livingHandlerBuilder);
		}
		handlerIdToBuilder = Optional.of(map);
		version = Serializer.FILE_VERSION;
	}
	
	public Optional<Collection<LivingHandlerBuilder>> getHandlers() {
		return handlerIdToBuilder.isPresent() ? Optional.of(handlerIdToBuilder.get().values()) : Optional
				.<Collection<LivingHandlerBuilder>> absent();
	}

	@Override
	public String getVersion() {
		return version;
	}

	public static class Serializer implements JsonSerializer<LivingHandlerLoader>,
			JsonDeserializer<LivingHandlerLoader> {
		// Hack to provide backwards compatability: read contents from LivingGroups and move them to LivingHandler
		public static HashMap<String, List<String>> livingGroupContents = new HashMap<String, List<String>>();

		public static final String FILE_VERSION = "3.0";
		public final String FILE_VERSION_KEY = "FILE_VERSION";
		public final String HANDLERS_KEY = "LIVING_HANDLERS";

		@Deprecated
		final String STATS_KEY = "Type-Enabled";

		@Deprecated
		final String TAGS_KEY = "Tags";

		public final String SPAWN_TAG_KEY = "Spawn Tag";
		public final String DESPAWN_KEY = "Despawn Tags";
		public final String INSTANT_DESPAWN_KEY = "InstantDespawn Tags";
		public final String POSTSPAWN_KEY = "PostSpawn Tags";

		public final String DESPAWN_AGE_EXP_KEY = "Depawnable Age Tag";
		public final String RESET_AGE_KEY = "Reset Age Tag";

		@Deprecated
		public final String ENTITY_EXP_KEY = "Entity Tags";
		@Deprecated
		final String MIN_DESPAWN_RANGE_KEY = "Min Despawn Range";
		@Deprecated
		final String MAX_DESPAWN_RANGE_KEY = "Max Despawn Range";
		@Deprecated
		final String ENTITY_CAP_KEY = "Entity Cap";
		@Deprecated
		final String DESPAWN_AGE_KEY = "Despawn Age";
		@Deprecated
		final String DESPAWN_RATE_KEY = "Despawn Rate";
		@Deprecated
		final String SPAWN_OPERAND_KEY = "Spawn Operand";
		@Deprecated
		public final String CONTENTS_KEY = "Contents";
		private String currentVersion;

		@Override
		public JsonElement serialize(LivingHandlerLoader src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			JsonObject livingHandlers = new JsonObject();
			for (LivingHandlerBuilder builder : src.handlerIdToBuilder.get().values()) {
				JsonObject handler = new JsonObject();
				if (!"".equals(builder.getCanSpawn())) {
					handler.addProperty(SPAWN_TAG_KEY, builder.getCanSpawn());
				}

				if (!"".equals(builder.getCanDspwn())) {
					handler.addProperty(DESPAWN_KEY, builder.getCanDspwn());
				}

				if (!"".equals(builder.getShouldInstantDspwn())) {
					handler.addProperty(INSTANT_DESPAWN_KEY, builder.getShouldInstantDspwn());
				}

				if (!"".equals(builder.getPostSpawn())) {
					handler.addProperty(POSTSPAWN_KEY, builder.getPostSpawn());
				}

				if (!"".equals(builder.getIsDspnbleAge())) {
					handler.addProperty(DESPAWN_AGE_EXP_KEY, builder.getIsDspnbleAge());
				}

				if (!"".equals(builder.getShouldResetAge())) {
					handler.addProperty(RESET_AGE_KEY, builder.getShouldResetAge());
				}
				livingHandlers.add(builder.getLivingHandlerID(), handler);
			}
			endObject.add(HANDLERS_KEY, livingHandlers);
			return endObject;
		}
		
		@Override
		public LivingHandlerLoader deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			LivingHandlerLoader saveObject = new LivingHandlerLoader();
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
					saveObject.handlerIdToBuilder.get().put(builder.getLivingHandlerID(), builder);
				}
			}
			saveObject.version = currentVersion;
			return saveObject;
		}

		private LivingHandlerBuilder getBuilder(JsonObject handler, String handlerId) {
			if (currentVersion.equals("3.0")) {
				LivingHandlerBuilder builder = new LivingHandlerBuilder(handlerId);
				return builder;
			} else {
				String stats = GsonHelper.getMemberOrDefault(handler, STATS_KEY, "NONE-true");
				if (stats.split("-").length != 2) {
					JASLog.log().severe("Error parsing LivingHandler %s stats data: %s is an invalid format",
							handlerId, stats);
					stats = "NONE-true";
				}
				/* Maintain these for Compatability */
				String creatureTypeId = stats.split("-")[0];
				boolean shouldSpawn = Boolean.parseBoolean(stats.split("-")[1].trim());

				LivingHandlerBuilder builder = new LivingHandlerBuilder(handlerId);
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
								// Optional.of(conv.operand)
								builder.setCanSpawn(conv.expression);
							}
						} else if (Key.despawn.keyParser.isMatch(titletag)) {
							conv = new TagConverter(parsed);
							if (!conv.expression.trim().equals("")) {
								builder.setCanDspwn(conv.expression);
							}
						} else if (Key.postspawn.keyParser.isMatch(titletag)) {
							conv = new TagConverter(parsed);
							if (!conv.expression.trim().equals("")) {
								builder.setPostSpawn(conv.expression);
							}
						}
						if (conv != null) {
							int maxDespawnRange = conv.maxSpawnRange.isPresent() ? conv.maxSpawnRange.get() : 128;
							int despawnAge = conv.despawnAge.isPresent() ? conv.despawnAge.get() : 600;
							int despawnRate = conv.despawnRate.isPresent() ? conv.despawnRate.get() : 40;
							int resetAgeDistance = conv.minDespawnRage.isPresent() ? conv.minDespawnRage.get() : 32;

							builder.setShouldInstantDspwn("sp.plyrDist < " + maxDespawnRange);
							builder.setIsDspnbleAge("!(ent.age > " + despawnAge + " && util.random(1+" + despawnRate
									+ "/3,0,0))");
							builder.setShouldResetAge("sp.plyrDist > " + resetAgeDistance);
							/* Preserve this incase we decide to use it in converter */
							int entityCap = conv.entityCap.isPresent() ? conv.entityCap.get() : -1;
						}
					}
				} else {
					String spawnTag = GsonHelper.getMemberOrDefault(handler, SPAWN_TAG_KEY, "");
					String spawnOperand = GsonHelper.getMemberOrDefault(handler, SPAWN_OPERAND_KEY, "");
					builder.setCanSpawn(spawnTag);
					// Optional.of("OR".equalsIgnoreCase(spawnOperand) ? Operand.OR : Operand.AND)
					String despawnTag = GsonHelper.getMemberOrDefault(handler, DESPAWN_KEY, "");
					builder.setCanDspwn(despawnTag);
					String instantdespawnTag = GsonHelper.getMemberOrDefault(handler, INSTANT_DESPAWN_KEY, "");
					builder.setShouldInstantDspwn(instantdespawnTag);
					String postspawnTag = GsonHelper.getMemberOrDefault(handler, POSTSPAWN_KEY, "");
					builder.setPostSpawn(postspawnTag);

					int resetAgeDistance = GsonHelper.getMemberOrDefault(handler, MIN_DESPAWN_RANGE_KEY, -1);
					int maxDespawnRange = GsonHelper.getMemberOrDefault(handler, MAX_DESPAWN_RANGE_KEY, -1);
					int despawnAge = GsonHelper.getMemberOrDefault(handler, DESPAWN_AGE_KEY, -1);
					int despawnRate = GsonHelper.getMemberOrDefault(handler, DESPAWN_RATE_KEY, -1);

					builder.setShouldInstantDspwn("sp.plyrDist < " + maxDespawnRange);
					builder.setIsDspnbleAge("!(ent.age > " + despawnAge + " && util.random(1+" + despawnRate
							+ "/3,0,0))");
					builder.setShouldResetAge("sp.plyrDist > " + resetAgeDistance);
					/* Preserve this incase we decide to use it in converter */
					int entityCap = GsonHelper.getMemberOrDefault(handler, ENTITY_CAP_KEY, -1);
				}

				if (builder.getCanSpawn().trim().equals("")) {
					builder.setCanSpawn("!(modspawn || sp.clearBounding)");
				} else {
					builder.setCanSpawn(builder.getCanSpawn() + "|| !sp.clearBounding)");
				}

				if (builder.getCanDspwn().trim().equals("")) {
					builder.setCanDspwn("false");
				}

				/* Preserve this incase we decide to use it in converter */
				String entityExpression = GsonHelper.getMemberOrDefault(handler, ENTITY_EXP_KEY, "");
				List<String> builderContents = new ArrayList<String>();
				JsonArray contents = GsonHelper.getMemberOrDefault(handler, CONTENTS_KEY, getDefaultArray(handlerId));
				for (JsonElement jsonElement : contents) {
					String content = GsonHelper.getAsOrDefault(jsonElement, "");
					if (content != null && !content.trim().equals("")) {
						builderContents.add(content);
					}
				}

				return builder;
			}
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
