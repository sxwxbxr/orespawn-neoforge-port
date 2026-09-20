package com.swbr.orespawn.item.bow;

import com.swbr.orespawn.entity.arrow.IrukandjiArrow;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.MyDispenserBehaviorArrow} (MyDispenserBehaviorArrow.java:7-14),
 * registered for {@code irukandjiarrow} (OreSpawnMain.java:5415). The original only overrode
 * {@code getProjectileEntity} (:9-13); everything else is the inherited 1.7.10
 * {@code BehaviorProjectileDispense.dispenseStack}, written out here (verhalten/core-02.md,
 * MyDispenserBehaviorAcid): position = block centre + 0.7 along the facing, heading
 * {@code (fx, fy + 0.1, fz)} at 1.1 with spread 6.0, one item less, level event 1002.
 *
 * <p>PORT: not {@code ProjectileItem} + {@code DispenserBlock.registerProjectileBehavior}: vanilla
 * 1.21.1 lifts the <em>position</em> by 0.1 and shoots straight along the facing, 1.7.10 lifted the
 * <em>direction</em>. Same choice as {@code item.rock.MyDispenserBehaviorRock}. The spread is
 * ignored anyway: {@code IrukandjiArrow.setThrowableHeading} has none.
 */
public final class MyDispenserBehaviorArrow extends DefaultDispenseItemBehavior {

    /** {@code BehaviorProjectileDispense.dispenseStack}. */
    @Override
    protected ItemStack execute(final BlockSource source, final ItemStack stack) {
        final Level world = source.level();
        final Position iposition = DispenserBlock.getDispensePosition(source, 0.7, Vec3.ZERO);
        final Direction enumfacing = source.state().getValue(DispenserBlock.FACING);
        final IrukandjiArrow iprojectile = getProjectileEntity(world, iposition);
        iprojectile.shoot(enumfacing.getStepX(), enumfacing.getStepY() + 0.1f, enumfacing.getStepZ(), 1.1f, 6.0f);
        world.addFreshEntity(iprojectile);
        stack.split(1);
        return stack;
    }

    /**
     * {@code getProjectileEntity} (:9-13): {@code canBePickedUp = 1}. It has no effect - the arrow
     * can never be picked up (see {@code entity.arrow.LegacyArrow}).
     */
    private static IrukandjiArrow getProjectileEntity(final Level par1World, final Position par2IPosition) {
        final IrukandjiArrow entityarrow = new IrukandjiArrow(par1World, par2IPosition.x(), par2IPosition.y(), par2IPosition.z());
        entityarrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return entityarrow;
    }

    /** The inherited {@code playDispenseSound}: level event 1002 (shoot), not 1000. */
    @Override
    protected void playSound(final BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
