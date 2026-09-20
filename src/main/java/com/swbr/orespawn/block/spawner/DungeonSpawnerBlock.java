package com.swbr.orespawn.block.spawner;

import com.swbr.orespawn.block.plant.ReedLikePlant;
import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.dungeon.a.GenericDungeonA;
import com.swbr.orespawn.world.maze.BasiliskMaze;
import com.swbr.orespawn.world.maze.RubyBirdDungeon;
import com.swbr.orespawn.world.tree.Trees;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.DungeonSpawnerBlock} (DungeonSpawnerBlock.java:11-220): Random Dungeon Spawner
 * ({@code dungeonspawner}, OreSpawnMain.java:1604, light 0.9; registered :1844). A one-shot {@code BlockReed} that
 * builds one of fifty structures where it stands, 400 ticks after it was placed or earlier on a random tick
 * (verhalten/itemblock-03.md, "DungeonSpawnerBlock"). Placed only by {@code ItemRandomDungeon}.
 *
 * <ul>
 *   <li>Bounds 0.125..0.875, full height (:14-15) - {@link ReedLikePlant#SHAPE}; random ticks (:16).</li>
 *   <li>{@code canPlaceBlockAt} (:19-21): a solid block below. {@code canBlockStay} (:212-214) is {@code true}, so a
 *       neighbour change never drops it ({@link #canSurvive}).</li>
 *   <li>Drops the item {@code randomdungeon} once (:204-210) - loot table {@code blocks/dungeonspawner}; pick block
 *       gives the same item (:199-202). Breaking it builds nothing (:36-38).</li>
 * </ul>
 *
 * <p>The structures of cases 2-21 and 24-49 are {@code GenericDungeon} (W13). Each runs the builder registered under
 * its {@code orespawn:make_*} id through {@link GenericDungeonA#buildLive}: unclipped on the live level with
 * {@code world.rand} as a {@code LegacyRandom}, as 1.7.10 called {@code OreSpawnMain.MyDungeon.makeXxx} directly.
 * Variants: case 2 King castle (1), 47 Queen castle ({@code make_enormous_castle_q}), 31 King altar (0), 42 Queen
 * altar (1).
 */
public class DungeonSpawnerBlock extends ReedLikePlant {

    /** {@code OreSpawnMain.RandomDungeon}, looked up by id so the block does not depend on the item holder. */
    public static final ResourceLocation RANDOM_DUNGEON = ResourceLocation.fromNamespaceAndPath("orespawn", "randomdungeon");

    public DungeonSpawnerBlock(final Properties properties) {
        super(properties);
    }

    /**
     * Registration properties. {@code BlockReed}'s constructor set {@code Material.plants} with hardness 0 and
     * {@code Block}'s default step sound, stone; {@code setLightLevel(0.9f)} (:1604) is {@code (int) (15 * 0.9f)} = 13.
     * Same as {@code IslandBlock}.
     */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE).lightLevel(s -> 13);
    }

    /** {@code canPlaceBlockAt} (:19-21): {@code getMaterial().isSolid()} of the block below. */
    @Override
    @SuppressWarnings("deprecation") // BlockState.isSolid() is the 1.7.10 Material.isSolid()
    protected boolean mayPlaceOn(final BlockState soil) {
        return soil.isSolid();
    }

    /** {@code canBlockStay} (:212-214): always. */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return true;
    }

    /** {@code randomDisplayTick} (:23-27): five firework sparks every display tick, from {@code par1World.rand}. */
    @Override
    public void animateTick(final BlockState state, final Level par1World, final BlockPos pos, final RandomSource par5Random) {
        final int par2 = pos.getX();
        final int par3 = pos.getY();
        final int par4 = pos.getZ();
        for (int j1 = 0; j1 < 5; ++j1) {
            par1World.addParticle(ParticleTypes.FIREWORK, par2 + par1World.random.nextFloat(),
                    par3 + (double) par1World.random.nextFloat(), par4 + par1World.random.nextFloat(),
                    (par1World.random.nextFloat() - par1World.random.nextFloat()) / 4.0,
                    par1World.random.nextFloat() / 2.0,
                    (par1World.random.nextFloat() - par1World.random.nextFloat()) / 4.0);
        }
    }

    /** {@code onBlockAdded} (:29-34): server schedules the build in 400 ticks. */
    @Override
    protected void onPlace(final BlockState state, final Level world, final BlockPos pos, final BlockState oldState,
                           final boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        if (world.isClientSide) {
            return;
        }
        world.scheduleTick(pos, this, 400);
    }

    /** {@code updateTick} as the scheduled tick. */
    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /** {@code updateTick} as the random tick ({@code setTickRandomly(true)}, :16). */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * {@code updateTick} (:40-197): this block and the one above become air (flag 2), then {@code world.rand.nextInt(50)}
     * picks the structure, built at the block position; cases 43-45 one block higher.
     */
    public void updateTick(final ServerLevel world, final int clickedX, final int clickedY, final int clickedZ) {
        if (world.isClientSide) {
            return;
        }
        // A scheduled tick left over after a random tick already built runs only while the block is still this
        // one - 1.7.10 compared the block id before updateTick, 1.21.1 LevelTicks does the same.
        world.setBlock(new BlockPos(clickedX, clickedY, clickedZ), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        world.setBlock(new BlockPos(clickedX, clickedY + 1, clickedZ), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        final int type = world.random.nextInt(50);
        if (type == 0) {
            Trees.FairyTree(world, clickedX, clickedY, clickedZ);
        }
        if (type == 1) {
            Trees.FairyCastleTree(world, clickedX, clickedY, clickedZ);
        }
        if (type == 2) {
            // OreSpawnMain.MyDungeon.makeEnormousCastle(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ENORMOUS_CASTLE, clickedX, clickedY, clickedZ, 1);
        }
        if (type == 3) {
            // OreSpawnMain.MyDungeon.makeRotatorStation(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ROTATOR_STATION, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 4) {
            // OreSpawnMain.MyDungeon.makeBeeHive(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_BEE_HIVE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 5) {
            // OreSpawnMain.MyDungeon.makeHauntedHouse(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_HAUNTED_HOUSE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 6) {
            // OreSpawnMain.MyDungeon.makeMantisHive(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_MANTIS_HIVE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 7) {
            // OreSpawnMain.MyDungeon.makeKyuubiDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_KYUUBI_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 8) {
            // OreSpawnMain.MyDungeon.makeSmallBeeHive(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_SMALL_BEE_HIVE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 9) {
            // OreSpawnMain.MyDungeon.makeShadowDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_SHADOW_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 10) {
            // OreSpawnMain.MyDungeon.makeAlienWTFDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ALIEN_WTF_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 11) {
            // OreSpawnMain.MyDungeon.makeEnderKnightDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ENDER_KNIGHT_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 12) {
            // OreSpawnMain.MyDungeon.makePlayPool(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_PLAY_POOL, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 13) {
            // OreSpawnMain.MyDungeon.makeWaterDragonLair(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_WATER_DRAGON_LAIR, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 14) {
            // OreSpawnMain.MyDungeon.makeCloudSharkDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_CLOUD_SHARK_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 15) {
            // OreSpawnMain.MyDungeon.makeLeafMonsterDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_LEAF_MONSTER_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 16) {
            // OreSpawnMain.MyDungeon.makeMiniDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_MINI_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 17) {
            // OreSpawnMain.MyDungeon.makeGoldFishBowl(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_GOLD_FISH_BOWL, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 18) {
            // OreSpawnMain.MyDungeon.makeEnderReaperGraveyard(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ENDER_REAPER_GRAVEYARD, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 19) {
            // OreSpawnMain.MyDungeon.makeSpitBugLair(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_SPIT_BUG_LAIR, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 20) {
            // OreSpawnMain.MyDungeon.makeIgloo(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_IGLOO, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 21) {
            // OreSpawnMain.MyDungeon.makeDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 22) {
            RubyBirdDungeon.makeDungeon(world, clickedX, clickedY, clickedZ);
        }
        if (type == 23) {
            BasiliskMaze.buildBasiliskMaze(world, clickedX, clickedY, clickedZ);
        }
        if (type == 24) {
            // OreSpawnMain.MyDungeon.makeEnderDragonHospital(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ENDER_DRAGON_HOSPITAL, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 25) {
            // OreSpawnMain.MyDungeon.makeCrystalHauntedHouse(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_CRYSTAL_HAUNTED_HOUSE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 26) {
            // OreSpawnMain.MyDungeon.makeBouncyCastle(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_BOUNCY_CASTLE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 27) {
            // OreSpawnMain.MyDungeon.makeEnderCastle(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ENDER_CASTLE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 28) {
            // OreSpawnMain.MyDungeon.makeDamselInDistress(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_DAMSEL_IN_DISTRESS, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 29) {
            // OreSpawnMain.MyDungeon.makeIncaPyramid(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_INCA_PYRAMID, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 30) {
            // OreSpawnMain.MyDungeon.makeRobotLab(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ROBOT_LAB, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 31) {
            // OreSpawnMain.MyDungeon.makeKingAltar(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_KING_ALTAR, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 32) {
            // OreSpawnMain.MyDungeon.makeLeonNest(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_LEON_NEST, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 33) {
            // OreSpawnMain.MyDungeon.makeCrystalBattleTower(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_CRYSTAL_BATTLE_TOWER, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 34) {
            // OreSpawnMain.MyDungeon.makeCephadromeAltar(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_CEPHADROME_ALTAR, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 35) {
            // OreSpawnMain.MyDungeon.makeGirlfriendIsland(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_GIRLFRIEND_ISLAND, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 36) {
            // OreSpawnMain.MyDungeon.makeGreenhouseDungeon(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_GREENHOUSE_DUNGEON, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 37) {
            // OreSpawnMain.MyDungeon.makeMonsterIsland(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_MONSTER_ISLAND, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 38) {
            // OreSpawnMain.MyDungeon.makeNightmareRookery(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_NIGHTMARE_ROOKERY, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 39) {
            // OreSpawnMain.MyDungeon.makeStinkyHouse(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_STINKY_HOUSE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 40) {
            // OreSpawnMain.MyDungeon.makeRubberDuckyPond(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_RUBBER_DUCKY_POND, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 41) {
            // OreSpawnMain.MyDungeon.makeWhiteHouse(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_WHITE_HOUSE, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 42) {
            // OreSpawnMain.MyDungeon.makeQueenAltar(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_KING_ALTAR, clickedX, clickedY, clickedZ, 1);
        }
        if (type == 43) {
            // OreSpawnMain.MyDungeon.makeFrogPond(world, clickedX, clickedY + 1, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_FROG_POND, clickedX, clickedY + 1, clickedZ, 0);
        }
        if (type == 44) {
            // OreSpawnMain.MyDungeon.makePumpkin(world, clickedX, clickedY + 1, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_PUMPKIN, clickedX, clickedY + 1, clickedZ, 0);
        }
        if (type == 45) {
            // OreSpawnMain.MyDungeon.makeRoundRotator(world, clickedX, clickedY + 1, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_ROUND_ROTATOR, clickedX, clickedY + 1, clickedZ, 0);
        }
        if (type == 46) {
            // OreSpawnMain.MyDungeon.makeRainbow(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_RAINBOW, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 47) {
            // OreSpawnMain.MyDungeon.makeEnormousCastleQ(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, GenericDungeonA.MAKE_ENORMOUS_CASTLE_Q, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 48) {
            // OreSpawnMain.MyDungeon.makeSpiderHangout(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_SPIDER_HANGOUT, clickedX, clickedY, clickedZ, 0);
        }
        if (type == 49) {
            // OreSpawnMain.MyDungeon.makeRedAntHangout(world, clickedX, clickedY, clickedZ)
            GenericDungeonA.buildLive(world, OreSpawnWorld.MAKE_RED_ANT_HANGOUT, clickedX, clickedY, clickedZ, 0);
        }
    }

    /** {@code getItem} (:199-202): the Random Dungeon item. */
    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final LevelReader level,
                                       final BlockPos pos, final Player player) {
        return new ItemStack(BuiltInRegistries.ITEM.get(RANDOM_DUNGEON));
    }
}
