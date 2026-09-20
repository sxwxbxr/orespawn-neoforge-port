package com.swbr.orespawn.world.dimension;

import com.swbr.orespawn.OreSpawn;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.OreSpawnTeleporter} (OreSpawnTeleporter.java:13-190), the landing search
 * behind every creature portal (verhalten/world-02.md):
 *
 * <ul>
 *   <li>EntityAnt.java:81 - {@link #UTOPIA}</li>
 *   <li>EntityRedAnt.java:71 - {@link #MINING}</li>
 *   <li>EntityRainbowAnt.java:46 - {@link #VILLAGE}</li>
 *   <li>EntityUnstableAnt.java:46 - {@link #DANGER}</li>
 *   <li>Termite.java:95 - {@link #CRYSTAL}</li>
 *   <li>EntityButterfly.java:265 - {@link #CHAOS}</li>
 * </ul>
 * Each caller travels to its dimension, or to the Overworld when the player already stands in it.
 *
 * <p><b>Entry point for callers:</b> {@link #transferPlayerToDimension(ServerPlayer, ResourceKey, Level)},
 * the successor of {@code MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(
 * player, dim, new OreSpawnTeleporter(worldServerForDimension(dim), dim, this.worldObj))}.
 *
 * <p>No portal and no platform: {@code placeInPortal} and {@code placeInExistingPortal} only called
 * {@code justPutMe} (:28-35), {@code makePortal} returned {@code true} without building anything (:37-39)
 * and {@code removeStalePortalLocations} was empty (:188-189). The last two have no caller in 1.21.1 and
 * are not ported; there is no {@code Teleporter} base class any more (DECISIONS R13:
 * {@code changeDimension(DimensionTransition)}).
 *
 * <p>Load: up to 1000 columns across roughly ±600 blocks are read from the target level on the server
 * thread, and every chunk they touch is loaded or generated - as in 1.7.10, where {@code world.getBlock} did
 * that. In the port {@code getBlockState} loads as well; outside the six OreSpawn dimensions the first read of a
 * column is its height in {@link #searchTop}, which therefore calls {@code getChunk} before reading the heightmap.
 * A plain {@code Level.getHeight} does <em>not</em> load: it answers {@code getMinBuildHeight()} for a chunk that
 * is not loaded (BUGHUNT2 2.2).
 */
public class OreSpawnTeleporter {

    // Aliases of the WorldProviderOreSpawnN holders (integrator W05): one source of truth per dimension key.
    /** {@code DimensionID} "Dimension-Utopia" - Brown Ant (DECISIONS R13). */
    public static final ResourceKey<Level> UTOPIA = com.swbr.orespawn.world.dimension.utopia.WorldProviderOreSpawn.DIMENSION;
    /** {@code DimensionID2} "Dimension-Extreme" - Red Ant. */
    public static final ResourceKey<Level> MINING = com.swbr.orespawn.world.dimension.mining.WorldProviderOreSpawn2.DIMENSION;
    /** {@code DimensionID3} "Dimension-VillageMania" - Rainbow Ant. */
    public static final ResourceKey<Level> VILLAGE = com.swbr.orespawn.world.dimension.village.WorldProviderOreSpawn3.DIMENSION;
    /** {@code DimensionID4} "Dimension-Islands" - Unstable Ant. */
    public static final ResourceKey<Level> DANGER = com.swbr.orespawn.world.dimension.danger.WorldProviderOreSpawn4.DIMENSION;
    /** {@code DimensionID5} "Dimension-Crystal" - Termite. */
    public static final ResourceKey<Level> CRYSTAL = com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5.DIMENSION;
    /** {@code DimensionID6} "Dimension-Chaos" - Butterfly. */
    public static final ResourceKey<Level> CHAOS = com.swbr.orespawn.world.dimension.chaos.WorldProviderOreSpawn6.DIMENSION;

    private final ServerLevel world;
    private final Level oldWorld;
    private final Random random;
    private final ResourceKey<Level> newdim;

    /** {@code OreSpawnTeleporter(WorldServer, int dim, World)} (:20-26): own {@code Random(world.getSeed())}. */
    public OreSpawnTeleporter(final ServerLevel par1WorldServer, final ResourceKey<Level> dim, final Level par2World) {
        this.world = par1WorldServer;
        this.oldWorld = par2World;
        this.random = new Random(par1WorldServer.getSeed());
        this.newdim = dim;
    }

    private static ResourceKey<Level> key(final String path) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path));
    }

    /**
     * {@code transferPlayerToDimension(player, dim, new OreSpawnTeleporter(worldServerForDimension(dim), dim, oldWorld))}.
     * A fresh teleporter per call, like the original, so {@link #random} restarts from the seed each time.
     *
     * @param oldWorld the level of the creature that was clicked ({@code this.worldObj} of the caller)
     * @return {@code false} only when the target dimension does not exist
     */
    public static boolean transferPlayerToDimension(final ServerPlayer player, final ResourceKey<Level> dim, final Level oldWorld) {
        final ServerLevel target = player.server.getLevel(dim);
        if (target == null) {
            // PORT (R18 case 1): 1.7.10 DimensionManager created a registered dimension on demand; a key
            // without its datapack dimension has no level in 1.21.1, and the original call would NPE.
            return false;
        }
        new OreSpawnTeleporter(target, dim, oldWorld).placeInPortal(player);
        return true;
    }

    /** {@code placeInPortal} (:28-30) and {@code placeInExistingPortal} (:32-35): both just {@code justPutMe}. */
    public void placeInPortal(final Entity par1Entity) {
        this.justPutMe(par1Entity);
    }

    /**
     * {@code isGroundBlock} (:41-43). Only feeds {@code inarow}, which cannot reach 3 after
     * {@code airfound} (verhalten/world-02.md: every air-air pair ends the column) - dead, kept.
     * PORT: the 1.7.10 blocks with metadata variants map to all their 1.21.1 split blocks (R18 rule
     * for split types): dirt with coarse dirt and podzol, sand with red sand, sandstone with chiseled and
     * cut sandstone.
     */
    private boolean isGroundBlock(final BlockState bid) {
        return !bid.isAir() && (bid.is(Blocks.DIRT) || bid.is(Blocks.COARSE_DIRT) || bid.is(Blocks.PODZOL)
                || bid.is(Blocks.GRASS_BLOCK) || bid.is(Blocks.STONE) || bid.is(Blocks.END_STONE)
                || bid.is(Blocks.NETHERRACK) || bid.is(Blocks.COBBLESTONE) || bid.is(Blocks.SAND)
                || bid.is(Blocks.RED_SAND) || bid.is(Blocks.SANDSTONE) || bid.is(Blocks.CHISELED_SANDSTONE)
                || bid.is(Blocks.CUT_SANDSTONE) || bid.is(Blocks.FARMLAND));
    }

    /**
     * {@code Blocks.tallgrass}. PORT: its metas became {@code short_grass} and {@code fern}; the dead
     * shrub meta merged into {@code dead_bush} with the separate 1.7.10 {@code deadbush} block, which the
     * original did not test, so it stays out.
     */
    private static boolean isTallGrass(final BlockState bid) {
        return bid.is(Blocks.SHORT_GRASS) || bid.is(Blocks.FERN);
    }

    /**
     * {@code bid == Blocks.air || bid == null}. PORT: {@code isAir()} - cave air and void air (outside
     * the build height) did not exist in 1.7.10 and stand where air stood.
     */
    private BlockState blockAt(final int x, final int y, final int z) {
        return this.world.getBlockState(new BlockPos(x, y, z));
    }

    /**
     * {@code justPutMe} (:45-157), line by line.
     *
     * <p>PORT (R18, R20 exception): the {@code (int)} casts toward zero stay. Before
     * {@code placeInPortal}, {@code ServerConfigurationManager.transferEntityToWorld} set the entity to
     * {@code (double) MathHelper.clamp_int((int) posX, -29999872, 29999872)} and the same for z
     * (client-1.7.10.jar, {@code oi}); this method therefore saw the truncated coordinates, and so does
     * the port. At a negative coordinate the column searched is {@code (int) x} but the landing is
     * {@code (int) x - 0.5}, the neighbouring column - the original's error, kept.
     *
     * <p>PORT: {@code getMaterial().isSolid()} is {@link BlockState#isSolid()}, which 1.21.1 derives from
     * the collision shape (legacy solid): water, lava, snow layers, carpets, plants and torches are not
     * solid, leaves, glass and fences are - the 1.7.10 material split.
     *
     * <p>PORT: R22 (return into a vanilla dimension) and R21 - the literal 180 is kept only when the target is one
     * of the six OreSpawn dimensions (Y 0-256, terrain from the ported generators). Any other target (the Overworld
     * on the way home) starts each column's search at its {@code WORLD_SURFACE} height + 1, capped at the build
     * height, so a peak above Y 181 is not skipped into the cave below it; see {@link #searchTop}. The lower end
     * (down to Y 2) is unchanged.
     */
    @SuppressWarnings("deprecation")
    public boolean justPutMe(final Entity par1Entity) {
        final double entityX = (double) Mth.clamp((int) par1Entity.getX(), -29999872, 29999872);
        final double entityZ = (double) Mth.clamp((int) par1Entity.getZ(), -29999872, 29999872);
        int posX = (int) entityX;
        int posZ = (int) entityZ;
        int posY = 120;
        int found = 0;
        int inarow = 0;
        int airfound = 0;
        for (int i = 0; i < 1000 && found == 0; ++i) {
            for (posY = this.searchTop(posX, posZ); posY > 1; --posY) {
                BlockState bid = this.blockAt(posX, posY + 1, posZ);
                if (bid.isAir()) {
                    inarow = 0;
                    bid = this.blockAt(posX, posY, posZ);
                    if (bid.isAir()) {
                        airfound = 1;
                        bid = this.blockAt(posX, posY - 1, posZ);
                        if (!bid.isAir()) {
                            if (this.blockAt(posX, posY - 1, posZ).isSolid()) {
                                found = 1;
                                break;
                            }
                            if (isTallGrass(bid) && this.blockAt(posX, posY - 2, posZ).isSolid()) {
                                found = 1;
                                --posY;
                                break;
                            }
                            break;
                        }
                    }
                } else {
                    if (this.isGroundBlock(bid)) {
                        ++inarow;
                    }
                    if (airfound != 0 && inarow >= 3) {
                        break;
                    }
                }
            }
            if (found == 0) {
                // :85-98 - world.rand is the target level's random, OreSpawnRand the shared Random(151),
                // this.random the seed-bound one; evaluation order left to right as in Java 1.7.10.
                posX = (int) entityX + this.world.random.nextInt(3 + i / 5) - this.world.random.nextInt(3 + i / 5);
                if (i > 100) {
                    posX = posX + OreSpawn.OreSpawnRand.nextInt(2 + i / 5) - OreSpawn.OreSpawnRand.nextInt(2 + i / 5);
                }
                if (i > 500) {
                    posX = posX + this.random.nextInt(2 + i / 5) - this.random.nextInt(2 + i / 5);
                }
                posZ = (int) entityZ + this.world.random.nextInt(3 + i / 5) - this.world.random.nextInt(3 + i / 5);
                if (i > 100) {
                    posZ = posZ + OreSpawn.OreSpawnRand.nextInt(2 + i / 5) - OreSpawn.OreSpawnRand.nextInt(2 + i / 5);
                }
                if (i > 500) {
                    posZ = posZ + this.random.nextInt(2 + i / 5) - this.random.nextInt(2 + i / 5);
                }
                airfound = 0;
                inarow = 0;
            }
        }
        if (found == 0) {
            // :103-111 - back to the start column, first air-air over anything; Y 1 when nothing matches.
            posX = (int) entityX;
            posZ = (int) entityZ;
            for (posY = this.searchTop(posX, posZ); posY > 1; --posY) {
                if (this.blockAt(posX, posY + 1, posZ).isAir() && this.blockAt(posX, posY, posZ).isAir()
                        && !this.blockAt(posX, posY - 1, posZ).isAir()) {
                    break;
                }
            }
        }
        final double oldX = entityX;
        final double oldY = par1Entity.getY();
        final double oldZ = entityZ;
        double newX = posX;
        double newZ = posZ;
        final double newY = posY;
        if (newX < 0.0) {
            newX -= 0.5;
        } else {
            newX += 0.5;
        }
        if (newZ < 0.0) {
            newZ -= 0.5;
        } else {
            newZ += 0.5;
        }
        final MinecraftServer minecraftserver = this.world.getServer();
        final ServerLevel worldserver = minecraftserver.getLevel(this.oldWorld.dimension());
        final ServerLevel worldserver2 = minecraftserver.getLevel(this.newdim);
        if (par1Entity instanceof Player) {
            // :138-153 - companions in a 48x24x48 box around the old position that do not sit and belong to
            // the player. They travel first: in 1.7.10 this ran inside placeInPortal, before the player
            // was spawned into the new world.
            final Player ep = (Player) par1Entity;
            final AABB bb = new AABB(oldX - 24.0, oldY - 12.0, oldZ - 24.0, oldX + 24.0, oldY + 12.0, oldZ + 24.0);
            final List<TamableAnimal> var5 = this.oldWorld.getEntitiesOfClass(TamableAnimal.class, bb);
            for (final TamableAnimal et : var5) {
                // PORT: EntityTameable.isSitting() read the synced flag, which is isInSittingPose().
                if (!et.isInSittingPose()) {
                    final String p1 = ep.getUUID().toString();
                    // func_152113_b() was the owner UUID string ("" without an owner), func_152114_e(player)
                    // "player is the owner" - getOwnerUUID and isOwnedBy.
                    final UUID owner = et.getOwnerUUID();
                    final String p2 = owner == null ? null : owner.toString();
                    if ((p1 == null || p2 == null || !p1.equals(p2)) && !et.isOwnedBy(ep)) {
                        continue;
                    }
                    this.sendToThisDimension(et, newX, newY, newZ, (int) ep.getYRot());
                }
            }
        }
        // :130-134 setLocationAndAngles(newX, newY, newZ, rotationYaw, 0) and zero motion, then the
        // configuration manager spawned the entity into the new world.
        par1Entity.changeDimension(new DimensionTransition(this.world, new Vec3(newX, newY, newZ), Vec3.ZERO,
                par1Entity.getYRot(), 0.0f, DimensionTransition.DO_NOTHING));
        // :154-155 resetUpdateEntityTick() on both worlds.
        if (worldserver != null) {
            worldserver.resetEmptyTime();
        }
        if (worldserver2 != null) {
            worldserver2.resetEmptyTime();
        }
        return true;
    }

    /**
     * The first Y of a column search: the original's literal 180 inside the six OreSpawn dimensions (R21), otherwise
     * {@code WORLD_SURFACE} + 1 of the column, at most the build height (R22), read from the chunk after loading it.
     * PORT: no counterpart in the original.
     */
    private int searchTop(final int x, final int z) {
        if (UTOPIA.equals(this.newdim) || MINING.equals(this.newdim) || VILLAGE.equals(this.newdim)
                || DANGER.equals(this.newdim) || CRYSTAL.equals(this.newdim) || CHAOS.equals(this.newdim)) {
            return 180;
        }
        // PORT (BUGHUNT2 2.2, fix2): Level.getHeight returns getMinBuildHeight() (-64) when the chunk is not loaded
        // (Level.java:383-397), so the search started at -63 and never ran. getChunk loads or generates the chunk
        // first (FULL status, as world.getBlock did in 1.7.10); the chunk heightmap does not add the +1 itself.
        final int surface = this.world.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))
                .getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) + 1;
        return Math.min(surface, this.world.getMaxBuildHeight());
    }

    /**
     * {@code sendToThisDimension} (:159-186): remove the animal, create a copy by entity name in the
     * target world, {@code copyDataFrom}, place it with the player's (int) yaw and pitch 0, no motion.
     * {@link Entity#changeDimension} does the same create / {@code restoreFrom} / add sequence.
     *
     * <p>PORT: 1.21.1 drops a lead when an entity changes dimension
     * ({@code Entity.removeAfterChangingDimensions}); the 1.7.10 NBT copy carried the leash tag along.
     */
    public void sendToThisDimension(final Entity e, final double newX, final double newY, final double newZ, final int ro) {
        if (this.oldWorld.isClientSide) {
            return;
        }
        final Entity var6 = e.changeDimension(new DimensionTransition(this.world, new Vec3(newX, newY, newZ), Vec3.ZERO,
                (float) ro, 0.0f, DimensionTransition.DO_NOTHING));
        if (var6 != null) {
            // changeDimension keeps the copied pitch; the original set 0.
            var6.setXRot(0.0f);
        }
    }
}
