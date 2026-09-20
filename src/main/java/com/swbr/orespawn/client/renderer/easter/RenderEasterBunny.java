package com.swbr.orespawn.client.renderer.easter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.EasterBunnyModel;
import com.swbr.orespawn.entity.easter.EasterBunny;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderEasterBunny} (RenderEasterBunny.java:9-53). ClientProxyOreSpawn registered it as
 * {@code new RenderEasterBunny(new ModelEasterBunny(0.55f), 0.5f, 1.0f)} (ClientProxyOreSpawn.java:128): register with
 * {@code ctx -> new RenderEasterBunny(ctx, 0.5f, 1.0f)}; the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, halved for a baby ({@code preRenderScale}), one texture
 * ({@code EasterBunnytexture.png}, lower-cased by the asset generator).
 */
public class RenderEasterBunny extends MobRenderer<EasterBunny, EasterBunnyModel> {

    protected EasterBunnyModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/easterbunnytexture.png");

    /** {@code RenderEasterBunny(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderEasterBunny(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new EasterBunnyModel(context.bakeLayer(EasterBunnyModel.LAYER), 0.55f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:34-44): {@code glScalef(scale / 2)} for a child, else
     * {@code glScalef(scale)} - {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final EasterBunny par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final EasterBunny entity) {
        return RenderEasterBunny.texture;
    }
}
