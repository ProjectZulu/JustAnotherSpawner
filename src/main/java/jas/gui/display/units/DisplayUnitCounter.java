package jas.gui.display.units;


import jas.gui.DefaultProps;
import jas.gui.GuiLog;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.utilities.Coord;
import jas.gui.utilities.GsonHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public abstract class DisplayUnitCounter extends DisplayUnitMoveable implements DisplayUnitCountable {
    private static final ResourceLocation inventory = new ResourceLocation("textures/gui/container/inventory.png");
    private static final ResourceLocation countdown = new ResourceLocation(DefaultProps.guiKey, "countdown.png");

    private boolean displayAnalogBar;
    private boolean displayNumericCounter;
    private Coord analogOffset;
    private Coord digitalOffset;
    public int textDisplayColor;

    public DisplayUnitCounter(Coord offset, boolean displayAnalogBar, boolean displayNumericCounter) {
        super(offset);
        this.displayAnalogBar = displayAnalogBar;
        this.displayNumericCounter = displayNumericCounter;
        this.analogOffset = new Coord(1, 18);
        this.digitalOffset = new Coord(1, 0);
        this.textDisplayColor = 1030655;
    }

    @Override
    public boolean isAnalogEnabled() {
        return displayAnalogBar;
    }

    @Override
    public void enableAnalogDisplay(boolean enable) {
        displayAnalogBar = enable;
    }

    @Override
    public void setAnalogOffset(Coord coord) {
        analogOffset = coord;
    }

    @Override
    public Coord getAnalogOffset() {
        return analogOffset;
    }

    @Override
    public boolean isDigitalEnabled() {
        return displayNumericCounter;
    }

    @Override
    public void enableDigitalCounter(boolean enable) {
        displayNumericCounter = enable;
    }

    @Override
    public void setDigitalOffset(Coord coord) {
        digitalOffset = coord;
    }

    @Override
    public Coord getDigitalOffset() {
        return digitalOffset;
    }

    @Override
    public Coord getSize() {
        return new Coord(largestXDistance(getOffset().x, getAnalogOffset().x, getDigitalOffset().x) + 2,
                largestZDistance(getOffset().z, getAnalogOffset().z, getDigitalOffset().z) + 2);
    }

    private int largestXDistance(int iconCoord, int anaOffset, int digOffset) {
        // icon is 16x16 and its base is origin (0,0) for analog and digital offsets
        int farEdgePointAnalog = anaOffset >= 0 ? anaOffset + 16 : anaOffset;
        int farEdgePointDigit = digOffset >= 0 ? digOffset + 8 : digOffset;
        if (farEdgePointAnalog >= 0 && farEdgePointDigit >= 0) {
            return Math.max(Math.max(farEdgePointAnalog, farEdgePointDigit), 16);
        } else if (farEdgePointAnalog >= 0) {
            return Math.max(Math.max(farEdgePointAnalog, farEdgePointAnalog - farEdgePointDigit),
                    16 - farEdgePointDigit);
        } else if (farEdgePointDigit >= 0) {
            return Math.max(Math.max(farEdgePointDigit, farEdgePointDigit - farEdgePointAnalog),
                    16 - farEdgePointAnalog);
        } else {
            // Else Case both are < 0
            return Math.max(16 - farEdgePointAnalog, 16 - farEdgePointDigit);
        }
    }

    private int largestZDistance(int iconCoord, int anaOffset, int digOffset) {
        // icon is 16x16 and its base is origin (0,0) for analog and digital offsets
        int farEdgePointAnalog = anaOffset >= 0 ? anaOffset + 4 : anaOffset;
        int farEdgePointDigit = digOffset >= 0 ? digOffset + 8 : digOffset;
        if (farEdgePointAnalog >= 0 && farEdgePointDigit >= 0) {
            return Math.max(Math.max(farEdgePointAnalog, farEdgePointDigit), 16);
        } else if (farEdgePointAnalog >= 0) {
            return Math.max(Math.max(farEdgePointAnalog, farEdgePointAnalog - farEdgePointDigit),
                    16 - farEdgePointDigit);
        } else if (farEdgePointDigit >= 0) {
            return Math.max(Math.max(farEdgePointDigit, farEdgePointDigit - farEdgePointAnalog),
                    16 - farEdgePointAnalog);
        } else {
            // Else Case both are < 0
            return Math.max(16 - farEdgePointAnalog, 16 - farEdgePointDigit);
        }
    }

    /**
     * Used to Draw Analog Bar.
     * 
     * @param mc The Minecraft Instance
     * @param centerOfDisplay The Center Position where the bar needs to be offset From.
     * @param analogValue The value representing how full the Bar is
     * @param analogMax The value that represents the width of the full bar.
     */
    protected void renderAnalogBar(Minecraft mc, Coord centerOfDisplay, Coord offSet, int analogValue, int analogMax) {
        mc.renderEngine.bindTexture(countdown);
        int scaledValue = scaleAnalogizeValue(analogValue, analogMax);
        DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, 10.0f, centerOfDisplay.x + offSet.x,
                centerOfDisplay.z + offSet.z, 0, 0, 16, 3);
        if (scaledValue > 9) {
            DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, 10.0f, centerOfDisplay.x + offSet.x,
                    centerOfDisplay.z + offSet.z, 0, 3, scaledValue, 3);
        } else if (scaledValue > 4) {
            DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, 10.0f, centerOfDisplay.x + offSet.x,
                    centerOfDisplay.z + offSet.z, 0, 6, scaledValue, 3);
        } else {
            DisplayRenderHelper.drawTexturedModalRect(Tessellator.instance, 10.0f, centerOfDisplay.x + offSet.x,
                    centerOfDisplay.z + offSet.z, 0, 9, scaledValue, 3);
        }
    }

    /**
     * Scale a tracked value from range [0-analogMax] to fit the display bars resolution of [0-16]
     */
    private int scaleAnalogizeValue(int analogValue, int analogMax) {
        if (analogValue > analogMax) {
            analogValue = analogMax;
        }
        if (analogValue < 0) {
            analogValue = 0;
        }
        return (int) ((float) (analogValue) / (float) (analogMax) * 18);
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
        String displayAmount = Integer.toString(counterAmount);
        switch (getHorizontalAlignment()) {
        case CENTER_ABSO:
            mc.fontRenderer.drawString(displayAmount,
                    centerOfDisplay.x + 8 - mc.fontRenderer.getStringWidth(displayAmount) / 2 + offSet.x,
                    centerOfDisplay.z - offSet.z, textDisplayColor);
            break;
        case LEFT_ABSO:
            mc.fontRenderer.drawString(displayAmount, centerOfDisplay.x + offSet.x, centerOfDisplay.z - offSet.z,
                    textDisplayColor);
            break;
        case RIGHT_ABSO:
            mc.fontRenderer.drawString(displayAmount, centerOfDisplay.x - mc.fontRenderer.getStringWidth(displayAmount)
                    + offSet.x, centerOfDisplay.z - offSet.z, textDisplayColor);
            break;
        }
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
        super.loadCustomData(factory, customData);
        textDisplayColor = customData.get("TEXT_COLOR").getAsInt();

        JsonObject analogObj = GsonHelper.getMemberOrDefault(customData, "ANALOG_BAR", new JsonObject());
        displayAnalogBar = GsonHelper.getMemberOrDefault(analogObj, "IS_ENABLED", true);
        analogOffset = parseCoord(GsonHelper.getMemberOrDefault(analogObj, "OFFSET", ""), new Coord(0, 0));

        JsonObject digitalObj = GsonHelper.getMemberOrDefault(customData, "DIGITAL_BAR", new JsonObject());
        displayNumericCounter = GsonHelper.getMemberOrDefault(digitalObj, "IS_ENABLED", true);
        digitalOffset = parseCoord(GsonHelper.getMemberOrDefault(digitalObj, "OFFSET", ""), new Coord(0, 8));
    }

    private Coord parseCoord(String stringForm, Coord defaultCoord) {
        String[] parts = stringForm.split(",");
        if (parts.length == 2) {
            try {
                int xCoord = Integer.parseInt(parts[0]);
                int zCoord = Integer.parseInt(parts[1]);
                return new Coord(xCoord, zCoord);
            } catch (NumberFormatException e) {
                GuiLog.log().info("Error parsing coordinate string %s. Will be replaced by %s", stringForm, defaultCoord);
            }
        }
        return defaultCoord;
    }

    @Override
    public void saveCustomData(JsonObject jsonObject) {
        super.saveCustomData(jsonObject);

        jsonObject.addProperty("TEXT_COLOR", textDisplayColor);
        JsonObject analogObj = new JsonObject();
        analogObj.addProperty("IS_ENABLED", displayAnalogBar);
        analogObj.addProperty("OFFSET", analogOffset.x + "," + analogOffset.z);
        jsonObject.add("ANALOG_BAR", analogObj);

        JsonObject digitalObj = new JsonObject();
        digitalObj.addProperty("IS_ENABLED", displayNumericCounter);
        digitalObj.addProperty("OFFSET", digitalOffset.x + "," + digitalOffset.z);
        jsonObject.add("DIGITAL_BAR", digitalObj);

    }
}
