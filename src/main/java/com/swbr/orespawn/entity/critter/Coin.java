package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.Coin} (Coin.java:12-145), id {@code coin} ("Coin",
 * OreSpawnMain.java:3937-3941, tracking 64/1/false). The rotating website coin: an unmoving
 * {@code EntityAnimal} that only looks around, dies from one hit and drops exactly one gift.
 *
 * <p>Values: health 1, speed 0, attack 0, armour 0 ({@link #getLegacyArmorValue()}), hitbox 1.5
 * (entity type). {@code experienceValue = 10} (:20) is never read: {@code EntityAnimal.getExperiencePoints}
 * gives {@code 1 + rand(3)}, inherited from {@code Animal} (W04 lesson; the catalogue's "XP 10" is the dead field).
 *
 * <p>Not carried over: {@code entityInit}, {@code onUpdate}, {@code onLivingUpdate} (only super calls);
 * {@code initCreature} (:122, empty); {@code isAIEnabled}. The lang name is the manifest's "Coin"
 * ({@code tools/assets.py}); {@code LanguageRegistry} said "Coin!" (OreSpawnMain.java:3938).
 */
public class Coin extends Animal implements LegacyArmor {

    private float moveSpeed;

    /** Constructor (:16-23). {@code setSize(1.5f, 1.5f)} is the entity type's size. */
    public Coin(final EntityType<? extends Coin> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.0f;
        // EntityAILookIdle (:22) with the 1.7.10 cadence.
        this.goalSelector.addGoal(0, new EntityAILookIdle(this));
    }

    /** {@code applyEntityAttributes} (:25-31). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code fireResistance = 100} (:21). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code canDespawn} (:37-39): {@code !isNoDespawnRequired()}; {@code Animal} would answer {@code false}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code mygetMaxHealth} (:45-47). */
    public int mygetMaxHealth() {
        return 1;
    }

    /** {@code getTotalArmorValue} (:49-51). */
    @Override
    public int getLegacyArmorValue() {
        return 0;
    }

    /** {@code getLivingSound} (:61-63). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:65-67). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:69-71). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code getSoundVolume} (:73-75). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:77-79). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropItemRand} (:85-88): one stack a block higher, x and z each shifted by
     * {@code OreSpawnRand.nextInt(2) - OreSpawnRand.nextInt(2)}, added straight to the world - the vanilla
     * item motion of the {@code EntityItem} constructor, no pickup delay, not through the drop capture.
     */
    private void dropItemRand(final Item index, final int par1) {
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /**
     * {@code dropFewItems} (:90-120): {@code rand(10)} on the world random picks exactly one item - diamond,
     * uranium nugget, titanium nugget, emerald, the four emerald tools, the Coin spawn egg, or the emerald sword
     * of 9. Looting and {@code recentlyHit} are ignored as in the original; equipment afterwards (super).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        final int i = this.level().random.nextInt(10);
        Item j = ModItems.EMERALD_SWORD.get();
        if (i == 0) {
            j = Items.DIAMOND;
        }
        if (i == 1) {
            j = ModItems.URANIUM_NUGGET.get();
        }
        if (i == 2) {
            j = ModItems.TITANIUM_NUGGET.get();
        }
        if (i == 3) {
            j = Items.EMERALD;
        }
        if (i == 4) {
            j = ModItems.EMERALD_AXE.get();
        }
        if (i == 5) {
            j = ModItems.EMERALD_SHOVEL.get();
        }
        if (i == 6) {
            j = ModItems.EMERALD_PICKAXE.get();
        }
        if (i == 7) {
            j = ModItems.EMERALD_HOE.get();
        }
        if (i == 8) {
            j = ModItems.EGG_COIN.get();
        }
        this.dropItemRand(j, 1);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code interact} (:124-126): always {@code false}, so no feeding either (no {@code Animal.mobInteract}). */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * {@code getCanSpawnHere} (:128-140) on the instance: by day, Y 50 and above, and no other coin in
     * {@code expand(20, 8, 20)} ({@code findNearestEntityWithinAABB} skips the coin itself).
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (!InsectSupport.isDaytime(this.level())) {
            return false;
        }
        if (this.getY() < 50.0) {
            return false;
        }
        return level.getEntitiesOfClass(Coin.class, this.getBoundingBox().inflate(20.0, 8.0, 20.0), e -> e != this).isEmpty();
    }

    /** PORT: the collision and liquid half of 1.7.10's {@code getCanSpawnHere}, which the override replaced. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:128-140) as the placement predicate, with the box of a coin at the block centre. */
    public static boolean checkCoinSpawnRules(final EntityType<Coin> type, final ServerLevelAccessor level,
                                              final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        final AABB box = type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return level.getEntitiesOfClass(Coin.class, box.inflate(20.0, 8.0, 20.0)).isEmpty();
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat. Never reached, {@code mobInteract} passes. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:142-144). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
