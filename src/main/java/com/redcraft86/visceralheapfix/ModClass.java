package com.redcraft86.visceralheapfix;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;

@Mod(ModClass.MOD_ID)
public class ModClass {
    public static final String MOD_ID = "visceralheapfix";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModClass(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }
}
