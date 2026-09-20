package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.Acid} (entity {@code acid}): a {@link LaserBall} with the acid
 * flag (Acid.java:13). 16 damage plus one second of fire; TrooperBug and SpitBug are immune, robots,
 * ridden dragons and riding players are not; no particles, no sound, no explosion
 * (verhalten/entity-04.md). Thrown by ItemAcid, the dispenser and SpitBug.
 */
public class Acid extends LaserBall {

    /** Shadows LaserBall's own index, as in the original (Acid.java:12). */
    private final int my_index = 85;

    /** Registry factory; {@code Acid(World)}. */
    public Acid(EntityType<? extends Acid> type, Level level) {
        super(type, level);
        super.setAcid();
    }

    /** {@code Acid(World, EntityLivingBase)} - ItemAcid. */
    public Acid(Level level, LivingEntity thrower) {
        super(ModEntities.ACID.get(), level, thrower);
        super.setAcid();
    }

    /** {@code Acid(World, double, double, double)} - dispenser, SpitBug. */
    public Acid(Level level, double x, double y, double z) {
        super(ModEntities.ACID.get(), level, x, y, z);
        super.setAcid();
    }

    /** Spinner tile 85 (RenderItemUrchin.java:32-35). */
    public int getAcidIndex() {
        return this.my_index;
    }
}
