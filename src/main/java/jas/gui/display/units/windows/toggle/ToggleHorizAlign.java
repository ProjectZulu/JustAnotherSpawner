package jas.gui.display.units.windows.toggle;

import jas.gui.display.units.DisplayUnitSettable;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;
import jas.gui.utilities.Coord;

public class ToggleHorizAlign implements Toggle {
    private DisplayUnitSettable displayToSet;
    private HorizontalAlignment alignmentToSet;

    public ToggleHorizAlign(DisplayUnitSettable displayToSet, HorizontalAlignment alignment) {
        this.displayToSet = displayToSet;
        this.alignmentToSet = alignment;
    }

    @Override
    public void toggle() {
        displayToSet.setHorizontalAlignment(alignmentToSet);
        // Reset position to prevent display from becoming lost outside screen
        displayToSet.setOffset(new Coord(0, 0));
    }

    @Override
    public boolean isToggled() {
        return displayToSet.getHorizontalAlignment() == alignmentToSet;
    }
}
