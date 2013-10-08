package jas.common.config;

import jas.common.DefaultProps;
import jas.common.WorldProperties;

import java.io.File;

import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class LivingGroupConfiguration extends Configuration {

    public static final String ConfigName = "LivingGroups.cfg";
    public static final String ConfigCategory = "livinggroups";
    public static final String GroupListCategory = ConfigCategory + ".livinggroups";
    public static final String AttributeListCategory = ConfigCategory + ".attributegroups";
    public static final String EntityMappingCategory = ConfigCategory + ".customentityname";

    public LivingGroupConfiguration(File configDirectory, WorldProperties worldProperties) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName + "/" + ConfigName));
    }

    public Property getEntityMapping(String nameKey, String nameValue) {
        getCategory(EntityMappingCategory).setComment("Names must be unique and is case sensitive.");
        return get(EntityMappingCategory, nameKey, nameValue);
    }

    public ConfigCategory getEntityAttributes() {
        return this.getCategory(AttributeListCategory);
    }

    public ConfigCategory getEntityGroups() {
        return this.getCategory(GroupListCategory);
    }

    public Property getEntityGroupList(String saveFormat, String groupContents) {
        int last = saveFormat.lastIndexOf(":");
        return this.get(saveFormat.substring(0, last), saveFormat.substring(last + 1, saveFormat.length()),
                groupContents);
    }

    public static String defaultGroupCategory(String groupID) {
        String[] parts = groupID.split("\\.");
        if (parts.length > 1) {
            return LivingGroupConfiguration.GroupListCategory + Configuration.CATEGORY_SPLITTER + parts[0] + ":"
                    + groupID;
        } else {
            return LivingGroupConfiguration.GroupListCategory + ":" + groupID;
        }
    }
}
