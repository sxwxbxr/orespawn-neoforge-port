package com.swbr.orespawn.entity.aquatic;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.Whale} (Whale.java:14-304), id {@code whale}: a peaceful whale that blows a spout,
 * heals in water, crawls back to water on land and dries out without it. Bred with crystal apples.
 *
 * <p>Not carried over: {@code experienceValue = 40} (:36, ignored by {@code EntityAnimal.getExperiencePoints}, which
 * gives 1-3 - inherited from {@code Animal}); {@code getDropItem} (:126-128, unused because {@code dropFewItems} is
 * overridden); {@code isWheat} (:297-299, no caller); the empty {@code entityInit} (:55-57).
 * {@code canBreatheUnderwater} (:98-100) is the tag {@code minecraft:can_breathe_under_water}.
 */
public class Whale extends Animal {

    private float moveSpeed = 0.35f;
    private int spray = 0;
    private int spray_timer = 0;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    /**
     * Constructor (:24-45). {@code setSize(1.5f, 2.5f)} is the entity type's size, {@code fireResistance = 100} is
     * {@link #getFireImmuneTicks()}. The tasks are {@link #registerGoals()}.
     */
    public Whale(final EntityType<? extends Whale> type, final Level level) {
        super(type, level);
        this.moveSpeed = 0.35f;
        this.spray = 0;
        this.spray_timer = 0;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // PORT: getNavigator().setAvoidsWater(false) (:37) is a water path malus of 0 (vanilla default 8), as for
        // Girlfriend (W04).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    /** {@code applyEntityAttributes} (:47-53): health 100, speed {@code moveSpeed} 0.35, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3499999940395355)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /**
     * The tasks of the constructor (:38-44), same priorities.
     *
     * <p>Panic is the 1.7.10 {@code EntityAIPanic} ({@link LegacyPanic#legacyPanic}): it runs while a revenge
     * target is set or the whale burns, so the 1/200 revenge reset of {@link #customServerAiStep} still ends a panic,
     * and it runs to a random spot, never to water (W07 catch-up: the former inline goal kept 1.21.1's water search).
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        // PORT: Items.fish matched every meta (raw cod, salmon, clownfish, pufferfish) - four items in 1.21.1.
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.2000000476837158,
                stack -> stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH),
                false));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Player.class, 12.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(7, new EntityAILookIdle(this));
    }

    /** {@code fireResistance = 100} (:35). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /**
     * {@code onUpdate} (:59-92). The spout: a pause of 250-499 ticks, then 25-49 ticks with ten bubbles and ten
     * splashes per tick in a cone above the head. Both sides run the timer, only the client counts {@code spray}
     * down, so on the server it stays above 0 after the first spout and its timer stops - original behaviour, kept.
     * Then a heal of 1 with 1/200, on both sides like the original.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.spray == 0) {
            if (this.spray_timer > 0) {
                --this.spray_timer;
            }
            if (this.spray_timer == 0) {
                this.spray_timer = 250 + this.level().random.nextInt(250);
                this.spray = 25 + this.level().random.nextInt(25);
            }
        }
        if (this.level().isClientSide && this.spray > 0) {
            for (int i = 0; i < 20; ++i) {
                double d = this.level().random.nextDouble() * 0.75;
                d *= d;
                double dir = this.level().random.nextDouble() * 2.0 * 3.141592653589793;
                dir -= 3.141592653589793;
                final double dx = Math.cos(dir) * d / 2.0;
                final double dz = Math.sin(dir) * d / 2.0;
                dir += 1.5707963267948966;
                if (i < 10) {
                    this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + dx, this.getY() + 1.0 + d, this.getZ() + dz,
                            Math.cos(dir) * this.level().random.nextFloat() / 4.0, (double) (this.level().random.nextFloat() * 2.0f),
                            Math.sin(dir) * this.level().random.nextFloat() / 4.0);
                } else {
                    this.level().addParticle(ParticleTypes.SPLASH, this.getX() + dx, this.getY() + 1.0 + d, this.getZ() + dz,
                            Math.cos(dir) * this.level().random.nextFloat() / 4.0, (double) (this.level().random.nextFloat() * 2.0f),
                            Math.sin(dir) * this.level().random.nextFloat() / 4.0);
                }
            }
            --this.spray;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.heal(1.0f);
        }
    }

    /** {@code mygetMaxHealth} (:102-104). */
    public int mygetMaxHealth() {
        return 100;
    }

    /** {@code getLivingSound} (:106-108): {@code "splash"} without namespace - silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:110-112). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LITTLE_SPLAT.get();
    }

    /** {@code getDeathSound} (:114-116). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BIG_SPLAT.get();
    }

    /** {@code getSoundVolume} (:118-120). */
    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    /** {@code getSoundPitch} (:122-124). */
    @Override
    public float getVoicePitch() {
        return 0.5f;
    }

    /**
     * {@code dropItemRand} (:130-133): a fresh stack up to three blocks off on the shared {@code OreSpawnRand}, one
     * block up, added straight to the level (no pickup delay, not part of the captured drops).
     */
    private void dropItemRand(final Item index, final int par1) {
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems}, then {@code dropEquipment} ({@code Mob.dropCustomDeathLoot}).
     * The override below ignores both arguments, so looting is not looked up.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:135-142): 20-44 raw fish ({@code Items.fish} meta 0 = cod), one per stack. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        var3 = this.random.nextInt(25);
        var3 += 20;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.dropItemRand(Items.COD, 1);
        }
    }

    /**
     * {@code Blocks.water || Blocks.flowing_water}: one block in 1.21.1. A waterlogged block is not a water block, as
     * 1.7.10 had none.
     */
    private boolean isWater(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /**
     * {@code scan_it} (:144-225): the six faces of a box with half sizes {@code dx, dy, dz} around {@code (x, y, z)};
     * every water block closer than the best so far becomes the target.
     */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isWater(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWater(x - dx, y + i, z + j)) {
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
                if (this.isWater(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWater(x + i, y - dy, z + j)) {
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
                if (this.isWater(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isWater(x + i, y + j, z - dz)) {
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
     * {@code heal(f)} with a negative amount, as 1.7.10 applied it: {@code setHealth(health + f)} while alive.
     * NeoForge's {@code heal} drops amounts {@code <= 0} (LivingEntity.java:1117-1119), so this goes straight to
     * {@code setHealth} (R18, same ruling as the Hydrolisc).
     */
    private void legacyHeal(final float f) {
        final float f1 = this.getHealth();
        if (f1 > 0.0f) {
            this.setHealth(f1 + f);
        }
    }

    /**
     * {@code updateAITick} (:227-270). After the animal's own step: 1/200 forget the attacker; on land with 1/20 scan
     * shells 1, 2, 3, 4, 5, 7, 9 for water and walk there, or, with no water in reach, lose 4 health with 1/25 and
     * vanish without drops at 0; in water with 1/50 heal 1.
     *
     * <p>R20: the {@code (int)} casts of the scan origin are {@code Mth.floor}.
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (!this.isInWater() && this.level().random.nextInt(20) == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 11; ++i) {
                int j = i;
                if (j > 4) {
                    j = 4;
                }
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) (this.ty - 1), (double) this.tz, 1.0);
            } else {
                if (this.level().random.nextInt(25) == 1) {
                    this.legacyHeal(-4.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.isInWater() && this.level().random.nextInt(50) == 0) {
            // playSound("splash", 1.0f, rand * 0.2f + 0.9f): no namespace, silent (R18). The pitch argument still
            // drew from the world random.
            this.level().random.nextFloat();
            this.heal(1.0f);
        }
    }

    /** {@code findBuddies} (:272-275) around a spawn position, before the entity exists. */
    private static int findBuddies(final EntityType<?> type, final LevelAccessor level, final BlockPos pos) {
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                .inflate(32.0, 8.0, 32.0);
        return level.getEntitiesOfClass(Whale.class, box).size();
    }

    /**
     * {@code getCanSpawnHere} (:277-279) as the placement predicate: Y 50 or higher, daytime, 1/50 on the world random,
     * no other whale within 32/8/32. 1.7.10 asked it once per attempt with the mob placed at the block centre; the
     * chance roll lives here only, so it is not rolled twice. The instance checks below answer {@code true} because
     * the override dropped the {@code EntityLiving} collision and liquid test - a whale spawns in water.
     */
    public static boolean checkWhaleSpawnRules(final EntityType<Whale> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel()) && random.nextInt(50) == 1
                && findBuddies(type, level, pos) <= 0;
    }

    /** See {@link #checkWhaleSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** See {@link #checkWhaleSpawnRules}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code canDespawn} (:281-287) runs in 1.7.10 {@code despawnEntity} whenever the whale is not persistent and any
     * player exists - before the distance test. A calf becomes persistent right there. 1.21.1 asks
     * {@link #removeWhenFarAway} only beyond the despawn distance, so the side effect is made here with the same
     * condition first.
     */
    @Override
    public void checkDespawn() {
        if (!this.isPersistenceRequired() && !this.requiresCustomPersistence() && this.isBaby()
                && this.level().getNearestPlayer(this, -1.0) != null) {
            this.setPersistenceRequired();
        }
        super.checkDespawn();
    }

    /** {@code canDespawn} (:281-287). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return true;
    }

    /** {@code createChild} (:289-291). */
    @Nullable
    @Override
    public Whale getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:293-295). */
    @Nullable
    public Whale spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.WHALE.get().create(this.level());
    }

    /** {@code isBreedingItem} (:301-303). */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /**
     * No {@code interact} in Whale.java: 1.7.10 {@code EntityAnimal.interact} put an adult of age 0 in love and fed no
     * calf. 1.21.1 {@code Animal.mobInteract} would use up a Crystal Apple on a calf and age it up (see
     * {@link HerbivoreSupport#legacyAnimalInteract}).
     */
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
