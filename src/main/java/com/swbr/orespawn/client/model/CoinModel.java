package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CoinGeometry;
import com.swbr.orespawn.entity.critter.Coin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCoin} (ModelCoin.java:7-42): one 256x256x1 disc on a 512x512 texture,
 * geometry {@link CoinGeometry}. {@code wingspeed} 0.22 in ClientProxyOreSpawn. No GL calls. The back face's
 * UV runs two pixels past the texture; the normalised UV wraps in 1.21.1 as it did in 1.7.10
 * (docs/research/06-models-design.md).
 */
public class CoinModel extends EntityModel<Coin> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "coin"), "main");

    private final float wingspeed;
    private final ModelPart Shape1;

    /** {@code ModelCoin(float f1)} (:11-21). */
    public CoinModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Shape1 = root.getChild(CoinGeometry.SHAPE1);
    }

    /** The write of {@code render()} (:26-29): the disc swings ±180° about Y, back and forth. */
    @Override
    public void setupAnim(final Coin entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Shape1.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.05f * this.wingspeed) * 3.1415927f;
        this.Shape1.yRot = newangle;
    }

    /** {@code render()} (:30). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
