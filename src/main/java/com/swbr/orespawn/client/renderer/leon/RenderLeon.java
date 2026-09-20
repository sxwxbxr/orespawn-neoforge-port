package com.swbr.orespawn.client.renderer.leon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.LeonModel;
import com.swbr.orespawn.entity.leon.Leon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderLeon} (RenderLeon.java:9-49), a {@code RenderLiving} with a {@code ModelLeon}.
 * ClientProxyOreSpawn.java:130: {@code new RenderLeon(new ModelLeon(0.22f), 1.0f, 1.75f)} - register as
 * {@code ctx -> new RenderLeon(ctx, 1.0f, 1.75f)}; the model's argument is fixed here (PitchBlack precedent). Shadow
 * {@code par2 * par3} (:16), scale {@code par3} (:34-40), one texture {@code Leon.png} (:42-48).
 */
public class RenderLeon extends MobRenderer<Leon, LeonModel> {

    /** {@code Leon.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py (manifest texture_map). */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/leon.png");

    protected LeonModel model;
    private float scale = 1.0f;

    /** {@code RenderLeon(model, par2, par3)} (:15-20). */
    public RenderLeon(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new LeonModel(context.bakeLayer(LeonModel.LAYER), 0.22f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Leon par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Leon entity) {
        return texture;
    }
}
