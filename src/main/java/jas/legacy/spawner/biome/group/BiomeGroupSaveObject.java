package jas.legacy.spawner.biome.group;

import jas.legacy.DefaultProps;
import jas.legacy.GsonHelper;
import jas.legacy.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import com.google.gson.annotations.SerializedName;

public class BiomeGroupSaveObject {
    public final String fileVersion = "1.0";
    public final TreeMap<String, String> biomeMappings; // Use TreeMaps instead of hashmaps EVERYWHERE to sort by keys
    private Optional<TreeMap<String, TreeMap<String, BiomeGroup>>> configNameToAttributeGroups;
    private Optional<TreeMap<String, TreeMap<String, BiomeGroup>>> configNameToBiomeGroups;

    /* For Serialization Only */
    private BiomeGroupSaveObject() {
        this.biomeMappings = new TreeMap<String, String>();
        this.configNameToAttributeGroups = Optional.absent();
        this.configNameToBiomeGroups = Optional.absent();
    }

    public BiomeGroupSaveObject(Map<String, String> biomeMappings, Collection<BiomeGroup> attributeGroups,
            Collection<BiomeGroup> biomeGroups) {
        this.biomeMappings = new TreeMap<String, String>(biomeMappings);
        this.configNameToBiomeGroups = Optional.of(new TreeMap<String, TreeMap<String, BiomeGroup>>());
        this.configNameToAttributeGroups = Optional.of(new TreeMap<String, TreeMap<String, BiomeGroup>>());
        for (BiomeGroup group : attributeGroups) {
            getOrCreate(configNameToAttributeGroups.get(), group.configName).put(group.groupID, group);
        }
        for (BiomeGroup group : biomeGroups) {
            getOrCreate(configNameToBiomeGroups.get(), group.configName).put(group.groupID, group);
        }
    }

    public Optional<TreeMap<String, TreeMap<String, BiomeGroup>>> getConfigNameToAttributeGroups() {
        return configNameToAttributeGroups;
    }

    public Optional<TreeMap<String, TreeMap<String, BiomeGroup>>> getConfigNameToBiomeGroups() {
        return configNameToBiomeGroups;
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
        public final String FILE_VERSION_KEY = "FILE_VERSION";
        public final String FILE_VERSION = "1.0";
        public final String BIOME_MAPPINGS = "Biome Mappings";
        public final String ATTRIBUTE_GROUPS = "Attribute Groups";
        public final String BIOME_GROUPS = "Biome Groups";
        public final String CONTENTS_KEY = "contents";

        @Override
        public JsonElement serialize(BiomeGroupSaveObject saveObject, Type type, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, saveObject.fileVersion);

            JsonObject mappingObject = new JsonObject();
            for (Entry<String, String> entry : saveObject.biomeMappings.entrySet()) {
                mappingObject.addProperty(entry.getKey(), entry.getValue());
            }
            endObject.add(BIOME_MAPPINGS, mappingObject);

            JsonObject attributeObject = new JsonObject();
            for (Entry<String, TreeMap<String, BiomeGroup>> outerEntry : saveObject.configNameToAttributeGroups.get()
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
                    contentsObject.add(CONTENTS_KEY, contents);
                    biomeObject.add(groupName, contentsObject);
                }
                attributeObject.add(configName, biomeObject);
            }
            endObject.add(ATTRIBUTE_GROUPS, attributeObject);

            JsonObject biomeGroupObject = new JsonObject();
            for (Entry<String, TreeMap<String, BiomeGroup>> outerEntry : saveObject.configNameToBiomeGroups.get()
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
                    contentsObject.add(CONTENTS_KEY, contents);
                    biomeObject.add(groupName, contentsObject);
                }
                biomeGroupObject.add(configName, biomeObject);
            }
            endObject.add(BIOME_GROUPS, biomeGroupObject);
            return endObject;
        }

        @Override
        public BiomeGroupSaveObject deserialize(JsonElement object, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            BiomeGroupSaveObject saveObject = new BiomeGroupSaveObject();
            JsonObject endObject = object.getAsJsonObject();
            String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);

            JsonObject mappingsObject = GsonHelper.getMemberOrDefault(endObject, BIOME_MAPPINGS, new JsonObject());
            for (Entry<String, JsonElement> entry : mappingsObject.entrySet()) {
                saveObject.biomeMappings.put(entry.getKey(), entry.getValue().getAsString());
            }

            JsonElement attrElement = endObject.get(ATTRIBUTE_GROUPS);
            if (attrElement != null && attrElement.isJsonObject()) {
                saveObject.configNameToAttributeGroups = Optional
                        .of(new TreeMap<String, TreeMap<String, BiomeGroup>>());
                JsonObject attributeObject = attrElement.getAsJsonObject();
                for (Entry<String, JsonElement> outerEntry : attributeObject.entrySet()) {
                    String configName = outerEntry.getKey();
                    TreeMap<String, BiomeGroup> groupNameToBiomeGroup = saveObject.configNameToAttributeGroups.get()
                            .get(configName);
                    if (groupNameToBiomeGroup == null) {
                        groupNameToBiomeGroup = new TreeMap<String, BiomeGroup>();
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
                        groupNameToBiomeGroup.put(groupName, new BiomeGroup(groupName, configName, contents));
                    }
                }
            }
            JsonElement biomeElement = endObject.get(BIOME_GROUPS);
            if (biomeElement != null && biomeElement.isJsonObject()) {
                saveObject.configNameToBiomeGroups = Optional.of(new TreeMap<String, TreeMap<String, BiomeGroup>>());

                JsonObject biomeGroupObject = biomeElement.getAsJsonObject();
                for (Entry<String, JsonElement> outerEntry : biomeGroupObject.entrySet()) {
                    String configName = outerEntry.getKey();
                    TreeMap<String, BiomeGroup> groupNameToBiomeGroup = saveObject.configNameToBiomeGroups.get().get(
                            configName);
                    if (groupNameToBiomeGroup == null) {
                        groupNameToBiomeGroup = new TreeMap<String, BiomeGroup>();
                        saveObject.configNameToBiomeGroups.get().put(configName, groupNameToBiomeGroup);
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
                        groupNameToBiomeGroup.put(groupName, new BiomeGroup(groupName, configName, contents));
                    }
                }
            }
            return saveObject;
        }
    }
}