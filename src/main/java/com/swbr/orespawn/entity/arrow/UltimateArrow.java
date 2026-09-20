package com.swbr.orespawn.entity.arrow;

import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.UltimateArrow} (UltimateArrow.java:15-323), registry id
 * {@code ultimate_arrow} (OreSpawnMain.java:5016, {@code registerGlobalEntityID} only - the vanilla
 * tracker handled it as an {@code EntityArrow}: 64 blocks, every 20 ticks, no velocity updates,
 * bytecode {@code mn.a(sa)}). Shot by the Ultimate Bow (UltimateBow.java:39, speed 3.0) and by
 * Girlfriend and Boyfriend (Girlfriend.java:990, Boyfriend.java:890; speed 2.0, spread 10.0).
 * The shared physics copy is {@link LegacyArrow}; this class holds what differs from
 * {@link IrukandjiArrow} (verhalten/entity-13.md):
 *
 * <ul>
 *   <li>damage {@code ceil(speed * UltimateBowDamage)} (:187-188), {@code getDamage} =
 *       {@code UltimateBowDamage} (default 10, clamped 2..20, {@link TweakStats});</li>
 *   <li>the PvP guard heals the protected target by 1 (:193, :201);</li>
 *   <li>gone after 500 ticks in the ground, on both sides (:121-126);</li>
 *   <li>the block hit remembers the block and calls its {@code onEntityCollidedWithBlock}
 *       ({@code BlockState.entityInside}) - wooden buttons, pressure plates, tripwire (:252,
 *       :265-267).</li>
 * </ul>
 */
public class UltimateArrow extends LegacyArrow {

    /** {@code inTile} (:20); {@code inData} (:21) was written (:253) and never read. */
    private BlockState inTile = Blocks.AIR.defaultBlockState();

    /** {@code UltimateArrow(World)} (:27-36) - the registry factory. */
    public UltimateArrow(EntityType<? extends UltimateArrow> type, Level level) {
        super(type, level);
    }

    /** {@code UltimateArrow(World, double, double, double)} (:38-47). */
    public UltimateArrow(Level level, double x, double y, double z) {
        super(ModEntities.ULTIMATE_ARROW.get(), level, x, y, z);
    }

    /** {@code UltimateArrow(World, EntityLiving, EntityLivingBase, float, float)} (:49-58): Girlfriend and Boyfriend. */
    public UltimateArrow(Level level, LivingEntity shooter, LivingEntity target, float speed, float spread) {
        super(ModEntities.ULTIMATE_ARROW.get(), level, shooter, target, speed, spread);
    }

    /** {@code UltimateArrow(World, EntityPlayer, float)} (:60-69): the Ultimate Bow. */
    public UltimateArrow(Level level, LivingEntity shooter, float speed) {
        super(ModEntities.ULTIMATE_ARROW.get(), level, shooter, speed);
    }

    /** :187-188. */
    @Override
    protected float hitDamage(float speed) {
        return (float) Mth.ceil(speed * (double) TweakStats.UltimateBowDamage());
    }

    /** :193 / :201: {@code heal(1.0f)}. */
    @Override
    protected void onPvpProtected(LivingEntity target) {
        target.heal(1.0f);
    }

    /** :121: any non-air block keeps it. */
    @Override
    protected boolean isStillStuck(BlockState state) {
        return !state.isAir();
    }

    /** :122-125. */
    @Override
    protected void tickInGround(int ticksInGround) {
        if (ticksInGround == 500) {
            this.discard();
        }
    }

    /** :252. */
    @Override
    protected void onTileSet(BlockPos pos, BlockState state) {
        this.inTile = state;
    }

    /** :265-267. */
    @Override
    protected void afterBlockHit(BlockPos pos) {
        if (!this.inTile.isAir()) {
            this.inTile.entityInside(this.level(), pos, this);
        }
    }

    /** {@code getDamage} (:320-322). */
    @Override
    public double getBaseDamage() {
        return TweakStats.UltimateBowDamage();
    }
}
