package jas.gui.display.units.windows;

import jas.gui.display.DisplayHelper;
import jas.gui.display.DisplayRenderHelper;
import jas.gui.display.DisplayUnitFactory;
import jas.gui.display.units.DisplayUnit;
import jas.gui.display.units.DisplayUnit.HoverAction;
import jas.gui.display.units.DisplayUnit.HoverTracker;
import jas.gui.display.units.DisplayUnit.ActionResult.SimpleAction;
import jas.gui.utilities.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.gson.JsonObject;

/**
 * Basic text field with wrapping text
 */
public class DisplayUnitTextField implements DisplayUnit {
    public static final ResourceLocation guiButton = new ResourceLocation("mosi", "buttongui.png");

    public static final String DISPLAY_ID = "DisplayUnitTextField";

    private Coord offset;
    private Coord size;
    private VerticalAlignment vertAlign;
    private HorizontalAlignment horizAlign;
    private boolean isSelected = false;
    private boolean isMouseOver = false;

    /** Text that will need to be displayed, shouldn't be set directly use {@link #setText() or {@link #writeText()} */
    private String displayText;
    private Validator textValidator;

    /** The current character index that should be used as start of the rendered text. */
    private int lineScrollOffset;
    /** opposite end of cursorPosition when selecting multiple character, is otherwise identical to cursorPosition */
    private int cursorPosition;
    /** opposite end of cursorPosition when selecting multiple character, is otherwise identical to cursorPosition */
    private int selectionEnd;
    /** Maximum length allowed for displayText */
    private int maxStringLength;
    /*
     * Previous entry grabbed from validator.getString used in the update loop to check if the property has changed from
     * an external source which should override current text
     */
    private String prevEntry;
    /* Default entry grabbed when enabling box, assumed to be valid (as it was already assigned) */
    private String defaultEntry;
    /* Used to allow text entry cursor to flash in/out */
    private int cursorCounter = 0;

    /**
     * Validates input, validation is done per character.
     * 
     * The entire entry validation {@link isStringValid} is called when the box loses focus and allows for a
     * backup/default value to be provided to corrent it.
     * 
     * i.e. Consider a text box representing position. The default location could be saved when the box is selected.
     * Upon deselection, if the position was outside the screen bounds the saved position could be supplemented.
     */
    public static interface Validator {
        /* Validate if the character would be a valid addition */
        public abstract boolean isCharacterValid(char eventCharacter); // i.e.
                                                                       // ChatAllowedCharacters.isAllowedCharacter(eventCharacter)

        /* Validate if the entire string is valid, may be used on occasion to reset the text */
        public abstract boolean isStringValid(String text);

        /* Set text to base display */
        public abstract void setString(String text);

        /*
         * Get string from client, can be used for initial value. Should return the currently set property changed by
         * setString
         */
        public abstract String getString();
    }

    public DisplayUnitTextField(Coord offset, Coord size, VerticalAlignment vertAlign, HorizontalAlignment horizAlign,
            int maxStringLength, Validator textValidator) {
        this.offset = offset;
        this.size = size;
        this.vertAlign = vertAlign;
        this.horizAlign = horizAlign;
        this.maxStringLength = maxStringLength;
        this.textValidator = textValidator;
        setText(textValidator.getString());
        setCursorPosition(displayText.length());
    }

    /** Performs enable routine IF not already enabled */
    private void enableBox() {
        if (isSelected) {
            return;
        }
        isSelected = true;
        String entry = textValidator.getString();
        setText(entry);
        defaultEntry = entry;
        prevEntry = entry;
    }

    /** Performs disabled routine IF not already disabled */
    private void disabledBox() {
        if (!isSelected) {
            return;
        }
        isSelected = false;
        if (textValidator.isStringValid(displayText)) {
            textValidator.setString(displayText);
        } else {
            textValidator.setString(defaultEntry);
            setText(defaultEntry);
        }
    }

    private FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    /** Replaces the text wholsesale with minor checks for validity */
    public void setText(String text) {
        if (text.length() > this.maxStringLength) {
            displayText = text.substring(0, this.maxStringLength);
        } else {
            displayText = text;
        }
        setCursorPositionEnd();
    }

