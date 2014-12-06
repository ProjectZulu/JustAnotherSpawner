package jas.gui.display.resource;

import jas.gui.utilities.Coord;
import net.minecraft.util.ResourceLocation;

public class SimpleImageResource implements ImageResource {
    public static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");
    public static final ResourceLocation guiIcons = new ResourceLocation("mosi", "icons.png");

    private final ResourceLocation resourceLocation;
    private final Coord imageUV;
    private final Coord imageSize;

    public SimpleImageResource(ResourceLocation resourceLocation, Coord imageUV, Coord imageSize) {
        this.resourceLocation = resourceLocation;
        this.imageUV = imageUV;
        this.imageSize = imageSize;
    }

    @Override
    public ResourceLocation getImageToBind() {
        return resourceLocation;
    }

    @Override
    public Coord getImageUV() {
        return imageUV;
    }

    @Override
    public Coord getImageSize() {
        return imageSize;
    }

    public static class GuiButtonImageResource extends SimpleImageResource {
        public GuiButtonImageResource(Coord imageUV, Coord imageSize) {
            super(guiButton, imageUV, imageSize);
        }
    }

    public static class GuiIconImageResource extends SimpleImageResource {
        public GuiIconImageResource(Coord imageUV, Coord imageSize) {
            super(guiIcons, imageUV, imageSize);
        }
    }
}
