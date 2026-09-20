package com.swbr.orespawn.entity.easter;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.EasterBunny} (EasterBunny.java:13-598): {@code easter_bunny}, a shy rabbit that
 * lays one to three random OreSpawn spawn eggs about every 600 AI ticks ("Easter Bunny", OreSpawnMain.java:4013,
 * {@code registerModEntity} :4017 64/1/false). It spawns naturally only while {@code easter_day} is set, which
 * {@code platform.Holidays} fixes on April 20th at server start (R18); the date guard lives in the W12 spawn table,
 * not here (OreSpawnMain.java:4341).
 *
 * <p>Values: health 10 (:72-74), speed 0.45 re-set every tick (:19, :47-50), attack 8 registered but used by no
 * goal (:39-40), hitbox 0.5 x 0.75 (:20), {@code fireResistance} 100 (:22). {@code experienceValue = 5} (:23) is
 * never read (1.7.10 {@code EntityAnimal.getExperiencePoints}, verhalten/entity-07.md). No DataWatcher entries, no NBT
 * of its own. Breeds with the Crystal Apple, cannot be tamed.
 */
public class EasterBunny extends Animal {

    private float moveSpeed;

    /** {@code EasterBunny(World)} (:17-33). */
    public EasterBunny(final EntityType<? extends EasterBunny> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.45f;
        // setSize(0.5f, 0.75f) (:20) is the entity type's size (R9).
        this.moveSpeed = 0.45f;
        // fireResistance = 100 (:22) is getFireImmuneTicks(); experienceValue = 5 (:23) is dead.
        // PORT: getNavigator().setAvoidsWater(true) (:24) - a water path malus of -1, as in Cassowary.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(3, new EntityAIAvoidEntity(this, Player.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(7, new EntityAILookIdle(this));
    }

    /** {@code applyEntityAttributes} (:35-41). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.45f)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    /** {@code fireResistance = 100} (:22). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:47-50), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code getCanSpawnHere} (:52-62) around a spawn position, before the entity exists: no bunny to skip. */
    private static boolean noOtherBunny(final EntityType<?> type, final LevelAccessor level, final BlockPos pos) {
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                .inflate(32.0, 8.0, 32.0);
        return level.getEntitiesOfClass(EasterBunny.class, box).isEmpty();
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:52-62) - Y 50 or higher, daytime, and no other Easter Bunny within
     * {@code expand(32, 8, 32)}. No light, grass or free-space test: the override replaced {@code EntityAnimal}'s.
     * {@code posY} of a 1.7.10 spawn attempt was the block Y.
     */
    public static boolean checkEasterBunnySpawnRules(final EntityType<EasterBunny> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        return noOtherBunny(type, level, pos);
    }

    /**
     * {@code getCanSpawnHere} (:52-62) on the positioned instance (mob spawners, the second test of a natural spawn).
     * {@code findNearestEntityWithinAABB(EasterBunny.class, boundingBox.expand(32, 8, 32), this)} skips the asking
     * bunny. No random roll, so asking twice is harmless.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (this.getY() < 50.0) {
            return false;
        }
        if (!HerbivoreSupport.isDaytime(this.level())) {
            return false;
        }
        EasterBunny target = null;
        for (final EasterBunny other : this.level().getEntitiesOfClass(EasterBunny.class,
                this.getBoundingBox().inflate(32.0, 8.0, 32.0))) {
            if (other != this) {
                target = other;
                break;
            }
        }
        return target == null;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code mygetMaxHealth} (:72-74). */
    public int mygetMaxHealth() {
        return 10;
    }

