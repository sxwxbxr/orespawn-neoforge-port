package com.swbr.orespawn.entity.cannonfodder;

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
 * Keeps the 1.7.10 order of a right click on every {@link EntityCannonFodder} (Chipmunk, Lizard, Ostrich,
 * VelocityRaptor) and on {@link Gazelle} - the same restoration {@code CompanionInteractEvents} does for
 * Girlfriend and Boyfriend (W04).
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} - drop this player's
 * leash, attach a lead to a wild mob or for its owner, otherwise {@code interact} - and only when that
 * answered {@code false} the held item ({@code ItemNameTag}: needs a custom name, sets it, makes the mob
 * persistent). 1.21.1 {@code Mob.interact} is final and applies the name tag first, then the leash (any
 * player's lead on a tame mob), then {@code mobInteract}.
 *
 * <p>Three consequences would change these mobs:
 * <ul>
 *   <li>A name tag reached the mob's own {@code interact} first. The Lizard answers every click, so it could
 *       never be named; an activated battle mob toggles guard duty instead; a tame Chipmunk or Gazelle names
 *       itself after the tag's display name ("Name Tag" for an unnamed tag) without becoming persistent.</li>
 *   <li>If {@code interact} said no, vanilla would call {@code mobInteract} a second time after an unnamed
 *       tag, and the owner rotation of {@link EntityCannonFodder} would run twice. The click is therefore
 *       finished here with the 1.7.10 item step.</li>
 *   <li>A stranger's lead on a tame mob went to {@code interact} (the owner rotation included) and did not
 *       attach.</li>
 * </ul>
 * {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn} ahead of {@code Mob.interact}.
 * Every other click takes the vanilla path, whose leash-then-{@code mobInteract} order matches.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class CannonFodderInteractEvents {

    private CannonFodderInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof EntityCannonFodder) && !(target instanceof Gazelle)) {
            return;
        }
        final TamableAnimal animal = (TamableAnimal) target;
        if (!animal.isAlive()) {
            return;
        }
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        final boolean nameTag = stack.is(Items.NAME_TAG);
        final boolean strangersLead = stack.is(Items.LEAD) && animal.isTame() && !animal.isOwnedBy(player)
                && animal.getLeashHolder() != player;
        if (!nameTag && !strangersLead) {
            return;
        }
        final boolean isClientSide = animal.level().isClientSide;
        if (animal.getLeashHolder() == player) {
            // interactFirst: clearLeashed(true, !capabilities.isCreativeMode), then return true.
            if (!isClientSide) {
                animal.dropLeash(true, !player.hasInfiniteMaterials());
                animal.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            cancel(event, InteractionResult.sidedSuccess(isClientSide));
            return;
        }
        final InteractionResult result = animal.mobInteract(player, event.getHand());
        if (result.consumesAction()) {
            // Mob.interact announces a consumed mobInteract the same way.
            animal.gameEvent(GameEvent.ENTITY_INTERACT, player);
            cancel(event, result);
            return;
        }
        if (strangersLead) {
            cancel(event, InteractionResult.PASS);
            return;
        }
        // The held-item step of EntityPlayer.interactWith: creative players act with a copy, as
        // Player.interactOn does. NameTagItem is the 1.7.10 ItemNameTag rule (custom name, persistence, -1).
        final ItemStack used = player.getAbilities().instabuild ? stack.copy() : stack;
        final InteractionResult tag = used.interactLivingEntity(player, animal, event.getHand());
        if (tag.consumesAction()) {
            animal.level().gameEvent(GameEvent.ENTITY_INTERACT, animal.position(), GameEvent.Context.of(player));
            cancel(event, tag);
        } else {
            cancel(event, InteractionResult.PASS);
        }
    }

    private static void cancel(final PlayerInteractEvent.EntityInteract event, final InteractionResult result) {
        event.setCancellationResult(result);
        event.setCanceled(true);
    }
}
