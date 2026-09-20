package com.swbr.orespawn.client.renderer.crystal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.VortexModel;
import com.swbr.orespawn.entity.crystal.Vortex;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderVortex} (RenderVortex.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderVortex(new ModelVortex(0.25f), 0.1f, 1.0f)} (manifest {@code renderer_args}, {@code model_args}):
 * register with {@code ctx -> new RenderVortex(ctx, 0.1f, 1.0f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}, no child halving), one texture
 * ({@code Vortextexture.png}, lower-cased by the asset generator).
 */
public class RenderVortex extends MobRenderer<Vortex, VortexModel> {

    protected VortexModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/vortextexture.png");

    /** {@code RenderVortex(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderVortex(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new VortexModel(context.bakeLayer(VortexModel.LAYER), 0.25f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Vortex par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Vortex entity) {
        return RenderVortex.texture;
    }
}
