package jas.gui.display;

import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.DisplayUnit.VerticalAlignment;
import jas.gui.utilities.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Helper for non-rendering display tasks. Contains custom written tasks as well as wrappers around vanilla obfuscated
 * names to centralize changes
 */
public class DisplayHelper {
    /**
     * Mouse coordinates are assumed to share the same origin as the display offset such that this is a simple bounds
     * check with limits dependent on if the box is centered/left/right aligned
     * 
     * @param mousePos
     * @param display
     * @return
     */
    public static boolean isCursorOverDisplay(Coord mousePos, DisplayUnit display) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolition = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        Coord displaySizePerc = display.getSize().multf(100f)
                .divf(scaledResolition.getScaledWidth(), scaledResolition.getScaledHeight());
        if (!isWithinXBounds(display.getHorizontalAlignment(), mousePos.x, display.getOffset().x, display.getSize().x,
                displaySizePerc.x)) {
            return false;
        }

        if (!isWithinYBounds(display.getVerticalAlignment(), mousePos.z, display.getOffset().z, display.getSize().z,
                displaySizePerc.z)) {
            return false;
        }

        return true;
    }

    private static boolean isWithinXBounds(HorizontalAlignment alignment, int mousePos, int dispOffset, int dispSize,
            int dispSizePerc) {
        switch (alignment) {
        case LEFT_ABSO:
            return isWithinBounds(mousePos, dispOffset, dispOffset + dispSize);
        case CENTER_ABSO:
            return isWithinBounds(mousePos, dispOffset - dispSize / 2, dispOffset + dispSize / 2);
        case RIGHT_ABSO:
            return isWithinBounds(mousePos, dispOffset - dispSize, dispOffset);
        }
        throw new IllegalArgumentException("This should not happen, alignment invalid case " + alignment.toString());
    }

    private static boolean isWithinYBounds(VerticalAlignment alignment, int mousePos, int dispOffset, int dispSize,
            int dispSizePerc) {
        switch (alignment) {
        case TOP_ABSO:
            return isWithinBounds(mousePos, dispOffset, dispOffset + dispSize);
        case CENTER_ABSO:
            return isWithinBounds(mousePos, dispOffset - dispSize / 2, dispOffset + dispSize / 2);
        case BOTTOM_ABSO:
            return isWithinBounds(mousePos, dispOffset - dispSize, dispOffset);
        }
        throw new IllegalArgumentException("This should not happen, alignment invalid case " + alignment.toString());
    }

    private static boolean isWithinBounds(int value, int min, int max) {
        return value >= min && value <= max;
    }

    @Deprecated
    public static Coord determineScreenPositionFromDisplay(Minecraft mc, DisplayUnit display) {
        ScaledResolution scaledResolition = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return determineScreenPositionFromDisplay(mc, new Coord(0, 0), new Coord(scaledResolition.getScaledWidth(),
                scaledResolition.getScaledHeight()), display);
    }

    public static Coord determineScreenPositionFromDisplay(Minecraft mc, Coord originPos, Coord originSize,
            DisplayUnit display) {
        ScaledResolution scaledResolition = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        Coord displaySize = display.getSize();
        Coord displayOffset = display.getOffset();
        int horzCoord = horizontalDisplayToScreenCoord(display.getHorizontalAlignment(), scaledResolition, originPos,
                originSize, displayOffset, displaySize);
        int vertCoord = verticalDisplayToScreenCoord(display.getVerticalAlignment(), scaledResolition, originPos,
                originSize, displayOffset, displaySize);
        return new Coord(horzCoord, vertCoord);
    }

    private static int horizontalDisplayToScreenCoord(HorizontalAlignment vertAlign, ScaledResolution resolution,
            Coord originPos, Coord originSize, Coord displayOffset, Coord displaySize) {
        // Reminder: displayOffset.x is a % when doing any of the _PERC alignment situations
        int percOffset = (int) (originSize.x * displayOffset.x / 100f);
        switch (vertAlign) {
        default:
        case LEFT_ABSO:
            return originPos.x + displayOffset.x;
        case CENTER_ABSO:
            return originPos.x + (originSize.x / 2 - displaySize.x / 2) + displayOffset.x;
        case RIGHT_ABSO:
            return originPos.x + (originSize.x - displaySize.x) + displayOffset.x;
        }
    }

    private static int verticalDisplayToScreenCoord(VerticalAlignment vertAlign, ScaledResolution resolution,
            Coord originPos, Coord originSize, Coord displayOffset, Coord displaySize) {
        // Reminder: displayOffset.z is a % when doing any of the _PERC alignment situations
        int percOffset = (int) (originSize.z * displayOffset.z / 100f);
        switch (vertAlign) {
        default:
        case TOP_ABSO:
            return originPos.z + displayOffset.z;
        case CENTER_ABSO:
            return originPos.z + (originSize.z / 2 - displaySize.z / 2) + displayOffset.z;
        case BOTTOM_ABSO:
            return originPos.z + (originSize.z - displaySize.z) + displayOffset.z;
        }
    }

    /**
     * Conceptual opposite of DetermineScreenPositionFromDisplay. This takes the ScreenPosition of an Object (mouse
     * Coordiantes) and converts into the local coordinates of the display (to be used for ClickEvents, Hovering, etc.)
     * 
     * Note: This is for the base Display which is the minecraft guiscreen which is assumed to be top-left aligned with
     * {0,0} offset. See overloaded version for converting between two nested DisplayUnits.
     */
    public static Coord localizeMouseCoords(Minecraft mc, int mouseScaledX, int mouseScaledY, DisplayUnit display) {
        ScaledResolution scaledResolition = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        Coord scaledMouse = new Coord(mouseScaledX, mouseScaledY);
        Coord originPos = new Coord(0, 0);
        Coord originSize = new Coord(scaledResolition.getScaledWidth(), scaledResolition.getScaledHeight());

        int horizCoord = getHorizontalCoord(mc, scaledMouse, HorizontalAlignment.LEFT_ABSO, new Coord(0, 0),
                originSize, display.getHorizontalAlignment(), display.getOffset(), display.getSize());
        int vertiCoord = getVerticalCoord(mc, scaledMouse, VerticalAlignment.TOP_ABSO, new Coord(0, 0), originSize,
                display.getVerticalAlignment(), display.getOffset(), display.getSize());
        return new Coord(horizCoord, vertiCoord);
    }

    public static Coord localizeMouseCoords(Minecraft mc, Coord scaledMouse, DisplayUnit parentDisplay,
            DisplayUnit display) {
        ScaledResolution scaledResolition = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        Coord originPos = new Coord(0, 0);
        Coord originSize = new Coord(scaledResolition.getScaledWidth(), scaledResolition.getScaledHeight());

        int horizCoord = getHorizontalCoord(mc, scaledMouse, parentDisplay.getHorizontalAlignment(),
                parentDisplay.getOffset(), parentDisplay.getSize(), display.getHorizontalAlignment(),
                display.getOffset(), display.getSize());
        int vertiCoord = getVerticalCoord(mc, scaledMouse, parentDisplay.getVerticalAlignment(),
                parentDisplay.getOffset(), parentDisplay.getSize(), display.getVerticalAlignment(),
                display.getOffset(), display.getSize());
        return new Coord(horizCoord, vertiCoord);
    }

    private static int getHorizontalCoord(Minecraft mc, Coord originMouse, HorizontalAlignment originAlign,
            Coord originPos, Coord originSize, HorizontalAlignment displayAlign, Coord displayPos, Coord displaySize) {
        /*
         * DisplayOrigin is set dependent on whether it is Center/Left/right justified. The difference between a parent
         * and child origin is a ratio of the parents Size (For example if parent is left and child right, child must be
         * shifted 1/2 parent size).
         * 
         * The below complicated looking switch-if statement is merely all combinations Center/left/Rigth chlid-parent
         * factors.
         */
        switch (originAlign) {
        case LEFT_ABSO: {
            int offsetConstant = displayAlign == HorizontalAlignment.LEFT_ABSO ? 0
                    : displayAlign == HorizontalAlignment.CENTER_ABSO ? 1 : 2;
            return originMouse.x - (originPos.x + originSize.x / 2 * offsetConstant);
        }
        case CENTER_ABSO: {
            int offsetConstant = displayAlign == HorizontalAlignment.LEFT_ABSO ? -1
                    : displayAlign == HorizontalAlignment.CENTER_ABSO ? 0 : +1;
            return originMouse.x - (originPos.x + originSize.x / 2 * offsetConstant);
        }
        case RIGHT_ABSO: {
            int offsetConstant = displayAlign == HorizontalAlignment.LEFT_ABSO ? -2
                    : displayAlign == HorizontalAlignment.CENTER_ABSO ? -1 : 0;
            return originMouse.x - (originPos.x + originSize.x / 2 * offsetConstant);
        }
        }
        throw new IllegalArgumentException("This should not happen, alignment invalid case " + displayAlign.toString());
    }

    private static int getVerticalCoord(Minecraft mc, Coord originMouse, VerticalAlignment originAlign,
            Coord originPos, Coord originSize, VerticalAlignment displayAlign, Coord displayPos, Coord displaySize) {
        /*
         * DisplayOrigin is set dependent on whether it is Center/Top/bottom justified. The difference between a parent
         * and child origin is a ratio of the parents Size (For example if parent is top and child bottom, child must be
         * shifted 1/2 parent size).
         * 
         * The below complicated looking switch-if statement is merely all combinations Center/Top/Rigth chlid-parent
         * factors.W
         */
        switch (originAlign) {
        case TOP_ABSO: {
            if (displayAlign == VerticalAlignment.TOP_ABSO) {
                return originMouse.z - (originPos.z + originSize.z / 2 * 0);
            } else if (displayAlign == VerticalAlignment.CENTER_ABSO) {
                return originMouse.z - (originPos.z + originSize.z / 2 * 1);
            } else {
                return originMouse.z - (originPos.z + originSize.z / 2 * 2);
            }
        }
        case CENTER_ABSO: {
            int offsetConstant = displayAlign == VerticalAlignment.TOP_ABSO ? -1
                    : displayAlign == VerticalAlignment.CENTER_ABSO ? 0 : +1;
            return originMouse.z - (originPos.z + originSize.z / 2 * offsetConstant);
        }
        case BOTTOM_ABSO: {
            int offsetConstant = displayAlign == VerticalAlignment.TOP_ABSO ? -2
                    : displayAlign == VerticalAlignment.CENTER_ABSO ? -1 : 0;
            return originMouse.z - (originPos.z + originSize.z / 2 * offsetConstant);
        }
        }
        throw new IllegalArgumentException("This should not happen, alignment invalid case " + displayAlign.toString());
    }

    public static boolean isCtrlKeyDown() {
        return GuiScreen.isCtrlKeyDown();
    }

    public static boolean isShiftKeyDown() {
        return GuiScreen.isShiftKeyDown();
    }

    public static void setClipboardString(String string) {
        GuiScreen.setClipboardString(string);
    }

    public static String getClipboardString() {
        return GuiScreen.getClipboardString();
    }
}
