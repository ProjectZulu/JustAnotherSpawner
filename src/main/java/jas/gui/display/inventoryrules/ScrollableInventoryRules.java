package jas.gui.display.inventoryrules;

import jas.gui.display.units.DisplayUnitInventoryRule;
import jas.gui.display.units.windows.DisplayWindowScrollList;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import com.google.common.base.Optional;

public class ScrollableInventoryRules implements Scrollable<InventoryRule> {

    private InventoryRules rules;
    ArrayList<ScrollableElement<InventoryRule>> scrollableList;
    private Optional<ScrollableElement<InventoryRule>> selectedEntry = Optional.absent();

    public ScrollableInventoryRules(InventoryRules rules) {
        this.rules = rules;
        scrollableList = new ArrayList<ScrollableElement<InventoryRule>>();
        for (InventoryRule inventoryRule : rules) {
            scrollableList.add(inventoryRuleToScrollable(inventoryRule));
        }
    }

    private ScrollableElement<InventoryRule> inventoryRuleToScrollable(InventoryRule inventoryRule) {
        // TODO: This is beyond ugly, something should be done... eventually
        if (inventoryRule instanceof ItemHandMatch) {
            return new DisplayUnitInventoryRule((ItemHandMatch) inventoryRule, this);
        } else if (inventoryRule instanceof ItemMetaMatch) {
            return new DisplayUnitInventoryRule((ItemMetaMatch) inventoryRule, this);
        } else if (inventoryRule instanceof ItemIdMatch) {
            return new DisplayUnitInventoryRule((ItemIdMatch) inventoryRule, this);
        } else if (inventoryRule instanceof ItemSlotMatch) {
            return new DisplayUnitInventoryRule((ItemSlotMatch) inventoryRule, this);
        } else {
            throw new IllegalArgumentException("Unknown InventoryRule type " + inventoryRule);
        }
    }

    @Override
    public Collection<? extends ScrollableElement<InventoryRule>> getElements() {
        return scrollableList;
    }

    @Override
    public boolean removeElement(ScrollableElement<InventoryRule> element) {
        rules.remove(element.getSource());
        return scrollableList.remove(element);
    }

    @Override
    public boolean addElement(ScrollableElement<InventoryRule> element) {
        rules.add(element.getSource());
        return scrollableList.add(element);
    }

    public boolean addElement(InventoryRule element) {
        rules.add(element);
        return scrollableList.add(inventoryRuleToScrollable(element));
    }

    @Override
    public void moveElement(ScrollableElement<InventoryRule> element, int unitstoMove) {
        int scrollIndex = scrollableList.indexOf(element);
        int sourceIndex = rules.get().indexOf(element.getSource());
        if (isSwapValid(scrollableList, scrollIndex, unitstoMove) && isSwapValid(rules.get(), sourceIndex, unitstoMove)) {
            Collections.swap(scrollableList, scrollIndex, scrollIndex + unitstoMove);
            Collections.swap(rules.get(), sourceIndex, sourceIndex + unitstoMove);
        }
    }

    private <K> boolean isSwapValid(List<K> list, int index, int indexToMove) {
        if (index >= 0 && index < list.size()) {
            if (index + indexToMove >= 0 && index + indexToMove < list.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setSelected(ScrollableElement<InventoryRule> element) {
        selectedEntry = element != null ? Optional.of(element) : Optional.<ScrollableElement<InventoryRule>> absent();
    }

    @Override
    public Optional<ScrollableElement<InventoryRule>> getSelected() {
        return selectedEntry;
    }
}
