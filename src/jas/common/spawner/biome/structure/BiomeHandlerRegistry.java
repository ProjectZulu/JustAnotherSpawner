package jas.common.spawner.biome.structure;

import jas.api.BiomeInterpreter;
import jas.common.WorldProperties;
import jas.common.config.StructureConfiguration;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.World;

public class BiomeHandlerRegistry {
    private static final ArrayList<BiomeInterpreter> biomeInterpreters = new ArrayList<BiomeInterpreter>();
    private final ArrayList<BiomeHandler> biomeHandlers = new ArrayList<BiomeHandler>();
    public final CreatureHandlerRegistry creatureHandlerRegistry;
    public final WorldProperties worldProperties;

    static {
        biomeInterpreters.add(new BiomeInterpreterSwamp());
        biomeInterpreters.add(new BiomeInterpreterNether());
        biomeInterpreters.add(new BiomeInterpreterOverworldStructures());
    }

    public static void registerInterpreter(BiomeInterpreter biomeInterpreter) {
        biomeInterpreters.add(biomeInterpreter);
    }

    public BiomeHandlerRegistry(CreatureHandlerRegistry creatureHandlerRegistry, WorldProperties worldProperties) {
        this.creatureHandlerRegistry = creatureHandlerRegistry;
        this.worldProperties = worldProperties;
    }

    public void setupHandlers(File configDirectory, World world) {
        for (BiomeInterpreter interpreter : biomeInterpreters) {
            biomeHandlers.add(new BiomeHandler(interpreter));
        }

        StructureConfiguration structureConfig = new StructureConfiguration(configDirectory, worldProperties);
        structureConfig.load();
        for (BiomeHandler biomeHandler : biomeHandlers) {
            biomeHandler.readFromConfig(creatureHandlerRegistry, structureConfig, world, worldProperties);
        }
        structureConfig.save();
    }

    public Iterator<BiomeHandler> getHandlers() {
        return biomeHandlers.iterator();
    }

    public ImmutableList<BiomeHandler> handlers() {
        return ImmutableList.copyOf(biomeHandlers);
    }

    /**
     * Used to save the currently loaded settings into the Configuration Files
     * 
     * If config settings are already present, they will be overwritten
     */
    public void saveCurrentToConfig(File configDirectory) {
        StructureConfiguration structureConfig = new StructureConfiguration(configDirectory, worldProperties);
        structureConfig.load();
        for (BiomeHandler handler : biomeHandlers) {
            handler.saveToConfig(structureConfig, worldProperties);
        }
        structureConfig.save();
    }

    public Collection<SpawnListEntry> getSpawnListAt(World world, int xCoord, int yCoord, int zCoord) {
        Iterator<BiomeHandler> iterator = this.getHandlers();
        while (iterator.hasNext()) {
            BiomeHandler handler = iterator.next();
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
