package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BasiliskGeometry;
import com.swbr.orespawn.entity.dino.Basilisk;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelBasilisk} (ModelBasilisk.java:7-222): 21 boxes, 256x64. Geometry from the
 * generated {@link BasiliskGeometry}; the animation is {@code render()} (:144-211): the six body segments and four tail
 * segments snake sideways, each segment hung 10-12 px behind the previous one along its yaw (the original faked the
 * hierarchy by rewriting pivots every frame), and the jaw works while attacking.
 */
public class BasiliskModel extends EntityModel<Basilisk> {

    /** Register with {@code BasiliskGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "basilisk"), "main");

    /** {@code wingspeed = f1} (:34); ClientProxyOreSpawn passes 0.3 (ClientProxyOreSpawn.java:50). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart body3;
    private final ModelPart body2;
    private final ModelPart body1;
    private final ModelPart body4;
    private final ModelPart body5;
    private final ModelPart body6;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart jaw;

    public BasiliskModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[BasiliskGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(BasiliskGeometry.PARTS[i]);
        }
        this.body3 = root.getChild(BasiliskGeometry.BODY3);
        this.body2 = root.getChild(BasiliskGeometry.BODY2);
        this.body1 = root.getChild(BasiliskGeometry.BODY1);
        this.body4 = root.getChild(BasiliskGeometry.BODY4);
        this.body5 = root.getChild(BasiliskGeometry.BODY5);
        this.body6 = root.getChild(BasiliskGeometry.BODY6);
        this.tail1 = root.getChild(BasiliskGeometry.TAIL1);
        this.tail2 = root.getChild(BasiliskGeometry.TAIL2);
        this.tail3 = root.getChild(BasiliskGeometry.TAIL3);
        this.tail4 = root.getChild(BasiliskGeometry.TAIL4);
        this.jaw = root.getChild(BasiliskGeometry.JAW);
    }

    /** The rotation and pivot writes of {@code render()} (:148-189), in their original order. */
    @Override
    public void setupAnim(final Basilisk e, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
                          final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f * f1;
        } else {
            newangle = 0.0f;
        }
        // newangle (:148-154) is computed and never read again, as in the original.
        final float pi4 = 0.7853975f;
        this.body1.yRot = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f * f1;
        this.body2.z = this.body1.z + (float) Math.cos(this.body1.yRot) * 12.0f;
        this.body2.x = this.body1.x + (float) Math.sin(this.body1.yRot) * 12.0f;
        this.body2.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - pi4) * 3.1415927f * 0.1f * f1;
        this.body3.z = this.body2.z + (float) Math.cos(this.body2.yRot) * 11.0f;
        this.body3.x = this.body2.x + (float) Math.sin(this.body2.yRot) * 11.0f;
        this.body3.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.body4.z = this.body3.z + (float) Math.cos(this.body3.yRot) * 12.0f;
        this.body4.x = this.body3.x + (float) Math.sin(this.body3.yRot) * 12.0f;
        this.body4.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.body5.z = this.body4.z + (float) Math.cos(this.body4.yRot) * 12.0f;
        this.body5.x = this.body4.x + (float) Math.sin(this.body4.yRot) * 12.0f;
        this.body5.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 4.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.body6.z = this.body5.z + (float) Math.cos(this.body5.yRot) * 12.0f;
        this.body6.x = this.body5.x + 0.5f + (float) Math.sin(this.body5.yRot) * 12.0f;
        this.body6.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 5.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.tail1.z = this.body6.z + (float) Math.cos(this.body6.yRot) * 12.0f;
        this.tail1.x = this.body6.x + 1.0f + (float) Math.sin(this.body6.yRot) * 12.0f;
        this.tail1.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 6.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 10.0f;
        this.tail2.x = this.tail1.x + 1.5f + (float) Math.sin(this.tail1.yRot) * 10.0f;
        this.tail2.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 7.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 10.0f;
        this.tail3.x = this.tail2.x + 1.0f + (float) Math.sin(this.tail2.yRot) * 10.0f;
        this.tail3.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 8.0f * pi4) * 3.1415927f * 0.1f * f1;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 10.0f;
        this.tail4.x = this.tail3.x + 1.0f + (float) Math.sin(this.tail3.yRot) * 10.0f;
        this.tail4.yRot = Mth.cos(f2 * 1.3f * this.wingspeed - 9.0f * pi4) * 3.1415927f * 0.1f * f1;
        if (e.getAttacking() != 0) {
            this.jaw.xRot = -1.0f + Mth.cos(f2 * 0.45f) * 3.1415927f * 0.18f;
        } else {
            this.jaw.xRot = -1.1f;
        }
    }

    /** {@code render()} (:190-210): all 21 parts in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
