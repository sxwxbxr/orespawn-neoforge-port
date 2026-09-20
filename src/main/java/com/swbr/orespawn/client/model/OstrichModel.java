package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.OstrichGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.rider.Ostrich;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelOstrich} (ModelOstrich.java:7-400), geometry {@link OstrichGeometry}
 * (256x128, 38 parts). {@code wingspeed} is the constructor argument, 0.65 in ClientProxyOreSpawn (manifest
 * {@code model_args}).
 *
 * <p>Animation from {@code render()} (:246-346): legs swing with the distance moved in the last tick (not
 * {@code limbSwing}), the tail sways on slow cosines, the head follows the head yaw at half strength - or, while
 * ridden, a smoothed turn rate kept in the entity's {@link RenderInfo} ({@code rf1}) - and flips upside down on a
 * sitting, inactive ostrich. The wings flap in cycles where a 1/3 roll came up at the cycle start ({@code ri1}). The
 * rolls use the client world random and are frame-driven, as in the original. The two hat parts show the battle
 * mob's activation (1 = brim, 2 = brim and crown).
 *
 * <p>No GL calls in the original: {@link RenderType#entityCutoutNoCull} (alpha-tested, no culling, as 1.7.10's living
 * renderer).
 */
public class OstrichModel extends EntityModel<Ostrich> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ostrich"), "main");

    private final float wingspeed;
    private final ModelPart Body1;
    private final ModelPart body2;
    private final ModelPart LLeg1;
    private final ModelPart Rleg1;
    private final ModelPart LLeg2;
    private final ModelPart Lfoot1;
    private final ModelPart RLeg2;
    private final ModelPart Lfoot2;
    private final ModelPart Lfoot3;
    private final ModelPart LClaw1;
    private final ModelPart LClaw2;
    private final ModelPart LClaw3;
    private final ModelPart Lfoot4;
    private final ModelPart LClaw4;
    private final ModelPart Rfoot1;
    private final ModelPart Rfoot2;
    private final ModelPart Rclaw1;
    private final ModelPart Rfoot3;
    private final ModelPart Rclaw3;
    private final ModelPart Rfoot4;
    private final ModelPart Rclaw2;
    private final ModelPart Rclaw4;
    private final ModelPart Body3;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Body4;
    private final ModelPart head;
    private final ModelPart leftleg;
    private final ModelPart Neck1;
    private final ModelPart Head1;
    private final ModelPart mouth1;
    private final ModelPart neck2;
    private final ModelPart rightleg;
    private final ModelPart Lwing;
    private final ModelPart Rwing;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    /** Every part {@link #setupAnim} writes, reset first (R8). */
    private final ModelPart[] animated;

    /** {@code o.get_is_activated()} read in {@code render()} (:383-388), carried from {@link #setupAnim}. */
    private int activated = 0;

    /** {@code ModelOstrich(float f1)} (:49-244). */
    public OstrichModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Body1 = root.getChild(OstrichGeometry.BODY1);
        this.body2 = root.getChild(OstrichGeometry.BODY2);
        this.LLeg1 = root.getChild(OstrichGeometry.LLEG1);
        this.Rleg1 = root.getChild(OstrichGeometry.RLEG1);
        this.LLeg2 = root.getChild(OstrichGeometry.LLEG2);
        this.Lfoot1 = root.getChild(OstrichGeometry.LFOOT1);
        this.RLeg2 = root.getChild(OstrichGeometry.RLEG2);
        this.Lfoot2 = root.getChild(OstrichGeometry.LFOOT2);
        this.Lfoot3 = root.getChild(OstrichGeometry.LFOOT3);
        this.LClaw1 = root.getChild(OstrichGeometry.LCLAW1);
        this.LClaw2 = root.getChild(OstrichGeometry.LCLAW2);
        this.LClaw3 = root.getChild(OstrichGeometry.LCLAW3);
        this.Lfoot4 = root.getChild(OstrichGeometry.LFOOT4);
        this.LClaw4 = root.getChild(OstrichGeometry.LCLAW4);
        this.Rfoot1 = root.getChild(OstrichGeometry.RFOOT1);
        this.Rfoot2 = root.getChild(OstrichGeometry.RFOOT2);
        this.Rclaw1 = root.getChild(OstrichGeometry.RCLAW1);
        this.Rfoot3 = root.getChild(OstrichGeometry.RFOOT3);
        this.Rclaw3 = root.getChild(OstrichGeometry.RCLAW3);
        this.Rfoot4 = root.getChild(OstrichGeometry.RFOOT4);
        this.Rclaw2 = root.getChild(OstrichGeometry.RCLAW2);
        this.Rclaw4 = root.getChild(OstrichGeometry.RCLAW4);
        this.Body3 = root.getChild(OstrichGeometry.BODY3);
        this.Tail1 = root.getChild(OstrichGeometry.TAIL1);
        this.Tail2 = root.getChild(OstrichGeometry.TAIL2);
        this.Tail3 = root.getChild(OstrichGeometry.TAIL3);
        this.Body4 = root.getChild(OstrichGeometry.BODY4);
        this.head = root.getChild(OstrichGeometry.HEAD);
        this.leftleg = root.getChild(OstrichGeometry.LEFTLEG);
        this.Neck1 = root.getChild(OstrichGeometry.NECK1);
        this.Head1 = root.getChild(OstrichGeometry.HEAD1);
        this.mouth1 = root.getChild(OstrichGeometry.MOUTH1);
        this.neck2 = root.getChild(OstrichGeometry.NECK2);
        this.rightleg = root.getChild(OstrichGeometry.RIGHTLEG);
        this.Lwing = root.getChild(OstrichGeometry.LWING);
        this.Rwing = root.getChild(OstrichGeometry.RWING);
        this.Hat1 = root.getChild(OstrichGeometry.HAT1);
        this.Hat2 = root.getChild(OstrichGeometry.HAT2);
        this.animated = new ModelPart[] {
            this.leftleg, this.LLeg1, this.LLeg2, this.Lfoot1, this.Lfoot2, this.Lfoot3, this.Lfoot4,
            this.LClaw1, this.LClaw2, this.LClaw3, this.LClaw4,
            this.rightleg, this.Rleg1, this.RLeg2, this.Rfoot1, this.Rfoot2, this.Rfoot3, this.Rfoot4,
            this.Rclaw1, this.Rclaw2, this.Rclaw3, this.Rclaw4,
            this.Tail1, this.Tail2, this.Tail3,
            this.Head1, this.head, this.mouth1, this.Neck1, this.Hat1, this.Hat2,
            this.Lwing, this.Rwing
        };
    }

    /** The writes of {@code render()} (:255-346), in the original order. */
    @Override
    public void setupAnim(final Ostrich o, final float f, final float f1, final float f2, float f3, final float f4) {
        for (final ModelPart part : this.animated) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        float lspeed = 0.0f;
        lspeed = (float) ((o.xo - o.getX()) * (o.xo - o.getX()) + (o.zo - o.getZ()) * (o.zo - o.getZ()));
        lspeed = (float) Math.sqrt(lspeed);
        newangle = Mth.cos(f2 * 1.25f * this.wingspeed) * 3.1415927f * lspeed * 0.4f;
        if (newangle > 0.5) {
            newangle = 0.75f;
        }
        if (newangle < -0.5) {
            newangle = -0.75f;
        }
        this.leftleg.xRot = -0.297f + newangle;
        this.LLeg1.xRot = 0.483f + newangle;
        this.LLeg2.xRot = -0.437f + newangle;
        this.Lfoot1.xRot = newangle;
        this.Lfoot2.xRot = newangle;
        this.Lfoot3.xRot = newangle;
        this.Lfoot4.xRot = newangle;
        this.LClaw1.xRot = newangle;
        this.LClaw2.xRot = newangle;
        this.LClaw3.xRot = newangle;
        this.LClaw4.xRot = newangle;
        this.rightleg.xRot = -0.297f - newangle;
        this.Rleg1.xRot = 0.483f - newangle;
        this.RLeg2.xRot = -0.437f - newangle;
        this.Rfoot1.xRot = -newangle;
        this.Rfoot2.xRot = -newangle;
        this.Rfoot3.xRot = -newangle;
        this.Rfoot4.xRot = -newangle;
        this.Rclaw1.xRot = -newangle;
        this.Rclaw2.xRot = -newangle;
        this.Rclaw3.xRot = -newangle;
        this.Rclaw4.xRot = -newangle;
        this.Tail1.xRot = -0.594f + Mth.cos(f2 * 0.05f) * 3.1415927f * 0.06f;
        this.Tail2.xRot = this.Tail1.xRot;
        this.Tail3.xRot = this.Tail1.xRot;
        this.Tail3.yRot = -0.334f + Mth.cos(f2 * 0.061f) * 3.1415927f * 0.08f;
        this.Tail2.yRot = 0.334f - Mth.cos(f2 * 0.072f) * 3.1415927f * 0.08f;
        r = o.getRenderInfo();
        if (o.isVehicle()) {
            f3 = (o.yRotO - o.getYRot()) * 20.0f;
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
        } else {
            f3 /= 2.0f;
        }
        if (o.isSitting() && o.get_is_activated() == 0) {
            f3 = 0.0f;
            this.Head1.xRot = 3.1415f;
        } else {
            this.Head1.xRot = 0.0f;
        }
        this.head.xRot = this.Head1.xRot;
        this.mouth1.xRot = this.Head1.xRot;
        this.Neck1.xRot = this.Head1.xRot;
        this.Hat1.xRot = this.Head1.xRot;
        this.Hat2.xRot = this.Head1.xRot;
        this.Head1.yRot = (float) Math.toRadians(f3) * 0.65f;
        this.head.yRot = this.Head1.yRot;
        this.mouth1.yRot = this.Head1.yRot;
        this.Hat1.yRot = this.Head1.yRot;
        this.Hat2.yRot = this.Head1.yRot;
        newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.3f) * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (o.level().random.nextInt(3) == 1) {
                r.ri1 = 1;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        newangle = Math.abs(newangle);
        this.Lwing.zRot = -newangle;
        this.Lwing.yRot = newangle / 2.0f;
        this.Rwing.zRot = newangle;
        this.Rwing.yRot = -newangle / 2.0f;
        o.setRenderInfo(r);
        this.activated = o.get_is_activated();
    }

    /** Draw order of {@code render()} (:347-388); the hats by activation. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Body1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LLeg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LLeg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lfoot1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RLeg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lfoot2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lfoot3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LClaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LClaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LClaw3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lfoot4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LClaw4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rfoot1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rfoot2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rclaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rfoot3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rclaw3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rfoot4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rclaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rclaw4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Body3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Body4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Neck1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mouth1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        // o instanceof EntityCannonFodder is always true here.
        if (this.activated != 0) {
            this.Hat1.render(poseStack, buffer, packedLight, packedOverlay, color);
            if (this.activated > 1) {
                this.Hat2.render(poseStack, buffer, packedLight, packedOverlay, color);
            }
        }
    }
}
