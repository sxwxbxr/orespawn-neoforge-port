package com.swbr.orespawn.client.renderer.boss.princeadult;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ThePrinceAdultModel;
import com.swbr.orespawn.entity.boss.princeadult.ThePrinceAdult;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderThePrinceAdult} (RenderThePrinceAdult.java:9-49), a {@code RenderLiving} with a
 * {@code ModelThePrinceAdult}. ClientProxyOreSpawn: {@code new RenderThePrinceAdult(new ModelThePrinceAdult(0.65f), 1.2f,
 * 1.0f)} (manifest {@code renderer_args}/{@code model_args}) - register as {@code ctx -> new RenderThePrinceAdult(ctx,
 * 1.2f, 1.0f)}; the model's argument is fixed here (Leon precedent). Shadow {@code par2 * par3} (:16), scale
 * {@code par3} (:34-40), one texture {@code TheKingtexture.png} (:42-48).
 *
 * <p>The translucent wing membranes of the model's blend block are drawn by {@link ThePrinceAdultWingLayer}, which runs
 * after the opaque body with the same pose, as the second half of the original {@code render()} did.
 */
public class RenderThePrinceAdult extends MobRenderer<ThePrinceAdult, ThePrinceAdultModel> {

    /** {@code TheKingtexture.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py (manifest texture_map). */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/thekingtexture.png");

    protected ThePrinceAdultModel model;
    private float scale = 1.0f;

    /** {@code RenderThePrinceAdult(model, par2, par3)} (:15-20). */
    public RenderThePrinceAdult(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new ThePrinceAdultModel(context.bakeLayer(ThePrinceAdultModel.LAYER), 0.65f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new ThePrinceAdultWingLayer(this));
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final ThePrinceAdult par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final ThePrinceAdult entity) {
        return texture;
    }
}
