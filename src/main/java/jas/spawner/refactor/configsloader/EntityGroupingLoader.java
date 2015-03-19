package jas.spawner.refactor.configsloader;

import jas.common.helper.GsonHelper;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;
import jas.spawner.refactor.entities.LivingGroupBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EntityGroupingLoader implements VersionedFile {
	private String version;
	public final TreeMap<String, String> fmlToJASName;
	public Optional<TreeMap<String, TreeMap<String, LivingGroupBuilder>>> configNameToAttributeGroups;

	public EntityGroupingLoader() {
		this.fmlToJASName = new TreeMap<String, String>();
		this.configNameToAttributeGroups = Optional.absent();
		this.version = Serializer.FILE_VERSION;
	}

	public EntityGroupingLoader(Map<Class<? extends EntityLiving>, String> classNamesToJASNames,
			Collection<LivingGroupBuilder> attributeGroups) {
		this.fmlToJASName = new TreeMap<String, String>();
		for (Entry<Class<? extends EntityLiving>, String> entry : classNamesToJASNames.entrySet()) {
			String fmlName = (String) EntityList.classToStringMapping.get(entry.getKey());
			fmlToJASName.put(fmlName, entry.getValue());
		}
		this.configNameToAttributeGroups = Optional.of(new TreeMap<String, TreeMap<String, LivingGroupBuilder>>());
		for (LivingGroupBuilder group : attributeGroups) {
			getOrCreate(configNameToAttributeGroups.get(), group.configName).put(group.groupID, group);
		}
		this.version = Serializer.FILE_VERSION;
	}

	private TreeMap<String, LivingGroupBuilder> getOrCreate(TreeMap<String, TreeMap<String, LivingGroupBuilder>> map,
			String key) {
		TreeMap<String, LivingGroupBuilder> group = map.get(key);
		if (group == null) {
			group = new TreeMap<String, LivingGroupBuilder>();
			map.put(key, group);
		}
		return group;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public static class Serializer implements JsonSerializer<EntityGroupingLoader>,
			JsonDeserializer<EntityGroupingLoader> {
		public final static String FILE_VERSION = "2.0";
		public final static String FILE_VERSION_KEY = "File Version";
		public final static String ENTITY_MAP_KEY = "CustomEntityNames";
		public final static String ATTRIBUTE_KEY = "AttributeGroups";
		public final static String CONTENTS_KEY = "contents";

		@Deprecated
		public final static String GROUP_KEY = "EntityGroups";

		@Override
		public JsonElement serialize(EntityGroupingLoader saveObject, Type type, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);

			JsonObject mappingObject = new JsonObject();
			for (Entry<String, String> entry : saveObject.fmlToJASName.entrySet()) {
				mappingObject.addProperty(entry.getKey(), entry.getValue());
			}
			endObject.add(ENTITY_MAP_KEY, mappingObject);

			JsonObject attributeObject = new JsonObject();
			for (Entry<String, TreeMap<String, LivingGroupBuilder>> outerEntry : saveObject.configNameToAttributeGroups
					.get().entrySet()) {
				String configName = outerEntry.getKey();
				JsonObject biomeObject = new JsonObject();
				for (Entry<String, LivingGroupBuilder> innerEntry : outerEntry.getValue().entrySet()) {
					String groupName = innerEntry.getKey();
					LivingGroupBuilder group = innerEntry.getValue();
					JsonArray contents = new JsonArray();
					for (String content : group.contents) {
						contents.add(new JsonPrimitive(content));
					}
					JsonObject contentsObject = new JsonObject();
					contentsObject.add(CONTENTS_KEY, contents);
					biomeObject.add(groupName, contentsObject);
				}
				attributeObject.add(configName, biomeObject);
			}
			endObject.add(ATTRIBUTE_KEY, attributeObject);

			return endObject;
		}

		@Override
		public EntityGroupingLoader deserialize(JsonElement object, Type type, JsonDeserializationContext context) {
			EntityGroupingLoader saveObject = new EntityGroupingLoader();
			JsonObject endObject = object.getAsJsonObject();
			String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);

			JsonObject mappingsObject = GsonHelper.getMemberOrDefault(endObject, ENTITY_MAP_KEY, new JsonObject());
			for (Entry<String, JsonElement> entry : mappingsObject.entrySet()) {
				saveObject.fmlToJASName.put(entry.getKey(), entry.getValue().getAsString());
			}

			JsonElement attribElement = endObject.get(ATTRIBUTE_KEY);
			if (attribElement != null && attribElement.isJsonObject()) {
				JsonObject attributeObject = attribElement.getAsJsonObject();
				for (Entry<String, JsonElement> outerEntry : attributeObject.entrySet()) {
					String configName = outerEntry.getKey();
					saveObject.configNameToAttributeGroups = Optional
							.of(new TreeMap<String, TreeMap<String, LivingGroupBuilder>>());
					TreeMap<String, LivingGroupBuilder> groupNameToBiomeGroup = saveObject.configNameToAttributeGroups
							.get().get(configName);
					if (groupNameToBiomeGroup == null) {
						groupNameToBiomeGroup = new TreeMap<String, LivingGroupBuilder>();
						saveObject.configNameToAttributeGroups.get().put(configName, groupNameToBiomeGroup);
					}
					JsonObject innerObject = outerEntry.getValue().getAsJsonObject();
					for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
						String groupName = innerEntry.getKey();
						JsonArray contentsArray = innerEntry.getValue().getAsJsonObject().get(CONTENTS_KEY)
								.getAsJsonArray();
						ArrayList<String> contents = new ArrayList<String>();
						for (JsonElement jsonElement : contentsArray) {
							contents.add(jsonElement.getAsString());
						}
						groupNameToBiomeGroup.put(groupName, new LivingGroupBuilder(groupName, configName, contents));
					}
				}
			}
			if (fileVersion.equals("1.0")) {
				compatabilityLivingGroupReader(endObject);
			}
			saveObject.version = fileVersion;
			return saveObject;
		}

		public void compatabilityLivingGroupReader(JsonObject endObject) {
			JsonElement groupElement = endObject.get(GROUP_KEY);
			if (groupElement != null && groupElement.isJsonObject()) {
				LivingHandlerLoader.Serializer.livingGroupContents.clear();
				JsonObject biomeGroupObject = groupElement.getAsJsonObject();
				for (Entry<String, JsonElement> outerEntry : biomeGroupObject.entrySet()) {
					String configName = outerEntry.getKey();
					JsonObject innerObject = GsonHelper.getAsJsonObject(outerEntry.getValue());
					for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
						String groupName = innerEntry.getKey();

						JsonArray contentsArray = GsonHelper.getMemberOrDefault(
								GsonHelper.getAsJsonObject(innerEntry.getValue()), CONTENTS_KEY, new JsonArray());
						ArrayList<String> contents = new ArrayList<String>();
						for (JsonElement jsonElement : contentsArray) {
							contents.add(jsonElement.getAsString());
						}
						LivingHandlerLoader.Serializer.livingGroupContents.put(groupName, contents);
					}
				}
			}
		}
	}
}
