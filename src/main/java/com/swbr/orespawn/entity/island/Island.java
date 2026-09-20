package com.swbr.orespawn.entity.island;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.Island} (Island.java:13-356), id {@code island}, "Light Floating Island"
 * (OreSpawnMain.java:3635-3639, registerModEntity 64/1/false). The invisible-in-spirit engine of a <em>round</em>
 * floating island of mycelium over end stone and diamond ore: an {@code EntityAnimal} without AI and without physics
 * that builds its island from blocks on its first server tick and then shifts it block by block every 73 ticks
 * (verhalten/entity-09.md, "Island"; catalogue README 4.9: a ticking entity with runtime block writers, not a
 * structure).
 *
 * <p>Spawned only by {@link com.swbr.orespawn.block.island.IslandBlock} (1 in 25). Never despawns. Drops the
 * Island Block. No attributes of its own (1.7.10 {@code EntityLivingBase} defaults, health 20), no DataWatcher, no
 * sounds.
 *
 * <p>Coordinates. {@code posY} casts are {@link Mth#floor} (R20). The X/Z casts stay the original
 * {@code (int)} truncation: the ring positions, the cell-change test ({@code (int) myX != (int) posX}), the snap to
 * the cell centre ({@code (int) myX +- 0.5}) and the explicit {@code xoff/zoff = -1} sign corrections at the
 * centre block form one self-consistent algorithm built on truncation - replacing the casts with {@code floor} while
 * keeping the corrections would move every island on the negative side by one block, removing the corrections would
 * be a rewrite. PORT: deliberate, same class as the R20 exception for the {@code (int)} casts of the teleporter and
 * the GenericDungeon circles ("der Versatz ist Teil des Originalverhaltens, auch im Negativen").
 *
 * <p>XP: 1.7.10 {@code EntityAnimal.getExperiencePoints} gives 1-3, which {@link Animal#getBaseExperienceReward}
 * still does (W04) - nothing to override.
 */
public class Island extends Animal {

    /** Registry id of the Triffid the island spawns; looked up by id because the class is ported in parallel (W07 precedent). */
    private static final ResourceLocation TRIFFID_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "triffid");

    private float dir;
    private float speed;
    private int radius;
    private int depth;
    private final int timer;
    private int just_spawned;
    private int ticker;
    private int once;
    private double myX;
    private double myY;
    private double myZ;
    private int dirchange;

    /** Constructor (:28-41); {@code setSize(0.5f, 0.5f)} is the entity type's size. */
    public Island(final EntityType<? extends Island> type, final Level par1World) {
        super(type, par1World);
        this.dir = 0.0f;
        this.speed = 0.1f;
        this.radius = 5;
        this.depth = 3;
        this.timer = 73;
        this.just_spawned = 1;
        this.ticker = 0;
        this.once = 1;
        this.ticker = par1World.random.nextInt(50);
        this.dirchange = par1World.random.nextInt(2500);
    }

    /**
     * No {@code applyEntityAttributes} in Island.java: the 1.7.10 {@code EntityLiving} defaults (health 20, follow
     * range 16, speed 0.7) - {@link Mob#createMobAttributes()}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    /**
     * {@code onUpdate} (:43-91): the inherited tick, then all motion to 0; on the server the first-tick build, the
     * 73-tick shift and the direction change countdown. All randoms are {@code worldObj.rand} - {@code level().random}.
     */
    @Override
    public void tick() {
        super.tick();
        final double motionX = 0.0;
        this.setDeltaMovement(motionX, motionX, motionX);
        if (this.level().isClientSide) {
            return;
        }
        if (this.once != 0) {
            this.myX = this.getX();
            this.myY = this.getY();
            this.myZ = this.getZ();
            this.once = 0;
        }
        if (this.just_spawned != 0) {
            this.dir = this.level().random.nextFloat() * 3.1415927f;
            if (this.level().random.nextInt(2) == 1) {
                this.dir *= -1.0f;
            }
            if (this.level().random.nextInt(40) != 1) {
                this.radius = 3 + this.level().random.nextInt(4);
                this.depth = 2 + this.level().random.nextInt(3);
                this.speed = this.level().random.nextFloat() / 50.0f * TweakStats.IslandSpeedFactor();
            } else {
                this.radius = 6 + this.level().random.nextInt(5);
                this.depth = 3 + this.level().random.nextInt(4);
                this.speed = this.level().random.nextFloat() / 200.0f * TweakStats.IslandSpeedFactor();
            }
            this.create_island();
            this.ticker = this.level().random.nextInt(50);
            this.dirchange = this.level().random.nextInt(10000);
        }
        ++this.ticker;
        if (this.ticker >= this.timer) {
            this.update_island();
            this.ticker = 0;
        }
        --this.dirchange;
        if (this.dirchange <= 0) {
            this.dirchange = this.level().random.nextInt(5000);
            this.dir = this.level().random.nextFloat() * 3.1415927f;
            if (this.level().random.nextInt(2) == 1) {
                this.dir *= -1.0f;
            }
        }
        this.just_spawned = 0;
    }

    /**
     * {@code onLivingUpdate} (:93-97): only on the client. On the server no movement, no gravity, no AI, no pushing,
     * no love or age countdown - which also makes {@code updateAITick} and {@code updateAITasks} (:99-103, empty)
     * unreachable, as they were.
     */
    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            super.aiStep();
        }
    }

    /** {@code updateAITasks} (:102-103), empty. Unreachable on the server anyway, see {@link #aiStep}. */
    @Override
    protected void customServerAiStep() {
    }

    /** {@code fall} (:105-106), empty: no fall damage. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code canDespawn} (:108-110). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code readEntityFromNBT} (:112-119). {@code myX/Y/Z} are not saved: {@code once} re-reads the position. */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.just_spawned = par1NBTTagCompound.getInt("JustSpawned");
        this.depth = par1NBTTagCompound.getInt("Idepth");
        this.radius = par1NBTTagCompound.getInt("Iradius");
        this.speed = par1NBTTagCompound.getFloat("Ispeed");
        this.dir = par1NBTTagCompound.getFloat("Idir");
    }

    /** {@code writeEntityToNBT} (:121-128). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("JustSpawned", this.just_spawned);
        par1NBTTagCompound.putInt("Idepth", this.depth);
        par1NBTTagCompound.putInt("Iradius", this.radius);
        par1NBTTagCompound.putFloat("Ispeed", this.speed);
        par1NBTTagCompound.putFloat("Idir", this.dir);
    }

    /** {@code createChild} (:130-132). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** Not overridden: 1.7.10 {@code EntityAnimal.isBreedingItem} is wheat. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** No {@code interact} in Island.java: 1.7.10 {@code EntityAnimal.interact} (see {@link HerbivoreSupport#legacyAnimalInteract}). */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /**
     * {@code create_island} (:134-193): {@code depth} discs of radius {@code radius / (i + 1)}, sampled every 6 degrees
     * and every 0.35 blocks from 0.75. The top disc (Y+1) only fills air - 1/5000 lava, else mycelium with a 1/20
     * mushroom; bedrock kills the island. Lower discs overwrite anything with end stone or 1/10 diamond ore.
     */
    private void create_island() {
        final double deltadir = 0.10471975333333333;
        final double deltamag = 0.3499999940395355;
        int ixlast = 0;
        int izlast = 0;
        int xoff = 0;
        int zoff = 0;
        final Level world = this.level();
        for (int i = 0; i < this.depth; ++i) {
            izlast = (ixlast = 0);
            for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
                double tradius = this.radius;
                tradius /= i + 1;
                for (double h = 0.75; h < tradius; h += deltamag) {
                    final int ix = (int) (this.getX() + Math.cos(curdir + this.dir) * h);
                    final int iz = (int) (this.getZ() + Math.sin(curdir + this.dir) * h);
                    if (ix != ixlast || iz != izlast) {
                        ixlast = ix;
                        izlast = iz;
                        if (i == 0) {
                            final BlockState bid;
                            if ((bid = this.getBlock(ix, Mth.floor(this.getY()) - i + 1, iz)).isAir()) {
                                if (world.random.nextInt(5000) == 1) {
                                    this.setBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.LAVA);
                                } else {
                                    this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.MYCELIUM);
                                    if (world.random.nextInt(20) == 1 && this.getBlock(ix, Mth.floor(this.getY()) - i + 2, iz).isAir()) {
                                        if (world.random.nextInt(2) == 1) {
                                            this.setBlock(ix, Mth.floor(this.getY()) - i + 2, iz, Blocks.BROWN_MUSHROOM);
                                        } else {
                                            this.setBlock(ix, Mth.floor(this.getY()) - i + 2, iz, Blocks.RED_MUSHROOM);
                                        }
                                    }
                                }
                            } else if (bid.is(Blocks.BEDROCK)) {
                                this.discard();
                                return;
                            }
                        } else if (world.random.nextInt(10) == 1) {
                            this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.DIAMOND_ORE);
                        } else {
                            this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.END_STONE);
                        }
                    }
                }
            }
        }
        if (this.getX() < 0.0) {
            xoff = -1;
        }
        if (this.getZ() < 0.0) {
            zoff = -1;
        }
        this.setBlock((int) this.getX() + xoff, Mth.floor(this.getY()), (int) this.getZ() + zoff, Blocks.AIR);
        this.FastSetBlock((int) this.getX() + xoff, Mth.floor(this.getY()), (int) this.getZ() + zoff, Blocks.AIR);
    }

    /**
     * {@code update_island} (:195-336): advance the virtual position; when it enters another block cell, clear the
     * outer ring of every disc (and the mushrooms on the top ring), put end stone at the old centre, jump to the new
     * cell centre, rebuild the outer ring - a lower disc hitting stone sets off a block-breaking explosion of 5 - and
     * clear the new centre. Then, 1 in {@code 2 + 2000 / 73} = 29, a Triffid on top if none stands within 10/5/10.
     */
    private void update_island() {
        final double deltadir = 0.10471975333333333;
        final double deltamag = 0.3499999940395355;
        final double pi2 = 1.57079632675;
        int ixlast = 0;
        int izlast = 0;
        int xoff = 0;
        int zoff = 0;
        final Level world = this.level();
        this.myX += this.speed * Math.cos(this.dir);
        this.myZ += this.speed * Math.sin(this.dir);
        final int mx = (int) this.myX;
        final int mz = (int) this.myZ;
        final int px = (int) this.getX();
        final int pz = (int) this.getZ();
        if (mx != px || mz != pz) {
            for (int i = 0; i < this.depth; ++i) {
                izlast = (ixlast = 0);
                for (double curdir = -3.3; curdir < 3.3; curdir += deltadir / 2.0) {
                    double tradius;
                    double h;
                    for (tradius = this.radius, tradius /= i + 1, h = 0.75; h < tradius; h += deltamag) {
                    }
                    h -= deltamag;
                    if (h < 0.75) {
                        h = 0.75;
                    }
                    while (h < tradius + deltamag) {
                        final int ix = (int) (this.getX() + Math.cos(curdir + this.dir) * h);
                        final int iz = (int) (this.getZ() + Math.sin(curdir + this.dir) * h);
                        if (ix != ixlast || iz != izlast) {
                            ixlast = ix;
                            izlast = iz;
                            if (i == 0) {
                                final BlockState bid = this.getBlock(ix, Mth.floor(this.getY()) + 1 + 1, iz);
                                if (bid.is(Blocks.RED_MUSHROOM) || bid.is(Blocks.BROWN_MUSHROOM)) {
                                    this.FastSetBlock(ix, Mth.floor(this.getY()) + 1 + 1, iz, Blocks.AIR);
                                }
                            }
                            this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.AIR);
                        }
                        h += deltamag / 2.0;
                    }
                }
            }
            if (this.getX() < 0.0) {
                xoff = -1;
            }
            if (this.getZ() < 0.0) {
                zoff = -1;
            }
            this.setBlock((int) this.getX() + xoff, Mth.floor(this.getY()), (int) this.getZ() + zoff, Blocks.END_STONE);
            double newX = (int) this.myX;
            if (this.myX < 0.0) {
                newX -= 0.5;
            } else {
                newX += 0.5;
            }
            this.setPosX(newX);
            double newZ = (int) this.myZ;
            if (this.myZ < 0.0) {
                newZ -= 0.5;
            } else {
                newZ += 0.5;
            }
            this.setPosZ(newZ);
            for (int i = 0; i < this.depth; ++i) {
                izlast = (ixlast = 0);
                for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
                    double tradius;
                    double h;
                    for (tradius = this.radius, tradius /= i + 1, h = 0.75; h < tradius; h += deltamag) {
                    }
                    h -= deltamag * 3.0;
                    if (h < 0.75) {
                        h = 0.75;
                    }
                    while (h < tradius) {
                        final int ix = (int) (this.getX() + Math.cos(curdir + this.dir) * h);
                        final int iz = (int) (this.getZ() + Math.sin(curdir + this.dir) * h);
                        if (ix != ixlast || iz != izlast) {
                            ixlast = ix;
                            izlast = iz;
                            if (i == 0) {
                                final BlockState bid;
                                if ((bid = this.getBlock(ix, Mth.floor(this.getY()) - i + 1, iz)).isAir()) {
                                    if (world.random.nextInt(5000) == 1) {
                                        this.setBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.LAVA);
                                    } else {
                                        this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.MYCELIUM);
                                        if (world.random.nextInt(20) == 1 && this.getBlock(ix, Mth.floor(this.getY()) - i + 2, iz).isAir()) {
                                            if (world.random.nextInt(2) == 1) {
                                                this.setBlock(ix, Mth.floor(this.getY()) - i + 2, iz, Blocks.BROWN_MUSHROOM);
                                            } else {
                                                this.setBlock(ix, Mth.floor(this.getY()) - i + 2, iz, Blocks.RED_MUSHROOM);
                                            }
                                        }
                                    }
                                } else if (bid.is(Blocks.BEDROCK)) {
                                    this.discard();
                                    return;
                                }
                            } else {
                                final BlockState bid = this.getBlock(ix, Mth.floor(this.getY()) - i + 1, iz);
                                if (bid.is(Blocks.STONE)) {
                                    if (!world.isClientSide) {
                                        // createExplosion(this, x, y, z, 5.0f, true): block damage regardless of
                                        // mobGriefing - TNT interaction (verhalten/entity-09.md).
                                        world.explode(this, ix, this.getY() - i + 1.0, iz, 5.0f, Level.ExplosionInteraction.TNT);
                                    }
                                } else if (world.random.nextInt(10) == 1) {
                                    this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.DIAMOND_ORE);
                                } else {
                                    this.FastSetBlock(ix, Mth.floor(this.getY()) - i + 1, iz, Blocks.END_STONE);
                                }
                            }
                        }
                        h += deltamag;
                    }
                }
            }
            xoff = 0;
            if (this.getX() < 0.0) {
                xoff = -1;
            }
            zoff = 0;
            if (this.getZ() < 0.0) {
                zoff = -1;
            }
            this.setBlock((int) this.getX() + xoff, Mth.floor(this.getY()), (int) this.getZ() + zoff, Blocks.AIR);
            this.FastSetBlock((int) this.getX() + xoff, Mth.floor(this.getY()), (int) this.getZ() + zoff, Blocks.AIR);
        }
        if (world.random.nextInt(2 + 2000 / this.timer) == 1) {
            final AABB bb = new AABB(this.getX() - 10.0, this.getY() - 5.0, this.getZ() - 10.0,
                    this.getX() + 10.0, this.getY() + 5.0, this.getZ() + 10.0);
            final Optional<EntityType<?>> triffid = BuiltInRegistries.ENTITY_TYPE.getOptional(TRIFFID_ID);
            // getEntitiesWithinAABB(Triffid.class, bb): Triffid has no subclass, so the type is the same set.
            final List<Entity> var5 = world.getEntities((Entity) null, bb,
                    e -> triffid.isPresent() && e.getType() == triffid.get());
            if (var5.isEmpty()) {
                // PORT: EntityList.createEntityByName("Triffid") returned null for an unknown name and spawned nothing;
                // an absent registry entry behaves the same.
                triffid.ifPresent(type -> spawnCreature(world, type, this.getX(), this.getY() + 2.01, this.getZ()));
            }
        }
    }

    /**
     * {@code spawnCreature} (:338-347): create by type (no {@code onSpawnWithEgg}, so no {@code finalizeSpawn}),
     * random yaw, add, and play the living sound.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2,
                                       final double par4, final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            // ((EntityLiving) var8).playLivingSound(): the original cast unconditionally.
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems(recentlyHit, looting)}, then {@code dropEquipment}. Looting counts only
     * for a player kill.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        int looting = 0;
        if (damageSource.getEntity() instanceof Player killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        this.dropFewItems(recentlyHit, looting);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** The inherited 1.7.10 {@code EntityLiving.dropFewItems}: {@code rand(3)} plus {@code rand(looting + 1)} single stacks. */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Item item = this.getDropItem();
        if (item != null) {
            int j = this.random.nextInt(3);
            if (par2 > 0) {
                j += this.random.nextInt(par2 + 1);
            }
            for (int k = 0; k < j; ++k) {
                this.spawnAtLocation(new ItemStack(item, 1));
            }
        }
    }

    /** {@code getDropItem} (:349-351): the Island Block. */
    @Nullable
    protected Item getDropItem() {
        return ModBlocks.ISLAND.get().asItem();
    }

    /** {@code FastSetBlock} (:353-355): {@code OreSpawnMain.setBlockFast(world, x, y, z, id, 0, 3)}. */
    public void FastSetBlock(final int ix, final int iy, final int iz, final Block id) {
        FastBlocks.setBlockFast(this.level(), ix, iy, iz, id.defaultBlockState(), 3);
    }

    /** 1.7.10 {@code World.setBlock(x, y, z, block)}: metadata 0, flags 3. */
    private void setBlock(final int x, final int y, final int z, final Block block) {
        this.level().setBlock(new BlockPos(x, y, z), block.defaultBlockState(), Block.UPDATE_ALL);
    }

    /**
     * 1.7.10 {@code World.getBlock}. PORT: {@code == Blocks.air} is {@link BlockState#isAir()} at the call sites -
     * 1.21.1 splits air into air, cave air and void air, 1.7.10 had one block.
     */
    private BlockState getBlock(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /** {@code this.posX = v}: PORT: {@code setPos} so the bounding box follows (1.7.10 moved it on the next tick). */
    private void setPosX(final double v) {
        this.setPos(v, this.getY(), this.getZ());
    }

    /** {@code this.posZ = v}, see {@link #setPosX}. */
    private void setPosZ(final double v) {
        this.setPos(this.getX(), this.getY(), v);
    }

    // ---- read access for GameTests; the original had no accessors.

    public int islandRadius() {
        return this.radius;
    }

    public int islandDepth() {
        return this.depth;
    }

    public float islandDir() {
        return this.dir;
    }

    public float islandSpeed() {
        return this.speed;
    }

    /** For GameTests: fix direction and speed after the first build (e.g. {@code dir = 0}, a full block per update). */
    public void setIslandMotion(final float dir, final float speed) {
        this.dir = dir;
        this.speed = speed;
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
