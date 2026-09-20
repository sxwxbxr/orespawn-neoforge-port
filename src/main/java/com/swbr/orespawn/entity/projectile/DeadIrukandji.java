package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.DeadIrukandji} (entity {@code dead_irukandji}): a {@link LaserBall}
 * with the Irukandji flag (which also sets the acid flag, LaserBall.java:90-93). 100 damage to
 * anything it touches, before every immunity check; a block hit drops the {@code deadirukandji}
 * item back on the server. No particles, no sound (verhalten/entity-06.md).
 */
public class DeadIrukandji extends LaserBall {

    /** Shadows LaserBall's own index, as in the original (DeadIrukandji.java:12). */
    private final int my_index = 86;

    /** Registry factory; {@code DeadIrukandji(World)}. */
    public DeadIrukandji(EntityType<? extends DeadIrukandji> type, Level level) {
        super(type, level);
        super.setIrukandji();
    }

    /** {@code DeadIrukandji(World, EntityLivingBase)} - ItemIrukandji. */
    public DeadIrukandji(Level level, LivingEntity thrower) {
        super(ModEntities.DEAD_IRUKANDJI.get(), level, thrower);
        super.setIrukandji();
    }

    /** {@code DeadIrukandji(World, double, double, double)} - dispenser. */
    public DeadIrukandji(Level level, double x, double y, double z) {
        super(ModEntities.DEAD_IRUKANDJI.get(), level, x, y, z);
        super.setIrukandji();
    }

    /** Spinner tile 86 (RenderItemUrchin.java:36-39). */
    public int getIrukandjiIndex() {
        return this.my_index;
    }
}
