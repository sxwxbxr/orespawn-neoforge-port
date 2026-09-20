package com.swbr.orespawn.client.renderer.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.BasiliskModel;
import com.swbr.orespawn.entity.dino.Basilisk;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderBasilisk} (RenderBasilisk.java:10-49). ClientProxyOreSpawn.java:50 registered
 * {@code new RenderBasilisk(new ModelBasilisk(0.3f), 0.5f, 1.25f)}: register with
 * {@code ctx -> new RenderBasilisk(ctx, 0.5f, 1.25f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, {@code glScalef(scale)}, one texture ({@code basilisk.png}).
 */
public class RenderBasilisk extends MobRenderer<Basilisk, BasiliskModel> {

    protected BasiliskModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/basilisk.png");

    /** {@code RenderBasilisk(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderBasilisk(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new BasiliskModel(context.bakeLayer(BasiliskModel.LAYER), 0.3f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale, scale, scale)} (R8). */
    @Override
    protected void scale(final Basilisk par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Basilisk entity) {
        return RenderBasilisk.texture;
    }
}
