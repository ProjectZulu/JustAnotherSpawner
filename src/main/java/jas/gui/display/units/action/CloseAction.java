package jas.gui.display.units.action;

import jas.gui.display.units.DisplayUnit;

public class CloseAction extends ReplaceAction {
    public CloseAction(DisplayUnit displayToClose) {
        this(new DisplayUnit[] { displayToClose });
    }

    public CloseAction(DisplayUnit[] displaysToClose) {
        super(new DisplayUnit[] {}, displaysToClose);
    }

    public CloseAction(boolean closeAll) {
        super(new DisplayUnit[] {}, closeAll);
    }
}
