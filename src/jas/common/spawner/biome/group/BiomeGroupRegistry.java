package jas.common.spawner.biome.group;

import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.Properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;

public enum BiomeGroupRegistry {
    INSTANCE;

    /** Group Identifier to Group Instance Regsitry */
    private final HashMap<String, BiomeGroup> iDToGroup = new HashMap<String, BiomeGroup>();

    /** Reverse Look-up Map to Get All Groups a Particular Biome is In */
    private final ListMultimap<String, String> packgNameToGroupIDList = ArrayListMultimap.create();

    /** Cusom Biome Names: Mappings For CustomBiomeNames to PackageNames used to read from configuration */
    private final HashMap<String, String> biomeMappingToPckg = new HashMap<String, String>();
    /** Cusom Biome Names: Mappings For PackageNames to CustomBiomeNames used to write to configuration */
    private final HashMap<String, String> biomePckgToMapping = new HashMap<String, String>();

    /*
     * Reverse Lookup Used to Filter Used to get a biome without needed to run through entire array. Primary use is to
     * filter submitted BiomeNames to Ensure they correspond to actual Biomes.
     */
    public ListMultimap<String, Integer> pckgNameToBiomeID = ArrayListMultimap.create();

    public static final String BiomeConfigName = "BiomeGroups.cfg";
    public static final String BiomeConfigCategory = "BiomeGroups";

    public static final String BiomeMappingsCategory = BiomeConfigCategory + ".packagenamemappings";
    public static final String BiomeMappingsComment = "Custom Name Mapping for Unique Biome Package Name. Mapping is used to add biomes to groups. Each must be unique or biome will be unreachable. Case sensitive.";

    public static final String BiomeListCategory = BiomeConfigCategory + ".BiomeLists";
    public static final String BiomeListProperty = "BiomeList";
    public static final String BiomeListComment = "List of All Biomes Contained in this Group. Format is package mapping (see packagenamemappings) seperated by commas.";

    public static final String BiomeCustomCategory = BiomeConfigCategory + ".customgroups";
    public static final String BiomeCustomProperty = "Custom Group Names";
    public static final String BiomeCustomComment = "Custom Group Names. Seperated by Commas. Edit this to add/remove groups";

    /**
     * Should Only Be Used to Register BiomeGroups with their finished
     */
    public void registerGroup(BiomeGroup group) {
        JASLog.info("Registering BiomeGroup %s", group.groupID);
        iDToGroup.put(group.groupID, group);
        for (String biomeName : group.pckgNames) {
            JASLog.info("BiomeGroup %s contains PckgBiome %s", group.groupID, biomeName);
        }
        for (String pckgName : group.pckgNames) {
            packgNameToGroupIDList.get(pckgName).add(group.groupID);
        }
    }

    public BiomeGroup getBiomeGroup(String groupID) {
        return iDToGroup.get(groupID);
    }

    /**
     * Return Immutable List of all BiomeGroups
     * 
     * @param groupID
     * @return
     */
    public Collection<BiomeGroup> getBiomeGroups() {
        return Collections.unmodifiableCollection(iDToGroup.values());
    }

    /**
     * 
     * @return
     */
    public ImmutableMultimap<String, String> getPackgNameToGroupIDList() {
        return ImmutableMultimap.copyOf(packgNameToGroupIDList);
    }

    /**
     * Biome Group Class
     * 
     */
    public static class BiomeGroup {
        public final String groupID;
        private final List<String> pckgNames = new ArrayList<String>();

        public BiomeGroup(String groupID) {
            this.groupID = groupID.toLowerCase();
        }

        public ImmutableList<String> getBiomeNames() {
            return ImmutableList.copyOf(pckgNames);
        }

        @Override
        public boolean equals(Object paramObject) {
            if (paramObject == null || !(paramObject instanceof BiomeGroup)) {
                return false;
            }
            return ((BiomeGroup) paramObject).groupID.equals(groupID);
        }

        @Override
        public int hashCode() {
            return groupID.hashCode();
        }
    }

    public void createBiomeGroups(File configDirectory) {
        Configuration worldConfig = createBiomeGroupConfiguration(configDirectory);
        worldConfig.load();

        /* Create Package Name Mappings */
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            String packageName = BiomeHelper.getPackageName(biome);

            Property nameMapping = getPackageMappingProperty(worldConfig, packageName, biome.biomeName);

            biomeMappingToPckg.put(nameMapping.getString(), packageName);
            biomePckgToMapping.put(packageName, nameMapping.getString());
        }

        ArrayList<BiomeGroup> biomeGroups = new ArrayList<BiomeGroupRegistry.BiomeGroup>();

