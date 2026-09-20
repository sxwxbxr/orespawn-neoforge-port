package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.TRexGeometry;
import com.swbr.orespawn.entity.dino.TRex;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelTRex} (ModelTRex.java:7-245): the Alosaurus body plus tail extension and five
 * spines, 27 boxes, 128x128. Geometry from the generated {@link TRexGeometry}; the animation is {@code render()}
 * (:180-234), identical to the Alosaurus.
 */
public class TRexModel extends EntityModel<TRex> {

    /** Register with {@code TRexGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "t_rex"), "main");

    /** {@code wingspeed = f1} (:42); ClientProxyOreSpawn passes 0.2 (ClientProxyOreSpawn.java:47). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart jaw;
    private final ModelPart leftleg;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart Shape11;
    private final ModelPart rightleg;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart leftleg4;
    private final ModelPart rightleg4;
    private final ModelPart Shape17;

    public TRexModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[TRexGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(TRexGeometry.PARTS[i]);
        }
        this.jaw = root.getChild(TRexGeometry.JAW);
        this.leftleg = root.getChild(TRexGeometry.LEFTLEG);
        this.leftleg2 = root.getChild(TRexGeometry.LEFTLEG2);
        this.leftleg3 = root.getChild(TRexGeometry.LEFTLEG3);
        this.Shape11 = root.getChild(TRexGeometry.SHAPE11);
        this.rightleg = root.getChild(TRexGeometry.RIGHTLEG);
        this.rightleg2 = root.getChild(TRexGeometry.RIGHTLEG2);
        this.rightleg3 = root.getChild(TRexGeometry.RIGHTLEG3);
        this.leftleg4 = root.getChild(TRexGeometry.LEFTLEG4);
        this.rightleg4 = root.getChild(TRexGeometry.RIGHTLEG4);
        this.Shape17 = root.getChild(TRexGeometry.SHAPE17);
    }

    /** The angle writes of {@code render()} (:184-206). */
    @Override
    public void setupAnim(final TRex e, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
                          final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.rightleg.xRot = -0.174f + newangle;
        this.rightleg2.xRot = 0.506f + newangle;
        this.rightleg3.xRot = -0.401f + newangle;
        this.rightleg4.xRot = newangle;
        this.leftleg.xRot = -0.174f - newangle;
        this.leftleg2.xRot = 0.506f - newangle;
        this.leftleg3.xRot = -0.401f - newangle;
        this.leftleg4.xRot = -newangle;
        if (e.getAttacking() != 0) {
            this.jaw.xRot = 0.52f + Mth.cos(f2 * 0.45f) * 3.1415927f * 0.18f;
        } else {
            this.jaw.xRot = 0.1f;
        }
        this.Shape17.xRot = -0.523f + Mth.cos(f2 * 0.1f) * 3.1415927f * 0.05f;
        this.Shape11.xRot = -0.523f + Mth.cos(f2 * 0.1f) * 3.1415927f * 0.05f;
    }

    /** {@code render()} (:207-233): all 27 parts. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
