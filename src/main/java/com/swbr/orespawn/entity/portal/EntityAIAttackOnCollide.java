package com.swbr.orespawn.entity.portal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

/**
 * Port of the vanilla 1.7.10 goal {@code net.minecraft.entity.ai.EntityAIAttackOnCollide} ({@code uq} in
 * client-1.7.10.jar, disassembled), used by {@link EntityRedAnt} and {@link Termite}. No original
 * OreSpawn class; it lives here because this wave is the first user.
 *
 * <p>Not 1.21.1's {@code MeleeAttackGoal}, because two numbers differ where the ants feel it:
 * <ul>
 *   <li>Reach: 1.7.10 hit when the squared distance to the target's feet was at most
 *       {@code (width * 2)^2 + target width}; for a 0.2 ant against a player 0.76, about 0.87 blocks.
 *       {@code MeleeAttackGoal} reaches about 1.2 blocks with an inflated box.</li>
 *   <li>Cadence: {@code attackTick} is decremented to 0 but the test is {@code attackTick <= 20}, so an
 *       in-range attacker attacks every tick. {@code MeleeAttackGoal} waits 20 ticks. With the ants'
 *       one-in-fifteen {@code attackEntityAsMob}, that is a bite chance per tick, not per second.</li>
 * </ul>
 * Mutex 3 is {@code MOVE} and {@code LOOK}. {@code updateTask} ran every tick, hence
 * {@link #requiresUpdateEveryTick()}.
 */
public class EntityAIAttackOnCollide extends Goal {

    PathfinderMob attacker;
    int attackTick;
    double speedTowardsTarget;
    boolean longMemory;
    Path entityPathEntity;
    Class<?> classTarget;
    /** {@code field_75445_i}: ticks until the path may be recomputed. */
    private int delayCounter;
    /** {@code field_151497_i/j/k}: where the target stood at the last path update. */
    private double targetX;
    private double targetY;
    private double targetZ;

    /** {@code (EntityCreature, Class, double, boolean)}: only targets of that class. */
    public EntityAIAttackOnCollide(final PathfinderMob par1EntityCreature, final Class<?> par2Class, final double par3,
                                   final boolean par5) {
        this(par1EntityCreature, par3, par5);
        this.classTarget = par2Class;
    }

    /** {@code (EntityCreature, double, boolean)}: speed and "long memory" (keep chasing without sight). */
    public EntityAIAttackOnCollide(final PathfinderMob par1EntityCreature, final double par2, final boolean par4) {
        this.attacker = par1EntityCreature;
        this.speedTowardsTarget = par2;
        this.longMemory = par4;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** {@code shouldExecute}: a living target of the class and a path to it. */
    @Override
    public boolean canUse() {
        final LivingEntity entitylivingbase = this.attacker.getTarget();
        if (entitylivingbase == null) {
            return false;
        }
        if (!entitylivingbase.isAlive()) {
            return false;
        }
        if (this.classTarget != null && !this.classTarget.isAssignableFrom(entitylivingbase.getClass())) {
            return false;
        }
        this.entityPathEntity = this.attacker.getNavigation().createPath(entitylivingbase, 0);
        return this.entityPathEntity != null;
    }

    /** {@code continueExecuting}: without long memory as long as there is a path, with it inside the home range. */
    @Override
    public boolean canContinueToUse() {
        final LivingEntity entitylivingbase = this.attacker.getTarget();
        if (entitylivingbase == null) {
            return false;
        }
        if (!entitylivingbase.isAlive()) {
            return false;
        }
        if (!this.longMemory) {
            return !this.attacker.getNavigation().isDone();
        }
        return this.attacker.isWithinRestriction(new BlockPos(Mth.floor(entitylivingbase.getX()),
                Mth.floor(entitylivingbase.getY()), Mth.floor(entitylivingbase.getZ())));
    }

    /** {@code startExecuting}. */
    @Override
    public void start() {
        this.attacker.getNavigation().moveTo(this.entityPathEntity, this.speedTowardsTarget);
        this.delayCounter = 0;
    }

    /** {@code resetTask}: {@code clearPathEntity}. */
    @Override
    public void stop() {
        this.attacker.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** {@code updateTask} ({@code uq.e}, offsets 0-351). */
    @Override
    public void tick() {
        final LivingEntity entitylivingbase = this.attacker.getTarget();
        if (entitylivingbase == null) {
            // PORT (R18 case 1): 1.21.1 ticks every-tick goals on odd ticks without re-asking
            // canContinueToUse, so the target can already be gone; 1.7.10 never got here without one.
            return;
        }
        this.attacker.getLookControl().setLookAt(entitylivingbase, 30.0f, 30.0f);
        final double d0 = this.attacker.distanceToSqr(entitylivingbase.getX(), entitylivingbase.getBoundingBox().minY,
                entitylivingbase.getZ());
        final double d1 = (double) (this.attacker.getBbWidth() * 2.0f * this.attacker.getBbWidth() * 2.0f
                + entitylivingbase.getBbWidth());
        --this.delayCounter;
        if ((this.longMemory || this.attacker.getSensing().hasLineOfSight(entitylivingbase)) && this.delayCounter <= 0
                && ((this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0)
                    || entitylivingbase.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0
                    || this.attacker.getRandom().nextFloat() < 0.05f)) {
            this.targetX = entitylivingbase.getX();
            this.targetY = entitylivingbase.getBoundingBox().minY;
            this.targetZ = entitylivingbase.getZ();
            this.delayCounter = 4 + this.attacker.getRandom().nextInt(7);
            if (d0 > 1024.0) {
                this.delayCounter += 10;
            } else if (d0 > 256.0) {
                this.delayCounter += 5;
            }
            if (!this.attacker.getNavigation().moveTo(entitylivingbase, this.speedTowardsTarget)) {
                this.delayCounter += 15;
            }
        }
        this.attackTick = Math.max(this.attackTick - 1, 0);
        if (d0 <= d1 && this.attackTick <= 20) {
            this.attackTick = 20;
            if (!this.attacker.getMainHandItem().isEmpty()) {
                this.attacker.swing(InteractionHand.MAIN_HAND);
            }
            this.attacker.doHurtTarget(entitylivingbase);
        }
    }
}
