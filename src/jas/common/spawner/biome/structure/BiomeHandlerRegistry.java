package jas.common.spawner.biome.structure;

import jas.api.BiomeInterpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.world.WorldServer;

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

    public void setupHandlers(File configDir, WorldServer worldServer) {
        for (BiomeInterpreter interpreter : biomeInterpreters) {
            biomeHandlers.add(new BiomeHandler(interpreter));
        }

        for (BiomeHandler biomeHandler : biomeHandlers) {
            biomeHandler.readFromConfig(configDir, worldServer);
        }
    }

    public Iterator<BiomeHandler> getHandlers() {
        return biomeHandlers.iterator();
    }
}
