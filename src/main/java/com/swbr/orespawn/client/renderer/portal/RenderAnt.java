package com.swbr.orespawn.client.renderer.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.client.model.AntModel;
import com.swbr.orespawn.entity.portal.EntityAnt;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderAnt} (RenderAnt.java:10-45), the renderer of all five ants
 * (ClientProxyOreSpawn.java:42-45, :106):
 * <ul>
 *   <li>{@code ant}, {@code rainbow_ant}, {@code unstable_ant}: {@code new RenderAnt(new ModelAnt(), 0.1f, 0.25f)}</li>
 *   <li>{@code red_ant}, {@code termite}: {@code new RenderAnt(new ModelAnt(), 0.15f, 0.35f)}</li>
 * </ul>
 * Register one instance per type, e.g. {@code ctx -> new RenderAnt(ctx, 0.15f, 0.35f)}; each gets its
 * own model like each 1.7.10 registration did.
 */
public class RenderAnt extends MobRenderer<EntityAnt, AntModel> {

    protected AntModel model;
    private float scale;

    /** {@code RenderAnt(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderAnt(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new AntModel(context.bakeLayer(AntModel.LAYER)), par2 * par3);
        this.scale = 0.25f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale, scale, scale)}. */
    @Override
    protected void scale(final EntityAnt par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-45): the class switch lives in {@link EntityAnt#getTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final EntityAnt entity) {
        final EntityAnt a = entity;
        return a.getTexture(a);
    }
}
