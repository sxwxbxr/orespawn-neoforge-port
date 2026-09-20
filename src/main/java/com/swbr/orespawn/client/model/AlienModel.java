package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.AlienGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.monster.Alien;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelAlien} (ModelAlien.java:7-645): 55 boxes, 256x128 texture, geometry
 * {@link AlienGeometry}. {@code render()} (:348-554) splits into {@link #setupAnim} (all angle and pivot writes,
 * including the helpers {@code doLeftLeg}, {@code doRightLeg}, {@code doJaw}, {@code doTail}, {@code doLeftClaw},
 * {@code doRightClaw}) and {@link #renderToBuffer} (the draw calls in their order). No GL calls in the model.
 *
 * <p>Arguments of the 1.7.10 {@code render(entity, f, f1, f2, f3, f4, f5)}: {@code f1} limb swing amount, {@code f2}
 * ticks plus partial tick, {@code f3} head yaw - {@code limbSwingAmount}, {@code ageInTicks}, {@code netHeadYaw} here.
 *
 * <p>PORT: the {@code RenderInfo} the original kept on the entity ({@code e.getRenderInfo()} / {@code setRenderInfo},
 * :453-498) is client-only scratch space; it lives here per entity instance, weakly keyed so a removed alien lets go of
 * it. A new record starts zeroed, as {@code Alien.entityInit} zeroed it. Its random draws use the entity's (client)
 * level random, as {@code e.worldObj.rand} did.
 */
public class AlienModel extends EntityModel<Alien> {

    /** Register with {@code AlienGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "alien"), "main");

    /** {@code ModelAlien(float f1)}: {@code wingspeed = f1} (:67-68); ClientProxyOreSpawn passes 0.22 (manifest). */
    private final float wingspeed;
    /** {@code Alien.renderdata} per entity (see the class Javadoc). */
    private final Map<Alien, RenderInfo> renderInfos = new WeakHashMap<>();

    private final ModelPart torso;
    private final ModelPart stomach;
    private final ModelPart rThigh;
    private final ModelPart lThigh;
    private final ModelPart lShin;
    private final ModelPart rShin;
    private final ModelPart lShin1;
    private final ModelPart rShin1;
    private final ModelPart lFoot;
    private final ModelPart rFoot;
    private final ModelPart neck;
    private final ModelPart fan;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail1;
    private final ModelPart fanl1;
    private final ModelPart fanr1;
    private final ModelPart fanl2;
    private final ModelPart fanr2;
    private final ModelPart fanl3;
    private final ModelPart fanr3;
    private final ModelPart fanl4;
    private final ModelPart fanr4;
    private final ModelPart fanl5;
    private final ModelPart fanr5;
    private final ModelPart fanl6;
    private final ModelPart fanr6;
    private final ModelPart spike4;
    private final ModelPart spike5;
    private final ModelPart spike3;
    private final ModelPart fanl7;
    private final ModelPart fanr7;
    private final ModelPart head;
    private final ModelPart head1;
    private final ModelPart jaw1;
    private final ModelPart head2;
    private final ModelPart jaw2;
    private final ModelPart fang1;
    private final ModelPart fang2;
    private final ModelPart fang3;
    private final ModelPart fang4;
    private final ModelPart spike2;
    private final ModelPart spike1;
    private final ModelPart arml1;
    private final ModelPart armr1;
    private final ModelPart arml2;
    private final ModelPart armr2;
    private final ModelPart clawr1;
    private final ModelPart clawr2;
    private final ModelPart clawr3;
    private final ModelPart clawl2;
    private final ModelPart clawl3;
    private final ModelPart clawl1;
    /** The draw order of {@code render()} (:499-553). */
    private final ModelPart[] drawOrder;

    public AlienModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.torso = root.getChild(AlienGeometry.TORSO);
        this.stomach = root.getChild(AlienGeometry.STOMACH);
        this.rThigh = root.getChild(AlienGeometry.R_THIGH);
        this.lThigh = root.getChild(AlienGeometry.L_THIGH);
        this.lShin = root.getChild(AlienGeometry.L_SHIN);
        this.rShin = root.getChild(AlienGeometry.R_SHIN);
        this.lShin1 = root.getChild(AlienGeometry.L_SHIN1);
        this.rShin1 = root.getChild(AlienGeometry.R_SHIN1);
        this.lFoot = root.getChild(AlienGeometry.L_FOOT);
        this.rFoot = root.getChild(AlienGeometry.R_FOOT);
        this.neck = root.getChild(AlienGeometry.NECK);
        this.fan = root.getChild(AlienGeometry.FAN);
        this.tail2 = root.getChild(AlienGeometry.TAIL2);
        this.tail3 = root.getChild(AlienGeometry.TAIL3);
        this.tail4 = root.getChild(AlienGeometry.TAIL4);
        this.tail5 = root.getChild(AlienGeometry.TAIL5);
        this.tail1 = root.getChild(AlienGeometry.TAIL1);
        this.fanl1 = root.getChild(AlienGeometry.FANL1);
        this.fanr1 = root.getChild(AlienGeometry.FANR1);
        this.fanl2 = root.getChild(AlienGeometry.FANL2);
        this.fanr2 = root.getChild(AlienGeometry.FANR2);
        this.fanl3 = root.getChild(AlienGeometry.FANL3);
        this.fanr3 = root.getChild(AlienGeometry.FANR3);
        this.fanl4 = root.getChild(AlienGeometry.FANL4);
        this.fanr4 = root.getChild(AlienGeometry.FANR4);
        this.fanl5 = root.getChild(AlienGeometry.FANL5);
        this.fanr5 = root.getChild(AlienGeometry.FANR5);
        this.fanl6 = root.getChild(AlienGeometry.FANL6);
        this.fanr6 = root.getChild(AlienGeometry.FANR6);
        this.spike4 = root.getChild(AlienGeometry.SPIKE4);
        this.spike5 = root.getChild(AlienGeometry.SPIKE5);
        this.spike3 = root.getChild(AlienGeometry.SPIKE3);
        this.fanl7 = root.getChild(AlienGeometry.FANL7);
        this.fanr7 = root.getChild(AlienGeometry.FANR7);
        this.head = root.getChild(AlienGeometry.HEAD);
        this.head1 = root.getChild(AlienGeometry.HEAD1);
        this.jaw1 = root.getChild(AlienGeometry.JAW1);
        this.head2 = root.getChild(AlienGeometry.HEAD2);
        this.jaw2 = root.getChild(AlienGeometry.JAW2);
        this.fang1 = root.getChild(AlienGeometry.FANG1);
        this.fang2 = root.getChild(AlienGeometry.FANG2);
        this.fang3 = root.getChild(AlienGeometry.FANG3);
        this.fang4 = root.getChild(AlienGeometry.FANG4);
        this.spike2 = root.getChild(AlienGeometry.SPIKE2);
        this.spike1 = root.getChild(AlienGeometry.SPIKE1);
        this.arml1 = root.getChild(AlienGeometry.ARML1);
        this.armr1 = root.getChild(AlienGeometry.ARMR1);
        this.arml2 = root.getChild(AlienGeometry.ARML2);
        this.armr2 = root.getChild(AlienGeometry.ARMR2);
        this.clawr1 = root.getChild(AlienGeometry.CLAWR1);
        this.clawr2 = root.getChild(AlienGeometry.CLAWR2);
        this.clawr3 = root.getChild(AlienGeometry.CLAWR3);
        this.clawl2 = root.getChild(AlienGeometry.CLAWL2);
        this.clawl3 = root.getChild(AlienGeometry.CLAWL3);
        this.clawl1 = root.getChild(AlienGeometry.CLAWL1);
        this.drawOrder = new ModelPart[] {
                this.torso, this.stomach, this.rThigh, this.lThigh, this.lShin, this.rShin, this.lShin1, this.rShin1,
                this.lFoot, this.rFoot, this.neck, this.tail2, this.tail3, this.tail4, this.tail5, this.tail1, this.spike4,
                this.spike5, this.spike3, this.head, this.head1, this.jaw1, this.head2, this.jaw2, this.fang1, this.fang2,
                this.fang3, this.fang4, this.spike2, this.spike1, this.arml1, this.armr1, this.arml2, this.armr2,
                this.clawr1, this.clawr2, this.clawr3, this.clawl2, this.clawl3, this.clawl1, this.fan, this.fanl1,
                this.fanr1, this.fanl2, this.fanr2, this.fanl3, this.fanr3, this.fanl4, this.fanr4, this.fanl5, this.fanr5,
                this.fanl6, this.fanr6, this.fanl7, this.fanr7 };
    }

    /** The writes of {@code render()} (:353-498). */
    @Override
    public void setupAnim(final Alien e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float nextangle = 0.0f;
        newangle = Mth.cos(f2 * 4.0f * this.wingspeed) * 3.1415927f * 0.5f * f1;
        this.doLeftLeg(newangle);
        this.doRightLeg(-newangle);
        if (e.getAttacking() == 0) {
            this.fan.zRot = 0.0f;
            this.fanl1.zRot = 0.0f;
            this.fanl2.zRot = 0.0f;
            this.fanl3.zRot = 0.0f;
            this.fanl4.zRot = 0.0f;
            this.fanl5.zRot = 0.0f;
            this.fanl6.zRot = 0.0f;
            this.fanl7.zRot = 0.0f;
            this.fanr1.zRot = 0.0f;
            this.fanr2.zRot = 0.0f;
            this.fanr3.zRot = 0.0f;
            this.fanr4.zRot = 0.0f;
            this.fanr5.zRot = 0.0f;
            this.fanr6.zRot = 0.0f;
            this.fanr7.zRot = 0.0f;
            this.fan.xRot = -1.85f;
            this.fanl1.xRot = -1.85f;
            this.fanl2.xRot = -1.85f;
            this.fanl3.xRot = -1.85f;
            this.fanl4.xRot = -1.85f;
            this.fanl5.xRot = -1.85f;
            this.fanl6.xRot = -1.85f;
            this.fanl7.xRot = -1.85f;
            this.fanr1.xRot = -1.85f;
            this.fanr2.xRot = -1.85f;
            this.fanr3.xRot = -1.85f;
            this.fanr4.xRot = -1.85f;
            this.fanr5.xRot = -1.85f;
            this.fanr6.xRot = -1.85f;
            this.fanr7.xRot = -1.85f;
        } else {
            final float pi6 = 0.5235988f;
            final float fanspeed = 1.22f;
            final float fanamp = 0.1f;
            this.fan.xRot = Mth.cos(f2 * fanspeed * this.wingspeed) * 3.1415927f * fanamp;
            this.fanl1.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 1.0f * pi6) * 3.1415927f * fanamp;
            this.fanl2.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 2.0f * pi6) * 3.1415927f * fanamp;
            this.fanl3.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 3.0f * pi6) * 3.1415927f * fanamp;
            this.fanl4.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 4.0f * pi6) * 3.1415927f * fanamp;
            this.fanl5.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 5.0f * pi6) * 3.1415927f * fanamp;
            this.fanl6.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 6.0f * pi6) * 3.1415927f * fanamp;
            this.fanl7.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 7.0f * pi6) * 3.1415927f * fanamp;
            this.fanr1.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 1.0f * pi6) * 3.1415927f * fanamp;
            this.fanr2.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 2.0f * pi6) * 3.1415927f * fanamp;
            this.fanr3.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 3.0f * pi6) * 3.1415927f * fanamp;
            this.fanr4.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 4.0f * pi6) * 3.1415927f * fanamp;
            this.fanr5.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 5.0f * pi6) * 3.1415927f * fanamp;
            this.fanr6.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 6.0f * pi6) * 3.1415927f * fanamp;
            this.fanr7.xRot = Mth.cos(f2 * fanspeed * this.wingspeed - 7.0f * pi6) * 3.1415927f * fanamp;
            this.fan.zRot = 0.0f;
            this.fanl1.zRot = 0.261f;
            this.fanl2.zRot = 0.523f;
            this.fanl3.zRot = 0.785f;
            this.fanl4.zRot = 1.047f;
            this.fanl5.zRot = 1.309f;
            this.fanl6.zRot = 1.571f;
            this.fanl7.zRot = 1.832f;
            this.fanr1.zRot = -0.261f;
            this.fanr2.zRot = -0.523f;
            this.fanr3.zRot = -0.785f;
            this.fanr4.zRot = -1.047f;
            this.fanr5.zRot = -1.309f;
            this.fanr6.zRot = -1.571f;
            this.fanr7.zRot = -1.832f;
        }
        this.neck.yRot = (float) Math.toRadians(f3) * 0.35f;
        this.head.yRot = (float) Math.toRadians(f3) * 0.75f;
        this.head.z = this.neck.z - (float) Math.cos(this.neck.yRot) * 3.0f;
        this.head.x = this.neck.x + (float) Math.sin(this.neck.yRot) * 3.0f;
        this.head1.yRot = this.head.yRot;
        this.head1.z = this.head.z;
        this.head1.x = this.head.x;
        this.head2.yRot = this.head.yRot;
        this.head2.z = this.head.z;
        this.head2.x = this.head.x;
        this.fang1.yRot = this.head.yRot;
        this.fang1.z = this.head.z;
        this.fang1.x = this.head.x;
        this.fang2.yRot = this.head.yRot;
        this.fang2.z = this.head.z;
        this.fang2.x = this.head.x;
        this.fang3.yRot = this.head.yRot;
        this.fang3.z = this.head.z;
        this.fang3.x = this.head.x;
        this.fang4.yRot = this.head.yRot;
        this.fang4.z = this.head.z;
        this.fang4.x = this.head.x;
        this.jaw1.yRot = this.head.yRot;
        this.jaw1.z = this.head.z - (float) Math.cos(this.head.yRot) * 8.0f;
        this.jaw1.x = this.head.x - (float) Math.sin(this.head.yRot) * 8.0f;
        this.jaw2.yRot = this.jaw1.yRot;
        this.jaw2.z = this.jaw1.z;
        this.jaw2.x = this.jaw1.x;
        final RenderInfo r = this.renderInfos.computeIfAbsent(e, k -> new RenderInfo());
        newangle = Mth.cos(f2 * 3.5f * this.wingspeed) * 3.1415927f * 0.5f;
        nextangle = Mth.cos((f2 + 0.2f) * 3.5f * this.wingspeed) * 3.1415927f * 0.5f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            if (e.getAttacking() == 0) {
                r.ri1 = e.level().random.nextInt(15);
                r.ri2 = e.level().random.nextInt(15);
                r.ri3 = e.level().random.nextInt(15);
            } else {
                r.ri1 = e.level().random.nextInt(4);
                r.ri2 = e.level().random.nextInt(2);
                r.ri3 = 1;
            }
        }
        if (r.ri2 == 1) {
            this.doTail(newangle);
        } else {
            newangle = Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.05f;
            this.doTail(newangle);
        }
        if (r.ri3 == 1) {
            newangle = Mth.cos(f2 * 3.5f * this.wingspeed) * 3.1415927f * 0.35f;
            this.doJaw(newangle);
        } else {
            newangle = Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.02f;
            this.doJaw(newangle);
        }
        newangle = Mth.cos(f2 * this.wingspeed * 3.5f) * 3.1415927f * 0.2f;
        if (r.ri1 == 1 || r.ri1 == 3) {
            this.doLeftClaw(newangle);
        } else {
            newangle = Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.03f;
            this.doLeftClaw(newangle);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            this.doRightClaw(-newangle);
        } else {
            newangle = Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.03f;
            this.doRightClaw(-newangle);
        }
        // e.setRenderInfo(r) (:498): the record is already the stored one.
    }

    /** The draw calls of {@code render()} (:499-553) at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    /** {@code doLeftLeg} (:566-571). */
    private void doLeftLeg(final float angle) {
        this.lFoot.xRot = angle;
        this.lShin.xRot = angle - 0.4f;
        this.lShin1.xRot = angle - 0.8f;
        this.lThigh.xRot = angle - 0.8f;
    }

    /** {@code doRightLeg} (:573-578). */
    private void doRightLeg(final float angle) {
        this.rFoot.xRot = angle;
        this.rShin.xRot = angle - 0.4f;
        this.rShin1.xRot = angle - 0.8f;
        this.rThigh.xRot = angle - 0.8f;
    }

    /** {@code doJaw} (:580-583). */
    private void doJaw(final float angle) {
        this.jaw1.xRot = Math.abs(angle);
        this.jaw2.xRot = this.jaw1.xRot;
    }

    /** {@code doTail} (:585-612): each segment 10 units behind the previous one along its yaw. */
    private void doTail(final float angle) {
        this.tail1.yRot = angle * 0.25f;
        this.spike1.yRot = this.tail1.yRot;
        this.tail2.yRot = angle * 0.5f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 10.0f;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 10.0f;
        this.spike2.yRot = this.tail2.yRot;
        this.spike2.z = this.tail2.z;
        this.spike2.x = this.tail2.x;
        this.tail3.yRot = angle * 0.8f;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 10.0f;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 10.0f;
        this.spike3.yRot = this.tail3.yRot;
        this.spike3.z = this.tail3.z;
        this.spike3.x = this.tail3.x;
        this.tail4.yRot = angle * 1.25f;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 10.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 10.0f;
        this.spike4.yRot = this.tail4.yRot + 0.52f;
        this.spike4.z = this.tail4.z;
        this.spike4.x = this.tail4.x;
        this.spike5.yRot = this.tail4.yRot - 0.52f;
        this.spike5.z = this.tail4.z;
        this.spike5.x = this.tail4.x;
        this.tail5.yRot = angle * 1.5f;
        this.tail5.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 10.0f;
        this.tail5.x = this.tail4.x + (float) Math.sin(this.tail4.yRot) * 10.0f;
    }

    /** {@code doLeftClaw} (:614-628). */
    private void doLeftClaw(final float angle) {
        this.arml1.yRot = -0.52f + Math.abs(angle * 2.0f);
        this.arml2.z = this.arml1.z - (float) Math.sin(this.arml1.yRot) * 9.0f;
        this.arml2.x = this.arml1.x + (float) Math.cos(this.arml1.yRot) * 9.0f;
        this.arml2.yRot = 0.855f + Math.abs(angle);
        this.clawl1.z = this.arml2.z - (float) Math.sin(this.arml2.yRot) * 14.0f;
        this.clawl1.x = this.arml2.x + (float) Math.cos(this.arml2.yRot) * 14.0f;
        this.clawl1.yRot = 2.7f + Math.abs(angle * 4.0f);
        this.clawl2.z = this.clawl1.z;
        this.clawl2.x = this.clawl1.x;
        this.clawl2.yRot = 2.27f + Math.abs(angle * 4.0f);
        this.clawl3.z = this.clawl1.z;
        this.clawl3.x = this.clawl1.x;
        this.clawl3.yRot = 2.7f + Math.abs(angle * 4.0f);
    }

    /** {@code doRightClaw} (:630-644). */
    private void doRightClaw(final float angle) {
        this.armr1.yRot = -2.61f - Math.abs(angle * 2.0f);
        this.armr2.z = this.armr1.z - (float) Math.sin(this.armr1.yRot) * 9.0f;
        this.armr2.x = this.armr1.x + (float) Math.cos(this.armr1.yRot) * 9.0f;
        this.armr2.yRot = 2.27f - Math.abs(angle);
        this.clawr1.z = this.armr2.z - (float) Math.sin(this.armr2.yRot) * 14.0f;
        this.clawr1.x = this.armr2.x + (float) Math.cos(this.armr2.yRot) * 14.0f;
        this.clawr1.yRot = 0.436f - Math.abs(angle * 4.0f);
        this.clawr2.z = this.clawr1.z;
        this.clawr2.x = this.clawr1.x;
        this.clawr2.yRot = 0.87f - Math.abs(angle * 4.0f);
        this.clawr3.z = this.clawr1.z;
        this.clawr3.x = this.clawr1.x;
        this.clawr3.yRot = 0.436f - Math.abs(angle * 4.0f);
    }
}
