package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.Robot1Geometry;
import com.swbr.orespawn.entity.robot.Robot1;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelRobot1} (ModelRobot1.java:7-237): the Bomb-Omb - a round body of stacked
 * boxes, two eyes, a fuse on top, two stomping feet and the wind-up key at the back. Geometry (:38-178) is the
 * generated {@link Robot1Geometry}.
 *
 * <p>{@code wingspeed} is the constructor argument, 2.0 for the Bomb-Omb (manifest {@code model_args}).
 * {@code entityCutoutNoCull} as 1.7.10 {@code RendererLivingEntity} drew (W06 precedent). {@code resetPose()} at
 * the start (R8); every animated field is recomputed each frame.
 */
public class Robot1Model extends EntityModel<Robot1> {

    /** Register with {@code Robot1Geometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot1"), "main");

    private final ModelPart root;
    private final float wingspeed;
    /** Every part in the order {@code render()} drew them (:199-225), which is the constructor order. */
    private final ModelPart[] parts;
    private final ModelPart rfoot;
    private final ModelPart lfoot;
    private final ModelPart key2;
    private final ModelPart key1;
    private final ModelPart key3;
    private final ModelPart key4;
    private final ModelPart key5;

    /** {@code ModelRobot1(float f1)} (:38-178). */
    public Robot1Model(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.parts = new ModelPart[Robot1Geometry.PARTS.length];
        for (int i = 0; i < Robot1Geometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(Robot1Geometry.PARTS[i]);
        }
        this.rfoot = root.getChild(Robot1Geometry.RFOOT);
        this.lfoot = root.getChild(Robot1Geometry.LFOOT);
        this.key2 = root.getChild(Robot1Geometry.KEY2);
        this.key1 = root.getChild(Robot1Geometry.KEY1);
        this.key3 = root.getChild(Robot1Geometry.KEY3);
        this.key4 = root.getChild(Robot1Geometry.KEY4);
        this.key5 = root.getChild(Robot1Geometry.KEY5);
    }

    /**
     * The writes of {@code render()} (:180-198): the feet stomp while walking ({@code f1 > 0.1}), the key turns with
     * age. {@code f1} limb swing amount, {@code f2} age in ticks.
     */
    @Override
    public void setupAnim(final Robot1 entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        float newangle;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.75f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lfoot.xRot = newangle;
        this.rfoot.xRot = -newangle;
        newangle = (float) Math.toRadians(f2 * 0.75f * this.wingspeed);
        this.key1.zRot = newangle;
        this.key2.zRot = newangle;
        this.key3.zRot = newangle;
        this.key4.zRot = newangle;
        this.key5.zRot = newangle;
    }

    /** The {@code render(f5)} calls of {@code render()} (:199-225). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
