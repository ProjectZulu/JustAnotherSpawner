package jas.common.spawner.creature.handler;

import jas.common.ImportedSpawnList;
import jas.common.WorldProperties;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraftforge.common.Property;

import com.google.common.collect.ImmutableList;

public class LivingHandlerRegistry {
    /* Mapping from GroupID to LivingHandler */
    private final HashMap<String, LivingHandler> livingHandlers = new HashMap<String, LivingHandler>();

    public LivingHandler getLivingHandler(String groupID) {
        return livingHandlers.get(groupID);
    }
    
    public List<LivingHandler> getLivingHandlers(Class<? extends EntityLiving> entityClass) {
        List<LivingHandler> list = new ArrayList<LivingHandler>();
        for (String groupID : livingGroupRegistry.getGroupsWithEntity(livingGroupRegistry.EntityClasstoJASName
                .get(entityClass))) {
            LivingHandler handler = livingHandlers.get(groupID);
            if (handler != null) {
                list.add(handler);
            }
        }
        return list;
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
    private LivingGroupRegistry livingGroupRegistry;
    private WorldProperties worldProperties;

    public LivingHandlerRegistry(LivingGroupRegistry livingGroupRegistry, CreatureTypeRegistry creatureTypeRegistry,
            WorldProperties worldProperties) {
        this.livingGroupRegistry = livingGroupRegistry;
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
    public void loadFromConfig(File configDirectory, World world, ImportedSpawnList spawnList) {
        livingHandlers.clear();

        Collection<LivingGroup> livingGroups = livingGroupRegistry.getEntityGroups();

        for (LivingGroup livingGroup : livingGroups) {
            LivingHandler livingHandler = new LivingHandler(creatureTypeRegistry, livingGroup.groupID,
                    guessCreatureTypeOfGroup(livingGroup, world), true, "");
            livingHandlers.put(livingGroup.groupID, livingHandler);
        }

        MobSpecificConfigCache cache = new MobSpecificConfigCache(worldProperties);
        for (String groupID : livingHandlers.keySet()) {
            LivingConfiguration worldConfig = cache.getLivingEntityConfig(configDirectory, groupID);

            LivingHandler resultLivingHandler = livingHandlers.get(groupID).createFromConfig(worldConfig);
            livingHandlers.put(groupID, resultLivingHandler);
        }

        cache.saveAndCloseConfigs();
    }

    /**
     * Determines the Default JAS Living Type from the Vanilla EnumCreatureType
     * 
     * @param livingClass
     * @return
     */
    private String guessCreatureTypeOfGroup(LivingGroup livingGroup, World world) {
        for (String jasName : livingGroup.entityJASNames()) {
            Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass.get(jasName);
            EntityLiving creature = LivingHelper.createCreature(livingClass, world);
            for (EnumCreatureType type : EnumCreatureType.values()) {
                boolean isType = creature != null ? creature.isCreatureType(type, true) : type.getClass()
                        .isAssignableFrom(livingClass);
                if (isType && creatureTypeRegistry.getCreatureType(type.toString()) != null) {
                    return type.toString();
                }
            }
        }
        return CreatureTypeRegistry.NONE;
    }

    public void saveToConfig(File configDirectory) {
        LivingConfiguration tempSettings = new LivingConfiguration(configDirectory, "temporarySaveSettings",
                worldProperties);
        tempSettings.load();
        Property byBiome = tempSettings.getSavedSortByBiome(worldProperties.savedSortCreatureByBiome);
        byBiome.set(worldProperties.savedSortCreatureByBiome);
        Property isUniversal = tempSettings.getSavedUseUniversalConfig(worldProperties.savedUniversalDirectory);
        isUniversal.set(worldProperties.savedUniversalDirectory);
        tempSettings.save();
        MobSpecificConfigCache cache = new MobSpecificConfigCache(worldProperties);

        for (Entry<String, LivingHandler> handler : livingHandlers.entrySet()) {
            LivingConfiguration config = cache.getLivingEntityConfig(configDirectory, handler.getKey());
            handler.getValue().saveToConfig(config);
            if (handler.getValue().creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
                continue;
            }
        }
        cache.saveAndCloseConfigs();
    }
}