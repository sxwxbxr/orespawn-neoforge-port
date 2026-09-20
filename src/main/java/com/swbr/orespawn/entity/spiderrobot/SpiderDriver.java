package com.swbr.orespawn.entity.spiderrobot;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.SpiderDriver} (SpiderDriver.java:11-172, verhalten/entity-12.md "SpiderDriver"),
 * registry id {@code spider_driver} ("Spider Driver", OreSpawnMain.java:4149/:4153, tracking 64 / 1 / no velocity).
 * A spider that climbs into the nearest empty {@link SpiderRobot} and steers it towards whatever it sees.
 *
 * <h2>Spider base values (catalogue 6.4)</h2>
 * The class overrides no size, attribute, sound or drop, so those are 1.7.10 {@code EntitySpider}/{@code EntityMob},
 * read from the bytecode of {@code reference/jar/mcp/client-1.7.10.jar} ({@code javap -c}, names via
 * {@code joined.srg}):
 * <ul>
 *   <li>{@code yn.<init>}: {@code setSize(1.4f, 0.9f)} - the 1.21.1 spider hitbox, set on the entity type;</li>
 *   <li>{@code yn.aD} (applyEntityAttributes): max health 16, movement speed 0.800000011920929; attack damage 2.0 is
 *       the {@code SharedMonsterAttributes} default registered by {@code yg.aD}; follow range 16 from
 *       {@code sw.aD} - see {@link #createAttributes};</li>
 *   <li>{@code yg.<init>}: {@code experienceValue} 5 = {@link Monster}'s {@code xpReward} (EntityMob, not EntityAnimal:
 *       no 1-3 roll, W04 XP lesson);</li>
 *   <li>{@code yn.b(ZI)V} (dropFewItems) and {@code yn.u} (getDropItem = string): see {@link #dropFewItems};</li>
 *   <li>{@code yn.h} (climb flag), {@code yn.as} (no web slow-down), {@code yn.d(rw)} (poison immunity),
 *       {@code yn.bd} (arthropod), the sounds and {@code yn.a(sy)} (jockey and effects on spawn) match
 *       {@link Spider} 1.21.1 and are inherited. Arthropod in 1.21.1 is the entity type tag
 *       {@code #minecraft:arthropod} (registry entry of this wave).</li>
 * </ul>
 *
 * <h2>AI</h2>
 * {@code isAIEnabled()} is {@code true} (:30-32), so only the task lists run; 1.7.10 {@code EntitySpider} adds none of
 * its own. {@code findPlayerToAttack} (:34-37) and {@code attackEntity} (:73-81, 16-tick bite with 50 % poison) belong
 * to the old {@code updateEntityActionState} path that 1.7.10 never calls for an AI-enabled mob: they were dead and are
 * not ported (verhalten/entity-12.md "Es gibt keine Nahkampf-Task"). The driver does not attack by itself; the robot
 * does.
 */
public class SpiderDriver extends Spider implements LegacyArmor {

    private GenericTargetSorter TargetSorter;

    /** {@code SpiderDriver(World)} (:15-24). */
    public SpiderDriver(final EntityType<? extends SpiderDriver> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent); Spider.registerGoals is
        // replaced by an empty override, since EntitySpider 1.7.10 had no task list.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.65f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** 1.7.10 {@code EntitySpider}/{@code EntityMob}/{@code EntityLiving} attributes (class comment). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.800000011920929)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    /** No vanilla spider goals; the original list is added in the constructor. */
    @Override
    protected void registerGoals() {
    }

    /**
     * PORT: 1.21.1 {@code Spider} paths up walls ({@code WallClimberNavigation}); the 1.7.10 AI-enabled spider used
     * the plain {@code PathNavigate} and climbed only physically, through {@code isOnLadder}. The plain ground
     * navigation is {@code Mob.createNavigation}.
     */
    @Override
    protected PathNavigation createNavigation(final Level level) {
        return new GroundPathNavigation(this, level);
    }

    /** {@code canDespawn} (:26-28): not while riding; the persistence half is {@code Mob.checkDespawn}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPassenger();
    }

    /** 1.21.1 runs this after the goal selectors and the navigation (Mob.serverAiStep); see {@link #updateAITasks}. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:39-71), after {@code super} (the goals, in {@code serverAiStep}). On foot, one tick in five:
     * climb into the nearest empty robot within reach, or walk to it. Riding, one tick in four: steer the robot towards
     * a target at least 11 blocks away.
     */
    protected void updateAITasks() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(5) == 0 && !this.isPassenger()) {
            final LivingEntity e = this.findSpiderRobot();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                final float reach = 4.0f + e.getBbWidth() / 2.0f;
                if (this.distanceToSqr(e) < reach * reach) {
                    this.startRiding(e);
                } else {
                    this.getNavigation().moveTo(e, 0.55);
                }
            }
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(4) == 0 && this.isPassenger()) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                final float reach = 11.0f + e.getBbWidth() / 2.0f;
                if (this.distanceToSqr(e) >= reach * reach) {
                    if (this.getVehicle() instanceof SpiderRobot sp) {
                        final double d1 = e.getZ() - this.getZ();
                        final double d2 = e.getX() - this.getX();
                        final double dd = Math.atan2(d1, d2);
                        sp.goThisWay(0.35 * Math.cos(dd), 0.35 * Math.sin(dd));
                    }
                }
            }
        }
    }

    /** {@code getTotalArmorValue} (:83-88): 8 while riding, 20 on foot. */
    public int getTotalArmorValue() {
        if (this.isPassenger()) {
            return 8;
        }
        return 20;
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code findSpiderRobot} (:90-107): the nearest robot without a rider in the box 25/15/25. */
    @Nullable
    private LivingEntity findSpiderRobot() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<SpiderRobot> var5 = this.level().getEntitiesOfClass(SpiderRobot.class, this.getBoundingBox().inflate(25.0, 15.0, 25.0));
        var5.sort(this.TargetSorter);
        for (final SpiderRobot var8 : var5) {
            if (var8.getFirstPassenger() == null) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code isSuitableTarget} (:109-146): not peaceful, alive, not ignorable, no spider of any kind, visible; players
     * outside creative at any distance, everything else from six blocks out.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderRobot) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderDriver) {
            return false;
        }
        if (par1EntityLiving instanceof Spider) {
            return false;
        }
        if (par1EntityLiving instanceof CaveSpider) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return this.distanceToSqr(par1EntityLiving) >= 36.0;
    }

    /** {@code findSomethingToAttack} (:148-165): the nearest suitable entity in the box 35/15/35. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(35.0, 15.0, 35.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code getCanSpawnHere} (:167-171) as the placement predicate: a Spider Robot within 24/12/24 of the spawn box
     * allows the spawn outright, otherwise {@code EntityMob.getCanSpawnHere}: not peaceful, the 1.7.10 light test
     * ({@link LegacyLightLevel}) and {@code EntityCreature}'s path weight {@code 0.5 - brightness >= 0}. The collision
     * and liquid half of {@code EntityLiving} is {@link #checkSpawnObstruction}.
     *
     * <p>PORT: {@code findNearestEntityWithinAABB} searched around the created entity's box; a predicate runs before an
     * entity exists, so the box is the type's spawn box at the position (LurkingTerror precedent).
     */
    public static boolean checkSpiderDriverSpawnRules(final EntityType<SpiderDriver> type, final ServerLevelAccessor level,
                                                      final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (robotNearby(level, type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {
            return true;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        // EntityMob.getBlockPathWeight at floor(posX), floor(boundingBox.minY), floor(posZ).
        return 0.5f - level.getLightLevelDependentMagicValue(pos) >= 0.0f;
    }

    /** The whole {@code getCanSpawnHere} light and path rule is {@link #checkSpiderDriverSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** {@code EntityLiving}'s collision and liquid test, skipped when a Spider Robot is near (the {@code ||} of :170). */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        // PORT: LevelReader has no entity query; the reader passed here is the entity's level in 1.21.1.
        if (robotNearby(level instanceof EntityGetter getter ? getter : this.level(), this.getBoundingBox())) {
            return true;
        }
        return super.checkSpawnObstruction(level);
    }

    private static boolean robotNearby(final EntityGetter level, final AABB box) {
        return !level.getEntitiesOfClass(SpiderRobot.class, box.inflate(24.0, 12.0, 24.0)).isEmpty();
    }

    /**
     * 1.7.10 {@code onDeath} rolled {@code dropFewItems(recentlyHit, looting)} and then {@code dropEquipment};
     * {@code Mob.dropCustomDeathLoot} is the latter (R10: drops in code, the loot table stays empty). Looting counts
     * only when a player killed the driver.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code EntitySpider.dropFewItems} ({@code yn.b(ZI)V}): {@code EntityLiving.dropFewItems} ({@code sw.b(ZI)V}) with
     * {@code getDropItem} = string - {@code rand(3)}, plus {@code rand(looting + 1)} only with looting - then one
     * spider eye if recently hit and {@code rand(3) == 0 || rand(1 + looting) > 0}. Each {@code dropItem(item, 1)} is
     * its own stack at the feet.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        int j = this.random.nextInt(3);
        if (par2 > 0) {
            j += this.random.nextInt(par2 + 1);
        }
        for (int k = 0; k < j; ++k) {
            this.spawnAtLocation(new ItemStack(Items.STRING, 1));
        }
        if (par1 && (this.random.nextInt(3) == 0 || this.random.nextInt(1 + par2) > 0)) {
            this.spawnAtLocation(new ItemStack(Items.SPIDER_EYE, 1));
        }
    }

    /** The looting level {@code onDeath} passed on: the killer's, when the killer is a player (RedCow precedent). */
    private static int lootingLevel(final ServerLevel level, final DamageSource damageSource) {
        if (damageSource.getEntity() instanceof Player killer) {
            return EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        return 0;
    }
}
