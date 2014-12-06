package jas.gui.display.units.windows.button;

import jas.gui.display.units.DisplayUnit.ActionResult;
import jas.gui.display.units.windows.DisplayUnitButton.Clicker;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;

import com.google.common.base.Optional;

public class MoveScrollElementToggle<T> implements Toggle {

    private Scrollable<T> container;
    private int unitstoMove;

    public MoveScrollElementToggle(Scrollable<T> container, int unitstoMove) {
        this.container = container;
        this.unitstoMove = unitstoMove;
    }

    @Override
    public void toggle() {
        Optional<ScrollableElement<T>> toMove = container.getSelected();
        if (toMove.isPresent()) {
            container.moveElement(toMove.get(), unitstoMove);
        }
    }

    @Override
    public boolean isToggled() {
        return container.getSelected().isPresent();
    }
}
