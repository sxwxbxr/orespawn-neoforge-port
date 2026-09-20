package com.swbr.orespawn.world.tree;

import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The slice of the 1.7.10 {@code World} that {@code Trees}, {@code BasiliskMaze}, {@code RubyBirdDungeon} and
 * {@code ItemMagicApple} read and write. No original class.
 *
 * <p>The shared writer of the port is {@link StructureWriter} ({@code world/structure/}); this interface is the
 * narrow view these generators take of it, implemented by {@link StructureLegacyWriter}. It exists for one reason:
 * those generators are run again for every chunk a {@code LegacyStructurePiece} touches, and unlike the rooms of
 * {@code GenericDungeon} some of them decide <em>whether to draw a random number</em> from what they read
 * ({@code ItemMagicApple.make_branch}: chest, iron golem and vine rolls behind an air check; a chest's fill draws).
 * Every run must take the same path, so this view adds, on top of the clip:
 * <ul>
 * <li>{@link #getBlock} answers this run's own earlier writes first, and air outside the clip - never the
 * neighbouring chunk, which may already hold this very structure from its own run;</li>
 * <li>{@link #placeChest} returns {@code null} outside the clip, and {@link #generateChestContents} still draws every
 * number a real chest would;</li>
 * <li>{@link #spawnEntity} draws the yaw whenever the type exists, inside the clip or not.</li>
 * </ul>
 * On an unclipped writer (a live level at run time) all three behave exactly like the original.
 *
 * <p>Semantics kept from the original:
 * <ul>
 * <li>{@link #getBlock}: {@code world.getBlock(x, y, z)}; {@code == Blocks.air} is {@link BlockState#isAir()}.</li>
 * <li>{@link #setBlockFast}: {@code OreSpawnMain.setBlockFast(world, x, y, z, block, meta, 2)} and
 * {@code setBlockSuperFast(..., 2, chunk)}; {@link #setBlock}: {@code world.setBlock(x, y, z, block, meta, 2)}.
 * Both are one {@link StructureWriter#setBlock(int, int, int, BlockState)} in 1.21.1 (flag 2, block entity created).</li>
 * <li>A vanilla leaves block gets a leaves tick, on a live level as well as in world generation
 * ({@code world.gen.LegacyWorld}, DECISIONS R21): 1.7.10 leaves placed with metadata 0 carried no distance, a 1.21.1
 * leaves block at its default distance 7 would decay on the first random tick.</li>
 * </ul>
 */
public interface LegacyWriter {

    /** The view on a shared writer; see the class comment for what it adds when the writer is clipped. */
    static LegacyWriter of(final StructureWriter writer) {
        return new StructureLegacyWriter(writer);
    }

    /** A live level at run time: {@link StructureWriter#unclipped}. */
    static LegacyWriter live(final LevelAccessor level) {
        return of(StructureWriter.unclipped(level));
    }

    /**
     * {@code WeightedRandomChestContent.generateChestContents(rand, list, chest, draws)} for a chest from
     * {@link #placeChest}. A {@code null} chest (outside the clip) is filled into a throw-away 27-slot container so
     * that the random numbers are drawn anyway.
     */
    static void generateChestContents(final Random rand, final WeightedRandomChestContent[] list,
                                      @Nullable final Container chest, final int draws) {
        WeightedRandomChestContent.generateChestContents(rand, list, chest != null ? chest : new SimpleContainer(27), draws);
    }

    /** {@code world.getBlock(x, y, z)}. */
    BlockState getBlock(int x, int y, int z);

    /** {@code world.isAirBlock(x, y, z)}. */
    default boolean isAirBlock(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).isAir();
    }

    /** {@code OreSpawnMain.setBlockFast(world, x, y, z, block, meta, 2)}. */
    void setBlockFast(int x, int y, int z, BlockState state);

    /** {@code world.setBlock(x, y, z, block, meta, 2)}. */
    void setBlock(int x, int y, int z, BlockState state);

    /**
     * {@code world.setBlock(x, y, z, Blocks.mob_spawner, 0, 2)} and
     * {@code ((TileEntityMobSpawner) world.getTileEntity(x, y, z)).func_145881_a().setEntityName(name)}, the name as a
     * registry id; an id that is not registered (a later wave) leaves an empty spawner.
     */
    void placeSpawner(int x, int y, int z, String entityId);

    /**
     * {@code world.setBlock(x, y, z, Blocks.chest, 0, 2)} (or {@code setBlockFast}), then
     * {@code (TileEntityChest) world.getTileEntity(x, y, z)}.
     *
     * @return the chest, or {@code null} when it lies outside the clip; fill it with {@link #generateChestContents}
     *         either way
     */
    @Nullable
    Container placeChest(int x, int y, int z);

    /**
     * {@code EntityList.createEntityByName(name, world)} (or {@code createEntityByID}), then
     * {@code setLocationAndAngles(x, y, z, yawRand.nextFloat() * 360.0f, 0.0f)}, {@code spawnEntityInWorld} and
     * {@code playLivingSound}. The yaw is drawn only when the type exists - the original drew it inside
     * {@code if (var8 != null)} - and then also outside the clip.
     *
     * @return the entity, or {@code null} when the type is not registered (later wave) or the position is outside
     *         the clip
     */
    @Nullable
    default Entity spawnEntity(String entityId, double x, double y, double z, Random yawRand) {
        return this.spawnEntity(entityId, x, y, z, yawRand, entity -> { });
    }

    /**
     * As {@link #spawnEntity(String, double, double, double, Random)}, with {@code beforeAdd} run on the created
     * entity before it is added. State the original set on the live entity after {@code spawnEntityInWorld} (for
     * example {@code func_110163_bv}) must go here: in world generation {@code WorldGenRegion.addFreshEntity} ends in
     * {@code ProtoChunk.addEntity}, which serialises the entity at once, so anything set afterwards is lost.
     */
    @Nullable
    Entity spawnEntity(String entityId, double x, double y, double z, Random yawRand,
                       java.util.function.Consumer<Entity> beforeAdd);
}
