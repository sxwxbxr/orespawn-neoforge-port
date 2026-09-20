package com.swbr.orespawn.client.renderer.terror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.RatModel;
import com.swbr.orespawn.entity.terror.Rat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRat} (RenderRat.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderRat(new ModelRat(1.0f), 0.1, 0.75)} (manifest): register with
 * {@code ctx -> new RenderRat(ctx, 0.1f, 0.75f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code Rattexture.png}, lower-cased).
 */
public class RenderRat extends MobRenderer<Rat, RatModel> {

    protected RatModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/rattexture.png");

    /** {@code RenderRat(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderRat(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new RatModel(context.bakeLayer(RatModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Rat par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Rat entity) {
        return RenderRat.texture;
    }
}
