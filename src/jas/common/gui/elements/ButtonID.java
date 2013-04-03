package jas.common.gui.elements;

public enum ButtonID {
    UNKNOWN(-1);

    int index;
    ButtonID(int index) {
        this.index = index;
    }

    public static ButtonID getButtonByIndex(int index) {
        for (ButtonID buttonID : ButtonID.values()) {
            if (buttonID.index == index) {
                return buttonID;
            }
        }
        return null;
    }
}