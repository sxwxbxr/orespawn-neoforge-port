package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CricketGeometry;
import com.swbr.orespawn.entity.critter.Cricket;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCricket} (ModelCricket.java:7-139): body, head, abdomen, four thin
 * legs and two two-part jumping legs, geometry {@link CricketGeometry} (64x64). {@code wingspeed} 2.5 in
 * ClientProxyOreSpawn. No GL calls. The jumping legs rub while the entity's synced {@code getSinging()}
 * (DataWatcher 20) is set.
 */
public class CricketModel extends EntityModel<Cricket> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cricket"), "main");

    private final float wingspeed;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart abdomen;
    private final ModelPart lfleg;
    private final ModelPart lrleg;
    private final ModelPart rfleg;
    private final ModelPart rrleg;
    private final ModelPart lleg1;
    private final ModelPart rleg1;
    private final ModelPart lleg2;
    private final ModelPart rleg2;

    /** {@code ModelCricket(float f1)} (:21-82). */
    public CricketModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(CricketGeometry.BODY);
        this.head = root.getChild(CricketGeometry.HEAD);
        this.abdomen = root.getChild(CricketGeometry.ABDOMEN);
        this.lfleg = root.getChild(CricketGeometry.LFLEG);
        this.lrleg = root.getChild(CricketGeometry.LRLEG);
        this.rfleg = root.getChild(CricketGeometry.RFLEG);
        this.rrleg = root.getChild(CricketGeometry.RRLEG);
        this.lleg1 = root.getChild(CricketGeometry.LLEG1);
        this.rleg1 = root.getChild(CricketGeometry.RLEG1);
        this.lleg2 = root.getChild(CricketGeometry.LLEG2);
        this.rleg2 = root.getChild(CricketGeometry.RLEG2);
    }

    /**
     * The writes of {@code render()} (:85-116): the thin legs swing with the walk amount above 0.1; the
     * jumping legs either rub at three times the speed (singing) or stand spread and still.
     */
    @Override
    public void setupAnim(final Cricket c, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lfleg.resetPose();
        this.rfleg.resetPose();
        this.lrleg.resetPose();
        this.rrleg.resetPose();
        this.lleg1.resetPose();
        this.lleg2.resetPose();
        this.rleg1.resetPose();
        this.rleg2.resetPose();
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lfleg.yRot = 0.47f + newangle;
        this.rfleg.yRot = -0.54f + newangle;
        this.lrleg.yRot = -0.296f - newangle;
        this.rrleg.yRot = 0.384f - newangle;
        if (c.getSinging() != 0) {
            newangle = Mth.cos(f2 * 3.0f * this.wingspeed) * 3.1415927f * 0.25f;
            this.lleg1.yRot = -0.035f;
            this.lleg2.yRot = -0.105f;
            this.rleg1.yRot = 0.035f;
            this.rleg2.yRot = 0.105f;
        } else {
            newangle = 0.0f;
            this.lleg1.yRot = 0.436f;
            this.lleg2.yRot = 0.349f;
            this.rleg1.yRot = -0.436f;
            this.rleg2.yRot = -0.349f;
        }
        this.lleg1.xRot = newangle + 0.558f;
        this.lleg2.xRot = newangle - 0.366f;
        this.rleg1.xRot = -newangle + 0.558f;
        this.rleg2.xRot = -newangle - 0.366f;
    }

    /** Draw order of {@code render()} (:117-127). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lrleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rrleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
