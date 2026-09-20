package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.entity.projectile.WaterBall;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

/** Port of {@code danger.orespawn.MyDispenserBehaviorWDCharge} ("WD" = WaterDragon): dispenses {@code waterball} as a {@link WaterBall}. */
final class MyDispenserBehaviorWDCharge extends BehaviorProjectileDispense {

    /** MyDispenserBehaviorWDCharge.java:9-12. */
    @Override
    protected Projectile getProjectileEntity(Level world, Position position) {
        return new WaterBall(world, position.x(), position.y(), position.z());
    }
}
