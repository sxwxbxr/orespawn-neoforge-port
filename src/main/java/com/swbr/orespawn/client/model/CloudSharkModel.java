package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CloudSharkGeometry;
import com.swbr.orespawn.entity.sea.CloudShark;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCloudShark} (ModelCloudShark.java:7-97): body, head, jaw, top fin, back body,
 * tail fins and two side fins on a 64x64 texture, geometry {@link CloudSharkGeometry}. {@code wingspeed} 1.0 in
 * ClientProxyOreSpawn (:101). No GL calls; {@code setRotationAngles} only calls {@code super} (empty).
 */
public class CloudSharkModel extends EntityModel<CloudShark> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cloud_shark"), "main");

    private final float wingspeed;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart topfin;
    private final ModelPart bbody;
    private final ModelPart fins;
    private final ModelPart leftfin;
    private final ModelPart rightfin;

    /** {@code ModelCloudShark(float f1)} (:19-...). */
    public CloudSharkModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(CloudSharkGeometry.BODY);
        this.head = root.getChild(CloudSharkGeometry.HEAD);
        this.jaw = root.getChild(CloudSharkGeometry.JAW);
        this.topfin = root.getChild(CloudSharkGeometry.TOPFIN);
        this.bbody = root.getChild(CloudSharkGeometry.BBODY);
        this.fins = root.getChild(CloudSharkGeometry.FINS);
        this.leftfin = root.getChild(CloudSharkGeometry.LEFTFIN);
        this.rightfin = root.getChild(CloudSharkGeometry.RIGHTFIN);
    }

    /** The writes of {@code render()}: fins and jaw swing on {@code ageInTicks}. */
    @Override
    public void setupAnim(final CloudShark entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.leftfin.resetPose();
        this.rightfin.resetPose();
        this.fins.resetPose();
        this.jaw.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.15f;
        this.leftfin.yRot = 1.15f + newangle;
        newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.15f;
        this.rightfin.yRot = -0.9f + newangle;
        newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.25f;
        this.fins.yRot = newangle;
        newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.1f;
        this.jaw.xRot = 0.5f + newangle;
    }

    /** Draw order of {@code render()}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.bbody.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.fins.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightfin.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
