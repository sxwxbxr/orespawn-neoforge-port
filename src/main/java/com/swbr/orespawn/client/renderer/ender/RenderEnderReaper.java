package com.swbr.orespawn.client.renderer.ender;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.EnderReaperModel;
import com.swbr.orespawn.entity.ender.EnderReaper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderEnderReaper} (RenderEnderReaper.java:9-49), a {@code RenderLiving} with a
 * {@code ModelEnderReaper}. ClientProxyOreSpawn: {@code (new ModelEnderReaper(0.23f), 0.2f, 1.0f)} - register as
 * {@code ctx -> new RenderEnderReaper(ctx, 0.2f, 1.0f, 0.23f)}. Shadow {@code par2 * par3} (:16), scale
 * {@code par3} (:34-40), one texture (:42-48).
 */
public class RenderEnderReaper extends MobRenderer<EnderReaper, EnderReaperModel> {

    /** {@code EnderReapertexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/enderreapertexture.png");

    protected EnderReaperModel model;
    private float scale = 1.0f;

    /** {@code RenderEnderReaper(model, par2, par3)} (:15-20) with the model's {@code wingspeed}. */
    public RenderEnderReaper(final EntityRendererProvider.Context context, final float par2, final float par3,
                             final float wingspeed) {
        super(context, new EnderReaperModel(context.bakeLayer(EnderReaperModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final EnderReaper par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final EnderReaper entity) {
        return texture;
    }
}
