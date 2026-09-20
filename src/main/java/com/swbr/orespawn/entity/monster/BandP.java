package com.swbr.orespawn.entity.monster;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.BandP} (BandP.java:17-329), id {@code criminal} ("Criminal", OreSpawnMain.java:4073,
 * tracking 64/1/false) - "Politicians and Bankers". A thief that attacks players, villagers, Girlfriend and Boyfriend
 * and takes one item from a player with every hit (verhalten/entity-04.md).
 *
 * <p>Values: {@code BandP_stats} (100/1/18), speed 0.32 re-set every tick (:29, :64), {@code experienceValue} 1000
 * (:36), {@code fireResistance} 2 (:37), a 100-slot loot inventory (:32). No swimming task, no target tasks
 * (:39-44): it can drown, as in the original.
 *
 * <p>State: DataWatcher 20 {@code what} = variant 0/1, a {@code Byte} in the bytecode ({@code javap}); drawn once on
 * the first server update and neither saved nor read by model or renderer - after a reload it is drawn again (R18,
 * 1:1). NBT {@code GotStuff} (int) always, {@code Inventory} (list of {@code Slot} byte plus item) only while
 * {@code got_stuff != 0}.
 *
 * <p>R18: stolen goods lose their components on the drop (1:1 with the lost NBT), and the off hand - which 1.7.10 did
 * not have - is never robbed.
 */
