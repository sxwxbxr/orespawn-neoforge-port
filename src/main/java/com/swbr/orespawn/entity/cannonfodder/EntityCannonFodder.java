package com.swbr.orespawn.entity.cannonfodder;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.registry.ModItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.EntityCannonFodder} (EntityCannonFodder.java:14-392, verhalten/entity-07.md):
 * the tameable base of the "battle mobs" {@link Chipmunk}, {@link Lizard}, Ostrich and VelocityRaptor. Not
 * registered itself. Fed a carrot, quinoa or a potato it takes a red, green or blue hat (team 1, 2, 3); a
 * second click of an owner activates it, and from then on it hunts monsters, strangers and every battle mob
 * with another hat, guards a patrol point while sitting, and clones itself for a corn cob.
 *
 * <p>Kept non-final with protected hooks for the subclasses written by other porters:
 * {@link #legacyInteract} (the original {@code interact}), {@link #updateAITasks} and {@link #updateAITick}
 * (the two halves of 1.7.10 AI ticking, see there), {@link #dropFewItems}/{@link #getDropItem}, and
 * {@link #setSitting}/{@link #isSitting}.
 *
 * <p>Original behaviour kept (R18), each checked against the source:
 * <ul>
 *   <li>The owner rotation (:75-96): once tamed with {@code name_one} set, a click by anyone but the second
 *       owner makes the clicker {@code name_one} and the old owner {@code name_two}; whoever never clicks
 *       after taming lets any other player become co-owner.</li>
 *   <li>{@code PlayNicely} is not read (:319-333).</li>
 *   <li>{@code is_activated}/{@code hat_color} are server fields pushed into the DataWatcher every sixth
 *       tick and read back from it on the client (:50-64). The catalogue proposed dropping that sync; it
 *       stays, because {@code interact} runs on both sides and changes the client fields too - the client
 *       copy, which has no owner names, drifts until the next read. Written straight into
 *       {@code SynchedEntityData}, a client-side write would stick until the server changed the value.</li>
 * </ul>
 */
public class EntityCannonFodder extends TamableAnimal implements LegacyArmor {

    /** DataWatcher 20: {@code is_activated} - 0 wild, 1 fed a hat, 2 activated. */
    private static final EntityDataAccessor<Integer> DATA_IS_ACTIVATED = SynchedEntityData.defineId(EntityCannonFodder.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code hat_color} - 0 none, 1 carrot (red), 2 quinoa (green), 3 potato (blue). */
    private static final EntityDataAccessor<Integer> DATA_HAT_COLOR = SynchedEntityData.defineId(EntityCannonFodder.class, EntityDataSerializers.INT);

    /** Manifest id of {@code VelocityRaptor}, see {@link #isVelocityRaptor()}. */
    private static final ResourceLocation VELOCITY_RAPTOR_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "velocity_raptor");

    /** Owner UUID strings, package access in 1.7.10 (all classes shared {@code danger.orespawn}). */
    protected String name_one;
    protected String name_two;
    private int is_activated;
    private int hat_color;
    private int syncer;
    private int px;
    private int pz;
    private int py;
    private GenericTargetSorter LocalTargetSorter;

    /** {@code EntityCannonFodder(World)} (:26-38). */
    public EntityCannonFodder(final EntityType<? extends EntityCannonFodder> type, final Level par1World) {
        super(type, par1World);
        this.name_one = null;
        this.name_two = null;
        this.is_activated = 0;
        this.hat_color = 0;
        this.syncer = 0;
        this.px = 0;
        this.pz = 0;
        this.py = 0;
        this.LocalTargetSorter = null;
        this.LocalTargetSorter = new GenericTargetSorter(this);
    }

    // applyEntityAttributes (:40-42) only called super: the subclasses bring the attribute sets.

    /** {@code entityInit} (:44-48). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_ACTIVATED, 0);
        builder.define(DATA_HAT_COLOR, 0);
    }

    /** {@code onUpdate} (:50-64): the six-tick field sync, after the vanilla tick. */
    @Override
    public void tick() {
        super.tick();
        ++this.syncer;
        if (this.syncer > 5) {
            if (this.level().isClientSide) {
                this.is_activated = this.entityData.get(DATA_IS_ACTIVATED);
                this.hat_color = this.entityData.get(DATA_HAT_COLOR);
            } else {
                this.entityData.set(DATA_IS_ACTIVATED, this.is_activated);
                this.entityData.set(DATA_HAT_COLOR, this.hat_color);
            }
            this.syncer = 0;
        }
    }

    /**
     * Entry point of a right click. PORT: 1.7.10 had one hand and read {@code inventory.getCurrentItem()};
     * the off hand does nothing. The original's {@code true} (returned on both sides) is
     * {@code sidedSuccess}. The click order around it (name tag, a stranger's lead) is restored by
     * {@link CannonFodderInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return this.legacyInteract(par1EntityPlayer) ? InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
    }

    /** {@code interact} (:66-209), in the original order. Subclasses override this, not {@link #mobInteract}. */
    protected boolean legacyInteract(final Player par1EntityPlayer) {
        // :67-71 - a stack of size 0 was replaced by null; in 1.21.1 it is ItemStack.EMPTY.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        // :72-74 super.interact: EntityAnimal breeding (see CannonFodderSupport.animalInteract).
        if (CannonFodderSupport.animalInteract(this, par1EntityPlayer)) {
            return true;
        }
        final String clicker = par1EntityPlayer.getUUID().toString();
        // :75-96 - the owner rotation, no return except for a foreign third player.
        if (this.name_one != null && this.isTame()) {
            if (this.name_one.equals(clicker)) {
                if (this.name_two == null) {
                    this.name_two = this.name_one;
                    this.setOwnerName(this.name_one = clicker);
                    this.is_activated = 2;
                }
            } else if (this.name_two != null) {
                if (!this.name_two.equals(clicker)) {
                    return true;
                }
                this.name_two = this.name_one;
                this.setOwnerName(this.name_one = clicker);
                this.is_activated = 2;
            } else {
                this.name_two = this.name_one;
                this.setOwnerName(this.name_one = clicker);
                this.is_activated = 2;
            }
        }
        // :97-118 carrot -> hat 1.
        if (!var2.isEmpty() && var2.is(Items.CARROT) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            this.hat_color = 1;
            this.takeHat(par1EntityPlayer, var2);
            return true;
        }
        // :119-140 potato -> hat 3.
        if (!var2.isEmpty() && var2.is(Items.POTATO) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            this.hat_color = 3;
            this.takeHat(par1EntityPlayer, var2);
            return true;
        }
        // :141-162 MyQuinoa -> hat 2.
        if (!var2.isEmpty() && var2.is(ModItems.QUINOA.get()) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            this.hat_color = 2;
            this.takeHat(par1EntityPlayer, var2);
            return true;
        }
        // :163-193 MyCornCob (corn_seed) on an activated mob -> clone.
        if (!var2.isEmpty() && this.is_activated == 2 && var2.is(ModItems.CORN_SEED.get()) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.level().isClientSide) {
                // PORT: spawnCreature by global name ("Ostrich" by default, "Lizard", "Chipmunk",
                // "Velocity Raptor" by instanceof, :164-173) is this mob's own EntityType: the four names are
                // exactly the four subclasses, so the spawned class is the same.
                final Entity newent = spawnCreature(this.level(), this.getType(),
                        this.getX() + this.level().random.nextFloat(), this.getY() + 0.01, this.getZ() + this.level().random.nextFloat());
                if (newent != null) {
                    final EntityCannonFodder cf = (EntityCannonFodder) newent;
                    // func_152115_b(func_152113_b()): the owner name, copied.
                    cf.setOwnerUUID(this.getOwnerUUID());
                    cf.setTame(true, false);
                    cf.setStuff(this.hat_color, this.is_activated, this.name_one, this.name_two);
                }
            }
            this.spawnTamingParticles(true);
            // :184 World.playSoundAtEntity(player, "random.explode", 0.75, 2.0): broadcast from the server,
            // silent in the client world (a null player makes ClientLevel skip it, as 1.7.10 RenderGlobal did).
            this.level().playSound((Player) null, par1EntityPlayer.getX(), par1EntityPlayer.getY(), par1EntityPlayer.getZ(),
                    SoundEvents.GENERIC_EXPLODE, par1EntityPlayer.getSoundSource(), 0.75f, 2.0f);
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        // :194-207 any other click on an activated mob toggles guard duty and stores the patrol point.
        if (this.is_activated == 2 && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (this.isSitting()) {
                this.setSitting(false);
                this.spawnTamingParticles(true);
            } else {
                this.setSitting(true);
                this.spawnTamingParticles(false);
                // :202-204 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
                this.px = Mth.floor(this.getX());
                this.py = Mth.floor(this.getY());
                this.pz = Mth.floor(this.getZ());
            }
            return true;
        }
        return false;
    }

    /** The shared body of the three hat branches (:98-117, :120-139, :142-161). */
    private void takeHat(final Player par1EntityPlayer, final ItemStack var2) {
        if (this.name_one == null) {
            this.name_one = par1EntityPlayer.getUUID().toString();
        }
        if (this.is_activated == 0) {
            this.is_activated = 1;
        }
        this.setTame(true, false);
        this.setOwnerName(this.name_one);
        this.spawnTamingParticles(true);
        this.heal(this.getMaxHealth() - this.getHealth());
        // func_110163_bv = enablePersistence
        this.setPersistenceRequired();
        CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
    }

    /**
     * {@code spawnCreature} (:211-220): create, place with a random yaw and pitch 0, add to the world, play the
     * living sound. PORT: an {@link EntityType} instead of the {@code EntityList} name.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2, final double par4, final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            // PORT: the original cast to EntityLiving unchecked; every caller passes a battle mob.
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }

    /** {@code setStuff} (:222-228). */
    public void setStuff(final int hc, final int ia, final String s1, final String s2) {
        this.hat_color = hc;
        this.is_activated = ia;
        this.name_one = s1;
        this.name_two = s2;
        this.setPersistenceRequired();
    }

    /** {@code getHatColor} (:230-232): the field (on the client: the last six-tick read). */
    public int getHatColor() {
        return this.hat_color;
    }

    /** {@code get_is_activated} (:234-236): the field (on the client: the last six-tick read). */
    public int get_is_activated() {
        return this.is_activated;
    }

    /** {@code writeEntityToNBT} (:238-257). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        if (this.name_one == null) {
            par1NBTTagCompound.putString("NameOne", "");
        } else {
            par1NBTTagCompound.putString("NameOne", this.name_one);
        }
        if (this.name_two == null) {
            par1NBTTagCompound.putString("NameTwo", "");
        } else {
            par1NBTTagCompound.putString("NameTwo", this.name_two);
        }
        par1NBTTagCompound.putInt("IsActivated", this.is_activated);
        par1NBTTagCompound.putInt("HatColor", this.hat_color);
        par1NBTTagCompound.putInt("PatrolX", this.px);
        par1NBTTagCompound.putInt("PatrolY", this.py);
        par1NBTTagCompound.putInt("PatrolZ", this.pz);
    }

    /** {@code readEntityFromNBT} (:259-278): a stored {@code NameOne} makes the mob tame, owned by it. */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.name_one = par1NBTTagCompound.getString("NameOne");
        if (this.name_one != null && this.name_one.equals("")) {
            this.name_one = null;
        }
        this.name_two = par1NBTTagCompound.getString("NameTwo");
        if (this.name_two != null && this.name_two.equals("")) {
            this.name_two = null;
        }
        this.is_activated = par1NBTTagCompound.getInt("IsActivated");
        this.hat_color = par1NBTTagCompound.getInt("HatColor");
        this.px = par1NBTTagCompound.getInt("PatrolX");
        this.py = par1NBTTagCompound.getInt("PatrolY");
        this.pz = par1NBTTagCompound.getInt("PatrolZ");
        if (this.name_one != null) {
            this.setTame(true, false);
            this.setOwnerName(this.name_one);
        }
    }

    /** {@code isSuitableTarget} (:280-317). */
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
        if (this.isSitting()) {
            final double dx = this.px - par1EntityLiving.getX();
            final double dy = this.py - par1EntityLiving.getY();
            final double dz = this.pz - par1EntityLiving.getZ();
            if (dx * dx + dy * dy + dz * dz > 144.0) {
                return false;
            }
        }
        // EntityMob is Monster (as in MyUtils).
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof EntityCannonFodder) {
            final EntityCannonFodder cf = (EntityCannonFodder) par1EntityLiving;
            final int i = cf.getHatColor();
            return i != 0 && i != this.hat_color;
        }
        if (par1EntityLiving instanceof Player) {
            final Player p = (Player) par1EntityLiving;
            final String uuid = p.getUUID().toString();
            return !p.getAbilities().instabuild && (this.name_one == null || !this.name_one.equals(uuid))
                    && (this.name_two == null || !this.name_two.equals(uuid));
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:319-333): the first suitable living entity by {@link GenericTargetSorter}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 4.0, 10.0));
        var5.sort(this.LocalTargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getTotalArmorValue} (:335-340): 3 while activated, else 0. Lizard overrides it with 5. */
    public int getTotalArmorValue() {
        if (this.is_activated == 2) {
            return 3;
        }
        return 0;
    }

    /** R5: the legacy formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code attackEntityAsFodder} (:342-344): mob damage, no knockback, no enchantments. */
    public void attackEntityAsFodder(final Entity par1Entity, final float f) {
        par1Entity.hurt(this.damageSources().mobAttack(this), f);
    }

    /**
     * 1.21.1 calls this where 1.7.10 {@code EntityLiving.updateAITasks} called {@code updateAITick}: after the
     * goal selectors and the navigation, before the move, look and jump controls ({@code Mob.serverAiStep}).
     * The whole OreSpawn {@code updateAITasks} chain runs from here, see {@link #updateAITasks}.
     */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:346-387). {@code super.updateAITasks()} (:350) ran vanilla AI ticking, of which
     * only {@code updateAITick} is OreSpawn code - so it is {@link #updateAITick} here; the goals, navigation
     * and controls run around it in {@code Mob.serverAiStep}. PORT: the battle logic below therefore runs
     * before the move and look controls of the same tick instead of after them.
     *
     * <p>Subclasses override this for their own {@code updateAITasks} (Lizard) and {@link #updateAITick} for
     * {@code updateAITick} (Chipmunk, Ostrich, VelocityRaptor).
     */
    protected void updateAITasks() {
        // isDead is the removed flag, not the health.
        if (this.isRemoved()) {
            return;
        }
        this.updateAITick();
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (this.is_activated != 2) {
            return;
        }
        int pfreq = 5;
        int sfreq = 7;
        float dm = 4.0f;
        if (this instanceof Chipmunk) {
            dm = 3.0f;
            sfreq = 6;
        }
        if (this instanceof Lizard) {
            dm = 6.0f;
            sfreq = 8;
        }
        if (this.isVelocityRaptor()) {
            sfreq = 6;
            pfreq = 4;
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(pfreq) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 9.0 && (this.random.nextInt(sfreq + 1) == 0 || this.random.nextInt(sfreq) == 1)) {
                    this.attackEntityAsFodder(e, dm);
                }
            } else if (this.isSitting()) {
                this.getNavigation().moveTo((double) this.px, (double) this.py, (double) this.pz, 0.6499999761581421);
            }
        }
        if (this.level().random.nextInt(250) == 1) {
            this.heal(1.0f);
        }
    }

    /**
     * 1.7.10 {@code updateAITick} of the vanilla chain: {@code EntityAnimal} clears {@code inLove} when the age
     * is not 0 - {@code Animal.customServerAiStep}. Subclasses call {@code super.updateAITick()} where the
     * original did.
     */
    protected void updateAITick() {
        super.customServerAiStep();
    }

    /**
     * {@code this instanceof VelocityRaptor} (:368). PORT: VelocityRaptor is ported in parallel in another
     * package; the registry id (manifest {@code velocity_raptor}) names the same class without a compile-time
     * dependency, and no class extends VelocityRaptor.
     */
    private boolean isVelocityRaptor() {
        return VELOCITY_RAPTOR_ID.equals(EntityType.getKey(this.getType()));
    }

    /** {@code createChild} (:389-391). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat, unless a subclass overrides it. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems(recentlyHit, looting)}, then {@code dropEquipment} - which is
     * {@code Mob.dropCustomDeathLoot}. The {@code rand(200) - looting < 5} rare-drop roll that followed called
     * the empty {@code dropRareDrop} of every battle mob; only its random draw is not repeated.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, CannonFodderSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** 1.7.10 {@code EntityLiving.dropFewItems}: the base class does not override it (verhalten: no own drops). */
    protected void dropFewItems(final boolean par1, final int par2) {
        CannonFodderSupport.vanillaDropFewItems(this, this.getDropItem(), par2);
    }

    /** 1.7.10 {@code EntityLiving.getDropItem}: nothing. */
    @Nullable
    protected Item getDropItem() {
        return null;
    }

    /**
     * 1.7.10 {@code EntityTameable.func_152115_b(String)}: stores the owner name, a UUID string. The empty string
     * or a non-UUID clears the owner (see {@link CannonFodderSupport#parseOwner}).
     */
    public void setOwnerName(@Nullable final String name) {
        this.setOwnerUUID(CannonFodderSupport.parseOwner(name));
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
     * {@code onDeath}: no override in the original (EntityCannonFodder.java:14, {@code extends EntityTameable}), and 1.7.10
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
