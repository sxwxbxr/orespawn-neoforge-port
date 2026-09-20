package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GoldFishGeometry;
import com.swbr.orespawn.entity.aquatic.GoldFish;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelGoldFish} (ModelGoldFish.java:7-158): a 16-box goldfish on a 64x64 texture.
 * The geometry (:27-112) is the generated {@link GoldFishGeometry}.
 *
 * <p>{@code wingspeed} is the constructor argument (:27-29): 0.7 for the Gold Fish (ClientProxyOreSpawn.java:100,
 * manifest {@code model_args}).
 *
 * <p>{@code render()} (:114-147) writes fin and jaw angles from {@code f2} (age in ticks) and draws every part;
 * the writes move to {@link #setupAnim}, the draws to {@link #renderToBuffer} (R8). Culling off and alpha test on,
 * as 1.7.10 {@code RendererLivingEntity} drew: {@code entityCutoutNoCull} - the fins are zero-thickness planes.
 */
public class GoldFishModel extends EntityModel<GoldFish> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "gold_fish"), "main");

    private final float wingspeed;
    private final ModelPart Body;
    private final ModelPart Head;
    private final ModelPart Dorsalfin;
    private final ModelPart Mouth;
    private final ModelPart Jaw;
    private final ModelPart Pectoralfin1;
    private final ModelPart Pectoralfin2;
    private final ModelPart Pectoralfin3;
    private final ModelPart Pectoralfin4;
    private final ModelPart Bottomfin;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Caudalfin1;
    private final ModelPart Caudalfin2;
    private final ModelPart Bottomfin1;
    private final ModelPart Bottomfin2;

    /** {@code ModelGoldFish(float f1)} (:27-112). */
    public GoldFishModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Body = root.getChild(GoldFishGeometry.BODY);
        this.Head = root.getChild(GoldFishGeometry.HEAD);
        this.Dorsalfin = root.getChild(GoldFishGeometry.DORSALFIN);
        this.Mouth = root.getChild(GoldFishGeometry.MOUTH);
        this.Jaw = root.getChild(GoldFishGeometry.JAW);
        this.Pectoralfin1 = root.getChild(GoldFishGeometry.PECTORALFIN1);
        this.Pectoralfin2 = root.getChild(GoldFishGeometry.PECTORALFIN2);
        this.Pectoralfin3 = root.getChild(GoldFishGeometry.PECTORALFIN3);
        this.Pectoralfin4 = root.getChild(GoldFishGeometry.PECTORALFIN4);
        this.Bottomfin = root.getChild(GoldFishGeometry.BOTTOMFIN);
        this.Tail1 = root.getChild(GoldFishGeometry.TAIL1);
        this.Tail2 = root.getChild(GoldFishGeometry.TAIL2);
        this.Caudalfin1 = root.getChild(GoldFishGeometry.CAUDALFIN1);
        this.Caudalfin2 = root.getChild(GoldFishGeometry.CAUDALFIN2);
        this.Bottomfin1 = root.getChild(GoldFishGeometry.BOTTOMFIN1);
        this.Bottomfin2 = root.getChild(GoldFishGeometry.BOTTOMFIN2);
    }

    /**
     * The angle writes of {@code render()} (:118-130). Only the Y angle of the pectoral and bottom fins and the X
     * angle of the jaw change; their other constructor angles stay, which {@code resetPose()} restores first.
     */
    @Override
    public void setupAnim(final GoldFish entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Pectoralfin1.resetPose();
        this.Pectoralfin2.resetPose();
        this.Pectoralfin3.resetPose();
        this.Pectoralfin4.resetPose();
        this.Bottomfin1.resetPose();
        this.Bottomfin2.resetPose();
        this.Jaw.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.15f;
        this.Pectoralfin1.yRot = 0.4f + newangle;
        newangle = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.15f;
        this.Pectoralfin2.yRot = -0.4f + newangle;
        newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.15f;
        this.Pectoralfin3.yRot = 0.4f + newangle;
        newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        this.Pectoralfin4.yRot = -0.4f + newangle;
        newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.25f;
        this.Bottomfin1.yRot = newangle;
        this.Bottomfin2.yRot = -newangle;
        newangle = Mth.cos(f2 * 0.7f * this.wingspeed) * 3.1415927f * 0.1f;
        this.Jaw.xRot = -0.25f + newangle;
    }

    /** Draw order of {@code render()} (:131-146). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Dorsalfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Mouth.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Jaw.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Pectoralfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Pectoralfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Pectoralfin3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Pectoralfin4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Bottomfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Caudalfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Caudalfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Bottomfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Bottomfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
