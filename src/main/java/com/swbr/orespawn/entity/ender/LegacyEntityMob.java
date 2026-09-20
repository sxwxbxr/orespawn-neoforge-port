package com.swbr.orespawn.entity.ender;

import com.swbr.orespawn.entity.insect.InsectSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * The 1.7.10 "old AI" of an {@code EntityMob} whose {@code isAIEnabled()} stays {@code false}: no task lists, but
 * {@code EntityCreature.updateEntityActionState} with {@code findPlayerToAttack}, {@code attackEntity} and a path
 * followed by hand. {@code EnderKnight} and {@code EnderReaper} run on it. No OreSpawn class of its own - this is
 * vanilla 1.7.10, disassembled from {@code reference/jar/mcp/client-1.7.10.jar} with {@code javap -c} and named
 * through {@code joined.srg}/{@code methods.csv}:
 *
 * <ul>
 *   <li>{@code yg} {@code EntityMob}: {@code <init>} (experienceValue 5 = {@link Monster}'s {@code xpReward}),
 *       {@code e} onLivingUpdate ({@link Monster#aiStep}: swing progress, age +2 in bright light), {@code h} onUpdate
 *       (peaceful despawn = {@code Monster.shouldDespawnInPeaceful}), {@code bR} findPlayerToAttack,
 *       {@code a(ro,F)} attackEntityFrom, {@code n} attackEntityAsMob, {@code a(sa,F)} attackEntity, {@code a(III)}
 *       getBlockPathWeight, {@code j_} isValidLightLevel (now {@code entity.ai.LegacyLightLevel}, R21);</li>
 *   <li>{@code td} {@code EntityCreature}: {@code bq} updateEntityActionState, {@code bQ} updateWanderPath;</li>
 *   <li>{@code sw} {@code EntityLiving}: {@code bq} updateEntityActionState (the fallback without a path);</li>
 *   <li>{@code sv} {@code EntityLivingBase}: {@code bq} (++entityAge), {@code bl} getAIMoveSpeed (0.1 without AI),
 *       the {@code attackTime} countdown in {@code C} onEntityUpdate.</li>
 * </ul>
 *
 * <p>How the old path maps onto the 1.21.1 tick ({@code Mob.serverAiStep} is final): the whole
 * {@code updateEntityActionState} runs in {@link #customServerAiStep}. Its movement is the 1.7.10 one, not a
 * {@code MoveControl}: {@code moveForward} is written straight into {@code zza} with the <em>attribute value</em>
 * (0.32, or 6.52 with the Ender attack boost), and {@link #getSpeed()} is the constant 0.1 of
 * {@code getAIMoveSpeed()} - {@code moveFlying} and {@code moveRelative} both clamp the input length to at least 1,
 * so the boost only lifts the input to full speed, exactly as in 1.7.10. Three 1.21.1 controls would overwrite
 * that state after {@code customServerAiStep} and are neutralised:
 * {@link InsectSupport.LegacyMoveControl} keeps {@code zza}, {@link LegacyLookControl} copies the body yaw into the
 * head ({@code rotationYawHead = rotationYaw}, EntityLivingBase.onLivingUpdate after the AI call) instead of
 * turning or pitching, and {@code isJumping} reaches {@code jumping} through {@code JumpControl.jump()}.
 *
 * <p>PORT (R18 case 3), where the 1.21.1 frame differs:
 * <ul>
 *   <li>{@code ++entityAge} and {@code despawnEntity()} ran only on ticks without a path (inside
 *       {@code EntityLiving.updateEntityActionState}); {@code Mob.serverAiStep} counts {@code noActionTime} and
 *       {@code ServerLevel} runs {@code checkDespawn} on every tick. Both reset {@code noActionTime} to 0 while a
 *       player is within 32 blocks, so the wander rule {@code entityAge < 100} is the same whenever it matters.</li>
 *   <li>Paths come from the 1.21.1 {@code PathNavigation.createPath} (A*, other node costs), which also refuses to
 *       plan while the mob is airborne ({@code GroundPathNavigation.canUpdatePath}). The flags of
 *       {@code getPathEntityToEntity(..., true, false, false, true)} are open doors yes, closed doors no, avoid water
 *       no, swim yes: {@code setCanFloat(true)}, the rest is the ground navigation's default. The entity path range
 *       16 is {@code FOLLOW_RANGE}; the wander path passes its range 10 explicitly.</li>
 *   <li>{@code attackEntityAsMob} is {@code Mob.doHurtTarget}: same attribute damage and 0.6 slowdown on knockback,
 *       with the 1.21.1 enchantment hooks in place of {@code EnchantmentHelper.getEnchantmentModifierLiving}.</li>
 *   <li>{@code fleeingTick} is only ever set by {@code EntityAnimal.attackEntityFrom}; for an {@code EntityMob} its
 *       branches (flee-speed modifier removal, forced wander) are dead and not carried over.</li>
 * </ul>
 */
public abstract class LegacyEntityMob extends Monster {

    /** {@code EntityCreature.entityToAttack} ({@code bm}): any entity, not synchronised, not the 1.21.1 target. */
    @Nullable
    protected Entity entityToAttack;
    /** {@code EntityCreature.pathToEntity} ({@code bp}). */
    @Nullable
    protected Path pathToEntity;
    /** {@code EntityCreature.hasAttacked} ({@code bn}) = {@link #isMovementCeased()}, always false here. */
    protected boolean hasAttacked;
    /** {@code EntityLivingBase.attackTime} ({@code aB}), counted down once per tick. */
    protected int attackTime;
    /** {@code EntityLivingBase.isJumping} ({@code bc}); handed to the jump control after the AI step. */
    protected boolean isJumping;
    /** {@code EntityLiving.currentTarget} ({@code bu}), the player looked at while idle. */
    @Nullable
    private Entity currentTarget;
    /** {@code EntityLiving.numTicksToChaseTarget} ({@code g}). */
    private int numTicksToChaseTarget;
    /** {@code EntityLivingBase.randomYawVelocity} ({@code bf}). */
    protected float randomYawVelocity;

    protected LegacyEntityMob(final EntityType<? extends LegacyEntityMob> type, final Level level) {
        super(type, level);
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
        this.lookControl = new LegacyLookControl(this);
        this.getNavigation().setCanFloat(true);
    }

    /** {@code EntityLivingBase.getAIMoveSpeed()} ({@code sv.bl}): 0.1 for an entity without AI tasks. */
    @Override
    public float getSpeed() {
        return 0.1f;
    }

    /** The {@code attackTime} countdown of {@code EntityLivingBase.onEntityUpdate} ({@code sv.C}, pc 346-363). */
    @Override
    public void baseTick() {
        super.baseTick();
        if (this.attackTime > 0) {
            --this.attackTime;
        }
    }

    /**
     * {@code EntityLivingBase.onLivingUpdate} ({@code sv.e}): the non-AI branch calls
     * {@code updateEntityActionState()} on the server; afterwards {@code randomYawVelocity *= 0.9}.
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.updateEntityActionState();
        this.randomYawVelocity *= 0.9f;
        if (this.isJumping) {
            this.getJumpControl().jump();
        }
    }

    /** {@code EntityCreature.updateEntityActionState} ({@code td.bq}, pc 0-813). */
    protected void updateEntityActionState() {
        this.hasAttacked = this.isMovementCeased();
        final float f4 = 16.0f;
        if (this.entityToAttack == null) {
            this.entityToAttack = this.findPlayerToAttack();
            if (this.entityToAttack != null) {
                this.pathToEntity = this.getPathEntityToEntity(this.entityToAttack);
            }
        } else if (this.entityToAttack.isAlive()) {
            final float f = this.entityToAttack.distanceTo(this);
            if (this.hasLineOfSight(this.entityToAttack)) {
                this.attackEntity(this.entityToAttack, f);
            }
        } else {
            this.entityToAttack = null;
        }
        if (this.entityToAttack instanceof ServerPlayer player && player.isCreative()) {
            this.entityToAttack = null;
        }
        if (!this.hasAttacked && this.entityToAttack != null && (this.pathToEntity == null || this.random.nextInt(20) == 0)) {
            this.pathToEntity = this.getPathEntityToEntity(this.entityToAttack);
        } else if (!this.hasAttacked
                && ((this.pathToEntity == null && this.random.nextInt(180) == 0) || this.random.nextInt(120) == 0)
                && this.noActionTime < 100) {
            this.updateWanderPath();
        }
        final int i = Mth.floor(this.getBoundingBox().minY + 0.5);
        final boolean flag = this.isInWater();
        final boolean flag1 = this.isInLava();
        this.setXRot(0.0f);
        if (this.pathToEntity == null || this.random.nextInt(100) == 0) {
            this.livingUpdateEntityActionState();
            this.pathToEntity = null;
            return;
        }
        // PORT (R18 case 1): a 1.21.1 path can arrive with no node left; getNextEntityPos would throw. The original's
        // PathEntity never had zero points, so this is the "finished" branch of the loop below.
        Vec3 vec3 = null;
        if (this.pathToEntity.isDone()) {
            this.pathToEntity = null;
        } else {
            vec3 = this.pathToEntity.getNextEntityPos(this);
        }
        final double d0 = this.getBbWidth() * 2.0f;
        while (vec3 != null && vec3.distanceToSqr(this.getX(), vec3.y, this.getZ()) < d0 * d0) {
            this.pathToEntity.advance();
            if (this.pathToEntity.isDone()) {
                vec3 = null;
                this.pathToEntity = null;
            } else {
                vec3 = this.pathToEntity.getNextEntityPos(this);
            }
        }
        this.isJumping = false;
        if (vec3 != null) {
            final double d1 = vec3.x - this.getX();
            final double d2 = vec3.z - this.getZ();
            final double d3 = vec3.y - i;
            final float f1 = (float) (Math.atan2(d2, d1) * 180.0 / 3.1415927410125732) - 90.0f;
            float f2 = Mth.wrapDegrees(f1 - this.getYRot());
            this.zza = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (f2 > 30.0f) {
                f2 = 30.0f;
            }
            if (f2 < -30.0f) {
                f2 = -30.0f;
            }
            this.setYRot(this.getYRot() + f2);
            if (this.hasAttacked && this.entityToAttack != null) {
                final double d4 = this.entityToAttack.getX() - this.getX();
                final double d5 = this.entityToAttack.getZ() - this.getZ();
                final float f3 = this.getYRot();
                this.setYRot((float) (Math.atan2(d5, d4) * 180.0 / 3.1415927410125732) - 90.0f);
                f2 = (f3 - this.getYRot() + 90.0f) * 3.1415927f / 180.0f;
                this.xxa = -Mth.sin(f2) * this.zza * 1.0f;
                this.zza = Mth.cos(f2) * this.zza * 1.0f;
            }
            if (d3 > 0.0) {
                this.isJumping = true;
            }
        }
        if (this.entityToAttack != null) {
            this.lookAt(this.entityToAttack, 30.0f, 30.0f);
        }
        if (this.horizontalCollision && !this.hasPath()) {
            this.isJumping = true;
        }
        if (this.random.nextFloat() < 0.8f && (flag || flag1)) {
            this.isJumping = true;
        }
    }

    /**
     * {@code EntityLiving.updateEntityActionState} ({@code sw.bq}): idle strafe/forward reset, occasionally look at
     * the nearest player within 8 for 10..29 ticks, otherwise drift the yaw; jump in water or lava with 0.8.
     * {@code ++entityAge} ({@code sv.bq}) and {@code despawnEntity()} are 1.21.1's own per-tick bookkeeping (see
     * the class comment).
     */
    protected void livingUpdateEntityActionState() {
        this.xxa = 0.0f;
        this.zza = 0.0f;
        final float f = 8.0f;
        if (this.random.nextFloat() < 0.02f) {
            // PORT: EntityGetter.getNearestPlayer(Entity, double) is getClosestPlayerToEntity without spectators,
            // which 1.7.10 did not have (same reading as entity.ai.EntityAIWatchClosest).
            final Player entityplayer = this.level().getNearestPlayer(this, (double) f);
            if (entityplayer != null) {
                this.currentTarget = entityplayer;
                this.numTicksToChaseTarget = 10 + this.random.nextInt(20);
            } else {
                this.randomYawVelocity = (this.random.nextFloat() - 0.5f) * 20.0f;
            }
        }
        if (this.currentTarget != null) {
            this.lookAt(this.currentTarget, 10.0f, (float) this.getMaxHeadXRot());
            if (this.numTicksToChaseTarget-- <= 0 || this.currentTarget.isRemoved()
                    || this.currentTarget.distanceToSqr(this) > (double) (f * f)) {
                this.currentTarget = null;
            }
        } else {
            if (this.random.nextFloat() < 0.05f) {
                this.randomYawVelocity = (this.random.nextFloat() - 0.5f) * 20.0f;
            }
            this.setYRot(this.getYRot() + this.randomYawVelocity);
            this.setXRot(0.0f); // defaultPitch, never changed for these mobs
        }
        final boolean flag = this.isInWater();
        final boolean flag1 = this.isInLava();
        if (flag || flag1) {
            this.isJumping = this.random.nextFloat() < 0.8f;
        }
    }

    /** {@code EntityCreature.updateWanderPath} ({@code td.bQ}): best-lit-weight block of ten tries, path range 10. */
    protected void updateWanderPath() {
        boolean flag = false;
        int i = -1;
        int j = -1;
        int k = -1;
        float f = -99999.0f;
        for (int l = 0; l < 10; ++l) {
            final int i1 = Mth.floor(this.getX() + this.random.nextInt(13) - 6.0);
            final int j1 = Mth.floor(this.getY() + this.random.nextInt(7) - 3.0);
            final int k1 = Mth.floor(this.getZ() + this.random.nextInt(13) - 6.0);
            final float f1 = this.getBlockPathWeight(i1, j1, k1);
            if (f1 > f) {
                f = f1;
                i = i1;
                j = j1;
                k = k1;
                flag = true;
            }
        }
        if (flag) {
            this.pathToEntity = this.getEntityPathToXYZ(i, j, k);
        }
    }

    /** {@code EntityCreature.isMovementCeased} ({@code td.bP}): false, not overridden by {@code EntityMob}. */
    protected boolean isMovementCeased() {
        return false;
    }

    /** {@code EntityCreature.hasPath} ({@code td.bS}). */
    protected boolean hasPath() {
        return this.pathToEntity != null;
    }

    /** {@code EntityMob.findPlayerToAttack} ({@code yg.bR}): the nearest vulnerable player within 16, if visible. */
    @Nullable
    protected Entity findPlayerToAttack() {
        final Player entityplayer = this.getClosestVulnerablePlayerToEntity(16.0);
        return entityplayer != null && this.hasLineOfSight(entityplayer) ? entityplayer : null;
    }

    /**
     * {@code World.getClosestVulnerablePlayerToEntity} ({@code ahb.b(sa, D)} -> {@code ahb.b(DDDD)}): players whose
     * {@code capabilities.disableDamage} is off and who are alive; the range shrinks by 0.8 for sneaking and by
     * {@code 0.7 * max(armor visibility, 0.1)} for invisible players; a negative range accepts everyone. Spectators
     * have {@code invulnerable} set in 1.21.1 and drop out with the creative players, as they would have.
     */
    @Nullable
    protected Player getClosestVulnerablePlayerToEntity(final double par2) {
        double d4 = -1.0;
        Player entityplayer = null;
        for (final Player entityplayer1 : this.level().players()) {
            if (entityplayer1.getAbilities().invulnerable || !entityplayer1.isAlive()) {
                continue;
            }
            final double d5 = entityplayer1.distanceToSqr(this.getX(), this.getY(), this.getZ());
            double d6 = par2;
            if (entityplayer1.isShiftKeyDown()) {
                d6 *= 0.800000011920929;
            }
            if (entityplayer1.isInvisible()) {
                float f = entityplayer1.getArmorCoverPercentage();
                if (f < 0.1f) {
                    f = 0.1f;
                }
                d6 *= 0.7f * f;
            }
            if ((par2 < 0.0 || d5 < d6 * d6) && (d4 == -1.0 || d5 < d4)) {
                d4 = d5;
                entityplayer = entityplayer1;
            }
        }
        return entityplayer;
    }

    /**
     * {@code EntityMob.attackEntity} ({@code yg.a(sa, F)}): melee when the cooldown is over, closer than 2 and the
     * boxes overlap vertically; cooldown 20 ticks.
     */
    protected void attackEntity(final Entity par1Entity, final float par2) {
        if (this.attackTime <= 0 && par2 < 2.0f && par1Entity.getBoundingBox().maxY > this.getBoundingBox().minY
                && par1Entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.attackTime = 20;
            this.attackEntityAsMob(par1Entity);
        }
    }

    /** {@code EntityMob.attackEntityAsMob} ({@code yg.n}); see the class comment for the {@code doHurtTarget} note. */
    protected boolean attackEntityAsMob(final Entity par1Entity) {
        return this.doHurtTarget(par1Entity);
    }

    /**
     * {@code EntityMob.getBlockPathWeight} ({@code yg.a(III)}): {@code 0.5 - world.getLightBrightness}. The 1.21.1
     * brightness table lookup is {@code getLightLevelDependentMagicValue(BlockPos)}.
     */
    protected float getBlockPathWeight(final int par1, final int par2, final int par3) {
        return 0.5f - this.level().getLightLevelDependentMagicValue(new BlockPos(par1, par2, par3));
    }

    /**
     * {@code EntityMob.attackEntityFrom} ({@code yg.a(ro, F)}): after damage went through, the source's entity becomes
     * {@code entityToAttack} - any entity, not only players - unless it rides this mob or is ridden by it. With no
     * source entity the rider/vehicle comparison matches {@code null} and nothing changes, as in the bytecode.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        if (this.isInvulnerable()) {
            return false;
        }
        if (super.hurt(par1DamageSource, par2)) {
            final Entity entity = par1DamageSource.getEntity();
            if (this.getFirstPassenger() == entity || this.getVehicle() == entity) {
                return true;
            }
            if (entity != this) {
                this.entityToAttack = entity;
            }
            return true;
        }
        return false;
    }

    /** {@code World.getPathEntityToEntity(this, target, 16, true, false, false, true)}. */
    @Nullable
    protected Path getPathEntityToEntity(final Entity target) {
        return this.getNavigation().createPath(target, 0);
    }

    /** {@code World.getEntityPathToXYZ(this, x, y, z, 10, true, false, false, true)}. */
    @Nullable
    protected Path getEntityPathToXYZ(final int x, final int y, final int z) {
        // PathNavigation.createPath(BlockPos, int, int) passes its second int as accuracy and its third as range.
        return this.getNavigation().createPath(new BlockPos(x, y, z), 0, 10);
    }

    /**
     * The head of an entity without AI followed its body: {@code rotationYawHead = rotationYaw} after
     * {@code updateEntityActionState}. The pitch is the AI's own ({@code faceEntity}), so nothing is reset here.
     */
    static final class LegacyLookControl extends LookControl {

        LegacyLookControl(final Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            this.mob.setYHeadRot(this.mob.getYRot());
        }
    }
}
