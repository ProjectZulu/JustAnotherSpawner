package jas.common.spawner.creature.handler;

import jas.common.JASLog;
import jas.common.TopologicalSort;
import jas.common.TopologicalSort.DirectedGraph;
import jas.common.TopologicalSortingException;
import jas.common.WorldProperties;
import jas.common.config.LivingGroupConfiguration;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.toposort.ModSortingException.SortingExceptionData;

public class LivingGroupRegistry {

    /* Contains packages with known prefixes apriori such as net.minecraft.entity should have vanilla prefix */
    public static final HashMap<String, String> entityPackageToPrefix;
    public static final String UNKNOWN_PREFIX;
    static {
        UNKNOWN_PREFIX = "unknown";
        entityPackageToPrefix = new HashMap<String, String>(5);
        entityPackageToPrefix.put("net.minecraft.entity.monster", "");
        entityPackageToPrefix.put("net.minecraft.entity.passive", "");
        entityPackageToPrefix.put("net.minecraft.entity.boss", "");
    }

    /** Group Identifier to Group Instance */
    private final HashMap<String, LivingGroup> iDToGroup = new HashMap<String, LivingGroup>();
    /** Reverse Look-up Map to Get All Groups a Particular Entity is In */
    private final ListMultimap<String, String> entityIDToGroupIDList = ArrayListMultimap.create();

    /**
     * Should Only Be Used to Register BiomeGroups when they finished and valid
     */
    public void registerGroup(LivingGroup group) {
        JASLog.info("Registering EntityGroup %s", group.toString());
        iDToGroup.put(group.groupID, group);
        for (String jasName : group.entityJASNames) {
            entityIDToGroupIDList.get(jasName).add(group.groupID);
        }
    }

    public List<LivingGroup> getEntityGroups(Class<? extends EntityLiving> entityClass) {
        String jasName = EntityClasstoJASName.get(entityClass);
        List<LivingGroup> list = new ArrayList<LivingGroup>();
        for (String groupID : entityIDToGroupIDList.get(jasName)) {
            list.add(iDToGroup.get(groupID));
        }
        return list;
    }

    public Class<? extends EntityLiving> getRandomEntity(String livingGroupID, Random random) {
        LivingGroup livingGroup = this.getLivingGroup(livingGroupID);
        int selectedEntry = random.nextInt(1 + livingGroup.entityJASNames.size());
        int i = 0;
        for (String jasName : livingGroup.entityJASNames) {
            if (++i == selectedEntry) {
                return this.JASNametoEntityClass.get(jasName);
            }
        }
        return null;
    }

    public LivingGroup getLivingGroup(String groupID) {
        return iDToGroup.get(groupID);
    }

    /**
     * @return Immutable view of all registered livinggroups
     */
    public Collection<LivingGroup> getEntityGroups() {
        return Collections.unmodifiableCollection(iDToGroup.values());
    }

    public ImmutableCollection<String> getGroupsWithEntity(String jasName) {
        return ImmutableList.copyOf(entityIDToGroupIDList.get(jasName));
    }

    public ImmutableMultimap<String, String> getEntityIDToGroupIDList() {
        return ImmutableMultimap.copyOf(entityIDToGroupIDList);
    }

    /** Group Identifier to Group Instance */
    private final HashMap<String, LivingGroup> iDToAttribute = new HashMap<String, LivingGroup>();

    /* Mapping From Entity Name (via EntityList.classToString) to JAS Name */
    public final BiMap<Class<? extends EntityLiving>, String> EntityClasstoJASName = HashBiMap.create();
    /* Mapping From JAS Name to Entity Name (via EntityList.classToString) */
    public final BiMap<String, Class<? extends EntityLiving>> JASNametoEntityClass = EntityClasstoJASName.inverse();

    private WorldProperties worldProperties;

