package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.Robot3Geometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.robot.Robot3;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelRobot3} (ModelRobot3.java:7-199): the Robo-Gunner - splayed two-part legs,
 * hips and a stacked waist, a huge tilted body block with the laser cannon in front, and folded arms. Geometry
 * (:30-130) is the generated {@link Robot3Geometry}.
 *
 * <p>The cannon turns with half the head yaw; the arms pump only while {@code attacking} ({@code ri1} of the
 * entity's {@link RenderInfo}, latched when the swing crosses zero). {@code wingspeed} 1.0 (manifest), renderer
 * scale 0.5. {@code resetPose()} at the start (R8).
 */
public class Robot3Model extends EntityModel<Robot3> {

    /** Register with {@code Robot3Geometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot3"), "main");

    private final ModelPart root;
    private final float wingspeed;
    /** Every part in the order {@code render()} drew them (:169-187), which is the constructor order. */
    private final ModelPart[] parts;
    private final ModelPart rleg1;
    private final ModelPart lleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg2;
    private final ModelPart lazer;
    private final ModelPart larm3;
    private final ModelPart rarm3;
    private final ModelPart larm2;
    private final ModelPart rarm2;
    private final ModelPart larm1;
    private final ModelPart rarm1;

    /** {@code ModelRobot3(float f1)} (:30-130). */
    public Robot3Model(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.parts = new ModelPart[Robot3Geometry.PARTS.length];
        for (int i = 0; i < Robot3Geometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(Robot3Geometry.PARTS[i]);
        }
        this.rleg1 = root.getChild(Robot3Geometry.RLEG1);
        this.lleg1 = root.getChild(Robot3Geometry.LLEG1);
        this.rleg2 = root.getChild(Robot3Geometry.RLEG2);
        this.lleg2 = root.getChild(Robot3Geometry.LLEG2);
        this.lazer = root.getChild(Robot3Geometry.LAZER);
        this.larm3 = root.getChild(Robot3Geometry.LARM3);
        this.rarm3 = root.getChild(Robot3Geometry.RARM3);
        this.larm2 = root.getChild(Robot3Geometry.LARM2);
        this.rarm2 = root.getChild(Robot3Geometry.RARM2);
        this.larm1 = root.getChild(Robot3Geometry.LARM1);
        this.rarm1 = root.getChild(Robot3Geometry.RARM1);
    }

    /**
     * The writes of {@code render()} (:132-168). {@code f1} limb swing amount, {@code f2} age in ticks, {@code f3}
     * head yaw minus body yaw in degrees. {@code e.setRenderInfo(r)} (:168) copied the object onto itself.
     */
    @Override
    public void setupAnim(final Robot3 e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.55f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lleg1.xRot = newangle;
        this.lleg2.xRot = newangle;
        this.rleg1.xRot = -newangle;
        this.rleg2.xRot = -newangle;
        this.lazer.yRot = (float) Math.toRadians(f3 / 2.0);
        r = e.getRenderInfo();
        newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.3f) * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (e.getAttacking() != 0) {
                r.ri1 = 1;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        this.rarm1.xRot = newangle - 1.0f;
        this.rarm2.xRot = newangle + 1.0f;
        this.rarm3.xRot = newangle + 1.0f;
        this.larm1.xRot = newangle - 1.0f;
        this.larm2.xRot = newangle + 1.0f;
        this.larm3.xRot = newangle + 1.0f;
    }

    /** The {@code render(f5)} calls of {@code render()} (:169-187). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
