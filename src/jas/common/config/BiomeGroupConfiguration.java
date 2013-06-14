package jas.common.config;

import jas.common.DefaultProps;
import jas.common.Properties;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class BiomeGroupConfiguration extends Configuration {

    public static final String BiomeConfigName = "BiomeGroups.cfg";
    public static final String BiomeConfigCategory = "biomegroups";

    public BiomeGroupConfiguration(File configDirectory) {
        super(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + Properties.saveName + "/" + BiomeConfigName));
    }

    public Property getBiomeMapping(String packageName, String biomeName) {
        this.getCategory(BiomeConfigCategory + ".packagenamemappings")
                .setComment(
                        "Custom Name Mapping for Unique Biome Package Name. Mapping is used to add biomes to groups. Each must be unique or biome will be unreachable. Case sensitive.");
        return this.get(BiomeConfigCategory + ".packagenamemappings", packageName, biomeName);
    }

    public Property getBiomeGroupBiomes(String biomeGroupID, String defautlGroupString) {
        return this
                .get(BiomeConfigCategory + ".BiomeLists." + biomeGroupID, "BiomeList", defautlGroupString,
                        "List of All Biomes Contained in this Group. Format is package mapping (see packagenamemappings) seperated by commas");
    }

    public Property getBiomeGroupList(String groupNames) {
        return this.get(BiomeConfigCategory + ".customgroups", "Custom Group Names", groupNames,
                "Custom Group Names. Seperated by Commas. Edit this to add/remove groups");
    }

    public Property getAttributeList(String attributeNames) {
        return get(BiomeConfigCategory + ".customattributes", "Custom Group Names", attributeNames,
                "Custom Attribute Names. Seperated by Commas. Edit this to add/remove groups");
    }

    public Property getAtrributeBiomes(String attributeID, String defautlGroupString) {
        return get(
                BiomeConfigCategory + ".attributebiomelists." + attributeID,
                "AtrributeList",
                defautlGroupString,
                "List of All Biomes Contained in this Atrribute. Format is package mapping (see packagenamemappings) seperated by commas");
    }
}
