package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.SunspotUrchin} (entity {@code sunspot_urchin}): 3 damage, 6 against
 * creepers, five seconds of fire - but never against players, who are neither hurt nor lit. A block
 * hit lights the air block on the struck face without asking mobGriefing. The urchin burns while it
 * flies (verhalten/entity-13.md).
 */
public class SunspotUrchin extends LegacyThrowable {

    private float my_rotation = 0.0F;
    private final int my_index = 50;

    /** Registry factory; {@code SunspotUrchin(World)}. */
    public SunspotUrchin(EntityType<? extends SunspotUrchin> type, Level level) {
        super(type, level);
    }

    /** {@code SunspotUrchin(World, EntityLivingBase)} - ItemSunspotUrchin. */
    public SunspotUrchin(Level level, LivingEntity thrower) {
        super(ModEntities.SUNSPOT_URCHIN.get(), level, thrower);
    }

    /** {@code SunspotUrchin(World, double, double, double)} - dispenser. */
    public SunspotUrchin(Level level, double x, double y, double z) {
        super(ModEntities.SUNSPOT_URCHIN.get(), level, x, y, z);
    }

    /** Spinner tile 50 (RenderItemUrchin.java:12-15). */
    public int getUrchinIndex() {
        return this.my_index;
    }

    /** SunspotUrchin.java:51-105. */
    @Override
    protected void onImpact(HitResult result) {
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null) {
            float var2 = 3.0F;
            if (entityHit instanceof Creeper) {
                var2 = 6.0F;
            }
            if (!(entityHit instanceof Player)) {
                entityHit.hurt(this.damageSources().thrown(this, this.getThrower()), var2);
                if (!entityHit.fireImmune()) {
                    entityHit.igniteForSeconds(5.0F);
                }
            }
        } else if (result instanceof BlockHitResult blockHit) {
            // sideHit 0..5 -> the neighbour on the struck face (SunspotUrchin.java:65-93).
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            // PORT: server only; the client's copy of this setBlock was a ghost the server overwrote.
            if (!this.level().isClientSide && this.level().isEmptyBlock(pos)) {
                this.level().setBlockAndUpdate(pos, LegacyProjectiles.fireState(this.level(), pos));
            }
        }
        for (int var3 = 0; var3 < 5; ++var3) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), (double) this.level().random.nextFloat(),
                    (double) this.level().random.nextFloat(), (double) this.level().random.nextFloat());
            this.level().addParticle(LegacyProjectiles.reddust((double) this.level().random.nextFloat(), (double) this.level().random.nextFloat(),
                    (double) this.level().random.nextFloat()), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /** SunspotUrchin.java:107-118. */
    @Override
    public void tick() {
        super.tick();
        this.igniteForSeconds(1.0F);
        this.my_rotation += 30.0F;
        while (this.my_rotation > 360.0F) {
            this.my_rotation -= 360.0F;
        }
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation;
        this.setXRot(my_rotation);
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
    }
}
