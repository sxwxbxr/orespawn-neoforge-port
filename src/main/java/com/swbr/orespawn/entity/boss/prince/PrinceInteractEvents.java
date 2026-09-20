package com.swbr.orespawn.entity.boss.prince;

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
 * Keeps the 1.7.10 order of a right click on {@link ThePrince}, {@link ThePrinceTeen} and {@link ThePrincess} - the
 * restoration W09's {@code DragonInteractEvents} does for Dragon and Spyro, for the same reason.
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} - drop this player's leash,
 * attach a lead to a wild mob or for its owner, otherwise {@code interact} - and only when that answered {@code false}
 * the held item ({@code ItemNameTag}: needs a custom name, sets it, makes the mob persistent). 1.21.1 {@code Mob.interact}
 * is final and applies the name tag first, then the leash (any player's lead on a tame mob), then {@code mobInteract}.
 *
 * <p>What would change otherwise: the owner's name tag reached {@code interact} first and named the prince
 * <em>without</em> persistence (the Young Prince may despawn only while wild, so persistence would matter for a
 * stranger's tag on a wild one); a stranger's lead on a tame prince reached {@code interact}, which has no branch for
 * it, and did not attach. {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn} ahead of
 * {@code Mob.interact}. Every other click takes the vanilla path, whose leash-then-{@code mobInteract} order matches.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class PrinceInteractEvents {

    private PrinceInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof ThePrince) && !(target instanceof ThePrinceTeen) && !(target instanceof ThePrincess)) {
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
        final InteractionResult result;
        if (animal instanceof ThePrince prince) {
            result = prince.mobInteract(player, event.getHand());
        } else if (animal instanceof ThePrinceTeen teen) {
            result = teen.mobInteract(player, event.getHand());
        } else {
            result = ((ThePrincess) animal).mobInteract(player, event.getHand());
        }
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
        // The held-item step of EntityPlayer.interactWith: creative players act with a copy, as Player.interactOn does.
        // NameTagItem is the 1.7.10 ItemNameTag rule (custom name, persistence, -1).
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