public class BandP extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code what}, the variant that decides the nugget drops. */
    private static final EntityDataAccessor<Byte> DATA_WHAT = SynchedEntityData.defineId(BandP.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    private int whatset;
    private int whatami;
    /** {@code MymainInventory}: 100 slots, {@link ItemStack#EMPTY} is the original's {@code null}. */
    public NonNullList<ItemStack> MymainInventory;
    int got_stuff;

    /** {@code BandP(World)} (:26-45). {@code setSize(0.75f, 1.75f)} (:34) is the entity type's size (R9). */
    public BandP(final EntityType<? extends BandP> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.32f;
        this.whatset = 0;
        this.whatami = 0;
        this.MymainInventory = NonNullList.withSize(100, ItemStack.EMPTY);
        this.got_stuff = 0;
        // PORT: getNavigator().setAvoidsWater(true) (:35) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 1000;
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, MonsterSupport.legacyMoveThroughVillage(this, 0.5));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 0.5));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 10.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // EntityAIOpenDoor needed getNavigator().getCanBreakDoors(), which nothing here sets; OpenDoorGoal likewise needs
        // canOpenDoors() - both never run, as in the original (Girlfriend, W04).
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        // :44; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(5, new EntityAIMoveIndoors(this));
        final MobStats stats = MobStats.BandP_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:47-52) at registration time (R3), armor from {@code getTotalArmorValue} (:76-78). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.BandP_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.32f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:54-57). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WHAT, (byte) 0);
    }

    /** {@code fireResistance = 2} (:37). */
    @Override
    protected int getFireImmuneTicks() {
        return 2;
    }

    /** {@code canDespawn} (:59-61): a thief with loot stays. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && this.got_stuff == 0;
    }

    /** {@code onUpdate} (:63-70): speed, the vanilla tick, then on the first server tick the variant 0/1 from the world random. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (!this.level().isClientSide && this.whatset == 0) {
            this.whatset = 1;
            this.setWhat(this.whatami = this.level().random.nextInt(2));
        }
    }

    /** {@code mygetMaxHealth} (:72-74). */
    public int mygetMaxHealth() {
        return MobStats.BandP_stats().health();
    }

    /** {@code getTotalArmorValue} (:76-78) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.BandP_stats().defense();
    }

    /** {@code getLivingSound} (:88-90): {@code mob.villager.idle}. */
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    /** {@code getHurtSound} (:92-94): {@code mob.villager.hit}. */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    /** {@code getDeathSound} (:96-98): {@code mob.villager.death}. */
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    /** {@code getSoundVolume} (:100-102). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:104-106). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:108-110) emerald: unused. interact (:145-147) false: the default. attackEntityAsMob (:149-151)
    // only calls super: the vanilla Mob.doHurtTarget.

    /**
     * {@code dropItemRand} (:112-123): a fresh stack of item and count, spread ±1 on {@code OreSpawnRand}, y+1.
     *
     * <p>PORT (R18 case 4): the original changed the damage value of the stack <em>after</em> spawning the item entity,
     * so the client had seen the undamaged stack; {@code damage} is therefore passed in and set before the entity is
     * added. 1.7.10's damage value doubled as metadata; after the flattening the variant is the item itself, so the
     * copy is done only for damageable items - a {@code DAMAGE} component on anything else would keep the stack from
     * merging with normal ones.
     */
    private ItemStack dropItemRand(@Nullable final Item index, final int par1, final int damage) {
        if (index == null) {
            return ItemStack.EMPTY;
        }
        final ItemStack is = new ItemStack(index, par1);
        if (damage >= 0 && is.isDamageableItem()) {
            is.setDamageValue(damage);
        }
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + com.swbr.orespawn.OreSpawn.OreSpawnRand.nextInt(2) - com.swbr.orespawn.OreSpawn.OreSpawnRand.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + com.swbr.orespawn.OreSpawn.OreSpawnRand.nextInt(2) - com.swbr.orespawn.OreSpawn.OreSpawnRand.nextInt(2),
                is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:125-143): 10..14 emeralds; variant 0 also 2..4 each uranium and titanium nuggets; then every
     * stolen stack as item and count - the damage value only for a single item - without any other data (R18).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 10 + this.level().random.nextInt(5), i = 0; i < var4; ++i) {
            this.dropItemRand(Items.EMERALD, 1, -1);
        }
        if (this.getWhat() == 0) {
            for (int var4 = 2 + this.level().random.nextInt(3), i = 0; i < var4; ++i) {
                this.dropItemRand(ModItems.URANIUM_NUGGET.get(), 1, -1);
                this.dropItemRand(ModItems.TITANIUM_NUGGET.get(), 1, -1);
            }
        }
        for (int i = 0; i < this.MymainInventory.size(); ++i) {
            final ItemStack stolen = this.MymainInventory.get(i);
            if (!stolen.isEmpty() && stolen.getCount() != 0) {
                this.dropItemRand(stolen.getItem(), stolen.getCount(), stolen.getCount() == 1 ? stolen.getDamageValue() : -1);
            }
        }
    }

    /**
     * {@code updateAITasks} (:153-207), after the goal selectors: 1 in 12 look for a target and face it; within squared
     * 9 hit it without a chance roll and, for a player, steal - whatever the hit did - into the first free slot: the
     * worn armor piece with the highest index (helmet first), otherwise the highest occupied main-inventory slot. Out of
     * reach walk at 1.25.
     *
     * <p>{@code armorInventory} and {@code mainInventory} keep their 1.7.10 order and size in 1.21.1
     * ({@link Inventory#armor}: 0 feet .. 3 head; {@link Inventory#items}: 36). R18: the off hand is not robbed.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(12) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < 9.0) {
                    this.doHurtTarget(e);
                    if (e instanceof Player p) {
                        int k = -1;
                        int kp = -1;
                        for (int i = 0; i < this.MymainInventory.size(); ++i) {
                            if (this.MymainInventory.get(i).isEmpty()) {
                                k = i;
                                break;
                            }
                        }
                        if (k >= 0) {
                            final Inventory inventory = p.getInventory();
                            for (int i = inventory.armor.size() - 1; i >= 0; --i) {
                                if (!inventory.armor.get(i).isEmpty()) {
                                    kp = i;
                                    break;
                                }
                            }
                            if (kp >= 0) {
                                this.MymainInventory.set(k, inventory.armor.get(kp));
                                inventory.armor.set(kp, ItemStack.EMPTY);
                                ++this.got_stuff;
                            }
                            if (kp < 0) {
                                for (int i = inventory.items.size() - 1; i >= 0; --i) {
                                    if (!inventory.items.get(i).isEmpty()) {
                                        kp = i;
                                        break;
                                    }
                                }
                                if (kp >= 0) {
                                    this.MymainInventory.set(k, inventory.items.get(kp));
                                    inventory.items.set(kp, ItemStack.EMPTY);
                                    ++this.got_stuff;
                                }
                            }
                        }
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25);
                }
            }
        }
    }

    /** {@code isSuitableTarget} (:209-227): visible, a non-creative player, a villager, Girlfriend or Boyfriend. */
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
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return par1EntityLiving instanceof Villager || par1EntityLiving instanceof Girlfriend || par1EntityLiving instanceof Boyfriend;
    }

    /** {@code findSomethingToAttack} (:229-244): the first suitable entity in {@code expand(20, 6, 20)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 6.0, 20.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getWhat} (:246-248). */
    public int getWhat() {
        return this.entityData.get(DATA_WHAT);
    }

    /** {@code setWhat} (:250-252). */
    public void setWhat(final int par1) {
        this.entityData.set(DATA_WHAT, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:254-287). A spawner allows it; otherwise day, y at least 100 (the y 50
     * test before it is redundant), no other thief within {@code expand(32, 12, 32)} and a villager within
     * {@code expand(36, 12, 36)}. No light test.
     *
     * <p>PORT: spawner scan → {@link MobSpawnType#SPAWNER} (README 5.9). The absolute y 100 stays (R18, "absolute
     * Originalwerte"); in the 1.21.1 overworld that is mountain villages. Boxes around the type's spawn box at the
     * position, no self to exclude (the 1.7.10 entity was not in the world yet); a {@code WorldGenRegion} lists no
     * entities (Baryonyx, W06), so no thief spawns with chunk generation.
     */
    public static boolean checkBandPSpawnRules(final EntityType<BandP> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (pos.getY() < 100.0) {
            return false;
        }
        if (!level.getEntitiesOfClass(BandP.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(32.0, 12.0, 32.0)).isEmpty()) {
            return false;
        }
        return !level.getEntitiesOfClass(Villager.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(36.0, 12.0, 36.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkBandPSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code writeEntityToNBT} (:289-295): the inventory only with loot, {@code GotStuff} always. */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        if (this.got_stuff != 0) {
            par1NBTTagCompound.put("Inventory", this.writeToNBT(new ListTag()));
        }
        par1NBTTagCompound.putInt("GotStuff", this.got_stuff);
    }

    /** {@code readEntityFromNBT} (:297-304). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.got_stuff = par1NBTTagCompound.getInt("GotStuff");
        if (this.got_stuff != 0) {
            final ListTag nbttaglist = par1NBTTagCompound.getList("Inventory", Tag.TAG_COMPOUND);
            this.readFromNBT(nbttaglist);
        }
    }

    /**
     * {@code writeToNBT(NBTTagList)} (:306-316): one compound per occupied slot with a {@code Slot} byte and the stack.
     * PORT: the 1.21.1 stack codec ({@code ItemStack.save}) needs the registry access.
     */
    public ListTag writeToNBT(final ListTag par1NBTTagList) {
        for (int i = 0; i < this.MymainInventory.size(); ++i) {
            if (!this.MymainInventory.get(i).isEmpty()) {
                final CompoundTag nbttagcompound = new CompoundTag();
                nbttagcompound.putByte("Slot", (byte) i);
                par1NBTTagList.add(this.MymainInventory.get(i).save(this.registryAccess(), nbttagcompound));
            }
        }
        return par1NBTTagList;
    }

    /** {@code readFromNBT(NBTTagList)} (:318-328): a fresh inventory, then every valid slot. */
    public void readFromNBT(final ListTag par1NBTTagList) {
        this.MymainInventory = NonNullList.withSize(100, ItemStack.EMPTY);
        for (int i = 0; i < par1NBTTagList.size(); ++i) {
            final CompoundTag nbttagcompound = par1NBTTagList.getCompound(i);
            final int j = nbttagcompound.getByte("Slot") & 0xFF;
            final ItemStack itemstack = ItemStack.parseOptional(this.registryAccess(), nbttagcompound);
            if (!itemstack.isEmpty() && j >= 0 && j < this.MymainInventory.size()) {
                this.MymainInventory.set(j, itemstack);
            }
        }
    }
}
