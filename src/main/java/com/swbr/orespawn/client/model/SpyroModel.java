package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SpyroGeometry;
import com.swbr.orespawn.entity.dragon.Spyro;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelSpyro} (ModelSpyro.java:7-384): 37 boxes, 64x64 texture, geometry from the
 * generated {@link SpyroGeometry}. The animation is {@code render()} (:240-335):
 * <ul>
 *   <li>wings flap with the walk speed ({@code f1 > 0.1}), half as far in activity 3 (never set by the entity);</li>
 *   <li>legs walk on the ground and stretch forward and back in flight (activity 2);</li>
 *   <li>a two-link tail wags, still while sitting;</li>
 *   <li>every head box follows head yaw and pitch, the horns with their rest tilt.</li>
 * </ul>
 * Reads {@link Spyro#getActivity()} (synced) and {@link Spyro#isSitting()}.
 *
 * <p>Rest pose every frame ({@code resetPose}, R8): every written value depends only on rest values or on values
 * written earlier in the same frame. {@code f3}/{@code f4} were unwrapped head yaw and pitch; 1.21.1 passes the yaw
 * wrapped to [-180, 180) (docs/research/06-models-design.md), the same value for every normal pose.
 */
public class SpyroModel extends EntityModel<Spyro> {

    /** Register with {@code SpyroGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "baby_dragon"), "main");

    /** {@code ModelSpyro(float f1)}: {@code wingspeed = f1} (:48-50); ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:336-372), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart RightFrontPaw;
    private final ModelPart WingLeft;
    private final ModelPart LegRightFrontTop;
    private final ModelPart LegRightFrontBottom;
    private final ModelPart LegRightBackTop;
    private final ModelPart LegRightBackBottom;
    private final ModelPart RightBackPaw;
    private final ModelPart LegLeftFrontTop;
    private final ModelPart SnoutRight;
    private final ModelPart LeftFrontPaw;
    private final ModelPart LegLeftBackTop;
    private final ModelPart LegLeftBackBottom;
    private final ModelPart LeftBackPaw;
    private final ModelPart LegLeftFrontBottom;
    private final ModelPart TailPieceSmall;
    private final ModelPart JawPiece;
    private final ModelPart HeadPieceBottom;
    private final ModelPart HeadPieceTop;
    private final ModelPart HornRightBottom;
    private final ModelPart HornLeftBottom;
    private final ModelPart HornRightTop;
    private final ModelPart HornLeftTop;
    private final ModelPart SnoutLeft;
    private final ModelPart WingRight;
    private final ModelPart TailBack;
    private final ModelPart TailFront;
    private final ModelPart ScaleBackHead;
    private final ModelPart TailPieceLarge;
    private final ModelPart ScaleTailPiece;
    private final ModelPart ScaleHead;
    private final ModelPart ScaleTop1;

    public SpyroModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[SpyroGeometry.PARTS.length];
        for (int i = 0; i < SpyroGeometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(SpyroGeometry.PARTS[i]);
        }
        this.RightFrontPaw = root.getChild(SpyroGeometry.RIGHT_FRONT_PAW);
        this.WingLeft = root.getChild(SpyroGeometry.WING_LEFT);
        this.LegRightFrontTop = root.getChild(SpyroGeometry.LEG_RIGHT_FRONT_TOP);
        this.LegRightFrontBottom = root.getChild(SpyroGeometry.LEG_RIGHT_FRONT_BOTTOM);
        this.LegRightBackTop = root.getChild(SpyroGeometry.LEG_RIGHT_BACK_TOP);
        this.LegRightBackBottom = root.getChild(SpyroGeometry.LEG_RIGHT_BACK_BOTTOM);
        this.RightBackPaw = root.getChild(SpyroGeometry.RIGHT_BACK_PAW);
        this.LegLeftFrontTop = root.getChild(SpyroGeometry.LEG_LEFT_FRONT_TOP);
        this.SnoutRight = root.getChild(SpyroGeometry.SNOUT_RIGHT);
        this.LeftFrontPaw = root.getChild(SpyroGeometry.LEFT_FRONT_PAW);
        this.LegLeftBackTop = root.getChild(SpyroGeometry.LEG_LEFT_BACK_TOP);
        this.LegLeftBackBottom = root.getChild(SpyroGeometry.LEG_LEFT_BACK_BOTTOM);
        this.LeftBackPaw = root.getChild(SpyroGeometry.LEFT_BACK_PAW);
        this.LegLeftFrontBottom = root.getChild(SpyroGeometry.LEG_LEFT_FRONT_BOTTOM);
        this.TailPieceSmall = root.getChild(SpyroGeometry.TAIL_PIECE_SMALL);
        this.JawPiece = root.getChild(SpyroGeometry.JAW_PIECE);
        this.HeadPieceBottom = root.getChild(SpyroGeometry.HEAD_PIECE_BOTTOM);
        this.HeadPieceTop = root.getChild(SpyroGeometry.HEAD_PIECE_TOP);
        this.HornRightBottom = root.getChild(SpyroGeometry.HORN_RIGHT_BOTTOM);
        this.HornLeftBottom = root.getChild(SpyroGeometry.HORN_LEFT_BOTTOM);
        this.HornRightTop = root.getChild(SpyroGeometry.HORN_RIGHT_TOP);
        this.HornLeftTop = root.getChild(SpyroGeometry.HORN_LEFT_TOP);
        this.SnoutLeft = root.getChild(SpyroGeometry.SNOUT_LEFT);
        this.WingRight = root.getChild(SpyroGeometry.WING_RIGHT);
        this.TailBack = root.getChild(SpyroGeometry.TAIL_BACK);
        this.TailFront = root.getChild(SpyroGeometry.TAIL_FRONT);
        this.ScaleBackHead = root.getChild(SpyroGeometry.SCALE_BACK_HEAD);
        this.TailPieceLarge = root.getChild(SpyroGeometry.TAIL_PIECE_LARGE);
        this.ScaleTailPiece = root.getChild(SpyroGeometry.SCALE_TAIL_PIECE);
        this.ScaleHead = root.getChild(SpyroGeometry.SCALE_HEAD);
        this.ScaleTop1 = root.getChild(SpyroGeometry.SCALE_TOP1);
    }

    /** The angle and pivot writes of {@code render()} (:242-335). */
    @Override
    public void setupAnim(final Spyro entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final Spyro c = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        final float f3 = netHeadYaw;
        final float f4 = headPitch;
        float newangle = 0.0f;
        final int current_activity = c.getActivity();
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.4f * f1;
        } else {
            newangle = 0.0f;
        }
        if (current_activity == 3) {
            newangle *= 0.5f;
        }
        this.WingLeft.zRot = newangle;
        this.WingRight.zRot = -newangle;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        if (current_activity == 3) {
            newangle = 0.0f;
        }
        if (current_activity != 2) {
            this.LegRightFrontTop.xRot = newangle - 0.087f;
            this.LegRightFrontBottom.xRot = newangle - 0.17f;
            this.RightFrontPaw.xRot = newangle;
            this.LegLeftFrontTop.xRot = -newangle - 0.087f;
            this.LegLeftFrontBottom.xRot = -newangle - 0.17f;
            this.LeftFrontPaw.xRot = -newangle;
            this.LegRightBackBottom.xRot = -newangle + 0.139f;
            this.LegRightBackTop.xRot = -newangle - 0.174f;
            this.RightBackPaw.xRot = -newangle;
            this.LegLeftBackBottom.xRot = newangle + 0.139f;
            this.LegLeftBackTop.xRot = newangle - 0.174f;
            this.LeftBackPaw.xRot = newangle;
        } else {
            newangle = -1.0f;
            this.LegRightFrontTop.xRot = newangle - 0.087f;
            this.LegRightFrontBottom.xRot = newangle - 0.17f;
            this.RightFrontPaw.xRot = newangle;
            this.LegLeftFrontTop.xRot = newangle - 0.087f;
            this.LegLeftFrontBottom.xRot = newangle - 0.17f;
            this.LeftFrontPaw.xRot = newangle;
            newangle = 1.0f;
            this.LegRightBackBottom.xRot = newangle + 0.139f;
            this.LegRightBackTop.xRot = newangle - 0.174f;
            this.RightBackPaw.xRot = newangle;
            this.LegLeftBackBottom.xRot = newangle + 0.139f;
            this.LegLeftBackTop.xRot = newangle - 0.174f;
            this.LeftBackPaw.xRot = newangle;
        }
        newangle = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.25f;
        if (c.isSitting() || current_activity == 3) {
            newangle = 0.0f;
        }
        this.TailBack.yRot = newangle;
        this.ScaleTailPiece.yRot = newangle;
        this.TailFront.z = this.TailBack.z + (float) Math.cos(this.TailBack.yRot) * 3.0f;
        this.TailFront.x = this.TailBack.x + (float) Math.sin(this.TailBack.yRot) * 3.0f - 0.5f;
        this.TailFront.yRot = newangle * 1.6f;
        this.TailPieceLarge.z = this.TailFront.z;
        this.TailPieceLarge.x = this.TailFront.x;
        this.TailPieceLarge.yRot = this.TailFront.yRot;
        this.TailPieceSmall.z = this.TailFront.z;
        this.TailPieceSmall.x = this.TailFront.x;
        this.TailPieceSmall.yRot = this.TailFront.yRot;
        this.HeadPieceTop.yRot = (float) Math.toRadians(f3);
        this.HeadPieceBottom.yRot = (float) Math.toRadians(f3);
        this.JawPiece.yRot = (float) Math.toRadians(f3);
        this.SnoutRight.yRot = (float) Math.toRadians(f3);
        this.SnoutLeft.yRot = (float) Math.toRadians(f3);
        this.ScaleTop1.yRot = (float) Math.toRadians(f3);
        this.ScaleHead.yRot = (float) Math.toRadians(f3);
        this.ScaleBackHead.yRot = (float) Math.toRadians(f3);
        this.HornRightBottom.yRot = (float) Math.toRadians(f3) + 0.785f;
        this.HornRightTop.yRot = (float) Math.toRadians(f3) + 0.785f;
        this.HornLeftBottom.yRot = (float) Math.toRadians(f3) - 0.785f;
        this.HornLeftTop.yRot = (float) Math.toRadians(f3) - 0.785f;
        this.HeadPieceTop.xRot = (float) Math.toRadians(f4);
        this.HeadPieceBottom.xRot = (float) Math.toRadians(f4);
        this.JawPiece.xRot = (float) Math.toRadians(f4);
        this.SnoutRight.xRot = (float) Math.toRadians(f4);
        this.SnoutLeft.xRot = (float) Math.toRadians(f4);
        this.ScaleTop1.xRot = (float) Math.toRadians(f4);
        this.ScaleHead.xRot = (float) Math.toRadians(f4);
        this.ScaleBackHead.xRot = (float) Math.toRadians(f4);
        this.HornRightBottom.xRot = (float) Math.toRadians(f4) - 0.785f;
        this.HornRightTop.xRot = (float) Math.toRadians(f4) - 0.785f;
        this.HornLeftBottom.xRot = (float) Math.toRadians(f4) - 0.785f;
        this.HornLeftTop.xRot = (float) Math.toRadians(f4) - 0.785f;
    }

    /** {@code render()} (:336-372). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
