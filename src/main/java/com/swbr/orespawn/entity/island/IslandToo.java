package com.swbr.orespawn.entity.island;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
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

/**
 * Port of {@code danger.orespawn.IslandToo} (IslandToo.java:12-485), id {@code island_too}, "Dark Floating Island"
 * (OreSpawnMain.java:3643-3647, registerModEntity 64/1/false). The engine of a <em>square</em> grass island with an
 * ore core, an upside-down pyramid that is widest at the top; 24 of 25 island spawns (verhalten/entity-09.md,
 * "IslandToo"). Same shape of class as {@link Island}: an {@code EntityAnimal} shell without AI or physics that
 * writes its blocks from the entity tick, every 42 ticks, one block cell at a time in one of four directions.
 *
 * <p>Coordinates: as in {@link Island}, Y casts are {@link Mth#floor} (R20) and the X/Z casts keep the original
 * truncation with its explicit {@code xoff/zoff = 1} corrections (PORT, see the class comment there).
 *
 * <p>XP 1-3 through {@link Animal#getBaseExperienceReward} (1.7.10 {@code EntityAnimal}, W04).
 */
public class IslandToo extends Animal {

    private int dir;
    private float speed;
    private int width;
    private int depth;
    private int length;
    private final int timer;
    private int just_spawned;
    private int ticker;
    private int once;
    private double myX;
    private double myY;
    private double myZ;
    private int dirchange;
    private int blocktype;

    /** Constructor (IslandToo.java:29-45); {@code setSize(0.5f, 0.5f)} is the entity type's size. */
    public IslandToo(final EntityType<? extends IslandToo> type, final Level par1World) {
        super(type, par1World);
        this.dir = 0;
        this.speed = 0.1f;
        this.width = 5;
        this.depth = 3;
        this.length = 10;
        this.timer = 42;
        this.just_spawned = 1;
        this.ticker = 0;
        this.once = 1;
        this.dirchange = 0;
        this.blocktype = 0;
        this.ticker = par1World.random.nextInt(50);
        this.dirchange = par1World.random.nextInt(5000);
    }

