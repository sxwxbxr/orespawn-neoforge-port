package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.FrogGeometry;
import com.swbr.orespawn.entity.aquatic.Frog;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelFrog} (ModelFrog.java:7-133): ten boxes on a 64x64 texture. The geometry
 * (:21-76) is the generated {@link FrogGeometry}; {@code wingspeed} is the constructor argument, 1.0 for the frog
 * (ClientProxyOreSpawn.java:142).
 *
 * <p>{@code render()} (:78-122): the legs paddle while walking, the jaw opens while {@link Frog#getSinging()} is set,
 * and the hind legs stretch out while the frog moves up or down faster than 0.1 per tick. The lower hind legs hang at
 * the end of the upper ones, 9 pixels along their angle (R8: pivot writes in {@link #setupAnim}, reset first).
 *
 * <p>PORT: the jump pose reads the client copy of the motion, as {@code c.motionY} did. 1.7.10 sent no velocity for
 * this entity ({@code registerModEntity(..., false)}) except after knockback, so the stretched legs showed mostly
 * after a hit. 1.21.1 also sends the motion when {@code hasImpulse} is set, which {@code Frog.jumpAround} does, so
 * the pose should also show on the frog's own hops. Not rendered on this machine (R17).
 */
public class FrogModel extends EntityModel<Frog> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "frog"), "main");

    private final float wingspeed;
    private final ModelPart body;
    private final ModelPart jaw;
    private final ModelPart lfleg;
    private final ModelPart rfleg;
    private final ModelPart lleg1;
    private final ModelPart rleg1;
    private final ModelPart lleg2;
    private final ModelPart rleg2;
    private final ModelPart leye;
    private final ModelPart reye;

    /** {@code ModelFrog(float f1)} (:21-76). */
    public FrogModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(FrogGeometry.BODY);
        this.jaw = root.getChild(FrogGeometry.JAW);
        this.lfleg = root.getChild(FrogGeometry.LFLEG);
        this.rfleg = root.getChild(FrogGeometry.RFLEG);
        this.lleg1 = root.getChild(FrogGeometry.LLEG1);
        this.rleg1 = root.getChild(FrogGeometry.RLEG1);
        this.lleg2 = root.getChild(FrogGeometry.LLEG2);
        this.rleg2 = root.getChild(FrogGeometry.RLEG2);
        this.leye = root.getChild(FrogGeometry.LEYE);
        this.reye = root.getChild(FrogGeometry.REYE);
    }

    /** The angle and pivot writes of {@code render()} (:82-111). */
    @Override
    public void setupAnim(final Frog c, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lfleg.resetPose();
        this.rfleg.resetPose();
        this.lleg1.resetPose();
        this.rleg1.resetPose();
        this.lleg2.resetPose();
        this.rleg2.resetPose();
        this.jaw.resetPose();
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * this.wingspeed * 1.4f) * 3.1415927f * 0.55f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lfleg.yRot = newangle;
        this.rfleg.yRot = -newangle;
        this.lleg2.yRot = -newangle / 2.0f;
        this.rleg2.yRot = newangle / 2.0f;
        if (c.getSinging() != 0) {
            newangle = Mth.cos(f2 * 0.85f * this.wingspeed) * 3.1415927f * 0.15f;
        } else {
            newangle = 0.0f;
        }
        this.jaw.xRot = newangle + 1.22f;
        final double motionY = c.getDeltaMovement().y;
        if (motionY > 0.10000000149011612 || motionY < -0.10000000149011612) {
            this.lleg1.zRot = 2.44f;
            this.rleg1.zRot = -2.44f;
        } else {
            this.lleg1.zRot = 0.227f;
            this.rleg1.zRot = -0.227f;
        }
        this.lleg2.y = this.lleg1.y - (float) Math.cos(this.lleg1.zRot) * 9.0f;
        this.lleg2.x = this.lleg1.x + (float) Math.sin(this.lleg1.zRot) * 9.0f;
        this.rleg2.y = this.rleg1.y - (float) Math.cos(this.rleg1.zRot) * 9.0f;
        this.rleg2.x = this.rleg1.x + (float) Math.sin(this.rleg1.zRot) * 9.0f;
    }

    /** Draw order of {@code render()} (:112-121). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leye.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.reye.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
