package com.swbr.orespawn.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.HammerheadModel;
import com.swbr.orespawn.entity.monster.Hammerhead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderHammerhead} (RenderHammerhead.java:9-49), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:131: {@code new RenderHammerhead(new ModelHammerhead(0.33f), 1.0f, 2.5f)} - register as
 * {@code ctx -> new RenderHammerhead(ctx, 1.0f, 2.5f)}. Shadow {@code par2 * par3} = 2.5, scale {@code par3} = 2.5, one
 * texture {@code Hammerheadtexture.png}.
 */
public class RenderHammerhead extends MobRenderer<Hammerhead, HammerheadModel> {

    /** {@code Hammerheadtexture.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/hammerheadtexture.png");

    protected HammerheadModel model;
    private float scale = 1.0f;

    /** {@code RenderHammerhead(model, par2, par3)} (:15-20). */
    public RenderHammerhead(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new HammerheadModel(context.bakeLayer(HammerheadModel.LAYER), 0.33f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Hammerhead par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Hammerhead entity) {
        return texture;
    }
}
