package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.TrooperBugGeometry;
import com.swbr.orespawn.entity.arthropod.TrooperBug;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelTrooperBug} (ModelTrooperBug.java:7-1288), the Jumpy Bug: 134 boxes, 512x256
 * texture, geometry baked from the generated {@link TrooperBugGeometry}. The animation is {@code render()} (:824-906)
 * with the four leg helpers (:1053-1287).
 *
 * <p>Antennae, the four arms, head ridges and the jaw move faster while the bug attacks (DataWatcher 20). The front leg
 * helpers ({@code doRightFrontLeg} leg1 base +1.2, {@code doLeftFrontLeg} leg2 base -1.2) share their statements and
 * are {@link #doFrontLeg}; the rear helpers ({@code doRightRearLeg} leg4 base -1.2, {@code doLeftRearLeg} leg3 base
 * +1.2) mirror the angles and offsets and are {@link #doRearLeg}. Aliased decompiled locals are spelled out.
 */
public class TrooperBugModel extends EntityModel<TrooperBug> {

    /** Register with {@code TrooperBugGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "jumpy_bug"), "main");

    /** {@code ModelTrooperBug(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 0.22 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:907-1040), which is the creation order. */
    private final ModelPart[] parts;
    /** {@code legNpart1, part1b, elbow, part2, part2b, part2c, part3, part3b, part3c, part3d}. */
    private final ModelPart[] leg1;
    private final ModelPart[] leg2;
    private final ModelPart[] leg3;
    private final ModelPart[] leg4;
    private final ModelPart antenna1part2;
    private final ModelPart antenna2part2;
    private final ModelPart arm1part3;
    private final ModelPart arm1part3b;
    private final ModelPart arm2part3;
    private final ModelPart arm2part3b;
    private final ModelPart arm3part1;
    private final ModelPart arm3part1b;
    private final ModelPart arm3part1c;
    private final ModelPart arm4part1;
    private final ModelPart arm4part1b;
    private final ModelPart arm4part1c;
    private final ModelPart headleftridge;
    private final ModelPart headrightridge;
    private final ModelPart upperjawridgeleft;
    private final ModelPart upperjawridgeright;
    private final ModelPart jawbase;
    private final ModelPart jawbase2;
    private final ModelPart jawbase3;
    private final ModelPart jawbase4;
    private final ModelPart jawbase5;
    private final ModelPart jawbase6;
    private final ModelPart jawbase7;
    private final ModelPart jawbase8;
    private final ModelPart jawbase9;
    private final ModelPart jawend;
    private final ModelPart jawleft;
    private final ModelPart jawright;

    public TrooperBugModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[TrooperBugGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(TrooperBugGeometry.PARTS[i]);
        }
        this.leg1 = children(root, TrooperBugGeometry.LEG1PART1, TrooperBugGeometry.LEG1PART1B, TrooperBugGeometry.LEG1ELBOW,
                TrooperBugGeometry.LEG1PART2, TrooperBugGeometry.LEG1PART2B, TrooperBugGeometry.LEG1PART2C,
                TrooperBugGeometry.LEG1PART3, TrooperBugGeometry.LEG1PART3B, TrooperBugGeometry.LEG1PART3C, TrooperBugGeometry.LEG1PART3D);
        this.leg2 = children(root, TrooperBugGeometry.LEG2PART1, TrooperBugGeometry.LEG2PART1B, TrooperBugGeometry.LEG2ELBOW,
                TrooperBugGeometry.LEG2PART2, TrooperBugGeometry.LEG2PART2B, TrooperBugGeometry.LEG2PART2C,
                TrooperBugGeometry.LEG2PART3, TrooperBugGeometry.LEG2PART3B, TrooperBugGeometry.LEG2PART3C, TrooperBugGeometry.LEG2PART3D);
        this.leg3 = children(root, TrooperBugGeometry.LEG3PART1, TrooperBugGeometry.LEG3PART1B, TrooperBugGeometry.LEG3ELBOW,
                TrooperBugGeometry.LEG3PART2, TrooperBugGeometry.LEG3PART2B, TrooperBugGeometry.LEG3PART2C,
                TrooperBugGeometry.LEG3PART3, TrooperBugGeometry.LEG3PART3B, TrooperBugGeometry.LEG3PART3C, TrooperBugGeometry.LEG3PART3D);
        this.leg4 = children(root, TrooperBugGeometry.LEG4PART1, TrooperBugGeometry.LEG4PART1B, TrooperBugGeometry.LEG4ELBOW,
                TrooperBugGeometry.LEG4PART2, TrooperBugGeometry.LEG4PART2B, TrooperBugGeometry.LEG4PART2C,
                TrooperBugGeometry.LEG4PART3, TrooperBugGeometry.LEG4PART3B, TrooperBugGeometry.LEG4PART3C, TrooperBugGeometry.LEG4PART3D);
        this.antenna1part2 = root.getChild(TrooperBugGeometry.ANTENNA1PART2);
        this.antenna2part2 = root.getChild(TrooperBugGeometry.ANTENNA2PART2);
        this.arm1part3 = root.getChild(TrooperBugGeometry.ARM1PART3);
        this.arm1part3b = root.getChild(TrooperBugGeometry.ARM1PART3B);
        this.arm2part3 = root.getChild(TrooperBugGeometry.ARM2PART3);
        this.arm2part3b = root.getChild(TrooperBugGeometry.ARM2PART3B);
        this.arm3part1 = root.getChild(TrooperBugGeometry.ARM3PART1);
        this.arm3part1b = root.getChild(TrooperBugGeometry.ARM3PART1B);
        this.arm3part1c = root.getChild(TrooperBugGeometry.ARM3PART1C);
        this.arm4part1 = root.getChild(TrooperBugGeometry.ARM4PART1);
        this.arm4part1b = root.getChild(TrooperBugGeometry.ARM4PART1B);
        this.arm4part1c = root.getChild(TrooperBugGeometry.ARM4PART1C);
        this.headleftridge = root.getChild(TrooperBugGeometry.HEADLEFTRIDGE);
        this.headrightridge = root.getChild(TrooperBugGeometry.HEADRIGHTRIDGE);
        this.upperjawridgeleft = root.getChild(TrooperBugGeometry.UPPERJAWRIDGELEFT);
        this.upperjawridgeright = root.getChild(TrooperBugGeometry.UPPERJAWRIDGERIGHT);
        this.jawbase = root.getChild(TrooperBugGeometry.JAWBASE);
        this.jawbase2 = root.getChild(TrooperBugGeometry.JAWBASE2);
        this.jawbase3 = root.getChild(TrooperBugGeometry.JAWBASE3);
        this.jawbase4 = root.getChild(TrooperBugGeometry.JAWBASE4);
        this.jawbase5 = root.getChild(TrooperBugGeometry.JAWBASE5);
        this.jawbase6 = root.getChild(TrooperBugGeometry.JAWBASE6);
        this.jawbase7 = root.getChild(TrooperBugGeometry.JAWBASE7);
        this.jawbase8 = root.getChild(TrooperBugGeometry.JAWBASE8);
        this.jawbase9 = root.getChild(TrooperBugGeometry.JAWBASE9);
        this.jawend = root.getChild(TrooperBugGeometry.JAWEND);
        this.jawleft = root.getChild(TrooperBugGeometry.JAWLEFT);
        this.jawright = root.getChild(TrooperBugGeometry.JAWRIGHT);
    }

    private static ModelPart[] children(final ModelPart root, final String... names) {
        final ModelPart[] out = new ModelPart[names.length];
        for (int i = 0; i < names.length; ++i) {
            out[i] = root.getChild(names[i]);
        }
        return out;
    }

    /** The writes of {@code render()} (:824-906). */
    @Override
    public void setupAnim(final TrooperBug e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.4f * this.wingspeed) * 3.1415927f * 0.05f;
        } else {
            newangle = Mth.cos(f2 * 1.4f * this.wingspeed) * 3.1415927f * 0.1f;
        }
        this.antenna2part2.yRot = 0.78f + newangle;
        this.antenna1part2.yRot = -0.78f - newangle;
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.05f;
        } else {
            newangle = Mth.cos(f2 * 2.5f * this.wingspeed) * 3.1415927f * 0.15f;
        }
        this.arm4part1.yRot = newangle;
        this.arm4part1b.yRot = newangle;
        this.arm4part1c.yRot = newangle;
        this.arm3part1.yRot = -newangle;
        this.arm3part1b.yRot = -newangle;
        this.arm3part1c.yRot = -newangle;
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.05f;
        } else {
            newangle = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.2f;
        }
        this.arm1part3.xRot = 1.56f + newangle;
        this.arm1part3b.xRot = 1.56f + newangle;
        this.arm2part3.xRot = 1.56f - newangle;
        this.arm2part3b.xRot = 1.56f - newangle;
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.1f * this.wingspeed) * 3.1415927f * 0.02f;
        } else {
            newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.1f;
        }
        this.headleftridge.yRot = -0.25f + newangle;
        this.headrightridge.yRot = 0.25f - newangle;
        this.upperjawridgeleft.yRot = -0.372f + newangle;
        this.upperjawridgeright.yRot = 0.372f - newangle;
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.015f;
        } else {
            newangle = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.1f;
        }
        this.jawbase.xRot = 0.22f + newangle;
        this.jawbase2.xRot = 0.22f + newangle;
        this.jawbase3.xRot = 0.22f + newangle;
        this.jawbase4.xRot = 0.22f + newangle;
        this.jawbase5.xRot = 0.22f + newangle;
        this.jawbase6.xRot = 0.22f + newangle;
        this.jawbase7.xRot = 0.22f + newangle;
        this.jawbase8.xRot = 0.2146f + newangle;
        this.jawbase8.yRot = 0.1487f + newangle;
        this.jawbase9.xRot = 0.1f + newangle;
        this.jawbase9.yRot = 0.07f + newangle;
        this.jawend.xRot = 0.22f + newangle;
        this.jawleft.xRot = 0.22f + newangle;
        this.jawright.xRot = 0.22f + newangle;
        newangle = Mth.sin(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.sin((f2 + 0.1f) * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1);
        }
        // doLeftFrontLeg (leg2), doLeftRearLeg (leg3).
        doFrontLeg(this.leg2, -1.2f, newangle, upangle);
        doRearLeg(this.leg3, 1.2f, -newangle, upangle);
        newangle = Mth.sin((float) (f2 * 2.0f * this.wingspeed + 3.141592653589793)) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.sin((float) ((f2 + 0.1f) * 2.0f * this.wingspeed + 3.141592653589793)) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos((float) (f2 * 2.0f * this.wingspeed + 3.141592653589793)) * 3.1415927f * 0.12f * f1);
        }
        // doRightFrontLeg (leg1), doRightRearLeg (leg4).
        doFrontLeg(this.leg1, 1.2f, -newangle, upangle);
        doRearLeg(this.leg4, -1.2f, newangle, upangle);
    }

    /**
     * {@code doRightFrontLeg} / {@code doLeftFrontLeg} (:1053-1169).
     *
     * @param base 1.2 for leg1 (right front), -1.2 for leg2 (left front)
     */
    private static void doFrontLeg(final ModelPart[] l, final float base, final float angle, final float upangle) {
        final ModelPart part1 = l[0];
        final ModelPart part1b = l[1];
        final ModelPart elbow = l[2];
        final ModelPart part2 = l[3];
        final ModelPart part2b = l[4];
        final ModelPart part2c = l[5];
        final ModelPart part3 = l[6];
        final ModelPart part3b = l[7];
        final ModelPart part3c = l[8];
        final ModelPart part3d = l[9];
        part1.yRot = base + angle;
        part1b.yRot = part1.yRot;
        elbow.yRot = part1.yRot;
        part2.yRot = part1.yRot;
        part2b.yRot = part1.yRot;
        part2c.yRot = part1.yRot;
        part3.yRot = part1.yRot;
        part3b.yRot = part1.yRot;
        part3c.yRot = part1.yRot;
        part3d.yRot = part1.yRot;
        part1.xRot = 1.115f + upangle;
        part1b.xRot = 1.078f + upangle;
        elbow.xRot = part1.xRot;
        float dist = 26.0f;
        dist *= (float) Math.cos(part1.xRot);
        final float rotationPointZ = (float) (part1.z - Math.cos(part1.yRot) * dist);
        part2c.z = rotationPointZ;
        part2b.z = rotationPointZ;
        part2.z = rotationPointZ;
        // The decompiled local "legNpart3" here is this.legNpart2.
        final float rotationPointX = (float) (part1.x - Math.sin(part1.yRot) * dist);
        part2c.x = rotationPointX;
        part2b.x = rotationPointX;
        part2.x = rotationPointX;
        part2.xRot = 1.871f - upangle;
        part2b.xRot = 1.817f - upangle;
        part2c.xRot = 1.762f - upangle;
        dist = 32.0f;
        dist = (float) Math.abs(dist * Math.cos(part2.xRot));
        final float n = (float) (part2.z - Math.cos(part2.yRot) * dist);
        part3d.z = n;
        part3c.z = n;
        part3b.z = n;
        part3.z = n;
        final float n2 = (float) (part2.x - Math.sin(part2.yRot) * dist);
        part3d.x = n2;
        part3c.x = n2;
        part3b.x = n2;
        part3.x = n2;
        part3.xRot = 1.08f + upangle;
        part3b.xRot = 1.08f + upangle;
        part3c.xRot = 1.08f + upangle;
        part3d.xRot = 1.08f + upangle;
    }

    /**
     * {@code doRightRearLeg} / {@code doLeftRearLeg} (:1171-1287).
     *
     * @param base -1.2 for leg4 (right rear), 1.2 for leg3 (left rear)
     */
    private static void doRearLeg(final ModelPart[] l, final float base, final float angle, final float upangle) {
        final ModelPart part1 = l[0];
        final ModelPart part1b = l[1];
        final ModelPart elbow = l[2];
        final ModelPart part2 = l[3];
        final ModelPart part2b = l[4];
        final ModelPart part2c = l[5];
        final ModelPart part3 = l[6];
        final ModelPart part3b = l[7];
        final ModelPart part3c = l[8];
        final ModelPart part3d = l[9];
        part1.yRot = base + angle;
        part1b.yRot = part1.yRot;
        elbow.yRot = part1.yRot;
        part2.yRot = part1.yRot;
        part2b.yRot = part1.yRot;
        part2c.yRot = part1.yRot;
        part3.yRot = part1.yRot;
        part3b.yRot = part1.yRot;
        part3c.yRot = part1.yRot;
        part3d.yRot = part1.yRot;
        part1.xRot = -1.115f + upangle;
        part1b.xRot = -1.078f + upangle;
        elbow.xRot = part1.xRot;
        float dist = 26.0f;
        dist *= (float) Math.cos(part1.xRot);
        final float rotationPointZ = (float) (part1.z + Math.cos(part1.yRot) * dist);
        part2c.z = rotationPointZ;
        part2b.z = rotationPointZ;
        part2.z = rotationPointZ;
        final float rotationPointX = (float) (part1.x + Math.sin(part1.yRot) * dist);
        part2c.x = rotationPointX;
        part2b.x = rotationPointX;
        part2.x = rotationPointX;
        part2.xRot = -1.871f - upangle;
        part2b.xRot = -1.817f - upangle;
        part2c.xRot = -1.762f - upangle;
        dist = 32.0f;
        dist = (float) Math.abs(dist * Math.cos(part2.xRot));
        final float n = (float) (part2.z + Math.cos(part2.yRot) * dist);
        part3d.z = n;
        part3c.z = n;
        part3b.z = n;
        part3.z = n;
        final float n2 = (float) (part2.x + Math.sin(part2.yRot) * dist);
        part3d.x = n2;
        part3c.x = n2;
        part3b.x = n2;
        part3.x = n2;
        part3.xRot = -1.08f + upangle;
        part3b.xRot = -1.08f + upangle;
        part3c.xRot = -1.08f + upangle;
        part3d.xRot = -1.08f + upangle;
    }

    /** {@code render()} (:907-1040). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
