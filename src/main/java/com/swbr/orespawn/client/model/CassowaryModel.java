package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CassowaryGeometry;
import com.swbr.orespawn.entity.herbivore.Cassowary;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCassowary} (ModelCassowary.java:6-152): 12 boxes, 64x32 texture. Geometry
 * from the generated {@link CassowaryGeometry}; the animation is {@code render()} (:96-141): a walk cycle on the
 * legs, a nodding neck and the head, crest and beak hung onto the neck's end by rewriting their pivots.
 */
public class CassowaryModel extends EntityModel<Cassowary> {

    /** Register with {@code CassowaryGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cassowary"), "main");

    /** {@code ModelCassowary(float f1)}: {@code wingspeed = f1} (:24-25); ClientProxyOreSpawn passes 0.55 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:128-139), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart crest;
    private final ModelPart foot1;
    private final ModelPart foot2;
    private final ModelPart gobbler;

    public CassowaryModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[CassowaryGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(CassowaryGeometry.PARTS[i]);
        }
        this.neck = root.getChild(CassowaryGeometry.NECK);
        this.head = root.getChild(CassowaryGeometry.HEAD);
        this.beak = root.getChild(CassowaryGeometry.BEAK);
        this.leg1 = root.getChild(CassowaryGeometry.LEG1);
        this.leg2 = root.getChild(CassowaryGeometry.LEG2);
        this.crest = root.getChild(CassowaryGeometry.CREST);
        this.foot1 = root.getChild(CassowaryGeometry.FOOT1);
        this.foot2 = root.getChild(CassowaryGeometry.FOOT2);
        this.gobbler = root.getChild(CassowaryGeometry.GOBBLER);
    }

    /** The angle and pivot writes of {@code render()} (:100-127). */
    @Override
    public void setupAnim(final Cassowary entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.15f * f1;
            newangle2 = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.1f * f1;
        } else {
            newangle2 = (newangle = 0.0f);
        }
        this.foot2.xRot = newangle;
        this.leg1.xRot = newangle;
        this.foot1.xRot = -newangle;
        this.leg2.xRot = -newangle;
        this.neck.xRot = -2.827f + newangle2;
        this.gobbler.xRot = newangle2;
        final float rotationPointZ = this.neck.z + Mth.sin(this.neck.xRot) * 7.0f;
        this.beak.z = rotationPointZ;
        this.crest.z = rotationPointZ;
        this.head.z = rotationPointZ;
        final float rotationPointY = this.neck.y + Mth.cos(this.neck.xRot) * 7.0f;
        this.beak.y = rotationPointY;
        this.crest.y = rotationPointY;
        this.head.y = rotationPointY;
    }

    /** {@code render()} (:128-139). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
