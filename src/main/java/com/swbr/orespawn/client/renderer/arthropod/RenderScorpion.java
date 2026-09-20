package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ScorpionModel;
import com.swbr.orespawn.entity.arthropod.Scorpion;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderScorpion} (RenderScorpion.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderScorpion(new ModelScorpion(0.62f), 0.35f, 0.75f)} (manifest {@code renderer_args}, {@code model_args}):
 * register with {@code ctx -> new RenderScorpion(ctx, 0.35f, 0.75f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}, no child halving), one texture
 * ({@code Scorpion.png}, lower-cased by the asset generator).
 */
public class RenderScorpion extends MobRenderer<Scorpion, ScorpionModel> {

    protected ScorpionModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/scorpion.png");

    /** {@code RenderScorpion(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderScorpion(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new ScorpionModel(context.bakeLayer(ScorpionModel.LAYER), 0.62f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Scorpion par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Scorpion entity) {
        return RenderScorpion.texture;
    }
}
