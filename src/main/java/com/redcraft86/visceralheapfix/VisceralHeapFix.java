package com.redcraft86.visceralheapfix;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VisceralHeapFix.MOD_ID)
public class VisceralHeapFix {
    public static final String MOD_ID = "visceralheapfix";

    public VisceralHeapFix(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, CommonCfg.SPEC);
    }
}