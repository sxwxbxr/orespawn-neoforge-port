package com.swbr.orespawn.client.renderer.terror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.MantisModel;
import com.swbr.orespawn.entity.terror.Mantis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderMantis} (RenderMantis.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderMantis(new ModelMantis(2.0f), 0.9, 1.1)} (manifest): register with
 * {@code ctx -> new RenderMantis(ctx, 0.9f, 1.1f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code Mantistexture.png}, lower-cased).
 */
public class RenderMantis extends MobRenderer<Mantis, MantisModel> {

    protected MantisModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/mantistexture.png");

    /** {@code RenderMantis(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderMantis(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new MantisModel(context.bakeLayer(MantisModel.LAYER), 2.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Mantis par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Mantis entity) {
        return RenderMantis.texture;
    }
}
