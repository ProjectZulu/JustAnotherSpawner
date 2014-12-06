package jas.gui.display.resource;

import jas.gui.utilities.Coord;
import net.minecraft.util.ResourceLocation;

public interface ImageResource {

    public abstract ResourceLocation getImageToBind();

    public abstract Coord getImageUV();

    public abstract Coord getImageSize();
}
