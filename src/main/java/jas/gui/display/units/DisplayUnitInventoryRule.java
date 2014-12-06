package jas.gui.display.units;

import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.inventoryrules.InventoryRule;
import jas.gui.display.inventoryrules.ItemHandMatch;
import jas.gui.display.inventoryrules.ItemIdMatch;
import jas.gui.display.inventoryrules.ItemMetaMatch;
import jas.gui.display.inventoryrules.ItemSlotMatch;
import jas.gui.display.resource.SimpleImageResource.GuiIconImageResource;
import jas.gui.display.units.windows.DisplayUnitButton;
import jas.gui.display.units.windows.DisplayUnitTextField;
import jas.gui.display.units.windows.DisplayUnitToggle;
import jas.gui.display.units.windows.DisplayWindow;
import jas.gui.display.units.windows.DisplayUnitTextField.Validator;
import jas.gui.display.units.windows.DisplayUnitToggle.Toggle;
import jas.gui.display.units.windows.DisplayWindowScrollList.Scrollable;
import jas.gui.display.units.windows.DisplayWindowScrollList.ScrollableElement;
import jas.gui.display.units.windows.list.ScrobbleElementRemoveButton;
import jas.gui.display.units.windows.text.ValidatorBoundedInt;
import jas.gui.display.units.windows.text.ValidatorInt;
import jas.gui.utilities.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonObject;

public class DisplayUnitInventoryRule extends DisplayWindow implements ScrollableElement<InventoryRule> {
    public static final String DISPLAY_ID = "DisplayWindowMenu";
    private static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");

    private RULEID ruleId;
    private Coord size;
    private VerticalAlignment vertAlign = VerticalAlignment.TOP_ABSO;
    private HorizontalAlignment horizAlign = HorizontalAlignment.LEFT_ABSO;
    private boolean isMouseOver;
    private boolean scrollVisibility;
    private Scrollable<InventoryRule> container;
    private InventoryRule source;

    // TODO: A more generic way to get editable segments of InventoryRules. Theres only a few atm, so individual support
    // is manageable
    public enum RULEID {
        HAND, ID, IDMETA("ID-META"), SLOT;
        public final String displayName;

        private RULEID() {
            this.displayName = this.toString();
        }

