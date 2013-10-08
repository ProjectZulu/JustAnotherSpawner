package jas.common;

import jas.api.StructureInterpreter;
import jas.api.CompatibilityLoader;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;

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
