package com.swbr.orespawn.entity.ai;

import java.util.EnumSet;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the 1.7.10 vanilla {@code EntityAIMoveIndoors} ({@code ur} in
 * {@code reference/jar/mcp/client-1.7.10.jar}, read with javap; SRG names from joined.srg), the
 * priority-11 task of Girlfriend and Boyfriend and of fourteen later classes (BandP, Camarasaurus,
 * Chipmunk, Dragon, Gazelle, Hydrolisc, Ostrich, Spyro, StinkBug, Stinky, ThePrince, ThePrincess,
 * ThePrinceTeen, VelocityRaptor). DECISIONS R18: "als eigenes Goal nachbauen".
 *
 * <p>The original, line by line:
 * <ul>
 *   <li>{@code shouldExecute} ({@code ur.a}): only when it is night ({@code World.isDaytime} false),
 *       raining, or the biome cannot rain ({@code BiomeGenBase.canSpawnLightningBolt} false), and the
 *       dimension has a sky ({@code !provider.hasNoSky}); then {@code nextInt(50) == 0}; not while the
 *       mob stands within 2 blocks (squared 4, at its own height) of the inside position it reached
 *       last time; then the nearest village within 14 blocks of its border
 *       ({@code VillageCollection.findNearestVillage(x, y, z, 14)}) and its nearest unrestricted door
 *       ({@code Village.findNearestDoorUnrestricted}).</li>
 *   <li>{@code continueExecuting} ({@code ur.b}): while the navigator has a path.</li>
 *   <li>{@code startExecuting} ({@code ur.c}): forget the last inside position; more than 16 blocks
 *       (squared 256) from the door's inside position, walk to a random spot within 14/3 blocks
 *       towards it ({@code RandomPositionGenerator.findRandomTargetBlockTowards}), otherwise straight
 *       to the inside position +0.5, both at speed 1.0.</li>
 *   <li>{@code resetTask} ({@code ur.d}): remember the inside position, drop the door.</li>
 * </ul>
 * Mutex 1 = {@link Goal.Flag#MOVE}.
 *
 * <p>PORT (R18 case 3, the mechanic has no 1.21.1 counterpart): 1.21.1 has no village collection and
 * no door records. A village is defined by points of interest, and the one a mob is "inside" is a
 * home - so the door's inside position becomes the nearest {@code minecraft:home} POI (the head of a
 * bed), searched within {@link #VILLAGE_RANGE} = 32 + 14 blocks: 32 is the smallest 1.7.10 village
 * radius ({@code Village.villageRadius = max(32, ...)}), 14 the argument of the call. A lone bed near
 * a player's house counts, as a lone door did in 1.7.10. Within 16 blocks the original preferred the
 * door with the lowest villager restriction counter, and nothing in this mod raises one; the port
 * takes the closest home instead of the first door in the village's list.
 *
 * <p>PORT: {@code canSpawnLightningBolt} was a biome-wide flag ({@code enableSnow ? false : enableRain});
 * 1.21.1 decides rain or snow per height ({@link Biome#getPrecipitationAt}), which is read at the
 * mob's feet. {@code isDaytime} is {@code skylightSubtracted < 4}, i.e. {@code getSkyDarken() < 4}
 * (same reading as {@link MyEntityAIFollowOwner}).
 *
 * <p>The chance is rolled at the 1.7.10 cadence of one {@code shouldExecute} every three ticks
 * ({@link LegacyAiTick}).
 */
public class EntityAIMoveIndoors extends Goal {

    /** Smallest 1.7.10 village radius (32) plus the {@code findNearestVillage} margin (14). */
    public static final int VILLAGE_RANGE = 32 + 14;

    private final PathfinderMob entityObj;
    /** {@code doorInfo}: here the inside position itself, a home POI. */
    @Nullable
    private BlockPos doorInfo;
    private int insidePosX = -1;
    private int insidePosZ = -1;
    private final LegacyAiTick cadence = new LegacyAiTick();

    /** {@code ur.<init>(td)}: mutex 1. */
    public EntityAIMoveIndoors(final PathfinderMob par1EntityCreature) {
        this.entityObj = par1EntityCreature;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /** {@code shouldExecute} ({@code ur.a}). */
    @Override
    public boolean canUse() {
        // LegacyAiTick contract: consume the ticks before any early return.
        final int rolls = this.cadence.dueRolls(this.entityObj);
        if (!(this.entityObj.level() instanceof ServerLevel level)) {
            return false;
        }
        final int i = Mth.floor(this.entityObj.getX());
        final int j = Mth.floor(this.entityObj.getY());
        final int k = Mth.floor(this.entityObj.getZ());
        final BlockPos feet = new BlockPos(i, j, k);
        final boolean isDaytime = level.getSkyDarken() < 4;
        final boolean canRain = level.getBiome(feet).value().getPrecipitationAt(feet) == Biome.Precipitation.RAIN;
        if (!((!isDaytime || level.isRaining() || !canRain) && level.dimensionType().hasSkyLight())) {
            return false;
        }
        boolean rolled = false;
        for (int r = 0; r < rolls; ++r) {
            if (this.entityObj.getRandom().nextInt(50) == 0) {
                rolled = true;
            }
        }
        if (!rolled) {
            return false;
        }
        if (this.insidePosX != -1
                && this.entityObj.distanceToSqr(this.insidePosX, this.entityObj.getY(), this.insidePosZ) < 4.0) {
            return false;
        }
        final Optional<BlockPos> home = level.getPoiManager()
                .findClosest(holder -> holder.is(PoiTypes.HOME), feet, VILLAGE_RANGE, PoiManager.Occupancy.ANY);
        this.doorInfo = home.map(BlockPos::immutable).orElse(null);
        return this.doorInfo != null;
    }

    /** {@code continueExecuting} ({@code ur.b}). */
    @Override
    public boolean canContinueToUse() {
        return !this.entityObj.getNavigation().isDone();
    }

    /** {@code startExecuting} ({@code ur.c}). */
    @Override
    public void start() {
        this.insidePosX = -1;
        final BlockPos door = this.doorInfo;
        if (door == null) {
            return;
        }
        if (this.entityObj.distanceToSqr(door.getX(), door.getY(), door.getZ()) > 256.0) {
            // PORT: findRandomTargetBlockTowards kept candidates in the target's half-plane
            // (dot product >= 0), which is a direction spread of +-90 degrees.
            final Vec3 vec3 = DefaultRandomPos.getPosTowards(this.entityObj, 14, 3,
                    new Vec3(door.getX() + 0.5, door.getY(), door.getZ() + 0.5), Math.PI / 2.0);
            if (vec3 != null) {
                this.entityObj.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0);
            }
        } else {
            this.entityObj.getNavigation().moveTo(door.getX() + 0.5, door.getY(), door.getZ() + 0.5, 1.0);
        }
    }

    /** {@code resetTask} ({@code ur.d}). */
    @Override
    public void stop() {
        if (this.doorInfo != null) {
            this.insidePosX = this.doorInfo.getX();
            this.insidePosZ = this.doorInfo.getZ();
        }
        this.doorInfo = null;
        this.cadence.skipWhileRunning(this.entityObj);
    }
}
