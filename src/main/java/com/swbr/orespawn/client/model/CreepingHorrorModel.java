package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CreepingHorrorGeometry;
import com.swbr.orespawn.entity.terror.CreepingHorror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCreepingHorror} (ModelCreepingHorror.java:7-260): 26 boxes, 128x128 texture,
 * geometry from the generated {@link CreepingHorrorGeometry}. The animation is {@code render()}: legs swinging with the
 * walk speed {@code f1}, pincers, a curling three-segment tail and five wobbling back spikes. No GL calls.
 */
public class CreepingHorrorModel extends EntityModel<CreepingHorror> {

    /** Register with {@code CreepingHorrorGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "creeping_horror"), "main");

    /** Every part in {@code render()} order, which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart leg1;
    private final ModelPart leg1part2;
    private final ModelPart leg2;
    private final ModelPart leg2part2;
    private final ModelPart leg3;
    private final ModelPart leg3part2;
    private final ModelPart leg4;
    private final ModelPart leg4part2;
    private final ModelPart tailseg1;
    private final ModelPart tailseg2;
    private final ModelPart tailseg3;
    private final ModelPart pincer1;
    private final ModelPart pincer1part2;
    private final ModelPart pincer2;
    private final ModelPart pincer2part2;
    private final ModelPart spike1;
    private final ModelPart spike2;
    private final ModelPart spike3;
    private final ModelPart spike4;
    private final ModelPart spike5;

    public CreepingHorrorModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[CreepingHorrorGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(CreepingHorrorGeometry.PARTS[i]);
        }
        this.leg1 = root.getChild(CreepingHorrorGeometry.LEG1);
        this.leg1part2 = root.getChild(CreepingHorrorGeometry.LEG1PART2);
        this.leg2 = root.getChild(CreepingHorrorGeometry.LEG2);
        this.leg2part2 = root.getChild(CreepingHorrorGeometry.LEG2PART2);
        this.leg3 = root.getChild(CreepingHorrorGeometry.LEG3);
        this.leg3part2 = root.getChild(CreepingHorrorGeometry.LEG3PART2);
        this.leg4 = root.getChild(CreepingHorrorGeometry.LEG4);
        this.leg4part2 = root.getChild(CreepingHorrorGeometry.LEG4PART2);
        this.tailseg1 = root.getChild(CreepingHorrorGeometry.TAILSEG1);
        this.tailseg2 = root.getChild(CreepingHorrorGeometry.TAILSEG2);
        this.tailseg3 = root.getChild(CreepingHorrorGeometry.TAILSEG3);
        this.pincer1 = root.getChild(CreepingHorrorGeometry.PINCER1);
        this.pincer1part2 = root.getChild(CreepingHorrorGeometry.PINCER1PART2);
        this.pincer2 = root.getChild(CreepingHorrorGeometry.PINCER2);
        this.pincer2part2 = root.getChild(CreepingHorrorGeometry.PINCER2PART2);
        this.spike1 = root.getChild(CreepingHorrorGeometry.SPIKE1);
        this.spike2 = root.getChild(CreepingHorrorGeometry.SPIKE2);
        this.spike3 = root.getChild(CreepingHorrorGeometry.SPIKE3);
        this.spike4 = root.getChild(CreepingHorrorGeometry.SPIKE4);
        this.spike5 = root.getChild(CreepingHorrorGeometry.SPIKE5);
    }

    /** The writes of {@code render()} (ModelCreepingHorror.java:186-233). */
    @Override
    public void setupAnim(final CreepingHorror entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = Mth.cos(f2 * 1.25f) * 3.1415927f * 0.35f * f1;
        this.leg1.yRot = 0.576f + newangle;
        this.leg1part2.yRot = this.leg1.yRot;
        this.leg2.yRot = -0.576f - newangle;
        this.leg2part2.yRot = this.leg2.yRot;
        this.leg3.yRot = -0.576f - newangle;
        this.leg3part2.yRot = this.leg3.yRot;
        this.leg4.yRot = 0.576f + newangle;
        this.leg4part2.yRot = this.leg4.yRot;
        newangle = Mth.cos(f2 * 0.48f) * 3.1415927f * 0.15f;
        this.pincer1.yRot = newangle;
        this.pincer1part2.yRot = newangle;
        this.pincer2.yRot = -newangle;
        this.pincer2part2.yRot = -newangle;
        newangle = Mth.cos(f2 * 0.11f) * 3.1415927f * 0.25f;
        newangle = Math.abs(newangle);
        this.tailseg1.xRot = -0.55f + newangle;
        this.tailseg3.xRot = -0.22f + newangle;
        this.tailseg2.xRot = newangle;
        newangle = Mth.cos(f2 * 0.81f) * 3.1415927f * 0.08f;
        this.spike1.xRot = 0.7f + newangle;
        newangle = Mth.cos(f2 * 0.87f) * 3.1415927f * 0.08f;
        this.spike2.xRot = 0.7f + newangle;
        newangle = Mth.cos(f2 * 0.99f) * 3.1415927f * 0.08f;
        this.spike3.xRot = 0.7f + newangle;
        newangle = Mth.cos(f2 * 0.103f) * 3.1415927f * 0.08f;
        this.spike4.xRot = 0.7f + newangle;
        newangle = Mth.cos(f2 * 0.107f) * 3.1415927f * 0.08f;
        this.spike5.xRot = 0.7f + newangle;
        newangle = Mth.cos(f2 * 1.11f) * 3.1415927f * 0.08f;
        this.spike1.yRot = newangle;
        newangle = Mth.cos(f2 * 1.17f) * 3.1415927f * 0.08f;
        this.spike2.yRot = newangle;
        newangle = Mth.cos(f2 * 1.25f) * 3.1415927f * 0.08f;
        this.spike3.yRot = newangle;
        newangle = Mth.cos(f2 * 1.28f) * 3.1415927f * 0.08f;
        this.spike4.yRot = newangle;
        newangle = Mth.cos(f2 * 1.31f) * 3.1415927f * 0.08f;
        this.spike5.yRot = newangle;
        newangle = Mth.cos(f2 * 1.41f) * 3.1415927f * 0.08f;
        this.spike1.zRot = newangle;
        newangle = Mth.cos(f2 * 1.47f) * 3.1415927f * 0.08f;
        this.spike2.zRot = newangle;
        newangle = Mth.cos(f2 * 1.55f) * 3.1415927f * 0.08f;
        this.spike3.zRot = newangle;
        newangle = Mth.cos(f2 * 1.58f) * 3.1415927f * 0.08f;
        this.spike4.zRot = newangle;
        newangle = Mth.cos(f2 * 1.61f) * 3.1415927f * 0.08f;
        this.spike5.zRot = newangle;
    }

    /** {@code render()} (:234-259): every part once, in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
