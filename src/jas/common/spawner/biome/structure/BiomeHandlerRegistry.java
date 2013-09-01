package jas.common.spawner.biome.structure;

import jas.api.BiomeInterpreter;
import jas.api.CompatibilityLoader;
import jas.common.config.StructureConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import net.minecraft.world.World;

public enum BiomeHandlerRegistry {
    INSTANCE;

    private final ArrayList<BiomeInterpreter> biomeInterpreters = new ArrayList<BiomeInterpreter>();
    private final ArrayList<BiomeHandler> biomeHandlers = new ArrayList<BiomeHandler>();

    BiomeHandlerRegistry() {
        biomeInterpreters.add(new BiomeInterpreterSwamp());
        biomeInterpreters.add(new BiomeInterpreterNether());
        biomeInterpreters.add(new BiomeInterpreterOverworldStructures());
    }

    public void registerInterpreter(BiomeInterpreter biomeInterpreter) {
        biomeInterpreters.add(biomeInterpreter);
    }

    public void setupHandlers(File configDir, World world) {
        for (BiomeInterpreter interpreter : biomeInterpreters) {
            biomeHandlers.add(new BiomeHandler(interpreter));
        }

        StructureConfiguration structureConfig = new StructureConfiguration(configDir);
        structureConfig.load();
        for (BiomeHandler biomeHandler : biomeHandlers) {
            biomeHandler.readFromConfig(structureConfig, world);
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
        StructureConfiguration structureConfig = new StructureConfiguration(configDirectory);
        for (BiomeHandler handler : biomeHandlers) {
            handler.saveToConfig(structureConfig);
        }
    }
}
