package jas.gui.display.units.windows.toggle;

import jas.gui.display.units.DisplayUnitCountable;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;

public class ToggleDigitalCounter implements Toggle {
    private DisplayUnitCountable countable;

    public ToggleDigitalCounter(DisplayUnitCountable countable) {
        this.countable = countable;
    }

    @Override
    public void toggle() {
        countable.enableDigitalCounter(!countable.isDigitalEnabled());
    }

    @Override
    public boolean isToggled() {
        return countable.isDigitalEnabled();
    }
}
