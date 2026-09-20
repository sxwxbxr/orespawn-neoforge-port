package com.swbr.orespawn.entity.arrow;

import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.WeaponStats;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.projectile.LegacyThrowable;
import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.BerthaHit} (BerthaHit.java:10-116), registry id {@code bertha_hit}
 * (OreSpawnMain.java:3106-3108: tracking 64 / every tick / velocity updates). The invisible reach
 * projectile of the four {@code Bertha} swords, spawned on every swing (verhalten/entity-04.md):
 *
 * <table>
 *   <tr><th>type</th><th>items</th><th>range^2 to thrower</th><th>damage</th><th>fire</th><th>push h / v</th><th>explosion</th></tr>
 *   <tr><td>0</td><td>Big Bertha, Slice</td><td>&lt; 81</td><td>{@code bertha_stats.damage} (496)</td><td>10 s</td><td>2.25 / 0.35</td><td>-</td></tr>
 *   <tr><td>2</td><td>Royal Guardian Sword</td><td>&lt; 101</td><td>{@code royal_stats.damage} (746)</td><td>-</td><td>1.5 / 0.25</td><td>-</td></tr>
 *   <tr><td>3</td><td>Attitude Adjuster</td><td>&lt; 64</td><td>{@code hammy_stats.damage} (82)</td><td>-</td><td>1.25 / 0.65</td><td>1.5 on an entity, 2.1 on a block</td></tr>
 * </table>
 *
 * <p>{@code hit_type} is a plain field, neither synchronised nor saved (:12, :51-53): the client
 * copy and a reloaded hit are type 0. Kept (R18) - nothing is drawn, so client and server never
 * show anything different.
 *
 * <p>Flight and hit detection are the 1.7.10 {@code EntityThrowable} of {@link LegacyThrowable}
 * ({@code zk.h}, the only thing {@code onUpdate} :113-115 called): gravity 0.03, drag 0.99 / 0.8 in
 * water; blocks traced with the selection box, so tall grass, flowers, torches, snow layers and portals
 * stop the hit and run {@code onImpact} (type 3 then explodes); entity hits server side through
 * {@code calculateIntercept}, which also hits a mob whose grown box already holds the start point -
 * {@code Bertha.onEntitySwing} spawns the hit two blocks ahead, inside a mob standing in melee range;
 * the thrower is skipped for the first five ticks.
 */
public class BerthaHit extends LegacyThrowable {

    private int hit_type;

    /** {@code BerthaHit(World)} (:14-17) - the registry factory; size 0.33 from the entity type. */
    public BerthaHit(EntityType<? extends BerthaHit> type, Level level) {
        super(type, level);
        this.hit_type = 0;
    }

    /**
     * {@code BerthaHit(World, EntityLivingBase)} (:24-39), the only constructor anything called
     * ({@code Bertha.onEntitySwing}). The super constructor launches the hit once from the thrower
     * (spread 1.0); this constructor then puts it back at eye height, 0.16 to the side, 0.1 down, and
     * heads it along the view at {@code func_70182_d()} = 1.5 with spread 0.1 ({@code func_70183_g()} = 0).
     * The constructors {@code (World, int)} (:19-22) and {@code (World, EntityLivingBase, int)} (:41-44)
     * had no caller and are not ported.
     */
    public BerthaHit(Level level, LivingEntity par2EntityLiving) {
        super(ModEntities.BERTHA_HIT.get(), level, par2EntityLiving);
        this.hit_type = 0;
        this.moveTo(par2EntityLiving.getX(), par2EntityLiving.getY() + par2EntityLiving.getEyeHeight(), par2EntityLiving.getZ(),
                par2EntityLiving.getYRot(), par2EntityLiving.getXRot());
        double posX = this.getX() - (double) (Mth.cos(this.getYRot() / 180.0f * 3.1415927f) * 0.16f);
        double posY = this.getY() - 0.1;
        double posZ = this.getZ() - (double) (Mth.sin(this.getYRot() / 180.0f * 3.1415927f) * 0.16f);
        this.setPos(posX, posY, posZ);
        final float f = 0.4f;
        double motionX = -Mth.sin(this.getYRot() / 180.0f * 3.1415927f) * Mth.cos(this.getXRot() / 180.0f * 3.1415927f) * f;
        double motionZ = Mth.cos(this.getYRot() / 180.0f * 3.1415927f) * Mth.cos(this.getXRot() / 180.0f * 3.1415927f) * f;
        double motionY = -Mth.sin((this.getXRot() + this.getThrowPitchOffset()) / 180.0f * 3.1415927f) * f;
        this.setThrowableHeading(motionX, motionY, motionZ, this.getThrowVelocity(), 0.1f);
    }

