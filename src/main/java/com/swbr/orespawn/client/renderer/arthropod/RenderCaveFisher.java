package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CaveFisherModel;
import com.swbr.orespawn.entity.arthropod.CaveFisher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCaveFisher} (RenderCaveFisher.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderCaveFisher(new ModelCaveFisher(0.62f), 0.35f, 0.75f)} (manifest): register with
 * {@code ctx -> new RenderCaveFisher(ctx, 0.35f, 0.75f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code CaveFisher.png}, lower-cased).
 */
public class RenderCaveFisher extends MobRenderer<CaveFisher, CaveFisherModel> {

    protected CaveFisherModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/cavefisher.png");

    /** {@code RenderCaveFisher(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderCaveFisher(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new CaveFisherModel(context.bakeLayer(CaveFisherModel.LAYER), 0.62f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final CaveFisher par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final CaveFisher entity) {
        return RenderCaveFisher.texture;
    }
}
