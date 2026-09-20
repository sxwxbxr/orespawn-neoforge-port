package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.WaterDragonGeometry;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelWaterDragon} (ModelWaterDragon.java:9-278): head with nose, jaw, fin and ears,
 * four neck and four body segments, a three-part tail fin, four paddle legs and two fins, 128x128 texture. Geometry
 * from the generated {@link WaterDragonGeometry}; the animation is {@code render()} (:162-267):
 * <ul>
 *   <li>the body/tail chain swings on Y with {@code limbSwingAmount}, each link's pivot following the previous one
 *       (7, 5, 3 px);</li>
 *   <li>the paddles move only above {@code f1 > 0.1}; the ears always; the three fins stop while sitting;</li>
 *   <li>the jaw snaps for {@code attacking} 1, stands open for 2, else closed;</li>
 *   <li>head, nose, jaw, head fin and ears follow the head yaw at 0.75, their pivots rotated around the head pivot.</li>
 * </ul>
 * No GL state in the original: cutout, no culling (1.7.10's living renderer disabled culling).
 */
public class WaterDragonModel extends EntityModel<WaterDragon> {

    /** Register with {@code WaterDragonGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "water_dragon"), "main");

    /** {@code ModelWaterDragon(float f1)}: {@code wingspeed = f1} (:35-37); ClientProxyOreSpawn passes 0.5 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:243-266), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart Head;
    private final ModelPart Leg8;
    private final ModelPart Leg2;
    private final ModelPart Leg7;
    private final ModelPart Leg1;
    private final ModelPart body3;
    private final ModelPart body4;
    private final ModelPart tail1;
    private final ModelPart tailmiddle;
    private final ModelPart tailtop;
    private final ModelPart tailbottom;
    private final ModelPart nose;
    private final ModelPart headfin;
    private final ModelPart rightear;
    private final ModelPart leftear;
    private final ModelPart neackfin;
    private final ModelPart Bodyfin;
    private final ModelPart jaw;

    public WaterDragonModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[WaterDragonGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(WaterDragonGeometry.PARTS[i]);
        }
        this.Head = root.getChild(WaterDragonGeometry.HEAD);
        this.Leg8 = root.getChild(WaterDragonGeometry.LEG8);
        this.Leg2 = root.getChild(WaterDragonGeometry.LEG2);
        this.Leg7 = root.getChild(WaterDragonGeometry.LEG7);
        this.Leg1 = root.getChild(WaterDragonGeometry.LEG1);
        this.body3 = root.getChild(WaterDragonGeometry.BODY3);
        this.body4 = root.getChild(WaterDragonGeometry.BODY4);
        this.tail1 = root.getChild(WaterDragonGeometry.TAIL1);
        this.tailmiddle = root.getChild(WaterDragonGeometry.TAILMIDDLE);
        this.tailtop = root.getChild(WaterDragonGeometry.TAILTOP);
        this.tailbottom = root.getChild(WaterDragonGeometry.TAILBOTTOM);
        this.nose = root.getChild(WaterDragonGeometry.NOSE);
        this.headfin = root.getChild(WaterDragonGeometry.HEADFIN);
        this.rightear = root.getChild(WaterDragonGeometry.RIGHTEAR);
        this.leftear = root.getChild(WaterDragonGeometry.LEFTEAR);
        this.neackfin = root.getChild(WaterDragonGeometry.NEACKFIN);
        this.Bodyfin = root.getChild(WaterDragonGeometry.BODYFIN);
        this.jaw = root.getChild(WaterDragonGeometry.JAW);
    }

    /**
     * The angle and pivot writes of {@code render()} (:165-242), in the original order. {@code f1} is
     * {@code limbSwingAmount}, {@code f2} {@code ageInTicks}, {@code f3} the head yaw in degrees.
     */
    @Override
    public void setupAnim(final WaterDragon entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final WaterDragon e = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        final float f3 = netHeadYaw;
        float newangle = 0.0f;
        final float pi4 = 0.7853982f;
        final float root13 = (float) Math.sqrt(13.0);
        final float root14 = (float) Math.sqrt(20.0);
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.2f * f1;
        } else {
            newangle = 0.0f;
        }
        this.body3.yRot = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.4f * f1;
        this.body4.z = this.body3.z + (float) Math.cos(this.body3.yRot) * 7.0f;
        this.body4.x = this.body3.x - 1.0f + (float) Math.sin(this.body3.yRot) * 7.0f;
        this.body4.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - pi4) * 3.1415927f * 0.4f * f1;
        this.tail1.z = this.body4.z + (float) Math.cos(this.body4.yRot) * 5.0f;
        this.tail1.x = this.body4.x + (float) Math.sin(this.body4.yRot) * 5.0f;
        this.tail1.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.4f * f1;
        this.tailmiddle.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 3.0f;
        this.tailmiddle.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 3.0f;
        this.tailmiddle.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.4f * f1;
        this.tailtop.yRot = this.tailmiddle.yRot;
        this.tailtop.z = this.tailmiddle.z;
        this.tailtop.x = this.tailmiddle.x;
        this.tailbottom.yRot = this.tailmiddle.yRot;
        this.tailbottom.z = this.tailmiddle.z;
        this.tailbottom.x = this.tailmiddle.x;
        this.Leg8.yRot = 0.58f + newangle;
        this.Leg2.yRot = -0.58f + newangle;
        this.Leg7.yRot = -0.58f - newangle;
        this.Leg1.yRot = 0.58f - newangle;
        newangle = Mth.cos(f2 * 0.8f * this.wingspeed) * 3.1415927f * 0.1f;
        this.leftear.yRot = 0.62f + newangle;
        this.rightear.yRot = -0.62f - newangle;
        newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.02f;
        if (e.isSitting()) {
            newangle = 0.0f;
        }
        this.Bodyfin.zRot = newangle;
        newangle = Mth.cos(f2 * 0.6f * this.wingspeed) * 3.1415927f * 0.1f;
        if (e.isSitting()) {
            newangle = 0.0f;
        }
        this.neackfin.yRot = newangle;
        newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.05f;
        if (e.isSitting()) {
            newangle = 0.0f;
        }
        this.headfin.yRot = newangle;
        if (e.getAttacking() == 1) {
            newangle = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.25f;
            this.jaw.xRot = newangle;
        } else if (e.getAttacking() == 2) {
            this.jaw.xRot = 0.45f;
        } else {
            this.jaw.xRot = -0.25f;
        }
        newangle = (float) Math.toRadians(f3) * 0.75f;
        this.Head.yRot = newangle;
        this.nose.yRot = newangle;
        this.nose.z = this.Head.z - (float) Math.cos(this.Head.yRot) * 8.0f;
        this.nose.x = this.Head.x - (float) Math.sin(this.Head.yRot) * 8.0f;
        this.jaw.yRot = newangle;
        this.jaw.z = this.Head.z - (float) Math.cos(this.Head.yRot) * 7.0f;
        this.jaw.x = this.Head.x - (float) Math.sin(this.Head.yRot) * 7.0f - 1.0f;
        this.headfin.yRot = newangle;
        this.headfin.z = this.Head.z - (float) Math.cos(this.Head.yRot) * 3.0f;
        this.headfin.x = this.Head.x - (float) Math.sin(this.Head.yRot) * 3.0f;
        this.leftear.yRot += newangle;
        this.leftear.z = this.Head.z - (float) Math.cos(this.Head.yRot - pi4) * root13;
        this.leftear.x = this.Head.x - (float) Math.sin(this.Head.yRot - pi4) * root13;
        this.rightear.yRot += newangle;
        this.rightear.z = this.Head.z - (float) Math.cos(this.Head.yRot + pi4) * root14;
        this.rightear.x = this.Head.x - (float) Math.sin(this.Head.yRot + pi4) * root14;
    }

    /** {@code render()} (:243-266): all 24 parts in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
