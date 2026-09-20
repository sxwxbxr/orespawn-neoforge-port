package com.swbr.orespawn.client.renderer.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CricketModel;
import com.swbr.orespawn.entity.critter.Cricket;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCricket} (RenderCricket.java:9-49), a {@code RenderLiving} with a
 * {@code ModelCricket}. ClientProxyOreSpawn (:140): {@code (new ModelCricket(2.5f), 0.15f, 0.5f)} - register
 * as {@code ctx -> new RenderCricket(ctx, 0.15f, 0.5f, 2.5f)}. Shadow {@code par2 * par3} (:15), scale
 * {@code par3} (:34-40), one texture (:42-48).
 */
public class RenderCricket extends MobRenderer<Cricket, CricketModel> {

    /** {@code Crickettexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/crickettexture.png");

    protected CricketModel model;
    private float scale = 1.0f;

    /** {@code RenderCricket(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderCricket(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new CricketModel(context.bakeLayer(CricketModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Cricket par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Cricket entity) {
        return texture;
    }
}
