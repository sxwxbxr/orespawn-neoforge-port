package com.swbr.orespawn.client.renderer.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GhostModel;
import com.swbr.orespawn.entity.ghost.Ghost;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGhost} (RenderGhost.java:9-49), a {@code RenderLiving} with a
 * {@code ModelGhost}. ClientProxyOreSpawn.java:39: {@code (new ModelGhost(), 0.0f, 0.65f)} - register as
 * {@code ctx -> new RenderGhost(ctx, 0.0f, 0.65f)}. Shadow {@code par2 * par3} = 0 (:18), scale {@code par3}
 * (:28-34). The translucency is the model's (R8).
 */
public class RenderGhost extends MobRenderer<Ghost, GhostModel> {

    /** {@code Ghosttexture.png} (:37-39), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/ghosttexture.png");

    protected GhostModel model;
    private float scale = 1.0f;

    /** {@code RenderGhost(model, par2, par3)} (:17-22). */
    public RenderGhost(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new GhostModel(context.bakeLayer(GhostModel.LAYER)), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:28-34). */
    @Override
    protected void scale(final Ghost par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:36-38). */
    @Override
    public ResourceLocation getTextureLocation(final Ghost entity) {
        return texture;
    }
}
