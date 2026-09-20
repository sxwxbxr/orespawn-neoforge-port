package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.FairyGeometry;
import com.swbr.orespawn.entity.fairy.Fairy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelFairy} (ModelFairy.java:9-160): body, legs, arms and four wings, geometry
 * {@link FairyGeometry} (64x64). {@code wingspeed} is the constructor argument, 1.5 in ClientProxyOreSpawn
 * (manifest {@code model_args}).
 *
 * <p>The light pass of {@code render()} (:131-148): the four wings are drawn first with the normal lightmap; then
 * the original set the lightmap to ({@code fly.getBlink()}, 240) and {@code glColor4f(1, 1, 1, 1)} and drew head,
 * body, legs and arms. In 1.21.1 both are per-part arguments (same translation as {@code FireflyModel}): the body
 * parts get a packed light with block coordinate {@code getBlink()} (240 = full glow, 0 = sky light only) and sky
 * coordinate 240, and opaque white - so, as the colour reset did, they ignore the renderer's colour.
 */
public class FairyModel extends EntityModel<Fairy> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "fairy"), "main");

    private final float wingspeed;
    private final ModelPart head;
    private final ModelPart chest;
    private final ModelPart waist;
    private final ModelPart hips;
    private final ModelPart lleg1;
    private final ModelPart lleg2;
    private final ModelPart rleg;
    private final ModelPart b1;
    private final ModelPart b2;
    private final ModelPart larm;
    private final ModelPart rarm;
    private final ModelPart lwing2;
    private final ModelPart lwing1;
    private final ModelPart rwing2;
    private final ModelPart rwing1;

    /** {@code onoff}: {@code fly.getBlink()} read in {@code render()} (:135), carried from {@link #setupAnim}. */
    private float onoff = 0.0f;

    /** {@code ModelFairy(float f1)} (:28-108). */
    public FairyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.head = root.getChild(FairyGeometry.HEAD);
        this.chest = root.getChild(FairyGeometry.CHEST);
        this.waist = root.getChild(FairyGeometry.WAIST);
        this.hips = root.getChild(FairyGeometry.HIPS);
        this.lleg1 = root.getChild(FairyGeometry.LLEG1);
        this.lleg2 = root.getChild(FairyGeometry.LLEG2);
        this.rleg = root.getChild(FairyGeometry.RLEG);
        this.b1 = root.getChild(FairyGeometry.B1);
        this.b2 = root.getChild(FairyGeometry.B2);
        this.larm = root.getChild(FairyGeometry.LARM);
        this.rarm = root.getChild(FairyGeometry.RARM);
        this.lwing2 = root.getChild(FairyGeometry.LWING2);
        this.lwing1 = root.getChild(FairyGeometry.LWING1);
        this.rwing2 = root.getChild(FairyGeometry.RWING2);
        this.rwing1 = root.getChild(FairyGeometry.RWING1);
        for (final String hidden : FairyGeometry.HIDDEN) {
            root.getChild(hidden).visible = false;
        }
    }

    /** The writes of {@code render()} (:114-130) and the blink read (:135). */
    @Override
    public void setupAnim(final Fairy fly, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lwing1.resetPose();
        this.rwing1.resetPose();
        this.lwing2.resetPose();
        this.rwing2.resetPose();
        this.head.resetPose();
        this.larm.resetPose();
        this.rarm.resetPose();
        this.lwing1.yRot = -0.6f + Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.35f;
        this.rwing1.yRot = -2.55f - Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.35f;
        this.lwing2.yRot = -0.6f + Mth.cos(f2 * this.wingspeed * 0.85f) * 3.1415927f * 0.25f;
        this.rwing2.yRot = -2.55f - Mth.cos(f2 * this.wingspeed * 0.85f) * 3.1415927f * 0.25f;
        this.head.yRot = (float) Math.toRadians(f3) * 0.45f;
        if (this.head.yRot > 0.45f) {
            this.head.yRot = 0.45f;
        }
        if (this.head.yRot < -0.45f) {
            this.head.yRot = -0.45f;
        }
        this.head.xRot = (float) Math.toRadians(f4);
        this.larm.xRot = -0.2f + Mth.cos(f2 * this.wingspeed * 0.15f) * 3.1415927f * 0.05f;
        this.rarm.xRot = -0.2f + Mth.cos(f2 * this.wingspeed * 0.12f) * 3.1415927f * 0.05f;
        this.larm.zRot = -0.15f + Mth.cos(f2 * this.wingspeed * 0.1f) * 3.1415927f * 0.03f;
        this.rarm.zRot = 0.15f + Mth.cos(f2 * this.wingspeed * 0.11f) * 3.1415927f * 0.03f;
        this.onoff = fly.getBlink();
    }

    /** Draw order of {@code render()} (:131-148): wings normally lit, then the body lit by the blink. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.lwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        // OpenGlHelper.setLightmapTextureCoords(lightmapTexUnit, onoff, 240): the low 16 bits of a packed light are
        // the block coordinate, the high 16 bits the sky coordinate.
        final int bodyLight = ((int) this.onoff) | (240 << 16);
        // GL11.glColor4f(1, 1, 1, 1)
        final int white = -1;
        this.head.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.chest.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.waist.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.hips.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.lleg1.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.lleg2.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.rleg.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.b1.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.b2.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.larm.render(poseStack, buffer, bodyLight, packedOverlay, white);
        this.rarm.render(poseStack, buffer, bodyLight, packedOverlay, white);
    }
}
