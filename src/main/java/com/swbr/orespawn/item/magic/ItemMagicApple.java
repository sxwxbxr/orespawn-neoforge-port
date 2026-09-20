package com.swbr.orespawn.item.magic;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.crop.CropBlocks;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import com.swbr.orespawn.world.tree.LegacyRandom;
import com.swbr.orespawn.world.tree.LegacyWriter;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.ItemMagicApple} (ItemMagicApple.java:19-854): {@code magicapple}
 * "OMG! No! Don't do it!!!" (OreSpawnMain.java:1587), stack 1, tab Decorations (:31-32). Right-click on grass, dirt
 * or farmland turns the block to gold and grows a giant tree on it (verhalten/itemblock-01.md, "ItemMagicApple").
 *
 * <p>The three generators are public because {@code OreSpawnWorld.addHugeTree} (OreSpawnWorld.java:1902-1950)
 * grows the same trees in Utopia. They take the world as a {@link LegacyWriter}: the item writes into the live level,
 * the world generator through a {@code LegacyStructurePiece} ({@link com.swbr.orespawn.world.tree.TreeBuilders}) - the trees reach far beyond any feature window. The
 * original's {@code Chunk} parameter only decided whether {@code setBlockSuperFast} notified the client (always
 * {@code null} from the item); it is dropped, see {@link com.swbr.orespawn.world.util.FastBlocks#setBlockSuperFast}.
 *
 * <p>Random streams: {@code this.rand} is {@code OreSpawnMain.OreSpawnRand} (:29), passed as {@code rand};
 * {@code MakeBigRoundTree} and the entity yaw used {@code world.rand}, passed as {@code worldRand}.
 *
 * <p>Block categories (DECISIONS R22): {@code Blocks.tallgrass} → {@link CannonFodderSupport#isLegacyTallGrass};
 * {@code red_flower}/{@code yellow_flower} → {@code #minecraft:small_flowers}; {@code Blocks.leaves} →
 * {@code #minecraft:leaves}; {@code Blocks.stone} → {@code #minecraft:base_stone_overworld};
 * grass/dirt → {@link CropBlocks#isGrassDirtOrFarmland}. {@code Blocks.snow} is the snow <em>block</em>
 * (the layer was {@code snow_layer}). {@code Blocks.log}/{@code Blocks.leaves} with metadata {@code tree_type}
 * 0-3 are oak, spruce, birch, jungle.
 *
 * <p>Coordinate casts (DECISIONS R20): the absolute {@code (int)} casts of {@code MakeCirclularBranch},
 * {@code MakeBigRoundTree} and {@code MakeRoundBranch} are {@link Mth#floor}; the relative ring offsets
 * {@code (int) (rad * sin + 0.5)} of {@code MakeBigCircularTree} stay {@code (int)}, they are shape, not position.
 */
public class ItemMagicApple extends Item {

    /** {@code EntityList.createEntityByID(99, world)}: {@code EntityVillagerGolem}. */
    public static final String IRON_GOLEM = "minecraft:iron_golem";
    /** {@code EntityList} name "The King" (:476). */
    public static final String THE_KING = "orespawn:the_king";
    /** {@code EntityList} name "The Queen" (:486). */
    public static final String THE_QUEEN = "orespawn:the_queen";

    /** {@code onCreated} (:35-37) and {@code onUsingTick} (:50-55): Fortune II, sentinel Fortune. */
    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(PreEnchant.entry(Enchantments.FORTUNE, 2));

    /** {@code chestContentsList} (ItemMagicApple.java:30): 40 entries. */
    public static final WeightedRandomChestContent[] chestContentsList = {
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 1, 2, 3),
        WeightedRandomChestContent.byId("minecraft:diamond", 1, 5, 15),
        WeightedRandomChestContent.byId("minecraft:blaze_rod", 1, 3, 10),
        WeightedRandomChestContent.byId("orespawn:cageempty", 1, 10, 7),
        WeightedRandomChestContent.byId("orespawn:cagegirlfriend", 1, 2, 6),
        WeightedRandomChestContent.byId("minecraft:iron_ingot", 1, 10, 16),
        WeightedRandomChestContent.byId("minecraft:gold_ingot", 1, 6, 16),
        WeightedRandomChestContent.byId("orespawn:uranium_nugget", 1, 6, 6),
        WeightedRandomChestContent.byId("orespawn:titanium_nugget", 1, 4, 6),
        WeightedRandomChestContent.byId("minecraft:bread", 1, 8, 20),
        WeightedRandomChestContent.byId("minecraft:apple", 1, 8, 20),
        WeightedRandomChestContent.byId("minecraft:cookie", 1, 16, 20),
        WeightedRandomChestContent.byId("minecraft:cooked_beef", 1, 8, 20),
        WeightedRandomChestContent.byId("minecraft:cooked_chicken", 1, 8, 20),
        WeightedRandomChestContent.byId("minecraft:cooked_cod", 1, 8, 20), // Items.cooked_fished, damage 0
        WeightedRandomChestContent.byId("minecraft:cooked_porkchop", 1, 8, 20),
        WeightedRandomChestContent.byId("minecraft:pumpkin_pie", 1, 4, 20),
        WeightedRandomChestContent.byId("minecraft:carrot", 1, 16, 20),
        WeightedRandomChestContent.byId("minecraft:potato", 1, 16, 20),
        WeightedRandomChestContent.byId("orespawn:sunfish", 1, 4, 6),
        WeightedRandomChestContent.byId("orespawn:firefish", 1, 8, 6),
        WeightedRandomChestContent.byId("orespawn:popcorn_bag", 1, 4, 16),
        WeightedRandomChestContent.byId("minecraft:iron_pickaxe", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:iron_sword", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:diamond_pickaxe", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:diamond_sword", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:bow", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:arrow", 1, 64, 20),
        WeightedRandomChestContent.byId("orespawn:ultimatepickaxe", 1, 1, 2),
        WeightedRandomChestContent.byId("orespawn:ultimatesword", 1, 1, 1),
        WeightedRandomChestContent.byId("orespawn:ultimatefishingrod", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:iron_chestplate", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:iron_helmet", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:iron_leggings", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:iron_boots", 1, 1, 20),
        WeightedRandomChestContent.byId("minecraft:diamond_chestplate", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:diamond_helmet", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:diamond_leggings", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:diamond_boots", 1, 1, 5),
        WeightedRandomChestContent.byId("minecraft:golden_apple", 1, 1, 5),
    };

    /** {@code tree_radius = 6} (:27). */
    public int tree_radius;
    /** {@code no_critters} (:28): an instance field of the singleton, rolled on every use (:801-804). */
    public boolean no_critters;
    /** {@code rand = OreSpawnMain.OreSpawnRand} (:29). */
    final Random rand;

    /** {@code ItemMagicApple(id)} (:26-33): {@code maxStackSize = 1}. */
    public ItemMagicApple(final Item.Properties props) {
        super(props.stacksTo(1));
        this.tree_radius = 6;
        this.no_critters = false;
        this.rand = OreSpawn.OreSpawnRand;
    }

    /** {@code onCreated} (:35-37): {@code addEnchantment(fortune, 2)}. */
    @Override
    public void onCraftedBy(final ItemStack stack, final Level level, final Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    /** {@code onUpdate} (:57-59) calls {@code onUsingTick} (:50-55): Fortune again when it is missing (R7). */
    @Override
    public void inventoryTick(final ItemStack stack, final Level level, final Entity entity, final int slotId,
                              final boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.FORTUNE, ENCHANTMENTS);
    }

    // =====================================================================================================
    // Block predicates

    private static BlockState state(final Block id) {
        return id.defaultBlockState();
    }

    /**
     * {@code FastSetBlock(world, x, y, z, id, meta, 2, chunk)} (:790-792) -> {@code setBlockSuperFast}. Metadata only
     * mattered for {@code Blocks.log} and {@code Blocks.leaves} (wood type 0-3); every other block ignores it.
     */
    private static void FastSetBlock(final LegacyWriter world, final int ix, final int iy, final int iz, final Block id,
                                     final int im) {
        world.setBlockFast(ix, iy, iz, withType(id, im));
    }

    /** 1.7.10 {@code Blocks.log}/{@code Blocks.leaves} metadata & 3: oak, spruce, birch, jungle. */
    static BlockState withType(final Block id, final int meta) {
        if (id == Blocks.OAK_LOG) {
            return switch (meta & 3) {
                case 1 -> Blocks.SPRUCE_LOG.defaultBlockState();
                case 2 -> Blocks.BIRCH_LOG.defaultBlockState();
                case 3 -> Blocks.JUNGLE_LOG.defaultBlockState();
                default -> Blocks.OAK_LOG.defaultBlockState();
            };
        }
        if (id == Blocks.OAK_LEAVES) {
            return switch (meta & 3) {
                case 1 -> Blocks.SPRUCE_LEAVES.defaultBlockState();
                case 2 -> Blocks.BIRCH_LEAVES.defaultBlockState();
                case 3 -> Blocks.JUNGLE_LEAVES.defaultBlockState();
                default -> Blocks.OAK_LEAVES.defaultBlockState();
            };
        }
        return id.defaultBlockState();
    }

    /** {@code isBoringBlock} (:61-94): may a leaf or step overwrite this block. */
    private static boolean isBoringBlock(final LegacyWriter world, final int x, final int y, final int z) {
        final BlockState var1 = world.getBlock(x, y, z);
        if (CannonFodderSupport.isLegacyTallGrass(var1)) {
            return true;
        }
        if (var1.is(Blocks.CACTUS)) {
            return true;
        }
        if (var1.is(BlockTags.SMALL_FLOWERS)) { // red_flower and yellow_flower
            return true;
        }
        if (var1.is(BlockTags.LEAVES)) {
            return true;
        }
        if (var1.is(Blocks.SNOW_BLOCK)) {
            return true;
        }
        if (var1.is(ModBlocks.STRAWBERRY_PLANT.get())) {
            return true;
        }
        if (var1.is(ModBlocks.LEAVES_APPLE.get())) {
            return true;
        }
        return var1.isAir();
    }

    /** {@code isBoringBaseBlock} (:96-108): anything but stone and bedrock may be overwritten by trunk and base. */
    private static boolean isBoringBaseBlock(final LegacyWriter world, final int x, final int y, final int z) {
        if (world.isAirBlock(x, y, z)) {
            return true;
        }
        final BlockState var1 = world.getBlock(x, y, z);
        if (var1.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return false;
        }
        return !var1.is(Blocks.BEDROCK);
    }

    /**
     * {@code spawnCreature(world, id, x, y, z)} (:39-48): create by numeric id (only 99, the iron golem), yaw from
     * {@code world.rand}, spawn, living sound.
     */
    @Nullable
    private static Entity spawnCreature(final LegacyWriter par0World, final Random worldRand, final String par1,
                                        final double par2, final double par4, final double par6) {
        return par0World.spawnEntity(par1, par2, par4, par6, worldRand);
    }

    /**
     * {@code growVines} (:110-123): a vine column into air from the start down, {@code par6} more below. Vine
     * metadata bit 1 south, 2 west, 4 north, 8 east (the face it clings to); the callers pass single bits.
     */
    private static void growVines(final LegacyWriter world, final int par2, int par3, final int par4, final int par5,
                                  int par6) {
        if (!world.getBlock(par2, par3, par4).isAir()) {
            return;
        }
        final BlockState vine = vine(par5);
        world.setBlockFast(par2, par3, par4, vine);
        while (par6 > 0) {
            --par3;
            if (!world.getBlock(par2, par3, par4).isAir()) {
                return;
            }
            world.setBlockFast(par2, par3, par4, vine);
            --par6;
        }
    }

    private static BlockState vine(final int meta) {
        BlockState vine = Blocks.VINE.defaultBlockState();
        for (int bit = 0; bit < 4; ++bit) {
            if ((meta & (1 << bit)) != 0) {
                // 2D data value 0 south, 1 west, 2 north, 3 east - the order of the 1.7.10 vine bits.
                vine = vine.setValue(VineBlock.getPropertyForFace(Direction.from2DDataValue(bit)), true);
            }
        }
        return vine;
    }

    // =====================================================================================================
    // Square tree

    /** {@code make_branch} (:125-249): recursive horizontal branch with chests, iron golems, leaf tufts and vines. */
    private static void make_branch(final LegacyWriter world, final Random rand, final Random worldRand, final int x,
                                    final int y, final int z, final int this_width, final int dirx, final int dirz,
                                    final Block ID, final Block leafID, final int tree_type, final int t_radius,
                                    final boolean bad_critters) {
        int current_width = this_width;
        int last_branch = 0;
        int branch_side = 1;
        int leaf_depth = 0;
        int leaf_width = 0;
        int xaccum = dirx;
        int zaccum = dirz;
        if (rand.nextInt(2) == 0) {
            branch_side = -1;
        }
        while (current_width >= 0) {
            for (int length = this_width * 3 + rand.nextInt(this_width + 3), i = 0; i < length; ++i) {
                for (int j = -current_width; j <= current_width; ++j) {
                    final int realx = x + j * dirz + xaccum;
                    final int realz = z + j * dirx + zaccum;
                    if (isBoringBlock(world, realx, y, realz)) {
                        if (tree_type >= 0) {
                            FastSetBlock(world, realx, y, realz, ID, tree_type);
                        } else {
                            FastSetBlock(world, realx, y, realz, ID, 0);
                        }
                    }
                    if (i > 0 && j == 0 && current_width >= 3) {
                        if ((tree_type >= 0 && rand.nextInt(75) == 0) || (tree_type < 0 && rand.nextInt(50) == 0)) {
                            if (!bad_critters && world.isAirBlock(realx, y + 1, realz)) {
                                final Container chest = world.placeChest(realx, y + 1, realz);
                                // Original: if (chest != null) ... PORT: drawn in any case (LegacyWriter#placeChest).
                                LegacyWriter.generateChestContents(rand, chestContentsList, chest, 1 + rand.nextInt(8));
                            }
                        } else if (rand.nextInt(50) == 0 && !bad_critters && world.isAirBlock(realx, y + 1, realz)
                                && world.isAirBlock(realx, y + 2, realz) && world.isAirBlock(realx, y + 3, realz)) {
                            spawnCreature(world, worldRand, IRON_GOLEM, realx + 0.5, y + 1.01, realz + 0.5);
                        }
                    }
                }
                if (current_width < 3 || this_width <= 1) {
                    leaf_depth = 2 + rand.nextInt(2);
                    leaf_width = 2 + rand.nextInt(3);
                    for (int n = 0; n < leaf_depth; ++n) {
                        int lw = current_width + leaf_width - n;
                        if (current_width == 0 && length - i <= 2 && lw >= length - i) {
                            lw = length - i - 1;
                        }
                        if (lw < 0) {
                            lw = 0;
                        }
                        for (int j = -lw; j <= lw; ++j) {
                            final int realx = x + j * Math.abs(dirz) + xaccum + dirx;
                            final int realz = z + j * Math.abs(dirx) + zaccum + dirz;
                            if (isBoringBlock(world, realx, y + n, realz)) {
                                if (tree_type >= 0) {
                                    FastSetBlock(world, realx, y + n, realz, leafID, tree_type);
                                    if (n == 0 && tree_type == 3 && lw != 0 && (j == lw || j == -lw) && rand.nextInt(5) == 0) {
                                        if (dirx == 0) {
                                            if (j == lw) {
                                                growVines(world, realx + 1, y, realz, 2, rand.nextInt(10));
                                            } else {
                                                growVines(world, realx - 1, y, realz, 8, rand.nextInt(10));
                                            }
                                        } else if (j == lw) {
                                            growVines(world, realx, y, realz + 1, 4, rand.nextInt(10));
                                        } else {
                                            growVines(world, realx, y, realz - 1, 1, rand.nextInt(10));
                                        }
                                    }
                                } else {
                                    Block local_leaf_type = leafID;
                                    if (rand.nextInt(20) == 1) {
                                        if (rand.nextInt(3) != 0) {
                                            local_leaf_type = Blocks.REDSTONE_BLOCK;
                                        } else {
                                            final int ilt = rand.nextInt(4);
                                            if (ilt == 0) {
                                                local_leaf_type = ModBlocks.BLOCKURANIUM.get();
                                            }
                                            if (ilt == 1) {
                                                local_leaf_type = ModBlocks.BLOCKTITANIUM.get();
                                            }
                                            if (ilt == 2) {
                                                local_leaf_type = ModBlocks.BLOCKRUBY.get();
                                            }
                                            if (ilt == 3) {
                                                local_leaf_type = ModBlocks.BLOCKAMETHYST.get();
                                            }
                                        }
                                    }
                                    FastSetBlock(world, realx, y + n, realz, local_leaf_type, 0);
                                }
                            }
                        }
                    }
                }
                if (current_width > 0 && last_branch > current_width && current_width != this_width
                        && rand.nextInt(current_width + 1) == 0) {
                    int subdirx = branch_side;
                    int subdirz = 0;
                    if (dirx != 0) {
                        subdirx = 0;
                        subdirz = branch_side;
                    }
                    make_branch(world, rand, worldRand, x + xaccum + current_width * subdirx, y,
                            z + zaccum + current_width * subdirz, current_width - 1, subdirx, subdirz, ID, leafID,
                            tree_type, t_radius, bad_critters);
                    last_branch = 0;
                    if (branch_side < 0) {
                        branch_side = 1;
                    } else {
                        branch_side = -1;
                    }
                }
                xaccum += dirx;
                zaccum += dirz;
                ++last_branch;
            }
            --current_width;
        }
    }

    /** Trunk or foundation block: {@code ID} with {@code tree_type} when it is at least 0, else metadata 0. */
    private static void setTrunk(final LegacyWriter world, final int x, final int y, final int z, final Block ID,
                                 final int tree_type) {
        if (tree_type >= 0) {
            FastSetBlock(world, x, y, z, ID, tree_type);
        } else {
            FastSetBlock(world, x, y, z, ID, 0);
        }
    }

    /**
     * {@code MakeBigSquareTree} (:251-496): hollow square trunk narrowing from {@code t_radius} to 0 with a spiral
     * stair of {@code stepID} outside, floors with a chest where the spiral passes 0, branches above the first
     * stage, two emerald blocks on top. With a diamond stair The King spawns up there, with an amethyst stair The
     * Queen (both W10).
     *
     * @param rand      {@code this.rand} ({@code OreSpawnRand})
     * @param worldRand {@code world.rand} (iron golem and boss yaw)
     */
    public static void MakeBigSquareTree(final LegacyWriter world, final Random rand, final Random worldRand, final int x,
                                         final int y, final int z, final Block ID, final Block leafID,
                                         final Block stepID, final int tree_type, final int t_radius,
                                         final boolean bad_critters) {
        int this_height = t_radius + rand.nextInt(t_radius);
        int this_width = t_radius;
        int base_height = t_radius * 3;
        int spiral = 0;
        int current_y = 0;
        @SuppressWarnings("unused")
        final int branch = 0;
        int do_floor = 0;
        int platform_looper = 1;
        int last = -1;
        int last_last = -1;
        for (int i = -t_radius; i <= t_radius; ++i) {
            if (isBoringBaseBlock(world, x + i, y, z - t_radius)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j > 0) { // the 1.7.10 world floor, literal
                        if (!isBoringBaseBlock(world, x + i, y - j, z - t_radius)) {
                            break;
                        }
                        setTrunk(world, x + i, y - j, z - t_radius, ID, tree_type);
                    }
                }
            }
            if (isBoringBaseBlock(world, x + i, y, z + t_radius)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j > 0) {
                        if (!isBoringBaseBlock(world, x + i, y - j, z + t_radius)) {
                            break;
                        }
                        setTrunk(world, x + i, y - j, z + t_radius, ID, tree_type);
                    }
                }
            }
            if (isBoringBaseBlock(world, x - t_radius, y, z + i)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j > 0) {
                        if (!isBoringBaseBlock(world, x - t_radius, y - j, z + i)) {
                            break;
                        }
                        setTrunk(world, x - t_radius, y - j, z + i, ID, tree_type);
                    }
                }
            }
            if (isBoringBaseBlock(world, x + t_radius, y, z + i)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j > 0) {
                        if (!isBoringBaseBlock(world, x + t_radius, y - j, z + i)) {
                            break;
                        }
                        setTrunk(world, x + t_radius, y - j, z + i, ID, tree_type);
                    }
                }
            }
        }
        current_y = y;
        do_floor = 0;
        spiral = -this_width;
        while (this_width >= 0) {
            if (this_width != t_radius) {
                base_height = 0;
            }
            for (int j = 0; j < this_height + base_height; ++j) {
                do_floor = 0;
                for (int i = -this_width; i <= this_width; ++i) {
                    if (isBoringBaseBlock(world, x + i, current_y, z - this_width)) {
                        setTrunk(world, x + i, current_y, z - this_width, ID, tree_type);
                    }
                    if (isBoringBaseBlock(world, x + i, current_y, z + this_width)) {
                        setTrunk(world, x + i, current_y, z + this_width, ID, tree_type);
                    }
                    if (isBoringBaseBlock(world, x - this_width, current_y, z + i)) {
                        setTrunk(world, x - this_width, current_y, z + i, ID, tree_type);
                    }
                    if (isBoringBaseBlock(world, x + this_width, current_y, z + i)) {
                        setTrunk(world, x + this_width, current_y, z + i, ID, tree_type);
                    }
                }
                if (this_width != 0 || j < this_height / 2) {
                    platform_looper = 1;
                    if ((spiral == 0 && this_width >= 2) || spiral == this_width
                            || (spiral == this_width - 1 && j == this_height + base_height - 1)) {
                        ++platform_looper;
                        if (spiral != 0 && this_width >= 3) {
                            ++platform_looper;
                        }
                        if (spiral == 0) {
                            do_floor = 1;
                        }
                    }
                    for (int k = 0; k < platform_looper; ++k) {
                        if (isBoringBlock(world, x - spiral, current_y, z - this_width - 1)) {
                            FastSetBlock(world, x - spiral, current_y, z - this_width - 1, stepID, 0);
                        }
                        if (isBoringBlock(world, x + spiral, current_y, z + this_width + 1)) {
                            FastSetBlock(world, x + spiral, current_y, z + this_width + 1, stepID, 0);
                        }
                        if (isBoringBlock(world, x - this_width - 1, current_y, z + spiral)) {
                            FastSetBlock(world, x - this_width - 1, current_y, z + spiral, stepID, 0);
                        }
                        if (isBoringBlock(world, x + this_width + 1, current_y, z - spiral)) {
                            FastSetBlock(world, x + this_width + 1, current_y, z - spiral, stepID, 0);
                        }
                        if (this_width >= 3) {
                            if (isBoringBlock(world, x - spiral, current_y, z - this_width - 2)) {
                                FastSetBlock(world, x - spiral, current_y, z - this_width - 2, stepID, 0);
                            }
                            if (isBoringBlock(world, x + spiral, current_y, z + this_width + 2)) {
                                FastSetBlock(world, x + spiral, current_y, z + this_width + 2, stepID, 0);
                            }
                            if (isBoringBlock(world, x - this_width - 2, current_y, z + spiral)) {
                                FastSetBlock(world, x - this_width - 2, current_y, z + spiral, stepID, 0);
                            }
                            if (isBoringBlock(world, x + this_width + 2, current_y, z - spiral)) {
                                FastSetBlock(world, x + this_width + 2, current_y, z - spiral, stepID, 0);
                            }
                        }
                        if (platform_looper != 1) {
                            ++spiral;
                        }
                    }
                    if (do_floor != 0) {
                        for (int m = -this_width; m <= this_width; ++m) {
                            for (int n = -this_width; n <= this_width; ++n) {
                                if (isBoringBlock(world, x + m, current_y, z + n)) {
                                    setTrunk(world, x + m, current_y, z + n, ID, tree_type);
                                    if (m == 0 && n == 0 && rand.nextInt(2) == 0 && !bad_critters
                                            && world.isAirBlock(x, current_y + 1, z)) {
                                        final Container chest = world.placeChest(x, current_y + 1, z);
                                        LegacyWriter.generateChestContents(rand, chestContentsList, chest,
                                                t_radius - this_width + rand.nextInt(10));
                                    }
                                }
                            }
                        }
                    }
                }
                if (this_width != t_radius) {
                    int next;
                    for (next = rand.nextInt(4 + this_width); next == last || next == last_last;
                            next = rand.nextInt(4 + this_width)) {
                    }
                    if (next < 4) {
                        last_last = last;
                        last = next;
                    }
                    switch (next) {
                        case 0:
                            make_branch(world, rand, worldRand, x + this_width, current_y, z, this_width, 1, 0, ID, leafID,
                                    tree_type, t_radius, bad_critters);
                            break;
                        case 1:
                            make_branch(world, rand, worldRand, x - this_width, current_y, z, this_width, -1, 0, ID, leafID,
                                    tree_type, t_radius, bad_critters);
                            break;
                        case 2:
                            make_branch(world, rand, worldRand, x, current_y, z + this_width, this_width, 0, 1, ID, leafID,
                                    tree_type, t_radius, bad_critters);
                            break;
                        case 3:
                            make_branch(world, rand, worldRand, x, current_y, z - this_width, this_width, 0, -1, ID, leafID,
                                    tree_type, t_radius, bad_critters);
                            break;
                        default:
                            break;
                    }
                }
                ++current_y;
                if (do_floor == 0) {
                    ++spiral;
                }
                if (spiral > this_width) {
                    spiral = -this_width;
                }
            }
            --this_width;
            if (Math.abs(spiral) > this_width) {
                spiral = -this_width;
            }
            this_height += rand.nextInt(t_radius);
        }
        if (isBoringBaseBlock(world, x, current_y, z)) {
            FastSetBlock(world, x, current_y, z, Blocks.EMERALD_BLOCK, 0);
            FastSetBlock(world, x, current_y + 1, z, Blocks.EMERALD_BLOCK, 0);
            if (stepID == Blocks.DIAMOND_BLOCK) {
                // :480-481 ((TheKing) var8).setGuardMode(1). PORT: set before the add - in world generation
                // ProtoChunk.addEntity serialises the entity at once (LegacyWriter.spawnEntity beforeAdd).
                world.spawnEntity(THE_KING, x, current_y + 4, z, worldRand, ent -> {
                    if (ent instanceof com.swbr.orespawn.entity.boss.king.TheKing king) {
                        king.setGuardMode(1);
                    }
                });
            }
            if (stepID == ModBlocks.BLOCKAMETHYST.get()) {
                // :490-492 ((TheQueen) var8).setGuardMode(1); setBadMood(1). PORT: set before the add, as for the King.
                world.spawnEntity(THE_QUEEN, x, current_y + 4, z, worldRand, ent -> {
                    if (ent instanceof com.swbr.orespawn.entity.boss.queen.TheQueen queen) {
                        queen.setGuardMode(1);
                        queen.setBadMood(1);
                    }
                });
            }
        }
    }

    // =====================================================================================================
    // Circular tree

    /** {@code MakeCirclularBranch} (:498-567): a curling flat branch, log core, leaves at the rim and on top. */
    private static void MakeCirclularBranch(final LegacyWriter world, final int iangle, final int branchlen, final int width,
                                            final int startx, final int starty, final int startz, final int twist,
                                            final Block ID, final Block leafID, final int tree_type) {
        double curlen = 0.0;
        int curangle = iangle;
        double curx = startx;
        double curz = startz;
        for (curlen = 0.0; curlen < branchlen; curlen += 0.5) {
            curx += 0.5 * Math.sin(Math.toRadians(curangle));
            curz += 0.5 * Math.cos(Math.toRadians(curangle));
            for (double tw = width - width * curlen / branchlen, wd = 0.0; wd <= tw; wd += 0.5) {
                Block id = leafID;
                if (wd < tw / 2.0) {
                    id = ID;
                }
                if (tw < 0.9) {
                    id = leafID;
                }
                int ta = curangle + 90;
                if (ta > 360) {
                    ta -= 360;
                }
                double wx = curx + wd * Math.sin(Math.toRadians(ta));
                double wz = curz + wd * Math.cos(Math.toRadians(ta));
                // PORT: (int) wx / (int) wz -> Mth.floor (DECISIONS R20), here and below.
                if (isBoringBlock(world, Mth.floor(wx), starty, Mth.floor(wz))) {
                    setTrunk(world, Mth.floor(wx), starty, Mth.floor(wz), id, tree_type);
                }
                if (id == ID && isBoringBlock(world, Mth.floor(wx), starty + 1, Mth.floor(wz))) {
                    setTrunk(world, Mth.floor(wx), starty + 1, Mth.floor(wz), leafID, tree_type);
                }
                ta = curangle - 90;
                if (ta < 0) {
                    ta += 360;
                }
                wx = curx + wd * Math.sin(Math.toRadians(ta));
                wz = curz + wd * Math.cos(Math.toRadians(ta));
                if (isBoringBlock(world, Mth.floor(wx), starty, Mth.floor(wz))) {
                    setTrunk(world, Mth.floor(wx), starty, Mth.floor(wz), id, tree_type);
                }
                if (id == ID && isBoringBlock(world, Mth.floor(wx), starty + 1, Mth.floor(wz))) {
                    setTrunk(world, Mth.floor(wx), starty + 1, Mth.floor(wz), leafID, tree_type);
                }
            }
            curangle += twist;
            if (curangle < 0) {
                curangle += 360;
            }
            if (curangle >= 360) {
                curangle -= 360;
            }
        }
    }

    /**
     * {@code MakeBigCircularTree} (:569-674): a ring trunk shrinking by {@code 0.01 * rand(15)} per level, a 3x3 step
     * plate winding round it, one curling branch per level above the radius, a solid floor with a chest (1/2) every
     * sixth level, a diamond block on top.
     *
     * @param rand {@code this.rand} ({@code OreSpawnRand}); this method drew nothing from {@code world.rand}
     */
    public static void MakeBigCircularTree(final LegacyWriter world, final Random rand, final int x, final int y,
                                           final int z, final Block ID, final Block leafID, final Block stepID,
                                           final int tree_type, final int t_radius, final boolean bad_critters) {
        double rad = t_radius;
        int curx = 0;
        int cury = 0;
        int curz = 0;
        int stepindex = rand.nextInt(360);
        int ibranch = 0;
        cury = y;
        for (int i = 0; i < 360; ++i) {
            double dt = rad * Math.sin(Math.toRadians(i)) + 0.5;
            curx = (int) dt;
            dt = rad * Math.cos(Math.toRadians(i)) + 0.5;
            curz = (int) dt;
            if (isBoringBaseBlock(world, x + curx, cury, z + curz)) {
                for (int j = 0; j < 20; ++j) {
                    if (cury - j > 0) {
                        if (!isBoringBaseBlock(world, x + curx, cury - j, z + curz)) {
                            break;
                        }
                        setTrunk(world, x + curx, cury - j, z + curz, ID, tree_type);
                    }
                }
            }
        }
        cury = 1;
        while (rad > 0.0) {
            for (int i = 0; i < 360; ++i) {
                double dt = rad * Math.sin(Math.toRadians(i)) + 0.5;
                curx = (int) dt;
                dt = rad * Math.cos(Math.toRadians(i)) + 0.5;
                curz = (int) dt;
                if (isBoringBaseBlock(world, x + curx, y + cury, z + curz)) {
                    setTrunk(world, x + curx, y + cury, z + curz, ID, tree_type);
                }
                if (i >= stepindex - 1 && i <= stepindex + 1 && rad > 1.0) {
                    dt = (rad + 1.9) * Math.sin(Math.toRadians(i)) + 0.5;
                    curx = (int) dt;
                    dt = (rad + 1.9) * Math.cos(Math.toRadians(i)) + 0.5;
                    curz = (int) dt;
                    for (int m = -1; m <= 1; ++m) {
                        for (int n = -1; n <= 1; ++n) {
                            if (isBoringBaseBlock(world, x + curx + m, y + cury, z + curz + n)) {
                                FastSetBlock(world, x + curx + m, y + cury, z + curz + n, stepID, 0);
                            }
                        }
                    }
                }
            }
            if (cury > (int) rad) {
                ibranch += 80 + rand.nextInt(80);
                if (ibranch > 360) {
                    ibranch -= 360;
                }
                final int ibranchlen = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);
                double dt = rad * Math.sin(Math.toRadians(ibranch)) + 0.5;
                curx = (int) dt;
                dt = rad * Math.cos(Math.toRadians(ibranch)) + 0.5;
                curz = (int) dt;
                final int twist = rand.nextInt(2) * ((rand.nextInt(2) == 0) ? -1 : 1);
                MakeCirclularBranch(world, ibranch, ibranchlen, (int) rad + 1, x + curx, y + cury, z + curz, twist, ID,
                        leafID, tree_type);
            }
            if (cury % 6 == 0 && rad > 3.0) {
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
                    for (int i = 0; i < 360; ++i) {
                        double dt = dr * Math.sin(Math.toRadians(i)) + 0.5;
                        curx = (int) dt;
                        dt = dr * Math.cos(Math.toRadians(i)) + 0.5;
                        curz = (int) dt;
                        if (isBoringBaseBlock(world, x + curx, y + cury, z + curz)) {
                            setTrunk(world, x + curx, y + cury, z + curz, ID, tree_type);
                        }
                    }
                }
                if (rand.nextInt(2) == 0 && !bad_critters && world.isAirBlock(x, y + cury + 1, z)) {
                    final Container chest = world.placeChest(x, y + cury + 1, z);
                    LegacyWriter.generateChestContents(rand, chestContentsList, chest,
                            t_radius - (int) rad + rand.nextInt(10));
                }
            }
            stepindex += 15 + (int) ((t_radius - rad) * 3.0);
            if (stepindex > 360) {
                stepindex -= 360;
            }
            ++cury;
            rad -= 0.01 * rand.nextInt(15);
            if (rad <= 0.0 && isBoringBaseBlock(world, x, y + cury, z)) {
                FastSetBlock(world, x, y + cury, z, Blocks.DIAMOND_BLOCK, 0);
            }
        }
    }

    // =====================================================================================================
    // Round tree

    /**
     * {@code MakeBigRoundTree} (:676-758): the circular tree with {@code world.rand}, without steps and chests; the
     * branches are flat discs ({@link #MakeRoundBranch}). {@code stepID} is not used.
     *
     * @param worldRand {@code world.rand}
     */
    public static void MakeBigRoundTree(final LegacyWriter world, final Random worldRand, final int inx, final int y,
                                        final int inz, final Block ID, final Block leafID, final Block stepID,
                                        final int tree_type, final int t_radius) {
        double rad = t_radius;
        int cury = 0;
        int ibranch = 0;
        float fx = (float) inx;
        fx += 0.5f;
        float fz = (float) inz;
        fz += 0.5f;
        cury = y;
        for (int i = 0; i < 360; ++i) {
            double dt = rad * Math.sin(Math.toRadians(i));
            final float fcurx = (float) dt;
            dt = rad * Math.cos(Math.toRadians(i));
            final float fcurz = (float) dt;
            // PORT: (int) (fx + fcurx) -> Mth.floor (DECISIONS R20), throughout this method.
            if (isBoringBaseBlock(world, Mth.floor(fx + fcurx), cury, Mth.floor(fz + fcurz))) {
                for (int j = 0; j < 20; ++j) {
                    if (cury - j > 0) {
                        if (!isBoringBaseBlock(world, Mth.floor(fx + fcurx), cury - j, Mth.floor(fz + fcurz))) {
                            break;
                        }
                        setTrunk(world, Mth.floor(fx + fcurx), cury - j, Mth.floor(fz + fcurz), ID, tree_type);
                    }
                }
            }
        }
        cury = 1;
        while (rad > 0.0) {
            for (int i = 0; i < 360; ++i) {
                double dt = rad * Math.sin(Math.toRadians(i));
                final float fcurx = (float) dt;
                dt = rad * Math.cos(Math.toRadians(i));
                final float fcurz = (float) dt;
                if (isBoringBaseBlock(world, Mth.floor(fx + fcurx), y + cury, Mth.floor(fz + fcurz))) {
                    setTrunk(world, Mth.floor(fx + fcurx), y + cury, Mth.floor(fz + fcurz), ID, tree_type);
                }
            }
            if (cury > (int) rad) {
                ibranch += 80 + worldRand.nextInt(80);
                if (ibranch > 360) {
                    ibranch -= 360;
                }
                final int ibranchlen = (int) (rad * 5.0) + worldRand.nextInt((int) rad + 2);
                double dt = rad * Math.sin(Math.toRadians(ibranch));
                final float fcurx = (float) dt;
                dt = rad * Math.cos(Math.toRadians(ibranch));
                final float fcurz = (float) dt;
                MakeRoundBranch(world, ibranch, ibranchlen, (int) rad + 1, fx + fcurx, y + cury, fz + fcurz, ID, leafID,
                        tree_type);
            }
            if (cury % 6 == 0 && rad > 3.0) {
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
                    for (int i = 0; i < 360; ++i) {
                        double dt = dr * Math.sin(Math.toRadians(i));
                        final float fcurx = (float) dt;
                        dt = dr * Math.cos(Math.toRadians(i));
                        final float fcurz = (float) dt;
                        if (isBoringBaseBlock(world, Mth.floor(fx + fcurx), y + cury, Mth.floor(fz + fcurz))) {
                            setTrunk(world, Mth.floor(fx + fcurx), y + cury, Mth.floor(fz + fcurz), ID, tree_type);
                        }
                    }
                }
            }
            ++cury;
            rad -= 0.01 * worldRand.nextInt(15);
            if (rad <= 0.0 && isBoringBaseBlock(world, Mth.floor(fx), y + cury, Mth.floor(fz))) {
                FastSetBlock(world, Mth.floor(fx), y + cury, Mth.floor(fz), Blocks.DIAMOND_BLOCK, 0);
            }
        }
    }

    /**
     * {@code MakeRoundBranch} (:760-788): a flat disc of radius {@code branchlen / 2} centred that far out along the
     * branch angle; leaves in the outer two blocks. {@code FastSetBlock} gets {@code tree_type} as metadata here even
     * for the leaves of other types - only log and leaves read it.
     */
    private static void MakeRoundBranch(final LegacyWriter world, final int iangle, final int branchlen, final int width,
                                        final float startx, final int starty, final float startz, final Block ID,
                                        final Block leafID, final int tree_type) {
        final double deltadir = 0.06283185200000001;
        final double deltamag = 0.3499999940395355;
        int ixlast = 0;
        int izlast = 0;
        final int radius = branchlen / 2;
        final float centerx = (float) (startx + radius * Math.sin(Math.toRadians(iangle)));
        final float centerz = (float) (startz + radius * Math.cos(Math.toRadians(iangle)));
        izlast = (ixlast = 0);
        for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
            for (double h = 0.75; h < radius; h += deltamag) {
                // PORT: (int) -> Mth.floor (DECISIONS R20).
                final int ix = Mth.floor(centerx + Math.cos(curdir) * h);
                final int iz = Mth.floor(centerz + Math.sin(curdir) * h);
                if (ix != ixlast || iz != izlast) {
                    ixlast = ix;
                    izlast = iz;
                    Block id = ID;
                    if (radius - h < 2.0) {
                        id = leafID;
                    }
                    if (isBoringBlock(world, ix, starty, iz)) {
                        FastSetBlock(world, ix, starty, iz, id, tree_type);
                    }
                }
            }
        }
    }

    // =====================================================================================================
    // Use

    /**
     * {@code onItemUse} (:794-848). Both sides: soil check (grass, farmland, dirt), {@code tree_type} and
     * {@code no_critters} from {@code OreSpawnRand} (:799-804), six of each smoke, explosion and redstone dust
     * particle, the explosion sound, one apple less outside creative. Server: the block turns to gold (:806), then
     * {@code rand_treetype = nextInt(100)} (:815-842): 40-99 square (1/10 apple leaves unless jungle), 20-39 round,
     * exactly 1 the Ginormous tree - 1 % (catalogue 6.8, the research's 50 % is wrong) - when
     * {@code GinormousEmeraldTreeEnable} is set (half gold/emerald/diamond, half obsidian/ruby/amethyst), else a
     * square tree with iron ore steps; 0 and 2-19 circular.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos clicked = context.getClickedPos();
        final int clickedX = clicked.getX();
        final int clickedY = clicked.getY();
        final int clickedZ = clicked.getZ();
        final BlockState var1 = world.getBlockState(clicked);
        if (!CropBlocks.isGrassDirtOrFarmland(var1)) { // grass, farmland, dirt
            return InteractionResult.PASS;
        }
        final int tree_type = this.rand.nextInt(4);
        Block leaf_type = Blocks.OAK_LEAVES;
        this.no_critters = true;
        if (this.rand.nextInt(2) == 1) {
            this.no_critters = false;
        }
        if (!world.isClientSide) {
            world.setBlock(clicked, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (int var2 = 0; var2 < 6; ++var2) {
            // spawnParticle is a no-op on the server world, as addParticle is on a ServerLevel.
            world.addParticle(ParticleTypes.LARGE_SMOKE, clickedX + 0.5f, clickedY + 1 + 0.25f, clickedZ + 0.5f, 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.EXPLOSION, clickedX + 0.5f, clickedY + 1 + 0.25f, clickedZ + 0.5f, 0.0, 0.0, 0.0);
            world.addParticle(DustParticleOptions.REDSTONE, clickedX + 0.5f, clickedY + 1 + 0.25f, clickedZ + 0.5f, 0.0, 0.0, 0.0);
        }
        final Player player = context.getPlayer();
        if (!world.isClientSide && player != null) {
            // PORT: playSoundAtEntity ran on both sides; the server's call reaches every client nearby, a client call
            // as well would play it twice for the user.
            world.playSound(null, player, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.8f, 1.5f);
        }
        if (!world.isClientSide) {
            final LegacyWriter w = LegacyWriter.live(world);
            final Random worldRand = new LegacyRandom(world.random);
            final int rand_treetype = this.rand.nextInt(100);
            if (rand_treetype >= 20) {
                if (rand_treetype >= 40) {
                    if (tree_type != 3 && this.rand.nextInt(10) == 1) {
                        leaf_type = ModBlocks.LEAVES_APPLE.get();
                    }
                    MakeBigSquareTree(w, this.rand, worldRand, clickedX, clickedY, clickedZ, Blocks.OAK_LOG, leaf_type,
                            Blocks.MOSSY_COBBLESTONE, tree_type, this.tree_radius, this.no_critters);
                } else {
                    MakeBigRoundTree(w, worldRand, clickedX, clickedY, clickedZ, Blocks.OAK_LOG, leaf_type,
                            Blocks.MOSSY_COBBLESTONE, tree_type, this.tree_radius);
                }
            } else if (rand_treetype == 1) {
                if (OreSpawnConfig.TWEAKS.GinormousEmeraldTreeEnable.get() != 0) {
                    if (this.rand.nextInt(2) == 0) {
                        MakeBigSquareTree(w, this.rand, worldRand, clickedX, clickedY, clickedZ, Blocks.GOLD_BLOCK,
                                Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, -1, this.tree_radius, true);
                    } else {
                        MakeBigSquareTree(w, this.rand, worldRand, clickedX, clickedY, clickedZ, Blocks.OBSIDIAN,
                                ModBlocks.BLOCKRUBY.get(), ModBlocks.BLOCKAMETHYST.get(), -1, this.tree_radius, true);
                    }
                } else {
                    MakeBigSquareTree(w, this.rand, worldRand, clickedX, clickedY, clickedZ, Blocks.OAK_LOG, leaf_type,
                            Blocks.IRON_ORE, tree_type, this.tree_radius, this.no_critters);
                }
            } else {
                MakeBigCircularTree(w, this.rand, clickedX, clickedY, clickedZ, Blocks.OAK_LOG, leaf_type,
                        Blocks.MOSSY_COBBLESTONE, tree_type, this.tree_radius, this.no_critters);
            }
        }
        final ItemStack par1ItemStack = context.getItemInHand();
        // PORT: the player is nullable in 1.21.1; none counts as "not creative", as in ItemSpawnEgg.
        if (player == null || !player.getAbilities().instabuild) {
            par1ItemStack.shrink(1);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
