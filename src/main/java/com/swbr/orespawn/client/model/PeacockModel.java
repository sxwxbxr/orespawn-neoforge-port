package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.PeacockGeometry;
import com.swbr.orespawn.entity.herbivore.Peacock;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelPeacock} (ModelPeacock.java:6-190): legs, body, neck, two head boxes,
 * three head feathers and seven tail feathers (zero-thickness planes), 128x128 texture. Geometry from the generated
 * {@link PeacockGeometry}; the animation is {@code render()} (:114-179): legs, and the fan - head feathers raised
 * and the tail fanned while {@link Peacock#getBlink()} is above 0, folded otherwise.
 *
 * <p>{@code tailf7.rotateAngleZ} is written in neither branch and keeps its constructor value (0), as in the
 * original.
 */
public class PeacockModel extends EntityModel<Peacock> {

    /** Register with {@code PeacockGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "peacock"), "main");

    /** {@code ModelPeacock(float f1)}: {@code wingspeed = f1} (:28-29); ClientProxyOreSpawn passes 0.75 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:163-178), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart lleg;
    private final ModelPart rleg;
    private final ModelPart hf1;
    private final ModelPart hf2;
    private final ModelPart hf3;
    private final ModelPart tailf1;
    private final ModelPart tailf2;
    private final ModelPart tailf3;
    private final ModelPart tailf4;
    private final ModelPart tailf5;
    private final ModelPart tailf6;
    private final ModelPart tailf7;

    public PeacockModel(final ModelPart root, final float f1) {
        // The feathers are planes without thickness: culling off, as RendererLivingEntity drew them.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[PeacockGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(PeacockGeometry.PARTS[i]);
        }
        this.lleg = root.getChild(PeacockGeometry.LLEG);
        this.rleg = root.getChild(PeacockGeometry.RLEG);
        this.hf1 = root.getChild(PeacockGeometry.HF1);
        this.hf2 = root.getChild(PeacockGeometry.HF2);
        this.hf3 = root.getChild(PeacockGeometry.HF3);
        this.tailf1 = root.getChild(PeacockGeometry.TAILF1);
        this.tailf2 = root.getChild(PeacockGeometry.TAILF2);
        this.tailf3 = root.getChild(PeacockGeometry.TAILF3);
        this.tailf4 = root.getChild(PeacockGeometry.TAILF4);
        this.tailf5 = root.getChild(PeacockGeometry.TAILF5);
        this.tailf6 = root.getChild(PeacockGeometry.TAILF6);
        this.tailf7 = root.getChild(PeacockGeometry.TAILF7);
    }

    /** The angle writes of {@code render()} (:118-162). */
    @Override
    public void setupAnim(final Peacock entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final Peacock p = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.15f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lleg.xRot = newangle;
        this.rleg.xRot = -newangle;
        if (p.getBlink() > 0) {
            this.hf1.xRot = 0.401f;
            this.hf2.xRot = -0.174f;
            this.hf3.xRot = -0.698f;
            this.tailf1.xRot = 1.047f;
            this.tailf2.xRot = 1.047f;
            this.tailf3.xRot = 1.047f;
            this.tailf4.xRot = 1.047f;
            this.tailf5.xRot = 1.047f;
            this.tailf6.xRot = 1.047f;
            this.tailf7.xRot = 1.047f;
            this.tailf1.zRot = -0.4f;
            this.tailf2.zRot = -0.8f;
            this.tailf3.zRot = -1.2f;
            this.tailf4.zRot = 0.4f;
            this.tailf5.zRot = 0.8f;
            this.tailf6.zRot = 1.2f;
        } else {
            this.hf1.xRot = -1.06f;
            this.hf2.xRot = -1.06f;
            this.hf3.xRot = -1.06f;
            this.tailf1.xRot = 0.0f;
            this.tailf2.xRot = 0.0f;
            this.tailf3.xRot = 0.0f;
            this.tailf4.xRot = 0.0f;
            this.tailf5.xRot = 0.0f;
            this.tailf6.xRot = 0.0f;
            this.tailf7.xRot = 0.0f;
            this.tailf1.zRot = 0.0f;
            this.tailf2.zRot = 0.0f;
            this.tailf3.zRot = 0.0f;
            this.tailf4.zRot = 0.0f;
            this.tailf5.zRot = 0.0f;
            this.tailf6.zRot = 0.0f;
        }
    }

    /** {@code render()} (:163-178). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
