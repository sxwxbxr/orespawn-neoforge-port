package com.swbr.orespawn.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.AttackSquidModel;
import com.swbr.orespawn.entity.sea.AttackSquid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderAttackSquid} (RenderAttackSquid.java:9-49), a {@code RenderLiving} with a
 * {@code ModelAttackSquid}. ClientProxyOreSpawn (:67): {@code (new ModelAttackSquid(1.0f), 0.25f, 0.9f)} - register as
 * {@code ctx -> new RenderAttackSquid(ctx, 0.25f, 0.9f, 1.0f)}. Shadow {@code par2 * par3} (:15), scale {@code par3} in
 * {@code preRenderCallback} (:34-40), one texture (:42-48).
 */
public class RenderAttackSquid extends MobRenderer<AttackSquid, AttackSquidModel> {

    /** {@code AttackSquid.png} (:47), lower-cased by the asset import. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/attacksquid.png");

    protected AttackSquidModel model;
    private float scale = 1.0f;

    /** {@code RenderAttackSquid(model, par2, par3)} (:14-19) with the model's {@code wingspeed}. */
    public RenderAttackSquid(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new AttackSquidModel(context.bakeLayer(AttackSquidModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final AttackSquid par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final AttackSquid entity) {
        return texture;
    }
}
