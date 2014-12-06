package jas.gui.display;

import jas.gui.GuiLog;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnitSettable;
import jas.gui.utilities.Coord;
import net.minecraft.client.Minecraft;

import com.google.gson.JsonObject;

/**
 * Allows for rendering a Display at a position other than its current, such as in a menu, while allowing interactions
 * to occur with it as if it was at the position.
 * 
 * RemoteDisplay should not update the wrapped display, as it is likely updating elsewhere, if neccesary.
 */
public class DisplayRemoteDisplay<T extends DisplayUnit> implements DisplayUnitSettable {
    public static final String DISPLAY_ID = "SubDisplay";

    protected T remoteDisplay;

    private Coord offset;
    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;

    public DisplayRemoteDisplay(T remoteDisplay) {
        this.remoteDisplay = remoteDisplay;
    }

    @Override
    public String getType() {
        return DISPLAY_ID + ":" + remoteDisplay.getType();
    }

    @Override
    public Coord getOffset() {
        return offset;
    }

    @Override
    public void setOffset(Coord offset) {
        this.offset = offset;
    }

    @Override
    public Coord getSize() {
        return remoteDisplay.getSize();
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return vertAlign;
    }

    @Override
    public void setVerticalAlignment(VerticalAlignment alignment) {
        vertAlign = alignment;
    }

    @Override
    public HorizontalAlignment getHorizontalAlignment() {
        return horizAlign;
    }

    @Override
    public void setHorizontalAlignment(HorizontalAlignment alignment) {
        horizAlign = alignment;
    }

    @Override
    public void onUpdate(Minecraft mc, int ticks) {

    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return true;
    }

    @Override
    public void renderDisplay(Minecraft mc, Coord position) {
        remoteDisplay.renderDisplay(mc, position);
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker alreadyHovering) {
        remoteDisplay.mousePosition(localToRemoteCoords(localMouse, remoteDisplay), hoverAction, alreadyHovering);
    }

    @Override
    public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
        Coord remoteCoords = localToRemoteCoords(localMouse, remoteDisplay);
        return remoteDisplay.mouseAction(remoteCoords, action, actionData);
    }

    private Coord localToRemoteCoords(Coord localMouse, DisplayUnit remoteDisplay) {
        int xCoord = remoteXCoord(localMouse.x, this, remoteDisplay);
        int yCoord = remoteYCoord(localMouse.z, this, remoteDisplay);
        return new Coord(xCoord, yCoord);
    }

    private int remoteXCoord(int localX, DisplayUnit localDisplay, DisplayUnit remoteDisplay) {
        HorizontalAlignment localAlign = localDisplay.getHorizontalAlignment();
        HorizontalAlignment remoteAlign = remoteDisplay.getHorizontalAlignment();
        int localOffset = localDisplay.getOffset().x;
        int remoteOffset = remoteDisplay.getOffset().x;
        int displaySize = remoteDisplay.getSize().x;
        if (localAlign == remoteAlign) {
            return localX + remoteOffset - localOffset;
        }
        if (localAlign == HorizontalAlignment.LEFT_ABSO) {
            if (remoteAlign == HorizontalAlignment.CENTER_ABSO) {
                return localX + remoteOffset - localOffset - displaySize / 2;
            } else if (remoteAlign == HorizontalAlignment.RIGHT_ABSO) {
                return localX + remoteOffset - localOffset - displaySize;
            }
        } else if (localAlign == HorizontalAlignment.CENTER_ABSO) {
            if (remoteAlign == HorizontalAlignment.LEFT_ABSO) {
                return localX + remoteOffset - localOffset + displaySize / 2;
            } else if (remoteAlign == HorizontalAlignment.RIGHT_ABSO) {
                return localX + remoteOffset - localOffset - displaySize / 2;
            }
        } else if (localAlign == HorizontalAlignment.RIGHT_ABSO) {
            if (remoteAlign == HorizontalAlignment.LEFT_ABSO) {
                return localX + remoteOffset - localOffset + displaySize;
            } else if (remoteAlign == HorizontalAlignment.CENTER_ABSO) {
                return localX + remoteOffset - localOffset + displaySize / 2;
            }
        }
        throw new IllegalArgumentException("Invalid Horizontal Alignment scenario");
    }

    private int remoteYCoord(int localX, DisplayUnit localDisplay, DisplayUnit remoteDisplay) {
        VerticalAlignment localAlign = localDisplay.getVerticalAlignment();
        VerticalAlignment remoteAlign = remoteDisplay.getVerticalAlignment();
        int localOffset = localDisplay.getOffset().z;
        int remoteOffset = remoteDisplay.getOffset().z;
        int displaySize = remoteDisplay.getSize().z;
        if (localAlign == remoteAlign) {
            return localX + remoteOffset - localOffset;
        }
        if (localAlign == VerticalAlignment.TOP_ABSO) {
            if (remoteAlign == VerticalAlignment.CENTER_ABSO) {
                return localX + remoteOffset - localOffset - displaySize / 2;
            } else if (remoteAlign == VerticalAlignment.BOTTOM_ABSO) {
                return localX + remoteOffset - localOffset - displaySize;
            }
        } else if (localAlign == VerticalAlignment.CENTER_ABSO) {
            if (remoteAlign == VerticalAlignment.TOP_ABSO) {
                return localX + remoteOffset - localOffset + displaySize / 2;
            } else if (remoteAlign == VerticalAlignment.BOTTOM_ABSO) {
                return localX + remoteOffset - localOffset - displaySize / 2;
            }
        } else if (localAlign == VerticalAlignment.BOTTOM_ABSO) {
            if (remoteAlign == VerticalAlignment.TOP_ABSO) {
                return localX + remoteOffset - localOffset + displaySize;
            } else if (remoteAlign == VerticalAlignment.CENTER_ABSO) {
                return localX + remoteOffset - localOffset + displaySize / 2;
            }
        }
        throw new IllegalArgumentException("Invalid Vertical Alignment scenario");
    }

    private int remoteYCoord(int localY, int displaySize, VerticalAlignment localAlign, VerticalAlignment remoteAlign) {
        if (localAlign == remoteAlign) {
            return localY;
        }

        if (localAlign == VerticalAlignment.TOP_ABSO) {
            if (remoteAlign == VerticalAlignment.CENTER_ABSO) {
                return localY - displaySize / 2;
            } else if (localAlign == VerticalAlignment.BOTTOM_ABSO) {
                return localY - displaySize;
            }
        } else if (localAlign == VerticalAlignment.CENTER_ABSO) {
            if (remoteAlign == VerticalAlignment.TOP_ABSO) {
                return localY + displaySize / 2;
            } else if (remoteAlign == VerticalAlignment.BOTTOM_ABSO) {
                return localY - displaySize / 2;
            }
        } else if (localAlign == VerticalAlignment.BOTTOM_ABSO) {
            if (remoteAlign == VerticalAlignment.TOP_ABSO) {
                return localY + displaySize;
            } else if (localAlign == VerticalAlignment.CENTER_ABSO) {
                return localY + displaySize / 2;
            }
        }
        throw new IllegalArgumentException("Invalid Horizontal Alignment scenario");
    }

    @Override
    public ActionResult keyTyped(char eventCharacter, int eventKey) {
        return ActionResult.NOACTION;
    }

    @Override
    public void saveCustomData(JsonObject jsonObject) {
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
    }
}
