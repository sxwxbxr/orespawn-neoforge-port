package com.swbr.orespawn.client.item.bertha;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.item.BigWeaponRenderer;
import com.swbr.orespawn.client.model.SliceModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRoyal} (RenderRoyal.java:9-74), the in-hand renderer of the
 * Royal Guardian Sword ({@code royalsmall}). The original constructed a {@code ModelSlice} (:15)
 * and drew it with its own texture; rotations and translations as {@link RenderBertha}, scale
 * 0.35 (:39, :43).
 */
public class RenderRoyal extends BigWeaponRenderer<SliceModel> {

    /** {@code new ResourceLocation("orespawn", "Royaltexture.png")} (:72). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/royaltexture.png");

    public RenderRoyal() {
        super(TEXTURE);
    }

    @Override
    protected SliceModel bake(EntityModelSet models) {
        return new SliceModel(models.bakeLayer(SliceModel.LAYER)); // :15
    }

    /** {@code renderSword(6, 3, -5, 0.35)} (:43, :49-58): first person. */
    @Override
    protected void renderSword(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.XP.rotationDegrees(190.0f)); // :51
        poseStack.mulPose(Axis.ZP.rotationDegrees(25.0f));  // :52
        renderModel(poseStack, buffer, packedLight, packedOverlay, 6.0f, 3.0f, -5.0f, 0.35f); // :53-56
    }

    /** {@code renderSwordF5(-4, 2, -3, 0.35)} (:39, :60-69): third person. */
    @Override
    protected void renderSwordF5(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));  // :62
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f)); // :63
        renderModel(poseStack, buffer, packedLight, packedOverlay, -4.0f, 2.0f, -3.0f, 0.35f); // :64-67
    }
}
