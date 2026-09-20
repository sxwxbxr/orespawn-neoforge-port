package com.swbr.orespawn.entity.boss.princeadult;

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
 * Keeps the 1.7.10 order of a right click on a {@link ThePrinceAdult} - the restoration {@code LeonInteractEvents} (W09)
 * does for the Leon, with the same rules.
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} asked {@code EntityLiving.interactFirst} - drop this player's leash, attach
 * a lead to a wild prince or for its owner, otherwise {@code ThePrinceAdult.interact} - and only when that answered
 * {@code false} the held item ({@code ItemNameTag}). 1.21.1 {@code Mob.interact} is final and applies the name tag
 * first, then the leash, then {@code mobInteract}.
 *
 * <p>For the prince that would change: the owner's name tag would go through {@code NameTagItem} (persistence, consumed
 * in creative, an unnamed tag refused) instead of the prince's own branch (:1250-1260: the tag's display name, no
 * persistence, kept in creative); and a stranger's lead on a tame prince would not reach {@code interact} and must not
 * attach. {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn} ahead of {@code Mob.interact}.
 * Every other click takes the vanilla path, whose leash-then-{@code mobInteract} order matches.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class ThePrinceAdultInteractEvents {

    private ThePrinceAdultInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof ThePrinceAdult prince)) {
            return;
        }
        if (!prince.isAlive()) {
            return;
        }
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        final boolean nameTag = stack.is(Items.NAME_TAG);
        final boolean strangersLead = stack.is(Items.LEAD) && prince.isTame() && !prince.isOwnedBy(player)
                && prince.getLeashHolder() != player;
        if (!nameTag && !strangersLead) {
            return;
        }
        final boolean isClientSide = prince.level().isClientSide;
        if (prince.getLeashHolder() == player) {
            // interactFirst: clearLeashed(true, !capabilities.isCreativeMode), then return true.
            if (!isClientSide) {
                prince.dropLeash(true, !player.hasInfiniteMaterials());
                prince.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            cancel(event, InteractionResult.sidedSuccess(isClientSide));
            return;
        }
        final InteractionResult result = prince.mobInteract(player, event.getHand());
        if (result.consumesAction()) {
            // Mob.interact announces a consumed mobInteract the same way.
            prince.gameEvent(GameEvent.ENTITY_INTERACT, player);
            cancel(event, result);
            return;
        }
        if (strangersLead) {
            cancel(event, InteractionResult.PASS);
            return;
        }
        // The held-item step of EntityPlayer.interactWith: creative players act with a copy, as Player.interactOn does.
        final ItemStack used = player.getAbilities().instabuild ? stack.copy() : stack;
        final InteractionResult tag = used.interactLivingEntity(player, prince, event.getHand());
        if (tag.consumesAction()) {
            prince.level().gameEvent(GameEvent.ENTITY_INTERACT, prince.position(), GameEvent.Context.of(player));
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
