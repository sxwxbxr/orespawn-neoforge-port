package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.entity.projectile.SunspotUrchin;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

/** Port of {@code danger.orespawn.MyDispenserBehaviorSunspotUrchin}: dispenses {@code sunspoturchin} as a {@link SunspotUrchin}. */
final class MyDispenserBehaviorSunspotUrchin extends BehaviorProjectileDispense {

    /** MyDispenserBehaviorSunspotUrchin.java:9-12. */
    @Override
    protected Projectile getProjectileEntity(Level world, Position position) {
        return new SunspotUrchin(world, position.x(), position.y(), position.z());
    }
}
