package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ScorpionGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.arthropod.Scorpion;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelScorpion} (ModelScorpion.java:7-268): 22 boxes, 88x24 texture, geometry from the
 * generated {@link ScorpionGeometry}. The animation is {@code render()} (:150-203) with the helpers {@code doLeftClaw},
 * {@code doRightClaw} and {@code doTail} (:238-267).
 *
 * <p>The claws and the tail strike only in cycles picked when the claw cosine crosses zero upwards: then a roll on the
 * client world's random decides which claw ({@code ri1}) and whether the tail ({@code ri2}) moves, with tighter odds
 * while the scorpion attacks. The note pad is the entity's {@link RenderInfo}, frame-driven as in the original.
 */
public class ScorpionModel extends EntityModel<Scorpion> {

    /** Register with {@code ScorpionGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "scorpion"), "main");

    /** {@code ModelScorpion(float f1)}: {@code wingspeed = f1} (:33-35); ClientProxyOreSpawn passes 0.62 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:204-225), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail6;
    private final ModelPart lleg1;
    private final ModelPart rleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg3;
    private final ModelPart rleg4;
    private final ModelPart rleg3;
    private final ModelPart lleg4;
    private final ModelPart lleg2;
    private final ModelPart larm2;
    private final ModelPart rarm2;
    private final ModelPart larm1;
    private final ModelPart rarm1;
    private final ModelPart lclaw;
    private final ModelPart rclaw;

    public ScorpionModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[ScorpionGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(ScorpionGeometry.PARTS[i]);
        }
        this.tail1 = root.getChild(ScorpionGeometry.TAIL1);
        this.tail2 = root.getChild(ScorpionGeometry.TAIL2);
        this.tail3 = root.getChild(ScorpionGeometry.TAIL3);
        this.tail4 = root.getChild(ScorpionGeometry.TAIL4);
        this.tail5 = root.getChild(ScorpionGeometry.TAIL5);
        this.tail6 = root.getChild(ScorpionGeometry.TAIL6);
        this.lleg1 = root.getChild(ScorpionGeometry.LLEG1);
        this.rleg1 = root.getChild(ScorpionGeometry.RLEG1);
        this.rleg2 = root.getChild(ScorpionGeometry.RLEG2);
        this.lleg3 = root.getChild(ScorpionGeometry.LLEG3);
        this.rleg4 = root.getChild(ScorpionGeometry.RLEG4);
        this.rleg3 = root.getChild(ScorpionGeometry.RLEG3);
        this.lleg4 = root.getChild(ScorpionGeometry.LLEG4);
        this.lleg2 = root.getChild(ScorpionGeometry.LLEG2);
        this.larm2 = root.getChild(ScorpionGeometry.LARM2);
        this.rarm2 = root.getChild(ScorpionGeometry.RARM2);
        this.larm1 = root.getChild(ScorpionGeometry.LARM1);
        this.rarm1 = root.getChild(ScorpionGeometry.RARM1);
        this.lclaw = root.getChild(ScorpionGeometry.LCLAW);
        this.rclaw = root.getChild(ScorpionGeometry.RCLAW);
    }

    /** The writes of {@code render()} (:150-203). */
    @Override
    public void setupAnim(final Scorpion e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        final float pi4 = 1.570795f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        this.lleg1.yRot = newangle + 0.49f;
        this.rleg1.yRot = -newangle + 2.65f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 1.0f * pi4) * 3.1415927f * 0.12f * f1;
        this.lleg2.yRot = newangle + 0.24f;
        this.rleg2.yRot = -newangle + 2.9f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.12f * f1;
        this.lleg3.yRot = newangle - 0.24f;
        this.rleg3.yRot = -newangle - 2.9f;
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.12f * f1;
        this.lleg4.yRot = newangle - 0.49f;
        this.rleg4.yRot = -newangle - 2.65f;
        r = e.getRenderInfo();
        newangle = Mth.cos(f2 * 3.0f * this.wingspeed) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.1f) * 3.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            // e.worldObj.rand is the client world's random here, as in the original.
            if (e.getAttacking() == 0) {
                r.ri1 = e.level().random.nextInt(20);
                r.ri2 = e.level().random.nextInt(25);
            } else {
                r.ri1 = e.level().random.nextInt(4);
                r.ri2 = e.level().random.nextInt(3);
            }
        }
        if (r.ri1 == 1 || r.ri1 == 3) {
            this.doLeftClaw(newangle);
        } else {
            this.doLeftClaw(0.0f);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            this.doRightClaw(newangle);
        } else {
            this.doRightClaw(0.0f);
        }
        if (r.ri2 == 1) {
            this.doTail(newangle);
        } else {
            this.doTail(0.0f);
        }
        e.setRenderInfo(r);
    }

    /** {@code doLeftClaw} (:238-243). */
    private void doLeftClaw(final float angle) {
        this.larm2.yRot = 0.52f + angle;
        this.larm1.z = (float) (this.larm2.z - Math.sin(this.larm2.yRot) * 4.5);
        this.lclaw.z = this.larm1.z - 3.0f;
        this.lclaw.yRot = 0.381f - angle;
    }

    /** {@code doRightClaw} (:245-250). */
    private void doRightClaw(final float angle) {
        this.rarm2.yRot = 2.61f - angle;
        this.rarm1.z = (float) (this.rarm2.z - Math.sin(this.rarm2.yRot) * 4.5);
        this.rclaw.z = this.rarm1.z - 3.0f;
        this.rclaw.yRot = -0.381f + angle;
    }

    /** {@code doTail} (:252-267): each segment hangs at the end of the previous one. */
    private void doTail(final float angle) {
        this.tail1.xRot = 0.26f + angle;
        this.tail2.xRot = this.tail1.xRot + 0.76900005f + angle;
        this.tail2.y = (float) (this.tail1.y - Math.sin(this.tail1.xRot) * 4.0);
        this.tail2.z = (float) (this.tail1.z + Math.cos(this.tail1.xRot) * 4.0);
        this.tail3.xRot = this.tail2.xRot + 0.701f + angle;
        this.tail3.y = (float) (this.tail2.y - Math.sin(this.tail2.xRot) * 4.0);
        this.tail3.z = (float) (this.tail2.z + Math.cos(this.tail2.xRot) * 4.0);
        this.tail4.xRot = this.tail3.xRot - 5.501f - angle * 3.0f / 2.0f - 0.4f;
        this.tail4.y = (float) (this.tail3.y - Math.sin(this.tail3.xRot) * 3.0);
        this.tail4.z = (float) (this.tail3.z + Math.cos(this.tail3.xRot) * 3.0);
        this.tail5.y = (float) (this.tail4.y - Math.sin(this.tail4.xRot) * 4.0);
        this.tail5.z = (float) (this.tail4.z + Math.cos(this.tail4.xRot) * 4.0);
        this.tail6.y = (float) (this.tail5.y - Math.sin(this.tail5.xRot) * 4.0);
        this.tail6.z = (float) (this.tail5.z + Math.cos(this.tail5.xRot) * 4.0);
    }

    /** {@code render()} (:204-225). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
