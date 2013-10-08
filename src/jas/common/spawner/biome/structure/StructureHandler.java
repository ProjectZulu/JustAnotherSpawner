package jas.common.spawner.biome.structure;

import jas.api.StructureInterpreter;
import jas.common.JASLog;
import jas.common.WorldProperties;
import jas.common.config.StructureConfiguration;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraftforge.common.Property;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

public class StructureHandler {
    private final StructureInterpreter interpreter;

    private final List<String> structureKeys = new ArrayList<String>();
    private final ListMultimap<String, jas.common.spawner.creature.entry.SpawnListEntry> structureKeysToSpawnList = ArrayListMultimap
            .create();

    public StructureHandler(StructureInterpreter interpreter) {
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
    public final void readFromConfig(LivingHandlerRegistry livingHandlerRegistry, StructureConfiguration worldConfig,
            WorldProperties worldProperties) {
        /*
         * For Every Structure Key; Generate Two Configuration Categories: One to list Entities, the Other to Generate
         * SpawnListEntry Settings
         */
        for (String structureKey : structureKeys) {
            StringBuilder livingHandlerIDs = new StringBuilder();
            Iterator<SpawnListEntry> iterator = interpreter.getStructureSpawnList(structureKey).iterator();
            while (iterator.hasNext()) {
                SpawnListEntry spawnListEntry = iterator.next();
                @SuppressWarnings("unchecked")
                List<LivingHandler> handlers = livingHandlerRegistry.getLivingHandlers(spawnListEntry.entityClass);
                if (!handlers.isEmpty()) {
                    livingHandlerIDs.append(handlers.get(0).groupID);
                    if (iterator.hasNext()) {
                        livingHandlerIDs.append(",");
                    }
                } else {
                    JASLog.warning(
                            "Default entity %s that should spawn in structure %s does not appear to belong to an entity group.",
                            spawnListEntry.entityClass.getSimpleName(), structureKey);
                }
            }
            /* Under StructureSpawns.SpawnList have List of Entities that are Spawnable. */
            Property resultNames = worldConfig.getStructureSpawns(structureKey, livingHandlerIDs.toString());

            for (String groupID : resultNames.getString().split(",")) {
                if (groupID.trim().equals("")) {
                    continue;
                }

                LivingHandler livingHandler = livingHandlerRegistry.getLivingHandler(groupID);
                if (livingHandler == null) {
                    JASLog.severe("Error parsing EntityGroup from Structure %s spawnlist %s. The key %s is unknown.",
                            structureKey, resultNames.getString(), groupID);
                    continue;
                }

                if (!livingHandler.creatureTypeID.equals(CreatureTypeRegistry.NONE)) {
                    jas.common.spawner.creature.entry.SpawnListEntry spawnListEntry = createDefaultJASSpawnEntry(
                            livingHandlerRegistry, livingHandler, structureKey).createFromConfig(worldConfig,
                            worldProperties);

                    if (spawnListEntry.itemWeight > 0 && livingHandler.shouldSpawn) {
                        JASLog.info("Adding SpawnListEntry %s of type %s to StructureKey %s", livingHandler.groupID,
                                livingHandler.creatureTypeID, structureKey);
                        structureKeysToSpawnList.get(structureKey).add(spawnListEntry);
                    } else {
                        JASLog.debug(
                                Level.INFO,
                                "Not adding Structure SpawnListEntry of %s to StructureKey %s due to Weight %s or ShouldSpawn %s.",
                                livingHandler.groupID, structureKey, spawnListEntry.itemWeight,
                                livingHandler.shouldSpawn);
                    }
                } else {
                    JASLog.debug(Level.INFO,
                            "Not Generating Structure %s SpawnList entries for %s. CreatureTypeID: %s", structureKey,
                            livingHandler.groupID, livingHandler.creatureTypeID);
                }
            }
        }
    }

    public void saveToConfig(StructureConfiguration config, WorldProperties worldProperties) {
        for (Entry<String, Collection<jas.common.spawner.creature.entry.SpawnListEntry>> entry : structureKeysToSpawnList
                .asMap().entrySet()) {
            String entityListString = "";
            Iterator<jas.common.spawner.creature.entry.SpawnListEntry> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                jas.common.spawner.creature.entry.SpawnListEntry spawnListEntry = iterator.next();
                spawnListEntry.saveToConfig(config, worldProperties);

                entityListString = entityListString.concat(spawnListEntry.livingGroupID);
                if (iterator.hasNext()) {
                    entityListString = entityListString.concat(",");
                }
            }
            config.getStructureSpawns(entry.getKey(), entityListString).set(entityListString);
        }

        for (jas.common.spawner.creature.entry.SpawnListEntry spawnListEntry : structureKeysToSpawnList.values()) {
            spawnListEntry.saveToConfig(config, worldProperties);
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
    @SuppressWarnings("unchecked")
    private jas.common.spawner.creature.entry.SpawnListEntry createDefaultJASSpawnEntry(
            LivingHandlerRegistry livingHandlerRegistry, LivingHandler livingHandler, String structureKey) {
        Iterator<SpawnListEntry> iterator = interpreter.getStructureSpawnList(structureKey).iterator();
        while (iterator.hasNext()) {
            SpawnListEntry spawnListEntry = iterator.next();
            for (LivingHandler spawnHandler : livingHandlerRegistry.getLivingHandlers(spawnListEntry.entityClass)) {
                if (spawnHandler.groupID.equals(livingHandler.groupID)) {
                    return new jas.common.spawner.creature.entry.SpawnListEntry(livingHandler.groupID, structureKey,
                            spawnListEntry.itemWeight, 4, spawnListEntry.minGroupCount, spawnListEntry.maxGroupCount,
                            "");
                }
            }
        }
        return new jas.common.spawner.creature.entry.SpawnListEntry(livingHandler.groupID, structureKey, 0, 4, 0, 4, "");
    }
}
