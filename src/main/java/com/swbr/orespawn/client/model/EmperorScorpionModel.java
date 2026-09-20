package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.EmperorScorpionGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.arthropod.EmperorScorpion;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelEmperorScorpion} (ModelEmperorScorpion.java:7-747): 78 boxes, 256x128 texture,
 * geometry from the generated {@link EmperorScorpionGeometry}. The animation is {@code render()} (:486-567) with the
 * helpers {@code doLeftLeg}/{@code doRightLeg} (:658-692), {@code doLeftClaw}/{@code doRightClaw} (:694-712) and
 * {@code doTail} (:714-746).
 *
 * <p>Eight legs of five segments, pairwise in four phases; a leg lifts ({@code upangle}) while its phase rises. The
 * mandibles clack faster while the scorpion attacks. Claws and tail strike in cycles picked by a roll on the client
 * world's random when the claw cosine crosses zero upwards, noted in the entity's {@link RenderInfo}.
 */
public class EmperorScorpionModel extends EntityModel<EmperorScorpion> {

    /** Register with {@code EmperorScorpionGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "emperor_scorpion"), "main");

    /** {@code ModelEmperorScorpion(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 0.22 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:568-645), which is the creation order. */
    private final ModelPart[] parts;
    /** {@code LegNSegM} as {@code legs[N - 1][M - 1]}. */
    private final ModelPart[][] legs = new ModelPart[8][5];
    private final ModelPart tailseg1;
    private final ModelPart tailseg2;
    private final ModelPart tailseg3;
    private final ModelPart tailseg4;
    private final ModelPart tailseg5;
    private final ModelPart tailseg6;
    private final ModelPart tailseg7;
    private final ModelPart tailseg8;
    private final ModelPart stinger1;
    private final ModelPart stinger2;
    private final ModelPart stinger3;
    private final ModelPart leftArmSeg1;
    private final ModelPart leftArmSeg2;
    private final ModelPart leftArmSeg3;
    private final ModelPart leftArmSeg4;
    private final ModelPart rightArmSeg1;
    private final ModelPart rightArmSeg2;
    private final ModelPart rightArmSeg3;
    private final ModelPart rightArmSeg4;
    private final ModelPart rightPincer;
    private final ModelPart leftPincer;
    private final ModelPart rightManPart2;
    private final ModelPart leftManPart2;

