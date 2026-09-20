package com.swbr.orespawn.client.renderer.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.DragonflyModel;
import com.swbr.orespawn.entity.critter.Dragonfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderDragonfly} (RenderDragonfly.java:9-49), a {@code RenderLiving} with a
 * {@code ModelDragonfly}. ClientProxyOreSpawn (:54): {@code (new ModelDragonfly(2.0f), 0.3f, 1.5f)} - register
 * as {@code ctx -> new RenderDragonfly(ctx, 0.3f, 1.5f, 2.0f)}. Shadow {@code par2 * par3} (:15), scale
 * {@code par3} (:34-40), one texture (:42-48).
 */
public class RenderDragonfly extends MobRenderer<Dragonfly, DragonflyModel> {

    /** {@code dragonfly.png} (:47). */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/dragonfly.png");

    protected DragonflyModel model;
    private float scale = 1.0f;

    /** {@code RenderDragonfly(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderDragonfly(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new DragonflyModel(context.bakeLayer(DragonflyModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Dragonfly par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Dragonfly entity) {
        return texture;
    }
}
