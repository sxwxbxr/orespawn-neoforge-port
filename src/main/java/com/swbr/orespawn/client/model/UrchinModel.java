package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.UrchinGeometry;
import com.swbr.orespawn.entity.sea.Urchin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelUrchin} (ModelUrchin.java:7-222): four inner and four outer legs, a spinning
 * center and eight spines on a 128x128 texture, geometry {@link UrchinGeometry}. {@code wingspeed} 1.0
 * (ClientProxyOreSpawn :117). The legs move with the walk; the center spins ten times faster and the spines quiver
 * harder while {@code attacking} is set. No GL calls.
 */
public class UrchinModel extends EntityModel<Urchin> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "crystal_urchin"), "main");

    private final float wingspeed;
    private final ModelPart if1;
    private final ModelPart if2;
    private final ModelPart if3;
    private final ModelPart if4;
    private final ModelPart of1;
    private final ModelPart of2;
    private final ModelPart of3;
    private final ModelPart of4;
    private final ModelPart center;
    private final ModelPart tis1;
    private final ModelPart tis2;
    private final ModelPart tis3;
    private final ModelPart tis4;
    private final ModelPart tos1;
    private final ModelPart tos2;
    private final ModelPart tos3;
    private final ModelPart tos4;
    private final ModelPart[] parts;

    /** {@code ModelUrchin(float f1)} (:28-...). */
    public UrchinModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.if1 = root.getChild(UrchinGeometry.IF1);
        this.if2 = root.getChild(UrchinGeometry.IF2);
        this.if3 = root.getChild(UrchinGeometry.IF3);
        this.if4 = root.getChild(UrchinGeometry.IF4);
        this.of1 = root.getChild(UrchinGeometry.OF1);
        this.of2 = root.getChild(UrchinGeometry.OF2);
        this.of3 = root.getChild(UrchinGeometry.OF3);
        this.of4 = root.getChild(UrchinGeometry.OF4);
        this.center = root.getChild(UrchinGeometry.CENTER);
        this.tis1 = root.getChild(UrchinGeometry.TIS1);
        this.tis2 = root.getChild(UrchinGeometry.TIS2);
        this.tis3 = root.getChild(UrchinGeometry.TIS3);
        this.tis4 = root.getChild(UrchinGeometry.TIS4);
        this.tos1 = root.getChild(UrchinGeometry.TOS1);
        this.tos2 = root.getChild(UrchinGeometry.TOS2);
        this.tos3 = root.getChild(UrchinGeometry.TOS3);
        this.tos4 = root.getChild(UrchinGeometry.TOS4);
        this.parts = new ModelPart[] {this.if1, this.if2, this.if3, this.if4, this.of1, this.of2, this.of3, this.of4,
                this.center, this.tis1, this.tis2, this.tis3, this.tis4, this.tos1, this.tos2, this.tos3, this.tos4};
    }

    /** The writes of {@code render()}, in its order. */
    @Override
    public void setupAnim(final Urchin u, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle;
        float newangle2;
        float newangle3;
        float newangle4;
        float newangle5;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.15f * f1;
            newangle2 = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.15f * f1;
            newangle3 = Mth.cos(f2 * 1.65f * this.wingspeed) * 3.1415927f * 0.15f * f1;
            newangle4 = Mth.cos(f2 * 1.75f * this.wingspeed) * 3.1415927f * 0.15f * f1;
            newangle5 = Mth.cos(f2 * 1.8f * this.wingspeed) * 3.1415927f * 0.15f * f1;
        } else {
            newangle = 0.0f;
            newangle2 = 0.0f;
            newangle3 = 0.0f;
            newangle4 = 0.0f;
            newangle5 = 0.0f;
        }
        this.if1.xRot = 0.261f + newangle2;
        this.if2.xRot = -0.261f - newangle3;
        this.if3.xRot = newangle4;
        this.if4.xRot = -newangle5;
        this.of1.zRot = -0.523f + newangle;
        this.of2.zRot = 0.523f - newangle;
        this.of3.xRot = -0.523f + newangle;
        this.of4.xRot = 0.523f - newangle;
        float newangle6;
        float newangle7;
        float newangle8;
        float newangle9;
        if (u.getAttacking() != 0) {
            newangle = (float) (f2 * 0.2f % 6.283185307179586);
            newangle2 = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle3 = Mth.cos(f2 * 0.65f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle4 = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle5 = Mth.cos(f2 * 0.8f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle6 = Mth.cos(f2 * 0.55f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle7 = Mth.cos(f2 * 0.45f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle8 = Mth.cos(f2 * 0.35f * this.wingspeed) * 3.1415927f * 0.06f;
            newangle9 = Mth.cos(f2 * 0.4f * this.wingspeed) * 3.1415927f * 0.06f;
        } else {
            newangle = (float) (f2 * 0.02f % 6.283185307179586);
            newangle2 = Mth.cos(f2 * 0.07f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle3 = Mth.cos(f2 * 0.065f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle4 = Mth.cos(f2 * 0.075f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle5 = Mth.cos(f2 * 0.08f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle6 = Mth.cos(f2 * 0.055f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle7 = Mth.cos(f2 * 0.045f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle8 = Mth.cos(f2 * 0.035f * this.wingspeed) * 3.1415927f * 0.02f;
            newangle9 = Mth.cos(f2 * 0.04f * this.wingspeed) * 3.1415927f * 0.02f;
        }
        this.center.yRot = newangle;
        this.tis1.xRot = 0.261f + newangle2;
        this.tis2.xRot = -0.261f + newangle3;
        this.tis3.xRot = newangle4;
        this.tis4.xRot = newangle5;
        this.tis1.zRot = newangle6;
        this.tis2.zRot = newangle7;
        this.tis3.zRot = 0.261f + newangle8;
        this.tis4.zRot = -0.261f + newangle9;
        this.tos1.xRot = -0.532f + newangle2;
        this.tos2.xRot = newangle8;
        this.tos3.xRot = newangle4;
        this.tos4.xRot = 0.532f + newangle6;
        this.tos1.zRot = newangle5;
        this.tos2.zRot = -0.523f + newangle7;
        this.tos3.zRot = 0.523f + newangle3;
        this.tos4.zRot = newangle9;
    }

    /** Draw order of {@code render()} (the creation order). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
