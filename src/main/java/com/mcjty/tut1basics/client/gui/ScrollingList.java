package com.mcjty.tut1basics.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

public class ScrollingList extends ObjectSelectionList<ScrollingList.Entry> {
    private Entry selectedEntry;
    private static final Logger LOGGER = LogManager.getLogger();
    private final Consumer<Entry> selectionCallback;

    public ScrollingList(Screen parent, int width, int height, int top, int bottom, int itemHeight, Map<Component, Object> contents, Consumer<Entry> selectionCallback) {
        super(Minecraft.getInstance(), width, height, top, bottom, itemHeight);
        this.selectionCallback = selectionCallback;
        // Add entries to the list here
        for (Map.Entry<Component, Object> entry : contents.entrySet()) {
            this.addEntry(new Entry(this, entry.getKey(), entry.getValue()));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return this.getSelected() == this.getEntry(index);
    }

    public void setSelected(Entry entry) {
        this.selectedEntry = entry;
        if (this.selectionCallback != null) {
            this.selectionCallback.accept(entry);
        }
    }

    public Entry getSelected() {
        return this.selectedEntry;
    }

    public static class Entry extends ObjectSelectionList.Entry<ScrollingList.Entry> {
        private final ScrollingList parent;
        private final Component text;
        private final Object value;

        public Entry(ScrollingList parent, Component text, Object value) {
            this.parent = parent;
            this.text = text;
            this.value = value;
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            pGuiGraphics.drawString(Minecraft.getInstance().font, this.text, pLeft + 2, pTop + 2, 0xFFFFFF);
            if (pHovering) {
                pGuiGraphics.fill(pLeft, pTop, pLeft + pWidth, pTop + pHeight, 0x80FFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.parent.getSelected() != this) {
                this.parent.setSelected(this);
                LOGGER.info("Selected item: {}", this.text.getString());
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return this.text;
        }

        // Getters
        public ScrollingList getParent() {
            return parent;
        }

        public Component getText() {
            return text;
        }

        public Object getValue() {
            return value;
        }
    }
}
