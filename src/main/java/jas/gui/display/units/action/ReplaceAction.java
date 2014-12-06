package jas.gui.display.units.action;

import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.ActionResult;

import java.util.ArrayList;
import java.util.List;


public class ReplaceAction implements ActionResult {
    private ArrayList<DisplayUnit> displaysToOpen;
    private ArrayList<DisplayUnit> displaysToClose;
    private boolean closeAll;
    private ActionResult parentResult;

    public ReplaceAction(DisplayUnit displayToOpen, DisplayUnit displayToClose) {
        this(new DisplayUnit[] { displayToOpen }, new DisplayUnit[] { displayToClose });
    }

    public ReplaceAction(DisplayUnit[] displaysToOpen, DisplayUnit[] displaysToClose) {
        this.displaysToOpen = new ArrayList<DisplayUnit>();
        for (DisplayUnit displayToOpen : displaysToOpen) {
            this.displaysToOpen.add(displayToOpen);
        }
        this.displaysToClose = new ArrayList<DisplayUnit>();
        for (DisplayUnit displayToClose : displaysToClose) {
            this.displaysToClose.add(displayToClose);
        }
        this.closeAll = false;
        this.parentResult = ActionResult.SIMPLEACTION;
    }

    public ReplaceAction(DisplayUnit displayToOpen, boolean closeAll) {
        this(new DisplayUnit[] { displayToOpen }, closeAll);
    }

    public ReplaceAction(DisplayUnit[] displaysToOpen, boolean closeAll) {
        this.displaysToOpen = new ArrayList<DisplayUnit>();
        for (DisplayUnit displayToOpen : displaysToOpen) {
            this.displaysToOpen.add(displayToOpen);
        }
        this.displaysToClose = new ArrayList<DisplayUnit>();
        this.closeAll = closeAll;
        this.parentResult = ActionResult.SIMPLEACTION;
    }

    public ReplaceAction setParentAction(ActionResult parentResult) {
        this.parentResult = parentResult;
        return this;
    }

    /**
     * Helper for when constructing an ReplaceAction instance and want to set self as parent instead of creating temp
     * local field
     */
    public ReplaceAction setSelfAsParentAction() {
        this.parentResult = this;
        return this;
    }

    @Override
    public boolean closeAll() {
        return closeAll;
    }

    @Override
    public boolean shouldStop() {
        return true;
    }

    @Override
    public List<DisplayUnit> screensToClose() {
        return displaysToClose;
    }

    @Override
    public List<DisplayUnit> screensToOpen() {
        return displaysToOpen;
    }

    @Override
    public ActionResult parentResult() {
        return parentResult;
    }
}
