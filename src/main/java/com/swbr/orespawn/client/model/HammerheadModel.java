package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.HammerheadGeometry;
import com.swbr.orespawn.entity.monster.Hammerhead;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelHammerhead} (ModelHammerhead.java:7-354): 37 boxes, 222x256 texture, geometry
 * {@link HammerheadGeometry}. {@code render()} (:240-343): six legs in two phases, the head group turning with a quarter
 * of the head yaw and nodding while attacking, the side armour breathing. No GL calls.
 */
public class HammerheadModel extends EntityModel<Hammerhead> {

    /** Register with {@code HammerheadGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "hammerhead"), "main");

    /** {@code ModelHammerhead(float f1)}: {@code wingspeed = f1} (:49-50); ClientProxyOreSpawn passes 0.33 (manifest). */
    private final float wingspeed;

    private final ModelPart chest;
    private final ModelPart abdomen;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart snout;
    private final ModelPart neck_armour;
    private final ModelPart horn_base;
    private final ModelPart horn_1;
    private final ModelPart horn_2;
    private final ModelPart horn_R;
    private final ModelPart horn_L;
    private final ModelPart back_armour1;
    private final ModelPart back_armour_2;
    private final ModelPart back_armour_3;
    private final ModelPart back_armour_3R;
    private final ModelPart back_armour_4;
    private final ModelPart back_armour_4R;
    private final ModelPart tail;
    private final ModelPart leg_1R;
    private final ModelPart leg_1;
    private final ModelPart leg_2;
    private final ModelPart leg_2R;
    private final ModelPart leg_3R;
    private final ModelPart leg_3;
    private final ModelPart leg_1Rb;
    private final ModelPart leg_1b;
    private final ModelPart leg_2b;
    private final ModelPart leg_2Rb;
    private final ModelPart leg_3Rb;
    private final ModelPart leg_3b;
    private final ModelPart fan1;
    private final ModelPart Lfan2;
    private final ModelPart Rfan2;
    private final ModelPart Lfan3;
    private final ModelPart Rfan3;
    private final ModelPart Lear;
    private final ModelPart Rear;
    /** The draw order of {@code render()} (:306-342). */
    private final ModelPart[] drawOrder;