    /**
     * Handles appending text, replacing selection, and then moving cursor. To replace text wholesale use
     * {@link #setText()}
     */
    public void writeText(String addition) {
        addition = filterAllowedCharacters(addition);
        int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
        int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
        int k = maxStringLength - displayText.length() - (i - selectionEnd);
        boolean flag = false;

        String result = "";
        if (displayText.length() > 0) {
            result = result + displayText.substring(0, i);
        }

        int l;

        if (k < addition.length()) {
            result = result + addition.substring(0, k);
            l = k;
        } else {
            result = result + addition;
            l = addition.length();
        }

        if (displayText.length() > 0 && j < displayText.length()) {
            result = result + displayText.substring(j);
        }

        displayText = result;
        moveCursorBy(i - selectionEnd + l);
    }

    private String filterAllowedCharacters(String string) {
        StringBuilder stringbuilder = new StringBuilder();
        for (char character : string.toCharArray()) {
            if (textValidator.isCharacterValid(character)) {
                stringbuilder.append(character);
            }
        }
        return stringbuilder.toString();
    }

    /**
     * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
     * the cursor.
     */
    private void deleteWords(int par1) {
        if (displayText.length() != 0) {
            if (selectionEnd != cursorPosition) {
                writeText("");
            } else {
                deleteFromCursor(getPosofNthWord(par1, cursorPosition) - cursorPosition);
            }
        }
    }

    /**
     * delete the selected text, otherwsie deletes characters from either side of the cursor. params: delete num
     */
    private void deleteFromCursor(int charsToDelete) {
        if (displayText.length() != 0) {
            if (selectionEnd != cursorPosition) {
                writeText("");
            } else {
                boolean flag = charsToDelete < 0;
                int j = flag ? cursorPosition + charsToDelete : cursorPosition;
                int k = flag ? cursorPosition : cursorPosition + charsToDelete;
                String s = "";

                if (j >= 0) {
                    s = displayText.substring(0, j);
                }

                if (k < displayText.length()) {
                    s = s + displayText.substring(k);
                }

                displayText = s;

                if (flag) {
                    this.moveCursorBy(charsToDelete);
                }
            }
        }
    }

    /**
     * Calculates position N words away from provided position in displayString. N may be negative; in which case it
     * searchs backwards
     */
    private int getPosofNthWord(int wordOffsetToGet, int position) {
        return getPosofNthWord(wordOffsetToGet, position, true);
    }

    private int getPosofNthWord(int wordOffsetToGet, int startPosition, boolean par3) {
        int position = startPosition;
        boolean searchBackwards = wordOffsetToGet < 0;
        int wordNum = Math.abs(wordOffsetToGet);

        for (int i = 0; i < wordNum; ++i) {
            if (searchBackwards) {
                while (par3 && position > 0 && displayText.charAt(position - 1) == 32) {
                    --position;
                }

                while (position > 0 && displayText.charAt(position - 1) != 32) {
                    --position;
                }
            } else {
                int maxPosition = displayText.length();
                position = displayText.indexOf(32, position);

                if (position == -1) {
                    position = maxPosition;
                } else {
                    while (par3 && position < maxPosition && displayText.charAt(position) == 32) {
                        ++position;
                    }
                }
            }
        }
        return position;
    }

    protected void setCursorPosition(int par1) {
        this.cursorPosition = par1;
        int j = this.displayText.length();

        if (this.cursorPosition < 0) {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > j) {
            this.cursorPosition = j;
        }

        this.setSelectionPos(this.cursorPosition);
    }

    private void setCursorPositionEnd() {
        setCursorPosition(displayText.length());
    }

    private void moveCursorBy(int par1) {
        setCursorPosition(selectionEnd + par1);
    }

