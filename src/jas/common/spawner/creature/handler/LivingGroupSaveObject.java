package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import com.google.gson.annotations.SerializedName;

public class LivingGroupSaveObject {
    @SerializedName("CustomEntityNames")
    public final TreeMap<String, String> fmlToJASName;
    @SerializedName("AttributeGroups")
    public TreeMap<String, TreeMap<String, LivingGroup>> configNameToAttributeGroups;
    @SerializedName("EntityGroups")
    public TreeMap<String, TreeMap<String, LivingGroup>> configNameToLivingGroups;

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
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + "LivingGroupsGSON.cfg");
    }

    private TreeMap<String, LivingGroup> getOrCreate(TreeMap<String, TreeMap<String, LivingGroup>> map, String key) {
        TreeMap<String, LivingGroup> group = map.get(key);
        if (group == null) {
            group = new TreeMap<String, LivingGroup>();
            map.put(key, group);
        }
        return group;
    }
}
