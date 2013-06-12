package jas.common.spawner.biome.group;

import jas.common.JASLog;
import jas.common.config.BiomeGroupConfiguration;

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
import java.util.logging.Level;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
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
    /** Reverse Look-up to get access the BiomeGenBase instances from the Biome Package Names */
    public ListMultimap<String, Integer> pckgNameToBiomeID = ArrayListMultimap.create();

    /**
     * Mapping Between AttributeID and the Biomes it Represents. Internally, Attributes are simply BiomeGroups (i.e list
     * of biomes).
     * 
     * Neither Attribute ID and BiomeMappings are not allowed to be the same. They can be the same as BiomeGroups.
     */
    private final HashMap<String, BiomeGroup> iDToAttribute = new HashMap<String, BiomeGroup>();

    /**
     * Should Only Be Used to Register BiomeGroups with their finished
     */
    public void registerGroup(BiomeGroup group) {
        JASLog.info("Registering BiomeGroup %s with biomes %s", group.groupID, groupBiomesToString(group));
        iDToGroup.put(group.groupID, group);
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
        /* String Used to Build Package Names i.e. desert,A|Forest,glacier */
        private String biomeString = "";

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
        BiomeGroupConfiguration biomeConfig = new BiomeGroupConfiguration(configDirectory);
        biomeConfig.load();

        /* Create Package Name Mappings */
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            String packageName = BiomeHelper.getPackageName(biome);

            Property nameMapping = biomeConfig.getBiomeMapping(packageName, biome.biomeName);

            biomeMappingToPckg.put(nameMapping.getString(), packageName);
            biomePckgToMapping.put(packageName, nameMapping.getString());
            pckgNameToBiomeID.put(packageName, biome.biomeID);
        }

        ArrayList<BiomeGroup> attributeGroups = getAttributeGroups(biomeConfig);
        ArrayList<BiomeGroup> biomeGroups = getBiomeGroups(biomeConfig);

        /* Filter Every Attribute Through Configuration */
        for (BiomeGroup attributeGroup : attributeGroups) {
            getAttributeSpawnList(biomeConfig, attributeGroup);
        }

        /* For Every biome Group; Filter BiomeList Through Configuration */
        for (BiomeGroup biomeGroup : biomeGroups) {
            getGroupSpawnList(biomeConfig, biomeGroup);
            if (biomeGroup.pckgNames.size() > 0) {
                registerGroup(biomeGroup);
            }
        }
        biomeConfig.save();
    }

    /**
     * Creates the default Attribute Groups as well as Custom AttributeGroups (which are empty by default)
     * 
     * @param biomeConfig
     * @return
     */
    private ArrayList<BiomeGroup> getAttributeGroups(BiomeGroupConfiguration biomeConfig) {
        ArrayList<BiomeGroup> attributeGroups = new ArrayList<BiomeGroupRegistry.BiomeGroup>();

        /* Get Default Groups From BiomeDictionary */
        for (Type type : BiomeDictionary.Type.values()) {
            BiomeGroup biomeGroup = new BiomeGroup(type.toString());
            for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) {
                biomeGroup.pckgNames.add(BiomeHelper.getPackageName(biome));
            }
            attributeGroups.add(biomeGroup);
            JASLog.debug(Level.INFO, "Created Attribute %s", biomeGroup.groupID);
            iDToAttribute.put(biomeGroup.groupID, biomeGroup);
        }

        /* Get Empty Custom Attributes */
        Property customAttributeProp = biomeConfig.getAttributeList("");
        String[] resultAttributes = customAttributeProp.getString().split(",");
        for (String attributeName : resultAttributes) {
            if (attributeName.trim().equals("")) {
                continue;
            }
            BiomeGroup biomeGroup = new BiomeGroup(attributeName);
            attributeGroups.add(biomeGroup);
            JASLog.debug(Level.INFO, "Created Attribute %s", biomeGroup.groupID);
            iDToAttribute.put(biomeGroup.groupID, biomeGroup);
        }
        return attributeGroups;
    }

    /**
     * Creates the default Biome Groups as well as Custom BiomeGroups (which are empty by default)
     * 
     * @param biomeConfig
     * @return
     */
    private ArrayList<BiomeGroup> getBiomeGroups(BiomeGroupConfiguration biomeConfig) {
        ArrayList<BiomeGroup> biomeGroups = new ArrayList<BiomeGroupRegistry.BiomeGroup>();

        /* Map PackageBiomeName to ID for default groups : Used to Overcome DuplicateIDs */
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
        Property customGroupProp = biomeConfig.getBiomeGroupList("");
        String[] resultgroups = customGroupProp.getString().split(",");
        for (String groupName : resultgroups) {
            if (groupName.trim().equals("")) {
                continue;
            }
            biomeGroups.add(new BiomeGroup(groupName));
        }
        return biomeGroups;
    }

    public void getAttributeSpawnList(BiomeGroupConfiguration config, BiomeGroup attributeGroup) {
        Property resultProp = config.getAtrributeBiomes(attributeGroup.groupID, groupBiomesToString(attributeGroup));

        String resultGroupString = resultProp.getString();
        String[] resultgroups = resultGroupString.split(",");
        Set<String> biomeNames = new HashSet<String>();
        for (String name : resultgroups) {
            if (name.equals("")) {
                continue;
            }
            String pckgName = biomeMappingToPckg.get(name);
            if (pckgName == null) {
                JASLog.severe("Error while Parsing %s AttributeGroup. Biome entry %s is not a valid biome mapping",
                        attributeGroup.groupID, name);
                continue;
            }
            biomeNames.add(pckgName);
        }
        attributeGroup.pckgNames.clear();
        attributeGroup.pckgNames.addAll(biomeNames);
        attributeGroup.biomeString = resultGroupString;
    }

    /**
     * Gets the List of All BiomeNames from Configuration Files. They are stored as a single string seperated by Commas.
     * 
     * @param config
     * @param group
     * @return
     */
    private void getGroupSpawnList(BiomeGroupConfiguration config, BiomeGroup group) {
        Property resultProp = config.getBiomeGroupBiomes(group.groupID, groupBiomesToString(group));

        String resultGroupString = resultProp.getString();
        String[] resultgroups = resultGroupString.split(",");
        Set<String> biomeNames = new HashSet<String>();
        for (String name : resultgroups) {
            if (name.equals("")) {
                continue;
            }
            boolean foundMatch = false;

            String[] parts = name.split("\\|", 2);
            if (parts.length == 1) {
                String pckgName = biomeMappingToPckg.get(parts[0]);
                if (pckgName != null) {
                    biomeNames.add(pckgName);
                    foundMatch = true;
                }
            } else if (parts[0].equalsIgnoreCase("A")) {
                BiomeGroup attributeGroup = iDToAttribute.get(parts[1].toLowerCase());
                if (attributeGroup != null) {
                    biomeNames.addAll(attributeGroup.pckgNames);
                    foundMatch = true;
                }
            }

            if (!foundMatch) {
                JASLog.severe("Error while Parsing %s BiomeGroup. Entry %s is not a valid biome mapping or attribute",
                        group.groupID, name);
            }
        }
        group.pckgNames.clear();
        group.pckgNames.addAll(biomeNames);
        group.biomeString = resultGroupString;
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
        BiomeGroupConfiguration biomeConfig = new BiomeGroupConfiguration(configDirectory);
        biomeConfig.load();
        saveMappingsToConfig(biomeConfig);
        saveCustomGroupsToConfig(biomeConfig);
        biomeConfig.save();
    }

    private void saveMappingsToConfig(BiomeGroupConfiguration config) {
        for (Entry<String, String> mappingEntry : biomePckgToMapping.entrySet()) {
            Property mappingProp = config.getBiomeMapping(mappingEntry.getKey(), mappingEntry.getValue());
            mappingProp.set(mappingEntry.getValue());
        }
    }

    private void saveCustomGroupsToConfig(BiomeGroupConfiguration config) {
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

        Property namesProp = config.getBiomeGroupList(biomeNameString);
        namesProp.set(biomeNameString);

        /* Save Group Contents to Config */
        for (Entry<String, BiomeGroup> entry : iDToGroup.entrySet()) {
            String biomelist = groupBiomesToString(entry.getValue());
            Property listProp = config.getBiomeGroupBiomes(entry.getKey(), biomelist);
            listProp.set(biomelist);
        }
    }
}