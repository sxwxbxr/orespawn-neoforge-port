package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Beaver} (Beaver.java:15-316): {@code beaver}, a peaceful animal that fells trees
 * and wooden structures recursively ("Beaver", OreSpawnMain.java:3805, tracking 64/1/false). Flees monsters and
 * players, breeds with the Crystal Apple, never despawns.
 *
 * <p>Values: health 15 (:254-256); speed 0.15 in the field initialiser, overwritten with 0.2 in the constructor and
 * re-set every tick (:26, :33, :60-63); attack 1 registered but never used (:52-53); hitbox 0.6 x 0.8 (:32);
 * {@code fireResistance} 100 (:34); breathes under water (:250-252, {@code #minecraft:can_breathe_under_water}).
 * {@code experienceValue = 5} (:36) is never read ({@code Animal.getBaseExperienceReward}, W04). No DataWatcher
 * entries, no NBT of its own. Drops: {@code getDropItem} porkchop (:278-280) through the default 1.7.10
 * {@code dropFewItems}.
 */
public class Beaver extends Animal {

    private float moveSpeed;
    private GenericTargetSorter TargetSorter;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Beaver(World)} (:24-46). */
    public Beaver(final EntityType<? extends Beaver> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.15f;
        this.TargetSorter = null;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // setSize(0.6f, 0.8f) (:32) is the entity type's size (R9).
        this.moveSpeed = 0.2f;
        // fireResistance = 100 (:34) is getFireImmuneTicks().
        // PORT: getNavigator().setAvoidsWater(false) (:35) - the 1.21.1 counterpart is a water path malus of 0
        // (vanilla default 8), the same translation as MyEntityAIFollowOwner and Girlfriend.
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        // experienceValue = 5 (:36) is dead.
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.5));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(5, new EntityAIAvoidEntity(this, Player.class, 8.0f, 1.0, 1.5));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(8, new EntityAILookIdle(this));
    }

    /** {@code applyEntityAttributes} (:48-54); speed as the first tick sets it. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.2f)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    /** {@code fireResistance = 100} (:34). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:60-63), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /**
     * {@code isWood} (:65-67): {@code Blocks.log}, {@code MyDT} ({@code duplicatortreelog}), {@code MySkyTreeLog},
     * {@code Blocks.fence}, {@code fence_gate}, {@code standing_sign}, compared by block. PORT: R22 - {@code log},
     * {@code fence}, {@code fence_gate} and {@code standing_sign} were the 1.7.10 categories "log", "wooden fence",
     * "fence gate" and "sign", so the port reads {@code #minecraft:logs_that_burn} (every overworld log and wood,
     * stripped included), {@code #minecraft:wooden_fences}, {@code #minecraft:fence_gates} and
     * {@code #minecraft:standing_signs}. The two OreSpawn logs are named blocks and stay single.
     */
    public boolean isWood(final BlockState bid) {
        return bid.is(BlockTags.LOGS_THAT_BURN)
                || bid.is(ModBlocks.DUPLICATOR_TREE_LOG.get()) || bid.is(ModBlocks.SKY_TREE_LOG.get())
                || (bid.is(BlockTags.WOODEN_FENCES) || bid.is(BlockTags.FENCE_GATES) || bid.is(BlockTags.STANDING_SIGNS));
    }

    /**
     * {@code Item.getItemFromBlock(block)} with metadata 0 (:176): every vanilla log and wood block drops an
     * <em>oak</em> log, fence and gate their own item, the two OreSpawn logs their own block item.
     *
     * <p>PORT (R18 case 1): {@code standing_sign} had no item, {@code getItemFromBlock} returned {@code null} and
     * the original spawned an {@code EntityItem} holding a stack without an item; picking that up dereferences the
     * missing item. The port spawns nothing for a sign.
     *
     * <p>PORT: R22 - the categories follow {@link #isWood}: every standing sign spawns nothing, every wooden fence
     * and fence gate drops its own item (in 1.7.10 the one oak fence dropped itself), every log and wood block
     * drops the meta-0 oak log as before.
     */
    @Nullable
    private static Item legacyItemFromBlock(final BlockState var11) {
        if (var11.is(BlockTags.STANDING_SIGNS)) {
            return null;
        }
        if (var11.is(BlockTags.WOODEN_FENCES) || var11.is(BlockTags.FENCE_GATES)
                || var11.is(ModBlocks.DUPLICATOR_TREE_LOG.get()) || var11.is(ModBlocks.SKY_TREE_LOG.get())) {
            return var11.getBlock().asItem();
        }
        return Items.OAK_LOG;
    }

    /**
     * Which of the {@link #isWood} blocks was a 1.7.10 {@code BlockLog} ({@code Blocks.log}), whose {@code breakBlock}
     * marked the leaves around it for decay ({@link HerbivoreSupport#legacyLogBreakBlock}). Fence, gate, sign and the
     * two OreSpawn logs were not. PORT: R22 - the log category is {@code #minecraft:logs_that_burn}, as in
     * {@link #isWood}.
     */
    private static boolean isBlockLog(final BlockState bid) {
        return bid.is(BlockTags.LOGS_THAT_BURN);
    }

    /** {@code scan_it} (:69-150): one shell of the search cube, nearest wood block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isWood(this.blockAt(x + dx, y + i, z + j))) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWood(this.blockAt(x - dx, y + i, z + j))) {
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
                if (this.isWood(this.blockAt(x + i, y + dy, z + j))) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWood(this.blockAt(x + i, y - dy, z + j))) {
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
                if (this.isWood(this.blockAt(x + i, y + j, z + dz))) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isWood(this.blockAt(x + i, y + j, z - dz))) {
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

    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /**
     * {@code dropItemRand} (:152-160): the shared {@code OreSpawnRand} for x/z, the world's random for the height
     * 4..7 above the beaver.
     */
    private ItemStack dropItemRand(@Nullable final Item index, final int par1) {
        if (index == null) {
            // PORT (R18 case 1): see legacyItemFromBlock.
            return ItemStack.EMPTY;
        }
        ItemEntity var3 = null;
        final ItemStack is = new ItemStack(index, par1);
        var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                this.getY() + 4.0 + this.level().random.nextInt(4),
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4), is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /**
     * {@code breakRecursor} (:162-185): removes every wood block in the 3x3x3 around (x, y, z) that is not the caller
     * and - past the first level - lies outside the caller's own 3x3x3, drops it and recurses, at most 200 levels
     * deep. Kept recursive: every removed block turns into air before the recursion, so the depth is bounded by 200
     * and the work by the number of wood blocks.
     */
    public void breakRecursor(final Level world, final int x, final int y, final int z, final int xf, final int yf,
                              final int zf, final int recursion) {
        final int var7 = 1;
        if (recursion > 200) {
            return;
        }
        for (int var8 = -var7; var8 <= var7; ++var8) {
            for (int var9 = -var7; var9 <= var7; ++var9) {
                for (int var10 = -var7; var10 <= var7; ++var10) {
                    if (var8 != 0 || var9 != 0 || var10 != 0) {
                        if (x + var8 != xf || y + var9 != yf || z + var10 != zf) {
                            if (recursion <= 0 || x + var8 < xf - var7 || x + var8 > xf + var7 || y + var9 < yf - var7
                                    || y + var9 > yf + var7 || z + var10 < zf - var7 || z + var10 > zf + var7) {
                                final BlockPos at = new BlockPos(x + var8, y + var9, z + var10);
                                final BlockState var11 = world.getBlockState(at);
                                if (this.isWood(var11)) {
                                    world.setBlock(at, Blocks.AIR.defaultBlockState(), HerbivoreSupport.LEGACY_FLAG_2);
                                    if (isBlockLog(var11)) {
                                        HerbivoreSupport.legacyLogBreakBlock(world, at);
                                    }
                                    this.dropItemRand(legacyItemFromBlock(var11), 1);
                                    this.breakRecursor(world, x + var8, y + var9, z + var10, x, y, z, recursion + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The griefing branch of {@code updateAITick} (:216-218): the found wood block turns to air with flag 2 and no
     * drop, then {@link #breakRecursor} starts from it. A separate method only so a GameTest can fell a tree without
     * waiting for the 1/350 roll.
     */
    public void fellAt(final int x, final int y, final int z, final int recursion) {
        final BlockPos at = new BlockPos(x, y, z);
        final BlockState felled = this.level().getBlockState(at);
        this.level().setBlock(at, Blocks.AIR.defaultBlockState(), HerbivoreSupport.LEGACY_FLAG_2);
        if (isBlockLog(felled)) {
            HerbivoreSupport.legacyLogBreakBlock(this.level(), at);
        }
        this.breakRecursor(this.level(), x, y, z, x, y, z, recursion);
    }

    /**
     * {@code updateAITick} (:187-232): forget the revenge target 1 in 200; fell wood with (1 in 30 while hurt or
     * 1 in 350) and {@code PlayNicely} 0 - the nearest wood block is removed without a drop and everything wooden
     * connected to it falls through {@link #breakRecursor} (mob griefing only), heal 1 and the chainsaw sound either
     * way; walk to the nearest beaver 1 in 200 (usually itself, it sorts first); then the inherited step.
     *
     * <p>PORT: {@code mobGriefing} is {@link EventHooks#canEntityGrief}; {@code (int)} coordinates are
     * {@code Mth.floor} (R20).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (((this.level().random.nextInt(30) == 0 && this.getBeaverHealth() < this.mygetMaxHealth())
                || this.level().random.nextInt(350) == 1) && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            // :196-199: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 11; ++i) {
                int j = i;
                if (j > 2) {
                    j = 2;
                }
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 6) {
                    ++i;
                }
            }
            final int i = 0;
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                if (this.closest < 12) {
                    if (EventHooks.canEntityGrief(this.level(), this)) {
                        this.fellAt(this.tx, this.ty, this.tz, i);
                    }
                    this.heal(1.0f);
                    this.playSound(ModSounds.CHAINSAW.get(), 1.0f, this.level().random.nextFloat() * 0.2f + 0.9f);
                }
            }
        }
        if (this.level().random.nextInt(200) == 1) {
            final Beaver buddy = this.findBuddy();
            if (buddy != null) {
                this.getNavigation().moveTo(buddy.getX(), buddy.getY(), buddy.getZ(), 0.5);
            }
        }
        super.customServerAiStep();
    }

    /** {@code findBuddy} (:234-244): the first beaver in {@code expand(16, 6, 16)} after sorting, itself included. */
    @Nullable
    private Beaver findBuddy() {
        final List<Beaver> var5 = new ArrayList<>(this.level().getEntitiesOfClass(Beaver.class, this.getBoundingBox().inflate(16.0, 6.0, 16.0)));
        var5.sort(this.TargetSorter);
        final Iterator<Beaver> var6 = var5.iterator();
        Beaver var8;
        if (var6.hasNext()) {
            var8 = var6.next();
            return var8;
        }
        return null;
    }

    // isAIEnabled (:246-248): every 1.21.1 mob runs goals.
    // canBreatheUnderwater (:250-252): final in 1.21.1, read from #minecraft:can_breathe_under_water.

    /** {@code mygetMaxHealth} (:254-256). */
    public int mygetMaxHealth() {
        return 15;
    }

    /** {@code getBeaverHealth} (:258-260). */
    public int getBeaverHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:262-264). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:266-268). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SCORPION_HIT.get();
    }

    /** {@code getDeathSound} (:270-272). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:274-276). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /**
     * {@code getDropItem} (:278-280) through the inherited 1.7.10 {@code EntityLiving.dropFewItems}: 0..2 raw
     * porkchops plus {@code rand(looting + 1)}; then the equipment roll.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, Items.PORKCHOP, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code getSoundPitch} (:282-284). */
    protected float getSoundPitch() {
        return this.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f)
                : ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
    }

    /** 1.21.1 reads the living, hurt and death sound pitch from here. */
    @Override
    public float getVoicePitch() {
        return this.getSoundPitch();
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:286-295) - Y 50..100 and the block below is {@code dirt},
     * {@code grass}, {@code tallgrass} or {@code leaves}; no light or time test. PORT: flattened blocks - {@code dirt}
     * metas 0-2 are dirt, coarse dirt and podzol; {@code tallgrass} short grass and fern. PORT: R22 - {@code leaves}
     * is the category {@code #minecraft:leaves} (acacia, dark oak, mangrove, cherry and azalea leaves included).
     * OreSpawn's own leaves are excluded: the port adds them to that tag ({@code data/minecraft/tags/block/leaves.json}),
     * but they were separate blocks the original never compared with {@code Blocks.leaves} - the same reading as
     * {@code MonsterSupport.isLegacyLeaves}.
     */
    public static boolean checkBeaverSpawnRules(final EntityType<Beaver> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return canSpawnOn(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /** {@code getCanSpawnHere} (:286-295) on the positioned instance; {@code (int)} casts are {@code Mth.floor} (R20). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return canSpawnOn(level, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    private static boolean canSpawnOn(final LevelAccessor level, final int posX, final int posY, final int posZ) {
        if (posY < 50.0) {
            return false;
        }
        if (posY > 100.0) {
            return false;
        }
        final BlockState bid = level.getBlockState(new BlockPos(posX, posY - 1, posZ));
        return bid.is(Blocks.DIRT) || bid.is(Blocks.COARSE_DIRT) || bid.is(Blocks.PODZOL)
                || bid.is(Blocks.GRASS_BLOCK)
                || bid.is(Blocks.SHORT_GRASS) || bid.is(Blocks.FERN)
                || (bid.is(BlockTags.LEAVES)
                        && !OreSpawn.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(bid.getBlock()).getNamespace()));
    }

    /** {@code canDespawn} (:297-299). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code createChild} (:301-303). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:305-307). */
    public Beaver spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.BEAVER.get().create(this.level());
    }

    /** {@code isWheat} (:309-311): dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:313-315). */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /** 1.7.10 {@code EntityAnimal.interact}: breeding only, no baby feeding. */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
