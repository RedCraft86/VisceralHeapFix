package com.redcraft86.visceralheapfix.mixin;

import biomesoplenty.init.ModTags;
import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.util.SimpleBlockPredicate;
import biomesoplenty.worldgen.feature.misc.FleshTendonFeature;

import com.redcraft86.visceralheapfix.CommonCfg;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FleshTendonFeature.class, remap = false)
public class FleshTendonMixin {
    @Unique private static final int MAX_PILLAR_LIMIT = 200;

    @Unique private final FleshTendonFeature thisObj = (FleshTendonFeature)(Object)this;
    @Unique private int nextBallIn = 0;

    @Shadow protected SimpleBlockPredicate replace;

    @Shadow private static BlockPos quadratic(float t, BlockPos v0, BlockPos v1, BlockPos v2) {
        throw new RuntimeException("Shadowed method should be implemented in mixin target!");
    }

    @Shadow private boolean respectsCutoff(WorldGenRegion region, BlockPos pos) {
        throw new RuntimeException("Shadowed method should be implemented in mixin target!");
    }

    @Unique private boolean tryPlaceBlock(WorldGenLevel level, RandomSource rand, BlockPos pos) {
        return thisObj.setBlock(level, pos, rand.nextInt(5) == 0
                ? BOPBlocks.POROUS_FLESH.defaultBlockState()
                : BOPBlocks.FLESH.defaultBlockState()
        );
    }

    @Unique private boolean tryPlaceColumn(WorldGenLevel level, RandomSource rand, BlockPos pos) {
        if (rand.nextInt(100) < CommonCfg.COLUMN_CHANCE.get()) {
            thisObj.placeFleshTendonColumn(level, rand, pos);
            return true;
        }
        return false;
    }

    @Unique private boolean tryPlaceBall(WorldGenLevel level, RandomSource rand, BlockPos pos) {
        if (nextBallIn <= 0) {
            nextBallIn = rand.nextInt(CommonCfg.BALL_OFFSET_MIN.get(), CommonCfg.BALL_OFFSET_MAX.get());
            thisObj.generateFleshBall(level, pos, rand);
            return true;
        } else {
            nextBallIn--;
            return false;
        }
    }

    @Inject(method = "place", at = @At("HEAD"), cancellable = true, remap = true)
    private void onPlace(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        final WorldGenLevel level = context.level();
        final BlockPos startPos = context.origin();

        final int BUILD_LIMIT = level.getMaxBuildHeight() - 1;
        final int GEN_LIMIT = CommonCfg.MAX_HEIGHT_OFFSET.get() + (
                CommonCfg.LIMIT_LOGICAL_HEIGHT.get() ? level.dimensionType().logicalHeight() : BUILD_LIMIT
        );

        // Don't generate if above the max gen height
        if (startPos.getY() > GEN_LIMIT) {
            cir.setReturnValue(false);
            return;
        }

        final RandomSource rand = context.random();

        // Not to self: This is doing >= unlike tryPlaceColumn because in this case evaluating true means cancelling
        if (rand.nextInt(100) >= CommonCfg.TENDON_CHANCE.get()) {
            cir.setReturnValue(false);
            return;
        }

        // Don't generate on irrelevant blocks
        if (!level.getBlockState(startPos.below()).is(ModTags.Blocks.FLESH)) {
            cir.setReturnValue(false);
            return;
        }

        final int minDist = CommonCfg.MIN_DISTANCE.get(), maxDist = CommonCfg.MAX_DISTANCE.get();
        final int minX = rand.nextBoolean() ? minDist : -minDist;
        final int minZ = rand.nextBoolean() ? minDist : -minDist;
        final int xOff = rand.nextInt(maxDist * 2) - maxDist;
        final int zOff = rand.nextInt(maxDist * 2) - maxDist;

        BlockPos endPos = startPos.offset(
                Math.abs(xOff) < minDist ? minX : xOff,
                startPos.getY(),
                Math.abs(zOff) < maxDist ? minZ : zOff
        );

        // Figure out the Y for endPos
        while (level.isEmptyBlock(endPos) && endPos.getY() < BUILD_LIMIT) {
            endPos = endPos.above();
        }

        // Gap must be at least 5 blocks to generate
        if (endPos.getY() - startPos.getY() < 5) {
            cir.setReturnValue(false);
            return;
        }

        BlockPos midPos = endPos.offset(0, Mth.floor(
                (startPos.getY() - endPos.getY()) * CommonCfg.MID_MULTI.get()
        ), 0);

        int sinceLastBall = 0;
        BlockPos lastPos = null;
        for (float d = 0.0f; d < 1.0f; d += CommonCfg.STEP_RATE.get()) {
            BlockPos curPos = quadratic(d, startPos, midPos, endPos);
            if (curPos.getY() > GEN_LIMIT) {
                break;
            }

            if (tryPlaceBlock(level, rand, curPos)) {
                lastPos = curPos;
                if (tryPlaceBall(level, rand, curPos)) {
                    sinceLastBall = 0;
                } else {
                    tryPlaceColumn(level, rand, curPos.below());
                    sinceLastBall++;
                }
            } else {
                BlockState state = level.getBlockState(curPos);
                if (!replace.test(level, curPos) && !state.is(ModTags.Blocks.FLESH)) {
                    // If failing to place and target is not replaceable nor flesh, likely hit a ceiling
                    lastPos = null;
                    break;
                }
            }
        }

        // Forcefully cancelled or didn't spawn
        if (lastPos == null) {
            cir.setReturnValue(true);
            return;
        }

        for (int i = 1; i < MAX_PILLAR_LIMIT; i++) {
            BlockPos curPos = lastPos.above();
            if (curPos.getY() > GEN_LIMIT) {
                break;
            }

            if (tryPlaceBlock(level, rand, curPos)) {
                lastPos = curPos;
                if (tryPlaceBall(level, rand, curPos)) {
                    sinceLastBall = 0;
                } else {
                    sinceLastBall++;
                }
            } else {
                // Likely intersecting a ball so try and skip up to 5 blocks
                for (int j = 1; j < 5 && !replace.test(level, curPos); j++) {
                    curPos = curPos.above();
                }

                // There's a chance the loop will overshoot it... for some reason
                BlockPos belowPos = curPos.below();
                if (replace.test(level, belowPos)) {
                    lastPos = belowPos;
                } else if (replace.test(level, curPos)) {
                    lastPos = curPos; // Loop didn't overshoot
                } else {
                    // If no replaceable space after skipping, it is likely a ceiling
                    break;
                }
            }
        }

        // Cap it off with a ball if we can have one
        if (sinceLastBall > 6) {
            thisObj.generateFleshBall(level, lastPos, rand);
        }

        cir.setReturnValue(true);
    }
}