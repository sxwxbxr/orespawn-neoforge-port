package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.LizardGeometry;
import com.swbr.orespawn.entity.cannonfodder.Lizard;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelLizard} (ModelLizard.java:7-672): 71 boxes on a 128x128 texture - three
 * body blocks, four sprawled two-segment legs with feet and toes, a five-link tail, a fin crest, the upper jaw
 * with nose, eyes and teeth, the lower jaw with its teeth, and the battle-mob hat. Geometry (:82-442) is the
 * generated {@link LizardGeometry}.
 *
 * <p>{@code wingspeed} is the constructor argument, 0.65 for the lizard (manifest {@code model_args}). The
 * original faked a hierarchy by rewriting {@code rotationPoint} of dependent parts every frame (tail links, the
 * head parts on the upper jaw); that is written to {@code ModelPart.x/z} here (research 06, "hierarchy").
 * {@code entityCutoutNoCull} as 1.7.10 {@code RendererLivingEntity} drew; the fins are zero-width planes.
 *
 * <p>{@code resetPose()} at the start (R8); every animated field is recomputed each frame, so nothing visible
 * depends on it.
 */
public class LizardModel extends EntityModel<Lizard> {

    /** Register with {@code LizardGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "lizard"), "main");

    private final ModelPart root;
    private final float wingspeed;
    private final ModelPart BodyBack;
    private final ModelPart TopBackLeftLeg;
    private final ModelPart TailTip;
    private final ModelPart BodyFront;
    private final ModelPart TailBase1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Tail4;
    private final ModelPart Neck;
    private final ModelPart TopFrontLeftLeg;
    private final ModelPart TopBackRightLeg;
    private final ModelPart BottomBackRightLeg;
    private final ModelPart TopFrontRightLeg;
    private final ModelPart BottomBackLeftLeg;
    private final ModelPart BottomFrontRightLeg;
    private final ModelPart BottomFrontLeftLeg;
    private final ModelPart BodyCenter;
    private final ModelPart Toe7;
    private final ModelPart Toe6;
    private final ModelPart BackLeftFoot;
    private final ModelPart Toe4;
    private final ModelPart Toe5;
    private final ModelPart BackRightFoot;
    private final ModelPart Toe8;
    private final ModelPart Toe1;
    private final ModelPart FrontLeftFoot;
    private final ModelPart Toe3;
    private final ModelPart Toe2;
    private final ModelPart FrontRightFoot;
    private final ModelPart FinRidge7;
    private final ModelPart FinRidge6;
    private final ModelPart FinRidge5;
    private final ModelPart FinRidge4;
    private final ModelPart FinRidge3;
    private final ModelPart FinRidge2;
    private final ModelPart FinRidge1;
    private final ModelPart Fin10;
    private final ModelPart Fin9;
    private final ModelPart Fin8;
    private final ModelPart Fin7;
    private final ModelPart Fin6;
    private final ModelPart Fin5;
    private final ModelPart Fin3;
    private final ModelPart Fin2;
    private final ModelPart Tooth11;
    private final ModelPart Tooth10;
    private final ModelPart Tooth8;
    private final ModelPart Tooth7;
    private final ModelPart Tooth6;
    private final ModelPart Tooth5;
    private final ModelPart Tooth4;
    private final ModelPart Tooth3;
    private final ModelPart Tooth2;
    private final ModelPart CenterRightNose;
    private final ModelPart CenterLeftNose;
    private final ModelPart Tooth1;
    private final ModelPart BottomNose;
    private final ModelPart TopNose;
    private final ModelPart JawTop;
    private final ModelPart CenterMiddleNose;
    private final ModelPart RightEye;
    private final ModelPart LeftEye;
    private final ModelPart Tooth16;
    private final ModelPart Tooth15;
    private final ModelPart Tooth14;
    private final ModelPart Tooth13;
    private final ModelPart Tooth12;
    private final ModelPart Tooth9;
    private final ModelPart BottomJaw;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    /** {@code ModelLizard(float f1)} (:82-442). */
    public LizardModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.BodyBack = root.getChild(LizardGeometry.BODY_BACK);
        this.TopBackLeftLeg = root.getChild(LizardGeometry.TOP_BACK_LEFT_LEG);
        this.TailTip = root.getChild(LizardGeometry.TAIL_TIP);
        this.BodyFront = root.getChild(LizardGeometry.BODY_FRONT);
        this.TailBase1 = root.getChild(LizardGeometry.TAIL_BASE1);
        this.Tail2 = root.getChild(LizardGeometry.TAIL2);
        this.Tail3 = root.getChild(LizardGeometry.TAIL3);
        this.Tail4 = root.getChild(LizardGeometry.TAIL4);
        this.Neck = root.getChild(LizardGeometry.NECK);
        this.TopFrontLeftLeg = root.getChild(LizardGeometry.TOP_FRONT_LEFT_LEG);
        this.TopBackRightLeg = root.getChild(LizardGeometry.TOP_BACK_RIGHT_LEG);
        this.BottomBackRightLeg = root.getChild(LizardGeometry.BOTTOM_BACK_RIGHT_LEG);
        this.TopFrontRightLeg = root.getChild(LizardGeometry.TOP_FRONT_RIGHT_LEG);
        this.BottomBackLeftLeg = root.getChild(LizardGeometry.BOTTOM_BACK_LEFT_LEG);
        this.BottomFrontRightLeg = root.getChild(LizardGeometry.BOTTOM_FRONT_RIGHT_LEG);
        this.BottomFrontLeftLeg = root.getChild(LizardGeometry.BOTTOM_FRONT_LEFT_LEG);
        this.BodyCenter = root.getChild(LizardGeometry.BODY_CENTER);
        this.Toe7 = root.getChild(LizardGeometry.TOE7);
        this.Toe6 = root.getChild(LizardGeometry.TOE6);
        this.BackLeftFoot = root.getChild(LizardGeometry.BACK_LEFT_FOOT);
        this.Toe4 = root.getChild(LizardGeometry.TOE4);
        this.Toe5 = root.getChild(LizardGeometry.TOE5);
        this.BackRightFoot = root.getChild(LizardGeometry.BACK_RIGHT_FOOT);
        this.Toe8 = root.getChild(LizardGeometry.TOE8);
        this.Toe1 = root.getChild(LizardGeometry.TOE1);
        this.FrontLeftFoot = root.getChild(LizardGeometry.FRONT_LEFT_FOOT);
        this.Toe3 = root.getChild(LizardGeometry.TOE3);
        this.Toe2 = root.getChild(LizardGeometry.TOE2);
        this.FrontRightFoot = root.getChild(LizardGeometry.FRONT_RIGHT_FOOT);
        this.FinRidge7 = root.getChild(LizardGeometry.FIN_RIDGE7);
        this.FinRidge6 = root.getChild(LizardGeometry.FIN_RIDGE6);
        this.FinRidge5 = root.getChild(LizardGeometry.FIN_RIDGE5);
        this.FinRidge4 = root.getChild(LizardGeometry.FIN_RIDGE4);
        this.FinRidge3 = root.getChild(LizardGeometry.FIN_RIDGE3);
        this.FinRidge2 = root.getChild(LizardGeometry.FIN_RIDGE2);
        this.FinRidge1 = root.getChild(LizardGeometry.FIN_RIDGE1);
        this.Fin10 = root.getChild(LizardGeometry.FIN10);
        this.Fin9 = root.getChild(LizardGeometry.FIN9);
        this.Fin8 = root.getChild(LizardGeometry.FIN8);
        this.Fin7 = root.getChild(LizardGeometry.FIN7);
        this.Fin6 = root.getChild(LizardGeometry.FIN6);
        this.Fin5 = root.getChild(LizardGeometry.FIN5);
        this.Fin3 = root.getChild(LizardGeometry.FIN3);
        this.Fin2 = root.getChild(LizardGeometry.FIN2);
        this.Tooth11 = root.getChild(LizardGeometry.TOOTH11);
        this.Tooth10 = root.getChild(LizardGeometry.TOOTH10);
        this.Tooth8 = root.getChild(LizardGeometry.TOOTH8);
        this.Tooth7 = root.getChild(LizardGeometry.TOOTH7);
        this.Tooth6 = root.getChild(LizardGeometry.TOOTH6);
        this.Tooth5 = root.getChild(LizardGeometry.TOOTH5);
        this.Tooth4 = root.getChild(LizardGeometry.TOOTH4);
        this.Tooth3 = root.getChild(LizardGeometry.TOOTH3);
        this.Tooth2 = root.getChild(LizardGeometry.TOOTH2);
        this.CenterRightNose = root.getChild(LizardGeometry.CENTER_RIGHT_NOSE);
        this.CenterLeftNose = root.getChild(LizardGeometry.CENTER_LEFT_NOSE);
        this.Tooth1 = root.getChild(LizardGeometry.TOOTH1);
        this.BottomNose = root.getChild(LizardGeometry.BOTTOM_NOSE);
        this.TopNose = root.getChild(LizardGeometry.TOP_NOSE);
        this.JawTop = root.getChild(LizardGeometry.JAW_TOP);
        this.CenterMiddleNose = root.getChild(LizardGeometry.CENTER_MIDDLE_NOSE);
        this.RightEye = root.getChild(LizardGeometry.RIGHT_EYE);
        this.LeftEye = root.getChild(LizardGeometry.LEFT_EYE);
        this.Tooth16 = root.getChild(LizardGeometry.TOOTH16);
        this.Tooth15 = root.getChild(LizardGeometry.TOOTH15);
        this.Tooth14 = root.getChild(LizardGeometry.TOOTH14);
        this.Tooth13 = root.getChild(LizardGeometry.TOOTH13);
        this.Tooth12 = root.getChild(LizardGeometry.TOOTH12);
        this.Tooth9 = root.getChild(LizardGeometry.TOOTH9);
        this.BottomJaw = root.getChild(LizardGeometry.BOTTOM_JAW);
        this.Hat1 = root.getChild(LizardGeometry.HAT1);
        this.Hat2 = root.getChild(LizardGeometry.HAT2);
    }

    /** Sets {@code rotationPointX/Z} and {@code rotateAngleY} of {@code part} to those of {@code from} (:508-585). */
    private static void follow(final ModelPart part, final ModelPart from) {
        part.z = from.z;
        part.x = from.x;
        part.yRot = from.yRot;
    }

    /**
     * The writes of {@code render()} (:449-585) and the hat condition (:647-652). {@code f1} limb swing amount,
     * {@code f2} age in ticks, {@code f3} head yaw minus body yaw in degrees.
     */
    @Override
    public void setupAnim(final Lizard entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        final Lizard e = entity;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        // :455-474 - the legs swing on Y, the lower segments on X, feet and toes follow on Y.
        this.TopFrontLeftLeg.yRot = newangle;
        this.BottomFrontLeftLeg.xRot = newangle;
        this.FrontLeftFoot.yRot = newangle;
        this.Toe8.yRot = newangle;
        this.Toe1.yRot = newangle;
        this.TopFrontRightLeg.yRot = newangle;
        this.BottomFrontRightLeg.xRot = -newangle;
        this.FrontRightFoot.yRot = newangle;
        this.Toe3.yRot = newangle;
        this.Toe2.yRot = newangle;
        this.TopBackLeftLeg.yRot = -newangle;
        this.BottomBackLeftLeg.xRot = -newangle;
        this.BackLeftFoot.yRot = -newangle;
        this.Toe7.yRot = -newangle;
        this.Toe6.yRot = -newangle;
        this.TopBackRightLeg.yRot = -newangle;
        this.BottomBackRightLeg.xRot = newangle;
        this.BackRightFoot.yRot = -newangle;
        this.Toe4.yRot = -newangle;
        this.Toe5.yRot = -newangle;
        // :475-486 - the lower jaw snaps while attacking; its teeth copy the angle.
        if (e.getAttacking() != 0) {
            this.BottomJaw.xRot = 0.52f + Mth.cos(f2 * 0.45f) * 0.35f;
        } else {
            this.BottomJaw.xRot = 0.25f;
        }
        this.Tooth9.xRot = this.BottomJaw.xRot;
        this.Tooth15.xRot = this.BottomJaw.xRot;
        this.Tooth14.xRot = this.BottomJaw.xRot;
        this.Tooth13.xRot = this.BottomJaw.xRot;
        this.Tooth16.xRot = this.BottomJaw.xRot;
        this.Tooth12.xRot = this.BottomJaw.xRot;
        // :487-503 - the tail sways, harder while attacking; each link sits at the end of the previous one.
        newangle = Mth.cos(f2 * 0.25f * this.wingspeed) * 3.1415927f * 0.05f;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.25f * this.wingspeed) * 3.1415927f * 0.35f;
        }
        this.TailBase1.yRot = newangle * 0.25f;
        this.Tail2.z = this.TailBase1.z + (float) Math.cos(this.TailBase1.yRot) * 12.0f;
        this.Tail2.x = this.TailBase1.x + (float) Math.sin(this.TailBase1.yRot) * 12.0f;
        this.Tail2.yRot = newangle * 0.5f;
        this.Tail3.z = this.Tail2.z + (float) Math.cos(this.Tail2.yRot) * 9.0f;
        this.Tail3.x = this.Tail2.x + (float) Math.sin(this.Tail2.yRot) * 9.0f;
        this.Tail3.yRot = newangle * 0.75f;
        this.Tail4.z = this.Tail3.z + (float) Math.cos(this.Tail3.yRot) * 7.0f;
        this.Tail4.x = this.Tail3.x + (float) Math.sin(this.Tail3.yRot) * 7.0f;
        this.Tail4.yRot = newangle * 1.0f;
        this.TailTip.z = this.Tail4.z + (float) Math.cos(this.Tail4.yRot) * 7.0f;
        this.TailTip.x = this.Tail4.x + (float) Math.sin(this.Tail4.yRot) * 7.0f;
        this.TailTip.yRot = newangle * 1.25f;
        // :504-564 - neck and upper jaw follow the head yaw; nose, eyes, teeth and hat sit on the upper jaw.
        this.Neck.yRot = (float) Math.toRadians(f3) * 0.25f;
        this.JawTop.z = this.Neck.z - (float) Math.cos(this.Neck.yRot) * 2.0f;
        this.JawTop.x = this.Neck.x - (float) Math.sin(this.Neck.yRot) * 2.0f;
        this.JawTop.yRot = (float) Math.toRadians(f3) * 0.5f;
        follow(this.TopNose, this.JawTop);
        follow(this.BottomNose, this.JawTop);
        follow(this.CenterRightNose, this.JawTop);
        follow(this.CenterMiddleNose, this.JawTop);
        follow(this.CenterLeftNose, this.JawTop);
        this.RightEye.z = this.JawTop.z;
        this.RightEye.x = this.JawTop.x;
        this.RightEye.yRot = this.JawTop.yRot + 0.78f;
        this.LeftEye.z = this.JawTop.z;
        this.LeftEye.x = this.JawTop.x;
        this.LeftEye.yRot = this.JawTop.yRot - 0.78f;
        follow(this.Tooth11, this.JawTop);
        follow(this.Tooth10, this.JawTop);
        follow(this.Tooth1, this.JawTop);
        follow(this.Tooth8, this.JawTop);
        follow(this.Tooth4, this.JawTop);
        follow(this.Tooth3, this.JawTop);
        follow(this.Tooth5, this.JawTop);
        follow(this.Tooth6, this.JawTop);
        follow(this.Tooth7, this.JawTop);
        follow(this.Tooth2, this.JawTop);
        follow(this.Hat1, this.JawTop);
        follow(this.Hat2, this.JawTop);
        // :565-585 - the lower jaw and its teeth.
        this.BottomJaw.z = this.Neck.z - (float) Math.cos(this.Neck.yRot) * 3.0f;
        this.BottomJaw.x = this.Neck.x - (float) Math.sin(this.Neck.yRot) * 3.0f;
        this.BottomJaw.yRot = (float) Math.toRadians(f3) * 0.5f;
        follow(this.Tooth9, this.BottomJaw);
        follow(this.Tooth16, this.BottomJaw);
        follow(this.Tooth15, this.BottomJaw);
        follow(this.Tooth14, this.BottomJaw);
        follow(this.Tooth13, this.BottomJaw);
        follow(this.Tooth12, this.BottomJaw);
        // :647-652 - Hat1 on any battle lizard, Hat2 only once activated.
        this.Hat1.visible = e.get_is_activated() != 0;
        this.Hat2.visible = e.get_is_activated() > 1;
    }

    /** Draw order of {@code render()} (:586-660); the hats obey {@code visible}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.BodyBack.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TopBackLeftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TailTip.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BodyFront.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TailBase1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TopFrontLeftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TopBackRightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BottomBackRightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TopFrontRightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BottomBackLeftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BottomFrontRightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BottomFrontLeftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BodyCenter.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BackLeftFoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BackRightFoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe8.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FrontLeftFoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Toe2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FrontRightFoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.FinRidge1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth11.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth10.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth8.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.CenterRightNose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.CenterLeftNose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BottomNose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TopNose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.JawTop.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.CenterMiddleNose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RightEye.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LeftEye.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth16.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth15.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth14.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth13.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth12.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tooth9.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BottomJaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hat1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hat2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin10.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin9.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin8.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fin2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
