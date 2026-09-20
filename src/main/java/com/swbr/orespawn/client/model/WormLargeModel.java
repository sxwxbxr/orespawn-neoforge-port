package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.WormLargeGeometry;
import com.swbr.orespawn.entity.worm.WormLarge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelWormLarge} (ModelWormLarge.java:7-338): 23 parts on a 256x256 texture - a
 * five-piece head, a five-piece neck rising out of the ground, a tail and eight teeth. The geometry is the generated
 * {@link WormLargeGeometry}; {@code render()} (:153-327) sways the neck, places the head at the neck's end (32 units),
 * the teeth around the mouth (19 units ahead) and snaps them open and shut. Only {@code f2} ({@code ageInTicks}) is
 * read.
 */
public class WormLargeModel extends EntityModel<WormLarge> {

    /** Register with {@code WormLargeGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "large_worm"), "main");

    /** Every part in {@code render()} order, which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart head4;
    private final ModelPart head5;
    private final ModelPart neck1;
    private final ModelPart neck4;
    private final ModelPart neck5;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart tailtip;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;
    private final ModelPart tooth4;
    private final ModelPart tooth5;
    private final ModelPart tooth6;
    private final ModelPart tooth7;
    private final ModelPart tooth8;

    public WormLargeModel(final ModelPart root) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[WormLargeGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(WormLargeGeometry.PARTS[i]);
        }
        this.head1 = root.getChild(WormLargeGeometry.HEAD1);
        this.head2 = root.getChild(WormLargeGeometry.HEAD2);
        this.head3 = root.getChild(WormLargeGeometry.HEAD3);
        this.head4 = root.getChild(WormLargeGeometry.HEAD4);
        this.head5 = root.getChild(WormLargeGeometry.HEAD5);
        this.neck1 = root.getChild(WormLargeGeometry.NECK1);
        this.neck4 = root.getChild(WormLargeGeometry.NECK4);
        this.neck5 = root.getChild(WormLargeGeometry.NECK5);
        this.neck2 = root.getChild(WormLargeGeometry.NECK2);
        this.neck3 = root.getChild(WormLargeGeometry.NECK3);
        this.tailtip = root.getChild(WormLargeGeometry.TAILTIP);
        this.tooth1 = root.getChild(WormLargeGeometry.TOOTH1);
        this.tooth2 = root.getChild(WormLargeGeometry.TOOTH2);
        this.tooth3 = root.getChild(WormLargeGeometry.TOOTH3);
        this.tooth4 = root.getChild(WormLargeGeometry.TOOTH4);
        this.tooth5 = root.getChild(WormLargeGeometry.TOOTH5);
        this.tooth6 = root.getChild(WormLargeGeometry.TOOTH6);
        this.tooth7 = root.getChild(WormLargeGeometry.TOOTH7);
        this.tooth8 = root.getChild(WormLargeGeometry.TOOTH8);
    }

    /** The rotation and rotation-point writes of {@code render()} (:154-303). */
    @Override
    public void setupAnim(final WormLarge entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f2 = ageInTicks;
        double dist = 32.0;
        float newangle = Mth.cos(f2 * 0.25f) * 3.1415927f * 0.08f;
        newangle -= 0.698f;
        this.neck1.xRot = newangle;
        float newangle2 = Mth.cos(f2 * 0.15f) * 3.1415927f * 0.07f;
        this.neck1.yRot = newangle2;
        final float rotateAngleX = this.neck1.xRot;
        this.neck5.xRot = rotateAngleX;
        this.neck4.xRot = rotateAngleX;
        this.neck3.xRot = rotateAngleX;
        this.neck2.xRot = rotateAngleX;
        final float rotateAngleY = this.neck1.yRot;
        this.neck5.yRot = rotateAngleY;
        this.neck4.yRot = rotateAngleY;
        this.neck3.yRot = rotateAngleY;
        this.neck2.yRot = rotateAngleY;
        double d1 = (float) (Math.cos(newangle) * dist);
        double d2 = (float) (Math.sin(newangle) * dist);
        this.head1.z = (float) (this.neck1.z - d1);
        double d3 = (float) (Math.sin(newangle2) * d1);
        double d4 = (float) (Math.cos(newangle2) * d1);
        this.head1.x = (float) (this.neck1.x - d3);
        this.head1.y = (float) (this.neck1.y + d2);
        newangle = (this.head1.xRot = Mth.cos(f2 * 0.35f) * 3.1415927f * 0.15f);
        newangle2 = (this.head1.yRot = Mth.cos(f2 * 0.45f) * 3.1415927f * 0.05f);
        final float rotationPointX = this.head1.x;
        this.head5.x = rotationPointX;
        this.head4.x = rotationPointX;
        this.head3.x = rotationPointX;
        this.head2.x = rotationPointX;
        final float rotationPointY = this.head1.y;
        this.head5.y = rotationPointY;
        this.head4.y = rotationPointY;
        this.head3.y = rotationPointY;
        this.head2.y = rotationPointY;
        final float rotationPointZ = this.head1.z;
        this.head5.z = rotationPointZ;
        this.head4.z = rotationPointZ;
        this.head3.z = rotationPointZ;
        this.head2.z = rotationPointZ;
        final float rotateAngleX2 = this.head1.xRot;
        this.head5.xRot = rotateAngleX2;
        this.head4.xRot = rotateAngleX2;
        this.head3.xRot = rotateAngleX2;
        this.head2.xRot = rotateAngleX2;
        final float rotateAngleY2 = this.head1.yRot;
        this.head5.yRot = rotateAngleY2;
        this.head4.yRot = rotateAngleY2;
        this.head3.yRot = rotateAngleY2;
        this.head2.yRot = rotateAngleY2;
        dist = 19.0;
        d1 = (float) (Math.cos(newangle) * dist);
        d2 = (float) (Math.sin(newangle) * dist);
        this.tooth1.z = (float) (this.head1.z - d1);
        d3 = (float) (Math.sin(newangle2) * d1);
        d4 = (float) (Math.cos(newangle2) * d1);
        this.tooth1.x = (float) (this.head1.x - d3);
        this.tooth1.y = (float) (this.head1.y + d2 - 9.0);
        this.tooth2.z = this.tooth1.z;
        this.tooth2.x = this.tooth1.x;
        this.tooth2.y = this.tooth1.y + 18.0f;
        this.tooth3.z = this.tooth1.z;
        this.tooth3.x = this.tooth1.x + 9.0f;
        this.tooth3.y = this.tooth1.y + 9.0f;
        this.tooth4.z = this.tooth1.z;
        this.tooth4.x = this.tooth1.x - 9.0f;
        this.tooth4.y = this.tooth1.y + 9.0f;
        this.tooth5.z = this.tooth1.z;
        this.tooth5.x = this.tooth1.x - 6.0f;
        this.tooth5.y = this.tooth1.y + 9.0f - 6.0f;
        this.tooth6.z = this.tooth1.z;
        this.tooth6.x = this.tooth1.x + 6.0f;
        this.tooth6.y = this.tooth1.y + 9.0f + 6.0f;
        this.tooth7.z = this.tooth1.z;
        this.tooth7.x = this.tooth1.x + 6.0f;
        this.tooth7.y = this.tooth1.y + 9.0f - 6.0f;
        this.tooth8.z = this.tooth1.z;
        this.tooth8.x = this.tooth1.x - 6.0f;
        this.tooth8.y = this.tooth1.y + 9.0f + 6.0f;
        this.tooth1.z -= (float) (Math.sin(this.head1.xRot) * 9.0);
        this.tooth2.z += (float) (Math.sin(this.head1.xRot) * 9.0);
        this.tooth3.z -= (float) (Math.sin(this.head1.yRot) * 9.0);
        this.tooth4.z += (float) (Math.sin(this.head1.yRot) * 9.0);
        this.tooth7.z -= (float) (Math.sin(this.head1.xRot) * 6.0);
        this.tooth7.z -= (float) (Math.sin(this.head1.yRot) * 6.0);
        this.tooth6.z += (float) (Math.sin(this.head1.xRot) * 6.0);
        this.tooth6.z -= (float) (Math.sin(this.head1.yRot) * 6.0);
        this.tooth5.z -= (float) (Math.sin(this.head1.xRot) * 6.0);
        this.tooth5.z += (float) (Math.sin(this.head1.yRot) * 6.0);
        this.tooth8.z += (float) (Math.sin(this.head1.xRot) * 6.0);
        this.tooth8.z += (float) (Math.sin(this.head1.yRot) * 6.0);
        newangle = Mth.cos(f2 * 0.57f) * 3.1415927f * 0.35f;
        this.tooth1.xRot = this.head1.xRot + newangle;
        this.tooth2.xRot = this.head1.xRot - newangle;
        this.tooth3.yRot = this.head1.yRot + newangle;
        this.tooth4.yRot = this.head1.yRot - newangle;
        this.tooth5.xRot = this.head1.xRot + newangle;
        this.tooth7.xRot = this.head1.xRot + newangle;
        this.tooth6.xRot = this.head1.xRot - newangle;
        this.tooth8.xRot = this.head1.xRot - newangle;
        this.tooth6.yRot = this.head1.yRot + newangle;
        this.tooth7.yRot = this.head1.yRot + newangle;
        this.tooth5.yRot = this.head1.yRot - newangle;
        this.tooth8.yRot = this.head1.yRot - newangle;
        newangle = Mth.cos(f2 * 0.63f) * 3.1415927f * 0.15f;
        this.tailtip.xRot = newangle + 0.35f;
        newangle = Mth.cos((float) (f2 * 0.63f + 1.57075)) * 3.1415927f * 0.15f;
        this.tailtip.yRot = newangle;
    }

    /** {@code render()} (:304-326): all 23 parts in creation order at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
