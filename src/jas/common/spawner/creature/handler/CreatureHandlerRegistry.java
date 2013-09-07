package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.ImportedSpawnList;
import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.WorldProperties;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.creature.entry.SpawnListEntry;
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
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreatureHandlerRegistry {
    private final HashMap<Class<? extends EntityLiving>, LivingHandler> livingHandlers = new HashMap<Class<? extends EntityLiving>, LivingHandler>();
    private final HashMap<Class<? extends EntityLiving>, Class<? extends LivingHandler>> handlersToAdd = new HashMap<Class<? extends EntityLiving>, Class<? extends LivingHandler>>();

    /* Map of Entity modID to their Respective Configuration File, cleared immediately After Saving */
    private final HashMap<String, LivingConfiguration> modConfigCache = new HashMap<String, LivingConfiguration>();

    /* List of Currently Found Entities, Cleared on World Start before re-populating */
    private List<Class<? extends EntityLiving>> entityList = new ArrayList<Class<? extends EntityLiving>>();

    /* Boolean Used by Client to know if setup has been run */
    @SideOnly(Side.CLIENT)
    public static boolean isSetup;

    @SideOnly(Side.CLIENT)
    public void clientStartup(World world) {
        if (!isSetup) {
            initializeLivingHandlers(world);
            isSetup = true;
        }
    }

    public final CreatureTypeRegistry creatureTypeRegistry;
    public final BiomeGroupRegistry biomeGroupRegistry;
    public final WorldProperties worldProperties;

    public CreatureHandlerRegistry(BiomeGroupRegistry biomeGroupRegistry, CreatureTypeRegistry creatureTypeRegistry,
            WorldProperties worldProperties) {
        this.creatureTypeRegistry = creatureTypeRegistry;
        this.biomeGroupRegistry = biomeGroupRegistry;
        this.worldProperties = worldProperties;
    }

    public void serverStartup(File configDirectory, World world, ImportedSpawnList spawnList) {
        initializeLivingHandlers(world);
        configLivingHandlers(configDirectory, world);
        saveAndCloseConfigs();
        if (worldProperties.universalDirectory != worldProperties.loadedUniversalDirectory
                || worldProperties.savedSortCreatureByBiome != worldProperties.loadedSortCreatureByBiome) {
            worldProperties.universalDirectory = worldProperties.loadedUniversalDirectory;
            worldProperties.savedSortCreatureByBiome = worldProperties.loadedSortCreatureByBiome;
            File entityFolder = new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldProperties.saveName
                    + "/" + DefaultProps.ENTITYSUBDIR);
            for (File file : entityFolder.listFiles()) {
                file.delete();
            }
            saveCurrentToConfig(configDirectory);
        }
    }

    public void saveCurrentToConfig(File configDirectory) {
        LivingConfiguration tempSettings = new LivingConfiguration(configDirectory, "temporarySaveSettings",
                worldProperties);
        tempSettings.load();
        Property byBiome = tempSettings
                .getSavedSortByBiome(JustAnotherSpawner.worldSettings().worldProperties().savedSortCreatureByBiome);
        byBiome.set(JustAnotherSpawner.worldSettings().worldProperties().savedSortCreatureByBiome);
        Property isUniversal = tempSettings.getSavedUseUniversalConfig(JustAnotherSpawner.worldSettings()
                .worldProperties().universalDirectory);
        isUniversal.set(JustAnotherSpawner.worldSettings().worldProperties().universalDirectory);
        tempSettings.save();

        for (Entry<Class<? extends EntityLiving>, LivingHandler> handler : livingHandlers.entrySet()) {
            LivingConfiguration config = getLoadedConfigurationFile(configDirectory, handler.getKey());
            handler.getValue().saveToConfig(config);
            if (handler.getValue().creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
                continue;
            }
        }
        saveAndCloseConfigs();
    }

    private void saveAndCloseConfigs() {
        for (Configuration config : modConfigCache.values()) {
            config.save();
        }
        modConfigCache.clear();
    }

    /**
     * Default Setup of LivingHandlers Inferring from Vanilla Entities
     */
    public void initializeLivingHandlers(World world) {
        populateEntityList();
        for (Class<? extends EntityLiving> livingClass : entityList) {
            LivingHandler livingHandler = new LivingHandler(creatureTypeRegistry, livingClass,
                    enumCreatureTypeToLivingType(livingClass, world), true, "");
            livingHandlers.put(livingClass, livingHandler);
        }
    }

    /**
     * Does customization of the LivingHandlers by exposing/reading their values from Configuration Files
     */
    public void configLivingHandlers(File configDirectory, World world) {
        for (Class<? extends EntityLiving> livingClass : livingHandlers.keySet()) {
            LivingConfiguration worldConfig = getLoadedConfigurationFile(configDirectory, livingClass);

            LivingHandler resultLivingHandler = livingHandlers.get(livingClass).createFromConfig(worldConfig);
            livingHandlers.put(livingClass, resultLivingHandler);
        }
    }

    private LivingConfiguration getLoadedConfigurationFile(File configDirectory,
            Class<? extends EntityLiving> entityClass) {
        return getLoadedConfigurationFile(configDirectory, entityClass, worldProperties.universalDirectory);
    }

    /**
     * Caches and Retrieves configuration files for individual modIDs. The ModID is inferred from the entity name in the
     * form ModID:EntityName
     * 
     * @param configDirectory
     * @param minecraftServer
     * @param fullMobName
     * @return
     */
    private LivingConfiguration getLoadedConfigurationFile(File configDirectory,
            Class<? extends EntityLiving> entityClass, boolean universalDirectory) {
        if (universalDirectory) {
            if (modConfigCache.get(worldProperties.saveName + "Universal") == null) {
                LivingConfiguration config = new LivingConfiguration(configDirectory, "Universal", worldProperties);
                config.load();
                LivingHandler.setupConfigCategory(config);
                SpawnListEntry.setupConfigCategory(config);
                modConfigCache.put(worldProperties.saveName + "Universal", config);
                return config;
            }
            return modConfigCache.get(worldProperties.saveName + "Universal");
        } else {
            String fullMobName = (String) EntityList.classToStringMapping.get(entityClass);
            String modID;
            String[] mobNameParts = fullMobName.split("\\.");
            if (mobNameParts.length >= 2) {
                String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
                modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
            } else {
                modID = "Vanilla";
            }

            if (modConfigCache.get(worldProperties.saveName + modID) == null) {
                LivingConfiguration config = new LivingConfiguration(configDirectory, modID, worldProperties);
                config.load();
                LivingHandler.setupConfigCategory(config);
                SpawnListEntry.setupConfigCategory(config);
                modConfigCache.put(worldProperties.saveName + modID, config);
            }
            return modConfigCache.get(worldProperties.saveName + modID);
        }
    }

    /**
     * Search EntityList for Valid Creature Entities
     */
    @SuppressWarnings("unchecked")
    private void populateEntityList() {
        entityList.clear();
        Iterator<?> entityIterator = EntityList.stringToClassMapping.keySet().iterator();
        while (entityIterator.hasNext()) {
            Object classKey = entityIterator.next();
            if (EntityLiving.class.isAssignableFrom((Class<?>) EntityList.stringToClassMapping.get(classKey))
                    && !Modifier.isAbstract(((Class<?>) EntityList.stringToClassMapping.get(classKey)).getModifiers())) {
                JASLog.info("Found Entity %s", classKey);
                entityList.add((Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(classKey));
            }
        }
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

    /**
     * Determines the Vanilla EnumCreatureType from the equivalent JAS living Type
     * 
     * @return
     */
    private EnumCreatureType livingTypeToEnumCreatureType(String creatureTypeID) {
        if (creatureTypeID.equals(CreatureTypeRegistry.MONSTER)) {
            return EnumCreatureType.monster;
        } else if (creatureTypeID.equals(CreatureTypeRegistry.AMBIENT)) {
            return EnumCreatureType.ambient;
        } else if (creatureTypeID.equals(CreatureTypeRegistry.CREATURE)) {
            return EnumCreatureType.creature;
        } else if (creatureTypeID.equals(CreatureTypeRegistry.WATERCREATURE)) {
            return EnumCreatureType.waterCreature;
        } else {
            return null;
        }
    }

    /**
     * Registers a Living Handler to be initialized by the System.
     * 
     * @param handlerID
     * @param handler
     * @return Returns False if Handler is replaced during registration
     */
    public boolean registerHandler(Class<? extends EntityLiving> livingEntity,
            Class<? extends LivingHandler> livingHandler) {
        boolean isReplaced = false;
        if (!handlersToAdd.containsKey(livingEntity)) {
            JASLog.warning("Custom Living Handler %s which was to be registered will be replaced with %s",
                    handlersToAdd.containsKey(livingEntity), livingHandler);
            isReplaced = true;
        }
        handlersToAdd.put(livingEntity, livingHandler);
        return !isReplaced;
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

    public ImmutableList<LivingHandler> getLivingHandlers() {
        return ImmutableList.copyOf(livingHandlers.values());
    }

    /**
     * Creates a new LivingHandler at the provided key with
     */
    public void updateLivingHandler(Class<? extends EntityLiving> entityClass, String creatureTypeID,
            boolean shouldSpawn) {
        livingHandlers.put(entityClass,
                livingHandlers.get(entityClass).toCreatureTypeID(creatureTypeID).toShouldSpawn(shouldSpawn));
    }

    public Iterator<Class<? extends EntityLiving>> getLivingKeys() {
        return livingHandlers.keySet().iterator();
    }
}
