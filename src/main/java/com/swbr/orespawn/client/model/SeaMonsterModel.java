package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SeaMonsterGeometry;
import com.swbr.orespawn.entity.sea.SeaMonster;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelSeaMonster} (ModelSeaMonster.java:7-293): a plesiosaur with a seven-part tail,
 * a six-part neck, jaws, eyes and four flippers on a 256x128 texture, geometry {@link SeaMonsterGeometry}.
 * {@code wingspeed} 0.5 (ClientProxyOreSpawn :126). The tail and neck chains are the faked hierarchy of
 * docs/research/06-models-design.md: each segment's pivot is recomputed from the previous one every frame. The jaw
 * snaps while {@code attacking} is set. No GL calls.
 */
public class SeaMonsterModel extends EntityModel<SeaMonster> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "sea_monster"), "main");

    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart TailTip;
    private final ModelPart TailBase;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Neck6;
    private final ModelPart NeckBase;
    private final ModelPart Neck2;
    private final ModelPart Neck3;
    private final ModelPart Neck4;
    private final ModelPart Neck5;
    private final ModelPart BottomJaw;
    private final ModelPart FinBackRight;
    private final ModelPart FinBackLeft;
    private final ModelPart FinFrontLeft;
    private final ModelPart FinFrontRight;
    private final ModelPart Tail4;
    private final ModelPart Tail5;
    private final ModelPart Tail6;
    private final ModelPart TopJaw;
    private final ModelPart RightEye;
    private final ModelPart LeftEye;

    /** {@code ModelSeaMonster(float f1)} (:34-...). */
    public SeaMonsterModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[SeaMonsterGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(SeaMonsterGeometry.PARTS[i]);
        }
        this.TailTip = root.getChild(SeaMonsterGeometry.TAIL_TIP);
        this.TailBase = root.getChild(SeaMonsterGeometry.TAIL_BASE);
        this.Tail2 = root.getChild(SeaMonsterGeometry.TAIL2);
        this.Tail3 = root.getChild(SeaMonsterGeometry.TAIL3);
        this.Neck6 = root.getChild(SeaMonsterGeometry.NECK6);
        this.NeckBase = root.getChild(SeaMonsterGeometry.NECK_BASE);
        this.Neck2 = root.getChild(SeaMonsterGeometry.NECK2);
        this.Neck3 = root.getChild(SeaMonsterGeometry.NECK3);
        this.Neck4 = root.getChild(SeaMonsterGeometry.NECK4);
        this.Neck5 = root.getChild(SeaMonsterGeometry.NECK5);
        this.BottomJaw = root.getChild(SeaMonsterGeometry.BOTTOM_JAW);
        this.FinBackRight = root.getChild(SeaMonsterGeometry.FIN_BACK_RIGHT);
        this.FinBackLeft = root.getChild(SeaMonsterGeometry.FIN_BACK_LEFT);
        this.FinFrontLeft = root.getChild(SeaMonsterGeometry.FIN_FRONT_LEFT);
        this.FinFrontRight = root.getChild(SeaMonsterGeometry.FIN_FRONT_RIGHT);
        this.Tail4 = root.getChild(SeaMonsterGeometry.TAIL4);
        this.Tail5 = root.getChild(SeaMonsterGeometry.TAIL5);
        this.Tail6 = root.getChild(SeaMonsterGeometry.TAIL6);
        this.TopJaw = root.getChild(SeaMonsterGeometry.TOP_JAW);
        this.RightEye = root.getChild(SeaMonsterGeometry.RIGHT_EYE);
        this.LeftEye = root.getChild(SeaMonsterGeometry.LEFT_EYE);
    }

    /** The writes of {@code render()}, in its order. */
    @Override
    public void setupAnim(final SeaMonster e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (f1 > 0.1 || e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.2f * f1;
        } else {
            newangle = 0.0f;
        }
        this.TailBase.yRot = newangle / 7.0f;
        this.Tail2.z = this.TailBase.z + (float) Math.cos(this.TailBase.yRot) * 10.0f;
        this.Tail2.x = this.TailBase.x + (float) Math.sin(this.TailBase.yRot) * 10.0f;
        this.Tail2.yRot = newangle / 6.0f;
        this.Tail3.z = this.Tail2.z + (float) Math.cos(this.Tail2.yRot) * 7.0f;
        this.Tail3.x = this.Tail2.x + (float) Math.sin(this.Tail2.yRot) * 7.0f;
        this.Tail3.yRot = newangle / 5.0f;
        this.Tail4.z = this.Tail3.z + (float) Math.cos(this.Tail3.yRot) * 5.0f;
        this.Tail4.x = this.Tail3.x + (float) Math.sin(this.Tail3.yRot) * 5.0f;
        this.Tail4.yRot = newangle / 4.0f;
        this.Tail5.z = this.Tail4.z + (float) Math.cos(this.Tail4.yRot) * 5.0f;
        this.Tail5.x = this.Tail4.x + (float) Math.sin(this.Tail4.yRot) * 5.0f;
        this.Tail5.yRot = newangle / 3.0f;
        this.Tail6.z = this.Tail5.z + (float) Math.cos(this.Tail5.yRot) * 5.0f;
        this.Tail6.x = this.Tail5.x + (float) Math.sin(this.Tail5.yRot) * 5.0f;
        this.Tail6.yRot = newangle / 2.0f;
        this.TailTip.z = this.Tail6.z + (float) Math.cos(this.Tail6.yRot) * 5.0f;
        this.TailTip.x = this.Tail6.x + (float) Math.sin(this.Tail6.yRot) * 5.0f;
        this.TailTip.yRot = newangle;
        if (f1 > 0.1 || e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.2f * f1;
        } else {
            newangle = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.02f;
        }
        this.FinFrontLeft.xRot = newangle - 0.523f;
        this.FinFrontLeft.yRot = newangle + 0.698f;
        this.FinBackLeft.xRot = -newangle - 0.523f;
        this.FinBackLeft.yRot = -newangle + 0.698f;
        this.FinFrontRight.xRot = newangle - 0.523f;
        this.FinFrontRight.yRot = newangle - 0.698f;
        this.FinBackRight.xRot = -newangle - 0.523f;
        this.FinBackRight.yRot = -newangle - 0.698f;
        if (f1 > 0.1 || e.getAttacking() != 0) {
            newangle = 0.455f * f1 + Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.02f;
        }
        this.NeckBase.xRot = 0.455f + newangle / 5.0f;
        this.Neck2.z = this.NeckBase.z - (float) Math.sin(this.NeckBase.xRot) * 9.0f;
        this.Neck2.y = this.NeckBase.y - (float) Math.cos(this.NeckBase.xRot) * 9.0f;
        this.Neck2.xRot = this.NeckBase.xRot + newangle / 4.0f;
        this.Neck3.z = this.Neck2.z - (float) Math.sin(this.Neck2.xRot) * 9.0f;
        this.Neck3.y = this.Neck2.y - (float) Math.cos(this.Neck2.xRot) * 9.0f;
        this.Neck3.xRot = this.Neck2.xRot + newangle / 3.0f;
        this.Neck4.z = this.Neck3.z - (float) Math.sin(this.Neck3.xRot) * 9.0f;
        this.Neck4.y = this.Neck3.y - (float) Math.cos(this.Neck3.xRot) * 9.0f;
        this.Neck4.xRot = this.Neck3.xRot + newangle / 2.0f;
        this.Neck5.z = this.Neck4.z - (float) Math.sin(this.Neck4.xRot) * 9.0f;
        this.Neck5.y = this.Neck4.y - (float) Math.cos(this.Neck4.xRot) * 9.0f;
        this.Neck5.xRot = this.Neck4.xRot - newangle / 2.0f;
        this.Neck6.z = this.Neck5.z - (float) Math.sin(this.Neck5.xRot) * 5.0f;
        this.Neck6.y = this.Neck5.y - (float) Math.cos(this.Neck5.xRot) * 5.0f;
        this.Neck6.xRot = this.Neck5.xRot - newangle / 3.0f;
        final float n = this.Neck6.z - (float) Math.sin(this.Neck6.xRot) * 5.0f;
        this.TopJaw.z = n;
        this.RightEye.z = n;
        this.LeftEye.z = n;
        this.BottomJaw.z = n;
        final float n2 = this.Neck6.y - (float) Math.cos(this.Neck6.xRot) * 5.0f;
        this.TopJaw.y = n2;
        this.RightEye.y = n2;
        this.LeftEye.y = n2;
        this.BottomJaw.y = n2;
        newangle = (float) Math.toRadians(f3) * 0.5f;
        final float n3 = newangle;
        this.TopJaw.yRot = n3;
        this.RightEye.yRot = n3;
        this.LeftEye.yRot = n3;
        this.BottomJaw.yRot = n3;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.17f;
            this.BottomJaw.xRot = 0.45f + newangle;
        } else {
            newangle = Mth.cos(f2 * 0.2f * this.wingspeed) * 3.1415927f * 0.05f;
            this.BottomJaw.xRot = 0.17f + newangle;
        }
    }

    /** Draw order of {@code render()}, which is the creation order of {@link SeaMonsterGeometry#PARTS}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
