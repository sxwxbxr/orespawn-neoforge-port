package com.swbr.orespawn.entity.terror;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Rat} (Rat.java:17-326), id {@code rat} ("Rat", 32/1/false): a small rodent on
 * {@code EntityMob} that attacks almost anything; rats summoned by the Rat Sword belong to a player
 * (verhalten/entity-11.md).
 *
 * <p>Values: {@code Rat_stats} (5/3/1), speed 0.25 re-set every tick (:26, :83), XP 5 (:30), {@code fireResistance} 10
 * (:31). Heals 1 with 1/250 per AI tick (:156-158). Suffocation immune (:274-281). The jump adds 0.25 to the motion
 * (:87-91). DataWatcher 20 = attacking (tail of {@code ModelRat}); NBT {@code MyOwner} (a string, {@code "null"} without
 * owner). Despawns only without persistence and owner (:54-56).
 *
 * <p><b>Original bug kept (R18, "Rat folgt nie"):</b> {@code setOwner} stores the player's UUID string (:251), but the
 * follow branch looks the owner up by <em>name</em> (:144). No player name is ever a UUID string, so the rat never
 * follows or teleports to its owner. The owner still counts for the target filter (:207-224), which compares UUIDs.
 *
 * <p>Not carried over: {@code onLivingUpdate} (:78-80) only calls {@code super}; {@code initCreature} (:117-118)
 * overrides nothing; {@code interact} (:120-122) returns {@code false}, the default.
 */
