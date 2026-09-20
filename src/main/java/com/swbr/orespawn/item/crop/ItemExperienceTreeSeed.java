package com.swbr.orespawn.item.crop;

import com.swbr.orespawn.block.crop.CropBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.ItemExperienceTreeSeed} (ItemExperienceTreeSeed.java:12-42):
 * {@code experiencetree_seed} "Experience Tree Seed" (OreSpawnMain.java:1611), stack 1, creative
 * tab {@code tabDecorations} (:15-16). Places the sapling {@code experiencesapling}
 * ({@code BlockExperiencePlant}, w02-trees-crystal) one block above the clicked one; the sapling's
 * own random tick grows the tree ({@code Trees.ExperienceTree}, W12).
 */
public class ItemExperienceTreeSeed extends Item {

    /** {@code OreSpawnMain.MyExperiencePlant} by manifest id. */
    public static final ResourceKey<Block> EXPERIENCE_SAPLING = CropBlocks.blockKey("experiencesapling");

    /** {@code ItemExperienceTreeSeed(id)} (:14-17): {@code maxStackSize = 1}. */
    public ItemExperienceTreeSeed(final Item.Properties props) {
        super(props.stacksTo(1));
    }

    /**
     * {@code onItemUse} (:19-36). Server (:20-26): only on grass, dirt or farmland, else
     * {@code false}; then {@code setBlock(x, y + 1, z, sapling, 0, 2)} - <em>without</em> looking at
     * what is there, it overwrites (R18). Client (:27-31): ten {@code happyVillager} particles in
     * the block above, no soil check. Both sides: one seed less outside creative mode (:32-34),
     * {@code true} (:35).
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        if (!world.isClientSide) { // :20
            final BlockState bid = world.getBlockState(pos); // :21
            if (!CropBlocks.isGrassDirtOrFarmland(bid)) { // :22-24
                return InteractionResult.PASS;
            }
            final Block sapling = CropBlocks.block(EXPERIENCE_SAPLING);
            // PORT: the sapling is registered by w02-trees-crystal; a build without it would write
            // air here and delete whatever stands above. Refuse instead of overwriting with air.
            if (sapling.defaultBlockState().isAir()) {
                return InteractionResult.FAIL;
            }
            world.setBlock(pos.above(), sapling.defaultBlockState(), Block.UPDATE_CLIENTS); // :25 flag 2
        } else {
            for (int j1 = 0; j1 < 10; ++j1) { // :28-30
                world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        x + world.random.nextFloat(), y + 1.0 + world.random.nextFloat(), z + world.random.nextFloat(),
                        0.0, 0.0, 0.0);
            }
        }
        final ItemStack stack = context.getItemInHand();
        final Player player = context.getPlayer();
        // PORT: the player is nullable in 1.21.1; none counts as "not creative", as in ItemSpawnEgg.
        if (player == null || !player.getAbilities().instabuild) { // :32
            stack.shrink(1); // :33
        }
        return InteractionResult.sidedSuccess(world.isClientSide); // :35
    }
}
