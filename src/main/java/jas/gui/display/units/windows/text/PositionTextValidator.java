package jas.gui.display.units.windows.text;

import jas.gui.display.units.DisplayUnitSettable;
import jas.gui.display.units.windows.DisplayUnitTextField.Validator;
import jas.gui.utilities.Coord;
import jas.gui.utilities.StringHelper;
import net.minecraft.util.ChatAllowedCharacters;

public class PositionTextValidator implements Validator {
    private DisplayUnitSettable display;
    private boolean xCoord;

    public PositionTextValidator(DisplayUnitSettable settableDisplay, boolean xCoord) {
        this.display = settableDisplay;
        this.xCoord = xCoord;
    }

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
    public void setString(String text) {
        if (xCoord) {
            display.setOffset(new Coord(Integer.parseInt(text), display.getOffset().z));
        } else {
            display.setOffset(new Coord(display.getOffset().x, Integer.parseInt(text)));
        }
    }

    @Override
    public String getString() {
        if (xCoord) {
            return Integer.toString(display.getOffset().x);
        } else {
            return Integer.toString(display.getOffset().z);
        }
    }
}
