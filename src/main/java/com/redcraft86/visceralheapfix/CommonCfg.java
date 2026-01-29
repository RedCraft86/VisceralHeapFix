package com.redcraft86.visceralheapfix;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeConfigSpec;

@Mod.EventBusSubscriber(modid = ModClass.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonCfg {

    public static final ForgeConfigSpec.IntValue TENDON_CHANCE;
    public static final ForgeConfigSpec.BooleanValue LIMIT_LOGICAL_HEIGHT;
    public static final ForgeConfigSpec.IntValue MAX_HEIGHT_OFFSET;

    public static final ForgeConfigSpec.IntValue MIN_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAX_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue MID_MULTI;
    public static final ForgeConfigSpec.DoubleValue STEP_RATE;

    public static final ForgeConfigSpec.IntValue COLUMN_CHANCE;
    public static final ForgeConfigSpec.IntValue BALL_OFFSET_MIN;
    public static final ForgeConfigSpec.IntValue BALL_OFFSET_MAX;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        TENDON_CHANCE = BUILDER.comment("Chance of tendons to spawn.")
                .defineInRange("tendonChance", 80, 10, 100);

        LIMIT_LOGICAL_HEIGHT = BUILDER.comment("Whether maximum spawnable Y should be limited to the logical height.")
                .define("limitLogicalHeight", false);

        MAX_HEIGHT_OFFSET = BUILDER.comment("Offset to the maximum spawnable Y.")
                .defineInRange("maxHeightOffset", -10, -128, 128);

        BUILDER.push("shape");

        MIN_DISTANCE = BUILDER.comment("Minimum horizontal distance the tendon must curve.\nLower = Less chance of curve, Higher = More chance of curve")
                .defineInRange("distanceMin", 12, 4, 16);

        MAX_DISTANCE = BUILDER.comment("Maximum horizontal distance the tendon can curve.\nLower = Less chance of curve, Higher = More chance of curve")
                .defineInRange("distanceMax", 32, 32, 64);

        MID_MULTI = BUILDER.comment("Controls the middle control point of the curve.\nLower = More sag, Higher = Less sag")
                .defineInRange("midPosMultiplier", 0.6, 0.1, 0.9);

        STEP_RATE = BUILDER.comment("Density/spacing of blocks along the curve.\nLower = Slower but less chance of skipping blocks, Higher = Faster but more chance of skipping blocks")
                .defineInRange("stepRate", 0.0035, 0.001, 0.01);

        BUILDER.pop();
        BUILDER.push("features");

        COLUMN_CHANCE = BUILDER.comment("Chance of hanging flesh columns to spawn on tendons.")
                .defineInRange("columnChance", 50, 10, 100);

        BALL_OFFSET_MIN = BUILDER.comment("Minimum distance since last one before spawning a flesh ball.")
                .defineInRange("ballOffsetMin", 24, 8, 128);

        BALL_OFFSET_MAX = BUILDER.comment("Maximum distance since last one before spawning a flesh ball.")
                .defineInRange("ballOffsetMax", 64, 8, 128);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
