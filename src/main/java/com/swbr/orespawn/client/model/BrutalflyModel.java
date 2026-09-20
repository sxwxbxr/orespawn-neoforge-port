package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BrutalflyGeometry;
import com.swbr.orespawn.entity.moth.Brutalfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelBrutalfly} (ModelBrutalfly.java:7-142): a one-pixel body and head with six wing
 * panels per side, 64x32 texture, geometry {@link BrutalflyGeometry}. {@code wingspeed} is the constructor argument
 * (:25), 0.2 for the Brutalfly (ClientProxyOreSpawn, manifest {@code model_args}).
 *
 * <p>{@code render()} (:102-131) draws head and body, then writes the flap angle to all twelve wing parts and draws them.
 * Head and body carry no rotation, so moving the writes to {@link #setupAnim} changes nothing (R8). No GL calls; the
 * overlay pass lives in {@code RenderBrutalfly}. Culling off and alpha test on, as 1.7.10 {@code RendererLivingEntity}
 * drew: {@code entityCutoutNoCull}.
 */
public class BrutalflyModel extends EntityModel<Brutalfly> {

    /** Register with {@code BrutalflyGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "brutalfly"), "main");

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
    private final ModelPart leftwing5;
    private final ModelPart leftwing6;
    private final ModelPart rightwing5;
    private final ModelPart rightwing6;
    private final float wingspeed;

    /** {@code ModelBrutalfly(float f1)} (:23-100). */
    public BrutalflyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(BrutalflyGeometry.BODY);
        this.leftwing = root.getChild(BrutalflyGeometry.LEFTWING);
        this.rightwing = root.getChild(BrutalflyGeometry.RIGHTWING);
        this.leftwing2 = root.getChild(BrutalflyGeometry.LEFTWING2);
        this.rightwing2 = root.getChild(BrutalflyGeometry.RIGHTWING2);
        this.leftwing3 = root.getChild(BrutalflyGeometry.LEFTWING3);
        this.rightwing3 = root.getChild(BrutalflyGeometry.RIGHTWING3);
        this.head = root.getChild(BrutalflyGeometry.HEAD);
        this.leftwing4 = root.getChild(BrutalflyGeometry.LEFTWING4);
        this.rightwing4 = root.getChild(BrutalflyGeometry.RIGHTWING4);
        this.leftwing5 = root.getChild(BrutalflyGeometry.LEFTWING5);
        this.leftwing6 = root.getChild(BrutalflyGeometry.LEFTWING6);
        this.rightwing5 = root.getChild(BrutalflyGeometry.RIGHTWING5);
        this.rightwing6 = root.getChild(BrutalflyGeometry.RIGHTWING6);
    }

    /** The angle writes of {@code render()} (:107-118); {@code f2} is {@code ageInTicks}. */
    @Override
    public void setupAnim(final Brutalfly entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.rightwing.resetPose();
        this.rightwing2.resetPose();
        this.rightwing3.resetPose();
        this.rightwing4.resetPose();
        this.rightwing5.resetPose();
        this.rightwing6.resetPose();
        this.leftwing.resetPose();
        this.leftwing2.resetPose();
        this.leftwing3.resetPose();
        this.leftwing4.resetPose();
        this.leftwing5.resetPose();
        this.leftwing6.resetPose();
        this.rightwing.zRot = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f;
        this.rightwing2.zRot = this.rightwing.zRot;
        this.rightwing3.zRot = this.rightwing.zRot;
        this.rightwing4.zRot = this.rightwing.zRot;
        this.rightwing5.zRot = this.rightwing.zRot;
        this.rightwing6.zRot = this.rightwing.zRot;
        this.leftwing.zRot = -this.rightwing.zRot;
        this.leftwing2.zRot = -this.rightwing.zRot;
        this.leftwing3.zRot = -this.rightwing.zRot;
        this.leftwing4.zRot = -this.rightwing.zRot;
        this.leftwing5.zRot = -this.rightwing.zRot;
        this.leftwing6.zRot = -this.rightwing.zRot;
    }

    /** Draw order of {@code render()} (:105-130). */
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
        this.leftwing5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing6.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
