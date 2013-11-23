package jas.common.spawner.creature.handler;

import jas.common.FileUtilities;
import jas.common.GsonHelper;
import jas.common.WorldProperties;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;
import jas.common.spawner.creature.type.LivingTypeSerializer;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LivingHandlerRegistry {
    /* Mapping from GroupID to LivingHandler */
    private ImmutableMap<String, LivingHandler> livingHandlers;

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
    public Collection<LivingHandler> getLivingHandlers() {
        return livingHandlers.values();
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
     */
    public void loadFromConfig(File configDirectory, World world) {
        Set<LivingHandler> livingHandlers = new HashSet<LivingHandler>();
        Type livingType = new TypeToken<HashMap<String, LivingHandlerBuilder>>() {
        }.getType();
        Gson gson = GsonHelper
                .createGson(false, new Type[] { livingType }, new Object[] { new LivingTypeSerializer() });
        File handlerFileFolder = LivingHandler.getFile(configDirectory,
                worldProperties.getFolderConfiguration().saveName, "");
        File[] files = FileUtilities.getFileInDirectory(handlerFileFolder, ".cfg");
        for (File livingFile : files) {
            Optional<HashMap<String, LivingHandlerBuilder>> read = GsonHelper.readFromGson(
                    FileUtilities.createReader(livingFile, false), livingType, gson);
            if (read.isPresent()) {
                for (Entry<String, LivingHandlerBuilder> entry : read.get().entrySet()) {
                    if (entry.getValue().getHandlerId() != null) {
                        LivingHandler handler = entry.getValue().build(creatureTypeRegistry);
                        livingHandlers.add(handler);
                    }
                }
            }
        }

        Collection<LivingGroup> livingGroups = livingGroupRegistry.getEntityGroups();
        for (LivingGroup livingGroup : livingGroups) {
            LivingHandlerBuilder builder = new LivingHandlerBuilder(livingGroup.groupID, guessCreatureTypeOfGroup(
                    livingGroup, world));
            livingHandlers.add(builder.build(creatureTypeRegistry));
        }
        Builder<String, LivingHandler> builder = ImmutableMap.<String, LivingHandler> builder();
        for (LivingHandler handler : livingHandlers) {
            builder.put(handler.groupID, handler);
        }
        this.livingHandlers = builder.build();
    }

    private String getSaveFileName(String groupID) {
        boolean universalCFG = worldProperties.getSavedFileConfiguration().universalDirectory;
        if (universalCFG) {
            return "Universal";
        } else {
            String modID;
            String[] mobNameParts = groupID.split("\\.");
            if (mobNameParts.length >= 2) {
                String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
                modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
            } else {
                modID = "Vanilla";
            }
            return modID;
        }
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
        worldProperties.saveToConfig(configDirectory);
        HashMap<String, HashMap<String, LivingHandlerBuilder>> fileNameToHandlerIdToHandler = new HashMap<String, HashMap<String, LivingHandlerBuilder>>();
        for (LivingHandler handler : livingHandlers.values()) {
            String saveName = getSaveFileName(handler.groupID);
            HashMap<String, LivingHandlerBuilder> idToHandler = fileNameToHandlerIdToHandler.get(saveName);
            if (idToHandler == null) {
                idToHandler = new HashMap<String, LivingHandlerBuilder>();
                fileNameToHandlerIdToHandler.put(saveName, idToHandler);
            }
            idToHandler.put(handler.groupID, new LivingHandlerBuilder(handler));
        }
        Type livingType = new TypeToken<HashMap<String, LivingHandlerBuilder>>() {
        }.getType();
        Gson gson = GsonHelper.createGson(true, new Type[] { livingType }, new Object[] { new LivingTypeSerializer() });
        for (Entry<String, HashMap<String, LivingHandlerBuilder>> entry : fileNameToHandlerIdToHandler.entrySet()) {
            File livingfile = LivingHandler.getFile(configDirectory, worldProperties.getFolderConfiguration().saveName,
                    entry.getKey());
            GsonHelper.writeToGson(FileUtilities.createWriter(livingfile, true), entry.getValue(), livingType, gson);
        }
    }
}