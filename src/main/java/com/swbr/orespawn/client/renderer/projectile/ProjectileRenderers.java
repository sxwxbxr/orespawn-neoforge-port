package com.swbr.orespawn.client.renderer.projectile;

import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Renderer registrations for the W04 throwables - the successor of ClientProxyOreSpawn.java:23-29,
 * called from {@code ClientSetup.onRegisterRenderers}.
 */
public final class ProjectileRenderers {

    private ProjectileRenderers() {
    }

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SUNSPOT_URCHIN.get(), RenderItemUrchin::new); // :23
        event.registerEntityRenderer(ModEntities.WATER_BALL.get(), RenderItemUrchin::new);     // :24
        event.registerEntityRenderer(ModEntities.INK_SACK.get(), RenderItemUrchin::new);       // :25
        event.registerEntityRenderer(ModEntities.LASER_BALL.get(), RenderItemUrchin::new);     // :26
        event.registerEntityRenderer(ModEntities.ICE_BALL.get(), RenderItemUrchin::new);       // :27
        event.registerEntityRenderer(ModEntities.ACID.get(), RenderItemUrchin::new);           // :28
        event.registerEntityRenderer(ModEntities.DEAD_IRUKANDJI.get(), RenderItemUrchin::new); // :29
        // PORT: BetterFireball had no renderer; 1.7.10 sent it as a vanilla large fireball, which
        // RenderFireball drew as a full-bright fire charge. Vanilla's FIREBALL registration, same look.
        event.registerEntityRenderer(ModEntities.BETTER_FIREBALL.get(), context -> new ThrownItemRenderer<>(context, 3.0F, true));
        // PORT: ThunderBolt had no renderer and was never tracked to clients; its flight shows only as the
        // four firework sparks per tick it spawns itself.
        event.registerEntityRenderer(ModEntities.THUNDER_BOLT.get(), NoopRenderer::new);
    }
}
