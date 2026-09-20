package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CaveFisherGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.arthropod.CaveFisher;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCaveFisher} (ModelCaveFisher.java:7-647): 75 boxes, 64x32 texture, geometry
 * from the generated {@link CaveFisherGeometry}. The animation is {@code render()} (:468-538) with {@code doLeftClaw}
 * and {@code doRightClaw} (:626-646).
 *
 * <p>Six legs of six boxes swing in three phases. Both claws lift together, in cycles picked by a roll on the client
 * world's random when the claw cosine crosses zero upwards ({@code ri1} 1 or 3; {@code ri2} is rolled but unused), noted
 * in the entity's {@link RenderInfo}.
 */
public class CaveFisherModel extends EntityModel<CaveFisher> {

    /** Register with {@code CaveFisherGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cave_fisher"), "main");

    /** {@code ModelCaveFisher(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 0.62 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:539-613), which is the creation order. */
    private final ModelPart[] parts;
    /** {@code LFLeg1..6}, {@code RFLeg1..6}, {@code LMLeg1..6}, {@code RMLeg1..6}, {@code LBLeg1..6}, {@code RBLeg1..6}. */
    private final ModelPart[] lfLeg;
    private final ModelPart[] rfLeg;
    private final ModelPart[] lmLeg;
    private final ModelPart[] rmLeg;
    private final ModelPart[] lbLeg;
    private final ModelPart[] rbLeg;
    private final ModelPart leftArmSeg1;
    private final ModelPart leftArmSeg2;
    private final ModelPart leftArmSeg3;
    private final ModelPart leftArmSeg4;
    private final ModelPart leftArmSeg5;
    private final ModelPart leftClawBase;
    private final ModelPart leftClawTop;
    private final ModelPart leftClawLow;
    private final ModelPart rightArmSeg1;
    private final ModelPart rightArmSeg2;
    private final ModelPart rightArmSeg3;
    private final ModelPart rightArmSeg4;
    private final ModelPart rightArmSeg5;
    private final ModelPart rightClawBase;
    private final ModelPart rightClawTop;
    private final ModelPart rightClawLow;

