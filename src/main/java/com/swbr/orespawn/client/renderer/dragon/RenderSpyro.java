package com.swbr.orespawn.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.SpyroModel;
import com.swbr.orespawn.entity.dragon.Spyro;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderSpyro} (RenderSpyro.java:9-49), a {@code RenderLiving} with a
 * {@code ModelSpyro}. ClientProxyOreSpawn registered {@code new RenderSpyro(new ModelSpyro(0.65f), 0.65f, 0.75f)}
 * (manifest {@code renderer_args}, {@code model_args}): register with {@code ctx -> new RenderSpyro(ctx, 0.65f, 0.75f)};
 * the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}, no baby halving), one texture
 * ({@code spyrotexture.png}).
 */
public class RenderSpyro extends MobRenderer<Spyro, SpyroModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/spyrotexture.png");

    protected SpyroModel model;
    private float scale;

    /** {@code RenderSpyro(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderSpyro(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new SpyroModel(context.bakeLayer(SpyroModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)};
     * {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final Spyro par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Spyro entity) {
        return RenderSpyro.texture;
    }
}
