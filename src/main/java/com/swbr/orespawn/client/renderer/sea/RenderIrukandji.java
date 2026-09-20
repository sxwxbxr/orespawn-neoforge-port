package com.swbr.orespawn.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.IrukandjiModel;
import com.swbr.orespawn.entity.sea.Irukandji;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderIrukandji} (RenderIrukandji.java:9-49), a {@code RenderLiving} with a
 * {@code ModelIrukandji}. ClientProxyOreSpawn (:115): {@code (new ModelIrukandji(1.0f), 0.1f, 0.25f)} - register as
 * {@code ctx -> new RenderIrukandji(ctx, 0.1f, 0.25f, 1.0f)}. Shadow {@code par2 * par3} (:15), scale {@code par3} in
 * {@code preRenderCallback} (:34-40), one texture (:42-48).
 */
public class RenderIrukandji extends MobRenderer<Irukandji, IrukandjiModel> {

    /** {@code Irukandjitexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/irukandjitexture.png");

    protected IrukandjiModel model;
    private float scale = 1.0f;

    /** {@code RenderIrukandji(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderIrukandji(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new IrukandjiModel(context.bakeLayer(IrukandjiModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Irukandji par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Irukandji entity) {
        return texture;
    }
}
