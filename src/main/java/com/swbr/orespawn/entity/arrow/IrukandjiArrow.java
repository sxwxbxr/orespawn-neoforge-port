package com.swbr.orespawn.entity.arrow;

import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.IrukandjiArrow} (IrukandjiArrow.java:15-317), registry id
 * {@code irukandji_arrow} (OreSpawnMain.java:5020, {@code registerGlobalEntityID} only; vanilla
 * arrow tracking 64/20/no velocity). Shot by the Skate Bow (SkateBow.java:38) and dispensers
 * ({@code item.bow.MyDispenserBehaviorArrow}). Shared physics in {@link LegacyArrow}; what differs
 * from {@link UltimateArrow} (verhalten/entity-09.md):
 *
 * <ul>
 *   <li>fixed damage 100, independent of speed (:188), {@code getDamage} = 100 (:314-316);</li>
 *   <li>the PvP guard only removes the arrow, no heal (:190-203);</li>
 *   <li>stays stuck only while the block keeps its metadata (:121), and after 50 ticks in the
 *       ground the server drops one {@code irukandjiarrow} and removes the arrow (:122-126);</li>
 *   <li>no {@code inTile} bookkeeping and no {@code onEntityCollidedWithBlock} on a block hit
 *       (:247-261).</li>
 * </ul>
 *
 * <p>No renderer was registered for this entity (catalogue 6.6); 1.7.10 fell back to the parent's
 * {@code RenderArrow} - see {@code client.item.bertha.ArrowRenderers}.
 */
public class IrukandjiArrow extends LegacyArrow {

    /**
     * {@code inData} (:21): the metadata of the hit block.
     *
     * <p>PORT: metadata no longer exists; the whole block state stands in for it. Stricter in one
     * corner: the original ignored which block was there as long as the metadata matched, the port
     * also releases the arrow when the block is swapped for another one.
     */
    @Nullable
    private BlockState inData = null;

    /** {@code IrukandjiArrow(World)} (:27-36) - the registry factory. */
    public IrukandjiArrow(EntityType<? extends IrukandjiArrow> type, Level level) {
        super(type, level);
    }

    /** {@code IrukandjiArrow(World, double, double, double)} (:38-47): the dispenser's arrow. */
    public IrukandjiArrow(Level level, double x, double y, double z) {
        super(ModEntities.IRUKANDJI_ARROW.get(), level, x, y, z);
    }

    /** {@code IrukandjiArrow(World, EntityLiving, EntityLivingBase, float, float)} (:49-58); no caller in 20.3. */
    public IrukandjiArrow(Level level, LivingEntity shooter, LivingEntity target, float speed, float spread) {
        super(ModEntities.IRUKANDJI_ARROW.get(), level, shooter, target, speed, spread);
    }

    /** {@code IrukandjiArrow(World, EntityPlayer, float)} (:60-69): the Skate Bow. */
    public IrukandjiArrow(Level level, LivingEntity shooter, float speed) {
        super(ModEntities.IRUKANDJI_ARROW.get(), level, shooter, speed);
    }

    /** :188. */
    @Override
    protected float hitDamage(float speed) {
        return 100.0f;
    }

    /** :190-203: sound and removal only. */
    @Override
    protected void onPvpProtected(LivingEntity target) {
    }

    /** :121: {@code var4 != Blocks.air && var5 == this.inData}. */
    @Override
    protected boolean isStillStuck(BlockState state) {
        return !state.isAir() && state == this.inData;
    }

    /** :122-126: {@code dropItem(MyIrukandjiArrow, 1)} is {@code entityDropItem(stack, 0.0f)}. */
    @Override
    protected void tickInGround(int ticksInGround) {
        if (ticksInGround == 50 && !this.level().isClientSide) {
            this.spawnAtLocation(new ItemStack(ModItems.IRUKANDJI_ARROW.get(), 1), 0.0f);
            this.discard();
        }
    }

    /** :250. */
    @Override
    protected void onTileSet(BlockPos pos, BlockState state) {
        this.inData = state;
    }

    /** Nothing: :247-261 has no collision call. */
    @Override
    protected void afterBlockHit(BlockPos pos) {
    }

    /** {@code getDamage} (:314-316). */
    @Override
    public double getBaseDamage() {
        return 100.0;
    }
}
