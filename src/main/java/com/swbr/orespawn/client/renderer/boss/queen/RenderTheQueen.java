package com.swbr.orespawn.client.renderer.boss.queen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TheQueenModel;
import com.swbr.orespawn.entity.boss.queen.TheQueen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTheQueen} (RenderTheQueen.java:9-59), a {@code RenderLiving} with a
 * {@code ModelTheQueen(0.65f)}. Manifest renderer args {@code (model, 1.9f, 2.0f)} - register as
 * {@code ctx -> new RenderTheQueen(ctx, 1.9f, 2.0f)}; the model's argument is fixed here (Leon precedent). Shadow
 * {@code par2 * par3} = 3.8 (:17), scale {@code par3}, a quarter of it while the watched PlayNicely is set (:35-41).
 *
 * <p>Texture (:47-53): {@code TheQueentexture2.png} while happy, {@code TheQueentexture.png} otherwise. The translucent
 * wing, cube and eye passes of the model run in {@link QueenPassesLayer} after the opaque body (R8).
 */
public class RenderTheQueen extends MobRenderer<TheQueen, TheQueenModel> {

    /** {@code TheQueentexture.png} (:56), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/thequeentexture.png");
    /** {@code TheQueentexture2.png} (:57). */
    private static final ResourceLocation texture2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/thequeentexture2.png");

    protected TheQueenModel model;
    private float scale = 1.0f;

    /** {@code RenderTheQueen(model, par2, par3)} (:16-21). */
    public RenderTheQueen(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new TheQueenModel(context.bakeLayer(TheQueenModel.LAYER), 0.65f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new QueenPassesLayer(this));
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:35-45). */
    @Override
    protected void scale(final TheQueen par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.getPlayNicely() != 0) {
            poseStack.scale(this.scale / 4.0f, this.scale / 4.0f, this.scale / 4.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:47-53). */
    @Override
    public ResourceLocation getTextureLocation(final TheQueen entity) {
        if (entity.isHappy()) {
            return texture2;
        }
        return texture;
    }

    /**
     * The GL blend block of {@code ModelTheQueen.render} (:1312-1342) as a layer: membranes and power cubes in the
     * translucent type, then the eyes opaque, both on the texture of the body.
     */
    static final class QueenPassesLayer extends RenderLayer<TheQueen, TheQueenModel> {

        QueenPassesLayer(final RenderLayerParent<TheQueen, TheQueenModel> parent) {
            super(parent);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final TheQueen entity, final float limbSwing, final float limbSwingAmount, final float partialTick,
                           final float ageInTicks, final float netHeadYaw, final float headPitch) {
            if (entity.isInvisible()) {
                return;
            }
            final ResourceLocation tex = this.getTextureLocation(entity);
            final int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0f);
            this.getParentModel().renderTranslucent(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(tex)),
                    packedLight, overlay);
            this.getParentModel().renderEyes(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex)), overlay);
        }
    }
}
