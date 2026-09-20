package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.EnderKnightGeometry;
import com.swbr.orespawn.entity.ender.EnderKnight;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelEnderKnight} (ModelEnderKnight.java:7-389): the skeletal knight with cape
 * and sword, geometry {@link EnderKnightGeometry} (512x512, 40 parts). {@code wingspeed} is the constructor
 * argument, 0.21 in ClientProxyOreSpawn (manifest {@code model_args}).
 *
 * <p>{@code render()} (:258-378) only writes angles and pivots and then draws every part once, in creation order;
 * no GL state is touched. The writes go to {@link #setupAnim}, the draws to {@link #renderToBuffer}. Every animated
 * field is written again each frame except {@code larm1}/{@code rarm1} Y and Z rotation in the screaming branch,
 * which the original left at their previous values - those equal the constructor pose (Y ±1.0, Z 0) and the calm
 * branch (:310-317), so {@code resetPose()} first gives the same numbers (R8).
 */
public class EnderKnightModel extends EntityModel<EnderKnight> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ender_knight"), "main");

    private final float wingspeed;
    /** Every part in the draw order of {@code render()} (:338-377), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart rleg1;
    private final ModelPart rleg3;
    private final ModelPart rleg2;
    private final ModelPart rfoot1;
    private final ModelPart rfoot3;
    private final ModelPart lleg3;
    private final ModelPart lleg2;
    private final ModelPart lleg1;
    private final ModelPart rfoot4;
    private final ModelPart rfoot2;
    private final ModelPart cape2;
    private final ModelPart lfoot1;
    private final ModelPart lfoot3;
    private final ModelPart lfoot2;
    private final ModelPart lfoot4;
    private final ModelPart head;
    private final ModelPart rarm3;
    private final ModelPart rarm2;
    private final ModelPart rarm1;
    private final ModelPart larm3;
    private final ModelPart larm2;
    private final ModelPart larm1;
    private final ModelPart blade;
    private final ModelPart handle;

    /** {@code ModelEnderKnight(float f1)} (:51-256). */
    public EnderKnightModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[EnderKnightGeometry.PARTS.length];
        for (int i = 0; i < EnderKnightGeometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(EnderKnightGeometry.PARTS[i]);
        }
        for (final String hidden : EnderKnightGeometry.HIDDEN) {
            root.getChild(hidden).visible = false;
        }
        this.rleg1 = root.getChild(EnderKnightGeometry.RLEG1);
        this.rleg3 = root.getChild(EnderKnightGeometry.RLEG3);
        this.rleg2 = root.getChild(EnderKnightGeometry.RLEG2);
        this.rfoot1 = root.getChild(EnderKnightGeometry.RFOOT1);
        this.rfoot3 = root.getChild(EnderKnightGeometry.RFOOT3);
        this.lleg3 = root.getChild(EnderKnightGeometry.LLEG3);
        this.lleg2 = root.getChild(EnderKnightGeometry.LLEG2);
        this.lleg1 = root.getChild(EnderKnightGeometry.LLEG1);
        this.rfoot4 = root.getChild(EnderKnightGeometry.RFOOT4);
        this.rfoot2 = root.getChild(EnderKnightGeometry.RFOOT2);
        this.cape2 = root.getChild(EnderKnightGeometry.CAPE2);
        this.lfoot1 = root.getChild(EnderKnightGeometry.LFOOT1);
        this.lfoot3 = root.getChild(EnderKnightGeometry.LFOOT3);
        this.lfoot2 = root.getChild(EnderKnightGeometry.LFOOT2);
        this.lfoot4 = root.getChild(EnderKnightGeometry.LFOOT4);
        this.head = root.getChild(EnderKnightGeometry.HEAD);
        this.rarm3 = root.getChild(EnderKnightGeometry.RARM3);
        this.rarm2 = root.getChild(EnderKnightGeometry.RARM2);
        this.rarm1 = root.getChild(EnderKnightGeometry.RARM1);
        this.larm3 = root.getChild(EnderKnightGeometry.LARM3);
        this.larm2 = root.getChild(EnderKnightGeometry.LARM2);
        this.larm1 = root.getChild(EnderKnightGeometry.LARM1);
        this.blade = root.getChild(EnderKnightGeometry.BLADE);
        this.handle = root.getChild(EnderKnightGeometry.HANDLE);
    }

    /** The writes of {@code render()} (:262-337), in the original order. */
    @Override
    public void setupAnim(final EnderKnight e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lfoot1.xRot = newangle;
        this.lfoot2.xRot = 0.6f + newangle;
        this.lfoot3.xRot = newangle;
        this.lfoot4.xRot = newangle;
        this.lleg1.xRot = newangle;
        this.lleg2.xRot = -0.1f + newangle;
        this.lleg3.xRot = -0.1f + newangle;
        this.rfoot1.xRot = -newangle;
        this.rfoot2.xRot = 0.6f - newangle;
        this.rfoot3.xRot = -newangle;
        this.rfoot4.xRot = -newangle;
        this.rleg1.xRot = -newangle;
        this.rleg2.xRot = -0.1f - newangle;
        this.rleg3.xRot = -0.1f - newangle;
        this.cape2.zRot = newangle / 4.0f;
        newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.02f;
        this.cape2.xRot = newangle;
        this.head.yRot = (float) Math.toRadians(f3) * 0.45f;
        if (this.head.yRot > 0.45f) {
            this.head.yRot = 0.45f;
        }
        if (this.head.yRot < -0.45f) {
            this.head.yRot = -0.45f;
        }
        newangle = Mth.cos(f2 * 2.7f * this.wingspeed) * 3.1415927f * 0.3f;
        if (e.isScreaming()) {
            this.larm2.xRot = -1.2f + newangle;
            this.larm3.xRot = -1.2f + newangle;
            this.rarm2.xRot = -1.2f + newangle;
            this.rarm3.xRot = -1.2f + newangle;
            this.larm1.xRot = -1.8f + newangle;
            this.rarm1.xRot = -1.8f + newangle;
            final float n = 0.5f + newangle * 3.0f / 2.0f;
            this.handle.xRot = n;
            this.blade.xRot = n;
        } else {
            this.larm2.xRot = -0.5f;
            this.larm3.xRot = -0.5f;
            this.larm1.zRot = 0.0f;
            this.larm1.yRot = 1.0f;
            this.larm1.xRot = -1.0f;
            this.rarm2.xRot = -0.5f;
            this.rarm3.xRot = -0.5f;
            this.rarm1.zRot = 0.0f;
            this.rarm1.yRot = -1.0f;
            this.rarm1.xRot = -1.0f;
            final float n2 = 0.35f;
            this.handle.xRot = n2;
            this.blade.xRot = n2;
        }
        this.larm1.y = (float) (this.larm2.y + Math.cos(this.larm2.xRot) * 10.0);
        this.larm1.z = (float) (this.larm2.z + Math.sin(this.larm2.xRot) * 10.0);
        this.rarm1.y = (float) (this.rarm2.y + Math.cos(this.rarm2.xRot) * 10.0);
        this.rarm1.z = (float) (this.rarm2.z + Math.sin(this.rarm2.xRot) * 10.0);
        final float n3 = (float) (this.rarm1.y + Math.cos(this.rarm1.xRot) * 7.0) + 1.0f;
        this.handle.y = n3;
        this.blade.y = n3;
        final float n4 = (float) (this.rarm1.z + Math.sin(this.rarm1.xRot) * 7.0);
        this.handle.z = n4;
        this.blade.z = n4;
    }

    /** The draws of {@code render()} (:338-377). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
