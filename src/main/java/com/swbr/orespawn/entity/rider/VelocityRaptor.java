package com.swbr.orespawn.entity.rider;

import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.entity.cannonfodder.EntityCannonFodder;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.VelocityRaptor} (VelocityRaptor.java:14-392), id {@code velocity_raptor}
 * ("Velocity Raptor", OreSpawnMain.java:3427/:3431, tracking 64/1/false). A small tameable battle mob on
 * {@link EntityCannonFodder}: it eats grass and flowers to heal, and feeding it an apple as its owner makes the
 * owner fast (verhalten/entity-13.md). The battle AI (4 damage, scan 1/4, swing "1/7 or 1/6") is the base class's.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>{@code mygetMaxHealth} is 20 when tamed, but the attribute was only set in {@code applyEntityAttributes},
 *       during construction, while the tame flag was still clear: the real maximum stays 10 and the 20 only enters
 *       comparisons and heal amounts.</li>
 *   <li>{@code experienceValue = 5} (:33) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} returned
 *       {@code 1 + rand.nextInt(3)} (W04 lesson), which {@code Animal.getBaseExperienceReward} still does.</li>
 *   <li>{@code isWheat} (apple) overrides nothing; breeding uses {@code isBreedingItem} = Crystal Apple.</li>
 *   <li>A wild raptor drops nothing: {@code dropFewItems} skips {@code super} and {@code getDropItem} is null.</li>
 * </ul>
 * The click order around {@link #legacyInteract} (name tag, a stranger's lead) is restored for the whole family by
 * {@code CannonFodderInteractEvents}.
 *
 * <p>Not carried over: {@code entityInit} (:55-59) only repeated {@code setSitting(false)}, which the constructor does
 * (the flags are not built while 1.21.1 defines synced data); {@code onLivingUpdate} (:221-223) only called
 * {@code super}; {@code isAIEnabled} (:209-211) is the 1.21.1 default; {@code canBreatheUnderwater} (:213-215)
 * returns the default {@code false} (final in 1.21.1).
 */
public class VelocityRaptor extends EntityCannonFodder {

    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /**
     * {@code VelocityRaptor(World)} (:22-44). {@code setSize(0.5f, 0.6f)} is the entity type's size;
     * {@code fireResistance = 10} is {@link #getFireImmuneTicks()}; goals in the original priorities, added in the
     * constructor on both sides like the original (W04 precedent).
     */
    public VelocityRaptor(final EntityType<? extends VelocityRaptor> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.55f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // getNavigator().setAvoidsWater(true) (:31): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 1.5f, 10.0f, 2.0f));
        // EntityMob.class is Monster.class (same reading as util.MyUtils).
        this.goalSelector.addGoal(3, new MyEntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.25, stack -> stack.is(Items.APPLE), false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.600000023841858));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWander(this, 0.9f));
        this.goalSelector.addGoal(8, new EntityAILookIdle(this));
        // :43; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(9, new EntityAIMoveIndoors(this));
    }

    /**
     * {@code applyEntityAttributes} (:46-53): health {@link #mygetMaxHealth()} while untamed = 10, attack 2.0. The
     * speed field was still 0 when the 1.7.10 constructor chain ran this; {@link #tick()} writes 0.55 before the
     * first use, so the supplier carries 0.55.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, javap on
     * {@code sv.<init>}); 1.21.1 defaults to 0.6.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.55f)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code fireResistance = 10} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /**
     * {@code getCanSpawnHere} (:61-63) as the placement predicate: at or above Y 50 and by day. No {@code super}
     * call in the original, so neither the animal light/grass rule nor the collision test applies (see the two
     * instance overrides below).
     */
    public static boolean checkVelocityRaptorSpawnRules(final EntityType<VelocityRaptor> type, final ServerLevelAccessor level,
                                                        final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel());
    }

    /** The whole rule is the placement predicate; {@code Mob}'s walk-target test was not part of the override. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code onUpdate} (:65-69): the speed attribute is rewritten every tick, then the base update. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /**
     * {@code fall} (:71-85) without {@code super}: {@code ceil(distance - 3)}, the big or small fall sound, at most
     * 2 damage. The multiplier is 1.21.1's form of 1.7.10's scaled {@code fall} argument (a hay bale passed
     * {@code distance * 0.2} in both versions).
     *
     * <p>PORT: {@code LivingFallEvent} does not fire, as it did not for this override in 1.7.10 either (Forge fired it
     * from {@code EntityLivingBase.fall}, which the override never called).
     */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        final float par1 = fallDistance * multiplier;
        float i = (float) Mth.ceil(par1 - 3.0f);
        if (i > 0.0f) {
            if (i > 3.0f) {
                this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0f, 1.0f); // "damage.fallbig"
            } else {
                this.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0f, 1.0f); // "damage.fallsmall"
            }
            if (i > 2.0f) {
                i = 2.0f;
            }
            this.hurt(source, i);
            return true;
        }
        return false;
    }

    /**
     * {@code scan_it} (:87-168): the six faces of the shell at distance (dx, dy, dz), each face scanned against
     * {@link RiderSupport#isRaptorFood}; a strictly closer hit replaces the target.
     */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = this.blockAt(x + dx, y + i, z + j);
                if (RiderSupport.isRaptorFood(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.blockAt(x - dx, y + i, z + j);
                if (RiderSupport.isRaptorFood(bid)) {
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
                BlockState bid = this.blockAt(x + i, y + dy, z + j);
                if (RiderSupport.isRaptorFood(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.blockAt(x + i, y - dy, z + j);
                if (RiderSupport.isRaptorFood(bid)) {
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
                BlockState bid = this.blockAt(x + i, y + j, z + dz);
                if (RiderSupport.isRaptorFood(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                bid = this.blockAt(x + i, y + j, z - dz);
                if (RiderSupport.isRaptorFood(bid)) {
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

    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /**
     * {@code updateAITick} (:170-207), called by {@code EntityCannonFodder.updateAITasks} before its battle AI:
     * {@code super} first (EntityAnimal's {@code inLove} reset), drop the revenge target with 1/200, then - not
     * sitting, ((1/20 and hurt) or 1/250) and {@code PlayNicely == 0} - search the shells i = 1..9 (dy at most 2,
     * every second shell from 5 on) for the closest plant, walk to it, and within squared distance 12 eat it (air
     * with mobGriefing), heal 2 and burp.
     */
    @Override
    protected void updateAITick() {
        super.updateAITick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (!this.isSitting()
                && ((this.level().random.nextInt(20) == 0 && this.getVHealth() < this.mygetMaxHealth())
                    || this.level().random.nextInt(250) == 0)
                && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            final boolean tx = false;
            this.tz = (tx ? 1 : 0);
            this.ty = (tx ? 1 : 0);
            this.tx = (tx ? 1 : 0);
            for (int i = 1; i < 10; ++i) {
                int j = i;
                if (j > 2) {
                    j = 2;
                }
                // :189 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                if (this.closest < 12) {
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        // setBlock(..., air, 0, 2): the other half of a double plant stays until something updates it,
                        // as in 1.7.10.
                        this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.AIR.defaultBlockState(),
                                CannonFodderSupport.LEGACY_FLAG_2);
                    }
                    this.heal(2.0f);
                    this.playSound(SoundEvents.PLAYER_BURP, 0.5f, this.level().random.nextFloat() * 0.2f + 1.5f); // "random.burp"
                }
            }
        }
    }

    /** {@code mygetMaxHealth} (:217-219). */
    public int mygetMaxHealth() {
        return this.isTame() ? 20 : 10;
    }

    /** {@code getVHealth} (:225-227), also read by the model for the feather speed. */
    public int getVHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code interact} (:229-318) in the original order: first {@code EntityCannonFodder.interact} (which starts
     * with the animal's breeding), then apple, dead bush, name tag, and the owner's sit toggle.
     *
     * <p>The player speed writes of :259, :283 and :313 sat in the <b>client</b> half of their branches; the port
     * runs {@link RiderSupport#setVelocityRaptorSpeed} in the server half of the same branches (R18, see there).
     */
    @Override
    protected boolean legacyInteract(final Player par1EntityPlayer) {
        final boolean isRemote = this.level().isClientSide;
        // :231-235 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (super.legacyInteract(par1EntityPlayer)) {
            return true;
        }
        if (!var2.isEmpty() && var2.is(Items.APPLE) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
                        this.setTame(true, false);
                        this.setOwnerName(par1EntityPlayer.getUUID().toString()); // func_152115_b
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
                } else {
                    // :259 setBaseValue(0.6000000238418579), client-side in the original.
                    RiderSupport.setVelocityRaptorSpeed(par1EntityPlayer, RiderSupport.RAPTOR_FED_SPEED);
                }
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal(this.mygetMaxHealth() - this.getHealth());
                }
            }
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                this.setHealth((float) this.mygetMaxHealth());
                this.setOwnerName(""); // func_152115_b("")
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
                // :283 setBaseValue(0.30000001192092896), the client (else) half in the original.
                RiderSupport.setVelocityRaptorSpeed(par1EntityPlayer, RiderSupport.RAPTOR_RESET_SPEED);
            }
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            this.setCustomName(var2.getHoverName());
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            if (!this.isSitting()) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            if (!isRemote) {
                // :313 setBaseValue(0.30000001192092896), client-side in the original.
                RiderSupport.setVelocityRaptorSpeed(par1EntityPlayer, RiderSupport.RAPTOR_RESET_SPEED);
            }
            return true;
        }
        return false;
    }

    /** {@code getLivingSound} (:320-325): silent either way. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        return null;
    }

    /** {@code getHurtSound} (:327-329). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRYO_HURT.get();
    }

    /** {@code getDeathSound} (:331-333). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:335-337). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code getDropItem} (:339-341). */
    @Nullable
    @Override
    protected Item getDropItem() {
        return null;
    }

    /**
     * {@code dropFewItems} (:343-352): a tamed raptor drops 2-6 poppies ({@code red_flower} meta 0), a wild one
     * nothing - no {@code super} call.
     */
    @Override
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        }
    }

    /** {@code getSoundPitch} (:354-356). */
    @Override
    public float getVoicePitch() {
        return this.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f)
                : ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
    }

    /** {@code attackEntityFrom} (:358-366): every hit capped at 10. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float p2 = par2;
        if (p2 > 10.0f) {
            p2 = 10.0f;
        }
        ret = super.hurt(par1DamageSource, p2);
        return ret;
    }

    /**
     * {@code canDespawn} (:368-374): a baby becomes persistent and never despawns; otherwise only wild and not
     * persistent ones. {@code Animal} would answer {@code false}.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired(); // func_110163_bv
            return false;
        }
        return !this.isTame() && !this.isPersistenceRequired();
    }

    /** {@code createChild} (:376-379). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /**
     * {@code spawnBabyAnimal} (:381-383): a new raptor of this type ({@code getType()} keeps this class free of the
     * registry holder's field name, as the base class's clone does).
     */
    @Nullable
    public VelocityRaptor spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final Entity baby = this.getType().create(this.level());
        return baby instanceof VelocityRaptor raptor ? raptor : null;
    }

    /** {@code isWheat} (:385-387): overrides nothing in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:389-391): the Crystal Apple. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }
}
