package com.swbr.orespawn.world.tree;

import com.swbr.orespawn.block.crop.CropBlocks;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.item.utility.OneUse;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.Trees} (Trees.java:11-819), the singleton {@code OreSpawnMain.OreSpawnTrees}
 * (OreSpawnMain.java:5445). Seven tree generators and the crystal chest list (verhalten/world-04.md, "Trees").
 *
 * <p>The singleton has no state, so the methods are static and take the world as a {@link LegacyWriter} and
 * {@code world.rand} as a {@link Random}. Runtime callers use the {@link Level} overloads, which write straight
 * into the level with the level's random; the world generator runs them as builders of a
 * {@code LegacyStructurePiece} ({@link TreeBuilders}), because Wind, Sky, Fairy and Fairy Castle trees overrun the 3x3-chunk window
 * of a feature (world-04.md, "Portierung 1.21.1").
 *
 * <p>Blocks: {@code Blocks.log} meta 0 is {@code oak_log} (vertical), {@code Blocks.leaves} meta 0
 * {@code oak_leaves}. {@code Blocks.grass}/{@code Blocks.dirt} are the category {@code #minecraft:dirt} and
 * {@code farmland} is named (DECISIONS R22; {@link OneUse#isDirt}, {@link CropBlocks#isGrassDirtOrFarmland}).
 * {@code == Blocks.air} is {@link BlockState#isAir()}.
 *
 * <p>Not ported: {@code ScragglyTreeWithBranches} and {@code makeScragglyBranch} (Trees.java:386-473) have no
 * caller; {@code ChunkProviderOreSpawn4/6} carry their own copies (world-04.md). Dead code, not registered.
 */
public final class Trees {

    /** {@code EntityList} name "Fairy" (Trees.java:502, :628). */
    public static final String FAIRY = "orespawn:fairy";

    /**
     * {@code CrystalChestContentsList} (Trees.java:817): 82 entries, weight sum 755. Shared by {@link #FairyTree},
     * {@link #addSomething} and {@code OreSpawnWorld.addCrystalChest} (OreSpawnWorld.java:1820-1830).
     */
    public static final WeightedRandomChestContent[] CrystalChestContentsList = {
        WeightedRandomChestContent.byId("orespawn:crystaltermiteblock", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:crystalflower_red", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalflower_blue", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalflower_green", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalflower_yellow", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalplanks", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalworkbench", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalfurnace", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_block", 1, 10, 5),
        WeightedRandomChestContent.byId("orespawn:crystalstone", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalrat", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalfairy", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalcoal", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalgrass", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalcrystal", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystaltorch", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystaltreeleaves", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystaltreeleaves2", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystaltreeleaves3", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystaltreelog", 1, 10, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye", 1, 10, 5),
        WeightedRandomChestContent.byId("orespawn:crystalwoodsword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalwoodaxe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalwoodshovel", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalwoodpickaxe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalwoodhoe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalpinksword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalpinkaxe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalpinkshovel", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalpinkpickaxe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalpinkhoe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_sword", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_axe", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_shovel", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_pickaxe", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_hoe", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:crystalstonesword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalstoneaxe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalstoneshovel", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalstonepickaxe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:crystalstonehoe", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_ingot", 1, 5, 5),
        WeightedRandomChestContent.byId("orespawn:crystalpink_ingot", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:crystalapple", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:peacockfeather", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:cookedpeacock", 1, 10, 20),
        WeightedRandomChestContent.byId("orespawn:rawpeacock", 1, 10, 20),
        WeightedRandomChestContent.byId("orespawn:rice", 1, 10, 20),
        WeightedRandomChestContent.byId("orespawn:quinoa", 1, 10, 20),
        WeightedRandomChestContent.byId("orespawn:pink_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_helmet", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_chest", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_leggings", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:tigerseye_boots", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:peacock_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:peacock_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:peacock_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:peacock_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:eggrotator", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggvortex", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggpeacock", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggdungeonbeast", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggfairy", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggrat", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggflounder", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggwhale", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggirukandji", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggskate", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggurchin", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggghost", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:eggghostskelly", 1, 5, 10),
        WeightedRandomChestContent.byId("orespawn:skatebow", 1, 1, 2),
        WeightedRandomChestContent.byId("orespawn:irukandjiarrow", 5, 10, 2),
        WeightedRandomChestContent.byId("orespawn:deadirukandji", 2, 8, 5),
        WeightedRandomChestContent.byId("orespawn:ultimatebow", 1, 1, 2),
        WeightedRandomChestContent.byId("orespawn:ultimatesword", 1, 1, 2),
        WeightedRandomChestContent.byId("minecraft:iron_ingot", 1, 4, 10),
        WeightedRandomChestContent.byId("minecraft:oak_log", 1, 4, 10), // Item.getItemFromBlock(Blocks.log), damage 0
        WeightedRandomChestContent.byId("minecraft:golden_apple", 1, 5, 2),
    };

    private Trees() {
    }

    private static BlockState log() {
        return Blocks.OAK_LOG.defaultBlockState();
    }

    private static BlockState leaves() {
        return Blocks.OAK_LEAVES.defaultBlockState();
    }

    private static BlockState skyTreeLog() {
        return ModBlocks.SKY_TREE_LOG.get().defaultBlockState();
    }

    private static BlockState duplicatorLog() {
        return ModBlocks.DUPLICATOR_TREE_LOG.get().defaultBlockState();
    }

    private static BlockState appleLeaves() {
        return ModBlocks.LEAVES_APPLE.get().defaultBlockState();
    }

    private static BlockState crystalLog() {
        return ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
    }

    // =====================================================================================================
    // Wind Tree (Utopia)

    /** {@code WindTreeBranch} (Trees.java:15-39). */
    private static void WindTreeBranch(final LegacyWriter world, final int x, final int y, final int z, final int length,
                                       final int dirx, final int dirz) {
        for (int i = 1; i <= length; ++i) {
            world.setBlockFast(x + i * dirx, y, z + i * dirz, log());
            if (world.getBlock(x + i * dirx, y + 1, z + i * dirz).isAir()) {
                world.setBlockFast(x + i * dirx, y + 1, z + i * dirz, leaves());
            }
            if (i < length / 3 && world.getBlock(x + i * dirx, y + 2, z + i * dirz).isAir()) {
                world.setBlockFast(x + i * dirx, y + 2, z + i * dirz, leaves());
            }
            if (i > length / 3) {
                if (world.getBlock(x + i * dirx + dirz, y, z + i * dirz + dirx).isAir()) {
                    world.setBlockFast(x + i * dirx + dirz, y, z + i * dirz + dirx, leaves());
                }
                if (world.getBlock(x + i * dirx - dirz, y, z + i * dirz - dirx).isAir()) {
                    world.setBlockFast(x + i * dirx - dirz, y, z + i * dirz - dirx, leaves());
                }
            }
        }
        if (world.getBlock(x + (length + 1) * dirx, y, z + (length + 1) * dirz).isAir()) {
            world.setBlockFast(x + (length + 1) * dirx, y, z + (length + 1) * dirz, leaves());
        }
        if (world.getBlock(x + (length + 2) * dirx, y, z + (length + 2) * dirz).isAir()) {
            world.setBlockFast(x + (length + 2) * dirx, y, z + (length + 2) * dirz, leaves());
        }
    }

    /**
     * {@code WindTree(world, x, y, z, dir)} (Trees.java:41-75): on grass or dirt only; trunk 40-47 high replacing the
     * ground block, leaves on the +dir side above a fifth of the height, a branch in +dir every fourth level above a
     * quarter. {@code width} (:64) is rolled and never used; the roll stays, it moves the random stream.
     */
    public static void WindTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z,
                                final int dir) {
        WindTree(world, rand, x, y, z, dir, true);
    }

    /**
     * {@link #WindTree(LegacyWriter, Random, int, int, int, int)} with the soil check switchable.
     *
     * <p>PORT: a structure piece passes {@code checkGround = false}. The trunk replaces the soil block, so the run of
     * the origin chunk turns it into a log, and the run of any other chunk cannot read it at all (outside its clip);
     * both would fail the check and grow nothing. The check belongs to the dispatcher, which already looked at the
     * surface before the start was made ({@code SurfaceProbe}).
     */
    public static void WindTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z,
                                final int dir, final boolean checkGround) {
        if (dir < 0 || dir > 3) {
            return;
        }
        int dirx = 1;
        int dirz = 0;
        if (dir == 1) {
            dirx = -1;
            dirz = 0;
        }
        if (dir == 2) {
            dirx = 0;
            dirz = 1;
        }
        if (dir == 3) {
            dirx = 0;
            dirz = -1;
        }
        final BlockState bid = world.getBlock(x, y, z);
        if (checkGround && !OneUse.isDirt(bid)) { // bid != Blocks.grass && bid != Blocks.dirt
            return;
        }
        final int height = rand.nextInt(8) + 40;
        @SuppressWarnings("unused")
        final int width = rand.nextInt(4) + 8;
        for (int j = 0; j < height; ++j) {
            world.setBlockFast(x, j + y, z, log());
            if (j > height / 5) {
                world.setBlockFast(x + dirx, j + y, z + dirz, leaves());
                if (j > height / 4 && j % 4 == 0) {
                    WindTreeBranch(world, x, j + y, z, height - j, dirx, dirz);
                }
            }
        }
        world.setBlockFast(x, y + height, z, leaves());
    }

    // =====================================================================================================
    // Sky Tree (Utopia, Island Too)

    /** {@code SkyTreeBranch} (Trees.java:77-93). */
    private static void SkyTreeBranch(final LegacyWriter world, final int x, final int y, final int z, final int length,
                                      final int dirx, final int dirz) {
        for (int i = 1; i < length; ++i) {
            world.setBlockFast(x + i * dirx, y, z + i * dirz, skyTreeLog());
            if (world.getBlock(x + i * dirx, y + 1, z + i * dirz).isAir()) {
                world.setBlockFast(x + i * dirx, y + 1, z + i * dirz, leaves());
            }
            if (world.getBlock(x + i * dirx + dirz, y, z + i * dirz + dirx).isAir()) {
                world.setBlockFast(x + i * dirx + dirz, y, z + i * dirz + dirx, leaves());
            }
            if (world.getBlock(x + i * dirx - dirz, y, z + i * dirz - dirx).isAir()) {
                world.setBlockFast(x + i * dirx - dirz, y, z + i * dirz - dirx, leaves());
            }
        }
        if (world.getBlock(x + length * dirx, y, z + length * dirz).isAir()) {
            world.setBlockFast(x + length * dirx, y, z + length * dirz, leaves());
        }
    }

    /**
     * {@code SkyTree(world, x, y, z)} (Trees.java:95-120): on grass or dirt only; the trunk top is the
     * <em>absolute</em> Y 190-204 and the tree is skipped when that is less than 20 above {@code y}. Four branches of
     * 25-34 at the top, four of a third of that 5-8 lower. Absolute Y is kept (DECISIONS R21: literal Y in the
     * OreSpawn dimensions).
     */
    public static void SkyTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        SkyTree(world, rand, x, y, z, true);
    }

    /** {@link #SkyTree(LegacyWriter, Random, int, int, int)} with the soil check switchable; PORT as in {@link #WindTree}. */
    public static void SkyTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z,
                               final boolean checkGround) {
        final BlockState bid = world.getBlock(x, y, z);
        if (checkGround && !OneUse.isDirt(bid)) {
            return;
        }
        int height = rand.nextInt(15) + 190;
        if (height - y < 20) {
            return;
        }
        int width = rand.nextInt(10) + 25;
        for (int j = y; j <= height; ++j) {
            world.setBlockFast(x, j, z, skyTreeLog());
        }
        world.setBlockFast(x, height + 1, z, leaves());
        SkyTreeBranch(world, x, height, z, width, 1, 0);
        SkyTreeBranch(world, x, height, z, width, -1, 0);
        SkyTreeBranch(world, x, height, z, width, 0, 1);
        SkyTreeBranch(world, x, height, z, width, 0, -1);
        height -= 5;
        height -= rand.nextInt(4);
        width /= 3;
        SkyTreeBranch(world, x, height, z, width, 1, 0);
        SkyTreeBranch(world, x, height, z, width, -1, 0);
        SkyTreeBranch(world, x, height, z, width, 0, 1);
        SkyTreeBranch(world, x, height, z, width, 0, -1);
    }

    // =====================================================================================================
    // Duplicator Tree (random tick of duplicatortreelog)

    /** {@code DuplicatorTree} with the live level and its random, for {@code BlockDuplicatorLog.randomTick}. */
    public static void DuplicatorTree(final Level world, final int x, final int y, final int z) {
        DuplicatorTree(LegacyWriter.live(world), new LegacyRandom(world.random), x, y, z);
    }

    /**
     * {@code DuplicatorTree(world, x, y, z)} (Trees.java:122-189): one growth step per call - the first missing of
     * three logs, the top apple leaf, the eight ring leaves; a grown tree copies one block of the 5x5 layer at its
     * base into an air spot of that layer. Only the lowest log standing directly on soil grows: the assignments of
     * {@code realy} in the fall-through branches are dead, both end in {@code return} (:124-138, R18).
     *
     * <p>The copy is {@code world.setBlock(x, y, z, bidm, meta, 2)} (:183): the whole {@link BlockState}, no block
     * entity contents, no block list - bedrock and spawners are copied too (world-04.md).
     */
    public static void DuplicatorTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        int realy = y;
        BlockState bid = world.getBlock(x, y - 1, z);
        if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
            bid = world.getBlock(x, y - 2, z);
            if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
                bid = world.getBlock(x, y - 3, z);
                if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
                    return;
                }
                realy = y - 3;
            } else {
                realy = y - 2;
            }
            return;
        }
        realy = y - 1;
        bid = world.getBlock(x, realy + 1, z);
        if (!bid.is(ModBlocks.DUPLICATOR_TREE_LOG.get())) {
            world.setBlockFast(x, realy + 1, z, duplicatorLog());
            return;
        }
        bid = world.getBlock(x, realy + 2, z);
        if (!bid.is(ModBlocks.DUPLICATOR_TREE_LOG.get())) {
            world.setBlockFast(x, realy + 2, z, duplicatorLog());
            return;
        }
        bid = world.getBlock(x, realy + 3, z);
        if (!bid.is(ModBlocks.DUPLICATOR_TREE_LOG.get())) {
            world.setBlockFast(x, realy + 3, z, duplicatorLog());
            return;
        }
        bid = world.getBlock(x, realy + 4, z);
        if (!bid.is(ModBlocks.LEAVES_APPLE.get())) {
            world.setBlockFast(x, realy + 4, z, appleLeaves());
            return;
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (j != 0 || i != 0) {
                    bid = world.getBlock(x + i, realy + 3, z + j);
                    if (!bid.is(ModBlocks.LEAVES_APPLE.get())) {
                        world.setBlockFast(x + i, realy + 3, z + j, appleLeaves());
                        return;
                    }
                }
            }
        }
        BlockState bidm = Blocks.AIR.defaultBlockState();
        for (int tries = 0; tries < 20 && (bidm.isAir() || bidm.is(ModBlocks.DUPLICATOR_TREE_LOG.get())); ++tries) {
            int i = rand.nextInt(5) - 2;
            int j = rand.nextInt(5) - 2;
            // bidm and meta (:175-176) are one BlockState in 1.21.1.
            bidm = world.getBlock(x + i, realy + 1, z + j);
            if (!bidm.isAir() && !bidm.is(ModBlocks.DUPLICATOR_TREE_LOG.get())) {
                for (int k = 0; k < 20; ++k) {
                    i = rand.nextInt(5) - 2;
                    j = rand.nextInt(5) - 2;
                    bid = world.getBlock(x + i, realy + 1, z + j);
                    if (bid.isAir()) {
                        world.setBlock(x + i, realy + 1, z + j, bidm);
                        return;
                    }
                }
            }
        }
    }

    // =====================================================================================================
    // Experience Tree (random tick of experiencesapling)

    /** {@code make_leaves} (Trees.java:191-202): 7x3x7 of experience leaves into air, from the log's level up. */
    private static void make_leaves(final LegacyWriter world, final int x, final int y, final int z) {
        final BlockState experienceLeaves = ModBlocks.LEAVES_EXPERIENCE.get().defaultBlockState();
        for (int l1 = -3; l1 <= 3; ++l1) {
            for (int l2 = -3; l2 <= 3; ++l2) {
                for (int l3 = 0; l3 <= 2; ++l3) {
                    final BlockState bid = world.getBlock(x + l1, y + l3, z + l2);
                    if (bid.isAir()) {
                        world.setBlockFast(x + l1, y + l3, z + l2, experienceLeaves);
                    }
                }
            }
        }
    }

    /** {@code grow_small_branch} (Trees.java:204-247). */
    private static void grow_small_branch(final LegacyWriter world, final Random rand, final int x, final int y,
                                          final int z, final int xdir, final int zdir, final int xxdir, final int zzdir) {
        int i2 = 0;
        int k2 = 0;
        int j2 = 0;
        int l = x;
        int m = y;
        int k3 = z;
        for (int grow = 4 + rand.nextInt(2), n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, log());
            make_leaves(world, l, m, k3);
            ++m;
            l += xdir;
            k3 += zdir;
            i2 = l;
            k2 = k3;
        }
        for (int grow = 4 + rand.nextInt(3), n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, log());
            make_leaves(world, l, m, k3);
            l += xdir;
            k3 += zdir;
        }
        for (int grow = 4 + rand.nextInt(3), n = 0; n < grow; ++n) {
            world.setBlockFast(i2, m, k2, log());
            make_leaves(world, i2, m, k2);
            i2 += xxdir;
            k2 += zzdir;
        }
        j2 = --m;
        for (int grow = 3 + rand.nextInt(3), n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, log());
            make_leaves(world, l, m, k3);
            l += xdir;
            k3 += zdir;
            --m;
        }
        for (int grow = 3 + rand.nextInt(3), n = 0; n < grow; ++n) {
            world.setBlockFast(i2, j2, k2, log());
            make_leaves(world, i2, j2, k2);
            i2 += xxdir;
            k2 += zzdir;
            --j2;
        }
    }

    /** {@code grow_branch} (Trees.java:249-292). */
    private static void grow_branch(final LegacyWriter world, final Random rand, final int x, final int y, final int z,
                                    final int xdir, final int zdir, final int xxdir, final int zzdir) {
        int i2 = 0;
        int k2 = 0;
        int j2 = 0;
        int l = x;
        int m = y;
        int k3 = z;
        for (int grow = 5 + rand.nextInt(4), n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, log());
            make_leaves(world, l, m, k3);
            ++m;
            l += xdir;
            k3 += zdir;
            i2 = l;
            k2 = k3;
        }
        for (int grow = 6 + rand.nextInt(5), n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, log());
            make_leaves(world, l, m, k3);
            l += xdir;
            k3 += zdir;
        }
        for (int grow = 6 + rand.nextInt(5), n = 0; n < grow; ++n) {
            world.setBlockFast(i2, m, k2, log());
            make_leaves(world, i2, m, k2);
            i2 += xxdir;
            k2 += zzdir;
        }
        j2 = --m;
        for (int grow = 4 + rand.nextInt(4), n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, log());
            make_leaves(world, l, m, k3);
            l += xdir;
            k3 += zdir;
            --m;
        }
        for (int grow = 4 + rand.nextInt(4), n = 0; n < grow; ++n) {
            world.setBlockFast(i2, j2, k2, log());
            make_leaves(world, i2, j2, k2);
            i2 += xxdir;
            k2 += zzdir;
            --j2;
        }
    }

    /** {@code ExperienceTree} with the live level and its random, for {@code BlockExperiencePlant.randomTick}. */
    public static void ExperienceTree(final Level world, final int x, final int y, final int z) {
        ExperienceTree(LegacyWriter.live(world), new LegacyRandom(world.random), x, y, z);
    }

    /**
     * {@code ExperienceTree(world, x, y, z)} (Trees.java:294-329), called on the soil block below the sapling: 2x2
     * oak trunk, four large branches at y+6, trunk to y+18, four small branches at y+19, a crown of 5-10 with a leaf
     * cloud on every log.
     */
    public static void ExperienceTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        final BlockState bid = world.getBlock(x, y, z);
        if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
            return;
        }
        for (int j = 1; j < 6; ++j) {
            for (int i = 0; i < 2; ++i) {
                for (int k = 0; k < 2; ++k) {
                    world.setBlockFast(x + i, y + j, z + k, log());
                }
            }
        }
        grow_branch(world, rand, x, y + 6, z, 0, 1, 1, 1);
        grow_branch(world, rand, x + 1, y + 6, z, 1, 0, 1, -1);
        grow_branch(world, rand, x, y + 6, z + 1, -1, 0, -1, 1);
        grow_branch(world, rand, x + 1, y + 6, z + 1, 0, -1, -1, -1);
        for (int j = 7; j < 19; ++j) {
            for (int i = 0; i < 2; ++i) {
                for (int k = 0; k < 2; ++k) {
                    world.setBlockFast(x + i, y + j, z + k, log());
                }
            }
        }
        grow_small_branch(world, rand, x, y + 19, z, 0, 1, -1, 1);
        grow_small_branch(world, rand, x + 1, y + 19, z, 1, 0, 1, 1);
        grow_small_branch(world, rand, x, y + 19, z + 1, -1, 0, -1, -1);
        grow_small_branch(world, rand, x + 1, y + 19, z + 1, 0, -1, 1, -1);
        for (int grow = 5 + rand.nextInt(6), j = 19; j < 19 + grow; ++j) {
            for (int i = 0; i < 2; ++i) {
                for (int k = 0; k < 2; ++k) {
                    world.setBlockFast(x + i, y + j, z + k, log());
                    make_leaves(world, x + i, y + j, z + k);
                }
            }
        }
    }

    // =====================================================================================================
    // Small Tree (Island Too)

    /** {@code SmallTree} with the live level and its random, for {@code IslandToo}. */
    public static void SmallTree(final Level world, final int x, final int y, final int z) {
        SmallTree(LegacyWriter.live(world), new LegacyRandom(world.random), x, y, z);
    }

    /**
     * {@code SmallTree(world, x, y, z)} (Trees.java:331-384): without soil one to three blocks down the position
     * becomes air; with soil two or three down nothing happens (the {@code realy} assignments are dead, R18).
     * Otherwise a sky tree log of 1-3 and apple leaves, both only into air.
     */
    public static void SmallTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        int realy = y;
        BlockState bid = world.getBlock(x, y - 1, z);
        if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
            bid = world.getBlock(x, y - 2, z);
            if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
                bid = world.getBlock(x, y - 3, z);
                if (!CropBlocks.isGrassDirtOrFarmland(bid)) {
                    world.setBlockFast(x, y, z, Blocks.AIR.defaultBlockState());
                    return;
                }
                realy = y - 3;
            } else {
                realy = y - 2;
            }
            return;
        }
        realy = y - 1;
        bid = world.getBlock(x, realy + 1, z);
        if (bid.isAir()) {
            world.setBlockFast(x, realy + 1, z, skyTreeLog());
        }
        if (rand.nextInt(2) == 1) {
            bid = world.getBlock(x, realy + 2, z);
            if (bid.isAir()) {
                world.setBlockFast(x, realy + 2, z, skyTreeLog());
            }
            if (rand.nextInt(2) == 1) {
                bid = world.getBlock(x, realy + 3, z);
                if (bid.isAir()) {
                    world.setBlockFast(x, realy + 3, z, skyTreeLog());
                }
            } else {
                --realy;
            }
        } else {
            realy -= 2;
        }
        bid = world.getBlock(x, realy + 4, z);
        if (bid.isAir()) {
            world.setBlockFast(x, realy + 4, z, appleLeaves());
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                bid = world.getBlock(x + i, realy + 3, z + j);
                if (bid.isAir()) {
                    world.setBlockFast(x + i, realy + 3, z + j, appleLeaves());
                }
            }
        }
    }

    // =====================================================================================================
    // Fairy Tree and Fairy Castle Tree (Crystal dimension, Random Dungeon Spawner)

    /** {@code FairyTree} with the live level and its random, for {@code DungeonSpawnerBlock}. */
    public static void FairyTree(final Level world, final int x, final int y, final int z) {
        FairyTree(LegacyWriter.live(world), new LegacyRandom(world.random), x, y, z);
    }

    /**
     * {@code FairyTree(world, x, y, z)} (Trees.java:475-509). No soil check. 2x2 crystal trunk, eight crystal
     * branches going down at the end, a crown of 5-9, a Fairy spawner at (x-1, y+1, z) and a crystal chest with 1-5
     * draws at (x+2, y+1, z). Spawner and chest go through {@code world.setBlock} (:499, :504).
     */
    public static void FairyTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        for (int j = 1; j < 6; ++j) {
            for (int i = 0; i < 2; ++i) {
                for (int k = 0; k < 2; ++k) {
                    world.setBlockFast(x + i, y + j, z + k, crystalLog());
                }
            }
        }
        grow_crystal_branch(world, rand, x, y + 5, z, 0, 1, 1, 1, -1);
        grow_crystal_branch(world, rand, x + 1, y + 5, z, 1, 0, 1, -1, -1);
        grow_crystal_branch(world, rand, x, y + 5, z + 1, -1, 0, -1, 1, -1);
        grow_crystal_branch(world, rand, x + 1, y + 5, z + 1, 0, -1, -1, -1, -1);
        grow_crystal_branch(world, rand, x, y + 6, z, 0, 1, -1, 1, -1);
        grow_crystal_branch(world, rand, x + 1, y + 6, z, 1, 0, 1, 1, -1);
        grow_crystal_branch(world, rand, x, y + 6, z + 1, -1, 0, -1, -1, -1);
        grow_crystal_branch(world, rand, x + 1, y + 6, z + 1, 0, -1, 1, -1, -1);
        for (int grow = 5 + rand.nextInt(5), j = 6; j < 6 + grow; ++j) {
            for (int i = 0; i < 2; ++i) {
                for (int k = 0; k < 2; ++k) {
                    world.setBlockFast(x + i, y + j, z + k, crystalLog());
                    make_crystal_leaves(world, x + i, y + j, z + k);
                }
            }
        }
        world.placeSpawner(x - 1, y + 1, z, FAIRY);
        final Container chest = world.placeChest(x + 2, y + 1, z);
        // Original: if (chest != null) generateChestContents(...). PORT: drawn in any case, see LegacyWriter#placeChest.
        LegacyWriter.generateChestContents(rand, CrystalChestContentsList, chest, 1 + rand.nextInt(5));
    }

    /** {@code make_crystal_leaves} (Trees.java:511-522): 5x2x5 of {@code crystaltreeleaves3} into air. */
    private static void make_crystal_leaves(final LegacyWriter world, final int x, final int y, final int z) {
        final BlockState leaves3 = ModBlocks.CRYSTAL_TREE_LEAVES3.get().defaultBlockState();
        for (int l1 = -2; l1 <= 2; ++l1) {
            for (int l2 = -2; l2 <= 2; ++l2) {
                for (int l3 = 0; l3 <= 1; ++l3) {
                    final BlockState bid = world.getBlock(x + l1, y + l3, z + l2);
                    if (bid.isAir()) {
                        world.setBlockFast(x + l1, y + l3, z + l2, leaves3);
                    }
                }
            }
        }
    }

    /** {@code make_crystal_castle_leaves} (Trees.java:524-540): 3x2x3 into air, lower layer leaves2, upper leaves3. */
    private static void make_crystal_castle_leaves(final LegacyWriter world, final int x, final int y, final int z) {
        final BlockState leaves2 = ModBlocks.CRYSTAL_TREE_LEAVES2.get().defaultBlockState();
        final BlockState leaves3 = ModBlocks.CRYSTAL_TREE_LEAVES3.get().defaultBlockState();
        for (int l1 = -1; l1 <= 1; ++l1) {
            for (int l2 = -1; l2 <= 1; ++l2) {
                for (int l3 = 0; l3 <= 1; ++l3) {
                    final BlockState bid = world.getBlock(x + l1, y + l3, z + l2);
                    if (bid.isAir()) {
                        if (l3 != 0) {
                            world.setBlockFast(x + l1, y + l3, z + l2, leaves3);
                        } else {
                            world.setBlockFast(x + l1, y + l3, z + l2, leaves2);
                        }
                    }
                }
            }
        }
    }

    /** {@code LessLag} shortening of every branch phase (Trees.java:550-555 and the four copies after it). */
    private static int lessLag(int grow) {
        if (TweakStats.LessLag() == 1) {
            --grow;
        }
        if (TweakStats.LessLag() == 2) {
            grow -= 2;
        }
        return grow;
    }

    /** {@code grow_crystal_branch} (Trees.java:542-620). */
    private static void grow_crystal_branch(final LegacyWriter world, final Random rand, final int x, final int y,
                                            final int z, final int xdir, final int zdir, final int xxdir,
                                            final int zzdir, final int ydir) {
        int i2 = 0;
        int k2 = 0;
        int j2 = 0;
        int l = x;
        int m = y;
        int k3 = z;
        int grow = lessLag(4 + rand.nextInt(4));
        for (int n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, crystalLog());
            make_crystal_leaves(world, l, m, k3);
            ++m;
            l += xdir;
            k3 += zdir;
            i2 = l;
            k2 = k3;
        }
        grow = lessLag(5 + rand.nextInt(5));
        for (int n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, crystalLog());
            make_crystal_leaves(world, l, m, k3);
            l += xdir;
            k3 += zdir;
        }
        grow = lessLag(5 + rand.nextInt(5));
        for (int n = 0; n < grow; ++n) {
            world.setBlockFast(i2, m, k2, crystalLog());
            make_crystal_leaves(world, i2, m, k2);
            i2 += xxdir;
            k2 += zzdir;
        }
        j2 = --m;
        grow = lessLag(4 + rand.nextInt(4));
        for (int n = 0; n < grow; ++n) {
            world.setBlockFast(l, m, k3, crystalLog());
            make_crystal_leaves(world, l, m, k3);
            l += xdir;
            k3 += zdir;
            m += ydir;
        }
        grow = lessLag(4 + rand.nextInt(4));
        for (int n = 0; n < grow; ++n) {
            world.setBlockFast(i2, j2, k2, crystalLog());
            make_crystal_leaves(world, i2, j2, k2);
            i2 += xxdir;
            k2 += zzdir;
            j2 += ydir;
        }
    }

    /**
     * {@code addSomething} (Trees.java:622-638): {@code nextInt(3)} - 1 a Fairy spawner on the platform, 2 a crystal
     * chest with 1-5 draws, 0 nothing.
     */
    public static void addSomething(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        final int i = rand.nextInt(3);
        if (i == 1) {
            world.placeSpawner(x, y + 1, z, FAIRY);
        }
        if (i == 2) {
            final Container chest = world.placeChest(x, y + 1, z);
            LegacyWriter.generateChestContents(rand, CrystalChestContentsList, chest, 1 + rand.nextInt(5));
        }
    }

    /** One square platform of {@link #FairyCastleTree} (Trees.java:654-670 and its seven copies :674-806). */
    private static void castlePlatform(final LegacyWriter world, final Random rand, final int cx, final int cy,
                                       final int cz, final int width, final boolean something) {
        final BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();
        for (int i = -width; i <= width; ++i) {
            for (int k = -width; k <= width; ++k) {
                world.setBlockFast(cx + i, cy, cz + k, crystalLog());
                if (i == -width || i == width || k == -width || k == width) {
                    make_crystal_castle_leaves(world, cx + i, cy, cz + k);
                }
                if (something && i == 0 && k == 0) {
                    addSomething(world, rand, cx + i, cy, cz + k);
                }
                if (i == -width && (k == -width || k == width)) {
                    world.setBlockFast(cx + i, cy + 1, cz + k, torch);
                }
                if (i == width && (k == -width || k == width)) {
                    world.setBlockFast(cx + i, cy + 1, cz + k, torch);
                }
            }
        }
    }

    /** {@code FairyCastleTree} with the live level and its random, for {@code DungeonSpawnerBlock}. */
    public static void FairyCastleTree(final Level world, final int x, final int y, final int z) {
        FairyCastleTree(LegacyWriter.live(world), new LegacyRandom(world.random), x, y, z);
    }

    /**
     * {@code FairyCastleTree(world, x, y, z)} (Trees.java:640-814): no trunk, floating crystal platforms. Per
     * iteration one platform at (+spread, 0); from iteration 1 also (-spread, 0), (0, +spread), (0, -spread); from
     * iteration 2 also the four diagonals. {@code addSomething} in every centre except the very first platform.
     *
     * <p>The eight platform blocks of the original are identical apart from the centre offset; they call the shared
     * {@link #castlePlatform} in the original order, with {@code width} and {@code randy} rolled right before each
     * one as the original did.
     */
    public static void FairyCastleTree(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        int nc = 6;
        if (TweakStats.LessLag() == 1) {
            --nc;
        }
        if (TweakStats.LessLag() == 2) {
            nc -= 2;
        }
        int j = 3 + rand.nextInt(3);
        int spread = 0;
        for (int iter = 0; iter < nc; ++iter) {
            final int grow = 4 + rand.nextInt(3);
            int width = 1 + rand.nextInt(3);
            int randy = rand.nextInt(3) - 1;
            castlePlatform(world, rand, x + spread, y + j + randy, z, width, iter != 0); // :654-670
            if (iter != 0) {
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x - spread, y + j + randy, z, width, true); // :674-690
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x, y + j + randy, z + spread, width, true); // :693-709
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x, y + j + randy, z - spread, width, true); // :712-728
            }
            if (iter >= 2) {
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x + spread, y + j + randy, z + spread, width, true); // :733-749
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x - spread, y + j + randy, z - spread, width, true); // :752-768
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x - spread, y + j + randy, z + spread, width, true); // :771-787
                width = 1 + rand.nextInt(3 + iter);
                randy = rand.nextInt(3) - 1;
                castlePlatform(world, rand, x + spread, y + j + randy, z - spread, width, true); // :790-806
            }
            j += grow;
            if (iter == 0) {
                spread = 3;
            }
            spread += grow;
        }
    }
}
