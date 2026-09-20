package com.swbr.orespawn.client.renderer.insect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.MosquitoModel;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderMosquito} (RenderMosquito.java:9-49), a {@code RenderLiving} with a
 * {@code ModelMosquito}. ClientProxyOreSpawn: {@code (new ModelMosquito(), 0.3f, 0.5f)} - register as
 * {@code ctx -> new RenderMosquito(ctx, 0.3f, 0.5f)}. Shadow {@code par2 * par3} (:16), scale {@code par3}
 * (:34-40), one texture (:42-48).
 */
public class RenderMosquito extends MobRenderer<EntityMosquito, MosquitoModel> {

    /** {@code texture} (:46-48), {@code mosquito.png}. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/mosquito.png");

    protected MosquitoModel model;
    private float scale = 1.0f;

    /** {@code RenderMosquito(model, par2, par3)} (:15-20). */
    public RenderMosquito(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new MosquitoModel(context.bakeLayer(MosquitoModel.LAYER)), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final EntityMosquito par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final EntityMosquito entity) {
        return texture;
    }
}
