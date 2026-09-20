package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ThePrinceGeometry;
import com.swbr.orespawn.entity.boss.prince.ThePrince;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelThePrince} (ModelThePrince.java:8-472): 35 boxes, 128x128 texture, geometry from
 * the generated {@link ThePrinceGeometry}. The animation is {@code render()} (:229-418):
 * <ul>
 *   <li>six wing plates flap with the walk speed or while attacking, otherwise they breathe slowly;</li>
 *   <li>the legs walk on the ground and hang back in flight;</li>
 *   <li>a five-link tail wags (still while sitting, faster while attacking), each link hung onto the tip of the last;</li>
 *   <li>three heads share the head yaw and pitch unevenly (two thirds, one third), their necks stretch with the entity's
 *       {@code head1ext..head3ext} and carry head, snout, fin and jaw; the jaws snap while attacking.</li>
 * </ul>
 * Reads {@link ThePrince#getActivity()}, {@link ThePrince#getAttacking()} (synced), {@link ThePrince#isSitting()} and the
 * three local head extensions.
 *
 * <p>Rest pose every frame ({@code resetPose}, R8): every written value depends only on rest values or on values
 * written earlier in the same frame. {@code f3} was unwrapped head yaw; 1.21.1 passes it wrapped to [-180, 180)
 * (docs/research/06-models-design.md), the same value for every normal pose.
 *
 * <p>The wing pass (:448-460, {@code GL_BLEND} with {@code glColor4f(0.75, 0.75, 0.75, 0.55)}) is not drawn by
 * {@link #renderToBuffer}: the renderer's translucent layer calls {@link #renderWings} with its own buffer (R8).
 */
public class ThePrinceModel extends EntityModel<ThePrince> {

    /** Register with {@code ThePrinceGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_prince"), "main");

    /** {@code ModelThePrince(float f1)}: {@code wingspeed = f1} (:47-49); ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart body;
    private final ModelPart neck1;
    private final ModelPart neck;
    private final ModelPart neckbase;
    private final ModelPart head;
    private final ModelPart Rleg1;
    private final ModelPart Lleg1;
    private final ModelPart snout;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart Lwing;
    private final ModelPart Rwing;
    private final ModelPart Tail5;
    private final ModelPart Tail6;
    private final ModelPart Lneck1;
    private final ModelPart Lneck;
    private final ModelPart Lhead;
    private final ModelPart Lsnout;
    private final ModelPart Rneck1;
    private final ModelPart Rneck;
    private final ModelPart Rhead;
    private final ModelPart Rsnout;
    private final ModelPart headfin;
    private final ModelPart Lheadfin;
    private final ModelPart Rheadfin;
    private final ModelPart Backfin;
    private final ModelPart Rwing2;
    private final ModelPart Rwing3;
    private final ModelPart Lwing2;
    private final ModelPart Lwing3;
    private final ModelPart Ljaw;
    private final ModelPart jaw;
    private final ModelPart Rjaw;

    public ThePrinceModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[ThePrinceGeometry.PARTS.length];
        for (int i = 0; i < ThePrinceGeometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(ThePrinceGeometry.PARTS[i]);
        }
        this.body = root.getChild(ThePrinceGeometry.BODY);
        this.neck1 = root.getChild(ThePrinceGeometry.NECK1);
        this.neck = root.getChild(ThePrinceGeometry.NECK);
        this.neckbase = root.getChild(ThePrinceGeometry.NECKBASE);
        this.head = root.getChild(ThePrinceGeometry.HEAD);
        this.Rleg1 = root.getChild(ThePrinceGeometry.RLEG1);
        this.Lleg1 = root.getChild(ThePrinceGeometry.LLEG1);
        this.snout = root.getChild(ThePrinceGeometry.SNOUT);
        this.tail1 = root.getChild(ThePrinceGeometry.TAIL1);
        this.tail2 = root.getChild(ThePrinceGeometry.TAIL2);
        this.tail3 = root.getChild(ThePrinceGeometry.TAIL3);
        this.tail4 = root.getChild(ThePrinceGeometry.TAIL4);
        this.Lwing = root.getChild(ThePrinceGeometry.LWING);
        this.Rwing = root.getChild(ThePrinceGeometry.RWING);
        this.Tail5 = root.getChild(ThePrinceGeometry.TAIL5);
        this.Tail6 = root.getChild(ThePrinceGeometry.TAIL6);
        this.Lneck1 = root.getChild(ThePrinceGeometry.LNECK1);
        this.Lneck = root.getChild(ThePrinceGeometry.LNECK);
        this.Lhead = root.getChild(ThePrinceGeometry.LHEAD);
        this.Lsnout = root.getChild(ThePrinceGeometry.LSNOUT);
        this.Rneck1 = root.getChild(ThePrinceGeometry.RNECK1);
        this.Rneck = root.getChild(ThePrinceGeometry.RNECK);
        this.Rhead = root.getChild(ThePrinceGeometry.RHEAD);
        this.Rsnout = root.getChild(ThePrinceGeometry.RSNOUT);
        this.headfin = root.getChild(ThePrinceGeometry.HEADFIN);
        this.Lheadfin = root.getChild(ThePrinceGeometry.LHEADFIN);
        this.Rheadfin = root.getChild(ThePrinceGeometry.RHEADFIN);
        this.Backfin = root.getChild(ThePrinceGeometry.BACKFIN);
        this.Rwing2 = root.getChild(ThePrinceGeometry.RWING2);
        this.Rwing3 = root.getChild(ThePrinceGeometry.RWING3);
        this.Lwing2 = root.getChild(ThePrinceGeometry.LWING2);
        this.Lwing3 = root.getChild(ThePrinceGeometry.LWING3);
        this.Ljaw = root.getChild(ThePrinceGeometry.LJAW);
        this.jaw = root.getChild(ThePrinceGeometry.JAW);
        this.Rjaw = root.getChild(ThePrinceGeometry.RJAW);
    }

    /** The angle and pivot writes of {@code render()} (:230-418). */
    @Override
    public void setupAnim(final ThePrince entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final ThePrince c = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        final float f3 = netHeadYaw;
        final float f4 = headPitch;
        float newangle = 0.0f;
        final int current_activity = c.getActivity();
        if (f1 > 0.1 || c.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.4f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.04f;
        }
        this.Rwing.zRot = newangle - 0.4f;
        this.Rwing2.zRot = newangle - 0.6f;
        this.Rwing3.zRot = newangle - 0.2f;
        this.Lwing.zRot = -newangle + 0.4f;
        this.Lwing2.zRot = -newangle + 0.6f;
        this.Lwing3.zRot = -newangle + 0.2f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        if (current_activity != 2 || c.getAttacking() != 0) {
            this.Rleg1.xRot = newangle;
            this.Lleg1.xRot = -newangle;
        } else {
            newangle = -1.0f;
            this.Rleg1.xRot = newangle;
            this.Lleg1.xRot = newangle;
        }
        newangle = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.06f;
        if (c.isSitting()) {
            newangle = 0.0f;
        }
        if (c.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.12f;
        }
        this.tail2.yRot = newangle;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 6.0f;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 6.0f;
        this.tail3.yRot = newangle * 1.6f;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 5.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 5.0f;
        this.tail4.yRot = newangle * 2.6f;
        this.Tail5.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 4.0f;
        this.Tail5.x = this.tail4.x + (float) Math.sin(this.tail4.yRot) * 4.0f;
        this.Tail5.yRot = newangle * 3.6f;
        this.Tail6.z = this.Tail5.z + (float) Math.cos(this.Tail5.yRot) * 4.0f;
        this.Tail6.x = this.Tail5.x + (float) Math.sin(this.Tail5.yRot) * 4.0f;
        this.Tail6.yRot = newangle * 4.6f;
        float h4;
        float h3;
        float h2 = h3 = (h4 = f3 * 2.0f / 3.0f);
        float d4;
        float d3;
        float d2 = d3 = (d4 = f4 * 2.0f / 3.0f);
        if (h3 < 0.0f) {
            h4 = (h2 = h3 / 2.0f);
            d4 = (d2 = d3 / 2.0f);
        } else {
            h3 = (h2 = h4 / 2.0f);
            d3 = (d2 = d4 / 2.0f);
        }
        this.head.yRot = (float) Math.toRadians(h2);
        this.snout.yRot = (float) Math.toRadians(h2);
        this.headfin.yRot = (float) Math.toRadians(h2);
        this.jaw.yRot = (float) Math.toRadians(h2);
        this.jaw.z = this.snout.z - (float) Math.cos(this.snout.yRot);
        this.jaw.x = this.snout.x - (float) Math.sin(this.snout.yRot);
        this.neck.yRot = (float) Math.toRadians(h2) / 2.0f;
        this.Lhead.yRot = (float) Math.toRadians(h3);
        this.Lsnout.yRot = (float) Math.toRadians(h3);
        this.Lheadfin.yRot = (float) Math.toRadians(h3);
        this.Ljaw.yRot = (float) Math.toRadians(h3);
        this.Ljaw.z = this.Lsnout.z - (float) Math.cos(this.Lsnout.yRot);
        this.Ljaw.x = this.Lsnout.x - (float) Math.sin(this.Lsnout.yRot);
        this.Lneck.yRot = (float) Math.toRadians(h3) / 2.0f;
        this.Rhead.yRot = (float) Math.toRadians(h4);
        this.Rsnout.yRot = (float) Math.toRadians(h4);
        this.Rheadfin.yRot = (float) Math.toRadians(h4);
        this.Rjaw.yRot = (float) Math.toRadians(h4);
        this.Rjaw.z = this.Rsnout.z - (float) Math.cos(this.Rsnout.yRot);
        this.Rjaw.x = this.Rsnout.x - (float) Math.sin(this.Rsnout.yRot);
        this.Rneck.yRot = (float) Math.toRadians(h4) / 2.0f;
        float Rjx;
        float Ljx;
        float jx = Ljx = (Rjx = 0.0f);
        if (c.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.9f * this.wingspeed) * 3.1415927f * 0.2f;
            Ljx = 0.2f + newangle;
            newangle = Mth.cos(f2 * 2.1f * this.wingspeed) * 3.1415927f * 0.2f;
            Rjx = 0.2f + newangle;
            newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.2f;
            jx = 0.2f + newangle;
        }
        this.head.xRot = (float) Math.toRadians(d2);
        this.snout.xRot = (float) Math.toRadians(d2);
        this.headfin.xRot = (float) Math.toRadians(d2);
        this.jaw.xRot = (float) Math.toRadians(d2) + jx;
        this.Lhead.xRot = (float) Math.toRadians(d3);
        this.Lsnout.xRot = (float) Math.toRadians(d3);
        this.Lheadfin.xRot = (float) Math.toRadians(d3);
        this.Ljaw.xRot = (float) Math.toRadians(d3) + Ljx;
        this.Rhead.xRot = (float) Math.toRadians(d4);
        this.Rsnout.xRot = (float) Math.toRadians(d4);
        this.Rheadfin.xRot = (float) Math.toRadians(d4);
        this.Rjaw.xRot = (float) Math.toRadians(d4) + Rjx;
        d3 = (float) c.getHead1Ext();
        d2 = (float) c.getHead2Ext();
        d4 = (float) c.getHead3Ext();
        this.Lneck.xRot = (float) Math.toRadians(d3);
        this.neck.xRot = (float) Math.toRadians(d2);
        this.Rneck.xRot = (float) Math.toRadians(d4);
        this.Lhead.y = this.Lneck.y - (float) Math.cos(this.Lneck.xRot) * 7.0f;
        this.Ljaw.y = this.Lhead.y;
        this.Lsnout.y = this.Lhead.y;
        this.Lheadfin.y = this.Lhead.y;
        this.Lhead.z = this.Lneck.z - (float) Math.sin(this.Lneck.xRot) * 7.0f;
        this.Ljaw.z = this.Lhead.z;
        this.Lsnout.z = this.Lhead.z;
        this.Lheadfin.z = this.Lhead.z;
        this.Lhead.x = this.Lneck.x - (float) Math.sin(this.Lneck.yRot) * 7.0f * (float) Math.sin(this.Lneck.xRot);
        this.Ljaw.x = this.Lhead.x;
        this.Lsnout.x = this.Lhead.x;
        this.Lheadfin.x = this.Lhead.x;
        this.Rhead.y = this.Rneck.y - (float) Math.cos(this.Rneck.xRot) * 7.0f;
        this.Rjaw.y = this.Rhead.y;
        this.Rsnout.y = this.Rhead.y;
        this.Rheadfin.y = this.Rhead.y;
        this.Rhead.z = this.Rneck.z - (float) Math.sin(this.Rneck.xRot) * 7.0f;
        this.Rjaw.z = this.Rhead.z;
        this.Rsnout.z = this.Rhead.z;
        this.Rheadfin.z = this.Rhead.z;
        this.Rhead.x = this.Rneck.x - (float) Math.sin(this.Rneck.yRot) * 7.0f * (float) Math.sin(this.Rneck.xRot);
        this.Rjaw.x = this.Rhead.x;
        this.Rsnout.x = this.Rhead.x;
        this.Rheadfin.x = this.Rhead.x;
        this.head.y = this.neck.y - (float) Math.cos(this.neck.xRot) * 7.0f;
        this.jaw.y = this.head.y;
        this.snout.y = this.head.y;
        this.headfin.y = this.head.y;
        this.head.z = this.neck.z - (float) Math.sin(this.neck.xRot) * 7.0f;
        this.jaw.z = this.head.z;
        this.snout.z = this.head.z;
        this.headfin.z = this.head.z;
        this.head.x = this.neck.x - (float) Math.sin(this.neck.yRot) * 7.0f * (float) Math.sin(this.neck.xRot);
        this.jaw.x = this.head.x;
        this.snout.x = this.head.x;
        this.headfin.x = this.head.x;
    }

    /** The opaque part of {@code render()} (:419-447), in the original order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neckbase.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.snout.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lneck1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lneck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lhead.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lsnout.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rneck1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rneck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rhead.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rsnout.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.headfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lheadfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rheadfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Backfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Ljaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rjaw.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    /**
     * The blended part of {@code render()} (:448-460): the six wing plates. {@code color} carries
     * {@code glColor4f(0.75, 0.75, 0.75, 0.55)}; the buffer must be a translucent entity type.
     */
    public void renderWings(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                            final int packedOverlay, final int color) {
        this.Rwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rwing3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lwing3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Lwing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Rwing.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
