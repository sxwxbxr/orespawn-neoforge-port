package com.swbr.orespawn.client.renderer.waterdragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GammaMetroidModel;
import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGammaMetroid} (RenderGammaMetroid.java:9-53). ClientProxyOreSpawn registered it
 * as {@code new RenderGammaMetroid(new ModelGammaMetroid(0.45f), 0.75f, 0.9f)} (manifest {@code renderer_args},
 * {@code model_args}): register with {@code ctx -> new RenderGammaMetroid(ctx, 0.75f, 0.9f)}; the model's constructor
 * argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, halved for a baby ({@code preRenderScale}), one texture
 * ({@code GammaMetroid.png}, lower-cased by the asset generator). The translucency is the model's (R8).
 */
public class RenderGammaMetroid extends MobRenderer<GammaMetroid, GammaMetroidModel> {

    protected GammaMetroidModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/gammametroid.png");

    /** {@code RenderGammaMetroid(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderGammaMetroid(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new GammaMetroidModel(context.bakeLayer(GammaMetroidModel.LAYER), 0.45f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:34-44): {@code glScalef(scale / 2)} for a child, else
     * {@code glScalef(scale)} (R8).
     */
    @Override
    protected void scale(final GammaMetroid par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final GammaMetroid entity) {
        return RenderGammaMetroid.texture;
    }
}
