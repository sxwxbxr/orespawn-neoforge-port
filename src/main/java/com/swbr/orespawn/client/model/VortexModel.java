package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.VortexGeometry;
import com.swbr.orespawn.entity.crystal.Vortex;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelVortex} (ModelVortex.java:6-38): one flat 128x64x0 plane on a 256x128 texture,
 * geometry from the generated {@link VortexGeometry}. {@code render()} (:23-27) draws it and animates nothing.
 *
 * <p>The plane has no depth and a mostly transparent texture: {@code entityCutoutNoCull}, as 1.7.10's
 * {@code RendererLivingEntity} drew with face culling off and the alpha test on. The constructor argument
 * {@code wingspeed} (0.25, manifest) is unused, as in the original.
 */
public class VortexModel extends EntityModel<Vortex> {

    /** Register with {@code VortexGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "vortex"), "main");

    /** {@code ModelVortex(float f1)}: {@code wingspeed = f1} (:11-13); unused. */
    @SuppressWarnings("unused")
    private final float wingspeed;
    private final ModelPart Shape1;

    public VortexModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Shape1 = root.getChild(VortexGeometry.SHAPE1);
    }

    /** {@code setRotationAngles} (:35-37) only calls {@code super}: nothing moves. */
    @Override
    public void setupAnim(final Vortex entity, final float f, final float f1, final float f2, final float f3, final float f4) {
    }

    /** {@code render()} (:23-27). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
