package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.IrukandjiGeometry;
import com.swbr.orespawn.entity.sea.Irukandji;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelIrukandji} (ModelIrukandji.java:7-157): a bell and four two-segment tentacles on
 * a 64x32 texture, geometry {@link IrukandjiGeometry}. Each lower segment ({@code t12}, {@code t22}, {@code t32},
 * {@code t42}) is hung at the tip of its upper segment by rewriting its pivot every frame (the faked hierarchy of
 * docs/research/06-models-design.md). {@code wingspeed} is stored but not read. No GL calls.
 */
public class IrukandjiModel extends EntityModel<Irukandji> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "irukandji"), "main");

    private final ModelPart body;
    private final ModelPart t11;
    private final ModelPart t12;
    private final ModelPart t21;
    private final ModelPart t22;
    private final ModelPart t31;
    private final ModelPart t32;
    private final ModelPart t41;
    private final ModelPart t42;

    /** {@code ModelIrukandji(float f1)} (:20-...); {@code wingspeed} has no reader. */
    public IrukandjiModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild(IrukandjiGeometry.BODY);
        this.t11 = root.getChild(IrukandjiGeometry.T11);
        this.t12 = root.getChild(IrukandjiGeometry.T12);
        this.t21 = root.getChild(IrukandjiGeometry.T21);
        this.t22 = root.getChild(IrukandjiGeometry.T22);
        this.t31 = root.getChild(IrukandjiGeometry.T31);
        this.t32 = root.getChild(IrukandjiGeometry.T32);
        this.t41 = root.getChild(IrukandjiGeometry.T41);
        this.t42 = root.getChild(IrukandjiGeometry.T42);
    }

    /** The writes of {@code render()}, in its order. */
    @Override
    public void setupAnim(final Irukandji entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.t11.resetPose();
        this.t12.resetPose();
        this.t21.resetPose();
        this.t22.resetPose();
        this.t31.resetPose();
        this.t32.resetPose();
        this.t41.resetPose();
        this.t42.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        this.t11.xRot = newangle;
        float d1 = (float) (Math.sin(newangle) * 7.0);
        float d2 = (float) (Math.cos(newangle) * 7.0);
        this.t12.z = this.t11.z + d1;
        newangle = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.1f;
        this.t11.zRot = newangle;
        float d3 = (float) (Math.cos(newangle) * d2);
        float d4 = (float) (Math.sin(newangle) * d2);
        this.t12.x = this.t11.x - d4;
        this.t12.y = this.t11.y + d3;
        newangle = Mth.cos(f2 * 0.45f) * 3.1415927f * 0.15f;
        this.t12.xRot = newangle;
        newangle = Mth.cos(f2 * 0.25f) * 3.1415927f * 0.1f;
        this.t12.zRot = newangle;
        newangle = Mth.cos(f2 * 0.65f) * 3.1415927f * 0.15f;
        this.t21.xRot = newangle;
        d1 = (float) (Math.sin(newangle) * 7.0);
        d2 = (float) (Math.cos(newangle) * 7.0);
        this.t22.z = this.t21.z + d1;
        newangle = Mth.cos(f2 * 0.45f) * 3.1415927f * 0.1f;
        this.t21.zRot = newangle;
        d3 = (float) (Math.cos(newangle) * d2);
        d4 = (float) (Math.sin(newangle) * d2);
        this.t22.x = this.t21.x - d4;
        this.t22.y = this.t21.y + d3;
        newangle = Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        this.t22.xRot = newangle;
        newangle = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.1f;
        this.t22.zRot = newangle;
        newangle = Mth.cos(f2 * 0.5f) * 3.1415927f * 0.15f;
        this.t31.xRot = newangle;
        d1 = (float) (Math.sin(newangle) * 7.0);
        d2 = (float) (Math.cos(newangle) * 7.0);
        this.t32.z = this.t31.z + d1;
        newangle = Mth.cos(f2 * 0.3f) * 3.1415927f * 0.1f;
        this.t31.zRot = newangle;
        d3 = (float) (Math.cos(newangle) * d2);
        d4 = (float) (Math.sin(newangle) * d2);
        this.t32.x = this.t31.x - d4;
        this.t32.y = this.t31.y + d3;
        newangle = Mth.cos(f2 * 0.4f) * 3.1415927f * 0.15f;
        this.t32.xRot = newangle;
        newangle = Mth.cos(f2 * 0.2f) * 3.1415927f * 0.1f;
        this.t32.zRot = newangle;
        newangle = Mth.cos(f2 * 0.57f) * 3.1415927f * 0.15f;
        this.t41.xRot = newangle;
        d1 = (float) (Math.sin(newangle) * 7.0);
        d2 = (float) (Math.cos(newangle) * 7.0);
        this.t42.z = this.t41.z + d1;
        newangle = Mth.cos(f2 * 0.37f) * 3.1415927f * 0.1f;
        this.t41.zRot = newangle;
        d3 = (float) (Math.cos(newangle) * d2);
        d4 = (float) (Math.sin(newangle) * d2);
        this.t42.x = this.t41.x - d4;
        this.t42.y = this.t41.y + d3;
        newangle = Mth.cos(f2 * 0.48f) * 3.1415927f * 0.15f;
        this.t42.xRot = newangle;
        newangle = Mth.cos(f2 * 0.29f) * 3.1415927f * 0.1f;
        this.t42.zRot = newangle;
    }

    /** Draw order of {@code render()}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t11.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t12.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t21.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t22.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t31.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t32.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t41.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.t42.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
