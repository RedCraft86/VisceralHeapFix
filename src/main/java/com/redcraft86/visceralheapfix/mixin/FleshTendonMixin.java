package com.redcraft86.visceralheapfix.mixin;

import biomesoplenty.init.ModTags;
import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.worldgen.feature.misc.FleshTendonFeature;

import com.redcraft86.visceralheapfix.CommonConfig;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FleshTendonFeature.class, remap = false)
public class FleshTendonMixin {
    @Unique
    private static final int MAX_FAIL_COUNT = 10;

    @Unique
    private static final int MAX_PILLAR_LIMIT = 250;

    @Unique
    private int nextBallAt = 0;

    @Unique
    private int sinceLastBall = 0;

    @Unique
    private final FleshTendonFeature thisObj = (FleshTendonFeature)(Object)this;

    @Shadow
    private static BlockPos quadratic(float t, BlockPos v0, BlockPos v1, BlockPos v2) {
        throw new AbstractMethodError("Shadow");
    }

    // m_142674_
    @Inject(method = "place", at = @At("HEAD"), cancellable = true, remap = true)
    private void onPlace(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        RandomSource rand = context.random();
        if (rand.nextInt(100) >= CommonConfig.tendonChance) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        final int MAX_Y = level.getMaxBuildHeight() - 1;

        if (!isFleshBlock(level, origin.below())) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        int xOff = rand.nextInt(CommonConfig.maxDistance * 2) - CommonConfig.maxDistance;
        int zOff = rand.nextInt(CommonConfig.maxDistance * 2) - CommonConfig.maxDistance;
        int minX = rand.nextBoolean() ? CommonConfig.minDistance : -CommonConfig.minDistance;
        int minZ = rand.nextBoolean() ? CommonConfig.minDistance : -CommonConfig.minDistance;
        BlockPos endPos = origin.offset(Math.abs(xOff) < CommonConfig.minDistance ? minX : xOff,
                origin.getY(), Math.abs(zOff) < CommonConfig.minDistance ? minZ : zOff);

        while (level.isEmptyBlock(endPos) && endPos.getY() < MAX_Y) {
            endPos = endPos.above(2);
        }

        // Gap must be higher than 5 blocks to generate
        if (Math.abs(origin.getY() - endPos.getY()) < 5) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        BlockPos midPos = endPos.offset(0, Mth.floor(-(endPos.getY() - origin.getY()) * CommonConfig.midPosMulti), 0);

        sinceLastBall = 0;
        int failCount = 0;
        BlockPos lastPos = null;
        for (float d = 0.0f; d < 1.0f; d += CommonConfig.tendonStep) {
            BlockPos curPos = quadratic(d, origin, midPos, endPos);
            if (curPos.getY() >= MAX_Y) {
                break;
            }

            // If we are already a flesh block, skip
            // This is to make sure the flesh balls are properly spaced out
            if (isFleshBlock(level, curPos)) {
                continue;
            }

            thisObj.setBlock(level, curPos, getFleshBlock(rand));
            if (level.isEmptyBlock(curPos)) {
                // Means the tendon is cutting off. We generally don't care if it's blocked since we go through it.
                // But we do care if it's stopping midair since it implies we're going out of the available bounds.
                failCount++;
                continue;
            } else if (failCount >= MAX_FAIL_COUNT) {
                break;
            }

            // Try to place glowing balls. Try columns if not.
            if (!tryPlaceBall(level, rand, curPos)) {
                tryPlaceColumn(level, rand, curPos.below());
            }

            lastPos = curPos;
        }

        // We didn't generate at all!?
        if (lastPos == null) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        int iterations = 0;
        lastPos = lastPos.above(); // Above last successful position, where potential air is
        do {
            if (!thisObj.setBlock(level, lastPos, getFleshBlock(rand))) {
                // Something seriously do not want us going further, just give up.
                break;
            }

            // This check is a duct tape solution but solves the issue of this being stopped due to the balls.
            if (iterations > 4) {
                tryPlaceBall(level, rand, lastPos.below(4));
            }

            iterations++;
            lastPos = lastPos.above();
        } while (level.isEmptyBlock(lastPos.above()) && iterations < MAX_PILLAR_LIMIT);

        // Cap it off with a ball if we can have one
        if (sinceLastBall > 4) {
            thisObj.generateFleshBall(level, lastPos.below(), rand);
        }

        cir.setReturnValue(true);
        cir.cancel();
    }

    private boolean isFleshBlock(WorldGenLevel level, BlockPos pos) {
        BlockState block = level.getBlockState(pos);
        return block.is(ModTags.Blocks.FLESH);
    }

    private BlockState getFleshBlock(RandomSource rand) {
        return rand.nextInt(5) == 0
                ? BOPBlocks.POROUS_FLESH.defaultBlockState()
                : BOPBlocks.FLESH.defaultBlockState();
    }

    private boolean tryPlaceBall(WorldGenLevel level, RandomSource rand, BlockPos pos) {
        if (sinceLastBall >= nextBallAt) {
            sinceLastBall = 0;
            nextBallAt = rand.nextInt(CommonConfig.ballOffsetMin, CommonConfig.ballOffsetMax);
            thisObj.generateFleshBall(level, pos, rand);
            return true;
        } else {
            sinceLastBall++;
            return false;
        }
    }

    private void tryPlaceColumn(WorldGenLevel level, RandomSource rand, BlockPos pos) {
        if (rand.nextInt(100) < CommonConfig.columnChance) {
            thisObj.placeFleshTendonColumn(level, rand, pos);
        }
    }
}
