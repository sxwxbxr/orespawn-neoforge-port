package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.Robot5Geometry;
import com.swbr.orespawn.entity.robot.Robot5;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelRobot5} (ModelRobot5.java:6-128): the Robo-Sniper - two wheels on an axle,
 * a thin stand and a long two-stage barrel with an ammo box on a swivel. Geometry (:21-81) is the generated
 * {@link Robot5Geometry}.
 *
 * <p>The wheels roll while it drives, the barrel turns with half the head yaw. {@code wingspeed} 1.0 (manifest),
 * unused by {@code render()}. {@code resetPose()} at the start (R8).
 */
public class Robot5Model extends EntityModel<Robot5> {

    /** Register with {@code Robot5Geometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot5"), "main");

    private final ModelPart root;
    @SuppressWarnings("unused")
    private final float wingspeed;
    /** Every part in the order {@code render()} drew them (:106-116), which is the constructor order. */
    private final ModelPart[] parts;
    private final ModelPart lwheel1;
    private final ModelPart lwheel2;
    private final ModelPart rwheel1;
    private final ModelPart rwheel2;
    private final ModelPart barrel1;
    private final ModelPart barrel2;
    private final ModelPart ammobox;

    /** {@code ModelRobot5(float f1)} (:21-81). */
    public Robot5Model(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.parts = new ModelPart[Robot5Geometry.PARTS.length];
        for (int i = 0; i < Robot5Geometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(Robot5Geometry.PARTS[i]);
        }
        this.lwheel1 = root.getChild(Robot5Geometry.LWHEEL1);
        this.lwheel2 = root.getChild(Robot5Geometry.LWHEEL2);
        this.rwheel1 = root.getChild(Robot5Geometry.RWHEEL1);
        this.rwheel2 = root.getChild(Robot5Geometry.RWHEEL2);
        this.barrel1 = root.getChild(Robot5Geometry.BARREL1);
        this.barrel2 = root.getChild(Robot5Geometry.BARREL2);
        this.ammobox = root.getChild(Robot5Geometry.AMMOBOX);
    }

    /**
     * The writes of {@code render()} (:83-105). {@code f1} limb swing amount, {@code f2} age in ticks, {@code f3}
     * head yaw minus body yaw in degrees.
     */
    @Override
    public void setupAnim(final Robot5 e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = f2 * 0.15f % 6.2831855f;
            newangle = Math.abs(newangle);
        } else {
            newangle = 0.0f;
        }
        this.lwheel1.xRot = newangle;
        this.lwheel2.xRot = (float) (newangle + 0.7853981633974483);
        this.rwheel1.xRot = newangle;
        this.rwheel2.xRot = (float) (newangle + 0.7853981633974483);
        final float rotateAngleY = (float) Math.toRadians(f3 / 2.0);
        this.ammobox.yRot = rotateAngleY;
        this.barrel2.yRot = rotateAngleY;
        this.barrel1.yRot = rotateAngleY;
    }

    /** The {@code render(f5)} calls of {@code render()} (:106-116). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
