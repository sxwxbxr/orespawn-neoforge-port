package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.WormMediumGeometry;
import com.swbr.orespawn.entity.worm.WormMedium;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelWormMedium} (ModelWormMedium.java:7-192): three 3x12x3 segments, a 4x8x4 head
 * sleeve and four teeth, 64x32 texture. The geometry is the generated {@link WormMediumGeometry}; {@code render()}
 * (:63-181) chains body, head and teeth onto the wiggling tail and opens and closes the teeth. Only {@code f2}
 * ({@code ageInTicks}) is read.
 */
public class WormMediumModel extends EntityModel<WormMedium> {

    /** Register with {@code WormMediumGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "medium_worm"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;
    private final ModelPart tooth4;
    private final ModelPart head2;

    public WormMediumModel(final ModelPart root) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.head = root.getChild(WormMediumGeometry.HEAD);
        this.body = root.getChild(WormMediumGeometry.BODY);
        this.tail = root.getChild(WormMediumGeometry.TAIL);
        this.tooth1 = root.getChild(WormMediumGeometry.TOOTH1);
        this.tooth2 = root.getChild(WormMediumGeometry.TOOTH2);
        this.tooth3 = root.getChild(WormMediumGeometry.TOOTH3);
        this.tooth4 = root.getChild(WormMediumGeometry.TOOTH4);
        this.head2 = root.getChild(WormMediumGeometry.HEAD2);
    }

    /** The rotation and rotation-point writes of {@code render()} (:66-172). */
    @Override
    public void setupAnim(final WormMedium entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        this.head.resetPose();
        this.body.resetPose();
        this.tail.resetPose();
        this.tooth1.resetPose();
        this.tooth2.resetPose();
        this.tooth3.resetPose();
        this.tooth4.resetPose();
        this.head2.resetPose();
        final float f2 = ageInTicks;
        float newangle = Mth.cos(f2 * 0.45f) * 3.1415927f * 0.1f;
        this.tail.xRot = newangle;
        float d1 = (float) (Math.sin(newangle) * 12.0);
        float d2 = (float) (Math.cos(newangle) * 12.0);
        this.body.z = this.tail.z - d1;
        newangle = Mth.cos(f2 * 0.25f) * 3.1415927f * 0.08f;
        this.tail.zRot = newangle;
        float d3 = (float) (Math.cos(newangle) * d2);
        float d4 = (float) (Math.sin(newangle) * d2);
        this.body.x = this.tail.x + d4;
        this.body.y = (float) (this.tail.y - 12.0 + (12.0 - d3));
        newangle = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.1f;
        this.body.xRot = newangle;
        d1 = (float) (Math.sin(newangle) * 12.0);
        d2 = (float) (Math.cos(newangle) * 12.0);
        final float n = this.body.z - d1;
        this.head.z = n;
        this.head2.z = n;
        newangle = Mth.cos(f2 * 0.15f) * 3.1415927f * 0.07f;
        this.body.zRot = newangle;
        d3 = (float) (Math.cos(newangle) * d2);
        d4 = (float) (Math.sin(newangle) * d2);
        final float n2 = this.body.x + d4;
        this.head.x = n2;
        this.head2.x = n2;
        final float n3 = (float) (this.body.y - 12.0 + (12.0 - d3));
        this.head.y = n3;
        this.head2.y = n3;
        final float n4 = 0.62f + Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        this.head.xRot = n4;
        this.head2.xRot = n4;
        final float n5 = Mth.cos(f2 * 0.25f) * 3.1415927f * 0.05f;
        this.head.zRot = n5;
        this.head2.zRot = n5;
        newangle = this.head.xRot;
        final float n6 = newangle;
        this.tooth4.xRot = n6;
        this.tooth3.xRot = n6;
        this.tooth2.xRot = n6;
        this.tooth1.xRot = n6;
        d1 = (float) (Math.sin(newangle) * 12.0);
        d2 = (float) (Math.cos(newangle) * 12.0);
        final float n7 = this.head.z - d1;
        this.tooth4.z = n7;
        this.tooth3.z = n7;
        this.tooth2.z = n7;
        this.tooth1.z = n7;
        newangle = this.head.zRot;
        final float n8 = newangle;
        this.tooth4.zRot = n8;
        this.tooth3.zRot = n8;
        this.tooth2.zRot = n8;
        this.tooth1.zRot = n8;
        d3 = (float) (Math.cos(newangle) * d2);
        d4 = (float) (Math.sin(newangle) * d2);
        final float n9 = this.head.x + d4;
        this.tooth4.x = n9;
        this.tooth3.x = n9;
        this.tooth2.x = n9;
        this.tooth1.x = n9;
        final float n10 = (float) (this.head.y - 12.0 + (12.0 - d3));
        this.tooth4.y = n10;
        this.tooth3.y = n10;
        this.tooth2.y = n10;
        this.tooth1.y = n10;
        ++this.tooth1.z;
        --this.tooth2.z;
        this.tooth1.xRot = this.tooth1.xRot - 0.4f - Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        this.tooth2.xRot = this.tooth2.xRot + 0.4f + Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        ++this.tooth3.x;
        --this.tooth4.x;
        this.tooth3.zRot = this.tooth3.zRot + 0.4f + Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
        this.tooth4.zRot = this.tooth4.zRot - 0.4f - Mth.cos(f2 * 0.55f) * 3.1415927f * 0.15f;
    }

    /** {@code render()} (:173-180): head, body, tail, four teeth, head sleeve. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tooth1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tooth2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tooth3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tooth4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
