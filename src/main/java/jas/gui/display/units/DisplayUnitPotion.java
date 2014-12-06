package jas.gui.display.units;

import jas.gui.DefaultProps;
import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.hiderules.HideExpression;
import jas.gui.display.resource.SimpleImageResource.GuiIconImageResource;
import jas.gui.display.units.DisplayUnit.HorizontalAlignment;
import jas.gui.display.units.DisplayUnit.VerticalAlignment;
import jas.gui.display.units.DisplayUnitItem.TrackMode;
import jas.gui.display.units.action.ReplaceAction;
import jas.gui.display.units.windows.DisplayUnitButton;
import jas.gui.display.units.windows.DisplayUnitTextBoard;
import jas.gui.display.units.windows.DisplayUnitTextField;
import jas.gui.display.units.windows.DisplayUnitToggle;
import jas.gui.display.units.windows.DisplayWindowMenu;
import jas.gui.display.units.windows.DisplayUnitButton.Clicker;
import jas.gui.display.units.windows.button.CloseClick;
import jas.gui.display.units.windows.button.SetHideExpressionClick;
import jas.gui.display.units.windows.text.AnalogCounterPositionValidator;
import jas.gui.display.units.windows.text.DigitalCounterPositionValidator;
import jas.gui.display.units.windows.text.PositionTextValidator;
import jas.gui.display.units.windows.text.RegularTextValidator;
import jas.gui.display.units.windows.text.ValidatorBoundedInt;
import jas.gui.display.units.windows.toggle.ToggleAnalogCounter;
import jas.gui.display.units.windows.toggle.ToggleDigitalCounter;
import jas.gui.display.units.windows.toggle.ToggleHorizAlign;
import jas.gui.display.units.windows.toggle.ToggleVertAlign;
import jas.gui.utilities.Coord;
import jas.gui.utilities.GsonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonObject;

public class DisplayUnitPotion extends DisplayUnitCounter implements DisplayUnitCountable, DisplayUnitSettable {
    public static final String DISPLAY_ID = "DisplayUnitPotion";
    public static final ResourceLocation inventory = new ResourceLocation("textures/gui/container/inventory.png");
    public static final ResourceLocation countdown = new ResourceLocation(DefaultProps.guiKey, "countdown.png");

    // User assigned name to item for display. Should only be used for display when necessary and not be null.
    public String nickname = "";
    // Frequency to search player inventory for updated item statistics, most commonly quantity
    public int updateFrequency;
    public int trackedPotion; // Id of Potion to be tracked

    public int trackedCount; // Value of tracked property, always duration for Potions
    private int prevTrackedCount;
    public int maxAnalogDuration; // in Ticks

    private transient boolean shouldDisplay;
    private HideExpression hidingRules;

    public HideExpression getHideRules() {
        return hidingRules;
    }

    private VerticalAlignment vertAlign = VerticalAlignment.CENTER_ABSO;
    private HorizontalAlignment horizAlign = HorizontalAlignment.CENTER_ABSO;

    public DisplayUnitPotion() {
        super(new Coord(0, 0), true, false);
        updateFrequency = 20;
        trackedPotion = 1;// Defaults to Speed, choice is arbitrary
        maxAnalogDuration = 60 * 20;
        this.hidingRules = new HideExpression();
    }

