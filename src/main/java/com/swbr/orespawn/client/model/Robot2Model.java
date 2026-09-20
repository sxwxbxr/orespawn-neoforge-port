package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.Robot2Geometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.robot.Robot2;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelRobot2} (ModelRobot2.java:7-187): the Robo-Pounder - two thick legs, a
 * stepped torso, a wide chest block, three-segment arms and a flat head. Geometry (:26-106) is the generated
 * {@link Robot2Geometry}.
 *
 * <p>While {@code attacking}, each time the swing phase crosses zero the robot rolls which arms pound: 1 right,
 * 2 left, 3 both ({@code ri1} of the entity's {@link RenderInfo}, a client-side note, rolled with the client
 * level's random as the original did). {@code wingspeed} 1.0 (manifest). {@code resetPose()} at the start (R8).
 */
public class Robot2Model extends EntityModel<Robot2> {

    /** Register with {@code Robot2Geometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot2"), "main");

    private final ModelPart root;
    private final float wingspeed;
    /** Every part in the order {@code render()} drew them (:161-175), which is the constructor order. */
    private final ModelPart[] parts;
    private final ModelPart rleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg2;
    private final ModelPart lleg1;
    private final ModelPart rarm3;
    private final ModelPart rarm2;
    private final ModelPart rarm1;
    private final ModelPart larm3;
    private final ModelPart larm2;
    private final ModelPart larm1;
    private final ModelPart head;

    /** {@code ModelRobot2(float f1)} (:26-106). */
    public Robot2Model(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.parts = new ModelPart[Robot2Geometry.PARTS.length];
        for (int i = 0; i < Robot2Geometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(Robot2Geometry.PARTS[i]);
        }
        this.rleg1 = root.getChild(Robot2Geometry.RLEG1);
        this.rleg2 = root.getChild(Robot2Geometry.RLEG2);
        this.lleg2 = root.getChild(Robot2Geometry.LLEG2);
        this.lleg1 = root.getChild(Robot2Geometry.LLEG1);
        this.rarm3 = root.getChild(Robot2Geometry.RARM3);
        this.rarm2 = root.getChild(Robot2Geometry.RARM2);
        this.rarm1 = root.getChild(Robot2Geometry.RARM1);
        this.larm3 = root.getChild(Robot2Geometry.LARM3);
        this.larm2 = root.getChild(Robot2Geometry.LARM2);
        this.larm1 = root.getChild(Robot2Geometry.LARM1);
        this.head = root.getChild(Robot2Geometry.HEAD);
    }

    /**
     * The writes of {@code render()} (:108-160). {@code f1} limb swing amount, {@code f2} age in ticks, {@code f3}
     * head yaw minus body yaw in degrees. {@code e.setRenderInfo(r)} (:160) copied the object onto itself and is
     * left out.
     */
    @Override
    public void setupAnim(final Robot2 e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        RenderInfo r = null;
        float newangle;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lleg1.xRot = newangle;
        this.lleg2.xRot = newangle;
        this.rleg1.xRot = -newangle;
        this.rleg2.xRot = -newangle;
        this.head.yRot = (float) Math.toRadians(f3);
        newangle = Mth.sin((float) Math.toRadians(f2 * 20.0f * this.wingspeed));
        final float nextangle = Mth.sin((float) Math.toRadians(f2 * 20.0f * this.wingspeed + 1.5f));
        r = e.getRenderInfo();
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (e.getAttacking() == 0) {
                r.ri1 = 0;
            } else {
                while (r.ri1 == 0) {
                    r.ri1 = e.level().random.nextInt(4);
                }
            }
        }
        newangle = (float) Math.toRadians(f2 * 20.0f * this.wingspeed);
        if (r.ri1 == 1 || r.ri1 == 3) {
            this.rarm1.xRot = newangle;
            this.rarm2.xRot = newangle;
            this.rarm3.xRot = newangle;
        } else {
            this.rarm1.xRot = 0.0f;
            this.rarm2.xRot = 0.0f;
            this.rarm3.xRot = 0.0f;
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            this.larm1.xRot = newangle;
            this.larm2.xRot = newangle;
            this.larm3.xRot = newangle;
        } else {
            this.larm1.xRot = 0.0f;
            this.larm2.xRot = 0.0f;
            this.larm3.xRot = 0.0f;
        }
    }

    /** The {@code render(f5)} calls of {@code render()} (:161-175). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
