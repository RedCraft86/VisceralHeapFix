package com.redcraft86.visceralheapfix;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;

@Mod(VisceralHeapFix.MOD_ID)
public final class VisceralHeapFix {
    public static final String MOD_ID = "visceralheapfix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VisceralHeapFix(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonCfg.SPEC);
    }
}
