package com.swbr.orespawn.client.renderer.crystal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.RotatorModel;
import com.swbr.orespawn.entity.crystal.Rotator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRotator} (RenderRotator.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderRotator(new ModelRotator(0.25f), 0.1f, 1.0f)} (manifest {@code renderer_args}, {@code model_args}):
 * register with {@code ctx -> new RenderRotator(ctx, 0.1f, 1.0f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}, no child halving), one texture
 * ({@code Rotatortexture.png}, lower-cased by the asset generator).
 */
public class RenderRotator extends MobRenderer<Rotator, RotatorModel> {

    protected RotatorModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/rotatortexture.png");

    /** {@code RenderRotator(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderRotator(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new RotatorModel(context.bakeLayer(RotatorModel.LAYER), 0.25f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Rotator par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Rotator entity) {
        return RenderRotator.texture;
    }
}
