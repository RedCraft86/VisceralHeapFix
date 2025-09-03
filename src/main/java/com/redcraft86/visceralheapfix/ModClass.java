package com.redcraft86.visceralheapfix;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

@Mod(ModClass.MOD_ID)
public class ModClass {
    public static final String MOD_ID = "visceralheapfix";
    public ModClass(FMLJavaModLoadingContext context)
    {
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }
}
