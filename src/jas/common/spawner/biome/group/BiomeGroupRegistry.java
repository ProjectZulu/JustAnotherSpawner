package jas.common.spawner.biome.group;

import jas.common.JASLog;
import jas.common.TopologicalSort;
import jas.common.TopologicalSort.DirectedGraph;
import jas.common.TopologicalSortingException;
import jas.common.WorldProperties;
import jas.common.config.BiomeGroupConfiguration;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.toposort.ModSortingException.SortingExceptionData;

public class BiomeGroupRegistry {
    /** Group Identifier to Group Instance Regsitry */
    private final HashMap<String, BiomeGroup> iDToGroup = new HashMap<String, BiomeGroup>();
    /** Reverse Look-up Map to Get All Groups a Particular Biome is In */
    private final ListMultimap<String, String> packgNameToGroupIDList = ArrayListMultimap.create();

    /** Cusom Biome Names: Mappings For CustomBiomeNames to PackageNames used to read from configuration */
    public final BiMap<String, String> biomeMappingToPckg = HashBiMap.create();
    /** Cusom Biome Names: Mappings For PackageNames to CustomBiomeNames used to write to configuration */
    public final BiMap<String, String> biomePckgToMapping = biomeMappingToPckg.inverse();
    /** Reverse Look-up to get access the BiomeGenBase instances from the Biome Package Names */
    public ListMultimap<String, Integer> pckgNameToBiomeID = ArrayListMultimap.create();

    /**
     * Mapping Between AttributeID and the Biomes it Represents. Internally, Attributes are simply BiomeGroups (i.e list
     * of biomes).
     * 
     * Neither Attribute ID and BiomeMappings are not allowed to be the same. They can be the same as BiomeGroups.
     */
    private final HashMap<String, BiomeGroup> iDToAttribute = new HashMap<String, BiomeGroup>();

    public final WorldProperties worldProperties;

