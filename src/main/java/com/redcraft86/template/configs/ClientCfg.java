package com.redcraft86.visceralheapfix.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientCfg {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

//    public static final ForgeConfigSpec.BooleanValue EXAMPLE_CFG = BUILDER
//            .comment("This is an example config")
//            .define("exampleCfg", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean isLoaded() { return SPEC.isLoaded(); }
}