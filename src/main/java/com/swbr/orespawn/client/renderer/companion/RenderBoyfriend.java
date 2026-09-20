package com.swbr.orespawn.client.renderer.companion;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.companion.Boyfriend;
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
 * Port of {@code danger.orespawn.RenderBoyfriend} ({@code RenderBiped} with a vanilla
 * {@code ModelBiped}, shadow 0.55; ClientProxyOreSpawn.java:17): the Girlfriend renderer without the
 * Valentine's scale. See {@link RenderGirlfriend} for the layer set.
 */
public class RenderBoyfriend extends HumanoidMobRenderer<Boyfriend, HumanoidModel<Boyfriend>> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "boyfriend"), "main");

    protected HumanoidModel<Boyfriend> model;

    public RenderBoyfriend(final EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.55f);
        this.model = this.getModel();
        final HumanoidModel<Boyfriend> innerArmor = new HumanoidModel<Boyfriend>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        final HumanoidModel<Boyfriend> outerArmor = new HumanoidModel<Boyfriend>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        this.addLayer(new HumanoidArmorLayer<Boyfriend, HumanoidModel<Boyfriend>, HumanoidModel<Boyfriend>>(this,
                innerArmor, outerArmor, context.getModelManager()));
    }

    /** {@code new ModelBiped()}: 64x32 texture, no cube deformation. */
    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f), 64, 32);
    }

    /** {@code getEntityTexture} (RenderBoyfriend.java:29-32). */
    @Override
    public ResourceLocation getTextureLocation(final Boyfriend entity) {
        final Boyfriend g = entity;
        return g.getTexture();
    }
}
