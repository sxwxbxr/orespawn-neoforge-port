package com.swbr.orespawn.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.BandPModel;
import com.swbr.orespawn.entity.monster.BandP;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderBandP} (RenderBandP.java:9-49), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:134: {@code new RenderBandP(new ModelBandP(0.4f), 1.0f, 1.0f)} - register as
 * {@code ctx -> new RenderBandP(ctx, 1.0f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * {@code BandPtexture.png}; the variant {@code what} picks no texture.
 */
public class RenderBandP extends MobRenderer<BandP, BandPModel> {

    /** {@code BandPtexture.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/bandptexture.png");

    protected BandPModel model;
    private float scale = 1.0f;

    /** {@code RenderBandP(model, par2, par3)} (:15-20). */
    public RenderBandP(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new BandPModel(context.bakeLayer(BandPModel.LAYER), 0.4f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final BandP par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final BandP entity) {
        return texture;
    }
}
