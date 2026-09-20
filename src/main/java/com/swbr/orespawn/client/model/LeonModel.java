package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.LeonGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.leon.Leon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelLeon} (ModelLeon.java:7-1091), geometry {@link LeonGeometry} (256x256, 98
 * parts). {@code wingspeed} is the constructor argument, 0.22 in ClientProxyOreSpawn.java:130 (manifest
 * {@code model_args}).
 *
 * <p>The skeleton exists twice: 49 ground parts and 49 flight parts with the prefix {@code f}. {@code render()}
 * (:606-1080) animates and draws the ground set while {@code getActivity() == 0} and the flight set otherwise. On the
 * ground the legs walk with the limb swing and the lower leg segments and feet are placed kinematically, the folded
 * wings breathe and the head follows the head yaw at half strength. In flight the wings beat (faster and wider while
 * attacking), the body bobs unless ridden, the neck follows in three segments, the legs are pulled in or swing while
 * attacking, and a ridden Leon turns its head with the smoothed turn rate kept in the entity's {@link RenderInfo}
 * ({@code rf1}).
 *
 * <p>All writes of {@code render()} are in {@link #setupAnim} in the original order; every part is reset first (R8).
 * The original never wrote a part in one branch that the other branch reads, and every part a branch draws is written
 * unconditionally or from its initial pose, so the reset changes nothing. {@link #renderToBuffer} draws the set of
 * the activity read in {@code setupAnim}, in the draw order of {@code render()} (which is the creation order).
 *
 * <p>No GL calls in the original: {@link RenderType#entityCutoutNoCull} (alpha-tested, no culling, as 1.7.10's living
 * renderer).
 */
public class LeonModel extends EntityModel<Leon> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "leonopteryx"), "main");

    private final float wingspeed;
    private final ModelPart chest;
    private final ModelPart neck_1;
    private final ModelPart neck_2;
    private final ModelPart neck_3;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart upper_jaw;
    private final ModelPart bottom_jaw;
    private final ModelPart chest_ridge;
    private final ModelPart upper_sail_1;
    private final ModelPart upper_sail2_;
    private final ModelPart upper_sail3;
    private final ModelPart lower_sail1;
    private final ModelPart lower_sail2;
    private final ModelPart lower_sail_3;
    private final ModelPart eye_ridge_L;
    private final ModelPart eye_ridge_R;
    private final ModelPart anntena_1_L;
    private final ModelPart anntena_1_R;
    private final ModelPart anntena_2_L;
    private final ModelPart anntena_2_R;
    private final ModelPart arm_1_L;
    private final ModelPart arm_2_L;
    private final ModelPart wing_1_L;
    private final ModelPart wing_2_L;
    private final ModelPart arm_1_R;
    private final ModelPart arm_2_R;
    private final ModelPart wing_1_R;
    private final ModelPart wing_2_R;
    private final ModelPart leg_1_L;
    private final ModelPart leg_1_R;
    private final ModelPart leg_2_L;
    private final ModelPart leg_2_R;
    private final ModelPart footL;
    private final ModelPart footR;
    private final ModelPart wing_3_L;
    private final ModelPart wing_3_R;
    private final ModelPart wing_4_L;
    private final ModelPart wing_4_R;
    private final ModelPart claw_L;
    private final ModelPart claw_R;
    private final ModelPart claw_L2;
    private final ModelPart claw_R_2;
    private final ModelPart wing_5_L;
    private final ModelPart wing_6_L;
    private final ModelPart wing_7_L;
    private final ModelPart wing_5_R;
    private final ModelPart wing_6_R;
    private final ModelPart wing_7_R;
    private final ModelPart fchest;
    private final ModelPart fneck_1;
    private final ModelPart fneck_2;
    private final ModelPart fneck_3;
    private final ModelPart fabdomen;
    private final ModelPart fhead;
    private final ModelPart fupper_jaw;
    private final ModelPart fbottom_jaw;
    private final ModelPart fchest_ridge;
    private final ModelPart fupper_sail_1;
    private final ModelPart fupper_sail2_;
    private final ModelPart fupper_sail3;
    private final ModelPart flower_sail1;
    private final ModelPart flower_sail2;
    private final ModelPart flower_sail_3;
    private final ModelPart feye_ridge_L;
    private final ModelPart feye_ridge_R;
    private final ModelPart fanntena_1_L;
    private final ModelPart fanntena_1_R;
    private final ModelPart fanntena_2_L;
    private final ModelPart fanntena_2_R;
    private final ModelPart farm_1_L;
    private final ModelPart farm_2_L;
    private final ModelPart fwing_1_L;
    private final ModelPart fwing_2_L;
    private final ModelPart farm_1_R;
    private final ModelPart farm_2_R;
    private final ModelPart fwing_1_R;
    private final ModelPart fwing_2_R;
    private final ModelPart fleg_1_L;
    private final ModelPart fleg_1_R;
    private final ModelPart fleg_2_L;
    private final ModelPart fleg_2_R;
    private final ModelPart ffootL;
    private final ModelPart ffootR;
    private final ModelPart fwing_3_L;
    private final ModelPart fwing_3_R;
    private final ModelPart fwing_4_L;
    private final ModelPart fwing_4_R;
    private final ModelPart fclaw_L;
    private final ModelPart fclaw_R;
    private final ModelPart fclaw_L2;
    private final ModelPart fclaw_R_2;
    private final ModelPart fwing_5_L;
    private final ModelPart fwing_6_L;
    private final ModelPart fwing_7_L;
    private final ModelPart fwing_5_R;
    private final ModelPart fwing_6_R;
    private final ModelPart fwing_7_R;

    /** Every part, reset at the start of {@link #setupAnim} (R8). */
    private final ModelPart[] all;
    /** The ground set in the draw order of {@code render()} (:733-781). */
    private final ModelPart[] ground;
    /** The flight set in the draw order of {@code render()} (:1030-1078). */
    private final ModelPart[] flight;

    /** {@code e.getActivity()} as read by {@code render()} (:627), carried from {@link #setupAnim}. */
    private int activity = 0;

    /** {@code ModelLeon(float f1)} (:109-604). */
    public LeonModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.chest = root.getChild(LeonGeometry.CHEST);
        this.neck_1 = root.getChild(LeonGeometry.NECK_1);
        this.neck_2 = root.getChild(LeonGeometry.NECK_2);
        this.neck_3 = root.getChild(LeonGeometry.NECK_3);
        this.abdomen = root.getChild(LeonGeometry.ABDOMEN);
        this.head = root.getChild(LeonGeometry.HEAD);
        this.upper_jaw = root.getChild(LeonGeometry.UPPER_JAW);
        this.bottom_jaw = root.getChild(LeonGeometry.BOTTOM_JAW);
        this.chest_ridge = root.getChild(LeonGeometry.CHEST_RIDGE);
        this.upper_sail_1 = root.getChild(LeonGeometry.UPPER_SAIL_1);
        this.upper_sail2_ = root.getChild(LeonGeometry.UPPER_SAIL2_);
        this.upper_sail3 = root.getChild(LeonGeometry.UPPER_SAIL3);
        this.lower_sail1 = root.getChild(LeonGeometry.LOWER_SAIL1);
        this.lower_sail2 = root.getChild(LeonGeometry.LOWER_SAIL2);
        this.lower_sail_3 = root.getChild(LeonGeometry.LOWER_SAIL_3);
        this.eye_ridge_L = root.getChild(LeonGeometry.EYE_RIDGE_L);
        this.eye_ridge_R = root.getChild(LeonGeometry.EYE_RIDGE_R);
        this.anntena_1_L = root.getChild(LeonGeometry.ANNTENA_1_L);
        this.anntena_1_R = root.getChild(LeonGeometry.ANNTENA_1_R);
        this.anntena_2_L = root.getChild(LeonGeometry.ANNTENA_2_L);
        this.anntena_2_R = root.getChild(LeonGeometry.ANNTENA_2_R);
        this.arm_1_L = root.getChild(LeonGeometry.ARM_1_L);
        this.arm_2_L = root.getChild(LeonGeometry.ARM_2_L);
        this.wing_1_L = root.getChild(LeonGeometry.WING_1_L);
        this.wing_2_L = root.getChild(LeonGeometry.WING_2_L);
        this.arm_1_R = root.getChild(LeonGeometry.ARM_1_R);
        this.arm_2_R = root.getChild(LeonGeometry.ARM_2_R);
        this.wing_1_R = root.getChild(LeonGeometry.WING_1_R);
        this.wing_2_R = root.getChild(LeonGeometry.WING_2_R);
        this.leg_1_L = root.getChild(LeonGeometry.LEG_1_L);
        this.leg_1_R = root.getChild(LeonGeometry.LEG_1_R);
        this.leg_2_L = root.getChild(LeonGeometry.LEG_2_L);
        this.leg_2_R = root.getChild(LeonGeometry.LEG_2_R);
        this.footL = root.getChild(LeonGeometry.FOOT_L);
        this.footR = root.getChild(LeonGeometry.FOOT_R);
        this.wing_3_L = root.getChild(LeonGeometry.WING_3_L);
        this.wing_3_R = root.getChild(LeonGeometry.WING_3_R);
        this.wing_4_L = root.getChild(LeonGeometry.WING_4_L);
        this.wing_4_R = root.getChild(LeonGeometry.WING_4_R);
        this.claw_L = root.getChild(LeonGeometry.CLAW_L);
        this.claw_R = root.getChild(LeonGeometry.CLAW_R);
        this.claw_L2 = root.getChild(LeonGeometry.CLAW_L2);
        this.claw_R_2 = root.getChild(LeonGeometry.CLAW_R_2);
        this.wing_5_L = root.getChild(LeonGeometry.WING_5_L);
        this.wing_6_L = root.getChild(LeonGeometry.WING_6_L);
        this.wing_7_L = root.getChild(LeonGeometry.WING_7_L);
        this.wing_5_R = root.getChild(LeonGeometry.WING_5_R);
        this.wing_6_R = root.getChild(LeonGeometry.WING_6_R);
        this.wing_7_R = root.getChild(LeonGeometry.WING_7_R);
        this.fchest = root.getChild(LeonGeometry.FCHEST);
        this.fneck_1 = root.getChild(LeonGeometry.FNECK_1);
        this.fneck_2 = root.getChild(LeonGeometry.FNECK_2);
        this.fneck_3 = root.getChild(LeonGeometry.FNECK_3);
        this.fabdomen = root.getChild(LeonGeometry.FABDOMEN);
        this.fhead = root.getChild(LeonGeometry.FHEAD);
        this.fupper_jaw = root.getChild(LeonGeometry.FUPPER_JAW);
        this.fbottom_jaw = root.getChild(LeonGeometry.FBOTTOM_JAW);
        this.fchest_ridge = root.getChild(LeonGeometry.FCHEST_RIDGE);
        this.fupper_sail_1 = root.getChild(LeonGeometry.FUPPER_SAIL_1);
        this.fupper_sail2_ = root.getChild(LeonGeometry.FUPPER_SAIL2_);
        this.fupper_sail3 = root.getChild(LeonGeometry.FUPPER_SAIL3);
        this.flower_sail1 = root.getChild(LeonGeometry.FLOWER_SAIL1);
        this.flower_sail2 = root.getChild(LeonGeometry.FLOWER_SAIL2);
        this.flower_sail_3 = root.getChild(LeonGeometry.FLOWER_SAIL_3);
        this.feye_ridge_L = root.getChild(LeonGeometry.FEYE_RIDGE_L);
        this.feye_ridge_R = root.getChild(LeonGeometry.FEYE_RIDGE_R);
        this.fanntena_1_L = root.getChild(LeonGeometry.FANNTENA_1_L);
        this.fanntena_1_R = root.getChild(LeonGeometry.FANNTENA_1_R);
        this.fanntena_2_L = root.getChild(LeonGeometry.FANNTENA_2_L);
        this.fanntena_2_R = root.getChild(LeonGeometry.FANNTENA_2_R);
        this.farm_1_L = root.getChild(LeonGeometry.FARM_1_L);
        this.farm_2_L = root.getChild(LeonGeometry.FARM_2_L);
        this.fwing_1_L = root.getChild(LeonGeometry.FWING_1_L);
        this.fwing_2_L = root.getChild(LeonGeometry.FWING_2_L);
        this.farm_1_R = root.getChild(LeonGeometry.FARM_1_R);
        this.farm_2_R = root.getChild(LeonGeometry.FARM_2_R);
        this.fwing_1_R = root.getChild(LeonGeometry.FWING_1_R);
        this.fwing_2_R = root.getChild(LeonGeometry.FWING_2_R);
        this.fleg_1_L = root.getChild(LeonGeometry.FLEG_1_L);
        this.fleg_1_R = root.getChild(LeonGeometry.FLEG_1_R);
        this.fleg_2_L = root.getChild(LeonGeometry.FLEG_2_L);
        this.fleg_2_R = root.getChild(LeonGeometry.FLEG_2_R);
        this.ffootL = root.getChild(LeonGeometry.FFOOT_L);
        this.ffootR = root.getChild(LeonGeometry.FFOOT_R);
        this.fwing_3_L = root.getChild(LeonGeometry.FWING_3_L);
        this.fwing_3_R = root.getChild(LeonGeometry.FWING_3_R);
        this.fwing_4_L = root.getChild(LeonGeometry.FWING_4_L);
        this.fwing_4_R = root.getChild(LeonGeometry.FWING_4_R);
        this.fclaw_L = root.getChild(LeonGeometry.FCLAW_L);
        this.fclaw_R = root.getChild(LeonGeometry.FCLAW_R);
        this.fclaw_L2 = root.getChild(LeonGeometry.FCLAW_L2);
        this.fclaw_R_2 = root.getChild(LeonGeometry.FCLAW_R_2);
        this.fwing_5_L = root.getChild(LeonGeometry.FWING_5_L);
        this.fwing_6_L = root.getChild(LeonGeometry.FWING_6_L);
        this.fwing_7_L = root.getChild(LeonGeometry.FWING_7_L);
        this.fwing_5_R = root.getChild(LeonGeometry.FWING_5_R);
        this.fwing_6_R = root.getChild(LeonGeometry.FWING_6_R);
        this.fwing_7_R = root.getChild(LeonGeometry.FWING_7_R);
        this.ground = new ModelPart[] {
            this.chest, this.neck_1, this.neck_2, this.neck_3, this.abdomen, this.head, this.upper_jaw,
            this.bottom_jaw, this.chest_ridge, this.upper_sail_1, this.upper_sail2_, this.upper_sail3,
            this.lower_sail1, this.lower_sail2, this.lower_sail_3, this.eye_ridge_L, this.eye_ridge_R,
            this.anntena_1_L, this.anntena_1_R, this.anntena_2_L, this.anntena_2_R, this.arm_1_L, this.arm_2_L,
            this.wing_1_L, this.wing_2_L, this.arm_1_R, this.arm_2_R, this.wing_1_R, this.wing_2_R, this.leg_1_L,
            this.leg_1_R, this.leg_2_L, this.leg_2_R, this.footL, this.footR, this.wing_3_L, this.wing_3_R,
            this.wing_4_L, this.wing_4_R, this.claw_L, this.claw_R, this.claw_L2, this.claw_R_2, this.wing_5_L,
            this.wing_6_L, this.wing_7_L, this.wing_5_R, this.wing_6_R, this.wing_7_R
        };
        this.flight = new ModelPart[] {
            this.fchest, this.fneck_1, this.fneck_2, this.fneck_3, this.fabdomen, this.fhead, this.fupper_jaw,
            this.fbottom_jaw, this.fchest_ridge, this.fupper_sail_1, this.fupper_sail2_, this.fupper_sail3,
            this.flower_sail1, this.flower_sail2, this.flower_sail_3, this.feye_ridge_L, this.feye_ridge_R,
            this.fanntena_1_L, this.fanntena_1_R, this.fanntena_2_L, this.fanntena_2_R, this.farm_1_L, this.farm_2_L,
            this.fwing_1_L, this.fwing_2_L, this.farm_1_R, this.farm_2_R, this.fwing_1_R, this.fwing_2_R,
            this.fleg_1_L, this.fleg_1_R, this.fleg_2_L, this.fleg_2_R, this.ffootL, this.ffootR, this.fwing_3_L,
            this.fwing_3_R, this.fwing_4_L, this.fwing_4_R, this.fclaw_L, this.fclaw_R, this.fclaw_L2,
            this.fclaw_R_2, this.fwing_5_L, this.fwing_6_L, this.fwing_7_L, this.fwing_5_R, this.fwing_6_R,
            this.fwing_7_R
        };
        this.all = new ModelPart[this.ground.length + this.flight.length];
        System.arraycopy(this.ground, 0, this.all, 0, this.ground.length);
        System.arraycopy(this.flight, 0, this.all, this.ground.length, this.flight.length);
    }

    /**
     * The writes of {@code render()} (:606-1029), in the original order. {@code f3} is the head yaw relative to the
     * body as in 1.7.10 (research 06, verified conventions); {@code super.render} and {@code setRotationAngles} were
     * empty.
     */
    @Override
    public void setupAnim(final Leon entity, final float f, final float f1, final float f2, float f3, final float f4) {
        for (final ModelPart part : this.all) {
            part.resetPose();
        }
        final Leon e = entity;
        RenderInfo r = null;
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        float spd = 1.0f;
        float amp = 1.0f;
        this.activity = e.getActivity();
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.8f * this.wingspeed) * 3.1415927f * 0.25f * f1;
            newangle2 = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        }
        else {
            newangle = 0.0f;
            newangle2 = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.02f;
            if (e.isSitting()) {
                newangle2 = 0.0f;
            }
        }
        if (e.getActivity() == 0) {
            this.leg_1_L.xRot = -0.611f + newangle;
            this.leg_1_R.xRot = -0.611f - newangle;
            this.leg_2_L.xRot = 0.611f + newangle;
            this.leg_2_L.z = (float)(this.leg_1_L.z + Math.sin(this.leg_1_L.xRot) * 9.0);
            this.leg_2_L.y = (float)(this.leg_1_L.y + Math.cos(this.leg_1_L.xRot) * 9.0);
            this.leg_2_R.xRot = 0.611f - newangle;
            this.leg_2_R.z = (float)(this.leg_1_R.z + Math.sin(this.leg_1_R.xRot) * 9.0);
            this.leg_2_R.y = (float)(this.leg_1_R.y + Math.cos(this.leg_1_R.xRot) * 9.0);
            this.footL.z = (float)(this.leg_2_L.z + Math.sin(this.leg_2_L.xRot) * 13.0);
            this.footL.y = (float)(this.leg_2_L.y + Math.cos(this.leg_2_L.xRot) * 13.0);
            this.footR.z = (float)(this.leg_2_R.z + Math.sin(this.leg_2_R.xRot) * 11.0);
            this.footR.y = (float)(this.leg_2_R.y + Math.cos(this.leg_2_R.xRot) * 11.0);
            this.wing_3_R.yRot = 0.523f - newangle / 10.0f;
            this.wing_3_L.yRot = -0.523f - newangle / 10.0f;
            newangle /= 2.0f;
            this.arm_1_L.xRot = -0.07f - newangle;
            this.arm_1_R.xRot = -0.07f + newangle;
            this.wing_1_L.xRot = -0.17f - newangle;
            this.wing_1_R.xRot = -0.17f + newangle;
            this.arm_2_L.xRot = -0.471f - newangle;
            this.wing_2_L.xRot = -0.523f - newangle;
            final ModelPart arm_2_L = this.arm_2_L;
            final ModelPart wing_2_L = this.wing_2_L;
            final float n = (float)(this.arm_1_L.z + Math.sin(this.arm_1_L.xRot) * 11.0);
            wing_2_L.z = n;
            arm_2_L.z = n;
            final ModelPart arm_2_L2 = this.arm_2_L;
            final ModelPart wing_2_L2 = this.wing_2_L;
            final float n2 = (float)(this.arm_1_L.y + Math.cos(this.arm_1_L.xRot) * 11.0);
            wing_2_L2.y = n2;
            arm_2_L2.y = n2;
            this.wing_5_L.xRot = 0.68f + newangle2 / 2.0f;
            this.wing_6_L.xRot = 0.453f + newangle2 / 4.0f;
            this.wing_7_R.xRot = 0.119f + newangle2 / 8.0f;
            this.wing_5_L.z = (float)(this.arm_2_L.z + Math.sin(this.arm_2_L.xRot) * 20.0);
            this.wing_5_L.y = (float)(this.arm_2_L.y + Math.cos(this.arm_2_L.xRot) * 20.0);
            this.wing_6_L.z = this.wing_5_L.z;
            this.wing_6_L.y = this.wing_5_L.y;
            this.wing_7_R.z = this.wing_5_L.z;
            this.wing_7_R.y = this.wing_5_L.y;
            this.claw_L.z = this.wing_5_L.z - 1.0f;
            this.claw_R_2.z = this.wing_5_L.z - 9.0f;
            this.claw_L.y = this.wing_5_L.y + 2.0f;
            this.claw_R_2.y = this.wing_5_L.y + 2.0f;
            this.arm_2_R.xRot = -0.471f + newangle;
            this.wing_2_R.xRot = -0.523f + newangle;
            final ModelPart arm_2_R = this.arm_2_R;
            final ModelPart wing_2_R = this.wing_2_R;
            final float n3 = (float)(this.arm_1_R.z + Math.sin(this.arm_1_R.xRot) * 11.0);
            wing_2_R.z = n3;
            arm_2_R.z = n3;
            final ModelPart arm_2_R2 = this.arm_2_R;
            final ModelPart wing_2_R2 = this.wing_2_R;
            final float n4 = (float)(this.arm_1_R.y + Math.cos(this.arm_1_R.xRot) * 11.0);
            wing_2_R2.y = n4;
            arm_2_R2.y = n4;
            this.wing_5_R.xRot = 0.68f + newangle2 / 2.0f;
            this.wing_6_R.xRot = 0.453f + newangle2 / 4.0f;
            this.wing_7_L.xRot = 0.119f + newangle2 / 8.0f;
            this.wing_5_R.z = (float)(this.arm_2_R.z + Math.sin(this.arm_2_R.xRot) * 20.0);
            this.wing_5_R.y = (float)(this.arm_2_R.y + Math.cos(this.arm_2_R.xRot) * 20.0);
            this.wing_6_R.z = this.wing_5_R.z;
            this.wing_6_R.y = this.wing_5_R.y;
            this.wing_7_L.z = this.wing_5_R.z;
            this.wing_7_L.y = this.wing_5_R.y;
            this.claw_R.z = this.wing_5_R.z - 1.0f;
            this.claw_L2.z = this.wing_5_R.z - 9.0f;
            this.claw_R.y = this.wing_5_R.y + 2.0f;
            this.claw_L2.y = this.wing_5_R.y + 2.0f;
            newangle2 = Mth.cos(f2 * 0.6f * this.wingspeed) * 3.1415927f * 0.02f;
            this.chest.xRot = -0.436f + newangle2 / 8.0f;
            this.chest_ridge.xRot = this.chest.xRot;
            this.bottom_jaw.xRot = -1.308f + newangle2 / 2.0f;
            this.lower_sail1.xRot = 0.297f + newangle2 / 2.0f;
            this.lower_sail2.xRot = 0.384f + newangle2 / 2.0f;
            this.lower_sail_3.xRot = -0.384f + newangle2 / 2.0f;
            newangle = (float)Math.toRadians(f3) * 0.5f;
            final ModelPart head = this.head;
            final ModelPart upper_jaw = this.upper_jaw;
            final ModelPart upper_sail_1 = this.upper_sail_1;
            final ModelPart upper_sail2_ = this.upper_sail2_;
            final ModelPart upper_sail3 = this.upper_sail3;
            final float rotateAngleY = newangle;
            upper_sail3.yRot = rotateAngleY;
            upper_sail2_.yRot = rotateAngleY;
            upper_sail_1.yRot = rotateAngleY;
            upper_jaw.yRot = rotateAngleY;
            head.yRot = rotateAngleY;
            this.eye_ridge_L.yRot = 0.558f + newangle;
            this.anntena_1_L.yRot = 0.366f + newangle;
            this.anntena_2_L.yRot = 0.139f + newangle;
            this.eye_ridge_R.yRot = -0.558f + newangle;
            this.anntena_1_R.yRot = -0.366f + newangle;
            this.anntena_2_R.yRot = -0.139f + newangle;
            final ModelPart bottom_jaw = this.bottom_jaw;
            final ModelPart lower_sail1 = this.lower_sail1;
            final ModelPart lower_sail2 = this.lower_sail2;
            final ModelPart lower_sail_3 = this.lower_sail_3;
            final float n5 = newangle;
            lower_sail_3.yRot = n5;
            lower_sail2.yRot = n5;
            lower_sail1.yRot = n5;
            bottom_jaw.yRot = n5;
            this.bottom_jaw.z = (float)(this.head.z - Math.cos(newangle) * 5.0);
            this.bottom_jaw.x = (float)(this.head.x - Math.sin(newangle) * 5.0);
        }
        else {
            if (e.getAttacking() != 0) {
                spd = 1.7f;
                amp = 1.4f;
            }
            newangle2 = Mth.cos(f2 * 1.6f * this.wingspeed * spd) * 3.1415927f * 0.06f;
            this.fchest.xRot = newangle2 / 8.0f;
            this.fchest_ridge.xRot = -0.18f + this.fchest.xRot;
            if (e.getBeingRidden() == 0) {
                this.fchest.y = (float)(-2.0 + Math.sin(newangle2) * 10.0 * amp);
            }
            else {
                this.fchest.y = -2.0f;
            }
            this.fchest_ridge.y = this.fchest.y;
            this.fabdomen.xRot = 0.0f;
            this.fabdomen.z = (float)(this.fchest.z + Math.cos(this.fchest.xRot) * 8.0);
            this.fabdomen.y = (float)(this.fchest.y - Math.sin(this.fchest.xRot) * 8.0 - 6.0);
            this.fwing_3_R.y = this.fabdomen.y;
            this.fwing_3_L.y = this.fabdomen.y;
            final ModelPart fwing_3_R = this.fwing_3_R;
            final ModelPart fwing_3_R2 = this.fwing_3_R;
            final float n6 = 0.0f;
            fwing_3_R2.zRot = n6;
            fwing_3_R.xRot = n6;
            final ModelPart fwing_3_L = this.fwing_3_L;
            final ModelPart fwing_3_L2 = this.fwing_3_L;
            final float n7 = 0.0f;
            fwing_3_L2.zRot = n7;
            fwing_3_L.xRot = n7;
            this.fwing_3_R.yRot = 0.785f;
            this.fwing_3_L.yRot = -0.785f;
            this.fwing_4_R.y = this.fabdomen.y + 0.55f;
            this.fwing_4_L.y = this.fabdomen.y + 0.55f;
            this.fwing_4_R.z = this.fabdomen.z + 26.0f;
            this.fwing_4_L.z = this.fabdomen.z + 26.0f;
            this.fwing_4_R.x = this.fabdomen.z + 8.0f;
            this.fwing_4_L.x = this.fabdomen.z - 9.0f;
            this.fwing_4_R.xRot = newangle2 / 10.0f;
            this.fwing_4_L.xRot = -newangle2 / 10.0f;
            if (e.getAttacking() == 0) {
                newangle = 1.5707964f;
                this.fleg_1_L.y = this.fabdomen.y + 5.0f;
                this.fleg_1_R.y = this.fabdomen.y + 5.0f;
                this.fleg_1_L.xRot = -0.1f + newangle;
                this.fleg_1_R.xRot = -0.1f + newangle;
                this.fleg_2_L.xRot = 0.1f + newangle;
                this.fleg_2_L.z = (float)(this.fleg_1_L.z + Math.sin(this.fleg_1_L.xRot) * 9.0);
                this.fleg_2_L.y = (float)(this.fleg_1_L.y + Math.cos(this.fleg_1_L.xRot) * 9.0);
                this.fleg_2_R.xRot = 0.1f + newangle;
                this.fleg_2_R.z = (float)(this.fleg_1_R.z + Math.sin(this.fleg_1_R.xRot) * 9.0);
                this.fleg_2_R.y = (float)(this.fleg_1_R.y + Math.cos(this.fleg_1_R.xRot) * 9.0);
                this.ffootL.z = (float)(this.fleg_2_L.z + Math.sin(this.fleg_2_L.xRot) * 13.0);
                this.ffootL.y = (float)(this.fleg_2_L.y + Math.cos(this.fleg_2_L.xRot) * 13.0);
                this.ffootR.z = (float)(this.fleg_2_R.z + Math.sin(this.fleg_2_R.xRot) * 11.0);
                this.ffootR.y = (float)(this.fleg_2_R.y + Math.cos(this.fleg_2_R.xRot) * 11.0);
                this.ffootL.xRot = 3.1415927f;
                this.ffootR.xRot = 3.1415927f;
                this.fleg_2_L.x = this.fleg_1_L.x;
                this.ffootL.x = this.fleg_1_L.x;
                this.fleg_2_R.x = this.fleg_1_R.x;
                this.ffootR.x = this.fleg_1_R.x;
            }
            else {
                newangle = -0.7853982f;
                newangle3 = Mth.cos(f2 * 3.6f * this.wingspeed) * 3.1415927f * 0.1f;
                this.fleg_1_L.y = this.fabdomen.y + 5.0f;
                this.fleg_1_R.y = this.fabdomen.y + 5.0f;
                this.fleg_1_L.xRot = -0.1f + newangle + newangle3;
                this.fleg_1_R.xRot = -0.1f + newangle - newangle3;
                this.fleg_2_L.xRot = 0.2f + newangle + newangle3 * 3.0f / 2.0f;
                this.fleg_2_L.z = (float)(this.fleg_1_L.z + Math.sin(this.fleg_1_L.xRot) * 9.0);
                this.fleg_2_L.y = (float)(this.fleg_1_L.y + Math.cos(this.fleg_1_L.xRot) * 9.0);
                this.fleg_2_R.xRot = 0.2f + newangle - newangle3 * 3.0f / 2.0f;
                this.fleg_2_R.z = (float)(this.fleg_1_R.z + Math.sin(this.fleg_1_R.xRot) * 9.0);
                this.fleg_2_R.y = (float)(this.fleg_1_R.y + Math.cos(this.fleg_1_R.xRot) * 9.0);
                this.ffootL.z = (float)(this.fleg_2_L.z + Math.sin(this.fleg_2_L.xRot) * 13.0);
                this.ffootL.y = (float)(this.fleg_2_L.y + Math.cos(this.fleg_2_L.xRot) * 13.0);
                this.ffootR.z = (float)(this.fleg_2_R.z + Math.sin(this.fleg_2_R.xRot) * 11.0);
                this.ffootR.y = (float)(this.fleg_2_R.y + Math.cos(this.fleg_2_R.xRot) * 11.0);
                this.ffootL.xRot = -0.7853982f + newangle3 * 2.0f;
                this.ffootR.xRot = -0.7853982f - newangle3 * 2.0f;
                this.fleg_2_L.x = 7.0f;
                this.ffootL.x = 11.0f;
                this.fleg_2_R.x = -9.0f;
                this.ffootR.x = -13.0f;
            }
            newangle = Mth.cos(f2 * 1.6f * this.wingspeed * spd) * 3.1415927f * 0.26f * amp;
            this.farm_1_L.zRot = (float)(-1.5707963267948966 - newangle);
            this.farm_1_R.zRot = (float)(1.5707963267948966 + newangle);
            this.fwing_1_L.zRot = (float)(-1.5707963267948966 - newangle);
            this.fwing_1_R.zRot = (float)(1.5707963267948966 + newangle);
            this.farm_2_L.zRot = (float)(-1.5707963267948966 - newangle * 1.3f);
            this.fwing_2_L.zRot = (float)(-1.5707963267948966 - newangle * 1.3f);
            final ModelPart farm_2_L = this.farm_2_L;
            final ModelPart fwing_2_L = this.fwing_2_L;
            final float n8 = (float)(this.farm_1_L.x + Math.cos(newangle) * 14.0);
            fwing_2_L.x = n8;
            farm_2_L.x = n8;
            final ModelPart farm_2_L2 = this.farm_2_L;
            final ModelPart fwing_2_L2 = this.fwing_2_L;
            final float n9 = (float)(this.farm_1_L.y - Math.sin(newangle) * 14.0);
            fwing_2_L2.y = n9;
            farm_2_L2.y = n9;
            this.fwing_5_L.x = (float)(this.farm_2_L.x + Math.cos(newangle * 1.3f) * 20.0);
            this.fwing_5_L.y = (float)(this.farm_2_L.y - Math.sin(newangle * 1.3f) * 20.0);
            this.fwing_6_L.x = this.fwing_5_L.x;
            this.fwing_6_L.y = this.fwing_5_L.y;
            this.fwing_7_R.x = this.fwing_5_L.x;
            this.fwing_7_R.y = this.fwing_5_L.y;
            this.fclaw_L.x = this.fwing_5_L.x;
            this.fclaw_R_2.x = this.fwing_5_L.x;
            this.fclaw_L.y = this.fwing_5_L.y;
            this.fclaw_R_2.y = this.fwing_5_L.y;
            this.fwing_5_L.zRot = (float)(-1.5707963267948966 - newangle * 1.65f);
            this.fwing_6_L.zRot = (float)(-1.5707963267948966 - newangle * 1.65f);
            this.fwing_7_R.zRot = (float)(-1.5707963267948966 - newangle * 1.65f);
            this.fwing_7_R.xRot = -1.5707964f;
            this.fwing_6_L.xRot = -1.1780972f;
            this.fwing_5_L.xRot = -0.7853982f;
            this.farm_2_R.zRot = (float)(1.5707963267948966 + newangle * 1.3f);
            this.fwing_2_R.zRot = (float)(1.5707963267948966 + newangle * 1.3f);
            final ModelPart farm_2_R = this.farm_2_R;
            final ModelPart fwing_2_R = this.fwing_2_R;
            final float n10 = (float)(this.farm_1_R.x - Math.cos(newangle) * 14.0);
            fwing_2_R.x = n10;
            farm_2_R.x = n10;
            final ModelPart farm_2_R2 = this.farm_2_R;
            final ModelPart fwing_2_R2 = this.fwing_2_R;
            final float n11 = (float)(this.farm_1_R.y - Math.sin(newangle) * 14.0);
            fwing_2_R2.y = n11;
            farm_2_R2.y = n11;
            this.fwing_5_R.x = (float)(this.farm_2_R.x - Math.cos(newangle * 1.3f) * 20.0);
            this.fwing_5_R.y = (float)(this.farm_2_R.y - Math.sin(newangle * 1.3f) * 20.0);
            this.fwing_6_R.x = this.fwing_5_R.x;
            this.fwing_6_R.y = this.fwing_5_R.y;
            this.fwing_7_L.x = this.fwing_5_R.x;
            this.fwing_7_L.y = this.fwing_5_R.y;
            this.fclaw_R.x = this.fwing_5_R.x;
            this.fclaw_L2.x = this.fwing_5_R.x;
            this.fclaw_R.y = this.fwing_5_R.y;
            this.fclaw_L2.y = this.fwing_5_R.y;
            this.fwing_5_R.zRot = (float)(1.5707963267948966 + newangle * 1.65f);
            this.fwing_6_R.zRot = (float)(1.5707963267948966 + newangle * 1.65f);
            this.fwing_7_L.zRot = (float)(1.5707963267948966 + newangle * 1.65f);
            this.fwing_7_L.xRot = -1.5707964f;
            this.fwing_6_R.xRot = -1.1780972f;
            this.fwing_5_R.xRot = -0.7853982f;
            this.fneck_1.xRot = -newangle / 12.0f;
            this.fneck_1.z = (float)(this.fchest.z - Math.cos(this.fchest.xRot) * 10.0);
            this.fneck_1.y = (float)(this.fchest.y + Math.sin(this.fchest.xRot) * 8.0 - 1.0);
            this.fneck_2.xRot = -newangle / 10.0f;
            this.fneck_2.z = (float)(this.fneck_1.z - Math.cos(this.fneck_1.xRot) * 7.0);
            this.fneck_2.y = (float)(this.fneck_1.y + Math.sin(this.fneck_1.xRot) * 6.0 - 1.0);
            this.fneck_3.xRot = -newangle / 8.0f;
            this.fneck_3.z = (float)(this.fneck_2.z - Math.cos(this.fneck_2.xRot) * 7.0);
            this.fneck_3.y = (float)(this.fneck_2.y + Math.sin(this.fneck_2.xRot) * 5.0);
            this.fhead.z = (float)(this.fneck_3.z - Math.cos(this.fneck_3.xRot) * 16.0);
            this.fhead.y = (float)(this.fneck_3.y + Math.sin(this.fneck_3.xRot) * 15.0);
            this.fupper_jaw.z = this.fhead.z;
            this.fupper_sail_1.z = this.fhead.z;
            this.fupper_sail2_.z = this.fhead.z;
            this.fupper_sail3.z = this.fhead.z;
            this.feye_ridge_L.z = this.fhead.z;
            this.fanntena_1_L.z = this.fhead.z;
            this.fanntena_2_L.z = this.fhead.z;
            this.feye_ridge_R.z = this.fhead.z;
            this.fanntena_1_R.z = this.fhead.z;
            this.fanntena_2_R.z = this.fhead.z;
            this.fbottom_jaw.z = this.fhead.z - 5.0f;
            this.flower_sail1.z = this.fhead.z - 5.0f;
            this.flower_sail2.z = this.fhead.z - 5.0f;
            this.flower_sail_3.z = this.fhead.z - 5.0f;
            this.fupper_jaw.y = this.fhead.y;
            this.fupper_sail_1.y = this.fhead.y;
            this.fupper_sail2_.y = this.fhead.y;
            this.fupper_sail3.y = this.fhead.y;
            this.feye_ridge_L.y = this.fhead.y;
            this.fanntena_1_L.y = this.fhead.y;
            this.fanntena_2_L.y = this.fhead.y;
            this.feye_ridge_R.y = this.fhead.y;
            this.fanntena_1_R.y = this.fhead.y;
            this.fanntena_2_R.y = this.fhead.y;
            this.fbottom_jaw.y = this.fhead.y + 4.0f;
            this.flower_sail1.y = this.fhead.y + 4.0f;
            this.flower_sail2.y = this.fhead.y + 4.0f;
            this.flower_sail_3.y = this.fhead.y + 4.0f;
            if (e.getBeingRidden() == 0) {
                newangle = (float)Math.toRadians(f3) * 0.5f;
            }
            else {
                r = e.getRenderInfo();
                f3 = (e.yRotO - e.getYRot()) * 8.0f;
                f3 = -f3;
                final RenderInfo renderInfo = r;
                renderInfo.rf1 += (f3 - r.rf1) / 60.0f;
                if (r.rf1 > 50.0f) {
                    r.rf1 = 50.0f;
                }
                if (r.rf1 < -50.0f) {
                    r.rf1 = -50.0f;
                }
                f3 = r.rf1;
                e.setRenderInfo(r);
                newangle = (float)Math.toRadians(f3) * 0.5f;
            }
            final ModelPart fhead = this.fhead;
            final ModelPart fupper_jaw = this.fupper_jaw;
            final ModelPart fupper_sail_1 = this.fupper_sail_1;
            final ModelPart fupper_sail2_ = this.fupper_sail2_;
            final ModelPart fupper_sail3 = this.fupper_sail3;
            final float rotateAngleY2 = newangle;
            fupper_sail3.yRot = rotateAngleY2;
            fupper_sail2_.yRot = rotateAngleY2;
            fupper_sail_1.yRot = rotateAngleY2;
            fupper_jaw.yRot = rotateAngleY2;
            fhead.yRot = rotateAngleY2;
            this.feye_ridge_L.yRot = 0.558f + newangle;
            this.fanntena_1_L.yRot = 0.366f + newangle;
            this.fanntena_2_L.yRot = 0.139f + newangle;
            this.feye_ridge_R.yRot = -0.558f + newangle;
            this.fanntena_1_R.yRot = -0.366f + newangle;
            this.fanntena_2_R.yRot = -0.139f + newangle;
            final ModelPart fbottom_jaw = this.fbottom_jaw;
            final ModelPart flower_sail1 = this.flower_sail1;
            final ModelPart flower_sail2 = this.flower_sail2;
            final ModelPart flower_sail_3 = this.flower_sail_3;
            final float n12 = newangle;
            flower_sail_3.yRot = n12;
            flower_sail2.yRot = n12;
            flower_sail1.yRot = n12;
            fbottom_jaw.yRot = n12;
            this.fbottom_jaw.z = (float)(this.fhead.z - Math.cos(newangle) * 5.0);
            this.fbottom_jaw.x = (float)(this.fhead.x - Math.sin(newangle) * 5.0);
            final float tf1 = 1.605f;
            final float tf2 = 1.6919999f;
            final float tf3 = 0.92399997f;
            if (e.getAttacking() == 0) {
                this.fbottom_jaw.xRot = -1.308f + newangle2 / 2.0f;
            }
            else {
                newangle2 = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.16f;
                this.fbottom_jaw.xRot = -0.9f + newangle2;
            }
            this.flower_sail1.xRot = this.fbottom_jaw.xRot + tf1;
            this.flower_sail2.xRot = this.fbottom_jaw.xRot + tf2;
            this.flower_sail_3.xRot = this.fbottom_jaw.xRot + tf3;
        }
    }

    /** The draw calls of {@code render()}: the ground set (:733-781) or the flight set (:1030-1078). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final ModelPart[] parts = this.activity == 0 ? this.ground : this.flight;
        for (final ModelPart part : parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
