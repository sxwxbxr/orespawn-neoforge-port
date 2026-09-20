package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.AttackSquidGeometry;
import com.swbr.orespawn.entity.sea.AttackSquid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelAttackSquid} (ModelAttackSquid.java:7-143): a body and eight tentacles on a
 * 64x32 texture, geometry {@link AttackSquidGeometry}. {@code wingspeed} 1.0 (ClientProxyOreSpawn :67). The
 * tentacles lash with the walk, idle slowly otherwise; the body wobbles and turns three quarters of the head yaw.
 * No GL calls.
 */
public class AttackSquidModel extends EntityModel<AttackSquid> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "attack_squid"), "main");

    private final float wingspeed;
    private final ModelPart tent1;
    private final ModelPart tent2;
    private final ModelPart tent3;
    private final ModelPart tent4;
    private final ModelPart tent5;
    private final ModelPart tent6;
    private final ModelPart tent7;
    private final ModelPart body;
    private final ModelPart tent8;

    /** {@code ModelAttackSquid(float f1)} (:20-...). */
    public AttackSquidModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.tent1 = root.getChild(AttackSquidGeometry.TENT1);
        this.tent2 = root.getChild(AttackSquidGeometry.TENT2);
        this.tent3 = root.getChild(AttackSquidGeometry.TENT3);
        this.tent4 = root.getChild(AttackSquidGeometry.TENT4);
        this.tent5 = root.getChild(AttackSquidGeometry.TENT5);
        this.tent6 = root.getChild(AttackSquidGeometry.TENT6);
        this.tent7 = root.getChild(AttackSquidGeometry.TENT7);
        this.body = root.getChild(AttackSquidGeometry.BODY);
        this.tent8 = root.getChild(AttackSquidGeometry.TENT8);
    }

    /** The writes of {@code render()}, in its order. */
    @Override
    public void setupAnim(final AttackSquid entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.tent1.resetPose();
        this.tent2.resetPose();
        this.tent3.resetPose();
        this.tent4.resetPose();
        this.tent5.resetPose();
        this.tent6.resetPose();
        this.tent7.resetPose();
        this.body.resetPose();
        this.tent8.resetPose();
        float newangleA = 0.0f;
        float newangleB = 0.0f;
        float newangle8 = 0.0f;
        float newangle9 = 0.0f;
        float newangle10 = 0.0f;
        float newangle11 = 0.0f;
        float newangle12 = 0.0f;
        float newangle13 = 0.0f;
        float newangle14 = 0.0f;
        float newangle15 = 0.0f;
        if (f1 > 0.1) {
            newangleA = Mth.cos(f2 * 0.25f * this.wingspeed) * 3.1415927f * 0.04f * f1;
            newangleB = Mth.cos(f2 * 0.39f * this.wingspeed) * 3.1415927f * 0.04f * f1;
            newangle9 = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle10 = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle11 = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle12 = Mth.cos(f2 * 1.9f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle13 = Mth.cos(f2 * 1.8f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle14 = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle15 = Mth.cos(f2 * 1.6f * this.wingspeed) * 3.1415927f * 0.4f * f1;
            newangle8 = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.4f * f1;
        } else {
            newangleA = Mth.cos(f2 * 0.25f * this.wingspeed) * 3.1415927f * 0.01f;
            newangleB = Mth.cos(f2 * 0.39f * this.wingspeed) * 3.1415927f * 0.01f;
            newangle9 = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle10 = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle11 = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle12 = Mth.cos(f2 * 1.9f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle13 = Mth.cos(f2 * 1.8f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle14 = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle15 = Mth.cos(f2 * 1.6f * this.wingspeed) * 3.1415927f * 0.1f;
            newangle8 = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.1f;
        }
        this.tent1.xRot = newangle9 - 1.03f;
        this.tent7.zRot = newangle10 + 0.37f;
        this.tent5.xRot = newangle11 + 0.6f;
        this.tent6.xRot = newangle12 - 0.48f;
        this.tent4.xRot = newangle13 + 0.63f;
        this.tent2.zRot = newangle14 - 0.26f;
        this.tent3.xRot = newangle15 - 1.03f;
        this.tent8.xRot = newangle8 + 0.43f;
        this.body.xRot = newangleA;
        this.body.zRot = newangleB;
        newangleA = (float) Math.toRadians(f3) * 0.75f;
        this.body.yRot = newangleA;
    }

    /** Draw order of {@code render()}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.tent1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tent8.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
