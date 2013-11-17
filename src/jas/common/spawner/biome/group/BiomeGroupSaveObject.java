package jas.common.spawner.biome.group;

import jas.common.DefaultProps;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class BiomeGroupSaveObject {
    @SerializedName("BiomeMappings")
    public final Map<String, String> biomeMappings;
    @SerializedName("AttributeGroups")
    public HashMap<String, HashMap<String, BiomeGroup>> configNameToAttributeGroups;
    @SerializedName("BiomeGroups")
    public HashMap<String, HashMap<String, BiomeGroup>> configNameToBiomeGroups;

    public BiomeGroupSaveObject() {
        this.biomeMappings = new HashMap<String, String>();
        this.configNameToAttributeGroups = null;
        this.configNameToBiomeGroups = null;
    }

    public BiomeGroupSaveObject(Map<String, String> biomeMappings, Collection<BiomeGroup> attributeGroups,
            Collection<BiomeGroup> biomeGroups) {
        this.biomeMappings = biomeMappings;
        this.configNameToBiomeGroups = new HashMap<String, HashMap<String, BiomeGroup>>();
        this.configNameToAttributeGroups = new HashMap<String, HashMap<String, BiomeGroup>>();
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

    private HashMap<String, BiomeGroup> getOrCreate(HashMap<String, HashMap<String, BiomeGroup>> map, String key) {
        HashMap<String, BiomeGroup> group = map.get(key);
        if (group == null) {
            group = new HashMap<String, BiomeGroup>();
            map.put(key, group);
        }
        return group;
    }
}