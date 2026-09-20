package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.entity.companion.Girlfriend;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.MyEntityAINearestAttackableTarget}: target search of the
 * <em>tamed</em> companions (bodyguard behaviour). Users: Girlfriend and Boyfriend, each with
 * {@code (Creeper, 20.0f, 0, true, true, IMob.mobSelector)} at priority 2 and
 * {@code (Mob, 15.0f, 0, true, true, IMob.mobSelector)} at priority 3, only while
 * {@code PlayNicely == 0} (Girlfriend.java:142-147, Boyfriend.java:117-120); {@link MyEntityAIJealousy}
 * extends it (verhalten/core-02.md).
 *
 * <p>Vanilla {@code NearestAttackableTargetGoal} rolls {@code nextInt(interval) != 0} and tests
 * through {@code TargetingConditions}; the original's chance semantics and its own sorter need this
 * class. 1.7.10 target-list mutex bit 1 is {@link Goal.Flag#TARGET}.
 */
public class MyEntityAINearestAttackableTarget extends MyEntityAITarget {

    /**
     * 1.7.10 {@code IMob.mobSelector}: {@code entity instanceof IMob} and nothing else (bytecode of
     * {@code yc.a(sa)} in {@code reference/jar/mcp/client-1.7.10.jar}: {@code instanceof yb; ireturn},
     * {@code yb} = {@code IMob} per joined.srg). {@code IMob} is {@link Enemy}. Offered here so the
     * companions pass the same predicate the original passed; liveness is tested later by
     * {@link MyEntityAITarget#isSuitableTarget}.
     */
    public static final Predicate<Entity> MOB_SELECTOR = e -> e instanceof Enemy;

    /**
     * The original typed this {@code EntityLiving} and hard-cast every list element (:50).
     * PORT: typed {@link LivingEntity} and not cast, because a target class of {@code Player} would
     * throw {@code ClassCastException} (R18 case 1); no call site passes one, so nothing changes.
     */
    @Nullable
    LivingEntity targetEntity;
    Class<? extends LivingEntity> targetClass;
    int targetChance;
    private final Predicate<Entity> targetEntitySelector;
    private MyEntityAINearestAttackableTargetSorter theNearestAttackableTargetSorter;

    public MyEntityAINearestAttackableTarget(final Mob par1EntityLiving, final Class<? extends LivingEntity> par2Class,
            final float par3, final int par4, final boolean par5) {
        this(par1EntityLiving, par2Class, par3, par4, par5, false);
    }

    public MyEntityAINearestAttackableTarget(final Mob par1EntityLiving, final Class<? extends LivingEntity> par2Class,
            final float par3, final int par4, final boolean par5, final boolean par6) {
        this(par1EntityLiving, par2Class, par3, par4, par5, par6, null);
    }

    /**
     * MyEntityAINearestAttackableTarget.java:24-32. {@code par7IEntitySelector} may be null (all
     * entities of the class), like the 1.7.10 {@code IEntitySelector}.
     */
    public MyEntityAINearestAttackableTarget(final Mob par1, final Class<? extends LivingEntity> par2, final float par3,
            final int par4, final boolean par5, final boolean par6, @Nullable final Predicate<? super Entity> par7IEntitySelector) {
        super(par1, par3, par5, par6);
        this.targetClass = par2;
        this.targetDistance = par3;
        this.targetChance = par4;
        this.theNearestAttackableTargetSorter = new MyEntityAINearestAttackableTargetSorter(this, par1);
        // PORT: Level.getEntitiesOfClass has no null-selector overload with an AABB; null becomes
        // "everything", which is what selectEntitiesWithinAABB(…, null) did.
        this.targetEntitySelector = par7IEntitySelector == null ? e -> true : par7IEntitySelector::test;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /**
     * MyEntityAINearestAttackableTarget.java:34-58: untamed tameables never; an untamed or sitting
     * Girlfriend never (a sitting Boyfriend keeps searching); chance; then the nearest suitable
     * entity of {@code targetClass} inside the box inflated by {@code (targetDistance, 4.0,
     * targetDistance)}.
     */
    @Override
    public boolean canUse() {
        // PORT: 1.7.10 evaluated shouldExecute() every 3 ticks, 1.21.1 asks canUse() every 2 (see
        // LegacyAiTick). The helper's count is taken before every early return; the checks below
        // consumed no RNG in the original, so only the chance draws are repeated per due evaluation.
        final int rolls = this.dueRolls();
        if (this.taskOwner instanceof TamableAnimal && !((TamableAnimal) this.taskOwner).isTame()) {
            return false;
        }
        if (this.taskOwner instanceof Girlfriend && !((Girlfriend) this.taskOwner).isTame()) {
            return false;
        }
        // PORT: EntityTameable.isSitting() read the synced DataWatcher flag; that is isInSittingPose().
        if (this.taskOwner instanceof Girlfriend && ((Girlfriend) this.taskOwner).isInSittingPose()) {
            return false;
        }
        if (rolls == 0) {
            // No 1.7.10 evaluation falls into this tick: neither a chance draw nor an entity scan.
            return false;
        }
        if (this.targetChance > 0) {
            // Original: `nextInt(100) > targetChance` aborts, so success is (chance + 1) / 100 per
            // evaluation; one draw per due evaluation, any success proceeds.
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
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                this.targetEntity = var8;
                return true;
            }
        }
        this.targetEntity = null;
        return false;
    }

    /** MyEntityAINearestAttackableTarget.java:60-64. */
    @Override
    public void start() {
        this.taskOwner.setTarget(this.targetEntity);
        super.start();
    }
}
