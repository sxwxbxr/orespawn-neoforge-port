package com.swbr.orespawn.client.renderer.cannonfodder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.LizardModel;
import com.swbr.orespawn.entity.cannonfodder.EntityCannonFodder;
import com.swbr.orespawn.entity.cannonfodder.Lizard;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderLizard} (RenderLizard.java:9-68), registered as
 * {@code new RenderLizard(new ModelLizard(0.65f), 0.75f, 1.0f)} (manifest): {@code ctx -> new RenderLizard(ctx,
 * 0.65f, 0.75f, 1.0f)}.
 *
 * <p>{@code Lizard.png}, {@code Lizard2.png}, {@code Lizard3.png} differ only in the hat field at 30,40-50,50:
 * red, green, blue (lower-cased by tools/assets.py).
 */
public class RenderLizard extends MobRenderer<Lizard, LizardModel> {

    private static final ResourceLocation texture = tex("lizard");
    private static final ResourceLocation texture2 = tex("lizard2");
    private static final ResourceLocation texture3 = tex("lizard3");

    protected LizardModel model;
    private float scale;

    /**
     * {@code RenderLizard(model, par2, par3)} (:17-22): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelLizard} constructor argument
     */
    public RenderLizard(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new LizardModel(context.bakeLayer(LizardModel.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:36-46): half scale for a baby. */
    @Override
    protected void scale(final Lizard par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /**
     * PORT: {@code MobRenderer} halves the shadow of a baby and scales it by the size attribute; 1.7.10
     * {@code RenderLiving} drew the constructor shadow for every lizard.
     */
    @Override
    protected float getShadowRadius(final Lizard entity) {
        return this.shadowRadius;
    }

    /** {@code getEntityTexture} (:48-62): the hat colour, but only on a battle lizard. */
    @Override
    public ResourceLocation getTextureLocation(final Lizard entity) {
        if (entity instanceof EntityCannonFodder) {
            final EntityCannonFodder c = entity;
            if (c.get_is_activated() != 0) {
                if (c.getHatColor() == 2) {
                    return texture2;
                }
                if (c.getHatColor() == 3) {
                    return texture3;
                }
            }
        }
        return texture;
    }

    /** {@code new ResourceLocation("orespawn", name + ".png")}, moved to {@code textures/entity/} by tools/assets.py. */
    private static ResourceLocation tex(final String name) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + name + ".png");
    }
}
