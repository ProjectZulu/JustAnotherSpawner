package jas.gui.display.units.windows.button;

import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;

import com.google.common.base.Optional;

public class RemoveScrollToggle<T> implements Toggle {

    private Scrollable<T> container;

    public RemoveScrollToggle(Scrollable<T> container) {
        this.container = container;
    }

    @Override
    public void toggle() {
        Optional<ScrollableElement<T>> toRemove = container.getSelected();
        if (toRemove.isPresent()) {
            if (container.removeElement(toRemove.get())) {
                container.setSelected(null);
            }
        }
    }

    @Override
    public boolean isToggled() {
        return container.getSelected().isPresent();
    }
}
