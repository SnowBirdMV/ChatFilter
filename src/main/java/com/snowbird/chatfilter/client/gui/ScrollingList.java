package com.snowbird.chatfilter.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mojang.blaze3d.vertex.PoseStack;

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
    protected boolean isSelectedItem(int index) {
        return this.getSelected() == this.getEntry(index);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getLeft() + this.getWidth() - 6; // Adjust the scrollbar position
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        drawBorder(pGuiGraphics);
    }

    private void drawBorder(GuiGraphics pGuiGraphics) {
        int left = this.getLeft();
        int right = this.getLeft() + this.getWidth();
        int top = this.getTop();
        int bottom = this.getBottom();

        // Draw the border
        pGuiGraphics.fill(left - 1, top - 1, right + 1, top, 0xFFFFFFFF); // Top border
        pGuiGraphics.fill(left - 1, bottom, right + 1, bottom + 1, 0xFFFFFFFF); // Bottom border
        pGuiGraphics.fill(left - 1, top, left, bottom, 0xFFFFFFFF); // Left border
        pGuiGraphics.fill(right, top, right + 1, bottom, 0xFFFFFFFF); // Right border
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
            PoseStack poseStack = pGuiGraphics.pose();
            int left = this.parent.getLeft();
            float scale = 0.8f; // Set the desired scale (smaller font size)

            poseStack.pushPose();
            poseStack.translate(left + 2, pTop + 2, 0);
            poseStack.scale(scale, scale, 1.0f);

            // Adjust coordinates after scaling
            int adjustedX = (int) ((pMouseX - left - 2) / scale);
            int adjustedY = (int) ((pMouseY - pTop - 2) / scale);
            int color = 0xFFFFFF;

            pGuiGraphics.drawString(Minecraft.getInstance().font, this.text, 0, 0, color);

            poseStack.popPose();

            if (pHovering) {
                pGuiGraphics.fill(left, pTop, left + pWidth, pTop + pHeight, 0x80FFFFFF);
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