package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GodzillaGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.boss.mobzilla.Godzilla;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelGodzilla} (ModelGodzilla.java:7-927): 71 boxes on a 1024x1024 texture, geometry
 * {@link GodzillaGeometry}. {@code wingspeed} is the constructor argument (:83-84), 0.2 (ClientProxyOreSpawn, manifest
 * {@code model_args}), so every animation runs at a fifth of its written frequency. No GL calls, no blend group.
 *
 * <p>{@code render()} (:444-880) sets every angle and pivot before the first draw and then draws the 71 parts in creation
 * order; the writes are {@link #setupAnim}, the draws {@link #renderToBuffer}. Legs with toe lift in the swing phase, the
 * tail wave ({@code doTail}, :892-926), head yaw and pitch with the lower jaw trailing 11 px, and jaw snaps and arm swings
 * triggered once per sine period by random bits kept in the entity's {@link RenderInfo} ({@code ri1}, {@code ri2},
 * {@code rf1}, {@code rf2}).
 *
 * <p>PORT (R8): {@code resetPose()} at the start replaces the field values 1.7.10 left standing between frames; every
 * animated field is overwritten before the draws anyway. The random draws use the entity's (client) level random, as
 * {@code e.worldObj.rand} did.
 */
public class GodzillaModel extends EntityModel<Godzilla> {

    /** Register with {@code GodzillaGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "mobzilla"), "main");

    private final float wingspeed;
    private final ModelPart LToe1;
    private final ModelPart LToe3;
    private final ModelPart LToe2;
    private final ModelPart LToe9;
    private final ModelPart LToe8;
    private final ModelPart LToe7;
    private final ModelPart LToe6;
    private final ModelPart LToe5;
    private final ModelPart LToe4;
    private final ModelPart RToe9;
    private final ModelPart RToe6;
    private final ModelPart RToe5;
    private final ModelPart RToe2;
    private final ModelPart RToe1;
    private final ModelPart RToe4;
    private final ModelPart RToe7;
    private final ModelPart RToe8;
    private final ModelPart RToe3;
    private final ModelPart LThigh;
    private final ModelPart LLowerLeg;
    private final ModelPart LUpperLeg;
    private final ModelPart TailTip;
    private final ModelPart RLegLower;
    private final ModelPart RLegUpper;
    private final ModelPart RThigh;
    private final ModelPart LowerJaw;
    private final ModelPart TailBase;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Tail4;
    private final ModelPart Tail5;
    private final ModelPart Tail6;
    private final ModelPart Tail7;
    private final ModelPart RLowerArm;
    private final ModelPart TopJaw;
    private final ModelPart Head;
    private final ModelPart RThumbTip;
    private final ModelPart RUpperArm;
    private final ModelPart RHand;
    private final ModelPart RThumbBase;
    private final ModelPart R3rdFingerTip;
    private final ModelPart R3rdFingerBase;
    private final ModelPart RIndexTip;
    private final ModelPart RIndexBase;
    private final ModelPart LUpperArm;
    private final ModelPart LLowerArm;
    private final ModelPart LIndexBase;
    private final ModelPart LIndexTip;
    private final ModelPart LHand;
    private final ModelPart LThumbBase;
    private final ModelPart LThumbTip;
    private final ModelPart L3rdFingerTip;
    private final ModelPart L3rdFingerBase;
    private final ModelPart Lspike5;
    private final ModelPart Rspike5;
    private final ModelPart Spike6;
    private final ModelPart Spikes7;
    /** All 71 parts in creation order, which is also the draw order of {@code render()} (:809-879). */
    private final ModelPart[] allParts;

