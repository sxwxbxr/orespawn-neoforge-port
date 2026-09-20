package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.EnderReaperGeometry;
import com.swbr.orespawn.entity.ender.EnderReaper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelEnderReaper} (ModelEnderReaper.java:7-548): the winged reaper with scythe,
 * geometry {@link EnderReaperGeometry} (512x512, 66 parts). {@code wingspeed} is 0.23 in ClientProxyOreSpawn
 * (manifest {@code model_args}).
 *
 * <p>{@code render()} (:414-537) writes the scythe, the left forearm, the six wing parts and the head, then draws
 * every part once in creation order without GL state. All written fields are written in both branches every frame;
 * {@code resetPose()} first changes nothing (R8).
 */
public class EnderReaperModel extends EntityModel<EnderReaper> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ender_reaper"), "main");

    private final float wingspeed;
    /** Every part in the draw order of {@code render()} (:471-536), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart rwing1;
    private final ModelPart lwing1;
    private final ModelPart larm1;
    private final ModelPart scythe1;
    private final ModelPart scythe2;
    private final ModelPart scythe3;
    private final ModelPart head;
    private final ModelPart lwing3;
    private final ModelPart lwing2;
    private final ModelPart rwing3;
    private final ModelPart rwing2;

    /** {@code ModelEnderReaper(float f1)} (:77-412). */
    public EnderReaperModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[EnderReaperGeometry.PARTS.length];
        for (int i = 0; i < EnderReaperGeometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(EnderReaperGeometry.PARTS[i]);
        }
        for (final String hidden : EnderReaperGeometry.HIDDEN) {
            root.getChild(hidden).visible = false;
        }
        this.rwing1 = root.getChild(EnderReaperGeometry.RWING1);
        this.lwing1 = root.getChild(EnderReaperGeometry.LWING1);
        this.larm1 = root.getChild(EnderReaperGeometry.LARM1);
        this.scythe1 = root.getChild(EnderReaperGeometry.SCYTHE1);
        this.scythe2 = root.getChild(EnderReaperGeometry.SCYTHE2);
        this.scythe3 = root.getChild(EnderReaperGeometry.SCYTHE3);
        this.head = root.getChild(EnderReaperGeometry.HEAD);
        this.lwing3 = root.getChild(EnderReaperGeometry.LWING3);
        this.lwing2 = root.getChild(EnderReaperGeometry.LWING2);
        this.rwing3 = root.getChild(EnderReaperGeometry.RWING3);
        this.rwing2 = root.getChild(EnderReaperGeometry.RWING2);
    }

    /** The writes of {@code render()} (:418-470), in the original order. */
    @Override
    public void setupAnim(final EnderReaper e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        final float rotateAngleZ = 1.0f - Math.abs(newangle);
        this.scythe1.zRot = rotateAngleZ;
        this.scythe2.zRot = rotateAngleZ;
        this.scythe3.zRot = rotateAngleZ;
        if (e.isScreaming()) {
            newangle = Mth.cos(f2 * 1.9f * this.wingspeed) * 3.1415927f * 0.25f;
            final float rotateAngleZ2 = 1.0f + newangle;
            this.scythe1.zRot = rotateAngleZ2;
            this.scythe2.zRot = rotateAngleZ2;
            this.scythe3.zRot = rotateAngleZ2;
            this.larm1.xRot = -0.436f;
            this.larm1.yRot = -0.488f;
            newangle = Mth.cos(f2 * 2.7f * this.wingspeed) * 3.1415927f * 0.3f;
        } else {
            this.larm1.xRot = -2.436f;
            this.larm1.yRot = 1.0f;
            newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.06f;
        }
        final float rotateAngleY = 0.785f + newangle;
        this.lwing3.yRot = rotateAngleY;
        this.lwing2.yRot = rotateAngleY;
        this.lwing1.yRot = rotateAngleY;
        final float rotateAngleY2 = -0.785f - newangle;
        this.rwing3.yRot = rotateAngleY2;
        this.rwing2.yRot = rotateAngleY2;
        this.rwing1.yRot = rotateAngleY2;
        this.head.yRot = (float) Math.toRadians(f3) * 0.45f;
        if (this.head.yRot > 0.45f) {
            this.head.yRot = 0.45f;
        }
        if (this.head.yRot < -0.45f) {
            this.head.yRot = -0.45f;
        }
    }

    /** The draws of {@code render()} (:471-536). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
