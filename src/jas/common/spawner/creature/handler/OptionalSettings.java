package jas.common.spawner.creature.handler;

import java.util.HashMap;

public abstract class OptionalSettings {

    public final String parseableString;
    protected boolean stringParsed = false;

    /* Internal Cache to Store Parsed Values */
    protected HashMap<String, Object> valueCache = new HashMap<String, Object>();

    public OptionalSettings(String parseableString) {
        this.parseableString = parseableString;
    }

    protected abstract void parseString();

    public abstract boolean isOptionalEnabled();
}
