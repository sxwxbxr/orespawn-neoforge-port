package com.swbr.orespawn.client.renderer.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GhostSkellyModel;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGhostSkelly} (RenderGhostSkelly.java:9-49), a {@code RenderLiving} with a
 * {@code ModelGhostSkelly}. ClientProxyOreSpawn.java:40: {@code (new ModelGhostSkelly(), 0.0f, 1.05f)} - register as
 * {@code ctx -> new RenderGhostSkelly(ctx, 0.0f, 1.05f)}. Shadow {@code par2 * par3} = 0 (:18), scale {@code par3}
 * (:28-34). The translucency is the model's (R8).
 */
public class RenderGhostSkelly extends MobRenderer<GhostSkelly, GhostSkellyModel> {

    /** {@code GhostSkellytexture.png} (:37-39), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/ghostskellytexture.png");

    protected GhostSkellyModel model;
    private float scale = 1.0f;

    /** {@code RenderGhostSkelly(model, par2, par3)} (:17-22). */
    public RenderGhostSkelly(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new GhostSkellyModel(context.bakeLayer(GhostSkellyModel.LAYER)), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:28-34). */
    @Override
    protected void scale(final GhostSkelly par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:36-38). */
    @Override
    public ResourceLocation getTextureLocation(final GhostSkelly entity) {
        return texture;
    }
}
