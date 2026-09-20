package com.swbr.orespawn.entity.boss.prince;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.critter.Cockateil;
import com.swbr.orespawn.entity.critter.Dragonfly;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.util.Royalty;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.ThePrince} (ThePrince.java:16-849), id {@code the_prince} ("The Prince",
 * OreSpawnMain.java:3985, tracking 64/1/false). The three-headed toddler of the royal line on {@code EntityTameable}:
 * tames itself to the nearest player, hunts monsters and insects, spits fireballs, thunderbolts and ice balls, and
 * grows into {@link ThePrinceTeen} (verhalten/entity-02.md, "ThePrince").
 *
 * <h2>Two activities</h2>
 * {@code activity} 1 is a ground pet with the vanilla goals. {@code activity} 2 is flight: {@code noClip}
 * ({@code noPhysics}) through blocks, {@code super.updateAITasks()} skipped (goals and controls, see
 * {@link PrinceSupport#gateGoals}), {@code motionY *= 0.6} every tick and the motion nudge of {@link #do_movement}.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>{@code head1ext..head3ext} are not synchronised; both sides roll them on their own world random (:410-466).</li>
 *   <li>A flint and steel is consumed as a whole ({@code --stackSize}), not damaged (:226-243).</li>
 *   <li>A diamond on a tamed prince without {@code ok_to_grow} toggles sitting on the client only; the server skips the
 *       diamond branch and toggles it too (:244-285).</li>
 *   <li>{@code experienceValue = 50} (:68) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} gives
 *       {@code 1 + rand.nextInt(3)}, as {@code Animal.getBaseExperienceReward} still does (W04 lesson).</li>
 *   <li>The field {@code activity} is overwritten by every {@link #getActivity()} (:111-114), on both sides.</li>
 * </ul>
 *
 * <p>Not carried over: {@code isAIEnabled} (:150) is the 1.21.1 default; {@code canBreatheUnderwater} (:154) is the
 * {@code minecraft:can_breathe_under_water} tag (final in 1.21.1, W04); {@code canTriggerWalking} true and
 * {@code doesEntityNotTriggerPressurePlate} false (:347, :357) are the defaults, so no {@code NoStepTrigger} (R20);
 * {@code getDropItem} (:330) is unused because {@code dropFewItems} is overridden.
 */
public class ThePrince extends TamableAnimal implements Royalty, LegacyArmor {

    /** DataWatcher 20: SpyroFire (0 out, 1 lit), initial 1. */
    private static final EntityDataAccessor<Integer> DATA_SPYRO_FIRE = SynchedEntityData.defineId(ThePrince.class, EntityDataSerializers.INT);
    /** DataWatcher 21: activity (1 ground, 2 flight), initial 1. */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(ThePrince.class, EntityDataSerializers.INT);
    /** DataWatcher 22: attacking (0/1), initial 0, not saved. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(ThePrince.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    public int activity;
    private int owner_flying;
    private float moveSpeed;
    private int syncit;
    private int head1ext;
    private int head2ext;
    private int head3ext;
    private int head1dir;
    private int head2dir;
    private int head3dir;
    private int ok_to_grow;
    private int kill_count;
    private int fed_count;
    private int day_count;
    private int is_day;
    /** PORT: the {@code activity != 2} decision of {@code updateAITasks}, taken at the start of the AI step. */
    private boolean legacyAiOff;

    /**
     * {@code ThePrince(World)} (:36-69) with {@code entityInit} (:79-88). {@code setSize(0.75f, 1.25f)} is the entity
     * type's size (R9); {@code fireResistance = 1000} is {@link #getFireImmuneTicks()}, {@code isImmuneToFire} the
     * type's {@code fireImmune()}.
     */
    public ThePrince(final EntityType<? extends ThePrince> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.activity = 1;
        this.owner_flying = 0;
        this.moveSpeed = 0.3f;
        this.syncit = 0;
        this.head1ext = 0;
        this.head2ext = 0;
        this.head3ext = 0;
        this.head1dir = 1;
        this.head2dir = 1;
        this.head3dir = 1;
        this.ok_to_grow = 0;
        this.kill_count = 0;
        this.fed_count = 0;
        this.day_count = 0;
        this.is_day = 0;
        this.moveSpeed = 0.32f;
        // getNavigator().setAvoidsWater(true) (:58): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        this.moveControl = new PrinceSupport.GatedMoveControl(this, () -> this.legacyAiOff);
        this.lookControl = new PrinceSupport.GatedLookControl(this, () -> this.legacyAiOff);
        this.jumpControl = new PrinceSupport.GatedJumpControl(this, () -> this.legacyAiOff);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 1.15f, 12.0f, 2.0f));
        this.goalSelector.addGoal(3, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(6, new EntityAILookIdle(this));
        // :66; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(7, new EntityAIMoveIndoors(this));
        this.TargetSorter = new GenericTargetSorter(this);
    }

    /**
     * {@code applyEntityAttributes} (:71-77): health 500, attack 10 (never read: {@link #doHurtTarget} strikes with
     * {@link #getAttackStrength} 10). The speed attribute is written every tick in {@link #aiStep()}; its initial value is
     * that effective 0.32 (verhalten/entity-02.md: the constructor field was still 0 when 1.7.10 applied it).
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, W09 Spyro);
     * 1.21.1 defaults to 0.6.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.32f)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code entityInit} (:79-88): 22 = 0, 21 = activity 1, 20 = 1; not sitting, not tamed. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_SPYRO_FIRE, 1);
    }

    /** {@code fireResistance = 1000} (:56). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code writeEntityToNBT} (:90-98). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("SpyroActivity", this.entityData.get(DATA_ACTIVITY));
        par1NBTTagCompound.putInt("SpyroFire", this.entityData.get(DATA_SPYRO_FIRE));
        par1NBTTagCompound.putInt("SpyroGrow", this.ok_to_grow);
        par1NBTTagCompound.putInt("SpyroKill", this.kill_count);
        par1NBTTagCompound.putInt("SpyroFed", this.fed_count);
        par1NBTTagCompound.putInt("SpyroDay", this.day_count);
    }

    /** {@code readEntityFromNBT} (:100-109). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.activity = par1NBTTagCompound.getInt("SpyroActivity");
        this.entityData.set(DATA_ACTIVITY, this.activity);
        this.entityData.set(DATA_SPYRO_FIRE, par1NBTTagCompound.getInt("SpyroFire"));
        this.ok_to_grow = par1NBTTagCompound.getInt("SpyroGrow");
        this.kill_count = par1NBTTagCompound.getInt("SpyroKill");
        this.fed_count = par1NBTTagCompound.getInt("SpyroFed");
        this.day_count = par1NBTTagCompound.getInt("SpyroDay");
    }

    /** {@code getActivity} (:111-114): reads DataWatcher 21 and stores it in the field. Read by {@code ThePrinceModel}. */
    public int getActivity() {
        final int i = this.entityData.get(DATA_ACTIVITY);
        return this.activity = i;
    }

    /** {@code setActivity} (:116-120): field and DataWatcher, no side check. */
    public void setActivity(final int par1) {
        this.activity = par1;
        this.entityData.set(DATA_ACTIVITY, 0);
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getSpyroFire} (:122-124). */
    public int getSpyroFire() {
        return this.entityData.get(DATA_SPYRO_FIRE);
    }

    /** {@code setSpyroFire} (:126-128). */
    public void setSpyroFire(final int par1) {
        this.entityData.set(DATA_SPYRO_FIRE, par1);
    }

    /** {@code getAttacking} (:130-132). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:134-136). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getHead1Ext} (:138-140): local, not synchronised. */
    public int getHead1Ext() {
        return this.head1ext;
    }

    /** {@code getHead2Ext} (:142-144). */
    public int getHead2Ext() {
        return this.head2ext;
    }

    /** {@code getHead3Ext} (:146-148). */
    public int getHead3Ext() {
        return this.head3ext;
    }

    /** {@code mygetMaxHealth} (:158-160). */
    public int mygetMaxHealth() {
        return 500;
    }

    /**
     * {@code interact} (:162-287), both sides like the original. A {@code true} answer is {@code sidedSuccess}. The
     * click order around it (name tag, a stranger's lead) is restored by {@link PrinceInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return this.legacyInteract(par1EntityPlayer);
    }

    /**
     * The body of {@code interact(EntityPlayer)} (:162-287); the last line, {@code super.interact}, is 1.7.10
     * {@code EntityAnimal.interact} ({@link HerbivoreSupport#legacyAnimalInteract}: wheat makes it fall in love, which
     * never leads anywhere). {@code func_152114_e} is "is owner", {@code func_152115_b} "set owner".
     */
    InteractionResult legacyInteract(final Player par1EntityPlayer) {
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :163-167 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (!var2.isEmpty() && var2.is(Items.DIAMOND_BLOCK) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!isRemote) {
                // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in Spyro.
                this.setTame(true, false);
                this.setOwnerUUID(par1EntityPlayer.getUUID());
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.mygetMaxHealth() - this.getHealth());
                this.ok_to_grow = 1;
                this.kill_count = 1000;
                this.fed_count = 1000;
                this.day_count = 1000;
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        // ItemFood is an item with a food component; func_150905_g (getHealAmount) is nutrition (W04 Girlfriend).
        final FoodProperties food = var2.isEmpty() ? null : var2.getFoodProperties(this);
        if (this.isTame() && !var2.isEmpty() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && food != null) {
            if (!isRemote) {
                final FoodProperties var3 = food;
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal((float) (var3.nutrition() * 10));
                }
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                ++this.fed_count;
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.ICE) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(0);
                par1EntityPlayer.sendSystemMessage(Component.literal("Prince fireballs extinguished."));
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.FLINT_AND_STEEL) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(1);
                par1EntityPlayer.sendSystemMessage(Component.literal("Prince fireballs lit!"));
            }
            // --stackSize on a damageable item: the whole flint and steel goes (R18).
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (!var2.isEmpty() && var2.is(Items.DIAMOND) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer) && !isRemote && this.ok_to_grow != 0) {
            ThePrinceTeen d = null;
            d = PrinceSupport.spawnCreature(this.level(), ModEntities.THE_YOUNG_PRINCE.get(), this.getX(), this.getY(), this.getZ());
            if (d != null) {
                if (this.isTame()) {
                    d.setTame(true, false);
                    d.setOwnerUUID(par1EntityPlayer.getUUID());
                }
                this.discard();
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            this.setCustomName(var2.getHoverName());
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            if (!this.isSitting()) {
                this.setSitting(true);
                this.setActivity(1);
            } else {
                this.setSitting(false);
            }
            return success;
        }
        return HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, InteractionHand.MAIN_HAND);
    }

    /** {@code set_ok_to_grow} (:289-294), called by {@link ThePrinceTeen} when it shrinks back. */
    public void set_ok_to_grow() {
        this.ok_to_grow = 1;
        this.kill_count = 0;
        this.fed_count = 0;
        this.day_count = 0;
    }

    /** {@code isWheat} (:296-298): overrides nothing in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}, not overridden: wheat, read by the {@code super.interact} step. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(Items.WHEAT);
    }

    /** {@code canDespawn} (:300-302). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getLivingSound} (:304-312): a roar only while attacking and not sitting. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        if (this.getAttacking() == 0) {
            return null;
        }
        return ModSounds.ROAR.get();
    }

    /** {@code getHurtSound} (:314-316). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:318-320). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:322-324). */
    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** {@code getTotalArmorValue} (:326-328) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 16;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:334-341): 1-4 raw beef ({@code dropItem}), from the world random. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        var3 = this.level().random.nextInt(4);
        ++var3;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.BEEF, 1));
        }
    }

    /** {@code getSoundPitch} (:343-345), from the world random. 1.21.1 reads the voice pitch from here. */
    @Override
    public float getVoicePitch() {
        return (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2f + 1.3f;
    }

    /** {@code fall} (:351-352): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:354-355): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** Spawn predicate: {@code getCanSpawnHere} (:361-363) is {@code true}. No natural spawns (manifest). */
    public static boolean checkThePrinceSpawnRules(final EntityType<ThePrince> type, final ServerLevelAccessor level,
                                                   final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkThePrinceSpawnRules}; {@code EntityAnimal}'s grass test was overridden. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code createChild} (:365-367). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** {@code getAttackStrength} (:369-371). */
    public float getAttackStrength(final Entity par1Entity) {
        return 10.0f;
    }

    /** {@code attackEntityAsMob} (:373-383): 10 as mob damage; a target left at 0 health counts as a kill. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final float var2 = this.getAttackStrength(par1Entity);
        final boolean var3 = par1Entity.hurt(this.damageSources().mobAttack(this), var2);
        if (par1Entity instanceof LivingEntity el) {
            if (el.getHealth() <= 0.0f) {
                ++this.kill_count;
            }
        }
        return var3;
    }

    /**
     * {@code attackEntityFrom} (:385-396): suffocation answers {@code false}, cactus is ignored; every other hit wakes it
     * and makes it fly, whatever {@code super} answered. PORT: {@code getDamageType()} is {@link DamageSource#getMsgId()},
     * the same string.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.getMsgId().equals("inWall")) {
            return false;
        }
        if (!par1DamageSource.getMsgId().equals("cactus")) {
            ret = super.hurt(par1DamageSource, par2);
            this.setSitting(false);
            this.setActivity(2);
        }
        return ret;
    }

    /**
     * {@code canSeeTarget} (:398-400): {@code rayTraceBlocks(from, to, false) == null} from 0.75 above the feet. 1.7.10
     * traced blocks by their selection box and ignored liquids ({@code ClipContext.Block.OUTLINE}, W06 GoldFish).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code onUpdate} (:402-467): the vanilla update, then {@code noClip} in flight and the three random head swings, on
     * both sides from the world random.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.getActivity() == 2) {
            this.noPhysics = true;
        } else {
            this.noPhysics = false;
        }
        if (this.level().random.nextInt(10) == 1) {
            final int i = this.level().random.nextInt(3);
            if (i == 0) {
                this.head1dir = 2;
            }
            if (i == 1) {
                this.head1dir = -2;
            }
            if (i == 2) {
                this.head1dir = 0;
            }
        }
        if (this.level().random.nextInt(10) == 1) {
            final int i = this.level().random.nextInt(3);
            if (i == 0) {
                this.head2dir = 2;
            }
            if (i == 1) {
                this.head2dir = -2;
            }
            if (i == 2) {
                this.head2dir = 0;
            }
        }
        if (this.level().random.nextInt(10) == 1) {
            final int i = this.level().random.nextInt(3);
            if (i == 0) {
                this.head3dir = 2;
            }
            if (i == 1) {
                this.head3dir = -2;
            }
            if (i == 2) {
                this.head3dir = 0;
            }
        }
        this.head1ext += this.head1dir;
        if (this.head1ext < 0) {
            this.head1ext = 0;
        }
        if (this.head1ext > 60) {
            this.head1ext = 60;
        }
        this.head2ext += this.head2dir;
        if (this.head2ext < 0) {
            this.head2ext = 0;
        }
        if (this.head2ext > 60) {
            this.head2ext = 60;
        }
        this.head3ext += this.head3dir;
        if (this.head3ext < 0) {
            this.head3ext = 0;
        }
        if (this.head3ext > 60) {
            this.head3ext = 60;
        }
    }

    /**
     * {@code onLivingUpdate} (:469-492): the speed attribute, the vanilla living update (which runs
     * {@link #customServerAiStep()}), buoyancy, the flight target, the activity resync every 21 ticks and the flight
     * damping - on both sides.
     */
    @Override
    public void aiStep() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        if (!this.level().isClientSide) {
            // updateAITasks (:501): `if (this.activity != 2) super.updateAITasks()`, see PrinceSupport.gateGoals.
            this.legacyAiOff = this.activity == 2;
            PrinceSupport.gateGoals(this, this.legacyAiOff);
        }
        super.aiStep();
        if (this.isInWater()) {
            final Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, m.y + 0.07, m.z);
        }
        if (this.currentFlightTarget == null) {
            // :476 (int) casts -> Mth.floor (DECISIONS R20), here and below.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        ++this.syncit;
        if (this.syncit > 20) {
            this.syncit = 0;
            if (this.level().isClientSide) {
                this.getActivity();
            } else {
                final int j = this.activity;
                this.setActivity(j);
            }
        }
        if (this.activity == 2) {
            final Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        }
    }

    /**
     * {@code updateAITasks} (:494-580) after the vanilla block: forget the revenge target with 1 in 200, heal 1 with
     * 1 in 200, tame itself to a player within 10, pick the activity and move (not sitting), grow into the Young Prince,
     * count sunrises.
     *
     * <p>PORT: 1.21.1 {@code Mob.serverAiStep} is final and runs the goals before this method; the original forgot the
     * revenge target before them. None of the prince's goals reads the revenge target, and the goals draw from the
     * entity random, this code from the world random.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        // :501-503 `if (this.activity != 2) super.updateAITasks()`: goals and controls are gated in aiStep; what is left
        // of the vanilla block here is EntityAnimal.updateAITick (inLove reset), Animal.customServerAiStep in 1.21.1.
        if (!this.legacyAiOff) {
            super.customServerAiStep();
        }
        if (this.level().random.nextInt(200) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
        if (!this.isTame()) {
            // getClosestPlayerToEntity(this, 10.0): 1.21.1 leaves spectators out, creative players stay in.
            final Player p = this.level().getNearestPlayer(this, 10.0);
            if (p != null) {
                this.setTame(true, false);
                this.setOwnerUUID(p.getUUID());
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.mygetMaxHealth() - this.getHealth());
            }
        }
        if (!this.isSitting()) {
            if (this.activity == 0) {
                this.setActivity(1);
            }
            if (this.level().random.nextInt(100) == 1) {
                if (this.level().random.nextInt(20) == 1) {
                    this.setActivity(2);
                } else {
                    this.setActivity(1);
                }
            }
            this.owner_flying = 0;
            // PORT: (EntityPlayer) getOwner() threw for a non-player owner; 1.21.1 getOwner is any LivingEntity (R18 case 1).
            if (this.isTame() && this.getOwner() instanceof Player e) {
                if (e.getAbilities().flying) {
                    this.owner_flying = 1;
                    this.setActivity(2);
                }
            }
            if (this.activity == 1 && this.isTame() && this.getOwner() != null) {
                final LivingEntity e2 = this.getOwner();
                if (this.distanceToSqr(e2) > 256.0) {
                    this.setActivity(2);
                }
            }
            this.do_movement();
        } else if (this.isTame() && this.getOwner() != null) {
            final LivingEntity e2 = this.getOwner();
            if (this.distanceToSqr(e2) > 256.0) {
                this.setSitting(false);
                this.setActivity(2);
            }
        }
        if (this.kill_count > 25 && this.fed_count > 10 && this.day_count > 10) {
            ThePrinceTeen d = null;
            d = PrinceSupport.spawnCreature(this.level(), ModEntities.THE_YOUNG_PRINCE.get(), this.getX(), this.getY(), this.getZ());
            if (d != null) {
                if (this.isTame()) {
                    d.setTame(true, false);
                    d.setOwnerUUID(this.getOwnerUUID()); // func_152115_b(func_152113_b())
                }
                this.discard();
            }
        }
        if (this.is_day == 0) {
            this.is_day = 1;
            if (!HerbivoreSupport.isDaytime(this.level())) {
                this.is_day = -1;
            }
        } else {
            if (this.is_day == -1 && HerbivoreSupport.isDaytime(this.level())) {
                ++this.day_count;
            }
            this.is_day = 1;
            if (!HerbivoreSupport.isDaytime(this.level())) {
                this.is_day = -1;
            }
        }
    }

    /**
     * {@code do_movement} (:582-735): hunt or flee (both activities), then in flight pick an air waypoint around the
     * owner or itself and nudge the motion towards it.
     */
    private void do_movement() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 10;
        int do_new = 0;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        int has_owner = 0;
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        final double pi = 3.1415926545;
        LivingEntity e = null;
        if (this.currentFlightTarget == null) {
            do_new = 1;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.activity == 2 && this.level().random.nextInt(300) == 0) {
            do_new = 1;
        }
        if (this.isTame() && this.getOwner() != null) {
            e = this.getOwner();
            has_owner = 1;
            ox = e.getX();
            oy = e.getY() + 1.0;
            oz = e.getZ();
            if (this.distanceToSqr(e) > 100.0) {
                do_new = 1;
            }
            if (this.owner_flying != 0 && this.distanceToSqr(e) > 36.0) {
                do_new = 1;
            }
        }
        if (this.level().random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.isTame() && this.getHealth() / this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(2);
                    this.setAttacking(0);
                    do_new = 0;
                    this.currentFlightTarget.set(Mth.floor(this.getX() + (this.getX() - e.getX())), Mth.floor(this.getY() + 1.0),
                            Mth.floor(this.getZ() + (this.getZ() - e.getZ())));
                } else {
                    this.setActivity(2);
                    this.setAttacking(1);
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                    do_new = 0;
                    if (this.distanceToSqr(e) < (3.0f + e.getBbWidth() / 2.0f) * (3.0f + e.getBbWidth() / 2.0f)) {
                        this.doHurtTarget(e);
                    } else if (this.distanceToSqr(e) > 25.0 && this.distanceToSqr(e) < 144.0 && !this.isInWater() && this.getSpyroFire() != 0
                            && (this.level().random.nextInt(3) == 0 || this.level().random.nextInt(4) == 1)) {
                        final int which = this.level().random.nextInt(3);
                        if (which == 0) {
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                this.firecanon(e);
                            }
                        } else if (which == 1) {
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                this.firecanonl(e);
                            }
                        } else {
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                this.firecanoni(e);
                            }
                        }
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.activity == 1) {
            return;
        }
        // ChunkCoordinates.getDistanceSquared(int, int, int) is a float of the int differences.
        final int cdx = this.currentFlightTarget.getX() - Mth.floor(this.getX());
        final int cdy = this.currentFlightTarget.getY() - Mth.floor(this.getY());
        final int cdz = this.currentFlightTarget.getZ() - Mth.floor(this.getZ());
        if ((float) (cdx * cdx + cdy * cdy + cdz * cdz) < 2.1f) {
            do_new = 1;
        }
        if (do_new != 0) {
            // for (bid = stone; bid != air && keep_trying != 0; bid = getBlock(target), --keep_trying)
            for (boolean airFound = false; !airFound && keep_trying != 0;
                 airFound = this.level().getBlockState(this.currentFlightTarget).isAir(), --keep_trying) {
                int gox = Mth.floor(this.getX());
                int goy = Mth.floor(this.getY());
                int goz = Mth.floor(this.getZ());
                if (has_owner == 1) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = this.level().random.nextInt(4) + 6;
                        xdir = this.level().random.nextInt(4) + 6;
                    } else {
                        zdir = this.level().random.nextInt(8);
                        xdir = this.level().random.nextInt(8);
                    }
                } else {
                    zdir = this.level().random.nextInt(5) + 6;
                    xdir = this.level().random.nextInt(5) + 6;
                }
                if (this.level().random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.level().random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir, goy + (this.level().random.nextInt(6 + this.owner_flying * 2) - 2), goz + zdir);
            }
        }
        double speed_factor = 1.0;
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.owner_flying != 0) {
            speed_factor = 1.75;
            if (this.isTame() && this.getOwner() != null) {
                e = this.getOwner();
                if (this.distanceToSqr(e) > 49.0) {
                    speed_factor = 3.5;
                }
            }
        }
        final Vec3 m = this.getDeltaMovement();
        double motionX = m.x;
        double motionY = m.y;
        double motionZ = m.z;
        motionX += (Math.signum(var1) * 0.5 - motionX) * 0.15 * speed_factor;
        motionY += (Math.signum(var2) * 0.7 - motionY) * 0.21 * speed_factor;
        motionZ += (Math.signum(var3) * 0.5 - motionZ) * 0.15 * speed_factor;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.setZza((float) (0.75 * speed_factor));
        this.setYRot(this.getYRot() + var5 / 3.0f);
    }

    /**
     * {@code isSuitableTarget} (:737-739): monsters, Mothra, butterflies, birds, dragonflies and mosquitoes in sight,
     * never royalty. {@code EntityMob} is {@link Monster} (util.MyUtils).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && !MyUtils.isRoyalty(par1EntityLiving)
                && (par1EntityLiving instanceof Monster || par1EntityLiving instanceof Mothra || par1EntityLiving instanceof EntityButterfly
                    || par1EntityLiving instanceof Cockateil || par1EntityLiving instanceof Dragonfly || par1EntityLiving instanceof EntityMosquito);
    }

    /**
     * {@code findSomethingToAttack} (:741-758): nothing under {@code PlayNicely}; else the first suitable one by the
     * sorter in ±12/6/12 that {@link #canSeeTarget} at its feet as well.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 6.0, 12.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false) && this.canSeeTarget(var8.getX(), var8.getY(), var8.getZ())) {
                return var8;
            }
        }
        return null;
    }

    /** {@code firecanon} (:760-778): a big fireball, half of them small, 3 ahead and 1 up, spread ±5/±3/±5. */
    private void firecanon(final LivingEntity e) {
        final double yoff = 1.0;
        final double xzoff = 3.0;
        BetterFireball bf = null;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        bf = new BetterFireball(this.level(), this, e.getX() - cx + r1, e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2,
                e.getZ() - cz + r3);
        bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
        bf.setPos(cx, this.getY() + yoff, cz);
        bf.setBig();
        if (this.level().random.nextInt(2) == 1) {
            bf.setSmall();
        }
        PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(bf);
    }

    /** {@code firecanonl} (:780-807): a thunderbolt without thrower, heading 1.4/4.0, motion ×3. The spread rolls are unused. */
    private void firecanonl(final LivingEntity e) {
        final double yoff = 1.0;
        final double xzoff = 3.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        // r1..r3 (:790-792): drawn, never read.
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        final ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
        lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
        var3 = e.getX() - lb.getX();
        var4 = e.getY() + 0.25 - lb.getY();
        var5 = e.getZ() - lb.getZ();
        var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
        lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
        final Vec3 lm = lb.getDeltaMovement();
        lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
        this.level().addFreshEntity(lb);
    }

    /** {@code firecanoni} (:809-837): an ice ball that makes ice, heading 1.4/4.0, motion ×3. The spread rolls are unused. */
    private void firecanoni(final LivingEntity e) {
        final double yoff = 1.0;
        final double xzoff = 3.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        // r1..r3 (:819-821): drawn, never read.
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        final IceBall lb = new IceBall(this.level(), cx, this.getY() + yoff, cz);
        lb.setIceMaker(1);
        lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
        var3 = e.getX() - lb.getX();
        var4 = e.getY() + 0.25 - lb.getY();
        var5 = e.getZ() - lb.getZ();
        var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
        lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
        final Vec3 lm = lb.getDeltaMovement();
        lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
        this.level().addFreshEntity(lb);
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: one synced flag; 1.21.1 splits it into order and pose. */
    public void setSitting(final boolean sitting) {
        HerbivoreSupport.setSitting(this, sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: not overridden, and 1.7.10 {@code EntityTameable} sent the owner no chat message.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code TamableAnimal.die} in 1.21.1 sends
     * {@code getCombatTracker().getDeathMessage()} to the owner. Java cannot skip one super level, so this is
     * {@code LivingEntity.die} line by line (NeoForge 21.1 sources) without the owner message, as in W09 Spyro.
     */
    @Override
    public void die(final DamageSource damageSource) {
        if (net.neoforged.neoforge.common.CommonHooks.onLivingDeath(this, damageSource)) {
            return;
        }
        if (!this.isRemoved() && !this.dead) {
            final Entity entity = damageSource.getEntity();
            final LivingEntity livingentity = this.getKillCredit();
            if (this.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(this, this.deathScore, damageSource);
            }
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            if (!this.level().isClientSide && this.hasCustomName()) {
                com.mojang.logging.LogUtils.getLogger().info("Named entity {} died: {}", this,
                        this.getCombatTracker().getDeathMessage().getString());
            }
            this.dead = true;
            this.getCombatTracker().recheckStatus();
            if (this.level() instanceof ServerLevel serverlevel) {
                if (entity == null || entity.killedEntity(serverlevel, this)) {
                    this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_DIE);
                    this.dropAllDeathLoot(serverlevel, damageSource);
                    this.createWitherRose(livingentity);
                }
                this.level().broadcastEntityEvent(this, (byte) 3);
            }
            this.setPose(net.minecraft.world.entity.Pose.DYING);
        }
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
