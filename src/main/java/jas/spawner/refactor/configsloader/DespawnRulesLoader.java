package jas.spawner.refactor.configsloader;

import jas.common.helper.GsonHelper;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;
import jas.spawner.refactor.despawn.DespawnRuleBuilder;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DespawnRulesLoader implements VersionedFile {
	private String version;
	private Set<DespawnRuleBuilder> builders;

	private DespawnRulesLoader() {
		this.builders = new HashSet<DespawnRuleBuilder>();
		version = Serializer.FILE_VERSION;
	}

	public DespawnRulesLoader(Collection<DespawnRuleBuilder> despawnRules) {
		this.builders = new HashSet<DespawnRuleBuilder>();
		this.builders.addAll(despawnRules);
		version = Serializer.FILE_VERSION;
	}

	public Collection<DespawnRuleBuilder> getRules() {
		return builders;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public static class Serializer implements JsonSerializer<DespawnRulesLoader>, JsonDeserializer<DespawnRulesLoader> {
		public final static String FILE_VERSION = "1.0";
		public final static String FILE_VERSION_KEY = "FILE_VERSION";
		public final static String RULES_KEY = "RULES";

		public final static String CAN_DESPAWN_KEY = "DESPAWN_TAG";
		public final static String CAN_INSTANT_DESPAWN_KEY = "INSTANT_DESPAWN_TAG";
		public final static String DIE_OF_AGE_KEY = "AGE_DEATH_TAG";
		public final static String RESET_AGE__KEY = "AGE_RESET_TAG";

		@Override
		public JsonElement serialize(DespawnRulesLoader loader, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			JsonObject rulesObject = new JsonObject();
			for (DespawnRuleBuilder builder : loader.builders) {
				JsonObject ruleObject = new JsonObject();
				ruleObject.addProperty(CAN_DESPAWN_KEY, builder.getCanDespawnExp());
				ruleObject.addProperty(CAN_INSTANT_DESPAWN_KEY, builder.getInstantDspwnExp());
				ruleObject.addProperty(DIE_OF_AGE_KEY, builder.getAgeDeathExp());
				ruleObject.addProperty(RESET_AGE__KEY, builder.getResetAgeExp());
				rulesObject.add(builder.content(), ruleObject);
			}
			endObject.addProperty(FILE_VERSION_KEY, loader.getVersion());
			endObject.add(RULES_KEY, rulesObject);

			return endObject;
		}

		@Override
		public DespawnRulesLoader deserialize(JsonElement object, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject endObject = GsonHelper.getAsJsonObject(object);
			DespawnRulesLoader loader = new DespawnRulesLoader();
			loader.version = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);

			final DespawnRuleBuilder DEFAULT_BUILDER_VALUE = new DespawnRuleBuilder("UseForDefaultValues");
			JsonObject rulesObject = GsonHelper.getMemberOrDefault(endObject, RULES_KEY, new JsonObject());
			for (Entry<String, JsonElement> entry : rulesObject.entrySet()) {
				if (entry.getKey() == null || entry.getKey().trim().equals("")) {
					continue;
				}
				JsonObject ruleObject = GsonHelper.getAsJsonObject(entry.getValue());
				DespawnRuleBuilder builder = new DespawnRuleBuilder(entry.getKey());
				builder.setCanDespawnExp(GsonHelper.getMemberOrDefault(ruleObject, CAN_DESPAWN_KEY,
						DEFAULT_BUILDER_VALUE.getCanDespawnExp()));
				builder.setInstantDspwnExp(GsonHelper.getMemberOrDefault(ruleObject, CAN_INSTANT_DESPAWN_KEY,
						DEFAULT_BUILDER_VALUE.getInstantDspwnExp()));
				builder.setAgeDeathExp(GsonHelper.getMemberOrDefault(ruleObject, DIE_OF_AGE_KEY,
						DEFAULT_BUILDER_VALUE.getAgeDeathExp()));
				builder.setResetAgeExp(GsonHelper.getMemberOrDefault(ruleObject, RESET_AGE__KEY,
						DEFAULT_BUILDER_VALUE.getResetAgeExp()));
				loader.builders.add(builder);
			}
			return loader;
		}
	}
}
