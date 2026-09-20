package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.WhaleGeometry;
import com.swbr.orespawn.entity.aquatic.Whale;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelWhale} (ModelWhale.java:7-165): 14 boxes on a 256x256 texture, no constructor
 * argument. The geometry (:24-97) is the generated {@link WhaleGeometry}.
 *
 * <p>{@code render()} (:99-154) flaps the pectoral fins, moves the jaw a little and beats the tail up and down. The
 * tail is a fake chain: each frame the pivots of {@code tail2} and the flukes are recomputed from the angle of the
 * segment before, 14 and 8 pixels along it (R8: pivot writes in {@link #setupAnim}, reset first).
 */
public class WhaleModel extends EntityModel<Whale> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "whale"), "main");

    private final ModelPart belly;
    private final ModelPart body;
    private final ModelPart back;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tailfin1;
    private final ModelPart tailfin2;
    private final ModelPart backfin;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart lfin1;
    private final ModelPart lfin2;
    private final ModelPart rfin1;
    private final ModelPart rfin2;

    /** {@code ModelWhale()} (:24-97). */
    public WhaleModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.belly = root.getChild(WhaleGeometry.BELLY);
        this.body = root.getChild(WhaleGeometry.BODY);
        this.back = root.getChild(WhaleGeometry.BACK);
        this.tail1 = root.getChild(WhaleGeometry.TAIL1);
        this.tail2 = root.getChild(WhaleGeometry.TAIL2);
        this.tailfin1 = root.getChild(WhaleGeometry.TAILFIN1);
        this.tailfin2 = root.getChild(WhaleGeometry.TAILFIN2);
        this.backfin = root.getChild(WhaleGeometry.BACKFIN);
        this.head = root.getChild(WhaleGeometry.HEAD);
        this.jaw = root.getChild(WhaleGeometry.JAW);
        this.lfin1 = root.getChild(WhaleGeometry.LFIN1);
        this.lfin2 = root.getChild(WhaleGeometry.LFIN2);
        this.rfin1 = root.getChild(WhaleGeometry.RFIN1);
        this.rfin2 = root.getChild(WhaleGeometry.RFIN2);
    }

    /** The angle and pivot writes of {@code render()} (:102-139); {@code f1} is the limb swing amount, {@code f2} the age. */
    @Override
    public void setupAnim(final Whale entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lfin1.resetPose();
        this.lfin2.resetPose();
        this.rfin1.resetPose();
        this.rfin2.resetPose();
        this.jaw.resetPose();
        this.tail1.resetPose();
        this.tail2.resetPose();
        this.tailfin1.resetPose();
        this.tailfin2.resetPose();
        // :102 - overwritten on both branches below, as in the original.
        float newangle = Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.3f) * 3.1415927f * 0.2f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.08f) * 3.1415927f * 0.05f;
        }
        this.lfin2.zRot = 0.436f + newangle;
        this.lfin1.zRot = this.lfin2.zRot / 2.0f;
        this.rfin2.zRot = -0.436f - newangle;
        this.rfin1.zRot = this.rfin2.zRot / 2.0f;
        newangle = Mth.cos(f2 * 0.03f) * 3.1415927f * 0.02f;
        this.jaw.xRot = 0.087f + newangle;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.4f) * 3.1415927f * 0.16f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.05f) * 3.1415927f * 0.03f;
        }
        this.tail1.xRot = newangle * 0.5f;
        this.tail2.xRot = newangle * 1.25f;
        final float n = newangle * 2.25f;
        this.tailfin2.xRot = n;
        this.tailfin1.xRot = n;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.xRot) * 14.0f;
        this.tail2.y = this.tail1.y - (float) Math.sin(this.tail1.xRot) * 14.0f;
        final float n2 = this.tail2.z + (float) Math.cos(this.tail2.xRot) * 8.0f;
        this.tailfin2.z = n2;
        this.tailfin1.z = n2;
        final float n3 = this.tail2.y - (float) Math.sin(this.tail2.xRot) * 8.0f;
        this.tailfin2.y = n3;
        this.tailfin1.y = n3;
    }

    /** Draw order of {@code render()} (:140-153). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.belly.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.back.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.backfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
