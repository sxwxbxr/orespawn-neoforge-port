package com.swbr.orespawn.entity.cephadrome;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Keeps the 1.7.10 order of a name-tag click on a {@link Cephadrome}, as {@code LeonInteractEvents} does for the Leon.
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} - drop this player's leash,
 * otherwise {@code Cephadrome.interact} (:888-924) - and only when that answered {@code false} the held item
 * ({@code ItemNameTag}). {@code interact} answers {@code true} for every held item that is not raw meat, so a name tag
 * never renamed a Cephadrome. 1.21.1 {@code Mob.interact} is final and applies {@code NameTagItem} first
 * ({@code checkAndHandleImportantInteractions}), which would rename it, make it persistent and use the tag up.
 * {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn} ahead of {@code Mob.interact}. Leads
 * need no restoration: 1.7.10 {@code interactFirst} attached them before {@code interact}, the same order as 1.21.1
 * {@code Entity.interact} before {@code mobInteract}.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class CephadromeInteractEvents {

    private CephadromeInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof Cephadrome cephadrome) || !cephadrome.isAlive()) {
            return;
        }
        final ItemStack stack = event.getItemStack();
        if (!stack.is(Items.NAME_TAG)) {
            return;
        }
        final Player player = event.getEntity();
        final boolean isClientSide = cephadrome.level().isClientSide;
        if (cephadrome.getLeashHolder() == player) {
            // interactFirst: clearLeashed(true, !capabilities.isCreativeMode), then return true.
            if (!isClientSide) {
                cephadrome.dropLeash(true, !player.hasInfiniteMaterials());
                cephadrome.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            cancel(event, InteractionResult.sidedSuccess(isClientSide));
            return;
        }
        final InteractionResult result = cephadrome.mobInteract(player, event.getHand());
        if (result.consumesAction()) {
            // Mob.interact announces a consumed mobInteract the same way.
            cephadrome.gameEvent(GameEvent.ENTITY_INTERACT, player);
            cancel(event, result);
            return;
        }
        // Unreachable with a name tag (interact only answers false for meat or an empty hand), kept for the 1.7.10
        // held-item step: creative players act with a copy, as Player.interactOn does.
        final ItemStack used = player.getAbilities().instabuild ? stack.copy() : stack;
        final InteractionResult tag = used.interactLivingEntity(player, cephadrome, event.getHand());
        if (tag.consumesAction()) {
            cephadrome.level().gameEvent(GameEvent.ENTITY_INTERACT, cephadrome.position(), GameEvent.Context.of(player));
        }
        cancel(event, tag.consumesAction() ? tag : InteractionResult.PASS);
    }

    private static void cancel(final PlayerInteractEvent.EntityInteract event, final InteractionResult result) {
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}
