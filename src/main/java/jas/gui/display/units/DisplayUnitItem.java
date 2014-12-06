package jas.gui.display.units;

import jas.gui.DefaultProps;
import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.hiderules.HideExpression;
import jas.gui.display.inventoryrules.InventoryRule;
import jas.gui.display.inventoryrules.InventoryRules;
import jas.gui.display.inventoryrules.ItemHandMatch;
import jas.gui.display.inventoryrules.ItemIdMatch;
import jas.gui.display.inventoryrules.ItemMetaMatch;
import jas.gui.display.inventoryrules.ItemSlotMatch;
import jas.gui.display.inventoryrules.ScrollableInventoryRules;
import jas.gui.display.resource.SimpleImageResource.GuiIconImageResource;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.DisplayUnit.VerticalAlignment;
import jas.gui.display.units.action.ReplaceAction;
import jas.gui.display.units.windows.DisplayUnitButton;
import jas.gui.display.units.windows.DisplayUnitTextBoard;
import jas.gui.display.units.windows.DisplayUnitTextField;
import jas.gui.display.units.windows.DisplayUnitToggle;
import jas.gui.display.units.windows.DisplayWindowMenu;
import jas.gui.display.units.windows.DisplayWindowScrollList;
import jas.gui.display.units.windows.DisplayUnitButton.Clicker;
import jas.gui.display.units.windows.button.AddScrollClick;
import jas.gui.display.units.windows.button.CloseClick;
import jas.gui.display.units.windows.button.MoveScrollElementToggle;
import jas.gui.display.units.windows.button.SetHideExpressionClick;
import jas.gui.display.units.windows.text.AnalogCounterPositionValidator;
import jas.gui.display.units.windows.text.DigitalCounterPositionValidator;
import jas.gui.display.units.windows.text.PositionTextValidator;
import jas.gui.display.units.windows.text.RegularTextValidator;
import jas.gui.display.units.windows.toggle.ToggleAnalogCounter;
import jas.gui.display.units.windows.toggle.ToggleDigitalCounter;
import jas.gui.display.units.windows.toggle.ToggleHorizAlign;
import jas.gui.display.units.windows.toggle.ToggleVertAlign;
import jas.gui.utilities.Coord;
import jas.gui.utilities.GsonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.gson.JsonObject;

public class DisplayUnitItem extends DisplayUnitCounter implements DisplayUnitCountable, DisplayUnitSettable {
    public static final String DISPLAY_ID = "DisplayUnitItem";
    public static final ResourceLocation countdown = new ResourceLocation(DefaultProps.guiKey, "countdown.png");

    private boolean displayOnHud;
    // User assigned name to item for display. Should only be used for display when neccessary and not be null.
    public String nickname;
    // Frequency to search player inventory for updated item statistics, most commonly quantity
    private int updateFrequency = 20;

    // Display ItemStack used when counting rules do not find an ItemStack
    private transient ItemStack missingDisplayStack;
    // Matching rules for Counting
    private InventoryRules countingRules;
    private HideExpression hidingRules;
    private TrackMode trackMode;

    // Information required to display
    private transient DisplayStats displayStats;
    private transient DisplayStats prevDisplayStat;

    private VerticalAlignment vertAlign = VerticalAlignment.CENTER_ABSO;
    private HorizontalAlignment horizAlign = HorizontalAlignment.CENTER_ABSO;

    public DisplayUnitItem() {
        super(new Coord(0, 0), true, true);
        displayOnHud = true;
        nickname = "";
        trackMode = TrackMode.QUANTITY;
        countingRules = new InventoryRules();
        countingRules.add(new ItemIdMatch("grass", true));
        this.hidingRules = new HideExpression().setExpression("(#{count}==0||#{count}>5)&&(#{unchanged}>60)");
        missingDisplayStack = new ItemStack(Blocks.dirt);
    }

