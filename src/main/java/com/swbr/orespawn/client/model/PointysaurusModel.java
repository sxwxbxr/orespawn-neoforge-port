package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.PointysaurusGeometry;
import com.swbr.orespawn.entity.dino.Pointysaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelPointysaurus} (ModelPointysaurus.java:7-307): 30 boxes, 128x128. Geometry from
 * the generated {@link PointysaurusGeometry}; the animation is {@code render()} (:198-265): four legs in diagonal
 * pairs, the head with horns, frill and its sixteen bumps following head yaw and pitch at 45 %, and a tail that wags
 * harder while attacking.
 *
 * <p>{@code f3} is {@code netHeadYaw}, {@code f4} is {@code headPitch}, both in degrees as in 1.7.10.
 */
public class PointysaurusModel extends EntityModel<Pointysaurus> {

    /** Register with {@code PointysaurusGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "pointysaurus"), "main");

    /** {@code wingspeed = f1} (:43); ClientProxyOreSpawn passes 1.0 (ClientProxyOreSpawn.java:139). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart lfleg;
    private final ModelPart rfleg;
    private final ModelPart lrleg;
    private final ModelPart rrleg;
    private final ModelPart head;
    private final ModelPart guard;
    private final ModelPart nose;
    private final ModelPart lhorn;
    private final ModelPart rhorn;
    private final ModelPart chorn;
    private final ModelPart tail;
    private final ModelPart[] bumps;

    public PointysaurusModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[PointysaurusGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(PointysaurusGeometry.PARTS[i]);
        }
        this.lfleg = root.getChild(PointysaurusGeometry.LFLEG);
        this.rfleg = root.getChild(PointysaurusGeometry.RFLEG);
        this.lrleg = root.getChild(PointysaurusGeometry.LRLEG);
        this.rrleg = root.getChild(PointysaurusGeometry.RRLEG);
        this.head = root.getChild(PointysaurusGeometry.HEAD);
        this.guard = root.getChild(PointysaurusGeometry.GUARD);
        this.nose = root.getChild(PointysaurusGeometry.NOSE);
        this.lhorn = root.getChild(PointysaurusGeometry.LHORN);
        this.rhorn = root.getChild(PointysaurusGeometry.RHORN);
        this.chorn = root.getChild(PointysaurusGeometry.CHORN);
        this.tail = root.getChild(PointysaurusGeometry.TAIL);
        this.bumps = new ModelPart[] {
                root.getChild(PointysaurusGeometry.BUMP1), root.getChild(PointysaurusGeometry.BUMP2),
                root.getChild(PointysaurusGeometry.BUMP3), root.getChild(PointysaurusGeometry.BUMP4),
                root.getChild(PointysaurusGeometry.BUMP5), root.getChild(PointysaurusGeometry.BUMP6),
                root.getChild(PointysaurusGeometry.BUMP7), root.getChild(PointysaurusGeometry.BUMP8),
                root.getChild(PointysaurusGeometry.BUMP9), root.getChild(PointysaurusGeometry.BUMP10),
                root.getChild(PointysaurusGeometry.BUMP11), root.getChild(PointysaurusGeometry.BUMP12),
                root.getChild(PointysaurusGeometry.BUMP13), root.getChild(PointysaurusGeometry.BUMP14),
                root.getChild(PointysaurusGeometry.BUMP15), root.getChild(PointysaurusGeometry.BUMP16)
        };
    }

    /** The angle writes of {@code render()} (:202-265); the sixteen {@code bumpN} lines are a loop over the same assignments. */
    @Override
    public void setupAnim(final Pointysaurus e, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
                          final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        final float f3 = netHeadYaw;
        final float f4 = headPitch;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lfleg.xRot = newangle;
        this.rrleg.xRot = newangle;
        this.rfleg.xRot = -newangle;
        this.lrleg.xRot = -newangle;
        this.head.yRot = (float) Math.toRadians(f3) * 0.45f;
        this.nose.yRot = this.head.yRot;
        this.chorn.yRot = this.head.yRot;
        this.lhorn.yRot = this.head.yRot - 0.14f;
        this.rhorn.yRot = this.head.yRot + 0.14f;
        this.guard.yRot = this.head.yRot;
        for (final ModelPart bump : this.bumps) {
            bump.yRot = this.head.yRot;
        }
        this.head.xRot = (float) Math.toRadians(f4) * 0.45f;
        this.nose.xRot = this.head.xRot;
        this.chorn.xRot = this.head.xRot;
        this.lhorn.xRot = this.head.xRot - 0.16f;
        this.rhorn.xRot = this.head.xRot - 0.16f;
        this.guard.xRot = this.head.xRot - 0.262f;
        for (final ModelPart bump : this.bumps) {
            bump.xRot = this.guard.xRot;
        }
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f;
        } else {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.05f;
        }
        this.tail.yRot = newangle;
        newangle = Mth.cos(f2 * 0.02f * this.wingspeed) * 3.1415927f * 0.15f;
        this.tail.xRot = newangle + 0.28f;
    }

    /** {@code render()} (:266-295): all 30 parts. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
