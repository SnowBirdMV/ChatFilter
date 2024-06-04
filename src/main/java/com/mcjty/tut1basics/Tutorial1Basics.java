package com.mcjty.tut1basics;

import com.mcjty.tut1basics.client.data.Config;
import com.mcjty.tut1basics.client.data.FilterRule;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.List;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Tutorial1Basics.MODID)
public class Tutorial1Basics {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tut1basics";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "HBW Helper";
    public static final String MOD_ID = "hbwhelper";

    public static Config config;

//    public static final KeyMapping SHOW_CONFIG_MAPPING = new KeyMapping(
//            "key.chatfilter.showconfig",
//            KeyConflictContext.GUI, // Mapping can only be used when a screen is open
//            InputConstants.Type.MOUSE, // Default mapping is on the mouse
//            GLFW.GLFW_KEY_COMMA, // Default mouse input is the left mouse button
//            "key.categories.examplemod.examplecategory" // Mapping will be in the new example category
//    );

    public static final KeyMapping SHOW_CONFIG_MAPPING = new KeyMapping("key.com.thomas7520.macrokeybinds.openoptions.desc" , GLFW.GLFW_KEY_K, "key.categories.com.thomas7520.macrokeybinds");



    public Tutorial1Basics() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SetupConfig.SPEC);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SetupConfig.SPEC);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "modid-client-config.toml");
        try{
            config = new Config();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }


        MinecraftForge.EVENT_BUS.register(ChatHandler.class);
        MinecraftForge.EVENT_BUS.register(ChatHandler.TitleHandler.class);
        MinecraftForge.EVENT_BUS.register(PokeRadar.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeybindingEvent);


//        ChatHandler.loadAdListConfig();
//        ChatHandler.loadOverlayListConfig();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
    }

    @SubscribeEvent
    public void registerKeybindingEvent(RegisterKeyMappingsEvent event) {
        System.out.println("REGISTERED THE KEYBIND");
        event.register(SHOW_CONFIG_MAPPING);
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
