package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.RockBaseGeometry;
import com.swbr.orespawn.entity.rock.RockBase;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * Port of {@code danger.orespawn.ModelRockBase} (ModelRockBase.java:8-211): 22 boxes, of which only
 * the ones of the current rock type are drawn (:151-200). Geometry (boxes, pivots, rotations, UVs,
 * 64x64, :34-149) is the generated {@link RockBaseGeometry}. Nothing is animated: the original
 * {@code setRotationAngles} only called the empty {@code ModelBase} one (:208-210), so there is no
 * pose to reset.
 *
 * <table>
 * <tr><th>type</th><th>parts</th></tr>
 * <tr><td>1</td><td>RockSmallShape1, RockSmallShape2 (:160-163)</td></tr>
 * <tr><td>7</td><td>RockSpikeyShape1-3 (:164-168)</td></tr>
 * <tr><td>8</td><td>RockTNTShape1-4 (:169-174)</td></tr>
 * <tr><td>9-12</td><td>CrystalShape1, 2, 3a-d, 4a-d, translucent (:175-194)</td></tr>
 * <tr><td>2-6</td><td>RockShape1-3 (:195-199)</td></tr>
 * <tr><td>other</td><td>nothing (:157-159)</td></tr>
 * </table>
 *
 * <p>The crystal block (:176-193) set {@code GL_BLEND} with {@code SRC_ALPHA, ONE_MINUS_SRC_ALPHA},
 * the lightmap to 240/240 and {@code glColor4f(0.75, 0.75, 0.75, 0.55)}. Per DECISIONS R8 that is a
 * translucent render type - chosen by {@code RenderRockBase.getRenderType} for types 9-12, because
 * a model only receives the one buffer its renderer opened - plus full-bright light and the ARGB
 * colour {@code 0x8CBFBFBF} here. {@code glColor4f} replaced the colour, so the incoming colour is
 * not multiplied in. PORT: the original never reset {@code glColor4f} after the block, so whatever
 * the renderer drew next inherited the tint; that GL state leak has no 1.21.1 counterpart.
 *
 * <p>The {@code setTextureSize(64, 32)} on RockShape1 (:41) and every {@code mirror = true} came after
 * {@code addBox} and changed nothing in 1.7.10, where a box baked its UVs on creation; the geometry
 * generator reproduces that.
 */
