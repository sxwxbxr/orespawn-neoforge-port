package com.swbr.orespawn.client.item.squidzooka;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.item.BigWeaponRenderer;
import com.swbr.orespawn.client.model.SquidZookaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderSquidZooka} (RenderSquidZooka.java:9-72), the in-hand renderer of the
 * SquidZooka ({@code squidzookasmall}). The same {@code IItemRenderer} shape as the W03 big weapons -
 * {@code EQUIPPED} and {@code EQUIPPED_FIRST_PERSON} only (:18-30), render helper always (:32-34), rotate, scale,
 * translate, draw (:49-67) - so the shared frame is {@link BigWeaponRenderer} and this class holds the numbers. The
 * item model {@code squidzookasmall.json} uses the same {@code bigweapon_hand} display transforms.
 */
public class RenderSquidZooka extends BigWeaponRenderer<SquidZookaModel> {

    /** {@code new ResourceLocation("orespawn", "SquidZookatexture.png")} (:70), moved to {@code textures/entity/} (manifest texture_map). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/squidzookatexture.png");

    public RenderSquidZooka() {
        super(TEXTURE);
    }

    @Override
    protected SquidZookaModel bake(final EntityModelSet models) {
        return new SquidZookaModel(models.bakeLayer(SquidZookaModel.LAYER)); // :15
    }

    /** {@code renderSword(4, 2, 2, 0.35)} (:43, :49-57): first person. */
    @Override
    protected void renderSword(final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-30.0f)); // :51
        renderModel(poseStack, buffer, packedLight, packedOverlay, 4.0f, 2.0f, 2.0f, 0.35f); // :52-55
    }

    /** {@code renderSwordF5(2, 8, 2, 0.35)} (:39, :59-67): third person. */
    @Override
    protected void renderSwordF5(final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(30.0f)); // :61
        renderModel(poseStack, buffer, packedLight, packedOverlay, 2.0f, 8.0f, 2.0f, 0.35f); // :62-65
    }
}
