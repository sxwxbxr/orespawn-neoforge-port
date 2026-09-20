package com.swbr.orespawn.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.KyuubiModel;
import com.swbr.orespawn.entity.monster.Kyuubi;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderKyuubi} (RenderKyuubi.java:9-49), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:62: {@code new RenderKyuubi(new ModelKyuubi(0.5f), 0.1f, 1.0f)} - register as
 * {@code ctx -> new RenderKyuubi(ctx, 0.1f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * {@code Kyuubi.png}. The translucency and the half turn are the model's (R8).
 */
public class RenderKyuubi extends MobRenderer<Kyuubi, KyuubiModel> {

    /** {@code Kyuubi.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/kyuubi.png");

    protected KyuubiModel model;
    private float scale = 1.0f;

    /** {@code RenderKyuubi(model, par2, par3)} (:15-20). */
    public RenderKyuubi(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new KyuubiModel(context.bakeLayer(KyuubiModel.LAYER), 0.5f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Kyuubi par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Kyuubi entity) {
        return texture;
    }
}
