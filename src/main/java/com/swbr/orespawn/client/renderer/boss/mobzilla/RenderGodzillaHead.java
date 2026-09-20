package com.swbr.orespawn.client.renderer.boss.mobzilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.entity.boss.mobzilla.GodzillaHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGodzillaHead} (RenderGodzillaHead.java:8-32): a {@code RenderLiving} whose
 * {@code doRender} overloads are empty and whose texture is {@code null}. ClientProxyOreSpawn:
 * {@code new RenderGodzillaHead(null, 0.0f, 0.0f)} - register as {@code RenderGodzillaHead::new}. Nothing is drawn: no model,
 * no name tag, and no shadow ({@code par2 * par3} = 0).
 *
 * <p>PORT: 1.21.1's {@code getTextureLocation} may not return {@code null}; it returns the block atlas like vanilla's
 * {@code NoopRenderer}, which is never bound because {@link #render} draws nothing.
 */
public class RenderGodzillaHead extends EntityRenderer<GodzillaHead> {

    /** {@code RenderGodzillaHead(null, 0.0f, 0.0f)} (:10-12): shadow radius {@code 0 * 0}. */
    public RenderGodzillaHead(final EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    /** {@code doRender} (:17-21): empty. */
    @Override
    public void render(final GodzillaHead entity, final float entityYaw, final float partialTicks, final PoseStack poseStack,
                       final MultiBufferSource buffer, final int packedLight) {
    }

    /** {@code getEntityTexture} (:29-31) returned {@code null}. */
    @Override
    public ResourceLocation getTextureLocation(final GodzillaHead entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
