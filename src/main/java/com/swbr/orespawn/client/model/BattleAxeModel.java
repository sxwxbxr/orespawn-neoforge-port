package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BattleAxeGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelBattleAxe} (ModelBattleAxe.java:5-127): the 15-box double
 * axe held by the Battle Axe item. The geometry (boxes, pivots, rotations, UVs, 128x64 texture,
 * :23-101) is the generated {@link BattleAxeGeometry}; this class only bakes it and renders the
 * parts in the order of the original {@code render()} (:103-120). Nothing is animated.
 *
 * <p>Units: the original rendered every part with {@code f5 = 1.0} (:104), one model unit per
 * GL unit. {@link ModelPart#render} divides by 16, so the caller scales by 16 before rendering
 * (see {@code client.item.BigWeaponRenderer}); the numbers here stay the original ones.
 */
public class BattleAxeModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "battleaxe"), "main");

    /** Every part, in the order {@code render()} drew them (ModelBattleAxe.java:105-119). */
    private final ModelPart[] parts;

    public BattleAxeModel(ModelPart root) {
        // Plain textured boxes with alpha (the 1.7.10 default); no-cull because the left-hand
        // mirror in the renderer flips the winding order.
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[BattleAxeGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(BattleAxeGeometry.PARTS[i]);
        }
    }

    /** {@code render()} (ModelBattleAxe.java:103-120). */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
