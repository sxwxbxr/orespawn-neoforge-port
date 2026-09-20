package com.swbr.orespawn.client.renderer.companion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.renderer.RenderSpinner;
import com.swbr.orespawn.entity.companion.Shoes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Port of {@code danger.orespawn.RenderShoe}: {@link RenderSpinner} with the tile index taken from the
 * shoe's synced id (2..6), so the flying shoe is drawn from {@code spinners.png} like in 1.7.10.
 */
public class RenderShoe extends RenderSpinner<Shoes> {

    public RenderShoe(final EntityRendererProvider.Context context) {
        super(context);
    }

    /** {@code doRender} (RenderShoe.java:7-14). */
    @Override
    public void render(final Shoes par1Entity, final float entityYaw, final float partialTick, final PoseStack poseStack,
            final MultiBufferSource buffer, final int packedLight) {
        if (par1Entity instanceof Shoes) {
            final Shoes var2 = par1Entity;
            this.spinItemIconIndex = var2.getShoeId();
        }
        super.render(par1Entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
