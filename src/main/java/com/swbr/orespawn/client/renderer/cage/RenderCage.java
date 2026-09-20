package com.swbr.orespawn.client.renderer.cage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.renderer.RenderSpinner;
import com.swbr.orespawn.entity.cage.EntityCage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Port of {@code danger.orespawn.RenderCage}: {@link RenderSpinner} with the {@code spinners.png} tile
 * taken from {@link EntityCage#getCageIndex()}. The client's cage is built by the registry factory, so
 * the index is the default 160, the empty critter cage (the only cage that is ever thrown).
 */
public class RenderCage extends RenderSpinner<EntityCage> {

    public RenderCage(final EntityRendererProvider.Context context) {
        super(context);
    }

    /** {@code doRender} (RenderCage.java:8-15). */
    @Override
    public void render(final EntityCage par1Entity, final float entityYaw, final float partialTick, final PoseStack poseStack,
            final MultiBufferSource buffer, final int packedLight) {
        this.spinItemIconIndex = 160;
        if (par1Entity instanceof EntityCage) {
            final EntityCage var2 = par1Entity;
            this.spinItemIconIndex = var2.getCageIndex();
        }
        super.render(par1Entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
