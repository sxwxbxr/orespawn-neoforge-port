package com.swbr.orespawn.dispenser;

import com.swbr.orespawn.entity.projectile.Acid;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

/** Port of {@code danger.orespawn.MyDispenserBehaviorAcid}: dispenses {@code acid} as an {@link Acid} (verhalten/core-02.md). */
final class MyDispenserBehaviorAcid extends BehaviorProjectileDispense {

    /** MyDispenserBehaviorAcid.java:9-12. */
    @Override
    protected Projectile getProjectileEntity(Level world, Position position) {
        return new Acid(world, position.x(), position.y(), position.z());
    }
}
