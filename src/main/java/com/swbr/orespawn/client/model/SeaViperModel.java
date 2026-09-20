package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SeaViperGeometry;
import com.swbr.orespawn.entity.sea.SeaViper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelSeaViper} (ModelSeaViper.java:7-436): a serpent of 22 body segments with head,
 * jaws, fangs, eyes and a forked tongue on a 128x128 texture, geometry {@link SeaViperGeometry}. {@code wingspeed} 0.5
 * (ClientProxyOreSpawn :127). {@code doseg} hangs every segment at the end of the previous one and swings it on a
 * phase-shifted wave (the faked hierarchy of docs/research/06-models-design.md); the mouth opens and the tongue flicks
 * while {@code attacking} is set. No GL calls.
 *
 * <p>PORT: the tongue parts move by {@code ModelRenderer.offsetZ}, which 1.7.10 applied as
 * {@code glTranslatef(0, 0, offsetZ)} in block units before the pivot translation. {@code ModelPart} has no offset;
 * both translations happen before the part's rotation, so the offset is added to the pivot in model pixels
 * ({@code z + offsetZ * 16}).
 */
public class SeaViperModel extends EntityModel<SeaViper> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "sea_viper"), "main");

    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart TailTip;
    private final ModelPart tBase;
    private final ModelPart t2;
    private final ModelPart t3;
    private final ModelPart t4;
    private final ModelPart t5;
    private final ModelPart t6;
    private final ModelPart t7;
    private final ModelPart t8;
    private final ModelPart t9;
    private final ModelPart t10;
    private final ModelPart t12;
    private final ModelPart t11;
    private final ModelPart t13;
    private final ModelPart t14;
    private final ModelPart t15;
    private final ModelPart t16;
    private final ModelPart t17;
    private final ModelPart t18;
    private final ModelPart t19;
    private final ModelPart t20;
    private final ModelPart t21;
    private final ModelPart MouthBottom;
    private final ModelPart ToungBase;
    private final ModelPart MiddleTounge;
    private final ModelPart EyeRight;
    private final ModelPart EyeLeft;
    private final ModelPart MouthTop;
    private final ModelPart Head;
    private final ModelPart FangRight;
    private final ModelPart FangLeft;
    private final ModelPart ForkRight;
    private final ModelPart ForkLeft;

    /** {@code ModelSeaViper(float f1)} (:45-...). */
    public SeaViperModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[SeaViperGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(SeaViperGeometry.PARTS[i]);
        }
        this.TailTip = root.getChild(SeaViperGeometry.TAIL_TIP);
        this.tBase = root.getChild(SeaViperGeometry.T_BASE);
        this.t2 = root.getChild(SeaViperGeometry.T2);
        this.t3 = root.getChild(SeaViperGeometry.T3);
        this.t4 = root.getChild(SeaViperGeometry.T4);
        this.t5 = root.getChild(SeaViperGeometry.T5);
        this.t6 = root.getChild(SeaViperGeometry.T6);
        this.t7 = root.getChild(SeaViperGeometry.T7);
        this.t8 = root.getChild(SeaViperGeometry.T8);
        this.t9 = root.getChild(SeaViperGeometry.T9);
        this.t10 = root.getChild(SeaViperGeometry.T10);
        this.t12 = root.getChild(SeaViperGeometry.T12);
        this.t11 = root.getChild(SeaViperGeometry.T11);
        this.t13 = root.getChild(SeaViperGeometry.T13);
        this.t14 = root.getChild(SeaViperGeometry.T14);
        this.t15 = root.getChild(SeaViperGeometry.T15);
        this.t16 = root.getChild(SeaViperGeometry.T16);
        this.t17 = root.getChild(SeaViperGeometry.T17);
        this.t18 = root.getChild(SeaViperGeometry.T18);
        this.t19 = root.getChild(SeaViperGeometry.T19);
        this.t20 = root.getChild(SeaViperGeometry.T20);
        this.t21 = root.getChild(SeaViperGeometry.T21);
        this.MouthBottom = root.getChild(SeaViperGeometry.MOUTH_BOTTOM);
        this.ToungBase = root.getChild(SeaViperGeometry.TOUNG_BASE);
        this.MiddleTounge = root.getChild(SeaViperGeometry.MIDDLE_TOUNGE);
        this.EyeRight = root.getChild(SeaViperGeometry.EYE_RIGHT);
        this.EyeLeft = root.getChild(SeaViperGeometry.EYE_LEFT);
        this.MouthTop = root.getChild(SeaViperGeometry.MOUTH_TOP);
        this.Head = root.getChild(SeaViperGeometry.HEAD);
        this.FangRight = root.getChild(SeaViperGeometry.FANG_RIGHT);
        this.FangLeft = root.getChild(SeaViperGeometry.FANG_LEFT);
        this.ForkRight = root.getChild(SeaViperGeometry.FORK_RIGHT);
        this.ForkLeft = root.getChild(SeaViperGeometry.FORK_LEFT);
    }

    /** The writes of {@code render()}, in its order; {@code f1} is clamped to 0 first (:227-229). */
    @Override
    public void setupAnim(final SeaViper e, final float f, float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (f1 < 0.0f) {
            f1 = 0.0f;
        }
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f * f1;
        this.tBase.yRot = newangle;
        this.doseg(this.tBase, this.t2, 2.0f, f1, f2);
        this.doseg(this.t2, this.t3, 2.0f, f1, f2);
        this.doseg(this.t3, this.t4, 3.0f, f1, f2);
        this.doseg(this.t4, this.t5, 4.0f, f1, f2);
        this.doseg(this.t5, this.t6, 5.0f, f1, f2);
        this.doseg(this.t6, this.t7, 6.0f, f1, f2);
        this.doseg(this.t7, this.t8, 7.0f, f1, f2);
        this.doseg(this.t8, this.t9, 8.0f, f1, f2);
        this.doseg(this.t9, this.t10, 9.0f, f1, f2);
        this.doseg(this.t10, this.t11, 10.0f, f1, f2);
        this.doseg(this.t11, this.t12, 11.0f, f1, f2);
        this.doseg(this.t12, this.t13, 12.0f, f1, f2);
        this.doseg(this.t13, this.t14, 13.0f, f1, f2);
        this.doseg(this.t14, this.t15, 14.0f, f1, f2);
        this.doseg(this.t15, this.t16, 15.0f, f1, f2);
        this.doseg(this.t16, this.t17, 16.0f, f1, f2);
        this.doseg(this.t17, this.t18, 17.0f, f1, f2);
        this.doseg(this.t18, this.t19, 18.0f, f1, f2);
        this.doseg(this.t19, this.t20, 19.0f, f1, f2);
        this.doseg(this.t20, this.t21, 20.0f, f1, f2);
        this.doseg(this.t21, this.TailTip, 21.0f, f1, f2);
        final float offsetZ;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.17f;
            this.MouthBottom.xRot = 0.65f + newangle;
            newangle = Mth.cos(f2 * 4.7f * this.wingspeed) * 3.1415927f * 0.07f;
            this.ToungBase.xRot = 0.261f + newangle;
            this.MiddleTounge.xRot = 0.174f + newangle;
            this.ForkLeft.xRot = 0.087f + newangle;
            this.ForkRight.xRot = 0.087f + newangle;
            newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.05f;
            offsetZ = newangle;
        } else {
            newangle = Mth.cos(f2 * 0.2f * this.wingspeed) * 3.1415927f * 0.02f;
            this.MouthBottom.xRot = 0.45f + newangle;
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.03f;
            this.ToungBase.xRot = 0.261f + newangle;
            this.MiddleTounge.xRot = 0.174f + newangle;
            this.ForkLeft.xRot = 0.087f + newangle;
            this.ForkRight.xRot = 0.087f + newangle;
            newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.05f;
            offsetZ = newangle;
        }
        newangle = (float) Math.toRadians(f3) * 0.5f;
        final float n3 = newangle;
        this.EyeRight.yRot = n3;
        this.EyeLeft.yRot = n3;
        this.MouthTop.yRot = n3;
        this.Head.yRot = n3;
        this.FangRight.yRot = newangle;
        this.FangLeft.yRot = newangle;
        this.MouthBottom.yRot = newangle;
        this.MouthBottom.z = this.Head.z - (float) Math.cos(this.Head.yRot) * 2.0f;
        this.MouthBottom.x = this.Head.x - (float) Math.sin(this.Head.yRot) * 2.0f;
        this.ToungBase.yRot = newangle;
        this.MiddleTounge.yRot = newangle;
        this.ForkLeft.yRot = newangle - 0.436f;
        this.ForkRight.yRot = newangle + 0.436f;
        // offsetZ of ForkRight, ForkLeft, MiddleTounge and ToungBase (see the class comment).
        this.ForkRight.z += offsetZ * 16.0f;
        this.ForkLeft.z += offsetZ * 16.0f;
        this.MiddleTounge.z += offsetZ * 16.0f;
        this.ToungBase.z += offsetZ * 16.0f;
    }

    /**
     * {@code doseg} (:405-413): the pivot of {@code notinn} at 9 units along the yaw of {@code inn} (shortened by
     * {@code |cos(inn.xRot)|}), its yaw a travelling wave phase-shifted by {@code pi/4 * f}, blended towards the rest
     * curve {@code cos(-pi/4 * f)} as the walk slows.
     */
    private void doseg(final ModelPart inn, final ModelPart notinn, final float f, final float f1, final float f2) {
        final float pi4 = 0.7853982f;
        float newangle = 0.0f;
        notinn.z = (float) (inn.z + (float) Math.cos(inn.yRot) * (9.0 * Math.abs(Math.cos(inn.xRot))));
        notinn.x = (float) (inn.x + (float) Math.sin(inn.yRot) * 9.0f * Math.abs(Math.cos(inn.xRot)));
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed - pi4 * f) * 3.1415927f * 0.2f * f1;
        final float a = Mth.cos(-(pi4 * f));
        notinn.yRot = newangle + a - a * f1;
    }

    /** Draw order of {@code render()}, which is the creation order of {@link SeaViperGeometry#PARTS}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
