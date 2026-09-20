package com.swbr.orespawn.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;

/**
 * The one port of 1.7.10 {@code EntityAIPanic} ({@code uz}, disassembled from {@code client-1.7.10.jar}). Before W12 it
 * lay five times - {@code EntityAnt} (W05), {@code CompanionSupport} (W04), {@code CritterSupport},
 * {@code HerbivoreSupport} and {@code CannonFodderSupport} (W06) - four of them textually identical (DECISIONS R21: no
 * duplicate 1.7.10 helpers).
 */
public final class LegacyPanic {

    private LegacyPanic() {
    }

    /**
     * {@code new EntityAIPanic(creature, speed)}: {@code shouldExecute} starts while the revenge target
     * ({@code getAITarget}, cleared 100 ticks after the hit by {@code LivingEntity.baseTick}) is set or the entity burns,
     * then {@code RandomPositionGenerator.findRandomTarget(entity, 5, 4)}, a random spot 5 wide and 4 high.
     *
     * <p>PORT: 1.21.1's {@link PanicGoal} asks the type of the last damage and walks a burning mob to water within 5
     * blocks before it looks for a random spot; both are replaced by the original trigger and target. The goal stays a
     * {@link PanicGoal} (start, stop, {@code canContinueToUse} unchanged), which the GameTests look up by class.
     *
     * <p>The former {@code CompanionSupport} copy overrode only {@code shouldPanic}, so it kept the water search. Girlfriend
     * and Boyfriend are {@code fireImmune()}, for which {@code Entity.isOnFire()} is always false, so the water branch
     * was never reached and this body is the same behaviour for them.
     */
    public static PanicGoal legacyPanic(final PathfinderMob creature, final double speed) {
        return new PanicGoal(creature, speed) {
            @Override
            public boolean canUse() {
                if (this.mob.getLastHurtByMob() == null && !this.mob.isOnFire()) {
                    return false;
                }
                return this.findRandomPosition();
            }
        };
    }
}
