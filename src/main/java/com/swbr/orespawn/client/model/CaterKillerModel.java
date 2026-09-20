package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CaterKillerGeometry;
import com.swbr.orespawn.entity.moth.CaterKiller;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCaterKiller} (ModelCaterKiller.java:7-342): 31 boxes on a 256x512 texture, geometry
 * {@link CaterKillerGeometry}. {@code wingspeed} is the constructor argument (:40), 0.22 (ClientProxyOreSpawn, manifest
 * {@code model_args}). No GL calls.
 *
 * <p>{@code render()} (:204-331) animates the head group (jaws, head bob, tusk tips) from {@code attacking} (DataWatcher
 * 20) and {@code ageInTicks}, then draws the {@code seg1} group three times and the {@code seg2} group six times, each
 * time after moving the same parts to the next segment position, and the tail segment behind the last copy.
 *
 * <p>PORT (R8): the writes before the first draw (:212-237) are in {@link #setupAnim}. The segment writes are interleaved
 * with the draws - a {@code ModelPart} has one pose at a time - so they stay in {@link #renderToBuffer}, which runs right
 * after {@code setupAnim} and draws the same part repeatedly, exactly like the original (design-entities-02.md offers
 * this 1:1 form). {@code resetPose()} at the start replaces the field values 1.7.10 left standing between frames; every
 * animated field is overwritten before its first draw anyway.
 */
public class CaterKillerModel extends EntityModel<CaterKiller> {

    /** Register with {@code CaterKillerGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cater_killer"), "main");

    private final float wingspeed;
    private final ModelPart Head;
    private final ModelPart falsehead;
    private final ModelPart seg1;
    private final ModelPart ltusk1;
    private final ModelPart ltusk2;
    private final ModelPart rtusk1;
    private final ModelPart rtusk2;
    private final ModelPart ljaw;
    private final ModelPart rjaw;
    private final ModelPart seg1lspike;
    private final ModelPart seg1rspike;
    private final ModelPart seg1ltopspike;
    private final ModelPart seg1rtopspike;
    private final ModelPart seg1lleg;
    private final ModelPart seg1rleg;
    private final ModelPart seg2;
    private final ModelPart seg2lfoot;
    private final ModelPart seg2rfoot;
    private final ModelPart seg2ltopspike;
    private final ModelPart seg2rtopspike;
    private final ModelPart seg2lspike;
    private final ModelPart seg2rspike;
    private final ModelPart seg3;
    private final ModelPart seg3lfoot;
    private final ModelPart seg3rfoot;
    private final ModelPart seg3lspike;
    private final ModelPart seg3rspike;
    private final ModelPart seg3ltopspike;
    private final ModelPart seg3rtopspike;
    private final ModelPart seg3lbackspike;
    private final ModelPart seg3rbackspike;
    private final ModelPart[] allParts;

    /** The render inputs {@link #renderToBuffer} needs: {@code attacking != 0}, {@code f1}, {@code f2}, {@code headoff}. */
    private boolean attacking;
    private float f1;
    private float f2;
    private float headoff;

    /** {@code ModelCaterKiller(float f1)} (:38-202). */
    public CaterKillerModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Head = root.getChild(CaterKillerGeometry.HEAD);
        this.falsehead = root.getChild(CaterKillerGeometry.FALSEHEAD);
        this.seg1 = root.getChild(CaterKillerGeometry.SEG1);
        this.ltusk1 = root.getChild(CaterKillerGeometry.LTUSK1);
        this.ltusk2 = root.getChild(CaterKillerGeometry.LTUSK2);
        this.rtusk1 = root.getChild(CaterKillerGeometry.RTUSK1);
        this.rtusk2 = root.getChild(CaterKillerGeometry.RTUSK2);
        this.ljaw = root.getChild(CaterKillerGeometry.LJAW);
        this.rjaw = root.getChild(CaterKillerGeometry.RJAW);
        this.seg1lspike = root.getChild(CaterKillerGeometry.SEG1LSPIKE);
        this.seg1rspike = root.getChild(CaterKillerGeometry.SEG1RSPIKE);
        this.seg1ltopspike = root.getChild(CaterKillerGeometry.SEG1LTOPSPIKE);
        this.seg1rtopspike = root.getChild(CaterKillerGeometry.SEG1RTOPSPIKE);
        this.seg1lleg = root.getChild(CaterKillerGeometry.SEG1LLEG);
        this.seg1rleg = root.getChild(CaterKillerGeometry.SEG1RLEG);
        this.seg2 = root.getChild(CaterKillerGeometry.SEG2);
        this.seg2lfoot = root.getChild(CaterKillerGeometry.SEG2LFOOT);
        this.seg2rfoot = root.getChild(CaterKillerGeometry.SEG2RFOOT);
        this.seg2ltopspike = root.getChild(CaterKillerGeometry.SEG2LTOPSPIKE);
        this.seg2rtopspike = root.getChild(CaterKillerGeometry.SEG2RTOPSPIKE);
        this.seg2lspike = root.getChild(CaterKillerGeometry.SEG2LSPIKE);
        this.seg2rspike = root.getChild(CaterKillerGeometry.SEG2RSPIKE);
        this.seg3 = root.getChild(CaterKillerGeometry.SEG3);
        this.seg3lfoot = root.getChild(CaterKillerGeometry.SEG3LFOOT);
        this.seg3rfoot = root.getChild(CaterKillerGeometry.SEG3RFOOT);
        this.seg3lspike = root.getChild(CaterKillerGeometry.SEG3LSPIKE);
        this.seg3rspike = root.getChild(CaterKillerGeometry.SEG3RSPIKE);
        this.seg3ltopspike = root.getChild(CaterKillerGeometry.SEG3LTOPSPIKE);
        this.seg3rtopspike = root.getChild(CaterKillerGeometry.SEG3RTOPSPIKE);
        this.seg3lbackspike = root.getChild(CaterKillerGeometry.SEG3LBACKSPIKE);
        this.seg3rbackspike = root.getChild(CaterKillerGeometry.SEG3RBACKSPIKE);
        this.allParts = new ModelPart[] {
                this.Head, this.falsehead, this.seg1, this.ltusk1, this.ltusk2, this.rtusk1, this.rtusk2, this.ljaw, this.rjaw,
                this.seg1lspike, this.seg1rspike, this.seg1ltopspike, this.seg1rtopspike, this.seg1lleg, this.seg1rleg, this.seg2,
                this.seg2lfoot, this.seg2rfoot, this.seg2ltopspike, this.seg2rtopspike, this.seg2lspike, this.seg2rspike, this.seg3,
                this.seg3lfoot, this.seg3rfoot, this.seg3lspike, this.seg3rspike, this.seg3ltopspike, this.seg3rtopspike,
                this.seg3lbackspike, this.seg3rbackspike };
    }

    /** The writes of {@code render()} before the first draw (:204-237); {@code f1} is {@code limbSwingAmount}, {@code f2} {@code ageInTicks}. */
    @Override
    public void setupAnim(final CaterKiller e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.allParts) {
            part.resetPose();
        }
        this.attacking = e.getAttacking() != 0;
        this.f1 = f1;
        this.f2 = f2;
        float newangle = 0.0f;
        float headoff = 0.0f;
        if (this.attacking) {
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.07f;
        } else {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.025f;
        }
        this.ljaw.zRot = 0.139f + newangle;
        this.rjaw.zRot = -0.139f - newangle;
        if (this.attacking) {
            headoff = Mth.cos(f2 * 1.7f * this.wingspeed) * 8.0f;
        } else {
            headoff = Mth.cos(f2 * 0.3f * this.wingspeed) * 2.0f;
        }
        this.headoff = headoff;
        this.Head.y = -8.0f + headoff;
        this.falsehead.y = -8.0f + headoff;
        this.ltusk1.y = -25.0f + headoff;
        this.ltusk2.y = -25.0f + headoff;
        this.rtusk1.y = -25.0f + headoff;
        this.rtusk2.y = -25.0f + headoff;
        this.ljaw.y = -1.0f + headoff;
        this.rjaw.y = -1.0f + headoff;
        newangle = Mth.cos(f2 * 2.11f * this.wingspeed) * 3.1415927f * 0.08f;
        this.ltusk2.yRot = 0.802f + newangle;
        newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.08f;
        this.rtusk2.yRot = -0.802f + newangle;
    }

    /** The draws of {@code render()} (:238-330) with the segment writes between them. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final float f1 = this.f1;
        final float f2 = this.f2;
        final float headoff = this.headoff;
        float newangle = 0.0f;
        float zpi = 0.0f;
        float zdist = 0.0f;
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.falsehead.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ltusk1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ltusk2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rtusk1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rtusk2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ljaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rjaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        for (int i = 0; i < 3; ++i) {
            this.seg1.y = -8.0f + headoff / (i + 1) + 8 * i;
            this.seg1lspike.y = -32.0f + headoff / (i + 1) + 8 * i;
            this.seg1rspike.y = -32.0f + headoff / (i + 1) + 8 * i;
            this.seg1ltopspike.y = -39.0f + headoff / (i + 1) + 8 * i;
            this.seg1rtopspike.y = -39.0f + headoff / (i + 1) + 8 * i;
            this.seg1lleg.y = -8.0f + headoff / (i + 1) + 8 * i;
            this.seg1rleg.y = -8.0f + headoff / (i + 1) + 8 * i;
            this.seg1.z = (float) (-12 + 14 * i);
            this.seg1lspike.z = (float) (-6 + 14 * i);
            this.seg1rspike.z = (float) (-6 + 14 * i);
            this.seg1ltopspike.z = (float) (-6 + 14 * i);
            this.seg1rtopspike.z = (float) (-6 + 14 * i);
            this.seg1lleg.z = (float) (-5 + 14 * i);
            this.seg1rleg.z = (float) (-5 + 14 * i);
            newangle = Mth.cos((float) (f2 * 0.91f * this.wingspeed + 0.39269908169872414 * i)) * 3.1415927f * 0.08f;
            this.seg1lspike.zRot = newangle;
            this.seg1rspike.zRot = -newangle;
            if (this.attacking) {
                newangle = Mth.cos((float) (f2 * 2.91f * this.wingspeed + 0.39269908169872414 * i)) * 3.1415927f * 0.15f;
            } else {
                newangle = Mth.cos((float) (f2 * 0.35f * this.wingspeed + 0.39269908169872414 * i)) * 3.1415927f * 0.04f;
            }
            this.seg1lleg.xRot = newangle;
            this.seg1rleg.xRot = -newangle;
            this.seg1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg1lspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg1rspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg1ltopspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg1rtopspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg1lleg.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg1rleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        for (int i = 0; i < 6; ++i) {
            zdist = Mth.cos(f2 * 1.7f * this.wingspeed + zpi) * 1.5f * f1;
            this.seg2.z = 39.0f + (16.0f + zdist) * i;
            this.seg2lfoot.z = 39.0f + (16.0f + zdist) * i;
            this.seg2rfoot.z = 39.0f + (16.0f + zdist) * i;
            this.seg2ltopspike.z = 39.0f + (16.0f + zdist) * i;
            this.seg2rtopspike.z = 39.0f + (16.0f + zdist) * i;
            this.seg2lspike.z = 39.0f + (16.0f + zdist) * i;
            this.seg2rspike.z = 39.0f + (16.0f + zdist) * i;
            newangle = Mth.cos((float) (f2 * 0.4f * this.wingspeed - 0.39269908169872414 * i)) * 3.1415927f * 0.07f;
            this.seg2lspike.zRot = newangle;
            this.seg2rspike.zRot = -newangle;
            this.seg2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg2lfoot.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg2rfoot.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg2ltopspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg2rtopspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg2lspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.seg2rspike.render(poseStack, buffer, packedLight, packedOverlay, color);
            zpi += 0.7853982f;
        }
        this.seg3.z = this.seg2rspike.z + 16.0f;
        this.seg3lfoot.z = this.seg3.z;
        this.seg3rfoot.z = this.seg3.z;
        this.seg3lspike.z = this.seg3.z;
        this.seg3rspike.z = this.seg3.z;
        this.seg3ltopspike.z = this.seg3.z;
        this.seg3rtopspike.z = this.seg3.z;
        this.seg3lbackspike.z = this.seg3.z + 6.0f;
        this.seg3rbackspike.z = this.seg3.z + 6.0f;
        final int i = 6;
        newangle = Mth.cos((float) (f2 * 0.4f * this.wingspeed - 0.39269908169872414 * i)) * 3.1415927f * 0.07f;
        this.seg3lspike.zRot = newangle;
        this.seg3rspike.zRot = -newangle;
        newangle = Mth.cos(f2 * 0.81f * this.wingspeed) * 3.1415927f * 0.04f;
        this.seg3lbackspike.xRot = -0.977f + newangle;
        newangle = Mth.cos(f2 * 0.87f * this.wingspeed) * 3.1415927f * 0.04f;
        this.seg3rbackspike.xRot = -0.977f + newangle;
        newangle = Mth.cos(f2 * 1.11f * this.wingspeed) * 3.1415927f * 0.04f;
        this.seg3lbackspike.yRot = 0.28f + newangle;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.04f;
        this.seg3rbackspike.yRot = -0.28f + newangle;
        this.seg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3lfoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3rfoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3lspike.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3rspike.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3ltopspike.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3rtopspike.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3lbackspike.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.seg3rbackspike.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