    /** Sets the position of the selection cursor and recalculates lineScrollOffset */
    private void setSelectionPos(int pos) {
        int maxPos = displayText.length();
        pos = pos > maxPos ? maxPos : pos < 0 ? 0 : pos;
        selectionEnd = pos;

        FontRenderer fontRenderer = getFontRenderer();

        if (fontRenderer != null) {
            if (lineScrollOffset > maxPos) {
                lineScrollOffset = maxPos;
            }

            int width = getSize().x;
            String trimDisplay = fontRenderer.trimStringToWidth(displayText.substring(lineScrollOffset), width);
            int maxPosToFit = trimDisplay.length() + lineScrollOffset;

            /* Calculate desired lineScrollOffset such that selected position is last character displayed. */
            if (pos == lineScrollOffset) {
                lineScrollOffset -= fontRenderer.trimStringToWidth(displayText, width, true).length();
            }

            if (pos > maxPosToFit) {
                lineScrollOffset += pos - maxPosToFit;
            } else if (pos <= lineScrollOffset) {
                lineScrollOffset -= lineScrollOffset - pos;
            }

            if (lineScrollOffset < 0) {
                lineScrollOffset = 0;
            }

            if (lineScrollOffset > maxPos) {
                lineScrollOffset = maxPos;
            }
        }
    }

    private String getSelectedtext() {
        int startIndex = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int endIndex = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return displayText.substring(startIndex, endIndex);
    }

    @Override
    public String getType() {
        return DISPLAY_ID;
    }

    @Override
    public Coord getOffset() {
        return offset;
    }

    @Override
    public Coord getSize() {
        return size;
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
    public void onUpdate(Minecraft mc, int ticks) {
        String entry = textValidator.getString();
        if (!entry.equals(prevEntry)) {
            setText(entry);
        }
        prevEntry = entry;
    }

    @Override
    public boolean shouldRender(Minecraft mc) {
        return true;
    }

    @Override
    public void renderDisplay(Minecraft mc, Coord position) {
        FontRenderer fontRenderer = mc.fontRenderer;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture(guiButton);

        /* Background */
        // TODO: The Background Texture and Coords for Toggled/UnToggled/Hover need to be configurable via a setter, BUT
        // the default is set during the constructor
        if (isSelected || isMouseOver) {
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -1.0f, position, getSize(), new Coord(000,
                    0), new Coord(127, 127));
        } else {
            DisplayRenderHelper.drawTexture4Quadrants(Tessellator.instance, -0.1f, position, getSize(), new Coord(129,
                    0), new Coord(127, 127));
        }

        int shortCurPos = cursorPosition - lineScrollOffset;
        String shortName = (String) fontRenderer.listFormattedStringToWidth(displayText.substring(lineScrollOffset),
                getSize().x).get(0);
        // Note posZ-4+getSize/2. -4 is to 'center' the string vertically, and getSize/2 is to move center to the
        // middle button
        DisplayRenderHelper.drawCenteredString(fontRenderer, shortName, position.x + getSize().x / 2, position.z - 4
                + getSize().z / 2, 16777120, true);

        if (isSelected) {
            if (cursorCounter / 24 % 2 != 0) {
                int stringStart = position.x + getSize().x / 2 - fontRenderer.getStringWidth(shortName) / 2;
                int lengthSize = fontRenderer.getStringWidth(shortCurPos > shortName.length() ? shortName : shortName
                        .substring(0, shortCurPos));
                int xCoord = stringStart + lengthSize;
                drawCursorVertical(xCoord, position.z + getSize().z - 3, xCoord + 1, position.z + getSize().z - 3
                        - fontRenderer.FONT_HEIGHT);
            }
        }
        cursorCounter++;
    }

