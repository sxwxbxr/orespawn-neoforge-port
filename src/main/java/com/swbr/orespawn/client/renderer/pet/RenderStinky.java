package com.swbr.orespawn.client.renderer.pet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.StinkyModel;
import com.swbr.orespawn.entity.pet.Stinky;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderStinky} (RenderStinky.java:9-141). ClientProxyOreSpawn registered it as
 * {@code new RenderStinky(new ModelStinky(0.65f), 0.75f, 1.0f)} (ClientProxyOreSpawn.java:120, manifest
 * {@code renderer_args}, {@code model_args}): register with {@code ctx -> new RenderStinky(ctx, 0.75f, 1.0f)}; the model's
 * constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} (no baby halving), 19 skins: skin {@code i} 1..18 is
 * {@code Stinkytexture(i+1).png}, anything else {@code Stinkytexture1.png} (lower-cased by the asset generator).
 */
public class RenderStinky extends MobRenderer<Stinky, StinkyModel> {

    protected StinkyModel model;
    private float scale;
    /** {@code texture1} .. {@code texture19} (:120-140). */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[19];

    static {
        for (int i = 0; i < TEXTURES.length; ++i) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/stinkytexture" + (i + 1) + ".png");
        }
    }

    /** {@code RenderStinky(model, par2, par3)} (:33-38): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderStinky(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new StinkyModel(context.bakeLayer(StinkyModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:52-58): {@code glScalef(scale)} -
     * {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final Stinky par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:60-118): by {@code getSkin()}, the synced skin. */
    @Override
    public ResourceLocation getTextureLocation(final Stinky entity) {
        final int i = entity.getSkin();
        if (i >= 1 && i <= 18) {
            return TEXTURES[i];
        }
        return TEXTURES[0];
    }
}
