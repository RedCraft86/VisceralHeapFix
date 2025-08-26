package com.redcraft86.visceralheapfix;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ModClass.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ForgeConfigSpec.IntValue MIN_DISTANCE;
    private static final ForgeConfigSpec.IntValue MAX_DISTANCE;
    private static final ForgeConfigSpec.DoubleValue MID_MULTI;
    private static final ForgeConfigSpec.DoubleValue STEP_RATE;

    private static final ForgeConfigSpec.IntValue TENDON_CHANCE;
    private static final ForgeConfigSpec.IntValue COLUMN_CHANCE;
    private static final ForgeConfigSpec.IntValue BALL_OFFSET_MIN;
    private static final ForgeConfigSpec.IntValue BALL_OFFSET_MAX;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        TENDON_CHANCE = BUILDER.comment("Chance of tendons to spawn.")
            .defineInRange("tendonChance", 75, 10, 100);

        COLUMN_CHANCE = BUILDER.comment("Chance of hanging flesh columns to spawn on tendons.")
            .defineInRange("columnChance", 50, 10, 100);

        BALL_OFFSET_MIN = BUILDER.comment("Minimum distance since last one before spawning a flesh ball.")
            .defineInRange("ballOffsetMin", 16, 1, 64);

        BALL_OFFSET_MAX = BUILDER.comment("Maximum distance since last one before spawning a flesh ball.")
            .defineInRange("ballOffsetMax", 32, 1, 64);

        BUILDER.push("Generation");

        MIN_DISTANCE = BUILDER.comment("Minimum horizontal distance the tendon must curve.\nLower = Less chance of curve, Higher = More chance of curve")
            .defineInRange("distanceMin", 12, 4, 16);

        MAX_DISTANCE = BUILDER.comment("Maximum horizontal distance the tendon can curve.\nLower = Less chance of curve, Higher = More chance of curve")
            .defineInRange("distanceMax", 32, 32, 64);

        MID_MULTI = BUILDER.comment("Controls the middle control point of the curve.\nLower = More sag, Higher = Less sag")
            .defineInRange("midPosMultiplier", 0.7, 0.1, 0.9);

        STEP_RATE = BUILDER.comment("Density/spacing of blocks along the curve.\nLower = Slower but less chance of skipping blocks, Higher = Faster but more chance of skipping blocks")
            .defineInRange("stepRate", 0.0035, 0.001, 0.01);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int tendonChance = 75;
    public static int columnChance = 50;
    public static int ballOffsetMin = 16;
    public static int ballOffsetMax = 32;
    public static int minDistance = 12;
    public static int maxDistance = 32;
    public static float midPosMulti = 0.7f;
    public static float tendonStep = 0.0035f;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == SPEC) {
            tendonChance = TENDON_CHANCE.get();
            columnChance = COLUMN_CHANCE.get();
            ballOffsetMin = BALL_OFFSET_MIN.get();
            ballOffsetMax = BALL_OFFSET_MAX.get();
            minDistance = MIN_DISTANCE.get();
            maxDistance = MAX_DISTANCE.get();
            midPosMulti = MID_MULTI.get().floatValue();
            tendonStep = STEP_RATE.get().floatValue();

            if (ballOffsetMin > ballOffsetMax) {
                // Must've swapped on accident
                int temp = ballOffsetMax;
                ballOffsetMax = ballOffsetMin;
                ballOffsetMin = temp;
            } else if (ballOffsetMin == ballOffsetMax) {
                // Ensure a gap otherwise rand.nextInt() will whine about it instead of assuming a constant
                if (ballOffsetMax < 32) {
                    ballOffsetMax++;
                } else {
                    ballOffsetMin--;
                }
            }
        }
    }
}
