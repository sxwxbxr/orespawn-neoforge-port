package com.swbr.orespawn.client.renderer.nightmare;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TriffidModel;
import com.swbr.orespawn.entity.triffid.Triffid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTriffid} (RenderTriffid.java:9-49), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:89: {@code new RenderTriffid(new ModelTriffid(1.0f), 0.3f, 1.0f)} - register as
 * {@code ctx -> new RenderTriffid(ctx, 0.3f, 1.0f)}; the model's argument is fixed here. Shadow {@code par2 * par3},
 * scale {@code par3}, one texture {@code Triffidtexture.png}.
 *
 * <p>Lives next to {@link RenderPitchBlack} in this package: the wave assigned both renderers of this porter to
 * {@code client.renderer.nightmare}.
 */
public class RenderTriffid extends MobRenderer<Triffid, TriffidModel> {

    /** {@code Triffidtexture.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/triffidtexture.png");

    protected TriffidModel model;
    private float scale = 1.0f;

    /** {@code RenderTriffid(model, par2, par3)} (:15-20). */
    public RenderTriffid(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new TriffidModel(context.bakeLayer(TriffidModel.LAYER), 1.0f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Triffid par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Triffid entity) {
        return texture;
    }
}
