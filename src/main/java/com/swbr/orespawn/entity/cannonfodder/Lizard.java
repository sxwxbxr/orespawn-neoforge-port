package com.swbr.orespawn.entity.cannonfodder;

import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.List;
import javax.annotation.Nullable;
import com.swbr.orespawn.entity.sea.AttackSquid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Lizard} (Lizard.java:15-413, verhalten/entity-10.md): the crocodile-like
 * battle mob of rivers and swamps. It seeks water, hunts spiders, cave spiders, chickens and Attack Squids,
 * follows a player who fed it any dye for 3000-4999 ticks, pairs up with other lizards, and fights as a
 * {@link EntityCannonFodder} once activated (6 damage, one-in-eight swings).
 *
 * <p>Health 30 (:76-78), armour 5 always (:81-83, replaces the battle-mob 3/0), speed 0.3 set every tick
 * (:72), bite fixed 6 (:310-314), immune to cactus (:96), {@code fireResistance} 3 (:42), hitbox 1.5 x 1.25
 * (:39). {@code experienceValue = 15} (:41) is dead, see {@link Chipmunk}.
 *
 * <p>Original behaviour kept (R18): once activated every click in reach toggles guard duty in the base class,
 * so the dye branch is unreachable; a living attack target is kept without any suitability test, so whoever
 * hits the lizard - a player included - is chased (:362-365).
 */
public class Lizard extends EntityCannonFodder {

