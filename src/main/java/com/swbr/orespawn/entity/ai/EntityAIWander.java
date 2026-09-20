package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the 1.7.10 vanilla {@code EntityAIWander} ({@code vc} in {@code reference/jar/mcp/client-1.7.10.jar},
 * disassembled), the wander goal of {@code EntityCow} and therefore of the RedCow family. OreSpawn's own
 * {@link MyEntityAIWander} is a different class with a different chance.
 *
 * <p>{@code shouldExecute} ({@code vc.a}): no wandering once the idle time ({@code getAge}, 1.21.1
 * {@code getNoActionTime}) is 100 or more; then {@code nextInt(120) == 0}; then a random target 10 wide and 7 high.
 * {@code continueExecuting}: until the path is done. No {@code resetTask}. Mutex 1 ({@link Goal.Flag#MOVE}).
 *
 * <p>PORT: 1.21.1's {@code RandomStrollGoal} rolls {@code nextInt(reducedTickDelay(120))} = {@code nextInt(60)} on every
 * second tick, 1/120 per tick against 1/360 in 1.7.10, which asked {@code shouldExecute} every third tick. The roll
 * here runs once per 1.7.10 evaluation through {@link LegacyAiTick}. It also stops the navigation when it ends,
 * which {@code vc} never did, and refuses a mob with a controlling passenger, which {@code vc} did not ask.
 */
public class EntityAIWander extends Goal {

    private final PathfinderMob entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private final double speed;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    public EntityAIWander(final PathfinderMob par1EntityCreature, final double par2) {
        this.entity = par1EntityCreature;
        this.speed = par2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // LegacyAiTick contract: take the due evaluations before anything can return.
        for (int rolls = this.legacyTick.dueRolls(this.entity); rolls > 0; --rolls) {
            if (this.shouldExecute()) {
                return true;
            }
        }
        return false;
    }

    /** {@code vc.a()}, one 1.7.10 evaluation. */
    private boolean shouldExecute() {
        if (this.entity.getNoActionTime() >= 100) {
            return false;
        }
        if (this.entity.getRandom().nextInt(120) != 0) {
            return false;
        }
        // PORT: RandomPositionGenerator.findRandomTarget(entity, 10, 7) is DefaultRandomPos.getPos, as in
        // MyEntityAIWander: same radius and height, the 1.21.1 search weighs candidates differently.
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
     * {@code vc.b()}: {@code !getNavigator().noPath()}. PORT: 1.21.1 asks every second tick, 1.7.10 every tick; at most
     * one tick of extra walking, not compensated (same note as {@link MyEntityAIWander}).
     */
    @Override
    public boolean canContinueToUse() {
        return !this.entity.getNavigation().isDone();
    }

    /** {@code vc.c()}: {@code tryMoveToXYZ(x, y, z, speed)}. */
    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }

    /**
     * {@code vc} has no {@code resetTask}, so the path is left alone. Only the 1.7.10 cadence is re-armed
     * ({@link LegacyAiTick#skipWhileRunning}).
     */
    @Override
    public void stop() {
        this.legacyTick.skipWhileRunning(this.entity);
    }
}
