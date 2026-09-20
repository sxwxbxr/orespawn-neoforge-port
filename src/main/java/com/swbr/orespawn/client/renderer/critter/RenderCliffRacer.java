package com.swbr.orespawn.client.renderer.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CliffRacerModel;
import com.swbr.orespawn.entity.critter.CliffRacer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCliffRacer} (RenderCliffRacer.java:9-49), a {@code RenderLiving} with a
 * {@code ModelCliffRacer}. ClientProxyOreSpawn (:88): {@code (new ModelCliffRacer(1.0f), 0.3f, 1.0f)} -
 * register as {@code ctx -> new RenderCliffRacer(ctx, 0.3f, 1.0f, 1.0f)}. Shadow {@code par2 * par3} (:15),
 * scale {@code par3} (:34-40), one texture (:42-48).
 */
public class RenderCliffRacer extends MobRenderer<CliffRacer, CliffRacerModel> {

    /** {@code Cliffracertexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/cliffracertexture.png");

    protected CliffRacerModel model;
    private float scale = 1.0f;

    /** {@code RenderCliffRacer(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderCliffRacer(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new CliffRacerModel(context.bakeLayer(CliffRacerModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final CliffRacer par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final CliffRacer entity) {
        return texture;
    }
}
