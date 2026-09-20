package com.swbr.orespawn.entity.portal;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.util.Ignoreable;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.EntityAnt} (EntityAnt.java:12-150): {@code ant}, the harmless Brown Ant
 * and portal to Utopia ("Ant", OreSpawnMain.java:3315-3319, tracking 16/1/false), and the base class of
 * {@link EntityRedAnt}, {@link EntityRainbowAnt}, {@link EntityUnstableAnt} and {@link Termite}. Right
 * click with an empty hand: into Utopia, or home to the Overworld when already there (:65-87).
 *
 * <p>Values: health 1 (:93-95), speed 0.15 re-set every tick (:24, :60-63), attack 0 (:36-37), hitbox
 * 0.1 (:25). {@code experienceValue = 0} (:26) is never read: {@code EntityAnimal.getExperiencePoints}
 * ({@code wf.e}) returns {@code 1 + rand(3)}, which is 1.21.1's {@code Animal.getBaseExperienceReward}.
 * No sounds (:97-121), no drops (:116-117, empty entity loot table per R10), no breeding (:123-125).
 * Not spawned through biomes (manifest {@code spawns: []}) - only nests, eggs and the troll blocks.
 *
 * <p>Goals are added in the constructor on both sides, like the 1.7.10 constructors did; the subclass
 * constructors add their own on top, so Panic and a wander goal sit in the list twice. That doubles the
 * wander chance of the Rainbow and Unstable Ant, and 1.21.1's {@code GoalSelector} keeps both entries
 * as 1.7.10's {@code EntityAITasks} did - kept (R18).
 */
public class EntityAnt extends Animal implements Ignoreable {

    public double moveSpeed;
    private static final ResourceLocation texture1 = texture("ant.png");
    private static final ResourceLocation texture2 = texture("red_ant.png");
    private static final ResourceLocation texture3 = texture("rainbow_ant.png");
    private static final ResourceLocation texture4 = texture("unstableant.png");
    private static final ResourceLocation texture5 = texture("termite.png");

    /** {@code EntityAnt(World)} (:22-30). */
    public EntityAnt(final EntityType<? extends EntityAnt> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.15000000596046448;
        // setSize(0.1f, 0.1f) is the EntityType size; experienceValue = 0 is dead (class Javadoc).
        // PORT: getNavigator().setAvoidsWater(true) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, LegacyPanic.legacyPanic(this, 1.4));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 9, 1.0));
    }

    /**
     * {@code applyEntityAttributes} (:32-38): {@code mygetMaxHealth()}, {@code moveSpeed}, attack 0.
     * In 1.7.10 this ran inside the {@code EntityLivingBase} constructor, before {@code moveSpeed} was
     * assigned, so the speed base was 0 until the first {@code onUpdate}; nothing moves before that tick,
     * the supplier carries the value the tick sets.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15000000596046448)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    private static ResourceLocation texture(final String file) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + file);
    }

    /** {@code getTexture} (:40-54), read by {@code RenderAnt}. */
    public ResourceLocation getTexture(final EntityAnt a) {
        if (a instanceof EntityRedAnt) {
            return EntityAnt.texture2;
        }
        if (a instanceof EntityRainbowAnt) {
            return EntityAnt.texture3;
        }
        if (a instanceof EntityUnstableAnt) {
            return EntityAnt.texture4;
        }
        if (a instanceof Termite) {
            return EntityAnt.texture5;
        }
        return EntityAnt.texture1;
    }

