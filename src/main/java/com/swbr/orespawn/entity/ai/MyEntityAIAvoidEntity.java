package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.entity.cannonfodder.EntityCannonFodder;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.MyEntityAIAvoidEntity} (MyEntityAIAvoidEntity.java:12-91): a copy of the
 * 1.7.10 {@code EntityAIAvoidEntity} with two rules of its own (verhalten/core-02.md).
 *
 * <ul>
 *   <li>An {@link EntityCannonFodder} with {@code get_is_activated() != 0} never flees (:34-39). This is the
 *       interaction catalogue 6.5 asked about, and it is real: all three users are battle mobs, so a chipmunk,
 *       ostrich or velocity raptor that has been fed a hat stands its ground; a wild one runs.</li>
 *   <li>The class branch selects with {@code IMob.mobSelector} ({@code instanceof IMob}, 1.21.1
 *       {@link Enemy}) instead of vanilla's "alive and visible" (:50).</li>
 * </ul>
 *
 * <p>Users (class, distance, far speed, near speed): Chipmunk (EntityMob, 8.0, 1.0, 1.6), Ostrich (EntityMob,
 * 8.0, 1.0, 1.9), VelocityRaptor (EntityMob, 8.0, 1.0, 1.4). {@code EntityMob} is {@code Monster.class} (as in
 * {@code MyUtils}). The tamed lock only guards the player branch, so tamed animals still flee monsters.
 *
 * <p>1.21.1's {@code AvoidEntityGoal} has the same constants (16/7 away, 49.0 near) but picks the nearest
 * entity through {@code TargetingConditions.forCombat}; the original rules are kept here. Mutex 1 =
 * {@link Goal.Flag#MOVE}.
 */
public class MyEntityAIAvoidEntity extends Goal {

    /** {@code IMob.mobSelector}. */
    private static final Predicate<Entity> MOB_SELECTOR = e -> e instanceof Enemy;

    private final PathfinderMob theEntity;
    private final double farSpeed;
    private final double nearSpeed;
    @Nullable
    private Entity closestLivingEntity;
    private final float distanceFromEntity;
    @Nullable
    private Path entityPathEntity;
    private final PathNavigation entityPathNavigate;
    private final Class<? extends Entity> targetEntityClass;

    /** MyEntityAIAvoidEntity.java:23-31: mutex 1. */
    public MyEntityAIAvoidEntity(final PathfinderMob par1EntityCreature, final Class<? extends Entity> par2Class, final float par3,
                                 final double par4, final double par6) {
        this.theEntity = par1EntityCreature;
        this.targetEntityClass = par2Class;
        this.distanceFromEntity = par3;
        this.farSpeed = par4;
        this.nearSpeed = par6;
        this.entityPathNavigate = par1EntityCreature.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * {@code shouldExecute} (:33-65).
     *
     * <p>PORT: 1.7.10 asked this every 3 ticks, 1.21.1 every 2; there is no chance roll, so the cadence is not
     * compensated (W01 precedent for goals without a per-evaluation chance).
     */
    @Override
    public boolean canUse() {
        // :34-39 - reads the server field of the battle mob, which is what the 1.7.10 server read.
        if (this.theEntity != null && this.theEntity instanceof EntityCannonFodder) {
            final EntityCannonFodder cf = (EntityCannonFodder) this.theEntity;
            if (cf.get_is_activated() != 0) {
                return false;
            }
        }
        if (this.targetEntityClass == Player.class) {
            // :41-43
            if (this.theEntity instanceof TamableAnimal && ((TamableAnimal) this.theEntity).isTame()) {
                return false;
            }
            // :44 getClosestPlayerToEntity(entity, dist): the nearest player, creative included; 1.21.1 skips
            // spectators, which did not exist.
            this.closestLivingEntity = this.theEntity.level().getNearestPlayer(this.theEntity, (double) this.distanceFromEntity);
            if (this.closestLivingEntity == null) {
                return false;
            }
        } else {
            // :50 selectEntitiesWithinAABB(class, boundingBox.expand(d, 3.0, d), IMob.mobSelector)
            final List<? extends Entity> list = this.theEntity.level().getEntitiesOfClass(this.targetEntityClass,
                    this.theEntity.getBoundingBox().inflate((double) this.distanceFromEntity, 3.0, (double) this.distanceFromEntity),
                    MOB_SELECTOR);
            if (list.isEmpty()) {
                return false;
            }
            // :54 PORT: list.get(0), the first entity, not the nearest (the catalogue suggested "nearest"; the
            // rule stays). 1.21.1's entity sections iterate in another order than the 1.7.10 chunk lists, so
            // "the first" can name another monster than it did.
            this.closestLivingEntity = list.get(0);
        }
        // :56 PORT: RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 16, 7, pos) is
        // DefaultRandomPos.getPosAway with the same radius and height; 1.21.1 weighs candidates differently.
        final Vec3 vec3 = DefaultRandomPos.getPosAway(this.theEntity, 16, 7, this.closestLivingEntity.position());
        if (vec3 == null) {
            return false;
        }
        // :60-62
        if (this.closestLivingEntity.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.closestLivingEntity.distanceToSqr(this.theEntity)) {
            return false;
        }
        // :63-64 getPathToXYZ -> createPath with accuracy 0.
        this.entityPathEntity = this.entityPathNavigate.createPath(vec3.x, vec3.y, vec3.z, 0);
        return this.entityPathEntity != null && isDestinationSame(this.entityPathEntity, vec3);
    }

    /**
     * 1.7.10 {@code PathEntity.isDestinationSame(vec)}: the final path point's x and z equal the truncated
     * target. PORT: {@code (int)} becomes {@code Mth.floor} (DECISIONS R20) - the 1.21.1 position is the block
     * centre (x + 0.5), which truncation maps to the neighbouring block at negative x/z.
     */
    private static boolean isDestinationSame(final Path path, final Vec3 vec3) {
        final Node pathpoint = path.getEndNode();
        return pathpoint != null && pathpoint.x == Mth.floor(vec3.x) && pathpoint.z == Mth.floor(vec3.z);
    }

    /** {@code continueExecuting} (:67-69). */
    @Override
    public boolean canContinueToUse() {
        return !this.entityPathNavigate.isDone();
    }

    /** {@code startExecuting} (:71-73). */
    @Override
    public void start() {
        this.entityPathNavigate.moveTo(this.entityPathEntity, this.farSpeed);
    }

    /** {@code resetTask} (:75-77). */
    @Override
    public void stop() {
        this.closestLivingEntity = null;
    }

    /** 1.7.10 ran {@code updateTask} of a running task every tick. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** {@code updateTask} (:79-86). */
    @Override
    public void tick() {
        if (this.closestLivingEntity == null) {
            return;
        }
        if (this.theEntity.distanceToSqr(this.closestLivingEntity) < 49.0) {
            this.theEntity.getNavigation().setSpeedModifier(this.nearSpeed);
        } else {
            this.theEntity.getNavigation().setSpeedModifier(this.farSpeed);
        }
    }

    /** {@code func_98217_a} (:88-90): unnamed static accessor of {@code theEntity}; no caller in the source. */
    static PathfinderMob func_98217_a(final MyEntityAIAvoidEntity par0EntityAIAvoidEntity) {
        return par0EntityAIAvoidEntity.theEntity;
    }
}
