package jas.gui.display.units.windows;

import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.resource.ImageResource;
import jas.gui.display.resource.SimpleImageResource.GuiButtonImageResource;
import jas.gui.display.units.DisplayUnit;
import jas.gui.utilities.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Optional;

/**
 * Simple menu implementation: plain background screens which automatically resizes to fit its elements;
 * 
 * TODO: AutoSizign has issues with different Alignments, I think it onyl works with TOP and LEFT
 */
public class DisplayWindowMenu extends DisplayWindow {
    public static final String DISPLAY_ID = "DisplayWindowMenu";
    private static final ResourceLocation widgets = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");

    private HorizontalAlignment horizAlign;
    private VerticalAlignment vertAlign;
    private Coord size;
    private Optional<? extends ImageResource> backgroundImage;
    private Optional<Coord> forceSize;

    public DisplayWindowMenu(Coord coord, HorizontalAlignment horizAlign, VerticalAlignment vertAlign) {
        super(coord);
        this.horizAlign = horizAlign;
        this.vertAlign = vertAlign;
        this.size = new Coord(20, 20);
        this.forceSize = Optional.<Coord> absent();
        setDefaultImageResource();
    }

    public DisplayWindowMenu forceSize(Coord forceSize) {
        this.forceSize = Optional.of(forceSize);
        return this;
    }

    public DisplayWindowMenu setBackgroundImage(ImageResource backgroundImage) {
        this.backgroundImage = backgroundImage != null ? Optional.of(backgroundImage) : Optional
                .<ImageResource> absent();
        return this;
    }

    private final void setDefaultImageResource() {
        backgroundImage = Optional.of(new GuiButtonImageResource(new Coord(129, 000), new Coord(127, 127)));
    }

    @Override
    public boolean addElement(DisplayUnit element) {
        if (super.addElement(element)) {
            size = calculateSize();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeElement(DisplayUnit window) {
        if (super.removeElement(window)) {
            size = calculateSize();
            return true;
        } else {
            return false;
        }
    }

    private final Coord calculateSize() {
        if (forceSize.isPresent()) {
            return forceSize.get();
        }
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        for (DisplayUnit display : elements) {
            Coord offset = display.getOffset();
            Coord size = display.getSize();
            minX = Math.min(offset.x, minX);
            minX = Math.min(offset.x + size.x, minX);
            maxX = Math.max(offset.x, maxX);
            maxX = Math.max(offset.x + size.x, maxX);
            minY = Math.min(offset.z, minY);
            minY = Math.min(offset.z + size.z, minY);
            maxY = Math.max(offset.z, maxY);
            maxY = Math.max(offset.z + size.z, maxY);
        }
        return new Coord(maxX - minX + 6, maxY - minY + 6);
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
    public HorizontalAlignment getHorizontalAlignment() {
        return horizAlign;
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return vertAlign;
    }

    @Override
    public void renderSubDisplay(Minecraft mc, Coord position) {
        if (backgroundImage.isPresent()) {
            FontRenderer fontrenderer = mc.fontRenderer;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            mc.getTextureManager().bindTexture(backgroundImage.get().getImageToBind()); // widgets
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -10.0f, position, getSize(),
                    backgroundImage.get().getImageUV(), backgroundImage.get().getImageSize());
        }
    }

    @Override
    public ActionResult subMouseAction(Coord localMouse, MouseAction action, int... actionData) {
        return ActionResult.NOACTION;
    }

    @Override
    public ActionResult subKeyTyped(char eventCharacter, int eventKey) {
        return ActionResult.NOACTION;
    }
}
