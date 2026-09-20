package com.swbr.orespawn.block.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockExperienceLeaves}: Experience Tree Leaves
 * ({@code leaves_experience}, OreSpawnMain.java:1609, hardness 0.2, light opacity 1, grass sound).
 *
 * <p>No item drops at all (BlockExperienceLeaves.java:27-33): the loot table holds only the
 * shears/silk-touch branch. {@code updateTick} (:35-74): radius 2. Sustained, at night only
 * (14000..22000): 1/65 a Bottle o' Enchanting appears two blocks above when the block above is air
 * (:51-56); 1/75 a thrown experience bottle starts one block below when that is air, aimed
 * (+-0.5, -0.1, +-0.5) at speed 0.4 with inaccuracy 5 (:57-64).
 *
 * <p>{@code randomDisplayTick} (:76-105): firework sparks between 13000 and 23000, more frequent
 * towards the edges of that window.
 */
public class BlockExperienceLeaves extends OreSpawnLeaves {

    public BlockExperienceLeaves(Properties properties) {
        super(properties);
    }

    @Override
    protected void sustained(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long t = level.getDayTime();
        t %= 24000L;
        if (t < 14000L || t > 22000L) {
            return;
        }
        if (random.nextInt(65) == 1) {
            final BlockState bid = level.getBlockState(pos.above());
            if (bid.isAir()) {
                // dropBlockAsItem(world, x, y + 2, z, new ItemStack(Items.experience_bottle))
                popResource(level, pos.above(2), new ItemStack(Items.EXPERIENCE_BOTTLE));
            }
        }
        if (random.nextInt(75) == 1) {
            final BlockState bid = level.getBlockState(pos.below());
            if (bid.isAir()) {
                final double x = pos.getX();
                final double y = pos.getY() - 1;
                final double z = pos.getZ();
                final ThrownExperienceBottle var11 = new ThrownExperienceBottle(level, x, y, z);
                var11.moveTo(x, y, z, 0.0f, 0.0f);
                var11.shoot((random.nextFloat() - random.nextFloat()) / 2.0f, -0.10000000149011612,
                        (random.nextFloat() - random.nextFloat()) / 2.0f, 0.4f, 5.0f);
                level.addFreshEntity(var11);
            }
        }
    }

    /** {@code randomDisplayTick} (BlockExperienceLeaves.java:76-105), client side. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        long t = level.getDayTime();
        t %= 24000L;
        if (t < 13000L || t > 23000L) {
            return;
        }
        int rate = 0;
        if (t < 14000L) {
            rate = (14000 - (int) t) / 2;
        }
        if (t > 22000L) {
            rate = (int) (t - 22000L) / 2;
        }
        if (random.nextInt(200 + rate) == 1) {
            final BlockState bid = level.getBlockState(pos.above());
            if (bid.isAir()) {
                for (int i = 0; i < 10; ++i) {
                    level.addParticle(ParticleTypes.FIREWORK, pos.getX(), pos.getY() + 1.25, pos.getZ(),
                            random.nextGaussian(), Math.abs(random.nextGaussian()), random.nextGaussian());
                }
            }
        }
        if (random.nextInt(40 + rate) == 1) {
            final BlockState bid = level.getBlockState(pos.below());
            if (bid.isAir()) {
                for (int i = 0; i < 4; ++i) {
                    level.addParticle(ParticleTypes.FIREWORK, pos.getX(), pos.getY() - 1.25, pos.getZ(),
                            random.nextFloat() - random.nextFloat(), -Math.abs(random.nextFloat()),
                            random.nextFloat() - random.nextFloat());
                }
            }
        }
    }
}
