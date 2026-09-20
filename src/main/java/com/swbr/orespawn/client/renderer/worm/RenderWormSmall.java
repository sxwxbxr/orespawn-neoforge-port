package com.swbr.orespawn.client.renderer.worm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.WormSmallModel;
import com.swbr.orespawn.entity.worm.WormSmall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderWormSmall} (RenderWormSmall.java:9-49). ClientProxyOreSpawn.java:96 registered
 * {@code new RenderWormSmall(new ModelWormSmall(), 0.1f, 1.0f)}: register with
 * {@code ctx -> new RenderWormSmall(ctx, 0.1f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * ({@code WormSmalltexture.png}, lower-cased by the asset generator).
 */
public class RenderWormSmall extends MobRenderer<WormSmall, WormSmallModel> {

    protected WormSmallModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/wormsmalltexture.png");

    /** {@code RenderWormSmall(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderWormSmall(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new WormSmallModel(context.bakeLayer(WormSmallModel.LAYER)), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final WormSmall par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final WormSmall entity) {
        return RenderWormSmall.texture;
    }
}
