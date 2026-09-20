package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * Port of {@code danger.orespawn.MyEntityAIJealousy}: a tamed Girlfriend or Boyfriend attacks
 * <em>untamed</em> members of its own kind nearby. Users, only while {@code PlayNicely == 0}:
 * Girlfriend priority 4 {@code (Girlfriend.class, 6.0f, 5, true)} and priority 5
 * {@code (Girlfriend.class, 3.0f, 15, true)} (Girlfriend.java:148-153); Boyfriend the same with
 * {@code Boyfriend.class} (Boyfriend.java:123-126). Chance 5 is a 6 % success per evaluation,
 * 15 is 16 % (verhalten/core-02.md).
 *
 * <p>The tamed-victim checks in step 2 are redundant with
 * {@link MyEntityAITarget#isSuitableTarget} (MyEntityAITarget.java:81-84); kept as written.
 */
public class MyEntityAIJealousy extends MyEntityAINearestAttackableTarget {

    /** Set in the constructor and never read, like the original (MyEntityAIJealousy.java:8). */
    private TamableAnimal theTameable;

    public MyEntityAIJealousy(final TamableAnimal par1EntityTameable, final Class<? extends LivingEntity> par2Class,
            final float par3, final int par4, final boolean par5) {
        super(par1EntityTameable, par2Class, par3, par4, par5);
        this.theTameable = par1EntityTameable;
    }

    /** MyEntityAIJealousy.java:16-53. */
    @Override
    public boolean canUse() {
        // PORT: consume the 1.7.10 evaluation ticks before the early returns (W01 rule); the
        // superclass reads the same memoised count afterwards.
        this.dueRolls();
        final TamableAnimal te = (TamableAnimal) this.taskOwner;
        Girlfriend gf = null;
        Boyfriend bf = null;
        LivingEntity ep = null;
        if (te == null) {
            return false;
        }
        if (!te.isTame()) {
            return false;
        }
        // PORT: EntityTameable.isSitting() read the synced DataWatcher flag; that is isInSittingPose().
        if (te.isInSittingPose()) {
            return false;
        }
        if (!super.canUse()) {
            return false;
        }
        final Entity victim = this.targetEntity;
        if (victim == null) {
            return false;
        }
        if (te instanceof Girlfriend) {
            if (victim instanceof Girlfriend) {
                gf = (Girlfriend) victim;
                if (gf.isTame()) {
                    return false;
                }
            }
        } else if (victim instanceof Boyfriend) {
            bf = (Boyfriend) victim;
            if (bf.isTame()) {
                return false;
            }
        }
        ep = te.getOwner();
        return ep != null;
    }
}