    private void drawCursorVertical(int highPosX, int highPosY, int lowPosX, int lowPosY) {
        int temp; // Holder to switch variables

        if (highPosX < lowPosX) {
            temp = highPosX;
            highPosX = lowPosX;
            lowPosX = temp;
        }

        if (highPosY < lowPosY) {
            temp = highPosY;
            highPosY = lowPosY;
            lowPosY = temp;
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_OR_REVERSE);
        tessellator.startDrawingQuads();
        tessellator.addVertex(highPosX, lowPosY, 0.0D);
        tessellator.addVertex(lowPosX, lowPosY, 0.0D);
        tessellator.addVertex(lowPosX, highPosY, 0.0D);
        tessellator.addVertex(highPosX, highPosY, 0.0D);
        tessellator.draw();
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void mousePosition(Coord localMouse, HoverAction hoverAction, HoverTracker hoverChecker) {
        if (!hoverChecker.isHoverFound() && hoverAction == HoverAction.HOVER) {
            isMouseOver = true;
            hoverChecker.markHoverFound();
        } else {
            isMouseOver = false;
        }
    }

    @Override
    public ActionResult mouseAction(Coord localMouse, MouseAction action, int... actionData) {
        if (DisplayHelper.isCursorOverDisplay(localMouse, this)) {
            if (action == MouseAction.CLICK && actionData[0] == 0) {
                enableBox();
                return ActionResult.SIMPLEACTION;
            }
        } else {
            disabledBox();
            return ActionResult.NOACTION;
        }
        return ActionResult.NOACTION;
    }

    // See func_146201_a in guiTextField
    @Override
    public ActionResult keyTyped(char eventCharacter, int eventKey) {
        // TODO: Esc key should be bound to escape text selection
        if (isSelected) {
            ActionResult result = processKeyEvent(eventCharacter, eventKey) ? ActionResult.SIMPLEACTION
                    : ActionResult.NOACTION;
            if (eventKey == Keyboard.KEY_RETURN) {
                disabledBox();
            }
            return result;
        } else {
            return ActionResult.NOACTION;
        }
    }

    private boolean processKeyEvent(char eventCharacter, int eventKey) {
        if (isSelected) {
            switch (eventCharacter) {
            case 1:
                this.setCursorPositionEnd();
                this.setSelectionPos(0);
                return true;
            case 3:// Copy, CTRL + C
                DisplayHelper.setClipboardString(getSelectedtext());
                return true;
            case 22:// Paste, CTRL + V
                writeText(DisplayHelper.getClipboardString());
                return true;
            case 24: // Cut, CTRL + X
                DisplayHelper.setClipboardString(getSelectedtext());
                writeText("");
                return true;
            default:
                switch (eventKey) {
                case 14: // Ctrl + backspace = DeleteLastWord
                    if (DisplayHelper.isCtrlKeyDown()) {
                        deleteWords(-1);
                    } else {
                        deleteFromCursor(-1);
                    }
                    return true;
                case 199:
                    if (DisplayHelper.isShiftKeyDown()) {
                        setSelectionPos(0);
                    } else {
                        setCursorPosition(0);
                    }
                    return true;
                case 203: // Left Arrow
                    if (DisplayHelper.isShiftKeyDown()) {
                        if (DisplayHelper.isCtrlKeyDown()) {
                            setSelectionPos(getPosofNthWord(-1, selectionEnd));
                        } else {
                            setSelectionPos(selectionEnd - 1);
                        }
                    } else if (DisplayHelper.isCtrlKeyDown()) {
                        setCursorPosition(getPosofNthWord(-1, cursorPosition));
                    } else {
                        moveCursorBy(-1);
                    }
                    return true;
                case 205:
                    if (DisplayHelper.isShiftKeyDown()) {
                        if (DisplayHelper.isCtrlKeyDown()) {
                            setSelectionPos(getPosofNthWord(1, selectionEnd));
                        } else {
                            setSelectionPos(selectionEnd + 1);
                        }
                    } else if (DisplayHelper.isCtrlKeyDown()) {
                        setCursorPosition(getPosofNthWord(1, cursorPosition));
                    } else {
                        moveCursorBy(1);
                    }
                    return true;
                case 207:
                    if (DisplayHelper.isShiftKeyDown()) {
                        setSelectionPos(displayText.length());
                    } else {
                        setCursorPositionEnd();
                    }
                    return true;
                case 211:
                    if (DisplayHelper.isCtrlKeyDown()) {
                        deleteWords(1);
                    } else {
                        deleteFromCursor(1);
                    }
                    return true;
                default:
                    if (textValidator.isCharacterValid(eventCharacter)) {
                        writeText(Character.toString(eventCharacter));
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public void saveCustomData(JsonObject jsonObject) {
    }

    @Override
    public void loadCustomData(DisplayUnitFactory factory, JsonObject customData) {
    }
}
