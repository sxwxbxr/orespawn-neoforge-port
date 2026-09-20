package com.swbr.orespawn.entity.companion;

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
 * Keeps the 1.7.10 order of a right click on {@link Girlfriend} and {@link Boyfriend}.
 *
 * <p>1.7.10 {@code EntityPlayer.interactWith} called {@code EntityLiving.interactFirst}, which
 * (1) dropped the leash if this player held it, (2) attached a lead if the mob was wild or the
 * player was its owner, and (3) otherwise asked {@code interact}; only when all of that returned
 * {@code false} did the held item act ({@code ItemNameTag}). 1.21.1 {@code Mob.interact} is final
 * and runs the name tag first, then {@code Entity.interact} (leash), then {@code mobInteract}, and
 * {@code TamableAnimal.canBeLeashed} accepts anyone's lead.
 *
 * <p>Two of those orderings would silently change the companions.
 * {@code PlayerInteractEvent.EntityInteract} fires in {@code Player.interactOn} ahead of
 * {@code Mob.interact}, so both are put back here:
 * <ul>
 *   <li>Name tag: both companions take any item of their owner into their hand, name tags included,
 *       so an owner could never name them (R18: "Namensschild-Zweig unerreichbar", kept). A
 *       stranger, a wild companion or an owner beyond 4 blocks got the vanilla name. The companion
 *       answers first; a leash this player holds is dropped before that, as step (1) did.</li>
 *   <li>A stranger's lead on a tamed companion: step (2) required the owner, so the click went to
 *       {@code interact}, where nothing matches a lead, and the lead itself does nothing.</li>
 * </ul>
 * Every other click takes the vanilla path, whose leash-then-{@code mobInteract} order already
 * matches {@code interactFirst}.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class CompanionInteractEvents {

    private CompanionInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        // PORT: 1.7.10 had one hand; an off-hand click keeps the vanilla path (mobInteract ignores it).
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof Girlfriend) && !(target instanceof Boyfriend)) {
            return;
        }
        final TamableAnimal companion = (TamableAnimal) target;
        if (!companion.isAlive()) {
            return;
        }
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        final boolean nameTag = stack.is(Items.NAME_TAG);
        final boolean strangersLead = stack.is(Items.LEAD) && companion.isTame() && !companion.isOwnedBy(player)
                && companion.getLeashHolder() != player;
        if (!nameTag && !strangersLead) {
            return;
        }
        final boolean isClientSide = companion.level().isClientSide;
        if (companion.getLeashHolder() == player) {
            // interactFirst: clearLeashed(true, !capabilities.isCreativeMode), then return true.
            if (!isClientSide) {
                companion.dropLeash(true, !player.hasInfiniteMaterials());
                companion.gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            cancel(event, InteractionResult.sidedSuccess(isClientSide));
            return;
        }
        final InteractionResult result = target instanceof Girlfriend girlfriend
                ? girlfriend.mobInteract(player, event.getHand())
                : ((Boyfriend) target).mobInteract(player, event.getHand());
        if (result.consumesAction()) {
            // Mob.interact announces a consumed mobInteract the same way.
            companion.gameEvent(GameEvent.ENTITY_INTERACT, player);
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
