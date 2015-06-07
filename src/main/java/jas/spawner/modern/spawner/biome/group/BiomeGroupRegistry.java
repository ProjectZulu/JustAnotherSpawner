package jas.spawner.modern.spawner.biome.group;

import jas.common.JASLog;
import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.common.helper.sort.TopologicalSort;
import jas.common.helper.sort.TopologicalSort.DirectedGraph;
import jas.common.helper.sort.TopologicalSortingException;
import jas.spawner.modern.math.SetAlgebra;
import jas.spawner.modern.math.SetAlgebra.OPERATION;
import jas.spawner.modern.spawner.biome.group.BiomeGroupSaveObject.BiomeGroupSaveObjectSerializer;
import jas.spawner.modern.world.WorldProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.toposort.ModSortingException.SortingExceptionData;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class BiomeGroupRegistry {
	private ImmutableMap<String, BiomeGroup> iDToGroup;

	/** New Mappings added the Last Time BiomeGroupRegistry.load() was run */
	public Set<String> newMappings = new HashSet<String>();

	/** Group Identifier to Group Instance Regsitry */
	public ImmutableMap<String, BiomeGroup> iDToGroup() {
		return iDToGroup;
	}

	public BiomeGroup getBiomeGroup(String groupID) {
		return iDToGroup.get(groupID);
	}

	private ImmutableListMultimap<String, String> packgNameToGroupIDs;

	/** Reverse Look-up Map to Get All Groups a Particular Biome is In */
	public ImmutableMultimap<String, String> packgNameToGroupIDs() {
		return packgNameToGroupIDs;
	}

	private ImmutableListMultimap<String, String> packgNameToAttribIDs;

	/** Reverse Look-up Map to Get All Attributes a Particular Biome is In */
	public ImmutableMultimap<String, String> packgNameToAttribIDs() {
		return packgNameToAttribIDs;
	}

	private ImmutableBiMap<String, String> biomeMappingToPckg;

	/**
	 * Cusom Biome Names: Mappings For CustomBiomeNames to PackageNames used to read from configuration
	 */
	public ImmutableBiMap<String, String> biomeMappingToPckg() {
		return biomeMappingToPckg;
	}

	private ImmutableBiMap<String, String> biomePckgToMapping;

	/**
	 * Cusom Biome Names: Mappings For PackageNames to CustomBiomeNames used to write to configuration
	 */
	public ImmutableBiMap<String, String> biomePckgToMapping() {
		return biomePckgToMapping;
	}

	private ImmutableListMultimap<String, Integer> pckgNameToBiomeID;

	/**
	 * Reverse Look-up to get access the BiomeGenBase instances from the Biome Package Names
	 */
	public ImmutableListMultimap<String, Integer> pckgNameToBiomeID() {
		return pckgNameToBiomeID;
	}

	private ImmutableMap<String, BiomeGroup> iDToAttribute;

	/** Mapping Between AttributeID and the Biomes it Represents. */
	public ImmutableMap<String, BiomeGroup> iDToAttribute() {
		return iDToAttribute;
	}

	private final WorldProperties worldProperties;

	public BiomeGroupRegistry(WorldProperties worldProperties) {
		this.worldProperties = worldProperties;
	}

	public static class BiomeGroup {
		public final String groupID;
		public final String configName;
		private final transient Set<String> pckgNames = new HashSet<String>();
		/*
		 * String Used to Build Group Content Names i.e. {desert,A|Forest,glacier}
		 */
		private final ArrayList<String> contents;

		public BiomeGroup() {
			this.groupID = "";
			this.configName = "";
			contents = new ArrayList<String>();
		}

		public BiomeGroup(String groupID) {
			this.groupID = groupID;
			String[] parts = groupID.split("\\.");
			if (parts.length > 1) {
				this.configName = parts[0];
			} else {
				this.configName = "";
			}
			contents = new ArrayList<String>();
		}

		public BiomeGroup(String groupID, String configName, ArrayList<String> contents) {
			this.groupID = groupID;
			this.configName = configName;
			this.contents = new ArrayList<String>(contents);
		}

		public List<String> getContents() {
			return Collections.unmodifiableList(contents);
		}

		public Set<String> getBiomeNames() {
			return Collections.unmodifiableSet(pckgNames);
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
		Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { BiomeGroupSaveObject.class },
				new Object[] { new BiomeGroupSaveObjectSerializer() });
		// Gson gson = new
		// GsonBuilder().setVersion(DefaultProps.GSON_VERSION).setPrettyPrinting().create();
		File gsonBiomeFile = BiomeGroupSaveObject.getFile(configDirectory,
				worldProperties.getFolderConfiguration().saveName);
		BiomeGroupSaveObject savedStats = GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(gsonBiomeFile, false), BiomeGroupSaveObject.class, gson);
		/* Build Reverse Lookup */
		ArrayListMultimap<String, Integer> pckgNameToBiomeIDBuilder = ArrayListMultimap.create();
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null) {
				pckgNameToBiomeIDBuilder.put(BiomeHelper.getPackageName(biome), biome.biomeID);
			}
		}
		pckgNameToBiomeID = ImmutableListMultimap.<String, Integer> builder().putAll(pckgNameToBiomeIDBuilder).build();
		
		newMappings = new HashSet<String>(loadMappings(savedStats));
		loadAttributes(savedStats);
		loadBiomeGroups(savedStats, newMappings);
	}

	/**
	 * @return Set of new mappings added
	 */
	private Set<String> loadMappings(BiomeGroupSaveObject savedStats) {
		Set<String> newMappings = new HashSet<String>();
		HashBiMap<String, String> biomeMappingToPckgBuilder = HashBiMap.create();
		BiMap<String, String> biomePckgToMappingBuilder = biomeMappingToPckgBuilder.inverse();
		biomePckgToMappingBuilder.putAll(savedStats.biomeMappings);
		// Check for Missing Mappings, keep track of them to create default
		// groups
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome == null) {
				continue;
			}
			String packageName = BiomeHelper.getPackageName(biome);
			if (!biomePckgToMappingBuilder.containsKey(packageName)) {
				String defaultMapping = biome.biomeName;
				// TODO: Method getUnusedMapping();
				int attempts = 0;
				while (defaultMapping == null || biomeMappingToPckgBuilder.containsKey(defaultMapping)) {
					defaultMapping = BiomeHelper.getShortPackageName(biome);
					if (attempts > 0) {
						// For multiple tries, concat the number of the attempts
						// to create a unique mapping
						defaultMapping = defaultMapping + "_" + attempts;
					}
					attempts++;
				}
				if (attempts > 0) {
					JASLog.log().info("Duplicate mapping %s and was renamed to %s.", biome.biomeName, defaultMapping);
				}
				biomePckgToMappingBuilder.put(packageName, defaultMapping);
				newMappings.add(defaultMapping);
			}
		}

		biomeMappingToPckg = ImmutableBiMap.<String, String> builder().putAll(biomeMappingToPckgBuilder).build();
		biomePckgToMapping = biomeMappingToPckg.inverse();
		return newMappings;
	}

	private void loadAttributes(BiomeGroupSaveObject savedStats) {
		Set<BiomeGroup> attributeGroups = new HashSet<BiomeGroup>();
		if (savedStats.getConfigNameToAttributeGroups().isPresent()) {
			for (TreeMap<String, BiomeGroup> entries : savedStats.getConfigNameToAttributeGroups().get().values()) {
				for (BiomeGroup attributeGroup : entries.values()) {
					if (!"".equals(attributeGroup.groupID)) {
						attributeGroups.add(attributeGroup);
					}
				}
			}
		} else {
			/* Get Default Groups From BiomeDictionary */
			for (Type type : BiomeDictionary.Type.values()) {
				BiomeGroup attributeGroup = new BiomeGroup(type.toString());
				for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) {
					attributeGroup.contents.add(biomePckgToMapping.get(BiomeHelper.getPackageName(biome)));
				}
				attributeGroups.add(attributeGroup);
			}
		}
		HashMap<String, BiomeGroup> iDToAttributeBuilder = new HashMap<String, BiomeGroup>();
		List<BiomeGroup> sortedAttributes = getSortedGroups(attributeGroups);
		ListMultimap<String, String> packgNameToAttribIDsBuilder = ArrayListMultimap.create();

		/*
		 * Evaluate and register groups. i.e. from group form A|allbiomes,&Jungle to individual jasNames
		 */
		for (BiomeGroup biomeGroup : sortedAttributes) {
			parseGroupContents(biomeGroup, null, iDToAttributeBuilder);
			// JASLog.log().info("Registering Attribute %s",
			// biomeGroup.toString());
			iDToAttributeBuilder.put(biomeGroup.groupID, biomeGroup);
			for (String pckgName : biomeGroup.pckgNames) {
				packgNameToAttribIDsBuilder.get(pckgName).add(biomeGroup.groupID);
			}
		}
		packgNameToAttribIDs = ImmutableListMultimap.<String, String> builder().putAll(packgNameToAttribIDsBuilder)
				.build();
		iDToAttribute = ImmutableMap.<String, BiomeGroup> builder().putAll(iDToAttributeBuilder).build();
	}

	private void loadBiomeGroups(BiomeGroupSaveObject savedStats, Set<String> newMappings) {
		Set<BiomeGroup> biomeGroups = new HashSet<BiomeGroup>();
		if (savedStats.getConfigNameToBiomeGroups().isPresent()) {
			for (TreeMap<String, BiomeGroup> entries : savedStats.getConfigNameToBiomeGroups().get().values()) {
				for (BiomeGroup biomeGroup : entries.values()) {
					if (!"".equals(biomeGroup.groupID)) {
						biomeGroups.add(biomeGroup);
					}
				}
			}
			
		}
		for (String biomeMapping : newMappings) {
			BiomeGroup group = new BiomeGroup(biomeMapping);
			group.contents.add(biomeMapping);
			biomeGroups.add(group);
		}
		
		List<BiomeGroup> sortedGroups = getSortedGroups(biomeGroups);
		HashMap<String, BiomeGroup> iDToGroupBuilder = new HashMap<String, BiomeGroup>();
		ListMultimap<String, String> packgNameToGroupIDsBuilder = ArrayListMultimap.create();

		/*
		 * Evaluate and register groups. i.e. from group form A|allbiomes,&Jungle to individual jasNames
		 */
		for (BiomeGroup biomeGroup : sortedGroups) {
			parseGroupContents(biomeGroup, iDToGroupBuilder, iDToAttribute);
			if (biomeGroup.pckgNames.size() > 0) {
				JASLog.log().info("Registering BiomeGroup %s", biomeGroup.toString());
				iDToGroupBuilder.put(biomeGroup.groupID, biomeGroup);
				for (String pckgName : biomeGroup.pckgNames) {
					packgNameToGroupIDsBuilder.get(pckgName).add(biomeGroup.groupID);
				}
			}
			iDToGroupBuilder.put(biomeGroup.groupID, biomeGroup);
		}
		packgNameToGroupIDs = ImmutableListMultimap.<String, String> builder().putAll(packgNameToGroupIDsBuilder)
				.build();
		iDToGroup = ImmutableMap.<String, BiomeGroup> builder().putAll(iDToGroupBuilder).build();
	}

	private List<BiomeGroup> getSortedGroups(Collection<BiomeGroup> groupsToSort) {
		/*
		 * Evaluate each group, ensuring entries are valid mappings or Groups and
		 */
		DirectedGraph<BiomeGroup> groupGraph = new DirectedGraph<BiomeGroup>();
		for (BiomeGroup group : groupsToSort) {
			groupGraph.addNode(group);
		}
		for (BiomeGroup currentGroup : groupsToSort) {
			for (String contentComponent : currentGroup.contents) {
				for (BiomeGroup possibleGroup : groupsToSort) {
					// Reminder: substring(2) is to remove mandatory A| and G|
					// for groups
					// Reminder: substring(1) is to remove operator +,-,&?
					String nameWithoutOperators;
					if (contentComponent.startsWith("-") || contentComponent.startsWith("+")
							|| contentComponent.startsWith("&")) {
						nameWithoutOperators = contentComponent.substring(3);
					} else {
						nameWithoutOperators = contentComponent.substring(2);
					}
					if (nameWithoutOperators.equals(possibleGroup.groupID)) {
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
			JASLog.log().severe(
					"A circular reference was detected when processing entity groups. Groups in the cycle were: ");
			int i = 1;
			for (BiomeGroup invalidGroups : exceptionData.getVisitedNodes()) {
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
	private void parseGroupContents(BiomeGroup biomeGroup, Map<String, BiomeGroup> iDToGroupBuilder,
			Map<String, BiomeGroup> iDToAttributeBuilder) {
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
				BiomeGroup groupToAdd = iDToGroupBuilder.get(contentComponent.substring(2));
				if (groupToAdd != null) {
					SetAlgebra.operate(biomeGroup.pckgNames, groupToAdd.pckgNames, operation);
					continue;
				}
			} else if (contentComponent.startsWith("A|")) {
				BiomeGroup groupToAdd = iDToAttributeBuilder.get(contentComponent.substring(2));
				if (groupToAdd != null) {
					SetAlgebra.operate(biomeGroup.pckgNames, groupToAdd.pckgNames, operation);
					continue;
				}
			} else if (biomeMappingToPckg.containsKey(contentComponent)) {
				SetAlgebra.operate(biomeGroup.pckgNames, Sets.newHashSet(biomeMappingToPckg.get(contentComponent)),
						operation);
				continue;
			}
			JASLog.log().severe("Error processing %s content from %s. The component %s does not exist.",
					biomeGroup.groupID, biomeGroup.contentsToString(), contentComponent);
		}
	}

	/**
	 * Used to save the currently loaded settings into the Configuration Files
	 * 
	 * If config settings are already present, they will be overwritten
	 */
	public void saveToConfig(File configDirectory) {
		// Gson gson = new
		// GsonBuilder().setVersion(DefaultProps.GSON_VERSION).setPrettyPrinting().create();
		Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { BiomeGroupSaveObject.class },
				new Object[] { new BiomeGroupSaveObjectSerializer() });
		File gsonBiomeFile = BiomeGroupSaveObject.getFile(configDirectory,
				worldProperties.getFolderConfiguration().saveName);
		BiomeGroupSaveObject biomeGroupAuthor = new BiomeGroupSaveObject(biomePckgToMapping, iDToAttribute.values(),
				iDToGroup.values());
		GsonHelper.writeToGson(FileUtilities.createWriter(gsonBiomeFile, true), biomeGroupAuthor, gson);
	}

	public void addBiomeGroup(String groupName, ArrayList<String> contents) {
		BiomeGroup newGroup = new BiomeGroup(groupName);
		newGroup.contents.addAll(contents);
		addBiomeGroup(newGroup);

		// private ImmutableMap<String, BiomeGroup> iDToGroup;
		// private ImmutableListMultimap<String, String> packgNameToGroupIDs;

		// private ImmutableListMultimap<String, String> packgNameToAttribIDs;
		// private ImmutableBiMap<String, String> biomeMappingToPckg;
		// private ImmutableBiMap<String, String> biomePckgToMapping;
		// private ImmutableListMultimap<String, Integer> pckgNameToBiomeID;
		// private ImmutableMap<String, BiomeGroup> iDToAttribute;
	}

	public void addBiomeGroup(String groupName, String configName, ArrayList<String> contents) {
		BiomeGroup newGroup = new BiomeGroup(groupName, configName, contents);
		addBiomeGroup(newGroup);
	}

	public void addBiomeGroup(BiomeGroup newGroup) {
		Set<BiomeGroup> biomeGroups = new HashSet<BiomeGroup>(iDToGroup.values());
		biomeGroups.add(newGroup);

		List<BiomeGroup> sortedGroups = getSortedGroups(biomeGroups);
		HashMap<String, BiomeGroup> iDToGroupBuilder = new HashMap<String, BiomeGroup>();
		ListMultimap<String, String> packgNameToGroupIDsBuilder = ArrayListMultimap.create();

		/*
		 * Evaluate and register groups. i.e. from group form A|allbiomes,&Jungle to individual jasNames
		 */
		for (BiomeGroup biomeGroup : sortedGroups) {
			parseGroupContents(biomeGroup, iDToGroupBuilder, iDToAttribute);
			if (biomeGroup.pckgNames.size() > 0) {
				JASLog.log().info("Registering BiomeGroup %s", biomeGroup.toString());
				iDToGroupBuilder.put(biomeGroup.groupID, biomeGroup);
				for (String pckgName : biomeGroup.pckgNames) {
					packgNameToGroupIDsBuilder.get(pckgName).add(biomeGroup.groupID);
				}
			}
			iDToGroupBuilder.put(biomeGroup.groupID, biomeGroup);
		}
		packgNameToGroupIDs = ImmutableListMultimap.<String, String> builder().putAll(packgNameToGroupIDsBuilder)
				.build();
		iDToGroup = ImmutableMap.<String, BiomeGroup> builder().putAll(iDToGroupBuilder).build();
	}

	public void removeBiomeGroup(String biomeGroupID) {
		removeBiomeGroup(new BiomeGroup(biomeGroupID));
	}

	public void removeBiomeGroup(BiomeGroup newGroup) {
		Set<BiomeGroup> biomeGroups = new HashSet<BiomeGroup>(iDToGroup.values());
		biomeGroups.remove(newGroup);

		List<BiomeGroup> sortedGroups = getSortedGroups(biomeGroups);
		HashMap<String, BiomeGroup> iDToGroupBuilder = new HashMap<String, BiomeGroup>();
		ListMultimap<String, String> packgNameToGroupIDsBuilder = ArrayListMultimap.create();

		/*
		 * Evaluate and register groups. i.e. from group form A|allbiomes,&Jungle to individual jasNames
		 */
		for (BiomeGroup biomeGroup : sortedGroups) {
			parseGroupContents(biomeGroup, iDToAttribute, iDToGroupBuilder);
			if (biomeGroup.pckgNames.size() > 0) {
				JASLog.log().info("Registering BiomeGroup %s", biomeGroup.toString());
				iDToGroupBuilder.put(biomeGroup.groupID, biomeGroup);
				for (String pckgName : biomeGroup.pckgNames) {
					packgNameToGroupIDsBuilder.get(pckgName).add(biomeGroup.groupID);
				}
			}
			iDToGroupBuilder.put(biomeGroup.groupID, biomeGroup);
		}
		packgNameToGroupIDs = ImmutableListMultimap.<String, String> builder().putAll(packgNameToGroupIDsBuilder)
				.build();
		iDToGroup = ImmutableMap.<String, BiomeGroup> builder().putAll(iDToGroupBuilder).build();
	}

	public void updateBiomeGroup(String prevBiomeGroupId, String groupName, ArrayList<String> contents) {
		BiomeGroup newGroup = new BiomeGroup(groupName);
		newGroup.contents.addAll(contents);
		updateBiomeGroup(prevBiomeGroupId, newGroup);
	}

	public void updateBiomeGroup(String prevBiomeGroupId, String groupName, String configName,
			ArrayList<String> contents) {
		updateBiomeGroup(prevBiomeGroupId, new BiomeGroup(groupName, configName, contents));
	}

	public void updateBiomeGroup(String prevBiomeGroupId, BiomeGroup newGroup) {
		removeBiomeGroup(prevBiomeGroupId);
		addBiomeGroup(newGroup);
	}
}