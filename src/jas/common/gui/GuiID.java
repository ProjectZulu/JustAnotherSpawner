package jas.common.gui;


public enum GuiID {
    Spawner(0),
    Unknown(-1);

    int iD;

    GuiID(int iD) {
        this.iD = iD;
    }

    public int getID() {
        return iD;
    }

    public static GuiID getGuiIDByID(int iD) {
        for (GuiID guiID : GuiID.values()) {
            if (guiID.iD == iD) {
                return guiID;
            }
        }
        return Unknown;
    }
}
