package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.DragonGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.dragon.Dragon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelDragon} (ModelDragon.java:7-606): 55 boxes, 256x128 texture, geometry from the
 * generated {@link DragonGeometry}. The animation is {@code render()} (:348-539):
 * <ul>
 *   <li>legs swing with the ground speed, or fold back in flight;</li>
 *   <li>two three-link wings flap, faster in flight or attacking, each outer link hung onto the tip of the inner one;</li>
 *   <li>a six-link tail wags (still while sitting), the back spikes ride on its links;</li>
 *   <li>neck and head turn with the head yaw on the ground; in flight they lean into turns through a smoothed value
 *       kept in the entity's {@link RenderInfo} ({@code rf1});</li>
 *   <li>the lower jaw opens and snaps while attacking.</li>
 * </ul>
 * Reads {@link Dragon#getActivity()}, {@link Dragon#getAttacking()} (both synced) and {@link Dragon#isSitting()}.
 *
 * <p>The original wrote pivots and angles into fields that stayed between frames; {@code setupAnim} starts every frame
 * from the rest pose ({@code resetPose}, R8). Every written value depends only on rest values or on values written
 * earlier in the same frame, so the result is the same. The {@code rf1} smoothing runs once per rendered frame, as in
 * 1.7.10 (frame-rate dependent in both).
 *
 * <p>{@code f3} was {@code rotationYawHead - renderYawOffset} unwrapped; 1.21.1 passes it wrapped to [-180, 180)
 * (docs/research/06-models-design.md), the same value for every normal pose.
 */
public class DragonModel extends EntityModel<Dragon> {

    /** Register with {@code DragonGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "dragon"), "main");

    /** {@code ModelDragon(float f1)}: {@code wingspeed = f1} (:66-68); ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:540-594), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart horn1;
    private final ModelPart horn2;
    private final ModelPart tail6;
    private final ModelPart wing15;
    private final ModelPart spike1;
    private final ModelPart spike2;
    private final ModelPart spike3;
    private final ModelPart wing14;
    private final ModelPart spike7;
    private final ModelPart spike8;
    private final ModelPart spike9;
    private final ModelPart spike10;
    private final ModelPart head;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;
    private final ModelPart leg9;
    private final ModelPart foot1;
    private final ModelPart foot2;
    private final ModelPart leg10;
    private final ModelPart leg11;
    private final ModelPart foot3;
    private final ModelPart foot4;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart mouth1;
    private final ModelPart mouth2;
    private final ModelPart tail5;
    private final ModelPart wing1;
    private final ModelPart wing2;
    private final ModelPart wing3;
    private final ModelPart wing4;
    private final ModelPart wing5;
    private final ModelPart wing6;
    private final ModelPart wing7;
    private final ModelPart wing8;
    private final ModelPart wing9;
    private final ModelPart wing10;
    private final ModelPart wing11;
    private final ModelPart wing12;
    private final ModelPart tail4;

    public DragonModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[DragonGeometry.PARTS.length];
        for (int i = 0; i < DragonGeometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(DragonGeometry.PARTS[i]);
        }
        this.horn1 = root.getChild(DragonGeometry.HORN1);
        this.horn2 = root.getChild(DragonGeometry.HORN2);
        this.tail6 = root.getChild(DragonGeometry.TAIL6);
        this.wing15 = root.getChild(DragonGeometry.WING15);
        this.spike1 = root.getChild(DragonGeometry.SPIKE1);
        this.spike2 = root.getChild(DragonGeometry.SPIKE2);
        this.spike3 = root.getChild(DragonGeometry.SPIKE3);
        this.wing14 = root.getChild(DragonGeometry.WING14);
        this.spike7 = root.getChild(DragonGeometry.SPIKE7);
        this.spike8 = root.getChild(DragonGeometry.SPIKE8);
        this.spike9 = root.getChild(DragonGeometry.SPIKE9);
        this.spike10 = root.getChild(DragonGeometry.SPIKE10);
        this.head = root.getChild(DragonGeometry.HEAD);
        this.leg1 = root.getChild(DragonGeometry.LEG1);
        this.leg2 = root.getChild(DragonGeometry.LEG2);
        this.leg3 = root.getChild(DragonGeometry.LEG3);
        this.leg4 = root.getChild(DragonGeometry.LEG4);
        this.neck1 = root.getChild(DragonGeometry.NECK1);
        this.neck2 = root.getChild(DragonGeometry.NECK2);
        this.neck3 = root.getChild(DragonGeometry.NECK3);
        this.leg5 = root.getChild(DragonGeometry.LEG5);
        this.leg6 = root.getChild(DragonGeometry.LEG6);
        this.leg7 = root.getChild(DragonGeometry.LEG7);
        this.leg9 = root.getChild(DragonGeometry.LEG9);
        this.foot1 = root.getChild(DragonGeometry.FOOT1);
        this.foot2 = root.getChild(DragonGeometry.FOOT2);
        this.leg10 = root.getChild(DragonGeometry.LEG10);
        this.leg11 = root.getChild(DragonGeometry.LEG11);
        this.foot3 = root.getChild(DragonGeometry.FOOT3);
        this.foot4 = root.getChild(DragonGeometry.FOOT4);
        this.tail1 = root.getChild(DragonGeometry.TAIL1);
        this.tail2 = root.getChild(DragonGeometry.TAIL2);
        this.tail3 = root.getChild(DragonGeometry.TAIL3);
        this.mouth1 = root.getChild(DragonGeometry.MOUTH1);
        this.mouth2 = root.getChild(DragonGeometry.MOUTH2);
        this.tail5 = root.getChild(DragonGeometry.TAIL5);
        this.wing1 = root.getChild(DragonGeometry.WING1);
        this.wing2 = root.getChild(DragonGeometry.WING2);
        this.wing3 = root.getChild(DragonGeometry.WING3);
        this.wing4 = root.getChild(DragonGeometry.WING4);
        this.wing5 = root.getChild(DragonGeometry.WING5);
        this.wing6 = root.getChild(DragonGeometry.WING6);
        this.wing7 = root.getChild(DragonGeometry.WING7);
        this.wing8 = root.getChild(DragonGeometry.WING8);
        this.wing9 = root.getChild(DragonGeometry.WING9);
        this.wing10 = root.getChild(DragonGeometry.WING10);
        this.wing11 = root.getChild(DragonGeometry.WING11);
        this.wing12 = root.getChild(DragonGeometry.WING12);
        this.tail4 = root.getChild(DragonGeometry.TAIL4);
    }

    /** The angle and pivot writes of {@code render()} (:350-539). */
    @Override
    public void setupAnim(final Dragon entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final Dragon e = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float f3 = netHeadYaw;
        float newangle = 0.0f;
        float lspeed = 0.0f;
        RenderInfo r = null;
        float tailspeed = 0.76f;
        float tailamp = 0.45f;
        r = e.getRenderInfo();
        if (f1 > 0.001) {
            lspeed = (float) ((e.xo - e.getX()) * (e.xo - e.getX()) + (e.zo - e.getZ()) * (e.zo - e.getZ()));
            lspeed = (float) Math.sqrt(lspeed);
            newangle = Mth.cos(f2 * 1.25f * this.wingspeed) * 3.1415927f * lspeed * 0.6f;
        } else {
            newangle = 0.0f;
        }
        if (e.getActivity() != 0) {
            newangle = 1.0f;
            this.leg4.xRot = 0.557f - newangle;
            this.leg5.xRot = -0.557f - newangle;
            this.foot2.xRot = -newangle;
            this.leg3.xRot = 0.557f - newangle;
            this.leg6.xRot = -0.557f - newangle;
            this.foot1.xRot = -newangle;
            this.leg2.xRot = -0.632f + newangle;
            this.leg7.xRot = 0.89f + newangle;
            this.leg10.xRot = -0.557f + newangle;
            this.foot3.xRot = newangle;
            this.leg1.xRot = -0.632f + newangle;
            this.leg9.xRot = 0.89f + newangle;
            this.leg11.xRot = -0.557f + newangle;
            this.foot4.xRot = newangle;
        } else {
            this.leg4.xRot = 0.557f + newangle;
            this.leg5.xRot = -0.557f + newangle;
            this.foot2.xRot = newangle;
            this.leg3.xRot = 0.557f - newangle;
            this.leg6.xRot = -0.557f - newangle;
            this.foot1.xRot = -newangle;
            this.leg2.xRot = -0.632f - newangle;
            this.leg7.xRot = 0.89f - newangle;
            this.leg10.xRot = -0.557f - newangle;
            this.foot3.xRot = -newangle;
            this.leg1.xRot = -0.632f + newangle;
            this.leg9.xRot = 0.89f + newangle;
            this.leg11.xRot = -0.557f + newangle;
            this.foot4.xRot = newangle;
        }
        if (e.getAttacking() != 0) {
            if (e.getActivity() != 0) {
                newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.28f;
            } else {
                newangle = -0.45f + Mth.cos(f2 * 0.85f * this.wingspeed) * 3.1415927f * 0.2f;
            }
        } else if (e.getActivity() != 0) {
            newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.28f;
        } else {
            newangle = -0.85f + Mth.cos(f2 * 0.2f * this.wingspeed) * 3.1415927f * 0.028f;
        }
        this.wing1.zRot = newangle;
        this.wing15.zRot = newangle;
        this.wing3.zRot = newangle * 4.0f / 3.0f;
        this.wing3.y = this.wing1.y + (float) Math.sin(this.wing1.zRot) * 7.0f;
        this.wing3.x = this.wing1.x + (float) Math.cos(this.wing1.zRot) * 7.0f;
        this.wing8.zRot = newangle * 4.0f / 3.0f;
        this.wing8.y = this.wing3.y;
        this.wing8.x = this.wing3.x;
        this.wing2.zRot = newangle * 3.0f / 2.0f;
        this.wing2.y = this.wing3.y + (float) Math.sin(this.wing3.zRot) * 6.0f;
        this.wing2.x = this.wing3.x + (float) Math.cos(this.wing3.zRot) * 6.0f;
        this.wing7.zRot = newangle * 3.0f / 2.0f;
        this.wing7.y = this.wing2.y;
        this.wing7.x = this.wing2.x;
        this.wing9.zRot = newangle * 3.0f / 2.0f;
        this.wing9.y = this.wing2.y;
        this.wing9.x = this.wing2.x;
        this.wing4.zRot = -newangle;
        this.wing14.zRot = -newangle;
        this.wing5.zRot = -newangle * 4.0f / 3.0f;
        this.wing5.y = this.wing4.y - (float) Math.sin(this.wing4.zRot) * 7.0f;
        this.wing5.x = this.wing4.x - (float) Math.cos(this.wing4.zRot) * 7.0f;
        this.wing10.zRot = -newangle * 4.0f / 3.0f;
        this.wing10.y = this.wing5.y;
        this.wing10.x = this.wing5.x;
        this.wing6.zRot = -newangle * 3.0f / 2.0f;
        this.wing6.y = this.wing5.y - (float) Math.sin(this.wing5.zRot) * 6.0f;
        this.wing6.x = this.wing5.x - (float) Math.cos(this.wing5.zRot) * 6.0f;
        this.wing11.zRot = -newangle * 3.0f / 2.0f;
        this.wing11.y = this.wing6.y;
        this.wing11.x = this.wing6.x;
        this.wing12.zRot = -newangle * 3.0f / 2.0f;
        this.wing12.y = this.wing6.y;
        this.wing12.x = this.wing6.x;
        if (e.getAttacking() != 0) {
            tailspeed = 0.96f;
            tailamp = 0.75f;
        }
        if (e.getActivity() == 0 && e.getAttacking() == 0) {
            tailspeed = 0.22f;
            tailamp = 0.22f;
        }
        if (e.isSitting()) {
            tailspeed = 0.0f;
            tailamp = 0.0f;
        }
        this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * 0.04f;
        this.spike10.z = this.tail1.z;
        this.spike10.x = this.tail1.x;
        this.spike10.yRot = this.tail1.yRot;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 6.0f;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 6.0f;
        this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp * 0.125f;
        this.spike7.z = this.tail2.z;
        this.spike7.x = this.tail2.x;
        this.spike7.yRot = this.tail2.yRot;
        this.neck1.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 6.0f;
        this.neck1.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 6.0f;
        this.neck1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp * 0.25f;
        this.spike8.z = this.neck1.z;
        this.spike8.x = this.neck1.x;
        this.spike8.yRot = this.neck1.yRot;
        this.tail3.z = this.neck1.z + (float) Math.cos(this.neck1.yRot) * 6.0f;
        this.tail3.x = this.neck1.x + 1.0f + (float) Math.sin(this.neck1.yRot) * 6.0f;
        this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp * 0.375f;
        this.spike3.z = this.tail3.z;
        this.spike3.x = this.tail3.x - 1.0f;
        this.spike3.yRot = this.tail3.yRot;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 6.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 6.0f;
        this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp * 0.5f;
        this.spike9.z = this.tail4.z;
        this.spike9.x = this.tail4.x - 1.0f;
        this.spike9.yRot = this.tail4.yRot;
        this.tail6.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 6.0f;
        this.tail6.x = this.tail4.x - 1.0f + (float) Math.sin(this.tail4.yRot) * 6.0f;
        this.tail6.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp * 0.625f;
        this.tail5.z = this.tail6.z + (float) Math.cos(this.tail6.yRot) * 6.0f;
        this.tail5.x = this.tail6.x - 0.5f + (float) Math.sin(this.tail6.yRot) * 6.0f;
        this.tail5.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp * 0.75f;
        if (e.getActivity() == 1) {
            f3 = (e.yRotO - e.getYRot()) * 8.0f;
            f3 = -f3;
            final RenderInfo renderInfo = r;
            renderInfo.rf1 += (f3 - r.rf1) / 60.0f;
            if (r.rf1 > 50.0f) {
                r.rf1 = 50.0f;
            }
            if (r.rf1 < -50.0f) {
                r.rf1 = -50.0f;
            }
            f3 = r.rf1;
        } else {
            f3 /= 2.0f;
        }
        this.neck2.yRot = (float) Math.toRadians(f3) * 0.25f;
        this.spike2.yRot = this.neck2.yRot;
        this.neck3.z = this.neck2.z - (float) Math.cos(this.neck2.yRot) * 6.0f;
        this.neck3.x = this.neck2.x - (float) Math.sin(this.neck2.yRot) * 6.0f;
        this.neck3.yRot = (float) Math.toRadians(f3) * 0.5f;
        this.spike1.z = this.neck3.z;
        this.spike1.x = this.neck3.x;
        this.spike1.yRot = this.neck3.yRot;
        this.head.z = this.neck3.z - (float) Math.cos(this.neck3.yRot) * 6.0f;
        this.head.x = this.neck3.x - (float) Math.sin(this.neck3.yRot) * 6.0f;
        this.head.yRot = (float) Math.toRadians(f3) * 0.75f;
        this.mouth1.z = this.head.z;
        this.mouth1.x = this.head.x;
        this.mouth1.yRot = this.head.yRot;
        this.horn1.z = this.head.z;
        this.horn1.x = this.head.x;
        this.horn1.yRot = this.head.yRot + 0.26f;
        this.horn2.z = this.head.z;
        this.horn2.x = this.head.x;
        this.horn2.yRot = this.head.yRot - 0.26f;
        this.mouth2.z = this.head.z - (float) Math.cos(this.head.yRot) * 9.0f;
        this.mouth2.x = this.head.x - (float) Math.sin(this.head.yRot) * 9.0f;
        this.mouth2.yRot = this.head.yRot;
        newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.14f;
        if (e.getAttacking() != 0) {
            this.mouth2.xRot = 0.4f + newangle;
        } else {
            this.mouth2.xRot = 0.07f;
        }
        e.setRenderInfo(r);
    }

    /** {@code render()} (:540-594). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
