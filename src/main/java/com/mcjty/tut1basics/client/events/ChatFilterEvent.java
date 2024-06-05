package com.mcjty.tut1basics.client.events;

import com.mcjty.tut1basics.client.data.ChatFilterOptions;
import com.mcjty.tut1basics.Tutorial1Basics;
import com.mcjty.tut1basics.client.screens.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = Tutorial1Basics.MODID)
public class ChatFilterEvent {

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.Key event) {
        if (Tutorial1Basics.SHOW_CONFIG_MAPPING.isDown()) {
            Minecraft.getInstance().setScreen(new ConfigScreen(new ChatFilterOptions()));
        }
    }
}
