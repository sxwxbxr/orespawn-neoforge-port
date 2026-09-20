package com.swbr.orespawn.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.MolenoidModel;
import com.swbr.orespawn.entity.monster.Molenoid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderMolenoid} (RenderMolenoid.java:9-49), a {@code RenderLiving}.
 * ClientProxyOreSpawn.java:125: {@code new RenderMolenoid(new ModelMolenoid(0.5f), 1.0f, 1.0f)} - register as
 * {@code ctx -> new RenderMolenoid(ctx, 1.0f, 1.0f)}. Shadow {@code par2 * par3}, scale {@code par3}, one texture
 * {@code Molenoidtexture.png}.
 */
public class RenderMolenoid extends MobRenderer<Molenoid, MolenoidModel> {

    /** {@code Molenoidtexture.png} (:47), lower-cased into {@code textures/entity/} by tools/assets.py. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/molenoidtexture.png");

    protected MolenoidModel model;
    private float scale = 1.0f;

    /** {@code RenderMolenoid(model, par2, par3)} (:15-20). */
    public RenderMolenoid(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new MolenoidModel(context.bakeLayer(MolenoidModel.LAYER), 0.5f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Molenoid par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Molenoid entity) {
        return texture;
    }
}