    public CaveFisherModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[CaveFisherGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(CaveFisherGeometry.PARTS[i]);
        }
        this.lfLeg = children(root, CaveFisherGeometry.LFLEG1, CaveFisherGeometry.LFLEG2, CaveFisherGeometry.LFLEG3,
                CaveFisherGeometry.LFLEG4, CaveFisherGeometry.LFLEG5, CaveFisherGeometry.LFLEG6);
        this.rfLeg = children(root, CaveFisherGeometry.RFLEG1, CaveFisherGeometry.RFLEG2, CaveFisherGeometry.RFLEG3,
                CaveFisherGeometry.RFLEG4, CaveFisherGeometry.RFLEG5, CaveFisherGeometry.RFLEG6);
        this.lmLeg = children(root, CaveFisherGeometry.LMLEG1, CaveFisherGeometry.LMLEG2, CaveFisherGeometry.LMLEG3,
                CaveFisherGeometry.LMLEG4, CaveFisherGeometry.LMLEG5, CaveFisherGeometry.LMLEG6);
        this.rmLeg = children(root, CaveFisherGeometry.RMLEG1, CaveFisherGeometry.RMLEG2, CaveFisherGeometry.RMLEG3,
                CaveFisherGeometry.RMLEG4, CaveFisherGeometry.RMLEG5, CaveFisherGeometry.RMLEG6);
        this.lbLeg = children(root, CaveFisherGeometry.LBLEG1, CaveFisherGeometry.LBLEG2, CaveFisherGeometry.LBLEG3,
                CaveFisherGeometry.LBLEG4, CaveFisherGeometry.LBLEG5, CaveFisherGeometry.LBLEG6);
        this.rbLeg = children(root, CaveFisherGeometry.RBLEG1, CaveFisherGeometry.RBLEG2, CaveFisherGeometry.RBLEG3,
                CaveFisherGeometry.RBLEG4, CaveFisherGeometry.RBLEG5, CaveFisherGeometry.RBLEG6);
        this.leftArmSeg1 = root.getChild(CaveFisherGeometry.LEFT_ARM_SEG1);
        this.leftArmSeg2 = root.getChild(CaveFisherGeometry.LEFT_ARM_SEG2);
        this.leftArmSeg3 = root.getChild(CaveFisherGeometry.LEFT_ARM_SEG3);
        this.leftArmSeg4 = root.getChild(CaveFisherGeometry.LEFT_ARM_SEG4);
        this.leftArmSeg5 = root.getChild(CaveFisherGeometry.LEFT_ARM_SEG5);
        this.leftClawBase = root.getChild(CaveFisherGeometry.LEFT_CLAW_BASE);
        this.leftClawTop = root.getChild(CaveFisherGeometry.LEFT_CLAW_TOP);
        this.leftClawLow = root.getChild(CaveFisherGeometry.LEFT_CLAW_LOW);
        this.rightArmSeg1 = root.getChild(CaveFisherGeometry.RIGHT_ARM_SEG1);
        this.rightArmSeg2 = root.getChild(CaveFisherGeometry.RIGHT_ARM_SEG2);
        this.rightArmSeg3 = root.getChild(CaveFisherGeometry.RIGHT_ARM_SEG3);
        this.rightArmSeg4 = root.getChild(CaveFisherGeometry.RIGHT_ARM_SEG4);
        this.rightArmSeg5 = root.getChild(CaveFisherGeometry.RIGHT_ARM_SEG5);
        this.rightClawBase = root.getChild(CaveFisherGeometry.RIGHT_CLAW_BASE);
        this.rightClawTop = root.getChild(CaveFisherGeometry.RIGHT_CLAW_TOP);
        this.rightClawLow = root.getChild(CaveFisherGeometry.RIGHT_CLAW_LOW);
    }

    private static ModelPart[] children(final ModelPart root, final String... names) {
        final ModelPart[] out = new ModelPart[names.length];
        for (int i = 0; i < names.length; ++i) {
            out[i] = root.getChild(names[i]);
        }
        return out;
    }

    /** The six boxes of one leg take the same yaw (:478-515). */
    private static void setYRot(final ModelPart[] leg, final float yRot) {
        for (final ModelPart part : leg) {
            part.yRot = yRot;
        }
    }

    /** The writes of {@code render()} (:468-538). */
    @Override
    public void setupAnim(final CaveFisher e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        final float pi4 = 1.570795f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        setYRot(this.lfLeg, newangle);
        setYRot(this.rfLeg, -newangle);
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 1.0f * pi4) * 3.1415927f * 0.12f * f1;
        setYRot(this.lmLeg, newangle);
        setYRot(this.rmLeg, -newangle);
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.12f * f1;
        setYRot(this.lbLeg, newangle);
        setYRot(this.rbLeg, -newangle);
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
            this.doRightClaw(newangle);
        } else {
            this.doLeftClaw(0.0f);
            this.doRightClaw(0.0f);
        }
        e.setRenderInfo(r);
    }

    /** {@code doLeftClaw} (:626-635). */
    private void doLeftClaw(final float angle) {
        this.leftArmSeg1.xRot = Math.abs(angle);
        this.leftArmSeg2.xRot = Math.abs(angle);
        this.leftArmSeg3.xRot = Math.abs(angle);
        this.leftArmSeg4.xRot = Math.abs(angle);
        this.leftArmSeg5.xRot = Math.abs(angle);
        this.leftClawBase.xRot = Math.abs(angle);
        this.leftClawTop.xRot = Math.abs(angle) - 0.54f;
        this.leftClawLow.xRot = Math.abs(angle) + 0.35f;
    }

    /** {@code doRightClaw} (:637-646). */
    private void doRightClaw(final float angle) {
        this.rightArmSeg1.xRot = Math.abs(angle);
        this.rightArmSeg2.xRot = Math.abs(angle);
        this.rightArmSeg3.xRot = Math.abs(angle);
        this.rightArmSeg4.xRot = Math.abs(angle);
        this.rightArmSeg5.xRot = Math.abs(angle);
        this.rightClawBase.xRot = Math.abs(angle);
        this.rightClawTop.xRot = Math.abs(angle) - 0.54f;
        this.rightClawLow.xRot = Math.abs(angle) + 0.35f;
    }

    /** {@code render()} (:539-613). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
