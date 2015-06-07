package jas.spawner.refactor.configsloader;

import jas.common.helper.GsonHelper;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;
import jas.spawner.refactor.entities.LivingHandlerBuilder;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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

		public final String MODID_KEY = "MOD_ID";
		public final String SPAWN_TAG_KEY = "SPAWN_TAG";
		public final String POSTSPAWN_KEY = "POST_SPAWN_TAG";

		public final String WEIGHT_KEY = "WEIGHT";
		public final String PASSIVE_SPAWN_KEY = "PASSIVE_PACKSIZE";
		public final String CHUNK_SPAWN_KEY = "CHUNK_PACKSIZE";
		private String currentVersion;

		@Override
		public JsonElement serialize(LivingHandlerLoader src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			JsonObject livingHandlers = new JsonObject();
			for (LivingHandlerBuilder builder : src.handlerIdToBuilder.get().values()) {
				JsonObject handler = new JsonObject();
				if (!"".equals(builder.getModID())) {
					handler.addProperty(POSTSPAWN_KEY, builder.getModID());
				}
				if (!"".equals(builder.getCanSpawn()) && builder.getCanSpawn().isPresent()) {
					handler.addProperty(SPAWN_TAG_KEY, builder.getCanSpawn().get());
				}
				if (!"".equals(builder.getPostSpawn()) && builder.getPostSpawn().isPresent()) {
					handler.addProperty(POSTSPAWN_KEY, builder.getPostSpawn().get());
				}

				if (!"".equals(builder.getWeight()) && builder.getWeight().isPresent()) {
					handler.addProperty(WEIGHT_KEY, builder.getWeight().get());
				}
				if (!"".equals(builder.getPassivePackSize()) && builder.getPassivePackSize().isPresent()) {
					handler.addProperty(PASSIVE_SPAWN_KEY, builder.getPassivePackSize().get());
				}
				if (!"".equals(builder.getChunkPackSize()) && builder.getChunkPackSize().isPresent()) {
					handler.addProperty(CHUNK_SPAWN_KEY, builder.getChunkPackSize().get());
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
			LivingHandlerBuilder builder = new LivingHandlerBuilder(handlerId);
			builder.setModID(GsonHelper.getMemberOrDefault(handler, MODID_KEY, LivingHandlerBuilder.defaultFileName));
			builder.setCanSpawn(GsonHelper
					.getMemberOrDefault(handler, SPAWN_TAG_KEY, "!(modspawn || sp.clearBounding)"));
			builder.setPostSpawn(GsonHelper.getMemberOrDefault(handler, POSTSPAWN_KEY, ""));
			builder.setWeight(GsonHelper.getMemberOrDefault(handler, WEIGHT_KEY, ""));
			builder.setPassivePackSize(GsonHelper.getMemberOrDefault(handler, PASSIVE_SPAWN_KEY, ""));
			builder.setChunkPackSize(GsonHelper.getMemberOrDefault(handler, CHUNK_SPAWN_KEY, ""));
			return builder;
		}
	}
}
