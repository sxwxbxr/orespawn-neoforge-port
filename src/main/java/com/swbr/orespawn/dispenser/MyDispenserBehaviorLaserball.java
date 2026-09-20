package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.entity.projectile.LaserBall;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

/** Port of {@code danger.orespawn.MyDispenserBehaviorLaserball}: dispenses {@code laserball} as a plain {@link LaserBall}. */
final class MyDispenserBehaviorLaserball extends BehaviorProjectileDispense {

    /** MyDispenserBehaviorLaserball.java:9-12. */
    @Override
    protected Projectile getProjectileEntity(Level world, Position position) {
        return new LaserBall(world, position.x(), position.y(), position.z());
    }
}
