package jas.common.spawner.creature.handler;

import jas.common.JASLog;
import jas.common.WorldProperties;
import jas.common.config.LivingGroupConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

public class LivingGroupRegistry {

    /* Contains packages with known prefixes apriori such as net.minecraft.entity should have vanilla prefix */
    public static final HashMap<String, String> entityPackageToPrefix;
    public static final String UNKNOWN_PREFIX;
    static {
        UNKNOWN_PREFIX = "Vanilla";
        entityPackageToPrefix = new HashMap<String, String>(5);
        entityPackageToPrefix.put("net.minecraft.entity.monster", UNKNOWN_PREFIX);
        entityPackageToPrefix.put("net.minecraft.entity.passive", UNKNOWN_PREFIX);
    }

    /** Group Identifier to Group Instance */
    private final HashMap<String, LivingGroup> iDToGroup = new HashMap<String, LivingGroup>();
    /** Reverse Look-up Map to Get All Groups a Particular Entity is In */
    private final ListMultimap<String, String> entityIDToGroupIDList = ArrayListMultimap.create();

    /* Mapping From Entity Name (via EntityList.classToString) to JAS Name */
    public final BiMap<String, String> FMLNametoJASName = HashBiMap.create();
    /* Mapping From JAS Name to Entity Name (via EntityList.classToString) */
    public final BiMap<String, String> JASNametoFMLName = FMLNametoJASName.inverse();

    private WorldProperties worldProperties;

