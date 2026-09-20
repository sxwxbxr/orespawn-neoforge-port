package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of the 1.7.10 vanilla {@code EntityAITempt} ({@code vk} in {@code reference/jar/mcp/client-1.7.10.jar},
 * disassembled): follow the nearest player within 10 blocks while that player holds the breeding item, then calm down
 * for 100 evaluations. Mutex 3 ({@link Goal.Flag#MOVE}, {@link Goal.Flag#LOOK}).
 *
 * <p>The item is a predicate because one 1.7.10 {@code Item} with any metadata is several 1.21.1 items
 * ({@code Items.fish}, {@code Items.dye}); the caller maps it.
 *
 * <p>PORT: 1.21.1's {@code TemptGoal} differs in three ways, all undone here. Its calm-down is
 * {@code reducedTickDelay(100)} = 50 evaluations on every second tick, 100 ticks; 1.7.10 counted
 * {@code delayTemptCounter} down once per evaluation on every third tick, 300 ticks - {@link LegacyAiTick} restores
 * that cadence. It picks the nearest player <em>holding</em> the item in either hand; 1.7.10 took the nearest player
 * and then looked at that player's held item only ({@code getCurrentEquippedItem}, the main hand). And it leaves the
 * water avoidance alone, which 1.7.10 lifted while tempted.
 */
public class EntityAITempt extends Goal {

    private final PathfinderMob temptedEntity;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double pitch;
    private double yaw;
    @Nullable
    private Player temptingPlayer;
    private int delayTemptCounter;
    private boolean isRunning;
    private final Predicate<ItemStack> breedingFood;
    private final boolean scaredByPlayerMovement;
    private float avoidWater;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    /** {@code vk(EntityCreature, double, Item, boolean)}. */
    public EntityAITempt(final PathfinderMob par1EntityCreature, final double par2, final Predicate<ItemStack> par4Item,
                         final boolean par5) {
        this.temptedEntity = par1EntityCreature;
        this.speed = par2;
        this.breedingFood = par4Item;
        this.scaredByPlayerMovement = par5;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // LegacyAiTick contract: take the due evaluations before anything can return. The calm-down counts
        // evaluations, so a pass without a due evaluation does not count it down.
        for (int rolls = this.legacyTick.dueRolls(this.temptedEntity); rolls > 0; --rolls) {
            if (this.shouldExecute()) {
                return true;
            }
        }
        return false;
    }

    /** {@code vk.a()}, one 1.7.10 evaluation. */
    private boolean shouldExecute() {
        if (this.delayTemptCounter > 0) {
            --this.delayTemptCounter;
            return false;
        }
        // PORT: EntityGetter.getNearestPlayer(Entity, double) is getClosestPlayerToEntity with spectators left out;
        // 1.7.10 had none.
        this.temptingPlayer = this.temptedEntity.level().getNearestPlayer(this.temptedEntity, 10.0);
        if (this.temptingPlayer == null) {
            return false;
        }
        final ItemStack var1 = this.temptingPlayer.getMainHandItem();
        // The empty stack is the original's null.
        if (var1.isEmpty()) {
            return false;
        }
        return this.breedingFood.test(var1);
    }

    /**
     * {@code vk.b()}: a scared mob gives up once the player moves or turns within 6 blocks; then
     * {@code shouldExecute()} again. It does not count down: the counter is 0 while the goal runs.
     */
    @Override
    public boolean canContinueToUse() {
        if (this.scaredByPlayerMovement) {
            if (this.temptedEntity.distanceToSqr(this.temptingPlayer) < 36.0) {
                if (this.temptingPlayer.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002) {
                    return false;
                }
                if (Math.abs((double) this.temptingPlayer.getXRot() - this.pitch) > 5.0
                        || Math.abs((double) this.temptingPlayer.getYRot() - this.yaw) > 5.0) {
                    return false;
                }
            } else {
                this.targetX = this.temptingPlayer.getX();
                this.targetY = this.temptingPlayer.getY();
                this.targetZ = this.temptingPlayer.getZ();
            }
            this.pitch = (double) this.temptingPlayer.getXRot();
            this.yaw = (double) this.temptingPlayer.getYRot();
        }
        return this.shouldExecute();
    }

    /**
     * {@code vk.c()}. {@code getNavigator().getAvoidsWater()} / {@code setAvoidsWater(false)} is the water path malus:
     * saved and set to 0, the translation {@link MyEntityAIFollowOwner} uses.
     */
    @Override
    public void start() {
        this.targetX = this.temptingPlayer.getX();
        this.targetY = this.temptingPlayer.getY();
        this.targetZ = this.temptingPlayer.getZ();
        this.isRunning = true;
        this.avoidWater = this.temptedEntity.getPathfindingMalus(PathType.WATER);
        this.temptedEntity.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    /** {@code vk.d()}: 100 evaluations of calm-down, water avoidance restored; then the 1.7.10 cadence is re-armed. */
    @Override
    public void stop() {
        this.temptingPlayer = null;
        this.temptedEntity.getNavigation().stop();
        this.delayTemptCounter = 100;
        this.isRunning = false;
        this.temptedEntity.setPathfindingMalus(PathType.WATER, this.avoidWater);
        this.legacyTick.skipWhileRunning(this.temptedEntity);
    }

    /** {@code updateTask} ran every tick in 1.7.10. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** {@code vk.e()}: look at the player (yaw speed 30), stop within 2.5 blocks, else walk to the player. */
    @Override
    public void tick() {
        this.temptedEntity.getLookControl().setLookAt(this.temptingPlayer, 30.0f, (float) this.temptedEntity.getMaxHeadXRot());
        if (this.temptedEntity.distanceToSqr(this.temptingPlayer) < 6.25) {
            this.temptedEntity.getNavigation().stop();
        } else {
            this.temptedEntity.getNavigation().moveTo(this.temptingPlayer, this.speed);
        }
    }

    /** {@code vk.f()}. */
    public boolean isRunning() {
        return this.isRunning;
    }
}
