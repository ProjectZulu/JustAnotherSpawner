package jas.common.spawner.creature.handler;

import jas.common.FileUtilities;
import jas.common.GsonHelper;
import jas.common.JASLog;
import jas.common.TopologicalSort;
import jas.common.TopologicalSort.DirectedGraph;
import jas.common.TopologicalSortingException;
import jas.common.WorldProperties;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;
import jas.common.spawner.creature.handler.LivingGroupSaveObject.LivingGroupSaveObjectSerializer;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

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
    private ImmutableMap<String, LivingGroup> iDToGroup;

    public Collection<LivingGroup> getEntityGroups() {
        return iDToGroup.values();
    }

    public LivingGroup getLivingGroup(String groupID) {
        return iDToGroup.get(groupID);
    }

    public Class<? extends EntityLiving> getRandomEntity(String livingGroupID, Random random) {
        LivingGroup livingGroup = getLivingGroup(livingGroupID);
        if (!livingGroup.entityJASNames.isEmpty()) {
            int selectedEntry = random.nextInt(livingGroup.entityJASNames.size());
            int i = 0;
            for (String jasName : livingGroup.entityJASNames) {
                if (i++ == selectedEntry) {
                    return this.JASNametoEntityClass.get(jasName);
                }
            }
        }
        return null;
    }

    /** Reverse Look-up Map to Get All Groups a Particular Entity is In */
    private ImmutableListMultimap<String, String> entityIDToGroupIDList;

    public ImmutableMultimap<String, String> getEntityIDToGroupIDList() {
        return entityIDToGroupIDList;
    }

    public ImmutableCollection<String> getGroupsWithEntity(String jasName) {
        return entityIDToGroupIDList.get(jasName);
    }

    public List<LivingGroup> getEntityGroups(Class<? extends EntityLiving> entityClass) {
        String jasName = EntityClasstoJASName.get(entityClass);
        List<LivingGroup> list = new ArrayList<LivingGroup>();
        for (String groupID : entityIDToGroupIDList.get(jasName)) {
            list.add(iDToGroup.get(groupID));
        }
        return list;
    }

    /** Group Identifier to Group Instance */
    private ImmutableMap<String, LivingGroup> iDToAttribute;

    public ImmutableMap<String, LivingGroup> iDToAttribute() {
        return iDToAttribute;
    }

    /* Mapping From Entity Name (via EntityList.classToString) to JAS Name */
    public ImmutableBiMap<Class<? extends EntityLiving>, String> EntityClasstoJASName;

    public ImmutableBiMap<Class<? extends EntityLiving>, String> entityClasstoJASName() {
        return EntityClasstoJASName;
    }

    /* Mapping From JAS Name to Entity Name (via EntityList.classToString) */
    public ImmutableBiMap<String, Class<? extends EntityLiving>> JASNametoEntityClass;

    public ImmutableBiMap<String, Class<? extends EntityLiving>> jasNametoEntityClass() {
        return JASNametoEntityClass;
    }

    private WorldProperties worldProperties;

    public LivingGroupRegistry(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    public static class LivingGroup {
        public final String groupID;
        public final String configName; // TODO Change COnfigName to be transient and fetch its value from the map on
                                        // loading
        private final transient Set<String> entityJASNames = new HashSet<String>();
        /* String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
        private final List<String> contents;

        public LivingGroup() {
            this.groupID = "";
            this.configName = "";
            contents = new ArrayList<String>();
        }

        public LivingGroup(String groupID) {
            if (groupID == null || groupID.trim().equals("")) {
                throw new IllegalArgumentException("Group ID cannot be " + groupID == null ? "null" : "empty");
            }
            this.groupID = groupID;
            String[] parts = groupID.split("\\.");
            if (parts.length > 1) {
                this.configName = parts[0];
            } else {
                this.configName = "";
            }
            contents = new ArrayList<String>();
        }

        public LivingGroup(String groupID, String configName, ArrayList<String> contents) {
            this.groupID = groupID;
            this.configName = configName;
            this.contents = new ArrayList<String>(contents);
        }

        public Set<String> entityJASNames() {
            return Collections.unmodifiableSet(entityJASNames);
        }

        public List<String> contents() {
            return Collections.unmodifiableList(contents);
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
        Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { LivingGroupSaveObject.class },
                new Object[] { new LivingGroupSaveObjectSerializer() });

        File gsonBiomeFile = LivingGroupSaveObject.getFile(configDirectory,
                worldProperties.getFolderConfiguration().saveName);
        LivingGroupSaveObject savedStats = GsonHelper.readOrCreateFromGson(FileUtilities.createReader(gsonBiomeFile, false),
                LivingGroupSaveObject.class, gson);

        List<String> newJASNames = loadMappings(savedStats);
        loadAttributes(savedStats);
        loadBiomes(savedStats, newJASNames);
    }

    private List<String> loadMappings(LivingGroupSaveObject savedStats) {
        List<String> newJASNames = new ArrayList<String>();
        BiMap<Class<? extends EntityLiving>, String> entityClassToJASNameBuilder = HashBiMap.create();
        for (Entry<String, String> entry : savedStats.fmlToJASName.entrySet()) {
            Class<?> entityClass = (Class<?>) EntityList.stringToClassMapping.get(entry.getKey());
            if (!EntityLiving.class.isAssignableFrom(entityClass) || Modifier.isAbstract(entityClass.getModifiers())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<? extends EntityLiving> livingClass = (Class<? extends EntityLiving>) entityClass;
            entityClassToJASNameBuilder.put(livingClass, entry.getValue());
        }
        @SuppressWarnings("unchecked")
        Set<Entry<Class<?>, String>> fmlNames = EntityList.classToStringMapping.entrySet();
        for (Entry<Class<?>, String> entry : fmlNames) {
            if (!EntityLiving.class.isAssignableFrom(entry.getKey())
                    || Modifier.isAbstract(entry.getKey().getModifiers())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<? extends EntityLiving> livingClass = (Class<? extends EntityLiving>) entry.getKey();
            String jasName;
            if (entry.getValue().contains(".")) {
                jasName = entry.getValue();
            } else {
                String prefix = guessPrefix(entry.getKey(), fmlNames);
                jasName = prefix.trim().equals("") ? entry.getValue() : prefix + "." + entry.getValue();
            }
            if (entityClassToJASNameBuilder.containsKey(livingClass)) {
                JASLog.log().severe(
                        "Duplicate entity class detected. Ignoring FML,JasName pair [%s,%s] for class, class %s",
                        entry.getValue(), jasName, livingClass);
            } else if (entityClassToJASNameBuilder.values().contains(jasName)) {
                JASLog.log()
                        .severe("Duplicate entity mapping reading config. Mapping JASname [%s]'s new key will be ignored: [FMLname:class]=[%s:%s]",
                                jasName, entry.getValue(), livingClass);
            } else {
                newJASNames.add(jasName);
                entityClassToJASNameBuilder.forcePut(livingClass, jasName);
            }
        }

        EntityClasstoJASName = ImmutableBiMap.<Class<? extends EntityLiving>, String> builder()
                .putAll(entityClassToJASNameBuilder).build();
        JASNametoEntityClass = EntityClasstoJASName.inverse();
        return newJASNames;
    }

    private void loadAttributes(LivingGroupSaveObject savedStats) {
        Set<LivingGroup> attributeGroups = new HashSet<LivingGroup>();
        if (savedStats.configNameToAttributeGroups.isPresent()) {
            Collection<TreeMap<String, LivingGroup>> mapOfGroups = savedStats.configNameToAttributeGroups.get()
                    .values();
            for (TreeMap<String, LivingGroup> treeMap : mapOfGroups) {
                for (LivingGroup attributeGroup : treeMap.values()) {
                    if (!"".equals(attributeGroup.groupID)) {
                        attributeGroups.add(attributeGroup);
                    }
                }
            }
        }
        List<LivingGroup> sortedAttributes = getSortedGroups(attributeGroups);
        HashMap<String, LivingGroup> iDToAttributeBuilder = new HashMap<String, LivingGroup>();
        for (LivingGroup livingGroup : sortedAttributes) {
            parseGroupContents(livingGroup);
            iDToAttributeBuilder.put(livingGroup.groupID, livingGroup);
        }
        this.iDToAttribute = ImmutableMap.<String, LivingGroupRegistry.LivingGroup> builder()
                .putAll(iDToAttributeBuilder).build();
    }

    private void loadBiomes(LivingGroupSaveObject savedStats, List<String> newJASNames) {
        Set<LivingGroup> livingGroups = new HashSet<LivingGroup>();
        if (savedStats.configNameToLivingGroups.isPresent()) {
            Collection<TreeMap<String, LivingGroup>> mapOfGroups = savedStats.configNameToLivingGroups.get().values();
            for (TreeMap<String, LivingGroup> treeMap : mapOfGroups) {
                for (LivingGroup biomeGroup : treeMap.values()) {
                    if (!"".equals(biomeGroup.groupID)) {
                        livingGroups.add(biomeGroup);
                    }
                }
            }
            for (String jasName : newJASNames) {
                LivingGroup livingGroup = new LivingGroup(jasName);
                livingGroup.contents.add(jasName);
                livingGroups.add(livingGroup);
            }
        } else {
            for (String jasName : JASNametoEntityClass.keySet()) {
                LivingGroup livingGroup = new LivingGroup(jasName);
                livingGroup.contents.add(jasName);
                livingGroups.add(livingGroup);
            }
        }
        List<LivingGroup> sortedGroups = getSortedGroups(livingGroups);
        HashMap<String, LivingGroup> iDToGroupBuilder = new HashMap<String, LivingGroup>();
        ListMultimap<String, String> entityIDToGroupIDListBuild = ArrayListMultimap.create();
        for (LivingGroup livingGroup : sortedGroups) {
            parseGroupContents(livingGroup);
            JASLog.log().info("Registering EntityGroup %s", livingGroup.toString());

            iDToGroupBuilder.put(livingGroup.groupID, livingGroup);
            for (String jasName : livingGroup.entityJASNames) {
                entityIDToGroupIDListBuild.get(jasName).add(livingGroup.groupID);
            }
        }
        this.iDToGroup = ImmutableMap.<String, LivingGroupRegistry.LivingGroup> builder().putAll(iDToGroupBuilder)
                .build();
        this.entityIDToGroupIDList = ImmutableListMultimap.<String, String> builder()
                .putAll(entityIDToGroupIDListBuild).build();
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
            JASLog.log().severe(
                    "A circular reference was detected when processing entity groups. Groups in the cycle were: ");
            int i = 1;
            for (LivingGroup invalidGroups : exceptionData.getVisitedNodes()) {
                JASLog.log().severe("Group %s: %s containing %s", i++, invalidGroups.groupID,
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
            JASLog.log().severe("Error processing %s content from %s. The component %s does not exist.",
                    livingGroup.groupID, livingGroup.contentsToString(), contentComponent);
        }
    }

    public void saveToConfig(File configDirectory) {
        Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { LivingGroupSaveObject.class },
                new Object[] { new LivingGroupSaveObjectSerializer() });

        File gsonBiomeFile = LivingGroupSaveObject.getFile(configDirectory,
                worldProperties.getFolderConfiguration().saveName);

        LivingGroupSaveObject biomeGroupAuthor = new LivingGroupSaveObject(EntityClasstoJASName,
                iDToAttribute.values(), iDToGroup.values());
        GsonHelper.writeToGson(FileUtilities.createWriter(gsonBiomeFile, true), biomeGroupAuthor, gson);
    }
}
