package jas.common.spawner.biome.group;

import jas.common.DefaultProps;
import jas.common.JASLog;

import java.io.File;
import java.util.ArrayList;
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
import com.google.common.collect.ListMultimap;

public enum BiomeGroupRegistry {
    INSTANCE;

    private final HashMap<String, BiomeGroup> iDToGroup = new HashMap<String, BiomeGroup>();
    private final ListMultimap<String, String> biomeNameToGroupIDList = ArrayListMultimap.create();

    /**
     * Should Only Be Used to Register BiomeGroups with their finished
     */
    public void registerGroup(BiomeGroup group) {
        JASLog.info("Registering BiomeGroup %s", group.groupID);
        iDToGroup.put(group.groupID, group);
        biomeNameToGroupIDList.get(group.groupID).addAll(group.biomeNames);
    }

    public static class BiomeGroup {
        private final String groupID;
        private final List<String> biomeNames = new ArrayList<String>();

        public BiomeGroup(String groupID) {
            this.groupID = groupID.toLowerCase();
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

        /*
         * By Default Every Biome is a Group. CategoryBiomeName to biomeID. This is used instead of looping through
         * biomList multiple times to tell if a category is a default biomeCategory which would require adding the
         * regular biome to it as a default.
         * 
         * Multimap is used to Accomodate Biomes with IdenticalNames
         */
        ListMultimap<String, Integer> groupIDToBiomeID = ArrayListMultimap.create();
        /* Reverse Loopup Used to Filter User Submitted BiomeNames to Ensure they correspond to actual Biomes */
        ListMultimap<String, Integer> pckgNameToBiomeID = ArrayListMultimap.create();
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            groupIDToBiomeID.put(biome.biomeName.toLowerCase(), biome.biomeID);
            pckgNameToBiomeID.put(BiomeHelper.getPackageName(biome), biome.biomeID);
        }

        /* Filter Through Config to Get Actual list */
        Configuration masterConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "Master/" + "BiomeGroups" + ".cfg"));
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + minecraftServer.worldServers[0].getWorldInfo().getWorldName() + "/" + "BiomeGroups" + ".cfg"));
        masterConfig.load();
        worldConfig.load();
        Set<String> groupNames = getBiomeGroups(worldConfig, getBiomeGroups(masterConfig, groupIDToBiomeID.keySet()));

        /* Create BiomeGroups */
        ArrayList<BiomeGroup> biomeGroups = new ArrayList<BiomeGroupRegistry.BiomeGroup>();
        for (String groupName : groupNames) {
            JASLog.info("Group Created %s", groupName);
            biomeGroups.add(new BiomeGroup(groupName));
        }

        /* Foreach Biome Group */
        for (BiomeGroup biomeGroup : biomeGroups) {
            /* Get Default BiomeNameList */
            if (groupIDToBiomeID.get(biomeGroup.groupID) != null) {
                for (Integer biomeID : groupIDToBiomeID.get(biomeGroup.groupID)) {
                    JASLog.info("Registering BiomeKey %s to Group %s ",
                            BiomeHelper.getPackageName(BiomeGenBase.biomeList[biomeID]), biomeGroup.groupID);
                    biomeGroup.biomeNames.add(BiomeHelper.getPackageName(BiomeGenBase.biomeList[biomeID]));
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
    private Set<String> getBiomeGroups(Configuration config, Set<String> defautlGroups) {
        String defautlGroupString = "";
        Iterator<String> iterator = defautlGroups.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            defautlGroupString = defautlGroupString.concat(string);
            if (iterator.hasNext()) {
                defautlGroupString = defautlGroupString.concat(",");
            }
        }
        Property resultProp = config.get("BiomeGroups.AllBiomes", "BiomeGroups", defautlGroupString);
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
        Iterator<String> iterator = group.biomeNames.iterator();
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
        group.biomeNames.clear();
        group.biomeNames.addAll(biomeNames);
        return group;
    }
}