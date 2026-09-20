package com.swbr.orespawn.entity.moth;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.CaterKiller} (CaterKiller.java:17-713), id {@code cater_killer} ("CaterKiller",
 * OreSpawnMain.java:4025, tracking 64/1/false). The giant forest caterpillar: eats leaves and logs to heal, spins webs
 * next to what it chases, and after 2400 ticks spent wounded bursts into a {@link Brutalfly} (verhalten/entity-05.md).
 *
 * <p>Values: {@code CaterKiller_stats} (450/32/19), speed 0.35 re-set every tick (:31, :88), {@code experienceValue} 200
 * (:45), {@code fireResistance} 100 (:46). Size 2.9 x 4.6, with {@code PlayNicely} 1.45 x 2.3 (:38-43), see
 * {@link #getDefaultDimensions}. DataWatcher 20 {@code attacking} (model animation), 21 {@code PlayNicely} (renderer
 * scale), both {@code Integer}. No NBT: {@code ticker}, {@code foundmob} and the eat target are lost on reload.
 *
 * <p>Not carried over: {@code getDropItem} (:131-133) is dead because {@code dropFewItems} is overridden;
 * {@code onLivingUpdate} (:104-106), {@code isAIEnabled} (:100-102), {@code initCreature} (:345-346) change nothing;
 * {@code interact} (:348-350) returning {@code false} is the default {@code mobInteract}.
 */
