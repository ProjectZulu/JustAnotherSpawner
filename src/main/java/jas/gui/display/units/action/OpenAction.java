package jas.gui.display.units.action;

import jas.gui.display.units.DisplayUnit;

public class OpenAction extends ReplaceAction {

    public OpenAction(DisplayUnit displaysToOpen) {
        this(new DisplayUnit[] { displaysToOpen });
    }

    public OpenAction(DisplayUnit[] displaysToOpen) {
        super(displaysToOpen, new DisplayUnit[] {});
    }

    public OpenAction(DisplayUnit[] displaysToOpen, boolean closeAll) {
        super(displaysToOpen, closeAll);
    }
}