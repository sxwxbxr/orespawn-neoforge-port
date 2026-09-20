package com.swbr.orespawn.client.renderer.ender;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.model.FairyModel;
import com.swbr.orespawn.entity.fairy.Fairy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderFairy} (RenderFairy.java:9-45), a {@code RenderLiving} with a
 * {@code ModelFairy}. ClientProxyOreSpawn: {@code (new ModelFairy(1.5f), 0.1f, 0.35f)} - register as
 * {@code ctx -> new RenderFairy(ctx, 0.1f, 0.35f, 1.5f)}. Shadow {@code par2 * par3} = 0.035 (:15), scale
 * {@code par3} (:33-39), texture variant from the entity's {@code fairy_type} (:41-44). The blink glow is the
 * model's.
 *
 * <p>Lives in {@code client.renderer.ender} with the two Ender renderers of the same porter assignment.
 */
public class RenderFairy extends MobRenderer<Fairy, FairyModel> {

    protected FairyModel model;
    private float scale = 1.0f;

    /** {@code RenderFairy(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderFairy(final EntityRendererProvider.Context context, final float par2, final float par3,
                       final float wingspeed) {
        super(context, new FairyModel(context.bakeLayer(FairyModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:33-39). */
    @Override
    protected void scale(final Fairy par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:41-44). */
    @Override
    public ResourceLocation getTextureLocation(final Fairy entity) {
        final Fairy a = entity;
        return a.getTexture(a);
    }
}
