package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.WormSmallGeometry;
import com.swbr.orespawn.entity.worm.WormSmall;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelWormSmall} (ModelWormSmall.java:7-74): three 1x5x1 segments, 64x32 texture. The
 * geometry is the generated {@link WormSmallGeometry}; {@code render()} (:33-63) wiggles the tail and hangs body and
 * head on its tip by rewriting their rotation points every frame. Only {@code f2} ({@code ageInTicks}) is read.
 */
public class WormSmallModel extends EntityModel<WormSmall> {

    /** Register with {@code WormSmallGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "small_worm"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;

    public WormSmallModel(final ModelPart root) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.head = root.getChild(WormSmallGeometry.HEAD);
        this.body = root.getChild(WormSmallGeometry.BODY);
        this.tail = root.getChild(WormSmallGeometry.TAIL);
    }

    /** The rotation and rotation-point writes of {@code render()} (:36-59). */
    @Override
    public void setupAnim(final WormSmall entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        this.head.resetPose();
        this.body.resetPose();
        this.tail.resetPose();
        final float f2 = ageInTicks;
        float newangle = Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        this.tail.xRot = newangle;
        float d1 = (float) (Math.sin(newangle) * 5.0);
        float d2 = (float) (Math.cos(newangle) * 5.0);
        this.body.z = this.tail.z - d1;
        newangle = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.1f;
        this.tail.zRot = newangle;
        float d3 = (float) (Math.cos(newangle) * d2);
        float d4 = (float) (Math.sin(newangle) * d2);
        this.body.x = this.tail.x + d4;
        this.body.y = (float) (this.tail.y - 5.0 + (5.0 - d3));
        newangle = Mth.cos(f2 * 0.45f) * 3.1415927f * 0.15f;
        this.body.xRot = newangle;
        d1 = (float) (Math.sin(newangle) * 5.0);
        d2 = (float) (Math.cos(newangle) * 5.0);
        this.head.z = this.body.z - d1;
        newangle = Mth.cos(f2 * 0.25f) * 3.1415927f * 0.1f;
        this.body.zRot = newangle;
        d3 = (float) (Math.cos(newangle) * d2);
        d4 = (float) (Math.sin(newangle) * d2);
        this.head.x = this.body.x + d4;
        this.head.y = (float) (this.body.y - 5.0 + (5.0 - d3));
        this.head.xRot = 0.62f + Mth.cos(f2 * 0.65f) * 3.1415927f * 0.15f;
        this.head.zRot = Mth.cos(f2 * 0.3f) * 3.1415927f * 0.05f;
    }

    /** {@code render()} (:60-62): head, body, tail at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
