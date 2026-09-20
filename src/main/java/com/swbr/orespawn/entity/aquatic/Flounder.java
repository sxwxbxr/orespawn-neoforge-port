package com.swbr.orespawn.entity.aquatic;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
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
import net.minecraft.world.entity.player.Player;
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
 * Port of {@code danger.orespawn.Flounder} (Flounder.java:13-260), id {@code flounder}: a shy flatfish that avoids
 * players, crawls back to water on land and slowly dies without it. Bred with crystal apples.
 *
 * <p>Not carried over: {@code experienceValue = 5} (:31, ignored by {@code EntityAnimal.getExperiencePoints}, 1-3 from
 * {@code Animal}); {@code getDropItem} (:87-89, unused); {@code isWheat} (:253-255, no caller); the empty
 * {@code entityInit}. {@code canBreatheUnderwater} (:63-65) is the tag {@code minecraft:can_breathe_under_water}.
 */
public class Flounder extends Animal {

    private float moveSpeed = 0.25f;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    /**
     * Constructor (:21-40). {@code setSize(0.55f, 0.25f)} is the entity type's size, {@code fireResistance = 15} is
     * {@link #getFireImmuneTicks()}. The tasks are {@link #registerGoals()}.
     */
    public Flounder(final EntityType<? extends Flounder> type, final Level level) {
        super(type, level);
        this.moveSpeed = 0.25f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // PORT: getNavigator().setAvoidsWater(false) (:32) is a water path malus of 0 (vanilla default 8).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    /** {@code applyEntityAttributes} (:42-48): health 5, speed {@code moveSpeed} 0.25, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /**
     * The tasks of the constructor (:33-39), same priorities.
     *
     * <p>Avoid and panic are the 1.7.10 goals (W07 catch-up of the W06 fix wave): {@link EntityAIAvoidEntity} takes the
     * closest player, creative ones included, without a sight test, as {@code tw} did; {@link LegacyPanic#legacyPanic}
     * starts on a revenge target or burning and runs to a random spot 5 wide and 4 high, never to water ({@code uz.a},
     * disassembled).
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new EntityAIAvoidEntity(this, Player.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Player.class, 12.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(7, new EntityAILookIdle(this));
    }

    /** {@code fireResistance = 15} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 15;
    }

    /** {@code onUpdate} (:54-57): the speed attribute is set again before every tick. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:67-69). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code getLivingSound} (:71-73): {@code "splash"} without namespace - silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:75-77): {@code "little_splat"} without the {@code orespawn:} prefix - silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:79-81). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RATDEAD.get();
    }

    /** {@code getSoundVolume} (:83-85). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
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

    /** {@code dropFewItems} (:91-98): 1-2 raw fish ({@code Items.fish} meta 0 = cod) at the feet. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        var3 = this.random.nextInt(2);
        ++var3;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.COD, 1));
        }
    }

    /** {@code Blocks.water || Blocks.flowing_water}: one block in 1.21.1; waterlogged blocks do not count. */
    private boolean isWater(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /** {@code scan_it} (:100-181), the same shell scan as the whale's. */
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
     * {@code heal(-1.0f)} as 1.7.10 applied it: {@code setHealth(health + f)} while alive. NeoForge's {@code heal}
     * drops amounts {@code <= 0}, so this goes straight to {@code setHealth} (R18).
     */
    private void legacyHeal(final float f) {
        final float f1 = this.getHealth();
        if (f1 > 0.0f) {
            this.setHealth(f1 + f);
        }
    }

    /**
     * {@code updateAITick} (:183-226). After the animal's own step: 1/200 forget the attacker; on land with 1/20 scan
     * for water and walk there, or, with none in reach, lose 1 health with 1/25 and vanish without drops at 0; in
     * water with 1/50 heal 1. R20: the scan origin is floored.
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
                    this.legacyHeal(-1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.isInWater() && this.level().random.nextInt(50) == 0) {
            // playSound("splash", 1.0f, rand * 0.2f + 0.9f): no namespace, silent (R18); the pitch still drew.
            this.level().random.nextFloat();
            this.heal(1.0f);
        }
    }

    /** {@code findBuddies} (:228-231) around a spawn position, before the entity exists. */
    private static int findBuddies(final EntityType<?> type, final LevelAccessor level, final BlockPos pos) {
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                .inflate(16.0, 8.0, 16.0);
        return level.getEntitiesOfClass(Flounder.class, box).size();
    }

    /**
     * {@code getCanSpawnHere} (:233-235) as the placement predicate: Y 50 or higher, daytime, 1/20 on the world random,
     * at most ten flounders within 16/8/16. The chance is rolled here only; the instance checks answer {@code true},
     * because the override dropped the {@code EntityLiving} collision and liquid test.
     */
    public static boolean checkFlounderSpawnRules(final EntityType<Flounder> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel()) && random.nextInt(20) == 1
                && findBuddies(type, level, pos) <= 10;
    }

    /** See {@link #checkFlounderSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** See {@link #checkFlounderSpawnRules}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code canDespawn} (:237-243): 1.7.10 asked it before the distance test whenever any player existed, making a
     * baby persistent right away; 1.21.1 asks only beyond the despawn distance, so the side effect runs here first.
     */
    @Override
    public void checkDespawn() {
        if (!this.isPersistenceRequired() && !this.requiresCustomPersistence() && this.isBaby()
                && this.level().getNearestPlayer(this, -1.0) != null) {
            this.setPersistenceRequired();
        }
        super.checkDespawn();
    }

    /** {@code canDespawn} (:237-243). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return true;
    }

    /** {@code createChild} (:245-247). */
    @Nullable
    @Override
    public Flounder getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:249-251). */
    @Nullable
    public Flounder spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.FLOUNDER.get().create(this.level());
    }

    /** {@code isBreedingItem} (:257-259). */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /**
     * No {@code interact} in Flounder.java: 1.7.10 {@code EntityAnimal.interact} put an adult of age 0 in love and fed
     * no young fish. 1.21.1 {@code Animal.mobInteract} would use up a Crystal Apple on a young one and age it up (see
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
