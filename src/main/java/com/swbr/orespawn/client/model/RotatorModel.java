package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.RotatorGeometry;
import com.swbr.orespawn.entity.crystal.Rotator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelRotator} (ModelRotator.java:7-83): three boxes, 64x32, geometry from the generated
 * {@link RotatorGeometry}. {@code render()} (:36-72) draws each box eight times at {@code rotateAngleZ} steps of 45 degrees,
 * which makes three rings - black outside, red in the middle, yellow inside - and spins the rings by {@code rf1} degrees
 * about X (yellow), Y (red) and Z (black), each spin undone before the next ring.
 *
 * <p><b>Time binding (DECISIONS R18, "ModelRotator/ModelChainsaw an Systemzeit gebunden: ageInTicks plus Teiltick").</b>
 * The original advanced {@code rf1} by 2 degrees per rendered frame and wrapped it past 359 (:66-70), per entity in its
 * {@code RenderInfo}. PORT: the port derives the angle from {@code ageInTicks} (entity tick count plus partial tick) at
 * {@link ChainsawModel#FRAMES_PER_TICK} original frames per tick, the rate W03 chose for the other frame-bound model; each
 * rotator keeps its own phase through its own tick count, as each had its own note pad. Motion is continuous instead of
 * 2-degree steps.
 *
 * <p>The constructor argument {@code wingspeed} (0.25, manifest) is unused by {@code render()}, as in the original.
 */
public class RotatorModel extends EntityModel<Rotator> {

    /** Register with {@code RotatorGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "rotator"), "main");

    /** {@code rf1} step per original frame (:66). */
    private static final float DEGREES_PER_FRAME = 2.0f;
    /** 180 frames per turn: 0, 2, ..., 358, then back to 0 (:67-69). */
    private static final float FRAMES_PER_TURN = 180.0f;

    /** {@code ModelRotator(float f1)}: {@code wingspeed = f1} (:13-15); unused. */
    @SuppressWarnings("unused")
    private final float wingspeed;
    private final ModelPart Shape1;
    private final ModelPart Shape2;
    private final ModelPart Shape3;
    /** {@code ri.rf1} of this frame, in degrees. */
    private float rf1;

    public RotatorModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Shape1 = root.getChild(RotatorGeometry.SHAPE1);
        this.Shape2 = root.getChild(RotatorGeometry.SHAPE2);
        this.Shape3 = root.getChild(RotatorGeometry.SHAPE3);
    }

    /** The spin angle from {@code ageInTicks} ({@code f2}); see the class comment. */
    @Override
    public void setupAnim(final Rotator entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Shape1.resetPose();
        this.Shape2.resetPose();
        this.Shape3.resetPose();
        final float frames = f2 * ChainsawModel.FRAMES_PER_TICK;
        final float inTurn = frames - (float) Math.floor(frames / FRAMES_PER_TURN) * FRAMES_PER_TURN;
        this.rf1 = inTurn * DEGREES_PER_FRAME;
    }

    /** {@code render()} (:42-65): three rings, each spun about its own axis. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        float newangle = 0.0f;
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(this.rf1));
        for (int i = 0; i < 8; ++i) {
            this.Shape1.zRot = newangle;
            this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
            newangle += 0.7853982f;
        }
        poseStack.popPose();
        newangle = 0.0f;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(this.rf1));
        for (int i = 0; i < 8; ++i) {
            this.Shape2.zRot = newangle;
            this.Shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
            newangle += 0.7853982f;
        }
        poseStack.popPose();
        newangle = 0.0f;
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(this.rf1));
        for (int i = 0; i < 8; ++i) {
            this.Shape3.zRot = newangle;
            this.Shape3.render(poseStack, buffer, packedLight, packedOverlay, color);
            newangle += 0.7853982f;
        }
        poseStack.popPose();
    }
}
