package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SquidZookaGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelSquidZooka} (ModelSquidZooka.java:6-110): the 12-box bazooka of the SquidZooka
 * ({@code squidzookasmall}) - barrel, seven tail rings, a three-part sight and a grip - on a 128x128 texture,
 * geometry {@link SquidZookaGeometry}. Nothing is animated. {@code render()} turns the whole model 180 degrees about
 * Z inside its own push/pop (:94-108); units as in {@link BerthaModel}.
 */
public class SquidZookaModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "squidzooka"), "main");

    /** Every part, in the order {@code render()} drew them (the creation order). */
    private final ModelPart[] parts;

    public SquidZookaModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[SquidZookaGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(SquidZookaGeometry.PARTS[i]);
        }
    }

    /** {@code render()} (:93-109): {@code glPushMatrix; glRotatef(180, 0, 0, 1)}, the twelve parts, {@code glPopMatrix}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        poseStack.popPose();
    }
}