    /** {@code ModelGodzilla(float f1)} (:82-442). */
    public GodzillaModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.allParts = new ModelPart[GodzillaGeometry.PARTS.length];
        for (int i = 0; i < GodzillaGeometry.PARTS.length; ++i) {
            this.allParts[i] = root.getChild(GodzillaGeometry.PARTS[i]);
        }
        this.LToe1 = root.getChild(GodzillaGeometry.LTOE1);
        this.LToe3 = root.getChild(GodzillaGeometry.LTOE3);
        this.LToe2 = root.getChild(GodzillaGeometry.LTOE2);
        this.LToe9 = root.getChild(GodzillaGeometry.LTOE9);
        this.LToe8 = root.getChild(GodzillaGeometry.LTOE8);
        this.LToe7 = root.getChild(GodzillaGeometry.LTOE7);
        this.LToe6 = root.getChild(GodzillaGeometry.LTOE6);
        this.LToe5 = root.getChild(GodzillaGeometry.LTOE5);
        this.LToe4 = root.getChild(GodzillaGeometry.LTOE4);
        this.RToe9 = root.getChild(GodzillaGeometry.RTOE9);
        this.RToe6 = root.getChild(GodzillaGeometry.RTOE6);
        this.RToe5 = root.getChild(GodzillaGeometry.RTOE5);
        this.RToe2 = root.getChild(GodzillaGeometry.RTOE2);
        this.RToe1 = root.getChild(GodzillaGeometry.RTOE1);
        this.RToe4 = root.getChild(GodzillaGeometry.RTOE4);
        this.RToe7 = root.getChild(GodzillaGeometry.RTOE7);
        this.RToe8 = root.getChild(GodzillaGeometry.RTOE8);
        this.RToe3 = root.getChild(GodzillaGeometry.RTOE3);
        this.LThigh = root.getChild(GodzillaGeometry.LTHIGH);
        this.LLowerLeg = root.getChild(GodzillaGeometry.LLOWER_LEG);
        this.LUpperLeg = root.getChild(GodzillaGeometry.LUPPER_LEG);
        this.TailTip = root.getChild(GodzillaGeometry.TAIL_TIP);
        this.RLegLower = root.getChild(GodzillaGeometry.RLEG_LOWER);
        this.RLegUpper = root.getChild(GodzillaGeometry.RLEG_UPPER);
        this.RThigh = root.getChild(GodzillaGeometry.RTHIGH);
        this.LowerJaw = root.getChild(GodzillaGeometry.LOWER_JAW);
        this.TailBase = root.getChild(GodzillaGeometry.TAIL_BASE);
        this.Tail2 = root.getChild(GodzillaGeometry.TAIL2);
        this.Tail3 = root.getChild(GodzillaGeometry.TAIL3);
        this.Tail4 = root.getChild(GodzillaGeometry.TAIL4);
        this.Tail5 = root.getChild(GodzillaGeometry.TAIL5);
        this.Tail6 = root.getChild(GodzillaGeometry.TAIL6);
        this.Tail7 = root.getChild(GodzillaGeometry.TAIL7);
        this.RLowerArm = root.getChild(GodzillaGeometry.RLOWER_ARM);
        this.TopJaw = root.getChild(GodzillaGeometry.TOP_JAW);
        this.Head = root.getChild(GodzillaGeometry.HEAD);
        this.RThumbTip = root.getChild(GodzillaGeometry.RTHUMB_TIP);
        this.RUpperArm = root.getChild(GodzillaGeometry.RUPPER_ARM);
        this.RHand = root.getChild(GodzillaGeometry.RHAND);
        this.RThumbBase = root.getChild(GodzillaGeometry.RTHUMB_BASE);
        this.R3rdFingerTip = root.getChild(GodzillaGeometry.R3RD_FINGER_TIP);
        this.R3rdFingerBase = root.getChild(GodzillaGeometry.R3RD_FINGER_BASE);
        this.RIndexTip = root.getChild(GodzillaGeometry.RINDEX_TIP);
        this.RIndexBase = root.getChild(GodzillaGeometry.RINDEX_BASE);
        this.LUpperArm = root.getChild(GodzillaGeometry.LUPPER_ARM);
        this.LLowerArm = root.getChild(GodzillaGeometry.LLOWER_ARM);
        this.LIndexBase = root.getChild(GodzillaGeometry.LINDEX_BASE);
        this.LIndexTip = root.getChild(GodzillaGeometry.LINDEX_TIP);
        this.LHand = root.getChild(GodzillaGeometry.LHAND);
        this.LThumbBase = root.getChild(GodzillaGeometry.LTHUMB_BASE);
        this.LThumbTip = root.getChild(GodzillaGeometry.LTHUMB_TIP);
        this.L3rdFingerTip = root.getChild(GodzillaGeometry.L3RD_FINGER_TIP);
        this.L3rdFingerBase = root.getChild(GodzillaGeometry.L3RD_FINGER_BASE);
        this.Lspike5 = root.getChild(GodzillaGeometry.LSPIKE5);
        this.Rspike5 = root.getChild(GodzillaGeometry.RSPIKE5);
        this.Spike6 = root.getChild(GodzillaGeometry.SPIKE6);
        this.Spikes7 = root.getChild(GodzillaGeometry.SPIKES7);
    }

    /**
     * The writes of {@code render()} (:444-808). {@code f1} limb swing amount, {@code f2} age in ticks, {@code f3} head yaw,
     * {@code f4} head pitch.
     */
    @Override
    public void setupAnim(final Godzilla e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.allParts) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        final float pscale = 1.0f;
        final float pi4 = 0.7853982f;
        final float clawZ = 6.0f;
        final float clawY = 16.0f;
        final float clawZamp = 35.0f * pscale;
        final float clawYamp = 18.0f * pscale;
        r = e.getRenderInfo();
        float t1 = 0.0f;
        float t2 = 0.0f;
        if (f1 > 0.001) {
            newangle = Mth.cos(f2 * 0.75f * this.wingspeed / pscale);
            newangle2 = Mth.cos(f2 * 0.75f * this.wingspeed / pscale + pi4);
            t1 = Mth.sin(f2 * 0.75f * this.wingspeed / pscale);
        } else {
            newangle2 = (newangle = 0.0f);
            t1 = 0.0f;
            t2 = 0.0f;
        }
        if (t1 > 0.0f) {
            t2 = t1 * clawYamp * f1;
            this.LToe1.y = clawY - t2;
        } else {
            this.LToe1.y = clawY;
        }
        this.LToe1.z = clawZ + clawZamp * newangle * f1;
        final float rotationPointZ = this.LToe1.z;
        this.LToe9.z = rotationPointZ;
        this.LToe8.z = rotationPointZ;
        this.LToe7.z = rotationPointZ;
        this.LToe6.z = rotationPointZ;
        this.LToe5.z = rotationPointZ;
        this.LToe4.z = rotationPointZ;
        this.LToe3.z = rotationPointZ;
        this.LToe2.z = rotationPointZ;
        final float rotationPointY = this.LToe1.y;
        this.LToe9.y = rotationPointY;
        this.LToe8.y = rotationPointY;
        this.LToe7.y = rotationPointY;
        this.LToe6.y = rotationPointY;
        this.LToe5.y = rotationPointY;
        this.LToe4.y = rotationPointY;
        this.LToe3.y = rotationPointY;
        this.LToe2.y = rotationPointY;
        this.LLowerLeg.z = this.LToe1.z;
        this.LLowerLeg.y = this.LToe1.y;
        this.LLowerLeg.xRot = 0.22f + newangle * 3.1415927f * 0.09f * f1;
        this.LUpperLeg.xRot = -0.17f + newangle2 * 3.1415927f * 0.15f * f1;
        this.LUpperLeg.y = this.LLowerLeg.y - (float) Math.cos(this.LLowerLeg.xRot) * 55.0f;
        this.LUpperLeg.z = this.LLowerLeg.z - (float) Math.sin(this.LLowerLeg.xRot) * 55.0f;
        this.LThigh.xRot = -0.558f + newangle2 * 3.1415927f * 0.1f * f1;
        this.LThigh.z = 2.0f + clawZamp * newangle * f1 / 4.0f;
        t1 = 0.0f;
        t2 = 0.0f;
        if (f1 > 0.001) {
            newangle = Mth.cos(f2 * 0.75f * this.wingspeed / pscale + pi4 * 4.0f);
            newangle2 = Mth.cos(f2 * 0.75f * this.wingspeed / pscale + pi4 * 5.0f);
            t1 = Mth.sin(f2 * 0.75f * this.wingspeed / pscale + pi4 * 4.0f);
        } else {
            newangle = 0.0f;
            t1 = 0.0f;
            t2 = 0.0f;
        }
        if (t1 > 0.0f) {
            t2 = t1 * clawYamp * f1;
            this.RToe1.y = clawY - t2;
        } else {
            this.RToe1.y = clawY;
        }
        this.RToe1.z = clawZ + clawZamp * newangle * f1;
        final float rotationPointZ2 = this.RToe1.z;
        this.RToe9.z = rotationPointZ2;
        this.RToe8.z = rotationPointZ2;
        this.RToe7.z = rotationPointZ2;
        this.RToe6.z = rotationPointZ2;
        this.RToe5.z = rotationPointZ2;
        this.RToe4.z = rotationPointZ2;
        this.RToe3.z = rotationPointZ2;
        this.RToe2.z = rotationPointZ2;
        final float rotationPointY2 = this.RToe1.y;
        this.RToe9.y = rotationPointY2;
        this.RToe8.y = rotationPointY2;
        this.RToe7.y = rotationPointY2;
        this.RToe6.y = rotationPointY2;
        this.RToe5.y = rotationPointY2;
        this.RToe4.y = rotationPointY2;
        this.RToe3.y = rotationPointY2;
        this.RToe2.y = rotationPointY2;
        this.RLegLower.z = this.RToe1.z;
        this.RLegLower.y = this.RToe1.y;
        this.RLegLower.xRot = 0.22f + newangle * 3.1415927f * 0.09f * f1;
        this.RLegUpper.xRot = -0.17f + newangle2 * 3.1415927f * 0.15f * f1;
        this.RLegUpper.y = this.RLegLower.y - (float) Math.cos(this.RLegLower.xRot) * 55.0f;
        this.RLegUpper.z = this.RLegLower.z - (float) Math.sin(this.RLegLower.xRot) * 55.0f;
        this.RThigh.xRot = -0.558f + newangle2 * 3.1415927f * 0.1f * f1;
        this.RThigh.z = 2.0f + clawZamp * newangle * f1 / 4.0f;
        final float rotateAngleX = 0.0f;
        this.LToe1.xRot = rotateAngleX;
        this.LToe9.xRot = rotateAngleX;
        this.LToe8.xRot = rotateAngleX;
        this.LToe7.xRot = rotateAngleX;
        this.LToe6.xRot = rotateAngleX;
        this.LToe5.xRot = rotateAngleX;
        this.LToe4.xRot = rotateAngleX;
        this.LToe3.xRot = rotateAngleX;
        this.LToe2.xRot = rotateAngleX;
        final float rotateAngleX2 = 0.0f;
        this.RToe1.xRot = rotateAngleX2;
        this.RToe9.xRot = rotateAngleX2;
        this.RToe8.xRot = rotateAngleX2;
        this.RToe7.xRot = rotateAngleX2;
        this.RToe6.xRot = rotateAngleX2;
        this.RToe5.xRot = rotateAngleX2;
        this.RToe4.xRot = rotateAngleX2;
        this.RToe3.xRot = rotateAngleX2;
        this.RToe2.xRot = rotateAngleX2;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * this.wingspeed * 1.75f) * 3.1415927f * 0.2f;
        } else {
            newangle = Mth.cos(f2 * this.wingspeed * 0.75f) * 3.1415927f * 0.05f;
        }
        this.doTail(newangle);
        newangle = (float) Math.toRadians(f3) * 0.55f;
        this.Head.yRot = newangle;
        this.TopJaw.yRot = newangle;
        this.LowerJaw.yRot = newangle;
        this.LowerJaw.z = this.Head.z - (float) Math.cos(this.Head.yRot) * 11.0f;
        this.LowerJaw.x = this.Head.x - (float) Math.sin(this.Head.yRot) * 11.0f;
        final float n = (float) Math.toRadians(f4);
        this.Head.xRot = n;
        this.TopJaw.xRot = n;
        newangle = Mth.cos(f2 * this.wingspeed * 1.5f) * 3.1415927f * 0.12f;
        float newrf1 = f2 * 1.5f * this.wingspeed % 6.2831855f;
        newrf1 = Math.abs(newrf1);
        if (newrf1 < r.rf2) {
            r.ri2 = 0;
            if (e.getAttacking() == 0) {
                if (e.level().random.nextInt(20) == 1) {
                    r.ri2 |= 0x1;
                }
            } else if (e.level().random.nextInt(2) == 1) {
                r.ri2 |= 0x1;
            }
        }
        r.rf2 = newrf1;
        if ((r.ri2 & 0x1) == 0x0) {
            newangle = 0.0f;
        }
        this.LowerJaw.xRot = 0.52f + newangle + this.TopJaw.xRot;
        newangle2 = (newangle = Mth.sin(f2 * this.wingspeed * 1.75f) * 3.1415927f * 0.16f);
        newrf1 = f2 * 1.75f * this.wingspeed % 6.2831855f;
        newrf1 = Math.abs(newrf1);
        if (newrf1 < r.rf1) {
            r.ri1 = 0;
            if (e.getAttacking() == 0) {
                if (e.level().random.nextInt(20) == 1) {
                    r.ri1 |= 0x1;
                }
                if (e.level().random.nextInt(20) == 1) {
                    r.ri1 |= 0x2;
                }
            } else {
                if (e.level().random.nextInt(2) == 1) {
                    r.ri1 |= 0x1;
                }
                if (e.level().random.nextInt(2) == 1) {
                    r.ri1 |= 0x2;
                }
            }
        }
        r.rf1 = newrf1;
        if ((r.ri1 & 0x1) == 0x0) {
            newangle = 0.0f;
        }
        if ((r.ri1 & 0x2) == 0x0) {
            newangle2 = 0.0f;
        }
        this.LUpperArm.yRot = 0.65f + newangle;
        this.LLowerArm.yRot = 0.78f + newangle * 3.0f / 2.0f;
        this.LLowerArm.z = this.LUpperArm.z - (float) Math.sin(this.LUpperArm.yRot) * 50.0f;
        this.LLowerArm.x = this.LUpperArm.x + (float) Math.cos(this.LUpperArm.yRot) * 50.0f;
        this.LLowerArm.y = this.LUpperArm.y - (float) Math.sin(this.LUpperArm.yRot) * 10.0f + 18.0f;
        this.LHand.z = this.LLowerArm.z - (float) Math.sin(this.LLowerArm.yRot) * 45.0f;
        this.LHand.x = this.LLowerArm.x + (float) Math.cos(this.LLowerArm.yRot) * 45.0f;
        this.LHand.y = this.LLowerArm.y - (float) Math.sin(this.LLowerArm.yRot) * 10.0f + 15.0f;
        final float rotationPointZ3 = this.LHand.z;
        this.L3rdFingerBase.z = rotationPointZ3;
        this.LThumbBase.z = rotationPointZ3;
        this.LIndexBase.z = rotationPointZ3;
        final float rotationPointZ4 = this.LHand.z;
        this.L3rdFingerTip.z = rotationPointZ4;
        this.LThumbTip.z = rotationPointZ4;
        this.LIndexTip.z = rotationPointZ4;
        final float rotationPointY3 = this.LHand.y;
        this.L3rdFingerBase.y = rotationPointY3;
        this.LThumbBase.y = rotationPointY3;
        this.LIndexBase.y = rotationPointY3;
        final float rotationPointY4 = this.LHand.y;
        this.L3rdFingerTip.y = rotationPointY4;
        this.LThumbTip.y = rotationPointY4;
        this.LIndexTip.y = rotationPointY4;
        final float rotationPointX = this.LHand.x;
        this.L3rdFingerBase.x = rotationPointX;
        this.LThumbBase.x = rotationPointX;
        this.LIndexBase.x = rotationPointX;
        final float rotationPointX2 = this.LHand.x;
        this.L3rdFingerTip.x = rotationPointX2;
        this.LThumbTip.x = rotationPointX2;
        this.LIndexTip.x = rotationPointX2;
        this.LHand.yRot = 1.308f + newangle * 2.0f;
        this.LIndexBase.yRot = -0.139f + newangle * 2.0f;
        this.LIndexTip.yRot = -0.034f + newangle * 2.0f;
        this.LThumbBase.yRot = 0.261f + newangle;
        this.LThumbTip.yRot = 0.139f + newangle;
        this.L3rdFingerBase.yRot = -0.471f + newangle * 3.0f;
        this.L3rdFingerTip.yRot = -0.331f + newangle * 3.0f;
        this.RUpperArm.yRot = -0.65f - newangle2;
        this.RLowerArm.yRot = -0.78f - newangle2 * 3.0f / 2.0f;
        this.RLowerArm.z = this.RUpperArm.z + (float) Math.sin(this.RUpperArm.yRot) * 50.0f;
        this.RLowerArm.x = this.RUpperArm.x - (float) Math.cos(this.RUpperArm.yRot) * 50.0f;
        this.RLowerArm.y = this.RUpperArm.y + (float) Math.sin(this.RUpperArm.yRot) * 10.0f + 18.0f;
        this.RHand.z = this.RLowerArm.z + (float) Math.sin(this.RLowerArm.yRot) * 45.0f;
        this.RHand.x = this.RLowerArm.x - (float) Math.cos(this.RLowerArm.yRot) * 45.0f;
        this.RHand.y = this.RLowerArm.y + (float) Math.sin(this.RLowerArm.yRot) * 10.0f + 15.0f;
        final float rotationPointZ5 = this.RHand.z;
        this.R3rdFingerBase.z = rotationPointZ5;
        this.RThumbBase.z = rotationPointZ5;
        this.RIndexBase.z = rotationPointZ5;
        final float rotationPointZ6 = this.RHand.z;
        this.R3rdFingerTip.z = rotationPointZ6;
        this.RThumbTip.z = rotationPointZ6;
        this.RIndexTip.z = rotationPointZ6;
        final float rotationPointY5 = this.RHand.y;
        this.R3rdFingerBase.y = rotationPointY5;
        this.RThumbBase.y = rotationPointY5;
        this.RIndexBase.y = rotationPointY5;
        final float rotationPointY6 = this.RHand.y;
        this.R3rdFingerTip.y = rotationPointY6;
        this.RThumbTip.y = rotationPointY6;
        this.RIndexTip.y = rotationPointY6;
        final float rotationPointX3 = this.RHand.x;
        this.R3rdFingerBase.x = rotationPointX3;
        this.RThumbBase.x = rotationPointX3;
        this.RIndexBase.x = rotationPointX3;
        final float rotationPointX4 = this.RHand.x;
        this.R3rdFingerTip.x = rotationPointX4;
        this.RThumbTip.x = rotationPointX4;
        this.RIndexTip.x = rotationPointX4;
        this.RHand.yRot = -2.0f - newangle2 * 2.0f;
        this.RIndexBase.yRot = 0.157f - newangle2 * 2.0f;
        this.RIndexTip.yRot = 0.174f - newangle2 * 2.0f;
        this.RThumbBase.yRot = -0.104f - newangle2;
        this.RThumbTip.yRot = 0.001f - newangle2;
        this.R3rdFingerTip.yRot = 0.68f - newangle2 * 3.0f;
        this.R3rdFingerBase.yRot = 0.645f - newangle2 * 3.0f;
        e.setRenderInfo(r);
    }

    /** {@code doTail} (:892-926): a Y wave down the seven tail segments, each pivot chained to the previous one. */
    private void doTail(final float angle) {
        this.TailBase.yRot = angle * 0.25f;
        final float rotateAngleY = this.TailBase.yRot;
        this.Rspike5.yRot = rotateAngleY;
        this.Lspike5.yRot = rotateAngleY;
        this.Tail2.yRot = angle * 0.5f;
        this.Tail2.z = this.TailBase.z + (float) Math.cos(this.TailBase.yRot) * 25.0f;
        this.Tail2.x = this.TailBase.x + (float) Math.sin(this.TailBase.yRot) * 25.0f;
        this.Spike6.yRot = this.Tail2.yRot;
        this.Spike6.z = this.Tail2.z;
        this.Spike6.x = this.Tail2.x;
        this.Tail3.yRot = angle * 0.75f;
        this.Tail3.z = this.Tail2.z + (float) Math.cos(this.Tail2.yRot) * 20.0f;
        this.Tail3.x = this.Tail2.x + (float) Math.sin(this.Tail2.yRot) * 20.0f;
        this.Spikes7.yRot = this.Tail3.yRot;
        this.Spikes7.z = this.Tail3.z;
        this.Spikes7.x = this.Tail3.x;
        this.Tail4.yRot = angle * 1.25f;
        this.Tail4.z = this.Tail3.z + (float) Math.cos(this.Tail3.yRot) * 20.0f;
        this.Tail4.x = this.Tail3.x + (float) Math.sin(this.Tail3.yRot) * 20.0f;
        this.Tail5.yRot = angle * 1.5f;
        this.Tail5.z = this.Tail4.z + (float) Math.cos(this.Tail4.yRot) * 25.0f;
        this.Tail5.x = this.Tail4.x + (float) Math.sin(this.Tail4.yRot) * 25.0f;
        this.Tail6.yRot = angle * 1.75f;
        this.Tail6.z = this.Tail5.z + (float) Math.cos(this.Tail5.yRot) * 27.0f;
        this.Tail6.x = this.Tail5.x + (float) Math.sin(this.Tail5.yRot) * 27.0f;
        this.Tail7.yRot = angle * 2.0f;
        this.Tail7.z = this.Tail6.z + (float) Math.cos(this.Tail6.yRot) * 28.0f;
        this.Tail7.x = this.Tail6.x + (float) Math.sin(this.Tail6.yRot) * 28.0f;
        this.TailTip.yRot = angle * 2.25f;
        this.TailTip.z = this.Tail7.z + (float) Math.cos(this.Tail7.yRot) * 18.0f;
        this.TailTip.x = this.Tail7.x + (float) Math.sin(this.Tail7.yRot) * 18.0f;
    }

    /** The draws of {@code render()} (:809-879), in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.allParts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
