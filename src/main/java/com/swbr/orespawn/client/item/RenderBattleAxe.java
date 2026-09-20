package com.swbr.orespawn.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.BattleAxeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * Port of {@code danger.orespawn.RenderBattleAxe} (RenderBattleAxe.java:9-74), the in-hand
 * renderer of the Battle Axe ({@code battleaxesmall}). The shared structure is in
 * {@link BigWeaponRenderer}; this class holds the numbers.
 */
public class RenderBattleAxe extends BigWeaponRenderer<BattleAxeModel> {

    /** {@code new ResourceLocation("orespawn", "BattleAxetexture.png")} (:72), moved by the
     *  asset generator to {@code textures/entity/} (manifest texture_map). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/battleaxetexture.png");

    /** {@code glRotatef(180, 1, 0.25, 0)} (:63) - a rotation axis tilted a quarter towards y. */
    private static final Axis TILTED_X = Axis.of(new Vector3f(1.0f, 0.25f, 0.0f).normalize());

    public RenderBattleAxe() {
        super(TEXTURE);
    }

    @Override
    protected BattleAxeModel bake(EntityModelSet models) {
        return new BattleAxeModel(models.bakeLayer(BattleAxeModel.LAYER)); // :15
    }

    /** {@code renderSword(-2, -4, -6, 0.35)} (:43, :49-59): first person. */
    @Override
    protected void renderSword(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(50.0f));   // :51
        poseStack.mulPose(Axis.XP.rotationDegrees(190.0f));  // :52
        poseStack.mulPose(Axis.ZP.rotationDegrees(15.0f));   // :53
        renderModel(poseStack, buffer, packedLight, packedOverlay, -2.0f, -4.0f, -6.0f, 0.35f); // :54-57
    }

    /** {@code renderSwordF5(3, -8, -2, 0.35)} (:39, :61-69): third person. */
    @Override
    protected void renderSwordF5(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(TILTED_X.rotationDegrees(180.0f)); // :63
        renderModel(poseStack, buffer, packedLight, packedOverlay, 3.0f, -8.0f, -2.0f, 0.35f); // :64-67
    }
}
