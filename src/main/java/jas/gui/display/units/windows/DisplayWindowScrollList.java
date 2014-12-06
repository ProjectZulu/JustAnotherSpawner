package jas.gui.display.units.windows;

import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnitSettable;
import jas.gui.display.units.windows.DisplayWindowSlider.Sliden;
import jas.gui.display.units.windows.button.CloseClick;
import jas.gui.utilities.Coord;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Optional;

/**
 * Vertical scrolling list that handles placement of a list of renderable DisplayUnits based on their sizes
 * 
 * List displays positions are set based on the scroll percentage. Their alignment is also set to LEFT and TOP
 */
public class DisplayWindowScrollList<T> extends DisplayWindow implements Sliden {
    public static final String DISPLAY_ID = "DisplayWindowMenu";
    private static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");

    private HorizontalAlignment horizAlign;
    private VerticalAlignment vertAlign;

    private int headerSize = 20;
    private int scrollLength;
    private DisplayWindowSlider slider;
    private int scrolledDistance;
    private Coord size;
    private Scrollable<T> scrollable;

    @Override
    public void setScrollDistance(int scrollDistance, int scrollLength) {
        this.scrolledDistance = scrollDistance;
    }

    // TODO: add/remove/up/down should in a subinterface as they are not needed in the base implementation
    public static interface Scrollable<T> {
        /**
         * Return list of elements to be scrolled, additions/subtractions should be made to list directly as changes are
         * not gauranteed to write through: {@link#addElement} and {@link#removeElement} should be used instead.
         */
        public abstract Collection<? extends ScrollableElement<T>> getElements();

        public abstract boolean addElement(ScrollableElement<T> element);

        public abstract boolean removeElement(ScrollableElement<T> element);

        public abstract void setSelected(ScrollableElement<T> element);
                
        public abstract void moveElement(ScrollableElement<T> element, int unitstoMove);

        public abstract Optional<ScrollableElement<T>> getSelected();
    }

    public static interface ScrollableElement<T> extends DisplayUnitSettable {
        public abstract void setScrollVisibity(boolean visibility);

        public abstract boolean isVisibleInScroll();

        /**
         * Be able to convert form ScrollableElement to T for removing from Scrollable wrapper to backing list of T
         * 
         * getSource may return the current isntance, say if T implements ScrollableElement.
         */
        public abstract T getSource();
    }

    public DisplayWindowScrollList(Coord offset, Coord size, int headerSize, VerticalAlignment vertAlign,
            HorizontalAlignment horizAlign, Scrollable<T> scrollable) {
        super(offset);
        this.horizAlign = horizAlign;
        this.vertAlign = vertAlign;
        this.size = size;
        this.headerSize = Math.abs(headerSize);
        int sliderWidth = 15;
        this.scrollLength = Math.abs(size.z - (this.headerSize * 2)) - sliderWidth;
        this.slider = new DisplayWindowSlider(new Coord(0, this.headerSize), new Coord(sliderWidth, sliderWidth),
                this.scrollLength, true, VerticalAlignment.TOP_ABSO, HorizontalAlignment.RIGHT_ABSO, this);
        this.scrollable = scrollable;
    }

    @Override
    public String getSubType() {
        return DISPLAY_ID;
    }

    @Override
    public Coord getSize() {
        return size;
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return vertAlign;
    }

    @Override
    public HorizontalAlignment getHorizontalAlignment() {
        return horizAlign;
    }

    @Override
    public void onUpdate(Minecraft mc, int ticks) {
        slider.onUpdate(mc, ticks);
        int listSize = 0;
        Collection<? extends ScrollableElement<T>> scrollDisplays = scrollable.getElements();
        final int maxWidth = getSize().x - slider.getSize().x;
        {
            int listWidth = 0;
            Coord previSize = new Coord(0, 0);
            Iterator<? extends ScrollableElement<T>> iterator = scrollDisplays.iterator();
            while (iterator.hasNext()) {
                DisplayUnit element = iterator.next();
                if (listWidth + previSize.x + element.getSize().x < maxWidth) {
                    listWidth += previSize.x;
                } else {
                    listWidth = 0;
                    listSize += previSize.z;
                }

                if (!iterator.hasNext()) {
                    listWidth += element.getSize().x;
                    listSize += element.getSize().z;
                }
                previSize = element.getSize();
            }
        }
        listSize = listSize - (getSize().z - headerSize * 2);
        int elementPosX = 0;
        int elementPosY = 0;
        Coord prevSize = new Coord(0, 0);
        for (DisplayUnitSettable element : scrollDisplays) {
            element.setHorizontalAlignment(HorizontalAlignment.LEFT_ABSO);
            element.setVerticalAlignment(VerticalAlignment.TOP_ABSO);
            if (elementPosX + prevSize.x + element.getSize().x < maxWidth) {
                elementPosX += prevSize.x;
            } else {
                elementPosX = 0;
                elementPosY += prevSize.z;
            }
            /*
             * Do not include bottom of list; when scroll bar reached bottom, the bottom element should be at bottom of
             * screen not top
             */
            float scrollPerc = scrolledDistance * 1f / scrollLength;
            int zCoord = headerSize + elementPosY - (int) (scrollPerc * listSize);
            element.setOffset(new Coord(elementPosX, zCoord));

            prevSize = element.getSize();
        }
        for (DisplayUnitSettable element : scrollDisplays) {
            element.onUpdate(mc, ticks);
        }

        super.onUpdate(mc, ticks);
    }

