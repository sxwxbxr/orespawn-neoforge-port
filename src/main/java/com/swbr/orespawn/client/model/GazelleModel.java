package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GazelleGeometry;
import com.swbr.orespawn.entity.cannonfodder.Gazelle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelGazelle} (ModelGazelle.java:7-313): 34 boxes on a 64x64 texture - body,
 * neck, head with nose, mouth, ears and two three-part horns, four legs of four or five segments, tail. Geometry
 * (:45-221) is the generated {@link GazelleGeometry}.
 *
 * <p>{@code wingspeed} is the constructor argument, 0.65 for the gazelle (manifest {@code model_args}). The
 * writes of {@code render()} (:223-267) are {@link #setupAnim}, the draw list {@link #renderToBuffer} (R8);
 * {@code entityCutoutNoCull} as 1.7.10 {@code RendererLivingEntity} drew.
 *
 * <p>PORT (R8): {@code resetPose()} at the start. The tail is written only while standing; sitting, the 1.7.10
 * field kept the last angle drawn (one model per renderer), here it rests in the constructor pose.
 */
public class GazelleModel extends EntityModel<Gazelle> {

    /** Register with {@code GazelleGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "gazelle"), "main");

    private final ModelPart root;
    private final float wingspeed;
    private final ModelPart Chest;
    private final ModelPart lfleg1;
    private final ModelPart lrleg2;
    private final ModelPart lrleg1;
    private final ModelPart rfleg3;
    private final ModelPart rrleg2;
    private final ModelPart rrleg3;
    private final ModelPart rfleg2;
    private final ModelPart lrleg4;
    private final ModelPart tail;
    private final ModelPart lear;
    private final ModelPart rrleg1;
    private final ModelPart rfleg1;
    private final ModelPart lrleg3;
    private final ModelPart lfleg2;
    private final ModelPart rrleg5;
    private final ModelPart rrleg4;
    private final ModelPart lfleg3;
    private final ModelPart rfleg4;
    private final ModelPart lfleg4;
    private final ModelPart lrleg5;
    private final ModelPart Body;
    private final ModelPart neck;
    private final ModelPart la3;
    private final ModelPart throatfluff;
    private final ModelPart rear;
    private final ModelPart head;
    private final ModelPart ra1;
    private final ModelPart la1;
    private final ModelPart la2;
    private final ModelPart ra2;
    private final ModelPart ra3;
    private final ModelPart nose;
    private final ModelPart mouth;

    /** {@code ModelGazelle(float f1)} (:45-221). */
    public GazelleModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.Chest = root.getChild(GazelleGeometry.CHEST);
        this.lfleg1 = root.getChild(GazelleGeometry.LFLEG1);
        this.lrleg2 = root.getChild(GazelleGeometry.LRLEG2);
        this.lrleg1 = root.getChild(GazelleGeometry.LRLEG1);
        this.rfleg3 = root.getChild(GazelleGeometry.RFLEG3);
        this.rrleg2 = root.getChild(GazelleGeometry.RRLEG2);
        this.rrleg3 = root.getChild(GazelleGeometry.RRLEG3);
        this.rfleg2 = root.getChild(GazelleGeometry.RFLEG2);
        this.lrleg4 = root.getChild(GazelleGeometry.LRLEG4);
        this.tail = root.getChild(GazelleGeometry.TAIL);
        this.lear = root.getChild(GazelleGeometry.LEAR);
        this.rrleg1 = root.getChild(GazelleGeometry.RRLEG1);
        this.rfleg1 = root.getChild(GazelleGeometry.RFLEG1);
        this.lrleg3 = root.getChild(GazelleGeometry.LRLEG3);
        this.lfleg2 = root.getChild(GazelleGeometry.LFLEG2);
        this.rrleg5 = root.getChild(GazelleGeometry.RRLEG5);
        this.rrleg4 = root.getChild(GazelleGeometry.RRLEG4);
        this.lfleg3 = root.getChild(GazelleGeometry.LFLEG3);
        this.rfleg4 = root.getChild(GazelleGeometry.RFLEG4);
        this.lfleg4 = root.getChild(GazelleGeometry.LFLEG4);
        this.lrleg5 = root.getChild(GazelleGeometry.LRLEG5);
        this.Body = root.getChild(GazelleGeometry.BODY);
        this.neck = root.getChild(GazelleGeometry.NECK);
        this.la3 = root.getChild(GazelleGeometry.LA3);
        this.throatfluff = root.getChild(GazelleGeometry.THROATFLUFF);
        this.rear = root.getChild(GazelleGeometry.REAR);
        this.head = root.getChild(GazelleGeometry.HEAD);
        this.ra1 = root.getChild(GazelleGeometry.RA1);
        this.la1 = root.getChild(GazelleGeometry.LA1);
        this.la2 = root.getChild(GazelleGeometry.LA2);
        this.ra2 = root.getChild(GazelleGeometry.RA2);
        this.ra3 = root.getChild(GazelleGeometry.RA3);
        this.nose = root.getChild(GazelleGeometry.NOSE);
        this.mouth = root.getChild(GazelleGeometry.MOUTH);
    }

    /** The writes of {@code render()} (:229-267). */
    @Override
    public void setupAnim(final Gazelle entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        final Gazelle g = entity;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.12f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lfleg1.xRot = 0.297f + newangle;
        this.lfleg2.xRot = -0.074f + newangle;
        this.lfleg3.xRot = -0.409f + newangle;
        this.lfleg4.xRot = newangle;
        this.rfleg1.xRot = 0.297f - newangle;
        this.rfleg2.xRot = -0.074f - newangle;
        this.rfleg3.xRot = -0.409f - newangle;
        this.rfleg4.xRot = -newangle;
        this.lrleg1.xRot = -newangle;
        this.lrleg2.xRot = 0.185f - newangle;
        this.lrleg3.xRot = -0.074f - newangle;
        this.lrleg4.xRot = -0.409f - newangle;
        this.lrleg5.xRot = -newangle;
        this.rrleg1.xRot = newangle;
        this.rrleg2.xRot = 0.185f + newangle;
        this.rrleg3.xRot = -0.074f + newangle;
        this.rrleg4.xRot = -0.409f + newangle;
        this.rrleg5.xRot = newangle;
        newangle = Mth.cos(f2 * 0.5f) * 3.1415927f * 0.02f;
        this.head.yRot = (float) Math.toRadians(f3) * 0.45f;
        this.nose.yRot = this.head.yRot;
        this.mouth.yRot = this.head.yRot;
        this.lear.yRot = 1.57f + this.head.yRot + newangle;
        this.rear.yRot = 1.57f + this.head.yRot + newangle;
        this.la1.yRot = this.head.yRot;
        this.la2.yRot = this.head.yRot;
        this.la3.yRot = this.head.yRot;
        this.ra1.yRot = this.head.yRot;
        this.ra2.yRot = this.head.yRot;
        this.ra3.yRot = this.head.yRot;
        if (!g.isSitting()) {
            this.tail.xRot = 1.0f + Mth.cos(f2 * 0.1f) * 3.1415927f * 0.06f;
        }
    }

    /** Draw order of {@code render()} (:268-301). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Chest.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrleg4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lear.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrleg5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrleg4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfleg4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfleg4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrleg5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.la3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.throatfluff.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rear.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ra1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.la1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.la2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ra2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.ra3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.nose.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
