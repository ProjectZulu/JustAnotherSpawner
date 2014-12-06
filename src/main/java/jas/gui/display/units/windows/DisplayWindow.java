package jas.gui.display.units.windows;

import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnitMoveable;
import jas.gui.utilities.Coord;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import net.minecraft.client.Minecraft;

import com.google.gson.JsonObject;

/**
 * Tree structure for propagating interactive events through nested DisplayUnits that wraps valid DisplayUnits
 * 
 * Not actually strictly speaking a DisplayUnit and should not be added to DisplayTicker/DisplayUnitRegistry
 * 
 * Implicitly assumes Children are 'above' the base display and are a priority for input events
 */
public abstract class DisplayWindow extends DisplayUnitMoveable {
    public static final String DISPLAY_ID = "DisplayUnitWindow";

    /**
     * Windows are child displays that, while depending on the parent display fulfill some isolated role such as a
     * pop-up to select an item. Windows should be able to be closed without closing the parent.
     */
    private ArrayList<DisplayUnit> windows;
    /**
     * Elements are part of the children of the current window, such as a text field and buttons that should exist as
     * long as the parent exists
     */
    protected final ArrayList<DisplayUnit> elements;

    /**
     * Temporary list of displays that need to be moved higher in the display list (higher displays get events sooner)
     * 
     * Displays added to priority that do not exist in the window list should will be ignored during processing.
     */
    private final Queue<DisplayUnit> priority;

    public DisplayWindow() {
        this(new Coord(0, 0));
    }

    public DisplayWindow(Coord coord) {
        super(coord);
        this.windows = new ArrayList<DisplayUnit>();
        this.priority = new ArrayDeque<DisplayUnit>();
        this.elements = new ArrayList<DisplayUnit>();
    }

    public final void addWindow(DisplayUnit window) {
        windows.add(0, window);
    }

    public final boolean removeWindow(DisplayUnit window) {
        return windows.remove(window);
    }

    protected final void clearWindows() {
        windows.clear();
    }

    public boolean addElement(DisplayUnit element) {
        return elements.add(element);
    }

    public boolean removeElement(DisplayUnit element) {
        return elements.remove(element);
    }

    @Override
    public final String getType() {
        StringBuilder sb = new StringBuilder();
        sb.append(DISPLAY_ID).append(":").append(getSubType()).append("[");
        Iterator<DisplayUnit> iterator = windows.iterator();
        while (iterator.hasNext()) {
            DisplayUnit window = iterator.next();
            sb.append(window.getType());
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");
        return DISPLAY_ID.concat(":").concat(getSubType()).concat("[");
    }

    public abstract String getSubType();

    @Override
    public void onUpdate(Minecraft mc, int ticks) {
        while (!priority.isEmpty()) {
            DisplayUnit display = priority.poll();
            if (windows.remove(display)) {
                windows.add(0, display);
            }
        }

        for (DisplayUnit window : windows) {
            window.onUpdate(mc, ticks);
        }

        for (DisplayUnit element : elements) {
            element.onUpdate(mc, ticks);
        }
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return true;
    }

    @Override
    public final void renderDisplay(Minecraft mc, Coord position) {
        renderSubDisplay(mc, position);

        for (int i = elements.size() - 1; i >= 0; i--) {
            DisplayUnit element = elements.get(i);
            element.renderDisplay(mc,
                    DisplayHelper.determineScreenPositionFromDisplay(mc, position, getSize(), element));
        }

        /**
         * Reverse iteration we are doing back to front rendering and top of list is considered 'front' i.e. given
         * priority for clicks
         */
        for (int i = windows.size() - 1; i >= 0; i--) {
            DisplayUnit window = windows.get(i);
            window.renderDisplay(mc, DisplayHelper.determineScreenPositionFromDisplay(mc, position, getSize(), window));
        }
    }

    public abstract void renderSubDisplay(Minecraft mc, Coord position);

    @Override
    public void saveCustomData(JsonObject jsonObject) {
        throw new UnsupportedOperationException("DisplayWindows do not have memory");
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
        throw new UnsupportedOperationException("DisplayWindows do not have memory");
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker alreadyHovering) {
        for (DisplayUnit window : windows) {
            Coord childCoords = DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, window);
            HoverAction childHover = HoverAction.OUTSIDE;
            if (DisplayHelper.isCursorOverDisplay(childCoords, window)) {
                childHover = !alreadyHovering.isHoverFound() ? HoverAction.HOVER : HoverAction.BLOCKED;
            }
            window.mousePosition(childCoords, childHover, alreadyHovering);
        }

        for (DisplayUnit element : elements) {
            Coord childCoords = DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, element);
            HoverAction childHover = HoverAction.OUTSIDE;
            if (DisplayHelper.isCursorOverDisplay(childCoords, element)) {
                childHover = !alreadyHovering.isHoverFound() ? HoverAction.HOVER : HoverAction.BLOCKED;
            }
            element.mousePosition(childCoords, childHover, alreadyHovering);
        }
        if (hoverAction != HoverAction.OUTSIDE) {
            alreadyHovering.markHoverFound();
        }
    }

    @Override
    public final ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
        for (DisplayUnit window : windows) {
            ActionResult result = window.mouseAction(
                    DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, window), action,
                    actionData);
            if (processActionResult(result, window)) {
                return result.parentResult();
            }
        }
        for (DisplayUnit element : elements) {
            ActionResult result = element.mouseAction(
                    DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, element), action,
                    actionData);
            if (processActionResult(result, element)) {
                return result.parentResult();
            }
        }
        ActionResult result = subMouseAction(localMouse, action, actionData);
        if (processActionResult(result, this)) {
            return result.parentResult();
        }
        return super.mouseAction(localMouse, action, actionData);
    }

    public abstract ActionResult subMouseAction(Coord localMouse, MouseAction action, int... actionData);

    /**
     * @return StopProcessing - true if processing should be stopped
     */
    private boolean processActionResult(ActionResult action, DisplayUnit provider) {
        if (action.closeAll()) {
            clearWindows();
        } else {
            List<DisplayUnit> displaysToClose = action.screensToClose();
            for (DisplayUnit displayUnit : displaysToClose) {
                removeWindow(displayUnit);
            }
        }

        List<DisplayUnit> displaysToOpen = action.screensToOpen();
        for (DisplayUnit displayUnit : displaysToOpen) {
            addWindow(displayUnit);
        }

        if (action.shouldStop()) {
            // Some interaction occurred in that display, elevate it to receive events sooner
            priority.add(provider);
        }
        return action.shouldStop();
    }

    @Override
    public final ActionResult keyTyped(char eventCharacter, int eventKey) {
        for (DisplayUnit window : windows) {
            ActionResult result = window.keyTyped(eventCharacter, eventKey);
            if (processActionResult(result, window)) {
                return result.parentResult();
            }
        }
        for (DisplayUnit element : elements) {
            ActionResult result = element.keyTyped(eventCharacter, eventKey);
            if (processActionResult(result, element)) {
                return result.parentResult();
            }
        }

        ActionResult result = subKeyTyped(eventCharacter, eventKey);
        if (processActionResult(result, this)) {
            return result.parentResult();
        }

        return super.keyTyped(eventCharacter, eventKey);
    }

    public abstract ActionResult subKeyTyped(char eventCharacter, int eventKey);
    
    public void saveWindow() {

    }

    public void closeWindow() {

    }
}
