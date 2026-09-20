package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.PitchBlackGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelPitchBlack} (ModelPitchBlack.java:7-1366): 101 boxes, 512x256 texture, geometry
 * {@link PitchBlackGeometry}. {@code render()} (:624-1355) splits into {@link #setupAnim} (all angle and pivot writes:
 * wing beat, head yaw, jaw, claws and legs, the forked tail) and {@link #renderToBuffer} (the draw calls in their
 * order). No GL calls in the model; the size comes from the renderer's {@code scale()}.
 *
 * <p>Arguments of the 1.7.10 {@code render(entity, f, f1, f2, f3, f4, f5)}: {@code f1} limb swing amount, {@code f2}
 * ticks plus partial tick, {@code f3} head yaw minus body yaw - {@code limbSwingAmount}, {@code ageInTicks},
 * {@code netHeadYaw} here.
 *
 * <p>PORT: the {@code RenderInfo} the original kept on the entity ({@code e.getRenderInfo()} / {@code setRenderInfo},
 * :635, :1253) is client-only scratch space; it lives here per entity instance, weakly keyed (AlienModel precedent). A
 * new record starts zeroed, as {@code PitchBlack.entityInit} zeroed it. Its random draw (the idle jaw snap) uses the
 * entity's client level random, as {@code e.worldObj.rand} did.
 */
public class PitchBlackModel extends EntityModel<PitchBlack> {

    /** Register with {@code PitchBlackGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "nightmare"), "main");

    /** {@code ModelPitchBlack(float f1)}: {@code wingspeed = f1} (:113-114); ClientProxyOreSpawn.java:90 passes 0.65. */
    private final float wingspeed;
    /** {@code PitchBlack.renderdata} per entity (see the class Javadoc). */
    private final Map<PitchBlack, RenderInfo> renderInfos = new WeakHashMap<>();

    private final ModelPart lclaw1;
    private final ModelPart body;
    private final ModelPart leftleg1;
    private final ModelPart tail1;
    private final ModelPart leftleg2;
    private final ModelPart body2;
    private final ModelPart leftleg3;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart lclaw2;
    private final ModelPart lclaw3;
    private final ModelPart lclaw4;
    private final ModelPart lclaw5;
    private final ModelPart lclaw6;
    private final ModelPart lclaw7;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail6;
    private final ModelPart tail7;
    private final ModelPart tail8;
    private final ModelPart tail9;
    private final ModelPart tailpoint1;
    private final ModelPart tailpoint2;
    private final ModelPart llegspike;
    private final ModelPart tailspike1;
    private final ModelPart tailspike2;
    private final ModelPart tailspike3;
    private final ModelPart tailspike4;
    private final ModelPart tailspike5;
    private final ModelPart tailspike6;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head1;
    private final ModelPart leye;
    private final ModelPart reye;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart head4;
    private final ModelPart head5;
    private final ModelPart head6;
    private final ModelPart jaw1;
    private final ModelPart jaw2;
    private final ModelPart jaw3;
    private final ModelPart jaw4;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;
    private final ModelPart tooth4;
    private final ModelPart tooth5;
    private final ModelPart jaw5;
    private final ModelPart head7;
    private final ModelPart tooth6;
    private final ModelPart tooth7;
    private final ModelPart tooth8;
    private final ModelPart tooth9;
    private final ModelPart tooth10;
    private final ModelPart tooth11;
    private final ModelPart tooth12;
    private final ModelPart tooth13;
    private final ModelPart rightleg1;
    private final ModelPart rightleg2;
    private final ModelPart tooth14;
    private final ModelPart tooth15;
    private final ModelPart tooth16;
    private final ModelPart tooth17;
    private final ModelPart tooth18;
    private final ModelPart tooth19;
    private final ModelPart tooth20;
    private final ModelPart tooth21;
    private final ModelPart tooth22;
    private final ModelPart tooth23;
    private final ModelPart rightleg3;
    private final ModelPart llegspike2;
    private final ModelPart rclaw2;
    private final ModelPart rclaw4;
    private final ModelPart rclaw1;
    private final ModelPart rclaw5;
    private final ModelPart rclaw7;
    private final ModelPart rclaw3;
    private final ModelPart rclaw6;
    private final ModelPart wing1;
    private final ModelPart wing2;
    private final ModelPart wing3;
    private final ModelPart mem1;
    private final ModelPart mem2;
    private final ModelPart mem3;
    private final ModelPart wingclaw1;
    private final ModelPart wingclaw2;
    private final ModelPart wingclaw3;
    private final ModelPart lshoulder;
    private final ModelPart rshoulder;
    private final ModelPart rwing1;
    private final ModelPart rmem1;
    private final ModelPart rwing2;
    private final ModelPart rmem2;
    private final ModelPart rwing3;
    private final ModelPart rmem3;
    private final ModelPart rwingclaw1;
    private final ModelPart rwingclaw2;
    private final ModelPart rwingclaw3;
    /** The draw order of {@code render()} (:1254-1354). */
    private final ModelPart[] drawOrder;

