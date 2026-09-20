package com.swbr.orespawn.client.renderer.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CoinModel;
import com.swbr.orespawn.entity.critter.Coin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCoin} (RenderCoin.java:9-49), a {@code RenderLiving} with a
 * {@code ModelCoin}. ClientProxyOreSpawn (:121): {@code (new ModelCoin(0.22f), 0.75f, 0.125f)} - register as
 * {@code ctx -> new RenderCoin(ctx, 0.75f, 0.125f, 0.22f)}. Shadow {@code par2 * par3} (:15), scale
 * {@code par3} (:34-40): the 256-pixel disc becomes two blocks wide. One texture (:42-48).
 */
public class RenderCoin extends MobRenderer<Coin, CoinModel> {

    /** {@code Cointexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/cointexture.png");

    protected CoinModel model;
    private float scale = 1.0f;

    /** {@code RenderCoin(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderCoin(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new CoinModel(context.bakeLayer(CoinModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Coin par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Coin entity) {
        return texture;
    }
}
