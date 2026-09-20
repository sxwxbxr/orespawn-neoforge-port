package com.swbr.orespawn.client.renderer.boss.king;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.PurplePowerModel;
import com.swbr.orespawn.entity.boss.king.PurplePower;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderPurplePower} (RenderPurplePower.java:9-67), a {@code RenderLiving}.
 * ClientProxyOreSpawn: {@code new RenderPurplePower(new ModelPurplePower(1.0f), 0.3f, 2.75f)} (manifest
 * {@code renderer_args}, {@code model_args}) - register as {@code ctx -> new RenderPurplePower(ctx, 0.3f, 2.75f)}.
 * Shadow {@code par2 * par3} = 0.825 for every type.
 *
 * <p>{@code preRenderScale} (:34-40): the constructor scale 2.75 for type 0 (The Queen's orb), 0.55 for every other type.
 * Texture by type (:42-58): 1, 2, 3 and 10 have their own, everything else {@code PurplePowertexture.png}. The
 * translucency, full brightness and random turns are the model's (R8).
 */
public class RenderPurplePower extends MobRenderer<PurplePower, PurplePowerModel> {

    private static final ResourceLocation texture = texture("purplepowertexture");
    private static final ResourceLocation texture2 = texture("purplepowertexture2");
    private static final ResourceLocation texture3 = texture("purplepowertexture3");
    private static final ResourceLocation texture4 = texture("purplepowertexture4");
    private static final ResourceLocation texture10 = texture("purplepowertexture10");

    protected PurplePowerModel model;
    private float scale;

    /** {@code RenderPurplePower(ModelPurplePower, float, float)} (:19-24). */
    public RenderPurplePower(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new PurplePowerModel(context.bakeLayer(PurplePowerModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderScale} (:34-40), called through {@code preRenderCallback} of {@code RenderLiving}. */
    @Override
    protected void scale(final PurplePower par1Entity, final PoseStack poseStack, final float par2) {
        float localscale = this.scale;
        if (par1Entity.getPurpleType() != 0) {
            localscale = 0.55f;
        }
        poseStack.scale(localscale, localscale, localscale);
    }

    /** {@code getEntityTexture} (:42-58). */
    @Override
    public ResourceLocation getTextureLocation(final PurplePower entity) {
        final PurplePower p = entity;
        final int i = p.getPurpleType();
        if (i == 1) {
            return texture2;
        }
        if (i == 2) {
            return texture3;
        }
        if (i == 3) {
            return texture4;
        }
        if (i == 10) {
            return texture10;
        }
        return texture;
    }

    /** {@code PurplePowertexture*.png} (:60-66), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static ResourceLocation texture(final String name) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + name + ".png");
    }
}
