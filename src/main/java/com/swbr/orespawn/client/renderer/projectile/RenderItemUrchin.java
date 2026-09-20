package com.swbr.orespawn.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.renderer.RenderSpinner;
import com.swbr.orespawn.entity.projectile.Acid;
import com.swbr.orespawn.entity.projectile.DeadIrukandji;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.InkSack;
import com.swbr.orespawn.entity.projectile.LaserBall;
import com.swbr.orespawn.entity.projectile.SunspotUrchin;
import com.swbr.orespawn.entity.projectile.WaterBall;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

/**
 * Port of {@code danger.orespawn.RenderItemUrchin}: the {@link RenderSpinner} for the seven sprite
 * projectiles, picking the {@code spinners.png} tile from the entity (RenderItemUrchin.java:10-40).
 * The checks run in the original order, so the subclasses of LaserBall (checked after it) end on
 * their own tile: IceBall 84, Acid 85, DeadIrukandji 86.
 *
 * <p>PORT: the first branch, {@code if (entity instanceof BerthaHit) return;}, is not here.
 * ClientProxyOreSpawn.java:30 registered this renderer for BerthaHit only to draw nothing; the
 * BerthaHit port (w04-bows-bertha) registers a {@code NoopRenderer} for it instead, which keeps this
 * class free of a reference into another package that is written in the same wave.
 */
public class RenderItemUrchin extends RenderSpinner<Entity> {

    public RenderItemUrchin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity instanceof SunspotUrchin var2) {
            this.spinItemIconIndex = var2.getUrchinIndex();
        }
        if (entity instanceof WaterBall var3) {
            this.spinItemIconIndex = var3.getWaterBallIndex();
        }
        if (entity instanceof InkSack var4) {
            this.spinItemIconIndex = var4.getInkSackIndex();
        }
        if (entity instanceof LaserBall var5) {
            this.spinItemIconIndex = var5.getLaserBallIndex();
        }
        if (entity instanceof IceBall var6) {
            this.spinItemIconIndex = var6.getIceBallIndex();
        }
        if (entity instanceof Acid var7) {
            this.spinItemIconIndex = var7.getAcidIndex();
        }
        if (entity instanceof DeadIrukandji var8) {
            this.spinItemIconIndex = var8.getIrukandjiIndex();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
