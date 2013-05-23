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

    /** Cusom Biome Names: Mappings For PackageNames to Unique Names */
    private final HashMap<String, String> biomeIdentifiers = new HashMap<String, String>();

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

        /*
         * By Default Every Biome is a Group. CategoryBiomeName to biomeID. This is used instead of looping through
         * biomList multiple times to tell if a category is a default biomeCategory which would require adding the
         * regular biome to it as a default.
         * 
         * Multimap is used to Accomodate Biomes with IdenticalNames
         */
        ListMultimap<String, Integer> groupIDToBiomeID = ArrayListMultimap.create();
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            groupIDToBiomeID.put(biome.biomeName.toLowerCase(), biome.biomeID);
            pckgNameToBiomeID.put(BiomeHelper.getPackageName(biome), biome.biomeID);
            /* Write Biome Name --> Package Names to Configuration for End User Reference */
            masterConfig.get("BiomeGroups.ReferenceBiomes", biome.biomeName, BiomeHelper.getPackageName(biome));
            worldConfig.get("BiomeGroups.ReferenceBiomes", biome.biomeName, BiomeHelper.getPackageName(biome));
        }
        Set<String> groupNames = getCustomBiomeGroups(worldConfig,
                getCustomBiomeGroups(masterConfig, Collections.<String> emptySet()));

        /* Create BiomeGroups */
        ArrayList<BiomeGroup> biomeGroups = new ArrayList<BiomeGroupRegistry.BiomeGroup>();
        for (String groupName : groupIDToBiomeID.keySet()) {
            biomeGroups.add(new BiomeGroup(groupName));
        }
        for (String groupName : groupNames) {
            if (groupIDToBiomeID.get(groupName).isEmpty()) {
                biomeGroups.add(new BiomeGroup(groupName));
            }
        }

        /* Foreach Biome Group */
        for (BiomeGroup biomeGroup : biomeGroups) {
            /* Get Default BiomeNameList */
            if (groupIDToBiomeID.get(biomeGroup.groupID) != null) {
                for (Integer biomeID : groupIDToBiomeID.get(biomeGroup.groupID)) {
                    biomeGroup.pckgNames.add(BiomeHelper.getPackageName(BiomeGenBase.biomeList[biomeID]));
                }
            }
            /* Filter List Through Configuration */
            getGroupSpawnList(worldConfig, getGroupSpawnList(masterConfig, biomeGroup, pckgNameToBiomeID),
                    pckgNameToBiomeID);
            registerGroup(biomeGroup);
        }
        masterConfig.save();
        worldConfig.save();
    }

    /**
     * Gets the List of All BiomeGroups from Configuration Files. They are stored as a single string seperated by Commas
     * 
     * @param config Configuration files to save/load from
     * @param defautlGroups Set of Default BiomeGroup groupIDs
     * @return Set of BiomeGroup groupIDs
     */
    private Set<String> getCustomBiomeGroups(Configuration config, Set<String> defautlGroups) {
        String defautlGroupString = "";
        Iterator<String> iterator = defautlGroups.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            defautlGroupString = defautlGroupString.concat(string);
            if (iterator.hasNext()) {
                defautlGroupString = defautlGroupString.concat(",");
            }
        }
        Property resultProp = config.get("BiomeGroups.CustomGroups", "Custom Group Names", defautlGroupString,
                "All Group Names. Seperated by Commas. Edit this to add/remove groups.");
        String resultGroupString = resultProp.getString();
        String[] resultgroups = resultGroupString.split(",");
        Set<String> biomeGroups = new HashSet<String>();
        for (String groupName : resultgroups) {
            if (groupName.equals("")) {
                continue;
            }
            biomeGroups.add(groupName);
        }
        return biomeGroups;
    }

    /**
     * Gets the List of All BiomeNames from Configuration Files. They are stored as a single string seperated by Commas
     * 
     * @param config
     * @param group
     * @param pckgNameToBiomeID Hashmap of BiomePackageNames used to Ensure only Valid Biomes are added. Used in place
     *            of Repeatedly looping though BiomeGenList
     * @return
     */
    private BiomeGroup getGroupSpawnList(Configuration config, BiomeGroup group,
            ListMultimap<String, Integer> pckgNameToBiomeID) {
        String defautlGroupString = "";
        Iterator<String> iterator = group.pckgNames.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            defautlGroupString = defautlGroupString.concat(string);
            if (iterator.hasNext()) {
                defautlGroupString = defautlGroupString.concat(",");
            }
        }
        Property resultProp = config.get("BiomeGroups.BiomeLists." + group.groupID, "BiomeList", defautlGroupString);

        String resultGroupString = resultProp.getString();
        String[] resultgroups = resultGroupString.split(",");
        Set<String> biomeNames = new HashSet<String>();
        for (String name : resultgroups) {
            if (!pckgNameToBiomeID.containsKey(name)) {
                continue;
            }
            biomeNames.add(name);
        }
        group.pckgNames.clear();
        group.pckgNames.addAll(biomeNames);
        return group;
    }
}