package com.swbr.orespawn.client.renderer.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.WhaleModel;
import com.swbr.orespawn.entity.aquatic.Whale;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderWhale} (RenderWhale.java:9-53). ClientProxyOreSpawn built it as
 * {@code new RenderWhale(new ModelWhale(), 0.1f, 1.0f)} (ClientProxyOreSpawn.java:114).
 *
 * <p>Shadow {@code par2 * par3} (:16); a calf is drawn at half scale (:34-40).
 */
public class RenderWhale extends MobRenderer<Whale, WhaleModel> {

    /** {@code Whaletexture.png} (:51), lower-cased by the asset generator. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/whaletexture.png");

    protected WhaleModel model;
    private float scale = 1.0f;

    /** {@code RenderWhale(model, par2, par3)} (:15-20). */
    public RenderWhale(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new WhaleModel(context.bakeLayer(WhaleModel.LAYER)), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Whale par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final Whale entity) {
        return texture;
    }
}
