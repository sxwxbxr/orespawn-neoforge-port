package com.swbr.orespawn.entity.companion;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIJealousy;
import com.swbr.orespawn.entity.ai.MyEntityAINearestAttackableTarget;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.arrow.UltimateArrow;
import com.swbr.orespawn.entity.vehicle.Elevator;
import com.swbr.orespawn.item.armor.ItemOreSpawnArmor;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Boyfriend} (verhalten/entity-05.md, design/design-entities-02.md): the
 * Girlfriend's twin without dance, Valentine's mode or reconciliation, tamed with cooked beef, dressed
 * with leather, throwing game controllers (shoe id 6), off by default ({@code BoyfriendEnable = 0}) and
 * with {@code BoyfriendBroMode} muting part of his voice.
 *
 * <p>Original bugs kept (R18): the diamond block ownership takeover (:699-713) and the unreachable name
 * tag branch (:714-724), exactly as in {@link Girlfriend}; {@link CompanionInteractEvents} restores the
 * 1.7.10 order in which the name tag reaches {@code interact} first.
 *
 * <p>Same {@code entityInit} double roll as the Girlfriend (see there), with the constructor rolling in
 * the order guy, voice, wet guy (:98-100) and {@code entityInit} in the order guy, wet guy, voice.
 */
public class Boyfriend extends TamableAnimal implements RangedAttackMob, AttackableNonMob, LegacyArmor {

    /** DataWatcher 20: outfit 0..27. */
    private static final EntityDataAccessor<Integer> DATA_WHICH_GUY = SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 21: voice 0..9. */
    private static final EntityDataAccessor<Integer> DATA_VOICE = SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 22: swim shorts 0..17. */
    private static final EntityDataAccessor<Integer> DATA_WHICH_WET_GUY = SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 23: voice_enable. */
    private static final EntityDataAccessor<Integer> DATA_VOICE_ENABLE = SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 24: is_prince 0/1/2. */
    private static final EntityDataAccessor<Integer> DATA_IS_PRINCE = SynchedEntityData.defineId(Boyfriend.class, EntityDataSerializers.INT);

    /** boyfriend0..27.png (:1010-1037). */
    private static final ResourceLocation[] DRY_TEXTURES = CompanionSupport.textures("boyfriend", 28);
    /** swimshorts0..17.png (:1038-1055). */
    private static final ResourceLocation[] WET_TEXTURES = CompanionSupport.textures("swimshorts", 18);
    /** FrogPrince.png, FrogPrince2.png (:1056-1057), lower-cased by the asset generator. */
    private static final ResourceLocation PRINCE_TEXTURE_1 = CompanionSupport.texture("frogprince");
    private static final ResourceLocation PRINCE_TEXTURE_2 = CompanionSupport.texture("frogprince2");

    public int which_guy;
    public int which_wet_guy;
    public int wet_count;
    private int auto_heal;
    private int force_sync;
    private int fight_sound_ticker;
    private int taunt_sound_ticker;
    private int had_target;
    private int voice;
    private float moveSpeed;
    private int voice_enable;
    public int passenger;
    private int is_prince;
    /** {@code EntityLivingBase.attackTime}; counted down by the base class and by the melee code (see Girlfriend). */
    private int attackTime;

