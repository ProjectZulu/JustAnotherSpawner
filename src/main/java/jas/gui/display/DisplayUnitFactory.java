package jas.gui.display;

import jas.gui.GuiLog;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnitItem;
import jas.gui.display.units.DisplayUnitPotion;
import jas.gui.display.units.DisplayUnitSortedPanel;
import jas.gui.display.units.DisplayUnitUnsortedPanel;

import java.util.HashMap;


import org.apache.logging.log4j.Level;

import com.google.gson.JsonObject;

public class DisplayUnitFactory {
    private HashMap<String, Class<? extends DisplayUnit>> displayTypes;

    public Class<? extends DisplayUnit> getDisplayType(String type) {
        return displayTypes.get(type);
    }

    // TODO: Change to event thats called during postinit or so, ensures Types cannot be added/removed while running
    public boolean addDisplayType(Class<? extends DisplayUnit> displayType) {
        if (displayType == null) {
            throw new IllegalArgumentException("Cannot register null DisplayType");
        }

        DisplayUnit displayUnit;
        try {
            displayUnit = displayType.newInstance();
        } catch (InstantiationException e) {
            GuiLog.log().warning("Type [%s] cannot be registered because it cannot be instantiated.", displayType);
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            GuiLog.log().warning("Type [%s] not able be registered because it cannot be instantiated.", displayType);
            e.printStackTrace();
            return false;
        }

        if (!displayTypes.containsKey(displayUnit.getType())) {
            displayTypes.put(displayUnit.getType(), displayType);
            GuiLog.log().debug(Level.INFO, "Registered Type %s to %s", displayUnit.getType(), displayType);
            return true;
        } else {
            GuiLog.log().warning("Type [%s] cannot be registered because TypeId [%s] is already taken.", displayType,
                    displayUnit.getType());
            return false;
        }
    }

    public DisplayUnit createDisplay(String type, JsonObject jsonObject) {
        Class<? extends DisplayUnit> displayType = getDisplayType(type);
        DisplayUnit displayTicker = null;
        if (displayType != null) {
            try {
                displayTicker = displayType.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            GuiLog.log().warning("Type [%s] is not defined. Cannot create display.", type);
        }
        displayTicker.loadCustomData(this, jsonObject);
        return displayTicker;
    }

    public DisplayUnitFactory() {
        displayTypes = new HashMap<String, Class<? extends DisplayUnit>>();
        // TODO Add display types
        addDisplayType(DisplayUnitItem.class);
        addDisplayType(DisplayUnitPotion.class);
        addDisplayType(DisplayUnitSortedPanel.class);
        addDisplayType(DisplayUnitUnsortedPanel.class);
    }
}
