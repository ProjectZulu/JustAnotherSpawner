package jas.gui.display.units.windows.text;

import jas.gui.display.units.windows.DisplayUnitTextField.Validator;
import jas.gui.utilities.StringHelper;
import net.minecraft.util.ChatAllowedCharacters;

public abstract class ValidatorInt implements Validator {

    @Override
    public boolean isCharacterValid(char eventCharacter) {
        return ('-' == eventCharacter || Character.isDigit(eventCharacter))
                && ChatAllowedCharacters.isAllowedCharacter(eventCharacter);
    }

    @Override
    public boolean isStringValid(String text) {
        return StringHelper.isInteger(text);
    }

    @Override
    public final void setString(String text) {
        setInt(Integer.parseInt(text));
    }

    public abstract void setInt(int textValue);

    @Override
    public final String getString() {
        return Integer.toString(getValue());
    }

    public abstract int getValue();
}
