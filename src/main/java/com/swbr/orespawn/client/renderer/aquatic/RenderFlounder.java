package com.swbr.orespawn.client.renderer.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.FlounderModel;
import com.swbr.orespawn.entity.aquatic.Flounder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderFlounder} (RenderFlounder.java:9-53). ClientProxyOreSpawn built it as
 * {@code new RenderFlounder(new ModelFlounder(), 0.1f, 1.0f)} (ClientProxyOreSpawn.java:113).
 *
 * <p>Shadow {@code par2 * par3} (:16); a baby is drawn at half scale (:34-40).
 */
public class RenderFlounder extends MobRenderer<Flounder, FlounderModel> {

    /** {@code Floundertexture.png} (:51), lower-cased by the asset generator. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/floundertexture.png");

    protected FlounderModel model;
    private float scale = 1.0f;

    /** {@code RenderFlounder(model, par2, par3)} (:15-20). */
    public RenderFlounder(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new FlounderModel(context.bakeLayer(FlounderModel.LAYER)), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Flounder par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final Flounder entity) {
        return texture;
    }
}
