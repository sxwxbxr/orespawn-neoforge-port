package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.block.misc.Sparkle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port of {@code OreTitanium} (OreTitanium.java:13-113): Titanium Ore, id {@code oretitanium}
 * (OreSpawnMain.java:1275). Touching it makes every Titanium Ore in view sparkle red for a while.
 *
 * <p>{@code glowing} and {@code glowcount} were instance fields of the block singleton (:15-16), so the
 * state is global per block type and separate per side. 1.21.1 blocks are singletons too, so the fields
 * stay where they were - which is exactly the "static field read by the display code" of DECISIONS R18.
 * The original never emitted light ({@code setLightLevel} is absent); the glow is particles only.
 *
 * <p>{@code tickRate()} (:29-31) and the empty {@code updateTick} (:54-55) were dead or empty; the block
 * still asked for random ticks (:25), which is kept.
 */
public class OreTitanium extends Block {

    private boolean glowing;
    private int glowcount;
    /** What {@code glow()} resets the counter to: 5 here (:50), 10 in {@link OreUranium}. */
    private final int glowTicks;

    public OreTitanium(BlockBehaviour.Properties properties) {
        this(properties, 5);
    }

    protected OreTitanium(BlockBehaviour.Properties properties, int glowTicks) {
        super(properties);
        this.glowing = false;
        this.glowcount = 0;
        this.glowTicks = glowTicks;
    }

    /** {@code Material.rock}, hardness 15, resistance 5, random ticks (:19-26). Any pickaxe harvests rock. */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0f, Legacy.resistance(5.0f))
                .requiresCorrectToolForDrops()
                .randomTicks();
    }

    /** {@code onBlockClicked} (:33-36): left click. */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        glow(level, pos);
        super.attack(state, level, pos, player);
    }

    /**
     * {@code onEntityWalking} (:38-41), behind the guard 1.7.10 put in front of it: not a sneaking player,
     * not a rider ({@link Legacy#wasWalking}). PORT: 1.7.10 fired this once per step-sound distance;
     * {@code stepOn} fires on every {@code move} call while an entity is on the block, standing still
     * included. The counter is re-armed more often - the visible effect (sparkle while something stands
     * on it) is the same.
     */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (Legacy.wasWalking(entity)) {
            glow(level, pos);
        }
        super.stepOn(level, pos, state, entity);
    }

    /** {@code onBlockActivated} (:43-46): right click; the original returned {@code super} = false, so PASS. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        glow(level, pos);
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    /** {@code glow} (:48-52). Runs on both sides like the original; the particle call is a no-op on the server. */
    private void glow(Level level, BlockPos pos) {
        this.glowing = true;
        this.glowcount = this.glowTicks;
        sparkle(level, pos);
    }

    /** {@code updateTick} was empty (:54-55). */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    /** {@code randomDisplayTick} (:58-68), client only by construction. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.glowing) {
            sparkle(level, pos);
            if (this.glowcount > 0) {
                --this.glowcount;
            } else {
                this.glowing = false;
            }
        }
    }

    /** {@code sparkle} (:70-99): six positions, only {@code reddust}. */
    private void sparkle(Level level, BlockPos pos) {
        Sparkle.sixSides(level, pos, (l, x, y, z) -> l.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0));
    }

    /**
     * {@code dropBlockAsItemWithChance} (:101-107): {@code 5 + nextInt(5) + nextInt(10)} experience, only
     * below y 40 (absolute, R18), on every drop path including explosions. The 1.7.10 silk-touch path
     * skipped this method entirely ({@code canSilkHarvest} was true for a normal block); {@code stack}
     * carries that here.
     *
     * <p>PORT: {@code dropExperience} is ignored - see {@link Legacy#dropXpOnBlockBreak}.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        int j1 = 5 + level.random.nextInt(5) + level.random.nextInt(10);
        if (pos.getY() < 40) {
            Legacy.dropXpOnBlockBreak(this, level, pos, stack, j1);
        }
    }
}
