package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CrabGeometry;
import com.swbr.orespawn.entity.terror.Crab;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCrab} (ModelCrab.java:7-432): 25 boxes, 256x512 texture, geometry from the
 * generated {@link CrabGeometry}. Only three leg parts ({@code leg1/2/3}) exist; {@code render()} (:165-424) draws them
 * eight times, re-posing them before each draw (x ±36, y 3, z 0/10/20/30, walking angle {@code -pi/2 ± cos(f2 * 1.7) *
 * pi * 0.15 * f1}, right side negated). After the eight leg passes {@code leg2} and {@code leg3} are drawn once more
 * with the last pose, between {@code body4} and {@code body5}. Eyes, mouth plates and claws move faster and wider while
 * the crab attacks (DataWatcher 20). No GL calls.
 *
 * <p>Because a leg is posed between draws, the leg passes live in {@link #renderToBuffer}; {@link #setupAnim} keeps the
 * two animation inputs they need ({@code f1}, {@code f2}) and ports the remaining writes.
 */
public class CrabModel extends EntityModel<Crab> {

    /** Register with {@code CrabGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "crab"), "main");

    /** {@code ModelCrab(float f)}: the argument is not read (:35-37); ClientProxyOreSpawn passes 1.0 (manifest). */
    private final ModelPart[] parts;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart leg1;
    private final ModelPart body3;
    private final ModelPart body4;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart body5;
    private final ModelPart body6;
    private final ModelPart leye1;
    private final ModelPart reye1;
    private final ModelPart leye2;
    private final ModelPart reye2;
    private final ModelPart lclaw1;
    private final ModelPart lclaw2;
    private final ModelPart lclaw3;
    private final ModelPart lclaw4;
    private final ModelPart lclaw5;
    private final ModelPart rclaw1;
    private final ModelPart rclaw2;
    private final ModelPart rclaw3;
    private final ModelPart rclaw4;
    private final ModelPart rclaw5;
    private final ModelPart rmouth;
    private final ModelPart lmouth;
    /** {@code f1} of the frame, for the leg passes. */
    private float limbSwingAmount;
    /** {@code f2} of the frame, for the leg passes. */
    private float ageInTicks;

    public CrabModel(final ModelPart root, final float f) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[CrabGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(CrabGeometry.PARTS[i]);
        }
        this.body1 = root.getChild(CrabGeometry.BODY1);
        this.body2 = root.getChild(CrabGeometry.BODY2);
        this.leg1 = root.getChild(CrabGeometry.LEG1);
        this.body3 = root.getChild(CrabGeometry.BODY3);
        this.body4 = root.getChild(CrabGeometry.BODY4);
        this.leg2 = root.getChild(CrabGeometry.LEG2);
        this.leg3 = root.getChild(CrabGeometry.LEG3);
        this.body5 = root.getChild(CrabGeometry.BODY5);
        this.body6 = root.getChild(CrabGeometry.BODY6);
        this.leye1 = root.getChild(CrabGeometry.LEYE1);
        this.reye1 = root.getChild(CrabGeometry.REYE1);
        this.leye2 = root.getChild(CrabGeometry.LEYE2);
        this.reye2 = root.getChild(CrabGeometry.REYE2);
        this.lclaw1 = root.getChild(CrabGeometry.LCLAW1);
        this.lclaw2 = root.getChild(CrabGeometry.LCLAW2);
        this.lclaw3 = root.getChild(CrabGeometry.LCLAW3);
        this.lclaw4 = root.getChild(CrabGeometry.LCLAW4);
        this.lclaw5 = root.getChild(CrabGeometry.LCLAW5);
        this.rclaw1 = root.getChild(CrabGeometry.RCLAW1);
        this.rclaw2 = root.getChild(CrabGeometry.RCLAW2);
        this.rclaw3 = root.getChild(CrabGeometry.RCLAW3);
        this.rclaw4 = root.getChild(CrabGeometry.RCLAW4);
        this.rclaw5 = root.getChild(CrabGeometry.RCLAW5);
        this.rmouth = root.getChild(CrabGeometry.RMOUTH);
        this.lmouth = root.getChild(CrabGeometry.LMOUTH);
    }

    /** The eye, mouth and claw writes of {@code render()} (:330-396); the leg poses are in {@link #renderToBuffer}. */
    @Override
    public void setupAnim(final Crab e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        this.limbSwingAmount = f1;
        this.ageInTicks = f2;
        if (e.getAttacking() == 0) {
            final float n = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.05f;
            this.leye2.xRot = n;
            this.leye1.xRot = n;
            final float n2 = 0.54f + Mth.cos(f2 * 0.25f) * 3.1415927f * 0.05f;
            this.leye2.zRot = n2;
            this.leye1.zRot = n2;
            final float n3 = Mth.cos(f2 * 0.3f) * 3.1415927f * 0.05f;
            this.reye2.xRot = n3;
            this.reye1.xRot = n3;
            final float n4 = -0.54f + Mth.cos(f2 * 0.45f) * 3.1415927f * 0.05f;
            this.reye2.zRot = n4;
            this.reye1.zRot = n4;
            this.lmouth.yRot = -0.72f + Mth.cos(f2 * 0.25f) * 3.1415927f * 0.05f;
            this.rmouth.yRot = 0.72f - Mth.cos(f2 * 0.25f) * 3.1415927f * 0.05f;
            float newangle = Mth.cos(f2 * 0.15f) * 3.1415927f * 0.03f;
            this.lclaw3.yRot = -0.453f + newangle;
            this.lclaw4.yRot = -0.349f + newangle;
            this.lclaw5.yRot = 0.384f - newangle;
            newangle = Mth.cos(f2 * 0.13f) * 3.1415927f * 0.02f;
            this.rclaw3.yRot = 0.453f + newangle;
            this.rclaw4.yRot = 0.349f + newangle;
            this.rclaw5.yRot = -0.384f - newangle;
        } else {
            final float n5 = Mth.cos(f2 * 0.45f) * 3.1415927f * 0.1f;
            this.leye2.xRot = n5;
            this.leye1.xRot = n5;
            final float n6 = 0.54f + Mth.cos(f2 * 0.35f) * 3.1415927f * 0.1f;
            this.leye2.zRot = n6;
            this.leye1.zRot = n6;
            final float n7 = Mth.cos(f2 * 0.4f) * 3.1415927f * 0.1f;
            this.reye2.xRot = n7;
            this.reye1.xRot = n7;
            final float n8 = -0.54f + Mth.cos(f2 * 0.55f) * 3.1415927f * 0.1f;
            this.reye2.zRot = n8;
            this.reye1.zRot = n8;
            this.lmouth.yRot = -0.72f + Mth.cos(f2 * 0.45f) * 3.1415927f * 0.15f;
            this.rmouth.yRot = 0.72f - Mth.cos(f2 * 0.45f) * 3.1415927f * 0.15f;
            float newangle = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.13f;
            this.lclaw3.yRot = -0.453f + newangle;
            this.lclaw4.yRot = -0.349f + newangle;
            this.lclaw5.yRot = 0.384f - newangle;
            newangle = Mth.cos(f2 * 0.43f) * 3.1415927f * 0.12f;
            this.rclaw3.yRot = 0.453f + newangle;
            this.rclaw4.yRot = 0.349f + newangle;
            this.rclaw5.yRot = -0.384f - newangle;
        }
    }

    /** One leg pass: pose {@code leg1/2/3} and draw them. */
    private void legPass(final float x, final float y, final float z, final float yRot, final PoseStack poseStack,
                         final VertexConsumer buffer, final int packedLight, final int packedOverlay, final int color) {
        this.leg3.x = x;
        this.leg2.x = x;
        this.leg1.x = x;
        this.leg3.y = y;
        this.leg2.y = y;
        this.leg1.y = y;
        this.leg3.z = z;
        this.leg2.z = z;
        this.leg1.z = z;
        this.leg3.yRot = yRot;
        this.leg2.yRot = yRot;
        this.leg1.yRot = yRot;
        this.leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    /**
     * {@code render()} (:169-424): the eight leg passes (the first four set x 36, y 3; the fifth sets x -36, y 3 again; z
     * is set in every pass), then the body in the original draw order - {@code leg2} and {@code leg3} once more with the
     * last leg pose, {@code leg1} not.
     */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final float f1 = this.limbSwingAmount;
        final float f2 = this.ageInTicks;
        final float plus = (float) (-1.5707963267948966 + Mth.cos(f2 * 1.7f) * 3.1415927f * 0.15f * f1);
        final float minus = (float) (-1.5707963267948966 - Mth.cos(f2 * 1.7f) * 3.1415927f * 0.15f * f1);
        this.legPass(36.0f, 3.0f, 0.0f, plus, poseStack, buffer, packedLight, packedOverlay, color);
        this.legPass(36.0f, 3.0f, 10.0f, minus, poseStack, buffer, packedLight, packedOverlay, color);
        this.legPass(36.0f, 3.0f, 20.0f, plus, poseStack, buffer, packedLight, packedOverlay, color);
        this.legPass(36.0f, 3.0f, 30.0f, minus, poseStack, buffer, packedLight, packedOverlay, color);
        final float negPlus = (float) (-(-1.5707963267948966 + Mth.cos(f2 * 1.7f) * 3.1415927f * 0.15f * f1));
        final float negMinus = (float) (-(-1.5707963267948966 - Mth.cos(f2 * 1.7f) * 3.1415927f * 0.15f * f1));
        this.legPass(-36.0f, 3.0f, 0.0f, negPlus, poseStack, buffer, packedLight, packedOverlay, color);
        this.legPass(-36.0f, 3.0f, 10.0f, negMinus, poseStack, buffer, packedLight, packedOverlay, color);
        this.legPass(-36.0f, 3.0f, 20.0f, negPlus, poseStack, buffer, packedLight, packedOverlay, color);
        this.legPass(-36.0f, 3.0f, 30.0f, negMinus, poseStack, buffer, packedLight, packedOverlay, color);
        this.body1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leye1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.reye1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leye2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.reye2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rmouth.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lmouth.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
