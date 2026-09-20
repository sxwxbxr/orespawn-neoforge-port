package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.FireflyGeometry;
import com.swbr.orespawn.entity.insect.Firefly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelFirefly} (ModelFirefly.java:9-125): body, two wings, head, mouth,
 * eyes, four legs and the {@code TailLight}, geometry {@link FireflyGeometry} (64x128). {@code wingspeed}
 * is the constructor argument, 2.5 in ClientProxyOreSpawn (manifest {@code model_args}).
 *
 * <p>The light pass of {@code render()} (:110-113): before the tail light the original set the lightmap
 * to ({@code getBlink()}, 240) and {@code glColor4f(1, 1, 1, 1)}. In 1.21.1 both are per-part arguments:
 * the tail light is drawn with a packed light of block coordinate {@code getBlink()} (240 = full, 0 =
 * dark) and sky coordinate 240, and with opaque white - so, exactly as the colour reset did, it ignores
 * the renderer's colour (the 15 % alpha of an invisible firefly a player can see).
 */
public class FireflyModel extends EntityModel<Firefly> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "firefly"), "main");

    private final float wingspeed;
    private final ModelPart body;
    private final ModelPart wing_left;
    private final ModelPart wing_right;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart eye_left;
    private final ModelPart eye_right;
    private final ModelPart front_leg_left_;
    private final ModelPart front_leg_right;
    private final ModelPart back_leg_left;
    private final ModelPart back_leg_right;
    private final ModelPart TailLight;

    /** {@code onoff}: {@code fly.getBlink()} read in {@code render()} (:110), carried from {@link #setupAnim}. */
    private float onoff = 0.0f;

    /** {@code ModelFirefly(float f1)} (:25-90). */
    public FireflyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(FireflyGeometry.BODY);
        this.wing_left = root.getChild(FireflyGeometry.WING_LEFT);
        this.wing_right = root.getChild(FireflyGeometry.WING_RIGHT);
        this.head = root.getChild(FireflyGeometry.HEAD);
        this.mouth = root.getChild(FireflyGeometry.MOUTH);
        this.eye_left = root.getChild(FireflyGeometry.EYE_LEFT);
        this.eye_right = root.getChild(FireflyGeometry.EYE_RIGHT);
        this.front_leg_left_ = root.getChild(FireflyGeometry.FRONT_LEG_LEFT_);
        this.front_leg_right = root.getChild(FireflyGeometry.FRONT_LEG_RIGHT);
        this.back_leg_left = root.getChild(FireflyGeometry.BACK_LEG_LEFT);
        this.back_leg_right = root.getChild(FireflyGeometry.BACK_LEG_RIGHT);
        this.TailLight = root.getChild(FireflyGeometry.TAIL_LIGHT);
    }

    /**
     * The writes of {@code render()} (:97-98, :110). Only {@code zRot} of the wings is overwritten; the
     * left wing's rest {@code yRot} 0.0174533 stays, which {@code resetPose} restores first.
     */
    @Override
    public void setupAnim(final Firefly entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.wing_left.resetPose();
        this.wing_right.resetPose();
        this.wing_left.zRot = 1.11f + Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.35f;
        this.wing_right.zRot = -1.11f - Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.35f;
        this.onoff = entity.getBlink();
    }

    /** Draw order of {@code render()} (:99-113); the tail light last, lit by the blink. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wing_left.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wing_right.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.eye_left.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.eye_right.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.front_leg_left_.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.front_leg_right.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.back_leg_left.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.back_leg_right.render(poseStack, buffer, packedLight, packedOverlay, color);
        // OpenGlHelper.setLightmapTextureCoords(lightmapTexUnit, onoff, 240): the low 16 bits of a packed
        // light are the block coordinate, the high 16 bits the sky coordinate.
        final int tailLight = ((int) this.onoff) | (240 << 16);
        // GL11.glColor4f(1, 1, 1, 1)
        this.TailLight.render(poseStack, buffer, tailLight, packedOverlay, -1);
    }
}
