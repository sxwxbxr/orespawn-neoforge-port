package com.swbr.orespawn.client.renderer.cannonfodder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GazelleModel;
import com.swbr.orespawn.entity.cannonfodder.Gazelle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGazelle} (RenderGazelle.java:9-53), registered as
 * {@code new RenderGazelle(new ModelGazelle(0.65f), 0.45f, 1.0f)} (manifest): {@code ctx -> new RenderGazelle(ctx,
 * 0.65f, 0.45f, 1.0f)}. One texture, {@code Gazelletexture.png} (lower-cased by tools/assets.py).
 */
public class RenderGazelle extends MobRenderer<Gazelle, GazelleModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/gazelletexture.png");

    protected GazelleModel model;
    private float scale;

    /**
     * {@code RenderGazelle(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelGazelle} constructor argument
     */
    public RenderGazelle(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new GazelleModel(context.bakeLayer(GazelleModel.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-44): half scale for a baby. */
    @Override
    protected void scale(final Gazelle par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /**
     * PORT: {@code MobRenderer} halves the shadow of a baby and scales it by the size attribute; 1.7.10
     * {@code RenderLiving} drew the constructor shadow for every gazelle.
     */
    @Override
    protected float getShadowRadius(final Gazelle entity) {
        return this.shadowRadius;
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final Gazelle entity) {
        return texture;
    }
}
