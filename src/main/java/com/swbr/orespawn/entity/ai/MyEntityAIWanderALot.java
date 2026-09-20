package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.MyEntityAIWanderALot}: random strolling with a configurable radius,
 * a one-in-thirty chance and a {@code busy} switch that {@code Godzilla} flips while it does
 * something else (Godzilla.java:354 sets 1, :398 sets 0).
 *
 * <p>49 call sites; (xzRange, speed) distribution: (8, 1.0) x2, (9, 1.0) x3, (10, 1.0) x9, (14, 1.0)
 * x13, (15, 1.0) x1, (16, 0.5) x1, (16, 1.0) x19, (20, 1.0) x1 (verhalten/core-02.md).
 * 1.7.10 mutex bit 1 is {@link Goal.Flag#MOVE}.
 */
public class MyEntityAIWanderALot extends Goal {

    private final PathfinderMob entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private final double speed;
    private int xzRange;
    private int busy;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    public MyEntityAIWanderALot(final PathfinderMob par1EntityCreature, final int par1, final double par2) {
        this.xzRange = 10;
        this.busy = 0;
        this.entity = par1EntityCreature;
        this.xzRange = par1;
        this.speed = par2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /** MyEntityAIWanderALot.java:27-29. Non-zero pauses the goal. */
    public void setBusy(final int i) {
        this.busy = i;
    }

    /**
     * MyEntityAIWanderALot.java:31-49: busy never, one-in-thirty chance from the <em>level</em> RNG
     * (not the entity's), sitting tameables never, then a random target within {@code xzRange}
     * horizontally and 7 vertically.
     */
    @Override
    public boolean canUse() {
        // PORT: 1.7.10 evaluated shouldExecute() every 3 ticks, 1.21.1 asks canUse() every 2. The
        // helper rolls the original 1/30 chance once per 3 game ticks so the average interval stays
        // 90 ticks (see LegacyAiTick). The RNG is the level's, as in the original (worldObj.rand).
        // The ticks are consumed before the busy check: the original was asked while busy and
        // returned without rolling, so those evaluations are spent, not deferred. Otherwise a
        // 600-tick fight would roll 200 times at once when Godzilla clears busy.
        final int rolls = this.legacyTick.dueRolls(this.entity);
        if (this.busy != 0) {
            return false;
        }
        boolean hit = false;
        for (int left = rolls; left > 0; --left) {
            if (this.entity.level().random.nextInt(30) == 0) {
                hit = true;
            }
        }
        if (!hit) {
            return false;
        }
        // PORT: EntityTameable.isSitting() read the synced DataWatcher flag; that is isInSittingPose().
        if (this.entity instanceof TamableAnimal && ((TamableAnimal) this.entity).isInSittingPose()) {
            return false;
        }
        // PORT: RandomPositionGenerator.findRandomTarget(entity, xzRange, 7) is DefaultRandomPos.getPos.
        final Vec3 var1 = DefaultRandomPos.getPos(this.entity, this.xzRange, 7);
        if (var1 == null) {
            return false;
        }
        this.xPosition = var1.x;
        this.yPosition = var1.y;
        this.zPosition = var1.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // PORT: 1.21.1 asks this every second tick instead of every tick; not compensated.
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }

    @Override
    public void stop() {
        // PORT: keeps the 3-tick cadence aligned after a walk; the original had no resetTask. Ticks
        // spent blocked by a higher-priority goal are dropped inside LegacyAiTick.dueRolls.
        this.legacyTick.skipWhileRunning(this.entity);
    }
}
