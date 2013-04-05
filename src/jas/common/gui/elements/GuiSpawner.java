package jas.common.gui.elements;

import jas.common.DefaultProps;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

/**
 * Spawner GUI Screen Root Directory FOr Background. Cycles Between Various "Pages"
 */
public class GuiSpawner extends GuiPanel {

    public static final String MENU_KEY = "MENU";

    public GuiSpawner() {
        super(null);
        children.put(MENU_KEY, new GuiSpawnerMenu(this));
    }

    @Override
    public void initGui() {
        panelPoint = new PanelPoint(new Point(width / 2, height / 2), new Point(225, 244));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        drawDefaultBackground();
        Keyboard.enableRepeatEvents(true);
        super.drawScreen(mouseX, mouseY, par3);
    }
    
    /**
     * This Should Return False until Fully Synced i.e. Until Server Tells Client Its Okay to Start
     */
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    @SuppressWarnings("incomplete-switch")
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
        int xCoord = panelPoint.left();
        int yCoord = panelPoint.top();
        this.drawTexturedModalRect(xCoord, yCoord, 0, 0, panelPoint.bounds.getX(), panelPoint.bounds.getY());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}
