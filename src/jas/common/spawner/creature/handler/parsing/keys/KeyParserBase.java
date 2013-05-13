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
        if (string == null) {
            return false;
        }

        Character character = isFirstSpecial(string);
        if (isSpecialCharValid(character)) {
            string = string.substring(1);
            character = isFirstSpecial(string);
            if (isSpecialCharValid(character)) {
                string = string.substring(1);
            }
        }

        return string.equalsIgnoreCase(key.key);
    }

    protected final Character isFirstSpecial(String string) {
        if (string.startsWith("&") || string.startsWith("|") || string.startsWith("!")) {
            return string.charAt(0);
        }
        return null;
    }

    protected final boolean isSpecialCharValid(Character character) {
        if (character == null) {
            return false;
        }
        if (character.equals('&') || character.equals('|')) {
            return getKeyType() == KeyType.CHAINABLE;
        } else if (character.equals('!')) {
            return isInvertable;
        }
        return false;
    }

    protected Operand getOperand(String[] parseable) {
        Operand operand = Operand.OR;
        if (parseable[0].charAt(0) == '&' || parseable[0].charAt(1) == '&') {
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