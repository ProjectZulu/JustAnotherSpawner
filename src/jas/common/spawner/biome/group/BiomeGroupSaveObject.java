package jas.common.spawner.biome.group;

import jas.common.DefaultProps;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.annotations.SerializedName;

public class BiomeGroupSaveObject {
    @SerializedName("BiomeMappings")
    public final TreeMap<String, String> biomeMappings; // Use TreeMaps instad of Hashmaps EVERYWHERE to gaurantee sort
                                                        // by keys
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
        LinkedHashMap<String, String> sortedMap = new LinkedHashMap<String, String>();

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
}