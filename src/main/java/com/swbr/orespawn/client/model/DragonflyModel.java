package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.DragonflyGeometry;
import com.swbr.orespawn.entity.critter.Dragonfly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelDragonfly} (ModelDragonfly.java:7-223): head, thorax, jaws, two-part
 * abdomen, four wings, legs and feelers, geometry {@link DragonflyGeometry} (64x64). {@code wingspeed} 2.0
 * in ClientProxyOreSpawn. No GL calls.
 */
public class DragonflyModel extends EntityModel<Dragonfly> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "dragonfly"), "main");

    private final float wingspeed;
    private final ModelPart Shape1;
    private final ModelPart lfwing;
    private final ModelPart Shape3;
    private final ModelPart Shape4;
    private final ModelPart Shape5;
    private final ModelPart rjaw;
    private final ModelPart ljaw;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart Shape10;
    private final ModelPart Shape11;
    private final ModelPart Shape12;
    private final ModelPart Shape13;
    private final ModelPart Shape14;
    private final ModelPart Shape15;
    private final ModelPart Shape16;
    private final ModelPart Shape17;
    private final ModelPart Shape18;
    private final ModelPart Shape19;
    private final ModelPart Shape20;
    private final ModelPart Shape21;
    private final ModelPart Shape22;
    private final ModelPart Shape23;
    private final ModelPart lrwing;
    private final ModelPart rfwing;
    private final ModelPart rrwing;

    /** {@code ModelDragonfly(float f1)} (:36-172). */
    public DragonflyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Shape1 = root.getChild(DragonflyGeometry.SHAPE1);
        this.lfwing = root.getChild(DragonflyGeometry.LFWING);
        this.Shape3 = root.getChild(DragonflyGeometry.SHAPE3);
        this.Shape4 = root.getChild(DragonflyGeometry.SHAPE4);
        this.Shape5 = root.getChild(DragonflyGeometry.SHAPE5);
        this.rjaw = root.getChild(DragonflyGeometry.RJAW);
        this.ljaw = root.getChild(DragonflyGeometry.LJAW);
        this.tail1 = root.getChild(DragonflyGeometry.TAIL1);
        this.tail2 = root.getChild(DragonflyGeometry.TAIL2);
        this.Shape10 = root.getChild(DragonflyGeometry.SHAPE10);
        this.Shape11 = root.getChild(DragonflyGeometry.SHAPE11);
        this.Shape12 = root.getChild(DragonflyGeometry.SHAPE12);
        this.Shape13 = root.getChild(DragonflyGeometry.SHAPE13);
        this.Shape14 = root.getChild(DragonflyGeometry.SHAPE14);
        this.Shape15 = root.getChild(DragonflyGeometry.SHAPE15);
        this.Shape16 = root.getChild(DragonflyGeometry.SHAPE16);
        this.Shape17 = root.getChild(DragonflyGeometry.SHAPE17);
        this.Shape18 = root.getChild(DragonflyGeometry.SHAPE18);
        this.Shape19 = root.getChild(DragonflyGeometry.SHAPE19);
        this.Shape20 = root.getChild(DragonflyGeometry.SHAPE20);
        this.Shape21 = root.getChild(DragonflyGeometry.SHAPE21);
        this.Shape22 = root.getChild(DragonflyGeometry.SHAPE22);
        this.Shape23 = root.getChild(DragonflyGeometry.SHAPE23);
        this.lrwing = root.getChild(DragonflyGeometry.LRWING);
        this.rfwing = root.getChild(DragonflyGeometry.RFWING);
        this.rrwing = root.getChild(DragonflyGeometry.RRWING);
    }

    /**
     * The writes of {@code render()} (:178-185): wings beat about Z (the hind wings drawn turned by 3.14),
     * the jaws open and close about X. Wings keep their rest {@code yRot}, jaws their rest {@code yRot};
     * {@code resetPose} restores them first (R8).
     */
    @Override
    public void setupAnim(final Dragonfly entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lfwing.resetPose();
        this.rfwing.resetPose();
        this.lrwing.resetPose();
        this.rrwing.resetPose();
        this.ljaw.resetPose();
        this.rjaw.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f;
        this.lfwing.zRot = newangle;
        this.rfwing.zRot = -newangle;
        this.lrwing.zRot = newangle + 3.14f;
        this.rrwing.zRot = -newangle + 3.14f;
        newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.ljaw.xRot = newangle;
        this.rjaw.xRot = -newangle;
    }

    /** Draw order of {@code render()} (:186-211). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rjaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ljaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape10.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape11.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape12.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape13.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape14.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape15.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape16.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape17.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape18.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape19.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape20.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape21.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape22.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape23.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrwing.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
