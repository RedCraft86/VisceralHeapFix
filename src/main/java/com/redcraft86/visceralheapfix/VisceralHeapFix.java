package com.redcraft86.visceralheapfix;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.bus.api.IEventBus;

@Mod(VisceralHeapFix.MOD_ID)
public final class VisceralHeapFix {
    public static final String MOD_ID = "visceralheapfix";

    public VisceralHeapFix(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonCfg.SPEC);
    }
}
