package com.mcjty.tut1basics.client.data;

import net.minecraft.client.OptionInstance;

public class ChatFilterOptions {
    private final OptionInstance<Boolean> modEnabled;

    public ChatFilterOptions() {
        this.modEnabled = OptionInstance.createBoolean("options.modEnabled", true);
    }

    public OptionInstance<Boolean> modEnabled() {
        return this.modEnabled;
    }
}
