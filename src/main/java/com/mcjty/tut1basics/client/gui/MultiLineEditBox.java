package com.mcjty.tut1basics.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MultiLineEditBox extends EditBox {
    private final int lineHeight = 10;
    private int scrollOffset = 0;
    private boolean followCursor = true;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private int wrappedLineCount = 0; // New variable to keep track of wrapped lines

    public MultiLineEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.setMaxLength(Integer.MAX_VALUE);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render background and border
        this.renderBackground(pGuiGraphics);

        // Set up rendering system
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Wrap text for multi-line display
        List<String> lines = wrapText(this.getValue(), this.getWidth() - 8);  // Adjust width for padding

        // Ensure there are lines to render
        if (lines.isEmpty()) {
            return;
        }

        // Adjust scrollOffset to keep the cursor line visible if followCursor is true
        if (followCursor) {
            ensureCursorVisible(lines);
        }

        int visibleLines = (this.getHeight() - 8) / lineHeight;  // Calculate number of visible lines
        int lineY = this.getY() + 4;

        // Render selection background
        if (selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd) {
            renderSelectionBackground(pGuiGraphics, lines);
        }

        for (int i = scrollOffset; i < lines.size() && i < scrollOffset + visibleLines; i++) {
            if (i >= 0 && i < lines.size()) {
                pGuiGraphics.drawString(Minecraft.getInstance().font, lines.get(i), this.getX() + 4, lineY, 0xFFFFFF);
            }
            lineY += lineHeight;
        }

        if (isFocused()) {
            int cursorLine = Math.max(0, Math.min(getCursorLine(lines), lines.size() - 1));
            int cursorPositionInLine = getCursorPositionInLine(lines, this.getOriginalCursorPosition());
            if (cursorLine >= 0 && cursorLine < lines.size() && cursorPositionInLine >= 0) {
                String line = lines.get(cursorLine);
                cursorPositionInLine = Math.min(cursorPositionInLine, line.length());
                int cursorX = this.getX() + 4 + Minecraft.getInstance().font.width(line.substring(0, cursorPositionInLine));
                int cursorY = this.getY() + 4 + (cursorLine - scrollOffset) * lineHeight;
                pGuiGraphics.hLine(cursorX, cursorX + 1, cursorY, 0xFFFFFFFF);
            }
        }

        // Render scrollbar
        renderScrollBar(pGuiGraphics, lines);
    }

    private void renderBackground(GuiGraphics pGuiGraphics) {
        int borderColor = isFocused() ? 0xFFFFFFFF : 0xFFAAAAAA;
        int backgroundColor = 0xFF000000;

        pGuiGraphics.fill(getX() - 2, getY() - 2, getX() + width + 2, getY() + height + 2, borderColor);
        pGuiGraphics.fill(getX(), getY(), getX() + width, getY() + height, backgroundColor);
    }

    private int getCursorLine(List<String> lines) {
        int cursorPos = this.getCursorPosition();
        int charCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            int lineLength = lines.get(i).length();
            if (cursorPos <= charCount + lineLength) {
                return i;
            }
            charCount += lineLength;
        }
        return Math.max(0, lines.size() - 1);
    }

    private int getCursorPositionInLine(List<String> lines, int cursorPos) {
        int charCount = 0;
        for (String line : lines) {
            int lineLength = line.length();
            if (cursorPos <= charCount + lineLength) {
                return cursorPos - charCount;
            }
            charCount += line.length();
        }
        return 0;
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> wrappedLines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            wrappedLines.add(""); // Ensure there is at least one line to prevent empty list issues
            return wrappedLines;
        }

        StringBuilder currentLine = new StringBuilder();
        String[] words = text.split(" ");
        wrappedLineCount = 0; // Reset wrapped line count

        for (String word : words) {
            if (Minecraft.getInstance().font.width(word) > maxWidth) {
                // If the word is too long to fit on a single line, split it
                while (Minecraft.getInstance().font.width(word) > maxWidth) {
                    int charLimit = maxWidth / Minecraft.getInstance().font.width("a"); // Approximate number of characters that fit in maxWidth
                    String part = word.substring(0, charLimit);
                    word = word.substring(charLimit);
                    if (currentLine.length() > 0) {
                        wrappedLines.add(currentLine.toString());
                        currentLine.setLength(0);
                    }
                    wrappedLines.add(part);
                    wrappedLineCount++; // Increment wrapped line count for each split part
                }
                if (!word.isEmpty()) {
                    currentLine.append(word);
                }
            } else {
                String potentialLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (Minecraft.getInstance().font.width(potentialLine) > maxWidth) {
                    wrappedLines.add(currentLine.toString());
                    currentLine.setLength(0);
                    currentLine.append(word);
                    wrappedLineCount++; // Increment wrapped line count for each wrapped line
                } else {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }
        }
        if (currentLine.length() > 0) {
            wrappedLines.add(currentLine.toString());
        }

        return wrappedLines;
    }

    private void ensureCursorVisible(List<String> lines) {
        int cursorLine = getCursorLine(lines);
        int visibleLines = (this.getHeight() - 8) / lineHeight;

        if (cursorLine < scrollOffset) {
            scrollOffset = cursorLine;
        } else if (cursorLine >= scrollOffset + visibleLines) {
            scrollOffset = cursorLine - visibleLines + 1;
        }
    }

    private void renderScrollBar(GuiGraphics pGuiGraphics, List<String> lines) {
        int visibleLines = (this.getHeight() - 8) / lineHeight;
        int totalLines = lines.size();

        if (totalLines > visibleLines) {
            int scrollBarHeight = Math.max(10, (int) ((float) visibleLines / totalLines * (this.getHeight() - 8)));
            int scrollBarY = this.getY() + 4 + (int) ((float) scrollOffset / (totalLines - visibleLines) * (this.getHeight() - 8 - scrollBarHeight));

            pGuiGraphics.fill(this.getX() + this.getWidth() - 6, scrollBarY, this.getX() + this.getWidth() - 2, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }
    }

    public void scroll(int lines) {
        followCursor = false;  // Disable followCursor when manually scrolling

        List<String> linesList = wrapText(this.getValue(), this.getWidth() - 8);
        int maxScrollOffset = Math.max(0, linesList.size() - (this.getHeight() - 8) / lineHeight);

        scrollOffset = Math.max(0, Math.min(scrollOffset + lines, maxScrollOffset));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (selectionStart != selectionEnd) {
            deleteSelectedText();
        }
        try {
            int cursorPos = this.getCursorPosition();
            int originalPos = this.getOriginalCursorPosition();
            StringBuilder value = new StringBuilder(this.getValue());
            if (cursorPos > value.length()){
                value.append(codePoint);
            }
            else{
                value.insert(cursorPos, codePoint);
            }

            this.setValue(value.toString());
            this.setCursorPosition(originalPos + 1);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        followCursor = true;  // Enable followCursor when typing
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                selectAllText();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_C) {
                copySelectedText();
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (selectionStart != selectionEnd) {
                deleteSelectedText();
            } else {
                int cursorPos = this.getCursorPosition();
                if (cursorPos > 0) {
                    StringBuilder value = new StringBuilder(this.getValue());
                    value.deleteCharAt(cursorPos - 1);
                    this.setValue(value.toString());
                    this.setCursorPosition(cursorPos - 1);
                    followCursor = true;  // Ensure followCursor is true after deleting text
                }
            }
            return true;
        }

        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        followCursor = true;  // Enable followCursor when using keyboard navigation
        return result;
    }

    private void copySelectedText() {
        if (selectionStart != selectionEnd) {
            int start = Math.min(selectionStart, selectionEnd);
            int end = Math.max(selectionStart, selectionEnd);
            String selectedText = this.getValue().substring(start, end);
            Minecraft.getInstance().keyboardHandler.setClipboard(selectedText);
        }
    }

    private void selectAllText() {
        selectionStart = 0;
        selectionEnd = this.getValue().length();
        this.setCursorPosition(selectionEnd);
        followCursor = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            int clickLine = ((int) mouseY - this.getY() - 4) / lineHeight + scrollOffset;
            List<String> lines = wrapText(this.getValue(), this.getWidth() - 8);
            if (clickLine >= 0 && clickLine < lines.size()) {
                int clickColumn = (int) mouseX - this.getX() - 4;
                int cursorPosition = calculateCursorPosition(clickLine, clickColumn, lines);
                this.setCursorPosition(cursorPosition);
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
                followCursor = true;  // Enable followCursor after clicking
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void deleteSelectedText() {
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        StringBuilder value = new StringBuilder(this.getValue());
        value.delete(start, end);
        this.setValue(value.toString());
        this.setCursorPosition(start);
        selectionStart = start;
        selectionEnd = start;
        followCursor = true;
    }

    private int calculateCursorPosition(int clickLine, int clickColumn, List<String> lines) {
        int cursorPosition = 0;
        for (int i = 0; i < clickLine; i++) {
            cursorPosition += lines.get(i).length();
        }
        String line = lines.get(clickLine);
        int columnWidth = 0;
        for (int i = 0; i < line.length(); i++) {
            int charWidth = Minecraft.getInstance().font.width(String.valueOf(line.charAt(i)));
            if (columnWidth + charWidth / 2 >= clickColumn) {
                return cursorPosition + i;
            }
            columnWidth += charWidth;
        }
        return cursorPosition + line.length() - wrappedLineCount;
    }

    @Override
    public int getCursorPosition() {
        int cursorPos = super.getCursorPosition();
        String truncatedString = this.getValue().substring(0, cursorPos);
        this.wrapText(truncatedString, this.getWidth() - 8);
        return this.wrappedLineCount + cursorPos;
    }

    private int getOriginalCursorPosition(){
        return super.getCursorPosition();
    }

    @Override
    public void insertText(String text) {
        if (selectionStart != selectionEnd) {
            deleteSelectedText();
        }
        try {
            int cursorPos = this.getCursorPosition();
            int originalPos = this.getOriginalCursorPosition();
            StringBuilder value = new StringBuilder(this.getValue());
            if (cursorPos > value.length()){
                value.append(text);
            }
            else{
                value.insert(cursorPos, text);
            }

            this.setValue(value.toString());
            this.setCursorPosition(originalPos + 1);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        followCursor = true;  // Enable followCursor when typing
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            int dragLine = ((int) mouseY - this.getY() - 4) / lineHeight + scrollOffset;
            List<String> lines = wrapText(this.getValue(), this.getWidth() - 8);
            if (dragLine >= 0 && dragLine < lines.size()) {
                int dragColumn = (int) mouseX - this.getX() - 4;
                int cursorPosition = calculateCursorPosition(dragLine, dragColumn, lines);
                this.setCursorPosition(cursorPosition);
                selectionEnd = cursorPosition;
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderSelectionBackground(GuiGraphics pGuiGraphics, List<String> lines) {
        int selectionStartLine = getCursorLine(lines, selectionStart);
        int selectionEndLine = getCursorLine(lines, selectionEnd);
        int selectionStartPos = getCursorPositionInLine(lines, selectionStart);
        int selectionEndPos = getCursorPositionInLine(lines, selectionEnd);

        if (selectionStart > selectionEnd) {
            int tempLine = selectionStartLine;
            selectionStartLine = selectionEndLine;
            selectionEndLine = tempLine;

            int tempPos = selectionStartPos;
            selectionStartPos = selectionEndPos;
            selectionEndPos = tempPos;
        }

        for (int line = selectionStartLine; line <= selectionEndLine; line++) {
            int lineStartX = this.getX() + 4;
            int lineEndX = this.getX() + 4 + Minecraft.getInstance().font.width(lines.get(line));
            int lineY = this.getY() + 4 + (line - scrollOffset) * lineHeight;

            if (line == selectionStartLine) {
                lineStartX += Minecraft.getInstance().font.width(lines.get(line).substring(0, selectionStartPos));
            }
            if (line == selectionEndLine) {
                lineEndX = this.getX() + 4 + Minecraft.getInstance().font.width(lines.get(line).substring(0, selectionEndPos));
            }

            pGuiGraphics.fill(lineStartX, lineY, lineEndX, lineY + lineHeight, 0x800000FF);
        }
    }

    private int getCursorLine(List<String> lines, int cursorPos) {
        int charCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            int lineLength = lines.get(i).length();
            if (cursorPos <= charCount + lineLength) {
                return i;
            }
            charCount += lineLength;
        }
        return Math.max(0, lines.size() - 1);
    }
}