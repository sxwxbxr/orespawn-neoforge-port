package com.swbr.orespawn.entity.portal;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.EntityRedAnt} (EntityRedAnt.java:10-103): {@code red_ant}, the biting
 * ant and portal to the Extreme dimension ("Red Ant", OreSpawnMain.java:3323-3327, tracking 16/1/false).
 *
 * <p>Health 2 (:39-41), speed 0.2 (:19), hitbox 0.2 (:18), attack attribute 1.0 (:35).
 * {@code experienceValue = 1} (:20) is as dead as the base class's 0. The bite itself is a fixed 1.0
 * with a one-in-fifteen chance per call from the shared {@code OreSpawnRand}, never on Peaceful
 * (:43-52); it is called by the attack goal and by a 20-tick clock against the nearest vulnerable
 * player within 1.5 (:79-102). The target goal exists only when {@code PlayNicely} was 0 at
 * construction (:25-27).
 */
public class EntityRedAnt extends EntityAnt {

    int attack_delay;

    /** {@code EntityRedAnt(World)} (:16-28); the base constructor has already added Panic and Wander(9). */
    public EntityRedAnt(final EntityType<? extends EntityRedAnt> type, final Level par1World) {
        super(type, par1World);
        this.attack_delay = 20;
        // setSize(0.2f, 0.2f) is the EntityType size; experienceValue = 1 is dead.
        this.moveSpeed = 0.20000000298023224;
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, LegacyPanic.legacyPanic(this, 1.399999976158142));
        this.goalSelector.addGoal(1, new EntityAIAttackOnCollide(this, Player.class, 1.0, false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 10, 1.0));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(1, new EntityAINearestAttackableTarget<>(this, Player.class, 4, true));
        }
    }

    /** {@code applyEntityAttributes} (:30-37): health 2, speed, attack 1.0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    /** {@code mygetMaxHealth} (:39-41). */
    @Override
    public int mygetMaxHealth() {
        return 2;
    }

    /** {@code attackEntityAsMob} (:43-52). */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (OreSpawn.OreSpawnRand.nextInt(15) != 0) {
            return false;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 1.0f);
        return var4;
    }

    /** {@code interact} (:54-77): into {@code DimensionID2}, or home from there. */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        final InteractionResult pass = checkPortalUse(par1EntityPlayer, hand);
        if (pass != null) {
            return pass;
        }
        if (par1EntityPlayer.level().dimension() != OreSpawnTeleporter.MINING) {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, OreSpawnTeleporter.MINING, this.level());
        } else {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, Level.OVERWORLD, this.level());
        }
        return portalUsed();
    }

    /**
     * {@code onUpdate} (:79-102), on both sides like the original: a client-side bite rolls
     * {@code OreSpawnRand} and then fails inside {@code hurt}, exactly as the 1.7.10 client did.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.attack_delay > 0) {
            --this.attack_delay;
        }
        if (this.attack_delay > 0) {
            return;
        }
        this.attack_delay = 20;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return;
        }
        final LivingEntity e = getClosestVulnerablePlayerToEntity(this, 1.5);
        if (e != null) {
            this.doHurtTarget(e);
        }
    }
}