    /** DataWatcher 23: {@code attacking}, read by the model for the jaw and the tail. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Lizard.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    public boolean should_despawn;
    @Nullable
    private LivingEntity buddy;
    private int follow_time;
    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Lizard(World)} (:27-53). */
    public Lizard(final EntityType<? extends Lizard> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.should_despawn = true;
        this.buddy = null;
        this.follow_time = 0;
        this.moveSpeed = 0.3f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.moveSpeed = 0.3f;
        // setSize(1.5f, 1.25f) (:39) is the entity type size.
        // PORT: getNavigator().setAvoidsWater(false) (:40) - water path malus 0 (vanilla default 8), as Girlfriend.
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        // experienceValue = 15 (:41) is dead; fireResistance = 3 (:42) is getFireImmuneTicks; isImmuneToFire false.
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new EntityAITempt(this, 1.25, CannonFodderSupport::isLegacyDye, false));
        this.goalSelector.addGoal(4, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it
        // has not seen for 60 ticks (TargetGoal.mustSee is fixed to true there).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:55-62): health 30, speed 0.3, attack 6.0 (see Chipmunk for the speed). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    /** {@code entityInit} (:64-68). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code onUpdate} (:70-74). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:76-78). */
    public int mygetMaxHealth() {
        return 30;
    }

    /** {@code getTotalArmorValue} (:80-83). */
    @Override
    public int getTotalArmorValue() {
        return 5;
    }

    /** {@code fireResistance = 3} (:42). */
    @Override
    protected int getFireImmuneTicks() {
        return 3;
    }

    // isAIEnabled (:85-87); onLivingUpdate (:89-91) only called super.

    /**
     * {@code attackEntityFrom} (:93-104): cactus does nothing; every other hit lands, and a living source
     * becomes the attack target. The follow time ends on any hit, cactus included. Like the original this runs
     * on the client too, where {@code super.hurt} returns {@code false}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        final Entity e = par1DamageSource.getEntity();
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
            if (e != null && e instanceof LivingEntity) {
                this.setTarget((LivingEntity) e);
            }
        }
        this.follow_time = 0;
        return ret;
    }

    /** {@code getLivingSound} (:106-108). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:110-112). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:114-116). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:118-120). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:122-124). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:126-128): no drops. */
    @Nullable
    @Override
    protected Item getDropItem() {
        return null;
    }

    /**
     * {@code interact} (:130-161): the battle mob first; then any dye makes the player the buddy; every other
     * click clears the buddy - and still counts as handled.
     */
    @Override
    protected boolean legacyInteract(final Player par1EntityPlayer) {
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (super.legacyInteract(par1EntityPlayer)) {
            return true;
        }
        if (!var2.isEmpty() && CannonFodderSupport.isLegacyDye(var2) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.level().isClientSide) {
                this.buddy = par1EntityPlayer;
                this.follow_time = 3000 + this.level().random.nextInt(2000);
            }
            this.spawnTamingParticles(true);
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (!this.level().isClientSide) {
            this.buddy = null;
            this.follow_time = 0;
        }
        this.spawnTamingParticles(false);
        return true;
    }

    /**
     * {@code scan_it} (:163-244): the six faces of the shell {@code dx/dy/dz} around {@code x, y, z}, keeping the
     * water block closest by squared face distance. {@code Blocks.water}/{@code flowing_water} are one block in
     * 1.21.1; waterlogged blocks are not water blocks, as in 1.7.10.
     */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isWater(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWater(x - dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x - dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isWater(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWater(x + i, y - dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y - dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                if (this.isWater(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isWater(x + i, y + j, z - dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z - dz;
                        ++found;
                    }
                }
            }
        }
        return found != 0;
    }

    private boolean isWater(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /** {@code updateAITasks} (:246-308): after the battle-mob AI, follow time, water, healing, hunting, buddy. */
    @Override
    protected void updateAITasks() {
        if (this.isRemoved()) {
            return;
        }
        super.updateAITasks();
        if (this.follow_time > 0) {
            --this.follow_time;
            this.should_despawn = false;
        } else {
            this.should_despawn = true;
        }
        if (!this.isInWater() && this.level().random.nextInt(100) == 0) {
            this.closest = 99999;
            final boolean tx = false;
            this.tz = (tx ? 1 : 0);
            this.ty = (tx ? 1 : 0);
            this.tx = (tx ? 1 : 0);
            for (int i = 1; i < 14; ++i) {
                int j = i;
                if (j > 5) {
                    j = 5;
                }
                // :270 (int) posX, (int) posY - 1, (int) posZ -> Mth.floor (DECISIONS R20).
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) (this.ty - 1), (double) this.tz, 1.33);
            }
        }
        if (this.getHealth() < this.mygetMaxHealth() && this.level().random.nextInt(300) == 1) {
            this.heal(1.0f);
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(10) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.follow_time = 0;
                if (this.distanceToSqr(e) < 12.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                }
            } else {
                // isDead is the removed flag, not the health.
                if (this.buddy != null && !this.buddy.isRemoved() && this.level().random.nextInt(15) == 1) {
                    this.getNavigation().moveTo(this.buddy, 1.0);
                }
                this.setAttacking(0);
            }
        }
        if (this.buddy != null && !this.buddy.isRemoved() && this.follow_time > 0 && this.level().random.nextInt(20) == 1) {
            this.getNavigation().moveTo(this.buddy, 1.0);
        }
    }

    /** {@code attackEntityAsMob} (:310-314): a fixed 6 as mob damage, no knockback, no enchantments. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final float i = 6.0f;
        final boolean flag = par1Entity.hurt(this.damageSources().mobAttack(this), i);
        return flag;
    }

    /** {@code isSuitableTarget} (:316-348). */
    private boolean isSuitableTarget(final LivingEntity par1EntityLiving, final boolean par2) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
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
        if (par1EntityLiving instanceof AttackSquid) { // :332-334
            return true;
        }
        if (par1EntityLiving instanceof Spider) {
            return true;
        }
        if (par1EntityLiving instanceof CaveSpider) {
            return true;
        }
        if (par1EntityLiving instanceof Chicken) {
            return true;
        }
        if (par1EntityLiving instanceof Lizard && this.level().random.nextInt(10) == 1 && this.follow_time <= 0) {
            this.buddy = par1EntityLiving;
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:350-375). */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 4.0, 12.0));
        var5.sort(this.TargetSorter);
        if (this.level().random.nextInt(100) == 0) {
            this.setTarget((LivingEntity) null);
        }
        final LivingEntity e = this.getTarget();
        if (e != null && e.isAlive()) {
            return e;
        }
        this.setTarget((LivingEntity) null);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:377-379). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:381-383). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:385-387): y at least 50, nothing else. The override dropped EntityLiving's
     * liquid test, which is why a {@code waterCreature} spawn in a river works at all; so
     * {@link #checkSpawnObstruction} answers {@code true}.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0;
    }

    /** See {@link #checkSpawnRules}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:385-387) as the placement predicate. */
    public static boolean checkLizardSpawnRules(final EntityType<Lizard> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0;
    }

    /** {@code canDespawn} (:389-395): babies never; otherwise not persistent, not tame and no follow time left. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired() && !this.isTame() && this.should_despawn;
    }

    /** {@code createChild} / {@code spawnBabyAnimal} (:397-404): a new lizard. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    @Nullable
    public Lizard spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final Entity baby = this.getType().create(this.level());
        return baby instanceof Lizard lizard ? lizard : null;
    }

    /** {@code isWheat} (:406-408): no caller, dead; kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:410-412): the Crystal Apple. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }
}
