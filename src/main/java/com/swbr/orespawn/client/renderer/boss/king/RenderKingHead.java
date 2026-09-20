package com.swbr.orespawn.client.renderer.boss.king;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.entity.boss.king.KingHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderKingHead} (RenderKingHead.java:8-32): a {@code RenderLiving} whose
 * {@code doRender} is empty - the head is invisible, without name tag; its shadow was {@code 0 * 0}
 * (ClientProxyOreSpawn passes {@code (null, 0, 0)}, manifest {@code renderer_args}). Register as
 * {@code RenderKingHead::new}.
 *
 * <p>PORT: {@code getEntityTexture} returned {@code null}; 1.21.1 needs a location and nothing reads it, so this
 * returns the block atlas, as vanilla's {@code NoopRenderer} does.
 */
public class RenderKingHead extends EntityRenderer<KingHead> {

    /** {@code RenderKingHead(ModelTheKing, float, float)} (:10-12): shadow {@code 0 * 0}. */
    public RenderKingHead(final EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    /** {@code doRender} (:17-21): nothing, not even the name tag. */
    @Override
    public void render(final KingHead entity, final float entityYaw, final float partialTick, final PoseStack poseStack,
                       final MultiBufferSource bufferSource, final int packedLight) {
    }

    /** {@code getEntityTexture} (:29-31). */
    @Override
    public ResourceLocation getTextureLocation(final KingHead entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
