package com.swbr.orespawn.client.renderer.ender;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.EnderKnightModel;
import com.swbr.orespawn.entity.ender.EnderKnight;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderEnderKnight} (RenderEnderKnight.java:9-49), a {@code RenderLiving} with a
 * {@code ModelEnderKnight}. ClientProxyOreSpawn: {@code (new ModelEnderKnight(0.21f), 0.3f, 1.0f)} - register as
 * {@code ctx -> new RenderEnderKnight(ctx, 0.3f, 1.0f, 0.21f)}. Shadow {@code par2 * par3} (:16), scale
 * {@code par3} (:34-40), one texture (:42-48).
 */
public class RenderEnderKnight extends MobRenderer<EnderKnight, EnderKnightModel> {

    /** {@code EnderKnighttexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/enderknighttexture.png");

    protected EnderKnightModel model;
    private float scale = 1.0f;

    /** {@code RenderEnderKnight(model, par2, par3)} (:15-20) with the model's {@code wingspeed}. */
    public RenderEnderKnight(final EntityRendererProvider.Context context, final float par2, final float par3,
                             final float wingspeed) {
        super(context, new EnderKnightModel(context.bakeLayer(EnderKnightModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final EnderKnight par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final EnderKnight entity) {
        return texture;
    }
}