    public LivingGroupRegistry(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    public static class LivingGroup {
        public final String groupID;
        private final Set<String> entityJASNames = new HashSet<String>();
        /* String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
        private final Set<String> contents = new HashSet<String>();
        /* How Group should be saved in the config file. Periods '.' mark categories. Last segment is the prop key */
        public final String saveFormat;

        public LivingGroup(String groupID) {
            this(groupID, LivingGroupConfiguration.defaultGroupCategory(groupID));
        }

        public LivingGroup(String groupID, String saveFormat) {
            if (groupID == null || groupID.trim().equals("")) {
                throw new IllegalArgumentException("Group ID cannot be " + groupID == null ? "null" : "empty");
            }
            this.groupID = groupID;
            this.saveFormat = saveFormat;
        }

        public Set<String> entityJASNames() {
            return Collections.unmodifiableSet(entityJASNames);
        }

        @Override
        public boolean equals(Object paramObject) {
            if (paramObject == null || !(paramObject instanceof LivingGroup)) {
                return false;
            }
            return ((LivingGroup) paramObject).groupID.equals(groupID);
        }

        @Override
        public int hashCode() {
            return groupID.hashCode();
        }

        @Override
        public String toString() {
            return groupID.concat(" contains ").concat(jasNamesToString().concat(" from ").concat(contentsToString()));
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

        public String jasNamesToString() {
            StringBuilder builder = new StringBuilder(entityJASNames.size() * 10);
            Iterator<String> iterator = entityJASNames.iterator();
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
        LivingGroupConfiguration config = new LivingGroupConfiguration(configDirectory, worldProperties);
        config.load();

        /* Load Mappings that already exist */
        @SuppressWarnings("unchecked")
        Set<Entry<Class<?>, String>> fmlNames = EntityList.classToStringMapping.entrySet();
        for (Entry<Class<?>, String> entry : fmlNames) {
            if (!EntityLiving.class.isAssignableFrom(entry.getKey())
                    || Modifier.isAbstract(entry.getKey().getModifiers())) {
                continue;
            }
            Property nameProp = config.getEntityMapping(entry.getValue(), null);
            if (nameProp != null) {
                @SuppressWarnings("unchecked")
                String prevKey = EntityClasstoJASName.put((Class<? extends EntityLiving>) entry.getKey(),
                        nameProp.getString());
                if (prevKey != null) {
                    JASLog.severe("Duplicate entity mapping. Pair at %s replaced by %s,%s", prevKey, entry.getValue(),
                            nameProp.getString());
                }
            }
        }

        /* Create Mapping For Entities That do not already exist */
        // Detect new mappings that are created. Used to detect if a new mapping living group needs to be added.
        Set<String> newMappings = new HashSet<String>();
        for (Entry<Class<?>, String> entry : fmlNames) {
            if (!EntityLiving.class.isAssignableFrom(entry.getKey())
                    || Modifier.isAbstract(entry.getKey().getModifiers())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<? extends EntityLiving> entityClass = (Class<? extends EntityLiving>) entry.getKey();

            String jasName;
            if (entry.getValue().contains(".")) {
                jasName = entry.getValue();
            } else {
                String prefix = guessPrefix(entry.getKey(), fmlNames);
                jasName = prefix.trim().equals("") ? entry.getValue() : prefix + "." + entry.getValue();
            }
            if (!EntityClasstoJASName.containsKey(entityClass)) {
                Property nameProp = config.getEntityMapping(entry.getValue(), jasName);
                String prevKey = EntityClasstoJASName.put(entityClass, nameProp.getString());
                newMappings.add(nameProp.getString());
                if (prevKey != null) {
                    JASLog.severe("Duplicate entity mapping. Pair at %s replaced by %s,%s", prevKey, entry.getValue(),
                            nameProp.getString());
                }
            }
        }

        /* Create / Get Base Attributes */
        ConfigCategory configAttribute = config.getEntityAttributes();
        Set<LivingGroup> attributeGroups = new HashSet<LivingGroup>();
        if (configAttribute.getChildren().isEmpty() && configAttribute.isEmpty()) {
            config.removeCategory(configAttribute);
            /* There are no default entity attributes */
        } else {
            /* Have Children, so don't generate defaults, read settings */
            Map<String, Property> propMap = configAttribute.getValues();
            attributeGroups.addAll(getGroupsFromProps(propMap, (String) null, configAttribute.getQualifiedName()));
            for (ConfigCategory child : configAttribute.getChildren()) {
                attributeGroups.addAll(getGroupsFromCategory(child));
            }
        }

        /* Create / Get Base Groups */
        Set<LivingGroup> livingGroups = new HashSet<LivingGroup>();
        ConfigCategory configCategory = config.getEntityGroups();
        if (configCategory.getChildren().isEmpty() && configCategory.isEmpty()) {
            config.removeCategory(configCategory);
            /* Category was nonexistent or empty; time to create default settings */
            JASLog.debug(Level.INFO, "Creating Default EntityGroups");
            for (LivingGroup livingGroup : getDefaultGroups(JASNametoEntityClass.keySet())) {
                Property prop = config.getEntityGroupList(livingGroup.saveFormat, livingGroup.contentsToString());
                LivingGroup newlivingGroup = new LivingGroup(livingGroup.groupID);
                for (String jasName : prop.getString().split(",")) {
                    newlivingGroup.contents.add(jasName);
                }
                livingGroups.add(newlivingGroup);
            }
            JASLog.debug(Level.INFO, "Finished Default EntityGroups");
        } else {
            for (LivingGroup livingGroup : getDefaultGroups(newMappings)) {
                Property prop = config.getEntityGroupList(livingGroup.saveFormat, livingGroup.contentsToString());
                LivingGroup newlivingGroup = new LivingGroup(livingGroup.groupID);
                for (String jasName : prop.getString().split(",")) {
                    newlivingGroup.contents.add(jasName);
                }
                livingGroups.add(newlivingGroup);
            }
            /* Have Children, so don't generate defaults, read settings */
            Map<String, Property> propMap = configCategory.getValues();
            livingGroups.addAll(getGroupsFromProps(propMap, (String) null, configCategory.getQualifiedName()));
            for (ConfigCategory child : configCategory.getChildren()) {
                livingGroups.addAll(getGroupsFromCategory(child));
            }
        }

        /* Sort Groups */
        List<LivingGroup> sortedAttributes = getSortedGroups(attributeGroups);
        List<LivingGroup> sortedGroups = getSortedGroups(livingGroups);

        /* Evaluate and register groups. i.e. from group form A|allbiomes,&Jungle to individual entity names jasNames */
        for (LivingGroup livingGroup : sortedAttributes) {
            parseGroupContents(livingGroup);
            iDToAttribute.put(livingGroup.groupID, livingGroup);
        }

        for (LivingGroup livingGroup : sortedGroups) {
            parseGroupContents(livingGroup);
            registerGroup(livingGroup);
        }
        config.save();
    }

    private Set<LivingGroup> getGroupsFromCategory(ConfigCategory parentConfig) {
        Set<LivingGroup> livingGroups = new HashSet<LivingGroup>();
        Map<String, Property> childPropMap = parentConfig.getValues();
        String configName = parentConfig.getQualifiedName().substring(
                parentConfig.getQualifiedName().lastIndexOf(Configuration.CATEGORY_SPLITTER) + 1,
                parentConfig.getQualifiedName().length());
        livingGroups.addAll(getGroupsFromProps(childPropMap, (String) configName, parentConfig.getQualifiedName()));
        for (ConfigCategory child : parentConfig.getChildren()) {
            livingGroups.addAll(getGroupsFromCategory(child));
        }
        return livingGroups;
    }

    private Set<LivingGroup> getGroupsFromProps(Map<String, Property> propMap, String shortConfigName,
            String fillConfigName) {
        Set<LivingGroup> groups = new HashSet<LivingGroup>();
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

            LivingGroup livingGroup = new LivingGroup(groupName, fillConfigName + Configuration.CATEGORY_SPLITTER + ":"
                    + groupName);
            String[] entityNames = entry.getValue().getString().split(",");
            for (String entityName : entityNames) {
                livingGroup.contents.add(entityName);
            }
            groups.add(livingGroup);
        }
        return groups;
    }

    /**
     * Attempts to Guess the prefix an entity should have.
     * 
     * First attempts to search
     */
    private String guessPrefix(Class<?> entity, Set<Entry<Class<?>, String>> fmlNames) {
        String currentPackage = entity.getName();

        if (currentPackage.lastIndexOf(".") != -1) {
            currentPackage = currentPackage.substring(0, currentPackage.lastIndexOf("."));
        }

        for (Entry<Class<?>, String> entry : fmlNames) {
            String packageName = entry.getKey().getName();
            if (packageName.lastIndexOf(".") != -1) {
                packageName = packageName.substring(0, packageName.lastIndexOf("."));
            }

            if (currentPackage.equals(packageName) && entry.getValue().contains(".")) {
                return entry.getValue().split("\\.")[0];
            }
        }
        String manualPrefix = entityPackageToPrefix.get(currentPackage);
        if (manualPrefix != null) {
            return manualPrefix;
        }
        String[] currentParts = currentPackage.split("\\.");
        return currentParts.length > 1 ? currentParts[0] : UNKNOWN_PREFIX;
    }

    private Set<LivingGroup> getDefaultGroups(Collection<String> jasNames) {
        Set<LivingGroup> livinggroups = new HashSet<LivingGroup>();
        for (String jasName : jasNames) {
            LivingGroup livingGroup = new LivingGroup(jasName);
            livingGroup.contents.add(jasName);
            livinggroups.add(livingGroup);
        }
        return livinggroups;
    }

    private List<LivingGroup> getSortedGroups(Collection<LivingGroup> livingGroups) {
        /* Evaluate each group, ensuring entries are valid mappings or Groups and */
        DirectedGraph<LivingGroup> groupGraph = new DirectedGraph<LivingGroup>();
        for (LivingGroup livingGroup : livingGroups) {
            groupGraph.addNode(livingGroup);
        }
        for (LivingGroup currentGroup : livingGroups) {
            for (String contentComponent : currentGroup.contents) {
                for (LivingGroup possibleGroup : livingGroups) {
                    // Reminder: substring(2) is to remove mandatory A| and G| for groups
                    if (contentComponent.substring(2).equals(possibleGroup.groupID)) {
                        groupGraph.addEdge(possibleGroup, currentGroup);
                    }
                }
            }
        }

        List<LivingGroup> sortedList;
        try {
            sortedList = TopologicalSort.topologicalSort(groupGraph);
        } catch (TopologicalSortingException sortException) {
            SortingExceptionData<LivingGroup> exceptionData = sortException.getExceptionData();
            JASLog.severe("A circular reference was detected when processing entity groups. Groups in the cycle were: ");
            int i = 1;
            for (LivingGroup invalidGroups : exceptionData.getVisitedNodes()) {
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
    private void parseGroupContents(LivingGroup livingGroup) {
        /* Evaluate contents and fill in jasNames */
        for (String contentComponent : livingGroup.contents) {
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
                LivingGroup groupToAdd = iDToGroup.get(contentComponent.substring(2));
                if (groupToAdd != null) {
                    SetAlgebra.operate(livingGroup.entityJASNames, groupToAdd.entityJASNames, operation);
                    continue;
                }
            } else if (contentComponent.startsWith("A|")) {
                LivingGroup groupToAdd = iDToAttribute.get(contentComponent.substring(2));
                if (groupToAdd != null) {
                    SetAlgebra.operate(livingGroup.entityJASNames, groupToAdd.entityJASNames, operation);
                    continue;
                }
            } else if (JASNametoEntityClass.containsKey(contentComponent)) {
                SetAlgebra.operate(livingGroup.entityJASNames, Sets.newHashSet(contentComponent), operation);
                continue;
            }
            JASLog.severe("Error processing %s content from %s. The component %s does not exist.", livingGroup.groupID,
                    livingGroup.contentsToString(), contentComponent);
        }
    }

    public void saveToConfig(File configDirectory) {
        LivingGroupConfiguration config = new LivingGroupConfiguration(configDirectory, worldProperties);
        config.load();
        for (Entry<Class<? extends EntityLiving>, String> entry : EntityClasstoJASName.entrySet()) {
            String entityName = (String) EntityList.classToStringMapping.get(entry.getKey());
            Property prop = config.getEntityMapping(entityName, entry.getValue());
            prop.set(entry.getValue());
        }

        for (LivingGroup group : iDToGroup.values()) {
            Property prop = config.getEntityGroupList(group.saveFormat, group.contentsToString());
            prop.set(group.contentsToString());
        }
        config.save();
    }
}
