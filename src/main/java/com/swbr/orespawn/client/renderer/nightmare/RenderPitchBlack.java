package com.swbr.orespawn.client.renderer.nightmare;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.PitchBlackModel;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderPitchBlack} (RenderPitchBlack.java:9-50), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:90: {@code new RenderPitchBlack(new ModelPitchBlack(0.65f), 1.25f, 1.0f)} - register as
 * {@code ctx -> new RenderPitchBlack(ctx, 1.25f, 1.0f)}; the model's argument is fixed here. Shadow {@code par2 * par3}
 * (not scaled with the size, as in 1.7.10), one texture {@code PitchBlacktexture.png}.
 *
 * <p>Unlike most renderers the stored {@code scale} is never used: {@code preRenderScale} scales by the entity's
 * {@code getPitchBlackScale()} (:34-37), so the five sizes are one renderer.
 */
public class RenderPitchBlack extends MobRenderer<PitchBlack, PitchBlackModel> {

    /** {@code PitchBlacktexture.png} (:48), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/pitchblacktexture.png");

    protected PitchBlackModel model;
    private float scale = 1.0f;

    /** {@code RenderPitchBlack(model, par2, par3)} (:15-20). */
    public RenderPitchBlack(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new PitchBlackModel(context.bakeLayer(PitchBlackModel.LAYER), 0.65f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(pscale)} with the entity's scale (R8). */
    @Override
    protected void scale(final PitchBlack par1Entity, final PoseStack poseStack, final float par2) {
        final float pscale = par1Entity.getPitchBlackScale();
        poseStack.scale(pscale, pscale, pscale);
    }

    /** {@code getEntityTexture} (:43-45). */
    @Override
    public ResourceLocation getTextureLocation(final PitchBlack entity) {
        return texture;
    }
}