    public BiomeGroupRegistry(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    /**
     * Should Only Be Used to Register BiomeGroups with their finished
     */
    public void registerGroup(BiomeGroup group) {
        JASLog.info("Registering BiomeGroup %s", group.toString());
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
        private final Set<String> pckgNames = new HashSet<String>();
        /* String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
        private final LinkedHashSet<String> contents = new LinkedHashSet<String>();
        /* How Group should be saved in the config file. Periods '.' mark categories. Last segment is the prop key */
        public final String saveFormat;

        public BiomeGroup(String groupID, boolean attribute) {
            this(groupID, attribute ? BiomeGroupConfiguration.defaultAttributeGroupCategory(groupID)
                    : BiomeGroupConfiguration.defaultGroupCategory(groupID));
        }

        public BiomeGroup(String groupID, String saveFormat) {
            if (groupID == null || groupID.trim().equals("")) {
                throw new IllegalArgumentException("Group ID cannot be " + groupID == null ? "null" : "empty");
            }
            this.groupID = groupID;
            this.saveFormat = saveFormat;
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

        @Override
        public String toString() {
            return groupID.concat(" contains ").concat(pckgNamesToString().concat(" from ").concat(contentsToString()));
        }

        public String contentsToString() {
            StringBuilder builder = new StringBuilder(contents.size() * 10);
            Iterator<String> iterator = contents.iterator();
            while (iterator.hasNext()) {
                String contentComponent = iterator.next();
                builder.append(contentComponent);
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            return builder.toString();
        }

        public String pckgNamesToString() {
            StringBuilder builder = new StringBuilder(pckgNames.size() * 10);
            Iterator<String> iterator = pckgNames.iterator();
            while (iterator.hasNext()) {
                String jasName = iterator.next();
                builder.append(jasName);
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            return builder.toString();
        }
    }

    public void loadFromConfig(File configDirectory) {
        BiomeGroupConfiguration biomeConfig = new BiomeGroupConfiguration(configDirectory, worldProperties);
        biomeConfig.load();

        /* Load Package Name Mappings that have previously been saved */
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            String packageName = BiomeHelper.getPackageName(biome);
            Property nameMapping = biomeConfig.getBiomeMapping(packageName, null);
            if (nameMapping != null) {
                biomeMappingToPckg.put(nameMapping.getString(), packageName);
                pckgNameToBiomeID.put(packageName, biome.biomeID);
            }
        }

        // Detect new mappings that are created. Used to detect if a new mapping biome group need to be added.
        Set<String> newMappings = new HashSet<String>();
        /* Create Package Name Mappings that do not already exist */
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            String packageName = BiomeHelper.getPackageName(biome);
            if (!pckgNameToBiomeID.containsKey(packageName)) {
                String defaultMapping = biome.biomeName;
                int attempts = 0;
                while (biomeMappingToPckg.containsKey(defaultMapping)) {
                    defaultMapping = BiomeHelper.getShortPackageName(biome);
                    if (attempts > 0) {
                        // For multiple tries, concat the number of the attempts to create a unique mapping
                        defaultMapping = defaultMapping + "_" + attempts;
                    }
                    attempts++;
                }
                if (attempts > 0) {
                    JASLog.info("Duplicate mapping %s and was renamed to %s.", biome.biomeName, defaultMapping);
                }

                Property nameMapping = biomeConfig.getBiomeMapping(packageName, defaultMapping);
                biomeMappingToPckg.put(nameMapping.getString(), packageName);
                pckgNameToBiomeID.put(packageName, biome.biomeID);
                newMappings.add(nameMapping.getString());
            }
        }

        /* Create / Get Base Attributes */
        ConfigCategory configAttribute = biomeConfig.getAttributesCategory();
        Set<BiomeGroup> attributeGroups = new HashSet<BiomeGroup>();
        if (configAttribute.getChildren().isEmpty() && configAttribute.isEmpty()) {
            biomeConfig.removeCategory(configAttribute);
            JASLog.debug(Level.INFO, "Creating Default Biome Attributes");
            // Load Default Entity Groups
            for (BiomeGroup biomeGroup : getDefaultAttributeGroups(biomePckgToMapping)) {
                Property prop = biomeConfig.getEntityGroupList(biomeGroup.saveFormat, biomeGroup.contentsToString());
                BiomeGroup newGroup = new BiomeGroup(biomeGroup.groupID,
                        BiomeGroupConfiguration.defaultAttributeGroupCategory(biomeGroup.groupID));
                for (String jasName : prop.getString().split(",")) {
                    if (!jasName.trim().equals("")) {
                        newGroup.contents.add(jasName);
                    }
                }
                attributeGroups.add(newGroup);
            }
            JASLog.debug(Level.INFO, "Finished Default Biome Attributes");
        } else {
            /* Have Children, so don't generate defaults, read settings */
            Map<String, Property> propMap = configAttribute.getValues();
            attributeGroups.addAll(getGroupsFromProps(propMap, (String) null, configAttribute.getQualifiedName()));
            for (ConfigCategory child : configAttribute.getChildren()) {
                attributeGroups.addAll(getGroupsFromCategory(child));
            }
        }

        /* Create / Get Base Groups */
        Set<BiomeGroup> biomeGroups = new HashSet<BiomeGroup>();
        ConfigCategory configCategory = biomeConfig.getGroupsCategory();
        if (!configCategory.getChildren().isEmpty() || !configCategory.isEmpty()) {
            /* Have Children, so don't generate defaults, read settings */
            Map<String, Property> propMap = configCategory.getValues();
            biomeGroups.addAll(getGroupsFromProps(propMap, (String) null, configCategory.getQualifiedName()));
            for (ConfigCategory child : configCategory.getChildren()) {
                biomeGroups.addAll(getGroupsFromCategory(child));
            }
        } else {
            biomeConfig.removeCategory(configCategory);
        }

        /* For any newly created BiomeMapping, we must create a new BiomeGroup to represent it */
        for (BiomeGroup biomeGroup : getDefaultGroups(newMappings)) {
            Property prop = biomeConfig.getEntityGroupList(biomeGroup.saveFormat, biomeGroup.contentsToString());
            BiomeGroup newlivingGroup = new BiomeGroup(biomeGroup.groupID,
                    BiomeGroupConfiguration.defaultGroupCategory(biomeGroup.groupID));
            for (String jasName : prop.getString().split(",")) {
                if (!jasName.trim().equals("")) {
                    newlivingGroup.contents.add(jasName);
                }
            }
            biomeGroups.add(newlivingGroup);
        }

        /* Sort Groups */
        List<BiomeGroup> sortedAttributes = getSortedGroups(attributeGroups);
        List<BiomeGroup> sortedGroups = getSortedGroups(biomeGroups);

        /*
         * Evaluate and register groups. i.e. from group form A|allbiomes,&Jungle to individual entity names jasNames
         */
        for (BiomeGroup biomeGroup : sortedAttributes) {
            parseGroupContents(biomeGroup);
            iDToAttribute.put(biomeGroup.groupID, biomeGroup);
        }

        for (BiomeGroup biomeGroup : sortedGroups) {
            parseGroupContents(biomeGroup);
            if (biomeGroup.pckgNames.size() > 0) {
                registerGroup(biomeGroup);
            }
        }

        biomeConfig.save();
    }

    private Set<BiomeGroup> getGroupsFromProps(Map<String, Property> propMap, String shortConfigName,
            String fillConfigName) {
        Set<BiomeGroup> groups = new HashSet<BiomeGroup>();
        for (Entry<String, Property> entry : propMap.entrySet()) {
            if (propMap.isEmpty()) {
                continue;
            }

            String groupName;
            if (shortConfigName != null
                    && (entry.getValue().getName().equals("AtrributeList") || entry.getValue().getName()
                            .equals("BiomeList"))) {
                groupName = shortConfigName;
            } else {
                groupName = entry.getKey();
            }

            if (groupName == null || groupName.trim().equals("")) {
                continue;
            }
            JASLog.debug(Level.INFO, "Parsing entity group %s with %s", groupName, entry.getValue().getString());

            BiomeGroup biomeGroup = new BiomeGroup(groupName, fillConfigName + Configuration.CATEGORY_SPLITTER + ":"
                    + groupName);
            String[] contents = entry.getValue().getString().split(",");
            for (String content : contents) {
                if (!content.trim().equals("")) {
                    biomeGroup.contents.add(content);
                }
            }
            groups.add(biomeGroup);
        }
        return groups;
    }

    private Set<BiomeGroup> getGroupsFromCategory(ConfigCategory parentConfig) {
        Set<BiomeGroup> biomeGroups = new HashSet<BiomeGroup>();
        Map<String, Property> childPropMap = parentConfig.getValues();
        String configName = parentConfig.getQualifiedName().substring(
                parentConfig.getQualifiedName().lastIndexOf(Configuration.CATEGORY_SPLITTER) + 1,
                parentConfig.getQualifiedName().length());
        biomeGroups.addAll(getGroupsFromProps(childPropMap, configName, parentConfig.getQualifiedName()));
        for (ConfigCategory child : parentConfig.getChildren()) {
            biomeGroups.addAll(getGroupsFromCategory(child));
        }
        return biomeGroups;
    }

    private Set<BiomeGroup> getDefaultAttributeGroups(BiMap<String, String> biomePckgToMapping) {
        Set<BiomeGroup> attributeGroups = new HashSet<BiomeGroup>();

        /* Create AllBiomes Group */
        BiomeGroup allbiomes = new BiomeGroup("allbiomes", true);
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome != null) {
                allbiomes.contents.add(biomePckgToMapping.get(BiomeHelper.getPackageName(biome)));
            }
        }
        JASLog.debug(Level.INFO, "Created Attribute %s", allbiomes.groupID);
        attributeGroups.add(allbiomes);

        /* Get Default Groups From BiomeDictionary */
        for (Type type : BiomeDictionary.Type.values()) {
            BiomeGroup biomeGroup = new BiomeGroup(type.toString(), true);
            for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) {
                biomeGroup.contents.add(biomePckgToMapping.get(BiomeHelper.getPackageName(biome)));
            }
            JASLog.debug(Level.INFO, "Created Attribute %s", biomeGroup.groupID);
            attributeGroups.add(biomeGroup);
        }

