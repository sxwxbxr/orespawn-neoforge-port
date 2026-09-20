package com.swbr.orespawn.client.renderer.boss.king;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TheKingModel;
import com.swbr.orespawn.entity.boss.king.TheKing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTheKing} (RenderTheKing.java:9-53), a {@code RenderLiving}.
 * ClientProxyOreSpawn: {@code new RenderTheKing(new ModelTheKing(0.65f), 1.9f, 2.1f)} (manifest {@code renderer_args},
 * {@code model_args}) - register as {@code ctx -> new RenderTheKing(ctx, 1.9f, 2.1f)}; the model's argument is fixed
 * here. Shadow {@code par2 * par3} = 3.99, one texture {@code TheKingtexture.png} (2048x2048).
 *
 * <p>{@code preRenderScale} (:34-40): {@code scale / 4} while the King's {@code PlayNicely} watcher is set, otherwise
 * {@code scale}. The ten translucent wing membranes of the model are {@link WingPass}.
 *
 * <p>{@code TheKing.isInRangeToRenderDist/Vec3D} return {@code true} (TheKing.java:100-108): no distance limit and no
 * frustum test for a 90-block model whose hitbox is a quarter of it. The distance half is the entity's
 * {@code shouldRenderAtSqrDistance}; the frustum half is {@link #shouldRender}.
 */
public class RenderTheKing extends MobRenderer<TheKing, TheKingModel> {

    /** {@code TheKingtexture.png} (:51), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/thekingtexture.png");

    protected TheKingModel model;
    private float scale;

    /** {@code RenderTheKing(ModelTheKing, float, float)} (:15-20). */
    public RenderTheKing(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new TheKingModel(context.bakeLayer(TheKingModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new WingPass(this));
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40) (R8). */
    @Override
    protected void scale(final TheKing par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.getPlayNicely() != 0) {
            poseStack.scale(this.scale / 4.0f, this.scale / 4.0f, this.scale / 4.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code isInRangeToRenderVec3D} of the entity (TheKing.java:105-108): never culled by the frustum. */
    @Override
    public boolean shouldRender(final TheKing livingEntity, final Frustum camera, final double camX, final double camY,
                                final double camZ) {
        return true;
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final TheKing entity) {
        return texture;
    }

    /**
     * The blend block at the end of {@code ModelTheKing.render} (ModelTheKing.java:1189-1205) as a layer: the ten wing
     * membranes, already posed by the main pass's {@code setupAnim}, into {@link RenderType#entityTranslucent} with the
     * model's fixed colour, the renderer's light and hurt overlay.
     *
     * <p>1.7.10 skipped the whole {@code render()} for an entity invisible to the viewer; the layer does the same
     * (layers run in 1.21.1 whether or not the body was drawn).
     */
    public static class WingPass extends RenderLayer<TheKing, TheKingModel> {

        public WingPass(final RenderLayerParent<TheKing, TheKingModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final TheKing entity, final float limbSwing, final float limbSwingAmount, final float partialTick,
                           final float ageInTicks, final float netHeadYaw, final float headPitch) {
            if (entity.isInvisibleTo(Minecraft.getInstance().player)) {
                return;
            }
            final VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
            this.getParentModel().renderWings(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0f));
        }
    }
}
