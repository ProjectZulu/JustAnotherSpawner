package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

public class LivingGroupSaveObject {
    @SerializedName("File Version")
    public final String fileVersion = "1.0";
    @SerializedName("CustomEntityNames")
    public final TreeMap<String, String> fmlToJASName;
    @SerializedName("AttributeGroups")
    public TreeMap<String, TreeMap<String, LivingGroup>> configNameToAttributeGroups;
    @SerializedName("EntityGroups")
    public TreeMap<String, TreeMap<String, LivingGroup>> configNameToLivingGroups;

    /* For Serialization Only */
    public LivingGroupSaveObject() {
        this.fmlToJASName = new TreeMap<String, String>();
        this.configNameToAttributeGroups = null;
        this.configNameToLivingGroups = null;
    }

    public LivingGroupSaveObject(Map<Class<? extends EntityLiving>, String> classNamesToJASNames,
            Collection<LivingGroup> attributeGroups, Collection<LivingGroup> LivingGroups) {
        this.fmlToJASName = new TreeMap<String, String>();
        for (Entry<Class<? extends EntityLiving>, String> entry : classNamesToJASNames.entrySet()) {
            String fmlName = (String) EntityList.classToStringMapping.get(entry.getKey());
            fmlToJASName.put(fmlName, entry.getValue());
        }
        this.configNameToLivingGroups = new TreeMap<String, TreeMap<String, LivingGroup>>();
        this.configNameToAttributeGroups = new TreeMap<String, TreeMap<String, LivingGroup>>();
        for (LivingGroup group : attributeGroups) {
            getOrCreate(configNameToAttributeGroups, group.configName).put(group.groupID, group);
        }
        for (LivingGroup group : LivingGroups) {
            getOrCreate(configNameToLivingGroups, group.configName).put(group.groupID, group);
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
        public final static String VERSION_KEY = "File Version";
        public final static String ENTITY_MAP_KEY = "CustomEntityNames";
        public final static String ATTRIBUTE_KEY = "AttributeGroups";
        public final static String GROUP_KEY = "EntityGroups";

        @Override
        public JsonElement serialize(LivingGroupSaveObject saveObject, Type type, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(VERSION_KEY, saveObject.fileVersion);

            JsonObject mappingObject = new JsonObject();
            for (Entry<String, String> entry : saveObject.fmlToJASName.entrySet()) {
                mappingObject.addProperty(entry.getKey(), entry.getValue());
            }
            endObject.add(ENTITY_MAP_KEY, mappingObject);

            JsonObject attributeObject = new JsonObject();
            for (Entry<String, TreeMap<String, LivingGroup>> outerEntry : saveObject.configNameToAttributeGroups
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
                    contentsObject.add("contents", contents);
                    biomeObject.add(groupName, contentsObject);
                }
                attributeObject.add(configName, biomeObject);
            }
            endObject.add(ATTRIBUTE_KEY, attributeObject);

            JsonObject biomeGroupObject = new JsonObject();
            for (Entry<String, TreeMap<String, LivingGroup>> outerEntry : saveObject.configNameToLivingGroups
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
                    contentsObject.add("contents", contents);
                    biomeObject.add(groupName, contentsObject);
                }
                biomeGroupObject.add(configName, biomeObject);
            }
            endObject.add(GROUP_KEY, biomeGroupObject);
            return endObject;
        }

        @Override
        public LivingGroupSaveObject deserialize(JsonElement object, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            LivingGroupSaveObject saveObject = new LivingGroupSaveObject();
            JsonObject endObject = object.getAsJsonObject();
            // JsonElement fileVersion = endObject.get("File Version");

            JsonObject mappingsObject = endObject.get("BiomeMappings").getAsJsonObject();
            for (Entry<String, JsonElement> entry : mappingsObject.entrySet()) {
                saveObject.fmlToJASName.put(entry.getKey(), entry.getValue().getAsString());
            }

            JsonObject attributeObject = endObject.get("AttributeGroups").getAsJsonObject();
            for (Entry<String, JsonElement> outerEntry : attributeObject.entrySet()) {
                String configName = outerEntry.getKey();
                TreeMap<String, LivingGroup> groupNameToBiomeGroup = saveObject.configNameToAttributeGroups
                        .get(configName);
                if (groupNameToBiomeGroup == null) {
                    groupNameToBiomeGroup = new TreeMap<>();
                    saveObject.configNameToAttributeGroups.put(configName, groupNameToBiomeGroup);
                }
                JsonObject innerObject = outerEntry.getValue().getAsJsonObject();
                for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
                    String groupName = innerEntry.getKey();
                    JsonArray contentsArray = innerEntry.getValue().getAsJsonObject().get("contents").getAsJsonArray();
                    ArrayList<String> contents = new ArrayList<String>();
                    for (JsonElement jsonElement : contentsArray) {
                        contents.add(jsonElement.getAsString());
                    }
                    groupNameToBiomeGroup.put(groupName, new LivingGroup(groupName, configName, contents));
                }
            }

            JsonObject biomeGroupObject = endObject.get("BiomeGroups").getAsJsonObject();
            for (Entry<String, JsonElement> outerEntry : biomeGroupObject.entrySet()) {
                String configName = outerEntry.getKey();
                TreeMap<String, LivingGroup> groupNameToBiomeGroup = saveObject.configNameToLivingGroups
                        .get(configName);
                if (groupNameToBiomeGroup == null) {
                    groupNameToBiomeGroup = new TreeMap<>();
                    saveObject.configNameToLivingGroups.put(configName, groupNameToBiomeGroup);
                }
                JsonObject innerObject = outerEntry.getValue().getAsJsonObject();
                for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
                    String groupName = innerEntry.getKey();
                    JsonArray contentsArray = innerEntry.getValue().getAsJsonObject().get("contents").getAsJsonArray();
                    ArrayList<String> contents = new ArrayList<String>();
                    for (JsonElement jsonElement : contentsArray) {
                        contents.add(jsonElement.getAsString());
                    }
                    groupNameToBiomeGroup.put(groupName, new LivingGroup(groupName, configName, contents));
                }
            }
            return saveObject;
        }
    }
}
