package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.moth.Mothra;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

/**
 * Port of {@code danger.orespawn.MyEntityAITarget}: the abstract base of the mod's own target goals,
 * a copy of 1.7.10 {@code EntityAITarget} with the tamed/owner rules and the holiday rule of the
 * companions (verhalten/core-02.md).
 *
 * <p>Vanilla {@link net.minecraft.world.entity.ai.goal.target.TargetGoal} has the same shape
 * ({@code unseenMemoryTicks = 60}, {@code reachCache}) but tests through {@code TargetingConditions}
 * (creative/spectator, teams, {@code canAttack}). The original tested none of that, so the checks
 * are written out here in the original order.
 *
 * <p>Field names: {@code field_75298_g} is {@code targetUnseenTicks} (fields.csv).
 */
public abstract class MyEntityAITarget extends Goal {

    protected Mob taskOwner;
    protected float targetDistance;
    protected boolean shouldCheckSight;
    private boolean nearbyOnly;
    /** 0 = unknown, 1 = reachable, 2 = unreachable (MyEntityAITarget.java:114-118). */
    private int targetSearchStatus;
    private int targetSearchDelay;
    private int targetUnseenTicks;

    /**
     * The 1.7.10 cadence of {@code shouldExecute()} for the subclasses (see {@link LegacyAiTick}).
     * The subclasses read it through {@link #dueRolls()}, which is idempotent within one tick so a
     * subclass may consume the ticks before its own early returns and the superclass may read the
     * same value afterwards.
     */
    private final LegacyAiTick legacyTick = new LegacyAiTick();
    private int dueRollsTick = -1;
    private int dueRollsCached;
    /** Tick of the last {@link #canContinueToUse()} call, to count unseen ticks 1:1 (see there). */
    private int lastContinueTick = -1;

    public MyEntityAITarget(final Mob par1EntityLiving, final float par2, final boolean par3) {
        this(par1EntityLiving, par2, par3, false);
    }

    public MyEntityAITarget(final Mob par1EntityLiving, final float par2, final boolean par3, final boolean par4) {
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.targetUnseenTicks = 0;
        this.taskOwner = par1EntityLiving;
        this.targetDistance = par2;
        this.shouldCheckSight = par3;
        this.nearbyOnly = par4;
    }

    /**
     * Number of 1.7.10 {@code shouldExecute()} evaluations due in this tick, memoised per tick.
     * Subclasses call it before every early return of {@code canUse()} (W01 rule) and again where the
     * original rolled its chance; both calls in the same tick see the same number.
     */
    protected int dueRolls() {
        final int now = this.taskOwner.tickCount;
        if (now != this.dueRollsTick) {
            this.dueRollsTick = now;
            this.dueRollsCached = this.legacyTick.dueRolls(this.taskOwner);
        }
        return this.dueRollsCached;
    }

    /**
     * MyEntityAITarget.java:35-59: target present and alive, within {@code targetDistance}, not tamed
     * against tamed, and with sight checking at most 60 ticks out of sight.
     */
    @Override
    public boolean canContinueToUse() {
        final LivingEntity var1 = this.taskOwner.getTarget();
        if (var1 == null) {
            return false;
        }
        if (!var1.isAlive()) {
            this.taskOwner.setTarget((LivingEntity) null);
            return false;
        }
        if (this.taskOwner.distanceToSqr(var1) > this.targetDistance * this.targetDistance) {
            return false;
        }
        if (this.taskOwner instanceof TamableAnimal && ((TamableAnimal) this.taskOwner).isTame()
                && var1 instanceof TamableAnimal && ((TamableAnimal) var1).isTame()) {
            return false;
        }
        if (this.shouldCheckSight) {
            // PORT: 1.7.10 asked continueExecuting() every tick and counted one unseen tick per
            // call (:54). 1.21.1 asks canContinueToUse() every second tick (GoalSelector.tick from
            // Mob.serverAiStep, Mob.java:779-792), so the counter advances by the ticks elapsed since
            // the last call instead of by one; the 60-tick memory stays the original number (vanilla
            // TargetGoal halves it with reducedTickDelay instead).
            final int now = this.taskOwner.tickCount;
            final int elapsed = this.lastContinueTick < 0 ? 1 : Math.max(1, now - this.lastContinueTick);
            this.lastContinueTick = now;
            if (this.taskOwner.getSensing().hasLineOfSight(var1)) {
                this.targetUnseenTicks = 0;
            } else if ((this.targetUnseenTicks += elapsed) > 60) {
                return false;
            }
        }
        return true;
    }

