package com.swbr.orespawn.client.renderer.pet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.RubberDuckyModel;
import com.swbr.orespawn.entity.pet.RubberDucky;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRubberDucky} (RenderRubberDucky.java:9-61). ClientProxyOreSpawn registered it as
 * {@code new RenderRubberDucky(new ModelRubberDucky(1.0f), 0.15f, 0.75f)} (ClientProxyOreSpawn.java:132, manifest
 * {@code renderer_args}, {@code model_args}): register with {@code ctx -> new RenderRubberDucky(ctx, 0.15f, 0.75f)}; the
 * model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, halved for a baby; the evil texture from 5 kills on
 * ({@code RubberDuckytexture.png} / {@code EvilRubberDuckytexture.png}, lower-cased by the asset generator).
 */
public class RenderRubberDucky extends MobRenderer<RubberDucky, RubberDuckyModel> {

    protected RubberDuckyModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/rubberduckytexture.png");
    private static final ResourceLocation texture2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/evilrubberduckytexture.png");

    /** {@code RenderRubberDucky(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderRubberDucky(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new RubberDuckyModel(context.bakeLayer(RubberDuckyModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale / 2)} for a child, else
     * {@code glScalef(scale)} - {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final RubberDucky par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:46-54). */
    @Override
    public ResourceLocation getTextureLocation(final RubberDucky entity) {
        if (entity.getKillCount() >= 5) {
            return RenderRubberDucky.texture2;
        }
        return RenderRubberDucky.texture;
    }
}
