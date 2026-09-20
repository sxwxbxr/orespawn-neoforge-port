package com.swbr.orespawn.entity.waterdragon;

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
 * Keeps the 1.7.10 order of a right click on {@link WaterDragon} and {@link GammaMetroid} - the same restoration
 * {@code CannonFodderInteractEvents} (W06) and {@code CompanionInteractEvents} (W04) do. No original class.
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} - drop this player's leash, attach
 * a lead to a wild mob or for its owner, otherwise {@code interact} - and only when that answered {@code false} the held
 * item ({@code ItemNameTag}: needs a custom name, sets it, makes the mob persistent). 1.21.1 {@code Mob.interact} applies
 * the name tag first, then the leash (any player's lead on a tame mob), then {@code mobInteract}.
 *
 * <p>Consequences without this class: the owner's name tag within 16 would make the tame animal persistent (1.7.10 named
 * it without), the owner's name tag at 16 to 25 blocks would name it instead of toggling sitting, and a stranger's lead
 * would attach to a tame one. {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn} ahead of
 * {@code Mob.interact}; every other click takes the vanilla path, whose leash-then-{@code mobInteract} order matches.
 *
 * <p>PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (both {@code mobInteract}s ignore it).
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WaterDragonInteractEvents {

    private WaterDragonInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof WaterDragon) && !(target instanceof GammaMetroid)) {
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
        // The held-item step of EntityPlayer.interactWith: creative players act with a copy, as Player.interactOn does.
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
