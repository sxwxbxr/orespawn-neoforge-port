package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.HerculesBeetleGeometry;
import com.swbr.orespawn.entity.arthropod.HerculesBeetle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelHerculesBeetle} (ModelHerculesBeetle.java:7-345): 37 boxes, 256x256 texture,
 * geometry from the generated {@link HerculesBeetleGeometry}. The animation is {@code render()} (:240-296): six legs in
 * three segments each, and nine jaw boxes that snap faster while the beetle attacks (DataWatcher 20).
 *
 * <p>The decompiled source reads through aliased locals ({@code lfleg4 = this.lfleg2}); written out, segment 2 and 3 of
 * every leg take the yaw of segment 1 (:247-281).
 */
public class HerculesBeetleModel extends EntityModel<HerculesBeetle> {

    /** Register with {@code HerculesBeetleGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "hercules_beetle"), "main");

    /** {@code ModelHerculesBeetle(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 1.0 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:297-333), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart lfleg1;
    private final ModelPart lfleg2;
    private final ModelPart lfleg3;
    private final ModelPart lmleg1;
    private final ModelPart lmleg2;
    private final ModelPart lmleg3;
    private final ModelPart lrleg1;
    private final ModelPart lrleg2;
    private final ModelPart lrleg3;
    private final ModelPart rfleg1;
    private final ModelPart rfleg2;
    private final ModelPart rfleg3;
    private final ModelPart rmleg1;
    private final ModelPart rmleg2;
    private final ModelPart rmleg3;
    private final ModelPart rrleg1;
    private final ModelPart rrleg2;
    private final ModelPart rrleg3;
    private final ModelPart jaw1;
    private final ModelPart jaw2;
    private final ModelPart jaw3;
    private final ModelPart jaw4;
    private final ModelPart jaw5;
    private final ModelPart jaw6;
    private final ModelPart jaw7;
    private final ModelPart jaw8;
    private final ModelPart jaw9;

    public HerculesBeetleModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[HerculesBeetleGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(HerculesBeetleGeometry.PARTS[i]);
        }
        this.lfleg1 = root.getChild(HerculesBeetleGeometry.LFLEG1);
        this.lfleg2 = root.getChild(HerculesBeetleGeometry.LFLEG2);
        this.lfleg3 = root.getChild(HerculesBeetleGeometry.LFLEG3);
        this.lmleg1 = root.getChild(HerculesBeetleGeometry.LMLEG1);
        this.lmleg2 = root.getChild(HerculesBeetleGeometry.LMLEG2);
        this.lmleg3 = root.getChild(HerculesBeetleGeometry.LMLEG3);
        this.lrleg1 = root.getChild(HerculesBeetleGeometry.LRLEG1);
        this.lrleg2 = root.getChild(HerculesBeetleGeometry.LRLEG2);
        this.lrleg3 = root.getChild(HerculesBeetleGeometry.LRLEG3);
        this.rfleg1 = root.getChild(HerculesBeetleGeometry.RFLEG1);
        this.rfleg2 = root.getChild(HerculesBeetleGeometry.RFLEG2);
        this.rfleg3 = root.getChild(HerculesBeetleGeometry.RFLEG3);
        this.rmleg1 = root.getChild(HerculesBeetleGeometry.RMLEG1);
        this.rmleg2 = root.getChild(HerculesBeetleGeometry.RMLEG2);
        this.rmleg3 = root.getChild(HerculesBeetleGeometry.RMLEG3);
        this.rrleg1 = root.getChild(HerculesBeetleGeometry.RRLEG1);
        this.rrleg2 = root.getChild(HerculesBeetleGeometry.RRLEG2);
        this.rrleg3 = root.getChild(HerculesBeetleGeometry.RRLEG3);
        this.jaw1 = root.getChild(HerculesBeetleGeometry.JAW1);
        this.jaw2 = root.getChild(HerculesBeetleGeometry.JAW2);
        this.jaw3 = root.getChild(HerculesBeetleGeometry.JAW3);
        this.jaw4 = root.getChild(HerculesBeetleGeometry.JAW4);
        this.jaw5 = root.getChild(HerculesBeetleGeometry.JAW5);
        this.jaw6 = root.getChild(HerculesBeetleGeometry.JAW6);
        this.jaw7 = root.getChild(HerculesBeetleGeometry.JAW7);
        this.jaw8 = root.getChild(HerculesBeetleGeometry.JAW8);
        this.jaw9 = root.getChild(HerculesBeetleGeometry.JAW9);
    }

    /** The writes of {@code render()} (:241-296). */
    @Override
    public void setupAnim(final HerculesBeetle b, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * this.wingspeed * 0.45f) * 3.1415927f * 0.12f * f1;
        this.lfleg1.yRot = 0.349f + newangle;
        this.lfleg2.yRot = this.lfleg1.yRot;
        this.lfleg3.yRot = this.lfleg1.yRot;
        this.lmleg1.yRot = -newangle;
        this.lmleg2.yRot = this.lmleg1.yRot;
        this.lmleg3.yRot = this.lmleg1.yRot;
        this.lrleg1.yRot = -0.349f + newangle;
        this.lrleg2.yRot = this.lrleg1.yRot;
        this.lrleg3.yRot = this.lrleg1.yRot;
        this.rfleg1.yRot = -0.349f + newangle;
        this.rfleg2.yRot = this.rfleg1.yRot;
        this.rfleg3.yRot = this.rfleg1.yRot;
        this.rmleg1.yRot = -newangle;
        this.rmleg2.yRot = this.rmleg1.yRot;
        this.rmleg3.yRot = this.rmleg1.yRot;
        this.rrleg1.yRot = 0.349f + newangle;
        this.rrleg2.yRot = this.rrleg1.yRot;
        this.rrleg3.yRot = this.rrleg1.yRot;
        if (b.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.051f * this.wingspeed) * 3.1415927f * 0.01f;
        } else {
            newangle = Mth.cos(f2 * 0.51f * this.wingspeed) * 3.1415927f * 0.07f;
        }
        this.jaw1.xRot = 0.122f + newangle;
        this.jaw2.xRot = 0.122f + newangle;
        this.jaw3.xRot = 0.0f + newangle;
        this.jaw4.xRot = 0.0f + newangle;
        this.jaw5.xRot = 0.122f + newangle;
        this.jaw6.xRot = 0.122f + newangle;
        this.jaw7.xRot = 0.0f + newangle;
        this.jaw8.xRot = 0.0f + newangle;
        this.jaw9.xRot = 0.314f + newangle;
    }

    /** {@code render()} (:297-333). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
