package com.swbr.orespawn.client.item.bertha;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.item.BigWeaponRenderer;
import com.swbr.orespawn.client.model.HammyModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderHammy} (RenderHammy.java:9-74), the in-hand renderer of the
 * Attitude Adjuster ({@code hammysmall}). Differs from {@link RenderBertha} in three places: an
 * extra 70 degree yaw before the first-person rotations (:51), a single 180 degree X rotation in
 * third person (:63), and its own translations with scale 0.15 (:39, :43).
 */
public class RenderHammy extends BigWeaponRenderer<HammyModel> {

    /** {@code new ResourceLocation("orespawn", "AttitudeAdjustertexture.png")} (:72). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/attitudeadjustertexture.png");

    public RenderHammy() {
        super(TEXTURE);
    }

    @Override
    protected HammyModel bake(EntityModelSet models) {
        return new HammyModel(models.bakeLayer(HammyModel.LAYER)); // :15
    }

    /** {@code renderSword(-10, -13, -5, 0.15)} (:43, :49-59): first person. */
    @Override
    protected void renderSword(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(70.0f));  // :51
        poseStack.mulPose(Axis.XP.rotationDegrees(190.0f)); // :52
        poseStack.mulPose(Axis.ZP.rotationDegrees(25.0f));  // :53
        renderModel(poseStack, buffer, packedLight, packedOverlay, -10.0f, -13.0f, -5.0f, 0.15f); // :54-57
    }

    /** {@code renderSwordF5(6, -20, -4, 0.15)} (:39, :61-69): third person. */
    @Override
    protected void renderSwordF5(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f)); // :63
        renderModel(poseStack, buffer, packedLight, packedOverlay, 6.0f, -20.0f, -4.0f, 0.15f); // :64-67
    }
}
