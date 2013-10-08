package jas.common.config;

import jas.common.DefaultProps;
import jas.common.WorldProperties;

import java.io.File;

import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class BiomeGroupConfiguration extends Configuration {
    public static final String BiomeConfigName = "BiomeGroups.cfg";
    public static final String BiomeConfigCategory = "biomegroups";
    public static final String GroupListCategory = BiomeConfigCategory + ".biomelists";
    public static final String AttributeListCategory = BiomeConfigCategory + ".attributebiomelists";

    public BiomeGroupConfiguration(File configDirectory, WorldProperties worldProperties) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName + "/"
                + BiomeConfigName));
    }

    public ConfigCategory getAttributesCategory() {
        return this.getCategory(AttributeListCategory);
    }

    public ConfigCategory getGroupsCategory() {
        return this.getCategory(GroupListCategory);
    }

    public Property getBiomeMapping(String packageName, String biomeMapping) {
        this.getCategory(BiomeConfigCategory + ".packagenamemappings")
                .setComment(
                        "Custom Name Mapping for Unique Biome Package Name. Mapping is used to add biomes to groups. Each must be unique or biome will be unreachable. Case sensitive.");
        return this.get(BiomeConfigCategory + ".packagenamemappings", packageName, biomeMapping);
    }

    public Property getBiomeGroupBiomes(String biomeGroupID, String defautlGroupString) {
        return this
                .get(GroupListCategory + "." + biomeGroupID, "BiomeList", defautlGroupString,
                        "List of All Biomes Contained in this Group. Format is package mapping (see packagenamemappings) seperated by commas");
    }

    /**
     * @Deprecated Should no longer be needed
     */
    @Deprecated
    public Property getBiomeGroupList(String groupNames) {
        return this.get(BiomeConfigCategory + ".customgroups", "Custom Group Names", groupNames,
                "Custom Group Names. Seperated by Commas. Edit this to add/remove groups");
    }

    /**
     * @Deprecated Should no longer be needed
     */
    @Deprecated
    public Property getAttributeList(String attributeNames) {
        return get(BiomeConfigCategory + ".customattributes", "Custom Group Names", attributeNames,
                "Custom Attribute Names. Seperated by Commas. Edit this to add/remove groups");
    }

    public Property getAtrributeBiomes(String attributeID, String defautlGroupString) {
        return get(
                AttributeListCategory + attributeID,
                "AtrributeList",
                defautlGroupString,
                "List of All Biomes Contained in this Atrribute. Format is package mapping (see packagenamemappings) seperated by commas");
    }

    public Property getEntityGroupList(String saveFormat, String groupContents) {
        int last = saveFormat.lastIndexOf(":");
        return this.get(saveFormat.substring(0, last), saveFormat.substring(last + 1, saveFormat.length()),
                groupContents);
    }

    public static String defaultAttributeGroupCategory(String groupID) {
        String[] parts = groupID.split("\\.");
        if (parts.length > 1) {
            return BiomeGroupConfiguration.AttributeListCategory + Configuration.CATEGORY_SPLITTER + parts[0] + ":"
                    + groupID;
        } else {
            return BiomeGroupConfiguration.AttributeListCategory + ":" + groupID;
        }
    }

    public static String defaultGroupCategory(String groupID) {
        String[] parts = groupID.split("\\.");
        if (parts.length > 1) {
            return BiomeGroupConfiguration.GroupListCategory + Configuration.CATEGORY_SPLITTER + parts[0] + ":"
                    + groupID;
        } else {
            return BiomeGroupConfiguration.GroupListCategory + ":" + groupID;
        }
    }
}
