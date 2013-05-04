package jas.common.spawner.creature.handler;

import java.util.ArrayList;
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

    boolean isEnabled = false;
    boolean isInverted = false;
    public final String parseableString;
    protected boolean stringParsed = false;

    enum Operand {
        AND, OR;
    }

    /** Value Stored for a Parsed chainable Keys */
    protected ArrayList<TypeValuePair> parsedChainable = new ArrayList<TypeValuePair>();

    /**
     * Operand value for a Particular Parsed Key
     * 
     * i.e. Is This Property supposed to be & or | with the previous parsed key
     */
    protected ArrayList<Operand> operandvalue = new ArrayList<Operand>();

    protected void addParsedChainable(TypeValuePair typeValue, Operand operand) {
        if (typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
        }
    }

    /* Internal Cache to Store Parsed Values */
    protected HashMap<String, Object> valueCache = new HashMap<String, Object>();

    public OptionalSettings(String parseableString) {
        this.parseableString = parseableString;
        parseString();
    }

    protected abstract void parseString();

    public abstract boolean isOptionalEnabled();

    public abstract boolean isInverted();
}
