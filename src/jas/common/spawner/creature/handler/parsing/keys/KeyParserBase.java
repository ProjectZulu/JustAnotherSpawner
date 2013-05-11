package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

public abstract class KeyParserBase extends KeyParser {

    public final Key key;
    public final boolean isInvertable;
    public final KeyType isChainable;

    public KeyParserBase(Key key, boolean isInvertable, KeyType isChainable) {
        this.key = key;
        this.isInvertable = isInvertable;
        this.isChainable = isChainable;
    }

    @Override
    public boolean isMatch(String string) {
        if (string.equalsIgnoreCase(key.key)) {
            return true;
        } else if (isInvertable() && string.equalsIgnoreCase("!" + key.key)) {
            return true;
        } else if (getKeyType() == KeyType.CHAINABLE
                && (string.equalsIgnoreCase("|" + key.key) || string.equalsIgnoreCase("&" + key.key))) {
            return true;
        }
        return false;
    }

    protected Operand getOperand(String[] parseable) {
        Operand operand = Operand.OR;
        if (parseable[0].startsWith("&")) {
            operand = Operand.AND;
        }
        return operand;
    }

    @Override
    public boolean isInvertable() {
        return isInvertable;
    }

    @Override
    public KeyType getKeyType() {
        return isChainable;
    }
}