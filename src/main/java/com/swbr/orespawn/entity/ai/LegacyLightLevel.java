package com.swbr.orespawn.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The one port of 1.7.10 {@code EntityMob.isValidLightLevel} ({@code yg.j_()Z}, disassembled from
 * {@code client-1.7.10.jar}). Before W08 it lay four times, in {@code LegacyEntityMob}, {@code DinoSupport},
 * {@code ArthropodSupport} and {@code RobotSupport}, all with the same rule (DECISIONS R21: no duplicate 1.7.10
 * helpers).
 */
public final class LegacyLightLevel {

    private LegacyLightLevel() {
    }

    /**
     * At {@code floor(posX), floor(boundingBox.minY), floor(posZ)}: the saved sky light above {@code rand.nextInt(32)}
     * refuses; otherwise {@code World.getBlockLightValue} - {@code max(sky - skylightSubtracted, block)}, with
     * {@code skylightSubtracted} forced to 10 while it thunders - must not exceed {@code rand.nextInt(8)}.
     *
     * <p>PORT: not {@code Monster.isDarkEnoughToSpawn}, which adds the 1.21.1 block-light limit of the dimension type
     * (0 in the overworld) and samples the light test from the dimension; the original allowed block light up to 7.
     * {@code getBlockLightValue} is {@code getMaxLocalRawBrightness}; 1.7.10's neighbour lookup for slabs and stairs
     * ({@code getBlockLightValue_do(..., true)}) is not reproduced. Placement predicates pass the spawn's random instead
     * of the entity's {@code rand}, because they run before an entity exists. The thunder test needs the server level; a
     * plain {@link LevelAccessor} that is not a {@link ServerLevelAccessor} is treated as not thundering (every caller
     * passes a server accessor on the server).
     */
    public static boolean isValidLightLevel(final LevelAccessor level, final BlockPos pos, final RandomSource random) {
        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) {
            return false;
        }
        final int l = level instanceof ServerLevelAccessor server && server.getLevel().isThundering()
                ? level.getMaxLocalRawBrightness(pos, 10)
                : level.getMaxLocalRawBrightness(pos);
        return l <= random.nextInt(8);
    }
}
