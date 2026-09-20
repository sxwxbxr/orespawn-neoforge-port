package com.swbr.orespawn.client.renderer.cannonfodder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ChipmunkModel;
import com.swbr.orespawn.entity.cannonfodder.Chipmunk;
import com.swbr.orespawn.entity.cannonfodder.EntityCannonFodder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderChipmunk} (RenderChipmunk.java:9-68), registered as
 * {@code new RenderChipmunk(new ModelChipmunk(1.0f), 0.15f, 0.9f)} (manifest {@code renderer_args},
 * {@code model_args}): {@code ctx -> new RenderChipmunk(ctx, 1.0f, 0.15f, 0.9f)}.
 *
 * <p>The three textures differ only in the hat square at uv 40,0: red, green, blue.
 */
public class RenderChipmunk extends MobRenderer<Chipmunk, ChipmunkModel> {

    private static final ResourceLocation texture = tex("chipmunktexture");
    private static final ResourceLocation texture2 = tex("chipmunktexture2");
    private static final ResourceLocation texture3 = tex("chipmunktexture3");

    protected ChipmunkModel model;
    private float scale;

    /**
     * {@code RenderChipmunk(model, par2, par3)} (:17-22): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelChipmunk} constructor argument
     */
    public RenderChipmunk(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new ChipmunkModel(context.bakeLayer(ChipmunkModel.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:36-46): half scale for a baby. */
    @Override
    protected void scale(final Chipmunk par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /**
     * PORT: {@code MobRenderer} halves the shadow of a baby and scales it by the size attribute; 1.7.10
     * {@code RenderLiving} drew the constructor shadow for every chipmunk.
     */
    @Override
    protected float getShadowRadius(final Chipmunk entity) {
        return this.shadowRadius;
    }

    /** {@code getEntityTexture} (:48-60): the hat colour, but only on a battle chipmunk. */
    @Override
    public ResourceLocation getTextureLocation(final Chipmunk entity) {
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
