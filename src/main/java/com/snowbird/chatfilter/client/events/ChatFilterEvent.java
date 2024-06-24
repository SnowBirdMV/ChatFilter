package com.snowbird.chatfilter.client.events;

import com.snowbird.chatfilter.client.data.ChatFilterOptions;
import com.snowbird.chatfilter.ChatFilter;
import com.snowbird.chatfilter.client.screens.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = ChatFilter.MODID)
public class ChatFilterEvent {

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.Key event) {
        if (ChatFilter.SHOW_CONFIG_MAPPING.isDown()) {
            Minecraft.getInstance().setScreen(new ConfigScreen(new ChatFilterOptions()));
        }
    }
}
