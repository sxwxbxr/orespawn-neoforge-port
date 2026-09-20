package com.swbr.orespawn.client.renderer.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TRexModel;
import com.swbr.orespawn.entity.dino.TRex;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTRex} (RenderTRex.java:10-49). ClientProxyOreSpawn.java:47 registered
 * {@code new RenderTRex(new ModelTRex(0.2f), 1.0f, 1.2f)}: register with {@code ctx -> new RenderTRex(ctx, 1.0f, 1.2f)};
 * the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, {@code glScalef(scale)}, one texture ({@code TRextexture.png}, lower-cased by the asset
 * generator).
 */
public class RenderTRex extends MobRenderer<TRex, TRexModel> {

    protected TRexModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/trextexture.png");

    /** {@code RenderTRex(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderTRex(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new TRexModel(context.bakeLayer(TRexModel.LAYER), 0.2f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale, scale, scale)} (R8). */
    @Override
    protected void scale(final TRex par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final TRex entity) {
        return RenderTRex.texture;
    }
}