    /**
     * {@code canDespawn} (:56-58): {@code !isNoDespawnRequired()}. 1.21.1's {@code Mob.checkDespawn}
     * already skips persistent mobs before asking, and {@code Animal} answers {@code false} - the override
     * keeps the ant despawnable.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:60-63), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    /**
     * {@code interact} (:65-87). Only an {@code EntityPlayerMP} - so the client does nothing - and only
     * with an empty hand.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        final InteractionResult pass = checkPortalUse(par1EntityPlayer, hand);
        if (pass != null) {
            return pass;
        }
        if (par1EntityPlayer.level().dimension() != OreSpawnTeleporter.UTOPIA) {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, OreSpawnTeleporter.UTOPIA, this.level());
        } else {
            OreSpawnTeleporter.transferPlayerToDimension((ServerPlayer) par1EntityPlayer, Level.OVERWORLD, this.level());
        }
        return portalUsed();
    }

    /**
     * The shared head of every ant {@code interact}: {@code null} player, not an {@code EntityPlayerMP},
     * a held item. Returns the result to give up with, or {@code null} to go on.
     *
     * <p>PORT: 1.7.10 had one hand and read {@code inventory.getCurrentItem()}; the off-hand call of
     * 1.21.1 does nothing. The {@code stackSize <= 0 -> null} clean-up is {@code ItemStack.isEmpty()}.
     * The original returned {@code false} without calling {@code super.interact}, which is {@code PASS}
     * without {@code Animal.mobInteract} (no feeding).
     */
    @Nullable
    static InteractionResult checkPortalUse(@Nullable final Player par1EntityPlayer, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (par1EntityPlayer == null) {
            return InteractionResult.PASS;
        }
        if (!(par1EntityPlayer instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (!var2.isEmpty()) {
            return InteractionResult.PASS;
        }
        return null;
    }

    /**
     * The {@code return true} after a transfer. PORT: {@code CONSUME}, not {@code SUCCESS}: 1.7.10's
     * {@code EntityPlayer.interactWith} returned without swinging the arm, and {@code SUCCESS} would make
     * the server broadcast a swing.
     */
    static InteractionResult portalUsed() {
        return InteractionResult.CONSUME;
    }

    /**
     * {@code World.getClosestVulnerablePlayerToEntity(entity, dist)} ({@code ahb.b(DDDD)} in
     * client-1.7.10.jar): the nearest living player without {@code disableDamage}, the radius shrunk to
     * 0.8 while sneaking and to {@code 0.7 * max(0.1, armour cover)} while invisible. Used by the bite
     * clocks of {@link EntityRedAnt} and {@link Termite}.
     *
     * <p>{@code capabilities.disableDamage} is {@code getAbilities().invulnerable}, which also covers
     * spectators; {@code getArmorVisibility} is {@code getArmorCoverPercentage}.
     */
    @Nullable
    static Player getClosestVulnerablePlayerToEntity(final Entity par1Entity, final double par2) {
        final double x = par1Entity.getX();
        final double y = par1Entity.getY();
        final double z = par1Entity.getZ();
        double d4 = -1.0;
        Player entityplayer = null;
        final List<? extends Player> players = par1Entity.level().players();
        for (int i = 0; i < players.size(); ++i) {
            final Player entityplayer1 = players.get(i);
            if (!entityplayer1.getAbilities().invulnerable && entityplayer1.isAlive()) {
                final double d5 = entityplayer1.distanceToSqr(x, y, z);
                double d6 = par2;
                if (entityplayer1.isShiftKeyDown()) {
                    d6 = par2 * 0.800000011920929;
                }
                if (entityplayer1.isInvisible()) {
                    float f = entityplayer1.getArmorCoverPercentage();
                    if (f < 0.1f) {
                        f = 0.1f;
                    }
                    d6 *= (double) (0.7f * f);
                }
                if ((par2 < 0.0 || d5 < d6 * d6) && (d4 == -1.0 || d5 < d4)) {
                    d4 = d5;
                    entityplayer = entityplayer1;
                }
            }
        }
        return entityplayer;
    }

    /** {@code mygetMaxHealth} (:93-95). */
    public int mygetMaxHealth() {
        return 1;
    }

    /** {@code getLivingSound} (:97-99). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:101-103). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:105-107). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code getSoundVolume} (:109-111). */
    @Override
    protected float getSoundVolume() {
        return 0.0f;
    }

    /** {@code playStepSound} (:113-114): empty. */
    @Override
    protected void playStepSound(final BlockPos pos, final BlockState block) {
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat. Never reached, {@code mobInteract} skips feeding. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:123-125). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /**
     * {@code getCanSpawnHere} (:127-129): Y at least 50 and at most 4 ants (subclasses included) in
     * {@code expand(20, 10, 20)}. No {@code super}: no light, block or free-space test.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0 && this.findBuddies() <= 4;
    }

    /**
     * PORT: 1.21.1 split the collision and liquid half of 1.7.10's {@code EntityLiving.getCanSpawnHere}
     * into this method; the ant's override replaced the whole of it, so nothing is checked here either.
     */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code findBuddies} (:131-134). */
    private int findBuddies() {
        final List<EntityAnt> var5 = this.level().getEntitiesOfClass(EntityAnt.class, this.getBoundingBox().inflate(20.0, 10.0, 20.0));
        return var5.size();
    }

    /** {@code updateAITick} (:136-141): 1 in 200 per tick forgets the revenge target. */
    @Override
    protected void customServerAiStep() {
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
