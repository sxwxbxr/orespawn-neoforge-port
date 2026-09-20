package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.HammyGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelHammy} (ModelHammy.java:5-253): the 33-box mallet of the
 * Attitude Adjuster ({@code hammysmall}) - a six-rayed shaft, an octagonal 40-unit head, eight
 * iron bands per end and white spikes, on a 128x256 texture.
 *
 * <p>The geometry (:41-209, the fixed +-45/+-60 degree part rotations included) is the generated
 * {@link HammyGeometry}; the parts are drawn in the order of {@code render()} (:211-246), which is
 * the creation order ({@code ... Point1b, Spike2b, Spike1b, Spike3b, Spike4b}). Nothing is
 * animated. Units as in {@link BerthaModel}.
 */
public class HammyModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "hammy"), "main");

    /** Every part, in the order {@code render()} drew them (ModelHammy.java:213-245). */
    private final ModelPart[] parts;

    public HammyModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[HammyGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(HammyGeometry.PARTS[i]);
        }
    }

    /** {@code render()} (ModelHammy.java:211-246). */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
