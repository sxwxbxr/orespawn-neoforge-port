package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.HerculesBeetleModel;
import com.swbr.orespawn.entity.arthropod.HerculesBeetle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderHerculesBeetle} (RenderHerculesBeetle.java:9-49). ClientProxyOreSpawn registered
 * it as {@code new RenderHerculesBeetle(new ModelHerculesBeetle(1.0f), 0.99f, 1.1f)} (manifest): register with
 * {@code ctx -> new RenderHerculesBeetle(ctx, 0.99f, 1.1f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code Beetletexture.png}, lower-cased).
 */
public class RenderHerculesBeetle extends MobRenderer<HerculesBeetle, HerculesBeetleModel> {

    protected HerculesBeetleModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/beetletexture.png");

    /** {@code RenderHerculesBeetle(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderHerculesBeetle(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new HerculesBeetleModel(context.bakeLayer(HerculesBeetleModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final HerculesBeetle par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final HerculesBeetle entity) {
        return RenderHerculesBeetle.texture;
    }
}