    /** No {@code applyEntityAttributes}: the 1.7.10 {@code EntityLiving} defaults. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    /** {@code onUpdate} (IslandToo.java:47-97): see {@link Island#tick()}; direction is one of four. */
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
            this.dir = this.level().random.nextInt(4);
            if (this.level().random.nextInt(40) != 1) {
                this.width = 1 + this.level().random.nextInt(5 * TweakStats.IslandSizeFactor());
                this.length = this.width;
                this.depth = 1 + this.level().random.nextInt(4);
                this.speed = this.level().random.nextFloat() / 40.0f * TweakStats.IslandSpeedFactor();
                if (this.length * this.width * this.depth <= 64) {
                    this.speed *= 2.0f;
                }
                if (this.length * this.width * this.depth <= 32) {
                    this.speed *= 2.0f;
                }
            } else {
                this.width = 5 + this.level().random.nextInt(8 * TweakStats.IslandSizeFactor());
                this.length = this.width;
                this.depth = 3 + this.level().random.nextInt(6);
                this.speed = this.level().random.nextFloat() / 150.0f * TweakStats.IslandSpeedFactor();
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
            this.dir = this.level().random.nextInt(4);
        }
        this.just_spawned = 0;
    }

    /** {@code onLivingUpdate}: only on the client, see {@link Island#aiStep()}. */
    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            super.aiStep();
        }
    }

    /** {@code updateAITasks}, empty. */
    @Override
    protected void customServerAiStep() {
    }

    /** {@code fall}, empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code canDespawn}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code readEntityFromNBT} (IslandToo.java:118-127). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.just_spawned = par1NBTTagCompound.getInt("JustSpawned");
        this.width = par1NBTTagCompound.getInt("Iwidth");
        this.depth = par1NBTTagCompound.getInt("Idepth");
        this.length = par1NBTTagCompound.getInt("Ilength");
        this.speed = par1NBTTagCompound.getFloat("Ispeed");
        this.dir = par1NBTTagCompound.getInt("Idir");
        this.blocktype = par1NBTTagCompound.getInt("Iblocktype");
    }

    /** {@code writeEntityToNBT} (IslandToo.java:129-138). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("JustSpawned", this.just_spawned);
        par1NBTTagCompound.putInt("Iwidth", this.width);
        par1NBTTagCompound.putInt("Idepth", this.depth);
        par1NBTTagCompound.putInt("Ilength", this.length);
        par1NBTTagCompound.putFloat("Ispeed", this.speed);
        par1NBTTagCompound.putInt("Idir", this.dir);
        par1NBTTagCompound.putInt("Iblocktype", this.blocktype);
    }

    /** {@code createChild}. */
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

    /** No {@code interact}: 1.7.10 {@code EntityAnimal.interact}. */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /**
     * {@code attackEntityFrom} (IslandToo.java:144-170): snap the position to the centre of its block cell, take the
     * damage through {@code super}, and always answer {@code false}. The damage still lands; the caller (a player's
     * attack) sees a miss - no extra knockback, no fire aspect, no item wear, as in 1.7.10.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final boolean ret = false;
        int xoff = 0;
        int zoff = 0;
        final int ix = (int) this.getX();
        final int iz = (int) this.getZ();
        double newX;
        double newZ;
        if (ix < 0) {
            xoff = 1;
            newX = ix;
            newX -= 0.5;
        } else {
            newX = ix;
            newX += 0.5;
        }
        if (iz < 0) {
            zoff = 1;
            newZ = iz;
            newZ -= 0.5;
        } else {
            newZ = iz;
            newZ += 0.5;
        }
        this.setPos(newX, this.getY(), newZ);
        super.hurt(par1DamageSource, par2);
        return ret;
    }

    /**
     * {@code create_island} (IslandToo.java:172-225): layers k = 0..depth at Y+k with half edge
     * {@code length / (depth - k + 1)}, at least 1. The top layer fills only air: 1/5000 water, else grass with 1/30 a
     * pink or blue flower, else 1/100 a small sky tree; bedrock kills the island. Lower layers are {@link #mySetBlock}.
     */
    private void create_island() {
        int xoff = 0;
        int zoff = 0;
        final Level world = this.level();
        if (this.getX() < 0.0) {
            xoff = 1;
        }
        if (this.getZ() < 0.0) {
            zoff = 1;
        }
        for (int k = 0; k <= this.depth; ++k) {
            int il = this.length / (this.depth - k + 1);
            if (il < 1) {
                il = 1;
            }
            for (int i = -il; i <= il; ++i) {
                for (int j = -il; j <= il; ++j) {
                    final int ix = (int) this.getX() + j - xoff;
                    final int iz = (int) this.getZ() + i - zoff;
                    if (k == this.depth) {
                        final BlockState bid;
                        if ((bid = this.getBlock(ix, Mth.floor(this.getY()) + k, iz)).isAir()) {
                            if (world.random.nextInt(5000) == 1) {
                                this.setBlock(ix, Mth.floor(this.getY()) + k, iz, Blocks.WATER);
                            } else {
                                this.FastSetBlock(ix, Mth.floor(this.getY()) + k, iz, Blocks.GRASS_BLOCK);
                                if (world.random.nextInt(30) == 1) {
                                    if (this.getBlock(ix, Mth.floor(this.getY()) + k + 1, iz).isAir()) {
                                        if (world.random.nextInt(2) == 1) {
                                            this.setBlock(ix, Mth.floor(this.getY()) + k + 1, iz, ModBlocks.FLOWER_PINK.get());
                                        } else {
                                            this.setBlock(ix, Mth.floor(this.getY()) + k + 1, iz, ModBlocks.FLOWER_BLUE.get());
                                        }
                                    }
                                } else if (world.random.nextInt(100) == 1 && this.getBlock(ix, Mth.floor(this.getY()) + k + 1, iz).isAir()) {
                                    com.swbr.orespawn.world.tree.Trees.SmallTree(world, ix, Mth.floor(this.getY()) + k + 1, iz);
                                }
                            }
                        } else if (bid.is(Blocks.BEDROCK)) {
                            this.discard();
                            return;
                        }
                    } else {
                        this.mySetBlock(ix, Mth.floor(this.getY()) + k, iz);
                    }
                }
            }
        }
        this.setBlock((int) this.getX() - xoff, Mth.floor(this.getY()), (int) this.getZ() - zoff, Blocks.AIR);
    }

    /**
     * {@code mySetBlock} (IslandToo.java:227-283): the ore kind is rolled once per island (1-8); stone gets that ore
     * at its chance, and a block that is still stone gets eight independent 1/3000 rolls for a treasure block, a
     * later hit overwriting an earlier one.
     */
    private void mySetBlock(final int ix, final int iy, final int iz) {
        final Level world = this.level();
        Block bid = Blocks.STONE;
        if (this.blocktype == 0) {
            this.blocktype = 1 + world.random.nextInt(8);
        }
        if (this.blocktype == 1 && world.random.nextInt(5) == 1) {
            bid = Blocks.COAL_ORE;
        }
        if (this.blocktype == 2 && world.random.nextInt(10) == 1) {
            bid = Blocks.IRON_ORE;
        }
        if (this.blocktype == 3 && world.random.nextInt(20) == 1) {
            bid = Blocks.EMERALD_ORE;
        }
        if (this.blocktype == 4 && world.random.nextInt(30) == 1) {
            bid = ModBlocks.ORETITANIUM.get();
        }
        if (this.blocktype == 5 && world.random.nextInt(30) == 1) {
            bid = ModBlocks.OREURANIUM.get();
        }
        if (this.blocktype == 6 && world.random.nextInt(30) == 1) {
            bid = ModBlocks.ORERUBY.get();
        }
        if (this.blocktype == 7 && world.random.nextInt(30) == 1) {
            bid = ModBlocks.OREAMETHYST.get();
        }
        if (this.blocktype == 8 && world.random.nextInt(20) == 1) {
            bid = Blocks.GOLD_ORE;
        }
        if (bid == Blocks.STONE) {
            if (world.random.nextInt(3000) == 1) {
                bid = ModBlocks.BLOCKENDERPEARL.get();
            }
            if (world.random.nextInt(3000) == 2) {
                bid = ModBlocks.BLOCKEYEOFENDER.get();
            }
            if (world.random.nextInt(3000) == 3) {
                bid = ModBlocks.BLOCKAMETHYST.get();
            }
            if (world.random.nextInt(3000) == 4) {
                bid = ModBlocks.BLOCKRUBY.get();
            }
            if (world.random.nextInt(3000) == 5) {
                bid = ModBlocks.BLOCKURANIUM.get();
            }
            if (world.random.nextInt(3000) == 6) {
                bid = ModBlocks.BLOCKTITANIUM.get();
            }
            if (world.random.nextInt(3000) == 7) {
                bid = Blocks.GOLD_BLOCK;
            }
            if (world.random.nextInt(3000) == 8) {
                bid = Blocks.DIAMOND_BLOCK;
            }
        }
        this.FastSetBlock(ix, iy, iz, bid);
    }

    /**
     * {@code update_island} (IslandToo.java:285-476): advance the virtual position along the axis of {@code dir}
     * (0 -Z, 1 +Z, 2 +X, 3 -X); on a cell change clear the trailing edge of every layer (with flowers, water and up to
     * three sky tree logs on the top layer), put {@link #mySetBlock} at the old centre, jump, clear the new centre,
     * build the leading edge - a lower layer hitting end stone explodes with 5 - and clear the centre again.
     */
    private void update_island() {
        int xoff = 0;
        int zoff = 0;
        final Level world = this.level();
        if (this.dir == 0) {
            this.myZ -= this.speed;
        } else if (this.dir == 1) {
            this.myZ += this.speed;
        } else if (this.dir == 2) {
            this.myX += this.speed;
        } else {
            this.myX -= this.speed;
        }
        int ke;
        int ks;
        int js;
        int je = js = (ks = (ke = 0));
        final int mx = (int) this.myX;
        final int mz = (int) this.myZ;
        final int px = (int) this.getX();
        final int pz = (int) this.getZ();
        if (mx != px || mz != pz) {
            if (this.dir == 0) {
                js = 1;
                je = 1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 1) {
                js = -1;
                je = -1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 2) {
                js = -1;
                je = 1;
                ks = -1;
                ke = -1;
            } else {
                js = -1;
                je = 1;
                ks = 1;
                ke = 1;
            }
            if (this.getX() < 0.0) {
                xoff = 1;
            }
            if (this.getZ() < 0.0) {
                zoff = 1;
            }
            for (int i = 0; i <= this.depth; ++i) {
                int il = this.length / (this.depth - i + 1);
                if (il < 1) {
                    il = 1;
                }
                for (int j = js * il; j <= je * il; ++j) {
                    for (int k = ks * il; k <= ke * il; ++k) {
                        final int ix = (int) this.getX() + k - xoff;
                        final int iz = (int) this.getZ() + j - zoff;
                        final int posY = Mth.floor(this.getY());
                        if (i == this.depth) {
                            BlockState bid = this.getBlock(ix, posY + i + 1, iz);
                            if (bid.is(ModBlocks.FLOWER_PINK.get()) || bid.is(ModBlocks.FLOWER_BLUE.get())
                                    || bid.is(ModBlocks.FLOWER_BLACK.get()) || bid.is(ModBlocks.FLOWER_SCARY.get())) {
                                this.FastSetBlock(ix, posY + i + 1, iz, Blocks.AIR);
                            }
                            // Blocks.water || Blocks.flowing_water: one block with a fluid level in 1.21.1.
                            if (bid.is(Blocks.WATER)) {
                                // The original clears Y+i here, not the Y+i+1 it tested (IslandToo.java:353-355).
                                this.setBlock(ix, posY + i, iz, Blocks.AIR);
                            }
                            if (bid.is(ModBlocks.SKY_TREE_LOG.get())) {
                                this.setBlock(ix, posY + i + 1, iz, Blocks.AIR);
                                bid = this.getBlock(ix, posY + i + 2, iz);
                                if (bid.is(ModBlocks.SKY_TREE_LOG.get())) {
                                    this.setBlock(ix, posY + i + 2, iz, Blocks.AIR);
                                    bid = this.getBlock(ix, posY + i + 3, iz);
                                    if (bid.is(ModBlocks.SKY_TREE_LOG.get())) {
                                        this.setBlock(ix, posY + i + 3, iz, Blocks.AIR);
                                    }
                                }
                            }
                            bid = this.getBlock(ix, posY + i, iz);
                            if (bid.is(Blocks.WATER)) {
                                this.setBlock(ix, posY + i, iz, Blocks.AIR);
                            }
                        }
                        this.FastSetBlock(ix, posY + i, iz, Blocks.AIR);
                    }
                }
            }
            this.mySetBlock((int) this.getX() - xoff, Mth.floor(this.getY()), (int) this.getZ() - zoff);
            double newX = mx;
            if (this.myX < 0.0) {
                newX -= 0.5;
            } else {
                newX += 0.5;
            }
            double newZ = mz;
            if (this.myZ < 0.0) {
                newZ -= 0.5;
            } else {
                newZ += 0.5;
            }
            this.setPos(newX, this.getY(), newZ);
            if (this.dir == 0) {
                js = -1;
                je = -1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 1) {
                js = 1;
                je = 1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 2) {
                js = -1;
                je = 1;
                ks = 1;
                ke = 1;
            } else {
                js = -1;
                je = 1;
                ks = -1;
                ke = -1;
            }
            zoff = (xoff = 0);
            if (this.getX() < 0.0) {
                xoff = 1;
            }
            if (this.getZ() < 0.0) {
                zoff = 1;
            }
            this.setBlock((int) this.getX() - xoff, Mth.floor(this.getY()), (int) this.getZ() - zoff, Blocks.AIR);
            for (int i = 0; i <= this.depth; ++i) {
                int il = this.length / (this.depth - i + 1);
                if (il < 1) {
                    il = 1;
                }
                for (int j = js * il; j <= je * il; ++j) {
                    for (int k = ks * il; k <= ke * il; ++k) {
                        final int ix = (int) this.getX() + k - xoff;
                        final int iz = (int) this.getZ() + j - zoff;
                        final int posY = Mth.floor(this.getY());
                        if (i == this.depth) {
                            final BlockState bid;
                            if ((bid = this.getBlock(ix, posY + i, iz)).isAir()) {
                                if (world.random.nextInt(5000) == 1) {
                                    this.setBlock(ix, posY + i, iz, Blocks.WATER);
                                } else {
                                    this.FastSetBlock(ix, posY + i, iz, Blocks.GRASS_BLOCK);
                                    if (world.random.nextInt(30) == 1) {
                                        if (this.getBlock(ix, posY + i + 1, iz).isAir()) {
                                            if (world.random.nextInt(2) == 1) {
                                                this.setBlock(ix, posY + i + 1, iz, ModBlocks.FLOWER_PINK.get());
                                            } else {
                                                this.setBlock(ix, posY + i + 1, iz, ModBlocks.FLOWER_BLUE.get());
                                            }
                                        }
                                    } else if (world.random.nextInt(100) == 1 && this.getBlock(ix, posY + i + 1, iz).isAir()) {
                                        com.swbr.orespawn.world.tree.Trees.SmallTree(world, ix, posY + i + 1, iz);
                                    }
                                }
                            } else if (bid.is(Blocks.BEDROCK)) {
                                this.discard();
                                return;
                            }
                        } else {
                            final BlockState bid = this.getBlock(ix, posY + i, iz);
                            if (bid.is(Blocks.END_STONE)) {
                                if (!world.isClientSide) {
                                    // createExplosion(this, x, y, z, 5.0f, true): TNT interaction, no mobGriefing check.
                                    world.explode(this, ix, this.getY() + i, iz, 5.0f, Level.ExplosionInteraction.TNT);
                                }
                            } else {
                                this.mySetBlock(ix, posY + i, iz);
                            }
                        }
                    }
                }
            }
            this.setBlock((int) this.getX() - xoff, Mth.floor(this.getY()), (int) this.getZ() - zoff, Blocks.AIR);
        }
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems(recentlyHit, looting)}, then {@code dropEquipment}. */
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

    /** The inherited 1.7.10 {@code EntityLiving.dropFewItems}. */
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

    /** {@code getDropItem} (IslandToo.java:478-480): the Island Block. */
    @Nullable
    protected Item getDropItem() {
        return ModBlocks.ISLAND.get().asItem();
    }

    /** {@code FastSetBlock} (IslandToo.java:482-484): {@code setBlockFast(world, x, y, z, id, 0, 3)}. */
    public void FastSetBlock(final int ix, final int iy, final int iz, final Block id) {
        FastBlocks.setBlockFast(this.level(), ix, iy, iz, id.defaultBlockState(), 3);
    }

    /** 1.7.10 {@code World.setBlock(x, y, z, block)}: flags 3. */
    private void setBlock(final int x, final int y, final int z, final Block block) {
        this.level().setBlock(new BlockPos(x, y, z), block.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** 1.7.10 {@code World.getBlock}; {@code == Blocks.air} is {@link BlockState#isAir()} (air, cave air, void air). */
    private BlockState getBlock(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    // ---- read access for GameTests; the original had no accessors.

    public int islandWidth() {
        return this.width;
    }

    public int islandDepth() {
        return this.depth;
    }

    public int islandDir() {
        return this.dir;
    }

    public float islandSpeed() {
        return this.speed;
    }

    public int islandBlocktype() {
        return this.blocktype;
    }

    /** For GameTests: fix direction (0-3) and speed after the first build. */
    public void setIslandMotion(final int dir, final float speed) {
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
