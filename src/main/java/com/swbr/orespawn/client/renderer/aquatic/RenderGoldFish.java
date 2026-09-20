package com.swbr.orespawn.client.renderer.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GoldFishModel;
import com.swbr.orespawn.entity.aquatic.GoldFish;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGoldFish} (RenderGoldFish.java:9-49). ClientProxyOreSpawn built it as
 * {@code new RenderGoldFish(new ModelGoldFish(0.7f), 0.2f, 1.0f)} (ClientProxyOreSpawn.java:100); the model is
 * baked here because 1.21.1 needs the context for it, so the registration is {@code (ctx, 0.2f, 1.0f, 0.7f)}.
 *
 * <p>Shadow {@code par2 * par3} (:16); {@code preRenderScale} (:34-40) is {@link #scale}.
 */
public class RenderGoldFish extends MobRenderer<GoldFish, GoldFishModel> {

    /** {@code GoldFish.png} (:47), lower-cased by the asset generator. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/goldfish.png");

    protected GoldFishModel model;
    private float scale = 1.0f;

    /** {@code RenderGoldFish(model, par2, par3)} (:15-20) with the model's {@code wingspeed}. */
    public RenderGoldFish(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new GoldFishModel(context.bakeLayer(GoldFishModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final GoldFish par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final GoldFish entity) {
        return texture;
    }
}