public class CaterKiller extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code CaterKillerModel} for the jaw, head and leg speed. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(CaterKiller.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code PlayNicely}, re-sent every AI tick (:456), read by {@code RenderCaterKiller}. */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(CaterKiller.class, EntityDataSerializers.INT);

    /** {@code setSize(1.45f, 2.3f)} (:42). */
    private static final EntityDimensions PLAY_NICELY_SIZE = EntityDimensions.scalable(1.45f, 2.3f);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    int foundmob;
    int ticker;
    private int closest;
    private int tx;
    private int ty;
    private int tz;
    /** The hitbox chosen in the constructor from {@code PlayNicely} (:38-43), server side. */
    private boolean smallSize;
    /** {@code Entity.isInWeb}: set by a cobweb in {@link #makeStuckInBlock}, cleared by the AI step (:469-481). */
    private boolean isInWeb;

    /** {@code CaterKiller(World)} (:28-54). */
    public CaterKiller(final EntityType<? extends CaterKiller> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.35f;
        this.foundmob = 0;
        this.ticker = 0;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // DataWatcher 21 starts at OreSpawnMain.PlayNicely (:66); the client receives the server's value.
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        this.smallSize = OreSpawnConfig.TWEAKS.PlayNicely.get() != 0;
        this.refreshDimensions();
        // PORT: getNavigator().setAvoidsWater(true) (:44) - water malus -1 (Hammerhead, W07).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 200;
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT (R18 case 3): EntityAIMoveThroughVillage (:49) has no 1.21.1 counterpart with village records; vanilla's
        // POI-based MoveThroughVillageGoal with the parameters of MonsterSupport.legacyMoveThroughVillage (W07): not
        // nocturnal, distance 4, no doors.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        final MobStats stats = MobStats.CaterKiller_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:56-61) at registration time (R3); armor from {@code getTotalArmorValue} (:96-98). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.CaterKiller_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:63-67). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
    }

    /** {@code getPlayNicely} (:69-71). */
    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    /**
     * {@code setSize} as dimensions (R9): 1.45 x 2.3 when {@code PlayNicely} was set at construction, otherwise the type's
     * 2.9 x 4.6.
     *
     * <p>PORT (R18 case 4): 1.7.10 read the client's own config in the client's constructor; a client of a dedicated
     * server could therefore size the box differently from the server. The client takes the synced DataWatcher 21
     * instead, which is the server's value. If {@code PlayNicely} is edited while the entity lives, the client box follows
     * the new value and the server box keeps the constructor's, as the original server did.
     */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        final boolean small = this.level().isClientSide ? this.getPlayNicely() != 0 : this.smallSize;
        if (small) {
            return PLAY_NICELY_SIZE;
        }
        return super.getDefaultDimensions(pose);
    }

    /** Refreshes the client hitbox when DataWatcher 21 arrives (see {@link #getDefaultDimensions}). */
    @Override
    public void onSyncedDataUpdated(final EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_PLAY_NICELY.equals(key) && this.level().isClientSide) {
            this.refreshDimensions();
        }
    }

    /** {@code fireResistance = 100} (:46). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /**
     * {@code attackEntityFrom} (:77-85): the hit, then an {@code EntityLiving} source becomes the attack target. Players
     * are no {@code EntityLiving}; they come in through the hurt-by target goal.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        Entity e = null;
        final boolean ret = super.hurt(par1DamageSource, par2);
        e = par1DamageSource.getEntity();
        if (e != null && e instanceof Mob) {
            this.setTarget((LivingEntity) e);
        }
        return ret;
    }

    /** {@code onUpdate} (:87-90), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:92-94). */
    public int mygetMaxHealth() {
        return MobStats.CaterKiller_stats().health();
    }

    /** {@code getTotalArmorValue} (:96-98) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.CaterKiller_stats().defense();
    }

    /** {@code getLivingSound} (:108-113): 1 in 3 from the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return ModSounds.CATERKILLER_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:115-117). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CATERKILLER_HIT.get();
    }

    /** {@code getDeathSound} (:119-121). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CATERKILLER_DEATH.get();
    }

    /** {@code getSoundVolume} (:123-125). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:127-129). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:145-329), every item spread ±4 at y+1 ({@code dropItemRand}, :135-143): jaw, item frame, 10
     * leather, 6 beef; then 1-5 rolls of {@code rand(20)} on the world random - 0 Ultimate Sword, 1 ruby, 2 diamond block,
     * 3 enchanted ruby sword, 4-7 enchanted ruby shovel/pickaxe/axe/hoe, 8-11 enchanted ruby armour, 12 Ultimate Bow,
     * 13-19 nothing; then 25 butterflies at the feet +1 with their living sound.
     *
     * <p>The enchantment tables are the same blocks the W07 bosses share ({@link ArthropodSupport#enchantSword} and
     * siblings, same order of rolls, Sharpness twice summed - PORT there). The stack is enchanted before the item entity
     * is added, so the client receives it enchanted; both random streams keep their order.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.CATERKILLER_JAW.get(), 1), 5);
        ArthropodSupport.dropItemRand(this, new ItemStack(Items.ITEM_FRAME, 1), 5);
        for (int var4 = 0; var4 < 10; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.LEATHER, 1), 5);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.BEEF, 1), 5);
        }
        final Level world = this.level();
        for (int i = 1 + world.random.nextInt(5), var4 = 0; var4 < i; ++var4) {
            final int var5 = world.random.nextInt(20);
            ItemStack is = null;
            switch (var5) {
                case 0:
                    is = new ItemStack(ModItems.ULTIMATE_SWORD.get(), 1);
                    break;
                case 1:
                    is = new ItemStack(ModItems.RUBY.get(), 1);
                    break;
                case 2:
                    is = new ItemStack(Blocks.DIAMOND_BLOCK.asItem(), 1);
                    break;
                case 3:
                    is = new ItemStack(ModItems.RUBY_SWORD.get(), 1);
                    ArthropodSupport.enchantSword(is, world);
                    break;
                case 4:
                    is = new ItemStack(ModItems.RUBY_SHOVEL.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                    break;
                case 5:
                    is = new ItemStack(ModItems.RUBY_PICKAXE.get(), 1);
                    ArthropodSupport.enchantPickaxe(is, world);
                    break;
                case 6:
                    is = new ItemStack(ModItems.RUBY_AXE.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                    break;
                case 7:
                    is = new ItemStack(ModItems.RUBY_HOE.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                    break;
                case 8:
                    is = new ItemStack(ModItems.RUBY_HELMET.get(), 1);
                    ArthropodSupport.enchantHelmet(is, world);
                    break;
                case 9:
                    is = new ItemStack(ModItems.RUBY_CHEST.get(), 1);
                    ArthropodSupport.enchantBody(is, world);
                    break;
                case 10:
                    is = new ItemStack(ModItems.RUBY_LEGGINGS.get(), 1);
                    ArthropodSupport.enchantBody(is, world);
                    break;
                case 11:
                    is = new ItemStack(ModItems.RUBY_BOOTS.get(), 1);
                    ArthropodSupport.enchantBoots(is, world);
                    break;
                case 12:
                    is = new ItemStack(ModItems.ULTIMATE_BOW.get(), 1);
                    break;
                default:
                    break;
            }
            if (is != null) {
                ArthropodSupport.dropItemRand(this, is, 5);
            }
        }
        for (int var4 = 0; var4 < 25; ++var4) {
            ItemSpawnEgg.spawnSomething(ModEntities.BUTTERFLY.get(), world, this.getX(), this.getY() + 1.0, this.getZ());
        }
    }

    /**
     * {@code attackEntityAsMob} (:352-366): the vanilla hit, then {@code addVelocity(cos * 1.2, 0.1, sin * 1.2)} away from
     * the attacker, vertical doubled for a player or a dead ({@code isDead}, i.e. removed) target.
     *
     * <p>PORT: {@code addVelocity} is {@link Entity#push(double, double, double)}; {@code hurtMarked} carries the impulse
     * to a server player (MonsterSupport.legacyKnockback, W07).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                final double ks = 1.2;
                double inair = 0.1;
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
                par1Entity.hurtMarked = true;
            }
            return true;
        }
        return false;
    }

    /**
     * The food of {@code scan_it} (:373 and its three copies): {@code leaves}, {@code vine}, {@code log}, {@code MyDT}
     * ({@code duplicatortreelog}), {@code log2}, {@code leaves2} and OreSpawn's apple, experience, scary, peach and cherry
     * leaves.
     */
    private static boolean isFood(final BlockState bid) {
        return MothSupport.isLegacyLeaves(bid) || bid.is(Blocks.VINE) || MothSupport.isLegacyLog(bid)
                || bid.is(ModBlocks.DUPLICATOR_TREE_LOG.get()) || MothSupport.isLegacyLog2(bid) || MothSupport.isLegacyLeaves2(bid)
                || MothSupport.isOreSpawnLeaves(bid);
    }

    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /** {@code scan_it} (:368-449): the six faces of the shell at distance (dx, dy, dz), nearest food block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = this.blockAt(x + dx, y + i, z + j);
                if (isFood(bid)) {
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
                if (isFood(bid)) {
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
                if (isFood(bid)) {
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
                if (isFood(bid)) {
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
                if (isFood(bid)) {
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
                if (isFood(bid)) {
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

    /**
     * {@code Entity.isInWeb} was set by {@code BlockWeb.onEntityCollidedWithBlock} during the move. PORT: 1.21.1 has no
     * field; the cobweb calls {@code makeStuckInBlock}, and this remembers that it was a cobweb (sweet berries and powder
     * snow did not set {@code isInWeb}).
     */
    @Override
    public void makeStuckInBlock(final BlockState state, final Vec3 motionMultiplier) {
        if (state.is(Blocks.COBWEB)) {
            this.isInWeb = true;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    /**
     * {@code updateAITasks} (:451-558), after the goal selectors:
     * <ol>
     *   <li>DataWatcher 21 = {@code PlayNicely};</li>
     *   <li>while {@code health + 1 < maxHealth}: {@code ticker}, and above 2400 a Brutalfly four blocks up, the explosion
     *       sound, ten butterflies and {@code setDead} without drops;</li>
     *   <li>caught in a web: every cobweb in x/z ±2, y -1..4 is removed, {@code isInWeb} cleared;</li>
     *   <li>with 1/4: target upkeep and search; in reach ({@code (5 + width/2)^2}) {@code attacking} and a bite with
     *       (1/3 or 1/4), out of reach walk at 1.25 and with 1/4 spin a web next to the target (no mobGriefing check);</li>
     *   <li>(1/8 while hurt, or 1/30) without {@code PlayNicely}: search the shells 1..9, 11 for food, walk there without a
     *       target, and within 9 blocks eat it (block only with mobGriefing) for +2 health.</li>
     * </ol>
     * {@code (int)} casts on coordinates are {@link Mth#floor} (R20); {@code worldObj.rand} is the level random.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        final Level world = this.level();
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        if (this.getHealth() + 1.0f < this.getMaxHealth()) {
            ++this.ticker;
            if (this.ticker > 2400) {
                ItemSpawnEgg.spawnSomething(ModEntities.BRUTALFLY.get(), world, this.getX(), this.getY() + 4.0, this.getZ());
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, world.random.nextFloat() * 0.2f + 0.9f);
                for (int i = 0; i < 10; ++i) {
                    ItemSpawnEgg.spawnSomething(ModEntities.BUTTERFLY.get(), world, this.getX(), this.getY() + 1.0 + world.random.nextInt(4), this.getZ());
                }
                // setDead: gone without death, drops or XP.
                this.discard();
                return;
            }
        }
        if (this.isInWeb) {
            for (int i = -2; i <= 2; ++i) {
                for (int j = -1; j < 5; ++j) {
                    for (int k = -2; k <= 2; ++k) {
                        final BlockPos at = new BlockPos(Mth.floor(this.getX()) + i, Mth.floor(this.getY()) + j, Mth.floor(this.getZ()) + k);
                        if (world.getBlockState(at).is(Blocks.COBWEB)) {
                            // setBlock(air) with flag 3; the following setBlockMetadataWithNotify(..., 0, 3) on air changed nothing.
                            world.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
            this.isInWeb = false;
            // PORT: isInWeb = false made the next moveEntity skip the web slowdown; 1.21.1 keeps that in stuckSpeedMultiplier.
            this.stuckSpeedMultiplier = Vec3.ZERO;
        }
        if (world.random.nextInt(4) == 0) {
            LivingEntity e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (world.random.nextInt(200) == 0) {
                this.setTarget(null);
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.foundmob = 1;
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (5.0f + e.getBbWidth() / 2.0f) * (5.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (world.random.nextInt(3) == 0 || world.random.nextInt(4) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.setAttacking(0);
                    this.getNavigation().moveTo(e, 1.25);
                    if (world.random.nextInt(4) == 0) {
                        double dx = e.getX();
                        double dz = e.getZ();
                        dx += (world.random.nextFloat() - world.random.nextFloat()) * 2.0;
                        dz += (world.random.nextFloat() - world.random.nextFloat()) * 2.0;
                        for (int i = 2; i > -2; --i) {
                            if (MothSupport.isAir(world, Mth.floor(dx), Mth.floor(e.getY()) + i + 1, Mth.floor(dz))
                                    && !MothSupport.isAir(world, Mth.floor(dx), Mth.floor(e.getY()) + i, Mth.floor(dz))) {
                                world.setBlock(new BlockPos(Mth.floor(dx), Mth.floor(e.getY()) + i + 1, Mth.floor(dz)),
                                        Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
                                break;
                            }
                        }
                    }
                }
            } else {
                this.setAttacking(0);
                this.foundmob = 0;
            }
        }
        if (((world.random.nextInt(8) == 0 && this.getHealth() < this.mygetMaxHealth()) || world.random.nextInt(30) == 0)
                && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            // :527-530, a decompiled "tx = ty = tz = false ? 1 : 0"
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 13; ++i) {
                int j = i;
                if (j > 9) {
                    j = 9;
                }
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 9) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                if (this.foundmob == 0) {
                    this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                }
                if (this.closest < 81) {
                    // PORT: the mobGriefing gamerule through EventHooks.canEntityGrief, which reads the same rule
                    // (Chipmunk, W06). Flag 2 is HerbivoreSupport.LEGACY_FLAG_2; the breakBlock of the removed log or
                    // leaves - which 1.7.10 ran whatever the flags - follows as its port.
                    if (EventHooks.canEntityGrief(world, this)) {
                        final BlockPos at = new BlockPos(this.tx, this.ty, this.tz);
                        final BlockState eaten = world.getBlockState(at);
                        world.setBlock(at, Blocks.AIR.defaultBlockState(), HerbivoreSupport.LEGACY_FLAG_2);
                        if (MothSupport.isLegacyLog(eaten) || MothSupport.isLegacyLog2(eaten)) {
                            HerbivoreSupport.legacyLogBreakBlock(world, at);
                        } else if (MothSupport.isLegacyLeaves(eaten) || MothSupport.isLegacyLeaves2(eaten) || MothSupport.isOreSpawnLeaves(eaten)) {
                            HerbivoreSupport.legacyLeavesBreakBlock(world, at);
                        }
                    }
                    this.heal(2.0f);
                    if (world.random.nextInt(20) == 1) {
                        this.playSound(SoundEvents.PLAYER_BURP, 1.0f, world.random.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:560-585): alive and seen by {@link #MyCanSee}; a player out of creative mode; never another
     * CaterKiller; any monster; otherwise {@code isAttackableNonMob}.
     */
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
        if (!this.MyCanSee(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof CaterKiller) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /** {@code findSomethingToAttack} (:587-602): nothing with {@code PlayNicely}; the first suitable entity in {@code expand(20, 8, 20)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 8.0, 20.0)));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:604-606). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:608-610). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** The wounded-tick counter towards the Brutalfly (:22, :457-468); exposed for GameTests only. */
    public int getTicker() {
        return this.ticker;
    }

    /** Sets {@link #getTicker()}; for GameTests only, so a test need not wait 2400 ticks. */
    public void setTicker(final int ticker) {
        this.ticker = ticker;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:612-650). A "CaterKiller" spawner allows it; otherwise Y at least 50, a 1
     * in 10 roll on the world random, daytime, only air, leaves, leaves2, log or log2 in x/z -1..1, y +1..+4, and no other
     * CaterKiller within {@code expand(48, 16, 48)}.
     *
     * <p>PORT: the spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} (catalogue 5.9); {@code worldObj.rand}
     * is the spawn's random, which natural spawning takes from the level.
     */
    public static boolean checkCaterKillerSpawnRules(final EntityType<CaterKiller> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (random.nextInt(10) != 0) {
            return false;
        }
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        for (int k = -1; k < 2; ++k) {
            for (int j = -1; j < 2; ++j) {
                for (int i = 1; i < 5; ++i) {
                    final BlockState bid = level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + i, pos.getZ() + k));
                    if (!bid.isAir() && !MothSupport.isLegacyLeaves(bid) && !MothSupport.isLegacyLeaves2(bid)
                            && !MothSupport.isLegacyLog(bid) && !MothSupport.isLegacyLog2(bid)) {
                        return false;
                    }
                }
            }
        }
        return level.getEntitiesOfClass(CaterKiller.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(48.0, 16.0, 48.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkCaterKillerSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code MyCanSee} (:652-712): a ray of its own from 2.5 blocks ahead of the body at y+3 towards the middle of the
     * target, in steps of at most one block per axis; only air, cobweb, tall grass and {@code Blocks.leaves} (not
     * {@code leaves2}) let it through. {@code nblks *= (int) |d|} truncates, as in the original. Block positions of the
     * float coordinates are {@link Mth#floor} (R20).
     */
    public boolean MyCanSee(final LivingEntity e) {
        final double xzoff = 2.5;
        int nblks = 10;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + 3.0);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 10.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 10.0);
        float dz = (float) ((e.getZ() - startz) / 10.0);
        if (Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks *= (int) Math.abs(dx);
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if (Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks *= (int) Math.abs(dy);
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if (Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks *= (int) Math.abs(dz);
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            final BlockState bid = this.blockAt(Mth.floor(startx), Mth.floor(starty), Mth.floor(startz));
            if (!bid.isAir()) {
                if (!bid.is(Blocks.COBWEB)) {
                    if (!MothSupport.isLegacyTallGrass(bid)) {
                        if (!MothSupport.isLegacyLeaves(bid)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
