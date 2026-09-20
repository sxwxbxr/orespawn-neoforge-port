package com.swbr.orespawn.entity.cannonfodder;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/**
 * 1.7.10 to 1.21.1 mappings that {@link EntityCannonFodder}, {@link Chipmunk}, {@link Lizard} and
 * {@link Gazelle} share. None of the originals had a helper class - each repeated the lines - so this holds
 * translations only, never behaviour; every decision stays in the entity classes, in the original order.
 * Public so the other {@code EntityCannonFodder} subclasses (Ostrich, VelocityRaptor) can use the same
 * translations.
 */
public final class CannonFodderSupport {

    /**
     * 1.7.10 {@code setBlock(x, y, z, block, meta, 2)}: tell clients, no neighbour updates. In 1.21.1 flag 2
     * alone still runs the neighbour shape updates ({@code Level.setBlock}, bit 16 unset), so the 1.7.10
     * flag is {@code UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE} (W03 Miner's Dream, W05 Termite).
     */
    public static final int LEGACY_FLAG_2 = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private CannonFodderSupport() {}

    /**
     * {@code stack.getItem() == Items.dye}. 1.7.10 had one dye item with sixteen metadata values and the Lizard
     * compared the item only, so every value matched (catalogue 6.8, "frisst jeden Farbstoff"). The flattening split
     * them into these sixteen items: ink sac (0), red (1), green (2), cocoa beans (3), lapis lazuli (4), purple (5),
     * cyan (6), light gray (7), gray (8), pink (9), lime (10), yellow (11), light blue (12), magenta (13), orange (14),
     * bone meal (15).
     *
     * <p>PORT (R22): "dye" is a category, so the four dyes added in 1.14 (black, brown, blue, white) count as well,
     * through {@code #c:dyes}; the sixteen flattened items stay listed because ink sac, cocoa beans, lapis lazuli and
     * bone meal are not in that tag.
     */
    public static boolean isLegacyDye(final ItemStack stack) {
        return stack.is(Items.INK_SAC) || stack.is(Items.RED_DYE) || stack.is(Items.GREEN_DYE)
                || stack.is(Items.COCOA_BEANS) || stack.is(Items.LAPIS_LAZULI) || stack.is(Items.PURPLE_DYE)
                || stack.is(Items.CYAN_DYE) || stack.is(Items.LIGHT_GRAY_DYE) || stack.is(Items.GRAY_DYE)
                || stack.is(Items.PINK_DYE) || stack.is(Items.LIME_DYE) || stack.is(Items.YELLOW_DYE)
                || stack.is(Items.LIGHT_BLUE_DYE) || stack.is(Items.MAGENTA_DYE) || stack.is(Items.ORANGE_DYE)
                || stack.is(Items.BONE_MEAL)
                || stack.is(Tags.Items.DYES);
    }

    /**
     * {@code Blocks.dirt}: one block with the metadata values dirt (0), coarse dirt (1) and podzol (2), compared by
     * block identity.
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): "dirt" is a category, so the whole {@code #minecraft:dirt} tag as
     * 1.21.1 ships it counts - rooted dirt, moss, mud and muddy mangrove roots, and also the grass block and mycelium,
     * which were separate blocks in 1.7.10. Every caller that names {@code Blocks.grass} next to it (Molenoid dig and
     * {@code MyCanSee}, Gazelle spawn ground, the ostrich's sitting ground) treats grass exactly like dirt, so no
     * separate grass branch changes its result. Chipmunk (:91, dirt or farmland) has no grass branch; it now digs
     * grass and mycelium too, as the addendum rules for a caller without one.
     */
    public static boolean isLegacyDirt(final BlockState state) {
        return state.is(BlockTags.DIRT);
    }

    /**
     * {@code Blocks.tallgrass}. Meta 1 is {@code SHORT_GRASS}, meta 2 {@code FERN}. PORT: meta 0, the
     * "shrub", became {@code DEAD_BUSH} in the flattening; it is left out, because the real
     * {@code Blocks.deadbush} (a separate block the originals never matched) is the same block now and by far
     * the more common one.
     */
    public static boolean isLegacyTallGrass(final BlockState state) {
        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    /**
     * {@code Blocks.double_plant}: all six metadata values, upper and lower half - sunflower, lilac, tall grass, large
     * fern, rose bush, peony.
     *
     * <p>PORT (R22): the flowering half is the category {@code #minecraft:tall_flowers}, which adds the pitcher plant
     * (1.20); tall grass and large fern are not flowers and stay listed.
     */
    public static boolean isLegacyDoublePlant(final BlockState state) {
        return state.is(BlockTags.TALL_FLOWERS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.LARGE_FERN);
    }

    /**
     * {@code if (!capabilities.isCreativeMode) { --stackSize; if (stackSize <= 0) slot = null; }}; an emptied
     * stack reads as empty, which replaces the explicit {@code null}.
     */
    public static void consumeOne(final Player player, final ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /**
     * 1.7.10 {@code EntityAnimal.interact} ({@code wf.a(yz)} in client-1.7.10.jar): the breeding item with
     * growing age 0 and {@code inLove <= 0} is used up (unless creative) and starts the love mode
     * ({@code func_146082_f}: {@code inLove = 600}, entity state 18 - {@link Animal#setInLove}); otherwise
     * {@code EntityAgeable.interact}, which only reacted to a vanilla {@code ItemMonsterPlacer} of the same
     * class - no OreSpawn mob has such an egg, so it is {@code false}.
     *
     * <p>PORT: this replaces {@code Animal.mobInteract}, which also feeds babies ({@code ageUp}); 1.7.10 had
     * no such branch. Both sides run it, as the original did.
     */
    public static boolean animalInteract(final Animal animal, final Player player) {
        final ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.isEmpty() && animal.isFood(itemstack) && animal.getAge() == 0 && !animal.isInLove()) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            animal.setInLove(player);
            return true;
        }
        return false;
    }

    /**
     * The looting level 1.7.10 {@code EntityLivingBase.onDeath} passed to {@code dropFewItems}: only when the
     * damage came from a player ({@code instanceof EntityPlayer}, bytecode {@code sv.a(ro)} pc 64-78),
     * as {@code Firefly} (W05) reads it.
     */
    public static int lootingLevel(final ServerLevel level, final DamageSource damageSource) {
        if (damageSource.getEntity() instanceof Player killer) {
            return EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        return 0;
    }

    /**
     * 1.7.10 {@code EntityLiving.dropFewItems} ({@code sw.b(ZI)V}): {@code getDropItem()}; if not null,
     * {@code rand(3)} plus {@code rand(looting + 1)} when looting is above zero, each dropped as its own
     * stack of one with no offset ({@code dropItem(item, 1)}).
     */
    public static void vanillaDropFewItems(final LivingEntity entity, @Nullable final Item item, final int looting) {
        if (item != null) {
            int j = entity.getRandom().nextInt(3);
            if (looting > 0) {
                j += entity.getRandom().nextInt(looting + 1);
            }
            for (int k = 0; k < j; ++k) {
                entity.spawnAtLocation(new ItemStack(item, 1));
            }
        }
    }

    /**
     * {@code UUID.fromString(ownerName)} as 1.7.10 {@code EntityTameable.getOwner} did it: a name that is no
     * UUID gave no owner ({@code IllegalArgumentException} caught there). The originals only ever stored
     * {@code getUniqueID().toString()}, so this only matters for foreign NBT and the empty string.
     */
    @Nullable
    public static UUID parseOwner(@Nullable final String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
