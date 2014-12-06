package jas.gui.display.inventoryrules;

import net.minecraft.item.ItemStack;

public class ItemHandMatch implements InventoryRule {

    @Override
    public boolean isMatch(ItemStack itemStack, int slotId, boolean armorSlot, boolean currentItem) {
        return currentItem;
    }

    @Override
    public boolean allowMultipleMatches() {
        return false;
    }
}
