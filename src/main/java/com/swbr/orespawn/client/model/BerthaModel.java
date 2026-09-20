package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BerthaGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelBertha} (ModelBertha.java:5-106): the 12-box great sword
 * of Big Bertha ({@code berthasmall}). The geometry (boxes, pivots, UVs, 64x128 texture, :20-83)
 * is the generated {@link BerthaGeometry}; this class only bakes it and draws the parts in the
 * order of the original {@code render()} (:85-99), which is the creation order. Nothing is
 * animated.
 *
 * <p>Units: the original rendered with {@code f5 = 1.0} (:86), one model unit per GL unit;
 * {@link ModelPart#render} divides by 16, so the caller scales by 16 first
 * ({@code client.item.BigWeaponRenderer#renderModel}). The numbers here stay the original ones.
 */
public class BerthaModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "bertha"), "main");

    /** Every part, in the order {@code render()} drew them (ModelBertha.java:87-98). */
    private final ModelPart[] parts;

    public BerthaModel(ModelPart root) {
        // Plain textured boxes with alpha; no-cull because the left-hand mirror of the renderer
        // flips the winding order (same choice as the W03 big weapons).
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[BerthaGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(BerthaGeometry.PARTS[i]);
        }
    }

    /** {@code render()} (ModelBertha.java:85-99). */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