        /* Map PackageBiomeName to ID for default groups */
        ListMultimap<String, Integer> groupIDToBiomeID = ArrayListMultimap.create();
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            groupIDToBiomeID.put(BiomeHelper.getPackageName(biome), biome.biomeID);
        }

        /* For Each Unique PackageName Create a Group containing all biomes of that name */
        for (String pckgName : groupIDToBiomeID.keySet()) {
            List<Integer> biomeIDs = groupIDToBiomeID.get(pckgName);
            BiomeGroup group = new BiomeGroup(BiomeGenBase.biomeList[biomeIDs.get(0)].biomeName);
            for (Integer integer : biomeIDs) {
                group.pckgNames.add(BiomeHelper.getPackageName(BiomeGenBase.biomeList[integer]));
            }
            biomeGroups.add(group);
        }

        /* Get Custom BiomeGroupNames */

        Property customGroupProp = getCustomGroupProperty(worldConfig, "");
        String[] resultgroups = customGroupProp.getString().split(",");
        for (String groupName : resultgroups) {
            biomeGroups.add(new BiomeGroup(groupName));
        }

        /* For Every Biome Group; Filter BiomeList Through Configuration */
        for (BiomeGroup biomeGroup : biomeGroups) {
            getGroupSpawnList(worldConfig, biomeGroup);
            if (biomeGroup.pckgNames.size() > 0) {
                registerGroup(biomeGroup);
            }
        }
        worldConfig.save();
    }

    /**
     * Helper method to ensure loading and saving is done through identical properties
     */
    private Configuration createBiomeGroupConfiguration(File configDirectory) {
        return new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + Properties.saveName + "/"
                + BiomeConfigName));
    }

    /**
     * Helper method to ensure loading and saving is done through identical properties
     * 
     * TODO: Wrap these Inside a Custom extension of Forge Configuration class
     */
    private Property getPackageMappingProperty(Configuration config, String packageName, String biomeName) {
        config.getCategory(BiomeMappingsCategory).setComment(BiomeMappingsComment);
        return config.get(BiomeMappingsCategory, packageName, biomeName);
    }

    /**
     * Helper method to ensure loading and saving is done through identical properties
     */
    private Property getCustomGroupProperty(Configuration config, String groupNames) {
        return config.get(BiomeCustomCategory, BiomeCustomProperty, groupNames, BiomeCustomComment);
    }

    /**
     * Helper method to ensure loading and saving is done through identical properties
     */
    private Property getBiomeListProperty(Configuration config, String biomeGroupID, String defautlGroupString) {
        return config.get(BiomeListCategory + biomeGroupID, BiomeListProperty, defautlGroupString, BiomeListComment);
    }

    /**
     * Gets the List of All BiomeNames from Configuration Files. They are stored as a single string seperated by Commas.
     * 
     * @param config
     * @param group
     * @return
     */
    private void getGroupSpawnList(Configuration config, BiomeGroup group) {
        Property resultProp = getBiomeListProperty(config, group.groupID, groupBiomesToString(group));

        String resultGroupString = resultProp.getString();
        String[] resultgroups = resultGroupString.split(",");
        Set<String> biomeNames = new HashSet<String>();
        for (String name : resultgroups) {
            if (name.equals("")) {
                continue;
            }
            String pckgName = biomeMappingToPckg.get(name);
            if (pckgName == null) {
                JASLog.severe("Error while Parsing %s BiomeGroup. BiomeEntry %s is not a valid biome mapping",
                        group.groupID, name);
                continue;
            }
            biomeNames.add(pckgName);
        }
        group.pckgNames.clear();
        group.pckgNames.addAll(biomeNames);
    }

    private String groupBiomesToString(BiomeGroup group) {
        String defautlGroupString = "";
        Iterator<String> iterator = group.pckgNames.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            defautlGroupString = defautlGroupString.concat(biomePckgToMapping.get(string));
            if (iterator.hasNext()) {
                defautlGroupString = defautlGroupString.concat(",");
            }
        }
        return defautlGroupString;
    }

    /**
     * Used to save the currently loaded settings into the Configuration Files
     * 
     * If config settings are already present, they will be overwritten
     */
    public void saveCurrentToConfig(File configDirectory) {
        Configuration biomeConfig = createBiomeGroupConfiguration(configDirectory);
        biomeConfig.load();
        saveMappingsToConfig(biomeConfig);
        saveCustomGroupsToConfig(biomeConfig);
        biomeConfig.save();
    }

    private void saveMappingsToConfig(Configuration config) {
        for (Entry<String, String> mappingEntry : biomePckgToMapping.entrySet()) {
            Property mappingProp = getPackageMappingProperty(config, mappingEntry.getKey(), mappingEntry.getValue());
            mappingProp.set(mappingEntry.getValue());
        }
    }

    private void saveCustomGroupsToConfig(Configuration config) {
        /* Save Group Names to Config */
        String biomeNameString = "";
        Iterator<String> iterator = iDToGroup.keySet().iterator();
        while (iterator.hasNext()) {
            String biomeID = iterator.next();
            boolean isDefaultGroup = false;
            for (BiomeGenBase biome : BiomeGenBase.biomeList) {
                if (biomeID.equalsIgnoreCase(biome.biomeName)) {
                    isDefaultGroup = true;
                    break;
                }
            }
            if (!isDefaultGroup) {
                biomeNameString.concat(biomeID);
            }
        }

        Property namesProp = getCustomGroupProperty(config, biomeNameString);
        namesProp.set(biomeNameString);

        /* Save Group Contents to Config */
        for (Entry<String, BiomeGroup> entry : iDToGroup.entrySet()) {
            String biomelist = groupBiomesToString(entry.getValue());
            Property listProp = getBiomeListProperty(config, entry.getKey(), biomelist);
            listProp.set(biomelist);
        }
    }
}