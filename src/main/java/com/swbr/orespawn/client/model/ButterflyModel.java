package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ButterflyGeometry;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelButterfly} (ModelButterfly.java:7-110): a one-pixel body and head
 * with four wing panels per side, 64x32 texture, shared by butterfly, moth and Mothra (W08). The geometry
 * (:21-76) is the generated {@link ButterflyGeometry}.
 *
 * <p>{@code wingspeed} is the constructor argument (:25): 1.0 for the butterfly, 0.75 for the moth
 * (ClientProxyOreSpawn, manifest {@code model_args}).
 *
 * <p>{@code render()} (:78-99) draws head and body, then writes the flap angle and draws the eight wings;
 * head and body carry no rotation, so moving the angle writes to {@link #setupAnim} changes nothing (R8).
 * Culling off and alpha test on, as 1.7.10 {@code RendererLivingEntity} drew: {@code entityCutoutNoCull}.
 */
public class ButterflyModel<T extends EntityButterfly> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "butterfly"), "main");

    private final ModelPart body;
    private final ModelPart leftwing;
    private final ModelPart rightwing;
    private final ModelPart leftwing2;
    private final ModelPart rightwing2;
    private final ModelPart leftwing3;
    private final ModelPart rightwing3;
    private final ModelPart head;
    private final ModelPart leftwing4;
    private final ModelPart rightwing4;
    private final float wingspeed;

    /** {@code ModelButterfly(float f1)} (:21-76). */
    public ButterflyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(ButterflyGeometry.BODY);
        this.leftwing = root.getChild(ButterflyGeometry.LEFTWING);
        this.rightwing = root.getChild(ButterflyGeometry.RIGHTWING);
        this.leftwing2 = root.getChild(ButterflyGeometry.LEFTWING2);
        this.rightwing2 = root.getChild(ButterflyGeometry.RIGHTWING2);
        this.leftwing3 = root.getChild(ButterflyGeometry.LEFTWING3);
        this.rightwing3 = root.getChild(ButterflyGeometry.RIGHTWING3);
        this.head = root.getChild(ButterflyGeometry.HEAD);
        this.leftwing4 = root.getChild(ButterflyGeometry.LEFTWING4);
        this.rightwing4 = root.getChild(ButterflyGeometry.RIGHTWING4);
    }

    /** The angle writes of {@code render()} (:83-90); {@code f2} is {@code ageInTicks}. */
    @Override
    public void setupAnim(final T entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.rightwing.resetPose();
        this.rightwing2.resetPose();
        this.rightwing3.resetPose();
        this.rightwing4.resetPose();
        this.leftwing.resetPose();
        this.leftwing2.resetPose();
        this.leftwing3.resetPose();
        this.leftwing4.resetPose();
        this.rightwing.zRot = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f;
        this.rightwing2.zRot = this.rightwing.zRot;
        this.rightwing3.zRot = this.rightwing.zRot;
        this.rightwing4.zRot = this.rightwing.zRot;
        this.leftwing.zRot = -this.rightwing.zRot;
        this.leftwing2.zRot = -this.rightwing.zRot;
        this.leftwing3.zRot = -this.rightwing.zRot;
        this.leftwing4.zRot = -this.rightwing.zRot;
    }

    /** Draw order of {@code render()} (:81-98). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing4.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
