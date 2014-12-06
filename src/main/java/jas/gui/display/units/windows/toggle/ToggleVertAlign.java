package jas.gui.display.units.windows.toggle;

import jas.gui.display.units.DisplayUnitSettable;
import jas.gui.display.units.DisplayUnit.VerticalAlignment;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;
import jas.gui.utilities.Coord;

public class ToggleVertAlign implements Toggle {
    private DisplayUnitSettable displayToSet;
    private VerticalAlignment alignmentToSet;

    public ToggleVertAlign(DisplayUnitSettable displayToSet, VerticalAlignment alignment) {
        this.displayToSet = displayToSet;
        this.alignmentToSet = alignment;
    }

    @Override
    public void toggle() {
        displayToSet.setVerticalAlignment(alignmentToSet);
        // Reset position to prevent display from becoming lost outside screen
        displayToSet.setOffset(new Coord(0, 0));
    }

    @Override
    public boolean isToggled() {
        return displayToSet.getVerticalAlignment() == alignmentToSet;
    }
}
