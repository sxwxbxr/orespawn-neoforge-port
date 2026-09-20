package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BaryonyxGeometry;
import com.swbr.orespawn.entity.herbivore.Baryonyx;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelBaryonyx} (ModelBaryonyx.java:6-413): 52 boxes {@code Shape1..52}, 128x128
 * texture. The geometry is the generated {@link BaryonyxGeometry}; this class bakes it and animates it the way
 * {@code render()} (:330-397) did. {@code setRotationAngles} (:405-407) only called the empty {@code ModelBase}
 * version.
 *
 * <p>Arguments of the 1.7.10 {@code render(entity, f, f1, f2, f3, f4, f5)}: {@code f1} limb swing amount,
 * {@code f2} ticks existed plus partial tick - {@code limbSwingAmount} and {@code ageInTicks} here.
 */
public class BaryonyxModel extends EntityModel<Baryonyx> {

    /** Register with {@code BaryonyxGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "baryonyx"), "main");

    /** {@code ModelBaryonyx(float f1)}: {@code wingspeed = f1} (:64-65); ClientProxyOreSpawn passes 0.25 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order, which is the creation order (Shape27..51, Shape1..26, Shape52). */
    private final ModelPart[] parts;
    private final ModelPart Shape13;
    private final ModelPart Shape15;
    private final ModelPart Shape16;
    private final ModelPart Shape17;
    private final ModelPart Shape21;
    private final ModelPart Shape24;
    private final ModelPart Shape25;
    private final ModelPart Shape26;

    public BaryonyxModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[BaryonyxGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(BaryonyxGeometry.PARTS[i]);
        }
        this.Shape13 = root.getChild(BaryonyxGeometry.SHAPE13);
        this.Shape15 = root.getChild(BaryonyxGeometry.SHAPE15);
        this.Shape16 = root.getChild(BaryonyxGeometry.SHAPE16);
        this.Shape17 = root.getChild(BaryonyxGeometry.SHAPE17);
        this.Shape21 = root.getChild(BaryonyxGeometry.SHAPE21);
        this.Shape24 = root.getChild(BaryonyxGeometry.SHAPE24);
        this.Shape25 = root.getChild(BaryonyxGeometry.SHAPE25);
        this.Shape26 = root.getChild(BaryonyxGeometry.SHAPE26);
    }

    /** The angle writes of {@code render()} (:334-347). */
    @Override
    public void setupAnim(final Baryonyx entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.15f * f1;
        } else {
            newangle = 0.0f;
        }
        this.Shape24.xRot = newangle;
        this.Shape25.xRot = -0.17f + newangle;
        this.Shape26.xRot = newangle;
        this.Shape13.xRot = -newangle;
        this.Shape15.xRot = -0.17f - newangle;
        this.Shape17.xRot = -newangle;
        newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.25f;
        this.Shape21.zRot = newangle;
        this.Shape16.zRot = -newangle;
    }

    /** {@code render()} (:348-399): all 52 parts at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
