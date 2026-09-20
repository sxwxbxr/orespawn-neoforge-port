package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.Hydrolisc;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.TrooperBug} (TrooperBug.java:18-558): {@code jumpy_bug} ("Jumpy Bug"), the boss-sized
 * swamp and mesa beetle that leaps at its prey and calls Spit Bugs (verhalten/entity-13.md). Registered as "Jumpy Bug"
 * 64/1/false, {@code EntityMob} in {@code ambient} lists.
 *
 * <p>Values: health, attack and defense from {@link MobStats#TrooperBug_stats()} (200/20/15), speed 0.4 re-set every
 * tick (:32, :77), XP 150 (:35), {@code fireResistance} 100 (:36), not fire immune (:37). After every accepted hit 20
 * ticks without damage ({@code hurt_timer}, :369-374); cactus and fall damage are ignored. Heals 1 with 1/150 per AI
 * tick. While airborne the path is dropped (:79-81). DataWatcher 20 = attacking, no NBT; {@code hurt_timer} is not saved.
 *
 * <p>Not carried over: {@code force_sync} (:22, :30, :62) is never read; {@code getDropItem} (:161-163) is dead because
 * {@code dropFewItems} is overridden; {@code initCreature} (:343-344) overrides nothing; {@code interact} (:346-348) is
 * {@code Mob.mobInteract}'s {@code PASS}; {@code isAIEnabled}, {@code onLivingUpdate}, {@code canDespawn} are the 1.21.1
 * defaults.
 */
public class TrooperBug extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:50): antennae, arms and jaw of {@code ModelTrooperBug}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TrooperBug.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side note pad (:51-61); {@code ModelTrooperBug} does not use it. Never synchronised or saved. */
    private RenderInfo renderdata;
    private int hurt_timer;
    private float moveSpeed;

    /** {@code TrooperBug(World)} (:26-46) with {@code entityInit} (:48-63) and {@code applyEntityAttributes} (:65-70). */
    public TrooperBug(final EntityType<? extends TrooperBug> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.hurt_timer = 0;
        this.moveSpeed = 0.4f;
        // setSize(3.0f, 3.5f) (:33) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:34) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 150;
        // fireResistance = 100 (:36) is getFireImmuneTicks; isImmuneToFire = false (:37) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.TrooperBug_stats().attack());
        this.setHealth(this.getMaxHealth());
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(0.9, false) walked to the doors of a 1.7.10 village; villages are POI-based in
        // 1.21.1, so the vanilla MoveThroughVillageGoal (not only at night, POI distance 4 as the zombie, no doors) stands in.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 0.8999999761581421, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:65-70): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.4f)
                .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    /** {@code entityInit} (:48-50). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 100} (:36). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:76-82), both sides: while {@code isAirBorne} ({@code hasImpulse}) the path is dropped. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.hasImpulse) {
            this.getNavigation().stop();
        }
    }

    /** {@code mygetMaxHealth} (:84-86). */
    public int mygetMaxHealth() {
        return MobStats.TrooperBug_stats().health();
    }

    /** {@code getRenderInfo} (:88-90). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:92-101). */
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

    /** {@code getTotalArmorValue} (:103-105), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.TrooperBug_stats().defense();
    }

    /**
     * {@code jump} (:115-122): no vanilla jump, only 1.15 upward and a forward shove of 0.2-0.65 along the head yaw on the
     * world random. {@code isAirBorne} is {@code hasImpulse}.
     *
     * <p>PORT: {@code this.posY += 1.5} is left out. 1.7.10 {@code Entity.moveEntity} rebuilt {@code posY} from the
     * unmoved bounding box later in the same {@code onLivingUpdate}, so the write never took effect (Frog, Cricket, W06).
     */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        final double motionY = m.y + 1.149999976158142;
        final float f = 0.2f + Math.abs(this.level().random.nextFloat() * 0.45f);
        final double motionX = m.x - f * Math.sin(Math.toRadians(this.getYHeadRot()));
        final double motionZ = m.z + f * Math.cos(Math.toRadians(this.getYHeadRot()));
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    /** {@code jumpAtEntity} (:124-132): 1.25 upward and a shove of 0.3-0.55 towards the target; same PORT as {@link #jumpFromGround}. */
    protected void jumpAtEntity(final LivingEntity e) {
        final Vec3 m = this.getDeltaMovement();
        final double motionY = m.y + 1.25;
        final float f = 0.3f + Math.abs(this.level().random.nextFloat() * 0.25f);
        final float d = (float) Math.atan2(e.getX() - this.getX(), e.getZ() - this.getZ());
        final double motionX = m.x + f * Math.sin(d);
        final double motionZ = m.z + f * Math.cos(d);
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    /** {@code getTrooperBugHealth} (:134-136). */
    public int getTrooperBugHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:138-143): clatter with 1/4 on the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0) {
            return ModSounds.CLATTER.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:145-147). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRUNCH.get();
    }

    /** {@code getDeathSound} (:149-151). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EMPERORSCORPION_DEATH.get();
    }

    /** {@code getSoundVolume} (:153-155). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:157-159). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code dropItemRand} (:165-173): up to four blocks off on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final ItemStack is) {
        ArthropodSupport.dropItemRand(this, is, 5);
    }

    /**
     * {@code dropFewItems} (:175-341), then {@code dropEquipment}: Jumpy Bug scale, item frame, 2-6 amethysts, and 1-5
     * rolls of {@code rand(14)} for the amethyst table (1, 12 and 13 give nothing; 0 falls through to the amethyst
     * block of case 2, R18). Looting and the recently-hit flag are ignored.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        final RandomSource rand = this.level().random;
        this.dropItemRand(new ItemStack(ModItems.JUMPY_BUG_SCALE.get(), 1));
        this.dropItemRand(new ItemStack(Items.ITEM_FRAME, 1));
        for (int i = 2 + rand.nextInt(5), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(new ItemStack(ModItems.AMETHYST.get(), 1));
        }
        for (int i = 1 + rand.nextInt(5), var4 = 0; var4 < i; ++var4) {
            final int var5 = rand.nextInt(14);
            switch (var5) {
                case 0:
                    // case 0: {} (:184) has no break and falls into case 2.
                case 2: {
                    this.dropItemRand(new ItemStack(ModItems.BLOCKAMETHYST.get(), 1));
                    break;
                }
                case 3: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_SWORD.get(), 1);
                    ArthropodSupport.enchantSword(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 4: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_SHOVEL.get(), 1);
                    ArthropodSupport.enchantTool(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 5: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_PICKAXE.get(), 1);
                    ArthropodSupport.enchantPickaxe(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 6: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_AXE.get(), 1);
                    ArthropodSupport.enchantTool(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 7: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_HOE.get(), 1);
                    ArthropodSupport.enchantTool(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 8: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_HELMET.get(), 1);
                    ArthropodSupport.enchantHelmet(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 9: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_CHEST.get(), 1);
                    ArthropodSupport.enchantBody(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 10: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_LEGGINGS.get(), 1);
                    ArthropodSupport.enchantBody(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                case 11: {
                    final ItemStack is = new ItemStack(ModItems.AMETHYST_BOOTS.get(), 1);
                    ArthropodSupport.enchantBoots(is, this.level());
                    this.dropItemRand(is);
                    break;
                }
                default:
                    break;
            }
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityAsMob} (:350-365): the vanilla mob hit, then a throw of 1.8 away and 0.2 up (0.4 against players
     * and removed targets).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 1.8;
        double inair = 0.2;
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
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
     * {@code attackEntityFrom} (:367-384): refused while {@code hurt_timer} runs; cactus and fall are ignored; every other
     * hit restarts the 20-tick window, and a mob attacker ({@code EntityLiving}, which 1.7.10 players were not) becomes
     * the target at once and makes the call report {@code true} whatever the damage did.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS) && !par1DamageSource.is(DamageTypes.FALL)) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 20;
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof Mob) {
                this.setTarget((LivingEntity) e);
                // setTarget(e) (:378) wrote EntityCreature.entityToAttack, which only the pre-task AI read.
                this.getNavigation().moveTo(e, 1.2);
                ret = true;
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
        if (rand.nextInt(5) == 0) {
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (rand.nextInt(10) == 1 && !this.hasImpulse) {
                    this.jumpAtEntity(e);
                } else if (this.distanceToSqr(e) < (5.0f + e.getBbWidth() / 2.0f) * (5.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (rand.nextInt(6) == 0 || rand.nextInt(7) == 1) {
                        this.doHurtTarget(e);
                        if (!this.level().isClientSide) {
                            if (rand.nextInt(3) == 1) {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.SCORPION_ATTACK.get(), 1.4f, 1.0f);
                            } else {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.CLATTER.get(), 1.0f, 1.0f);
                            }
                        }
                    }
                } else if (!this.hasImpulse) {
                    this.getNavigation().moveTo(e, 1.2);
                }
                if (rand.nextInt(30) == 1) {
                    // spawnCreature(world, "Spit Bug", ...) (:427, :439-448) is ItemSpawnEgg.spawnSomething: create, random
                    // yaw on the world random, add, play the ambient sound. No cap, SpitBugEnable is not read.
                    ItemSpawnEgg.spawnSomething(ModEntities.SPIT_BUG.get(), this.level(),
                            (this.getX() + e.getX()) / 2.0 + rand.nextInt(5) - rand.nextInt(5),
                            (this.getY() + e.getY()) / 2.0 + 1.01,
                            (this.getZ() + e.getZ()) / 2.0 + rand.nextInt(5) - rand.nextInt(5));
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (rand.nextInt(150) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /** {@code isSuitableTarget} (:450-495), checks in the original order. */
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
        if (par1EntityLiving instanceof Hydrolisc) {
            return false;
        }
        if (ArthropodSupport.isEnderReaper(par1EntityLiving)) {
            return false;
        }
        if (ArthropodSupport.isEnderKnight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EnderMan) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return false;
        }
        if (par1EntityLiving instanceof TrooperBug) {
            return false;
        }
        if (par1EntityLiving instanceof SpitBug) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:497-514): {@code expand(12, 7, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 12.0, 7.0, 12.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:516-518). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:520-522). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:524-557) - a valid light level, by day only when {@code rand(20) <= 1},
     * and air in x/z -2..1, y +1..+4.
     *
     * <p>PORT: the "Jumpy Bug" spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9); the
     * day roll uses the spawner's random.
     */
    public static boolean checkTrooperBugSpawnRules(final EntityType<TrooperBug> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel()) && random.nextInt(20) > 1) {
            return false;
        }
        return ArthropodSupport.isAllAir(level, pos, -2, 2, 1, 5);
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
