package com.swbr.orespawn.client.renderer.cow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.cow.CrystalCow;
import com.swbr.orespawn.entity.cow.EnchantedCow;
import com.swbr.orespawn.entity.cow.GoldCow;
import com.swbr.orespawn.entity.cow.RedCow;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderEnchantedCow} (RenderEnchantedCow.java:9-60): the one renderer of all four
 * OreSpawn cows. ClientProxyOreSpawn built it as {@code new RenderEnchantedCow(new ModelCow(), 0.7f)} for
 * {@code RedCow}, {@code GoldCow}, {@code EnchantedCow} and {@code CrystalCow} (:18-21); the vanilla
 * {@code ModelCow} is 1.21.1's {@link CowModel} on the vanilla cow layer, with the original textures.
 *
 * <p>No {@code preRenderCallback}, so no scale; shadow 0.7. The glint of {@code shouldRenderPass} is
 * {@link EnchantedPass}.
 */
public class RenderEnchantedCow extends MobRenderer<RedCow, CowModel<RedCow>> {

    // :55-59, lower-cased under textures/entity/ by the asset generator (manifest texture_map).
    private static final ResourceLocation texture3 = texture("crystal_cow.png");
    private static final ResourceLocation texture1 = texture("red_cow.png");
    private static final ResourceLocation texture2 = texture("gold_cow.png");

    protected CowModel<RedCow> model;

    /** The registration of ClientProxyOreSpawn.java:18-21: shadow 0.7. */
    public RenderEnchantedCow(final EntityRendererProvider.Context context) {
        this(context, 0.7f);
    }

    /** {@code RenderEnchantedCow(ModelCow, float)} (:16-19). */
    public RenderEnchantedCow(final EntityRendererProvider.Context context, final float par2) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), par2);
        this.model = this.getModel();
        this.addLayer(new EnchantedPass(this));
    }

    private static ResourceLocation texture(final String file) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + file);
    }

    /** {@code getEntityTexture} (:42-53), same order of checks. */
    @Override
    public ResourceLocation getTextureLocation(final RedCow entity) {
        if (entity instanceof EnchantedCow) {
            return texture2;
        }
        if (entity instanceof GoldCow) {
            return texture2;
        }
        if (entity instanceof CrystalCow) {
            return texture3;
        }
        return texture1;
    }

    /**
     * {@code shouldRenderPass} (:33-40) as a layer. For an {@code EnchantedCow} in pass 3 it named the main model as
     * the pass model, set the colour to white and returned 31. 1.7.10 {@code RendererLivingEntity.doRender} reads
     * that value as bit masks: {@code > 0} draws the pass model once, {@code (31 & 240) == 16} draws it a second time,
     * {@code (31 & 15) == 15} draws the enchantment glint over it - two scrolling layers of
     * {@code enchanted_item_glint.png}, additive {@code (SRC_COLOR, ONE)}, depth test {@code EQUAL}.
     *
     * <p>PORT: the two plain redraws used the texture that was still bound (the cow's own) at the same depth and add
     * nothing visible; they are not repeated. The glint is {@link RenderType#armorEntityGlint()}: the same blend,
     * depth test and scrolling glint on a model, the render type vanilla uses for enchanted armour on mobs. Its
     * vertex format has no colour, so the 1.7.10 tint {@code (0.38, 0.19, 0.61)} cannot be applied; 1.21.1's glint
     * texture carries the purple itself. Not rendered on this machine (DECISIONS R17).
     */
    public static class EnchantedPass extends RenderLayer<RedCow, CowModel<RedCow>> {

        public EnchantedPass(final RenderLayerParent<RedCow, CowModel<RedCow>> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final RedCow par1EntityLiving, final float limbSwing, final float limbSwingAmount,
                           final float partialTick, final float ageInTicks, final float netHeadYaw, final float headPitch) {
            if (!(par1EntityLiving instanceof EnchantedCow)) {
                return;
            }
            final VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorEntityGlint());
            this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
