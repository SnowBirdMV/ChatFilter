package com.mcjty.tut1basics;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = Tutorial1Basics.MODID)
public class ChatFilterEvent {

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.Key event) {
        System.out.println(event.getKey());
        if (Tutorial1Basics.SHOW_CONFIG_MAPPING.isDown()) {
            System.out.println("PRESSED THE CORRECT KEY");
            Minecraft.getInstance().setScreen(new ConfigScreen(new ChatFilterOptions()));
        }
    }

    @SubscribeEvent
    public static void clientChatReceived(ClientChatReceivedEvent e) {
        try {
            System.out.println("HI IM HERE FIND ME");
        }
        catch (Exception ex){
            System.out.println("GOT AN ERROR:");
            System.out.println(ex.getMessage());
        }
    }
}
