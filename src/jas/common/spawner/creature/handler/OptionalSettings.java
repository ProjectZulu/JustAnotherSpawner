package jas.common.spawner.creature.handler;

import java.util.HashMap;

/**
 * For Optional Settings methods return style is to return true when evaluation should continue unobstructed, false to
 * obstruct, null to use default check.
 * 
 * i.e: If entity is Despawning, and is checking isValidLight: true means continue despawning, false means stop, null
 * means no opinion use default check
 * 
 */
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

    public abstract boolean isInverted();
}
