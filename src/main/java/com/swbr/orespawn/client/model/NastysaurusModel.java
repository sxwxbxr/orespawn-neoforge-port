package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.NastysaurusGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.dino.Nastysaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelNastysaurus} (ModelNastysaurus.java:7-736): 58 boxes, 512x256. Geometry from the
 * generated {@link NastysaurusGeometry}; the animation is {@code render()} (:372-665).
 *
 * <p>What it does: the head and neck follow the head yaw at 35 %; the jaw (with its teeth) chews while attacking, and
 * when idle snaps open for one cycle with 1 in 20 each time the {@code f2 * 0.7} phase wraps - the wrap is detected with
 * {@code rf1} and the snap flag kept in {@code ri1} of the entity's {@link RenderInfo}, and the roll uses the client
 * world's random, frame-driven as in the original. The feet walk on an ellipse (claw pivots moved), the upper legs are
 * re-hung 17 px above the foot each frame, and the tail tip swings faster while attacking.
 */
public class NastysaurusModel extends EntityModel<Nastysaurus> {

    /** Register with {@code NastysaurusGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "nastysaurus"), "main");

    /** {@code wingspeed = f1} (:72); ClientProxyOreSpawn passes 0.65 (ClientProxyOreSpawn.java:138). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart lclaw1;
    private final ModelPart leftleg1;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart tail3;
    private final ModelPart lclaw2;
    private final ModelPart lclaw3;
    private final ModelPart lclaw4;
    private final ModelPart lclaw5;
    private final ModelPart lclaw6;
    private final ModelPart lclaw7;
    private final ModelPart neck3;
    private final ModelPart head3;
    private final ModelPart jaw1;
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
    private final ModelPart rclaw2;
    private final ModelPart rclaw4;
    private final ModelPart rclaw1;
    private final ModelPart rclaw5;
    private final ModelPart rclaw7;
    private final ModelPart rclaw3;
    private final ModelPart rclaw6;
    private final ModelPart neck2;
    private final ModelPart tail4;

    public NastysaurusModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[NastysaurusGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(NastysaurusGeometry.PARTS[i]);
        }
        this.lclaw1 = root.getChild(NastysaurusGeometry.LCLAW1);
        this.leftleg1 = root.getChild(NastysaurusGeometry.LEFTLEG1);
        this.leftleg2 = root.getChild(NastysaurusGeometry.LEFTLEG2);
        this.leftleg3 = root.getChild(NastysaurusGeometry.LEFTLEG3);
        this.tail3 = root.getChild(NastysaurusGeometry.TAIL3);
        this.lclaw2 = root.getChild(NastysaurusGeometry.LCLAW2);
        this.lclaw3 = root.getChild(NastysaurusGeometry.LCLAW3);
        this.lclaw4 = root.getChild(NastysaurusGeometry.LCLAW4);
        this.lclaw5 = root.getChild(NastysaurusGeometry.LCLAW5);
        this.lclaw6 = root.getChild(NastysaurusGeometry.LCLAW6);
        this.lclaw7 = root.getChild(NastysaurusGeometry.LCLAW7);
        this.neck3 = root.getChild(NastysaurusGeometry.NECK3);
        this.head3 = root.getChild(NastysaurusGeometry.HEAD3);
        this.jaw1 = root.getChild(NastysaurusGeometry.JAW1);
        this.tooth1 = root.getChild(NastysaurusGeometry.TOOTH1);
        this.tooth2 = root.getChild(NastysaurusGeometry.TOOTH2);
        this.tooth3 = root.getChild(NastysaurusGeometry.TOOTH3);
        this.tooth4 = root.getChild(NastysaurusGeometry.TOOTH4);
        this.tooth5 = root.getChild(NastysaurusGeometry.TOOTH5);
        this.jaw5 = root.getChild(NastysaurusGeometry.JAW5);
        this.head7 = root.getChild(NastysaurusGeometry.HEAD7);
        this.tooth6 = root.getChild(NastysaurusGeometry.TOOTH6);
        this.tooth7 = root.getChild(NastysaurusGeometry.TOOTH7);
        this.tooth8 = root.getChild(NastysaurusGeometry.TOOTH8);
        this.tooth9 = root.getChild(NastysaurusGeometry.TOOTH9);
        this.tooth10 = root.getChild(NastysaurusGeometry.TOOTH10);
        this.tooth11 = root.getChild(NastysaurusGeometry.TOOTH11);
        this.tooth12 = root.getChild(NastysaurusGeometry.TOOTH12);
        this.tooth13 = root.getChild(NastysaurusGeometry.TOOTH13);
        this.rightleg1 = root.getChild(NastysaurusGeometry.RIGHTLEG1);
        this.rightleg2 = root.getChild(NastysaurusGeometry.RIGHTLEG2);
        this.tooth14 = root.getChild(NastysaurusGeometry.TOOTH14);
        this.tooth15 = root.getChild(NastysaurusGeometry.TOOTH15);
        this.tooth16 = root.getChild(NastysaurusGeometry.TOOTH16);
        this.tooth17 = root.getChild(NastysaurusGeometry.TOOTH17);
        this.tooth18 = root.getChild(NastysaurusGeometry.TOOTH18);
        this.tooth19 = root.getChild(NastysaurusGeometry.TOOTH19);
        this.tooth20 = root.getChild(NastysaurusGeometry.TOOTH20);
        this.tooth21 = root.getChild(NastysaurusGeometry.TOOTH21);
        this.tooth22 = root.getChild(NastysaurusGeometry.TOOTH22);
        this.tooth23 = root.getChild(NastysaurusGeometry.TOOTH23);
        this.rightleg3 = root.getChild(NastysaurusGeometry.RIGHTLEG3);
        this.rclaw2 = root.getChild(NastysaurusGeometry.RCLAW2);
        this.rclaw4 = root.getChild(NastysaurusGeometry.RCLAW4);
        this.rclaw1 = root.getChild(NastysaurusGeometry.RCLAW1);
        this.rclaw5 = root.getChild(NastysaurusGeometry.RCLAW5);
        this.rclaw7 = root.getChild(NastysaurusGeometry.RCLAW7);
        this.rclaw3 = root.getChild(NastysaurusGeometry.RCLAW3);
        this.rclaw6 = root.getChild(NastysaurusGeometry.RCLAW6);
        this.neck2 = root.getChild(NastysaurusGeometry.NECK2);
        this.tail4 = root.getChild(NastysaurusGeometry.TAIL4);
    }

    /** The rotation and pivot writes of {@code render()} (:374-665), in their original order. */
    @Override
    public void setupAnim(final Nastysaurus e, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
                          final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float f3 = netHeadYaw;
        RenderInfo r = null;
        float newangle = 0.0f;
        final float pscale = 2.0f;
        float tailspeed = 0.0f;
        float tailamp = 0.0f;
        final float clawZ = 15.0f;
        final float clawY = 21.0f;
        final float clawZamp = 5.0f * pscale;
        final float clawYamp = 2.0f * pscale;
        final float pi4 = 0.7853982f;
        r = e.getRenderInfo();
        f3 %= 360.0f;
        f3 *= 0.35f;
        this.neck2.yRot = (float) Math.toRadians(f3) * 0.5f;
        this.head3.yRot = (float) Math.toRadians(f3);
        this.head7.yRot = this.head3.yRot;
        this.neck3.yRot = this.head3.yRot;
        this.jaw5.yRot = this.head3.yRot;
        this.jaw1.yRot = this.head3.yRot;
        this.tooth5.yRot = this.head3.yRot;
        this.tooth4.yRot = this.head3.yRot;
        this.tooth3.yRot = this.head3.yRot;
        this.tooth2.yRot = this.head3.yRot;
        this.tooth1.yRot = this.head3.yRot;
        this.tooth10.yRot = this.head3.yRot;
        this.tooth9.yRot = this.head3.yRot;
        this.tooth8.yRot = this.head3.yRot;
        this.tooth7.yRot = this.head3.yRot;
        this.tooth6.yRot = this.head3.yRot;
        this.tooth15.yRot = this.head3.yRot;
        this.tooth14.yRot = this.head3.yRot;
        this.tooth13.yRot = this.head3.yRot;
        this.tooth12.yRot = this.head3.yRot;
        this.tooth11.yRot = this.head3.yRot;
        this.tooth20.yRot = this.head3.yRot;
        this.tooth19.yRot = this.head3.yRot;
        this.tooth18.yRot = this.head3.yRot;
        this.tooth17.yRot = this.head3.yRot;
        this.tooth16.yRot = this.head3.yRot;
        this.tooth23.yRot = this.head3.yRot;
        this.tooth22.yRot = this.head3.yRot;
        this.tooth21.yRot = this.head3.yRot;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.85f * this.wingspeed) * 3.1415927f * 0.16f;
            newangle += 0.5f;
        } else {
            newangle = f2 * 0.7f * this.wingspeed % 6.2831855f;
            newangle = Math.abs(newangle);
            if (newangle < r.rf1) {
                r.ri1 = 0;
                if (e.level().random.nextInt(20) == 1) {
                    final RenderInfo renderInfo = r;
                    renderInfo.ri1 |= 0x1;
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
        float t1 = 0.0f;
        float t2 = 0.0f;
        if (f1 > 0.001) {
            newangle = Mth.cos(f2 * this.wingspeed / pscale);
            t1 = Mth.sin(f2 * this.wingspeed / pscale);
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
        this.lclaw7.z = this.lclaw1.z;
        this.lclaw6.z = this.lclaw1.z;
        this.lclaw5.z = this.lclaw1.z;
        this.lclaw4.z = this.lclaw1.z;
        this.lclaw3.z = this.lclaw1.z;
        this.lclaw2.z = this.lclaw1.z;
        this.lclaw7.y = this.lclaw1.y;
        this.lclaw6.y = this.lclaw1.y;
        this.lclaw5.y = this.lclaw1.y;
        this.lclaw4.y = this.lclaw1.y;
        this.lclaw3.y = this.lclaw1.y;
        this.lclaw2.y = this.lclaw1.y;
        this.leftleg3.z = this.lclaw1.z;
        this.leftleg3.y = this.lclaw1.y;
        this.leftleg3.xRot = -0.523f + newangle * 3.1415927f * 0.15f * f1;
        this.leftleg1.xRot = -0.576f + newangle * 3.1415927f * 0.06f * f1;
        this.leftleg2.xRot = 0.977f + newangle * 3.1415927f * 0.06f * f1;
        final float n3 = this.leftleg3.y - (float) Math.cos(this.leftleg3.xRot) * 17.0f;
        this.leftleg2.y = n3;
        this.leftleg1.y = n3;
        final float n4 = this.leftleg3.z - (float) Math.sin(this.leftleg3.xRot) * 17.0f;
        this.leftleg2.z = n4;
        this.leftleg1.z = n4;
        t1 = 0.0f;
        t2 = 0.0f;
        if (f1 > 0.001) {
            newangle = Mth.cos(f2 * this.wingspeed / pscale + pi4 * 4.0f);
            t1 = Mth.sin(f2 * this.wingspeed / pscale + pi4 * 4.0f);
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
        this.rclaw7.z = this.rclaw1.z;
        this.rclaw6.z = this.rclaw1.z;
        this.rclaw5.z = this.rclaw1.z;
        this.rclaw4.z = this.rclaw1.z;
        this.rclaw3.z = this.rclaw1.z;
        this.rclaw2.z = this.rclaw1.z;
        this.rclaw7.y = this.rclaw1.y;
        this.rclaw6.y = this.rclaw1.y;
        this.rclaw5.y = this.rclaw1.y;
        this.rclaw4.y = this.rclaw1.y;
        this.rclaw3.y = this.rclaw1.y;
        this.rclaw2.y = this.rclaw1.y;
        this.rightleg3.z = this.rclaw1.z;
        this.rightleg3.y = this.rclaw1.y;
        this.rightleg3.xRot = -0.523f + newangle * 3.1415927f * 0.15f * f1;
        this.rightleg1.xRot = -0.576f + newangle * 3.1415927f * 0.06f * f1;
        this.rightleg2.xRot = 0.977f + newangle * 3.1415927f * 0.06f * f1;
        final float n5 = this.rightleg3.y - (float) Math.cos(this.rightleg3.xRot) * 17.0f;
        this.rightleg2.y = n5;
        this.rightleg1.y = n5;
        final float n6 = this.rightleg3.z - (float) Math.sin(this.rightleg3.xRot) * 17.0f;
        this.rightleg2.z = n6;
        this.rightleg1.z = n6;
        this.lclaw1.xRot = 0.0f;
        this.lclaw7.xRot = 0.0f;
        this.lclaw6.xRot = 0.0f;
        this.lclaw5.xRot = 0.0f;
        this.lclaw4.xRot = 0.0f;
        this.lclaw3.xRot = 0.0f;
        this.lclaw2.xRot = 0.0f;
        this.rclaw1.xRot = 0.0f;
        this.rclaw7.xRot = 0.0f;
        this.rclaw6.xRot = 0.0f;
        this.rclaw5.xRot = 0.0f;
        this.rclaw4.xRot = 0.0f;
        this.rclaw3.xRot = 0.0f;
        this.rclaw2.xRot = 0.0f;
        if (e.getAttacking() != 0) {
            tailspeed = 0.76f;
            tailamp = 0.25f;
        } else {
            tailspeed = 0.26f;
            tailamp = 0.08f;
        }
        this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp / 2.0f;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 11.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 11.0f;
        this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp;
        e.setRenderInfo(r);
    }

    /** {@code render()} (:666-724): all 58 parts in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
