package com.mcjty.tut1basics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mcjty.tut1basics.client.data.FilterRule;
import com.mcjty.tut1basics.client.gui.ScrollingList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {
    private static final Component CREDITS_AND_ATTRIBUTION = Component.translatable("options.credits_and_attribution");
    private static final Component ENABLE_MOD = Component.translatable("options.chatfilter.enablemod");
    private static final int COLUMNS = 2;
    private ScrollingList scrollingList;
    private final ChatFilterOptions options;
    private EditBox nameBox;
    private EditBox filterValueBox;
    private Checkbox isRegexCheckbox;
    private Checkbox isOverlayCheckbox;
    public boolean test = true;
    public ConfigScreen(ChatFilterOptions chatFilterOptions) {
        super(Component.translatable("options.title"));
        this.options = chatFilterOptions;


    }

    protected void init() {
        super.init();
        // Add a button
        Button testButton = Button.builder(Component.translatable("options.chatfilter.enablemod"), button -> {
            // Action to perform when button is pressed
            Tutorial1Basics.config.setModEnabled(!Tutorial1Basics.config.isModEnabled());
        }).pos(10, 10).size(100, 20).build();
        this.addRenderableWidget(testButton);

        // Add a scrolling list
        Map<Component, Object> filterMap = new HashMap<>();
        for (FilterRule rule : Tutorial1Basics.config.getFilterRules()) {
            filterMap.put(Component.translatable(rule.getName()), rule);
        }
        this.scrollingList = new ScrollingList(this, 100, this.height, 32, this.height - 32, 20, filterMap, this::onFilterListItemSelected);
        this.addRenderableWidget(this.scrollingList);

        // Add a text input box
        this.nameBox = new EditBox(this.font, 120, 40, 200, 20, Component.translatable("options.chatfilter.namebox"));
        this.nameBox.setMaxLength(Short.MAX_VALUE);
        this.addRenderableWidget(this.nameBox);
        this.filterValueBox = new EditBox(this.font, 120, 80, 200, 20, Component.translatable("options.chatfilter.filtervaluebox"));
        this.filterValueBox.setMaxLength(Short.MAX_VALUE);
        this.addRenderableWidget(this.filterValueBox);
        this.isRegexCheckbox = new Checkbox(120, this.height - 50, 20, 20, Component.translatable("options.chatfilter.isregexcheckbox"), false);
        this.addRenderableWidget(this.isRegexCheckbox);
        this.isOverlayCheckbox = new Checkbox(220, this.height - 50, 20, 20, Component.translatable("options.chatfilter.isoverlaycheckbox"), false);
        this.addRenderableWidget(this.isOverlayCheckbox);

    }




    public void removed() {
//        this.options.save();
    }

    private void onFilterListItemSelected(ScrollingList.Entry entry) {
        // Handle the selection event here
        FilterRule rule = (FilterRule)entry.getValue();
        System.out.println("Item selected: " + entry.getText().getString());
        this.nameBox.setValue(rule.getName());
        this.filterValueBox.setValue(rule.getFilterText());
        if (this.isRegexCheckbox.selected() != rule.isRegex()){
            this.isRegexCheckbox.onPress();
        }
        if (this.isOverlayCheckbox.selected() != rule.isOverlay()){
            this.isOverlayCheckbox.onPress();
        }
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
        this.scrollingList.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    private Button openScreenButton(Component p_261565_, Supplier<Screen> p_262119_) {
        return Button.builder(p_261565_, (p_280808_) -> {
            this.minecraft.setScreen((Screen) p_262119_.get());
        }).build();
    }
}