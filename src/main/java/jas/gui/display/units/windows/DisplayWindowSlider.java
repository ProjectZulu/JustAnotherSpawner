package jas.gui.display.units.windows;

import jas.gui.GuiLog;
import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.ActionResult;
import jas.gui.display.units.DisplayUnit.HoverAction;
import jas.gui.display.units.DisplayUnit.HoverTracker;
import jas.gui.display.units.DisplayUnit.MouseAction;
import jas.gui.display.units.DisplayUnit.ActionResult.SimpleAction;
import jas.gui.utilities.Coord;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonObject;


public class DisplayWindowSlider implements DisplayUnit {
    public static final String DISPLAY_ID = "DisplayWindowSlider";

    private static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");

    private Coord offset;
    private Coord size;
    private boolean scrollVertically;
    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;
    private float zLevel = -10.0f;
    private boolean isMouseOver;
    // Represents the origin for ScrollHeight
    private Coord scrollOrigin;
    private int scrollHeight;
    private Sliden scrolled;
    protected transient boolean clickedOn = false;
    // Mouse location was on mouseClick. Click + Drag -> Offset = OriginalOffset + (MousePos - mousePosOnClick)
    protected transient Coord mousePosOnClick;
    // Original location were on mouseClick. Click + Drag -> Offset = OriginalOffset + (MousePos - mousePosOnClick)
    protected transient Coord offsetPosOnClick;

    public static interface Sliden {
        /**
         * Scroll Distance is the distance the scroll bar has moved. It is bounded between 0 and scrollLength
         * 
         * This is only called when scrollDistance changes
         */
        public abstract void setScrollDistance(int scrollDistance, int scrollLength);
    }

    public DisplayWindowSlider(Coord offset, Coord size, int scrollHeight, boolean scrollVertically,
            VerticalAlignment vertAlign, HorizontalAlignment horizAlign, Sliden scrolled) {
        this.offset = offset;
        this.scrollOrigin = offset;
        this.size = size;
        this.scrollHeight = scrollHeight;
        this.scrollVertically = scrollVertically;
        this.vertAlign = vertAlign;
        this.horizAlign = horizAlign;
        this.scrolled = scrolled;
        this.mousePosOnClick = offset;
        this.offsetPosOnClick = offset;

    }

    public DisplayWindowSlider setZLevel(float zLevel) {
        this.zLevel = zLevel;
        return this;
    }

    @Override
    public String getType() {
        return DISPLAY_ID;
    }

    @Override
    public Coord getOffset() {
        return offset;
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
        if (ticks % 80 == 0) {
            if (ensureScrollBounded()) {
                scrolled.setScrollDistance(getScrollDistance(), scrollHeight);
            }
        }
    }

    private int getScrollDistance() {
        if (scrollVertically) {
            return offset.z - scrollOrigin.z;
        } else {
            return offset.x - scrollOrigin.x;
        }
    }

    private boolean ensureScrollBounded() {
        if (scrollVertically) {
            int minCoord = Math.min(scrollOrigin.z, scrollOrigin.z + scrollHeight);
            int maxCoord = Math.max(scrollOrigin.z, scrollOrigin.z + scrollHeight);
            if (getOffset().z < minCoord) {
                offset = new Coord(getOffset().x, minCoord);
                return true;
            } else if (getOffset().z > maxCoord) {
                offset = new Coord(getOffset().x, maxCoord);
                return true;
            }
            return false;
        } else {
            int minCoord = Math.min(scrollOrigin.x, scrollOrigin.x + scrollHeight);
            int maxCoord = Math.max(scrollOrigin.x, scrollOrigin.x + scrollHeight);
            if (getOffset().x < minCoord) {
                offset = new Coord(minCoord, getOffset().z);
                return true;
            } else if (getOffset().x > maxCoord) {
                offset = new Coord(maxCoord, getOffset().z);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return true;
    }

    @Override
    public void renderDisplay(Minecraft mc, Coord position) {
        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(guiButton);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (isMouseOver) {
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, zLevel, position, getSize(), new Coord(000,
                    000), new Coord(127, 127));
        } else {
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, zLevel, position, getSize(), new Coord(000,
                    000), new Coord(127, 127));
        }
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker hoverChecker) {
        if (!hoverChecker.isHoverFound() && hoverAction == HoverAction.HOVER) {
            isMouseOver = true;
            hoverChecker.markHoverFound();
        } else {
            isMouseOver = false;
        }
    }

    @Override
    public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
        switch (action) {
        case CLICK:
            // actionData[0] == EventButton, 0 == Left-Click, 1 == Right-Click
            if (actionData[0] == 0 && DisplayHelper.isCursorOverDisplay(localMouse, this)) {
                clickedOn = true;
                mousePosOnClick = localMouse;
                offsetPosOnClick = offset;
                return ActionResult.SIMPLEACTION;
            }
            break;
        case CLICK_MOVE:
            if (clickedOn) {
                if (scrollVertically) {
                    offset = offsetPosOnClick.add(0, localMouse.z - mousePosOnClick.z);
                } else {
                    offset = offsetPosOnClick.add(localMouse.x - mousePosOnClick.x, 0);
                }
                ensureScrollBounded();
                scrolled.setScrollDistance(getScrollDistance(), scrollHeight);
                return ActionResult.SIMPLEACTION;
            }
            break;
        case RELEASE:
            clickedOn = false;
            return ActionResult.NOACTION;
        case SCROLL:
            if (!clickedOn) {
                int scrollFactor = actionData[0] / 120;
                if (scrollVertically) {
                    offset = offset.add(0, scrollHeight * scrollFactor / 20);
                } else {
                    offset = offsetPosOnClick.add(scrollHeight * scrollFactor / 20, 0);
                }
                ensureScrollBounded();
                scrolled.setScrollDistance(getScrollDistance(), scrollHeight);
                return ActionResult.SIMPLEACTION;
            }
            break;
        }
        return ActionResult.NOACTION;
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
