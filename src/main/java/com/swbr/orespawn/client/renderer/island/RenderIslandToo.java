package com.swbr.orespawn.client.renderer.island;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.IslandModel;
import com.swbr.orespawn.entity.island.IslandToo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderIslandToo} (RenderIslandToo.java:10-49): {@link RenderIsland} with the red
 * {@code IslandToo.png}. Built as {@code new RenderIslandToo(new ModelIsland(1.0f), 0.25f, 1.0f)} (manifest), so the
 * registration is {@code (ctx, 0.25f, 1.0f, 1.0f)}. Kept as its own class like the original rather than one renderer
 * with two textures, so every original file maps to one port file.
 */
public class RenderIslandToo extends MobRenderer<IslandToo, IslandModel<IslandToo>> {

    /** {@code IslandToo.png} (:47), lower-cased by the asset generator. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/islandtoo.png");

    protected IslandModel<IslandToo> model;
    private float scale = 1.0f;

    /** {@code RenderIslandToo(model, par2, par3)} (:15-20). */
    public RenderIslandToo(final EntityRendererProvider.Context context, final float par2, final float par3, final float modelArg) {
        super(context, new IslandModel<>(context.bakeLayer(IslandModel.LAYER), modelArg), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final IslandToo par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final IslandToo entity) {
        return texture;
    }
}
