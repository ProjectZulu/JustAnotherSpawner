package jas.gui;


import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.inventoryrules.InventoryRule;
import jas.gui.display.inventoryrules.ItemSlotMatch;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnitItem;
import jas.gui.display.units.DisplayUnitPotion;
import jas.gui.display.units.DisplayUnitSortedPanel;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.DisplayUnit.VerticalAlignment;
import jas.gui.display.units.DisplayUnitItem.TrackMode;
import jas.gui.display.units.DisplayUnitPanel.DisplayMode;
import jas.gui.display.units.DisplayUnitSortedPanel.SortMode;
import jas.gui.utilities.Coord;
import jas.gui.utilities.FileUtilities;
import jas.gui.utilities.GsonHelper;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DisplayUnitRegistry {

    DisplayUnitFactory displayFactory;
    private ArrayList<DisplayUnit> displays = new ArrayList<DisplayUnit>();

    public ArrayList<DisplayUnit> currentDisplays() {
        return displays;
    }

    private ChangeManager displayChanger = new ChangeManager();

    public DisplayChanger getDisplayChanger() {
        return displayChanger;
    }

    /** Public interface to allow external components to cause addition/removal */
    public static interface DisplayChanger {
        public abstract void addToRemoveList(DisplayUnit display);

        public abstract void addToAddList(DisplayUnit display);
    }

    /**
     * Responsible for adding/removing display in a non-conflicting manner
     */
    private static class ChangeManager implements DisplayChanger {
        public ArrayList<DisplayUnit> addList;
        public ArrayList<DisplayUnit> removeList;

        private ChangeManager() {
            addList = new ArrayList<DisplayUnit>();
            removeList = new ArrayList<DisplayUnit>();
        }

        @Override
        public void addToAddList(DisplayUnit display) {
            addList.add(display);
        }

        @Override
        public void addToRemoveList(DisplayUnit display) {
            removeList.add(display);
        }
    }

    public DisplayUnitRegistry(DisplayUnitFactory displayFactory, File configDirectory) {
        this.displayFactory = displayFactory;
        loadFromConfig(configDirectory);
    }

    public void loadFromConfig(File configDirectory) {
        Gson gson = GsonHelper.createGson(true, true, new Class[] { DisplayRegistrySaveObject.class },
                new Object[] { new DisplayRegistrySaveObject.Serializer(displayFactory) });
        File displayListFile = DisplayUnitRegistry.getFile(configDirectory);
        DisplayRegistrySaveObject displayResult = GsonHelper.readOrCreateFromGson(
                FileUtilities.createReader(displayListFile, false), DisplayRegistrySaveObject.class, gson);
        if (displayResult.getDisplays().isPresent()) {
            displays = new ArrayList<DisplayUnit>(displayResult.getDisplays().get());
        } else {
            ArrayList<DisplayUnit> defaultDisplays = new ArrayList<DisplayUnit>();
            {
                DisplayUnitItem headArmorDisplay = new DisplayUnitItem(new Coord(-99, 2), TrackMode.DURABILITY,
                        VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.CENTER_ABSO, "",
                        new InventoryRule[] { new ItemSlotMatch(2, true) });
                headArmorDisplay.enableDigitalCounter(false);
                headArmorDisplay.setAnalogOffset(new Coord(0, 16));
                defaultDisplays.add(headArmorDisplay);

                DisplayUnitItem chestArmorDisplay = new DisplayUnitItem(new Coord(-99, -16), TrackMode.DURABILITY,
                        VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.CENTER_ABSO, "",
                        new InventoryRule[] { new ItemSlotMatch(3, true) });
                chestArmorDisplay.enableDigitalCounter(false);
                chestArmorDisplay.setAnalogOffset(new Coord(0, 16));
                defaultDisplays.add(chestArmorDisplay);

                DisplayUnitItem legsDisplay = new DisplayUnitItem(new Coord(101, -16), TrackMode.DURABILITY,
                        VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.CENTER_ABSO, "",
                        new InventoryRule[] { new ItemSlotMatch(1, true) });
                legsDisplay.enableDigitalCounter(false);
                legsDisplay.setAnalogOffset(new Coord(0, 16));
                defaultDisplays.add(legsDisplay);

                DisplayUnitItem feetDisplay = new DisplayUnitItem(new Coord(101, 2), TrackMode.DURABILITY,
                        VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.CENTER_ABSO, "",
                        new InventoryRule[] { new ItemSlotMatch(0, true) });
                feetDisplay.enableDigitalCounter(false);
                feetDisplay.setAnalogOffset(new Coord(0, 16));
                defaultDisplays.add(feetDisplay);

                ArrayList<DisplayUnitPotion> goodEffectPotions = new ArrayList<DisplayUnitPotion>();
                ArrayList<DisplayUnitPotion> badEffectPotions = new ArrayList<DisplayUnitPotion>();
                for (int i = 0; i < Potion.potionTypes.length; i++) {
                    Potion potion = Potion.potionTypes[i];
                    if (potion != null) {
                        DisplayUnitPotion potionDisplay = new DisplayUnitPotion(new Coord(0, 0), 20, i, "#{count}==0");
                        if (potion.isBadEffect()) {
                            badEffectPotions.add(potionDisplay);
                        } else {
                            goodEffectPotions.add(potionDisplay);
                        }
                    }
                }
                DisplayUnitSortedPanel buffBarDisplay = new DisplayUnitSortedPanel(new Coord(0, 2), SortMode.LOWHIGH,
                        DisplayMode.COLUMN_GRID, new Coord(5, 2), false, VerticalAlignment.BOTTOM_ABSO,
                        HorizontalAlignment.LEFT_ABSO,
                        goodEffectPotions.toArray(new DisplayUnitPotion[goodEffectPotions.size()]));
                defaultDisplays.add(buffBarDisplay);
                DisplayUnitSortedPanel debuffBarDisplay = new DisplayUnitSortedPanel(new Coord(0, 2), SortMode.LOWHIGH,
                        DisplayMode.COLUMN_GRID, new Coord(5, 2), false, VerticalAlignment.BOTTOM_ABSO,
                        HorizontalAlignment.RIGHT_ABSO, badEffectPotions.toArray(new DisplayUnitPotion[badEffectPotions
                                .size()]));
                defaultDisplays.add(debuffBarDisplay);
            }
            this.displays = defaultDisplays;

        }

        // Load implicitly saves changes due to errors/corrections appear i.e. a number that cannot be below zero is set
        // to zero and should be set as such in the config
        saveToConfig(configDirectory);
    }

    public void saveToConfig(File configDirectory) {
        Gson gson = GsonHelper.createGson(true, true, new Class[] { DisplayRegistrySaveObject.class },
                new Object[] { new DisplayRegistrySaveObject.Serializer(displayFactory) });
        File displayListFile = DisplayUnitRegistry.getFile(configDirectory);
        GsonHelper.writeToGson(FileUtilities.createWriter(displayListFile, true), new DisplayRegistrySaveObject(
                currentDisplays()), gson);
    }

    public static File getFile(File configDirectory) {
        return new File(configDirectory, DefaultProps.MOD_DIR + "DisplaySettings.cfg");
    }
}