    public LivingGroupRegistry(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    public static class LivingGroup {
        public final String groupID;
        private final Set<String> entityJASNames = new HashSet<String>();
        /* How Group should be saved in the config file. Periods '.' mark categories. Last segment is the prop key */
        public final String saveFormat;

        public LivingGroup(String groupID) {
            this(groupID, LivingGroupConfiguration.GroupListCategory + Configuration.CATEGORY_SPLITTER + groupID);
        }

        public LivingGroup(String groupID, String saveFormat) {
            if (groupID == null || groupID.trim().equals("")) {
                throw new IllegalArgumentException("Group ID cannot be " + groupID == null ? "null" : "empty");
            }
            this.groupID = groupID;
            this.saveFormat = saveFormat;
        }

        public ImmutableList<String> getEntityNames() {
            return ImmutableList.copyOf(entityJASNames);
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
            return groupID.concat(" containing ").concat(contentsToString());
        }

        public String contentsToString() {
            StringBuilder builder = new StringBuilder(entityJASNames.size() * 10);
            for (String jasName : entityJASNames) {
                builder.append(jasName);
            }
            return builder.toString();
        }
    }

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

    public void loadFromConfig(File configDirectory) {
        LivingGroupConfiguration config = new LivingGroupConfiguration(configDirectory, worldProperties);
        config.load();

        /* Create Mapping For Entities */
        @SuppressWarnings("unchecked")
        Set<Entry<Class<?>, String>> fmlNames = EntityList.classToStringMapping.entrySet();
        for (Entry<Class<?>, String> entry : fmlNames) {
            if (!EntityLiving.class.isAssignableFrom(entry.getKey())) {
                continue;
            }

            String jasName;
            if (entry.getValue().contains(":")) {
                jasName = entry.getValue();
            } else {
                jasName = guessPrefix(entry.getKey(), fmlNames) + ":" + entry.getValue();
            }
            Property nameProp = config.getEntityMapping(entry.getValue(), jasName);
            String prevKey = FMLNametoJASName.put(entry.getValue(), nameProp.getString());
            if (prevKey != null) {
                JASLog.severe("Duplicate entity mapping. Pair at %s replaced by %s,%s", prevKey, entry.getValue(),
                        nameProp.getString());
            }
        }

        ConfigCategory configCategory = config.getEntityGroups();
        Set<LivingGroup> livingGroups = new HashSet<LivingGroup>();
        if (configCategory.getChildren().isEmpty() && configCategory.isEmpty()) {
            config.removeCategory(configCategory);
            /* Category was nonexistent or empty; time to create default settings */
            for (LivingGroup livingGroup : getDefaultGroups(JASNametoFMLName)) {
                Property prop = config.getEntityGroupList(livingGroup.saveFormat, livingGroup.contentsToString());
                LivingGroup newlivingGroup = new LivingGroup(livingGroup.groupID);
                JASLog.info("XXX default group %s", livingGroup.groupID);
                for (String jasName : prop.getString().split(",")) {
                    if (!jasName.trim().equals("") && JASNametoFMLName.containsKey(jasName)) {
                        JASLog.info("XXX adding %s to %s", jasName, newlivingGroup.groupID);
                        newlivingGroup.entityJASNames.add(jasName);
                    }
                }
                livingGroups.add(newlivingGroup);
            }
        } else {
            /* Have Children, so don't generate defaults, read settings */
            Map<String, Property> propMap = configCategory.getValues();
            livingGroups.addAll(getGroupsFromProps(propMap, (String) null, configCategory.getQualifiedName()));
            for (ConfigCategory child : configCategory.getChildren()) {
                livingGroups.addAll(getGroupsFromCategory(child));
            }
        }
        for (LivingGroup livingGroup : livingGroups) {
            registerGroup(livingGroup);
        }
        config.save();
    }

    private Set<LivingGroup> getGroupsFromCategory(ConfigCategory configCategory) {
        Set<LivingGroup> livingGroups = new HashSet<LivingGroup>();
        for (ConfigCategory child : configCategory.getChildren()) {
            Map<String, Property> childPropMap = child.getValues();
            String configName = child.getQualifiedName().substring(
                    child.getQualifiedName().lastIndexOf(Configuration.CATEGORY_SPLITTER) + 1,
                    child.getQualifiedName().length());
            livingGroups.addAll(getGroupsFromProps(childPropMap, (String) configName, child.getQualifiedName()));
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
            String groupName = shortConfigName != null && entry.getValue().getName().equals("AtrributeList") ? shortConfigName
                    : entry.getKey();
            if (groupName == null || groupName.trim().equals("")) {
                continue;
            }
            JASLog.debug(Level.INFO, "Parsing entity group %s with %s", groupName, entry.getValue().getString());

            LivingGroup livingGroup = new LivingGroup(groupName, fillConfigName + Configuration.CATEGORY_SPLITTER
                    + groupName);
            String[] entityNames = entry.getValue().getString().split(",");
            for (String entityName : entityNames) {
                if (JASNametoFMLName.containsKey(entityName)) {
                    livingGroup.entityJASNames.add(entityName);
                } else {
                    JASLog.severe("Error while reading entity group %s. Entity name %s does not exist", groupName,
                            entityName);
                }
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

        if (currentPackage.lastIndexOf(Configuration.CATEGORY_SPLITTER) != -1) {
            currentPackage = currentPackage.substring(0, currentPackage.lastIndexOf(Configuration.CATEGORY_SPLITTER));
        }

        for (Entry<Class<?>, String> entry : fmlNames) {
            String packageName = entry.getKey().getName();
            if (packageName.lastIndexOf(Configuration.CATEGORY_SPLITTER) != -1) {
                packageName.substring(0, packageName.lastIndexOf(Configuration.CATEGORY_SPLITTER));
            }

            if (currentPackage.equals(packageName) && entry.getValue().contains(":")) {
                return entry.getValue().split(":")[0];
            }
        }
        String[] currentParsts = currentPackage.split(Configuration.CATEGORY_SPLITTER);
        return currentParsts.length > 1 ? currentParsts[0] : UNKNOWN_PREFIX;
    }

    private Set<LivingGroup> getDefaultGroups(BiMap<String, String> JASNametoFMLName) {
        Set<LivingGroup> livinggroups = new HashSet<LivingGroup>();
        for (String jasName : JASNametoFMLName.keySet()) {
            LivingGroup livingGroup = new LivingGroup(jasName);
            livingGroup.entityJASNames.add(jasName);
            livinggroups.add(livingGroup);
        }
        return livinggroups;
    }

    public void saveToConfig(File configDirectory) {
        LivingGroupConfiguration config = new LivingGroupConfiguration(configDirectory, worldProperties);
        config.load();
        for (Entry<String, String> entry : FMLNametoJASName.entrySet()) {
            Property prop = config.getEntityMapping(entry.getKey(), entry.getValue());
            prop.set(entry.getValue());
        }

        for (LivingGroup group : iDToGroup.values()) {
            Property prop = config.getEntityGroupList(group.saveFormat, group.contentsToString());
            prop.set(group.contentsToString());
        }
        config.save();
    }
}
