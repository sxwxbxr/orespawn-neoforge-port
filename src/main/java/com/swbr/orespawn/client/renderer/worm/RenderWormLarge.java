package com.swbr.orespawn.client.renderer.worm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.WormLargeModel;
import com.swbr.orespawn.entity.worm.WormLarge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderWormLarge} (RenderWormLarge.java:9-49). ClientProxyOreSpawn.java:98 registered
 * {@code new RenderWormLarge(new ModelWormLarge(), 0.9f, 1.0f)}: register with
 * {@code ctx -> new RenderWormLarge(ctx, 0.9f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * ({@code WormLargetexture.png}, lower-cased by the asset generator).
 */
public class RenderWormLarge extends MobRenderer<WormLarge, WormLargeModel> {

    protected WormLargeModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/wormlargetexture.png");

    /** {@code RenderWormLarge(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderWormLarge(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new WormLargeModel(context.bakeLayer(WormLargeModel.LAYER)), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final WormLarge par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final WormLarge entity) {
        return RenderWormLarge.texture;
    }
}
