package com.swbr.orespawn.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.SeaMonsterModel;
import com.swbr.orespawn.entity.sea.SeaMonster;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderSeaMonster} (RenderSeaMonster.java:9-49), a {@code RenderLiving} with a
 * {@code ModelSeaMonster}. ClientProxyOreSpawn (:126): {@code (new ModelSeaMonster(0.5f), 1.0f, 1.0f)} - register as
 * {@code ctx -> new RenderSeaMonster(ctx, 1.0f, 1.0f, 0.5f)}. Shadow {@code par2 * par3} (:15), scale {@code par3} in
 * {@code preRenderCallback} (:34-40), one texture (:42-48).
 */
public class RenderSeaMonster extends MobRenderer<SeaMonster, SeaMonsterModel> {

    /** {@code SeaMonstertexture.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/seamonstertexture.png");

    protected SeaMonsterModel model;
    private float scale = 1.0f;

    /** {@code RenderSeaMonster(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderSeaMonster(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new SeaMonsterModel(context.bakeLayer(SeaMonsterModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final SeaMonster par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final SeaMonster entity) {
        return texture;
    }
}
