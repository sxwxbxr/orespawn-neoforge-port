package com.swbr.orespawn.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.LeafMonsterModel;
import com.swbr.orespawn.entity.monster.LeafMonster;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderLeafMonster} (RenderLeafMonster.java:9-49), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:102: {@code new RenderLeafMonster(new ModelLeafMonster(), 0.65f, 1.0f)} - register as
 * {@code ctx -> new RenderLeafMonster(ctx, 0.65f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * {@code LeafMonstertexture.png}.
 */
public class RenderLeafMonster extends MobRenderer<LeafMonster, LeafMonsterModel> {

    /** {@code LeafMonstertexture.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/leafmonstertexture.png");

    protected LeafMonsterModel model;
    private float scale = 1.0f;

    /** {@code RenderLeafMonster(model, par2, par3)} (:15-20). */
    public RenderLeafMonster(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new LeafMonsterModel(context.bakeLayer(LeafMonsterModel.LAYER)), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final LeafMonster par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final LeafMonster entity) {
        return texture;
    }
}
