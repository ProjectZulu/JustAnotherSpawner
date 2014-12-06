package jas.gui.display.resource;

import jas.gui.utilities.Coord;
import net.minecraft.util.ResourceLocation;

public interface DualImageResource extends ImageResource {

    public ResourceLocation getSecondaryToBind();

    public abstract Coord getSecondaryUV();

    public abstract Coord getSecondarySize();
}
