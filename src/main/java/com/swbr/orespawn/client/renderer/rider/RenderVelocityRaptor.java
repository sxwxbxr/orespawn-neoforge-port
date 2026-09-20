package com.swbr.orespawn.client.renderer.rider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.VelocityRaptorModel;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderVelocityRaptor} (RenderVelocityRaptor.java:9-68), a {@code RenderLiving} with a
 * {@code ModelVelocityRaptor}. ClientProxyOreSpawn.java:53: {@code (new ModelVelocityRaptor(1.25f), 0.55f, 0.75f)} -
 * register as {@code ctx -> new RenderVelocityRaptor(ctx, 0.55f, 0.75f, 1.25f)}. Shadow {@code par2 * par3} (:18),
 * scale {@code par3}, halved for a baby (:36-42), team texture by hat colour (:48-61).
 */
public class RenderVelocityRaptor extends MobRenderer<VelocityRaptor, VelocityRaptorModel> {

    /** {@code velocityraptor.png}, {@code 2}, {@code 3} (:63-67), moved into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/velocityraptor.png");
    private static final ResourceLocation texture2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/velocityraptor2.png");
    private static final ResourceLocation texture3 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/velocityraptor3.png");

    protected VelocityRaptorModel model;
    private float scale = 1.0f;

    /** {@code RenderVelocityRaptor(model, par2, par3)} (:17-22) with the model's {@code wingspeed}. */
    public RenderVelocityRaptor(final EntityRendererProvider.Context context, final float par2, final float par3,
                                final float wingspeed) {
        super(context, new VelocityRaptorModel(context.bakeLayer(VelocityRaptorModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:36-46). */
    @Override
    protected void scale(final VelocityRaptor par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:48-61): an activated mob wears its team colour; hat 1 (carrot) keeps the base texture. */
    @Override
    public ResourceLocation getTextureLocation(final VelocityRaptor c) {
        if (c.get_is_activated() != 0) {
            if (c.getHatColor() == 2) {
                return texture2;
            }
            if (c.getHatColor() == 3) {
                return texture3;
            }
        }
        return texture;
    }
}
