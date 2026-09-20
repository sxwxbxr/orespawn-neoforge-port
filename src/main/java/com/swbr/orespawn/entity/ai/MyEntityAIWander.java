package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.MyEntityAIWander}: random strolling at a fixed rate, with two rules
 * the vanilla goal lacks - sitting tameables never wander, and a tameable standing on its owner's
 * block column stops walking.
 *
 * <p>24 call sites; speed 0.65f x1, 0.75f x10, 0.9f x1, 1.0f x12 (verhalten/core-02.md).
 * 1.7.10 mutex bit 1 is {@link Goal.Flag#MOVE}.
 */
public class MyEntityAIWander extends Goal {

    private final PathfinderMob entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private final float speed;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    public MyEntityAIWander(final PathfinderMob par1EntityCreature, final float par2) {
        this.entity = par1EntityCreature;
        this.speed = par2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * MyEntityAIWander.java:22-37: one-in-ninety chance, sitting tameables never, then a random
     * target within 10 blocks horizontally and 7 vertically.
     */
    @Override
    public boolean canUse() {
        // PORT: 1.7.10 evaluated shouldExecute() every 3 ticks, 1.21.1 asks canUse() every 2. The
        // helper rolls the original 1/90 chance once per 3 game ticks so the average interval stays
        // 270 ticks (see LegacyAiTick). Every roll consumes one entity RNG draw, like the original.
        // Ticks in which this goal was blocked (follow owner, panic, tempt, attack holding MOVE) are
        // dropped by the helper, as 1.7.10 never asked a task behind a conflicting running one.
        boolean hit = false;
        for (int rolls = this.legacyTick.dueRolls(this.entity); rolls > 0; --rolls) {
            if (this.entity.getRandom().nextInt(90) == 0) {
                hit = true;
            }
        }
        if (!hit) {
            return false;
        }
        // PORT: EntityTameable.isSitting() read the synced DataWatcher flag; that is isInSittingPose(),
        // not the server-side isOrderedToSit().
        if (this.entity instanceof TamableAnimal && ((TamableAnimal) this.entity).isInSittingPose()) {
            return false;
        }
        // PORT: RandomPositionGenerator.findRandomTarget(entity, 10, 7) is DefaultRandomPos.getPos.
        // The 1.21.1 search weighs candidates differently, but the radius and vertical range are the
        // original numbers.
        final Vec3 var1 = DefaultRandomPos.getPos(this.entity, 10, 7);
        if (var1 == null) {
            return false;
        }
        this.xPosition = var1.x;
        this.yPosition = var1.y;
        this.zPosition = var1.z;
        return true;
    }

    /**
     * MyEntityAIWander.java:39-48: a tameable that shares the owner's integer x/z column and stands
     * within the open range of +-2 in y stops; otherwise walk until the path ends.
     */
    @Override
    public boolean canContinueToUse() {
        if (this.entity != null && this.entity instanceof TamableAnimal) {
            final TamableAnimal gf = (TamableAnimal) this.entity;
            final LivingEntity var1 = gf.getOwner();
            // :43 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
            if (var1 != null
                    && Mth.floor(gf.getZ()) == Mth.floor(var1.getZ())
                    && Mth.floor(gf.getX()) == Mth.floor(var1.getX())
                    && Mth.floor(gf.getY()) < Mth.floor(var1.getY()) + 2
                    && Mth.floor(gf.getY()) > Mth.floor(var1.getY()) - 2) {
                return false;
            }
        }
        // PORT: 1.7.10 asked continueExecuting() every tick, 1.21.1 asks canContinueToUse() every
        // second tick. At most one tick of extra walking; not compensated.
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, (double) this.speed);
    }

    @Override
    public void stop() {
        // PORT: keeps the 3-tick cadence aligned after a walk; the original never asked a running
        // task shouldExecute(). Not part of the original class (it had no resetTask). Ticks spent
        // blocked by a higher-priority goal are dropped inside LegacyAiTick.dueRolls.
        this.legacyTick.skipWhileRunning(this.entity);
    }
}
