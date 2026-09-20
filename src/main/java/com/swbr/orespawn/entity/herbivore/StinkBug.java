package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.StinkBug} (StinkBug.java:16-159): {@code stink_bug}, a small passive bug whose
 * death gives everything around it Nausea ("Stink Bug", OreSpawnMain.java:3623, tracking 32/1/false). Breeds with the
 * Crystal Apple.
 *
 * <p>Values: health 5 (:93-95), speed 0.15 re-set every tick (:22, :49-52), attack 0 (:42), hitbox 0.55 (:23),
 * {@code fireResistance} 10 (:24). {@code experienceValue = 2} (:26) is never read
 * ({@code Animal.getBaseExperienceReward}, W04). No DataWatcher entries, no NBT of its own. Drops: {@code getDropItem}
 * {@code deadstinkbug} (:113-115) through the default 1.7.10 {@code dropFewItems}.
 */
public class StinkBug extends Animal {

    /** The registry id of this type, which is what a mob spawner stores (1.7.10: the name {@code "Stink Bug"}). */
    private static final String SPAWNER_ID = OreSpawn.MOD_ID + ":stink_bug";

    private float moveSpeed;

    /** {@code StinkBug(World)} (:20-35). */
    public StinkBug(final EntityType<? extends StinkBug> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.15f;
        // setSize(0.55f, 0.55f) (:23) is the entity type's size (R9); fireResistance = 10 (:24) is getFireImmuneTicks().
        // PORT: getNavigator().setAvoidsWater(true) (:25) - a water path malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        // experienceValue = 2 (:26) is dead.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(5, new EntityAIAvoidEntity(this, Player.class, 4.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(9, new EntityAILookIdle(this));
        // :34; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(10, new EntityAIMoveIndoors(this));
    }

    /** {@code applyEntityAttributes} (:37-43). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.15f)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code fireResistance = 10} (:24). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /** {@code onUpdate} (:49-52), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code updateAITick} (:54-62): forget the revenge target 1 in 200, before the inherited step. */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
    }

    // isAIEnabled (:64-66): every 1.21.1 mob runs goals.

    /**
     * {@code attackEntityFrom} (:68-87): after the damage, while the bug's health is at or below 0, every living
     * entity in x +-8, y -5..+10, z +-8 - players, other mobs and the bug itself - gets Nausea for 300 ticks.
     *
     * <p>Kept 1:1 in {@code hurt}, not moved to {@code die}: {@code isDead} was only set when the corpse was removed,
     * so every further hit during the 20-tick death animation gave the Nausea again, and a client-side hit on a dying
     * bug ran the same code on the client, both exactly as here. {@code isDead} is {@code isRemoved()}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.isRemoved()) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        if (this.getHealth() <= 0.0f || this.isRemoved()) {
            final AABB bb = new AABB(this.getX() - 8.0, this.getY() - 5.0, this.getZ() - 8.0,
                    this.getX() + 8.0, this.getY() + 10.0, this.getZ() + 8.0);
            final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, bb);
            final Iterator<LivingEntity> var6 = var5.iterator();
            LivingEntity var7 = null;
            while (var6.hasNext()) {
                var7 = var6.next();
                if (var7 != null) {
                    var7.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
                }
            }
        }
        return ret;
    }

    // canBreatheUnderwater (:89-91) false is the default.

    /** {@code mygetMaxHealth} (:93-95). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code getLivingSound} (:97-99). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:101-103). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:105-107). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FART.get();
    }

    /** {@code getSoundVolume} (:109-111). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /**
     * {@code getDropItem} (:113-115) through the inherited 1.7.10 {@code EntityLiving.dropFewItems}: 0..2 dead stink
     * bugs plus {@code rand(looting + 1)}; then the equipment roll.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, ModItems.DEAD_STINK_BUG.get(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:117-134) - a spawner of this type in x/z -3..2, y 0..4 always allows
     * the spawn, otherwise Y at least 50; no light or grass test.
     */
    public static boolean checkStinkBugSpawnRules(final EntityType<StinkBug> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (HerbivoreSupport.spawnerNearby(level, pos.getX(), pos.getY(), pos.getZ(), SPAWNER_ID)) {
            return true;
        }
        return pos.getY() >= 50.0;
    }

    /** {@code getCanSpawnHere} (:117-134) on the positioned instance; {@code (int)} casts are {@code Mth.floor} (R20). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (HerbivoreSupport.spawnerNearby(level, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()), SPAWNER_ID)) {
            return true;
        }
        return this.getY() >= 50.0;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code canDespawn} (:136-142): babies become persistent, adults despawn unless persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired();
    }

    /** {@code createChild} (:144-146). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:148-150). */
    public StinkBug spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.STINK_BUG.get().create(this.level());
    }

    /** {@code isWheat} (:152-154): raw fish; dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && HerbivoreSupport.isRawFish(par1ItemStack);
    }

    /** {@code isBreedingItem} (:156-158). */
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
