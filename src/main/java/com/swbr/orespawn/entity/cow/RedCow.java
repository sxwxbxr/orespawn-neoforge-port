package com.swbr.orespawn.entity.cow;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWander;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.RedCow} (RedCow.java:8-39), id {@code apple_cow}: a vanilla cow that also drops
 * apples, forgets its attacker now and then and never despawns. Base of {@link GoldCow}, {@link EnchantedCow} and
 * {@link CrystalCow}, which override only the drops and the calf.
 *
 * <p>What the original inherited from 1.7.10 {@code EntityCow} and 1.21.1 {@link Cow} does differently is written out
 * here from the bytecode of {@code EntityCow} ({@code wh} in {@code reference/jar/mcp/client-1.7.10.jar}): the goal
 * list with the revenge-driven panic that RedCow's own reset acts on, the water avoidance, the 0.9x1.3 size of a calf
 * and the leather/beef drops. Sounds and the 1.7.10 animal XP (1-3, {@code Animal.getBaseExperienceReward}) are
 * inherited unchanged.
 *
 * <p>PORT: milking and feeding stay 1.21.1 {@code Cow.mobInteract}/{@code Animal.mobInteract}. A calf cannot be
 * milked, a creative player gets a milk bucket, and wheat makes a calf grow faster. 1.7.10 {@code EntityCow.interact}
 * milked calves too, only outside creative mode ({@code wh.a(yz)}), and {@code EntityAnimal.interact} fed only adults
 * ({@code wf.a(yz)}). These are vanilla base-class rules, not RedCow code.
 */
public class RedCow extends Cow {

    public RedCow(final EntityType<? extends RedCow> type, final Level level) {
        super(type, level);
        // EntityCow's constructor: setSize(0.9f, 1.3f) is the entity type's size; getNavigator().setAvoidsWater(true)
        // is a blocked water path node, as for EntityButterfly (W05).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    /** No {@code applyEntityAttributes} of its own: {@code EntityCow}'s health 10 and speed 0.2, the same as 1.21.1. */
    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    /**
     * {@code EntityCow}'s task list (bytecode {@code wh.<init>}, pc 21-167): 0 swimming, 1 panic 2.0, 2 mate 1.0,
     * 3 tempt 1.25 with wheat, 4 follow parent 1.25, 5 wander 1.0, 6 watch players 6.0, 7 look idle.
     *
     * <p>Panic is the 1.7.10 {@code EntityAIPanic} ({@link LegacyPanic#legacyPanic}): 1.21.1's {@code PanicGoal}
     * starts on the type of the last damage and ignores the revenge target, so the 1/200 reset in
     * {@link #customServerAiStep} would never end a panic, and it walks a burning cow to water within 5 blocks, which
     * {@code uz.a} never did (W07 catch-up: the former inline goal restored only the trigger). 1.21.1 Cow's
     * {@code WaterAvoidingRandomStrollGoal} is the plain {@code EntityAIWander} of 1.7.10 again.
     *
     * <p>Tempt, wander, watch and look idle are the 1.7.10 goals ({@link EntityAITempt}, {@link EntityAIWander},
     * {@link EntityAIWatchClosest}, {@link EntityAILookIdle}), not 1.21.1's {@code TemptGoal}, {@code RandomStrollGoal},
     * {@code LookAtPlayerGoal} and {@code RandomLookAroundGoal}: those roll their chances on every second tick instead of
     * every third (wandering three times as often, looking 1.5 times as often) and calm down after 100 ticks instead
     * of 300. GoldCow, EnchantedCow and CrystalCow inherit this list.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 2.0));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new EntityAITempt(this, 1.25, stack -> stack.is(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new EntityAIWander(this, 1.0));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new EntityAILookIdle(this));
    }

    /**
     * {@code updateAITick} (:29-34): with 1/200 on the world random, forget the revenge target, then the animal's own
     * step ({@code EntityAnimal.updateAITick}, the love reset of {@code Animal.customServerAiStep}).
     */
    @Override
    protected void customServerAiStep() {
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
    }

    /** {@code canDespawn} (:36-38). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /**
     * 1.7.10 halved the {@code EntityCow} size for a calf ({@code EntityAgeable.setScaleForAge}), 0.45x0.65. 1.21.1
     * {@link Cow} returns half of the vanilla cow type (0.9x1.4) instead of half of this type.
     */
    @Override
    public EntityDimensions getDefaultDimensions(final Pose pose) {
        return this.getType().getDimensions().scale(this.getAgeScale());
    }

    /** {@code createChild} (:21-23). */
    @Nullable
    @Override
    public RedCow getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:25-27); each subclass returns its own type. */
    @Nullable
    public RedCow spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.APPLE_COW.get().create(this.level());
    }

    /**
     * 1.7.10 {@code onDeath} rolled {@code dropFewItems(recentlyHit, looting)} and then {@code dropEquipment};
     * {@code Mob.dropCustomDeathLoot} is the latter (R10: drops in code, the loot table stays empty). Looting counts
     * only when a player killed the cow, as {@code EnchantmentHelper.getLootingModifier} in {@code onDeath}.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:14-19): {@code rand(3) + rand(1 + looting)} apples, then {@code EntityCow}'s drops. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var3 = this.random.nextInt(3) + this.random.nextInt(1 + par2), var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.APPLE, 1));
        }
        this.entityCowDropFewItems(par1, par2);
    }

    /**
     * {@code super.dropFewItems} of RedCow: 1.7.10 {@code EntityCow.dropFewItems} (bytecode {@code wh.b(ZI)V}).
     * {@code rand(3) + rand(1 + looting)} leather, then {@code rand(3) + 1 + rand(1 + looting)} beef, cooked while
     * the cow burns. Each {@code dropItem(item, 1)} is its own stack at the feet.
     */
    protected final void entityCowDropFewItems(final boolean par1, final int par2) {
        int j = this.random.nextInt(3) + this.random.nextInt(1 + par2);
        for (int k = 0; k < j; ++k) {
            this.spawnAtLocation(new ItemStack(Items.LEATHER, 1));
        }
        j = this.random.nextInt(3) + 1 + this.random.nextInt(1 + par2);
        for (int k = 0; k < j; ++k) {
            if (this.isOnFire()) {
                this.spawnAtLocation(new ItemStack(Items.COOKED_BEEF, 1));
            } else {
                this.spawnAtLocation(new ItemStack(Items.BEEF, 1));
            }
        }
    }

    /** The looting level {@code onDeath} passed on: the killer's, when the killer is a player. */
    protected static int lootingLevel(final ServerLevel level, final DamageSource damageSource) {
        if (damageSource.getEntity() instanceof Player killer) {
            return EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        return 0;
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
