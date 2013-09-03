package jas.compatability;

import java.util.List;

public interface LoadInfo {
    
    public abstract List<String> getRequiredModIDs();

    public abstract List<Object> getObjectsToRegister();
    
    public abstract String loaderID();
}
