package jas.common.gui.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.util.Point;

/**
 * GuiScreen Designed to Hold Several Child Elements that Whose Elements are Positioned Together
 */
public abstract class Panel extends GuiScreen {
    public final Panel parent;
    private List<? extends Panel> children = new ArrayList<>();

    public PanelPoint panelPoint;
    private Point defaultBounds = new Point(256, 244);
    
    public Panel(Panel parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void setWorldAndResolution(Minecraft minecraft, int width, int height) {
        super.setWorldAndResolution(minecraft, width, height);
        for (Panel childPanel : children) {
            childPanel.setWorldAndResolution(minecraft, width, height);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        if (parent == null) {
            panelPoint = new PanelPoint(new Point(width / 2, height / 2), defaultBounds);
        } else {
            panelPoint = new PanelPoint(parent.panelPoint.location, parent.panelPoint.bounds);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (Panel childPanel : children) {
            childPanel.updateScreen();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        for (Panel childPanel : children) {
            childPanel.actionPerformed(button);
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyID) {
        super.keyTyped(keyChar, keyID);
        for (Panel childPanel : children) {
            childPanel.keyTyped(keyChar, keyID);
        }
    }

    @Override
    protected void mouseClicked(int clickedX, int clickedY, int mouseState) {
        super.mouseClicked(clickedX, clickedY, mouseState);
        for (Panel childPanel : children) {
            childPanel.mouseClicked(clickedX, clickedY, mouseState);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        super.drawScreen(mouseX, mouseY, par3);
        for (Panel childPanel : children) {
            childPanel.drawScreen(mouseX, mouseY, par3);
        }
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        for (Panel childPanel : children) {
            childPanel.onGuiClosed();
        }
    }
}
