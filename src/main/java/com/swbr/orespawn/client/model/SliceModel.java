package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SliceGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelSlice} (ModelSlice.java:5-120): the 14-box fan sword with
 * four blades at +-20 degrees around Y. One model, two items: {@code RenderSlice} draws it with
 * {@code slicetexture.png} ({@code slicesmall}), {@code RenderRoyal} with {@code royaltexture.png}
 * ({@code royalsmall}) - both originals constructed {@code new ModelSlice()}
 * (RenderSlice.java:15, RenderRoyal.java:15).
 *
 * <p>The geometry (:22-95, fixed Y rotations of the blades included) is the generated
 * {@link SliceGeometry}; the parts are drawn in the order of {@code render()} (:97-113), which is
 * the creation order. Nothing is animated. Units as in {@link BerthaModel}.
 */
public class SliceModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "slice"), "main");

    /** Every part, in the order {@code render()} drew them (ModelSlice.java:99-112). */
    private final ModelPart[] parts;

    public SliceModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[SliceGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(SliceGeometry.PARTS[i]);
        }
    }

    /** {@code render()} (ModelSlice.java:97-113). */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