    public DisplayUnitPotion(Coord offset, int updateFrequency, int trackedPotion, String hidingRules) {
        super(offset, true, false);
        this.updateFrequency = updateFrequency;
        this.trackedPotion = trackedPotion;
        this.maxAnalogDuration = 60 * 20;
        this.hidingRules = new HideExpression().setExpression(hidingRules);
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
            Potion potion = Potion.potionTypes[trackedPotion];
            mc.thePlayer.isPotionActive(potion);
            PotionEffect effect = mc.thePlayer.getActivePotionEffect(potion);
            trackedCount = effect != null ? effect.getDuration() : 0;
            hidingRules.update(trackedCount, prevTrackedCount, maxAnalogDuration, updateFrequency);
            this.prevTrackedCount = trackedCount;
            shouldDisplay = Potion.potionTypes[trackedPotion] != null && !hidingRules.shouldHide();
        }
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return shouldDisplay;
    }

    @Override
    public void renderDisplay(Minecraft mc, Coord position) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(inventory);
        int iconIndex = Potion.potionTypes[trackedPotion].getStatusIconIndex();
        int iconXCoord = 0 + iconIndex % 8 * 18;
        int iconYCoord = 198 + iconIndex / 8 * 18;
        DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, -9.0f, position.x, position.z, iconXCoord,
                iconYCoord, 18, 18);

        if (isAnalogEnabled()) {
            renderAnalogBar(mc, position, getAnalogOffset(), trackedCount, maxAnalogDuration);
        }

        if (isDigitalEnabled()) {
            renderCounterBar(mc, position, getDigitalOffset(), trackedCount);
        }
    }

    /**
     * Used to Draw Analog Bar.
     * 
     * @param mc The Minecraft Instance
     * @param fontRenderer The fontRenderer
     * @param centerOfDisplay The Center Position where the bar is offset From.
     * @param analogValue The value representing how full the Bar is
     * @param analogMax The value that represents the width of the full bar.
     */
    protected void renderCounterBar(Minecraft mc, Coord centerOfDisplay, Coord offSet, int counterAmount) {
        int totalSeconds = counterAmount / 20;

        /* Get Duration in Seconds */
        int seconds = totalSeconds % 60;
        /* Get Duration in Minutes */
        int minutes = (totalSeconds / 60) % 60;
        String formattedTime;
        if (seconds < 10) {
            formattedTime = Integer.toString(minutes);
        } else if (minutes == 0) {
            formattedTime = String.format("%02d", seconds);
        } else {
            formattedTime = minutes + ":" + String.format("%02d", seconds);
        }

        String displayAmount = Integer.toString(counterAmount);
        // 8 is constant chosen by testing to keep the displaystring roughly center. It just works.
        mc.fontRenderer.drawString(formattedTime,
                centerOfDisplay.x + (8 - mc.fontRenderer.getStringWidth(formattedTime) / 2) + offSet.x,
                centerOfDisplay.z + offSet.z, textDisplayColor);
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
            // Nickname
            menu.addElement(new DisplayUnitTextField(new Coord(0, 4), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 13, new RegularTextValidator() {
                        private DisplayUnitPotion display;

                        public RegularTextValidator init(DisplayUnitPotion display) {
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

            /* Alignment Setting */
            menu.addElement(new DisplayUnitTextBoard(new Coord(0, 55), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Alignment").setBackgroundImage(null));
            menu.addElement(new DisplayUnitToggle(new Coord(-22, 68), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleHorizAlign(this, HorizontalAlignment.LEFT_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(111, 2), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+00, 68), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleHorizAlign(this, HorizontalAlignment.CENTER_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(129, 2), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+22, 68), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleHorizAlign(this, HorizontalAlignment.RIGHT_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(147, 2), new Coord(13, 16))));

            menu.addElement(new DisplayUnitToggle(new Coord(-22, 89), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleVertAlign(this, VerticalAlignment.TOP_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(111, 23), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+00, 89), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleVertAlign(this, VerticalAlignment.CENTER_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(129, 23), new Coord(13, 16))));
            menu.addElement(new DisplayUnitToggle(new Coord(+22, 89), new Coord(20, 20), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, new ToggleVertAlign(this, VerticalAlignment.BOTTOM_ABSO))
                    .setIconImageResource(new GuiIconImageResource(new Coord(147, 23), new Coord(13, 16))));

            /* Analog Bar Settings */
            menu.addElement(new DisplayUnitTextBoard(new Coord(8, 93 + 13), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Analog").setBackgroundImage(null));
            menu.addElement(new DisplayUnitToggle(new Coord(-24, 100 + 13), new Coord(20, 20),
                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.CENTER_ABSO, new ToggleAnalogCounter(this))
                    .setIconImageResource(new GuiIconImageResource(new Coord(129, 44), new Coord(13, 16))));
            menu.addElement(new DisplayUnitTextField(new Coord(-2, 106 + 13), new Coord(22, 15),
                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.CENTER_ABSO, 3, new AnalogCounterPositionValidator(
                            this, true)));
            menu.addElement(new DisplayUnitTextField(new Coord(21, 106 + 13), new Coord(22, 15),
                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.CENTER_ABSO, 3, new AnalogCounterPositionValidator(
                            this, false)));

            /* Digital Counter Settings */
            menu.addElement(new DisplayUnitTextBoard(new Coord(8, 118 + 13), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Digital").setBackgroundImage(null));
            menu.addElement(new DisplayUnitToggle(new Coord(-24, 125 + 13), new Coord(20, 20),
                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.CENTER_ABSO, new ToggleDigitalCounter(this))
                    .setIconImageResource(new GuiIconImageResource(new Coord(111, 44), new Coord(13, 16))));
            menu.addElement(new DisplayUnitTextField(new Coord(-2, 131 + 13), new Coord(22, 15),
                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.CENTER_ABSO, 3,
                    new DigitalCounterPositionValidator(this, true)));
            menu.addElement(new DisplayUnitTextField(new Coord(21, 131 + 13), new Coord(22, 15),
                    VerticalAlignment.TOP_ABSO, HorizontalAlignment.CENTER_ABSO, 3,
                    new DigitalCounterPositionValidator(this, false)));

            /* Potion Tracking Setting */
            menu.addElement(new DisplayUnitTextBoard(new Coord(-10, 44), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, "Track PID:").setBackgroundImage(null));
            menu.addElement(new DisplayUnitTextField(new Coord(26, 44), new Coord(18, 15), VerticalAlignment.TOP_ABSO,
                    HorizontalAlignment.CENTER_ABSO, 2, new ValidatorBoundedInt(0, Potion.potionTypes.length - 1) {

                        private DisplayUnitPotion display;

                        public ValidatorBoundedInt init(DisplayUnitPotion display) {
                            this.display = display;
                            return this;
                        }

                        @Override
                        public boolean isStringValid(String text) {
                            if (!super.isStringValid(text)) {
                                return false;
                            }
                            return Potion.potionTypes[Integer.parseInt(text)] != null;
                        }

                        public void setInt(int textValue) {
                            display.trackedPotion = textValue;
                        }

                        @Override
                        public int getValue() {
                            return display.trackedPotion;
                        }
                    }.init(this)));

            /* Open HideRules Editor */
            menu.addElement(new DisplayUnitButton(new Coord(0, 160), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
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

            menu.addElement(new DisplayUnitButton(new Coord(0, 176), new Coord(80, 15), VerticalAlignment.TOP_ABSO,
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
        jsonObject.addProperty("TRACKED_POTION", trackedPotion);
        jsonObject.addProperty("UPDATE_FREQUENCY", updateFrequency);
        jsonObject.addProperty("MAX_ANALOG_TICKS", maxAnalogDuration);
        jsonObject.addProperty("VERTICAL_ALIGN", vertAlign.toString());
        jsonObject.addProperty("HORIZONTAL_ALIGN", horizAlign.toString());
        jsonObject.addProperty("HIDE_EXPRESSION", hidingRules.getExpression());
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
        nickname = GsonHelper.getMemberOrDefault(customData, "NICKNAME", "");
        super.loadCustomData(factory, customData);
        trackedPotion = GsonHelper.getMemberOrDefault(customData, "TRACKED_POTION", 1);
        updateFrequency = GsonHelper.getMemberOrDefault(customData, "UPDATE_FREQUENCY", 20);
        maxAnalogDuration = GsonHelper.getMemberOrDefault(customData, "MAX_ANALOG_TICKS", 60 * 20);

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
    }

    @Override
    public int getCount() {
        return trackedCount;
    }
}
