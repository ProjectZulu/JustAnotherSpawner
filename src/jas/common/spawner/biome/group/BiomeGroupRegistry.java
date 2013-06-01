package jas.common.spawner.biome.group;

import jas.common.DefaultProps;
import jas.common.JASLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
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

    public void createBiomeGroups(File configDirectory, MinecraftServer minecraftServer) {
        Configuration masterConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "Master/" + "BiomeGroups" + ".cfg"));
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + minecraftServer.worldServers[0].getWorldInfo().getWorldName() + "/" + "BiomeGroups" + ".cfg"));
        masterConfig.load();
        worldConfig.load();

        /* Create Package Name Mappings */
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            String packageName = BiomeHelper.getPackageName(biome);
            String mappingComment = "Custom Name Mapping for Unique Biome Package Name. Mapping is used to add biomes to groups. Each must be unique or biome will be unreachable. Case sensitive.";
            masterConfig.getCategory("biomegroups.packagenamemappings").setComment(mappingComment);
            worldConfig.getCategory("biomegroups.packagenamemappings").setComment(mappingComment);

            Property nameMapping = masterConfig.get("biomegroups.packagenamemappings", packageName, biome.biomeName);
            nameMapping = worldConfig.get("biomegroups.packagenamemappings", packageName, nameMapping.getString());

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
        String customGroupComment = "Custom Group Names. Seperated by Commas. Edit this to add/remove groups";
        Property customGroupProp = masterConfig.get("BiomeGroups.CustomGroups", "Custom Group Names", "",
                customGroupComment);
        customGroupProp = worldConfig.get("BiomeGroups.CustomGroups", "Custom Group Names",
                customGroupProp.getString(), customGroupComment);
        String[] resultgroups = customGroupProp.getString().split(",");
        for (String groupName : resultgroups) {
            biomeGroups.add(new BiomeGroup(groupName));
        }

        /* For Every Biome Group; Filter BiomeList Through Configuration */
        for (BiomeGroup biomeGroup : biomeGroups) {
            getGroupSpawnList(masterConfig, biomeGroup);
            getGroupSpawnList(worldConfig, biomeGroup);
            registerGroup(biomeGroup);
        }

        masterConfig.save();
        worldConfig.save();
    }

    /**
     * Gets the List of All BiomeNames from Configuration Files. They are stored as a single string seperated by Commas.
     * 
     * @param config
     * @param group
     * @return
     */
    private void getGroupSpawnList(Configuration config, BiomeGroup group) {
        String defautlGroupString = "";
        Iterator<String> iterator = group.pckgNames.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            JASLog.info("String %s", string);
            defautlGroupString = defautlGroupString.concat(biomePckgToMapping.get(string));
            if (iterator.hasNext()) {
                defautlGroupString = defautlGroupString.concat(",");
            }
        }
        Property resultProp = config
                .get("BiomeGroups.BiomeLists." + group.groupID, "BiomeList", defautlGroupString,
                        "List of All Biomes Contained in this Group. Format is package mapping (see packagenamemappings) seperated by commas.");

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
}