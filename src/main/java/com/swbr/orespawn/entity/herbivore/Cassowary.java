package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
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
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Cassowary} (Cassowary.java:12-126): {@code cassowary}, a shy flightless bird that
 * flees monsters and players ("Cassowary", OreSpawnMain.java:3751, tracking 64/1/false). Breeds with the Crystal
 * Apple, cannot be tamed.
 *
 * <p>Values: health 10 (:59-61), speed 0.25 re-set every tick (:18, :46-49), attack 8 registered but used by no
 * goal (:38-39), hitbox 0.5 x 1.2 (:19), {@code fireResistance} 100 (:21). {@code experienceValue = 5} (:22) is
 * never read ({@code Animal.getBaseExperienceReward}, W04). No DataWatcher entries, no NBT of its own.
 */
public class Cassowary extends Animal {

    private float moveSpeed;

    /** {@code Cassowary(World)} (:16-32). */
    public Cassowary(final EntityType<? extends Cassowary> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.25f;
        // setSize(0.5f, 1.2f) (:19) is the entity type's size (R9).
        this.moveSpeed = 0.25f;
        // fireResistance = 100 (:21) is getFireImmuneTicks(); experienceValue = 5 (:22) is dead.
        // PORT: getNavigator().setAvoidsWater(true) (:23) - a water path malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(3, new EntityAIAvoidEntity(this, Player.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Mob.class, 12.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(7, new EntityAILookIdle(this));
    }

    /** {@code applyEntityAttributes} (:34-40). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    /** {@code fireResistance = 100} (:21). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:46-49), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:59-61). */
    public int mygetMaxHealth() {
        return 10;
    }

    /** {@code getLivingSound} (:63-65). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:67-69). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:71-73). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getSoundVolume} (:75-77). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code dropFewItems} first, then the equipment roll; {@code getDropItem} (:79-81) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:83-90): 2..4 raw chicken, looting ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        var3 = this.random.nextInt(3);
        var3 += 2;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.CHICKEN, 1));
        }
    }

    /** {@code updateAITick} (:92-97): forget the revenge target 1 in 200, before the inherited step. */
    @Override
    protected void customServerAiStep() {
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:99-101), day and nothing else - no light, grass or free-space
     * test.
     */
    public static boolean checkCassowarySpawnRules(final EntityType<Cassowary> type, final ServerLevelAccessor level,
                                                   final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return HerbivoreSupport.isDaytime(level.getLevel());
    }

    /** {@code getCanSpawnHere} (:99-101) on the positioned instance. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return HerbivoreSupport.isDaytime(this.level());
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code canDespawn} (:103-109): babies become persistent, adults despawn unless persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired();
    }

    /** {@code createChild} (:111-113). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:115-117). */
    public Cassowary spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.CASSOWARY.get().create(this.level());
    }

    /** {@code isWheat} (:119-121): dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:123-125). */
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
