package jas.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

public enum CreatureHandlerRegistry {
    INSTANCE;
    private final HashMap<String, LivingHandler> livingHandlers = new HashMap<String, LivingHandler>(); //TODO: EntityName or Class?
    private List<Class<? extends EntityLiving>> entityList = new ArrayList<Class<? extends EntityLiving>>();

    /**
     * Searhes and Process Entities it can Find in EntityList to create default the LivingHandlers. These are later customized via Configuration files.
     * @param configDirectory
     */
    public void findProcessEntitesForHandlers(File configDirectory) {
        findValidEntities();
        generateHandlersFromConfig(configDirectory);
    }
    
    /**
     * Search FML/Minecraft for Valid Creature Entities
     */
    @SuppressWarnings("unchecked")
    // TODO: This Should Probably Clear the EntityList, such that It could be called Multiple Times To 'Catch' Entities
    // that may be declared in weird places i.e. Mo'Creatures on ServerStart
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
    // TODO: Not Implemented
    public void generateHandlersFromConfig(File configDirectory) {
        for (Class<? extends EntityLiving> livingClass : entityList) {
            if(livingHandlers.containsKey(livingClass)){
                continue;
            }
            EntityLiving entityLiving = LivingHelper.createCreature(livingClass);
            if(entityLiving != null){
                
            }
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
     * Registers a Living Handler. Will Replace a previously generated handler
     * 
     * @param handlerID
     * @param handler
     * @return Returns True if Handler was Registered without needing to replace an already registered handler
     */
    private boolean forceRegisterHandler(String handlerID, LivingHandler handler) {
        livingHandlers.put(handlerID, handler);

        boolean keyPresent = false;
        if (livingHandlers.containsKey(handlerID)) {
            keyPresent = true;
        }
        return keyPresent;
    }
    
    /**
     * Registers a Living Handler. Will Not Replace
     * 
     * @param handlerID
     * @param handler
     * @return Returns True if Handler is registered after Natural Handler Generation
     */
    public boolean registerHandler(String handlerID, LivingHandler handler) {
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
     * @param handlerID
     * @return
     */
    public LivingHandler getLivingHandler(String handlerID) {
        return livingHandlers.get(handlerID);
    }
}
