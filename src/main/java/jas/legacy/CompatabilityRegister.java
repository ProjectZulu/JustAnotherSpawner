package jas.legacy;

import jas.api.CompatibilityLoader;
import jas.api.StructureInterpreter;
import jas.legacy.spawner.biome.structure.StructureHandlerRegistry;

public class CompatabilityRegister implements CompatibilityLoader {

    @Override
    public boolean registerObject(Object object) {
        if (object instanceof StructureInterpreter) {
            StructureHandlerRegistry.registerInterpreter((StructureInterpreter) object);
            return true;
        }
        return false;
    }
}