    public EmperorScorpionModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[EmperorScorpionGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(EmperorScorpionGeometry.PARTS[i]);
        }
        final String[][] legNames = {
                {EmperorScorpionGeometry.LEG1_SEG1, EmperorScorpionGeometry.LEG1_SEG2, EmperorScorpionGeometry.LEG1_SEG3, EmperorScorpionGeometry.LEG1_SEG4, EmperorScorpionGeometry.LEG1_SEG5},
                {EmperorScorpionGeometry.LEG2_SEG1, EmperorScorpionGeometry.LEG2_SEG2, EmperorScorpionGeometry.LEG2_SEG3, EmperorScorpionGeometry.LEG2_SEG4, EmperorScorpionGeometry.LEG2_SEG5},
                {EmperorScorpionGeometry.LEG3_SEG1, EmperorScorpionGeometry.LEG3_SEG2, EmperorScorpionGeometry.LEG3_SEG3, EmperorScorpionGeometry.LEG3_SEG4, EmperorScorpionGeometry.LEG3_SEG5},
                {EmperorScorpionGeometry.LEG4_SEG1, EmperorScorpionGeometry.LEG4_SEG2, EmperorScorpionGeometry.LEG4_SEG3, EmperorScorpionGeometry.LEG4_SEG4, EmperorScorpionGeometry.LEG4_SEG5},
                {EmperorScorpionGeometry.LEG5_SEG1, EmperorScorpionGeometry.LEG5_SEG2, EmperorScorpionGeometry.LEG5_SEG3, EmperorScorpionGeometry.LEG5_SEG4, EmperorScorpionGeometry.LEG5_SEG5},
                {EmperorScorpionGeometry.LEG6_SEG1, EmperorScorpionGeometry.LEG6_SEG2, EmperorScorpionGeometry.LEG6_SEG3, EmperorScorpionGeometry.LEG6_SEG4, EmperorScorpionGeometry.LEG6_SEG5},
                {EmperorScorpionGeometry.LEG7_SEG1, EmperorScorpionGeometry.LEG7_SEG2, EmperorScorpionGeometry.LEG7_SEG3, EmperorScorpionGeometry.LEG7_SEG4, EmperorScorpionGeometry.LEG7_SEG5},
                {EmperorScorpionGeometry.LEG8_SEG1, EmperorScorpionGeometry.LEG8_SEG2, EmperorScorpionGeometry.LEG8_SEG3, EmperorScorpionGeometry.LEG8_SEG4, EmperorScorpionGeometry.LEG8_SEG5}
        };
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 5; ++j) {
                this.legs[i][j] = root.getChild(legNames[i][j]);
            }
        }
        this.tailseg1 = root.getChild(EmperorScorpionGeometry.TAILSEG1);
        this.tailseg2 = root.getChild(EmperorScorpionGeometry.TAILSEG2);
        this.tailseg3 = root.getChild(EmperorScorpionGeometry.TAILSEG3);
        this.tailseg4 = root.getChild(EmperorScorpionGeometry.TAILSEG4);
        this.tailseg5 = root.getChild(EmperorScorpionGeometry.TAILSEG5);
        this.tailseg6 = root.getChild(EmperorScorpionGeometry.TAILSEG6);
        this.tailseg7 = root.getChild(EmperorScorpionGeometry.TAILSEG7);
        this.tailseg8 = root.getChild(EmperorScorpionGeometry.TAILSEG8);
        this.stinger1 = root.getChild(EmperorScorpionGeometry.STINGER1);
        this.stinger2 = root.getChild(EmperorScorpionGeometry.STINGER2);
        this.stinger3 = root.getChild(EmperorScorpionGeometry.STINGER3);
        this.leftArmSeg1 = root.getChild(EmperorScorpionGeometry.LEFT_ARM_SEG1);
        this.leftArmSeg2 = root.getChild(EmperorScorpionGeometry.LEFT_ARM_SEG2);
        this.leftArmSeg3 = root.getChild(EmperorScorpionGeometry.LEFT_ARM_SEG3);
        this.leftArmSeg4 = root.getChild(EmperorScorpionGeometry.LEFT_ARM_SEG4);
        this.rightArmSeg1 = root.getChild(EmperorScorpionGeometry.RIGHT_ARM_SEG1);
        this.rightArmSeg2 = root.getChild(EmperorScorpionGeometry.RIGHT_ARM_SEG2);
        this.rightArmSeg3 = root.getChild(EmperorScorpionGeometry.RIGHT_ARM_SEG3);
        this.rightArmSeg4 = root.getChild(EmperorScorpionGeometry.RIGHT_ARM_SEG4);
        this.rightPincer = root.getChild(EmperorScorpionGeometry.RIGHT_PINCER);
        this.leftPincer = root.getChild(EmperorScorpionGeometry.LEFT_PINCER);
        this.rightManPart2 = root.getChild(EmperorScorpionGeometry.RIGHT_MAN_PART2);
        this.leftManPart2 = root.getChild(EmperorScorpionGeometry.LEFT_MAN_PART2);
    }

    /** The writes of {@code render()} (:486-567). */
    @Override
    public void setupAnim(final EmperorScorpion e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        final float pi4 = 1.570795f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.cos((f2 + 0.1f) * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * f1 - Math.abs(newangle);
        }
        doLeftLeg(this.legs[0], newangle, upangle);
        doRightLeg(this.legs[4], -newangle, upangle);
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 1.0f * pi4) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.cos((f2 + 0.1f) * 2.0f * this.wingspeed - 1.0f * pi4) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * f1 - Math.abs(newangle);
        }
        doLeftLeg(this.legs[1], newangle, upangle);
        doRightLeg(this.legs[5], -newangle, upangle);
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.cos((f2 + 0.1f) * 2.0f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * f1 - Math.abs(newangle);
        }
        doLeftLeg(this.legs[2], newangle, upangle);
        doRightLeg(this.legs[6], -newangle, upangle);
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.cos((f2 + 0.1f) * 2.0f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * f1 - Math.abs(newangle);
        }
        doLeftLeg(this.legs[3], newangle, upangle);
        doRightLeg(this.legs[7], -newangle, upangle);
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.05f;
        } else {
            newangle = Mth.cos(f2 * 2.5f * this.wingspeed) * 3.1415927f * 0.15f;
        }
        this.leftManPart2.zRot = newangle;
        this.rightManPart2.zRot = -newangle;
        r = e.getRenderInfo();
        newangle = Mth.cos(f2 * 3.0f * this.wingspeed) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.1f) * 3.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            // e.worldObj.rand is the client world's random here, as in the original.
            if (e.getAttacking() == 0) {
                r.ri1 = e.level().random.nextInt(20);
                r.ri2 = e.level().random.nextInt(25);
            } else {
                r.ri1 = e.level().random.nextInt(4);
                r.ri2 = e.level().random.nextInt(3);
            }
        }
        if (r.ri1 == 1 || r.ri1 == 3) {
            this.doLeftClaw(newangle);
        } else {
            this.doLeftClaw(0.0f);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            this.doRightClaw(newangle);
        } else {
            this.doRightClaw(0.0f);
        }
        if (r.ri2 == 1) {
            this.doTail(newangle);
        } else {
            this.doTail(0.0f);
        }
        e.setRenderInfo(r);
    }

    /** {@code doLeftLeg(seg1..seg5, angle, upangle)} (:658-674); {@code seg1} is passed but never written. */
    private static void doLeftLeg(final ModelPart[] leg, final float angle, final float upangle) {
        final ModelPart seg2 = leg[1];
        final ModelPart seg3 = leg[2];
        final ModelPart seg4 = leg[3];
        final ModelPart seg5 = leg[4];
        seg2.yRot = angle;
        seg3.yRot = angle;
        seg4.yRot = angle;
        seg5.yRot = angle;
        seg3.z = (float) (seg2.z - Math.sin(angle) * 6.0);
        seg3.x = (float) (seg2.x - Math.abs(Math.sin(angle) * 6.0) + 6.0);
        seg4.z = (float) (seg3.z - Math.sin(angle) * 9.0);
        seg4.x = (float) (seg3.x - Math.abs(Math.sin(angle) * 9.0) + 9.0);
        seg5.z = (float) (seg4.z - Math.sin(angle) * 1.0);
        seg5.x = (float) (seg4.x - Math.abs(Math.sin(angle) * 1.0) + 1.0);
        seg2.zRot = -upangle - 0.929f;
        seg3.zRot = -upangle + 0.632f;
        seg3.y = seg2.y + (float) (11.5 * Math.sin(seg2.zRot));
        seg4.y = seg3.y + (float) (11.5 * Math.sin(seg3.zRot));
        seg5.y = seg4.y + 6.5f;
    }

    /** {@code doRightLeg(seg1..seg5, angle, upangle)} (:676-692); the foot takes {@code -angle}. */
    private static void doRightLeg(final ModelPart[] leg, final float angle, final float upangle) {
        final ModelPart seg2 = leg[1];
        final ModelPart seg3 = leg[2];
        final ModelPart seg4 = leg[3];
        final ModelPart seg5 = leg[4];
        seg2.yRot = angle;
        seg3.yRot = angle;
        seg4.yRot = angle;
        seg5.yRot = -angle;
        seg3.z = (float) (seg2.z + Math.sin(angle) * 6.0);
        seg3.x = (float) (seg2.x + Math.abs(Math.sin(angle) * 6.0) - 6.0);
        seg4.z = (float) (seg3.z + Math.sin(angle) * 9.0);
        seg4.x = (float) (seg3.x + Math.abs(Math.sin(angle) * 9.0) - 9.0);
        seg5.z = (float) (seg4.z + Math.sin(angle) * 1.0);
        seg5.x = (float) (seg4.x + Math.abs(Math.sin(angle) * 1.0) - 1.0);
        seg2.zRot = upangle + 0.929f;
        seg3.zRot = upangle - 0.632f;
        seg3.y = seg2.y - (float) (11.5 * Math.sin(seg2.zRot));
        seg4.y = seg3.y - (float) (11.5 * Math.sin(seg3.zRot));
        seg5.y = seg4.y + 6.5f;
    }

    /** {@code doLeftClaw} (:694-702). */
    private void doLeftClaw(final float angle) {
        this.leftArmSeg1.yRot = -1.57f + angle;
        this.leftArmSeg2.z = (float) (-22.0 - Math.cos(this.leftArmSeg1.yRot) * 12.0);
        this.leftArmSeg3.z = this.leftArmSeg2.z - 11.0f;
        this.leftArmSeg4.z = this.leftArmSeg2.z - 11.0f;
        this.leftPincer.z = this.leftArmSeg2.z - 11.0f;
        this.leftArmSeg3.yRot = 0.074f + angle;
        this.leftPincer.yRot = 0.371f - angle;
    }

    /** {@code doRightClaw} (:704-712). */
    private void doRightClaw(final float angle) {
        this.rightArmSeg1.yRot = 1.57f - angle;
        this.rightArmSeg2.z = (float) (-22.0 - Math.cos(this.rightArmSeg1.yRot) * 12.0);
        this.rightArmSeg3.z = this.rightArmSeg2.z - 11.0f;
        this.rightArmSeg4.z = this.rightArmSeg2.z - 11.0f;
        this.rightPincer.z = this.rightArmSeg2.z - 11.0f;
        this.rightArmSeg3.yRot = -0.074f - angle;
        this.rightPincer.yRot = -0.371f + angle;
    }

    /** {@code doTail} (:714-746): each segment hangs at the end of the previous one. */
    private void doTail(final float angle) {
        this.tailseg1.xRot = 0.594f + angle;
        this.tailseg2.xRot = this.tailseg1.xRot + 0.48399997f + angle;
        this.tailseg2.y = (float) (this.tailseg1.y - Math.sin(this.tailseg1.xRot) * 9.0);
        this.tailseg2.z = (float) (this.tailseg1.z + Math.cos(this.tailseg1.xRot) * 9.0);
        this.tailseg3.xRot = this.tailseg2.xRot + 0.6320001f + angle;
        this.tailseg3.y = (float) (this.tailseg2.y - Math.sin(this.tailseg2.xRot) * 10.0);
        this.tailseg3.z = (float) (this.tailseg2.z + Math.cos(this.tailseg2.xRot) * 10.0);
        this.tailseg4.xRot = this.tailseg3.xRot + 0.5569999f - angle;
        this.tailseg4.y = (float) (this.tailseg3.y - Math.sin(this.tailseg3.xRot) * 10.0);
        this.tailseg4.z = (float) (this.tailseg3.z + Math.cos(this.tailseg3.xRot) * 10.0);
        this.tailseg5.xRot = this.tailseg4.xRot + 0.63199997f - angle;
        this.tailseg5.y = (float) (this.tailseg4.y - Math.sin(this.tailseg4.xRot) * 10.0);
        this.tailseg5.z = (float) (this.tailseg4.z + Math.cos(this.tailseg4.xRot) * 10.0);
        this.tailseg6.xRot = this.tailseg5.xRot - 5.501f - angle * 3.0f / 2.0f - 0.4f;
        this.tailseg6.y = (float) (this.tailseg5.y - Math.sin(this.tailseg5.xRot) * 10.0);
        this.tailseg6.z = (float) (this.tailseg5.z + Math.cos(this.tailseg5.xRot) * 10.0);
        this.tailseg7.xRot = this.tailseg6.xRot - 2.822f - angle * 2.5f - 2.2f;
        this.tailseg7.y = (float) (this.tailseg6.y - Math.sin(this.tailseg6.xRot) * 10.0);
        this.tailseg7.z = (float) (this.tailseg6.z + Math.cos(this.tailseg6.xRot) * 10.0);
        this.tailseg8.xRot = this.tailseg7.xRot;
        this.tailseg8.y = this.tailseg7.y;
        this.tailseg8.z = this.tailseg7.z;
        this.stinger1.xRot = this.tailseg7.xRot + 0.0f + angle * 0.66f;
        this.stinger1.y = (float) (this.tailseg7.y - Math.sin(this.tailseg7.xRot) * 10.0);
        this.stinger1.z = (float) (this.tailseg7.z + Math.cos(this.tailseg7.xRot) * 10.0);
        this.stinger2.xRot = this.stinger1.xRot - 0.48f + angle;
        this.stinger2.y = (float) (this.stinger1.y - Math.sin(this.stinger1.xRot) * 3.0);
        this.stinger2.z = (float) (this.stinger1.z + Math.cos(this.stinger1.xRot) * 3.0);
        this.stinger3.xRot = this.stinger2.xRot - 1.01f + angle * 1.7f;
        this.stinger3.y = (float) (this.stinger2.y - Math.sin(this.stinger2.xRot) * 3.0);
        this.stinger3.z = (float) (this.stinger2.z + Math.cos(this.stinger2.xRot) * 3.0);
    }

    /** {@code render()} (:568-645). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
