package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ChipmunkGeometry;
import com.swbr.orespawn.entity.cannonfodder.Chipmunk;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelChipmunk} (ModelChipmunk.java:7-191): 18 boxes on a 64x32 texture, the
 * two hat parts only on battle chipmunks. Geometry (:28-129) is the generated {@link ChipmunkGeometry}.
 *
 * <p>{@code wingspeed} is the constructor argument, 1.0 for the chipmunk (manifest {@code model_args}).
 * {@code render()} (:131-179) wrote all angles first and then drew, so the writes are {@link #setupAnim} and the
 * draw list is {@link #renderToBuffer} (R8). Culling off and alpha test on, as 1.7.10
 * {@code RendererLivingEntity} drew: {@code entityCutoutNoCull}.
 *
 * <p>PORT (R8): {@code resetPose()} at the start. The tail is written only while the chipmunk stands; sitting,
 * the 1.7.10 fields kept the last angle any chipmunk had been drawn with (one model per renderer), here the
 * tail rests in the constructor pose.
 */
public class ChipmunkModel extends EntityModel<Chipmunk> {

    /** Register with {@code ChipmunkGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "chipmunk"), "main");

    private final ModelPart root;
    private final float wingspeed;
    private final ModelPart Cheek2;
    private final ModelPart Leg1;
    private final ModelPart Leg2;
    private final ModelPart Leg3;
    private final ModelPart Leg4;
    private final ModelPart Tail2;
    private final ModelPart Neck;
    private final ModelPart Head;
    private final ModelPart MouthUnder;
    private final ModelPart Cheek1;
    private final ModelPart Ear2;
    private final ModelPart Nose;
    private final ModelPart Ear1;
    private final ModelPart Body;
    private final ModelPart BodyTail;
    private final ModelPart Tail1;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    /** {@code ModelChipmunk(float f1)} (:28-129). */
    public ChipmunkModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.Cheek2 = root.getChild(ChipmunkGeometry.CHEEK2);
        this.Leg1 = root.getChild(ChipmunkGeometry.LEG1);
        this.Leg2 = root.getChild(ChipmunkGeometry.LEG2);
        this.Leg3 = root.getChild(ChipmunkGeometry.LEG3);
        this.Leg4 = root.getChild(ChipmunkGeometry.LEG4);
        this.Tail2 = root.getChild(ChipmunkGeometry.TAIL2);
        this.Neck = root.getChild(ChipmunkGeometry.NECK);
        this.Head = root.getChild(ChipmunkGeometry.HEAD);
        this.MouthUnder = root.getChild(ChipmunkGeometry.MOUTH_UNDER);
        this.Cheek1 = root.getChild(ChipmunkGeometry.CHEEK1);
        this.Ear2 = root.getChild(ChipmunkGeometry.EAR2);
        this.Nose = root.getChild(ChipmunkGeometry.NOSE);
        this.Ear1 = root.getChild(ChipmunkGeometry.EAR1);
        this.Body = root.getChild(ChipmunkGeometry.BODY);
        this.BodyTail = root.getChild(ChipmunkGeometry.BODY_TAIL);
        this.Tail1 = root.getChild(ChipmunkGeometry.TAIL1);
        this.Hat1 = root.getChild(ChipmunkGeometry.HAT1);
        this.Hat2 = root.getChild(ChipmunkGeometry.HAT2);
    }

    /**
     * The writes of {@code render()} (:136-157) and the hat condition (:174-178). {@code f1} limb swing amount,
     * {@code f2} age in ticks, {@code f3} head yaw minus body yaw in degrees.
     */
    @Override
    public void setupAnim(final Chipmunk entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        final Chipmunk c = entity;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.Leg1.xRot = newangle;
        this.Leg3.xRot = newangle;
        this.Leg2.xRot = -newangle;
        this.Leg4.xRot = -newangle;
        this.Head.yRot = (float) Math.toRadians(f3) * 0.45f;
        this.Nose.yRot = this.Head.yRot;
        this.Ear1.yRot = this.Head.yRot;
        this.Ear2.yRot = this.Head.yRot;
        this.MouthUnder.yRot = this.Head.yRot;
        this.Cheek1.yRot = this.Head.yRot;
        this.Cheek2.yRot = this.Head.yRot;
        this.Hat1.yRot = this.Head.yRot;
        this.Hat2.yRot = this.Head.yRot;
        if (!c.isSitting()) {
            this.Tail1.xRot = 0.306f + Mth.cos(f2 * 0.25f) * 3.1415927f * 0.06f;
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
            this.Tail1.xRot += newangle;
            this.Tail2.xRot = 0.306f + this.Tail1.xRot;
        }
        // :174-178 - Hat1 on any battle chipmunk, Hat2 only once activated.
        this.Hat1.visible = c.get_is_activated() != 0;
        this.Hat2.visible = c.get_is_activated() > 1;
    }

    /** Draw order of {@code render()} (:158-178); the hats obey {@code visible}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Cheek2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Leg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Leg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Leg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Leg4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.MouthUnder.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Cheek1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Ear2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Nose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Ear1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.BodyTail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hat1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Hat2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
