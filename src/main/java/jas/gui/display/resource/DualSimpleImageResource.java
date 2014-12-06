package jas.gui.display.resource;

import jas.gui.utilities.Coord;
import net.minecraft.util.ResourceLocation;

public class DualSimpleImageResource implements DualImageResource {

    private final SimpleImageResource primary;
    private final SimpleImageResource secondary;

    public DualSimpleImageResource(SimpleImageResource primary) {
        this.primary = primary;
        this.secondary = primary;
    }

    public DualSimpleImageResource(SimpleImageResource primary, SimpleImageResource secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public ResourceLocation getImageToBind() {
        return primary.getImageToBind();
    }

    @Override
    public Coord getImageUV() {
        return primary.getImageUV();
    }

    @Override
    public Coord getImageSize() {
        return primary.getImageSize();
    }

    @Override
    public ResourceLocation getSecondaryToBind() {
        return secondary.getImageToBind();
    }

    @Override
    public Coord getSecondaryUV() {
        return secondary.getImageUV();
    }

    @Override
    public Coord getSecondarySize() {
        return secondary.getImageSize();
    }
}
