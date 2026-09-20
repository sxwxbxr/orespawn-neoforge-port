package com.swbr.orespawn.entity.leon;

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
 * Keeps the 1.7.10 order of a right click on a {@link Leon} - the restoration {@code CannonFodderInteractEvents} (W06)
 * and {@code CompanionInteractEvents} (W04) do for their mobs, with the same rules.
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} - drop this player's leash, attach
 * a lead to a wild Leon or for its owner, otherwise {@code Leon.interact} - and only when that answered {@code false}
 * the held item ({@code ItemNameTag}: needs a custom name, sets it, makes the mob persistent). 1.21.1
 * {@code Mob.interact} is final and applies the name tag first, then the leash, then {@code mobInteract}.
 *
 * <p>For the Leon that would change: the owner's name tag would go through {@code NameTagItem} (persistence, consumed
 * in creative, an unnamed tag refused) instead of Leon's own branch (:1067-1077: the tag's display name, "Name Tag" for
 * an unnamed one, no persistence, kept in creative); and a stranger's lead on a tame Leon would not reach
 * {@code interact} and must not attach. {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn}
 * ahead of {@code Mob.interact}. Every other click takes the vanilla path, whose leash-then-{@code mobInteract} order
 * matches.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class LeonInteractEvents {

    private LeonInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof Leon leon)) {
            return;
        }
        if (!leon.isAlive()) {
            return;
        }
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        final boolean nameTag = stack.is(Items.NAME_TAG);
        final boolean strangersLead = stack.is(Items.LEAD) && leon.isTame() && !leon.isOwnedBy(player)
                && leon.getLeashHolder() != player;
        if (!nameTag && !strangersLead) {
            return;
        }
        final boolean isClientSide = leon.level().isClientSide;
        if (leon.getLeashHolder() == player) {
            // interactFirst: clearLeashed(true, !capabilities.isCreativeMode), then return true.
            if (!isClientSide) {
                leon.dropLeash(true, !player.hasInfiniteMaterials());
                leon.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            cancel(event, InteractionResult.sidedSuccess(isClientSide));
            return;
        }
        final InteractionResult result = leon.mobInteract(player, event.getHand());
        if (result.consumesAction()) {
            // Mob.interact announces a consumed mobInteract the same way.
            leon.gameEvent(GameEvent.ENTITY_INTERACT, player);
            cancel(event, result);
            return;
        }
        if (strangersLead) {
            cancel(event, InteractionResult.PASS);
            return;
        }
        // The held-item step of EntityPlayer.interactWith: creative players act with a copy, as Player.interactOn does.
        final ItemStack used = player.getAbilities().instabuild ? stack.copy() : stack;
        final InteractionResult tag = used.interactLivingEntity(player, leon, event.getHand());
        if (tag.consumesAction()) {
            leon.level().gameEvent(GameEvent.ENTITY_INTERACT, leon.position(), GameEvent.Context.of(player));
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
