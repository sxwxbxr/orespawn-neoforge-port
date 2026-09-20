package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.entity.projectile.DeadIrukandji;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

/** Port of {@code danger.orespawn.MyDispenserBehaviorDeadIrukandji}: dispenses {@code deadirukandji} as a {@link DeadIrukandji}. */
final class MyDispenserBehaviorDeadIrukandji extends BehaviorProjectileDispense {

    /** MyDispenserBehaviorDeadIrukandji.java:9-12. */
    @Override
    protected Projectile getProjectileEntity(Level world, Position position) {
        return new DeadIrukandji(world, position.x(), position.y(), position.z());
    }
}
