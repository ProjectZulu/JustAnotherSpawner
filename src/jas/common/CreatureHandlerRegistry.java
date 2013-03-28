package jas.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.common.Configuration;

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
    // masterLivingHandlers, then again to Change them to Default
    public void findProcessEntitesForHandlers(File configDirectory) {
        modConfigCache.clear();
        findValidEntities();
        generateHandlersFromConfig(configDirectory);
        
        /* Save All Configs that were accessed Configs */
        for (Configuration config : modConfigCache.values()) {
            config.save();
        }
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
    public void generateHandlersFromConfig(File configDirectory) {
        for (Class<? extends EntityLiving> livingClass : entityList) {
            if (livingHandlers.containsKey(livingClass)) {
                continue;
            }
            String modID;
            String fullMobName = (String) EntityList.classToStringMapping.get(livingClass);
            String[] mobNameParts = fullMobName.split("\\.");
            if (mobNameParts.length == 2) {
                modID = mobNameParts[1];
            } else {
                modID = "Vanilla";
            }

            // EntityLiving entityLiving = LivingHelper.createCreature(livingClass);
            Configuration config;
            if (modConfigCache.get(modID) == null) {
                config = new Configuration(new File(configDirectory, DefaultProps.MODDIR + DefaultProps.ENTITYSUBDIR
                        + modID + ".cfg"));
                config.load();
                CreatureHandlerConfiguration.setupCategories(config);
                modConfigCache.put(modID, config);
            } else {
                config = modConfigCache.get(modID);
            }
            livingHandlers.put(livingClass,
                    CreatureHandlerConfiguration.getLivingHandlerSettings(config, livingClass, fullMobName));
        }
    }

    /**
     * Will use already generated livingHandlers to generate Biome Specific SpawnList Entries to Populate the each
     * CreatureType biomeSpawnLists.
     * 
     * @param configDirectory
     */
    // TODO: Not Implemented
    public void generateSpawnLists(File configDirectory) {

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
