package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.block.misc.NonOpaqueBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code OreCrystalCrystal} (OreCrystalCrystal.java:12-81), two blocks:
 * <ul>
 *   <li>{@code crystalcrystal} Pink Tourmaline (OreSpawnMain.java:1529, {@code (…, 0.4f, 12.0f, 40.0f)})</li>
 *   <li>{@code tigerseye} Tiger's Eye (OreSpawnMain.java:1530, {@code (…, 0.5f, 15.0f, 60.0f)})</li>
 * </ul>
 * The original told them apart with {@code this == OreSpawnMain.TigersEye} (:35, :56, :71); here that is
 * the {@code tigersEye} flag.
 *
 * <p>Cross model with cutout (:43-53), see {@link OreCrystal}. Drops itself: Pink Tourmaline one,
 * Tiger's Eye {@code nextInt(2)} = 0-1 ({@code quantityDropped}, :70-75) - in the loot tables. No Silk
 * Touch special case ({@code canSilkHarvest} false).
 */
public class OreCrystalCrystal extends NonOpaqueBlock {

    private final boolean tigersEye;

    public OreCrystalCrystal(BlockBehaviour.Properties properties, boolean tigersEye) {
        super(properties);
        this.tigersEye = tigersEye;
    }

    /** Constructor {@code (int, float lv, float f1, float f2)} (:14-21), identical to {@link OreCrystal}. */
    public static BlockBehaviour.Properties originalProperties(float lv, float f1, float f2) {
        int light = Legacy.light(lv);
        return Legacy.notANormalCube(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(f1, Legacy.resistance(f2))
                .lightLevel(state -> light)
                .requiresCorrectToolForDrops()
                .randomTicks());
    }

    /** {@code randomDisplayTick} (:24-28): one in twenty ticks, exactly one particle. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.random.nextInt(20) == 0) {
            sparkle(level, pos);
        }
    }

    /** {@code sparkle} (:30-41): {@code flame} for Tiger's Eye, {@code fireworksSpark} otherwise. */
    private void sparkle(Level level, BlockPos pos) {
        RandomSource rand = level.random;
        final float dx = 0.5f;
        final float dz = 0.5f;
        final float dy = 0.5f;
        level.addParticle(this.tigersEye ? ParticleTypes.FLAME : ParticleTypes.FIREWORK,
                pos.getX() + dx, pos.getY() + (double) dy, pos.getZ() + dz,
                (rand.nextFloat() - rand.nextFloat()) / 4.0f,
                (rand.nextFloat() - rand.nextFloat()) / 4.0f,
                (rand.nextFloat() - rand.nextFloat()) / 4.0f);
    }

    /**
     * {@code onBlockDestroyedByPlayer} (:55-60): only Pink Tourmaline, with {@code nextInt(10) == 1} an
     * explosion of 1.0 with fire, block damage per {@code mobGriefing}. Order and sides as in
     * {@link OreCrystal#onDestroyedByPlayer}.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (!this.tigersEye && !level.isClientSide && level.random.nextInt(10) == 1) {
            level.explode(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 1.0f, true,
                    Level.ExplosionInteraction.MOB);
        }
        return removed;
    }

    /**
     * {@code dropBlockAsItemWithChance} (:62-68): {@code 5 + nextInt(5) + nextInt(10)} experience below
     * y 40, on every drop path including explosions - and under Silk Touch, because {@code canSilkHarvest}
     * was false for this non-normal cube (hence {@code ItemStack.EMPTY} instead of the tool).
     *
     * <p>PORT: {@code dropExperience} is ignored - see {@link Legacy#dropXpOnBlockBreak}.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        int j1 = 5 + level.random.nextInt(5) + level.random.nextInt(10);
        if (pos.getY() < 40) {
            Legacy.dropXpOnBlockBreak(this, level, pos, ItemStack.EMPTY, j1);
        }
    }
}
