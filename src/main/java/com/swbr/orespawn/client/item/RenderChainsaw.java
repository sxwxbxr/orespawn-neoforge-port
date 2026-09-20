package com.swbr.orespawn.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ChainsawModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderChainsaw} (RenderChainsaw.java:9-75), the in-hand
 * renderer of the Chainsaw ({@code chainsawsmall}). The shared structure is in
 * {@link BigWeaponRenderer}; this class holds the numbers and feeds the model its clock.
 *
 * <p>The original model advanced its chain and sprocket once per {@code render()} call; the
 * port binds them to game time plus partial tick (DECISIONS R18, see {@link ChainsawModel}).
 * The animation ran whenever the item was drawn in a hand, also without swinging - that stays.
 */
public class RenderChainsaw extends BigWeaponRenderer<ChainsawModel> {

    /** {@code new ResourceLocation("orespawn", "Chainsawtexture.png")} (:73), moved by the
     *  asset generator to {@code textures/entity/} (manifest texture_map). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/chainsawtexture.png");

    public RenderChainsaw() {
        super(TEXTURE);
    }

    @Override
    protected ChainsawModel bake(EntityModelSet models) {
        return new ChainsawModel(models.bakeLayer(ChainsawModel.LAYER)); // :15
    }

    /** The clock of the animation: client game time plus partial tick, in original frames. */
    @Override
    protected void animate(ChainsawModel model) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        long gameTime = level == null ? 0L : level.getGameTime();
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        model.setupAnim(ChainsawModel.frames(gameTime, partialTick));
    }

    /** {@code renderSword(-10, 1, -4, 0.25)} (:43, :49-58): first person. */
    @Override
    protected void renderSword(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YP.rotationDegrees(150.0f));  // :51
        poseStack.mulPose(Axis.XP.rotationDegrees(100.0f));  // :52
        renderModel(poseStack, buffer, packedLight, packedOverlay, -10.0f, 1.0f, -4.0f, 0.25f); // :53-56
    }

    /** {@code renderSwordF5(-3, -3, -2, 0.25)} (:39, :60-70): third person. */
    @Override
    protected void renderSwordF5(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));  // :62
        poseStack.mulPose(Axis.YP.rotationDegrees(-20.0f));  // :63
        poseStack.mulPose(Axis.XP.rotationDegrees(-20.0f));  // :64
        renderModel(poseStack, buffer, packedLight, packedOverlay, -3.0f, -3.0f, -2.0f, 0.25f); // :65-68
    }
}