public class Rat extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:51). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Rat.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    /** The owner's UUID as a string (:21, :251), or {@code null}. Server state, not synchronised (as in 1.7.10). */
    @Nullable
    private String myowner;

    /** {@code Rat(World)} (:23-40) with {@code applyEntityAttributes} (:42-47). */
    public Rat(final EntityType<? extends Rat> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.25f;
        this.myowner = null;
        // setSize(0.25f, 0.5f) (:28) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:29) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 5;
        // fireResistance = 10 (:31) is getFireImmuneTicks.
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // EntityAIPanic(1.35): while a revenge target is set or it burns (LegacyPanic.legacyPanic).
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 1.350000023841858));
        // PORT (R18 case 3): EntityAIMoveThroughVillage(1.0, false) is vanilla's POI-based MoveThroughVillageGoal, the
        // same reading as MonsterSupport.legacyMoveThroughVillage (W07, package-private there).
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Rat_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:42-47) at registration time (R3), armor from {@code getTotalArmorValue} (:70-72). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Rat_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:49-52). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 10} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /** {@code canDespawn} (:54-56): not persistent and no owner. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && this.myowner == null;
    }

    /** {@code getAttacking} (:58-60). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:62-64). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code mygetMaxHealth} (:66-68). */
    public int mygetMaxHealth() {
        return MobStats.Rat_stats().health();
    }

    /** {@code getTotalArmorValue} (:70-72), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Rat_stats().defense();
    }

    /** {@code onUpdate} (:82-85): speed re-set, then the vanilla tick. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /**
     * {@code jump} (:87-91): the vanilla jump, then {@code motionY += 0.25} and {@code posY += 0.25}. The {@code posY}
     * write never moved the rat: 1.7.10 {@code moveEntity} recomputes {@code posY} from the bounding box, which the bare
     * field write did not touch (Ostrich and Elevator, W04/W06). Only the motion part is carried over.
     */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
    }

    /** {@code getLivingSound} (:93-95). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RATLIVE.get();
    }

    /** {@code getHurtSound} (:97-99). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.RATHIT.get();
    }

    /** {@code getDeathSound} (:101-103). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RATDEAD.get();
    }

    /** {@code getSoundVolume} (:105-107). */
    @Override
    protected float getSoundVolume() {
        return 0.45f;
    }

    /** {@code getSoundPitch} (:109-111). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:113-115). */
    @Nullable
    protected Item getDropItem() {
        return Items.ROTTEN_FLESH;
    }

    /** 1.7.10 {@code onDeath}: the inherited {@code EntityLiving.dropFewItems} with {@link #getDropItem()} (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code updateAITasks} (:124-159), after the goals: 1/200 forget the revenge target; 1/5 look for a target - with
     * one set {@code attacking}, walk at 1.25 and hit within squared 4 with {@code rand(8) == 0 || rand(7) == 1};
     * without one clear {@code attacking} and (dead, see the class comment) follow the owner at 1.75 beyond 8 blocks and
     * teleport to him beyond 16. Then 1/250 heal 1.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (this.level().random.nextInt(5) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.setAttacking(1);
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 4.0 && (this.random.nextInt(8) == 0 || this.random.nextInt(7) == 1)) {
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
                if (this.myowner != null) {
                    // getPlayerEntityByName with the stored UUID string: never a match (R18 bug kept).
                    final Player p = TerrorSupport.getPlayerEntityByName(this.level(), this.myowner);
                    if (p != null) {
                        if (this.distanceToSqr(p) > 64.0) {
                            this.getNavigation().moveTo(p, 1.75);
                        }
                        if (this.distanceToSqr(p) > 256.0) {
                            this.setPos(p.getX() + this.level().random.nextFloat() - this.level().random.nextFloat(), p.getY(),
                                    p.getZ() + this.level().random.nextFloat() - this.level().random.nextFloat());
                        }
                    }
                }
            }
        }
        if (this.level().random.nextInt(250) == 1) {
            this.heal(1.0f);
        }
    }

    /**
     * {@code isSuitableTarget} (:161-226): alive, not ignoreable, visible, not one of the listed water and ghost kinds;
     * not a creative player; for an owned rat not its owner and - with {@code RatPlayerFriendly} - no player at all, and
     * no tamed pet with {@code RatPetFriendly} and never a pet of its owner. Everything else is a target.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "irukandji")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "skate")) {
            return false;
        }
        if (par1EntityLiving instanceof Whale) {
            return false;
        }
        if (par1EntityLiving instanceof Flounder) {
            return false;
        }
        if (par1EntityLiving instanceof Rat) {
            return false;
        }
        if (par1EntityLiving instanceof Ghost) {
            return false;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "dungeon_beast")) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
            if (this.myowner != null) {
                if (this.myowner.equals(p.getUUID().toString())) {
                    return false;
                }
                if (OreSpawnConfig.TWEAKS.RatPlayerFriendly.get() != 0) {
                    return false;
                }
            }
        }
        if (this.myowner != null && par1EntityLiving instanceof TamableAnimal e) {
            if (OreSpawnConfig.TWEAKS.RatPetFriendly.get() != 0 && e.isTame()) {
                return false;
            }
            // func_152113_b (EntityTameable owner string) held the owner's UUID string in 1.7.10.
            if (e.getOwnerUUID() != null && this.myowner.equals(e.getOwnerUUID().toString())) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:228-245): {@code expand(9, 2, 9)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 9.0, 2.0, 9.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code setOwner} (:247-256): only a player becomes the owner, stored as the UUID string. */
    public void setOwner(@Nullable final LivingEntity e) {
        Player p = null;
        if (e != null && e instanceof Player) {
            p = (Player) e;
            final String s = p.getUUID().toString();
            if (s != null) {
                this.myowner = s;
            }
        }
    }

    /** {@code writeEntityToNBT} (:258-264): {@code MyOwner}, {@code "null"} without owner (the field becomes "null" too). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        if (this.myowner == null) {
            this.myowner = "null";
        }
        par1NBTTagCompound.putString("MyOwner", this.myowner);
    }

    /** {@code readEntityFromNBT} (:266-272). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.myowner = par1NBTTagCompound.getString("MyOwner");
        if (this.myowner != null && this.myowner.equals("null")) {
            this.myowner = null;
        }
    }

    /** {@code attackEntityFrom} (:274-281): suffocation ({@code "inWall"}) is ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        ret = super.hurt(par1DamageSource, par2);
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:283-320). A "Rat" spawner allows it; otherwise the monster light test;
     * in Crystal y at most 50 and at least 4 air blocks among the 9 on y+1; at most 8 rats in {@code expand(20, 10, 20)}.
     *
     * <p>PORT: spawner scan (x/z -2..1, y 0..4) → {@link MobSpawnType#SPAWNER} (catalogue 5.9); {@code isValidLightLevel}
     * → {@link LegacyLightLevel#isValidLightLevel} with the spawner's random; {@code findBuddies} ran on the not-yet-added
     * rat, so the box is the type's spawn box at the position and the rat itself is not counted (Molenoid, W07).
     */
    public static boolean checkRatSpawnRules(final EntityType<Rat> type, final ServerLevelAccessor level,
                                             final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        int sc = 0;
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (TerrorSupport.isCrystal(level.getLevel())) {
            if (pos.getY() > 50.0) {
                return false;
            }
            for (int k = -1; k <= 1; ++k) {
                for (int j = -1; j <= 1; ++j) {
                    if (level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + 1, pos.getZ() + k)).isAir()) {
                        ++sc;
                    }
                }
            }
            if (sc < 4) {
                return false;
            }
        }
        return findBuddies(type, level, pos) <= 8;
    }

    /** {@code findBuddies} (:322-325): rats in {@code expand(20, 10, 20)}. */
    private static int findBuddies(final EntityType<Rat> type, final ServerLevelAccessor level, final BlockPos pos) {
        return level.getEntitiesOfClass(Rat.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 10.0, 20.0)).size();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkRatSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
