package jas.spawner.legacy.spawner.biome.structure;

import jas.api.StructureInterpreter;
import jas.common.JASLog;
import jas.spawner.legacy.spawner.creature.entry.SpawnListEntry;
import jas.spawner.legacy.spawner.creature.entry.SpawnListEntryBuilder;
import jas.spawner.legacy.spawner.creature.handler.LivingHandler;
import jas.spawner.legacy.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.legacy.spawner.creature.type.CreatureTypeRegistry;
import jas.spawner.legacy.world.WorldProperties;
import jas.spawner.modern.DefaultProps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.world.World;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public class StructureHandler {
    public final StructureInterpreter interpreter;

    private final ImmutableList<String> structureKeys;
    private ImmutableListMultimap<String, SpawnListEntry> structureKeysToSpawnList;
    /* Tracks valid entries that are currently ignore (weight ==0 or type==NONE) but need to be kept in the config */
    private ImmutableListMultimap<String, SpawnListEntry> structureKeysToDisabledpawnList;

    public StructureHandler(StructureInterpreter interpreter) {
        Builder<String> builder = ImmutableList.builder();
        this.interpreter = interpreter;
        for (String structureKey : interpreter.getStructureKeys()) {
            builder.add(structureKey);
        }
        structureKeys = builder.build();
    }

    public ImmutableList<String> getStructureKeys() {
        return structureKeys;
    }

    public ImmutableList<SpawnListEntry> getStructureSpawnList(String structureKey) {
        return structureKeysToSpawnList.get(structureKey);
    }

    public ImmutableList<SpawnListEntry> getStructureDisabledSpawnList(String structureKey) {
        return structureKeysToDisabledpawnList.get(structureKey);
    }

    /**
     * Gets the SpawnList For the Worlds Biome Coords Provided.
     * 
     * @return Collection of JAS SpawnListEntries that should be spawn. Return Empty list if none.
     */
    public Collection<SpawnListEntry> getStructureSpawnList(World world, int xCoord, int yCoord, int zCoord) {
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
    public final void readFromConfig(LivingHandlerRegistry livingHandlerRegistry,
            HashMap<String, Collection<SpawnListEntryBuilder>> readSpawnLists, WorldProperties worldProperties) {

        ListMultimap<String, SpawnListEntry> structureKeysToSpawnList = ArrayListMultimap.create();
        ListMultimap<String, SpawnListEntry> structureKeysToDisabledpawnList = ArrayListMultimap.create();

        for (String structureKey : structureKeys) {
            Collection<SpawnListEntryBuilder> spawnList = readSpawnLists.get(structureKey);
            if (spawnList == null) {
                spawnList = new ArrayList<SpawnListEntryBuilder>();
                for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : interpreter
                        .getStructureSpawnList(structureKey)) {
                    @SuppressWarnings("unchecked")
                    List<LivingHandler> handlers = livingHandlerRegistry.getLivingHandlers(spawnListEntry.entityClass);
                    if (!handlers.isEmpty()) {
                        SpawnListEntryBuilder builder = new SpawnListEntryBuilder(handlers.get(0).groupID, structureKey);
                        builder.setWeight(spawnListEntry.itemWeight).setMinChunkPack(spawnListEntry.minGroupCount)
                                .setMaxChunkPack(spawnListEntry.maxGroupCount);
                        spawnList.add(builder);
                    } else {
                        JASLog.log().warning(
                                "Default entity %s that should spawn in structure %s does not appear to belong to an entity group.",
                                spawnListEntry.entityClass.getSimpleName(), structureKey);
                    }
                }
            }

            for (SpawnListEntryBuilder spawnBuilder : spawnList) {
                SpawnListEntry spawnEntry = spawnBuilder.build();
                LivingHandler handler = livingHandlerRegistry.getLivingHandler(spawnBuilder.getLivingGroupId());
                if (handler == null) {
                    JASLog.log().severe("Error loading structure %s. EntityGroup %s doesn not appear to exist.",
                            structureKey, spawnEntry.livingGroupID);
                    continue;
                }

                if (!handler.creatureTypeID.equals(CreatureTypeRegistry.NONE)) {
                    if (spawnEntry.itemWeight > 0 && handler.shouldSpawn) {
                        JASLog.log().info("Adding SpawnListEntry %s of type %s to StructureKey %s", handler.groupID,
                                handler.creatureTypeID, structureKey);
                        structureKeysToSpawnList.put(structureKey, spawnEntry);
                    } else {
                        structureKeysToDisabledpawnList.put(structureKey, spawnEntry);
                        JASLog.log().debug(
                                Level.INFO,
                                "Not adding Structure SpawnListEntry of %s to StructureKey %s due to Weight %s or ShouldSpawn %s.",
                                handler.groupID, structureKey, spawnEntry.itemWeight, handler.shouldSpawn);
                    }
                } else {
                    JASLog.log().debug(Level.INFO,
                            "Not Generating Structure %s SpawnList entries for %s. CreatureTypeID: %s", structureKey,
                            handler.groupID, handler.creatureTypeID);
                }
            }

        }
        this.structureKeysToSpawnList = ImmutableListMultimap.<String, SpawnListEntry> builder()
                .putAll(structureKeysToSpawnList).build();
        this.structureKeysToDisabledpawnList = ImmutableListMultimap.<String, SpawnListEntry> builder()
                .putAll(structureKeysToDisabledpawnList).build();
    }

    public static File getFile(File configDirectory, String saveName) {
        return new File(configDirectory, DefaultProps.WORLDSETTINGSDIR + saveName + "/" + "StructureSpawns.cfg");
    }
}
