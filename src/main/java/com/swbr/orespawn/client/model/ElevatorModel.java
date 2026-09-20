package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ElevatorGeometry;
import com.swbr.orespawn.entity.vehicle.Elevator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelElevator} (ModelElevator.java:6-65): the hoverboard, a flat
 * 8x1x16 deck ({@code Shape1}) with a two-step nose and tail ({@code Shape2}/{@code Shape3} front,
 * {@code Shape4}/{@code Shape5} back), 64x64 texture. The geometry (:15-44) is the generated
 * {@link ElevatorGeometry}; this class bakes it and draws the parts in {@code render()} order.
 *
 * <p>Nothing is animated: {@code setRotationAngles} (:62-64) only calls the empty
 * {@code ModelBase} version, and {@code render()} (:46-54) writes no angle or pivot. The field
 * {@code wingspeed} (:8, :16) is never read and has no successor. The manifest lists this model as
 * unused - a false alarm, {@code RenderElevator} instantiates it directly (RenderElevator.java:17,
 * catalogue 6.6).
 */
public class ElevatorModel extends EntityModel<Elevator> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "hoverboard"), "main");

    private final ModelPart Shape1;
    private final ModelPart Shape2;
    private final ModelPart Shape3;
    private final ModelPart Shape4;
    private final ModelPart Shape5;

    public ElevatorModel(final ModelPart root) {
        // A 1.7.10 Render (not RenderLiving) drew with the world's GL state: alpha test on, blending
        // off, back-face culling on - RendererLivingEntity was the one that disabled culling. The
        // renderer's scale(-1, -1, 1) keeps the winding order (determinant +1), so culling is safe.
        super(RenderType::entityCutout);
        this.Shape1 = root.getChild(ElevatorGeometry.SHAPE1);
        this.Shape2 = root.getChild(ElevatorGeometry.SHAPE2);
        this.Shape3 = root.getChild(ElevatorGeometry.SHAPE3);
        this.Shape4 = root.getChild(ElevatorGeometry.SHAPE4);
        this.Shape5 = root.getChild(ElevatorGeometry.SHAPE5);
    }

    /** {@code setRotationAngles} (:62-64): no writes, so no {@code resetPose()} is needed either. */
    @Override
    public void setupAnim(final Elevator entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
    }

    /** {@code render()} (:46-54): Shape1, Shape2, Shape3, Shape4, Shape5 at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape5.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
