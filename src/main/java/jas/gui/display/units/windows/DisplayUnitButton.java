package jas.gui.display.units.windows;

import jas.gui.GuiLog;
import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.resource.ImageResource;
import jas.gui.display.resource.SimpleImageResource.GuiButtonImageResource;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.ActionResult;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.DisplayUnit.HoverAction;
import jas.gui.display.units.DisplayUnit.HoverTracker;
import jas.gui.display.units.DisplayUnit.MouseAction;
import jas.gui.display.units.DisplayUnit.ActionResult.SimpleAction;
import jas.gui.utilities.Coord;

import javax.swing.GroupLayout.Alignment;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;


/**
 * Interactive display which performs an action when CLICK is RELEASE.
 */
// TODO: Not finished. Still need clicked interface for command pattern
public class DisplayUnitButton implements DisplayUnit {
    public static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");
    public static final ResourceLocation guiIcons = new ResourceLocation("mosi", "icons.png");

    public static final String DISPLAY_ID = "DisplayUnitButton";
    private Coord offset;
    private Coord size;
    private boolean isClicked;
    private boolean isMouseOver;
    private Clicker clicker;

    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;

    private Optional<? extends ImageResource> iconImage;
    private Optional<String> displayText;

    private ImageResource mouseOverImage;
    private ImageResource downImage;
    private ImageResource upImage;

    /**
     * Clicker interface for handling logic. Note that release is only performed if mouse is still within button bounds
     * and thus onRelease() is not guaranteed for every onClick()
     */
    public static interface Clicker {
        public abstract ActionResult onClick();

        /** Perform action on release ONLY IF mouse is still over button */
        public abstract ActionResult onRelease();
    }

    public DisplayUnitButton(Coord offset, Coord size, VerticalAlignment vertAlign, HorizontalAlignment horizAlign,
            Clicker clicker) {
        this(offset, size, vertAlign, horizAlign, clicker, null);
    }

    public DisplayUnitButton(Coord offset, Coord size, VerticalAlignment vertAlign, HorizontalAlignment horizAlign,
            Clicker clicker, String displayText) {
        this.offset = offset;
        this.size = size;
        this.vertAlign = vertAlign;
        this.horizAlign = horizAlign;
        this.clicker = clicker;
        this.displayText = displayText != null ? Optional.of(displayText) : Optional.<String> absent();
        iconImage = Optional.absent();
        setDefaultImageResources();
    }

    private final void setDefaultImageResources() {
        downImage = new GuiButtonImageResource(new Coord(129, 129), new Coord(127, 127));
        mouseOverImage = new GuiButtonImageResource(new Coord(000, 000), new Coord(127, 127));
        upImage = new GuiButtonImageResource(new Coord(129, 000), new Coord(127, 127));
    }

    public DisplayUnitButton setIconImageResource(ImageResource resource) {
        iconImage = Optional.of(resource);
        return this;
    }

    public DisplayUnitButton setDownImage(ImageResource resource) {
        downImage = resource;
        return this;
    }

    public DisplayUnitButton setMouseOverImage(ImageResource resource) {
        mouseOverImage = resource;
        return this;
    }

    public DisplayUnitButton setUpImage(ImageResource resource) {
        upImage = resource;
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
    public HorizontalAlignment getHorizontalAlignment() {
        return horizAlign;
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return vertAlign;
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
        FontRenderer fontRenderer = mc.fontRenderer;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        /* Background */
        if (isClicked) {
            mc.getTextureManager().bindTexture(downImage.getImageToBind());
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                    downImage.getImageUV(), downImage.getImageSize());
        } else if (isMouseOver) {
            mc.getTextureManager().bindTexture(mouseOverImage.getImageToBind());
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                    mouseOverImage.getImageUV(), mouseOverImage.getImageSize());
        } else {
            mc.getTextureManager().bindTexture(upImage.getImageToBind());
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -0.1f, position, getSize(),
                    upImage.getImageUV(), upImage.getImageSize());
        }

        /* GUI Image */
        if (iconImage.isPresent()) {
            mc.getTextureManager().bindTexture(iconImage.get().getImageToBind());
            Coord imageSize = iconImage.get().getImageSize();
            Coord iamgeUV = iconImage.get().getImageUV();
            DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, 1.0f, new Coord(position.x + getSize().x
                    / 2 - imageSize.x / 2, position.z + getSize().z / 2 - imageSize.z / 2), iamgeUV, imageSize);
        }

        if (displayText.isPresent()) {
            String shortName = (String) fontRenderer.listFormattedStringToWidth(displayText.get(), getSize().x).get(0);
            // Note posZ-4+getSize/2. -4 is to 'center' the string vertically, and getSize/2 is to move center to the
            // middle button
            DisplayRenderHelper.drawCenteredString(fontRenderer, shortName, position.x + 1 + getSize().x / 2,
                    position.z - 4 + getSize().z / 2, 16777120, true);
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
                isClicked = true;
                return clicker.onClick();
            }
            break;
        case CLICK_MOVE:
            break;
        case RELEASE:
            if (isClicked && DisplayHelper.isCursorOverDisplay(localMouse, this)) {
                isClicked = false;
                return clicker.onRelease();
            } else {
                isClicked = false;
                return ActionResult.NOACTION;
            }
        case SCROLL:
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