public class RockBaseModel extends EntityModel<RockBase> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "rock"), "main");

    /** {@code OpenGlHelper.setLightmapTextureCoords(lightmapTexUnit, 240, 240)} (:180). */
    private static final int CRYSTAL_LIGHT = LightTexture.FULL_BRIGHT;
    /** {@code glColor4f(0.75f, 0.75f, 0.75f, 0.55f)} (:181). */
    private static final int CRYSTAL_COLOR = FastColor.ARGB32.colorFromFloat(0.55f, 0.75f, 0.75f, 0.75f);

    /** {@code wingspeed} (:10, :35-36): set from the constructor argument and never read. */
    @SuppressWarnings("unused")
    private final float wingspeed;
    private final ModelPart rockShape1;
    private final ModelPart rockShape2;
    private final ModelPart rockShape3;
    private final ModelPart rockSmallShape2;
    private final ModelPart rockSmallShape1;
    private final ModelPart rockTNTShape1;
    private final ModelPart rockTNTShape2;
    private final ModelPart rockTNTShape3;
    private final ModelPart rockTNTShape4;
    private final ModelPart rockSpikeyShape1;
    private final ModelPart rockSpikeyShape2;
    private final ModelPart rockSpikeyShape3;
    private final ModelPart crystalShape1;
    private final ModelPart crystalShape2;
    private final ModelPart crystalShape3a;
    private final ModelPart crystalShape3b;
    private final ModelPart crystalShape3c;
    private final ModelPart crystalShape3d;
    private final ModelPart crystalShape4a;
    private final ModelPart crystalShape4b;
    private final ModelPart crystalShape4c;
    private final ModelPart crystalShape4d;

    /** The type read in {@link #setupAnim} for the following {@link #renderToBuffer} (:152-156). */
    private int rt;

    /** {@code ModelRockBase(float f1)} (:34-149); the renderer passes 1.0 (ClientProxyOreSpawn, manifest {@code model_args}). */
    public RockBaseModel(final ModelPart root, final float f1) {
        this.wingspeed = f1;
        this.rockShape1 = root.getChild(RockBaseGeometry.ROCK_SHAPE1);
        this.rockShape2 = root.getChild(RockBaseGeometry.ROCK_SHAPE2);
        this.rockShape3 = root.getChild(RockBaseGeometry.ROCK_SHAPE3);
        this.rockSmallShape2 = root.getChild(RockBaseGeometry.ROCK_SMALL_SHAPE2);
        this.rockSmallShape1 = root.getChild(RockBaseGeometry.ROCK_SMALL_SHAPE1);
        this.rockTNTShape1 = root.getChild(RockBaseGeometry.ROCK_TNTSHAPE1);
        this.rockTNTShape2 = root.getChild(RockBaseGeometry.ROCK_TNTSHAPE2);
        this.rockTNTShape3 = root.getChild(RockBaseGeometry.ROCK_TNTSHAPE3);
        this.rockTNTShape4 = root.getChild(RockBaseGeometry.ROCK_TNTSHAPE4);
        this.rockSpikeyShape1 = root.getChild(RockBaseGeometry.ROCK_SPIKEY_SHAPE1);
        this.rockSpikeyShape2 = root.getChild(RockBaseGeometry.ROCK_SPIKEY_SHAPE2);
        this.rockSpikeyShape3 = root.getChild(RockBaseGeometry.ROCK_SPIKEY_SHAPE3);
        this.crystalShape1 = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE1);
        this.crystalShape2 = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE2);
        this.crystalShape3a = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE3A);
        this.crystalShape3b = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE3B);
        this.crystalShape3c = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE3C);
        this.crystalShape3d = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE3D);
        this.crystalShape4a = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE4A);
        this.crystalShape4b = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE4B);
        this.crystalShape4c = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE4C);
        this.crystalShape4d = root.getChild(RockBaseGeometry.CRYSTAL_SHAPE4D);
    }

    /** Types 9-12 draw the translucent crystal cluster (:175). */
    public static boolean isCrystal(final int rockType) {
        return rockType >= 9 && rockType <= 12;
    }

    /** {@code render} (:152-156): the cast to {@code RockBase} and {@code getRockType()}; {@code setRotationAngles} (:208-210) is empty. */
    @Override
    public void setupAnim(final RockBase entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        this.rt = entity.getRockType();
    }

    /** {@code render} (:157-199), part order as in the original. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final int rt = this.rt;
        if (rt < 1 || rt > 12) { // :157-159
            return;
        }
        if (rt == 1) { // :160-163
            this.rockSmallShape1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockSmallShape2.render(poseStack, buffer, packedLight, packedOverlay, color);
        } else if (rt == 7) { // :164-168
            this.rockSpikeyShape1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockSpikeyShape2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockSpikeyShape3.render(poseStack, buffer, packedLight, packedOverlay, color);
        } else if (rt == 8) { // :169-174
            this.rockTNTShape1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockTNTShape2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockTNTShape3.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockTNTShape4.render(poseStack, buffer, packedLight, packedOverlay, color);
        } else if (rt >= 9 && rt <= 12) { // :175-194, glPushMatrix/glPopMatrix without a transform in between
            this.crystalShape1.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape2.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape3a.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape3b.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape3c.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape3d.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape4a.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape4b.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape4c.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
            this.crystalShape4d.render(poseStack, buffer, CRYSTAL_LIGHT, packedOverlay, CRYSTAL_COLOR);
        } else { // :195-199
            this.rockShape1.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockShape2.render(poseStack, buffer, packedLight, packedOverlay, color);
            this.rockShape3.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