        return attributeGroups;
    }

    private Set<BiomeGroup> getDefaultGroups(Collection<String> biomeMappings) {
        Set<BiomeGroup> groups = new HashSet<BiomeGroup>();
        /* A group is created for each mapping */
        for (String biomeMapping : biomeMappings) {
            BiomeGroup group = new BiomeGroup(biomeMapping, false);
            group.contents.add(biomeMapping);
            groups.add(group);
        }
        return groups;
    }

    private List<BiomeGroup> getSortedGroups(Collection<BiomeGroup> groupsToSort) {
        /* Evaluate each group, ensuring entries are valid mappings or Groups and */
        DirectedGraph<BiomeGroup> groupGraph = new DirectedGraph<BiomeGroup>();
        for (BiomeGroup group : groupsToSort) {
            groupGraph.addNode(group);
        }
        for (BiomeGroup currentGroup : groupsToSort) {
            for (String contentComponent : currentGroup.contents) {
                for (BiomeGroup possibleGroup : groupsToSort) {
                    // Reminder: substring(2) is to remove mandatory A| and G| for groups
                    if (contentComponent.substring(2).equals(possibleGroup.groupID)) {
                        groupGraph.addEdge(possibleGroup, currentGroup);
                    }
                }
            }
        }

        List<BiomeGroup> sortedList;
        try {
            sortedList = TopologicalSort.topologicalSort(groupGraph);
        } catch (TopologicalSortingException sortException) {
            SortingExceptionData<BiomeGroup> exceptionData = sortException.getExceptionData();
            JASLog.severe("A circular reference was detected when processing entity groups. Groups in the cycle were: ");
            int i = 1;
            for (BiomeGroup invalidGroups : exceptionData.getVisitedNodes()) {
                JASLog.severe("Group %s: %s containing %s", i++, invalidGroups.groupID,
                        invalidGroups.contentsToString());
            }
            throw sortException;
        }
        return sortedList;
    }

    /**
     * Evaluate build instructions (i.e. A|allbiomes,&Jungle) of group and evalute them into jasNames
     */
    private void parseGroupContents(BiomeGroup biomeGroup) {
        /* Evaluate contents and fill in jasNames */
        for (String contentComponent : biomeGroup.contents) {
            OPERATION operation;
            if (contentComponent.startsWith("-")) {
                contentComponent = contentComponent.substring(1);
                operation = OPERATION.COMPLEMENT;
            } else if (contentComponent.startsWith("&")) {
                contentComponent = contentComponent.substring(1);
                operation = OPERATION.INTERSECT;
            } else {
                operation = OPERATION.UNION;
                if (contentComponent.startsWith("+")) {
                    contentComponent = contentComponent.substring(1);
                }
            }

            if (contentComponent.startsWith("G|")) {
                BiomeGroup groupToAdd = iDToGroup.get(contentComponent.substring(2));
                if (groupToAdd != null) {
                    SetAlgebra.operate(biomeGroup.pckgNames, groupToAdd.pckgNames, operation);
                    continue;
                }
            } else if (contentComponent.startsWith("A|")) {
                BiomeGroup groupToAdd = iDToAttribute.get(contentComponent.substring(2));
                if (groupToAdd != null) {
                    SetAlgebra.operate(biomeGroup.pckgNames, groupToAdd.pckgNames, operation);
                    continue;
                }
            } else if (biomeMappingToPckg.containsKey(contentComponent)) {
                SetAlgebra.operate(biomeGroup.pckgNames, Sets.newHashSet(contentComponent), operation);
                continue;
            }
            JASLog.severe("Error processing %s content from %s. The component %s does not exist.", biomeGroup.groupID,
                    biomeGroup.contentsToString(), contentComponent);
        }
    }

    /**
     * Used to save the currently loaded settings into the Configuration Files
     * 
     * If config settings are already present, they will be overwritten
     */
    public void saveToConfig(File configDirectory) {
        BiomeGroupConfiguration config = new BiomeGroupConfiguration(configDirectory, worldProperties);
        config.load();
        /* Save Mapping i.e. net.minecraft.Forest=Forest */
        for (Entry<String, String> entry : biomePckgToMapping.entrySet()) {
            Property prop = config.getBiomeMapping(entry.getKey(), entry.getValue());
            prop.set(entry.getValue());
        }

        /* Save Attributes */
        for (BiomeGroup group : iDToAttribute.values()) {
            Property prop = config.getEntityGroupList(group.saveFormat, group.contentsToString());
            prop.set(group.contentsToString());
        }

        /* Save Groups */
        for (BiomeGroup group : iDToGroup.values()) {
            Property prop = config.getEntityGroupList(group.saveFormat, group.contentsToString());
            prop.set(group.contentsToString());
        }
        config.save();
    }
}