package com.swbr.orespawn.entity.rider;

import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 to 1.21.1 translations only {@link Ostrich} and {@link VelocityRaptor} need. The ones the whole battle-mob
 * family shares (panic trigger, consuming an item, the flattened dirt/tall grass/double plant blocks, setBlock flag
 * 2, the vanilla drop roll) are {@link CannonFodderSupport}'s and used from there. Neither original had a helper
 * class; every decision stays in the entity classes, in the original order.
 */
final class RiderSupport {

    /** {@code +5.0 x base}: the vanilla player base 0.1 becomes the original's 0.6 (VelocityRaptor.java:259). */
    static final double RAPTOR_FED_SPEED = 5.0;
    /** {@code +2.0 x base}: 0.1 becomes the original's 0.3 (VelocityRaptor.java:283, :313). */
    static final double RAPTOR_RESET_SPEED = 2.0;

    private RiderSupport() {}

    /**
     * The ground an ostrich sits down on (Ostrich.java:220): {@code sand}, {@code gravel}, {@code dirt},
     * {@code farmland}, {@code grass}. The 1.7.10 check compared the block only, so every metadata variant matched:
     * sand is sand and red sand, dirt is dirt, coarse dirt and podzol.
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): sand is {@code #minecraft:sand} (adds suspicious sand), gravel is
     * gravel and suspicious gravel ({@code #c:gravels} lacks the latter), dirt is {@code #minecraft:dirt} through
     * {@link CannonFodderSupport#isLegacyDirt} - mycelium, moss, mud and rooted dirt included; the original's grass
     * term gives the same answer as before.
     */
    static boolean isSittingGround(final BlockState bid) {
        return bid.is(BlockTags.SAND)
                || bid.is(Blocks.GRAVEL) || bid.is(Blocks.SUSPICIOUS_GRAVEL)
                || CannonFodderSupport.isLegacyDirt(bid)
                || bid.is(Blocks.FARMLAND)
                || bid.is(Blocks.GRASS_BLOCK);
    }

    /**
     * The plants a velocity raptor eats (VelocityRaptor.java:92 and the five repeats in {@code scan_it}):
     * {@code tallgrass}, {@code yellow_flower}, {@code red_flower}, {@code deadbush}, {@code double_plant}, each with
     * every metadata variant as in 1.7.10. The tall grass shrub (meta 0) is the dead bush, which the list names anyway.
     *
     * <p>PORT (R22): {@code red_flower} (nine metas) is the category {@code #minecraft:small_flowers} minus the
     * dandelion, which stays its own {@code yellow_flower} term - adding cornflower, lily of the valley, wither rose and
     * torchflower; {@code double_plant} includes {@code #minecraft:tall_flowers} (pitcher plant) through
     * {@link CannonFodderSupport#isLegacyDoublePlant}.
     */
    static boolean isRaptorFood(final BlockState bid) {
        return CannonFodderSupport.isLegacyTallGrass(bid)
                || bid.is(Blocks.DANDELION)
                || (bid.is(BlockTags.SMALL_FLOWERS) && !bid.is(Blocks.DANDELION))
                || bid.is(Blocks.DEAD_BUSH)
                || CannonFodderSupport.isLegacyDoublePlant(bid);
    }

    /**
     * {@code player.getEntityAttribute(movementSpeed).setBaseValue(...)} of VelocityRaptor.java:259/:283/:313.
     *
     * <p>PORT (R18 case 1): the original wrote the base value on the <b>client</b> only ({@code worldObj.isRemote}),
     * a client-side hack the server never knew. The port sets a modifier on the server that yields the same value on
     * the vanilla base 0.1 ({@code ADD_MULTIPLIED_BASE}, so the sprint boost multiplies on top as before) and lets
     * the attribute sync carry it to the client, where player movement is computed. The modifier is transient (not
     * saved) and, as in 1.7.10, drops off at the player's next sprint toggle ({@link VelocityRaptorSpeed}, R26).
     */
    static void setVelocityRaptorSpeed(final Player player, final double amount) {
        VelocityRaptorSpeed.set(player, amount);
    }
}
