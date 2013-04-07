package jas.common.gui.elements;

import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.util.Point;

/**
 * GuiScreen Designed to Hold Several Child Elements that Whose Elements are Positioned Together
 */
public abstract class GuiPanel extends GuiScreen {
    public final GuiPanel parent;
    public final HashMap<String, GuiPanel> children = new HashMap<String, GuiPanel>();

    public PanelPoint panelPoint;
    private boolean enabled = true;

    public GuiPanel(GuiPanel parent) {
        super();
        this.parent = parent;
        enabled = parent != null ? parent.enabled : true;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        for (GuiPanel childPanel : children.values()) {
            childPanel.setEnabled(enabled);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        super.setWorldAndResolution(minecraft, width, height);
        for (GuiPanel childPanel : children.values()) {
            childPanel.setWorldAndResolution(minecraft, width, height);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        panelPoint = new PanelPoint(new Point(width / 2, height / 2), new Point(256, 244));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiPanel childPanel : children.values()) {
            if (childPanel.enabled) {
                childPanel.updateScreen();
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        for (GuiPanel childPanel : children.values()) {
            if (childPanel.enabled) {
                childPanel.actionPerformed(button);
            }
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyID) {
        super.keyTyped(keyChar, keyID);
        for (GuiPanel childPanel : children.values()) {
            if (childPanel.enabled) {
                childPanel.keyTyped(keyChar, keyID);
            }
        }
    }

    @Override
    protected void mouseClicked(int clickedX, int clickedY, int mouseState) {
        super.mouseClicked(clickedX, clickedY, mouseState);
        for (GuiPanel childPanel : children.values()) {
            if (childPanel.enabled) {
                childPanel.mouseClicked(clickedX, clickedY, mouseState);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        super.drawScreen(mouseX, mouseY, par3);
        for (GuiPanel childPanel : children.values()) {
            if (childPanel.enabled) {
                childPanel.drawScreen(mouseX, mouseY, par3);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        for (GuiPanel childPanel : children.values()) {
            childPanel.onGuiClosed();
        }
        super.onGuiClosed();
    }
}
