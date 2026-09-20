package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.QueenBattleAxeGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.ModelQueenBattleAxe} (ModelQueenBattleAxe.java:5-85): the
 * 9-box axe with four fanned blade plates held by the Queen Scale Battle Axe item. The geometry
 * (:17-65) is the generated {@link QueenBattleAxeGeometry}; this class bakes it and renders the
 * parts in the order of the original {@code render()} (:67-78). Nothing is animated.
 *
 * <p>Units as in {@link BattleAxeModel}: the original rendered with {@code f5 = 1.0} (:68), the
 * caller scales by 16 to undo {@link ModelPart}'s division.
 */
public class QueenBattleAxeModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "queenbattleaxe"), "main");

    /** Every part, in the order {@code render()} drew them (ModelQueenBattleAxe.java:69-77). */
    private final ModelPart[] parts;

    public QueenBattleAxeModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[QueenBattleAxeGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = root.getChild(QueenBattleAxeGeometry.PARTS[i]);
        }
    }

    /** {@code render()} (ModelQueenBattleAxe.java:67-78). */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
