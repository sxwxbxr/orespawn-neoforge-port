package com.swbr.orespawn.client.renderer.cephadrome;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CephadromeModel;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCephadrome} (RenderCephadrome.java:9-49), a {@code RenderLiving} with a
 * {@code ModelCephadrome}. ClientProxyOreSpawn (manifest {@code renderer_args}): {@code (new ModelCephadrome(0.55f),
 * 1.25f, 1.0f)} - register as {@code ctx -> new RenderCephadrome(ctx, 1.25f, 1.0f, 0.55f)}. Shadow {@code par2 * par3}
 * (:16), scale {@code par3} (:34-36), one texture, no variants (:42-48).
 */
public class RenderCephadrome extends MobRenderer<Cephadrome, CephadromeModel> {

    /** {@code Cephadrome.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/cephadrome.png");

    protected CephadromeModel model;
    private float scale = 1.0f;

    /** {@code RenderCephadrome(model, par2, par3)} (:15-20) with the model's {@code wingspeed}. */
    public RenderCephadrome(final EntityRendererProvider.Context context, final float par2, final float par3,
                            final float wingspeed) {
        super(context, new CephadromeModel(context.bakeLayer(CephadromeModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Cephadrome par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Cephadrome entity) {
        return texture;
    }
}
