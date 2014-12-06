package jas.gui.display.inventoryrules;

import jas.gui.display.DisplayRemoteDisplay;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;
import jas.gui.utilities.Coord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import com.google.common.base.Optional;

public class ScrollableSubDisplays<T extends DisplayUnit> implements Scrollable<T> {
    private List<T> source;
    private List<ScrollableElement<T>> displays;
    private Optional<ScrollableElement<T>> selectedEntry = Optional.absent();

    private static class ScrollableRemoteDisplay<T extends DisplayUnit> extends DisplayRemoteDisplay<T> implements
            ScrollableElement<T> {
        public static final String DISPLAY_ID = "SubDisplay";
        private boolean scrollVisibility = false;

        public ScrollableRemoteDisplay(T subDisplay) {
            super(subDisplay);
        }

        @Override
        public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
            if (action == MouseAction.CLICK && actionData[0] == 0) {
                // Convert LeftClicks into RightClicks, as dragging is not supported by LeftClick is always drag
                return super.mouseAction(localMouse, action, new int[] { 1 });
            } else {
                return super.mouseAction(localMouse, action, actionData);
            }
        }

        @Override
        public void setScrollVisibity(boolean visibility) {
            scrollVisibility = visibility;
        }

        @Override
        public boolean isVisibleInScroll() {
            return scrollVisibility;
        }

        @Override
        public T getSource() {
            return remoteDisplay;
        }
    }

    public ScrollableSubDisplays(List<T> displaySource) {
        this.source = displaySource;
        this.displays = new ArrayList<ScrollableElement<T>>();
        for (T displayUnit : this.source) {
            this.displays.add(new ScrollableRemoteDisplay<T>(displayUnit));
        }
    }

    @Override
    public Collection<? extends ScrollableElement<T>> getElements() {
        return displays;
    }

    @Override
    public boolean removeElement(ScrollableElement<T> element) {
        source.remove(element.getSource());
        return displays.remove(element);
    }

    public boolean addElement(T element) {
        source.add(element);
        return displays.add(new ScrollableRemoteDisplay<T>(element));
    }

    @Override
    public boolean addElement(ScrollableElement<T> element) {
        source.add(element.getSource());
        return displays.add(element);
    }

    @Override
    public void moveElement(ScrollableElement<T> element, int unitstoMove) {
        int scrollIndex = displays.indexOf(element);
        int sourceIndex = source.indexOf(element.getSource());
        if (isSwapValid(displays, scrollIndex, unitstoMove) && isSwapValid(source, sourceIndex, unitstoMove)) {
            Collections.swap(displays, scrollIndex, scrollIndex + unitstoMove);
            Collections.swap(source, sourceIndex, sourceIndex + unitstoMove);
        }
    }

    private <K> boolean isSwapValid(List<K> list, int index, int indexToMove) {
        if (index >= 0 && index < list.size()) {
            if (index + indexToMove >= 0 && index + indexToMove < list.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setSelected(ScrollableElement<T> element) {
        selectedEntry = element != null ? Optional.of(element) : Optional.<ScrollableElement<T>> absent();
    }

    @Override
    public Optional<ScrollableElement<T>> getSelected() {
        return selectedEntry;
    }
}