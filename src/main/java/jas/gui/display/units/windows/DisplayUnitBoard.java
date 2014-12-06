package jas.gui.display.units.windows;

import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.resource.ImageResource;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.DisplayUnit.VerticalAlignment;
import jas.gui.utilities.Coord;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;


public class DisplayUnitBoard implements DisplayUnit {
    public static final String DISPLAY_ID = "DisplayUnitBoard";
    private Coord offset;
    private Coord size;
    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;
    private Optional<ImageResource> backgroundImage;

    public DisplayUnitBoard(Coord offset, Coord size, VerticalAlignment vertAlign, HorizontalAlignment horizAlign,
            ImageResource backgroundImage) {
        this.offset = offset;
        this.size = size;
        this.vertAlign = vertAlign;
        this.horizAlign = horizAlign;
        setBackgroundImage(backgroundImage);
    }

    public final DisplayUnitBoard setBackgroundImage(ImageResource backgrounImage) {
        backgroundImage = backgrounImage != null ? Optional.of(backgrounImage) : Optional.<ImageResource> absent();
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
        if (backgroundImage.isPresent()) {
            mc.getTextureManager().bindTexture(backgroundImage.get().getImageToBind()); // widgets
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -10.0f, position, getSize(),
                    backgroundImage.get().getImageUV(), backgroundImage.get().getImageSize());
        }
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker alreadyHovering) {
        if (hoverAction == HoverAction.HOVER) {
            alreadyHovering.markHoverFound();
        }
    }

    @Override
    public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
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
