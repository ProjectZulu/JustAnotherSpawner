package jas.common.gui.elements;

public enum ButtonID {
    EXIT(0), BACK(1), CATEGORY_EDIT(2), SPAWN_EDIT(3), CREATURE_EDIT(4), UNKNOWN(-1);

    int iD;

    ButtonID(int iD) {
        this.iD = iD;
    }

    public static ButtonID getButtonByIndex(int iD) {
        for (ButtonID buttonID : ButtonID.values()) {
            if (buttonID.iD == iD) {
                return buttonID;
            }
        }
        return null;
    }
}