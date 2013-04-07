package jas.common.spawner.biome;

import jas.api.BiomeInterpreter;
import jas.common.DefaultProps;
import jas.common.JASLog;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class BiomeHandler {
    private final BiomeInterpreter interpreter;

    private final List<String> structureKeys = new ArrayList<String>();
    private final ListMultimap<String, jas.common.spawner.creature.entry.SpawnListEntry> structureKeysToSpawnList = ArrayListMultimap
            .create();

    public BiomeHandler(BiomeInterpreter interpreter) {
        this.interpreter = interpreter;

        for (String structureKey : interpreter.getStructureKeys()) {
            structureKeys.add(structureKey);
        }
    }
    
    /**
     * Gets the SpawnList For the Worlds Biome Coords Provided.
     * 
     * @return Collection of JAS SpawnListEntries that should be spawn. Return Empty list if none.
     */
    public Collection<jas.common.spawner.creature.entry.SpawnListEntry> getStructureSpawnList(World world, int xCoord,
            int yCoord, int zCoord) {
        String structureKey = interpreter.areCoordsStructure(world, xCoord, yCoord, zCoord);
        if (structureKey != null) {
            return structureKeysToSpawnList.get(structureKey);
        }
        return Collections.emptyList();
    }
    
    public boolean doesHandlerApply(World world, int xCoord, int yCoord, int zCoord) {
        return interpreter.shouldUseHandler(world, world.getBiomeGenForCoords(xCoord, zCoord));
    }
    
    /**
     * Allow user Customization of the Interpreter Input by Filtering it Through the Configuration Files
     * 
     * @param configDirectory
     * @param world
     */
    public final void readFromConfig(File configDirectory, World world) {
        Configuration masterConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + "Master/" + "StructureSpawns" + ".cfg"));
        Configuration worldConfig = new Configuration(new File(configDirectory, DefaultProps.WORLDSETTINGSDIR
                + world.getWorldInfo().getWorldName() + "/" + "StructureSpawns" + ".cfg"));
        masterConfig.load();
        worldConfig.load();
        /*
         * For Every Structure Key; Generate Two Configuration Categories: One to list Entities, the Other to Generate
         * SpawnListEntry Settings
         */
        for (String structureKey : structureKeys) {
            String entityList = "";
            Iterator<SpawnListEntry> iterator = interpreter.getStructureSpawnList(structureKey).iterator();
            while (iterator.hasNext()) {
                SpawnListEntry spawnListEntry = iterator.next();
                String mobName = (String) EntityList.classToStringMapping.get(spawnListEntry.entityClass);
                if (mobName != null) {
                    entityList = entityList.concat(mobName);
                    if (iterator.hasNext()) {
                        entityList = entityList.concat(",");
                    }
                }
            }
            /* Under StructureSpawns.SpawnList have List of Entities that are Spawnable. */
            Property resultNames = masterConfig.get("CreatureSettings.SpawnList", structureKey, entityList);
            resultNames = worldConfig.get("CreatureSettings.SpawnList", structureKey, resultNames.getString());
            ArrayList<Class<? extends EntityLiving>> classList = mobNamesToMobClasses(resultNames.getString());

            /*
             * Under StructureSpawns.StructureKey have SpawnListEntry Settings For Each Entity. Use the Global
             * LivingHandlers
             */
            for (Class<? extends EntityLiving> livingClass : classList) {
                String mobName = (String) EntityList.classToStringMapping.get(livingClass);
                if (CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).shouldSpawn
                        && !CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).creatureTypeID
                                .equals(CreatureTypeRegistry.NONE)) {
                    jas.common.spawner.creature.entry.SpawnListEntry spawnListEntry = createDefaultJASSpawnEntry(
                            livingClass, structureKey).createFromConfig(masterConfig).createFromConfig(worldConfig);

                    if (spawnListEntry.itemWeight > 0) {
                        JASLog.info("Adding SpawnListEntry %s of type %s to StructureKey %s", mobName,
                                spawnListEntry.getLivingHandler().creatureTypeID, structureKey);
                        structureKeysToSpawnList.get(structureKey).add(spawnListEntry);
                    } else {
                        JASLog.debug(
                                Level.INFO,
                                "Not adding Structure SpawnListEntry of %s to StructureKey %s due to Weight. ItemWeight: %s",
                                mobName, structureKey, spawnListEntry.itemWeight);
                    }
                } else {
                    JASLog.debug(
                            Level.INFO,
                            "Not Generating Structure %s SpawnList entries for %s. ShouldSpawn: %s, CreatureTypeID: %s",
                            mobName, CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).shouldSpawn,
                            CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).creatureTypeID);
                }
            }
        }
        masterConfig.save();
        worldConfig.save();
    }

    /**
     * Generates the Default JAS SpawnListEntry for the Given Entity by First Seach the Interpreter for The Mod Default
     * and assigning 'regular default' values if one is not found
     * 
     * @param livingClass
     * @param structureKey
     * @param biome
     * @return
     */
    private jas.common.spawner.creature.entry.SpawnListEntry createDefaultJASSpawnEntry(
            Class<? extends EntityLiving> livingClass, String structureKey) {
        Iterator<SpawnListEntry> iterator = interpreter.getStructureSpawnList(structureKey).iterator();
        while (iterator.hasNext()) {
            SpawnListEntry spawnListEntry = iterator.next();
            if (spawnListEntry.entityClass.equals(livingClass)) {
                return new jas.common.spawner.creature.entry.SpawnListEntry(livingClass, structureKey,
                        spawnListEntry.itemWeight, 4, spawnListEntry.minGroupCount, spawnListEntry.maxGroupCount);
            }
        }
        return new jas.common.spawner.creature.entry.SpawnListEntry(livingClass, structureKey, 0, 4, 0, 4);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ArrayList<Class<? extends EntityLiving>> mobNamesToMobClasses(String mobNames) {
        ArrayList<Class<? extends EntityLiving>> classList = new ArrayList();
        String[] parts = mobNames.split("\\,");
        for (String mobName : parts) {
            if(mobName.equals("")){
                continue;
            }
            Object object = EntityList.stringToClassMapping.get(mobName);
            if (object != null && EntityLiving.class.isAssignableFrom((Class<?>) object)) {
                classList.add((Class<? extends EntityLiving>) object);
            } else {
                JASLog.severe("Parsing Entity %s for StructureSpawn is not a declared entity. Ignoring", mobName);
            }
        }
        return classList;
    }
}
