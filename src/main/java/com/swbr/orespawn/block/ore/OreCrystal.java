package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.block.misc.NonOpaqueBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
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
 * Port of {@code OreCrystal} (OreCrystal.java:12-80): Crystal Energy, id {@code crystalcoal}
 * (OreSpawnMain.java:1527, {@code (…, 0.6f, 6.0f, 20.0f)}).
 *
 * <p>Render type 1 (:49-51) is the cross model {@code minecraft:block/cross} with cutout; the block still
 * collides as a full cube. {@code isOpaqueCube} and {@code renderAsNormalBlock} false (:53-59) →
 * {@link NonOpaqueBlock} plus {@link Legacy#notANormalCube}. Random ticks were requested (:20) without an
 * {@code updateTick}; kept. Drops itself (no override); {@code canSilkHarvest} was false because
 * {@code renderAsNormalBlock} was false, so Silk Touch changes nothing here - not the drop and not the
 * experience.
 */
public class OreCrystal extends NonOpaqueBlock {

    public OreCrystal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** Constructor {@code (int, float lv, float f1, float f2)} (:14-21): light, hardness, resistance. */
    public static BlockBehaviour.Properties originalProperties(float lv, float f1, float f2) {
        int light = Legacy.light(lv);
        return Legacy.notANormalCube(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(f1, Legacy.resistance(f2))
                .lightLevel(state -> light)
                .requiresCorrectToolForDrops()
                .randomTicks());
    }

    /** {@code randomDisplayTick} (:24-28): one in five ticks, five particles from the centre. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.random.nextInt(5) == 0) {
            sparkle(level, pos);
        }
    }

    /** {@code sparkle} (:30-47). */
    private void sparkle(Level level, BlockPos pos) {
        RandomSource rand = level.random;
        final float dx = 0.5f;
        final float dz = 0.5f;
        final float dy = 0.5f;
        for (int j1 = 0; j1 < 5; ++j1) {
            int which = rand.nextInt(3);
            ParticleOptions particle = null;
            if (which == 0) {
                particle = ParticleTypes.FLAME;
            }
            if (which == 1) {
                particle = ParticleTypes.SMOKE;
            }
            if (which == 2) {
                particle = DustParticleOptions.REDSTONE;
            }
            level.addParticle(particle,
                    pos.getX() + dx, pos.getY() + (double) dy, pos.getZ() + dz,
                    (rand.nextFloat() - rand.nextFloat()) / 4.0f,
                    (rand.nextFloat() - rand.nextFloat()) / 4.0f,
                    (rand.nextFloat() - rand.nextFloat()) / 4.0f);
        }
    }

    /**
     * {@code onBlockDestroyedByPlayer} (:61-66): with {@code nextInt(3) == 1} an explosion of 1.5 with fire,
     * block damage only when {@code mobGriefing} is on. In 1.7.10 the block was already air when the hook
     * ran ({@code ItemInWorldManager.removeBlock}); {@code super} first keeps that order. Runs in creative
     * mode too, on both sides, with the server check inside - as the original.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (!level.isClientSide && level.random.nextInt(3) == 1) {
            level.explode(null, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 1.5f, true,
                    Level.ExplosionInteraction.MOB);
        }
        return removed;
    }

    /**
     * {@code dropBlockAsItemWithChance} (:68-74): {@code 5 + nextInt(5) + nextInt(10)} experience below
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
