package jas.common.spawner.biome.structure;

import jas.api.BiomeInterpreter;
import jas.common.JASLog;
import jas.common.config.StructureConfiguration;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraftforge.common.Property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
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

    public ImmutableList<String> getStructureKeys() {
        return ImmutableList.copyOf(structureKeys);
    }

    public ImmutableList<jas.common.spawner.creature.entry.SpawnListEntry> getStructureSpawnList(String structureKey) {
        return ImmutableList.copyOf(structureKeysToSpawnList.get(structureKey));
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

    public String getStructure(World world, int xCoord, int yCoord, int zCoord) {
        return interpreter.areCoordsStructure(world, xCoord, yCoord, zCoord);
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
    public final void readFromConfig(StructureConfiguration worldConfig, World world) {
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
            Property resultNames = worldConfig.getStructureSpawns(structureKey, entityList);
            ArrayList<Class<? extends EntityLiving>> classList = mobNamesToMobClasses(resultNames.getString());

            /*
             * Under StructureSpawns.StructureKey have SpawnListEntry Settings For Each Entity. Use the Global
             * LivingHandlers
             */
            for (Class<? extends EntityLiving> livingClass : classList) {
                String mobName = (String) EntityList.classToStringMapping.get(livingClass);
                if (!CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).creatureTypeID
                        .equals(CreatureTypeRegistry.NONE)) {
                    jas.common.spawner.creature.entry.SpawnListEntry spawnListEntry = createDefaultJASSpawnEntry(
                            livingClass, structureKey).createFromConfig(worldConfig);

                    if (spawnListEntry.itemWeight > 0
                            && CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).shouldSpawn) {
                        JASLog.info("Adding SpawnListEntry %s of type %s to StructureKey %s", mobName,
                                spawnListEntry.getLivingHandler().creatureTypeID, structureKey);
                        structureKeysToSpawnList.get(structureKey).add(spawnListEntry);
                    } else {
                        JASLog.debug(
                                Level.INFO,
                                "Not adding Structure SpawnListEntry of %s to StructureKey %s due to Weight %s or ShouldSpawn %s.",
                                mobName, structureKey, spawnListEntry.itemWeight,
                                CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).shouldSpawn);
                    }
                } else {
                    JASLog.debug(Level.INFO,
                            "Not Generating Structure %s SpawnList entries for %s. CreatureTypeID: %s", structureKey,
                            mobName, CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass).creatureTypeID);
                }
            }
        }
    }

    public void saveToConfig(StructureConfiguration config) {
        for (Entry<String, Collection<jas.common.spawner.creature.entry.SpawnListEntry>> entry : structureKeysToSpawnList
                .asMap().entrySet()) {

            String entityListString = "";
            Iterator<jas.common.spawner.creature.entry.SpawnListEntry> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                jas.common.spawner.creature.entry.SpawnListEntry spawnListEntry = iterator.next();
                spawnListEntry.saveToConfig(config);

                entityListString = entityListString.concat((String) EntityList.classToStringMapping.get(spawnListEntry
                        .getClass()));
                if (iterator.hasNext()) {
                    entityListString = entityListString.concat(",");
                }
            }
            config.getStructureSpawns(entry.getKey(), entityListString).set(entityListString);
        }
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
                        spawnListEntry.itemWeight, 4, spawnListEntry.minGroupCount, spawnListEntry.maxGroupCount, "");
            }
        }
        return new jas.common.spawner.creature.entry.SpawnListEntry(livingClass, structureKey, 0, 4, 0, 4, "");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ArrayList<Class<? extends EntityLiving>> mobNamesToMobClasses(String mobNames) {
        ArrayList<Class<? extends EntityLiving>> classList = new ArrayList();
        String[] parts = mobNames.split("\\,");
        for (String mobName : parts) {
            if (mobName.equals("")) {
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