        private RULEID(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * 
     * @param inventoryRule
     * @param Scrollable is used for this unit to remove itself, if possible
     */
    public DisplayUnitInventoryRule(ItemHandMatch inventoryRule, Scrollable<InventoryRule> container) {
        ruleId = RULEID.HAND;
        // Add button to remove rule
        addElement(new DisplayUnitButton(new Coord(104, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new ScrobbleElementRemoveButton<InventoryRule>(this, container))
                .setIconImageResource(new GuiIconImageResource(new Coord(201, 44), new Coord(13, 16))));
        size = new Coord(139, 24);
        this.container = container;
        this.source = inventoryRule;
    }

    public DisplayUnitInventoryRule(ItemIdMatch inventoryRule, Scrollable<InventoryRule> container) {
        ruleId = RULEID.ID;
        // Add TextBox to set string id --> will eventually be scroll list
        addElement(new DisplayUnitTextField(new Coord(22, 2), new Coord(60, 16), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, 30, new ItemIdTextField(inventoryRule)));
        // Add Toggle to set multipleMatches
        addElement(new DisplayUnitToggle(new Coord(83, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new ToggleMultipleMatches(inventoryRule))
                .setIconImageResource(new GuiIconImageResource(new Coord(183, 23), new Coord(13, 16))));
        // Add button to remove rule
        addElement(new DisplayUnitButton(new Coord(104, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new ScrobbleElementRemoveButton<InventoryRule>(this, container))
                .setIconImageResource(new GuiIconImageResource(new Coord(201, 44), new Coord(13, 16))));
        size = new Coord(139, 24);
        this.container = container;
        this.source = inventoryRule;
    }

    public DisplayUnitInventoryRule(final ItemMetaMatch inventoryRule, Scrollable<InventoryRule> container) {
        ruleId = RULEID.IDMETA;
        // Add TextBox to set string id --> will eventually be scroll list
        addElement(new DisplayUnitTextField(new Coord(2, 24), new Coord(60, 16), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, 30, new ItemIdTextField(inventoryRule)));
        // Add TextBox to set min string damage (--> eventually scroll list that selects id + damage?)
        addElement(new DisplayUnitTextField(new Coord(63, 24), new Coord(30, 16), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, 5, new ValidatorBoundedInt(0, 65535) {

                    @Override
                    public void setInt(int textValue) {
                        inventoryRule.setMinItemDamage(textValue);
                    }

                    public int getValue() {
                        return inventoryRule.getMinItemDamage();
                    }
                }));
        // Add TextBox to set max string damage (--> eventually scroll list that selects id + damage?)
        addElement(new DisplayUnitTextField(new Coord(94, 24), new Coord(30, 16), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, 5, new ValidatorBoundedInt(0, 65535) {

                    @Override
                    public void setInt(int textValue) {
                        inventoryRule.setMaxItemDamage(textValue);
                    }

                    public int getValue() {
                        return inventoryRule.getMaxItemDamage();
                    }
                }));
        // Add Toggle to set multipleMatches
        addElement(new DisplayUnitToggle(new Coord(83, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new ToggleMultipleMatches(inventoryRule))
                .setIconImageResource(new GuiIconImageResource(new Coord(183, 23), new Coord(13, 16))));
        // Add button to remove rule
        addElement(new DisplayUnitButton(new Coord(104, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new ScrobbleElementRemoveButton<InventoryRule>(this, container))
                .setIconImageResource(new GuiIconImageResource(new Coord(201, 44), new Coord(13, 16))));
        size = new Coord(139, 42);
        this.container = container;
        this.source = inventoryRule;
    }

    public DisplayUnitInventoryRule(final ItemSlotMatch inventoryRule, Scrollable<InventoryRule> container) {
        ruleId = RULEID.SLOT;
        // Add TextBox to set string slotId --> will eventually be scroll list
        // Add TextBox to set string armorSlot (--> eventually scroll list that selected id + damage?)
        addElement(new DisplayUnitTextField(new Coord(52, 2), new Coord(30, 16), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, 2, new ValidatorInt() {

                    @Override
                    public boolean isStringValid(String text) {
                        if (!super.isStringValid(text)) {
                            return false;
                        }
                        Integer slotId = Integer.parseInt(text);
                        if (inventoryRule.armorSlot) {
                            return slotId >= 0
                                    && slotId < Minecraft.getMinecraft().thePlayer.inventory.armorInventory.length;
                        } else {
                            return slotId >= 0
                                    && slotId < Minecraft.getMinecraft().thePlayer.inventory.mainInventory.length;
                        }
                    }

                    @Override
                    public void setInt(int textValue) {
                        inventoryRule.setSlotId(textValue);
                    }

                    @Override
                    public int getValue() {
                        return inventoryRule.getSlotId();
                    }

                }));
        // Add Toggle for ifArmorSlot
        addElement(new DisplayUnitToggle(new Coord(83, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new Toggle() {

                    @Override
                    public void toggle() {
                        inventoryRule.armorSlot = !inventoryRule.armorSlot;
                        /* Reset armor slot such that the slotId is still properly bounded */
                        inventoryRule.setSlotId(inventoryRule.getSlotId());
                    }

                    @Override
                    public boolean isToggled() {
                        return inventoryRule.armorSlot;
                    }
                }).setIconImageResource(new GuiIconImageResource(new Coord(202, 23), new Coord(13, 16))));
        // Add button to remove rule
        addElement(new DisplayUnitButton(new Coord(104, 2), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                HorizontalAlignment.LEFT_ABSO, new ScrobbleElementRemoveButton<InventoryRule>(this, container))
                .setIconImageResource(new GuiIconImageResource(new Coord(201, 44), new Coord(13, 16))));
        size = new Coord(139, 24);
        this.container = container;
        this.source = inventoryRule;
    }

    private static class ItemIdTextField implements Validator {
        private ItemIdMatch matchRule;

        public ItemIdTextField(ItemIdMatch matchRule) {
            this.matchRule = matchRule;
        }

        @Override
        public boolean isCharacterValid(char eventCharacter) {
            return ChatAllowedCharacters.isAllowedCharacter(eventCharacter);
        }

        @Override
        public boolean isStringValid(String text) {
            return Item.itemRegistry.getObject(text) != null;
        }

        @Override
        public void setString(String text) {
            matchRule.itemId = text;
        }

        @Override
        public String getString() {
            return matchRule.itemId;
        }
    }

    private static class ToggleMultipleMatches implements Toggle {
        private ItemIdMatch matchRule;

        public ToggleMultipleMatches(ItemIdMatch matchRule) {
            this.matchRule = matchRule;
        }

        @Override
        public void toggle() {
            matchRule.multipleMatches = !matchRule.multipleMatches;
        }

        @Override
        public boolean isToggled() {
            return matchRule.multipleMatches;
        }
    }

    @Override
    public String getSubType() {
        return DISPLAY_ID;
    }

    @Override
    public Coord getSize() {
        return size;
    }

    @Override
    public void setOffset(Coord offset) {
        this.offset = offset;
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return vertAlign;
    }

    @Override
    public HorizontalAlignment getHorizontalAlignment() {
        return horizAlign;
    }

    @Override
    public void setVerticalAlignment(VerticalAlignment alignment) {
        vertAlign = alignment;
    }

    @Override
    public void setHorizontalAlignment(HorizontalAlignment alignment) {
        horizAlign = alignment;
    }

    @Override
    public void onUpdate(Minecraft mc, int ticks) {
        super.onUpdate(mc, ticks);
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return true;
    }

    @Override
    public void renderSubDisplay(Minecraft mc, Coord position) {
        FontRenderer fontRenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(guiButton);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (isMouseOver) {
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(),
                    new Coord(000, 000), new Coord(127, 127));
        } else {
            // DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -5.0f, position, getSize(), new
            // Coord(000,
            // 128), new Coord(127, 127));
        }

        String shortName = (String) fontRenderer.listFormattedStringToWidth(ruleId.displayName, getSize().x).get(0);
        // Note posZ-4+getSize/2. -4 is to 'center' the string vertically, and getSize/2 is to move center to the
        // middle button
        DisplayRenderHelper.drawString(fontRenderer, shortName + ":", position.x + 3, position.z
                + fontRenderer.FONT_HEIGHT / 2, 16777120, true);
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker hoverChecker) {
        super.mousePosition(localMouse, hoverAction, hoverChecker);
        if (!hoverChecker.isHoverFound() && hoverAction == HoverAction.HOVER) {
            isMouseOver = true;
            hoverChecker.markHoverFound();
        } else {
            isMouseOver = false;
        }
    }

    @Override
    public ActionResult subMouseAction(Coord localMouse, MouseAction action, int... actionData) {
        return ActionResult.NOACTION;
    }

    @Override
    public ActionResult subKeyTyped(char eventCharacter, int eventKey) {
        return ActionResult.NOACTION;
    }

    @Override
    public void saveCustomData(JsonObject jsonObject) {
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
    }

    @Override
    public void setScrollVisibity(boolean visibility) {
        scrollVisibility = visibility;
    }

    @Override
    public boolean isVisibleInScroll() {
        return scrollVisibility;
    }

    @Override
    public InventoryRule getSource() {
        return source;
    }
}
