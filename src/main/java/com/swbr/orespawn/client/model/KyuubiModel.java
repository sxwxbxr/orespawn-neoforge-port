package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.KyuubiGeometry;
import com.swbr.orespawn.entity.monster.Kyuubi;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelKyuubi} (ModelKyuubi.java:8-449): 42 boxes, 512x256 texture, geometry
 * {@link KyuubiGeometry}. {@code render()} (:271-438): walking legs and arms, two five-link horn chains and the
 * nine-link tail fan waving independently of any attack.
 *
 * <p>GL (:387-437), around <em>every</em> draw call: {@code glPushMatrix}, {@code glEnable(GL_NORMALIZE)},
 * {@code glEnable(GL_BLEND)} with {@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)}, {@code glRotatef(180, 0, 1, 0)}
 * between two zero translations, then {@code glDisable(GL_BLEND)} and {@code glPopMatrix}. There is no
 * {@code glColor4f}: the translucency is the texture alpha (154/255 on the tails, horns and the eleven "Fire" shells,
 * 255 on the body, design-entities-04.md). That is one pass with {@link RenderType#entityTranslucent} (no culling, as
 * 1.7.10's living renderer) over the whole model in the original draw order, and the half turn as a {@link PoseStack}
 * rotation inside a push/pop in {@link #renderToBuffer} (R8). The head's {@code + pi} (:309) compensates that turn and
 * stays. {@code GL_NORMALIZE} has no counterpart; 1.21.1 transforms normals with the pose's normal matrix.
 */
public class KyuubiModel extends EntityModel<Kyuubi> {

    /** Register with {@code KyuubiGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "kyuubi"), "main");

    /** {@code ModelKyuubi(float f1)}: {@code wingspeed = f1} (:55-56); ClientProxyOreSpawn passes 0.5 (manifest). */
    private final float wingspeed;

    private final ModelPart rtHorn5;
    private final ModelPart lfHorn5;
    private final ModelPart tail9;
    private final ModelPart tail8;
    private final ModelPart tail7;
    private final ModelPart tail6;
    private final ModelPart tail5;
    private final ModelPart tail2;
    private final ModelPart tail1;
    private final ModelPart tail0;
    private final ModelPart lfLegLower;
    private final ModelPart rtLegLower;
    private final ModelPart head;
    private final ModelPart chest;
    private final ModelPart lfArmUpper;
    private final ModelPart rtArmLower;
    private final ModelPart lfLegUpper;
    private final ModelPart rtLegUpper;
    private final ModelPart body;
    private final ModelPart rtArmUpper;
    private final ModelPart lfArmLower;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart lfHorn2;
    private final ModelPart rtHorn1;
    private final ModelPart rtHorn2;
    private final ModelPart lfHorn1;
    private final ModelPart lfHorn3;
    private final ModelPart rtHorn3;
    private final ModelPart lfHorn4;
    private final ModelPart rtHorn4;
    private final ModelPart headFire;
    private final ModelPart lfArmUpperFire;
    private final ModelPart chestFire;
    private final ModelPart bodyFire;
    private final ModelPart lfArmLowerFire;
    private final ModelPart rtArmUpperFire;
    private final ModelPart rtArmLowerFire;
    private final ModelPart lfLegUppperFire;
    private final ModelPart lfLegLowerFire;
    private final ModelPart rtLegUpperFire;
    private final ModelPart rtLegLowerFire;
    /** The draw order of {@code render()} (:394-435): body first, tails and horns, the fire shells last. */
    private final ModelPart[] drawOrder;

    public KyuubiModel(final ModelPart root, final float f1) {
        super(RenderType::entityTranslucent);
        this.wingspeed = f1;
        this.rtHorn5 = root.getChild(KyuubiGeometry.RT_HORN5);
        this.lfHorn5 = root.getChild(KyuubiGeometry.LF_HORN5);
        this.tail9 = root.getChild(KyuubiGeometry.TAIL9);
        this.tail8 = root.getChild(KyuubiGeometry.TAIL8);
        this.tail7 = root.getChild(KyuubiGeometry.TAIL7);
        this.tail6 = root.getChild(KyuubiGeometry.TAIL6);
        this.tail5 = root.getChild(KyuubiGeometry.TAIL5);
        this.tail2 = root.getChild(KyuubiGeometry.TAIL2);
        this.tail1 = root.getChild(KyuubiGeometry.TAIL1);
        this.tail0 = root.getChild(KyuubiGeometry.TAIL0);
        this.lfLegLower = root.getChild(KyuubiGeometry.LF_LEG_LOWER);
        this.rtLegLower = root.getChild(KyuubiGeometry.RT_LEG_LOWER);
        this.head = root.getChild(KyuubiGeometry.HEAD);
        this.chest = root.getChild(KyuubiGeometry.CHEST);
        this.lfArmUpper = root.getChild(KyuubiGeometry.LF_ARM_UPPER);
        this.rtArmLower = root.getChild(KyuubiGeometry.RT_ARM_LOWER);
        this.lfLegUpper = root.getChild(KyuubiGeometry.LF_LEG_UPPER);
        this.rtLegUpper = root.getChild(KyuubiGeometry.RT_LEG_UPPER);
        this.body = root.getChild(KyuubiGeometry.BODY);
        this.rtArmUpper = root.getChild(KyuubiGeometry.RT_ARM_UPPER);
        this.lfArmLower = root.getChild(KyuubiGeometry.LF_ARM_LOWER);
        this.tail3 = root.getChild(KyuubiGeometry.TAIL3);
        this.tail4 = root.getChild(KyuubiGeometry.TAIL4);
        this.lfHorn2 = root.getChild(KyuubiGeometry.LF_HORN2);
        this.rtHorn1 = root.getChild(KyuubiGeometry.RT_HORN1);
        this.rtHorn2 = root.getChild(KyuubiGeometry.RT_HORN2);
        this.lfHorn1 = root.getChild(KyuubiGeometry.LF_HORN1);
        this.lfHorn3 = root.getChild(KyuubiGeometry.LF_HORN3);
        this.rtHorn3 = root.getChild(KyuubiGeometry.RT_HORN3);
        this.lfHorn4 = root.getChild(KyuubiGeometry.LF_HORN4);
        this.rtHorn4 = root.getChild(KyuubiGeometry.RT_HORN4);
        this.headFire = root.getChild(KyuubiGeometry.HEAD_FIRE);
        this.lfArmUpperFire = root.getChild(KyuubiGeometry.LF_ARM_UPPER_FIRE);
        this.chestFire = root.getChild(KyuubiGeometry.CHEST_FIRE);
        this.bodyFire = root.getChild(KyuubiGeometry.BODY_FIRE);
        this.lfArmLowerFire = root.getChild(KyuubiGeometry.LF_ARM_LOWER_FIRE);
        this.rtArmUpperFire = root.getChild(KyuubiGeometry.RT_ARM_UPPER_FIRE);
        this.rtArmLowerFire = root.getChild(KyuubiGeometry.RT_ARM_LOWER_FIRE);
        this.lfLegUppperFire = root.getChild(KyuubiGeometry.LF_LEG_UPPPER_FIRE);
        this.lfLegLowerFire = root.getChild(KyuubiGeometry.LF_LEG_LOWER_FIRE);
        this.rtLegUpperFire = root.getChild(KyuubiGeometry.RT_LEG_UPPER_FIRE);
        this.rtLegLowerFire = root.getChild(KyuubiGeometry.RT_LEG_LOWER_FIRE);
        this.drawOrder = new ModelPart[] {
                this.lfLegLower, this.rtLegLower, this.head, this.chest, this.lfArmUpper, this.rtArmLower, this.lfLegUpper,
                this.rtLegUpper, this.body, this.rtArmUpper, this.lfArmLower, this.rtHorn5, this.lfHorn5, this.tail9,
                this.tail8, this.tail7, this.tail6, this.tail5, this.tail2, this.tail1, this.tail0, this.tail3, this.tail4,
                this.lfHorn2, this.rtHorn1, this.rtHorn2, this.lfHorn1, this.lfHorn3, this.rtHorn3, this.lfHorn4,
                this.rtHorn4, this.headFire, this.lfArmUpperFire, this.chestFire, this.bodyFire, this.lfArmLowerFire,
                this.rtArmUpperFire, this.rtArmLowerFire, this.lfLegUppperFire, this.lfLegLowerFire, this.rtLegUpperFire,
                this.rtLegLowerFire };
    }

    /** The writes of {@code render()} (:275-386). */
    @Override
    public void setupAnim(final Kyuubi e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.2f * f1;
        } else {
            newangle = 0.0f;
        }
        this.rtLegUpper.xRot = 0.59f + newangle;
        this.rtLegUpperFire.xRot = 0.59f + newangle;
        this.rtLegLower.xRot = -0.15f + newangle;
        this.rtLegLowerFire.xRot = -0.15f + newangle;
        this.rtLegLower.z = (float) (Math.sin(this.rtLegUpperFire.xRot) * 8.0);
        this.rtLegLowerFire.z = (float) (Math.sin(this.rtLegUpperFire.xRot) * 8.0);
        this.lfLegUpper.xRot = 0.26f - newangle;
        this.lfLegUppperFire.xRot = 0.26f - newangle;
        this.lfLegLower.xRot = -0.44f - newangle;
        this.lfLegLowerFire.xRot = -0.44f - newangle;
        this.lfLegLower.z = (float) (Math.sin(this.lfLegUppperFire.xRot) * 8.0);
        this.lfLegLowerFire.z = (float) (Math.sin(this.lfLegUppperFire.xRot) * 8.0);
        newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.08f * f1;
        newangle += Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.01f;
        this.rtArmUpper.xRot = newangle;
        this.rtArmUpperFire.xRot = newangle;
        this.rtArmLower.xRot = 0.48f + newangle;
        this.rtArmLowerFire.xRot = 0.48f + newangle;
        this.rtArmLower.z = (float) (Math.sin(this.rtArmUpperFire.xRot) * 8.0);
        this.rtArmLowerFire.z = (float) (Math.sin(this.rtArmUpperFire.xRot) * 8.0);
        this.lfArmUpper.xRot = -newangle;
        this.lfArmUpperFire.xRot = -newangle;
        this.lfArmLower.xRot = 0.48f - newangle;
        this.lfArmLowerFire.xRot = 0.48f - newangle;
        this.lfArmLower.z = (float) (Math.sin(this.lfArmUpperFire.xRot) * 8.0);
        this.lfArmLowerFire.z = (float) (Math.sin(this.lfArmUpperFire.xRot) * 8.0);
        final float pi4 = 0.7853975f;
        this.head.yRot = (float) Math.toRadians(f3) + pi4 * 4.0f;
        this.headFire.yRot = (float) Math.toRadians(f3);
        float fc = (float) Math.cos(this.headFire.yRot + pi4);
        float fs = (float) Math.sin(this.headFire.yRot + pi4);
        this.lfHorn1.z = this.headFire.z - fc * 3.6f;
        this.lfHorn1.x = this.headFire.x - fs * 3.6f;
        this.lfHorn1.yRot = this.headFire.yRot + 0.244f + Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.lfHorn2.z = this.lfHorn1.z - (float) Math.cos(this.lfHorn1.yRot) * 2.0f;
        this.lfHorn2.x = this.lfHorn1.x - (float) Math.sin(this.lfHorn1.yRot) * 2.0f;
        this.lfHorn2.yRot = this.headFire.yRot + 0.244f + Mth.cos(f2 * 1.3f * this.wingspeed - pi4) * 3.1415927f * 0.1f;
        this.lfHorn3.z = this.lfHorn2.z - (float) Math.cos(this.lfHorn2.yRot) * 4.0f;
        this.lfHorn3.x = this.lfHorn2.x - (float) Math.sin(this.lfHorn2.yRot) * 4.0f;
        this.lfHorn3.yRot = this.headFire.yRot + 0.244f + Mth.cos(f2 * 1.3f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.1f;
        this.lfHorn4.z = this.lfHorn3.z - (float) Math.cos(this.lfHorn3.yRot) * 3.0f;
        this.lfHorn4.x = this.lfHorn3.x - (float) Math.sin(this.lfHorn3.yRot) * 3.0f;
        this.lfHorn4.yRot = this.headFire.yRot + 0.244f + Mth.cos(f2 * 1.3f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.1f;
        this.lfHorn5.z = this.lfHorn4.z - (float) Math.cos(this.lfHorn4.yRot) * 2.0f;
        this.lfHorn5.x = this.lfHorn4.x - (float) Math.sin(this.lfHorn4.yRot) * 2.0f;
        this.lfHorn5.yRot = this.headFire.yRot + 0.244f + Mth.cos(f2 * 1.3f * this.wingspeed - 4.0f * pi4) * 3.1415927f * 0.1f;
        fc = (float) Math.cos(this.headFire.yRot - pi4);
        fs = (float) Math.sin(this.headFire.yRot - pi4);
        this.rtHorn1.z = this.headFire.z - fc * 3.6f;
        this.rtHorn1.x = this.headFire.x - fs * 3.6f;
        this.rtHorn1.yRot = this.headFire.yRot - 0.244f - Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.rtHorn2.z = this.rtHorn1.z - (float) Math.cos(this.rtHorn1.yRot) * 2.0f;
        this.rtHorn2.x = this.rtHorn1.x - (float) Math.sin(this.rtHorn1.yRot) * 2.0f;
        this.rtHorn2.yRot = this.headFire.yRot - 0.244f - Mth.cos(f2 * 1.3f * this.wingspeed - pi4) * 3.1415927f * 0.1f;
        this.rtHorn3.z = this.rtHorn2.z - (float) Math.cos(this.rtHorn2.yRot) * 4.0f;
        this.rtHorn3.x = this.rtHorn2.x - (float) Math.sin(this.rtHorn2.yRot) * 4.0f;
        this.rtHorn3.yRot = this.headFire.yRot - 0.244f - Mth.cos(f2 * 1.3f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.1f;
        this.rtHorn4.z = this.rtHorn3.z - (float) Math.cos(this.rtHorn3.yRot) * 3.0f;
        this.rtHorn4.x = this.rtHorn3.x - (float) Math.sin(this.rtHorn3.yRot) * 3.0f;
        this.rtHorn4.yRot = this.headFire.yRot - 0.244f - Mth.cos(f2 * 1.3f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.1f;
        this.rtHorn5.z = this.rtHorn4.z - (float) Math.cos(this.rtHorn4.yRot) * 2.0f;
        this.rtHorn5.x = this.rtHorn4.x - (float) Math.sin(this.rtHorn4.yRot) * 2.0f;
        this.rtHorn5.yRot = this.headFire.yRot - 0.244f - Mth.cos(f2 * 1.3f * this.wingspeed - 4.0f * pi4) * 3.1415927f * 0.1f;
        this.tail1.yRot = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.2f;
        this.tail2.x = this.tail1.x - (float) Math.sin(this.tail1.yRot) * 3.0f;
        this.tail2.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - pi4) * 3.1415927f * 0.2f;
        this.tail3.x = this.tail2.x - (float) Math.sin(this.tail2.yRot) * 4.0f;
        this.tail3.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.2f;
        this.tail4.x = this.tail3.x - (float) Math.sin(this.tail3.yRot) * 3.5f;
        this.tail4.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.2f;
        this.tail5.x = this.tail4.x - (float) Math.sin(this.tail4.yRot) * 5.0f;
        this.tail5.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 4.0f * pi4) * 3.1415927f * 0.2f;
        this.tail6.x = this.tail5.x - (float) Math.sin(this.tail5.yRot) * 4.0f;
        this.tail6.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 5.0f * pi4) * 3.1415927f * 0.2f;
        this.tail7.x = this.tail6.x - (float) Math.sin(this.tail6.yRot) * 3.0f;
        this.tail7.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 6.0f * pi4) * 3.1415927f * 0.2f;
        this.tail8.x = this.tail7.x - (float) Math.sin(this.tail7.yRot) * 2.0f;
        this.tail8.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 7.0f * pi4) * 3.1415927f * 0.2f;
        this.tail9.x = this.tail8.x - (float) Math.sin(this.tail8.yRot) * 1.0f;
        this.tail9.yRot = Mth.cos(f2 * 0.9f * this.wingspeed - 8.0f * pi4) * 3.1415927f * 0.2f;
        this.tail1.xRot = -0.26f + Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.1f;
        this.tail2.y = this.tail1.y + (float) Math.sin(this.tail1.xRot) * 3.0f;
        this.tail2.z = this.tail1.z - (float) Math.cos(this.tail1.xRot) * 3.0f;
        this.tail2.xRot = -0.78f + Mth.cos(f2 * 0.5f * this.wingspeed - pi4) * 3.1415927f * 0.1f;
        this.tail3.y = this.tail2.y + (float) Math.sin(this.tail2.xRot) * 4.0f;
        this.tail3.z = this.tail2.z - (float) Math.cos(this.tail2.xRot) * 4.0f;
        this.tail3.xRot = -1.11f + Mth.cos(f2 * 0.5f * this.wingspeed - 2.0f * pi4) * 3.1415927f * 0.1f;
        this.tail4.y = this.tail3.y + (float) Math.sin(this.tail3.xRot) * 3.5f;
        this.tail4.z = this.tail3.z - (float) Math.cos(this.tail3.xRot) * 3.5f;
        this.tail4.xRot = -0.18f + Mth.cos(f2 * 0.5f * this.wingspeed - 3.0f * pi4) * 3.1415927f * 0.1f;
        this.tail5.y = this.tail4.y + (float) Math.sin(this.tail4.xRot) * 5.0f;
        this.tail5.z = this.tail4.z - (float) Math.cos(this.tail4.xRot) * 5.0f;
        this.tail5.xRot = 0.22f + Mth.cos(f2 * 0.5f * this.wingspeed - 4.0f * pi4) * 3.1415927f * 0.1f;
        this.tail6.y = this.tail5.y + (float) Math.sin(this.tail5.xRot) * 4.0f;
        this.tail6.z = this.tail5.z - (float) Math.cos(this.tail5.xRot) * 4.0f;
        this.tail6.xRot = 0.63f + Mth.cos(f2 * 0.5f * this.wingspeed - 5.0f * pi4) * 3.1415927f * 0.1f;
        this.tail7.y = this.tail6.y + (float) Math.sin(this.tail6.xRot) * 3.0f;
        this.tail7.z = this.tail6.z - (float) Math.cos(this.tail6.xRot) * 3.0f;
        this.tail7.xRot = 0.89f + Mth.cos(f2 * 0.5f * this.wingspeed - 6.0f * pi4) * 3.1415927f * 0.1f;
        this.tail8.y = this.tail7.y + (float) Math.sin(this.tail7.xRot) * 2.0f;
        this.tail8.z = this.tail7.z - (float) Math.cos(this.tail7.xRot) * 2.0f;
        this.tail8.xRot = 1.52f + Mth.cos(f2 * 0.5f * this.wingspeed - 7.0f * pi4) * 3.1415927f * 0.1f;
        this.tail9.y = this.tail8.y + (float) Math.sin(this.tail8.xRot) * 2.0f;
        this.tail9.z = this.tail8.z - (float) Math.cos(this.tail8.xRot) * 2.0f;
        this.tail9.xRot = 2.0f + Mth.cos(f2 * 0.5f * this.wingspeed - 8.0f * pi4) * 3.1415927f * 0.1f;
    }

    /** The blend block of {@code render()} (:387-437): half turn about Y, all parts in their draw order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        poseStack.pushPose();
        // glTranslatef(0, 0, 0), glRotatef(180, 0, 1, 0), glTranslatef(0, 0, 0) (:391-393).
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        poseStack.popPose();
    }
}
