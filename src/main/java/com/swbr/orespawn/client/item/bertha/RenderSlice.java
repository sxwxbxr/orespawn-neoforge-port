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
 * Port of {@code danger.orespawn.RenderSlice} (RenderSlice.java:9-74), the in-hand renderer of
 * Slice ({@code slicesmall}). Same rotations and translations as {@link RenderBertha}, the fan
 * model and scale 0.3 (:39, :43).
 */
public class RenderSlice extends BigWeaponRenderer<SliceModel> {

    /** {@code new ResourceLocation("orespawn", "Slicetexture.png")} (:72). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/slicetexture.png");

    public RenderSlice() {
        super(TEXTURE);
    }

    @Override
    protected SliceModel bake(EntityModelSet models) {
        return new SliceModel(models.bakeLayer(SliceModel.LAYER)); // :15
    }

    /** {@code renderSword(6, 3, -5, 0.3)} (:43, :49-58): first person. */
    @Override
    protected void renderSword(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.XP.rotationDegrees(190.0f)); // :51
        poseStack.mulPose(Axis.ZP.rotationDegrees(25.0f));  // :52
        renderModel(poseStack, buffer, packedLight, packedOverlay, 6.0f, 3.0f, -5.0f, 0.3f); // :53-56
    }

    /** {@code renderSwordF5(-4, 2, -3, 0.3)} (:39, :60-69): third person. */
    @Override
    protected void renderSwordF5(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));  // :62
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f)); // :63
        renderModel(poseStack, buffer, packedLight, packedOverlay, -4.0f, 2.0f, -3.0f, 0.3f); // :64-67
    }
}
