package jas.common;
import jas.api.BiomeInterpreter;
import jas.api.CompatibilityLoader;
import jas.common.spawner.biome.structure.BiomeHandlerRegistry;

public class CompatabilityRegister implements CompatibilityLoader {

    @Override
    public boolean registerObject(Object object) {
        if (object instanceof BiomeInterpreter) {
            BiomeHandlerRegistry.INSTANCE.registerInterpreter((BiomeInterpreter) object);
            return true;
        }
        return false;
    }
}
