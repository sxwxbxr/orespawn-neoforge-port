package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.AntGeometry;
import com.swbr.orespawn.entity.portal.EntityAnt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelAnt} (ModelAnt.java:6-183), shared by all five ant renderers
 * (ClientProxyOreSpawn.java:42-45, :106). Thorax from three boxes, two-part abdomen, head, two jaws and
 * six two-segment legs, 64x32 texture. The geometry (:28-130) is the generated {@link AntGeometry};
 * this class bakes it and animates it the way {@code render()} did.
 *
 * <p>Arguments of the 1.7.10 {@code render(entity, f, f1, f2, f3, f4, f5)} as
 * {@code RendererLivingEntity.renderModel} passed them: {@code f} limb swing, {@code f1} limb swing
 * amount (clamped to 1), {@code f2} ticks existed plus partial tick. The model reads only {@code f1}
 * and {@code f2}, which are {@code limbSwingAmount} and {@code ageInTicks} here.
 */
public class AntModel extends EntityModel<EntityAnt> {

    /** Register with {@code AntGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ant"), "main");

    private final ModelPart thorax;
    private final ModelPart thorax1;
    private final ModelPart thorax3;
    private final ModelPart abdomen;
    private final ModelPart abdomen1;
    private final ModelPart head;
    private final ModelPart jawsr;
    private final ModelPart jawsl;
    private final ModelPart llegtop1;
    private final ModelPart llegbot1;
    private final ModelPart llegtop2;
    private final ModelPart llegbot2;
    private final ModelPart llegtop3;
    private final ModelPart llegbot3;
    private final ModelPart rlegtop1;
    private final ModelPart rlegbot1;
    private final ModelPart rlegtop2;
    private final ModelPart rlegbot2;
    private final ModelPart rlegtop3;
    private final ModelPart rlegbot3;

    public AntModel(final ModelPart root) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.thorax = root.getChild(AntGeometry.THORAX);
        this.thorax1 = root.getChild(AntGeometry.THORAX1);
        this.thorax3 = root.getChild(AntGeometry.THORAX3);
        this.abdomen = root.getChild(AntGeometry.ABDOMEN);
        this.abdomen1 = root.getChild(AntGeometry.ABDOMEN1);
        this.head = root.getChild(AntGeometry.HEAD);
        this.jawsr = root.getChild(AntGeometry.JAWSR);
        this.jawsl = root.getChild(AntGeometry.JAWSL);
        this.llegtop1 = root.getChild(AntGeometry.LLEGTOP1);
        this.llegbot1 = root.getChild(AntGeometry.LLEGBOT1);
        this.llegtop2 = root.getChild(AntGeometry.LLEGTOP2);
        this.llegbot2 = root.getChild(AntGeometry.LLEGBOT2);
        this.llegtop3 = root.getChild(AntGeometry.LLEGTOP3);
        this.llegbot3 = root.getChild(AntGeometry.LLEGBOT3);
        this.rlegtop1 = root.getChild(AntGeometry.RLEGTOP1);
        this.rlegbot1 = root.getChild(AntGeometry.RLEGBOT1);
        this.rlegtop2 = root.getChild(AntGeometry.RLEGTOP2);
        this.rlegbot2 = root.getChild(AntGeometry.RLEGBOT2);
        this.rlegtop3 = root.getChild(AntGeometry.RLEGTOP3);
        this.rlegbot3 = root.getChild(AntGeometry.RLEGBOT3);
    }

    /**
     * The angle writes of {@code render()} (:135-151); {@code setRotationAngles} (:178-180) only called
     * the empty {@code ModelBase} version. Every written angle is recomputed each frame, so the
     * {@code resetPose()} of R8 changes nothing visible; it is there because the rule says so.
     */
    @Override
    public void setupAnim(final EntityAnt entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        this.thorax.resetPose();
        this.thorax1.resetPose();
        this.thorax3.resetPose();
        this.abdomen.resetPose();
        this.abdomen1.resetPose();
        this.head.resetPose();
        this.jawsr.resetPose();
        this.jawsl.resetPose();
        this.llegtop1.resetPose();
        this.llegbot1.resetPose();
        this.llegtop2.resetPose();
        this.llegbot2.resetPose();
        this.llegtop3.resetPose();
        this.llegbot3.resetPose();
        this.rlegtop1.resetPose();
        this.rlegbot1.resetPose();
        this.rlegtop2.resetPose();
        this.rlegbot2.resetPose();
        this.rlegtop3.resetPose();
        this.rlegbot3.resetPose();
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        // :138-149 - tripod gait, the lower segment follows its upper one rigidly.
        this.llegtop1.xRot = Mth.cos(f2 * 2.7f) * 3.1415927f * 0.45f * f1;
        this.llegbot1.xRot = this.llegtop1.xRot;
        this.rlegtop2.xRot = this.llegtop1.xRot;
        this.rlegbot2.xRot = this.llegtop1.xRot;
        this.rlegtop3.xRot = this.llegtop1.xRot;
        this.rlegbot3.xRot = this.llegtop1.xRot;
        this.rlegtop1.xRot = -this.llegtop1.xRot;
        this.rlegbot1.xRot = -this.llegtop1.xRot;
        this.llegtop2.xRot = -this.llegtop1.xRot;
        this.llegbot2.xRot = -this.llegtop1.xRot;
        this.llegtop3.xRot = -this.llegtop1.xRot;
        this.llegbot3.xRot = -this.llegtop1.xRot;
        // :150-151 - the jaws chew all the time, also standing still.
        this.jawsl.yRot = Mth.cos(f2 * 0.4f) * 3.1415927f * 0.05f;
        this.jawsr.yRot = -this.jawsl.yRot;
    }

    /** {@code render()} (:152-171): the twenty parts in creation order at scale {@code f5 = 0.0625}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.thorax.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.thorax1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.thorax3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.abdomen1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jawsr.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jawsl.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.llegtop1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.llegbot1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.llegtop2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.llegbot2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.llegtop3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.llegbot3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rlegtop1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rlegbot1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rlegtop2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rlegbot2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rlegtop3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rlegbot3.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
