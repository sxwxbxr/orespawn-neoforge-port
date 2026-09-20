package com.swbr.orespawn.entity.insect;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.registry.ModBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.EntityLunaMoth} (EntityLunaMoth.java:9-206), id {@code moth}. A night
 * flyer that seeks out torches. It is an {@link EntityButterfly}, and everything of the butterfly runs
 * first: the butterfly flight, the vampire bite when the inherited {@code butterfly_type} is 1 in the
 * Islands dimension, the Chaos teleport on an empty-hand click and {@code motionY *= 0.6} - the moth
 * then steers again with its own target and damps once more (effectively ×0.36 per tick). All of that
 * is original behaviour and kept (R18).
 *
 * <p>{@code moth_type} was neither synchronised nor saved in 1.7.10 (:12, :26): client and server rolled
 * it independently and a reload rolled it again. DECISIONS R18 (case 4) rules: synchronise and save.
 */
public class EntityLunaMoth extends EntityButterfly {

    /** New synced value for {@code moth_type} 0..3 (R18 case 4). */
    private static final EntityDataAccessor<Integer> DATA_MOTH_TYPE =
            SynchedEntityData.defineId(EntityLunaMoth.class, EntityDataSerializers.INT);

    /** Hides the butterfly's own flight target (:11): the two targets are steered independently. */
    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    /**
     * Constructor (:18-29): the butterfly constructor rolls {@code butterfly_type} first, then the moth
     * rolls {@code moth_type} - same order on the shared {@code OreSpawnRand}. {@code setSize(0.5f, 0.5f)}
     * is the entity type's size. PORT: rolled on the server only, the client receives it (R18 case 4).
     */
    public EntityLunaMoth(final EntityType<? extends EntityLunaMoth> type, final Level level) {
        super(type, level);
        if (!level.isClientSide) {
            this.setMothType(OreSpawn.OreSpawnRand.nextInt(4));
        }
        this.setPathfindingMalus(PathType.WATER, -1.0f); // :28
    }

    /** {@code applyEntityAttributes} (:31-37): the butterfly values again. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOTH_TYPE, 0);
    }

    /** The public field {@code moth_type}. */
    public int getMothType() {
        return this.entityData.get(DATA_MOTH_TYPE);
    }

    public void setMothType(final int moth_type) {
        this.entityData.set(DATA_MOTH_TYPE, moth_type);
    }

    /** The {@code instanceof EntityLunaMoth} arm of {@code EntityButterfly.getTexture} (EntityButterfly.java:58-69). */
    @Override
    public ResourceLocation getTexture() {
        if (this.getMothType() == 1) {
            return texture5;
        }
        if (this.getMothType() == 2) {
            return texture7;
        }
        if (this.getMothType() == 3) {
            return texture8;
        }
        return texture6;
    }

    /** {@code RenderButterfly.shouldRenderPass} (RenderButterfly.java:47-51): the luna moth (type 0) glows. */
    @Override
    public boolean hasGlowPass() {
        return this.getMothType() == 0;
    }

    /** {@code onUpdate} (:53-57): after the butterfly's {@code motionY *= 0.6000000238418579}, once more ×0.6. */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    /**
     * 1.7.10 {@code Blocks.torch} carried standing and wall torch in one block, as did
     * {@code OreSpawnMain.ExtremeTorch}; both twins count (R18 torch ids).
     */
    private static boolean isTorch(final BlockState bid) {
        return bid.is(Blocks.TORCH) || bid.is(Blocks.WALL_TORCH)
                || bid.is(ModBlocks.EXTREME_TORCH.get()) || bid.is(ModBlocks.EXTREME_TORCH_WALL.get());
    }

    /** {@code scan_it} (:59-140): the six faces of the shell at distance (dx, dy, dz), nearest torch wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        final Level world = this.level();
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = world.getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (isTorch(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = world.getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (isTorch(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x - dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = world.getBlockState(new BlockPos(x + i, y + dy, z + j));
                if (isTorch(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = world.getBlockState(new BlockPos(x + i, y - dy, z + j));
                if (isTorch(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y - dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                BlockState bid = world.getBlockState(new BlockPos(x + i, y + j, z + dz));
                if (isTorch(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                bid = world.getBlockState(new BlockPos(x + i, y + j, z - dz));
                if (isTorch(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z - dz;
                        ++found;
                    }
                }
            }
        }
        return found != 0;
    }

    /**
     * {@code updateAITasks} (:142-182): first the whole butterfly step (:148), then the moth's own target
     * - a new random one (x/z ±9) with 1/100 or when reached, otherwise at night with 1/10 the torch search
     * over the shells 2, 3, 4, 5, 6, 8, 10, 12, 14 (:163-167) - and a second steering pass with y 0.68 and
     * {@code moveForward 0.75}.
     */
    @Override
    protected void customServerAiStep() {
        int keep_trying = 25;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.random.nextInt(100) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 4.0f) {
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0;
                    bid = this.level().getBlockState(this.currentFlightTarget), --keep_trying) {
                this.currentFlightTarget.set(
                        Mth.floor(this.getX()) + this.random.nextInt(10) - this.random.nextInt(10),
                        Mth.floor(this.getY()) + this.random.nextInt(6) - 2,
                        Mth.floor(this.getZ()) + this.random.nextInt(10) - this.random.nextInt(10));
            }
        } else if (!InsectSupport.isDaytime(this.level()) && this.random.nextInt(10) == 0) {
            this.closest = 99999;
            this.tz = 0; // :159-162, a decompiled "tx = ty = tz = false ? 1 : 0"
            this.ty = 0;
            this.tx = 0;
            for (int i = 2; i < 15 && !this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()), i, i, i); ++i) {
                if (i >= 6) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.currentFlightTarget.set(this.tx, this.ty + 1, this.tz);
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.5 - motion.x) * 0.10000000149011612;
        final double motionY = motion.y + (Math.signum(var2) * 0.68 - motion.y) * 0.10000000149011612;
        final double motionZ = motion.z + (Math.signum(var3) * 0.5 - motion.z) * 0.10000000149011612;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var5);
    }

    /**
     * {@code getCanSpawnHere} (:197-201): air, <em>night</em>, and the Islands dimension or Y ≥ 50. No
     * spawner case - the butterfly's scan and its type-1 side effect are not inherited.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        final BlockState bid = level.getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())));
        return bid.isAir() && !InsectSupport.isDaytime(this.level())
                && (this.level().dimension().equals(InsectSupport.DIMENSION_ISLANDS) || this.getY() >= 50.0);
    }

    /** {@code getCanSpawnHere} (:197-201) as the placement predicate. */
    public static boolean checkMothSpawnRules(final EntityType<EntityLunaMoth> type, final ServerLevelAccessor level,
                                              final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final ServerLevel world = level.getLevel();
        return level.getBlockState(pos).isAir() && !InsectSupport.isDaytime(world)
                && (world.dimension().equals(InsectSupport.DIMENSION_ISLANDS) || pos.getY() >= 50.0);
    }

    /** PORT: new key {@code MothType} (R18 case 4); the original saved nothing of its own. */
    @Override
    public void addAdditionalSaveData(final CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MothType", this.getMothType());
    }

    /**
     * PORT: a moth saved before the key existed has none; it keeps the type rolled in its constructor,
     * which is what every load did in 1.7.10.
     */
    @Override
    public void readAdditionalSaveData(final CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MothType")) {
            this.setMothType(tag.getInt("MothType"));
        }
    }
}
