package jas.common.gui.elements;

import jas.common.DefaultProps;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

/**
 * Spawner GUI Screen "Main Menu"
 */
public class GuiSpawner extends Panel {

    public GuiSpawner() {
        super(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        super.drawScreen(mouseX, mouseY, par3);
        drawDefaultBackground();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.enabled) {
            switch (ButtonID.getButtonByIndex(button.id)) {
            case UNKNOWN:
                throw new IllegalStateException("Button action does not exist.");
            }
        }
    }

    @Override
    public void drawDefaultBackground() {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(DefaultProps.GUIDIR + "large_background.png");
        int xCoord = (width - panelPoint.bounds.getX()) / 2;
        int yCoord = (height - panelPoint.bounds.getY()) / 2;
        this.drawTexturedModalRect(xCoord, yCoord, 0, 0, panelPoint.bounds.getX(), panelPoint.bounds.getY());
    }
}