    public DisplayUnitItem(Coord position, TrackMode mode, VerticalAlignment vertAlign, HorizontalAlignment horizAlign,
            String hideExpression, InventoryRule[] inventoryRules, ItemStack... missingDisplayStack) {
        super(position, true, true);
        displayOnHud = true;
        nickname = "";
        trackMode = mode;
        countingRules = new InventoryRules();
        for (InventoryRule rule : inventoryRules) {
            countingRules.add(rule);
        }
        this.vertAlign = vertAlign;
        this.horizAlign = horizAlign;
        this.hidingRules = new HideExpression().setExpression(hideExpression);
        this.missingDisplayStack = missingDisplayStack.length > 0 ? missingDisplayStack[0] : new ItemStack(Blocks.dirt);
    }

    /* Changes the quality that is being counted */
    public enum TrackMode {
        DURABILITY, QUANTITY, DURATION; // Duration not needed as PotionDisplayUnit will need to be seperate?
    }

    public static class DisplayStats {
        public final ItemStack stackToDisplay;
        public final int trackedCount;
        public final int maximumCount;

        public DisplayStats(ItemStack stackToDisplay, int trackedCount, int maximumCount) {
            this.stackToDisplay = stackToDisplay.copy();
            this.trackedCount = trackedCount;
            this.maximumCount = maximumCount;
        }
    }

    @Override
    public String getType() {
        return DISPLAY_ID;
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
    public void setVerticalAlignment(VerticalAlignment alignment) {
        vertAlign = alignment;
    }

    @Override
    public HorizontalAlignment getHorizontalAlignment() {
        return horizAlign;
    }

    @Override
    public void setHorizontalAlignment(HorizontalAlignment alignment) {
        horizAlign = alignment;
    }

    @Override
    public void onUpdate(Minecraft mc, int ticks) {
        if (ticks % updateFrequency == 0) {
            prevDisplayStat = displayStats;
            displayStats = calculateDisplayStats(mc);
            Integer count = displayStats != null ? displayStats.trackedCount : null;
            Integer prevCount = prevDisplayStat != null ? prevDisplayStat.trackedCount : null;
            hidingRules.update(count, prevCount, displayStats.maximumCount, updateFrequency);
            if (displayStats != null) {
                displayOnHud = !hidingRules.shouldHide();
            } else {
                displayOnHud = false;
            }
        }
    }

    private DisplayStats calculateDisplayStats(Minecraft mc) {
        ItemStack stackToDisplay = missingDisplayStack;
        int trackedCount = 0;
        boolean foundMatch = false;
        RULE_LOOP: for (InventoryRule rule : countingRules) {
            ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] == null) {
                    continue;
                }
                ItemStack itemStack = inventory[i];
                if (rule.isMatch(itemStack, i, false, mc.thePlayer.inventory.currentItem == i)) {
                    if (!foundMatch) {
                        stackToDisplay = itemStack.copy();
                        foundMatch = true;
                    }
                    trackedCount += countStack(itemStack);
                    if (!rule.allowMultipleMatches()) {
                        continue RULE_LOOP;
                    }
                }
            }

