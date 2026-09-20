package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TrooperBugModel;
import com.swbr.orespawn.entity.arthropod.TrooperBug;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTrooperBug} (RenderTrooperBug.java:9-49), the Jumpy Bug. ClientProxyOreSpawn
 * registered it as {@code new RenderTrooperBug(new ModelTrooperBug(0.22f), 0.95f, 1.1f)} (manifest): register with
 * {@code ctx -> new RenderTrooperBug(ctx, 0.95f, 1.1f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code TrooperBug.png}, lower-cased).
 */
public class RenderTrooperBug extends MobRenderer<TrooperBug, TrooperBugModel> {

    protected TrooperBugModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/trooperbug.png");

    /** {@code RenderTrooperBug(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderTrooperBug(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new TrooperBugModel(context.bakeLayer(TrooperBugModel.LAYER), 0.22f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final TrooperBug par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final TrooperBug entity) {
        return RenderTrooperBug.texture;
    }
}
