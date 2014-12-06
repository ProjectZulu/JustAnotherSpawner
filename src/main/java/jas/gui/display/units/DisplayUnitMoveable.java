package jas.gui.display.units;

import jas.gui.GuiLog;
import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.utilities.Coord;
import jas.gui.utilities.GsonHelper;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;

public abstract class DisplayUnitMoveable implements DisplayUnit {
    protected Coord offset;
    protected transient boolean clickedOn = false;
    // Mouse location was on mouseClick. Click + Drag -> Offset = OriginalOffset + (MousePos - mousePosOnClick)
    protected transient Coord mousePosOnClick;
    // Original location were on mouseClick. Click + Drag -> Offset = OriginalOffset + (MousePos - mousePosOnClick)
    protected transient Coord offsetPosOnClick;

    public DisplayUnitMoveable(Coord offset) {
        this.offset = offset;
    }

    @Override
    public final Coord getOffset() {
        return offset;
    }

    @Override
    public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
        switch (action) {
        case CLICK:
            // actionData[0] == EventButton, 0 == Left-Click, 1 == Right-Click
            if (DisplayHelper.isCursorOverDisplay(localMouse, this)) {
                if (actionData[0] == 0) {
                    clickedOn = true;
                    mousePosOnClick = localMouse;
                    offsetPosOnClick = offset;
                }
                return ActionResult.SIMPLEACTION;
            }
            break;
        case CLICK_MOVE:
            if (clickedOn) {
                offset = offsetPosOnClick.add(localMouse.subt(mousePosOnClick));
                return ActionResult.SIMPLEACTION;
            }
            break;
        case RELEASE:
            clickedOn = false;
            return ActionResult.NOACTION;
        case SCROLL:
            break;
        }
        return ActionResult.NOACTION;
    }

    @Override
    public ActionResult keyTyped(char eventCharacter, int eventKey) {
        if (clickedOn) {
            if (Keyboard.KEY_LEFT == eventKey) {
                offset = offset.add(-1, 0);
                mousePosOnClick = mousePosOnClick.subt(-1, 0);
            } else if (Keyboard.KEY_RIGHT == eventKey) {
                offset = offset.add(+1, 0);
                mousePosOnClick = mousePosOnClick.subt(+1, 0);
            } else if (Keyboard.KEY_DOWN == eventKey) {
                offset = offset.add(0, +1);
                mousePosOnClick = mousePosOnClick.subt(0, +1);
            } else if (Keyboard.KEY_UP == eventKey) {
                offset = offset.add(0, -1);
                mousePosOnClick = mousePosOnClick.subt(0, -1);
            }
        }
        return ActionResult.NOACTION;
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
        offset = parseCoord(GsonHelper.getMemberOrDefault(customData, "DISPLAY_OFFSET", ""), new Coord(0, 0));
    }

    private Coord parseCoord(String stringForm, Coord defaultCoord) {
        String[] parts = stringForm.split(",");
        if (parts.length == 2) {
            try {
                int xCoord = Integer.parseInt(parts[0]);
                int zCoord = Integer.parseInt(parts[1]);
                return new Coord(xCoord, zCoord);
            } catch (NumberFormatException e) {
                GuiLog.log().info("Error parsing coordinate string %s. Will be replaced by %s", stringForm, defaultCoord);
            }
        }
        return defaultCoord;
    }
    
    @Override
    public void saveCustomData(JsonObject jsonObject) {
        jsonObject.addProperty("DISPLAY_OFFSET", offset.x + "," + offset.z);
    }
}
