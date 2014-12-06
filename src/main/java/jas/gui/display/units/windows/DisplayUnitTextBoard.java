package jas.gui.display.units.windows;

import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.resource.ImageResource;
import jas.gui.display.resource.SimpleImageResource.GuiButtonImageResource;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.HoverAction;
import jas.gui.display.units.DisplayUnit.HoverTracker;
import jas.gui.display.units.DisplayUnit.ActionResult.SimpleAction;
import jas.gui.utilities.Coord;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import scala.Array;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;


/**
 * Simple Text board which resizes to fit provided text
 */
public class DisplayUnitTextBoard implements DisplayUnit {
    public static final String DISPLAY_ID = "DisplayUnitTextField";
    private Coord offset;
    private Coord size;
    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;
    private Optional<ImageResource> backgroundImage;
    private ArrayList<String> displayText;

    public DisplayUnitTextBoard(Coord offset, VerticalAlignment vertAlign, HorizontalAlignment horizAlign,
            String... displayText) {
        this.offset = offset;
        this.vertAlign = vertAlign;
        this.horizAlign = horizAlign;
        this.displayText = new ArrayList<String>(displayText.length + 2);
        final int maxLineLength = 249; // Larger strings causes background issues withdrawing pattern, should be
                                       // configurable
        for (String displayLine : displayText) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            for (Object nonGenericString : fontRenderer.listFormattedStringToWidth(displayLine, maxLineLength)) {
                this.displayText.add((String) nonGenericString);
            }
        }
        this.size = calculateSize();
        setDefaultImageResource();
    }

    private final void setDefaultImageResource() {
        backgroundImage = Optional.<ImageResource> of(new GuiButtonImageResource(new Coord(129, 000), new Coord(127,
                127)));
    }

    public final DisplayUnitTextBoard setBackgroundImage(ImageResource backgrounImage) {
        backgroundImage = backgrounImage != null ? Optional.of(backgrounImage) : Optional.<ImageResource> absent();
        return this;
    }

    private final Coord calculateSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int widestLine = 10; // Minimum width
        for (String displayLine : displayText) {
            int width = fontRenderer.getStringWidth(displayLine);
            if (width > widestLine) {
                widestLine = width;
            }
        }
        // length + 1 To allow for some clearance room
        return new Coord(widestLine + 10, fontRenderer.FONT_HEIGHT * (displayText.size() + 1));
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
        this.size = calculateSize();
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

        for (int i = 0; i < displayText.size(); i++) {
            String displayLine = displayText.get(i);
            DisplayRenderHelper.drawString(fontRenderer, displayLine, position.x + 5, position.z
                    + fontRenderer.FONT_HEIGHT / 2 + i * fontRenderer.FONT_HEIGHT, 16777120, true);
        }
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker hoverChecker) {
        if (hoverAction == HoverAction.HOVER) {
            hoverChecker.markHoverFound();
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
