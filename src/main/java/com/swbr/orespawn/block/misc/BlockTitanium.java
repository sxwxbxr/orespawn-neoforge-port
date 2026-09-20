package com.swbr.orespawn.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code BlockTitanium} (BlockTitanium.java:11-76): Titanium Block, id {@code blocktitanium}
 * (OreSpawnMain.java:1279). A storage block that throws flame, smoke and redstone sparks now and then.
 * {@code tickRate()} (:21-23) never overrode anything and stays out. Drops itself.
 */
public class BlockTitanium extends Block {

    public BlockTitanium(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock}, hardness 5, resistance 5, light 0.5 (:14-18). */
    public static BlockBehaviour.Properties originalProperties() {
        return originalProperties(0.5f);
    }

    /** Shared with {@link BlockUranium}, which differs only in the light level. */
    protected static BlockBehaviour.Properties originalProperties(float lightLevel) {
        int light = Legacy.light(lightLevel);
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, Legacy.resistance(5.0f))
                .lightLevel(state -> light)
                .requiresCorrectToolForDrops();
    }

    /** {@code randomDisplayTick} (:26-30): one in twenty ticks. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.random.nextInt(20) == 0) {
            sparkle(level, pos);
        }
    }

    /** {@code sparkle} (:32-70): six positions, each {@code flame}, {@code smoke} or {@code reddust}. */
    private void sparkle(Level level, BlockPos pos) {
        Sparkle.sixSides(level, pos, (l, x, y, z) -> {
            int which = l.random.nextInt(3);
            if (which == 0) {
                l.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            }
            if (which == 1) {
                l.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            }
            if (which == 2) {
                l.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
            }
        });
    }
}