    public Boyfriend(final EntityType<? extends Boyfriend> type, final Level par1World) {
        super(type, par1World);
        this.which_guy = 0;
        this.which_wet_guy = 0;
        this.wet_count = 0;
        this.auto_heal = 200;
        this.force_sync = 50;
        this.fight_sound_ticker = 0;
        this.taunt_sound_ticker = 0;
        this.had_target = 0;
        this.voice = 0;
        this.moveSpeed = 0.3f;
        this.voice_enable = 1;
        this.passenger = 0;
        this.is_prince = 0;
        this.which_guy = this.random.nextInt(28);
        this.voice = this.random.nextInt(10);
        this.which_wet_guy = this.random.nextInt(18);
        // setSize(0.5f, 1.6f) (:101) is the entity type size; isImmuneToFire (:102) is fireImmune().
        // PORT: getNavigator().setAvoidsWater(false) (:104) is a water path malus of 0.
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setSitting(false);
        // PORT: goals on both sides, as the 1.7.10 constructor added them (see Girlfriend).
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 1.4f, 12.0f, 1.5f));
        // Tempt, watch closest and look idle are the 1.7.10 vanilla goals from entity.ai on the legacy cadence (see
        // Girlfriend); EntityAITempt reads the main hand only.
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.25, stack -> stack.is(Items.COOKED_BEEF), false));
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        // EntityAIPanic: runs while a revenge target is set (see LegacyPanic.legacyPanic).
        this.goalSelector.addGoal(6, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(7, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(9, new EntityAILookIdle(this));
        // Never runs, as in the original: nothing enables door opening on the navigation.
        this.goalSelector.addGoal(10, new OpenDoorGoal(this, true));
        // :115; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(11, new EntityAIMoveIndoors(this));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(2, new MyEntityAINearestAttackableTarget(this, Creeper.class, 20.0f, 0, true, true,
                    MyEntityAINearestAttackableTarget.MOB_SELECTOR));
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(3, new MyEntityAINearestAttackableTarget(this, Mob.class, 15.0f, 0, true, true,
                    MyEntityAINearestAttackableTarget.MOB_SELECTOR));
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(4, new MyEntityAIJealousy(this, Boyfriend.class, 6.0f, 5, true));
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(5, new MyEntityAIJealousy(this, Boyfriend.class, 3.0f, 15, true));
        }
        // experienceValue = 0 (:128) is never read: 1.7.10 EntityAnimal.getExperiencePoints (wf.e) returns
        // 1 + rand.nextInt(3) and Boyfriend does not override it - the inherited Animal.getBaseExperienceReward.
        // applyEntityAttributes (:150-156).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.setHealth(this.getMaxHealth());
    }

    /** Attribute set for {@code EntityAttributeCreationEvent}: {@code applyEntityAttributes} (:150-156). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    /** {@code entityInit} (:131-148). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        this.which_guy = this.random.nextInt(28);
        builder.define(DATA_WHICH_GUY, this.which_guy);
        this.wet_count = 0;
        this.which_wet_guy = this.random.nextInt(18);
        builder.define(DATA_WHICH_WET_GUY, this.which_wet_guy);
        this.voice = this.random.nextInt(10);
        builder.define(DATA_VOICE, this.voice);
        // voice_enable and is_prince are still 0 here, exactly as in 1.7.10.
        builder.define(DATA_VOICE_ENABLE, this.voice_enable);
        builder.define(DATA_IS_PRINCE, this.is_prince);
        this.auto_heal = 200;
        this.force_sync = 50;
        this.fight_sound_ticker = 0;
        this.taunt_sound_ticker = 0;
        this.had_target = 0;
        // setSitting(false) (:147): the flags are not built yet; the constructor repeats it.
    }

    /** {@code getTotalArmorValue()} (:158-173) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return CompanionSupport.totalArmorValue(this);
    }

    /** {@code onUpdate} (:175-197). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        this.passenger = 0;
        if (this.isTame() && !this.isSitting()) {
            final Entity e = this.getOwner();
            if (e != null && e instanceof Player) {
                final Player p = (Player) e;
                final Entity r = e.getVehicle();
                if (r != null && r instanceof Elevator) {
                    final float f = -0.45f;
                    this.setPos(r.getX() - f * Math.sin(Math.toRadians(r.getYRot())), r.getY(),
                            r.getZ() + f * Math.cos(Math.toRadians(r.getYRot())));
                    this.setYRot(r.getYRot());
                    this.setXRot(r.getXRot());
                    // PORT: limbSwing cannot be reset in 1.21.1, only the swing speed (see Girlfriend).
                    this.walkAnimation.setSpeed(0.0f);
                    this.fallDistance = 0.0f;
                    this.passenger = 1;
                }
            }
        }
    }

    /** The {@code attackTime} countdown of 1.7.10 {@code EntityLivingBase.onEntityUpdate}. */
    @Override
    public void baseTick() {
        super.baseTick();
        if (this.attackTime > 0) {
            --this.attackTime;
        }
    }

    /** {@code writeEntityToNBT} (:199-206), preceded by {@code AttackTime} of {@code EntityLivingBase}. */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putShort("AttackTime", (short) this.attackTime);
        par1NBTTagCompound.putInt("GirlType", this.getTameSkin());
        par1NBTTagCompound.putInt("WetGirlType", this.getWetTameSkin());
        par1NBTTagCompound.putInt("GirlVoice", this.entityData.get(DATA_VOICE));
        par1NBTTagCompound.putInt("GirlVoiceEnable", this.entityData.get(DATA_VOICE_ENABLE));
        par1NBTTagCompound.putInt("IsPrince", this.entityData.get(DATA_IS_PRINCE));
    }

    /** {@code readEntityFromNBT} (:208-218). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.attackTime = par1NBTTagCompound.getShort("AttackTime");
        this.setTameSkin(this.which_guy = par1NBTTagCompound.getInt("GirlType"));
        this.setWetTameSkin(this.which_wet_guy = par1NBTTagCompound.getInt("WetGirlType"));
        this.voice = par1NBTTagCompound.getInt("GirlVoice");
        this.entityData.set(DATA_VOICE, this.voice);
        this.voice_enable = par1NBTTagCompound.getInt("GirlVoiceEnable");
        this.entityData.set(DATA_VOICE_ENABLE, this.voice_enable);
        this.is_prince = par1NBTTagCompound.getInt("IsPrince");
        this.entityData.set(DATA_IS_PRINCE, this.is_prince);
    }

    /** {@code updateAITick} (:220-272); unlike the Girlfriend no random attack-target reset. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        final ItemStack stack = this.getCurrentEquippedItem();
        Entity victim = this.getTarget();
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            victim = null;
        }
        if (this.level().random.nextInt(100) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (!stack.isEmpty() && !this.isSitting()) {
            if (victim != null) {
                if (victim instanceof LivingEntity && !this.getMainHandItem().isEmpty()) {
                    if (this.distanceTo(victim) < 4.0f
                            || (CompanionSupport.isItem(stack, "berthasmall") && this.distanceTo(victim) < 10.0f)) {
                        --this.attackTime;
                        if (this.attackTime <= 0) {
                            this.attackTime = 25;
                            this.swing(InteractionHand.MAIN_HAND);
                            this.attackTargetEntityWithCurrentItem(victim);
                            --this.fight_sound_ticker;
                            if (this.fight_sound_ticker <= 0) {
                                if (!this.level().isClientSide && this.voice_enable != 0) {
                                    CompanionSupport.playSoundAtEntity(this, ModSounds.B_FIGHT.get(), 0.5f, this.getSoundPitch());
                                }
                                this.fight_sound_ticker = 3;
                            }
                            this.had_target = 1;
                        }
                    } else if (this.distanceTo(victim) < 7.0f && !CompanionSupport.isItem(stack, "ultimatebow")) {
                        --this.taunt_sound_ticker;
                        if (this.taunt_sound_ticker <= 0) {
                            if (!this.level().isClientSide && this.voice_enable != 0) {
                                CompanionSupport.playSoundAtEntity(this, ModSounds.B_TAUNT.get(), 0.5f, this.getSoundPitch());
                            }
                            this.taunt_sound_ticker = 300;
                        }
                        this.getNavigation().moveTo(victim, 1.25);
                    }
                }
            } else {
                this.fight_sound_ticker = 0;
                this.attackTime = 0;
                if (this.had_target != 0) {
                    this.had_target = 0;
                    if (!this.level().isClientSide && this.voice_enable != 0) {
                        CompanionSupport.playSoundAtEntity(this, ModSounds.B_WOOHOO.get(), 0.4f, this.getSoundPitch());
                    }
                }
            }
        }
    }

    /** Sets the field only; DataWatcher 24 follows at the next force sync (:274-276). */
    public void setPrince(final int par1) {
        this.is_prince = par1;
    }

    /** {@code getTexture} (:278-430), read by {@code RenderBoyfriend.getTextureLocation}. */
    public ResourceLocation getTexture() {
        if (this.wet_count <= 0) {
            final int txture = this.getTameSkin();
            if (this.is_prince == 1) {
                return PRINCE_TEXTURE_1;
            }
            if (this.is_prince == 2) {
                return PRINCE_TEXTURE_2;
            }
            if (txture >= 0 && txture <= 27) {
                return DRY_TEXTURES[txture];
            }
        } else {
            final int temp = this.getWetTameSkin();
            if (temp >= 0 && temp <= 17) {
                return WET_TEXTURES[temp];
            }
        }
        // PORT: null crashed bindTexture (R18 case 1); only out-of-range NBT reaches this line.
        return DRY_TEXTURES[0];
    }

    public int getTameSkin() {
        return this.entityData.get(DATA_WHICH_GUY);
    }

    public int getVoice() {
        return this.entityData.get(DATA_VOICE);
    }

    public void setTameSkin(final int par1) {
        this.entityData.set(DATA_WHICH_GUY, par1);
        this.which_guy = par1;
    }

    public int getWetTameSkin() {
        return this.entityData.get(DATA_WHICH_WET_GUY);
    }

    public void setWetTameSkin(final int par1) {
        this.entityData.set(DATA_WHICH_WET_GUY, par1);
        this.which_wet_guy = par1;
    }

    // isAIEnabled (:454-456): every 1.21.1 mob runs goals.
    // canBreatheUnderwater (:458-460): final in 1.21.1, read from #minecraft:can_breathe_under_water.

    /** {@code fall} (:462-474); see Girlfriend for what the override leaves out. */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        float i = (float) Mth.ceil(par1 - 3.0f);
        if (i > 0.0f) {
            if (i > 3.0f) {
                this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0f, 1.0f);
                i = 3.0f;
            } else {
                this.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0f, 1.0f);
            }
            this.hurt(this.damageSources().fall(), i);
            return true;
        }
        return false;
    }

    /** {@code fireResistance = 100} (:103). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    public int mygetMaxHealth() {
        return 80;
    }

    /** {@code onLivingUpdate} (:480-511). */
    @Override
    public void aiStep() {
        this.updateSwingTime();
        super.aiStep();
        if (this.isInWater() || this.isInLava()) {
            this.wet_count = 500;
        } else if (this.wet_count > 0) {
            --this.wet_count;
        }
        --this.auto_heal;
        if (this.auto_heal <= 0) {
            if (this.mygetMaxHealth() > this.getBoyfriendHealth()) {
                this.heal(1.0f);
            }
            this.auto_heal = 150;
        }
        --this.force_sync;
        if (this.force_sync <= 0) {
            this.force_sync = 20;
            if (!this.level().isClientSide) {
                this.entityData.set(DATA_VOICE, this.voice);
                this.entityData.set(DATA_VOICE_ENABLE, this.voice_enable);
                this.entityData.set(DATA_IS_PRINCE, this.is_prince);
                this.setSitting(this.isSitting());
            } else {
                this.voice = this.getVoice();
                this.voice_enable = this.entityData.get(DATA_VOICE_ENABLE);
                this.is_prince = this.entityData.get(DATA_IS_PRINCE);
            }
        }
    }

    /**
     * Also read by {@code GirlfriendOverlayGui} (GirlfriendOverlayGui.java:97) through
     * {@code client.gui.GirlfriendOverlayTarget.select}.
     */
    public int getBoyfriendHealth() {
        return (int) this.getHealth();
    }

    /** {@code interact} (:517-763), checked in the original order on both sides. */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand; the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (!var2.isEmpty() && (var2.is(Items.COOKED_BEEF) || var2.is(ModItems.COOKED_PEACOCK.get()))
                && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.level().random.nextInt(3) == 0) {
                        this.setTame(true, false);
                        this.setOwnerUUID(par1EntityPlayer.getUUID());
                        this.spawnTamingParticles(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.mygetMaxHealth() - this.getHealth());
                    } else {
                        this.spawnTamingParticles(false);
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(par1EntityPlayer)) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal(this.mygetMaxHealth() - this.getHealth());
                }
            }
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(ModItems.RUBY.get()) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.voice_enable = 0;
                this.entityData.set(DATA_VOICE_ENABLE, this.voice_enable);
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(ModItems.AMETHYST.get()) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.voice_enable = 1;
                this.entityData.set(DATA_VOICE_ENABLE, this.voice_enable);
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && (var2.is(Items.LEATHER) || var2.is(ModItems.PEACOCK_FEATHER.get()))
                && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                if (this.wet_count > 0 || this.isInWater() || this.isInLava()) {
                    ++this.which_wet_guy;
                    if (this.which_wet_guy > 17) {
                        this.which_wet_guy = 0;
                    }
                    this.setWetTameSkin(this.which_wet_guy);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    if (this.isInWater() || this.isInLava()) {
                        this.wet_count = 500;
                    }
                } else {
                    ++this.which_guy;
                    if (this.which_guy > 27) {
                        this.which_guy = 0;
                    }
                    this.setTameSkin(this.which_guy);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
            }
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            final FoodProperties var3 = var2.getFoodProperties(this);
            if (var3 != null) {
                if (!isRemote) {
                    if (this.mygetMaxHealth() > this.getHealth()) {
                        this.heal((float) (var3.nutrition() * 5));
                    }
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                CompanionSupport.consumeOne(par1EntityPlayer, var2);
            } else {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                final ItemStack var4 = this.getCurrentEquippedItem();
                CompanionSupport.setCurrentItemOrArmor(this, 0, var2);
                if (var2.is(Items.DIAMOND)) {
                    this.setSitting(true);
                } else {
                    this.setSitting(false);
                }
                if (!var4.isEmpty()) {
                    CompanionSupport.setHeldSlot(par1EntityPlayer, var4);
                } else {
                    CompanionSupport.setHeldSlot(par1EntityPlayer, ItemStack.EMPTY);
                    final Item itm = var2.getItem();
                    if (itm instanceof ItemOreSpawnArmor) {
                        if (itm == ModItems.EMERALD_HELMET.get() || itm == ModItems.AMETHYST_HELMET.get() || itm == ModItems.ULTIMATE_HELMET.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 4);
                            CompanionSupport.setCurrentItemOrArmor(this, 4, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                        if (itm == ModItems.EMERALD_CHEST.get() || itm == ModItems.AMETHYST_CHEST.get() || itm == ModItems.ULTIMATE_CHEST.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 3);
                            CompanionSupport.setCurrentItemOrArmor(this, 3, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                        if (itm == ModItems.EMERALD_LEGGINGS.get() || itm == ModItems.AMETHYST_LEGGINGS.get() || itm == ModItems.ULTIMATE_LEGGINGS.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 2);
                            CompanionSupport.setCurrentItemOrArmor(this, 2, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                        if (itm == ModItems.EMERALD_BOOTS.get() || itm == ModItems.AMETHYST_BOOTS.get() || itm == ModItems.ULTIMATE_BOOTS.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 1);
                            CompanionSupport.setCurrentItemOrArmor(this, 1, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                    }
                }
            }
            return success;
        }
        // Original bug kept (R18): no owner check - a stranger with a diamond block takes him over.
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DIAMOND_BLOCK) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            this.setSitting(false);
            this.setTame(true, false);
            this.setOwnerUUID(par1EntityPlayer.getUUID());
            this.spawnTamingParticles(true);
            this.level().broadcastEntityEvent(this, (byte) 7);
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        // Original bug kept (R18): unreachable, the owner's name tag was taken into his hand above.
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            this.setCustomName(var2.getHoverName());
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            ItemStack var4 = CompanionSupport.getEquipmentInSlot(this, 0);
            int it = 0;
            if (var4.isEmpty()) {
                ++it;
                var4 = CompanionSupport.getEquipmentInSlot(this, it);
            }
            if (var4.isEmpty()) {
                ++it;
                var4 = CompanionSupport.getEquipmentInSlot(this, it);
            }
            if (var4.isEmpty()) {
                ++it;
                var4 = CompanionSupport.getEquipmentInSlot(this, it);
            }
            if (var4.isEmpty()) {
                ++it;
                var4 = CompanionSupport.getEquipmentInSlot(this, it);
            }
            if (!var4.isEmpty()) {
                CompanionSupport.setHeldSlot(par1EntityPlayer, var4);
                CompanionSupport.setCurrentItemOrArmor(this, it, ItemStack.EMPTY);
                this.setSitting(false);
                if (!isRemote) {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            } else if (!isRemote) {
                this.setSitting(false);
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                String healthMessage = new String();
                healthMessage = String.format("I have %d health. Thanks for asking!", this.getBoyfriendHealth());
                par1EntityPlayer.sendSystemMessage(Component.literal(healthMessage));
            }
            return success;
        }
        return super.mobInteract(par1EntityPlayer, hand);
    }

    /** {@code isWheat} (:765-767): no caller; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && (par1ItemStack.is(Items.COOKED_BEEF) || par1ItemStack.is(ModItems.COOKED_PEACOCK.get()));
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat, since {@code isWheat} overrides nothing. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code canDespawn} (:769-771). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getLivingSound} (:773-817); all rolls on the world random, unlike the Girlfriend. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting() || this.voice_enable == 0) {
            return null;
        }
        if (OreSpawnConfig.TWEAKS.BoyfriendBroMode.get() != 0 && this.level().random.nextInt(2) == 1) {
            return null;
        }
        if (this.level().random.nextInt(11) != 1) {
            return null;
        }
        final Entity victim = this.getTarget();
        if (victim != null) {
            return null;
        }
        if (this.isInWater() || this.isInLava()) {
            return ModSounds.B_WATER.get();
        }
        if (this.level().random.nextInt(4) != 0) {
            if (this.getY() < 60.0) {
                return null;
            }
            if (this.level().isThundering()) {
                return ModSounds.B_THUNDER.get();
            }
            if (this.level().isRaining()) {
                return ModSounds.B_RAIN.get();
            }
            // :800 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
            if (!this.level().isDay() && this.level().canSeeSky(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())))) {
                if (this.level().random.nextInt(3) == 0) {
                    return ModSounds.B_DARK.get();
                }
                return null;
            }
        }
        if (!this.isTame()) {
            return null;
        }
        if (this.mygetMaxHealth() > this.getHealth()) {
            return ModSounds.B_HURT.get();
        }
        if (OreSpawnConfig.TWEAKS.BoyfriendBroMode.get() != 0) {
            return ModSounds.BB_HAPPY.get();
        }
        return ModSounds.B_HAPPY.get();
    }

    /** {@code getHurtSound} (:819-827). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        if (this.voice_enable == 0) {
            return null;
        }
        if (OreSpawnConfig.TWEAKS.BoyfriendBroMode.get() != 0 && this.level().random.nextInt(2) == 1) {
            return null;
        }
        return ModSounds.B_OW.get();
    }

    /** {@code getDeathSound} (:829-834). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        if (OreSpawnConfig.TWEAKS.BoyfriendBroMode.get() != 0) {
            return null;
        }
        return this.isTame() ? ModSounds.B_DEATH_BOYFRIEND.get() : ModSounds.B_DEATH_SINGLE.get();
    }

    /** {@code getSoundVolume} (:836-838). */
    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    /** {@code dropFewItems} first, then the vanilla equipment roll, as 1.7.10 {@code onDeath} did (see Girlfriend). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:844-881); equipment drops as fresh stacks, components lost (original, R18). */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        }
        final Item v6 = ModItems.GAME_CONTROLLER.get();
        var3 = this.level().random.nextInt(26);
        var3 += 10;
        for (int var5 = 0; var5 < var3; ++var5) {
            this.spawnAtLocation(new ItemStack(v6, 1));
        }
        if (this.isTame()) {
            ItemStack var6 = this.getCurrentEquippedItem();
            if (!var6.isEmpty() && var6.getCount() > 0) {
                this.spawnAtLocation(new ItemStack(var6.getItem(), var6.getCount()));
            }
            var6 = CompanionSupport.getEquipmentInSlot(this, 1);
            if (!var6.isEmpty() && var6.getCount() > 0) {
                this.spawnAtLocation(new ItemStack(var6.getItem(), var6.getCount()));
            }
            var6 = CompanionSupport.getEquipmentInSlot(this, 2);
            if (!var6.isEmpty() && var6.getCount() > 0) {
                this.spawnAtLocation(new ItemStack(var6.getItem(), var6.getCount()));
            }
            var6 = CompanionSupport.getEquipmentInSlot(this, 3);
            if (!var6.isEmpty() && var6.getCount() > 0) {
                this.spawnAtLocation(new ItemStack(var6.getItem(), var6.getCount()));
            }
            var6 = CompanionSupport.getEquipmentInSlot(this, 4);
            if (!var6.isEmpty() && var6.getCount() > 0) {
                this.spawnAtLocation(new ItemStack(var6.getItem(), var6.getCount()));
            }
        }
    }

    /** {@code attackEntityWithRangedAttack} (:883-917): Ultimate Bow arrow or a game controller (shoe id 6). */
    public void attackEntityWithRangedAttack(final LivingEntity par1EntityLiving) {
        ItemStack it = ItemStack.EMPTY;
        if (this.swinging) {
            return;
        }
        it = this.getCurrentEquippedItem();
        if (CompanionSupport.isItem(it, "ultimatebow")) {
            final UltimateArrow var8 = new UltimateArrow(this.level(), this, par1EntityLiving, 2.0f, 10.0f);
            if (this.level().random.nextInt(4) == 1) {
                var8.setCritArrow(true);
            }
            final int var9 = CompanionSupport.enchantmentLevel(this.level(), it, Enchantments.PUNCH);
            if (var9 > 0) {
                var8.setKnockbackStrength(var9);
            }
            if (CompanionSupport.enchantmentLevel(this.level(), it, Enchantments.FLAME) > 0) {
                var8.igniteForSeconds(100.0f);
            }
            it.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            CompanionSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f,
                    1.0f / (this.level().random.nextFloat() * 0.4f + 1.2f) + 0.5f);
            var8.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            this.level().addFreshEntity(var8);
        } else {
            final Shoes var10 = new Shoes(this.level(), this, 6);
            final double var11 = par1EntityLiving.getX() - this.getX();
            final double var12 = par1EntityLiving.getEyeY() - 1.1 - var10.getY();
            final double var13 = par1EntityLiving.getZ() - this.getZ();
            final float var14 = (float) Math.sqrt(var11 * var11 + var13 * var13) * 0.2f;
            var10.setThrowableHeading(var11, var12 + var14, var13, 1.8f, 4.0f);
            CompanionSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(var10);
        }
        this.swing(InteractionHand.MAIN_HAND);
    }

    public ItemStack getCurrentEquippedItem() {
        return CompanionSupport.getEquipmentInSlot(this, 0);
    }

    /**
     * {@code attackTargetEntityWithCurrentItem} (:923-964); identical to the Girlfriend's except that
     * the critical bonus rolls on the world random (:945).
     */
    public void attackTargetEntityWithCurrentItem(final Entity par1Entity) {
        final ItemStack stack = this.getCurrentEquippedItem();
        if (!stack.isEmpty()) {
            float var2 = 0.0f;
            if (this.hasEffect(MobEffects.DAMAGE_BOOST)) {
                var2 += 3 << this.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
            }
            if (this.hasEffect(MobEffects.WEAKNESS)) {
                var2 -= 2 << this.getEffect(MobEffects.WEAKNESS).getAmplifier();
            }
            int var3 = 0;
            float var4 = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            final DamageSource source = this.damageSources().mobAttack(this);
            if (par1Entity instanceof Mob && this.level() instanceof ServerLevel serverLevel) {
                var4 = EnchantmentHelper.modifyDamage(serverLevel, stack, par1Entity, source, var4);
                var3 += (int) EnchantmentHelper.modifyKnockback(serverLevel, stack, par1Entity, source, 0.0f);
            }
            if (this.isSprinting()) {
                ++var3;
            }
            if (var2 > 0.0f || var4 > 0.0f) {
                final boolean var5 = this.fallDistance > 0.0f && !this.onGround() && !this.onClimbable() && !this.isInWater()
                        && !this.isInLava() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && par1Entity instanceof Mob;
                if (var5) {
                    // PORT: a non-positive bound crashed in 1.7.10 too; skipped (R18 case 1).
                    final int bound = (int) var2 / 2 + 2;
                    if (bound > 0) {
                        var2 += this.level().random.nextInt(bound);
                    }
                }
                var2 += var4;
                final boolean var6 = par1Entity.hurt(source, var2);
                if (var6 && var3 > 0) {
                    par1Entity.push((double) (-Mth.sin(this.getYRot() * 3.1415927f / 180.0f) * var3 * 0.5f), 0.1,
                            (double) (Mth.cos(this.getYRot() * 3.1415927f / 180.0f) * var3 * 0.5f));
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                    this.setSprinting(false);
                }
                final ItemStack var7 = this.getCurrentEquippedItem();
                if (par1Entity instanceof Mob) {
                    final int var8 = CompanionSupport.enchantmentLevel(this.level(), var7, Enchantments.FIRE_ASPECT);
                    if (var8 > 0 && var6) {
                        par1Entity.igniteForSeconds((float) (var8 * 4));
                    }
                }
            }
        }
    }

    /** {@code getSoundPitch} (:966-968). */
    protected float getSoundPitch() {
        return (this.voice - 5) * 0.02f + 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return this.getSoundPitch();
    }

    /** {@code createChild} (:970-972). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    @Override
    public void performRangedAttack(final LivingEntity entityliving, final float f) {
        this.attackEntityWithRangedAttack(entityliving);
    }

    /** {@code attackEntityFrom} (:978-988): every hit capped at 10, cactus ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float p2 = par2;
        if (p2 > 10.0f) {
            p2 = 10.0f;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, p2);
        }
        return ret;
    }

    /** Spawn predicate: {@code getCanSpawnHere} (:990-1007); see {@link Girlfriend#checkGirlfriendSpawnRules}. */
    public static boolean checkBoyfriendSpawnRules(final EntityType<Boyfriend> type, final ServerLevelAccessor level,
            final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (CompanionSupport.spawnerNearby(level, pos, OreSpawn.MOD_ID + ":boyfriend")) {
            return true;
        }
        return Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    /** 1.7.10 {@code EntityTameable.setSitting}. */
    public void setSitting(final boolean sitting) {
        this.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: no override in the original (Boyfriend.java:19, {@code extends EntityTameable}), and 1.7.10
     * {@code EntityTameable} did not override it either, so the owner got no chat message when the animal died.
     *
     * <p>PORT: R22 (addendum 2026-09-14, owner death message of tame OreSpawn animals 1:1 off) -
     * {@code TamableAnimal.die} in 1.21.1 sends {@code getCombatTracker().getDeathMessage()} to the owner. Java cannot
     * skip one super level, so this is {@code LivingEntity.die} line by line (NeoForge 21.1 sources) without the
     * owner message; the named-entity log line uses a logger of its own instead of {@code LivingEntity}'s private one.
     */
    @Override
    public void die(final net.minecraft.world.damagesource.DamageSource damageSource) {
        if (net.neoforged.neoforge.common.CommonHooks.onLivingDeath(this, damageSource)) {
            return;
        }
        if (!this.isRemoved() && !this.dead) {
            final net.minecraft.world.entity.Entity entity = damageSource.getEntity();
            final net.minecraft.world.entity.LivingEntity livingentity = this.getKillCredit();
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
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverlevel) {
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
