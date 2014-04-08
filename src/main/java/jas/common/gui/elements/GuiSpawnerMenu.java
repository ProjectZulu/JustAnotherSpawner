package jas.common.gui.elements;

import net.minecraft.client.gui.GuiButton;

/**
 * Main Menu For Spawner
 */
public class GuiSpawnerMenu extends GuiPanel {

    public GuiSpawnerMenu(GuiPanel parent) {
        super(parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(ButtonID.CATEGORY_EDIT.iD, panelPoint.center().getX() - 90, panelPoint.center()
                .getY() - 36, 180, 20, "Categories"));
        buttonList.add(new GuiButton(ButtonID.CREATURE_EDIT.iD, panelPoint.center().getX() - 90, panelPoint.center()
                .getY() - 12, 180, 20, "Creatures"));
        buttonList.add(new GuiButton(ButtonID.SPAWN_EDIT.iD, panelPoint.center().getX() - 90, panelPoint.center()
                .getY() + 12, 180, 20, "Spawns"));
        buttonList.add(new GuiButton(ButtonID.EXIT.iD, panelPoint.center().getX() - 90,
                panelPoint.center().getY() + 36, 180, 20, "Exit"));
    }

    @Override
    @SuppressWarnings("incomplete-switch")
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.enabled) {
            switch (ButtonID.getButtonByIndex(button.id)) {
            case CATEGORY_EDIT:
            case CREATURE_EDIT:
            case SPAWN_EDIT:
            case EXIT:
                mc.currentScreen = null;
                break;
            case UNKNOWN:
                throw new IllegalStateException("Button action does not exist.");
            }
        }
    }
}