            inventory = mc.thePlayer.inventory.armorInventory;
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] == null) {
                    continue;
                }
                ItemStack itemStack = inventory[i];
                if (rule.isMatch(itemStack, i, true, mc.thePlayer.inventory.currentItem == i)) {
                    if (!foundMatch) {
                        stackToDisplay = itemStack.copy();
                        foundMatch = true;
                    }
                    trackedCount += countStack(itemStack);
                    if (!rule.allowMultipleMatches()) {
                        continue RULE_LOOP;
                    }
                }
            }
        }

        int maximumCount;
        if (trackMode == TrackMode.DURABILITY) {
            maximumCount = stackToDisplay.getMaxDamage();
        } else {
            maximumCount = 64;
        }
        return new DisplayStats(stackToDisplay, trackedCount, maximumCount);
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return displayOnHud;
    }

    @Override
    public void renderDisplay(Minecraft mc, Coord position) {
        DisplayStats displayStats = getDisplayInfo(mc);
        if (displayStats == null) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        float opacity = 1;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, opacity);
        RenderItem renderItem = new RenderItem();
        renderItem.zLevel = 200.0F;
        renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, displayStats.stackToDisplay,
                position.x, position.z);
        GL11.glDisable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        if (isAnalogEnabled()) {
            renderAnalogBar(mc, position, getAnalogOffset(), displayStats.trackedCount, displayStats.maximumCount);
        }

        if (isDigitalEnabled()) {
            renderCounterBar(mc, position, getDigitalOffset(), displayStats.trackedCount);
        }
    }

    public DisplayStats getDisplayInfo(Minecraft mc) {
        if (displayStats == null) {
            displayStats = calculateDisplayStats(mc);
        }
        return displayStats;
    }

    private int countStack(ItemStack stackToCount) {
        if (trackMode == TrackMode.DURABILITY) {
            int currentDamage = stackToCount.getItemDamage();
            int maxDamage = stackToCount.getItem().getMaxDamage();
            return maxDamage - currentDamage;
        } else {
            return stackToCount.stackSize;
        }
    }

    /**
     * Helper method that Maps the real value provided (representing damage typically) to a different scale (typically
     * resolution, 16)
     * 
     * @param realValue represents value in Set 1
     * @param realMax is the max value in set 1, min value is assumed zero.
     * @param scaleMax is the max value in set 2, min value is assumed zero.
     * @return realValue in set 2
     */
    protected int mapValueToScale(int realValue, int realMax, int scaleMax) {
        return realValue > realMax ? scaleMax : realValue < 0 ? 0 : (int) (((float) realValue) / realMax * scaleMax);
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker hoverChecker) {
        if (hoverAction == HoverAction.HOVER) {
            hoverChecker.markHoverFound();
        }
    }

    @Override
    public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
        if (action == MouseAction.CLICK && actionData[0] == 1 && DisplayHelper.isCursorOverDisplay(localMouse, this)) {
            DisplayWindowMenu menu = new DisplayWindowMenu(getOffset(), getHorizontalAlignment(),
                    getVerticalAlignment());
            /* Nickname textField */
            menu.addElement(new DisplayUnitTextField(new Coord(0, 4), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 13, new RegularTextValidator() {
                        private DisplayUnitItem display;

                        public RegularTextValidator init(DisplayUnitItem display) {
                            this.display = display;
                            return this;
                        }

                        @Override
                        public void setString(String text) {
                            display.nickname = text;
                        }

                        @Override
                        public String getString() {
                            return display.nickname;
                        }
                    }.init(this)));
            /* Generic DisplayUnitEditable Settings */
            menu.addElement(new DisplayUnitTextBoard(new Coord(0, 16), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Position").setBackgroundImage(null));
            menu.addElement(new DisplayUnitTextField(new Coord(-17, 29), new Coord(32, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 5, new PositionTextValidator(this, true)));
            menu.addElement(new DisplayUnitTextField(new Coord(+18, 29), new Coord(32, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 5, new PositionTextValidator(this, false)));
            menu.addElement(new DisplayUnitTextBoard(new Coord(0, 40), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Alignment").setBackgroundImage(null));
            menu.addElement(new DisplayUnitToggle(new Coord(-22, 54), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleHorizAlign(this, HorizontalAlignment.LEFT_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(111, 2), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+00, 54), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleHorizAlign(this, HorizontalAlignment.CENTER_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(129, 2), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+22, 54), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleHorizAlign(this, HorizontalAlignment.RIGHT_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(147, 2), new Coord(13, 16))));

            menu.addElement(new DisplayUnitToggle(new Coord(-22, 75), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleVertAlign(this, VerticalAlignment.TOP_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(111, 23), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+00, 75), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleVertAlign(this, VerticalAlignment.CENTER_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(129, 23), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+22, 75), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleVertAlign(this, VerticalAlignment.BOTTOM_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(147, 23), new Coord(13, 16))));

            /* Analog Bar Settings */
            menu.addElement(new DisplayUnitTextBoard(new Coord(8, 93), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Analog").setBackgroundImage(null));
            menu.addElement(new DisplayUnitToggle(new Coord(-24, 100), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleAnalogCounter(this))
                    .setIconImageResource(new GuiIconImageResource(new Coord(129, 44), new Coord(13, 16))));
            menu.addElement(new DisplayUnitTextField(new Coord(-2, 106), new Coord(22, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 3, new AnalogCounterPositionValidator(this, true)));
            menu.addElement(new DisplayUnitTextField(new Coord(21, 106), new Coord(22, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 3, new AnalogCounterPositionValidator(this, false)));

            /* Digital Counter Settings */
            menu.addElement(new DisplayUnitTextBoard(new Coord(8, 118), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Digital").setBackgroundImage(null));
            menu.addElement(new DisplayUnitToggle(new Coord(-24, 125), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleDigitalCounter(this))
                    .setIconImageResource(new GuiIconImageResource(new Coord(111, 44), new Coord(13, 16))));
            menu.addElement(new DisplayUnitTextField(new Coord(-2, 131), new Coord(22, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 3, new DigitalCounterPositionValidator(this, true)));
            menu.addElement(new DisplayUnitTextField(new Coord(21, 131), new Coord(22, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 3, new DigitalCounterPositionValidator(this, false)));

            /* Open Inventory Editor */
            menu.addElement(new DisplayUnitButton(new Coord(0, 149), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new Clicker() {
                        private InventoryRules rules;
                        private VerticalAlignment parentVert;
                        private HorizontalAlignment parentHorz;

                        private Clicker init(InventoryRules rules, VerticalAlignment parentVert,
                                HorizontalAlignment parentHorz) {
                            this.rules = rules;
                            this.parentVert = parentVert;
                            this.parentHorz = parentHorz;
                            return this;
                        }

                        @Override
                        public ActionResult onClick() {
                            return ActionResult.SIMPLEACTION;
                        }

                        @Override
                        public ActionResult onRelease() {
                            ScrollableInventoryRules scrollable = new ScrollableInventoryRules(rules);
                            DisplayWindowScrollList<InventoryRule> scrollList = new DisplayWindowScrollList<InventoryRule>(
                                    new Coord(0, 0), new Coord(140, 200), 25, parentVert, parentHorz, scrollable);
                            // Add InventoryRules
                            scrollList.addElement(new DisplayUnitButton(new Coord(2, 2), new Coord(20, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new AddScrollClick<InventoryRule, ScrollableInventoryRules>(scrollable) {
                                        @Override
                                        public void performScrollAddition(ScrollableInventoryRules container) {
                                            container.addElement(new ItemHandMatch());
                                        }
                                    }).setIconImageResource(new GuiIconImageResource(new Coord(183, 65), new Coord(13,
                                    16))));
                            scrollList.addElement(new DisplayUnitButton(new Coord(23, 2), new Coord(20, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new AddScrollClick<InventoryRule, ScrollableInventoryRules>(scrollable) {
                                        @Override
                                        public void performScrollAddition(ScrollableInventoryRules container) {
                                            container.addElement(new ItemIdMatch("grass", true));
                                        }
                                    }).setIconImageResource(new GuiIconImageResource(new Coord(201, 65), new Coord(13,
                                    16))));
                            scrollList.addElement(new DisplayUnitButton(new Coord(44, 2), new Coord(20, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new AddScrollClick<InventoryRule, ScrollableInventoryRules>(scrollable) {
                                        @Override
                                        public void performScrollAddition(ScrollableInventoryRules container) {
                                            container.addElement(new ItemMetaMatch("grass", 0, true));
                                        }
                                    }).setIconImageResource(new GuiIconImageResource(new Coord(219, 65), new Coord(13,
                                    16))));
                            scrollList.addElement(new DisplayUnitButton(new Coord(65, 2), new Coord(20, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new AddScrollClick<InventoryRule, ScrollableInventoryRules>(scrollable) {
                                        @Override
                                        public void performScrollAddition(ScrollableInventoryRules container) {
                                            container.addElement(new ItemSlotMatch(0, false));
                                        }
                                    }).setIconImageResource(new GuiIconImageResource(new Coord(111, 87), new Coord(13,
                                    16))));

                            // Move Up/Down buttons
                            scrollList.addElement(new DisplayUnitToggle(new Coord(-2, 2), new Coord(20, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.RIGHT_ABSO,
                                    new MoveScrollElementToggle<InventoryRule>(scrollable, 1))
                                    .setIconImageResource(new GuiIconImageResource(new Coord(165, 66),
                                            new Coord(12, 15))));
                            scrollList.addElement(new DisplayUnitToggle(new Coord(-23, 2), new Coord(20, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.RIGHT_ABSO,
                                    new MoveScrollElementToggle<InventoryRule>(scrollable, -1))
                                    .setIconImageResource(new GuiIconImageResource(new Coord(147, 66),
                                            new Coord(12, 15))));
                            // Close Button
                            scrollList.addElement(new DisplayUnitButton(new Coord(0, -2), new Coord(50, 20),
                                    VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.CENTER_ABSO, new CloseClick(
                                            scrollList), "Close"));
                            return new ReplaceAction(scrollList, true);
                        }
                    }.init(countingRules, getVerticalAlignment(), getHorizontalAlignment()), "Count Rules"));
            /* Open HideRules Editor */
            menu.addElement(new DisplayUnitButton(new Coord(0, 164), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new Clicker() {
                        private HideExpression rules;
                        private VerticalAlignment parentVert;
                        private HorizontalAlignment parentHorz;

                        private Clicker init(HideExpression rules, VerticalAlignment parentVert,
                                HorizontalAlignment parentHorz) {
                            this.rules = rules;
                            this.parentVert = parentVert;
                            this.parentHorz = parentHorz;
                            return this;
                        }

                        @Override
                        public ActionResult onClick() {
                            return ActionResult.SIMPLEACTION;
                        }

                        @Override
                        public ActionResult onRelease() {
                            DisplayWindowMenu menu = new DisplayWindowMenu(new Coord(0, 0), parentHorz, parentVert)
                                    .forceSize(new Coord(243, 140));
                            menu.addElement(new DisplayUnitTextField(new Coord(3, -25), new Coord(237, 16),
                                    VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.LEFT_ABSO, 200,
                                    new RegularTextValidator() {

                                        public boolean isStringValid(String text) {
                                            if (super.isStringValid(text)) {
                                                return rules.isExpressionValid(text);
                                            } else {
                                                return false;
                                            }
                                        };

                                        @Override
                                        public void setString(String text) {
                                            rules.setExpression(text);
                                        }

                                        @Override
                                        public String getString() {
                                            return rules.getExpression();
                                        }
                                    }));

                            menu.addElement(new DisplayUnitTextBoard(new Coord(0, 3), VerticalAlignment.TOP_ABSO,
                                    HorizontalAlignment.LEFT_ABSO, "Select when to Hide or write your own.",
                                    "   #{count} the tracked quantity", "   #{prevCount} the previous count",
                                    "   #{unchanged} ticks count hasn't changed").setBackgroundImage(null));

                            menu.addElement(new DisplayUnitButton(new Coord(3, 45), new Coord(57, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules, " "), "Don't"));
                            menu.addElement(new DisplayUnitButton(new Coord(60, 45), new Coord(57, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules, "#{count}==0"), "Empty"));
                            menu.addElement(new DisplayUnitButton(new Coord(117, 45), new Coord(57, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules, "(#{count} * 18 / #{maxCount}) > 3"), "High"));
                            menu.addElement(new DisplayUnitButton(new Coord(174, 45), new Coord(64, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules, "(#{count} * 18 / #{maxCount}) > 6"), "Higher"));

                            menu.addElement(new DisplayUnitButton(new Coord(3, 68), new Coord(57, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules, "#{unchanged}>60"), "3s Same"));
                            menu.addElement(new DisplayUnitButton(new Coord(60, 68), new Coord(57, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules, "#{unchanged}>200"), "10s Same"));
                            menu.addElement(new DisplayUnitButton(new Coord(117, 68), new Coord(57, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules,
                                            "#{unchanged}>60&&(#{count} * 18 / #{maxCount}) > 3"), "High Same"));
                            menu.addElement(new DisplayUnitButton(new Coord(174, 68), new Coord(64, 20),
                                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.LEFT_ABSO,
                                    new SetHideExpressionClick(rules,
                                            "#{unchanged}>60&&(#{count} * 18 / #{maxCount}) > 6"), "Higher Same"));

                            menu.addElement(new DisplayUnitButton(new Coord(0, -3), new Coord(50, 20),
                                    VerticalAlignment.BOTTOM_ABSO, HorizontalAlignment.CENTER_ABSO,
                                    new CloseClick(menu), "Close"));
                            return new ReplaceAction(menu, true);
                        }
                    }.init(hidingRules, getVerticalAlignment(), getHorizontalAlignment()), "Hide Rules"));
            menu.addElement(new DisplayUnitButton(new Coord(0, 180), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new CloseClick(menu), "Close"));

            return new ReplaceAction(menu, true);
        }

        return super.mouseAction(localMouse, action, actionData);
    }

    @Override
    public ActionResult keyTyped(char eventCharacter, int eventKey) {
        return super.keyTyped(eventCharacter, eventKey);
    }

    @Override
    public void saveCustomData(JsonObject jsonObject) {
        jsonObject.addProperty("NICKNAME", nickname);
        super.saveCustomData(jsonObject);
        jsonObject.addProperty("TRACKMODE", trackMode.toString());
        jsonObject.addProperty("UPDATE_FREQUENCY", updateFrequency);
        jsonObject.addProperty("VERTICAL_ALIGN", vertAlign.toString());
        jsonObject.addProperty("HORIZONTAL_ALIGN", horizAlign.toString());
        jsonObject.addProperty("HIDE_EXPRESSION", hidingRules.getExpression());
        countingRules.saveCustomData(jsonObject);

        JsonObject missingStackObject = new JsonObject();
        missingStackObject.addProperty("NAME", Item.itemRegistry.getNameForObject(missingDisplayStack.getItem()));
        missingStackObject.addProperty("DAMAGE", missingDisplayStack.getItemDamage());
        jsonObject.add("MISSING_DISPLAY_STACK", missingStackObject);
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
        nickname = GsonHelper.getMemberOrDefault(customData, "NICKNAME", "");
        super.loadCustomData(factory, customData);
        String parsedTrack = GsonHelper.getMemberOrDefault(customData, "TRACKMODE", TrackMode.QUANTITY.toString())
                .trim();
        trackMode = TrackMode.QUANTITY.toString().equalsIgnoreCase(parsedTrack) ? TrackMode.QUANTITY
                : TrackMode.DURABILITY;
        updateFrequency = GsonHelper.getMemberOrDefault(customData, "UPDATE_FREQUENCY", 20);

        String verAl = GsonHelper.getMemberOrDefault(customData, "VERTICAL_ALIGN", "").trim();
        for (VerticalAlignment verticalAlignment : VerticalAlignment.values()) {
            if (verAl.trim().toUpperCase().equals(verticalAlignment.toString())) {
                vertAlign = verticalAlignment;
            }
        }

        String horAl = GsonHelper.getMemberOrDefault(customData, "HORIZONTAL_ALIGN", "").trim();
        for (HorizontalAlignment horizontalAlignment : HorizontalAlignment.values()) {
            if (horAl.trim().toUpperCase().equals(horizontalAlignment.toString())) {
                horizAlign = horizontalAlignment;
            }
        }
        hidingRules.setExpression(GsonHelper.getMemberOrDefault(customData, "HIDE_EXPRESSION", ""));
        countingRules.loadCustomData(customData);

        JsonObject missingStackObject = customData.get("MISSING_DISPLAY_STACK").getAsJsonObject();
        int damage = GsonHelper.getMemberOrDefault(customData, "DAMAGE", 0);
        Item item = (Item) Item.itemRegistry.getObject(GsonHelper.getMemberOrDefault(customData, "NAME", ""));
        if (item != null && damage >= 0) {
            missingDisplayStack = new ItemStack(item, 1, damage);
        }
    }

    @Override
    public int getCount() {
        return displayStats != null ? displayStats.trackedCount : 0;
    }
}
