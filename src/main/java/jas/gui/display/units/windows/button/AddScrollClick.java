package jas.gui.display.units.windows.button;

import jas.gui.display.units.DisplayUnit.ActionResult;
import jas.gui.display.units.windows.DisplayUnitButton.Clicker;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;

public abstract class AddScrollClick<T, K extends Scrollable<T>> implements Clicker {

    private K container;

    public AddScrollClick(K container) {
        this.container = container;
    }

    @Override
    public ActionResult onClick() {
        return ActionResult.SIMPLEACTION;
    }

    @Override
    public ActionResult onRelease() {
        performScrollAddition(container);
        return ActionResult.SIMPLEACTION;
    }

    public abstract void performScrollAddition(K container);
}
