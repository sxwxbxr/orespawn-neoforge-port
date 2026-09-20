package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.companion.Girlfriend;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.MyValentineTarget}: on Valentine's Day the Girlfriend hunts players
 * and Boyfriends. Users: Girlfriend priority 1 {@code (Player.class, 16.0f, 0, true, true)} and
 * priority 2 {@code (Boyfriend.class, 16.0f, 0, true, true)} (Girlfriend.java:140-141), always
 * registered, independent of {@code PlayNicely} (verhalten/core-02.md).
 *
 * <p>No taming requirement; the own owner is excluded by
 * {@link MyEntityAITarget#isSuitableTarget} (MyEntityAITarget.java:85-87), and a player passes that
 * method only while {@code valentines_day != 0} (:89-91). The flag is {@link OreSpawn#valentines_day},
 * set by {@code platform.Holidays} at mod construction and at server start (R18).
 */
public class MyValentineTarget extends MyEntityAITarget {

    @Nullable
    LivingEntity targetEntity;
    /** The owner again, as {@code EntityLivingBase}; set redundantly by all three constructors. */
    LivingEntity Me;
    Class<? extends LivingEntity> targetClass;
    int targetChance;
    private final Predicate<Entity> targetEntitySelector;
    private MyValentineTargetSorter theNearestAttackableTargetSorter;

    public MyValentineTarget(final Mob par1EntityLiving, final Class<? extends LivingEntity> par2Class, final float par3,
            final int par4, final boolean par5) {
        this(par1EntityLiving, par2Class, par3, par4, par5, false);
        this.Me = par1EntityLiving;
    }

    public MyValentineTarget(final Mob par1EntityLiving, final Class<? extends LivingEntity> par2Class, final float par3,
            final int par4, final boolean par5, final boolean par6) {
        this(par1EntityLiving, par2Class, par3, par4, par5, par6, null);
        this.Me = par1EntityLiving;
    }

    /** MyValentineTarget.java:26-35. {@code par7IEntitySelector} may be null. */
    public MyValentineTarget(final Mob par1, final Class<? extends LivingEntity> par2, final float par3, final int par4,
            final boolean par5, final boolean par6, @Nullable final Predicate<? super Entity> par7IEntitySelector) {
        super(par1, par3, par5, par6);
        this.targetClass = par2;
        this.targetDistance = par3;
        this.targetChance = par4;
        this.theNearestAttackableTargetSorter = new MyValentineTargetSorter(this, par1);
        // PORT: null selector becomes "everything", see MyEntityAINearestAttackableTarget.
        this.targetEntitySelector = par7IEntitySelector == null ? e -> true : par7IEntitySelector::test;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.Me = par1;
    }

    /**
     * MyValentineTarget.java:37-64: only on Valentine's Day; a Girlfriend that is already
     * {@code feelingBetter} never; chance; nearest suitable entity of {@code targetClass} inside the
     * box inflated by {@code (targetDistance, 4.0, targetDistance)}, sorted by pure distance.
     */
    @Override
    public boolean canUse() {
        // PORT: 1.7.10 cadence, see MyEntityAINearestAttackableTarget.canUse.
        final int rolls = this.dueRolls();
        if (OreSpawn.valentines_day == 0) {
            return false;
        }
        if (this.Me != null && this.Me instanceof Girlfriend) {
            final Girlfriend gf = (Girlfriend) this.Me;
            if (gf.feelingBetter != 0) {
                return false;
            }
        }
        if (rolls == 0) {
            return false;
        }
        if (this.targetChance > 0) {
            boolean hit = false;
            for (int left = rolls; left > 0; --left) {
                if (this.taskOwner.getRandom().nextInt(100) <= this.targetChance) {
                    hit = true;
                }
            }
            if (!hit) {
                return false;
            }
        }
        final AABB box = this.taskOwner.getBoundingBox().inflate((double) this.targetDistance, 4.0, (double) this.targetDistance);
        final List<? extends LivingEntity> var5 = this.taskOwner.level().getEntitiesOfClass(this.targetClass, box, this.targetEntitySelector);
        var5.sort(this.theNearestAttackableTargetSorter);
        // The original's `instanceof EntityLivingBase` test on each element (:53) is implied by the
        // typed list.
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                this.targetEntity = var8;
                return true;
            }
        }
        this.targetEntity = null;
        return false;
    }

    /** MyValentineTarget.java:66-70. */
    @Override
    public void start() {
        this.taskOwner.setTarget(this.targetEntity);
        super.start();
    }
}
