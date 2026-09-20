package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.entity.insect.InsectSupport;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.Tshirt} (Tshirt.java:9-104), id {@code t_shirt} ("T-Shirt",
 * OreSpawnMain.java:3627-3631, tracking 32/1/false). The rotating advertising T-shirt of VillageMania: a
 * 4 x 4 {@code EntityAnimal} without goals that dies from one hit.
 *
 * <p>Values: health 1, speed 0, attack 0, armour 0 ({@link #getLegacyArmorValue()}). {@code experienceValue
 * = 40} (:18) is never read: {@code EntityAnimal.getExperiencePoints} gives {@code 1 + rand(3)}, inherited
 * from {@code Animal}. Drops emeralds through vanilla {@code dropFewItems}.
 *
 * <p>Not carried over: {@code entityInit}, {@code onUpdate}, {@code onLivingUpdate} (only super calls);
 * {@code initCreature} (:82, empty); {@code isAIEnabled}. The lang name is the manifest's "T-Shirt".
 */
public class Tshirt extends Animal implements LegacyArmor {

    private float moveSpeed;

    /** Constructor (:13-20). {@code setSize(4.0f, 4.0f)} is the entity type's size. */
    public Tshirt(final EntityType<? extends Tshirt> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.0f;
    }

    /** {@code applyEntityAttributes} (:22-28). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code fireResistance = 100} (:19). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code canDespawn} (:34-36): {@code !isNoDespawnRequired()}; {@code Animal} would answer {@code false}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code mygetMaxHealth} (:42-44). */
    public int mygetMaxHealth() {
        return 1;
    }

    /** {@code getTotalArmorValue} (:46-48). */
    @Override
    public int getLegacyArmorValue() {
        return 0;
    }

    /** {@code getLivingSound} (:58-60). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:62-64). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:66-68). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code getSoundVolume} (:70-72). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:74-76). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:78-80) through vanilla {@code dropFewItems}: 0..2 emeralds; equipment afterwards (super). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, Items.EMERALD);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code interact} (:85-87): always {@code false}, so no feeding either. */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * {@code getCanSpawnHere} (:89-99) on the instance: by day, Y 50 and above, and no other T-shirt in
     * {@code expand(20, 8, 20)} ({@code findNearestEntityWithinAABB} skips the shirt itself).
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (!InsectSupport.isDaytime(this.level())) {
            return false;
        }
        if (this.getY() < 50.0) {
            return false;
        }
        return level.getEntitiesOfClass(Tshirt.class, this.getBoundingBox().inflate(20.0, 8.0, 20.0), e -> e != this).isEmpty();
    }

    /** PORT: the collision and liquid half of 1.7.10's {@code getCanSpawnHere}, which the override replaced. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:89-99) as the placement predicate, with the box of a shirt at the block centre. */
    public static boolean checkTshirtSpawnRules(final EntityType<Tshirt> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        final AABB box = type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return level.getEntitiesOfClass(Tshirt.class, box.inflate(20.0, 8.0, 20.0)).isEmpty();
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat. Never reached, {@code mobInteract} passes. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:101-103). */
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
