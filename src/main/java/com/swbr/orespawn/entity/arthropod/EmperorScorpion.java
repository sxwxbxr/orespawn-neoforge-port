package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.EmperorScorpion} (EmperorScorpion.java:18-553): {@code emperor_scorpion}, the desert
 * mini-boss that attacks nearly everything, poisons, throws its victims and calls Scorpions to its side
 * (verhalten/entity-07.md). Registered as "Emperor Scorpion" 64/1/false, {@code EntityMob} in an {@code ambient} list.
 *
 * <p>Values: health, attack and defense from {@link MobStats#EmperorScorpion_stats()} (350/35/20), speed 0.35 re-set
 * every tick (:30, :74), XP 200 (:33), fire immune with {@code fireResistance} 100 (:34-35). After every accepted hit
 * 30 ticks without damage ({@code hurt_timer}, :370-375, counted down in the AI step); cactus is ignored. Heals 2 with
 * 1/100 per AI tick. DataWatcher 20 = attacking, no NBT; {@code hurt_timer} is not saved.
 *
 * <p>Poison duration kept 1:1 with its bug (R18): {@code var2} is 6, and only EASY sets 8, because the NORMAL and HARD
 * checks sit inside the EASY branch (:345-353) - 120 ticks on Easy, 90 on every other difficulty.
 *
 * <p>Not carried over: {@code interact} (:335-337) is {@code Mob.mobInteract}'s {@code PASS}; {@code getDropItem}
 * (:139-141) is dead because {@code dropFewItems} is overridden; {@code isAIEnabled}, {@code onLivingUpdate},
 * {@code canDespawn} are the 1.21.1 defaults.
 */
public class EmperorScorpion extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:48): mandibles, claws and tail of {@code ModelEmperorScorpion}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EmperorScorpion.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side note pad of {@code ModelEmperorScorpion} (:49-59), never synchronised or saved. */
    private RenderInfo renderdata;
    private int hurt_timer;
    private float moveSpeed;

    /** {@code EmperorScorpion(World)} (:25-44) with {@code entityInit} (:46-60) and {@code applyEntityAttributes} (:62-67). */
    public EmperorScorpion(final EntityType<? extends EmperorScorpion> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.hurt_timer = 0;
        this.moveSpeed = 0.35f;
        // setSize(3.5f, 3.0f) (:31) is the entity type's size (R9); isImmuneToFire (:35) is the type's fireImmune().
        // PORT: getNavigator().setAvoidsWater(true) (:32) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 200;
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.EmperorScorpion_stats().attack());
        this.setHealth(this.getMaxHealth());
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(0.9, false) walked to the doors of a 1.7.10 village; villages are POI-based in
        // 1.21.1, so the vanilla MoveThroughVillageGoal (not only at night, POI distance 4 as the zombie, no doors) stands in.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 0.8999999761581421, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:62-67): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, 35.0);
    }

    /** {@code entityInit} (:46-48). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 100} (:34). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:73-76), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:78-80). */
    public int mygetMaxHealth() {
        return MobStats.EmperorScorpion_stats().health();
    }

    /** {@code getRenderInfo} (:82-84). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:86-95). */
    public void setRenderInfo(final RenderInfo r) {
        this.renderdata.rf1 = r.rf1;
        this.renderdata.rf2 = r.rf2;
        this.renderdata.rf3 = r.rf3;
        this.renderdata.rf4 = r.rf4;
        this.renderdata.ri1 = r.ri1;
        this.renderdata.ri2 = r.ri2;
        this.renderdata.ri3 = r.ri3;
        this.renderdata.ri4 = r.ri4;
    }

    /** {@code getTotalArmorValue} (:97-99), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.EmperorScorpion_stats().defense();
    }

    /**
     * {@code jump} (:109-113): the vanilla jump plus 0.25 upward.
     *
     * <p>PORT: {@code this.posY += 0.5} is left out. 1.7.10 {@code Entity.moveEntity} rebuilt {@code posY} from the
     * unmoved bounding box later in the same {@code onLivingUpdate}, so the write never took effect (Frog, Cricket, W06).
     */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.25, 0.0));
    }

    /** {@code getEmperorScorpionHealth} (:115-117). */
    public int getEmperorScorpionHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:119-121). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:123-125). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:127-129). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EMPERORSCORPION_DEATH.get();
    }

    /** {@code getSoundVolume} (:131-133). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:135-137). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropFewItems} (:153-333), then {@code dropEquipment}: scale, item frame, 4-8 obsidian, 4-11 beef, and 1-5
     * rolls of {@code rand(20)} for the treasure table (13-19 give nothing), each dropped up to four blocks off.
     * Looting and the recently-hit flag are ignored, as in the original.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        final RandomSource rand = this.level().random;
        this.dropItemRand(new ItemStack(ModItems.EMPEROR_SCORPION_SCALE.get(), 1));
        this.dropItemRand(new ItemStack(Items.ITEM_FRAME, 1));
        for (int i = 4 + rand.nextInt(5), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(new ItemStack(Items.OBSIDIAN, 1));
        }
        for (int i = 4 + rand.nextInt(8), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(new ItemStack(Items.BEEF, 1));
        }
        for (int i = 1 + rand.nextInt(5), var4 = 0; var4 < i; ++var4) {
            final int var5 = rand.nextInt(20);
            switch (var5) {
                case 0: {
                    this.dropItemRand(new ItemStack(ModItems.ULTIMATE_SWORD.get(), 1));
                    break;
                }
                case 1: {
                    this.dropItemRand(new ItemStack(Items.DIAMOND, 1));
                    break;
                }
                case 2: {
                    this.dropItemRand(new ItemStack(Items.DIAMOND_BLOCK, 1));
                    break;
                }
                case 3: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_SWORD, 1);
                    ArthropodSupport.enchantSword(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 4: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_SHOVEL, 1);
                    ArthropodSupport.enchantTool(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 5: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_PICKAXE, 1);
                    ArthropodSupport.enchantPickaxe(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 6: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_AXE, 1);
                    ArthropodSupport.enchantTool(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 7: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_HOE, 1);
                    ArthropodSupport.enchantTool(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 8: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_HELMET, 1);
                    ArthropodSupport.enchantHelmet(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 9: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_CHESTPLATE, 1);
                    ArthropodSupport.enchantBody(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 10: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_LEGGINGS, 1);
                    ArthropodSupport.enchantBody(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 11: {
                    final ItemStack is = new ItemStack(Items.DIAMOND_BOOTS, 1);
                    ArthropodSupport.enchantBoots(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 12: {
                    this.dropItemRand(new ItemStack(ModItems.ULTIMATE_BOW.get(), 1));
                    break;
                }
                default:
                    break;
            }
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropItemRand} (:143-151): up to four blocks off on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final ItemStack is) {
        ArthropodSupport.dropItemRand(this, is, 5);
    }

    /**
     * {@code attackEntityAsMob} (:339-366): the vanilla mob hit, then on a living target Poison with 1/3 and a throw of
     * 3.0 away and 0.2 up (0.4 against players and removed targets).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 3.0;
        double inair = 0.2;
        int var2 = 6;
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                if (this.level().getDifficulty() == Difficulty.EASY) {
                    var2 = 8;
                    // Unreachable as in the original (R18): the difficulty is EASY here.
                    if (this.level().getDifficulty() == Difficulty.NORMAL) {
                        var2 = 10;
                    } else if (this.level().getDifficulty() == Difficulty.HARD) {
                        var2 = 12;
                    }
                }
                if (this.level().random.nextInt(3) == 1) {
                    ((LivingEntity) par1Entity).addEffect(new MobEffectInstance(MobEffects.POISON, var2 * 15, 0));
                }
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:368-384): refused while {@code hurt_timer} runs; cactus is ignored; every other hit
     * restarts the 30-tick window, and a mob attacker ({@code EntityLiving}, which 1.7.10 players were not) becomes the
     * target at once.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 30;
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof Mob) {
                this.setTarget((LivingEntity) e);
                // setTarget(e) (:379) wrote EntityCreature.entityToAttack, which only the pre-task AI read.
                this.getNavigation().moveTo(e, 1.2);
            }
        }
        return ret;
    }

    /** {@code updateAITasks} (:386-437). */
    @Override
    protected void customServerAiStep() {
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        final RandomSource rand = this.level().random;
        if (rand.nextInt(4) == 0) {
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (rand.nextInt(100) == 0) {
                this.setTarget(null);
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (6.0f + e.getBbWidth() / 2.0f) * (6.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (rand.nextInt(4) == 0 || rand.nextInt(6) == 1) {
                        this.doHurtTarget(e);
                        if (!this.level().isClientSide) {
                            if (rand.nextInt(3) == 1) {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.SCORPION_ATTACK.get(), 1.4f, 1.0f);
                            } else {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.SCORPION_LIVING.get(), 1.0f, 1.0f);
                            }
                        }
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                }
                if (rand.nextInt(20) == 1) {
                    // spawnCreature(world, "Scorpion", ...) (:427, :439-448) is ItemSpawnEgg.spawnSomething: create, random
                    // yaw on the world random, add, play the ambient sound. No cap, ScorpionEnable is not read.
                    ItemSpawnEgg.spawnSomething(ModEntities.SCORPION.get(), this.level(),
                            (this.getX() + e.getX()) / 2.0 + rand.nextInt(5) - rand.nextInt(5),
                            (this.getY() + e.getY()) / 2.0 + 1.01,
                            (this.getZ() + e.getZ()) / 2.0 + rand.nextInt(5) - rand.nextInt(5));
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (rand.nextInt(100) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }
    }

    /** {@code isSuitableTarget} (:450-492), checks in the original order. */
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
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EnderMan) {
            return false;
        }
        if (ArthropodSupport.isEnderKnight(par1EntityLiving)) {
            return false;
        }
        if (ArthropodSupport.isEnderReaper(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return false;
        }
        if (par1EntityLiving instanceof Scorpion) {
            return false;
        }
        if (par1EntityLiving instanceof EmperorScorpion) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:494-511): {@code expand(24, 6, 24)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 24.0, 6.0, 24.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:513-515). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:517-519). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:521-552) - air in x/z -2..1, y +2..+4, a valid light level, night,
     * Y at least 50 and no other Emperor Scorpion in the bounding box grown by (20, 6, 20).
     *
     * <p>PORT: the "Emperor Scorpion" spawner found inside the air scan is {@link MobSpawnType#SPAWNER} here
     * (catalogue 5.9); the bounding box is the type's box at the spawn position, because no entity exists yet.
     */
    public static boolean checkEmperorScorpionSpawnRules(final EntityType<EmperorScorpion> type, final ServerLevelAccessor level,
                                                         final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!ArthropodSupport.isAllAir(level, pos, -2, 2, 2, 5)) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 6.0, 20.0);
        return level.getEntitiesOfClass(EmperorScorpion.class, box).isEmpty();
    }

    /** The override replaced {@code EntityCreature}'s path-weight test, which 1.21.1 asks here; the predicate holds the rule. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