    /** {@code getLivingSound} (:76-78). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:80-82). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:84-86). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getSoundVolume} (:88-90). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code dropFewItems} first, then the equipment roll; {@code getDropItem} (:92-94) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:96-103): 2..4 raw chicken, looting ignored. No eggs on death. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        var3 = this.random.nextInt(3);
        var3 += 2;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.spawnAtLocation(new ItemStack(Items.CHICKEN, 1));
        }
    }

    /**
     * {@code updateAITick} (:105-113): forget the revenge target 1 in 200 before the inherited step, lay 1..3 eggs
     * 1 in 600 after it. Both rolls on the world random.
     */
    @Override
    protected void customServerAiStep() {
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(600) == 1) {
            this.LayAnEgg(1 + this.level().random.nextInt(3));
        }
    }

    /**
     * {@code LayAnEgg} (:115-573): {@code nextInt(115)} on the world random; 5..113 pick one of 109 spawn eggs in the
     * original case order, 0..4 and 114 lay nothing. The stack lands up to one block off on the shared
     * {@code OreSpawnRand}, one block up, added straight to the level.
     *
     * @return the laid stack, or {@code null} when the roll hit an empty case
     */
    @Nullable
    public ItemStack LayAnEgg(final int par1) {
        ItemEntity var3 = null;
        int i = 0;
        Item index = null;
        // final int val = 0 (:119): the damage value, meaningless for an OreSpawn egg in 1.21.1.
        ItemStack is = null;
        i = this.level().random.nextInt(115);
        index = eggFor(i);
        if (index == null) {
            return null;
        }
        is = new ItemStack(index, par1);
        var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2), is);
        if (var3 != null) {
            this.level().addFreshEntity(var3);
        }
        return is;
    }

    /**
     * The {@code switch} of {@code LayAnEgg} (:122-563), case by case. Public so a test can check the table without
     * rolling the random.
     */
    @Nullable
    public static Item eggFor(final int i) {
        Item index;
        switch (i) {
            case 5 -> index = ModItems.EGG_GIRLFRIEND.get(); // GirlfriendEgg
            case 6 -> index = ModItems.EGG_RED_COW.get(); // RedCowEgg
            case 7 -> index = ModItems.EGG_GOLD_COW.get(); // GoldCowEgg
            case 8 -> index = ModItems.EGG_ENCHANTED_COW.get(); // EnchantedCowEgg
            case 9 -> index = ModItems.EGG_MOTHRA.get(); // MOTHRAEgg
            case 10 -> index = ModItems.EGG_ALOSAURUS.get(); // AloEgg
            case 11 -> index = ModItems.EGG_CRYOLOPHOSAURUS.get(); // CryoEgg
            case 12 -> index = ModItems.EGG_CAMARASAURUS.get(); // CamaEgg
            case 13 -> index = ModItems.EGG_VELOCITY_RAPTOR.get(); // VeloEgg
            case 14 -> index = ModItems.EGG_HYDROLISC.get(); // HydroEgg
            case 15 -> index = ModItems.EGG_BASILISK.get(); // BasilEgg
            case 16 -> index = ModItems.EGG_DRAGONFLY.get(); // DragonflyEgg
            case 17 -> index = ModItems.EGG_EMPEROR_SCORPION.get(); // EmperorScorpionEgg
            case 18 -> index = ModItems.EGG_SCORPION.get(); // ScorpionEgg
            case 19 -> index = ModItems.EGG_CAVE_FISHER.get(); // CaveFisherEgg
            case 20 -> index = ModItems.EGG_SPYRO.get(); // SpyroEgg
            case 21 -> index = ModItems.EGG_BARYONYX.get(); // BaryonyxEgg
            case 22 -> index = ModItems.EGG_GAMMA_METROID.get(); // GammaMetroidEgg
            case 23 -> index = ModItems.EGG_COCKATEIL.get(); // CockateilEgg
            case 24 -> index = ModItems.EGG_KYUUBI.get(); // KyuubiEgg
            case 25 -> index = ModItems.EGG_ALIEN.get(); // AlienEgg
            case 26 -> index = ModItems.EGG_ATTACK_SQUID.get(); // AttackSquidEgg
            case 27 -> index = ModItems.EGG_WATER_DRAGON.get(); // WaterDragonEgg
            case 28 -> index = ModItems.EGG_CEPHADROME.get(); // CephadromeEgg
            case 29 -> index = ModItems.EGG_DRAGON.get(); // DragonEgg
            case 30 -> index = ModItems.EGG_KRAKEN.get(); // KrakenEgg
            case 31 -> index = ModItems.EGG_LIZARD.get(); // LizardEgg
            case 32 -> index = ModItems.EGG_BEE.get(); // BeeEgg
            case 33 -> index = ModItems.EGG_TROOPER_BUG.get(); // TrooperBugEgg
            case 34 -> index = ModItems.EGG_SPIT_BUG.get(); // SpitBugEgg
            case 35 -> index = ModItems.EGG_STINK_BUG.get(); // StinkBugEgg
            case 36 -> index = ModItems.EGG_OSTRICH.get(); // OstrichEgg
            case 37 -> index = ModItems.EGG_GAZELLE.get(); // GazelleEgg
            case 38 -> index = ModItems.EGG_CHIPMUNK.get(); // ChipmunkEgg
            case 39 -> index = ModItems.EGG_CREEPING_HORROR.get(); // CreepingHorrorEgg
            case 40 -> index = ModItems.EGG_TERRIBLE_TERROR.get(); // TerribleTerrorEgg
            case 41 -> index = ModItems.EGG_CLIFF_RACER.get(); // CliffRacerEgg
            case 42 -> index = ModItems.EGG_TRIFFID.get(); // TriffidEgg
            case 43 -> index = ModItems.EGG_NIGHTMARE.get(); // PitchBlackEgg
            case 44 -> index = ModItems.EGG_LURKING_TERROR.get(); // LurkingTerrorEgg
            case 45 -> index = ModItems.EGG_GODZILLA.get(); // GodzillaEgg
            case 46 -> index = ModItems.EGG_SMALL_WORM.get(); // SmallWormEgg
            case 47 -> index = ModItems.EGG_MEDIUM_WORM.get(); // MediumWormEgg
            case 48 -> index = ModItems.EGG_LARGE_WORM.get(); // LargeWormEgg
            case 49 -> index = ModItems.EGG_CASSOWARY.get(); // CassowaryEgg
            case 50 -> index = ModItems.EGG_CLOUD_SHARK.get(); // CloudSharkEgg
            case 51 -> index = ModItems.EGG_GOLD_FISH.get(); // GoldFishEgg
            case 52 -> index = ModItems.EGG_LEAF_MONSTER.get(); // LeafMonsterEgg
            case 53 -> index = ModItems.EGG_TSHIRT.get(); // TshirtEgg
            case 54 -> index = ModItems.EGG_ENDER_KNIGHT.get(); // EnderKnightEgg
            case 55 -> index = ModItems.EGG_ENDER_REAPER.get(); // EnderReaperEgg
            case 56 -> index = ModItems.EGG_BEAVER.get(); // BeaverEgg
            case 57 -> index = ModItems.EGG_ROTATOR.get(); // RotatorEgg
            case 58 -> index = ModItems.EGG_VORTEX.get(); // VortexEgg
            case 59 -> index = ModItems.EGG_PEACOCK.get(); // PeacockEgg
            case 60 -> index = ModItems.EGG_FAIRY.get(); // FairyEgg
            case 61 -> index = ModItems.EGG_DUNGEON_BEAST.get(); // DungeonBeastEgg
            case 62 -> index = ModItems.EGG_RAT.get(); // RatEgg
            case 63 -> index = ModItems.EGG_FLOUNDER.get(); // FlounderEgg
            case 64 -> index = ModItems.EGG_WHALE.get(); // WhaleEgg
            case 65 -> index = ModItems.EGG_IRUKANDJI.get(); // IrukandjiEgg
            case 66 -> index = ModItems.EGG_SKATE.get(); // SkateEgg
            case 67 -> index = ModItems.EGG_URCHIN.get(); // UrchinEgg
            case 68 -> index = ModItems.EGG_ROBOT1.get(); // Robot1Egg
            case 69 -> index = ModItems.EGG_ROBOT2.get(); // Robot2Egg
            case 70 -> index = ModItems.EGG_ROBOT3.get(); // Robot3Egg
            case 71 -> index = ModItems.EGG_ROBOT4.get(); // Robot4Egg
            case 72 -> index = ModItems.EGG_GHOST.get(); // GhostEgg
            case 73 -> index = ModItems.EGG_GHOST_SKELLY.get(); // GhostSkellyEgg
            case 74 -> index = ModItems.EGG_BROWN_ANT.get(); // BrownAntEgg
            case 75 -> index = ModItems.EGG_RED_ANT.get(); // RedAntEgg
            case 76 -> index = ModItems.EGG_RAINBOW_ANT.get(); // RainbowAntEgg
            case 77 -> index = ModItems.EGG_UNSTABLE_ANT.get(); // UnstableAntEgg
            case 78 -> index = ModItems.EGG_TERMITE.get(); // TermiteEgg
            case 79 -> index = ModItems.EGG_BUTTERFLY.get(); // ButterflyEgg
            case 80 -> index = ModItems.EGG_MOTH.get(); // MothEgg
            case 81 -> index = ModItems.EGG_MOSQUITO.get(); // MosquitoEgg
            case 82 -> index = ModItems.EGG_FIREFLY.get(); // FireflyEgg
            case 83 -> index = ModItems.EGG_TREX.get(); // TRexEgg
            case 84 -> index = ModItems.EGG_HERCULES_BEETLE.get(); // HerculesEgg
            case 85 -> index = ModItems.EGG_MANTIS.get(); // MantisEgg
            case 86 -> index = ModItems.EGG_STINKY.get(); // StinkyEgg
            case 87 -> index = ModItems.EGG_ROBOT5.get(); // Robot5Egg
            case 88 -> index = ModItems.EGG_COIN.get(); // CoinEgg
            case 89 -> index = ModItems.EGG_BOYFRIEND.get(); // BoyfriendEgg
            case 90 -> index = ModItems.EGG_THE_KING.get(); // TheKingEgg
            case 91 -> index = ModItems.EGG_THE_PRINCE.get(); // ThePrinceEgg
            case 92 -> index = ModItems.EGG_EASTER_BUNNY.get(); // EasterBunnyEgg
            case 93 -> index = ModItems.EGG_MOLENOID.get(); // MolenoidEgg
            case 94 -> index = ModItems.EGG_SEA_MONSTER.get(); // SeaMonsterEgg
            case 95 -> index = ModItems.EGG_SEA_VIPER.get(); // SeaViperEgg
            case 96 -> index = ModItems.EGG_CATER_KILLER.get(); // CaterKillerEgg
            case 97 -> index = ModItems.EGG_LEON.get(); // LeonEgg
            case 98 -> index = ModItems.EGG_HAMMERHEAD.get(); // HammerheadEgg
            case 99 -> index = ModItems.EGG_RUBBER_DUCKY.get(); // RubberDuckyEgg
            case 100 -> index = ModItems.EGG_CRYSTAL_COW.get(); // CrystalCowEgg
            case 101 -> index = ModItems.EGG_CRIMINAL.get(); // CriminalEgg
            case 102 -> index = ModItems.EGG_THE_QUEEN.get(); // TheQueenEgg
            case 103 -> index = ModItems.EGG_BRUTALFLY.get(); // BrutalflyEgg
            case 104 -> index = ModItems.EGG_NASTYSAURUS.get(); // NastysaurusEgg
            case 105 -> index = ModItems.EGG_POINTYSAURUS.get(); // PointysaurusEgg
            case 106 -> index = ModItems.EGG_CRICKET.get(); // CricketEgg
            case 107 -> index = ModItems.EGG_THE_PRINCESS.get(); // ThePrincessEgg
            case 108 -> index = ModItems.EGG_FROG.get(); // FrogEgg
            case 109 -> index = ModItems.EGG_JEFFERY.get(); // JefferyEgg
            case 110 -> index = ModItems.EGG_ANT_ROBOT.get(); // AntRobotEgg
            case 111 -> index = ModItems.EGG_SPIDER_ROBOT.get(); // SpiderRobotEgg
            case 112 -> index = ModItems.EGG_SPIDER_DRIVER.get(); // SpiderDriverEgg
            case 113 -> index = ModItems.EGG_CRAB.get(); // CrabEgg
            default -> index = null;
        }
        return index;
    }

    /** {@code canDespawn} (:575-581): babies become persistent, adults despawn unless persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired();
    }

    /** {@code createChild} (:583-585). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:587-589). */
    public EasterBunny spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.EASTER_BUNNY.get().create(this.level());
    }

    /** {@code isWheat} (:591-593): dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:595-597). */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /** 1.7.10 {@code EntityAnimal.interact}: breeding only, no baby feeding. */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
