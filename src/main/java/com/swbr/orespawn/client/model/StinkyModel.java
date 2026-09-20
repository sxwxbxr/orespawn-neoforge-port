package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.StinkyGeometry;
import com.swbr.orespawn.entity.pet.Stinky;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelStinky} (ModelStinky.java:7-229): 20 boxes - body, neck, head with snout and four
 * horns, four legs, a four-piece tail and two flat wings - 128x64 texture. Geometry from the generated
 * {@link StinkyGeometry}; the animation is {@code render()} (:138-218): wings beat with the walk, legs walk on the
 * ground and tuck in flight ({@code activity} 2), the tail sways unless sitting and its pieces follow each other's
 * angle, the head, snout, neck and horns follow the look.
 *
 * <p>{@code setRotationAngles} (:226-228) only calls the empty {@code ModelBase} one.
 */
public class StinkyModel extends EntityModel<Stinky> {

    /** Register with {@code StinkyGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "stinky"), "main");

    /** {@code ModelStinky(float f1)}: {@code wingspeed = f1} (:31-33); ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:198-217), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart Rleg1;
    private final ModelPart Lleg1;
    private final ModelPart Lhorn1;
    private final ModelPart Rhorn1;
    private final ModelPart snout;
    private final ModelPart Lhorn2;
    private final ModelPart Rhorn2;
    private final ModelPart Rleg2;
    private final ModelPart Lleg2;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart Lwing;
    private final ModelPart Rwing;

    public StinkyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[StinkyGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(StinkyGeometry.PARTS[i]);
        }
        this.neck = root.getChild(StinkyGeometry.NECK);
        this.head = root.getChild(StinkyGeometry.HEAD);
        this.Rleg1 = root.getChild(StinkyGeometry.RLEG1);
        this.Lleg1 = root.getChild(StinkyGeometry.LLEG1);
        this.Lhorn1 = root.getChild(StinkyGeometry.LHORN1);
        this.Rhorn1 = root.getChild(StinkyGeometry.RHORN1);
        this.snout = root.getChild(StinkyGeometry.SNOUT);
        this.Lhorn2 = root.getChild(StinkyGeometry.LHORN2);
        this.Rhorn2 = root.getChild(StinkyGeometry.RHORN2);
        this.Rleg2 = root.getChild(StinkyGeometry.RLEG2);
        this.Lleg2 = root.getChild(StinkyGeometry.LLEG2);
        this.tail2 = root.getChild(StinkyGeometry.TAIL2);
        this.tail3 = root.getChild(StinkyGeometry.TAIL3);
        this.tail4 = root.getChild(StinkyGeometry.TAIL4);
        this.Lwing = root.getChild(StinkyGeometry.LWING);
        this.Rwing = root.getChild(StinkyGeometry.RWING);
    }

    /**
     * The angle writes of {@code render()} (:139-197). {@code f1} is the limb swing amount, {@code f2} the age in ticks,
     * {@code f3} the head yaw and {@code f4} the head pitch in degrees. {@code c.getActivity()} reads the synced activity.
     */
    @Override
    public void setupAnim(final Stinky c, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        final int current_activity = c.getActivity();
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.4f * f1;
        } else {
            newangle = 0.0f;
        }
        this.Rwing.zRot = newangle - 0.4f;
        this.Lwing.zRot = -newangle + 0.4f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        if (current_activity != 2) {
            this.Rleg1.xRot = newangle;
            this.Lleg1.xRot = -newangle;
            this.Rleg2.xRot = -newangle;
            this.Lleg2.xRot = newangle;
        } else {
            newangle = -1.0f;
            this.Rleg2.xRot = newangle;
            this.Lleg2.xRot = newangle;
            newangle = 1.0f;
            this.Rleg1.xRot = newangle;
            this.Lleg1.xRot = newangle;
        }
        newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.2f;
        if (c.isSitting()) {
            newangle = 0.0f;
        }
        this.tail2.yRot = newangle;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 4.0f;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 4.0f - 0.5f;
        this.tail3.yRot = newangle * 1.6f;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 3.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 3.0f - 0.5f;
        this.tail4.yRot = newangle * 2.6f;
        this.head.yRot = (float) Math.toRadians(f3);
        this.snout.yRot = (float) Math.toRadians(f3);
        this.neck.yRot = (float) Math.toRadians(f3) / 2.0f;
        this.Rhorn1.yRot = (float) Math.toRadians(f3);
        this.Rhorn2.yRot = (float) Math.toRadians(f3);
        this.Lhorn1.yRot = (float) Math.toRadians(f3);
        this.Lhorn2.yRot = (float) Math.toRadians(f3);
        this.head.xRot = (float) Math.toRadians(f4) / 3.0f;
        this.snout.xRot = (float) Math.toRadians(f4) / 3.0f;
        this.neck.xRot = (float) Math.toRadians(f4) / 3.0f;
        this.Rhorn1.xRot = (float) Math.toRadians(f4) / 3.0f;
        this.Rhorn2.xRot = (float) Math.toRadians(f4) / 3.0f;
        this.Lhorn1.xRot = (float) Math.toRadians(f4) / 3.0f;
        this.Lhorn2.xRot = (float) Math.toRadians(f4) / 3.0f;
    }

    /** {@code render()} (:198-217). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
