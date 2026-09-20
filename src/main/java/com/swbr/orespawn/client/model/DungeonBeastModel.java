package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.DungeonBeastGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.crystal.DungeonBeast;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelDungeonBeast} (ModelDungeonBeast.java:8-632): 64 boxes, 128x64 texture, geometry
 * from the generated {@link DungeonBeastGeometry}. The animation is {@code render()} (:403-559): legs swing about Z (the
 * parts are built lying on their side), back and tail spikes ripple about X, the seven tail segments swing about Y with
 * their pivots chained to the previous segment, and the six jaw parts snap in cycles.
 *
 * <p>The jaws use the entity's {@link RenderInfo}: at each upward zero crossing of {@code cos(f2 * 2 * wingspeed)} the
 * client world's random picks {@code ri1}/{@code ri2} from 0..14 (both 0 while attacking), and the jaws move only while
 * {@code ri1 == 0}. The note pad is frame-driven, as in the original.
 *
 * <p>The whole model is turned 90 degrees about Y before drawing ({@code glRotatef(90, 0, 1, 0)}, :560) - a
 * {@code PoseStack} rotation in {@link #renderToBuffer} (R8). {@code render()} never draws {@code ltoe1}, {@code ltoe3},
 * {@code rtoe1} and {@code rtoe3} (:561-620); they stay invisible here too.
 */
public class DungeonBeastModel extends EntityModel<DungeonBeast> {

    /** Register with {@code DungeonBeastGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "dungeon_beast"), "main");

    /** {@code ModelDungeonBeast(float f1)}: {@code wingspeed = f1} (:76-78); ClientProxyOreSpawn passes 0.62 (manifest). */
    private final float wingspeed;
    /** Every part, for {@code resetPose}. */
    private final ModelPart[] all;
    /** The parts {@code render()} draws, in its order (:561-620). */
    private final ModelPart[] drawn;
    private final ModelPart tail7;
    private final ModelPart ljaw3;
    private final ModelPart ljaw1;
    private final ModelPart ljaw2;
    private final ModelPart rjaw1;
    private final ModelPart rjaw2;
    private final ModelPart rjaw3;
    private final ModelPart t1s3;
    private final ModelPart rheel;
    private final ModelPart rleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg1;
    private final ModelPart lleg2;
    private final ModelPart rfoot;
    private final ModelPart ltoe2;
    private final ModelPart t2s3;
    private final ModelPart tail3;
    private final ModelPart t4s1;
    private final ModelPart t6s1;
    private final ModelPart tail6;
    private final ModelPart bodys1;
    private final ModelPart bodys2;
    private final ModelPart tail1;
    private final ModelPart bodys3;
    private final ModelPart t1s1;
    private final ModelPart t1s2;
    private final ModelPart tail2;
    private final ModelPart t3s2;
    private final ModelPart t2s2;
    private final ModelPart t2s1;
    private final ModelPart t3s1;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart t5s1;
    private final ModelPart lfoot;
    private final ModelPart rfoot2;
    private final ModelPart lfoot2;
    private final ModelPart lheel;
    private final ModelPart rtoe2;

    public DungeonBeastModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.all = new ModelPart[DungeonBeastGeometry.PARTS.length];
        for (int i = 0; i < this.all.length; ++i) {
            this.all[i] = root.getChild(DungeonBeastGeometry.PARTS[i]);
        }
        final String[] order = {
                DungeonBeastGeometry.TAIL7, DungeonBeastGeometry.HEAD3, DungeonBeastGeometry.NECK,
                DungeonBeastGeometry.LHORNBASE, DungeonBeastGeometry.LEYE, DungeonBeastGeometry.LJAW3,
                DungeonBeastGeometry.LJAW1, DungeonBeastGeometry.LJAW2, DungeonBeastGeometry.RJAW1,
                DungeonBeastGeometry.RJAW2, DungeonBeastGeometry.RJAW3, DungeonBeastGeometry.T1S3,
                DungeonBeastGeometry.RSHOULDER, DungeonBeastGeometry.RHEEL, DungeonBeastGeometry.LSHOULDER,
                DungeonBeastGeometry.RLEG1, DungeonBeastGeometry.RLEG2, DungeonBeastGeometry.LLEG1,
                DungeonBeastGeometry.LLEG2, DungeonBeastGeometry.RFOOT, DungeonBeastGeometry.LTOE2,
                DungeonBeastGeometry.HEAD1, DungeonBeastGeometry.HORN2, DungeonBeastGeometry.RHORNBASE,
                DungeonBeastGeometry.RH1, DungeonBeastGeometry.LH1, DungeonBeastGeometry.LH2,
                DungeonBeastGeometry.RH2, DungeonBeastGeometry.RH3, DungeonBeastGeometry.LH3,
                DungeonBeastGeometry.LH4, DungeonBeastGeometry.RH4, DungeonBeastGeometry.HORN1,
                DungeonBeastGeometry.T2S3, DungeonBeastGeometry.TAIL3, DungeonBeastGeometry.T4S1,
                DungeonBeastGeometry.T6S1, DungeonBeastGeometry.TAIL6, DungeonBeastGeometry.BODY,
                DungeonBeastGeometry.BODYS1, DungeonBeastGeometry.BODYS2, DungeonBeastGeometry.TAIL1,
                DungeonBeastGeometry.BODYS3, DungeonBeastGeometry.T1S1, DungeonBeastGeometry.T1S2,
                DungeonBeastGeometry.TAIL2, DungeonBeastGeometry.T3S2, DungeonBeastGeometry.T2S2,
                DungeonBeastGeometry.T2S1, DungeonBeastGeometry.T3S1, DungeonBeastGeometry.TAIL4,
                DungeonBeastGeometry.TAIL5, DungeonBeastGeometry.T5S1, DungeonBeastGeometry.HEAD2,
                DungeonBeastGeometry.REYE, DungeonBeastGeometry.LFOOT, DungeonBeastGeometry.RFOOT2,
                DungeonBeastGeometry.LFOOT2, DungeonBeastGeometry.LHEEL, DungeonBeastGeometry.RTOE2
        };
        this.drawn = new ModelPart[order.length];
        for (int i = 0; i < order.length; ++i) {
            this.drawn[i] = root.getChild(order[i]);
        }
        this.tail7 = root.getChild(DungeonBeastGeometry.TAIL7);
        this.ljaw3 = root.getChild(DungeonBeastGeometry.LJAW3);
        this.ljaw1 = root.getChild(DungeonBeastGeometry.LJAW1);
        this.ljaw2 = root.getChild(DungeonBeastGeometry.LJAW2);
        this.rjaw1 = root.getChild(DungeonBeastGeometry.RJAW1);
        this.rjaw2 = root.getChild(DungeonBeastGeometry.RJAW2);
        this.rjaw3 = root.getChild(DungeonBeastGeometry.RJAW3);
        this.t1s3 = root.getChild(DungeonBeastGeometry.T1S3);
        this.rheel = root.getChild(DungeonBeastGeometry.RHEEL);
        this.rleg1 = root.getChild(DungeonBeastGeometry.RLEG1);
        this.rleg2 = root.getChild(DungeonBeastGeometry.RLEG2);
        this.lleg1 = root.getChild(DungeonBeastGeometry.LLEG1);
        this.lleg2 = root.getChild(DungeonBeastGeometry.LLEG2);
        this.rfoot = root.getChild(DungeonBeastGeometry.RFOOT);
        this.ltoe2 = root.getChild(DungeonBeastGeometry.LTOE2);
        this.t2s3 = root.getChild(DungeonBeastGeometry.T2S3);
        this.tail3 = root.getChild(DungeonBeastGeometry.TAIL3);
        this.t4s1 = root.getChild(DungeonBeastGeometry.T4S1);
        this.t6s1 = root.getChild(DungeonBeastGeometry.T6S1);
        this.tail6 = root.getChild(DungeonBeastGeometry.TAIL6);
        this.bodys1 = root.getChild(DungeonBeastGeometry.BODYS1);
        this.bodys2 = root.getChild(DungeonBeastGeometry.BODYS2);
        this.tail1 = root.getChild(DungeonBeastGeometry.TAIL1);
        this.bodys3 = root.getChild(DungeonBeastGeometry.BODYS3);
        this.t1s1 = root.getChild(DungeonBeastGeometry.T1S1);
        this.t1s2 = root.getChild(DungeonBeastGeometry.T1S2);
        this.tail2 = root.getChild(DungeonBeastGeometry.TAIL2);
        this.t3s2 = root.getChild(DungeonBeastGeometry.T3S2);
        this.t2s2 = root.getChild(DungeonBeastGeometry.T2S2);
        this.t2s1 = root.getChild(DungeonBeastGeometry.T2S1);
        this.t3s1 = root.getChild(DungeonBeastGeometry.T3S1);
        this.tail4 = root.getChild(DungeonBeastGeometry.TAIL4);
        this.tail5 = root.getChild(DungeonBeastGeometry.TAIL5);
        this.t5s1 = root.getChild(DungeonBeastGeometry.T5S1);
        this.lfoot = root.getChild(DungeonBeastGeometry.LFOOT);
        this.rfoot2 = root.getChild(DungeonBeastGeometry.RFOOT2);
        this.lfoot2 = root.getChild(DungeonBeastGeometry.LFOOT2);
        this.lheel = root.getChild(DungeonBeastGeometry.LHEEL);
        this.rtoe2 = root.getChild(DungeonBeastGeometry.RTOE2);
    }

    /** The writes of {@code render()} (:408-559), in the original order. */
    @Override
    public void setupAnim(final DungeonBeast e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.all) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        float tailamp = 0.0f;
        final float pi4 = 0.39269876f;
        newangle = Mth.cos(f2 * 1.4f * this.wingspeed) * 3.1415927f * 0.22f * f1;
        this.rheel.zRot = newangle;
        this.rfoot2.zRot = newangle;
        this.rfoot.zRot = newangle;
        this.rleg2.zRot = newangle;
        this.rleg1.zRot = newangle;
        this.rtoe2.zRot = -0.785f + newangle;
        final float rotateAngleZ2 = -newangle;
        this.lheel.zRot = rotateAngleZ2;
        this.lfoot2.zRot = rotateAngleZ2;
        this.lfoot.zRot = rotateAngleZ2;
        this.lleg2.zRot = rotateAngleZ2;
        this.lleg1.zRot = rotateAngleZ2;
        this.ltoe2.zRot = -0.785f - newangle;
        this.bodys1.xRot = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.07f;
        this.bodys2.xRot = Mth.cos(f2 * 0.5f * this.wingspeed + pi4) * 3.1415927f * 0.07f;
        this.bodys3.xRot = Mth.cos(f2 * 0.5f * this.wingspeed + 2.0f * pi4) * 3.1415927f * 0.07f;
        this.t1s1.xRot = Mth.cos(f2 * 0.5f * this.wingspeed + 3.0f * pi4) * 3.1415927f * 0.07f;
        this.t1s2.xRot = Mth.cos(f2 * 0.5f * this.wingspeed + 4.0f * pi4) * 3.1415927f * 0.07f;
        this.t1s3.xRot = Mth.cos(f2 * 0.5f * this.wingspeed + 5.0f * pi4) * 3.1415927f * 0.07f;
        this.t2s1.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 6.0f * pi4) * 3.1415927f * 0.07f;
        this.t2s2.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 7.0f * pi4) * 3.1415927f * 0.07f;
        this.t2s3.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 8.0f * pi4) * 3.1415927f * 0.07f;
        this.t3s1.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 9.0f * pi4) * 3.1415927f * 0.07f;
        this.t3s2.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 10.0f * pi4) * 3.1415927f * 0.07f;
        this.t4s1.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 11.0f * pi4) * 3.1415927f * 0.07f;
        this.t5s1.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 12.0f * pi4) * 3.1415927f * 0.07f;
        this.t6s1.xRot = -Mth.cos(f2 * 0.5f * this.wingspeed + 13.0f * pi4) * 3.1415927f * 0.07f;
        if (e.getAttacking() == 0) {
            tailamp = f1;
        } else {
            tailamp = 1.25f;
        }
        newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.25f * tailamp;
        this.tail1.yRot = newangle * 0.25f;
        final float rotateAngleY = this.tail1.yRot;
        this.t1s3.yRot = rotateAngleY;
        this.t1s2.yRot = rotateAngleY;
        this.t1s1.yRot = rotateAngleY;
        this.tail2.yRot = newangle * 0.5f;
        this.tail2.x = this.tail1.x - (float) Math.cos(this.tail1.yRot) * 6.0f;
        this.tail2.z = this.tail1.z - (float) Math.sin(this.tail1.yRot) * 6.0f;
        final float rotateAngleY2 = this.tail2.yRot;
        this.t2s3.yRot = rotateAngleY2;
        this.t2s2.yRot = rotateAngleY2;
        this.t2s1.yRot = rotateAngleY2;
        this.t2s3.z = this.tail2.z;
        this.t2s2.z = this.tail2.z;
        this.t2s1.z = this.tail2.z;
        this.t2s3.x = this.tail2.x;
        this.t2s2.x = this.tail2.x;
        this.t2s1.x = this.tail2.x;
        this.tail3.yRot = newangle * 0.75f;
        this.tail3.x = this.tail2.x - (float) Math.cos(this.tail2.yRot) * 5.0f;
        this.tail3.z = this.tail2.z - (float) Math.sin(this.tail2.yRot) * 5.0f;
        final float rotateAngleY3 = this.tail3.yRot;
        this.t3s2.yRot = rotateAngleY3;
        this.t3s1.yRot = rotateAngleY3;
        this.t3s2.z = this.tail3.z;
        this.t3s1.z = this.tail3.z;
        this.t3s2.x = this.tail3.x;
        this.t3s1.x = this.tail3.x;
        this.tail4.yRot = newangle;
        this.tail4.x = this.tail3.x - (float) Math.cos(this.tail3.yRot) * 4.5f;
        this.tail4.z = this.tail3.z - (float) Math.sin(this.tail3.yRot) * 4.5f;
        this.t4s1.yRot = this.tail4.yRot;
        this.t4s1.z = this.tail4.z;
        this.t4s1.x = this.tail4.x;
        this.tail5.yRot = newangle * 1.25f;
        this.tail5.x = this.tail4.x - (float) Math.cos(this.tail4.yRot) * 4.0f;
        this.tail5.z = this.tail4.z - (float) Math.sin(this.tail4.yRot) * 4.0f;
        this.t5s1.yRot = this.tail5.yRot;
        this.t5s1.z = this.tail5.z;
        this.t5s1.x = this.tail5.x;
        this.tail6.yRot = newangle * 1.5f;
        this.tail6.x = this.tail5.x - (float) Math.cos(this.tail5.yRot) * 3.0f;
        this.tail6.z = this.tail5.z - (float) Math.sin(this.tail5.yRot) * 3.0f;
        this.t6s1.yRot = this.tail6.yRot;
        this.t6s1.z = this.tail6.z;
        this.t6s1.x = this.tail6.x;
        this.tail7.yRot = newangle * 1.75f;
        this.tail7.x = this.tail6.x - (float) Math.cos(this.tail6.yRot) * 3.0f;
        this.tail7.z = this.tail6.z - (float) Math.sin(this.tail6.yRot) * 3.0f;
        r = e.getRenderInfo();
        newangle = Mth.cos(f2 * 2.0f * this.wingspeed) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.1f) * 2.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            if (e.getAttacking() == 0) {
                r.ri1 = e.level().random.nextInt(15);
                r.ri2 = e.level().random.nextInt(15);
            } else {
                r.ri1 = 0;
                r.ri2 = 0;
            }
        }
        if (r.ri1 == 0) {
            this.ljaw1.yRot = -0.349f + newangle;
            this.ljaw2.yRot = 0.349f + newangle;
            this.ljaw3.yRot = 0.523f + newangle;
            this.rjaw1.yRot = 0.349f - newangle;
            this.rjaw2.yRot = -0.349f - newangle;
            this.rjaw3.yRot = -0.523f - newangle;
        } else {
            this.ljaw1.yRot = -0.349f;
            this.ljaw2.yRot = 0.349f;
            this.ljaw3.yRot = 0.523f;
            this.rjaw1.yRot = 0.349f;
            this.rjaw2.yRot = -0.349f;
            this.rjaw3.yRot = -0.523f;
        }
        e.setRenderInfo(r);
    }

    /** {@code render()} (:560-620): {@code glRotatef(90, 0, 1, 0)}, then the parts in the original order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        for (final ModelPart part : this.drawn) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        poseStack.popPose();
    }
}
