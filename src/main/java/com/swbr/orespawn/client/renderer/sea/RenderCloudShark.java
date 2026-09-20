package com.swbr.orespawn.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CloudSharkModel;
import com.swbr.orespawn.entity.sea.CloudShark;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCloudShark} (RenderCloudShark.java:9-49), a {@code RenderLiving} with a
 * {@code ModelCloudShark}. ClientProxyOreSpawn (:101): {@code (new ModelCloudShark(1.0f), 0.5f, 1.0f)} - register as
 * {@code ctx -> new RenderCloudShark(ctx, 0.5f, 1.0f, 1.0f)}. Shadow {@code par2 * par3} (:15), scale {@code par3} in
 * {@code preRenderCallback} (:34-40), one texture (:42-48).
 */
public class RenderCloudShark extends MobRenderer<CloudShark, CloudSharkModel> {

    /** {@code CloudShark.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/cloudshark.png");

    protected CloudSharkModel model;
    private float scale = 1.0f;

    /** {@code RenderCloudShark(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderCloudShark(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new CloudSharkModel(context.bakeLayer(CloudSharkModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final CloudShark par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final CloudShark entity) {
        return texture;
    }
}
