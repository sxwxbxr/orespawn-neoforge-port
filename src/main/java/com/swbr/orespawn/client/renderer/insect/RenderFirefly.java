package com.swbr.orespawn.client.renderer.insect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.model.FireflyModel;
import com.swbr.orespawn.entity.insect.Firefly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderFirefly} (RenderFirefly.java:9-45), a {@code RenderLiving} with a
 * {@code ModelFirefly}. ClientProxyOreSpawn: {@code (new ModelFirefly(2.5f), 0.2f, 0.75f)} - register as
 * {@code ctx -> new RenderFirefly(ctx, 0.2f, 0.75f, 2.5f)}. Shadow {@code par2 * par3} (:15), scale
 * {@code par3} (:33-39), texture from the entity (:41-44). The blinking tail light is the model's.
 */
public class RenderFirefly extends MobRenderer<Firefly, FireflyModel> {

    protected FireflyModel model;
    private float scale = 1.0f;

    /** {@code RenderFirefly(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderFirefly(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new FireflyModel(context.bakeLayer(FireflyModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:33-39). */
    @Override
    protected void scale(final Firefly par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:41-44). */
    @Override
    public ResourceLocation getTextureLocation(final Firefly entity) {
        return entity.getTexture();
    }
}
