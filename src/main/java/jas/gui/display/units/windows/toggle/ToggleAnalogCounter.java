package jas.gui.display.units.windows.toggle;

import jas.gui.display.units.DisplayUnitCountable;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;

public class ToggleAnalogCounter implements Toggle {
    private DisplayUnitCountable countable;

    public ToggleAnalogCounter(DisplayUnitCountable countable) {
        this.countable = countable;
    }

    @Override
    public void toggle() {
        countable.enableAnalogDisplay(!countable.isAnalogEnabled());
    }

    @Override
    public boolean isToggled() {
        return countable.isAnalogEnabled();
    }
}
