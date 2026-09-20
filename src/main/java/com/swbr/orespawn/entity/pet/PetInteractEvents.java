package com.swbr.orespawn.entity.pet;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Restores the 1.7.10 click order on {@link Stinky} and {@link RubberDucky}. No original class: 1.7.10
 * {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} first - (1) drop a leash this player holds,
 * (2) attach a lead to a wild pet or for its owner, (3) {@code interact} - and only when that returned {@code false}
 * the held item's {@code itemInteractionForEntity} (the name tag). 1.21.1 runs the name tag before everything
 * ({@code Mob.checkAndHandleImportantInteractions}) and attaches a lead for anyone ({@code Entity.interact}).
 *
 * <ul>
 *   <li>Name tag: {@code RubberDucky.interact} returns {@code true} for every click that reaches it, so no duck was ever
 *       named with a tag; a Stinky's owner within 4 blocks toggles sitting instead. Everyone else fell through to the
 *       vanilla tag. The pet answers first; a leash this player holds is dropped before that, as step (1) did.</li>
 *   <li>A stranger's lead on a tamed pet: step (2) required the owner, so the click went to {@code interact}; a duck
 *       consumed it there, a Stinky did nothing, and the lead itself has no entity action.</li>
 * </ul>
 * Every other click takes the vanilla path, whose leash-then-{@code mobInteract} order already matches
 * {@code interactFirst} (W04 {@code CompanionInteractEvents} precedent).
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class PetInteractEvents {

    private PetInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof Stinky) && !(target instanceof RubberDucky)) {
            return;
        }
        final TamableAnimal pet = (TamableAnimal) target;
        if (!pet.isAlive()) {
            return;
        }
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        final boolean nameTag = stack.is(Items.NAME_TAG);
        final boolean strangersLead = stack.is(Items.LEAD) && pet.isTame() && !pet.isOwnedBy(player)
                && pet.getLeashHolder() != player;
        if (!nameTag && !strangersLead) {
            return;
        }
        final boolean isClientSide = pet.level().isClientSide;
        if (pet.getLeashHolder() == player) {
            // interactFirst: clearLeashed(true, !capabilities.isCreativeMode), then return true.
            if (!isClientSide) {
                pet.dropLeash(true, !player.hasInfiniteMaterials());
                pet.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            cancel(event, InteractionResult.sidedSuccess(isClientSide));
            return;
        }
        final InteractionResult result = target instanceof Stinky stinky
                ? stinky.mobInteract(player, event.getHand())
                : ((RubberDucky) target).mobInteract(player, event.getHand());
        if (result.consumesAction()) {
            // Mob.interact announces a consumed mobInteract the same way.
            pet.gameEvent(GameEvent.ENTITY_INTERACT, player);
            cancel(event, result);
        } else if (strangersLead) {
            cancel(event, InteractionResult.PASS);
        }
    }

    private static void cancel(final PlayerInteractEvent.EntityInteract event, final InteractionResult result) {
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}
