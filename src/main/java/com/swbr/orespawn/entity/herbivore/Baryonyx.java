package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Baryonyx} (Baryonyx.java:14-255): {@code baryonyx}, a peaceful dinosaur that
 * grazes grass down to dirt ("Baryonyx", OreSpawnMain.java:3479, tracking 64/1/false). Breeds with the Crystal
 * Apple.
 *
 * <p>Values: health 40 (:72-74), speed 0.25 re-set every tick (:24, :59-62), attack 8 registered but used by no
 * goal (:47-48), hitbox 1.5 x 2.8 (:29), {@code fireResistance} 100 (:31). {@code experienceValue = 5} (:32) is
 * never read: {@code EntityAnimal.getExperiencePoints} returns {@code 1 + rand(3)}, which is 1.21.1's inherited
 * {@code Animal.getBaseExperienceReward} (W04 lesson). No DataWatcher entries beyond {@code EntityAgeable}, no NBT
 * of its own. Drops are Java (R10), the entity loot table stays empty.
 *
 * <p>Goals are added in the constructor on both sides, like the 1.7.10 constructor did (W04/W05 precedent).
 */
public class Baryonyx extends Animal {

    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Baryonyx(World)} (:22-41). */
    public Baryonyx(final EntityType<? extends Baryonyx> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.25f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // setSize(1.5f, 2.8f) (:29) is the entity type's size (R9).
        this.moveSpeed = 0.25f;
        // fireResistance = 100 (:31) is getFireImmuneTicks(); experienceValue = 5 (:32) is dead (class Javadoc).
        // PORT: getNavigator().setAvoidsWater(true) (:33) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        // EntityMob.class is Monster.class.
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Player.class, 12.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(7, new EntityAILookIdle(this));
    }

    /**
     * {@code applyEntityAttributes} (:43-49): {@code mygetMaxHealth()}, {@code moveSpeed}, attack 8. In 1.7.10 this
     * ran inside the {@code EntityLivingBase} constructor before {@code moveSpeed} was assigned, so the speed base
     * was 0 until the first {@code onUpdate}; nothing moves before that tick, the supplier carries the tick's value.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    /** {@code fireResistance = 100} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /**
     * Spawn predicate for {@code RegisterSpawnPlacementsEvent}: {@code getCanSpawnHere} (:55-57) - Y at least 50,
     * day, at most 8 Baryonyx in {@code expand(20, 10, 20)} of the spawn box. 1.21.1 asks it before the entity
     * exists; the box is the type's box at the block centre where 1.7.10 placed the mob. The mob was not yet in
     * the world when 1.7.10 asked either, so it never counted itself.
     *
     * <p>PORT: the entity list comes from the accessor, which during chunk generation is a
     * {@code WorldGenRegion} that lists no entities; reading the server level's entity lists from a worldgen
     * thread is not safe.
     */
    public static boolean checkBaryonyxSpawnRules(final EntityType<Baryonyx> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && HerbivoreSupport.isDaytime(level.getLevel())
                && level.getEntitiesOfClass(Baryonyx.class,
                        type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 10.0, 20.0)).size() <= 8;
    }

    /** {@code getCanSpawnHere} (:55-57) on the positioned instance, which natural spawning and spawners also ask. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0 && HerbivoreSupport.isDaytime(this.level()) && this.findBuddies(level) <= 8;
    }

    /**
     * The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test, which
     * 1.21.1 asks separately.
     */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code onUpdate} (:59-62), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    // isAIEnabled (:64-66): every 1.21.1 mob runs goals. canBreatheUnderwater (:68-70) false is the default.

    /** {@code mygetMaxHealth} (:72-74). */
    public int mygetMaxHealth() {
        return 40;
    }

    /** {@code getLivingSound} (:76-78). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:80-82). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:84-86). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getSoundVolume} (:88-90). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems} first, then the equipment roll ({@code Mob.dropCustomDeathLoot}).
     * {@code getDropItem} (:92-94) is unused because {@code dropFewItems} is overridden. Babies drop nothing in
     * both versions ({@code shouldDropLoot}).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:96-103): 2..6 raw beef, looting ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        var3 = this.random.nextInt(5);
        var3 += 2;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.BEEF, 1));
        }
    }

    /** {@code scan_it} (:105-186): one shell of the search cube, nearest grass block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isGrassAt(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isGrassAt(x - dx, y + i, z + j)) {
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
                if (this.isGrassAt(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isGrassAt(x + i, y - dy, z + j)) {
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
                if (this.isGrassAt(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isGrassAt(x + i, y + j, z - dz)) {
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

    /** {@code getBlock(...) == Blocks.grass}: the grass block. */
    private boolean isGrassAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.GRASS_BLOCK);
    }

    /**
     * {@code updateAITick} (:188-225): forget the revenge target 1 in 200; graze 1 in 60 while {@code PlayNicely}
     * is 0 - search shells of radius 1..10 around y+1 (height capped at 2, every second radius skipped from 6),
     * walk to the nearest grass block, and within squared distance 12 turn it into dirt (only with mob griefing)
     * and heal 1 with a burp either way.
     *
     * <p>PORT: the {@code mobGriefing} gamerule string is {@link EventHooks#canEntityGrief}, which reads the same
     * rule unless a mod answers the NeoForge event. {@code (int) posX/posY/posZ} is {@code Mth.floor} (R20).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (this.level().random.nextInt(60) == 0 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            // :198-201: final boolean tx = false; tz = ty = tx = 0.
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
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                if (this.closest < 12) {
                    if (EventHooks.canEntityGrief(this.level(), this)) {
                        this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.DIRT.defaultBlockState(),
                                HerbivoreSupport.LEGACY_FLAG_2);
                    }
                    this.heal(1.0f);
                    this.playSound(SoundEvents.PLAYER_BURP, 1.0f, this.level().random.nextFloat() * 0.2f + 0.9f);
                }
            }
        }
    }

    /**
     * {@code canDespawn} (:227-233): a baby becomes persistent ({@code func_110163_bv} = {@code enablePersistence})
     * and never despawns, an adult unless persistent. 1.21.1's {@code Animal} answers {@code false}.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired();
    }

    /** {@code createChild} (:235-237). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:239-241). */
    public Baryonyx spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.BARYONYX.get().create(this.level());
    }

    /** {@code isWheat} (:243-245): no caller and no vanilla override in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:247-249). */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /** 1.7.10 {@code EntityAnimal.interact}: breeding only, no baby feeding (see {@link HerbivoreSupport#legacyAnimalInteract}). */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /** {@code findBuddies} (:251-254). */
    private int findBuddies(final LevelAccessor level) {
        return level.getEntitiesOfClass(Baryonyx.class, this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
