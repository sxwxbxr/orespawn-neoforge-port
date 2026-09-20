package com.swbr.orespawn.entity.companion;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.projectile.LegacyThrowable;
import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.Shoes} (verhalten/entity-12.md, design/design-entities-05.md): the
 * spinning shoe the Girlfriend throws (id 2..5), the Boyfriend's game controller (id 6) and the
 * {@code ItemShoes} throw of a player.
 *
 * <p>Not a {@code ThrowableItemProjectile}: {@code RenderShoe} draws tile {@link #getShoeId()} of
 * {@code spinners.png} through {@code RenderSpinner}, so the id stays a synced integer (DataWatcher 20).
 * Tiles 2..6 are red heel, black heel, turquoise slipper, brown boot and game controller - the same
 * pictures as the five item icons (checked against the PNG, catalogue 6.5).
 *
 * <p>Flight, launch and hit detection are the 1.7.10 {@code EntityThrowable} of {@link LegacyThrowable}
 * ({@code zk.h}): the thrower is skipped for five ticks, candidate boxes grow by 0.3 and are tested with
 * {@code calculateIntercept}, blocks are traced with the selection box, the heading spreads with a
 * Gaussian. The 0.25 x 0.25 hitbox is the {@code EntityThrowable} {@code setSize} and belongs on the
 * entity type.
 *
 * <p>The id is not saved, as in the original: a reloaded shoe rolls a new one (2..5).
 */
public class Shoes extends LegacyThrowable {

    /** DataWatcher 20: {@code ShoeId}. */
    private static final EntityDataAccessor<Integer> DATA_SHOE_ID = SynchedEntityData.defineId(Shoes.class, EntityDataSerializers.INT);

    public int ShoeId;
    private float my_rotation;

    /** {@code Shoes(World)} (:15-21); also the entity type factory. */
    public Shoes(final EntityType<? extends Shoes> type, final Level par1World) {
        super(type, par1World);
        this.ShoeId = 0;
        this.my_rotation = 0.0f;
        this.ShoeId = this.random.nextInt(4) + 2;
        this.entityData.set(DATA_SHOE_ID, this.ShoeId);
    }

    /** {@code Shoes(World, int)} (:23-29). */
    public Shoes(final Level par1World, final int par2) {
        super(ModEntities.SHOES.get(), par1World);
        this.ShoeId = 0;
        this.my_rotation = 0.0f;
        this.ShoeId = par2;
        this.entityData.set(DATA_SHOE_ID, this.ShoeId);
    }

    /** {@code Shoes(World, EntityLivingBase)} (:31-37): launched from the thrower's eyes along his view. */
    public Shoes(final Level par1World, final LivingEntity par2EntityLiving) {
        super(ModEntities.SHOES.get(), par1World, par2EntityLiving);
        this.ShoeId = 0;
        this.my_rotation = 0.0f;
        this.ShoeId = this.random.nextInt(4) + 2;
        this.entityData.set(DATA_SHOE_ID, this.ShoeId);
    }

    /** {@code Shoes(World, EntityLivingBase, int)} (:39-45): the throw of Girlfriend, Boyfriend and ItemShoes. */
    public Shoes(final Level par1World, final LivingEntity par2EntityLiving, final int par3) {
        super(ModEntities.SHOES.get(), par1World, par2EntityLiving);
        this.ShoeId = 0;
        this.my_rotation = 0.0f;
        this.ShoeId = par3;
        this.entityData.set(DATA_SHOE_ID, this.ShoeId);
    }

    /** {@code Shoes(World, double, double, double)} (:47-53). */
    public Shoes(final Level par1World, final double par2, final double par4, final double par6) {
        super(ModEntities.SHOES.get(), par1World, par2, par4, par6);
        this.ShoeId = 0;
        this.my_rotation = 0.0f;
        this.ShoeId = this.random.nextInt(4) + 2;
        this.entityData.set(DATA_SHOE_ID, this.ShoeId);
    }

    /**
     * PORT: the original added DataWatcher 20 in every constructor with the id itself. 1.21.1 defines
     * the slot before any constructor body runs, so it starts at 0 and each constructor sets the id; the
     * client's own random roll is overwritten by the server's value, which always differs from 0.
     */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        builder.define(DATA_SHOE_ID, 0);
    }

    public int getShoeId() {
        return this.entityData.get(DATA_SHOE_ID);
    }

    /**
     * {@code onImpact} (:59-89): block hits on both sides, entity hits on the server only
     * ({@code zk.h}). Damage in override order: 2, 6 for the game controller, +4 against a creeper, 1
     * against Girlfriend/Boyfriend, 0 against a player, and 10 on Valentine's Day against anything.
     */
    @Override
    protected void onImpact(final HitResult par1MovingObjectPosition) {
        if (par1MovingObjectPosition.getType() == HitResult.Type.ENTITY) {
            final Entity entityHit = ((EntityHitResult) par1MovingObjectPosition).getEntity();
            float var2 = 2.0f;
            if (this.getShoeId() == 6) {
                var2 = 6.0f;
            }
            if (entityHit instanceof Creeper) {
                var2 += 4.0f;
            }
            if (entityHit instanceof Girlfriend) {
                var2 = 1.0f;
            }
            if (entityHit instanceof Boyfriend) {
                var2 = 1.0f;
            }
            if (entityHit instanceof Player) {
                var2 = 0.0f;
            }
            if (OreSpawn.valentines_day != 0) {
                var2 = 10.0f;
            }
            // DamageSource.causeThrownDamage(this, getThrower()), argument order as vanilla Snowball.
            entityHit.hurt(this.damageSources().thrown(this, this.getOwner()), var2);
        }
        for (int var3 = 0; var3 < 4; ++var3) {
            this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            // "reddust" with zero motion is pure red, the redstone dust colour.
            this.level().addParticle(DustParticleOptions.REDSTONE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /** {@code onUpdate} (:91-100): the shoe spins 20 degrees a tick around its pitch. */
    @Override
    public void tick() {
        super.tick();
        this.my_rotation += 20.0f;
        while (this.my_rotation > 360.0f) {
            this.my_rotation -= 360.0f;
        }
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation;
        this.setXRot(my_rotation);
    }
}
