package com.swbr.orespawn.client.renderer.boss.princeadult;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.model.ThePrinceAdultModel;
import com.swbr.orespawn.entity.boss.princeadult.ThePrinceAdult;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/**
 * The blend block of {@code ModelThePrinceAdult.render()} (ModelThePrinceAdult.java:1228-1244): {@code glEnable(GL_BLEND)},
 * {@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)}, {@code glColor4f(0.75, 0.75, 0.75, 0.55)}, then the ten wing
 * membranes. R8: a second pass with {@link RenderType#entityTranslucent} and the ARGB colour
 * {@link ThePrinceAdultModel#MEMBRANE_COLOR}.
 *
 * <p>PORT: a layer runs after the body inside the same pose ({@code LivingEntityRenderer.render}), with the angles
 * {@code setupAnim} has just written, which is where the original drew the block. An invisible prince draws no
 * membranes, as its body is not drawn either; 1.7.10 did not render the model of an invisible living entity at all.
 */
public class ThePrinceAdultWingLayer extends RenderLayer<ThePrinceAdult, ThePrinceAdultModel> {

    public ThePrinceAdultWingLayer(final RenderLayerParent<ThePrinceAdult, ThePrinceAdultModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                       final ThePrinceAdult livingEntity, final float limbSwing, final float limbSwingAmount,
                       final float partialTick, final float ageInTicks, final float netHeadYaw, final float headPitch) {
        if (livingEntity.isInvisible()) {
            return;
        }
        this.getParentModel().renderTranslucentToBuffer(poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(livingEntity))),
                packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0f), ThePrinceAdultModel.MEMBRANE_COLOR);
    }
}
