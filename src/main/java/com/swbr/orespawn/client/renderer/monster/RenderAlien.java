package com.swbr.orespawn.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.AlienModel;
import com.swbr.orespawn.entity.monster.Alien;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderAlien} (RenderAlien.java:9-49), a {@code RenderLiving}. ClientProxyOreSpawn.java:65:
 * {@code new RenderAlien(new ModelAlien(0.22f), 0.35f, 1.1f)} - register as {@code ctx -> new RenderAlien(ctx, 0.35f, 1.1f)};
 * the model's argument is fixed here. Shadow {@code par2 * par3}, scale {@code par3}, one texture {@code MyAlien.png}.
 */
public class RenderAlien extends MobRenderer<Alien, AlienModel> {

    /** {@code MyAlien.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/myalien.png");

    protected AlienModel model;
    private float scale = 1.0f;

    /** {@code RenderAlien(model, par2, par3)} (:15-20). */
    public RenderAlien(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new AlienModel(context.bakeLayer(AlienModel.LAYER), 0.22f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Alien par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Alien entity) {
        return texture;
    }
}
