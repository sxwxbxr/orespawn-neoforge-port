package com.swbr.orespawn.client.renderer.moth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CaterKillerModel;
import com.swbr.orespawn.entity.moth.CaterKiller;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCaterKiller} (RenderCaterKiller.java:9-53), a {@code RenderLiving}.
 * ClientProxyOreSpawn: {@code new RenderCaterKiller(new ModelCaterKiller(0.22f), 1.0f, 1.25f)} - register as
 * {@code ctx -> new RenderCaterKiller(ctx, 1.0f, 1.25f)}. Shadow {@code par2 * par3} = 1.25 (not halved with
 * {@code PlayNicely}, as in the original), scale 1.25, halved while DataWatcher 21 {@code PlayNicely} is set.
 */
public class RenderCaterKiller extends MobRenderer<CaterKiller, CaterKillerModel> {

    /** {@code CaterKillertexture.png} (:51), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/caterkillertexture.png");

    protected CaterKillerModel model;
    private float scale = 1.0f;

    /** {@code RenderCaterKiller(model, par2, par3)} (:15-20). */
    public RenderCaterKiller(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new CaterKillerModel(context.bakeLayer(CaterKillerModel.LAYER), 0.22f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-44): half size while {@code getPlayNicely() != 0} (R8). */
    @Override
    protected void scale(final CaterKiller par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.getPlayNicely() != 0) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final CaterKiller entity) {
        return texture;
    }
}
