package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.entity.sea.AttackSquid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.InkSack} (entity {@code ink_sack}), the AttackSquid's ink shot:
 * 1 damage, 4 against creepers, and a coin flip for Blindness 0 over 100-450 ticks. WaterDragon and
 * AttackSquid are passed through with an early return (verhalten/entity-09.md). No item.
 */
public class InkSack extends LegacyThrowable {

    private float my_rotation = 0.0F;
    private final int my_index = 65;

    /** Registry factory; {@code InkSack(World)}. */
    public InkSack(EntityType<? extends InkSack> type, Level level) {
        super(type, level);
    }

    /** {@code InkSack(World, EntityLiving)} - the original took a {@code EntityLiving}, i.e. a {@link Mob}. */
    public InkSack(Level level, Mob thrower) {
        super(ModEntities.INK_SACK.get(), level, thrower);
    }

    /** {@code InkSack(World, double, double, double)} - AttackSquid, WaterDragon. */
    public InkSack(Level level, double x, double y, double z) {
        super(ModEntities.INK_SACK.get(), level, x, y, z);
    }

    /** Spinner tile 65 (RenderItemUrchin.java:20-23). */
    public int getInkSackIndex() {
        return this.my_index;
    }

    /** InkSack.java:49-73. */
    @Override
    protected void onImpact(HitResult result) {
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null) {
            float var2 = 1.0F;
            if (entityHit instanceof Creeper) {
                var2 = 4.0F;
            }
            if (entityHit instanceof WaterDragon) { // InkSack.java:55-57
                return;
            }
            if (entityHit instanceof AttackSquid) { // InkSack.java:58-60
                return;
            }
            entityHit.hurt(this.damageSources().thrown(this, this.getThrower()), var2);
            if (entityHit instanceof LivingEntity living && this.level().random.nextInt(2) == 0) {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100 + 50 * this.level().random.nextInt(8), 0));
            }
        }
        for (int var3 = 0; var3 < 4; ++var3) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getY() + this.random.nextFloat() - this.random.nextFloat(), this.getZ() + this.random.nextFloat(), 0.0, 0.0, 0.0);
        }
        this.playSound(SoundEvents.GENERIC_SPLASH, 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.5F);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /** InkSack.java:76-85. */
    @Override
    public void tick() {
        super.tick();
        this.my_rotation += 30.0F;
        while (this.my_rotation > 360.0F) {
            this.my_rotation -= 360.0F;
        }
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation;
        this.setXRot(my_rotation);
    }
}
