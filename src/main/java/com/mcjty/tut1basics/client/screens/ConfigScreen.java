package com.mcjty.tut1basics.client.screens;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mcjty.tut1basics.client.data.ChatFilterOptions;
import com.mcjty.tut1basics.client.handlers.ChatHandler;
import com.mcjty.tut1basics.Tutorial1Basics;
import com.mcjty.tut1basics.client.data.Config;
import com.mcjty.tut1basics.client.data.FilterRule;
import com.mcjty.tut1basics.client.gui.MultiLineEditBox;
import com.mcjty.tut1basics.client.gui.ScrollingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {
    private ScrollingList scrollingList;
    private ScrollingList messageLogList;
    private final ChatFilterOptions options;
    private EditBox nameBox;
    private MultiLineEditBox filterValueBox;
    private Checkbox isRegexCheckbox;
    private Checkbox isOverlayCheckbox;
    private FilterRule selectedRule;
    private final Config config;

    private String getToggleButtonText() {
        return Tutorial1Basics.config.isModEnabled() ? "§a✓ Mod Enabled" : "§c✗ Mod Disabled";
    }

    private void updateToggleButtonText(Button button) {
        button.setMessage(Component.translatable(getToggleButtonText()));
    }

    public ConfigScreen(ChatFilterOptions chatFilterOptions) {
        super(Component.translatable("options.title"));
        this.options = chatFilterOptions;
        this.config = new Config();
    }

    @Override
    protected void init() {
        super.init();

        // Add the toggle mod button
        Button toggleModButton = Button.builder(Component.translatable(getToggleButtonText()), button -> {
            // Action to perform when button is pressed
            Tutorial1Basics.config.setModEnabled(!Tutorial1Basics.config.isModEnabled());
            updateToggleButtonText(button);
        }).pos(this.width / 100, this.height / 50).size(this.width / 5, this.height / 13).build();
        this.addRenderableWidget(toggleModButton);

        int bottomRowCutoff = this.height - this.height / 8;
        int topRowCutoff = this.height / 8;

        int checkboxHeight = 20;
        int checkboxWidth = checkboxHeight;

        // Initialize filter list
        initFilterList(bottomRowCutoff);

        // Add a scrolling list for the message log
        Map<Component, Object> messageLogMap = new LinkedHashMap<>();
        for (Component message : ChatHandler.getMessageLog()) {
            messageLogMap.put(message, message);
        }
        this.messageLogList = new ScrollingList(this, this.width / 5, this.height, this.height / 9, bottomRowCutoff, 12, messageLogMap, this::onMessageLogItemSelected);
        this.messageLogList.setLeftPos(this.width - (this.width / 100) - (this.width / 5));
        this.addRenderableWidget(this.messageLogList);

        // Add multi-line text input boxes
        int nameBoxX = (int) (this.width * .5 - (this.width * .25));
        int nameBoxY = this.height / 6;
        this.nameBox = new EditBox(this.font, nameBoxX, nameBoxY, this.width / 2, this.height / 12, Component.translatable("options.chatfilter.namebox"));
        this.addRenderableWidget(this.nameBox);

        int filterBoxX = (int) (this.width * .5 - (this.width * .25));
        int filterBoxY = this.height / 3;
        this.filterValueBox = new MultiLineEditBox(this.font, filterBoxX, filterBoxY, this.width / 2, this.height / 3, Component.translatable("options.chatfilter.filtervaluebox"));
        this.addRenderableWidget(this.filterValueBox);

        // Add checkboxes
        this.isRegexCheckbox = new Checkbox(this.width / 4, this.height - this.height / 6 - checkboxHeight, checkboxWidth, checkboxHeight, Component.translatable("options.chatfilter.isregexcheckbox"), false);
        this.addRenderableWidget(this.isRegexCheckbox);
        this.isOverlayCheckbox = new Checkbox(this.width - this.width / 2, this.height - this.height / 6 - checkboxHeight, checkboxWidth, checkboxHeight, Component.translatable("options.chatfilter.isoverlaycheckbox"), false);
        this.addRenderableWidget(this.isOverlayCheckbox);

        // Add save button
        Button saveButton = Button.builder(Component.translatable("options.chatfilter.save"), button -> {
            saveCurrentValues();
        }).pos((this.width / 2) - (this.width / 6), this.height - this.height / 10).size(this.width / 3, this.height / 13).build();
        this.addRenderableWidget(saveButton);

        // Add new filter button
        Button newFilterButton = Button.builder(Component.translatable("options.chatfilter.newfilter"), button -> {
            createNewFilter();
        }).pos(this.width / 50, this.height - this.height / 10).size(this.width / 6, this.height / 13).build();
        this.addRenderableWidget(newFilterButton);

        // Add delete button
        Button deleteButton = Button.builder(Component.translatable("options.chatfilter.delete").setStyle(Style.EMPTY.withColor(TextColor.parseColor("red"))), button -> {
            deleteSelectedFilter();
        }).pos(this.width - this.width / 6 - this.width / 50, this.height - this.height / 10).size(this.width / 6, this.height / 13).build();
        this.addRenderableWidget(deleteButton);
    }

    private void initFilterList(int bottomRowCutoff) {
        Map<Component, Object> filterMap = new LinkedHashMap<>();
        for (FilterRule rule : Tutorial1Basics.config.getFilterRules()) {
            filterMap.put(Component.translatable(rule.getName()), rule);
        }
        this.scrollingList = new ScrollingList(this, this.width / 5, this.height, this.height / 9, bottomRowCutoff, 12, filterMap, this::onFilterListItemSelected);
        this.scrollingList.setLeftPos(this.width / 100);
        this.addRenderableWidget(this.scrollingList);
    }

    @Override
    public void removed() {
        // this.options.save();
    }

    private void onFilterListItemSelected(ScrollingList.Entry entry) {
        // Handle the selection event here
        selectedRule = (FilterRule) entry.getValue();
        this.nameBox.setValue(selectedRule.getName());
        this.filterValueBox.setValue(selectedRule.getFilterText());
        if (this.isRegexCheckbox.selected() != selectedRule.isRegex()) {
            this.isRegexCheckbox.onPress();
        }
        if (this.isOverlayCheckbox.selected() != selectedRule.isOverlay()) {
            this.isOverlayCheckbox.onPress();
        }
    }

    private void onMessageLogItemSelected(ScrollingList.Entry entry) {
        Component message = (Component) entry.getValue();

        // Copy the raw message string to the clipboard, removing color codes
        String rawMessage = message.getString().replaceAll("§[0-9A-FK-ORa-fk-or]", "");
        Minecraft.getInstance().keyboardHandler.setClipboard(rawMessage);
    }

    private void saveCurrentValues() {
        if (selectedRule != null) {
            selectedRule.setName(this.nameBox.getValue());
            selectedRule.setFilterText(this.filterValueBox.getValue());
            selectedRule.setRegex(this.isRegexCheckbox.selected());
            selectedRule.setOverlay(this.isOverlayCheckbox.selected());
            refreshFilterList();
            config.writeConfig(Tutorial1Basics.config.getFilterRules());
        }
    }

    private void createNewFilter() {
        String baseName = "New Filter";
        String[] newName = { baseName };
        int counter = 1;
        while (Tutorial1Basics.config.getFilterRules().stream().anyMatch(rule -> rule.getName().equals(newName[0]))) {
            newName[0] = baseName + " " + counter;
            counter++;
        }
        FilterRule newRule = new FilterRule(newName[0], "", false, false);
        Tutorial1Basics.config.getFilterRules().add(newRule);
        blankInputFields();
        refreshFilterListToBottom();
        selectFilter(newRule);
        config.writeConfig(Tutorial1Basics.config.getFilterRules());
    }

    private void blankInputFields() {
        this.nameBox.setValue("");
        this.filterValueBox.setValue("");
        if (this.isRegexCheckbox.selected()) {
            this.isRegexCheckbox.onPress();
        }
        if (this.isOverlayCheckbox.selected()) {
            this.isOverlayCheckbox.onPress();
        }
    }

    private void deleteSelectedFilter() {
        if (selectedRule != null) {
            Tutorial1Basics.config.getFilterRules().remove(selectedRule);
            selectedRule = null;
            refreshFilterList();
            config.writeConfig(Tutorial1Basics.config.getFilterRules());
        }
        blankInputFields();
    }

    private void refreshFilterList() {
        int scrollAmount = (int) this.scrollingList.getScrollAmount();
        this.removeWidget(this.scrollingList);
        int bottomRowCutoff = this.height - this.height / 8;
        initFilterList(bottomRowCutoff);
        this.scrollingList.setScrollAmount(scrollAmount);
        if (selectedRule != null) {
            selectFilter(selectedRule);
        }
    }

    private void refreshFilterListToBottom() {
        this.removeWidget(this.scrollingList);
        int bottomRowCutoff = this.height - this.height / 8;
        initFilterList(bottomRowCutoff);
        this.scrollingList.setScrollAmount(this.scrollingList.getMaxScroll());
    }

    private void selectFilter(FilterRule rule) {
        for (ScrollingList.Entry entry : this.scrollingList.children()) {
            if (entry.getValue() == rule) {
                this.scrollingList.setSelected(entry);
                break;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (this.filterValueBox.isMouseOver(pMouseX, pMouseY)) {
            this.filterValueBox.scroll((int) -pScrollY);
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);

        // Draw labels
        pGuiGraphics.drawString(this.font, "Filter Name", (int) (this.width * .5 - (this.width * .25)), this.height / 6 - 10, 16777215);
        pGuiGraphics.drawString(this.font, "Filter Contents", (int) (this.width * .5 - (this.width * .25)), this.height / 3 - 10, 16777215);

        this.scrollingList.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.nameBox.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.filterValueBox.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.isRegexCheckbox.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.isOverlayCheckbox.render(pGuiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}