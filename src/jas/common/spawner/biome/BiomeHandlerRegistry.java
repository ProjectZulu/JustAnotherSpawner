package jas.common.spawner.biome;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.world.WorldServer;

public enum BiomeHandlerRegistry {
    INSTANCE;

    private final ArrayList<BiomeInterpreter> biomeInterpreters = new ArrayList<BiomeInterpreter>();
    private final ArrayList<BiomeHandler> biomeHandlers = new ArrayList<BiomeHandler>();

    BiomeHandlerRegistry() {
        biomeInterpreters.add(new BiomeInterpreterSwamp());
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
}
