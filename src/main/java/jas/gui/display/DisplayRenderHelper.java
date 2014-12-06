package jas.gui.display;

import jas.gui.utilities.Coord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import org.lwjgl.util.Point;

public class DisplayRenderHelper {

    /**
     * Draw a Texture, using drawTexturedModalRect but in 4 corners from each edge of the defined image. No checking is
     * done to ensure the desired render size is possible with the provided image.
     * 
     * Useful for being drawing images with programmatically set height.
     * 
     * i.e. For an image 200x200 with a render size of 50x50 the top-left corner is drawn form X{0-50},Y{0-50} of the
     * 200x200 image while the bottom-right corner would be drawn from X{150-200},Y{150-200} of the 200x200 image.
     * Similar schemes are used for top-right/bottom/left.
     */
    public static void drawTexture4Quadrants(Tessellator tess, float zLevel, Coord screenPos, Coord screenSize,
            Coord imageUV, Coord imageSize) {
        // Top-Left
        DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, zLevel + 0.01f, screenPos.x, screenPos.z,
                imageUV.x, imageUV.z, screenSize.x / 2, screenSize.z / 2);
        // Top-Right
        DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, zLevel + 0.02f, screenPos.x + screenSize.x / 2,
                screenPos.z, imageUV.x + imageSize.x - screenSize.x / 2, imageUV.z, screenSize.x / 2, screenSize.z / 2);
        // Bottom-Right
        DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, zLevel + 0.03f, screenPos.x, screenPos.z
                + screenSize.z / 2, imageUV.x, imageUV.z + imageSize.z - screenSize.z / 2, screenSize.x / 2,
                screenSize.z / 2);
        // Bottom-Left
        DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, zLevel + 0.04f, screenPos.x + screenSize.x / 2,
                screenPos.z + screenSize.z / 2, imageUV.x + imageSize.x - screenSize.x / 2, imageUV.z + imageSize.z
                        - screenSize.z / 2, screenSize.x / 2, screenSize.z / 2);
    }

    /**
     * Fundamental Minecraft Function to Draw a Texture in the World. Copied from Minecraft Code, not sure where. It is
     * literally identical in several places in MC code.
     * 
     * @param par1 posX
     * @param par2 posY
     * @param par3 ImageX
     * @param par4 ImageY
     * @param par5 ImageWidth
     * @param par6 ImageHeight
     */
    public static void drawTexturedModalRect(Tessellator tess, float zLevel, int par1, int par2, int par3, int par4,
            int par5, int par6) {
        float var7 = 0.00390625F; // == 1/256-> Image scale from screen to file? 1/256
        float var8 = 0.00390625F;
        tess.startDrawingQuads();
        // tes.addVertexWithUV(x1, y2, z, u1, v2);
        // tes.addVertexWithUV(x2, y2, z, u2, v2);
        // tes.addVertexWithUV(x2, y1, z, u2, v1);
        // tes.addVertexWithUV(x1, y1, z, u1, v1);
        tess.addVertexWithUV((double) (par1 + 0), (double) (par2 + par6), zLevel, ((float) (par3 + 0) * var7),
                ((float) (par4 + par6) * var8));
        tess.addVertexWithUV((double) (par1 + par5), (double) (par2 + par6), zLevel, ((float) (par3 + par5) * var7),
                ((float) (par4 + par6) * var8));
        tess.addVertexWithUV((double) (par1 + par5), (double) (par2 + 0), zLevel, ((float) (par3 + par5) * var7),
                ((float) (par4 + 0) * var8));
        tess.addVertexWithUV((double) (par1 + 0), (double) (par2 + 0), zLevel, ((float) (par3 + 0) * var7),
                ((float) (par4 + 0) * var8));
        tess.draw();
    }

    public static void drawTexturedModalRect(Tessellator tess, float zLevel, Coord screenPos, Coord imageUV,
            Coord imageSize) {
        float var7 = 0.00390625F; // == 1/256-> Image scale from screen to file? 1/256
        float var8 = 0.00390625F;
        tess.startDrawingQuads();
        double x1 = screenPos.x + 0;
        double y1 = screenPos.z + 0;
        double x2 = screenPos.x + imageSize.x;
        double y2 = screenPos.z + imageSize.z;
        double z = zLevel;
        double u1 = (imageUV.x + 0) * var7;
        double v1 = (imageUV.z + 0) * var8;
        double u2 = (imageUV.x + imageSize.x) * var7;
        double v2 = (imageUV.z + imageSize.z) * var8;

        tess.addVertexWithUV(x1, y2, zLevel, u1, v2);
        tess.addVertexWithUV(x2, y2, zLevel, u2, v2);
        tess.addVertexWithUV(x2, y1, zLevel, u2, v1);
        tess.addVertexWithUV(x1, y1, zLevel, u1, v1);
        tess.draw();
    }

    public static void drawTextureModelFromIcon(Tessellator tess, IIcon icon, Point screenPosition) {
        final float minU = icon.getMinU();
        final float maxU = icon.getMaxU();
        final float minV = icon.getMinV();
        final float maxV = icon.getMaxV();
        final float zLevel = 10.0F;

        tess.startDrawingQuads();

        tess.addVertexWithUV(screenPosition.getX() + 00.0D, screenPosition.getY() + 16.0D, zLevel, minU, maxV);
        tess.addVertexWithUV(screenPosition.getX() + 16.0D, screenPosition.getY() + 16.0D, zLevel, maxU, maxV);
        tess.addVertexWithUV(screenPosition.getX() + 16.0D, screenPosition.getY() + 00.0D, zLevel, maxU, minV);
        tess.addVertexWithUV(screenPosition.getX() + 00.0D, screenPosition.getY() + 00.0D, zLevel, minU, minV);
        tess.draw();
    }

    /**
     * Color is RGB `int color = (RED << 16) + (GREEN << 8) + BLUE` where REG/GREEN/BLUE are each a 0-255 int value
     */
    public static void drawCenteredString(FontRenderer fontRenderer, String text, int posX, int posY, int color,
            boolean shadow) {
        fontRenderer.drawStringWithShadow(text, posX - fontRenderer.getStringWidth(text) / 2, posY, color);
    }

    /**
     * Color is RGB `int color = (RED << 16) + (GREEN << 8) + BLUE` where REG/GREEN/BLUE are each a 0-255 int value
     */
    public static void drawString(FontRenderer fontRenderer, String text, int posX, int posY, int color, boolean shadow) {
        fontRenderer.drawString(text, posX, posY, color);
    }
}
