package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SpiderRobotGeometry;
import com.swbr.orespawn.entity.spiderrobot.RenderSpiderRobotInfo;
import com.swbr.orespawn.entity.spiderrobot.SpiderRobot;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelSpiderRobot} (ModelSpiderRobot.java:7-426): the robot spider - head with two
 * spikes and three-part mandibles, body, huge abdomen, tail, eight hip knobs, and <b>one</b> leg template (hip joint,
 * three 100-pixel segments, knees, foot, spikes) that {@code render()} poses and draws eight times from the entity's
 * {@link RenderSpiderRobotInfo}. Geometry (:48-253) is the generated {@link SpiderRobotGeometry}, texture 256 x 512.
 *
 * <p>PORT: a single {@code setupAnim} pose cannot express the eight re-posed draws of one part set, so
 * {@link #setupAnim} resets the pose (R8) and keeps its inputs, and {@link #renderToBuffer} runs the write-and-draw
 * sequence of {@code render()} in the original order (GiantRobotModel precedent, W07). {@code setRotationAngles}
 * (:422-424) only called the empty {@code ModelBase} version. {@code wingspeed} (:9, :45-46) is never read.
 */
public class SpiderRobotModel extends EntityModel<SpiderRobot> {

    /** Register with {@code SpiderRobotGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot_spider"), "main");

    private final ModelPart root;
    @SuppressWarnings("unused")
    private final float wingspeed;
    private final ModelPart Leg1p1;
    private final ModelPart Leg1p2;
    private final ModelPart Leg1p3;
    private final ModelPart Foot;
    private final ModelPart FootSpike1;
    private final ModelPart FootSpike2;
    private final ModelPart FootSpike3;
    private final ModelPart FootSpike4;
    private final ModelPart AnkleSpike1;
    private final ModelPart AnkleSpike2;
    private final ModelPart AnkleSpike3;
    private final ModelPart AnkleSpike4;
    private final ModelPart LowerKnee;
    private final ModelPart UpperKnee;
    private final ModelPart LegBump1;
    private final ModelPart LegBump2;
    private final ModelPart LowerKnee2;
    private final ModelPart UpperKnee2;
    private final ModelPart HipJoint;
    private final ModelPart BodyCenter;
    private final ModelPart Abdomen;
    private final ModelPart Head;
    private final ModelPart Ljaw1;
    private final ModelPart Rjaw1;
    private final ModelPart Ljaw2;
    private final ModelPart Rjaw2;
    private final ModelPart Ljaw3;
    private final ModelPart Rjaw3;
    private final ModelPart Tail;
    private final ModelPart HeadSpike1;
    private final ModelPart HeadSpike2;
    private final ModelPart Hip1;
    private final ModelPart Hip2;
    private final ModelPart Hip3;
    private final ModelPart Hip4;
    private final ModelPart Hip5;
    private final ModelPart Hip6;
    private final ModelPart Hip7;
    private final ModelPart Hip8;

    /** Inputs of the last {@link #setupAnim}, read by {@link #renderToBuffer}. */
    @Nullable
    private RenderSpiderRobotInfo r;
    private int attacking;

    /** {@code ModelSpiderRobot(float f1)} (:43-253). */
    public SpiderRobotModel(final ModelPart root, final float f1) {
        // A 1.7.10 Render that bypasses RendererLivingEntity drew with the world's GL state: culling on, as for
        // RenderElevator (W04). The renderer's scale(-1, -1, 1) keeps the winding order.
        super(RenderType::entityCutout);
        this.root = root;
        this.wingspeed = f1;
        this.Leg1p1 = root.getChild(SpiderRobotGeometry.LEG1P1);
        this.Leg1p2 = root.getChild(SpiderRobotGeometry.LEG1P2);
        this.Leg1p3 = root.getChild(SpiderRobotGeometry.LEG1P3);
        this.Foot = root.getChild(SpiderRobotGeometry.FOOT);
        this.FootSpike1 = root.getChild(SpiderRobotGeometry.FOOT_SPIKE1);
        this.FootSpike2 = root.getChild(SpiderRobotGeometry.FOOT_SPIKE2);
        this.FootSpike3 = root.getChild(SpiderRobotGeometry.FOOT_SPIKE3);
        this.FootSpike4 = root.getChild(SpiderRobotGeometry.FOOT_SPIKE4);
        this.AnkleSpike1 = root.getChild(SpiderRobotGeometry.ANKLE_SPIKE1);
        this.AnkleSpike2 = root.getChild(SpiderRobotGeometry.ANKLE_SPIKE2);
        this.AnkleSpike3 = root.getChild(SpiderRobotGeometry.ANKLE_SPIKE3);
        this.AnkleSpike4 = root.getChild(SpiderRobotGeometry.ANKLE_SPIKE4);
        this.LowerKnee = root.getChild(SpiderRobotGeometry.LOWER_KNEE);
        this.UpperKnee = root.getChild(SpiderRobotGeometry.UPPER_KNEE);
        this.LegBump1 = root.getChild(SpiderRobotGeometry.LEG_BUMP1);
        this.LegBump2 = root.getChild(SpiderRobotGeometry.LEG_BUMP2);
        this.LowerKnee2 = root.getChild(SpiderRobotGeometry.LOWER_KNEE2);
        this.UpperKnee2 = root.getChild(SpiderRobotGeometry.UPPER_KNEE2);
        this.HipJoint = root.getChild(SpiderRobotGeometry.HIP_JOINT);
        this.BodyCenter = root.getChild(SpiderRobotGeometry.BODY_CENTER);
        this.Abdomen = root.getChild(SpiderRobotGeometry.ABDOMEN);
        this.Head = root.getChild(SpiderRobotGeometry.HEAD);
        this.Ljaw1 = root.getChild(SpiderRobotGeometry.LJAW1);
        this.Rjaw1 = root.getChild(SpiderRobotGeometry.RJAW1);
        this.Ljaw2 = root.getChild(SpiderRobotGeometry.LJAW2);
        this.Rjaw2 = root.getChild(SpiderRobotGeometry.RJAW2);
        this.Ljaw3 = root.getChild(SpiderRobotGeometry.LJAW3);
        this.Rjaw3 = root.getChild(SpiderRobotGeometry.RJAW3);
        this.Tail = root.getChild(SpiderRobotGeometry.TAIL);
        this.HeadSpike1 = root.getChild(SpiderRobotGeometry.HEAD_SPIKE1);
        this.HeadSpike2 = root.getChild(SpiderRobotGeometry.HEAD_SPIKE2);
        this.Hip1 = root.getChild(SpiderRobotGeometry.HIP1);
        this.Hip2 = root.getChild(SpiderRobotGeometry.HIP2);
        this.Hip3 = root.getChild(SpiderRobotGeometry.HIP3);
        this.Hip4 = root.getChild(SpiderRobotGeometry.HIP4);
        this.Hip5 = root.getChild(SpiderRobotGeometry.HIP5);
        this.Hip6 = root.getChild(SpiderRobotGeometry.HIP6);
        this.Hip7 = root.getChild(SpiderRobotGeometry.HIP7);
        this.Hip8 = root.getChild(SpiderRobotGeometry.HIP8);
    }

    /**
     * {@code render()} before the first draw (:255-259): {@code r = e.getRenderSpiderRobotInfo()}; the attacking flag is
     * read for the jaws. The renderer passes {@code (0, 0, -0.1, 0, 0)} (RenderSpiderRobot.java:28), none of which the
     * model reads.
     */
    @Override
    public void setupAnim(final SpiderRobot e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.r = e.getRenderSpiderRobotInfo();
        this.attacking = e.getAttacking();
    }

    /**
     * {@code render()} (:255-420) at scale {@code f5 = 0.0625}: eight legs, each posed from {@code ydisplayangle},
     * {@code p1xangle..p3xangle + uddisplayangle}, the hip from {@code ymid}/{@code legoff}/{@code yoff} and the segment
     * ends chained by 99 pixels, drawn right after; then the jaws (closed or snapping with {@code gpcounter}) and the
     * body parts.
     */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final RenderSpiderRobotInfo r = this.r;
        if (r == null) {
            return;
        }
        for (int i = 0; i < 8; ++i) {
            final float rotateAngleY = r.ydisplayangle[i];
            this.Leg1p3.yRot = rotateAngleY;
            this.Leg1p2.yRot = rotateAngleY;
            this.Leg1p1.yRot = rotateAngleY;
            this.Foot.yRot = r.ydisplayangle[i];
            this.FootSpike1.yRot = r.ydisplayangle[i];
            this.FootSpike2.yRot = r.ydisplayangle[i];
            this.FootSpike3.yRot = r.ydisplayangle[i];
            this.FootSpike4.yRot = r.ydisplayangle[i];
            this.AnkleSpike1.yRot = r.ydisplayangle[i];
            this.AnkleSpike2.yRot = r.ydisplayangle[i];
            this.AnkleSpike3.yRot = r.ydisplayangle[i];
            this.AnkleSpike4.yRot = r.ydisplayangle[i];
            this.LowerKnee.yRot = r.ydisplayangle[i];
            this.UpperKnee.yRot = r.ydisplayangle[i];
            this.LegBump1.yRot = r.ydisplayangle[i];
            this.LegBump2.yRot = r.ydisplayangle[i];
            this.LowerKnee2.yRot = r.ydisplayangle[i];
            this.UpperKnee2.yRot = r.ydisplayangle[i];
            this.HipJoint.yRot = r.ydisplayangle[i];
            this.Leg1p1.xRot = (float) r.p1xangle[i] + r.uddisplayangle[i];
            this.UpperKnee2.xRot = this.Leg1p1.xRot;
            this.HipJoint.xRot = this.Leg1p1.xRot;
            this.Leg1p2.xRot = (float) r.p2xangle[i] + r.uddisplayangle[i];
            this.UpperKnee.xRot = this.Leg1p2.xRot;
            this.LowerKnee2.xRot = this.Leg1p2.xRot;
            this.Leg1p3.xRot = (float) r.p3xangle[i] + r.uddisplayangle[i];
            this.Foot.xRot = this.Leg1p3.xRot;
            this.FootSpike1.xRot = this.Leg1p3.xRot;
            this.FootSpike2.xRot = this.Leg1p3.xRot;
            this.FootSpike3.xRot = this.Leg1p3.xRot;
            this.FootSpike4.xRot = this.Leg1p3.xRot;
            this.AnkleSpike1.xRot = this.Leg1p3.xRot;
            this.AnkleSpike2.xRot = this.Leg1p3.xRot;
            this.AnkleSpike3.xRot = this.Leg1p3.xRot;
            this.AnkleSpike4.xRot = this.Leg1p3.xRot;
            this.LegBump1.xRot = this.Leg1p3.xRot;
            this.LegBump2.xRot = this.Leg1p3.xRot;
            this.LowerKnee.xRot = this.Leg1p3.xRot;
            this.Leg1p1.x = -(float) Math.cos(r.ymid[i]) * r.legoff[i] * 16.0f;
            this.Leg1p1.z = (float) Math.sin(r.ymid[i]) * r.legoff[i] * 16.0f;
            this.Leg1p1.y = r.yoff[i] * -16.0f;
            this.UpperKnee2.x = this.Leg1p1.x;
            this.UpperKnee2.y = this.Leg1p1.y;
            this.UpperKnee2.z = this.Leg1p1.z;
            this.HipJoint.x = this.Leg1p1.x;
            this.HipJoint.y = this.Leg1p1.y;
            this.HipJoint.z = this.Leg1p1.z;
            this.Leg1p2.y = this.Leg1p1.y - (float) Math.sin(this.Leg1p1.xRot) * 99.0f;
            this.Leg1p2.z = this.Leg1p1.z + (float) Math.cos(this.Leg1p1.xRot) * (float) Math.cos(this.Leg1p1.yRot) * 99.0f;
            this.Leg1p2.x = this.Leg1p1.x + (float) Math.cos(this.Leg1p1.xRot) * (float) Math.sin(this.Leg1p1.yRot) * 99.0f;
            this.UpperKnee.x = this.Leg1p2.x;
            this.UpperKnee.y = this.Leg1p2.y;
            this.UpperKnee.z = this.Leg1p2.z;
            this.LowerKnee2.x = this.Leg1p2.x;
            this.LowerKnee2.y = this.Leg1p2.y;
            this.LowerKnee2.z = this.Leg1p2.z;
            this.Leg1p3.y = this.Leg1p2.y - (float) Math.sin(this.Leg1p2.xRot) * 99.0f;
            this.Leg1p3.z = this.Leg1p2.z + (float) Math.cos(this.Leg1p2.xRot) * (float) Math.cos(this.Leg1p2.yRot) * 99.0f;
            this.Leg1p3.x = this.Leg1p2.x + (float) Math.cos(this.Leg1p2.xRot) * (float) Math.sin(this.Leg1p2.yRot) * 99.0f;
            this.copyPivot(this.Leg1p3, this.Foot);
            this.copyPivot(this.Leg1p3, this.FootSpike1);
            this.copyPivot(this.Leg1p3, this.FootSpike2);
            this.copyPivot(this.Leg1p3, this.FootSpike3);
            this.copyPivot(this.Leg1p3, this.FootSpike4);
            this.copyPivot(this.Leg1p3, this.AnkleSpike1);
            this.copyPivot(this.Leg1p3, this.AnkleSpike2);
            this.copyPivot(this.Leg1p3, this.AnkleSpike3);
            this.copyPivot(this.Leg1p3, this.AnkleSpike4);
            this.copyPivot(this.Leg1p3, this.LegBump1);
            this.copyPivot(this.Leg1p3, this.LegBump2);
            this.copyPivot(this.Leg1p3, this.LowerKnee);
            this.Leg1p1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Leg1p2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Leg1p3.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.FootSpike1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.FootSpike2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.FootSpike3.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.FootSpike4.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.AnkleSpike1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.AnkleSpike2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.AnkleSpike3.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.AnkleSpike4.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.LowerKnee.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.UpperKnee.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.LegBump1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.LegBump2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.LowerKnee2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.UpperKnee2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.HipJoint.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        if (this.attacking == 0) {
            this.Ljaw1.yRot = 0.0f;
            this.Ljaw2.yRot = 0.75f;
            this.Ljaw3.yRot = 1.71f;
            this.Rjaw1.yRot = 0.0f;
            this.Rjaw2.yRot = 2.3f;
            this.Rjaw3.yRot = 1.41f;
        } else {
            final float newangle = Mth.cos(r.gpcounter * 0.25f) * 3.1415927f * 0.22f;
            this.Ljaw1.yRot = newangle;
            this.Ljaw2.yRot = newangle + 0.75f;
            this.Ljaw3.yRot = newangle + 1.71f;
            this.Rjaw1.yRot = -newangle;
            this.Rjaw2.yRot = 2.3f - newangle;
            this.Rjaw3.yRot = 1.41f - newangle;
        }
        this.BodyCenter.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Abdomen.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Ljaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rjaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Ljaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rjaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Ljaw3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rjaw3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.HeadSpike1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.HeadSpike2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip8.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    /** {@code to.rotationPointX/Y/Z = from.rotationPointX/Y/Z} (:321-356). */
    private void copyPivot(final ModelPart from, final ModelPart to) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
    }
}
