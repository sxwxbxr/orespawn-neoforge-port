package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.TshirtGeometry;
import com.swbr.orespawn.entity.critter.Tshirt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelTshirt} (ModelTshirt.java:7-50): sleeves (256x64x1) and body
 * (128x128x1), geometry {@link TshirtGeometry}. {@code wingspeed} 0.22 in ClientProxyOreSpawn. No GL calls.
 * The model declares 512x256; {@code tshirttexture.png} is 320x160, the same aspect ratio - UVs are
 * normalised by the declared size in both versions, so the art maps unchanged (06-models-design.md,
 * "UV normalisation").
 */
public class TshirtModel extends EntityModel<Tshirt> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "t_shirt"), "main");

    private final float wingspeed;
    private final ModelPart Shape1;
    private final ModelPart Shape2;

    /** {@code ModelTshirt(float f1)} (:12-27). */
    public TshirtModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Shape1 = root.getChild(TshirtGeometry.SHAPE1);
        this.Shape2 = root.getChild(TshirtGeometry.SHAPE2);
    }

    /** The writes of {@code render()} (:32-36): both panels swing ±180° about Y together. */
    @Override
    public void setupAnim(final Tshirt entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Shape1.resetPose();
        this.Shape2.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.05f * this.wingspeed) * 3.1415927f;
        this.Shape1.yRot = newangle;
        this.Shape2.yRot = newangle;
    }

    /** Draw order of {@code render()} (:37-38). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
