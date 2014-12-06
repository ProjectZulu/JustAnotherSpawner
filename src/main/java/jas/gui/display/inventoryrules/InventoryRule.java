package jas.gui.display.inventoryrules;

import net.minecraft.item.ItemStack;

/**
 * Rule for determining/filtering if a particular item in an inventory is the one desired
 */
public interface InventoryRule {
    public boolean isMatch(ItemStack itemStack, int slotId, boolean armorSlot, boolean currentItem);

    public boolean allowMultipleMatches();
}
