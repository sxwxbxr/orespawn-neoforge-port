package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.util.MyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.IceBall} (entity {@code ice_ball}): a {@link LaserBall} with the ice
 * flag - 16 damage without fire and always the strength-3 explosion (LaserBall.java:172-174).
 * Royalty is untouched. With {@link #setIceMaker} (the royal family's ice stream) five ice blocks
 * replace whatever stands around the hit point (verhalten/entity-09.md).
 */
public class IceBall extends LaserBall {

    /** Shadows LaserBall's own index, as in the original (IceBall.java:15). */
    private final int my_index = 84;
    private int icemaker = 0;

    /** Registry factory; {@code IceBall(World)}. */
    public IceBall(EntityType<? extends IceBall> type, Level level) {
        super(type, level);
        super.setIceBall();
    }

    /** {@code IceBall(World, EntityLivingBase)} - ItemIceBall. */
    public IceBall(Level level, LivingEntity thrower) {
        super(ModEntities.ICE_BALL.get(), level, thrower);
        super.setIceBall();
    }

    /** {@code IceBall(World, double, double, double)} - dispenser, Dragon, the royal family. */
    public IceBall(Level level, double x, double y, double z) {
        super(ModEntities.ICE_BALL.get(), level, x, y, z);
        super.setIceBall();
    }

    /** Spinner tile 84 (RenderItemUrchin.java:28-31). */
    public int getIceBallIndex() {
        return this.my_index;
    }

    public void setIceMaker(int i) {
        this.icemaker = i;
    }

    /**
     * PORT (BUGHUNT2 2.6): the King's and the Queen's heads are no candidates. The royal salvos start inside the
     * head box that R25 moved to the mouth; hitting it would end every salvo on the royalty discard below. Other
     * candidates are unchanged.
     */
    @Override
    protected boolean canHitCandidate(Entity candidate) {
        return !LegacyProjectiles.isRoyalHead(candidate);
    }

    /** IceBall.java:57-84. */
    @Override
    protected void onImpact(HitResult result) {
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null && MyUtils.isRoyalty(entityHit)) {
            this.discard();
            return;
        }
        super.onImpact(result);
        // PORT: server only. The original also ran this on the client for block hits, with the client's
        // random, and left ghost ice where the server placed none (verhalten/entity-09.md). The blocks
        // still replace anything, bedrock and containers included - 1:1 (R18).
        if (this.icemaker != 0 && !this.level().isClientSide) {
            // hitVec: the entity's position for entity hits (MovingObjectPosition(Entity)), else the intercept.
            Vec3 hitVec = result.getLocation();
            for (int i = 0; i < 5; ++i) {
                int x = this.level().random.nextInt(4);
                if (this.level().random.nextInt(2) == 1) {
                    x = -x;
                }
                int y = this.level().random.nextInt(4);
                if (this.level().random.nextInt(2) == 1) {
                    y = -y;
                }
                int z = this.level().random.nextInt(4);
                if (this.level().random.nextInt(2) == 1) {
                    z = -z;
                }
                // :77-79 (int) hitVec -> Mth.floor (DECISIONS R20).
                x += Mth.floor(hitVec.x);
                y += Mth.floor(hitVec.y);
                z += Mth.floor(hitVec.z);
                this.level().setBlockAndUpdate(new BlockPos(x, y, z), Blocks.ICE.defaultBlockState());
            }
        }
        this.discard();
    }
}
