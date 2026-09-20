package com.swbr.orespawn.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.DragonModel;
import com.swbr.orespawn.entity.dragon.Dragon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderDragon} (RenderDragon.java:9-55), a {@code RenderLiving} with a
 * {@code ModelDragon}. ClientProxyOreSpawn registered {@code new RenderDragon(new ModelDragon(0.65f), 1.25f, 1.0f)}
 * (manifest {@code renderer_args}, {@code model_args}): register with {@code ctx -> new RenderDragon(ctx, 1.25f, 1.0f)};
 * the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}), texture by dragon type: 0 fire
 * ({@code Dragon.png}), anything else ice ({@code WhiteDragon.png}), lower-cased into {@code textures/entity/} by
 * tools/assets.py.
 */
public class RenderDragon extends MobRenderer<Dragon, DragonModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/dragon.png");
    private static final ResourceLocation texture2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/whitedragon.png");

    protected DragonModel model;
    private float scale;

    /** {@code RenderDragon(model, par2, par3)} (:16-21): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderDragon(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new DragonModel(context.bakeLayer(DragonModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:35-41): {@code glScalef(scale)};
     * {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final Dragon par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:43-49). */
    @Override
    public ResourceLocation getTextureLocation(final Dragon d) {
        if (d.getDragonType() != 0) {
            return RenderDragon.texture2;
        }
        return RenderDragon.texture;
    }
}
