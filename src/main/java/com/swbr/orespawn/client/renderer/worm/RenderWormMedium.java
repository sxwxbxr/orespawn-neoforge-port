package com.swbr.orespawn.client.renderer.worm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.WormMediumModel;
import com.swbr.orespawn.entity.worm.WormMedium;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderWormMedium} (RenderWormMedium.java:9-49). ClientProxyOreSpawn.java:97
 * registered {@code new RenderWormMedium(new ModelWormMedium(), 0.25f, 1.0f)}: register with
 * {@code ctx -> new RenderWormMedium(ctx, 0.25f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * ({@code WormMediumtexture.png}, lower-cased by the asset generator).
 */
public class RenderWormMedium extends MobRenderer<WormMedium, WormMediumModel> {

    protected WormMediumModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/wormmediumtexture.png");

    /** {@code RenderWormMedium(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderWormMedium(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new WormMediumModel(context.bakeLayer(WormMediumModel.LAYER)), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final WormMedium par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final WormMedium entity) {
        return RenderWormMedium.texture;
    }
}
