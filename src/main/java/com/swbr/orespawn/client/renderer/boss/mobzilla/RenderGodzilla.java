package com.swbr.orespawn.client.renderer.boss.mobzilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GodzillaModel;
import com.swbr.orespawn.entity.boss.mobzilla.Godzilla;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGodzilla} (RenderGodzilla.java:9-53), a {@code RenderLiving}. ClientProxyOreSpawn:
 * {@code new RenderGodzilla(new ModelGodzilla(0.2f), 1.0f, 2.0f)} - register as {@code ctx -> new RenderGodzilla(ctx, 1.0f,
 * 2.0f)}. Shadow {@code par2 * par3} = 2.0 (not reduced with {@code PlayNicely}, as in the original), scale 2.0, a quarter
 * of it while DataWatcher 21 {@code PlayNicely} is set.
 */
public class RenderGodzilla extends MobRenderer<Godzilla, GodzillaModel> {

    /** {@code Godzillatexture.png} (:51), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/godzillatexture.png");

    protected GodzillaModel model;
    private float scale = 1.0f;

    /** {@code RenderGodzilla(model, par2, par3)} (:15-20). */
    public RenderGodzilla(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new GodzillaModel(context.bakeLayer(GodzillaModel.LAYER), 0.2f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-44): a quarter size while {@code getPlayNicely() != 0} (R8). */
    @Override
    protected void scale(final Godzilla par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.getPlayNicely() != 0) {
            poseStack.scale(this.scale / 4.0f, this.scale / 4.0f, this.scale / 4.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-48). */
    @Override
    public ResourceLocation getTextureLocation(final Godzilla entity) {
        return texture;
    }
}
