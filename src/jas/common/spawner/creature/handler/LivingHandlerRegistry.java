package jas.common.spawner.creature.handler;

import jas.common.ImportedSpawnList;
import jas.common.JASLog;
import jas.common.WorldProperties;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraftforge.common.Property;

import com.google.common.collect.ImmutableList;

public class LivingHandlerRegistry {
    private final HashMap<Class<? extends EntityLiving>, LivingHandler> livingHandlers = new HashMap<Class<? extends EntityLiving>, LivingHandler>();

    public LivingHandler getLivingHandler(Class<? extends Entity> entityClass) {
        return livingHandlers.get(entityClass);
    }

    /**
     * Creates a Immutable copy of registered livinghandlers
     * 
     * @return Immutable copy of Collection of SpawnListEntries
     */
    public ImmutableList<LivingHandler> getLivingHandlers() {
        return ImmutableList.copyOf(livingHandlers.values());
    }

    private CreatureTypeRegistry creatureTypeRegistry;
    private WorldProperties worldProperties;

    public LivingHandlerRegistry(CreatureTypeRegistry creatureTypeRegistry, WorldProperties worldProperties) {
        this.creatureTypeRegistry = creatureTypeRegistry;
        this.worldProperties = worldProperties;
    }

    /**
     * Loads settings from Configuration. Currently loaded settings will be lost.
     * 
     * @param configDirectory
     * @param world
     * @param spawnList
     */
    @SuppressWarnings("unchecked")
    public void loadFromConfig(File configDirectory, World world, ImportedSpawnList spawnList) {
        livingHandlers.clear();
        List<Class<? extends EntityLiving>> entityList = new ArrayList<Class<? extends EntityLiving>>();
        Iterator<?> entityIterator = EntityList.stringToClassMapping.keySet().iterator();
        while (entityIterator.hasNext()) {
            Object classKey = entityIterator.next();
            if (EntityLiving.class.isAssignableFrom((Class<?>) EntityList.stringToClassMapping.get(classKey))
                    && !Modifier.isAbstract(((Class<?>) EntityList.stringToClassMapping.get(classKey)).getModifiers())) {
                JASLog.info("Found Entity %s", classKey);
                entityList.add((Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(classKey));
            }
        }

        for (Class<? extends EntityLiving> livingClass : entityList) {
            LivingHandler livingHandler = new LivingHandler(creatureTypeRegistry, livingClass,
                    enumCreatureTypeToLivingType(livingClass, world), true, "");
            livingHandlers.put(livingClass, livingHandler);
        }

        MobSpecificConfigCache cache = new MobSpecificConfigCache(worldProperties);
        for (Class<? extends EntityLiving> livingClass : livingHandlers.keySet()) {
            LivingConfiguration worldConfig = cache.getLivingEntityConfig(configDirectory, livingClass);

            LivingHandler resultLivingHandler = livingHandlers.get(livingClass).createFromConfig(worldConfig);
            livingHandlers.put(livingClass, resultLivingHandler);
        }

        cache.saveAndCloseConfigs();
    }

    /**
     * Determines the Default JAS Living Type from the Vanilla EnumCreatureType
     * 
     * @param livingClass
     * @return
     */
    private String enumCreatureTypeToLivingType(Class<? extends EntityLiving> livingClass, World world) {
        EntityLiving creature = LivingHelper.createCreature(livingClass, world);
        for (EnumCreatureType type : EnumCreatureType.values()) {
            boolean isType = creature != null ? creature.isCreatureType(type, true) : type.getClass().isAssignableFrom(
                    livingClass);
            if (isType && creatureTypeRegistry.getCreatureType(type.toString()) != null) {
                return type.toString();
            }
        }
        return CreatureTypeRegistry.NONE;
    }

    public void saveToConfig(File configDirectory) {
        LivingConfiguration tempSettings = new LivingConfiguration(configDirectory, "temporarySaveSettings",
                worldProperties);
        tempSettings.load();
        Property byBiome = tempSettings
                .getSavedSortByBiome(worldProperties.savedSortCreatureByBiome);
        byBiome.set(worldProperties.savedSortCreatureByBiome);
        Property isUniversal = tempSettings.getSavedUseUniversalConfig(worldProperties.savedUniversalDirectory);
        isUniversal.set(worldProperties.savedUniversalDirectory);
        tempSettings.save();
        MobSpecificConfigCache cache = new MobSpecificConfigCache(worldProperties);

        for (Entry<Class<? extends EntityLiving>, LivingHandler> handler : livingHandlers.entrySet()) {
            LivingConfiguration config = cache.getLivingEntityConfig(configDirectory, handler.getKey());
            handler.getValue().saveToConfig(config);
            if (handler.getValue().creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
                continue;
            }
        }
        cache.saveAndCloseConfigs();
    }
}