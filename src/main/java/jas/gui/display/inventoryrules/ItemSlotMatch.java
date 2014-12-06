package jas.gui.display.inventoryrules;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemSlotMatch implements InventoryRule {
    // SlotId is zero index
    private int slotId;
    public boolean armorSlot;

    public void setSlotId(int slotId) {
        if (slotId < 0) {
            slotId = 0;
        } else if (armorSlot && slotId >= Minecraft.getMinecraft().thePlayer.inventory.armorInventory.length) {
            slotId = Minecraft.getMinecraft().thePlayer.inventory.armorInventory.length - 1;
        } else if (!armorSlot && slotId >= Minecraft.getMinecraft().thePlayer.inventory.mainInventory.length) {
            slotId = Minecraft.getMinecraft().thePlayer.inventory.mainInventory.length - 1;
        }
        this.slotId = slotId;
    }

    public int getSlotId() {
        return slotId;
    }

    public ItemSlotMatch(int slotId, boolean armorSlot) {
        this.slotId = slotId;
        this.armorSlot = armorSlot;
    }

    @Override
    public boolean isMatch(ItemStack itemStack, int slotId, boolean armorSlot, boolean currentItem) {
        return this.armorSlot == armorSlot && this.slotId == slotId;
    }

    @Override
    public boolean allowMultipleMatches() {
        return false;
    }
}
