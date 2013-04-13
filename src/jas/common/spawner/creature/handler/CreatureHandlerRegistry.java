package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;

import com.google.common.base.CharMatcher;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum CreatureHandlerRegistry {
    INSTANCE;
    private final HashMap<Class<? extends EntityLiving>, LivingHandler> livingHandlers = new HashMap<Class<? extends EntityLiving>, LivingHandler>();
    private final HashMap<Class<? extends EntityLiving>, Class<? extends LivingHandler>> handlersToAdd = new HashMap<Class<? extends EntityLiving>, Class<? extends LivingHandler>>();
    
    private final HashMap<String, Configuration> modConfigCache = new HashMap<String, Configuration>();
    private List<Class<? extends EntityLiving>> entityList = new ArrayList<Class<? extends EntityLiving>>();
    public static final String delimeter = DefaultProps.DELIMETER;
    public static final String LivingHandlerCategoryComment = "Editable Format: CreatureType" + delimeter
            + "ShouldSpawn" + "{TAG1:PARAM1,value:PARAM2,value}{TAG2:PARAM1,value:PARAM2,value}";
    public static final String SpawnListCategoryComment = "Editable Format: SpawnWeight" + delimeter + "SpawnPackSize"
            + delimeter + "MinChunkPackSize" + delimeter + "MaxChunkPackSize";

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

    public void serverStartup(File configDirectory, World world) {
        initializeLivingHandlers(world);
        configLivingHandlers(configDirectory, world);
        generateSpawnListEntries(configDirectory, world);
        saveAndCloseConfigs();
    }

    public void clearSpawnLists() {
        Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
        while (iterator.hasNext()) {
            CreatureType type = iterator.next();
            type.resetSpawns();
        }
    }

    public void saveAndCloseConfigs() {
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
            LivingHandler livingHandler = new LivingHandler(livingClass, enumCreatureTypeToLivingType(livingClass,
                    world), false, "");
            livingHandlers.put(livingClass, livingHandler);
        }
    }

    /**
     * Does customization of the LivingHandlers by exposing/reading their values from Configuration Files
     */
    public void configLivingHandlers(File configDirectory, World world) {
        for (Class<? extends EntityLiving> livingClass : livingHandlers.keySet()) {
            String mobName = (String) EntityList.classToStringMapping.get(livingClass);

            Configuration masterConfig = getConfigurationFile(configDirectory, "Master", mobName);
            Configuration worldConfig = getConfigurationFile(configDirectory, world.getWorldInfo().getWorldName(),
                    mobName);

            LivingHandler resultLivingHandler = livingHandlers.get(livingClass).createFromConfig(masterConfig)
                    .createFromConfig(worldConfig);
            livingHandlers.put(livingClass, resultLivingHandler);
        }
    }

    /**
     * Generates SpawnListEntries for LivingHandlers which have been enabled to Spawn
     */
    public void generateSpawnListEntries(File configDirectory, World world) {
        clearSpawnLists();
        for (Class<? extends EntityLiving> livingClass : livingHandlers.keySet()) {
            String mobName = (String) EntityList.classToStringMapping.get(livingClass);

            Configuration masterConfig = getConfigurationFile(configDirectory, "Master", mobName);
            Configuration worldConfig = getConfigurationFile(configDirectory, world.getWorldInfo().getWorldName(),
                    mobName);

            if (!livingHandlers.get(livingClass).creatureTypeID.equals(CreatureTypeRegistry.NONE)) {
                for (BiomeGroup group : BiomeGroupRegistry.INSTANCE.getBiomeGroups()) {

                    SpawnListEntry spawnListEntry = findVanillaSpawnListEntry(group, livingClass).createFromConfig(
                            masterConfig).createFromConfig(worldConfig);

                    if (spawnListEntry.itemWeight > 0 && livingHandlers.get(livingClass).shouldSpawn) {
                        JASLog.info("Adding SpawnListEntry %s of type %s to BiomeGroup %s", mobName,
                                spawnListEntry.getLivingHandler().creatureTypeID, spawnListEntry.pckgName);
                        CreatureTypeRegistry.INSTANCE.getCreatureType(spawnListEntry.getLivingHandler().creatureTypeID)
                                .addSpawn(spawnListEntry);
                    } else {
                        JASLog.debug(
                                Level.INFO,
                                "Not adding Generated SpawnListEntry of %s due to Weight %s or ShouldSpawn %s, BiomeGroup: %s, ItemWeight: %s",
                                mobName, spawnListEntry.itemWeight, livingHandlers.get(livingClass).shouldSpawn,
                                group.groupID);
                    }
                }
            } else {
                JASLog.debug(Level.INFO,
                        "Not Generating SpawnList entries for %s as it does not have CreatureType. CreatureTypeID: %s",
                        mobName, livingHandlers.get(livingClass).creatureTypeID);
            }
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
            String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
            modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
        } else {
            modID = "Vanilla";
        }

        Configuration config;
        if (modConfigCache.get(worldName + modID) == null) {
            config = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + worldName + "/"
                    + DefaultProps.ENTITYSUBDIR + modID + ".cfg"));
            config.load();
            setupCategories(config);
            modConfigCache.put(worldName + modID, config);
        }
        return modConfigCache.get(worldName + modID);
    }

    private void setupCategories(Configuration config) {
        ConfigCategory category = config.getCategory("CreatureSettings.LivingHandler".toLowerCase(Locale.ENGLISH));
        category.setComment(LivingHandlerCategoryComment);

        category = config.getCategory("CreatureSettings.SpawnListEntry".toLowerCase(Locale.ENGLISH));
        category.setComment(SpawnListCategoryComment);
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
     * Searches For a Vanilla SpawnListEntry. Generates using defaults values (spawn rate == 0) if one doesn't exist.
     * 
     * @param biome
     * @param livingClass
     * @return
     */
    public SpawnListEntry findVanillaSpawnListEntry(BiomeGroup group, Class<? extends EntityLiving> livingClass) {
        for (String pckgNames : group.getBiomeNames()) {
            for (Integer biomeID : BiomeGroupRegistry.INSTANCE.pckgNameToBiomeID.get(pckgNames)) {
                BiomeGenBase biome = BiomeGenBase.biomeList[biomeID];
                EnumCreatureType creatureType = livingTypeToEnumCreatureType(livingHandlers.get(livingClass).creatureTypeID);
                if (creatureType != null) {
                    @SuppressWarnings("unchecked")
                    List<net.minecraft.world.biome.SpawnListEntry> spawnListEntries = biome
                            .getSpawnableList(creatureType);
                    for (net.minecraft.world.biome.SpawnListEntry spawnListEntry : spawnListEntries) {
                        if (spawnListEntry.entityClass.equals(livingClass)) {
                            return new SpawnListEntry(livingClass, group.groupID, spawnListEntry.itemWeight, 4,
                                    spawnListEntry.minGroupCount, spawnListEntry.maxGroupCount);
                        }
                    }
                }

            }
        }
        return new SpawnListEntry(livingClass, group.groupID, 0, 4, 0, 4);
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
            if (isType && CreatureTypeRegistry.INSTANCE.getCreatureType(type.toString()) != null) {
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