    public PitchBlackModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.lclaw1 = root.getChild(PitchBlackGeometry.LCLAW1);
        this.body = root.getChild(PitchBlackGeometry.BODY);
        this.leftleg1 = root.getChild(PitchBlackGeometry.LEFTLEG1);
        this.tail1 = root.getChild(PitchBlackGeometry.TAIL1);
        this.leftleg2 = root.getChild(PitchBlackGeometry.LEFTLEG2);
        this.body2 = root.getChild(PitchBlackGeometry.BODY2);
        this.leftleg3 = root.getChild(PitchBlackGeometry.LEFTLEG3);
        this.tail2 = root.getChild(PitchBlackGeometry.TAIL2);
        this.tail3 = root.getChild(PitchBlackGeometry.TAIL3);
        this.lclaw2 = root.getChild(PitchBlackGeometry.LCLAW2);
        this.lclaw3 = root.getChild(PitchBlackGeometry.LCLAW3);
        this.lclaw4 = root.getChild(PitchBlackGeometry.LCLAW4);
        this.lclaw5 = root.getChild(PitchBlackGeometry.LCLAW5);
        this.lclaw6 = root.getChild(PitchBlackGeometry.LCLAW6);
        this.lclaw7 = root.getChild(PitchBlackGeometry.LCLAW7);
        this.tail4 = root.getChild(PitchBlackGeometry.TAIL4);
        this.tail5 = root.getChild(PitchBlackGeometry.TAIL5);
        this.tail6 = root.getChild(PitchBlackGeometry.TAIL6);
        this.tail7 = root.getChild(PitchBlackGeometry.TAIL7);
        this.tail8 = root.getChild(PitchBlackGeometry.TAIL8);
        this.tail9 = root.getChild(PitchBlackGeometry.TAIL9);
        this.tailpoint1 = root.getChild(PitchBlackGeometry.TAILPOINT1);
        this.tailpoint2 = root.getChild(PitchBlackGeometry.TAILPOINT2);
        this.llegspike = root.getChild(PitchBlackGeometry.LLEGSPIKE);
        this.tailspike1 = root.getChild(PitchBlackGeometry.TAILSPIKE1);
        this.tailspike2 = root.getChild(PitchBlackGeometry.TAILSPIKE2);
        this.tailspike3 = root.getChild(PitchBlackGeometry.TAILSPIKE3);
        this.tailspike4 = root.getChild(PitchBlackGeometry.TAILSPIKE4);
        this.tailspike5 = root.getChild(PitchBlackGeometry.TAILSPIKE5);
        this.tailspike6 = root.getChild(PitchBlackGeometry.TAILSPIKE6);
        this.neck1 = root.getChild(PitchBlackGeometry.NECK1);
        this.neck2 = root.getChild(PitchBlackGeometry.NECK2);
        this.neck3 = root.getChild(PitchBlackGeometry.NECK3);
        this.head1 = root.getChild(PitchBlackGeometry.HEAD1);
        this.leye = root.getChild(PitchBlackGeometry.LEYE);
        this.reye = root.getChild(PitchBlackGeometry.REYE);
        this.head2 = root.getChild(PitchBlackGeometry.HEAD2);
        this.head3 = root.getChild(PitchBlackGeometry.HEAD3);
        this.head4 = root.getChild(PitchBlackGeometry.HEAD4);
        this.head5 = root.getChild(PitchBlackGeometry.HEAD5);
        this.head6 = root.getChild(PitchBlackGeometry.HEAD6);
        this.jaw1 = root.getChild(PitchBlackGeometry.JAW1);
        this.jaw2 = root.getChild(PitchBlackGeometry.JAW2);
        this.jaw3 = root.getChild(PitchBlackGeometry.JAW3);
        this.jaw4 = root.getChild(PitchBlackGeometry.JAW4);
        this.tooth1 = root.getChild(PitchBlackGeometry.TOOTH1);
        this.tooth2 = root.getChild(PitchBlackGeometry.TOOTH2);
        this.tooth3 = root.getChild(PitchBlackGeometry.TOOTH3);
        this.tooth4 = root.getChild(PitchBlackGeometry.TOOTH4);
        this.tooth5 = root.getChild(PitchBlackGeometry.TOOTH5);
        this.jaw5 = root.getChild(PitchBlackGeometry.JAW5);
        this.head7 = root.getChild(PitchBlackGeometry.HEAD7);
        this.tooth6 = root.getChild(PitchBlackGeometry.TOOTH6);
        this.tooth7 = root.getChild(PitchBlackGeometry.TOOTH7);
        this.tooth8 = root.getChild(PitchBlackGeometry.TOOTH8);
        this.tooth9 = root.getChild(PitchBlackGeometry.TOOTH9);
        this.tooth10 = root.getChild(PitchBlackGeometry.TOOTH10);
        this.tooth11 = root.getChild(PitchBlackGeometry.TOOTH11);
        this.tooth12 = root.getChild(PitchBlackGeometry.TOOTH12);
        this.tooth13 = root.getChild(PitchBlackGeometry.TOOTH13);
        this.rightleg1 = root.getChild(PitchBlackGeometry.RIGHTLEG1);
        this.rightleg2 = root.getChild(PitchBlackGeometry.RIGHTLEG2);
        this.tooth14 = root.getChild(PitchBlackGeometry.TOOTH14);
        this.tooth15 = root.getChild(PitchBlackGeometry.TOOTH15);
        this.tooth16 = root.getChild(PitchBlackGeometry.TOOTH16);
        this.tooth17 = root.getChild(PitchBlackGeometry.TOOTH17);
        this.tooth18 = root.getChild(PitchBlackGeometry.TOOTH18);
        this.tooth19 = root.getChild(PitchBlackGeometry.TOOTH19);
        this.tooth20 = root.getChild(PitchBlackGeometry.TOOTH20);
        this.tooth21 = root.getChild(PitchBlackGeometry.TOOTH21);
        this.tooth22 = root.getChild(PitchBlackGeometry.TOOTH22);
        this.tooth23 = root.getChild(PitchBlackGeometry.TOOTH23);
        this.rightleg3 = root.getChild(PitchBlackGeometry.RIGHTLEG3);
        this.llegspike2 = root.getChild(PitchBlackGeometry.LLEGSPIKE2);
        this.rclaw2 = root.getChild(PitchBlackGeometry.RCLAW2);
        this.rclaw4 = root.getChild(PitchBlackGeometry.RCLAW4);
        this.rclaw1 = root.getChild(PitchBlackGeometry.RCLAW1);
        this.rclaw5 = root.getChild(PitchBlackGeometry.RCLAW5);
        this.rclaw7 = root.getChild(PitchBlackGeometry.RCLAW7);
        this.rclaw3 = root.getChild(PitchBlackGeometry.RCLAW3);
        this.rclaw6 = root.getChild(PitchBlackGeometry.RCLAW6);
        this.wing1 = root.getChild(PitchBlackGeometry.WING1);
        this.wing2 = root.getChild(PitchBlackGeometry.WING2);
        this.wing3 = root.getChild(PitchBlackGeometry.WING3);
        this.mem1 = root.getChild(PitchBlackGeometry.MEM1);
        this.mem2 = root.getChild(PitchBlackGeometry.MEM2);
        this.mem3 = root.getChild(PitchBlackGeometry.MEM3);
        this.wingclaw1 = root.getChild(PitchBlackGeometry.WINGCLAW1);
        this.wingclaw2 = root.getChild(PitchBlackGeometry.WINGCLAW2);
        this.wingclaw3 = root.getChild(PitchBlackGeometry.WINGCLAW3);
        this.lshoulder = root.getChild(PitchBlackGeometry.LSHOULDER);
        this.rshoulder = root.getChild(PitchBlackGeometry.RSHOULDER);
        this.rwing1 = root.getChild(PitchBlackGeometry.RWING1);
        this.rmem1 = root.getChild(PitchBlackGeometry.RMEM1);
        this.rwing2 = root.getChild(PitchBlackGeometry.RWING2);
        this.rmem2 = root.getChild(PitchBlackGeometry.RMEM2);
        this.rwing3 = root.getChild(PitchBlackGeometry.RWING3);
        this.rmem3 = root.getChild(PitchBlackGeometry.RMEM3);
        this.rwingclaw1 = root.getChild(PitchBlackGeometry.RWINGCLAW1);
        this.rwingclaw2 = root.getChild(PitchBlackGeometry.RWINGCLAW2);
        this.rwingclaw3 = root.getChild(PitchBlackGeometry.RWINGCLAW3);
        this.drawOrder = new ModelPart[] {
                this.lclaw1, this.body, this.leftleg1, this.tail1, this.leftleg2, this.body2, this.leftleg3,
                this.tail2, this.tail3, this.lclaw2, this.lclaw3, this.lclaw4, this.lclaw5, this.lclaw6, this.lclaw7,
                this.tail4, this.tail5, this.tail6, this.tail7, this.tail8, this.tail9, this.tailpoint1,
                this.tailpoint2, this.llegspike, this.tailspike1, this.tailspike2, this.tailspike3, this.tailspike4,
                this.tailspike5, this.tailspike6, this.neck1, this.neck2, this.neck3, this.head1, this.leye,
                this.reye, this.head2, this.head3, this.head4, this.head5, this.head6, this.jaw1, this.jaw2,
                this.jaw3, this.jaw4, this.tooth1, this.tooth2, this.tooth3, this.tooth4, this.tooth5, this.jaw5,
                this.head7, this.tooth6, this.tooth7, this.tooth8, this.tooth9, this.tooth10, this.tooth11,
                this.tooth12, this.tooth13, this.rightleg1, this.rightleg2, this.tooth14, this.tooth15, this.tooth16,
                this.tooth17, this.tooth18, this.tooth19, this.tooth20, this.tooth21, this.tooth22, this.tooth23,
                this.rightleg3, this.llegspike2, this.rclaw2, this.rclaw4, this.rclaw1, this.rclaw5, this.rclaw7,
                this.rclaw3, this.rclaw6, this.wing1, this.wing2, this.wing3, this.mem1, this.mem2, this.mem3,
                this.wingclaw1, this.wingclaw2, this.wingclaw3, this.lshoulder, this.rshoulder, this.rwing1,
                this.rmem1, this.rwing2, this.rmem2, this.rwing3, this.rmem3, this.rwingclaw1, this.rwingclaw2,
                this.rwingclaw3 };
    }

    /**
     * The writes of {@code render()} (:625-1253), in their order. Every angle and pivot the original touched is written
     * again each frame after {@code resetPose()} (R8); the long chains of aliased assignments of the decompiled source
     * are collapsed to one line per part.
     */
    @Override
    public void setupAnim(final PitchBlack e, final float f, final float f1, final float f2, float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float tailspeed = 0.76f;
        float tailamp = 0.25f;
        final float pi4 = 0.7853982f;
        final float pscale = e.getPitchBlackScale();
        final RenderInfo r = this.renderInfos.computeIfAbsent(e, k -> new RenderInfo());
        if (e.getActivity() != 0) {
            newangle = Mth.cos(f2 * 0.45f * this.wingspeed / pscale) * 3.1415927f * 0.24f;
        } else {
            newangle = -pi4 + Mth.cos(f2 * 0.05f * this.wingspeed / pscale) * 3.1415927f * 0.02f;
        }
        this.wing1.zRot = newangle;
        this.mem1.zRot = newangle;
        this.wing2.zRot = newangle * 5.0f / 3.0f;
        this.wing2.y = this.wing1.y + (float) Math.sin(this.wing1.zRot) * 21.0f;
        this.wing2.x = this.wing1.x + (float) Math.cos(this.wing1.zRot) * 21.0f;
        this.mem2.zRot = newangle * 5.0f / 3.0f;
        this.mem2.y = this.wing2.y;
        this.mem2.x = this.wing2.x;
        this.wing3.zRot = newangle * 2.0f;
        this.wing3.y = this.wing2.y + (float) Math.sin(this.wing2.zRot) * 43.0f;
        this.wing3.x = this.wing2.x + (float) Math.cos(this.wing2.zRot) * 43.0f;
        this.mem3.zRot = newangle * 2.0f;
        this.mem3.y = this.wing3.y;
        this.mem3.x = this.wing3.x;
        final float rotateAngleZ = newangle * 3.0f / 2.0f;
        this.wingclaw3.zRot = rotateAngleZ;
        this.wingclaw2.zRot = rotateAngleZ;
        this.wingclaw1.zRot = rotateAngleZ;
        this.wingclaw3.y = this.wing3.y;
        this.wingclaw2.y = this.wing3.y;
        this.wingclaw1.y = this.wing3.y;
        this.wingclaw3.x = this.wing3.x;
        this.wingclaw2.x = this.wing3.x;
        this.wingclaw1.x = this.wing3.x;
        this.rwing1.zRot = -newangle;
        this.rmem1.zRot = -newangle;
        this.rwing2.zRot = -newangle * 5.0f / 3.0f;
        this.rwing2.y = this.rwing1.y - (float) Math.sin(this.rwing1.zRot) * 21.0f;
        this.rwing2.x = this.rwing1.x - (float) Math.cos(this.rwing1.zRot) * 21.0f;
        this.rmem2.zRot = -newangle * 5.0f / 3.0f;
        this.rmem2.y = this.rwing2.y;
        this.rmem2.x = this.rwing2.x;
        this.rwing3.zRot = -newangle * 2.0f;
        this.rwing3.y = this.rwing2.y - (float) Math.sin(this.rwing2.zRot) * 43.0f;
        this.rwing3.x = this.rwing2.x - (float) Math.cos(this.rwing2.zRot) * 43.0f;
        this.rmem3.zRot = -newangle * 2.0f;
        this.rmem3.y = this.rwing3.y;
        this.rmem3.x = this.rwing3.x;
        final float rotateAngleZ2 = -newangle * 3.0f / 2.0f;
        this.rwingclaw3.zRot = rotateAngleZ2;
        this.rwingclaw2.zRot = rotateAngleZ2;
        this.rwingclaw1.zRot = rotateAngleZ2;
        this.rwingclaw3.y = this.rwing3.y;
        this.rwingclaw2.y = this.rwing3.y;
        this.rwingclaw1.y = this.rwing3.y;
        this.rwingclaw3.x = this.rwing3.x;
        this.rwingclaw2.x = this.rwing3.x;
        this.rwingclaw1.x = this.rwing3.x;
        f3 %= 360.0f;
        if (e.getActivity() != 0) {
            f3 *= 0.2f;
        } else {
            f3 *= 0.55f;
        }
        this.neck3.yRot = (float) Math.toRadians(f3) * 0.5f;
        final float n = (float) Math.toRadians(f3);
        this.head4.yRot = n;
        this.head3.yRot = n;
        this.head2.yRot = n;
        this.head1.yRot = n;
        final float headYaw = this.head1.yRot;
        this.head7.yRot = headYaw;
        this.head6.yRot = headYaw;
        this.head5.yRot = headYaw;
        this.jaw5.yRot = headYaw;
        this.jaw4.yRot = headYaw;
        this.jaw3.yRot = headYaw;
        this.jaw2.yRot = headYaw;
        this.jaw1.yRot = headYaw;
        this.tooth5.yRot = headYaw;
        this.tooth4.yRot = headYaw;
        this.tooth3.yRot = headYaw;
        this.tooth2.yRot = headYaw;
        this.tooth1.yRot = headYaw;
        this.tooth10.yRot = headYaw;
        this.tooth9.yRot = headYaw;
        this.tooth8.yRot = headYaw;
        this.tooth7.yRot = headYaw;
        this.tooth6.yRot = headYaw;
        this.tooth15.yRot = headYaw;
        this.tooth14.yRot = headYaw;
        this.tooth13.yRot = headYaw;
        this.tooth12.yRot = headYaw;
        this.tooth11.yRot = headYaw;
        this.tooth20.yRot = headYaw;
        this.tooth19.yRot = headYaw;
        this.tooth18.yRot = headYaw;
        this.tooth17.yRot = headYaw;
        this.tooth16.yRot = headYaw;
        this.tooth23.yRot = headYaw;
        this.tooth22.yRot = headYaw;
        this.tooth21.yRot = headYaw;
        this.leye.yRot = headYaw;
        this.reye.yRot = headYaw;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.85f * this.wingspeed) * 3.1415927f * 0.16f;
            newangle += 0.5f;
        } else {
            newangle = f2 * 0.7f * this.wingspeed % 6.2831855f;
            newangle = Math.abs(newangle);
            if (newangle < r.rf1) {
                r.ri1 = 0;
                if (e.level().random.nextInt(20) == 1) {
                    r.ri1 |= 0x1;
                }
            }
            r.rf1 = newangle;
            if (r.ri1 != 0) {
                newangle = Mth.sin(f2 * 0.85f * this.wingspeed) * 3.1415927f * 0.16f;
                newangle += 0.5f;
            } else {
                newangle = pi4 / 4.0f;
            }
        }
        this.jaw5.xRot = newangle;
        this.jaw4.xRot = newangle;
        this.jaw3.xRot = newangle;
        this.jaw2.xRot = newangle;
        this.jaw1.xRot = newangle;
        this.tooth15.xRot = newangle;
        this.tooth14.xRot = newangle;
        this.tooth20.xRot = newangle;
        this.tooth19.xRot = newangle;
        this.tooth18.xRot = newangle;
        this.tooth17.xRot = newangle;
        this.tooth16.xRot = newangle;
        this.tooth23.xRot = newangle;
        this.tooth22.xRot = newangle;
        this.tooth21.xRot = newangle;
        float clawZ = 7.0f;
        float clawY = 21.0f;
        final float clawZamp = 12.0f * pscale;
        final float clawYamp = 6.0f * pscale;
        if (e.getActivity() == 0) {
            float t1 = 0.0f;
            float t2 = 0.0f;
            if (f1 > 0.001) {
                newangle = Mth.cos(f2 * 0.75f * this.wingspeed / pscale);
                t1 = Mth.sin(f2 * 0.75f * this.wingspeed / pscale);
            } else {
                newangle = 0.0f;
                t1 = 0.0f;
                t2 = 0.0f;
            }
            if (t1 > 0.0f) {
                t2 = t1 * clawYamp * f1;
                this.lclaw1.y = clawY - t2;
            } else {
                this.lclaw1.y = clawY;
            }
            this.lclaw1.z = clawZ + clawZamp * newangle * f1;
            this.copyClawPivot(this.lclaw1, this.lclaw2, this.lclaw3, this.lclaw4, this.lclaw5, this.lclaw6, this.lclaw7);
            this.leftleg3.z = this.lclaw1.z;
            this.llegspike.z = this.lclaw1.z;
            this.leftleg3.y = this.lclaw1.y;
            this.llegspike.y = this.lclaw1.y;
            this.leftleg3.xRot = -0.61f + newangle * 3.1415927f * 0.18f * f1;
            this.llegspike.xRot = -0.785f + newangle * 3.1415927f * 0.18f * f1;
            this.leftleg1.xRot = -0.576f + newangle * 3.1415927f * 0.18f * f1;
            this.leftleg2.xRot = 0.977f + newangle * 3.1415927f * 0.18f * f1;
            final float n3 = this.leftleg3.y - (float) Math.cos(this.leftleg3.xRot) * 17.0f + t2 / 2.0f;
            this.leftleg2.y = n3;
            this.leftleg1.y = n3;
            final float n4 = this.leftleg3.z - (float) Math.sin(this.leftleg3.xRot) * 17.0f;
            this.leftleg2.z = n4;
            this.leftleg1.z = n4;
            t1 = 0.0f;
            t2 = 0.0f;
            if (f1 > 0.001) {
                newangle = Mth.cos(f2 * 0.75f * this.wingspeed / pscale + pi4 * 4.0f);
                t1 = Mth.sin(f2 * 0.75f * this.wingspeed / pscale + pi4 * 4.0f);
            } else {
                newangle = 0.0f;
                t1 = 0.0f;
                t2 = 0.0f;
            }
            if (t1 > 0.0f) {
                t2 = t1 * clawYamp * f1;
                this.rclaw1.y = clawY - t2;
            } else {
                this.rclaw1.y = clawY;
            }
            this.rclaw1.z = clawZ + clawZamp * newangle * f1;
            this.copyClawPivot(this.rclaw1, this.rclaw2, this.rclaw3, this.rclaw4, this.rclaw5, this.rclaw6, this.rclaw7);
            // (:979-988) the right leg spike field is llegspike2 in the original.
            this.rightleg3.z = this.rclaw1.z;
            this.llegspike2.z = this.rclaw1.z;
            this.rightleg3.y = this.rclaw1.y;
            this.llegspike2.y = this.rclaw1.y;
            this.rightleg3.xRot = -0.61f + newangle * 3.1415927f * 0.18f * f1;
            this.llegspike2.xRot = -0.785f + newangle * 3.1415927f * 0.18f * f1;
            this.rightleg1.xRot = -0.576f + newangle * 3.1415927f * 0.18f * f1;
            this.rightleg2.xRot = 0.977f + newangle * 3.1415927f * 0.18f * f1;
            final float n5 = this.rightleg3.y - (float) Math.cos(this.rightleg3.xRot) * 17.0f + t2 / 2.0f;
            this.rightleg2.y = n5;
            this.rightleg1.y = n5;
            final float n6 = this.rightleg3.z - (float) Math.sin(this.rightleg3.xRot) * 17.0f;
            this.rightleg2.z = n6;
            this.rightleg1.z = n6;
            this.setClawPitch(0.0f, this.lclaw1, this.lclaw2, this.lclaw3, this.lclaw4, this.lclaw5, this.lclaw6, this.lclaw7);
            this.setClawPitch(0.0f, this.rclaw1, this.rclaw2, this.rclaw3, this.rclaw4, this.rclaw5, this.rclaw6, this.rclaw7);
        } else {
            clawZ = 7.0f;
            clawY = 9.0f;
            if (e.getAttacking() != 0) {
                newangle = Mth.cos(f2 * 0.85f * this.wingspeed / pscale) * 0.2f;
            } else {
                newangle = 0.0f;
            }
            this.lclaw1.z = clawZ;
            this.lclaw1.y = clawY + newangle * 30.0f;
            this.lclaw1.xRot = -0.7f + newangle;
            this.copyClawPivot(this.lclaw1, this.lclaw2, this.lclaw3, this.lclaw4, this.lclaw5, this.lclaw6, this.lclaw7);
            this.setClawPitch(this.lclaw1.xRot, this.lclaw2, this.lclaw3, this.lclaw4, this.lclaw5, this.lclaw6, this.lclaw7);
            this.leftleg3.z = this.lclaw1.z;
            this.llegspike.z = this.lclaw1.z;
            this.leftleg3.y = this.lclaw1.y;
            this.llegspike.y = this.lclaw1.y;
            this.leftleg3.xRot = -0.61f + this.lclaw1.xRot;
            this.llegspike.xRot = -0.785f + this.lclaw1.xRot;
            this.leftleg1.xRot = -0.576f - this.lclaw1.xRot / 4.0f;
            this.leftleg2.xRot = 0.977f - this.lclaw1.xRot / 4.0f;
            final float n7 = this.leftleg3.y - (float) Math.cos(this.leftleg3.xRot) * 17.0f;
            this.leftleg2.y = n7;
            this.leftleg1.y = n7;
            final float n8 = this.leftleg3.z - (float) Math.sin(this.leftleg3.xRot) * 17.0f;
            this.leftleg2.z = n8;
            this.leftleg1.z = n8;
            this.rclaw1.z = clawZ;
            this.rclaw1.y = clawY - newangle * 30.0f;
            this.rclaw1.xRot = -0.7f - newangle;
            this.copyClawPivot(this.rclaw1, this.rclaw2, this.rclaw3, this.rclaw4, this.rclaw5, this.rclaw6, this.rclaw7);
            this.setClawPitch(this.rclaw1.xRot, this.rclaw2, this.rclaw3, this.rclaw4, this.rclaw5, this.rclaw6, this.rclaw7);
            this.rightleg3.z = this.rclaw1.z;
            this.llegspike2.z = this.rclaw1.z;
            this.rightleg3.y = this.rclaw1.y;
            this.llegspike2.y = this.rclaw1.y;
            this.rightleg3.xRot = -0.61f + this.rclaw1.xRot;
            this.llegspike2.xRot = -0.785f + this.rclaw1.xRot;
            this.rightleg1.xRot = -0.576f - this.rclaw1.xRot / 4.0f;
            this.rightleg2.xRot = 0.977f - this.rclaw1.xRot / 4.0f;
            final float n9 = this.rightleg3.y - (float) Math.cos(this.rightleg3.xRot) * 17.0f;
            this.rightleg2.y = n9;
            this.rightleg1.y = n9;
            final float n10 = this.rightleg3.z - (float) Math.sin(this.rightleg3.xRot) * 17.0f;
            this.rightleg2.z = n10;
            this.rightleg1.z = n10;
        }
        if (e.getAttacking() != 0) {
            tailspeed = 0.76f / pscale;
            tailamp = 0.25f;
        } else {
            tailspeed = 0.26f / pscale;
            tailamp = 0.08f;
        }
        this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp / 2.0f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 11.0f;
        this.tail2.x = this.tail1.x - 1.0f + (float) Math.sin(this.tail1.yRot) * 11.0f;
        this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * 3.1415927f * tailamp;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 9.0f;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 9.0f;
        this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0f * pi4) * 3.1415927f * tailamp;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 9.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 9.0f;
        this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        this.tail5.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 9.0f;
        this.tail5.x = this.tail4.x + (float) Math.sin(this.tail4.yRot) * 9.0f;
        newangle = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        newangle /= 2.0f;
        this.tail5.yRot = this.tail4.yRot + newangle;
        this.tail6.z = this.tail5.z + (float) Math.cos(this.tail5.yRot) * 9.0f;
        this.tail6.x = this.tail5.x + (float) Math.sin(this.tail5.yRot) * 9.0f;
        this.tail6.yRot = 0.174f + this.tail5.yRot + newangle;
        this.tailspike3.z = this.tail6.z;
        this.tailspike2.z = this.tail6.z;
        this.tailspike3.x = this.tail6.x;
        this.tailspike2.x = this.tail6.x;
        this.tailspike3.yRot = this.tail6.yRot;
        this.tailspike2.yRot = this.tail6.yRot;
        this.tail9.z = this.tail6.z + (float) Math.cos(this.tail6.yRot) * 9.0f;
        this.tail9.x = this.tail6.x + (float) Math.sin(this.tail6.yRot) * 9.0f;
        this.tail9.yRot = this.tail6.yRot + newangle;
        this.tailspike1.z = this.tail9.z;
        this.tailspike1.x = this.tail9.x;
        this.tailspike1.yRot = this.tail9.yRot;
        this.tailpoint1.z = this.tail9.z + (float) Math.cos(this.tail9.yRot) * 9.0f;
        this.tailpoint1.x = this.tail9.x + (float) Math.sin(this.tail9.yRot) * 9.0f;
        this.tailpoint1.yRot = this.tail9.yRot + newangle;
        this.tail7.z = this.tail5.z + (float) Math.cos(this.tail5.yRot) * 9.0f;
        this.tail7.x = this.tail5.x + (float) Math.sin(this.tail5.yRot) * 9.0f;
        this.tail7.yRot = -0.174f + this.tail5.yRot + newangle;
        this.tailspike6.z = this.tail7.z;
        this.tailspike5.z = this.tail7.z;
        this.tailspike6.x = this.tail7.x;
        this.tailspike5.x = this.tail7.x;
        this.tailspike6.yRot = this.tail7.yRot;
        this.tailspike5.yRot = this.tail7.yRot;
        this.tail8.z = this.tail7.z + (float) Math.cos(this.tail7.yRot) * 9.0f;
        this.tail8.x = this.tail7.x + (float) Math.sin(this.tail7.yRot) * 9.0f;
        this.tail8.yRot = this.tail7.yRot + newangle;
        this.tailspike4.z = this.tail8.z;
        this.tailspike4.x = this.tail8.x;
        this.tailspike4.yRot = this.tail8.yRot;
        this.tailpoint2.z = this.tail8.z + (float) Math.cos(this.tail8.yRot) * 9.0f;
        this.tailpoint2.x = this.tail8.x + (float) Math.sin(this.tail8.yRot) * 9.0f;
        this.tailpoint2.yRot = this.tail8.yRot + newangle;
        // e.setRenderInfo(r) (:1253): the record is already the stored one.
    }

    /** Copies the pivot of the first claw part to the other six: z first, then y (:884-909, :953-978, :1046-1071, :1112-1137). */
    private void copyClawPivot(final ModelPart first, final ModelPart... rest) {
        for (final ModelPart part : rest) {
            part.z = first.z;
        }
        for (final ModelPart part : rest) {
            part.y = first.y;
        }
    }

    /** One pitch for several claw parts (:1003-1032, :1072-1084, :1138-1150). */
    private void setClawPitch(final float angle, final ModelPart... parts) {
        for (final ModelPart part : parts) {
            part.xRot = angle;
        }
    }

    /** The draw calls of {@code render()} (:1254-1354) at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
