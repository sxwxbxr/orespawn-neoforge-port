package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.EmperorScorpionModel;
import com.swbr.orespawn.entity.arthropod.EmperorScorpion;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderEmperorScorpion} (RenderEmperorScorpion.java:9-49). ClientProxyOreSpawn
 * registered it as {@code new RenderEmperorScorpion(new ModelEmperorScorpion(0.22f), 0.95f, 1.5f)} (manifest): register
 * with {@code ctx -> new RenderEmperorScorpion(ctx, 0.95f, 1.5f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code emperorscorpion.png}).
 */
public class RenderEmperorScorpion extends MobRenderer<EmperorScorpion, EmperorScorpionModel> {

    protected EmperorScorpionModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/emperorscorpion.png");

    /** {@code RenderEmperorScorpion(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderEmperorScorpion(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new EmperorScorpionModel(context.bakeLayer(EmperorScorpionModel.LAYER), 0.22f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final EmperorScorpion par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final EmperorScorpion entity) {
        return RenderEmperorScorpion.texture;
    }
}
