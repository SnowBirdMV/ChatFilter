package com.mcjty.tut1basics;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

public class PokeRadar {
    @SubscribeEvent
    public void LivingSpawnEventHandler(MobSpawnEvent e){
        System.out.println(e.toString());
    }
}
