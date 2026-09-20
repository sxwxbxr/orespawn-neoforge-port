package com.swbr.orespawn.client.renderer.boss.kraken;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.KrakenModel;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderKraken} (RenderKraken.java:9-53), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:74: {@code new RenderKraken(new ModelKraken(1.0f), 1.0f, 1.0f)} - register as
 * {@code ctx -> new RenderKraken(ctx, 1.0f, 1.0f)}. Shadow {@code par2 * par3} = 1.0 (not reduced with
 * {@code PlayNicely}, as in the original), scale 1.0, a third of it while DataWatcher 21 {@code PlayNicely} is set.
 */
public class RenderKraken extends MobRenderer<Kraken, KrakenModel> {

    /** {@code Kraken.png} (:51), lower-cased into {@code textures/entity/} by tools/assets.py (manifest texture_map). */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/kraken.png");

    protected KrakenModel model;
    private float scale = 1.0f;

    /** {@code RenderKraken(model, par2, par3)} (:15-20); the model is {@code new ModelKraken(1.0f)}. */
    public RenderKraken(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new KrakenModel(context.bakeLayer(KrakenModel.LAYER), 1.0f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-44): a third of the size while {@code getPlayNicely() != 0} (R8). */
    @Override
    protected void scale(final Kraken par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.getPlayNicely() != 0) {
            poseStack.scale(this.scale / 3.0f, this.scale / 3.0f, this.scale / 3.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final Kraken entity) {
        return texture;
    }
}
