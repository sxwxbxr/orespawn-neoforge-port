package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.AlosaurusGeometry;
import com.swbr.orespawn.entity.dino.Alosaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelAlosaurus} (ModelAlosaurus.java:7-203): 21 boxes, 128x128. Geometry from the
 * generated {@link AlosaurusGeometry}; the animation is {@code render()} (:144-192). {@code setRotationAngles}
 * (:200-202) only called the empty {@code ModelBase} version.
 *
 * <p>{@code f1} is {@code limbSwingAmount}, {@code f2} is {@code ageInTicks}.
 */
public class AlosaurusModel extends EntityModel<Alosaurus> {

    /** Register with {@code AlosaurusGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "alosaurus"), "main");

    /** {@code wingspeed = f1} (:36); ClientProxyOreSpawn passes 0.22 (ClientProxyOreSpawn.java:46). */
    private final float wingspeed;
    /** Every part; {@code render()} draws them in creation order. */
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

    public AlosaurusModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[AlosaurusGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(AlosaurusGeometry.PARTS[i]);
        }
        this.jaw = root.getChild(AlosaurusGeometry.JAW);
        this.leftleg = root.getChild(AlosaurusGeometry.LEFTLEG);
        this.leftleg2 = root.getChild(AlosaurusGeometry.LEFTLEG2);
        this.leftleg3 = root.getChild(AlosaurusGeometry.LEFTLEG3);
        this.Shape11 = root.getChild(AlosaurusGeometry.SHAPE11);
        this.rightleg = root.getChild(AlosaurusGeometry.RIGHTLEG);
        this.rightleg2 = root.getChild(AlosaurusGeometry.RIGHTLEG2);
        this.rightleg3 = root.getChild(AlosaurusGeometry.RIGHTLEG3);
        this.leftleg4 = root.getChild(AlosaurusGeometry.LEFTLEG4);
        this.rightleg4 = root.getChild(AlosaurusGeometry.RIGHTLEG4);
        this.Shape17 = root.getChild(AlosaurusGeometry.SHAPE17);
    }

    /** The angle writes of {@code render()} (:148-170): legs, jaw while {@code getAttacking() != 0}, arms. */
    @Override
    public void setupAnim(final Alosaurus e, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
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

    /** {@code render()} (:171-191): all 21 parts at {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
