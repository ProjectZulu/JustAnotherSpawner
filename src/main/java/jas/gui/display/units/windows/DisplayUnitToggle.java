package jas.gui.display.units.windows;

import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.resource.DualImageResource;
import jas.gui.display.resource.DualSimpleImageResource;
import jas.gui.display.resource.ImageResource;
import jas.gui.display.resource.SimpleImageResource;
import jas.gui.display.resource.SimpleImageResource.GuiButtonImageResource;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.HoverAction;
import jas.gui.display.units.DisplayUnit.HoverTracker;
import jas.gui.display.units.DisplayUnit.ActionResult.SimpleAction;
import jas.gui.utilities.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;

public class DisplayUnitToggle implements DisplayUnit {
    public static final String DISPLAY_ID = "DisplayUnitToggle";

    private Coord offset;
    private Coord size;
    private boolean isClicked;
    private boolean isMouseOver;

    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;
    private Toggle toggle;

    private Optional<? extends ImageResource> iconImage;
    private Optional<String> displayText;

    // Seconary Image is MouseOver
    private DualImageResource toggledImages;
    private DualImageResource defaultImages;

    public static interface Toggle {
        /* Toggles the internal value */
        public abstract void toggle();

        /* Checks if the value is toggled, used for rendering */
        public abstract boolean isToggled();
    }

    public DisplayUnitToggle(Coord offset, Coord size, VerticalAlignment vertAlign, HorizontalAlignment horzAlign,
            Toggle toggle) {
        this(offset, size, vertAlign, horzAlign, toggle, Optional.<String> absent());
    }

    public DisplayUnitToggle(Coord offset, Coord size, VerticalAlignment vertAlign, HorizontalAlignment horzAlign,
            Toggle toggle, Optional<String> displayText) {
        this.offset = offset;
        this.size = size;
        this.vertAlign = vertAlign;
        this.horizAlign = horzAlign;
        this.toggle = toggle;
        this.displayText = displayText;
        this.iconImage = Optional.absent();
        this.setDefaultImageResource();
    }

    private final void setDefaultImageResource() {
        toggledImages = new DualSimpleImageResource(
                new GuiButtonImageResource(new Coord(129, 129), new Coord(127, 127)));
        defaultImages = new DualSimpleImageResource(
                new GuiButtonImageResource(new Coord(129, 000), new Coord(127, 127)), new GuiButtonImageResource(
                        new Coord(000, 000), new Coord(127, 127)));
    }

    public final DisplayUnitToggle setIconImageResource(ImageResource resource) {
        iconImage = Optional.of(resource);
        return this;
    }

    public final DisplayUnitToggle setToggledImages(DualImageResource resource) {
        toggledImages = resource;
        return this;
    }

    public final DisplayUnitToggle setDefaultImages(DualImageResource resource) {
        defaultImages = resource;
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

        // DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, 1.0f, position.x + 3, position.z + 2, 111, 2,
        // 12, 16);

        /* Background */
        if (toggle.isToggled()) {
            if (isMouseOver) {
                mc.getTextureManager().bindTexture(toggledImages.getSecondaryToBind());
                DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                        toggledImages.getSecondaryUV(), toggledImages.getSecondarySize());
            } else {
                mc.getTextureManager().bindTexture(toggledImages.getImageToBind());
                DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                        toggledImages.getImageUV(), toggledImages.getImageSize());
            }
        } else {
            if (isMouseOver) {
                mc.getTextureManager().bindTexture(defaultImages.getSecondaryToBind());
                DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                        defaultImages.getSecondaryUV(), defaultImages.getSecondarySize());
            } else {
                mc.getTextureManager().bindTexture(defaultImages.getImageToBind());
                DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                        defaultImages.getImageUV(), defaultImages.getImageSize());
            }
        }

        // TODO: GuiIcons should be a passable parameter
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
        if (action == MouseAction.CLICK && actionData[0] == 0 && DisplayHelper.isCursorOverDisplay(localMouse, this)) {
            toggle.toggle();
            return ActionResult.SIMPLEACTION;
        } else {
            return ActionResult.NOACTION;
        }
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
