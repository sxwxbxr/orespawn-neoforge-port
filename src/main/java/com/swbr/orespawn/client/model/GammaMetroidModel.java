package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GammaMetroidGeometry;
import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelGammaMetroid} (ModelGammaMetroid.java:8-264): four shell plates, head with beak
 * and three tusks, four two-segment legs, and the translucent belly around a core, 256x64 texture. Geometry from the
 * generated {@link GammaMetroidGeometry}; the animation is {@code render()} (:145-214) and the four leg helpers
 * (:226-264):
 * <ul>
 *   <li>the tusks wobble on X and Y with six independent frequencies;</li>
 *   <li>the legs step with {@code limbSwingAmount}, lifted while the next angle is larger, knee pivots recomputed;</li>
 *   <li>the shell breathes, still while sitting; the lower beak chews.</li>
 * </ul>
 *
 * <p>The whole model is drawn inside one GL blend block (:189-213): {@code glEnable(GL_NORMALIZE)},
 * {@code glEnable(GL_BLEND)}, {@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)} and no {@code glColor4f}, so the
 * texture's own alpha (the half-transparent {@code Bellyoutside}) decides. That is {@link RenderType#entityTranslucent}
 * (R8; no culling, as 1.7.10's living renderer disabled it) with the renderer's colour. {@code GL_NORMALIZE} has no
 * counterpart; 1.21.1 transforms normals with the pose's normal matrix.
 *
 * <p>PORT: the draw order (core first, outer belly last) is kept in {@link #renderToBuffer}, but 1.21.1 sorts the quads
 * of a translucent buffer back to front on upload, so the order in which they reach the screen is the sorted one - in
 * practice the same look (core behind the belly hull when seen from outside), not the same call sequence.
 */
public class GammaMetroidModel extends EntityModel<GammaMetroid> {

    /** Register with {@code GammaMetroidGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "wtf"), "main");

    /** {@code ModelGammaMetroid(float f1)}: {@code wingspeed = f1} (:33-35); ClientProxyOreSpawn passes 0.45 (manifest). */
    private final float wingspeed;
    private final ModelPart[] all;
    private final ModelPart Shell3;
    private final ModelPart Shell4;
    private final ModelPart Head;
    private final ModelPart BeakUpper;
    private final ModelPart BeakLower;
    private final ModelPart LeftTusk;
    private final ModelPart MiddleTusk;
    private final ModelPart RightTusk;
    private final ModelPart LeftFrontUpperLeg;
    private final ModelPart LeftFrontLowerLeg;
    private final ModelPart LeftRearUpperLeg;
    private final ModelPart LeftRearLowerLeg;
    private final ModelPart RightFrontUpperLeg;
    private final ModelPart RightFrontLowerLeg;
    private final ModelPart RightRearUpperLeg;
    private final ModelPart RightRearLowerLeg;
    private final ModelPart Core;
    private final ModelPart Bellyinside;
    private final ModelPart Bellyoutside;
    private final ModelPart Shell1;
    private final ModelPart Shell2;

    public GammaMetroidModel(final ModelPart root, final float f1) {
        super(RenderType::entityTranslucent);
        this.wingspeed = f1;
        this.all = new ModelPart[GammaMetroidGeometry.PARTS.length];
        for (int i = 0; i < this.all.length; ++i) {
            this.all[i] = root.getChild(GammaMetroidGeometry.PARTS[i]);
        }
        this.Shell3 = root.getChild(GammaMetroidGeometry.SHELL3);
        this.Shell4 = root.getChild(GammaMetroidGeometry.SHELL4);
        this.Head = root.getChild(GammaMetroidGeometry.HEAD);
        this.BeakUpper = root.getChild(GammaMetroidGeometry.BEAK_UPPER);
        this.BeakLower = root.getChild(GammaMetroidGeometry.BEAK_LOWER);
        this.LeftTusk = root.getChild(GammaMetroidGeometry.LEFT_TUSK);
        this.MiddleTusk = root.getChild(GammaMetroidGeometry.MIDDLE_TUSK);
        this.RightTusk = root.getChild(GammaMetroidGeometry.RIGHT_TUSK);
        this.LeftFrontUpperLeg = root.getChild(GammaMetroidGeometry.LEFT_FRONT_UPPER_LEG);
        this.LeftFrontLowerLeg = root.getChild(GammaMetroidGeometry.LEFT_FRONT_LOWER_LEG);
        this.LeftRearUpperLeg = root.getChild(GammaMetroidGeometry.LEFT_REAR_UPPER_LEG);
        this.LeftRearLowerLeg = root.getChild(GammaMetroidGeometry.LEFT_REAR_LOWER_LEG);
        this.RightFrontUpperLeg = root.getChild(GammaMetroidGeometry.RIGHT_FRONT_UPPER_LEG);
        this.RightFrontLowerLeg = root.getChild(GammaMetroidGeometry.RIGHT_FRONT_LOWER_LEG);
        this.RightRearUpperLeg = root.getChild(GammaMetroidGeometry.RIGHT_REAR_UPPER_LEG);
        this.RightRearLowerLeg = root.getChild(GammaMetroidGeometry.RIGHT_REAR_LOWER_LEG);
        this.Core = root.getChild(GammaMetroidGeometry.CORE);
        this.Bellyinside = root.getChild(GammaMetroidGeometry.BELLYINSIDE);
        this.Bellyoutside = root.getChild(GammaMetroidGeometry.BELLYOUTSIDE);
        this.Shell1 = root.getChild(GammaMetroidGeometry.SHELL1);
        this.Shell2 = root.getChild(GammaMetroidGeometry.SHELL2);
    }

    /** The angle and pivot writes of {@code render()} (:149-188), in the original order. */
    @Override
    public void setupAnim(final GammaMetroid entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.all) {
            part.resetPose();
        }
        final GammaMetroid e = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.81f * this.wingspeed) * 3.1415927f * 0.08f;
        this.LeftTusk.xRot = newangle;
        newangle = Mth.cos(f2 * 0.87f * this.wingspeed) * 3.1415927f * 0.08f;
        this.RightTusk.xRot = newangle;
        newangle = Mth.cos(f2 * 0.99f * this.wingspeed) * 3.1415927f * 0.08f;
        this.MiddleTusk.xRot = newangle;
        newangle = Mth.cos(f2 * 1.11f * this.wingspeed) * 3.1415927f * 0.08f;
        this.LeftTusk.yRot = newangle;
        newangle = Mth.cos(f2 * 1.17f * this.wingspeed) * 3.1415927f * 0.08f;
        this.RightTusk.yRot = newangle;
        newangle = Mth.cos(f2 * 1.25f * this.wingspeed) * 3.1415927f * 0.08f;
        this.MiddleTusk.yRot = newangle;
        float nextangle = 0.0f;
        float upangle = 0.0f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        nextangle = Mth.cos((f2 + 0.1f) * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * f1 - Math.abs(newangle);
        }
        this.doLeftFLeg(this.LeftFrontUpperLeg, this.LeftFrontLowerLeg, newangle, upangle);
        this.doRightFLeg(this.RightFrontUpperLeg, this.RightFrontLowerLeg, -newangle, upangle);
        this.doLeftRLeg(this.LeftRearUpperLeg, this.LeftRearLowerLeg, -newangle, upangle);
        this.doRightRLeg(this.RightRearUpperLeg, this.RightRearLowerLeg, newangle, upangle);
        newangle = Mth.cos(f2 * 0.4f * this.wingspeed) * 3.1415927f * 0.05f;
        if (e.isSitting()) {
            newangle = 0.0f;
        }
        this.Shell1.xRot = newangle / 4.0f;
        this.Shell1.yRot = -(newangle / 4.0f);
        this.Shell2.xRot = newangle - 0.49f;
        this.Shell2.yRot = -newangle + 0.33f;
        this.Shell3.xRot = newangle - 0.96f;
        this.Shell3.yRot = -newangle + 0.63f;
        this.Shell4.xRot = newangle - 0.28f;
        newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.1f;
        newangle = Math.abs(newangle);
        this.BeakLower.xRot = newangle + 0.14f;
        this.BeakLower.zRot = newangle + 0.14f;
    }

    /** {@code doLeftFLeg} (:226-234). */
    private void doLeftFLeg(final ModelPart seg2, final ModelPart seg3, final float angle, final float upangle) {
        seg2.xRot = angle - 0.17f;
        seg3.xRot = angle - 0.26f;
        seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5f;
        seg2.zRot = -upangle - 0.66f;
        seg3.zRot = -upangle;
        seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
        seg3.x = (float) (seg2.x + Math.abs(Math.sin(seg2.zRot) * 7.0) + 1.0);
    }

    /** {@code doLeftRLeg} (:236-244). */
    private void doLeftRLeg(final ModelPart seg2, final ModelPart seg3, final float angle, final float upangle) {
        seg2.xRot = angle + 0.17f;
        seg3.xRot = angle + 0.31f;
        seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5f;
        seg2.zRot = -upangle - 0.82f;
        seg3.zRot = -upangle;
        seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
        seg3.x = (float) (seg2.x + Math.abs(Math.sin(seg2.zRot) * 7.0) + 1.5);
    }

    /** {@code doRightFLeg} (:246-254). */
    private void doRightFLeg(final ModelPart seg2, final ModelPart seg3, final float angle, final float upangle) {
        seg2.xRot = angle - 0.17f;
        seg3.xRot = angle - 0.26f;
        seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5f;
        seg2.zRot = -upangle + 0.34f;
        seg3.zRot = -upangle;
        seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
        seg3.x = (float) (seg2.x - Math.abs(Math.sin(seg2.zRot) * 7.0) - 1.0);
    }

    /** {@code doRightRLeg} (:256-264). */
    private void doRightRLeg(final ModelPart seg2, final ModelPart seg3, final float angle, final float upangle) {
        seg2.xRot = angle + 0.17f;
        seg3.xRot = angle + 0.31f;
        seg3.z = (float) (seg2.z + Math.sin(seg2.xRot) * 7.0) - 0.5f;
        seg2.zRot = -upangle + 0.82f;
        seg3.zRot = -upangle;
        seg3.y = seg2.y + (float) (5.0 * Math.cos(seg2.xRot));
        seg3.x = (float) (seg2.x - Math.abs(Math.sin(seg2.zRot) * 7.0) - 1.5);
    }

    /** {@code render()} (:192-212): the draw order of the blend block. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Core.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shell3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shell4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BeakUpper.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BeakLower.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LeftTusk.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.MiddleTusk.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RightTusk.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LeftFrontUpperLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LeftFrontLowerLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LeftRearUpperLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LeftRearLowerLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RightFrontUpperLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RightFrontLowerLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RightRearUpperLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RightRearLowerLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Bellyinside.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shell1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shell2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Bellyoutside.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
