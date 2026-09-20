package com.swbr.orespawn.client.item;

import com.swbr.orespawn.client.model.BattleAxeModel;
import com.swbr.orespawn.client.model.ChainsawModel;
import com.swbr.orespawn.client.model.QueenBattleAxeModel;
import com.swbr.orespawn.client.model.geom.BattleAxeGeometry;
import com.swbr.orespawn.client.model.geom.ChainsawGeometry;
import com.swbr.orespawn.client.model.geom.QueenBattleAxeGeometry;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Wiring for the three W03 item renderers - the successor of
 * {@code MinecraftForgeClient.registerItemRenderer(OreSpawnMain.MyBattleAxe, new RenderBattleAxe())}
 * and its two siblings (ClientProxyOreSpawn.java:154-156). Called from the hubs in
 * {@code client.ClientSetup}; the item holders are passed in so this class does not depend on
 * how {@code registry.ModItems} names them.
 *
 * <p>The renderer behind each {@link IClientItemExtensions} is created on first use, not in the
 * event: {@code RegisterClientExtensionsEvent} is posted from {@code ClientModLoader.begin}
 * (Minecraft.java:492), before {@code Minecraft.entityModels} and the block-entity dispatcher
 * exist (:525-527), and {@link BlockEntityWithoutLevelRenderer}'s constructor takes both.
 * {@code getCustomRenderer} is queried every frame and must always return the same instance,
 * so each extension caches its renderer.
 */
public final class BigWeaponRenderers {

    private BigWeaponRenderers() {}

    /** The three layers, one per generated geometry (R8). */
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BattleAxeModel.LAYER, BattleAxeGeometry::createBodyLayer);
        event.registerLayerDefinition(ChainsawModel.LAYER, ChainsawGeometry::createBodyLayer);
        event.registerLayerDefinition(QueenBattleAxeModel.LAYER, QueenBattleAxeGeometry::createBodyLayer);
    }

    /** The three item renderers (ClientProxyOreSpawn.java:154-156). */
    public static void registerItemExtensions(RegisterClientExtensionsEvent event,
                                              Holder<? extends Item> battleAxe,
                                              Holder<? extends Item> chainsaw,
                                              Holder<? extends Item> queenBattleAxe) {
        event.registerItem(lazy(RenderBattleAxe::new), battleAxe.value());
        event.registerItem(lazy(RenderChainsaw::new), chainsaw.value());
        event.registerItem(lazy(RenderQueenBattleAxe::new), queenBattleAxe.value());
    }

    private static IClientItemExtensions lazy(Supplier<? extends BlockEntityWithoutLevelRenderer> factory) {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = factory.get();
                }
                return this.renderer;
            }
        };
    }
}
