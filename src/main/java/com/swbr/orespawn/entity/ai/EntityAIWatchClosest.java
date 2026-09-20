package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/**
 * Port of the 1.7.10 vanilla {@code EntityAIWatchClosest} ({@code un} in {@code reference/jar/mcp/client-1.7.10.jar},
 * disassembled): with a chance per evaluation (default 0.02), look at the nearest player - or the nearest entity of
 * another class - for 40..79 ticks. Mutex 2 ({@link Goal.Flag#LOOK}).
 *
 * <p>PORT: 1.21.1's {@code LookAtPlayerGoal} rolls the chance on every second tick, 1.5 times as often as 1.7.10, which
 * asked {@code shouldExecute} every third tick. The roll runs once per 1.7.10 evaluation through {@link LegacyAiTick}
 * (W01 rule). The target search is the 1.7.10 one as well: {@code World.getClosestPlayerToEntity} measured from the
 * feet with no further filter ({@code ahb.a(sa, D)}); {@code LookAtPlayerGoal} measures from the eyes through
 * {@code TargetingConditions} and skips a player riding with the mob.
 */
public class EntityAIWatchClosest extends Goal {

    private final Mob theWatcher;
    @Nullable
    protected Entity closestEntity;
    private final float maxDistanceForPlayer;
    private int lookTime;
    private final float chance;
    private final Class<? extends LivingEntity> watchedClass;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    /** {@code un(EntityLiving, Class, float)}: chance 0.02. {@code EntityLiving.class} is {@code Mob.class}. */
    public EntityAIWatchClosest(final Mob par1EntityLiving, final Class<? extends LivingEntity> par2Class, final float par3) {
        this(par1EntityLiving, par2Class, par3, 0.02f);
    }

    /** {@code un(EntityLiving, Class, float, float)}. */
    public EntityAIWatchClosest(final Mob par1EntityLiving, final Class<? extends LivingEntity> par2Class, final float par3,
                                final float par4) {
        this.theWatcher = par1EntityLiving;
        this.watchedClass = par2Class;
        this.maxDistanceForPlayer = par3;
        this.chance = par4;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // LegacyAiTick contract: take the due evaluations before anything can return.
        for (int rolls = this.legacyTick.dueRolls(this.theWatcher); rolls > 0; --rolls) {
            if (this.shouldExecute()) {
                return true;
            }
        }
        return false;
    }

    /** {@code un.a()}, one 1.7.10 evaluation. The attack target is taken and then overwritten, as in the original. */
    private boolean shouldExecute() {
        if (this.theWatcher.getRandom().nextFloat() >= this.chance) {
            return false;
        }
        if (this.theWatcher.getTarget() != null) {
            this.closestEntity = this.theWatcher.getTarget();
        }
        if (this.watchedClass == Player.class) {
            // PORT: EntityGetter.getNearestPlayer(Entity, double) is getClosestPlayerToEntity with spectators left out;
            // 1.7.10 had none.
            this.closestEntity = this.theWatcher.level().getNearestPlayer(this.theWatcher, (double) this.maxDistanceForPlayer);
        } else {
            this.closestEntity = this.findNearestEntityWithinAABB();
        }
        return this.closestEntity != null;
    }

    /**
     * {@code World.findNearestEntityWithinAABB(watchedClass, boundingBox.expand(d, 3, d), watcher)} ({@code ahb.a(Class,
     * azt, sa)}, disassembled): skip the watcher, keep every entity not farther than the best so far ({@code <=}, so
     * the last of equally near ones wins).
     */
    @Nullable
    private Entity findNearestEntityWithinAABB() {
        final List<? extends LivingEntity> var4 = this.theWatcher.level().getEntitiesOfClass(this.watchedClass,
                this.theWatcher.getBoundingBox().inflate((double) this.maxDistanceForPlayer, 3.0, (double) this.maxDistanceForPlayer));
        Entity var5 = null;
        double var6 = Double.MAX_VALUE;
        for (final LivingEntity var9 : var4) {
            if (var9 == this.theWatcher) {
                continue;
            }
            final double var10 = this.theWatcher.distanceToSqr(var9);
            if (var10 > var6) {
                continue;
            }
            var5 = var9;
            var6 = var10;
        }
        return var5;
    }

    /**
     * {@code un.b()}. PORT: 1.21.1 asks every second tick, 1.7.10 every tick; the look can last one tick longer.
     */
    @Override
    public boolean canContinueToUse() {
        if (!this.closestEntity.isAlive()) {
            return false;
        }
        if (this.theWatcher.distanceToSqr(this.closestEntity) > (double) (this.maxDistanceForPlayer * this.maxDistanceForPlayer)) {
            return false;
        }
        return this.lookTime > 0;
    }

    /** {@code un.c()}. */
    @Override
    public void start() {
        this.lookTime = 40 + this.theWatcher.getRandom().nextInt(40);
    }

    /** {@code un.d()}, then the 1.7.10 cadence is re-armed. */
    @Override
    public void stop() {
        this.closestEntity = null;
        this.legacyTick.skipWhileRunning(this.theWatcher);
    }

    /** {@code updateTask} ran every tick in 1.7.10, and {@link #lookTime} counts those ticks. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** {@code un.e()}: look at the target's eyes with yaw speed 10 and the vertical face speed, then count down. */
    @Override
    public void tick() {
        this.theWatcher.getLookControl().setLookAt(this.closestEntity.getX(), this.closestEntity.getEyeY(),
                this.closestEntity.getZ(), 10.0f, (float) this.theWatcher.getMaxHeadXRot());
        --this.lookTime;
    }
}