    /** MyEntityAITarget.java:61-65. */
    @Override
    public void start() {
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.targetUnseenTicks = 0;
        this.lastContinueTick = this.taskOwner.tickCount;
    }

    /** MyEntityAITarget.java:67-69. */
    @Override
    public void stop() {
        this.taskOwner.setTarget((LivingEntity) null);
        // PORT: keeps the 3-tick cadence aligned after a run; not part of the original class.
        this.legacyTick.skipWhileRunning(this.taskOwner);
        this.dueRollsTick = -1;
    }

    /**
     * MyEntityAITarget.java:71-122. {@code par2} is unused, as in the original; the order of the
     * checks is the behaviour:
     * <ol>
     * <li>null, self, dead;
     * <li>tamed owner: tamed target or own owner;
     * <li>a player only on Valentine's Day, without sight or reach check;
     * <li>zombified piglin, enderman never;
     * <li>Mothra always, before the sight check (W08);
     * <li>sight;
     * <li>creeper and ghast without reach check;
     * <li>{@code nearbyOnly}: cached reachability.
     * </ol>
     */
    protected boolean isSuitableTarget(final LivingEntity par1EntityLiving, final boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this.taskOwner) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (this.taskOwner instanceof TamableAnimal && ((TamableAnimal) this.taskOwner).isTame()) {
            if (par1EntityLiving instanceof TamableAnimal && ((TamableAnimal) par1EntityLiving).isTame()) {
                return false;
            }
            if (par1EntityLiving == ((TamableAnimal) this.taskOwner).getOwner()) {
                return false;
            }
        }
        if (par1EntityLiving instanceof Player) {
            return OreSpawn.valentines_day != 0;
        }
        if (par1EntityLiving instanceof ZombifiedPiglin) {
            return false;
        }
        if (par1EntityLiving instanceof EnderMan) {
            return false;
        }
        // MyEntityAITarget.java:98-100, before the sight check. Mothra cannot be told apart by the W01 marker
        // interfaces: it is both AttackableNonMob and Ignoreable (README 4.14).
        if (par1EntityLiving instanceof Mothra) {
            return true;
        }
        if (this.shouldCheckSight && !this.taskOwner.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return true;
        }
        if (par1EntityLiving instanceof Ghast) {
            return true;
        }
        if (this.nearbyOnly) {
            if (--this.targetSearchDelay <= 0) {
                this.targetSearchStatus = 0;
            }
            if (this.targetSearchStatus == 0) {
                this.targetSearchStatus = (this.canEasilyReach(par1EntityLiving) ? 1 : 2);
            }
            if (this.targetSearchStatus == 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * MyEntityAITarget.java:124-137: a path whose last node lies within a horizontal distance
     * squared of 2.25 of the target's block. {@code getPathToEntityLiving} is
     * {@code createPath(entity, 0)}, {@code getFinalPathPoint} is {@code getEndNode}.
     */
    private boolean canEasilyReach(final LivingEntity par1EntityLiving) {
        // PORT: the delay is counted per isSuitableTarget call, as in the original (:111); vanilla
        // TargetGoal would halve it with reducedTickDelay.
        this.targetSearchDelay = 10 + this.taskOwner.getRandom().nextInt(5);
        final Path var2 = this.taskOwner.getNavigation().createPath(par1EntityLiving, 0);
        if (var2 == null) {
            return false;
        }
        final Node var3 = var2.getEndNode();
        if (var3 == null) {
            return false;
        }
        final int var4 = var3.x - Mth.floor(par1EntityLiving.getX());
        final int var5 = var3.z - Mth.floor(par1EntityLiving.getZ());
        return var4 * var4 + var5 * var5 <= 2.25;
    }
}
