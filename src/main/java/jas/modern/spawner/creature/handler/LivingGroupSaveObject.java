package jas.modern.spawner.creature.handler;

import jas.modern.DefaultProps;
import jas.modern.GsonHelper;
import jas.modern.spawner.creature.handler.LivingGroupRegistry.LivingGroup;

import java.io.File;
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
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LivingGroupSaveObject {
    public final TreeMap<String, String> fmlToJASName;
    public Optional<TreeMap<String, TreeMap<String, LivingGroup>>> configNameToAttributeGroups;
    public Optional<TreeMap<String, TreeMap<String, LivingGroup>>> configNameToLivingGroups;

    /* For Serialization Only */
    public LivingGroupSaveObject() {
        this.fmlToJASName = new TreeMap<String, String>();
        this.configNameToAttributeGroups = Optional.absent();
        this.configNameToLivingGroups = Optional.absent();
    }
    //TODO: Remove LivingGroups as they no longer exist
    public LivingGroupSaveObject(Map<Class<? extends EntityLiving>, String> classNamesToJASNames,
            Collection<LivingGroup> attributeGroups, Collection<LivingGroup> LivingGroups) {
        this.fmlToJASName = new TreeMap<String, String>();
        for (Entry<Class<? extends EntityLiving>, String> entry : classNamesToJASNames.entrySet()) {
            String fmlName = (String) EntityList.classToStringMapping.get(entry.getKey());
            fmlToJASName.put(fmlName, entry.getValue());
        }
        this.configNameToLivingGroups = Optional.of(new TreeMap<String, TreeMap<String, LivingGroup>>());
        this.configNameToAttributeGroups = Optional.of(new TreeMap<String, TreeMap<String, LivingGroup>>());
        for (LivingGroup group : attributeGroups) {
            getOrCreate(configNameToAttributeGroups.get(), group.configName).put(group.groupID, group);
        }
        for (LivingGroup group : LivingGroups) {
            getOrCreate(configNameToLivingGroups.get(), group.configName).put(group.groupID, group);
        }
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + "LivingGroups.cfg");
    }

    private TreeMap<String, LivingGroup> getOrCreate(TreeMap<String, TreeMap<String, LivingGroup>> map, String key) {
        TreeMap<String, LivingGroup> group = map.get(key);
        if (group == null) {
            group = new TreeMap<String, LivingGroup>();
            map.put(key, group);
        }
        return group;
    }

    public static class LivingGroupSaveObjectSerializer implements JsonSerializer<LivingGroupSaveObject>,
            JsonDeserializer<LivingGroupSaveObject> {
        public final static String FILE_VERSION = "2.0";
        public final static String FILE_VERSION_KEY = "File Version";
        public final static String ENTITY_MAP_KEY = "CustomEntityNames";
        public final static String ATTRIBUTE_KEY = "AttributeGroups";
        public final static String GROUP_KEY = "EntityGroups";
        public final static String CONTENTS_KEY = "contents";

        @Override
        public JsonElement serialize(LivingGroupSaveObject saveObject, Type type, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);

            JsonObject mappingObject = new JsonObject();
            for (Entry<String, String> entry : saveObject.fmlToJASName.entrySet()) {
                mappingObject.addProperty(entry.getKey(), entry.getValue());
            }
            endObject.add(ENTITY_MAP_KEY, mappingObject);

            JsonObject attributeObject = new JsonObject();
            for (Entry<String, TreeMap<String, LivingGroup>> outerEntry : saveObject.configNameToAttributeGroups.get()
                    .entrySet()) {
                String configName = outerEntry.getKey();
                JsonObject biomeObject = new JsonObject();
                for (Entry<String, LivingGroup> innerEntry : outerEntry.getValue().entrySet()) {
                    String groupName = innerEntry.getKey();
                    LivingGroup group = innerEntry.getValue();
                    JsonArray contents = new JsonArray();
                    for (String content : group.contents()) {
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
        public LivingGroupSaveObject deserialize(JsonElement object, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            LivingGroupSaveObject saveObject = new LivingGroupSaveObject();
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
							.of(new TreeMap<String, TreeMap<String, LivingGroup>>());
					TreeMap<String, LivingGroup> groupNameToBiomeGroup = saveObject.configNameToAttributeGroups.get()
							.get(configName);
					if (groupNameToBiomeGroup == null) {
						groupNameToBiomeGroup = new TreeMap<String, LivingGroup>();
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
						groupNameToBiomeGroup.put(groupName, new LivingGroup(groupName, configName, contents));
					}
				}
			}

            JsonElement groupElement = endObject.get(GROUP_KEY);
            if (groupElement != null && groupElement.isJsonObject()) {
            	//Hack to port LivningGroupContents to LivingHandler
            	LivingHandlerSaveObject.Serializer.livingGroupContents.clear(); 
                JsonObject biomeGroupObject = groupElement.getAsJsonObject();
                saveObject.configNameToLivingGroups = Optional.of(new TreeMap<String, TreeMap<String, LivingGroup>>());
                for (Entry<String, JsonElement> outerEntry : biomeGroupObject.entrySet()) {
                    String configName = outerEntry.getKey();
                    TreeMap<String, LivingGroup> groupNameToBiomeGroup = saveObject.configNameToLivingGroups.get().get(
                            configName);
                    if (groupNameToBiomeGroup == null) {
                        groupNameToBiomeGroup = new TreeMap<String, LivingGroup>();
                        saveObject.configNameToLivingGroups.get().put(configName, groupNameToBiomeGroup);
                    }

                    JsonObject innerObject = GsonHelper.getAsJsonObject(outerEntry.getValue());
                    for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
                        String groupName = innerEntry.getKey();

                        JsonArray contentsArray = GsonHelper.getMemberOrDefault(
                                GsonHelper.getAsJsonObject(innerEntry.getValue()), CONTENTS_KEY, new JsonArray());
                        ArrayList<String> contents = new ArrayList<String>();
                        for (JsonElement jsonElement : contentsArray) {
                            contents.add(jsonElement.getAsString());
                        }
						if (fileVersion.equals("1.0")) {
							LivingHandlerSaveObject.Serializer.livingGroupContents.put(groupName, contents);
						}
                    }
                }
            }
            return saveObject;
        }
    }
}