    @Override
    public void renderSubDisplay(Minecraft mc, Coord position) {
        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(guiButton);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -5.0f, position, getSize(),
                new Coord(000, 128), new Coord(127, 127));

        Optional<ScrollableElement<T>> selectedEntry = scrollable.getSelected();
        if (selectedEntry.isPresent()) {
            int index = 0;
            for (ScrollableElement<T> element : scrollable.getElements()) {
                if (element.isVisibleInScroll()) {
                    if (selectedEntry.get().equals(element)) {
                        Coord elemPos = DisplayHelper.determineScreenPositionFromDisplay(mc, position, getSize(),
                                element);
                        DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -5.0f, elemPos,
                                element.getSize(), new Coord(129, 129), new Coord(127, 127));
                    }
                    index++;
                }
            }
        }
        for (ScrollableElement<T> element : scrollable.getElements()) {
            Coord elemPos = DisplayHelper.determineScreenPositionFromDisplay(mc, position, getSize(), element);
            // Should scroll visibility logic be done during rendering? Can it be done without absolute position?
            if (elemPos.z > position.z && elemPos.z < position.z + getSize().z - element.getSize().z) {
                element.setScrollVisibity(true);
                element.renderDisplay(mc, elemPos);
            } else {
                element.setScrollVisibity(false);
            }
        }

        mc.getTextureManager().bindTexture(guiButton);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -5.0f, position, new Coord(getSize().x,
                headerSize), new Coord(0, 0), new Coord(127, 127));
        DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -5.0f,
                position.add(0, getSize().z + 1 - headerSize), new Coord(getSize().x, headerSize), new Coord(0, 0),
                new Coord(127, 127));

        slider.renderDisplay(mc, DisplayHelper.determineScreenPositionFromDisplay(mc, position, getSize(), slider));
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker hoverChecker) {
        // TODO: Why is Slider not an element?
        {
            Coord childCoords = DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, slider);
            HoverAction childAction = HoverAction.OUTSIDE;
            if (DisplayHelper.isCursorOverDisplay(childCoords, slider)) {
                childAction = !hoverChecker.isHoverFound() ? HoverAction.HOVER : HoverAction.BLOCKED;
            }
            slider.mousePosition(childCoords, childAction, hoverChecker);
        }

        for (ScrollableElement<T> element : scrollable.getElements()) {
            if (element.isVisibleInScroll()) {
                Coord childCoords = DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this,
                        element);
                HoverAction childAction = HoverAction.OUTSIDE;
                if (DisplayHelper.isCursorOverDisplay(childCoords, element)) {
                    childAction = !hoverChecker.isHoverFound() ? HoverAction.HOVER : HoverAction.BLOCKED;
                }
                element.mousePosition(childCoords, childAction, hoverChecker);
            }
        }
        super.mousePosition(localMouse, hoverAction, hoverChecker);
    }

    @Override
    public ActionResult subMouseAction(Coord localMouse, MouseAction action, int... actionData) {
        {
            ActionResult result = slider.mouseAction(
                    DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, slider), action,
                    actionData);
            if (result.shouldStop()) {
                return result;
            }
        }

        Iterator<? extends ScrollableElement<T>> iterator = scrollable.getElements().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            ScrollableElement<T> element = iterator.next();
            if (element.isVisibleInScroll()) {
                ActionResult result = element.mouseAction(
                        DisplayHelper.localizeMouseCoords(Minecraft.getMinecraft(), localMouse, this, element), action,
                        actionData);
                Optional<ScrollableElement<T>> selectedEntry = scrollable.getSelected();
                if (selectedEntry.isPresent() && selectedEntry.get().equals(element)) {
                    if (result.shouldStop()) {
                        return result;
                    }
                } else {
                    if (result.shouldStop()) {
                        scrollable.setSelected(element);
                        return ActionResult.SIMPLEACTION;
                    }
                }
                index++;
            }
        }
        return ActionResult.NOACTION;
    }

    @Override
    public ActionResult subKeyTyped(char eventCharacter, int eventKey) {
        {
            ActionResult result = slider.keyTyped(eventCharacter, eventKey);
            if (result.shouldStop()) {
                return result;
            }
        }

        for (ScrollableElement<T> element : scrollable.getElements()) {
            if (element.isVisibleInScroll()) {
                ActionResult result = element.keyTyped(eventCharacter, eventKey);
                if (result.shouldStop()) {
                    return result;
                }
            }
        }
        return ActionResult.NOACTION;
    }
}