    public HammerheadModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.chest = root.getChild(HammerheadGeometry.CHEST);
        this.abdomen = root.getChild(HammerheadGeometry.ABDOMEN);
        this.neck = root.getChild(HammerheadGeometry.NECK);
        this.head = root.getChild(HammerheadGeometry.HEAD);
        this.snout = root.getChild(HammerheadGeometry.SNOUT);
        this.neck_armour = root.getChild(HammerheadGeometry.NECK_ARMOUR);
        this.horn_base = root.getChild(HammerheadGeometry.HORN_BASE);
        this.horn_1 = root.getChild(HammerheadGeometry.HORN_1);
        this.horn_2 = root.getChild(HammerheadGeometry.HORN_2);
        this.horn_R = root.getChild(HammerheadGeometry.HORN_R);
        this.horn_L = root.getChild(HammerheadGeometry.HORN_L);
        this.back_armour1 = root.getChild(HammerheadGeometry.BACK_ARMOUR1);
        this.back_armour_2 = root.getChild(HammerheadGeometry.BACK_ARMOUR_2);
        this.back_armour_3 = root.getChild(HammerheadGeometry.BACK_ARMOUR_3);
        this.back_armour_3R = root.getChild(HammerheadGeometry.BACK_ARMOUR_3_R);
        this.back_armour_4 = root.getChild(HammerheadGeometry.BACK_ARMOUR_4);
        this.back_armour_4R = root.getChild(HammerheadGeometry.BACK_ARMOUR_4_R);
        this.tail = root.getChild(HammerheadGeometry.TAIL);
        this.leg_1R = root.getChild(HammerheadGeometry.LEG_1_R);
        this.leg_1 = root.getChild(HammerheadGeometry.LEG_1);
        this.leg_2 = root.getChild(HammerheadGeometry.LEG_2);
        this.leg_2R = root.getChild(HammerheadGeometry.LEG_2_R);
        this.leg_3R = root.getChild(HammerheadGeometry.LEG_3_R);
        this.leg_3 = root.getChild(HammerheadGeometry.LEG_3);
        this.leg_1Rb = root.getChild(HammerheadGeometry.LEG_1_RB);
        this.leg_1b = root.getChild(HammerheadGeometry.LEG_1B);
        this.leg_2b = root.getChild(HammerheadGeometry.LEG_2B);
        this.leg_2Rb = root.getChild(HammerheadGeometry.LEG_2_RB);
        this.leg_3Rb = root.getChild(HammerheadGeometry.LEG_3_RB);
        this.leg_3b = root.getChild(HammerheadGeometry.LEG_3B);
        this.fan1 = root.getChild(HammerheadGeometry.FAN1);
        this.Lfan2 = root.getChild(HammerheadGeometry.LFAN2);
        this.Rfan2 = root.getChild(HammerheadGeometry.RFAN2);
        this.Lfan3 = root.getChild(HammerheadGeometry.LFAN3);
        this.Rfan3 = root.getChild(HammerheadGeometry.RFAN3);
        this.Lear = root.getChild(HammerheadGeometry.LEAR);
        this.Rear = root.getChild(HammerheadGeometry.REAR);
        this.drawOrder = new ModelPart[] {
                this.chest, this.abdomen, this.neck, this.head, this.snout, this.neck_armour, this.horn_base, this.horn_1,
                this.horn_2, this.horn_R, this.horn_L, this.back_armour1, this.back_armour_2, this.back_armour_3,
                this.back_armour_3R, this.back_armour_4, this.back_armour_4R, this.tail, this.leg_1R, this.leg_1, this.leg_2,
                this.leg_2R, this.leg_3R, this.leg_3, this.leg_1Rb, this.leg_1b, this.leg_2b, this.leg_2Rb, this.leg_3Rb,
                this.leg_3b, this.fan1, this.Lfan2, this.Rfan2, this.Lfan3, this.Rfan3, this.Lear, this.Rear };
    }

    /** The writes of {@code render()} (:244-305). */
    @Override
    public void setupAnim(final Hammerhead e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f * f1;
            newangle2 = Mth.cos((float) (f2 * 1.3f * this.wingspeed + 0.7853981633974483)) * 3.1415927f * 0.1f * f1;
        } else {
            newangle = 0.0f;
        }
        this.leg_1.xRot = -0.087f + newangle;
        this.leg_1b.xRot = newangle;
        this.leg_1R.xRot = -0.087f - newangle;
        this.leg_1Rb.xRot = -newangle;
        this.leg_2.xRot = -0.052f + newangle2;
        this.leg_2b.xRot = newangle2;
        this.leg_2R.xRot = -0.052f - newangle2;
        this.leg_2Rb.xRot = -newangle2;
        this.leg_3.xRot = -0.349f - newangle;
        this.leg_3b.xRot = -newangle;
        this.leg_3R.xRot = -0.349f + newangle;
        this.leg_3Rb.xRot = newangle;
        this.neck.yRot = (float) Math.toRadians(f3) * 0.25f;
        this.neck_armour.yRot = this.neck.yRot;
        this.horn_base.yRot = this.neck.yRot;
        this.horn_1.yRot = this.neck.yRot;
        this.horn_2.yRot = this.neck.yRot;
        this.horn_L.yRot = this.neck.yRot;
        this.horn_R.yRot = this.neck.yRot;
        this.head.yRot = this.neck.yRot;
        this.snout.yRot = this.neck.yRot;
        this.fan1.yRot = this.neck.yRot;
        this.Lfan2.yRot = this.neck.yRot - 0.122f;
        this.Lfan3.yRot = this.neck.yRot - 0.226f;
        this.Rfan2.yRot = this.neck.yRot + 0.122f;
        this.Rfan3.yRot = this.neck.yRot + 0.226f;
        this.Lear.yRot = this.neck.yRot + 0.227f;
        this.Rear.yRot = this.neck.yRot - 0.227f;
        newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.03f;
        this.back_armour_4.yRot = 0.349f + newangle;
        this.back_armour_4R.yRot = -0.349f - newangle;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.13f;
        } else {
            newangle = 0.0f;
        }
        this.neck.xRot = newangle + 0.157f;
        this.neck_armour.xRot = newangle + 0.157f;
        this.horn_base.xRot = newangle + 0.087f;
        this.horn_1.xRot = newangle + 0.192f;
        this.horn_2.xRot = newangle + 0.192f;
        this.horn_L.xRot = newangle + 0.192f;
        this.horn_R.xRot = newangle + 0.192f;
        this.head.xRot = newangle + 0.209f;
        this.snout.xRot = newangle + 0.611f;
        this.fan1.xRot = newangle - 0.139f;
        this.Lfan2.xRot = newangle - 0.209f;
        this.Lfan3.xRot = newangle - 0.331f;
        this.Rfan2.xRot = newangle - 0.209f;
        this.Rfan3.xRot = newangle - 0.331f;
        this.Lear.xRot = newangle + 0.366f;
        this.Rear.xRot = newangle + 0.366f;
    }

    /** The draw calls of {@code render()} (:306-342). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
