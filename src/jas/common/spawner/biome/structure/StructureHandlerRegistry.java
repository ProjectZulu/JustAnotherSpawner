package jas.common.spawner.biome.structure;

import jas.api.StructureInterpreter;
import jas.common.WorldProperties;
import jas.common.config.StructureConfiguration;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.World;

public class StructureHandlerRegistry {
    private static final ArrayList<StructureInterpreter> structureInterpreters = new ArrayList<StructureInterpreter>();
    private final ArrayList<StructureHandler> structureHandlers = new ArrayList<StructureHandler>();
    public final LivingHandlerRegistry livingHandlerRegistry;
    public final WorldProperties worldProperties;

    static {
        structureInterpreters.add(new StructureInterpreterSwamp());
        structureInterpreters.add(new StructureInterpreterNether());
        structureInterpreters.add(new StructureInterpreterOverworldStructures());
    }

    public static void registerInterpreter(StructureInterpreter structureInterpreter) {
        structureInterpreters.add(structureInterpreter);
    }

    public StructureHandlerRegistry(LivingHandlerRegistry livingHandlerRegistry, WorldProperties worldProperties) {
        this.livingHandlerRegistry = livingHandlerRegistry;
        this.worldProperties = worldProperties;
    }

    public void setupHandlers(File configDirectory, World world) {
        for (StructureInterpreter interpreter : structureInterpreters) {
            structureHandlers.add(new StructureHandler(interpreter));
        }

        StructureConfiguration structureConfig = new StructureConfiguration(configDirectory, worldProperties);
        structureConfig.load();
        for (StructureHandler structureHandler : structureHandlers) {
            structureHandler.readFromConfig(livingHandlerRegistry, structureConfig, worldProperties);
        }
        structureConfig.save();
    }

    public Iterator<StructureHandler> getHandlers() {
        return structureHandlers.iterator();
    }

    public ImmutableList<StructureHandler> handlers() {
        return ImmutableList.copyOf(structureHandlers);
    }

    /**
     * Used to save the currently loaded settings into the Configuration Files
     * 
     * If config settings are already present, they will be overwritten
     */
    public void saveCurrentToConfig(File configDirectory) {
        StructureConfiguration structureConfig = new StructureConfiguration(configDirectory, worldProperties);
        structureConfig.load();
        for (StructureHandler handler : structureHandlers) {
            handler.saveToConfig(structureConfig, worldProperties);
        }
        structureConfig.save();
    }

    public Collection<SpawnListEntry> getSpawnListAt(World world, int xCoord, int yCoord, int zCoord) {
        Iterator<StructureHandler> iterator = this.getHandlers();
        while (iterator.hasNext()) {
            StructureHandler handler = iterator.next();
            if (handler.doesHandlerApply(world, xCoord, yCoord, zCoord)) {
                Collection<SpawnListEntry> spawnEntryList = handler
                        .getStructureSpawnList(world, xCoord, yCoord, zCoord);
                if (!spawnEntryList.isEmpty()) {
                    return spawnEntryList;
                }
            }
        }
        return Collections.emptyList();
    }
}
