package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.entity.projectile.IceBall;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

/** Port of {@code danger.orespawn.MyDispenserBehaviorIceball}: dispenses {@code iceball} as an {@link IceBall}. */
final class MyDispenserBehaviorIceball extends BehaviorProjectileDispense {

    /** MyDispenserBehaviorIceball.java:9-12. */
    @Override
    protected Projectile getProjectileEntity(Level world, Position position) {
        return new IceBall(world, position.x(), position.y(), position.z());
    }
}
