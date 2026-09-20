package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the vanilla 1.7.10 goal {@code net.minecraft.entity.ai.EntityAIAvoidEntity} ({@code tw} in
 * client-1.7.10.jar, disassembled; selector {@code tx}, {@code PathEntity.isDestinationSame} {@code ayf.b}).
 * Not OreSpawn's own {@link MyEntityAIAvoidEntity}. No original OreSpawn class. W06 users: Chipmunk, Gazelle,
 * Baryonyx, Beaver, Camarasaurus, Cassowary, Hydrolisc, Peacock, StinkBug (the W06 integrator merged the two
 * identical porter copies from {@code entity.cannonfodder} and {@code entity.herbivore} into this one, R21).
 *
 * <p>The original, line by line:
 * <ul>
 *   <li>{@code shouldExecute} ({@code tw.a}): for {@code EntityPlayer.class} a tamed {@code EntityTameable}
 *       never flees, otherwise the closest player within {@code distance} ({@code World.getClosestPlayerToEntity},
 *       which takes every player, creative ones included, without a sight test). For any other class the
 *       <em>first</em> living, visible entity in {@code boundingBox.expand(distance, 3, distance)} - not the
 *       nearest. Then a spot away from it ({@code findRandomTargetBlockAwayFrom(entity, 16, 7, pos)}), which must
 *       not be closer to the threat than the entity is, and a path whose final point lies on the spot's x/z
 *       column.</li>
 *   <li>{@code continueExecuting} ({@code tw.b}): while the navigator has a path.</li>
 *   <li>{@code startExecuting} ({@code tw.c}): follow that path at the far speed.</li>
 *   <li>{@code resetTask} ({@code tw.d}): forget the threat.</li>
 *   <li>{@code updateTask} ({@code tw.e}): near speed below 7 blocks (squared 49), far speed otherwise.</li>
 * </ul>
 * Mutex 1 = {@link Goal.Flag#MOVE}.
 *
 * <p>PORT: 1.21.1's {@link AvoidEntityGoal} picks the nearest candidate, skips creative players, tests sight
 * for players too and has no tamed rule; all four are undone here. Spectators did not exist in 1.7.10 and are
 * skipped ({@link EntitySelector#NO_SPECTATORS}). The random spot comes from {@link DefaultRandomPos#getPosAway},
 * which returns block centres ({@code x + 0.5}), so the column test floors the spot (R20) where 1.7.10 cast
 * whole-block doubles.
 */
public class EntityAIAvoidEntity extends Goal {

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

    /** {@code tw.<init>(creature, class, distance, farSpeed, nearSpeed)}: mutex 1. */
    public EntityAIAvoidEntity(final PathfinderMob par1EntityCreature, final Class<? extends Entity> par2Class,
                               final float par3, final double par4, final double par6) {
        this.theEntity = par1EntityCreature;
        this.targetEntityClass = par2Class;
        this.distanceFromEntity = par3;
        this.farSpeed = par4;
        this.nearSpeed = par6;
        this.entityPathNavigate = par1EntityCreature.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /** {@code shouldExecute} ({@code tw.a}). */
    @Override
    public boolean canUse() {
        if (this.targetEntityClass == Player.class) {
            if (this.theEntity instanceof TamableAnimal && ((TamableAnimal) this.theEntity).isTame()) {
                return false;
            }
            this.closestLivingEntity = this.theEntity.level().getNearestPlayer(this.theEntity.getX(), this.theEntity.getY(),
                    this.theEntity.getZ(), (double) this.distanceFromEntity, EntitySelector.NO_SPECTATORS);
            if (this.closestLivingEntity == null) {
                return false;
            }
        } else {
            final List<? extends Entity> var1 = this.theEntity.level().getEntitiesOfClass(this.targetEntityClass,
                    this.theEntity.getBoundingBox().inflate((double) this.distanceFromEntity, 3.0, (double) this.distanceFromEntity),
                    candidate -> candidate.isAlive() && this.theEntity.getSensing().hasLineOfSight(candidate));
            if (var1.isEmpty()) {
                return false;
            }
            this.closestLivingEntity = var1.get(0);
        }
        final Vec3 var2 = DefaultRandomPos.getPosAway(this.theEntity, 16, 7, this.closestLivingEntity.position());
        if (var2 == null) {
            return false;
        }
        if (this.closestLivingEntity.distanceToSqr(var2.x, var2.y, var2.z) < this.closestLivingEntity.distanceToSqr(this.theEntity)) {
            return false;
        }
        this.entityPathEntity = this.entityPathNavigate.createPath(var2.x, var2.y, var2.z, 0);
        if (this.entityPathEntity == null) {
            return false;
        }
        // PathEntity.isDestinationSame (ayf.b): final point on the spot's x/z column, y ignored.
        final Node end = this.entityPathEntity.getEndNode();
        return end != null && end.x == Mth.floor(var2.x) && end.z == Mth.floor(var2.z);
    }

    /** {@code continueExecuting} ({@code tw.b}). PORT: 1.21.1 asks every second tick; not compensated. */
    @Override
    public boolean canContinueToUse() {
        return !this.entityPathNavigate.isDone();
    }

    /** {@code startExecuting} ({@code tw.c}). */
    @Override
    public void start() {
        this.entityPathNavigate.moveTo(this.entityPathEntity, this.farSpeed);
    }

    /** {@code resetTask} ({@code tw.d}). */
    @Override
    public void stop() {
        this.closestLivingEntity = null;
    }

    /** 1.7.10 ran {@code updateTask} every tick. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** {@code updateTask} ({@code tw.e}). */
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
}
