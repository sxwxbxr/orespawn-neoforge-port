package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SpitBugGeometry;
import com.swbr.orespawn.entity.arthropod.SpitBug;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelSpitBug} (ModelSpitBug.java:7-934): 93 boxes, 512x256 texture, geometry from the
 * generated {@link SpitBugGeometry}. The animation is {@code render()} (:576-613) with the four leg helpers
 * {@code doRightFrontLeg}, {@code doLeftFrontLeg}, {@code doRightRearLeg}, {@code doLeftRearLeg} (:719-933).
 *
 * <p>The four helpers are the same statements on different parts with a different base yaw (leg1 -1.2, leg2 +1.2,
 * leg4 +2.1, leg3 -2.1); they are written once as {@link #doLeg}. Where the decompiled source assigned through aliased
 * locals ({@code leg1part3 = this.leg1part2}), the writes land on the aliased part, as spelled out below. The upper jaw
 * and teeth chatter faster while the bug attacks (DataWatcher 20).
 */
public class SpitBugModel extends EntityModel<SpitBug> {

    /** Register with {@code SpitBugGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "spit_bug"), "main");

    /** {@code ModelSpitBug(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 0.55 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:614-706), which is the creation order. */
    private final ModelPart[] parts;
    /** {@code legN, legNpart2, legNpart2b, legNpart2c, legNpart2d, legNpart3, legNpart3b, legNpart3c}. */
    private final ModelPart[] leg1;
    private final ModelPart[] leg2;
    private final ModelPart[] leg3;
    private final ModelPart[] leg4;
    private final ModelPart upperjawbasepart1;
    private final ModelPart upperjawbasepart2;
    private final ModelPart upperjawbasepart3;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;

    public SpitBugModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[SpitBugGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(SpitBugGeometry.PARTS[i]);
        }
        this.leg1 = children(root, SpitBugGeometry.LEG1, SpitBugGeometry.LEG1PART2, SpitBugGeometry.LEG1PART2B,
                SpitBugGeometry.LEG1PART2C, SpitBugGeometry.LEG1PART2D, SpitBugGeometry.LEG1PART3,
                SpitBugGeometry.LEG1PART3B, SpitBugGeometry.LEG1PART3C);
        this.leg2 = children(root, SpitBugGeometry.LEG2, SpitBugGeometry.LEG2PART2, SpitBugGeometry.LEG2PART2B,
                SpitBugGeometry.LEG2PART2C, SpitBugGeometry.LEG2PART2D, SpitBugGeometry.LEG2PART3,
                SpitBugGeometry.LEG2PART3B, SpitBugGeometry.LEG2PART3C);
        this.leg3 = children(root, SpitBugGeometry.LEG3, SpitBugGeometry.LEG3PART2, SpitBugGeometry.LEG3PART2B,
                SpitBugGeometry.LEG3PART2C, SpitBugGeometry.LEG3PART2D, SpitBugGeometry.LEG3PART3,
                SpitBugGeometry.LEG3PART3B, SpitBugGeometry.LEG3PART3C);
        this.leg4 = children(root, SpitBugGeometry.LEG4, SpitBugGeometry.LEG4PART2, SpitBugGeometry.LEG4PART2B,
                SpitBugGeometry.LEG4PART2C, SpitBugGeometry.LEG4PART2D, SpitBugGeometry.LEG4PART3,
                SpitBugGeometry.LEG4PART3B, SpitBugGeometry.LEG4PART3C);
        this.upperjawbasepart1 = root.getChild(SpitBugGeometry.UPPERJAWBASEPART1);
        this.upperjawbasepart2 = root.getChild(SpitBugGeometry.UPPERJAWBASEPART2);
        this.upperjawbasepart3 = root.getChild(SpitBugGeometry.UPPERJAWBASEPART3);
        this.tooth1 = root.getChild(SpitBugGeometry.TOOTH1);
        this.tooth2 = root.getChild(SpitBugGeometry.TOOTH2);
        this.tooth3 = root.getChild(SpitBugGeometry.TOOTH3);
    }

    private static ModelPart[] children(final ModelPart root, final String... names) {
        final ModelPart[] out = new ModelPart[names.length];
        for (int i = 0; i < names.length; ++i) {
            out[i] = root.getChild(names[i]);
        }
        return out;
    }

    /** The writes of {@code render()} (:576-613). */
    @Override
    public void setupAnim(final SpitBug e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        newangle = Mth.sin(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.sin((f2 + 0.1f) * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1);
        }
        // doLeftFrontLeg (leg2), doLeftRearLeg (leg3).
        doLeg(this.leg2, 1.2f, newangle, upangle);
        doLeg(this.leg3, -2.1f, -newangle, upangle);
        newangle = Mth.sin((float) (f2 * 2.0f * this.wingspeed + 3.141592653589793)) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.sin((float) ((f2 + 0.1f) * 2.0f * this.wingspeed + 3.141592653589793)) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos((float) (f2 * 2.0f * this.wingspeed + 3.141592653589793)) * 3.1415927f * 0.12f * f1);
        }
        // doRightFrontLeg (leg1), doRightRearLeg (leg4).
        doLeg(this.leg1, -1.2f, -newangle, upangle);
        doLeg(this.leg4, 2.1f, newangle, upangle);
        if (e.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.015f;
        } else {
            newangle = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.1f;
        }
        newangle = Math.abs(newangle);
        this.upperjawbasepart1.xRot = newangle;
        this.upperjawbasepart2.xRot = newangle;
        this.upperjawbasepart3.xRot = newangle;
        this.tooth1.xRot = 0.26f + newangle;
        this.tooth2.xRot = 0.26f + newangle;
        this.tooth3.xRot = 0.26f + newangle;
    }

    /**
     * One of the four leg helpers (:719-933). {@code leg.xRot} is never written, so it is the rest pose.
     *
     * @param base the helper's constant: -1.2 right front, 1.2 left front, 2.1 right rear, -2.1 left rear
     */
    private static void doLeg(final ModelPart[] l, final float base, final float angle, final float upangle) {
        final ModelPart leg = l[0];
        final ModelPart part2 = l[1];
        final ModelPart part2b = l[2];
        final ModelPart part2c = l[3];
        final ModelPart part2d = l[4];
        final ModelPart part3 = l[5];
        final ModelPart part3b = l[6];
        final ModelPart part3c = l[7];
        leg.yRot = base + angle;
        part2.yRot = leg.yRot;
        part2b.yRot = leg.yRot;
        part2c.yRot = leg.yRot;
        part2d.yRot = leg.yRot;
        part3.yRot = leg.yRot;
        part3b.yRot = leg.yRot;
        part3c.yRot = leg.yRot;
        float dist = 14.0f;
        dist *= (float) Math.cos(leg.xRot);
        final float n = (float) (leg.z - Math.cos(leg.yRot) * dist);
        part2d.z = n;
        part2c.z = n;
        part2b.z = n;
        part2.z = n;
        // The decompiled local "legNpart3" here is this.legNpart2 (:739-747).
        final float n2 = (float) (leg.x - Math.sin(leg.yRot) * dist);
        part2d.x = n2;
        part2c.x = n2;
        part2b.x = n2;
        part2.x = n2;
        part2.xRot = -1.152f + upangle;
        part2b.xRot = -0.743f + upangle;
        part2c.xRot = -0.632f + upangle;
        part2d.xRot = -1.041f + upangle;
        dist = 14.0f;
        dist *= (float) Math.cos(part2.xRot);
        part3.z = (float) (part2.z - Math.cos(part2.yRot) * dist);
        part3.x = (float) (part2.x - Math.sin(part2.yRot) * dist);
        part3.xRot = 0.669f - upangle;
        dist = 8.0f;
        dist = (float) Math.abs(dist * Math.cos(part3.xRot));
        final float n3 = (float) (part3.z - Math.cos(part3.yRot) * dist);
        part3c.z = n3;
        part3b.z = n3;
        final float n4 = (float) (part3.x - Math.sin(part3.yRot) * dist);
        part3c.x = n4;
        part3b.x = n4;
        part3b.xRot = -0.48f - upangle;
        part3c.xRot = -0.48f - upangle;
    }

    /** {@code render()} (:614-706). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
