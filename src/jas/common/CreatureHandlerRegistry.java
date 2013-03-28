package jas.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public enum CreatureHandlerRegistry {
    INSTANCE;
    private final HashMap<Class<? extends EntityLiving>, LivingHandler> livingHandlers = new HashMap<Class<? extends EntityLiving>, LivingHandler>();
    private List<Class<? extends EntityLiving>> entityList = new ArrayList<Class<? extends EntityLiving>>();
    private final HashMap<String, Configuration> modConfigCache = new HashMap<String, Configuration>();//TODO: This should probably be local?

    /**
     * Searhes and Process Entities it can Find in EntityList to create default the LivingHandlers. These are later
     * customized via Configuration files.
     * 
     * @param configDirectory
     */
    // TODO: When Doing World Specific Folders if a Master is wanted this will need to run 'twice' once to Load
    // masterLivingHandlers, then again to Change them to World-Specific
    public void findProcessEntitesForHandlers(File configDirectory, MinecraftServer minecraftServer) {
        modConfigCache.clear();
        findValidEntities();
        for (Class<? extends EntityLiving> livingClass : entityList) {
            if (livingHandlers.containsKey(livingClass)) {
                continue;
            }
            String mobName = (String) EntityList.classToStringMapping.get(livingClass);
            Configuration masterConfig = getConfigurationFile(configDirectory, "Master", mobName);
            Configuration worldConfig = getConfigurationFile(configDirectory, minecraftServer.getWorldName(), mobName);
            
            LivingHandler masterHandler = generateHandlerFromConfig(masterConfig, livingClass, mobName, minecraftServer.worldServers[0], null);
            livingHandlers.put(livingClass, generateHandlerFromConfig(worldConfig, livingClass, mobName, minecraftServer.worldServers[0], masterHandler));
            
            generateSpawnListEntry(worldConfig, livingClass, mobName, minecraftServer.worldServers[0]);
        }

        for (Configuration config : modConfigCache.values()) {
            config.save();
        }
    }

    /**
     * Caches and Retrieves Configration Files for Individual modIDs. The ModID is inferred from the entity name in the
     * form ModID:EntityName
     * 
     * @param configDirectory
     * @param minecraftServer
     * @param fullMobName
     * @return
     */
    private Configuration getConfigurationFile(File configDirectory, String worldName, String fullMobName) {
        String modID;
        String[] mobNameParts = fullMobName.split("\\.");
        if (mobNameParts.length == 2) {
            modID = mobNameParts[1];
        } else {
            modID = "Vanilla";
        }

        Configuration config;
        if (modConfigCache.get(modID) == null) {
            config = new Configuration(new File(configDirectory, DefaultProps.MODDIR + DefaultProps.ENTITYSUBDIR
                    + worldName + "/" + modID + ".cfg"));
            config.load();
            setupCategories(config);
            modConfigCache.put(modID, config);
            JASLog.info("Creating Config File for %s at %s", fullMobName, DefaultProps.MODDIR
                    + DefaultProps.ENTITYSUBDIR + modID + ".cfg");
        } else {
            JASLog.info("Grabbing Cache of Config File for %s", fullMobName);
        }
        return modConfigCache.get(modID);
    }

    private void setupCategories(Configuration config) {
        ConfigCategory category = config.getCategory("CreatureSettings.LivingHandler".toLowerCase(Locale.ENGLISH));
        category.setComment("Editable Format: CreatureType.UseModLocationCheck.ShouldSpawn");
    }
    
    /**
     * Search EntityList for Valid Creature Entities
     */
    @SuppressWarnings("unchecked")
    private void findValidEntities() {
        entityList.clear();
        Iterator<?> entityIterator = EntityList.stringToClassMapping.keySet().iterator();
        while (entityIterator.hasNext()) {
            Object classKey = entityIterator.next();
            if (EntityLiving.class.isAssignableFrom((Class<?>) EntityList.stringToClassMapping.get(classKey))) {
                JASLog.info("Found Entity %s", classKey);
                entityList.add((Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(classKey));
            }
        }
    }

    /**
     * Will Naturally Generate Handlers using Config Settings for all Found Entities
     * 
     * @param configDirectory
     */
    private LivingHandler generateHandlerFromConfig(Configuration config, Class<? extends EntityLiving> livingClass,
            String mobName, WorldServer worldServer, LivingHandler defaultSettings) {
        /* Default Values */
        String creatureTypeID = defaultSettings != null ? defaultSettings.creatureTypeID : CreatureTypeRegistry.NONE;
        boolean useModLocationCheck = defaultSettings != null ? defaultSettings.useModLocationCheck : true;
        boolean shouldSpawn = defaultSettings != null ? defaultSettings.shouldSpawn : false;

        String handlerString = creatureTypeID + "." + Boolean.toString(useModLocationCheck) + "."
                + Boolean.toString(shouldSpawn);
        Property handleProperty = config.get("CreatureSettings.LivingHandler", mobName, handlerString);
        String[] resultParts = handleProperty.getString().split("\\.");
        if (resultParts.length == 3) {
            String resultCreatureType = CreatureHandlerHelper.parseCreatureTypeID(resultParts[0], creatureTypeID,
                    "creatureTypeID");
            boolean resultLocationCheck = CreatureHandlerHelper.parseBoolean(resultParts[1], useModLocationCheck,
                    "LocationCheck");
            boolean resultShouldSpawn = CreatureHandlerHelper.parseBoolean(resultParts[2], shouldSpawn, "ShouldSpawn");
            return new LivingHandler(livingClass, resultCreatureType, resultLocationCheck, resultShouldSpawn);
        } else {
            JASLog.severe(
                    "LivingHandler Entry %s was invalid. Data is being ignored and loaded with default settings %s, %s, %s",
                    mobName, creatureTypeID, useModLocationCheck, shouldSpawn);
            return new LivingHandler(livingClass, creatureTypeID, useModLocationCheck, shouldSpawn);
        }
    }
    
    /**
     * Will use already generated livingHandlers to generate Biome Specific SpawnList Entries to Populate the each
     * CreatureType biomeSpawnLists.
     * 
     * @param configDirectory
     */
    // TODO: Not Implemented
    public void generateSpawnListEntry(Configuration config, Class<? extends EntityLiving> livingClass, String mobName, WorldServer worldServer) {
        
    }

    /**
     * Registers a Living Handler. Will Not Replace
     * 
     * @param handlerID
     * @param handler
     * @return Returns True if Handler is registered after Natural Handler Generation
     */
    // TODO: Change to Accept Class<? extends LivingHandler> instead of Instance of LivingHandler. Then we Manually
    // initialize it with the naturally generated values.
    //TODO: ONce a CLass is aCcepted. We Can hold off on Initializing UNtil ServerStarted so that we know the world Name
    public boolean registerHandler(Class<? extends EntityLiving> handlerID, LivingHandler handler) {
        if (!livingHandlers.containsKey(handlerID)) {
            JASLog.warning(
                    "LivingHandler %s added before natural generation. It will not take config generated settings into account.",
                    handler);
            return false;
        } else {
            livingHandlers.put(handlerID, handler);
            return true;
        }
    }

    /**
     * Gets the Appropriate LivingHandler from the Provided Key
     * 
     * @param handlerID
     * @return
     */
    public LivingHandler getLivingHandler(Class<? extends Entity> entityClass) {
        return livingHandlers.get(entityClass);
    }
}
