package com.swbr.orespawn.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code Lavafoam} (Lavafoam.java:13-126): Lava Foam, id {@code lavafoam} (OreSpawnMain.java:1281).
 * A slippery Nether block that throws living things off its sides and hurts them if it throws hard.
 *
 * <p>The collision box is inset by {@code 0.0125} on the four sides (:117-120), which is what lets an
 * entity walking against the block enter its cell and trigger the contact callback; the callback then
 * pushes it away along the nearest axis (:72-107). {@code tickRate()} (:24-26) was dead code; random ticks
 * were requested (:20) with no {@code updateTick} and are kept. Drops itself.
 */
public class Lavafoam extends Block {

    /** {@code getCollisionBoundingBoxFromPool} (:117-120): {@code f = 0.0125}, i.e. 0.2/16 on each side, full height. */
    private static final VoxelShape COLLISION = Block.box(0.2, 0.0, 0.2, 15.8, 16.0, 15.8);

    /** The decompiled literals (:88-101): {@code (double) 0.45f} and {@code (double) 1.35f}. */
    private static final double PUSH = 0.44999998807907104;
    private static final double BOOST = 1.350000023841858;

    public Lavafoam(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock}, hardness 5, resistance 5, random ticks, {@code slipperiness = 1.1} (:16-21). */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, Legacy.resistance(5.0f))
                .requiresCorrectToolForDrops()
                .randomTicks()
                .friction(1.1f);
    }

    /** {@code randomDisplayTick} (:29-33): one in twenty ticks. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.random.nextInt(20) == 0) {
            sparkle(level, pos);
        }
    }

    /** {@code sparkle} (:35-70): six positions, {@code nextInt(10)}: 1 smoke, 2 redstone, anything else nothing. */
    private void sparkle(Level level, BlockPos pos) {
        Sparkle.sixSides(level, pos, (l, x, y, z) -> {
            int which = l.random.nextInt(10);
            if (which == 1) {
                l.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            }
            if (which == 2) {
                l.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
            }
        });
    }

    /**
     * {@code onEntityCollidedWithBlock} (:72-107). Runs on both sides like the original, and like the
     * original it does not mark a player's velocity for sync: the client player runs this itself, and
     * {@code hurt} only counts on the server ({@code LivingEntity.hurt} returns false on the client, as
     * {@code attackEntityFrom} did in 1.7.10).
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        final double pi = 3.14159;
        final double pi2 = pi / 2.0;
        final double pi3 = pi / 4.0;
        super.entityInside(state, level, pos, entity);
        if (entity == null) {
            return;
        }
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        // (int + 0.5f) is the original's float arithmetic (:79), kept 1:1: beyond |coordinate| 2^23 a float
        // cannot hold the .5 and the chosen side becomes a rounding artefact - seen in the game-test world at
        // x ~ 1.3e7, irrelevant inside any playable distance.
        double d = Math.atan2(entity.getX() - (pos.getX() + 0.5f), entity.getZ() - (pos.getZ() + 0.5f));
        if (d < 0.0) {
            d += pi * 2.0;
        }
        Vec3 motion = entity.getDeltaMovement();
        double motionX = motion.x;
        double motionZ = motion.z;
        if (d > pi2 - pi3 && d < pi2 + pi3) {
            motionX = PUSH;
            motionZ *= BOOST;
        } else if (d > pi - pi3 && d < pi + pi3) {
            motionZ = -PUSH;
            motionX *= BOOST;
        } else if (d > pi + pi2 - pi3 && d < pi + pi2 + pi3) {
            motionX = -PUSH;
            motionZ *= BOOST;
        } else {
            motionZ = PUSH;
            motionX *= BOOST;
        }
        entity.setDeltaMovement(motionX, motion.y, motionZ);
        d = Math.sqrt(motionZ * motionZ + motionX * motionX);
        if (d > 1.0) {
            entity.hurt(level.damageSources().fall(), (float) d);
        }
    }

    /**
     * {@code dropBlockAsItemWithChance} (:109-115): {@code 5 + nextInt(5) + nextInt(5)} experience, only in
     * dimension -1, on every drop path including explosions of any cause. Silk Touch skipped the method
     * in 1.7.10 ({@code canSilkHarvest} true for a normal cube); {@code stack} carries that here.
     *
     * <p>PORT: {@code dropExperience} is ignored - see {@link Legacy#dropXpOnBlockBreak}.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        int j1 = 5 + level.random.nextInt(5) + level.random.nextInt(5);
        if (level.dimension() == Level.NETHER) {
            Legacy.dropXpOnBlockBreak(this, level, pos, stack, j1);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }
}
