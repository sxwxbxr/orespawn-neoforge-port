package com.swbr.orespawn.entity.rider;

import com.swbr.orespawn.OreSpawn;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The lifetime of the player speed that {@code VelocityRaptor.interact} sets (VelocityRaptor.java:259, :283, :313).
 *
 * <p>PORT (DECISIONS R26): the original wrote {@code movementSpeed.setBaseValue} on the client only. That value lived
 * until the server next sent {@code S20PacketEntityProperties} for the player, which in practice was the next sprint
 * toggle (the server adds or removes its sprint modifier and marks the attribute dirty). The port's server-side
 * modifier ({@link RiderSupport#setVelocityRaptorSpeed}) therefore drops off at the next change of
 * {@link Player#isSprinting()} after it was set. No original class: this is the replacement for the packet that
 * overwrote the client value.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class VelocityRaptorSpeed {

    /** Id of the server-side speed modifier (DECISIONS R18: "serverseitiger AttributeModifier"). */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "velocity_raptor_speed");

    /**
     * The sprint state each player had when the modifier was last set. Server thread only; weak so a player object
     * that is gone (relog, death) leaves nothing behind - its transient modifier is gone with it.
     */
    private static final Map<Player, Boolean> SPRINT_AT_SET = new WeakHashMap<>();

    private VelocityRaptorSpeed() {}

    /** Sets (or replaces) the modifier and remembers the sprint state it has to survive. */
    static void set(final Player player, final double amount) {
        final AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.addOrUpdateTransientModifier(new AttributeModifier(ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        SPRINT_AT_SET.put(player, player.isSprinting());
    }

    /**
     * Removes the modifier once {@link Player#isSprinting()} differs from the state at {@link #set}. Public so a
     * GameTest can drive it for a mock player that is never ticked by a connection.
     *
     * @return {@code true} if the modifier was removed by this call
     */
    public static boolean expireIfSprintChanged(final Player player) {
        final Boolean sprinting = SPRINT_AT_SET.get(player);
        if (sprinting == null || player.isSprinting() == sprinting) {
            return false;
        }
        SPRINT_AT_SET.remove(player);
        final AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.removeModifier(ID);
    }

    /** Removes the modifier unconditionally. */
    public static boolean expire(final Player player) {
        if (SPRINT_AT_SET.remove(player) == null) {
            return false;
        }
        final AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.removeModifier(ID);
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        final Player player = event.getEntity();
        if (!player.level().isClientSide) {
            expireIfSprintChanged(player);
        }
    }

    // PORT (R26): the other two ways the 1.7.10 client value was lost. A dimension change built a new client player
    // (base 0.1), and a Speed/Slowness change made the server send S20PacketEntityProperties for movementSpeed.
    @SubscribeEvent
    public static void onChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        expire(event.getEntity());
    }

    @SubscribeEvent
    public static void onEffectAdded(final MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player && touchesSpeed(event.getEffectInstance().getEffect())) {
            expire(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectRemoved(final MobEffectEvent.Remove event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player && touchesSpeed(event.getEffect())) {
            expire(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEffectExpired(final MobEffectEvent.Expired event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player && event.getEffectInstance() != null
                && touchesSpeed(event.getEffectInstance().getEffect())) {
            expire(player);
        }
    }

    private static boolean touchesSpeed(final Holder<MobEffect> effect) {
        return effect.value() == MobEffects.MOVEMENT_SPEED.value() || effect.value() == MobEffects.MOVEMENT_SLOWDOWN.value();
    }
}
