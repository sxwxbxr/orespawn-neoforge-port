package com.swbr.orespawn.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * The 1.7.10 vanilla {@code BehaviorProjectileDispense} ({@code kk} in client-1.7.10.jar), which
 * the six OreSpawn projectile dispenser behaviours extended. No original OreSpawn class.
 *
 * <p>Why not 1.21.1's {@code ProjectileDispenseBehavior}: it moves the dispense point 0.1 up and
 * shoots straight along the facing, while 1.7.10 dispensed from 0.7 along the facing without offset
 * and tilted the heading 0.1 upwards ({@code cr.d() + 0.1f}). Velocity 1.1, inaccuracy 6.0, sound
 * event 1002 and the smoke of the default behaviour are unchanged.
 */
public abstract class BehaviorProjectileDispense extends DefaultDispenseItemBehavior {

    /** kk.b(ck, add). */
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Level world = source.level();
        Position position = DispenserBlock.getDispensePosition(source);
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        Projectile projectile = this.getProjectileEntity(world, position);
        projectile.shoot((double) facing.getStepX(), (double) ((float) facing.getStepY() + 0.1F), (double) facing.getStepZ(),
                this.getProjectileVelocity(), this.getProjectileInaccuracy());
        world.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }

    /** kk.a(ck): level event 1002, the bow click. */
    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }

    /** {@code getProjectileEntity(World, IPosition)}. */
    protected abstract Projectile getProjectileEntity(Level world, Position position);

    /** kk.a() = 6.0. */
    protected float getProjectileInaccuracy() {
        return 6.0F;
    }

    /** kk.b() = 1.1. */
    protected float getProjectileVelocity() {
        return 1.1F;
    }
}
