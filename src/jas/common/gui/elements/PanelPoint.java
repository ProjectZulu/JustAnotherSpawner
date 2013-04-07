package jas.common.gui.elements;

import org.lwjgl.util.Point;

/**
 * Panel Location and Bounds: (0,0) is TOP-LEFT; Down is +Y; Right is +X
 */
public class PanelPoint {

    /**
     * Center of Panel
     */
    public final Point location;
    
    /**
     * Box Centered at {@link panelLocation} representing bounds to reference for TOP, BOTTOM, LEFT, RIGHT If a
     * Background Image is Present, this is typically equal to that Image Size
     */
    public final Point bounds;

    public PanelPoint(Point location, Point bounds) {
        this.location = location;
        this.bounds = bounds;
    }

    public Point center() {
        return location;
    }

    public int top() {
        return location.getY() - bounds.getY() / 2;
    }

    public int bottom() {
        return location.getY() + bounds.getY() / 2;
    }

    public int left() {
        return location.getX() - bounds.getX() / 2;
    }

    public int right() {
        return location.getX() + bounds.getX() / 2;
    }
}
