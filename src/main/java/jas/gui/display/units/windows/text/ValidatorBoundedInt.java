package jas.gui.display.units.windows.text;

import jas.gui.display.units.windows.DisplayUnitTextField.Validator;
import jas.gui.utilities.StringHelper;
import net.minecraft.util.ChatAllowedCharacters;

public abstract class ValidatorBoundedInt extends ValidatorInt {
    private int lowerBound;
    private int upperBound;

    public ValidatorBoundedInt(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean isStringValid(String text) {
        if (!super.isStringValid(text)) {
            return false;
        }
        int number = Integer.parseInt(text);
        return number >= lowerBound && number <= upperBound;
    }
}
