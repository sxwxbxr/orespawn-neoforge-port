package com.swbr.orespawn.item.rock;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Port of {@code danger.orespawn.ItemSifter} (ItemSifter.java:14-475), registry id {@code sifter}
 * (legacy id 9325, OreSpawnMain.java:1420): stack 1, 600 uses, creative tab {@code tabDecorations}
 * (:16-20). Right-click on water (or on a block under water), sand, gravel, dirt or grass: one roll
 * of that block's table, at most one item, and one use of durability on every click, even on stone
 * and even when nothing drops (:463). Verhalten: itemblock-01.md "ItemSifter".
 *
 * <p>The tables stay Java switch statements, not loot tables as the catalogue proposed: the
 * original is code with a fall-through bug, extra 1/3 and 1/2 rolls and a shared position random,
 * and a data-driven copy would be a second truth (the same reasoning as DECISIONS R10).
 *
 * <p>1.7.10 block to 1.21.1 block. PORT: R22 - {@code sand}, {@code gravel} and {@code dirt} were
 * the sand, gravel and dirt categories of 1.7.10 (sand held red sand, dirt held coarse dirt and
 * podzol as metadata), so the port covers the 1.21.1 categories:
 * <ul>
 *   <li>sand table: {@code #minecraft:sand} - sand, red sand, suspicious sand.</li>
 *   <li>gravel table: gravel and suspicious gravel.</li>
 *   <li>dirt table: {@code #minecraft:dirt} (dirt, coarse dirt, podzol, rooted dirt, mud, moss,
 *       muddy mangrove roots, mycelium) <em>without</em> the grass block, which keeps its own
 *       table (:399-462).</li>
 *   <li>{@code Blocks.water} and {@code Blocks.flowing_water} are one block, {@code Blocks.WATER}.</li>
 * </ul>
 * Item metadata 0 maps as in the flattening: {@code fish} -> cod, {@code coal} -> coal,
 * {@code red_flower} -> poppy, {@code yellow_flower} -> dandelion, {@code sign} -> oak sign.
 */
public class ItemSifter extends Item {

    /** {@code ItemSifter(id)} (:16-20): {@code maxStackSize = 1}, {@code setMaxDamage(600)}. */
    public ItemSifter(final Item.Properties props) {
        super(props.durability(600)); // durability() also sets the stack size to 1
    }

    /**
     * {@code dropItemRand} (:22-25): one item entity at {@code x + rand(2) - rand(2) + 0.5},
     * {@code y + 1.1}, {@code z + rand(2) - rand(2) + 0.5} from the shared {@code OreSpawnRand}, so
     * the drop lands on the clicked column or one of its eight neighbours. Evaluation order of the
     * four rolls as in the original argument list.
     */
    private void dropItemRand(final ItemLike index, final int par1, final Level world, final int x, final int y, final int z) {
        final double px = x + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2) + 0.5;
        final double py = y + 1.1;
        final double pz = z + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2) + 0.5;
        final ItemStack stack = new ItemStack(index, par1);
        if (stack.isEmpty()) {
            // PORT: only reachable while an item of a later wave (the shoes) is not registered yet;
            // an empty item entity would be discarded on its first tick anyway.
            return;
        }
        final ItemEntity var3 = new ItemEntity(world, px, py, pz, stack);
        world.addFreshEntity(var3);
    }

    /**
     * {@code onItemUse} (:27-465). Client: {@code true} at once (:28-30). Server: the clicked block,
     * replaced by water when the block above is water (:31-38), then one table per block type
     * (:39-462), then one point of damage (:463).
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level par3World = context.getLevel();
        final Player par2EntityPlayer = context.getPlayer();
        final ItemStack par1ItemStack = context.getItemInHand();
        final BlockPos pos = context.getClickedPos();
        final int par4 = pos.getX();
        final int par5 = pos.getY();
        final int par6 = pos.getZ();
        if (par3World.isClientSide) { // :28-30
            return InteractionResult.sidedSuccess(true);
        }
        Block bid = par3World.getBlockState(pos).getBlock(); // :31
        final Block bid2 = par3World.getBlockState(pos.above()).getBlock(); // :32
        // :33-38 - "bid2 == flowing_water" and "bid2 == water": one block in 1.21.1. Clicking flowing
        // water directly did not count in 1.7.10; a use-on ray never targets a fluid, so the
        // difference is unreachable.
        if (bid2 == Blocks.WATER) {
            bid = Blocks.WATER;
        }
        if (bid == Blocks.WATER) { // :39-223
            final int i = par3World.random.nextInt(160);
            switch (i) {
                case 0: {
                    this.dropItemRand(Items.COD, 1, par3World, par4, par5, par6); // Items.fish
                    break;
                }
                case 1: {
                    this.dropItemRand(ModItems.GREEN_FISH.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 2: {
                    this.dropItemRand(ModItems.BLUE_FISH.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 3: {
                    this.dropItemRand(ModItems.PINK_FISH.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 4: {
                    this.dropItemRand(ModItems.ROCK_FISH.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 5: {
                    this.dropItemRand(ModItems.WOOD_FISH.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 6: {
                    this.dropItemRand(ModItems.GREY_FISH.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 7: {
                    this.dropItemRand(Items.GLASS_BOTTLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 8: {
                    this.dropItemRand(Items.IRON_INGOT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 9: {
                    this.dropItemRand(Items.GOLD_NUGGET, 1, par3World, par4, par5, par6);
                    break;
                }
                case 10: {
                    this.dropItemRand(shoes("redheels"), 1, par3World, par4, par5, par6); // MyItemShoes
                    break;
                }
                case 11: {
                    this.dropItemRand(shoes("blackheels"), 1, par3World, par4, par5, par6); // MyItemShoes_1
                    break;
                }
                case 12: {
                    this.dropItemRand(shoes("slippers"), 1, par3World, par4, par5, par6); // MyItemShoes_2
                    break;
                }
                case 13: {
                    this.dropItemRand(shoes("boots"), 1, par3World, par4, par5, par6); // MyItemShoes_3
                    break;
                }
                case 14: {
                    this.dropItemRand(Items.GLASS_BOTTLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 15: {
                    this.dropItemRand(Items.BONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 16: {
                    this.dropItemRand(Items.STONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 17: {
                    this.dropItemRand(Items.BUCKET, 1, par3World, par4, par5, par6);
                    break;
                }
                case 18: {
                    this.dropItemRand(Items.WATER_BUCKET, 1, par3World, par4, par5, par6);
                    break;
                }
                case 19: {
                    if (par3World.random.nextInt(3) == 1) {
                        this.dropItemRand(Items.EMERALD, 1, par3World, par4, par5, par6);
                        break;
                    }
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 20: {
                    if (par3World.random.nextInt(3) == 1) {
                        this.dropItemRand(ModItems.RUBY.get(), 1, par3World, par4, par5, par6);
                        break;
                    }
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 21: {
                    if (par3World.random.nextInt(3) == 1) {
                        this.dropItemRand(ModItems.AMETHYST.get(), 1, par3World, par4, par5, par6);
                        break;
                    }
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 22: {
                    this.dropItemRand(ModItems.MOTH_SCALE.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 23: {
                    this.dropItemRand(ModItems.URANIUM_NUGGET.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 24: {
                    this.dropItemRand(ModItems.TITANIUM_NUGGET.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 25: {
                    if (par3World.random.nextInt(2) == 1) {
                        this.dropItemRand(Items.DIAMOND, 1, par3World, par4, par5, par6);
                        break;
                    }
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 26: {
                    this.dropItemRand(Items.IRON_INGOT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 27: {
                    this.dropItemRand(Items.GOLD_NUGGET, 1, par3World, par4, par5, par6);
                    break;
                }
                case 28: {
                    this.dropItemRand(Items.REDSTONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 29: {
                    this.dropItemRand(Items.COAL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 30: {
                    this.dropItemRand(shoes("redheels"), 1, par3World, par4, par5, par6);
                    break;
                }
                case 31: {
                    this.dropItemRand(shoes("blackheels"), 1, par3World, par4, par5, par6);
                    break;
                }
                case 32: {
                    this.dropItemRand(shoes("slippers"), 1, par3World, par4, par5, par6);
                    break;
                }
                case 33: {
                    this.dropItemRand(shoes("boots"), 1, par3World, par4, par5, par6);
                    break;
                }
                case 34: {
                    this.dropItemRand(Items.COD, 1, par3World, par4, par5, par6);
                    break;
                }
                case 35: {
                    this.dropItemRand(Items.GLASS_BOTTLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 36: {
                    this.dropItemRand(Items.BONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 37: {
                    this.dropItemRand(Items.STONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 38: {
                    this.dropItemRand(Items.STONE_BUTTON, 1, par3World, par4, par5, par6);
                    break;
                }
                case 39: {
                    this.dropItemRand(Items.BUCKET, 1, par3World, par4, par5, par6);
                    break;
                }
                case 40: {
                    this.dropItemRand(Items.WATER_BUCKET, 1, par3World, par4, par5, par6);
                    break;
                }
            }
        }
        if (bid.defaultBlockState().is(BlockTags.SAND)) { // :224-284
            final int i = par3World.random.nextInt(60);
            switch (i) {
                case 0: {
                    this.dropItemRand(Items.IRON_HORSE_ARMOR, 1, par3World, par4, par5, par6);
                    break;
                }
                case 1: {
                    this.dropItemRand(Items.SHEARS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 2: {
                    this.dropItemRand(Items.CARROT_ON_A_STICK, 1, par3World, par4, par5, par6);
                    break;
                }
                case 3: {
                    this.dropItemRand(Items.POISONOUS_POTATO, 1, par3World, par4, par5, par6);
                    break;
                }
                case 4: {
                    this.dropItemRand(Items.ITEM_FRAME, 1, par3World, par4, par5, par6);
                    break;
                }
                case 5: {
                    this.dropItemRand(Items.BONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 6: {
                    this.dropItemRand(Items.COMPASS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 7: {
                    this.dropItemRand(Items.GLASS_BOTTLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 8: {
                    this.dropItemRand(Items.SADDLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 9: {
                    this.dropItemRand(Items.IRON_HELMET, 1, par3World, par4, par5, par6);
                    break;
                }
                case 10: {
                    this.dropItemRand(Items.IRON_CHESTPLATE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 11: {
                    this.dropItemRand(Items.IRON_LEGGINGS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 12: {
                    this.dropItemRand(Items.IRON_BOOTS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 13: {
                    this.dropItemRand(Items.SAND, 1, par3World, par4, par5, par6);
                    break;
                }
            }
        }
        if (bid == Blocks.GRAVEL || bid == Blocks.SUSPICIOUS_GRAVEL) { // :285-337
            final int i = par3World.random.nextInt(60);
            switch (i) {
                case 0: {
                    this.dropItemRand(Items.FLINT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 1: {
                    this.dropItemRand(ModItems.SALT.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 2: {
                    this.dropItemRand(Items.FLINT_AND_STEEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 3: {
                    this.dropItemRand(Items.SPIDER_EYE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 4: {
                    this.dropItemRand(Items.ITEM_FRAME, 1, par3World, par4, par5, par6);
                    break;
                }
                case 5: {
                    this.dropItemRand(Items.FEATHER, 1, par3World, par4, par5, par6);
                    break;
                }
                case 6: {
                    this.dropItemRand(Items.STRING, 1, par3World, par4, par5, par6);
                    break;
                }
                case 7: {
                    this.dropItemRand(Items.GLASS_BOTTLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 8: {
                    this.dropItemRand(Items.LEAD, 1, par3World, par4, par5, par6);
                    break;
                }
                case 9: {
                    this.dropItemRand(Items.NAME_TAG, 1, par3World, par4, par5, par6);
                    break;
                }
                case 10: {
                    this.dropItemRand(Items.SAND, 1, par3World, par4, par5, par6);
                    break;
                }
                case 11: {
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
            }
        }
        if (bid.defaultBlockState().is(BlockTags.DIRT) && bid != Blocks.GRASS_BLOCK) { // :338-398
            final int i = par3World.random.nextInt(60);
            switch (i) {
                case 0: {
                    this.dropItemRand(Items.STRING, 1, par3World, par4, par5, par6);
                    break;
                }
                case 1: {
                    this.dropItemRand(ModItems.SALT.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 2: {
                    this.dropItemRand(Items.SHEARS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 3: {
                    this.dropItemRand(Items.STICK, 1, par3World, par4, par5, par6);
                    break;
                }
                case 4: {
                    this.dropItemRand(Items.BOWL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 5: {
                    this.dropItemRand(Items.FLOWER_POT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 6: {
                    this.dropItemRand(Items.OAK_SIGN, 1, par3World, par4, par5, par6); // Items.sign
                    break;
                }
                case 7: {
                    this.dropItemRand(Items.BRICK, 1, par3World, par4, par5, par6);
                    break;
                }
                case 8: {
                    this.dropItemRand(Items.PAPER, 1, par3World, par4, par5, par6);
                    break;
                }
                case 9: {
                    this.dropItemRand(Items.BONE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 10: {
                    this.dropItemRand(Items.GLASS_BOTTLE, 1, par3World, par4, par5, par6);
                    break;
                }
                case 11: {
                    this.dropItemRand(Items.SAND, 1, par3World, par4, par5, par6);
                    break;
                }
                case 12: {
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 13: {
                    this.dropItemRand(Items.DIRT, 1, par3World, par4, par5, par6);
                    break;
                }
            }
        }
        if (bid == Blocks.GRASS_BLOCK) { // :399-462
            final int i = par3World.random.nextInt(60);
            switch (i) {
                case 0: {
                    this.dropItemRand(Items.DANDELION, 1, par3World, par4, par5, par6); // yellow_flower
                    break;
                }
                case 1: {
                    this.dropItemRand(Items.POPPY, 1, par3World, par4, par5, par6); // red_flower, meta 0
                    break;
                }
                case 2: {
                    this.dropItemRand(ModItems.FLOWER_PINK.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 3: {
                    this.dropItemRand(ModItems.FLOWER_BLUE.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 4: {
                    this.dropItemRand(ModItems.FLOWER_BLACK.get(), 1, par3World, par4, par5, par6);
                    break;
                }
                case 5: {
                    this.dropItemRand(ModItems.FLOWER_SCARY.get(), 1, par3World, par4, par5, par6);
                    // :422-424 - no break: falls through and drops wheat as well (R18, "Sifter-Weizen").
                }
                case 6: {
                    this.dropItemRand(Items.WHEAT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 7: {
                    this.dropItemRand(Items.PUMPKIN_SEEDS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 8: {
                    this.dropItemRand(Items.MELON_SEEDS, 1, par3World, par4, par5, par6);
                    break;
                }
                case 9: {
                    this.dropItemRand(Items.CARROT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 10: {
                    this.dropItemRand(Items.POTATO, 1, par3World, par4, par5, par6);
                    break;
                }
                case 11: {
                    this.dropItemRand(Items.DEAD_BUSH, 1, par3World, par4, par5, par6);
                    break;
                }
                case 12: {
                    this.dropItemRand(Items.GRAVEL, 1, par3World, par4, par5, par6);
                    break;
                }
                case 13: {
                    this.dropItemRand(Items.DIRT, 1, par3World, par4, par5, par6);
                    break;
                }
                case 14: {
                    this.dropItemRand(Items.GRASS_BLOCK, 1, par3World, par4, par5, par6); // Item.getItemFromBlock(Blocks.grass)
                    break;
                }
            }
        }
        // :463 damageItem(1, (EntityLivingBase) player): nothing in creative, Unbreaking applies.
        if (par2EntityPlayer != null) {
            par1ItemStack.hurtAndBreak(1, par2EntityPlayer, LivingEntity.getSlotForHand(context.getHand()));
        } else {
            // PORT: 1.7.10 dereferenced the null entity here (NPE, R18 case 1); without a player the
            // point of damage is still taken.
            par1ItemStack.hurtAndBreak(1, (ServerLevel) par3World, (LivingEntity) null, item -> {
            });
        }
        return InteractionResult.sidedSuccess(false); // :464
    }

    /** {@code getMaterialName} (:467-469). No caller in the original or in 1.21.1; kept for the class shape. */
    public String getMaterialName() {
        return "Unknown";
    }

    /**
     * {@code OreSpawnMain.MyItemShoes..MyItemShoes_3} (legacy 9248-9251). The four shoe items are
     * registered by the thrown-shoes port of the same wave; looking them up by id keeps this class
     * independent of that holder's field names. An unregistered id resolves to air, see
     * {@link #dropItemRand}.
     */
    private static Item shoes(final String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, id));
    }
}
