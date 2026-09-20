package com.swbr.orespawn.entity.cannonfodder;

import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
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
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Chipmunk} (Chipmunk.java:15-271, verhalten/entity-06.md): the small battle
 * mob. Tamed with an apple (1 in 2), bred with a Crystal Apple, grazes dirt and farmland away, and fights as a
 * {@link EntityCannonFodder} once it wears a hat (3 damage, one-in-six swings).
 *
 * <p>Health 5 (:106-108), speed 0.38 set every tick (:59), attack attribute 1.0 that nothing reads, hitbox
 * 0.35 (:22, entity type), {@code fireResistance} 100 (:24). {@code experienceValue = 5} (:27) is dead: 1.7.10
 * {@code EntityAnimal.getExperiencePoints} returns {@code 1 + rand(3)}, inherited as
 * {@code Animal.getBaseExperienceReward} (W04 lesson).
 */
public class Chipmunk extends EntityCannonFodder {

    private float moveSpeed;

    /** {@code Chipmunk(World)} (:19-40). */
    public Chipmunk(final EntityType<? extends Chipmunk> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.38f;
        // setSize(0.35f, 0.35f) (:22) is the entity type size.
        this.moveSpeed = 0.38f;
        // fireResistance = 100 (:24): getFireImmuneTicks.
        // PORT: getNavigator().setAvoidsWater(true) (:25) - water is impassable for the path finder, malus -1
        // (Termite, W05).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        // experienceValue = 5 (:27): never read, see class comment.
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new MyEntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.600000023841858));
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.2000000476837158, s -> s.is(Items.APPLE), false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(6, new EntityAIAvoidEntity(this, Player.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(7, new EntityAIWatchClosest(this, Player.class, 6.0f));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(8, new EntityAIWatchClosest(this, Mob.class, 5.0f));
        this.goalSelector.addGoal(9, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(10, new EntityAILookIdle(this));
        // homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(11, new EntityAIMoveIndoors(this));
    }

    /**
     * {@code applyEntityAttributes} (:43-49): health 5, speed, attack 1.0. PORT: the original ran it in the
     * base constructor while {@code moveSpeed} was still 0, and {@code onUpdate} set 0.38 before the first
     * move; the supplier carries 0.38 directly (the value every tick sees).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.38f)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    // entityInit (:52-55) called setSitting(false) before the flags existed; the constructor repeats it.

    /** {@code onUpdate} (:58-61). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /**
     * {@code fall} (:63-77): damage {@code ceil(distance - 3)}, the sound chosen before the cap of 2. The
     * override replaced the whole 1.7.10 method (no rider propagation, no block fall sound, no
     * {@code LivingFallEvent}). PORT: the 1.21.1 multiplier (hay bale, bed) is ignored, as it did not exist.
     */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        float i = (float) Mth.ceil(par1 - 3.0f);
        if (i > 0.0f) {
            if (i > 3.0f) {
                this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0f, 1.0f);
            } else {
                this.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0f, 1.0f);
            }
            if (i > 2.0f) {
                i = 2.0f;
            }
            this.hurt(this.damageSources().fall(), i);
            return true;
        }
        return false;
    }

    /** {@code fireResistance = 100} (:24). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code updateAITick} (:79-96), called from {@link EntityCannonFodder#updateAITasks}. */
    @Override
    protected void updateAITick() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (this.level().random.nextInt(250) == 0) {
            this.heal(1.0f);
        }
        if (!this.level().isClientSide && this.level().random.nextInt(600) == 1) {
            // :90 (int) posX, (int) posY - 1, (int) posZ -> Mth.floor (DECISIONS R20).
            final BlockPos below = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()));
            final BlockState bid = this.level().getBlockState(below);
            // PORT: the mobGriefing rule is asked through EventHooks.canEntityGrief, which reads the same rule
            // and lets the NeoForge event veto (Termite, W05).
            if ((CannonFodderSupport.isLegacyDirt(bid) || bid.is(Blocks.FARMLAND)) && EventHooks.canEntityGrief(this.level(), this)) {
                this.level().setBlock(below, Blocks.AIR.defaultBlockState(), CannonFodderSupport.LEGACY_FLAG_2);
            }
        }
        super.updateAITick();
    }

    // isAIEnabled (:98-100): every 1.21.1 mob runs goals. canBreatheUnderwater false (:102-104): the default.

    /** {@code mygetMaxHealth} (:106-108). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code getChipmunkHealth} (:110-112). */
    public int getChipmunkHealth() {
        return (int) this.getHealth();
    }

    /** {@code interact} (:114-195): the battle mob first, then apple, dead bush, name tag, sitting. */
    @Override
    protected boolean legacyInteract(final Player par1EntityPlayer) {
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (super.legacyInteract(par1EntityPlayer)) {
            return true;
        }
        final boolean isRemote = this.level().isClientSide;
        if (!var2.isEmpty() && var2.is(Items.APPLE) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
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
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                // func_152115_b("")
                this.setOwnerName("");
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            // setCustomNameTag(getDisplayName()): an unnamed tag names it "Name Tag"; no persistence.
            this.setCustomName(var2.getHoverName());
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isSitting()) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return true;
        }
        return false;
    }

    /** {@code getLivingSound} (:197-202): none, sitting or not. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        return null;
    }

    /** {@code getHurtSound} (:204-206). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SCORPION_HIT.get();
    }

    /** {@code getDeathSound} (:208-210). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:212-214). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code getDropItem} (:216-218). */
    @Override
    protected Item getDropItem() {
        return Items.WHEAT;
    }

    /** {@code dropFewItems} (:220-232): tame 2-6 poppies ({@code red_flower} meta 0), wild the wheat roll. */
    @Override
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        } else {
            super.dropFewItems(par1, par2);
        }
    }

    /** {@code getSoundPitch} (:234-236): the spread is 0.1, not vanilla's 0.2. */
    @Override
    public float getVoicePitch() {
        return this.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f)
                : ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
    }

    /**
     * {@code getCanSpawnHere} (:238-245) on the instance, which natural spawning and mob spawners both ask: y at
     * least 50 and at most two chipmunks in the box grown by 20/10/20. The override dropped every vanilla test
     * (light, grass, collision), so {@link #checkSpawnObstruction} answers {@code true}.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0 && this.findBuddies(level) <= 2;
    }

    /** See {@link #checkSpawnRules}: EntityLiving's collision and liquid test was not called. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code findBuddies} (:242-245). The spawning mob is not in the world yet, as in 1.7.10. */
    private int findBuddies(final LevelAccessor level) {
        return level.getEntitiesOfClass(Chipmunk.class, this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }

    /**
     * {@code getCanSpawnHere} (:238-245) as the placement predicate, asked before the entity exists: the box is
     * the type's hitbox at the block centre, grown by 20/10/20.
     */
    public static boolean checkChipmunkSpawnRules(final EntityType<Chipmunk> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 10.0, 20.0);
        return pos.getY() >= 50.0 && level.getEntitiesOfClass(Chipmunk.class, box).size() <= 2;
    }

    /**
     * {@code canDespawn} (:247-253): a baby becomes persistent and stays; otherwise it despawns unless
     * persistent or tame. 1.21.1 asks this only for mobs without persistence, as 1.7.10 {@code despawnEntity} did.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired() && !this.isTame();
    }

    /** {@code createChild} / {@code spawnBabyAnimal} (:255-262): a new chipmunk. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    @Nullable
    public Chipmunk spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final Entity baby = this.getType().create(this.level());
        return baby instanceof Chipmunk chipmunk ? chipmunk : null;
    }

    /** {@code isWheat} (:264-266): no caller, dead; kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:268-270): the Crystal Apple. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }
}
