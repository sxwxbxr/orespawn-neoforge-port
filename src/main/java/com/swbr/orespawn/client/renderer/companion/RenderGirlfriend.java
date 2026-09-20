package com.swbr.orespawn.client.renderer.companion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.companion.Girlfriend;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGirlfriend} ({@code RenderBiped} with a vanilla
 * {@code ModelBiped}, shadow 0.5; ClientProxyOreSpawn.java:16).
 *
 * <p>{@code RenderBiped} drew the body, the armor pass with {@code ModelBiped(1.0)}/{@code (0.5)} and
 * the held item - {@link HumanoidMobRenderer} plus a {@link HumanoidArmorLayer} on the player armor
 * layers is the same set. The body layer is the 1.7.10 64x32 biped: {@code HumanoidModel.createMesh}
 * builds exactly its boxes and texture offsets, only the texture size differs from vanilla's 64x64.
 *
 * <p>Nothing here is seen on this machine (R17); the texture choice comes from
 * {@link Girlfriend#getTexture()}, the ×5 Valentine's scale from {@link #scale}.
 */
public class RenderGirlfriend extends HumanoidMobRenderer<Girlfriend, HumanoidModel<Girlfriend>> {

    /** Register with {@link #createBodyLayer()} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "girlfriend"), "main");

    protected HumanoidModel<Girlfriend> model;

    public RenderGirlfriend(final EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.5f);
        this.model = this.getModel();
        final HumanoidModel<Girlfriend> innerArmor = new HumanoidModel<Girlfriend>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        final HumanoidModel<Girlfriend> outerArmor = new HumanoidModel<Girlfriend>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        this.addLayer(new HumanoidArmorLayer<Girlfriend, HumanoidModel<Girlfriend>, HumanoidModel<Girlfriend>>(this,
                innerArmor, outerArmor, context.getModelManager()));
    }

    /** {@code new ModelBiped()}: 64x32 texture, no cube deformation. */
    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f), 64, 32);
    }

    /**
     * {@code preRenderCallback} (RenderGirlfriend.java:21-29): {@code glScalef(5, 5, 5)} while it is
     * Valentine's Day on this client and she is not feeling better. Same place in the transform chain
     * (after the body rotation, before the 1.501 model offset).
     */
    @Override
    protected void scale(final Girlfriend p_77041_1_, final PoseStack poseStack, final float p_77041_2_) {
        if (OreSpawn.valentines_day != 0 && p_77041_1_ != null && p_77041_1_ instanceof Girlfriend) {
            final Girlfriend gf = p_77041_1_;
            if (gf.feelingBetter == 0) {
                poseStack.scale(5.0f, 5.0f, 5.0f);
            }
        }
    }

    /** {@code getEntityTexture} (RenderGirlfriend.java:39-42). */
    @Override
    public ResourceLocation getTextureLocation(final Girlfriend entity) {
        final Girlfriend g = entity;
        return g.getTexture();
    }
}
