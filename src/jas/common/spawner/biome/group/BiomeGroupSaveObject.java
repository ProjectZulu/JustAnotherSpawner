package jas.common.spawner.biome.group;

import jas.common.DefaultProps;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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

public class BiomeGroupSaveObject {
    public final String fileVersion = "1.0";
    @SerializedName("BiomeMappings")
    public final TreeMap<String, String> biomeMappings; // Use TreeMaps instead of hashmaps EVERYWHERE to sort by keys
    @SerializedName("AttributeGroups")
    public TreeMap<String, TreeMap<String, BiomeGroup>> configNameToAttributeGroups;
    @SerializedName("BiomeGroups")
    public TreeMap<String, TreeMap<String, BiomeGroup>> configNameToBiomeGroups;

    public BiomeGroupSaveObject() {
        this.biomeMappings = new TreeMap<String, String>();
        this.configNameToAttributeGroups = null;
        this.configNameToBiomeGroups = null;
    }

    public BiomeGroupSaveObject(Map<String, String> biomeMappings, Collection<BiomeGroup> attributeGroups,
            Collection<BiomeGroup> biomeGroups) {
        this.biomeMappings = new TreeMap<String, String>(biomeMappings);
        this.configNameToBiomeGroups = new TreeMap<String, TreeMap<String, BiomeGroup>>();
        this.configNameToAttributeGroups = new TreeMap<String, TreeMap<String, BiomeGroup>>();
        for (BiomeGroup group : attributeGroups) {
            getOrCreate(configNameToAttributeGroups, group.configName).put(group.groupID, group);
        }
        for (BiomeGroup group : biomeGroups) {
            getOrCreate(configNameToBiomeGroups, group.configName).put(group.groupID, group);
        }
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + "BiomeGroups.cfg");
    }

    private TreeMap<String, BiomeGroup> getOrCreate(TreeMap<String, TreeMap<String, BiomeGroup>> map, String key) {
        TreeMap<String, BiomeGroup> group = map.get(key);
        if (group == null) {
            group = new TreeMap<String, BiomeGroup>();
            map.put(key, group);
        }
        return group;
    }

    public static class BiomeGroupSaveObjectSerializer implements JsonSerializer<BiomeGroupSaveObject>,
            JsonDeserializer<BiomeGroupSaveObject> {

        @Override
        public JsonElement serialize(BiomeGroupSaveObject saveObject, Type type, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty("File Version", saveObject.fileVersion);

            JsonObject mappingObject = new JsonObject();
            for (Entry<String, String> entry : saveObject.biomeMappings.entrySet()) {
                mappingObject.addProperty(entry.getKey(), entry.getValue());
            }
            endObject.add("BiomeMappings", mappingObject);

            JsonObject attributeObject = new JsonObject();
            for (Entry<String, TreeMap<String, BiomeGroup>> outerEntry : saveObject.configNameToAttributeGroups
                    .entrySet()) {
                String configName = outerEntry.getKey();
                JsonObject biomeObject = new JsonObject();
                for (Entry<String, BiomeGroup> innerEntry : outerEntry.getValue().entrySet()) {
                    String groupName = innerEntry.getKey();
                    BiomeGroup group = innerEntry.getValue();
                    JsonArray contents = new JsonArray();
                    for (String content : group.getContents()) {
                        contents.add(new JsonPrimitive(content));
                    }
                    JsonObject contentsObject = new JsonObject();
                    contentsObject.add("contents", contents);
                    biomeObject.add(groupName, contentsObject);
                }
                attributeObject.add(configName, biomeObject);
            }
            endObject.add("AttributeGroups", attributeObject);

            JsonObject biomeGroupObject = new JsonObject();
            for (Entry<String, TreeMap<String, BiomeGroup>> outerEntry : saveObject.configNameToBiomeGroups.entrySet()) {
                String configName = outerEntry.getKey();
                JsonObject biomeObject = new JsonObject();
                for (Entry<String, BiomeGroup> innerEntry : outerEntry.getValue().entrySet()) {
                    String groupName = innerEntry.getKey();
                    BiomeGroup group = innerEntry.getValue();
                    JsonArray contents = new JsonArray();
                    for (String content : group.getContents()) {
                        contents.add(new JsonPrimitive(content));
                    }
                    JsonObject contentsObject = new JsonObject();
                    contentsObject.add("contents", contents);
                    biomeObject.add(groupName, contentsObject);
                }
                biomeGroupObject.add(configName, biomeObject);
            }
            endObject.add("BiomeGroups", biomeGroupObject);
            return endObject;
        }

        @Override
        public BiomeGroupSaveObject deserialize(JsonElement object, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            BiomeGroupSaveObject saveObject = new BiomeGroupSaveObject();
            JsonObject endObject = object.getAsJsonObject();
            // JsonElement fileVersion = endObject.get("File Version");

            JsonObject mappingsObject = endObject.get("BiomeMappings").getAsJsonObject();
            for (Entry<String, JsonElement> entry : mappingsObject.entrySet()) {
                saveObject.biomeMappings.put(entry.getKey(), entry.getValue().getAsString());
            }

            JsonObject attributeObject = endObject.get("AttributeGroups").getAsJsonObject();
            for (Entry<String, JsonElement> outerEntry : attributeObject.entrySet()) {
                String configName = outerEntry.getKey();
                TreeMap<String, BiomeGroup> groupNameToBiomeGroup = saveObject.configNameToAttributeGroups
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
                    groupNameToBiomeGroup.put(groupName, new BiomeGroup(groupName, configName, contents));
                }
            }

            JsonObject biomeGroupObject = endObject.get("BiomeGroups").getAsJsonObject();
            for (Entry<String, JsonElement> outerEntry : biomeGroupObject.entrySet()) {
                String configName = outerEntry.getKey();
                TreeMap<String, BiomeGroup> groupNameToBiomeGroup = saveObject.configNameToBiomeGroups.get(configName);
                if (groupNameToBiomeGroup == null) {
                    groupNameToBiomeGroup = new TreeMap<>();
                    saveObject.configNameToBiomeGroups.put(configName, groupNameToBiomeGroup);
                }
                JsonObject innerObject = outerEntry.getValue().getAsJsonObject();
                for (Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
                    String groupName = innerEntry.getKey();
                    JsonArray contentsArray = innerEntry.getValue().getAsJsonObject().get("contents").getAsJsonArray();
                    ArrayList<String> contents = new ArrayList<String>();
                    for (JsonElement jsonElement : contentsArray) {
                        contents.add(jsonElement.getAsString());
                    }
                    groupNameToBiomeGroup.put(groupName, new BiomeGroup(groupName, configName, contents));
                }
            }
            return saveObject;
        }
    }
}