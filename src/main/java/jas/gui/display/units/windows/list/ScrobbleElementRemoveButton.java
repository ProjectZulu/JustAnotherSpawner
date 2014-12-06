package jas.gui.display.units.windows.list;

import jas.gui.display.units.DisplayUnit.ActionResult;
import jas.gui.display.units.windows.DisplayUnitButton.Clicker;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;

public class ScrobbleElementRemoveButton<T> implements Clicker {
    private ScrollableElement<T> element;
    private Scrollable<T> container;

    public ScrobbleElementRemoveButton(ScrollableElement<T> element, Scrollable<T> container) {
        this.element = element;
        this.container = container;
    }

    @Override
    public ActionResult onClick() {
        return ActionResult.SIMPLEACTION;
    }

    @Override
    public ActionResult onRelease() {
        container.removeElement(element);
        return ActionResult.SIMPLEACTION;
    }
}
