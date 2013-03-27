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
    private final HashMap<String, LivingHandler> livingHandlers = new HashMap<String, LivingHandler>();
    private List<Class<? extends EntityLiving>> entityList = new ArrayList<Class<? extends EntityLiving>>();

    /**
     * Search FML/Minecraft for Valid Creature Entities
     */
    @SuppressWarnings("unchecked")
    public void findValidEntities() {
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
    public void generateHandlers(File configDirectory) {

    }

    /**
     * Registers a Living Handler. Will Replace a previously generated handler
     * 
     * @param key
     * @param handler
     * @return Returns True if Handler was Registered without needing to replace an already registered handler
     */
    public boolean forceRegisterHandler(String key, LivingHandler handler) {
        boolean keyPresent = false;
        if (livingHandlers.containsKey(key)) {
            JASLog.warning("Key %s already taken. Creature Handler %s will replace %s", key,
                    livingHandlers.containsKey(key), handler);
            keyPresent = true;
        }
        livingHandlers.put(key, handler);
        return keyPresent;
    }

    /**
     * Registers a Living Handler. Will Not Replace
     * 
     * @param key
     * @param handler
     * @return Returns True is Handler is registered
     */
    public boolean registerHandler(String key, LivingHandler handler) {
        if (livingHandlers.containsKey(key)) {
            JASLog.warning("Key %s already taken. Creature Handler %s will NOT replace %s", key,
                    livingHandlers.containsKey(key), handler);
            return false;
        } else {
            livingHandlers.put(key, handler);
            return true;
        }
    }
}
