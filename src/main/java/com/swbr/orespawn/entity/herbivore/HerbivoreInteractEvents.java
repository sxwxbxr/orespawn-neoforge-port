package com.swbr.orespawn.entity.herbivore;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Keeps one 1.7.10 rule of a right click on the two tameable herbivores, {@link Camarasaurus} and
 * {@link Hydrolisc}: a stranger's lead does nothing on a tamed one.
 *
 * <p>1.7.10 {@code EntityLiving.interactFirst} attached a lead only when the mob was wild or the player was
 * its owner; a stranger's click went on to {@code interact}, where no branch takes a lead, and the lead item
 * itself has no entity action. 1.21.1 attaches it in {@code Entity.interact} for anyone
 * ({@code TamableAnimal.canBeLeashed} is {@code true}). {@code PlayerInteractEvent.EntityInteract} fires ahead
 * of {@code Mob.interact}, so the click is stopped here (same fix as W04's {@code CompanionInteractEvents}).
 *
 * <p>The name-tag order (1.21.1 runs the vanilla name tag before {@code mobInteract}) is <em>not</em> undone:
 * the owner branch and the vanilla tag give the same name, and the only difference, the persistence flag of
 * the vanilla tag, changes nothing for a tamed Camarasaurus (never despawns, cannot be untamed) or any
 * Hydrolisc (never despawns).
 *
 * <p>PORT: 1.7.10 had one hand; an off-hand lead keeps the vanilla path.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class HerbivoreInteractEvents {

    private HerbivoreInteractEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        final Entity target = event.getTarget();
        if (!(target instanceof Camarasaurus) && !(target instanceof Hydrolisc)) {
            return;
        }
        final TamableAnimal pet = (TamableAnimal) target;
        if (!pet.isAlive()) {
            return;
        }
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        if (stack.is(Items.LEAD) && pet.isTame() && !pet.isOwnedBy(player) && pet.getLeashHolder() != player) {
            // interact(player) has no branch for a stranger holding a lead: it returned false, nothing happened.
            event.setCancellationResult(InteractionResult.PASS);
            event.setCanceled(true);
        }
    }
}