    /** {@code BerthaHit(World, double, double, double)} (:46-49). */
    public BerthaHit(Level level, double x, double y, double z) {
        super(ModEntities.BERTHA_HIT.get(), level, x, y, z);
        this.hit_type = 0;
    }

    /** {@code setHitType} (:51-53). */
    public void setHitType(int i) {
        this.hit_type = i;
    }

    /**
     * {@code onImpact} (:55-111). Block hits arrive on both sides, entity hits on the server only
     * ({@code zk.h}); on the client the block branch only removes the local copy.
     */
    @Override
    protected void onImpact(HitResult par1MovingObjectPosition) {
        if (this.isRemoved()) {
            return;
        }
        final Entity entityHit = par1MovingObjectPosition instanceof EntityHitResult ehr ? ehr.getEntity() : null;
        final Entity thrower = this.getOwner();
        final int big_bertha_pvp = OreSpawnConfig.TWEAKS.BigBerthaPvp.get();
        if (entityHit != null && thrower != null) {
            final Entity e = entityHit;
            // Girlfriend and Boyfriend are spared whatever the config says; players only with the guard on.
            if ((big_bertha_pvp == 0 && e instanceof Player) || e instanceof Girlfriend || e instanceof Boyfriend) {
                this.discard();
                return;
            }
            if (big_bertha_pvp == 0 && e instanceof TamableAnimal t) {
                if (t.isTame()) {
                    this.discard();
                    return;
                }
            }
            if (this.hit_type == 0 && this.distanceToSqr(thrower) < 81.0 && e != thrower) {
                e.hurt(this.source(thrower), (float) WeaponStats.bertha_stats().damage());
                e.igniteForSeconds(10.0f);
                final double ks = 2.25;
                double inair = 0.35;
                final float f3 = (float) Math.atan2(e.getZ() - thrower.getZ(), e.getX() - thrower.getX());
                if (e.isRemoved()) {
                    inair *= 2.0;
                }
                e.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            if (this.hit_type == 2 && this.distanceToSqr(thrower) < 101.0 && e != thrower) {
                e.hurt(this.source(thrower), (float) WeaponStats.royal_stats().damage());
                final double ks = 1.5;
                double inair = 0.25;
                final float f3 = (float) Math.atan2(e.getZ() - thrower.getZ(), e.getX() - thrower.getX());
                if (e.isRemoved()) {
                    inair *= 2.0;
                }
                e.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            if (this.hit_type == 3 && this.distanceToSqr(thrower) < 64.0 && e != thrower) {
                e.hurt(this.source(thrower), (float) WeaponStats.hammy_stats().damage());
                final double ks = 1.25;
                double inair = 0.65;
                final float f3 = (float) Math.atan2(e.getZ() - thrower.getZ(), e.getX() - thrower.getX());
                if (e.isRemoved()) {
                    inair *= 2.0;
                }
                e.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
                if (!this.level().isClientSide && this.hit_type == 3 && this.distanceToSqr(thrower) < 64.0) {
                    this.explode(1.5f);
                }
            }
        } else if (!this.level().isClientSide && this.hit_type == 3 && thrower != null && this.distanceToSqr(thrower) < 64.0) {
            // PORT: `thrower != null` added - the original threw an NPE for an entity hit without a
            // thrower (:107, R18 case 1). Every BerthaHit has a thrower, so nothing else changes.
            this.explode(2.1f);
        }
        this.discard();
    }

    /**
     * {@code DamageSource.causePlayerDamage((EntityPlayer) getThrower())}. PORT: the cast failed for
     * a non-player thrower; only players swing a Bertha, so that never happened. A non-player gets the
     * thrown source instead of a {@code ClassCastException} (R18 case 1).
     */
    private DamageSource source(Entity thrower) {
        return thrower instanceof Player player
                ? this.damageSources().playerAttack(player)
                : this.damageSources().thrown(this, thrower);
    }

    /**
     * {@code worldObj.newExplosion(null, posX, posY, posZ, strength, true, mobGriefing)}: always
     * flaming, destroying blocks only with {@code mobGriefing} - {@code MOB} interaction when the rule
     * is on (drop chance 1/strength as in 1.7.10), {@code NONE} otherwise.
     */
    private void explode(float strength) {
        boolean mobGriefing = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), strength, true,
                mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
    }
}
