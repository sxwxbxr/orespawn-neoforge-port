package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GiantRobotGeometry;
import com.swbr.orespawn.entity.robot.GiantRobot;
import com.swbr.orespawn.entity.robot.RenderGiantRobotInfo;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelGiantRobot} (ModelGiantRobot.java:6-422): Jeffery - a hip bar with two long
 * legs, a tapering back with shoulders, neck and head, and two long arms with fists. Geometry (:29-126) is the
 * generated {@link GiantRobotGeometry}; the model has one set of leg and arm parts that {@code render()} poses and
 * draws twice.
 *
 * <p>{@code wingspeed} is the constructor argument, 0.25 for Jeffery (manifest {@code model_args}). The leg angles
 * go into the entity's {@link RenderGiantRobotInfo}, as in the original.
 *
 * <p>PORT: {@code render()} draws Thigh, Shin, the feet and the arm parts once for each side, re-posing them in
 * between. A single {@code setupAnim} pose cannot express that, so {@code setupAnim} does the writes that precede
 * the first draw ({@code renderdata}, the hip) and keeps its inputs, and {@link #renderToBuffer} runs the rest of
 * the write-and-draw sequence in the original order. {@code resetPose()} at the start of {@code setupAnim} (R8).
 */
public class GiantRobotModel extends EntityModel<GiantRobot> {

    /** Register with {@code GiantRobotGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "giant_robot"), "main");

    private final ModelPart root;
    private final float wingspeed;
    private float hipy;
    private final ModelPart Hip;
    private final ModelPart Thigh;
    private final ModelPart Shin;
    private final ModelPart Foot1;
    private final ModelPart Foot2;
    private final ModelPart Foot3;
    private final ModelPart Thigh2;
    private final ModelPart Thigh3;
    private final ModelPart Back1;
    private final ModelPart Back2;
    private final ModelPart Back3;
    private final ModelPart Shoulders;
    private final ModelPart Neck;
    private final ModelPart Head;
    private final ModelPart Arm1;
    private final ModelPart Arm2;
    private final ModelPart Arm3;
    private final ModelPart Knuckles;

    /** Inputs of the last {@link #setupAnim}, read by {@link #renderToBuffer}. */
    @Nullable
    private RenderGiantRobotInfo r;
    private int attacking;
    private float f2;
    private float f3;
    private float f4;

    /** {@code ModelGiantRobot(float f1)} (:29-126). */
    public GiantRobotModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.hipy = 0.0f;
        this.wingspeed = f1;
        this.Hip = root.getChild(GiantRobotGeometry.HIP);
        this.Thigh = root.getChild(GiantRobotGeometry.THIGH);
        this.Shin = root.getChild(GiantRobotGeometry.SHIN);
        this.Foot1 = root.getChild(GiantRobotGeometry.FOOT1);
        this.Foot2 = root.getChild(GiantRobotGeometry.FOOT2);
        this.Foot3 = root.getChild(GiantRobotGeometry.FOOT3);
        this.Thigh2 = root.getChild(GiantRobotGeometry.THIGH2);
        this.Thigh3 = root.getChild(GiantRobotGeometry.THIGH3);
        this.Back1 = root.getChild(GiantRobotGeometry.BACK1);
        this.Back2 = root.getChild(GiantRobotGeometry.BACK2);
        this.Back3 = root.getChild(GiantRobotGeometry.BACK3);
        this.Shoulders = root.getChild(GiantRobotGeometry.SHOULDERS);
        this.Neck = root.getChild(GiantRobotGeometry.NECK);
        this.Head = root.getChild(GiantRobotGeometry.HEAD);
        this.Arm1 = root.getChild(GiantRobotGeometry.ARM1);
        this.Arm2 = root.getChild(GiantRobotGeometry.ARM2);
        this.Arm3 = root.getChild(GiantRobotGeometry.ARM3);
        this.Knuckles = root.getChild(GiantRobotGeometry.KNUCKLES);
        // :125 hipy = Hip.rotationPointY
        this.hipy = this.Hip.y;
    }

    /**
     * {@code render()} up to the first draw (:128-147): the leg angles into {@code renderdata}, the hip bob and the
     * hip turn. {@code f1} limb swing amount, {@code f2} age in ticks, {@code f3} head yaw minus body yaw, {@code f4}
     * pitch, both in degrees.
     */
    @Override
    public void setupAnim(final GiantRobot e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        final RenderGiantRobotInfo r = e.getRenderGiantRobotInfo();
        float movescale = f1 * 0.65f;
        if (movescale > 1.0f) {
            movescale = 1.0f;
        }
        r.hipxdisplayangle = (float) (Math.cos(-f2 * this.wingspeed) * 3.141592653589793 * 0.10000000149011612 * movescale);
        r.hipydisplayangle = (float) (Math.sin(-f2 * this.wingspeed) * 3.141592653589793 * 0.10000000149011612 * movescale);
        r.thighdisplayangle[0] = (float) (Math.cos(-f2 * this.wingspeed + 1.5707963267948966) * 3.141592653589793 * 0.15000000596046448 * movescale)
                - (float) (0.19634954084936207 * movescale);
        r.thighdisplayangle[1] = (float) (Math.cos(-f2 * this.wingspeed + 3.141592653589793 + 1.5707963267948966) * 3.141592653589793 * 0.15000000596046448 * movescale)
                - (float) (0.19634954084936207 * movescale);
        r.shindisplayangle[0] = (float) ((float) (Math.cos(-f2 * this.wingspeed + 3.141592653589793) * 3.141592653589793 * 0.20000000298023224 * movescale)
                + 0.6283185400806344 * movescale);
        r.shindisplayangle[1] = (float) ((float) (Math.cos(-f2 * this.wingspeed) * 3.141592653589793 * 0.20000000298023224 * movescale)
                + 0.6283185400806344 * movescale);
        final float newangle = (float) (Math.cos(-f2 * this.wingspeed * 2.0f) * movescale);
        this.Hip.y = this.hipy + newangle * 4.0f;
        this.Hip.xRot = r.hipxdisplayangle;
        this.Hip.yRot = (float) (r.hipydisplayangle + 1.5707963267948966);
        this.r = r;
        this.attacking = e.getAttacking();
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
    }

    /** {@code render()} from the first draw on (:148-410), writes and draws interleaved as in the original. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final RenderGiantRobotInfo r = this.r;
        if (r == null) {
            return;
        }
        final float f2 = this.f2;
        this.Hip.render(poseStack, buffer, packedLight, packedOverlay, color);

        // First leg (:149-215).
        final float rotateAngleX = r.thighdisplayangle[0];
        this.Thigh3.xRot = rotateAngleX;
        this.Thigh2.xRot = rotateAngleX;
        this.Thigh.xRot = rotateAngleX;
        final float rotationPointY = this.Hip.y - (float) Math.sin(this.Hip.xRot) * 13.0f;
        this.Thigh3.y = rotationPointY;
        this.Thigh2.y = rotationPointY;
        this.Thigh.y = rotationPointY;
        final float rotationPointZ = this.Hip.z + (float) Math.cos(this.Hip.xRot) * (float) Math.cos(this.Hip.yRot) * 13.0f;
        this.Thigh3.z = rotationPointZ;
        this.Thigh2.z = rotationPointZ;
        this.Thigh.z = rotationPointZ;
        final float rotationPointX = this.Hip.x + (float) Math.cos(this.Hip.xRot) * (float) Math.sin(this.Hip.yRot) * 13.0f;
        this.Thigh3.x = rotationPointX;
        this.Thigh2.x = rotationPointX;
        this.Thigh.x = rotationPointX;
        this.Thigh.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Thigh2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Thigh3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shin.xRot = r.shindisplayangle[0];
        this.Shin.y = this.Thigh.y + (float) Math.cos(this.Thigh.xRot) * 40.0f;
        this.Shin.z = this.Thigh.z + (float) Math.sin(this.Thigh.xRot) * 40.0f;
        this.Shin.x = this.Thigh.x;
        this.Shin.render(poseStack, buffer, packedLight, packedOverlay, color);
        final float rotateAngleX2 = r.shindisplayangle[0];
        this.Foot3.xRot = rotateAngleX2;
        this.Foot2.xRot = rotateAngleX2;
        this.Foot1.xRot = rotateAngleX2;
        this.Foot3.y = this.Shin.y;
        this.Foot2.y = this.Shin.y;
        this.Foot1.y = this.Shin.y;
        this.Foot3.z = this.Shin.z;
        this.Foot2.z = this.Shin.z;
        this.Foot1.z = this.Shin.z;
        this.Foot3.x = this.Shin.x;
        this.Foot2.x = this.Shin.x;
        this.Foot1.x = this.Shin.x;
        this.Foot1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Foot2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Foot3.render(poseStack, buffer, packedLight, packedOverlay, color);

        // Second leg (:216-282).
        final float rotateAngleX3 = r.thighdisplayangle[1];
        this.Thigh3.xRot = rotateAngleX3;
        this.Thigh2.xRot = rotateAngleX3;
        this.Thigh.xRot = rotateAngleX3;
        final float rotationPointY3 = this.Hip.y + (float) Math.sin(this.Hip.xRot) * 13.0f;
        this.Thigh3.y = rotationPointY3;
        this.Thigh2.y = rotationPointY3;
        this.Thigh.y = rotationPointY3;
        final float rotationPointZ3 = this.Hip.z - (float) Math.cos(this.Hip.xRot) * (float) Math.cos(this.Hip.yRot) * 13.0f;
        this.Thigh3.z = rotationPointZ3;
        this.Thigh2.z = rotationPointZ3;
        this.Thigh.z = rotationPointZ3;
        final float rotationPointX3 = this.Hip.x - (float) Math.cos(this.Hip.xRot) * (float) Math.sin(this.Hip.yRot) * 13.0f;
        this.Thigh3.x = rotationPointX3;
        this.Thigh2.x = rotationPointX3;
        this.Thigh.x = rotationPointX3;
        this.Thigh.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Thigh2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Thigh3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shin.xRot = r.shindisplayangle[1];
        this.Shin.y = this.Thigh.y + (float) Math.cos(this.Thigh.xRot) * 40.0f;
        this.Shin.z = this.Thigh.z + (float) Math.sin(this.Thigh.xRot) * 40.0f;
        this.Shin.x = this.Thigh.x;
        this.Shin.render(poseStack, buffer, packedLight, packedOverlay, color);
        final float rotateAngleX4 = r.shindisplayangle[1];
        this.Foot3.xRot = rotateAngleX4;
        this.Foot2.xRot = rotateAngleX4;
        this.Foot1.xRot = rotateAngleX4;
        this.Foot3.y = this.Shin.y;
        this.Foot2.y = this.Shin.y;
        this.Foot1.y = this.Shin.y;
        this.Foot3.z = this.Shin.z;
        this.Foot2.z = this.Shin.z;
        this.Foot1.z = this.Shin.z;
        this.Foot3.x = this.Shin.x;
        this.Foot2.x = this.Shin.x;
        this.Foot1.x = this.Shin.x;
        this.Foot1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Foot2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Foot3.render(poseStack, buffer, packedLight, packedOverlay, color);

        // Arms (:283-388): walking they swing with the opposite thigh, attacking they punch.
        float shoulderangle = -r.hipydisplayangle;
        float a1angle;
        float a2angle = a1angle = r.thighdisplayangle[1];
        float b1angle;
        float b2angle = b1angle = r.thighdisplayangle[0];
        if (this.attacking != 0) {
            shoulderangle = (float) (-(Math.sin(f2 * this.wingspeed * 2.0f) * 3.141592653589793 * 0.20000000298023224));
            a1angle = (float) ((float) (Math.sin(f2 * this.wingspeed * 2.0f) * 3.141592653589793 / 5.0) - 0.7853981633974483);
            a2angle = (float) (-a1angle + 3.141592653589793);
            a1angle += (float) 0.6283185307179586;
            a2angle += (float) 0.6283185307179586;
            b1angle = (float) ((float) (-(Math.sin(f2 * this.wingspeed * 2.0f) * 3.141592653589793 / 5.0)) - 0.7853981633974483);
            b2angle = (float) (-b1angle + 3.141592653589793);
            b1angle += (float) 0.6283185307179586;
            b2angle += (float) 0.6283185307179586;
        }
        this.Back3.yRot = shoulderangle / 2.0f;
        this.Shoulders.yRot = shoulderangle;
        final float n = this.Hip.y - 60.0f;
        this.Arm2.y = n;
        this.Arm1.y = n;
        final float n2 = this.Hip.x + 26.0f;
        this.Arm2.x = n2;
        this.Arm1.x = n2;
        final float n3 = this.Shoulders.z - (float) Math.sin(this.Shoulders.yRot) * 26.0f;
        this.Arm2.z = n3;
        this.Arm1.z = n3;
        this.Arm2.xRot = a1angle;
        this.Arm1.xRot = a1angle;
        this.Arm1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Arm2.render(poseStack, buffer, packedLight, packedOverlay, color);
        final float n5 = (float) (a2angle - 0.19634954084936207);
        this.Knuckles.xRot = n5;
        this.Arm3.xRot = n5;
        final float n6 = this.Arm1.y + (float) Math.cos(this.Arm1.xRot) * 41.0f;
        this.Knuckles.y = n6;
        this.Arm3.y = n6;
        final float n7 = this.Arm1.z + (float) Math.sin(this.Arm1.xRot) * 41.0f;
        this.Knuckles.z = n7;
        this.Arm3.z = n7;
        this.Knuckles.x = this.Arm1.x;
        this.Arm3.x = this.Arm1.x;
        this.Arm3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Knuckles.render(poseStack, buffer, packedLight, packedOverlay, color);
        final float n8 = this.Hip.y - 60.0f;
        this.Arm2.y = n8;
        this.Arm1.y = n8;
        final float n9 = this.Hip.x - 26.0f;
        this.Arm2.x = n9;
        this.Arm1.x = n9;
        final float n10 = this.Shoulders.z + (float) Math.sin(this.Shoulders.yRot) * 26.0f;
        this.Arm2.z = n10;
        this.Arm1.z = n10;
        this.Arm2.xRot = b1angle;
        this.Arm1.xRot = b1angle;
        this.Arm1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Arm2.render(poseStack, buffer, packedLight, packedOverlay, color);
        final float n12 = (float) (b2angle - 0.19634954084936207);
        this.Knuckles.xRot = n12;
        this.Arm3.xRot = n12;
        final float n13 = this.Arm1.y + (float) Math.cos(this.Arm1.xRot) * 41.0f;
        this.Knuckles.y = n13;
        this.Arm3.y = n13;
        final float n14 = this.Arm1.z + (float) Math.sin(this.Arm1.xRot) * 41.0f;
        this.Knuckles.z = n14;
        this.Arm3.z = n14;
        this.Knuckles.x = this.Arm1.x;
        this.Arm3.x = this.Arm1.x;
        this.Arm3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Knuckles.render(poseStack, buffer, packedLight, packedOverlay, color);

        // Body and head (:389-410).
        final float rotationPointY5 = this.Hip.y;
        this.Back3.y = rotationPointY5;
        this.Back2.y = rotationPointY5;
        this.Back1.y = rotationPointY5;
        final float rotationPointY6 = this.Hip.y;
        this.Head.y = rotationPointY6;
        this.Neck.y = rotationPointY6;
        this.Shoulders.y = rotationPointY6;
        this.Head.yRot = (float) Math.toRadians(this.f3);
        this.Head.xRot = (float) Math.toRadians(this.f4) / 3.0f;
        this.Back1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Back2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Back3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shoulders.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
