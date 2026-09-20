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
import com.swbr.orespawn.entity.ai.MyEntityAIDance;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIJealousy;
import com.swbr.orespawn.entity.ai.MyEntityAINearestAttackableTarget;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.ai.MyValentineTarget;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
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
 * Port of {@code danger.orespawn.Girlfriend} (verhalten/entity-08.md, design/design-entities-03.md):
 * the tameable companion who carries a weapon and armor, fights in melee or throws {@link Shoes}
 * (or shoots the Ultimate Bow), dances at night on gem blocks, talks, and on Valentine's Day is an
 * 8-block giantess until a Rose Sword hit reconciles her.
 *
 * <p>Original bugs kept (R18): a stranger with a diamond block takes over ownership (:785-799, step 6
 * of the interaction - step 5 catches every item of the owner first); the name tag branch (:800-810) is
 * unreachable because step 5 puts the name tag into her hand. 1.21.1 handles name tags before
 * {@code mobInteract}; {@link CompanionInteractEvents} restores the original order.
 *
 * <p>1.7.10 ran {@code entityInit} inside the {@code Entity} constructor, before the field initialisers
 * of this class, and rolled {@code which_girl}/{@code which_wet_girl}/{@code voice} there a first time;
 * the constructor rolled them again into the fields. The DataWatcher therefore held the first roll, the
 * fields the second, and the server pushed the second voice into DW 21 at the first force sync. This
 * class keeps that: {@link #defineSynchedData} runs at the same point and the fields carry no
 * initialisers.
 */
public class Girlfriend extends TamableAnimal implements RangedAttackMob, AttackableNonMob, LegacyArmor {

    /** DataWatcher 20: dry outfit 0..40. */
    private static final EntityDataAccessor<Integer> DATA_WHICH_GIRL = SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 21: voice 0..9. */
    private static final EntityDataAccessor<Integer> DATA_VOICE = SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 22: bikini 0..17. */
    private static final EntityDataAccessor<Integer> DATA_WHICH_WET_GIRL = SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 23: voice_enable. */
    private static final EntityDataAccessor<Integer> DATA_VOICE_ENABLE = SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 24: is_princess 0/1/2. */
    private static final EntityDataAccessor<Integer> DATA_IS_PRINCESS = SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);
    /** DataWatcher 25: feelingBetter. */
    private static final EntityDataAccessor<Integer> DATA_FEELING_BETTER = SynchedEntityData.defineId(Girlfriend.class, EntityDataSerializers.INT);

    /** {@code setSize(2.5f, 8.0f)} (:121-123); the small size is the entity type's 0.5 x 1.6 (:120). */
    private static final EntityDimensions VALENTINE_SIZE = EntityDimensions.scalable(2.5f, 8.0f);

    /** girlfriend0..40.png (:1137-1177). */
    private static final ResourceLocation[] DRY_TEXTURES = CompanionSupport.textures("girlfriend", 41);
    /** girlfriendv.png (:1178). */
    private static final ResourceLocation VALENTINE_TEXTURE = CompanionSupport.texture("girlfriendv");
    /** bikini0..17.png (:1179-1196). */
    private static final ResourceLocation[] WET_TEXTURES = CompanionSupport.textures("bikini", 18);
    /** FrogPrincess.png, FrogPrincess2.png (:1197-1198), lower-cased by the asset generator. */
    private static final ResourceLocation PRINCESS_TEXTURE_1 = CompanionSupport.texture("frogprincess");
    private static final ResourceLocation PRINCESS_TEXTURE_2 = CompanionSupport.texture("frogprincess2");

    public int which_girl;
    public int which_wet_girl;
    public int wet_count;
    private int auto_heal;
    private int force_sync;
    private int fight_sound_ticker;
    private int taunt_sound_ticker;
    private int had_target;
    private int voice;
    private int is_princess;
    public MyEntityAIDance Dance;
    private float moveSpeed;
    private int voice_enable;
    public int passenger;
    public int feelingBetter;
    /**
     * {@code EntityLivingBase.attackTime} ({@code field_70724_aR}). 1.21.1 has no such field. The
     * 1.7.10 base class counted it down once per tick in {@code onEntityUpdate} whenever it was above
     * zero (bytecode of {@code sv.C()}, reference client jar) and saved it as {@code AttackTime}; the
     * melee code below counts it down a second time. Both are kept, so a swing lands every 13 ticks,
     * not every 25.
     */
    private int attackTime;

    public Girlfriend(final EntityType<? extends Girlfriend> type, final Level par1World) {
        super(type, par1World);
        this.which_girl = 0;
        this.which_wet_girl = 0;
        this.wet_count = 0;
        this.auto_heal = 200;
        this.force_sync = 50;
        this.fight_sound_ticker = 0;
        this.taunt_sound_ticker = 0;
        this.had_target = 0;
        this.voice = 0;
        this.is_princess = 0;
        this.Dance = null;
        this.moveSpeed = 0.3f;
        this.voice_enable = 1;
        this.passenger = 0;
        this.feelingBetter = 0;
        this.which_girl = this.random.nextInt(41);
        this.which_wet_girl = this.random.nextInt(18);
        this.voice = this.random.nextInt(10);
        // setSize(0.5f, 1.6f), on Valentine's Day setSize(2.5f, 8.0f) (:120-123): getDefaultDimensions.
        this.refreshDimensions();
        // isImmuneToFire (:124) is EntityType.Builder.fireImmune(); fireResistance = 100 (:125) is
        // getFireImmuneTicks().
        // PORT: getNavigator().setAvoidsWater(false) (:126) - the 1.21.1 counterpart is a water path
        // malus of 0 (vanilla default 8).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setSitting(false);
        // PORT: goals are added here on both sides, like the 1.7.10 constructor did, not in
        // registerGoals() (server only): getAmbientSound reads Dance on the client too.
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 1.4f, 12.0f, 1.5f));
        // Tempt, watch closest and look idle are the 1.7.10 vanilla goals from entity.ai (W06 fix wave), not 1.21.1's
        // TemptGoal, LookAtPlayerGoal and RandomLookAroundGoal: those roll on every second tick instead of every third
        // and calm down after 100 ticks instead of 300. Item.getItemFromBlock(Blocks.red_flower) matched every
        // metadata, the nine flowers of CompanionSupport.isRedFlower; EntityAITempt reads the main hand only.
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.25, CompanionSupport::isRedFlower, false));
        this.Dance = new MyEntityAIDance(this);
        this.goalSelector.addGoal(3, this.Dance);
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        // EntityAIPanic: runs while a revenge target is set (see LegacyPanic.legacyPanic).
        this.goalSelector.addGoal(6, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(7, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(9, new EntityAILookIdle(this));
        // EntityAIOpenDoor needed getNavigator().getCanBreakDoors(), which nothing here sets; OpenDoorGoal
        // likewise needs canOpenDoors() - both never run, as in the original.
        this.goalSelector.addGoal(10, new OpenDoorGoal(this, true));
        // :139; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(11, new EntityAIMoveIndoors(this));
        this.targetSelector.addGoal(1, new MyValentineTarget(this, Player.class, 16.0f, 0, true, true));
        this.targetSelector.addGoal(2, new MyValentineTarget(this, Boyfriend.class, 16.0f, 0, true, true));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(2, new MyEntityAINearestAttackableTarget(this, Creeper.class, 20.0f, 0, true, true,
                    MyEntityAINearestAttackableTarget.MOB_SELECTOR));
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            // EntityLiving.class is Mob.class.
            this.targetSelector.addGoal(3, new MyEntityAINearestAttackableTarget(this, Mob.class, 15.0f, 0, true, true,
                    MyEntityAINearestAttackableTarget.MOB_SELECTOR));
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(4, new MyEntityAIJealousy(this, Girlfriend.class, 6.0f, 5, true));
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(5, new MyEntityAIJealousy(this, Girlfriend.class, 3.0f, 15, true));
        }
        // experienceValue = 0 (:154) is never read: 1.7.10 EntityAnimal.getExperiencePoints (wf.e) returns
        // 1 + rand.nextInt(3) and Girlfriend does not override it - the inherited Animal.getBaseExperienceReward.
        // applyEntityAttributes (:177-183) ran in the EntityLivingBase constructor, while feelingBetter
        // was still 0, followed by setHealth(getMaxHealth()).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * Attribute set for {@code EntityAttributeCreationEvent}: {@code applyEntityAttributes} (:177-183).
     * The instance overrides the health base in the constructor (800 on Valentine's Day).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    /** {@code entityInit} (:157-175). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        this.which_girl = this.random.nextInt(41);
        builder.define(DATA_WHICH_GIRL, this.which_girl);
        this.wet_count = 0;
        this.which_wet_girl = this.random.nextInt(18);
        builder.define(DATA_WHICH_WET_GIRL, this.which_wet_girl);
        this.voice = this.random.nextInt(10);
        builder.define(DATA_VOICE, this.voice);
        // voice_enable, is_princess and feelingBetter are still 0 here, exactly as in 1.7.10.
        builder.define(DATA_VOICE_ENABLE, this.voice_enable);
        builder.define(DATA_IS_PRINCESS, this.is_princess);
        builder.define(DATA_FEELING_BETTER, this.feelingBetter);
        this.auto_heal = 200;
        this.force_sync = 50;
        this.fight_sound_ticker = 0;
        this.taunt_sound_ticker = 0;
        this.had_target = 0;
        // setSitting(false) (:174): the flags are not built yet; the constructor repeats it.
    }

    /** {@code getTotalArmorValue()} (:185-200) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return CompanionSupport.totalArmorValue(this);
    }

    /** {@code onUpdate} (:202-224): speed attribute, then the hoverboard seat behind the owner. */
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
                    // PORT: limbSwingAmount = limbSwing = 0. WalkAnimationState exposes the speed only; the
                    // swing position cannot be reset, so the legs freeze mid-stride instead of in the rest pose.
                    this.walkAnimation.setSpeed(0.0f);
                    this.fallDistance = 0.0f;
                    this.passenger = 1;
                }
            }
        }
    }

    /** The {@code attackTime} countdown of 1.7.10 {@code EntityLivingBase.onEntityUpdate}, see {@link #attackTime}. */
    @Override
    public void baseTick() {
        super.baseTick();
        if (this.attackTime > 0) {
            --this.attackTime;
        }
    }

    /** {@code writeEntityToNBT} (:226-234), preceded by the {@code AttackTime} tag of {@code EntityLivingBase}. */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putShort("AttackTime", (short) this.attackTime);
        par1NBTTagCompound.putInt("GirlType", this.getTameSkin());
        par1NBTTagCompound.putInt("WetGirlType", this.getWetTameSkin());
        par1NBTTagCompound.putInt("GirlVoice", this.entityData.get(DATA_VOICE));
        par1NBTTagCompound.putInt("GirlVoiceEnable", this.entityData.get(DATA_VOICE_ENABLE));
        par1NBTTagCompound.putInt("IsPrincess", this.entityData.get(DATA_IS_PRINCESS));
        par1NBTTagCompound.putInt("feelingBetter", this.entityData.get(DATA_FEELING_BETTER));
    }

    /** {@code readEntityFromNBT} (:236-251). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.attackTime = par1NBTTagCompound.getShort("AttackTime");
        this.setTameSkin(this.which_girl = par1NBTTagCompound.getInt("GirlType"));
        this.setWetTameSkin(this.which_wet_girl = par1NBTTagCompound.getInt("WetGirlType"));
        this.voice = par1NBTTagCompound.getInt("GirlVoice");
        this.entityData.set(DATA_VOICE, this.voice);
        this.voice_enable = par1NBTTagCompound.getInt("GirlVoiceEnable");
        this.entityData.set(DATA_VOICE_ENABLE, this.voice_enable);
        this.is_princess = par1NBTTagCompound.getInt("IsPrincess");
        this.entityData.set(DATA_IS_PRINCESS, this.is_princess);
        this.feelingBetter = par1NBTTagCompound.getInt("feelingBetter");
        this.entityData.set(DATA_FEELING_BETTER, this.feelingBetter);
        if (OreSpawn.valentines_day != 0 && this.feelingBetter != 0) {
            this.refreshDimensions();
        }
    }

    /**
     * {@code setSize} as dimensions: 2.5 x 8.0 while it is Valentine's Day and she is not feeling better,
     * otherwise the type's 0.5 x 1.6. The original only ever shrank her (reconciliation, load, client
     * sync); the size never grows back because {@code feelingBetter} never returns to 0.
     */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        if (OreSpawn.valentines_day != 0 && this.feelingBetter == 0) {
            return VALENTINE_SIZE;
        }
        return super.getDefaultDimensions(pose);
    }

    /** {@code updateAITick} (:253-308): the melee half of her fighting. */
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
        if (this.level().random.nextInt(200) == 1) {
            this.setTarget((LivingEntity) null);
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
                                    CompanionSupport.playSoundAtEntity(this, ModSounds.O_FIGHT.get(), 0.5f, this.getSoundPitch());
                                }
                                this.fight_sound_ticker = 3;
                            }
                            this.had_target = 1;
                        }
                    } else if (this.distanceTo(victim) < 7.0f && !CompanionSupport.isItem(stack, "ultimatebow")) {
                        --this.taunt_sound_ticker;
                        if (this.taunt_sound_ticker <= 0) {
                            if (!this.level().isClientSide && this.voice_enable != 0) {
                                CompanionSupport.playSoundAtEntity(this, ModSounds.O_TAUNT.get(), 0.5f, this.getSoundPitch());
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
                        CompanionSupport.playSoundAtEntity(this, ModSounds.O_WOOHOO.get(), 0.4f, this.getSoundPitch());
                    }
                }
            }
        }
    }

    /** Sets the field only; DataWatcher 24 follows at the next force sync (:310-312). */
    public void setPrincess(final int par1) {
        this.is_princess = par1;
    }

    /** {@code getTexture} (:314-508), read by {@code RenderGirlfriend.getTextureLocation}. */
    public ResourceLocation getTexture() {
        if (OreSpawn.valentines_day != 0 && this.feelingBetter == 0) {
            return VALENTINE_TEXTURE;
        }
        if (this.wet_count <= 0) {
            final int txture = this.getTameSkin();
            if (this.is_princess == 1) {
                return PRINCESS_TEXTURE_1;
            }
            if (this.is_princess == 2) {
                return PRINCESS_TEXTURE_2;
            }
            if (txture >= 0 && txture <= 40) {
                return DRY_TEXTURES[txture];
            }
        } else {
            final int temp = this.getWetTameSkin();
            if (temp >= 0 && temp <= 17) {
                return WET_TEXTURES[temp];
            }
        }
        // PORT: the original returned null here and bindTexture(null) crashed (R18 case 1); only an
        // out-of-range index from foreign NBT reaches this line.
        return DRY_TEXTURES[0];
    }

    public int getTameSkin() {
        return this.entityData.get(DATA_WHICH_GIRL);
    }

    public int getVoice() {
        return this.entityData.get(DATA_VOICE);
    }

    public void setTameSkin(final int par1) {
        this.entityData.set(DATA_WHICH_GIRL, par1);
        this.which_girl = par1;
    }

    public int getWetTameSkin() {
        return this.entityData.get(DATA_WHICH_WET_GIRL);
    }

    public void setWetTameSkin(final int par1) {
        this.entityData.set(DATA_WHICH_WET_GIRL, par1);
        this.which_wet_girl = par1;
    }

    // isAIEnabled (:532-534): every 1.21.1 mob runs goals.
    // canBreatheUnderwater (:536-538): final in 1.21.1, read from #minecraft:can_breathe_under_water.

    /**
     * {@code fall} (:540-552): damage {@code ceil(distance - 3)} capped at 3, with its own sounds, no
     * rider propagation, no block fall sound and no {@code LivingFallEvent} - the override replaced the
     * whole 1.7.10 method, Forge hook included. PORT: the 1.21.1 multiplier (hay bale, bed) did not
     * exist and is ignored, like the safe-fall attribute.
     */
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

    /** {@code fireResistance = 100} (:125). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    public int mygetMaxHealth() {
        if (OreSpawn.valentines_day != 0 && this.feelingBetter == 0) {
            return 800;
        }
        return 80;
    }

    /** {@code onLivingUpdate} (:561-597). */
    @Override
    public void aiStep() {
        // EntityAnimal did not advance the swing; the original called it itself (:562), as Monster does.
        this.updateSwingTime();
        super.aiStep();
        if (this.isInWater() || this.isInLava()) {
            this.wet_count = 500;
        } else if (this.wet_count > 0) {
            --this.wet_count;
        }
        --this.auto_heal;
        if (this.auto_heal <= 0) {
            if (this.mygetMaxHealth() > this.getGirlfriendHealth()) {
                this.heal(1.0f);
            }
            this.auto_heal = 100;
        }
        --this.force_sync;
        if (this.force_sync <= 0) {
            this.force_sync = 20;
            if (!this.level().isClientSide) {
                this.entityData.set(DATA_VOICE, this.voice);
                this.entityData.set(DATA_VOICE_ENABLE, this.voice_enable);
                this.entityData.set(DATA_IS_PRINCESS, this.is_princess);
                this.entityData.set(DATA_FEELING_BETTER, this.feelingBetter);
                this.setSitting(this.isSitting());
            } else {
                this.voice = this.getVoice();
                this.voice_enable = this.entityData.get(DATA_VOICE_ENABLE);
                // PORT: the original client never read DataWatcher 24 back, so both frog princess
                // textures stayed invisible (the server synced the value, the renderer read the stale
                // client field). R16 wants all 62 variants and R18 case 4 covers a value the client never
                // sees; Boyfriend.java:508 reads its twin exactly like this.
                this.is_princess = this.entityData.get(DATA_IS_PRINCESS);
                final int nowfeeling = this.entityData.get(DATA_FEELING_BETTER);
                if (nowfeeling != this.feelingBetter && nowfeeling != 0) {
                    this.feelingBetter = nowfeeling;
                    this.refreshDimensions();
                }
            }
        }
    }

    /**
     * Also read by {@code GirlfriendOverlayGui} through {@code client.gui.GirlfriendOverlayTarget.select}.
     */
    public int getGirlfriendHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code interact} (:603-849), checked in the original order. The original ran on both sides; so does
     * {@code mobInteract}. A {@code true} return is {@code sidedSuccess}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :604-608 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (!var2.isEmpty() && (CompanionSupport.isRedFlower(var2) || var2.is(ModItems.CRYSTAL_FLOWER_RED.get()))
                && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(3) == 0) {
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
        if (this.isTame() && !var2.isEmpty() && (var2.is(Items.DANDELION) || var2.is(ModItems.CRYSTAL_FLOWER_YELLOW.get()))
                && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                if (this.wet_count > 0 || this.isInWater() || this.isInLava()) {
                    ++this.which_wet_girl;
                    if (this.which_wet_girl > 17) {
                        this.which_wet_girl = 0;
                    }
                    this.setWetTameSkin(this.which_wet_girl);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    if (this.isInWater() || this.isInLava()) {
                        this.wet_count = 500;
                    }
                } else {
                    ++this.which_girl;
                    if (this.which_girl > 40) {
                        this.which_girl = 0;
                    }
                    this.setTameSkin(this.which_girl);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
            }
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            // ItemFood is an item with a food component; func_150905_g (getHealAmount) is nutrition.
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
                        if (itm == ModItems.PINK_HELMET.get() || itm == ModItems.TIGERSEYE_HELMET.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 4);
                            CompanionSupport.setCurrentItemOrArmor(this, 4, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                        if (itm == ModItems.PINK_CHEST.get() || itm == ModItems.TIGERSEYE_CHEST.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 3);
                            CompanionSupport.setCurrentItemOrArmor(this, 3, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                        if (itm == ModItems.PINK_LEGGINGS.get() || itm == ModItems.TIGERSEYE_LEGGINGS.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 2);
                            CompanionSupport.setCurrentItemOrArmor(this, 2, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                        if (itm == ModItems.PINK_BOOTS.get() || itm == ModItems.TIGERSEYE_BOOTS.get()) {
                            final ItemStack v4 = CompanionSupport.getEquipmentInSlot(this, 1);
                            CompanionSupport.setCurrentItemOrArmor(this, 1, var2);
                            CompanionSupport.setCurrentItemOrArmor(this, 0, v4);
                        }
                    }
                }
            }
            return success;
        }
        // Original bug kept (R18): no owner check, so only a stranger ever gets here and takes her over.
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DIAMOND_BLOCK) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            this.setSitting(false);
            this.setTame(true, false);
            this.setOwnerUUID(par1EntityPlayer.getUUID());
            this.spawnTamingParticles(true);
            this.level().broadcastEntityEvent(this, (byte) 7);
            CompanionSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        // Original bug kept (R18): unreachable, the branch above already took the owner's name tag.
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
                healthMessage = String.format("I have %d health. Thank you for asking! xoxo", this.getGirlfriendHealth());
                par1EntityPlayer.sendSystemMessage(Component.literal(healthMessage));
            }
            return success;
        }
        // super.interact: EntityAnimal breeding with wheat (isFood), pointless without a child (:848).
        return super.mobInteract(par1EntityPlayer, hand);
    }

    /** {@code isWheat} (:851-853): no caller and no vanilla override in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && CompanionSupport.isRedFlower(par1ItemStack);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat, since {@code isWheat} overrides nothing. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code canDespawn} (:855-857). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getLivingSound} (:859-900). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting() || this.voice_enable == 0) {
            return null;
        }
        if (this.Dance.is_dancing != 0) {
            return null;
        }
        if (this.random.nextInt(11) != 1) {
            return null;
        }
        final Entity victim = this.getTarget();
        if (victim != null) {
            return null;
        }
        if (this.isInWater() || this.isInLava()) {
            return ModSounds.O_WATER.get();
        }
        if (this.random.nextInt(4) != 0) {
            if (this.getY() < 60.0) {
                return null;
            }
            if (this.level().isThundering()) {
                return ModSounds.O_THUNDER.get();
            }
            if (this.level().isRaining()) {
                // "orespawn:o_rain" names no sound in the jar's sounds.json (manifest sounds): the
                // original was silent here, and so is the port.
                return null;
            }
            // :886 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
            if (!this.level().isDay() && this.level().canSeeSky(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())))) {
                if (this.level().random.nextInt(3) == 0) {
                    return ModSounds.O_DARK.get();
                }
                return null;
            }
        }
        if (!this.isTame()) {
            return null;
        }
        if (this.mygetMaxHealth() > this.getHealth() || (OreSpawn.valentines_day != 0 && this.feelingBetter == 0)) {
            return ModSounds.O_HURT.get();
        }
        return ModSounds.O_HAPPY.get();
    }

    /** {@code getHurtSound} (:902-907). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        if (this.voice_enable == 0) {
            return null;
        }
        return ModSounds.O_OW.get();
    }

    /** {@code getDeathSound} (:909-911). */
    @Override
    protected SoundEvent getDeathSound() {
        return this.isTame() ? ModSounds.O_DEATH_GIRLFRIEND.get() : ModSounds.O_DEATH_SINGLE.get();
    }

    /** {@code getSoundVolume} (:913-915). */
    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    /** {@code dropItemRand} (:921-924): the shared {@code OreSpawnRand}, not the entity's random. */
    private void dropItemRand(final Item index, final int par1) {
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /**
     * 1.7.10 {@code onDeath} dropped {@code dropFewItems} first and then the vanilla equipment roll
     * ({@code dropEquipment}, 8.5 % per slot on a player kill) - {@code Mob.dropCustomDeathLoot} is that
     * roll. {@code getDropItem} (:917-919) is unused because {@code dropFewItems} is overridden.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:926-981). {@code dropItem(item, n)} builds a fresh stack, so the equipment
     * comes out without damage, enchantments or name - original behaviour, kept (R18, like BandP).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        }
        final Item v6 = ModItems.RED_HEELS.get();
        final Item v7 = ModItems.BLACK_HEELS.get();
        final Item v8 = ModItems.SLIPPERS.get();
        final Item v9 = ModItems.BOOTS.get();
        var3 = this.random.nextInt(16);
        var3 += 4;
        for (int var5 = 0; var5 < var3; ++var5) {
            this.spawnAtLocation(new ItemStack(v6, 1));
        }
        var3 = this.random.nextInt(16);
        var3 += 4;
        for (int var5 = 0; var5 < var3; ++var5) {
            this.spawnAtLocation(new ItemStack(v7, 1));
        }
        var3 = this.random.nextInt(16);
        var3 += 4;
        for (int var5 = 0; var5 < var3; ++var5) {
            this.spawnAtLocation(new ItemStack(v8, 1));
        }
        var3 = this.random.nextInt(16);
        var3 += 4;
        for (int var5 = 0; var5 < var3; ++var5) {
            this.spawnAtLocation(new ItemStack(v9, 1));
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

    /** {@code attackEntityWithRangedAttack} (:983-1017): Ultimate Bow arrow or a thrown shoe. */
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
                // LegacyArrow keeps the 1.7.10 knockbackStrength field and reads it on a hit.
                var8.setKnockbackStrength(var9);
            }
            if (CompanionSupport.enchantmentLevel(this.level(), it, Enchantments.FLAME) > 0) {
                // setFire(100) counted seconds.
                var8.igniteForSeconds(100.0f);
            }
            it.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            CompanionSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f,
                    1.0f / (this.level().random.nextFloat() * 0.4f + 1.2f) + 0.5f);
            var8.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            this.level().addFreshEntity(var8);
        } else {
            final Shoes var10 = new Shoes(this.level(), this, 2 + this.random.nextInt(4));
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
     * {@code attackTargetEntityWithCurrentItem} (:1023-1064), the player attack formula. The weapon
     * reaches the damage through the attack-damage attribute: 1.7.10 {@code EntityLivingBase.onUpdate}
     * applied the held item's attribute modifiers (bytecode {@code sv.h()}), as 1.21.1 does. PORT: a sword
     * adds {@code 3 + tier bonus} here (R6) where 1.7.10 added {@code 4 + material damage}, one point less.
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
            // EntityLiving is Mob: players get neither the enchantment bonus nor enchantment knockback.
            if (par1Entity instanceof Mob && this.level() instanceof ServerLevel serverLevel) {
                // getEnchantmentModifierLiving returned the bonus; modifyDamage returns base plus bonus.
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
                    // PORT: nextInt(bound <= 0) threw in 1.7.10 as it does now (weakness II and up); the
                    // crash is skipped (R18 case 1), the bonus is then 0.
                    final int bound = (int) var2 / 2 + 2;
                    if (bound > 0) {
                        var2 += this.random.nextInt(bound);
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

    /** {@code getSoundPitch} (:1066-1068). */
    protected float getSoundPitch() {
        return (this.voice - 5) * 0.02f + 1.0f;
    }

    /** 1.21.1 reads the living, hurt and death sound pitch from here. */
    @Override
    public float getVoicePitch() {
        return this.getSoundPitch();
    }

    /** {@code createChild} (:1070-1072). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** {@code attackEntityWithRangedAttack(entity, float)} (:1074-1076). */
    @Override
    public void performRangedAttack(final LivingEntity entityliving, final float f) {
        this.attackEntityWithRangedAttack(entityliving);
    }

    /**
     * {@code attackEntityFrom} (:1078-1115): every hit capped at 10, cactus ignored, suffocation ignored
     * on Valentine's Day, and the Rose Sword reconciliation. The cap lands before the R5 armor
     * replacement, like the 1.7.10 cap before {@code damageEntity}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float p2 = par2;
        if (p2 > 10.0f) {
            p2 = 10.0f;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            if (par1DamageSource.is(DamageTypes.IN_WALL) && OreSpawn.valentines_day != 0) {
                return ret;
            }
            if (OreSpawn.valentines_day != 0 && !this.level().isClientSide && this.feelingBetter == 0) {
                final Entity e = par1DamageSource.getEntity();
                if (e != null && e instanceof Player) {
                    final Player eb = (Player) e;
                    final ItemStack ist = eb.getMainHandItem();
                    if (!ist.isEmpty()) {
                        final Item it = ist.getItem();
                        if (it == ModItems.ROSE_SWORD.get()) {
                            if (this.level().random.nextInt(4) == 1) {
                                this.feelingBetter = 1;
                                this.setTarget((LivingEntity) null);
                                this.refreshDimensions();
                                // PORT: 1.21.1 clamps the current health to the new maximum right here;
                                // 1.7.10 left health above 80 until the next setHealth.
                                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
                                for (int morelove = this.level().random.nextInt(10), i = 0; i < 10 + morelove; ++i) {
                                    this.dropItemRand(ModItems.HEART.get(), 1);
                                }
                            } else {
                                this.dropItemRand(ModItems.HEART.get(), 1);
                            }
                        }
                    }
                }
            }
            ret = super.hurt(par1DamageSource, p2);
        }
        return ret;
    }

    /**
     * Spawn predicate for {@code RegisterSpawnPlacementsEvent}: {@code getCanSpawnHere} (:1117-1134).
     * PORT: {@code super.getCanSpawnHere()} was 1.7.10 {@code EntityAnimal} (grass below, light above 8,
     * free space); {@link Animal#checkAnimalSpawnRules} is its 1.21.1 successor (#animals_spawnable_on,
     * light above 8), the free-space test stays with the spawner.
     */
    public static boolean checkGirlfriendSpawnRules(final EntityType<Girlfriend> type, final ServerLevelAccessor level,
            final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (CompanionSupport.spawnerNearby(level, pos, OreSpawn.MOD_ID + ":girlfriend")) {
            return true;
        }
        return Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: one synced flag; 1.21.1 splits it into order and pose. */
    public void setSitting(final boolean sitting) {
        this.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: no override in the original (Girlfriend.java:20, {@code extends EntityTameable}), and 1.7.10
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
