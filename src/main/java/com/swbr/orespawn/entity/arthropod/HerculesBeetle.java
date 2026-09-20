package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.insect.InsectSupport;
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
 * Port of {@code danger.orespawn.HerculesBeetle} (HerculesBeetle.java:17-477): {@code hercules_beetle}, the giant
 * hill beetle that attacks almost everything alive and tosses it into the air (verhalten/entity-09.md). Registered as
 * "Hercules Beetle" 64/1/false, {@code EntityMob} in {@code ambient} lists.
 *
 * <p>Values: health, attack and defense from {@link MobStats#HerculesBeetle_stats()} (250/30/19), speed 0.25 re-set every
 * tick (:27, :59), XP 200 (:30), fire immune with {@code fireResistance} 100 (:31-32). After every accepted hit 20 ticks
 * without damage ({@code hurt_timer}, :316-321); cactus is ignored. Heals 2 with 1/150 per AI tick. DataWatcher 20 =
 * attacking (jaws), no NBT; {@code hurt_timer} is not saved.
 *
 * <p>Not carried over: {@code interact} (:294-296) is {@code Mob.mobInteract}'s {@code PASS}; {@code getDropItem}
 * (:109-111) is dead because {@code dropFewItems} is overridden; {@code isAIEnabled}, {@code onLivingUpdate} and
 * {@code canDespawn} are the 1.21.1 defaults.
 */
public class HerculesBeetle extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:44): the jaws of {@code ModelHerculesBeetle} snap faster while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(HerculesBeetle.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private float moveSpeed;

    /** {@code HerculesBeetle(World)} (:23-40) with {@code entityInit} (:42-45) and {@code applyEntityAttributes} (:47-52). */
    public HerculesBeetle(final EntityType<? extends HerculesBeetle> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.hurt_timer = 0;
        this.moveSpeed = 0.25f;
        // setSize(3.25f, 2.75f) (:28) is the entity type's size (R9); isImmuneToFire (:32) is the type's fireImmune().
        // PORT: getNavigator().setAvoidsWater(true) (:29) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 200;
        this.TargetSorter = new GenericTargetSorter(this);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.HerculesBeetle_stats().attack());
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

    /** {@code applyEntityAttributes} (:47-52): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 30.0);
    }

    /** {@code entityInit} (:42-45). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 100} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:58-61), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:63-65). */
    public int mygetMaxHealth() {
        return MobStats.HerculesBeetle_stats().health();
    }

    /** {@code getTotalArmorValue} (:67-69), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.HerculesBeetle_stats().defense();
    }

    /**
     * {@code jump} (:79-83): the vanilla jump plus 0.25 upward.
     *
     * <p>PORT: {@code this.posY += 0.5} is left out. 1.7.10 {@code Entity.moveEntity} rebuilt {@code posY} from the
     * unmoved bounding box later in the same {@code onLivingUpdate}, so the write never took effect (Frog, Cricket, W06).
     */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.25, 0.0));
    }

    /** {@code getHerculesBeetleHealth} (:85-87). */
    public int getHerculesBeetleHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:89-91). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:93-95). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:97-99). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HERCULES_DEATH.get();
    }

    /** {@code getSoundVolume} (:101-103). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:105-107). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code dropItemRand} (:113-121): up to four blocks off on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final ItemStack is) {
        ArthropodSupport.dropItemRand(this, is, 5);
    }

    /**
     * {@code dropFewItems} (:123-292), then {@code dropEquipment}: Big Hammer, item frame, 4-11 beef, and 1-5 rolls of
     * {@code rand(20)} for the treasure table (0 and 12-19 give nothing). Looting and the recently-hit flag are ignored.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        final RandomSource rand = this.level().random;
        this.dropItemRand(new ItemStack(ModItems.BIG_HAMMER.get(), 1));
        this.dropItemRand(new ItemStack(Items.ITEM_FRAME, 1));
        for (int i = 4 + rand.nextInt(8), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(new ItemStack(Items.BEEF, 1));
        }
        for (int i = 1 + rand.nextInt(5), var4 = 0; var4 < i; ++var4) {
            final int var5 = rand.nextInt(20);
            switch (var5) {
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
                default:
                    break;
            }
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityAsMob} (:298-312): the vanilla mob hit, then a toss of 0.45 away and {@code 1.25 * |rand|} up
     * ({@code 2.5 * |rand|} against players and removed targets) on the world random.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 0.45;
        double inair = 1.25;
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair * Math.abs(this.level().random.nextFloat()), Math.sin(f3) * ks);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:314-330): refused while {@code hurt_timer} runs; cactus is ignored; every other hit
     * restarts the 20-tick window, and a mob attacker ({@code EntityLiving}, which 1.7.10 players were not) becomes the
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
            this.hurt_timer = 20;
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof Mob) {
                this.setTarget((LivingEntity) e);
                // setTarget(e) (:325) wrote EntityCreature.entityToAttack, which only the pre-task AI read.
                this.getNavigation().moveTo(e, 1.2);
            }
        }
        return ret;
    }

    /** {@code updateAITasks} (:332-377). */
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
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (5.0f + e.getBbWidth() / 2.0f) * (5.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (rand.nextInt(3) == 0 || rand.nextInt(4) == 1) {
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
            } else {
                this.setAttacking(0);
            }
        }
        if (rand.nextInt(150) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }
    }

    /** {@code isSuitableTarget} (:379-409), checks in the original order. */
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
        if (par1EntityLiving instanceof Creeper) {
            return false;
        }
        if (par1EntityLiving instanceof HerculesBeetle) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:411-428): {@code expand(16, 6, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 6.0, 16.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:430-432). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:434-436). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:438-476) - a valid light level, night, Y at least 50, air in x/z -2..1,
     * y +2..+4, and no other Hercules Beetle in the bounding box grown by (16, 6, 16).
     *
     * <p>PORT: the "Hercules Beetle" spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here
     * (catalogue 5.9); the bounding box is the type's box at the spawn position, because no entity exists yet.
     */
    public static boolean checkHerculesBeetleSpawnRules(final EntityType<HerculesBeetle> type, final ServerLevelAccessor level,
                                                        final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
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
        if (!ArthropodSupport.isAllAir(level, pos, -2, 2, 2, 5)) {
            return false;
        }
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 6.0, 16.0);
        return level.getEntitiesOfClass(HerculesBeetle.class, box).isEmpty();
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
