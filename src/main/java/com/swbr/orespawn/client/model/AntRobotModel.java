package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.AntRobotGeometry;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import com.swbr.orespawn.entity.spiderrobot.RenderSpiderRobotInfo;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelAntRobot} (ModelAntRobot.java:7-301): the Robot Red Ant - body, abdomen, head
 * with jaws and antennae, two jets, six hip blocks, and one leg (three segments plus seven foot parts) that
 * {@code render()} poses and draws six times from the entity's leg IK. Geometry (:38-178) is the generated
 * {@link AntRobotGeometry}.
 *
 * <p>{@code wingspeed} is the constructor argument (1.0 for the Red Ant, manifest {@code model_args}) and is read by
 * nothing, as in the original.
 *
 * <p>PORT: the leg parts are re-posed and drawn once per leg, which one {@code setupAnim} pose cannot express.
 * {@code setupAnim} keeps its inputs (the leg state and the attacking flag) and {@link #renderToBuffer} runs the
 * write-and-draw sequence in the original order (GiantRobotModel precedent). {@code resetPose()} at the start of
 * {@code setupAnim} (R8); every field the original reads is written before its first draw in the same frame.
 */
public class AntRobotModel extends EntityModel<AntRobot> {

    /** Register with {@code AntRobotGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot_red_ant"), "main");

    private final ModelPart root;
    @SuppressWarnings("unused")
    private final float wingspeed;
    private final ModelPart Leg1;
    private final ModelPart Leg2;
    private final ModelPart Leg3;
    private final ModelPart Foot1;
    private final ModelPart Foot2;
    private final ModelPart Foot3;
    private final ModelPart Foot4;
    private final ModelPart Foot5;
    private final ModelPart Foot6;
    private final ModelPart Foot7;
    private final ModelPart Body;
    private final ModelPart Abdomen;
    private final ModelPart Head;
    private final ModelPart Jet1;
    private final ModelPart Jet2;
    private final ModelPart Hip1;
    private final ModelPart Hip2;
    private final ModelPart LJaw1;
    private final ModelPart RJaw1;
    private final ModelPart LJaw2;
    private final ModelPart RJaw2;
    private final ModelPart LAntenna;
    private final ModelPart RAntenna;
    private final ModelPart Hip3;
    private final ModelPart Hip4;
    private final ModelPart Hip5;
    private final ModelPart Hip6;

    /** Inputs of the last {@link #setupAnim}, read by {@link #renderToBuffer}. */
    @Nullable
    private RenderSpiderRobotInfo r;
    private int attacking;

    /** {@code ModelAntRobot(float f1)} (:38-178). */
    public AntRobotModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.Leg1 = root.getChild(AntRobotGeometry.LEG1);
        this.Leg2 = root.getChild(AntRobotGeometry.LEG2);
        this.Leg3 = root.getChild(AntRobotGeometry.LEG3);
        this.Foot1 = root.getChild(AntRobotGeometry.FOOT1);
        this.Foot2 = root.getChild(AntRobotGeometry.FOOT2);
        this.Foot3 = root.getChild(AntRobotGeometry.FOOT3);
        this.Foot4 = root.getChild(AntRobotGeometry.FOOT4);
        this.Foot5 = root.getChild(AntRobotGeometry.FOOT5);
        this.Foot6 = root.getChild(AntRobotGeometry.FOOT6);
        this.Foot7 = root.getChild(AntRobotGeometry.FOOT7);
        this.Body = root.getChild(AntRobotGeometry.BODY);
        this.Abdomen = root.getChild(AntRobotGeometry.ABDOMEN);
        this.Head = root.getChild(AntRobotGeometry.HEAD);
        this.Jet1 = root.getChild(AntRobotGeometry.JET1);
        this.Jet2 = root.getChild(AntRobotGeometry.JET2);
        this.Hip1 = root.getChild(AntRobotGeometry.HIP1);
        this.Hip2 = root.getChild(AntRobotGeometry.HIP2);
        this.LJaw1 = root.getChild(AntRobotGeometry.LJAW1);
        this.RJaw1 = root.getChild(AntRobotGeometry.RJAW1);
        this.LJaw2 = root.getChild(AntRobotGeometry.LJAW2);
        this.RJaw2 = root.getChild(AntRobotGeometry.RJAW2);
        this.LAntenna = root.getChild(AntRobotGeometry.LANTENNA);
        this.RAntenna = root.getChild(AntRobotGeometry.RANTENNA);
        this.Hip3 = root.getChild(AntRobotGeometry.HIP3);
        this.Hip4 = root.getChild(AntRobotGeometry.HIP4);
        this.Hip5 = root.getChild(AntRobotGeometry.HIP5);
        this.Hip6 = root.getChild(AntRobotGeometry.HIP6);
    }

    /**
     * {@code render()} up to the first draw (:180-185): {@code super.render} and {@code setRotationAngles} do
     * nothing; the leg state is fetched from the entity. RenderAntRobot passes the original arguments
     * {@code (0, 0, -0.1, 0, 0)}, none of which is read.
     */
    @Override
    public void setupAnim(final AntRobot e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.r = e.getRenderSpiderRobotInfo();
        this.attacking = e.getAttacking();
    }

    /** {@code render()} from the leg loop on (:186-289), writes and draws interleaved as in the original. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final RenderSpiderRobotInfo r = this.r;
        if (r == null) {
            return;
        }
        for (int i = 0; i < 6; ++i) {
            final float rotateAngleY = r.ydisplayangle[i];
            this.Leg3.yRot = rotateAngleY;
            this.Leg2.yRot = rotateAngleY;
            this.Leg1.yRot = rotateAngleY;
            this.Foot1.yRot = r.ydisplayangle[i];
            this.Foot2.yRot = r.ydisplayangle[i];
            this.Foot3.yRot = r.ydisplayangle[i];
            this.Foot4.yRot = r.ydisplayangle[i];
            this.Foot5.yRot = r.ydisplayangle[i];
            this.Foot6.yRot = r.ydisplayangle[i];
            this.Foot7.yRot = r.ydisplayangle[i];
            this.Leg1.xRot = (float) r.p1xangle[i] + r.uddisplayangle[i];
            this.Leg2.xRot = (float) r.p2xangle[i] + r.uddisplayangle[i];
            this.Leg3.xRot = (float) r.p3xangle[i] + r.uddisplayangle[i];
            this.Foot1.xRot = this.Leg3.xRot;
            this.Foot2.xRot = this.Leg3.xRot;
            this.Foot3.xRot = this.Leg3.xRot;
            this.Foot4.xRot = this.Leg3.xRot;
            this.Foot5.xRot = this.Leg3.xRot;
            this.Foot6.xRot = this.Leg3.xRot;
            this.Foot7.xRot = this.Leg3.xRot;
            this.Leg1.x = -(float) Math.cos(r.ymid[i]) * r.legoff[i] * 16.0f;
            this.Leg1.z = (float) Math.sin(r.ymid[i]) * r.legoff[i] * 16.0f;
            this.Leg1.y = r.yoff[i] * -16.0f;
            this.Leg2.y = this.Leg1.y - (float) Math.sin(this.Leg1.xRot) * 49.0f;
            this.Leg2.z = this.Leg1.z + (float) Math.cos(this.Leg1.xRot) * (float) Math.cos(this.Leg1.yRot) * 49.0f;
            this.Leg2.x = this.Leg1.x + (float) Math.cos(this.Leg1.xRot) * (float) Math.sin(this.Leg1.yRot) * 49.0f;
            this.Leg3.y = this.Leg2.y - (float) Math.sin(this.Leg2.xRot) * 49.0f;
            this.Leg3.z = this.Leg2.z + (float) Math.cos(this.Leg2.xRot) * (float) Math.cos(this.Leg2.yRot) * 49.0f;
            this.Leg3.x = this.Leg2.x + (float) Math.cos(this.Leg2.xRot) * (float) Math.sin(this.Leg2.yRot) * 49.0f;
            this.Foot1.x = this.Leg3.x;
            this.Foot1.y = this.Leg3.y;
            this.Foot1.z = this.Leg3.z;
            this.Foot2.x = this.Leg3.x;
            this.Foot2.y = this.Leg3.y;
            this.Foot2.z = this.Leg3.z;
            this.Foot3.x = this.Leg3.x;
            this.Foot3.y = this.Leg3.y;
            this.Foot3.z = this.Leg3.z;
            this.Foot4.x = this.Leg3.x;
            this.Foot4.y = this.Leg3.y;
            this.Foot4.z = this.Leg3.z;
            this.Foot5.x = this.Leg3.x;
            this.Foot5.y = this.Leg3.y;
            this.Foot5.z = this.Leg3.z;
            this.Foot6.x = this.Leg3.x;
            this.Foot6.y = this.Leg3.y;
            this.Foot6.z = this.Leg3.z;
            this.Foot7.x = this.Leg3.x;
            this.Foot7.y = this.Leg3.y;
            this.Foot7.z = this.Leg3.z;
            this.Leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot3.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot4.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot5.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot6.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.Foot7.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        if (this.attacking == 0) {
            this.LJaw1.yRot = 0.89f;
            this.LJaw2.yRot = 1.378f;
            this.RJaw1.yRot = 2.216f;
            this.RJaw2.yRot = 1.745f;
            this.LAntenna.xRot = Mth.cos(r.gpcounter * 0.35f) * 3.1415927f * 0.05f;
            this.LAntenna.zRot = 0.54f + Mth.cos(r.gpcounter * 0.25f) * 3.1415927f * 0.05f;
            this.RAntenna.xRot = Mth.cos(r.gpcounter * 0.3f) * 3.1415927f * 0.05f;
            this.RAntenna.zRot = -0.54f + Mth.cos(r.gpcounter * 0.45f) * 3.1415927f * 0.05f;
        } else {
            final float newangle = Mth.cos(r.gpcounter * 0.25f) * 3.1415927f * 0.22f;
            this.LJaw1.yRot = newangle + 0.89f;
            this.LJaw2.yRot = newangle + 1.378f;
            this.RJaw1.yRot = -newangle + 2.216f;
            this.RJaw2.yRot = 1.745f - newangle;
            this.LAntenna.xRot = Mth.cos(r.gpcounter * 0.45f) * 3.1415927f * 0.1f;
            this.LAntenna.zRot = 0.54f + Mth.cos(r.gpcounter * 0.35f) * 3.1415927f * 0.1f;
            this.RAntenna.xRot = Mth.cos(r.gpcounter * 0.4f) * 3.1415927f * 0.1f;
            this.RAntenna.zRot = -0.54f + Mth.cos(r.gpcounter * 0.55f) * 3.1415927f * 0.1f;
        }
        this.Body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Abdomen.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Jet1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Jet2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hip6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LJaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RJaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LJaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RJaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LAntenna.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RAntenna.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
