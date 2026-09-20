package com.swbr.orespawn.client.renderer.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TshirtModel;
import com.swbr.orespawn.entity.critter.Tshirt;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTshirt} (RenderTshirt.java:9-49), a {@code RenderLiving} with a
 * {@code ModelTshirt}. ClientProxyOreSpawn (:48): {@code (new ModelTshirt(0.22f), 1.0f, 0.33f)} - register as
 * {@code ctx -> new RenderTshirt(ctx, 1.0f, 0.33f, 0.22f)}. Shadow {@code par2 * par3} (:15), scale
 * {@code par3} (:34-40), one texture (:42-48).
 */
public class RenderTshirt extends MobRenderer<Tshirt, TshirtModel> {

    /** {@code Tshirttexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/tshirttexture.png");

    protected TshirtModel model;
    private float scale = 1.0f;

    /** {@code RenderTshirt(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderTshirt(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new TshirtModel(context.bakeLayer(TshirtModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Tshirt par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Tshirt entity) {
        return texture;
    }
}
