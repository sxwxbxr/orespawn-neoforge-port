package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import com.swbr.orespawn.entity.boss.king.KingHead;
import com.swbr.orespawn.entity.boss.king.PurplePower;
import com.swbr.orespawn.entity.boss.king.TheKing;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.boss.mobzilla.Godzilla;
import com.swbr.orespawn.entity.boss.mobzilla.GodzillaHead;
import com.swbr.orespawn.entity.boss.prince.ThePrince;
import com.swbr.orespawn.entity.boss.prince.ThePrincess;
import com.swbr.orespawn.entity.boss.prince.ThePrinceTeen;
import com.swbr.orespawn.entity.boss.princeadult.ThePrinceAdult;
import com.swbr.orespawn.entity.boss.queen.QueenHead;
import com.swbr.orespawn.entity.boss.queen.TheQueen;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.pet.RubberDucky;
import com.swbr.orespawn.entity.pet.Stinky;
import com.swbr.orespawn.entity.spiderrobot.SpiderDriver;
import com.swbr.orespawn.entity.spiderrobot.SpiderRobot;
import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.entity.arrow.BerthaHit;
import com.swbr.orespawn.entity.arrow.IrukandjiArrow;
import com.swbr.orespawn.entity.arrow.UltimateArrow;
import com.swbr.orespawn.entity.arrow.UltimateFishHook;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Frog;
import com.swbr.orespawn.entity.aquatic.GoldFish;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.arthropod.Bee;
import com.swbr.orespawn.entity.arthropod.CaveFisher;
import com.swbr.orespawn.entity.arthropod.EmperorScorpion;
import com.swbr.orespawn.entity.arthropod.HerculesBeetle;
import com.swbr.orespawn.entity.arthropod.Scorpion;
import com.swbr.orespawn.entity.arthropod.SpitBug;
import com.swbr.orespawn.entity.arthropod.TrooperBug;
import com.swbr.orespawn.entity.cannonfodder.Chipmunk;
import com.swbr.orespawn.entity.cannonfodder.Gazelle;
import com.swbr.orespawn.entity.cannonfodder.Lizard;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.companion.Shoes;
import com.swbr.orespawn.entity.cow.CrystalCow;
import com.swbr.orespawn.entity.cow.EnchantedCow;
import com.swbr.orespawn.entity.cow.GoldCow;
import com.swbr.orespawn.entity.cow.RedCow;
import com.swbr.orespawn.entity.critter.CliffRacer;
import com.swbr.orespawn.entity.critter.Cockateil;
import com.swbr.orespawn.entity.critter.Coin;
import com.swbr.orespawn.entity.critter.Cricket;
import com.swbr.orespawn.entity.critter.Dragonfly;
import com.swbr.orespawn.entity.critter.RubyBird;
import com.swbr.orespawn.entity.critter.Tshirt;
import com.swbr.orespawn.entity.dino.Alosaurus;
import com.swbr.orespawn.entity.dino.Basilisk;
import com.swbr.orespawn.entity.dino.Cryolophosaurus;
import com.swbr.orespawn.entity.dino.Nastysaurus;
import com.swbr.orespawn.entity.dino.Pointysaurus;
import com.swbr.orespawn.entity.dino.TRex;
import com.swbr.orespawn.entity.ender.EnderKnight;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.fairy.Fairy;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.herbivore.Baryonyx;
import com.swbr.orespawn.entity.herbivore.Beaver;
import com.swbr.orespawn.entity.herbivore.Camarasaurus;
import com.swbr.orespawn.entity.herbivore.Cassowary;
import com.swbr.orespawn.entity.herbivore.Hydrolisc;
import com.swbr.orespawn.entity.herbivore.Peacock;
import com.swbr.orespawn.entity.herbivore.StinkBug;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityLunaMoth;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.monster.Alien;
import com.swbr.orespawn.entity.monster.BandP;
import com.swbr.orespawn.entity.monster.Hammerhead;
import com.swbr.orespawn.entity.monster.Kyuubi;
import com.swbr.orespawn.entity.monster.LeafMonster;
import com.swbr.orespawn.entity.monster.Molenoid;
import com.swbr.orespawn.entity.portal.EntityAnt;
import com.swbr.orespawn.entity.portal.EntityRainbowAnt;
import com.swbr.orespawn.entity.portal.EntityRedAnt;
import com.swbr.orespawn.entity.portal.EntityUnstableAnt;
import com.swbr.orespawn.entity.portal.Termite;
import com.swbr.orespawn.entity.projectile.Acid;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.DeadIrukandji;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.InkSack;
import com.swbr.orespawn.entity.projectile.LaserBall;
import com.swbr.orespawn.entity.projectile.SunspotUrchin;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.entity.projectile.WaterBall;
import com.swbr.orespawn.entity.rider.Ostrich;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import com.swbr.orespawn.entity.rock.EntityThrownRock;
import com.swbr.orespawn.entity.robot.GiantRobot;
import com.swbr.orespawn.entity.robot.Robot1;
import com.swbr.orespawn.entity.robot.Robot2;
import com.swbr.orespawn.entity.robot.Robot3;
import com.swbr.orespawn.entity.robot.Robot4;
import com.swbr.orespawn.entity.robot.Robot5;
import com.swbr.orespawn.entity.rock.RockBase;
import com.swbr.orespawn.entity.vehicle.Elevator;
import com.swbr.orespawn.entity.worm.WormLarge;
import com.swbr.orespawn.entity.worm.WormMedium;
import com.swbr.orespawn.entity.worm.WormSmall;
import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.entity.sea.CloudShark;
import com.swbr.orespawn.entity.sea.Irukandji;
import com.swbr.orespawn.entity.sea.SeaMonster;
import com.swbr.orespawn.entity.sea.SeaViper;
import com.swbr.orespawn.entity.sea.Skate;
import com.swbr.orespawn.entity.sea.Urchin;
import com.swbr.orespawn.entity.terror.Crab;
import com.swbr.orespawn.entity.terror.CreepingHorror;
import com.swbr.orespawn.entity.terror.LurkingTerror;
import com.swbr.orespawn.entity.terror.Mantis;
import com.swbr.orespawn.entity.terror.Rat;
import com.swbr.orespawn.entity.terror.TerribleTerror;
import com.swbr.orespawn.entity.crystal.DungeonBeast;
import com.swbr.orespawn.entity.crystal.Rotator;
import com.swbr.orespawn.entity.crystal.Vortex;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.entity.triffid.Triffid;
import com.swbr.orespawn.entity.island.Island;
import com.swbr.orespawn.entity.island.IslandToo;
import com.swbr.orespawn.entity.moth.Brutalfly;
import com.swbr.orespawn.entity.moth.CaterKiller;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.cage.EntityCage;
import com.swbr.orespawn.entity.easter.EasterBunny;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity types. 134 in the original (OreSpawnMain.java:3057-4177, manifest {@code entities}),
 * one {@code EntityType} per manifest entry (DECISIONS R9): {@code MobCategory} from the
 * {@code addSpawn} type, hitbox from the first {@code size} pair, tracking range in chunks =
 * original / 16.
 *
 * <p>There is no {@code DeferredRegister.Entities} in NeoForge 21.1.248 - only the generic form
 * (STYLE.md, traps). {@code BetterFireball} and {@code ThunderBolt} had no registration in 1.7.10
 * and get new ids in W04 (catalogue 6.6).
 *
 * <p>Holders are written in the order of their {@code registerModEntity} lines; the two
 * unregistered W04 projectiles follow at the end of their wave block.
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OreSpawn.MOD_ID);

    // =====================================================================================
    // W04 - projectiles, arrows, rocks, Girlfriend, Boyfriend, hoverboard.
    // setShouldReceiveVelocityUpdates is the NeoForge builder method (EntityType.java:1384).
    // =====================================================================================

    /** :3060; vanilla EntityFishHook tracker 64/5/true, {@code isImmuneToFire}; size 0.25 (EntityFishHook ctor). */
    public static final DeferredHolder<EntityType<?>, EntityType<UltimateFishHook>> ULTIMATE_FISH_HOOK = ENTITY_TYPES.register("ultimate_fish_hook",
            () -> EntityType.Builder.<UltimateFishHook>of(UltimateFishHook::new, MobCategory.MISC).sized(0.25f, 0.25f).fireImmune()
                    .clientTrackingRange(4).updateInterval(5).setShouldReceiveVelocityUpdates(true).build("ultimate_fish_hook"));

    // The throwables of OreSpawnMain.java:3064-3102: registerModEntity(..., 64, 1, true) -> 4 chunks, interval 1,
    // velocity updates on (R9); size 0.25 from EntityThrowable.setSize (bytecode zk.<init>).
    public static final DeferredHolder<EntityType<?>, EntityType<SunspotUrchin>> SUNSPOT_URCHIN = ENTITY_TYPES.register("sunspot_urchin",
            () -> EntityType.Builder.<SunspotUrchin>of(SunspotUrchin::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("sunspot_urchin")); // :3066
    public static final DeferredHolder<EntityType<?>, EntityType<WaterBall>> WATER_BALL = ENTITY_TYPES.register("water_ball",
            () -> EntityType.Builder.<WaterBall>of(WaterBall::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("water_ball")); // :3072
    public static final DeferredHolder<EntityType<?>, EntityType<InkSack>> INK_SACK = ENTITY_TYPES.register("ink_sack",
            () -> EntityType.Builder.<InkSack>of(InkSack::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("ink_sack")); // :3078
    public static final DeferredHolder<EntityType<?>, EntityType<LaserBall>> LASER_BALL = ENTITY_TYPES.register("laser_ball",
            () -> EntityType.Builder.<LaserBall>of(LaserBall::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("laser_ball")); // :3084
    public static final DeferredHolder<EntityType<?>, EntityType<IceBall>> ICE_BALL = ENTITY_TYPES.register("ice_ball",
            () -> EntityType.Builder.<IceBall>of(IceBall::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("ice_ball")); // :3090
    public static final DeferredHolder<EntityType<?>, EntityType<Acid>> ACID = ENTITY_TYPES.register("acid",
            () -> EntityType.Builder.<Acid>of(Acid::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("acid")); // :3096
    public static final DeferredHolder<EntityType<?>, EntityType<DeadIrukandji>> DEAD_IRUKANDJI = ENTITY_TYPES.register("dead_irukandji",
            () -> EntityType.Builder.<DeadIrukandji>of(DeadIrukandji::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("dead_irukandji")); // :3102

    /** :3108 registerModEntity 64/1/true; size 0.33 (BerthaHit ctor). Not living, invisible (NoopRenderer). */
    public static final DeferredHolder<EntityType<?>, EntityType<BerthaHit>> BERTHA_HIT = ENTITY_TYPES.register("bertha_hit",
            () -> EntityType.Builder.<BerthaHit>of(BerthaHit::new, MobCategory.MISC).sized(0.33f, 0.33f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("bertha_hit"));

    /** :3118-3120, tracking 64/1/true; EntityThrowable default size 0.25. */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityThrownRock>> ENTITY_THROWN_ROCK = ENTITY_TYPES.register("entity_thrown_rock",
            () -> EntityType.Builder.<EntityThrownRock>of(EntityThrownRock::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true)
                    .build("entity_thrown_rock"));

    /**
     * Girlfriend: registerModEntity "Girlfriend" 64/1/false (:3235-3239), size 0.5x1.6 (Girlfriend.java:120),
     * {@code isImmuneToFire} (:124). CREATURE from the addSpawn type (R9).
     */
    public static final DeferredHolder<EntityType<?>, EntityType<Girlfriend>> GIRLFRIEND = ENTITY_TYPES.register("girlfriend",
            () -> EntityType.Builder.<Girlfriend>of(Girlfriend::new, MobCategory.CREATURE).sized(0.5f, 1.6f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("girlfriend"));

    // W06 cows: registerModEntity 64/1/false -> 4 chunks; EntityCow size 0.9 x 1.3 (bytecode wh.<init>); addSpawn creature.
    // The holder names are load-bearing: getBreedOffspring calls ModEntities.X.get().create(level).
    /** "Apple Cow" :3247. */
    public static final DeferredHolder<EntityType<?>, EntityType<RedCow>> APPLE_COW = ENTITY_TYPES.register("apple_cow",
            () -> EntityType.Builder.<RedCow>of(RedCow::new, MobCategory.CREATURE).sized(0.9f, 1.3f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("apple_cow"));
    /** "Golden Apple Cow" :3255. */
    public static final DeferredHolder<EntityType<?>, EntityType<GoldCow>> GOLDEN_APPLE_COW = ENTITY_TYPES.register("golden_apple_cow",
            () -> EntityType.Builder.<GoldCow>of(GoldCow::new, MobCategory.CREATURE).sized(0.9f, 1.3f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("golden_apple_cow"));
    /** "Enchanted Golden Apple Cow" :3263. */
    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedCow>> ENCHANTED_GOLDEN_APPLE_COW = ENTITY_TYPES.register("enchanted_golden_apple_cow",
            () -> EntityType.Builder.<EnchantedCow>of(EnchantedCow::new, MobCategory.CREATURE).sized(0.9f, 1.3f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("enchanted_golden_apple_cow"));

    // =====================================================================================
    // W05 - portal creatures (ants, termite, butterfly, moth) and the other insects.
    // =====================================================================================

    /** :3271 registerModEntity 32/1/false; size 0.4 (EntityButterfly.java:41); addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityButterfly>> BUTTERFLY = ENTITY_TYPES.register("butterfly",
            () -> EntityType.Builder.<EntityButterfly>of(EntityButterfly::new, MobCategory.AMBIENT).sized(0.4f, 0.4f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("butterfly"));
    /** :3279 registerModEntity 32/1/false; size 0.5 (EntityLunaMoth.java:27). */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityLunaMoth>> MOTH = ENTITY_TYPES.register("moth",
            () -> EntityType.Builder.<EntityLunaMoth>of(EntityLunaMoth::new, MobCategory.AMBIENT).sized(0.5f, 0.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("moth"));
    /** :3287 registerModEntity 16/1/false; size 0.2 (EntityMosquito.java:18). */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityMosquito>> MOSQUITO = ENTITY_TYPES.register("mosquito",
            () -> EntityType.Builder.<EntityMosquito>of(EntityMosquito::new, MobCategory.AMBIENT).sized(0.2f, 0.2f)
                    .clientTrackingRange(1).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("mosquito"));
    /** :3295 registerModEntity 64/1/false; size 0.4 x 0.8 (Firefly.java:27). */
    public static final DeferredHolder<EntityType<?>, EntityType<Firefly>> FIREFLY = ENTITY_TYPES.register("firefly",
            () -> EntityType.Builder.<Firefly>of(Firefly::new, MobCategory.AMBIENT).sized(0.4f, 0.8f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("firefly"));
    /** W07 - "Bee" :3303 registerModEntity 64/1/false; setSize(1.5f, 2.5f) Bee.java:34; addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Bee>> BEE = ENTITY_TYPES.register("bee",
            () -> EntityType.Builder.<Bee>of(Bee::new, MobCategory.AMBIENT).sized(1.5f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("bee"));
    /** W08 - "Mothra" :3311 registerModEntity 128/1/false; setSize(5.0f, 2.0f) Mothra.java:40; isImmuneToFire (:43); addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Mothra>> MOTHRA = ENTITY_TYPES.register("mothra",
            () -> EntityType.Builder.<Mothra>of(Mothra::new, MobCategory.AMBIENT).sized(5.0f, 2.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("mothra"));

    // Portal ants: registerModEntity(..., 16, 1, false) -> 1 chunk; EntityAnimal base -> CREATURE (R9).
    /** "Ant" :3315-3319, setSize(0.1f, 0.1f) EntityAnt.java:25. */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityAnt>> ANT = ENTITY_TYPES.register("ant",
            () -> EntityType.Builder.<EntityAnt>of(EntityAnt::new, MobCategory.CREATURE).sized(0.1f, 0.1f)
                    .clientTrackingRange(1).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("ant"));
    /** "Red Ant" :3323-3327, setSize(0.2f, 0.2f) EntityRedAnt.java:18. */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityRedAnt>> RED_ANT = ENTITY_TYPES.register("red_ant",
            () -> EntityType.Builder.<EntityRedAnt>of(EntityRedAnt::new, MobCategory.CREATURE).sized(0.2f, 0.2f)
                    .clientTrackingRange(1).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("red_ant"));
    /** "Rainbow Ant" :3331-3335, setSize(0.1f, 0.1f). */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityRainbowAnt>> RAINBOW_ANT = ENTITY_TYPES.register("rainbow_ant",
            () -> EntityType.Builder.<EntityRainbowAnt>of(EntityRainbowAnt::new, MobCategory.CREATURE).sized(0.1f, 0.1f)
                    .clientTrackingRange(1).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("rainbow_ant"));
    /** "Unstable Ant" :3339-3343, setSize(0.1f, 0.1f). */
    public static final DeferredHolder<EntityType<?>, EntityType<EntityUnstableAnt>> UNSTABLE_ANT = ENTITY_TYPES.register("unstable_ant",
            () -> EntityType.Builder.<EntityUnstableAnt>of(EntityUnstableAnt::new, MobCategory.CREATURE).sized(0.1f, 0.1f)
                    .clientTrackingRange(1).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("unstable_ant"));

    // =====================================================================================
    // W07 - hostile land mobs, robots, worms, :3351-:3407 (between the ants and Camarasaurus).
    // Robots: Village/Chaos spawnableMonsterList -> MONSTER, isImmuneToFire -> fireImmune().
    // =====================================================================================

    /** "Bomb-Omb" :3351 registerModEntity 32/1/false; setSize(0.5f, 0.5f) Robot1.java:24; isImmuneToFire :28. */
    public static final DeferredHolder<EntityType<?>, EntityType<Robot1>> BOMB_OMB = ENTITY_TYPES.register("bomb_omb",
            () -> EntityType.Builder.<Robot1>of(Robot1::new, MobCategory.MONSTER).sized(0.5f, 0.5f).fireImmune()
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("bomb_omb"));
    /** "Robo-Pounder" :3359 registerModEntity 64/1/false; setSize(3.0f, 6.2f) Robot2.java:29; isImmuneToFire :33. */
    public static final DeferredHolder<EntityType<?>, EntityType<Robot2>> ROBO_POUNDER = ENTITY_TYPES.register("robo_pounder",
            () -> EntityType.Builder.<Robot2>of(Robot2::new, MobCategory.MONSTER).sized(3.0f, 6.2f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("robo_pounder"));
    /** "Robo-Gunner" :3367 registerModEntity 64/1/false; setSize(2.5f, 5.0f) Robot3.java:28; isImmuneToFire :32. */
    public static final DeferredHolder<EntityType<?>, EntityType<Robot3>> ROBO_GUNNER = ENTITY_TYPES.register("robo_gunner",
            () -> EntityType.Builder.<Robot3>of(Robot3::new, MobCategory.MONSTER).sized(2.5f, 5.0f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("robo_gunner"));
    /** "Robo-Warrior" :3375 registerModEntity 64/1/false; setSize(2.5f, 4.0f) Robot4.java:31; isImmuneToFire :35. */
    public static final DeferredHolder<EntityType<?>, EntityType<Robot4>> ROBO_WARRIOR = ENTITY_TYPES.register("robo_warrior",
            () -> EntityType.Builder.<Robot4>of(Robot4::new, MobCategory.MONSTER).sized(2.5f, 4.0f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("robo_warrior"));
    /** "Robo-Sniper" :3383 registerModEntity 64/1/false; setSize(1.0f, 2.25f) Robot5.java:27; isImmuneToFire :31. */
    public static final DeferredHolder<EntityType<?>, EntityType<Robot5>> ROBO_SNIPER = ENTITY_TYPES.register("robo_sniper",
            () -> EntityType.Builder.<Robot5>of(Robot5::new, MobCategory.MONSTER).sized(1.0f, 2.25f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("robo_sniper"));
    /** "Alosaurus" :3391 registerModEntity 64/1/false; setSize(1.9f, 3.6f) Alosaurus.java:24; EntityMob without addSpawn -> MONSTER (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<Alosaurus>> ALOSAURUS = ENTITY_TYPES.register("alosaurus",
            () -> EntityType.Builder.<Alosaurus>of(Alosaurus::new, MobCategory.MONSTER).sized(1.9f, 3.6f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("alosaurus"));
    /** "Cryolophosaurus" :3399 registerModEntity 64/1/false; setSize(0.75f, 0.75f) Cryolophosaurus.java:21; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<Cryolophosaurus>> CRYOLOPHOSAURUS = ENTITY_TYPES.register("cryolophosaurus",
            () -> EntityType.Builder.<Cryolophosaurus>of(Cryolophosaurus::new, MobCategory.MONSTER).sized(0.75f, 0.75f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cryolophosaurus"));
    /** "Basilisk" :3407 registerModEntity 64/1/false; setSize(1.6f, 3.5f) Basilisk.java:29; isImmuneToFire (:32); addSpawn type ambient -> AMBIENT (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Basilisk>> BASILISK = ENTITY_TYPES.register("basilisk",
            () -> EntityType.Builder.<Basilisk>of(Basilisk::new, MobCategory.AMBIENT).sized(1.6f, 3.5f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("basilisk"));

    // =====================================================================================
    // W06 - peaceful animals and battle mobs, :3415-:3499 (between the ants and the hoverboard).
    // =====================================================================================

    /** "Camarasaurus" :3415 registerModEntity 64/1/false; setSize(0.5f, 1.2f) Camarasaurus.java:29; EntityTameable without addSpawn -> CREATURE (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<Camarasaurus>> CAMARASAURUS = ENTITY_TYPES.register("camarasaurus",
            () -> EntityType.Builder.<Camarasaurus>of(Camarasaurus::new, MobCategory.CREATURE).sized(0.5f, 1.2f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("camarasaurus"));
    /** "Hydrolisc" :3423 registerModEntity 64/1/false; setSize(0.5f, 0.5f) Hydrolisc.java:29; addSpawn type creature. */
    public static final DeferredHolder<EntityType<?>, EntityType<Hydrolisc>> HYDROLISC = ENTITY_TYPES.register("hydrolisc",
            () -> EntityType.Builder.<Hydrolisc>of(Hydrolisc::new, MobCategory.CREATURE).sized(0.5f, 0.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("hydrolisc"));
    /**
     * "Velocity Raptor" :3427/:3431, 64/1/false; setSize(0.5f, 0.6f) VelocityRaptor.java:29; AMBIENT from the
     * ChunkProviderOreSpawn2 ambient list (:394). The id is load-bearing: EntityCannonFodder.isVelocityRaptor compares it.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<VelocityRaptor>> VELOCITY_RAPTOR = ENTITY_TYPES.register("velocity_raptor",
            () -> EntityType.Builder.<VelocityRaptor>of(VelocityRaptor::new, MobCategory.AMBIENT).sized(0.5f, 0.6f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("velocity_raptor"));
    /** "Dragonfly" :3435-3439 registerModEntity 64/1/false; size 1.5 x 0.5 (Dragonfly.java:21); addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Dragonfly>> DRAGONFLY = ENTITY_TYPES.register("dragonfly",
            () -> EntityType.Builder.<Dragonfly>of(Dragonfly::new, MobCategory.AMBIENT).sized(1.5f, 0.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("dragonfly"));
    /** W07 - "Emperor Scorpion" :3447 registerModEntity 64/1/false; setSize(3.5f, 3.0f) EmperorScorpion.java:31; isImmuneToFire :35; addSpawn ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<EmperorScorpion>> EMPEROR_SCORPION = ENTITY_TYPES.register("emperor_scorpion",
            () -> EntityType.Builder.<EmperorScorpion>of(EmperorScorpion::new, MobCategory.AMBIENT).sized(3.5f, 3.0f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("emperor_scorpion"));
    /** W07 - "Scorpion" :3455 registerModEntity 32/1/false; setSize(0.85f, 0.55f) Scorpion.java:26; addSpawn ambient. EmperorScorpion summons it by this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<Scorpion>> SCORPION = ENTITY_TYPES.register("scorpion",
            () -> EntityType.Builder.<Scorpion>of(Scorpion::new, MobCategory.AMBIENT).sized(0.85f, 0.55f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("scorpion"));
    /** W07 - "CaveFisher" :3463 registerModEntity 32/1/false; setSize(1.35f, 0.75f) CaveFisher.java:26; only monster lists (Mining, Utopia) -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<CaveFisher>> CAVE_FISHER = ENTITY_TYPES.register("cave_fisher",
            () -> EntityType.Builder.<CaveFisher>of(CaveFisher::new, MobCategory.MONSTER).sized(1.35f, 0.75f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cave_fisher"));
    /** W09 - "Baby Dragon" (class Spyro) :3471 registerModEntity 64/1/false; setSize(0.5f, 0.5f) Spyro.java:41; isImmuneToFire (:44); addSpawn type monster (ChunkProviderOreSpawn2.java:382, spawns.json). */
    public static final DeferredHolder<EntityType<?>, EntityType<Spyro>> BABY_DRAGON = ENTITY_TYPES.register("baby_dragon",
            () -> EntityType.Builder.<Spyro>of(Spyro::new, MobCategory.MONSTER).sized(0.5f, 0.5f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("baby_dragon"));
    /** "Baryonyx" :3479 registerModEntity 64/1/false; setSize(1.5f, 2.8f) Baryonyx.java:29; EntityAnimal without addSpawn -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<Baryonyx>> BARYONYX = ENTITY_TYPES.register("baryonyx",
            () -> EntityType.Builder.<Baryonyx>of(Baryonyx::new, MobCategory.CREATURE).sized(1.5f, 2.8f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("baryonyx"));
    /** W09 - "WTF?" (class GammaMetroid) :3483 registerModEntity 64/1/false; setSize(1.5f, 1.5f) GammaMetroid.java:35; monster lists of ChunkProviderOreSpawn2/BiomeGenUtopianPlains -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<GammaMetroid>> WTF = ENTITY_TYPES.register("wtf",
            () -> EntityType.Builder.<GammaMetroid>of(GammaMetroid::new, MobCategory.MONSTER).sized(1.5f, 1.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("wtf"));
    /** "Bird" :3491-3495 registerModEntity 32/1/false; size 0.5 (Cockateil.java:37); addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Cockateil>> BIRD = ENTITY_TYPES.register("bird",
            () -> EntityType.Builder.<Cockateil>of(Cockateil::new, MobCategory.AMBIENT).sized(0.5f, 0.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("bird"));
    /** "Ruby Bird" :3499-3503 registerModEntity 32/1/false; size 0.5 (inherited); no addSpawn, EntityAnimal base -> CREATURE (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<RubyBird>> RUBY_BIRD = ENTITY_TYPES.register("ruby_bird",
            () -> EntityType.Builder.<RubyBird>of(RubyBird::new, MobCategory.CREATURE).sized(0.5f, 0.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("ruby_bird"));
    /** W07 - "Kyuubi" :3511 registerModEntity 64/1/false; setSize(0.5f, 1.25f) Kyuubi.java:23; isImmuneToFire (:27); addSpawn type monster (hell). */
    public static final DeferredHolder<EntityType<?>, EntityType<Kyuubi>> KYUUBI = ENTITY_TYPES.register("kyuubi",
            () -> EntityType.Builder.<Kyuubi>of(Kyuubi::new, MobCategory.MONSTER).sized(0.5f, 1.25f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("kyuubi"));
    /** W09 - "Water Dragon" :3515 registerModEntity 64/1/false; setSize(1.25f, 1.9f) WaterDragon.java:43; isImmuneToFire (:47); addSpawn type waterCreature. */
    public static final DeferredHolder<EntityType<?>, EntityType<WaterDragon>> WATER_DRAGON = ENTITY_TYPES.register("water_dragon",
            () -> EntityType.Builder.<WaterDragon>of(WaterDragon::new, MobCategory.WATER_CREATURE).sized(1.25f, 1.9f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("water_dragon"));
    /** W08 - "Attack Squid" :3527 registerModEntity 32/1/false; setSize(1.0f, 1.25f) AttackSquid.java:39; addSpawn type waterCreature. ItemSquidZooka reads this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<AttackSquid>> ATTACK_SQUID = ENTITY_TYPES.register("attack_squid",
            () -> EntityType.Builder.<AttackSquid>of(AttackSquid::new, MobCategory.WATER_CREATURE).sized(1.0f, 1.25f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("attack_squid"));
    /** W07 - "Alien" :3535 registerModEntity 64/1/false; setSize(1.1f, 3.25f) Alien.java:38; ChunkProviderOreSpawn2 monster list -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<Alien>> ALIEN = ENTITY_TYPES.register("alien",
            () -> EntityType.Builder.<Alien>of(Alien::new, MobCategory.MONSTER).sized(1.1f, 3.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("alien"));

    /**
     * Elevator "Hoverboard" (:3539/:3543): hitbox 1.25 x 1.0 (Elevator.java:49), tracking 128 -> 8 chunks,
     * update interval 1, velocity updates; MISC (EntityLiving without addSpawn, R9).
     */
    public static final DeferredHolder<EntityType<?>, EntityType<Elevator>> HOVERBOARD = ENTITY_TYPES.register("hoverboard",
            () -> EntityType.Builder.<Elevator>of(Elevator::new, MobCategory.MISC)
                    .sized(1.25f, 1.0f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("hoverboard"));

    // W06, :3559-:3805 (between the hoverboard and the termite).

    /** "Lizard" :3559 registerModEntity 64/1/false; setSize(1.5f, 1.25f) Lizard.java:39; addSpawn type waterCreature -> WATER_CREATURE (R9/R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Lizard>> LIZARD = ENTITY_TYPES.register("lizard",
            () -> EntityType.Builder.<Lizard>of(Lizard::new, MobCategory.WATER_CREATURE).sized(1.5f, 1.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("lizard"));
    /** W09 - "Cephadrome" :3567 registerModEntity 128/1/true; setSize(2.5f, 2.25f) Cephadrome.java:58; addSpawn type ambient (icePlains, coldTaiga) -> AMBIENT (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Cephadrome>> CEPHADROME = ENTITY_TYPES.register("cephadrome",
            () -> EntityType.Builder.<Cephadrome>of(Cephadrome::new, MobCategory.AMBIENT).sized(2.5f, 2.25f)
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("cephadrome"));
    /** W09 - "Dragon" :3575 registerModEntity 128/1/true; setSize(1.5f, 1.25f) Dragon.java:77; isImmuneToFire (:81); addSpawn type ambient (BiomeGenUtopianPlains.java:84, R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Dragon>> DRAGON = ENTITY_TYPES.register("dragon",
            () -> EntityType.Builder.<Dragon>of(Dragon::new, MobCategory.AMBIENT).sized(1.5f, 1.25f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("dragon"));
    /** "Chipmunk" :3583 registerModEntity 32/1/false; setSize(0.35f, 0.35f) Chipmunk.java:22; addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Chipmunk>> CHIPMUNK = ENTITY_TYPES.register("chipmunk",
            () -> EntityType.Builder.<Chipmunk>of(Chipmunk::new, MobCategory.AMBIENT).sized(0.35f, 0.35f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("chipmunk"));
    /** "Gazelle" :3591 registerModEntity 64/1/false; setSize(0.6f, 1.8f) Gazelle.java:32; Utopia list creature, EntityTameable base -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<Gazelle>> GAZELLE = ENTITY_TYPES.register("gazelle",
            () -> EntityType.Builder.<Gazelle>of(Gazelle::new, MobCategory.CREATURE).sized(0.6f, 1.8f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("gazelle"));
    /** "Ostrich" :3595/:3599, 64/1/true; setSize(0.85f, 2.1f) Ostrich.java:40; addSpawn type ambient (:4428-4431). */
    public static final DeferredHolder<EntityType<?>, EntityType<Ostrich>> OSTRICH = ENTITY_TYPES.register("ostrich",
            () -> EntityType.Builder.<Ostrich>of(Ostrich::new, MobCategory.AMBIENT).sized(0.85f, 2.1f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("ostrich"));
    /** W07 - "Jumpy Bug" (class TrooperBug) :3607 registerModEntity 64/1/false; setSize(3.0f, 3.5f) TrooperBug.java:33; addSpawn ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<TrooperBug>> JUMPY_BUG = ENTITY_TYPES.register("jumpy_bug",
            () -> EntityType.Builder.<TrooperBug>of(TrooperBug::new, MobCategory.AMBIENT).sized(3.0f, 3.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("jumpy_bug"));
    /** W07 - "Spit Bug" :3615 registerModEntity 64/1/false; setSize(2.0f, 2.0f) SpitBug.java:34; addSpawn ambient. TrooperBug summons it by this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<SpitBug>> SPIT_BUG = ENTITY_TYPES.register("spit_bug",
            () -> EntityType.Builder.<SpitBug>of(SpitBug::new, MobCategory.AMBIENT).sized(2.0f, 2.0f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("spit_bug"));
    /** "Stink Bug" :3623 registerModEntity 32/1/false; setSize(0.55f, 0.55f) StinkBug.java:23; addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<StinkBug>> STINK_BUG = ENTITY_TYPES.register("stink_bug",
            () -> EntityType.Builder.<StinkBug>of(StinkBug::new, MobCategory.AMBIENT).sized(0.55f, 0.55f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("stink_bug"));
    /** "T-Shirt" :3627-3631 registerModEntity 32/1/false; size 4.0 (Tshirt.java:17); ambient from the VillageMania list (BiomeGenUtopianPlains.java:244). */
    public static final DeferredHolder<EntityType<?>, EntityType<Tshirt>> T_SHIRT = ENTITY_TYPES.register("t_shirt",
            () -> EntityType.Builder.<Tshirt>of(Tshirt::new, MobCategory.AMBIENT).sized(4.0f, 4.0f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("t_shirt"));
    /** W08 - "Island" :3635-3639 registerModEntity 64/1/false; setSize(0.5f, 0.5f) Island.java:38; EntityAnimal without addSpawn -> CREATURE (R9). IslandBlock reads this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<Island>> ISLAND = ENTITY_TYPES.register("island",
            () -> EntityType.Builder.<Island>of(Island::new, MobCategory.CREATURE).sized(0.5f, 0.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("island"));
    /** W08 - "IslandToo" :3643-3647 registerModEntity 64/1/false; setSize(0.5f, 0.5f) IslandToo.java:42; EntityAnimal without addSpawn -> CREATURE (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<IslandToo>> ISLAND_TOO = ENTITY_TYPES.register("island_too",
            () -> EntityType.Builder.<IslandToo>of(IslandToo::new, MobCategory.CREATURE).sized(0.5f, 0.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("island_too"));
    /** W08 - "Creeping Horror" :3655 registerModEntity 64/1/false; setSize(0.75f, 0.5f) CreepingHorror.java:21; Islands/Chaos monster lists -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<CreepingHorror>> CREEPING_HORROR = ENTITY_TYPES.register("creeping_horror",
            () -> EntityType.Builder.<CreepingHorror>of(CreepingHorror::new, MobCategory.MONSTER).sized(0.75f, 0.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("creeping_horror"));
    /** W08 - "Terrible Terror" :3663 registerModEntity 64/1/false; setSize(1.0f, 0.75f) TerribleTerror.java:23; Islands/Chaos monster lists -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<TerribleTerror>> TERRIBLE_TERROR = ENTITY_TYPES.register("terrible_terror",
            () -> EntityType.Builder.<TerribleTerror>of(TerribleTerror::new, MobCategory.MONSTER).sized(1.0f, 0.75f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("terrible_terror"));
    /** "Cliff Racer" :3667-3671 registerModEntity 32/1/false; size 0.75 x 0.5 (CliffRacer.java:18); ambient from the Islands/Chaos lists. */
    public static final DeferredHolder<EntityType<?>, EntityType<CliffRacer>> CLIFF_RACER = ENTITY_TYPES.register("cliff_racer",
            () -> EntityType.Builder.<CliffRacer>of(CliffRacer::new, MobCategory.AMBIENT).sized(0.75f, 0.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cliff_racer"));
    /** W08 - "Triffid" :3679 registerModEntity 64/1/false; setSize(2.0f, 4.0f) Triffid.java:28; EntityMob without addSpawn -> MONSTER. Island looks the id up. */
    public static final DeferredHolder<EntityType<?>, EntityType<Triffid>> TRIFFID = ENTITY_TYPES.register("triffid",
            () -> EntityType.Builder.<Triffid>of(Triffid::new, MobCategory.MONSTER).sized(2.0f, 4.0f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("triffid"));
    /** W08 - "Nightmare" (class PitchBlack) :3687 registerModEntity 64/1/false; setSize(2.0f, 3.0f) PitchBlack.java:36 (runtime 2.5*scale x 3.5*scale via getDefaultDimensions); Islands/Chaos monster lists -> MONSTER. CreepingHorror checks the id. */
    public static final DeferredHolder<EntityType<?>, EntityType<PitchBlack>> NIGHTMARE = ENTITY_TYPES.register("nightmare",
            () -> EntityType.Builder.<PitchBlack>of(PitchBlack::new, MobCategory.MONSTER).sized(2.0f, 3.0f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("nightmare"));
    /** W08 - "Lurking Terror" :3695 registerModEntity 64/1/false; setSize(1.75f, 1.25f) LurkingTerror.java:25; Islands/Chaos monster lists -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<LurkingTerror>> LURKING_TERROR = ENTITY_TYPES.register("lurking_terror",
            () -> EntityType.Builder.<LurkingTerror>of(LurkingTerror::new, MobCategory.MONSTER).sized(1.75f, 1.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("lurking_terror"));
    /** "Ghost" :3707/:3711, 32/1/false; setSize(0.5f, 1.5f) Ghost.java:19; addSpawn type ambient (:4443-4448). */
    public static final DeferredHolder<EntityType<?>, EntityType<Ghost>> GHOST = ENTITY_TYPES.register("ghost",
            () -> EntityType.Builder.<Ghost>of(Ghost::new, MobCategory.AMBIENT).sized(0.5f, 1.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("ghost"));
    /** "Ghost Pumpkin Skelly" :3715/:3719, 64/1/false; setSize(1.5f, 2.0f) GhostSkelly.java:21; addSpawn type ambient (:4450-4455). */
    public static final DeferredHolder<EntityType<?>, EntityType<GhostSkelly>> GHOST_PUMPKIN_SKELLY = ENTITY_TYPES.register("ghost_pumpkin_skelly",
            () -> EntityType.Builder.<GhostSkelly>of(GhostSkelly::new, MobCategory.AMBIENT).sized(1.5f, 2.0f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("ghost_pumpkin_skelly"));
    // W07 worms. The holder names SMALL_WORM and MEDIUM_WORM are load-bearing: WormLarge's brood reads them.
    /** "Small Worm" :3727 registerModEntity 32/1/false; setSize(0.25f, 1.0f) WormSmall.java:22; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<WormSmall>> SMALL_WORM = ENTITY_TYPES.register("small_worm",
            () -> EntityType.Builder.<WormSmall>of(WormSmall::new, MobCategory.MONSTER).sized(0.25f, 1.0f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("small_worm"));
    /** "Medium Worm" :3735 registerModEntity 64/1/false; setSize(0.5f, 2.0f) WormMedium.java:22; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<WormMedium>> MEDIUM_WORM = ENTITY_TYPES.register("medium_worm",
            () -> EntityType.Builder.<WormMedium>of(WormMedium::new, MobCategory.MONSTER).sized(0.5f, 2.0f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("medium_worm"));
    /** "Large Worm" :3743 registerModEntity 64/1/false; setSize(1.55f, 2.5f) WormLarge.java:23; addSpawn type creature (plains/savanna/savannaPlateau) -> CREATURE (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<WormLarge>> LARGE_WORM = ENTITY_TYPES.register("large_worm",
            () -> EntityType.Builder.<WormLarge>of(WormLarge::new, MobCategory.CREATURE).sized(1.55f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("large_worm"));
    /** "Cassowary" :3751 registerModEntity 64/1/false; setSize(0.5f, 1.2f) Cassowary.java:19; addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Cassowary>> CASSOWARY = ENTITY_TYPES.register("cassowary",
            () -> EntityType.Builder.<Cassowary>of(Cassowary::new, MobCategory.AMBIENT).sized(0.5f, 1.2f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cassowary"));
    /** W08 - "Cloud Shark" :3759 registerModEntity 64/1/false; setSize(1.0f, 0.75f) CloudShark.java:22; addSpawn type ambient (Islands/Chaos lists, R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<CloudShark>> CLOUD_SHARK = ENTITY_TYPES.register("cloud_shark",
            () -> EntityType.Builder.<CloudShark>of(CloudShark::new, MobCategory.AMBIENT).sized(1.0f, 0.75f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cloud_shark"));
    /** "Gold Fish" :3767, 32/1/false; size 0.75 x 0.5 (GoldFish.java:18); only cave-creature spawn lists -> AMBIENT. */
    public static final DeferredHolder<EntityType<?>, EntityType<GoldFish>> GOLD_FISH = ENTITY_TYPES.register("gold_fish",
            () -> EntityType.Builder.<GoldFish>of(GoldFish::new, MobCategory.AMBIENT).sized(0.75f, 0.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("gold_fish"));
    /** W07 - "Leaf Monster" :3775 registerModEntity 64/1/false; setSize(1.0f, 2.5f) LeafMonster.java:24; addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<LeafMonster>> LEAF_MONSTER = ENTITY_TYPES.register("leaf_monster",
            () -> EntityType.Builder.<LeafMonster>of(LeafMonster::new, MobCategory.AMBIENT).sized(1.0f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("leaf_monster"));
    /** W07 - "Ender Knight" :3785 registerModEntity 64/1/false; setSize(0.6f, 2.9f) EnderKnight.java:26; addSpawn type ambient (R9/R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<EnderKnight>> ENDER_KNIGHT = ENTITY_TYPES.register("ender_knight",
            () -> EntityType.Builder.<EnderKnight>of(EnderKnight::new, MobCategory.AMBIENT).sized(0.6f, 2.9f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("ender_knight"));
    /** W07 - "Ender Reaper" :3793 registerModEntity 64/1/false; setSize(0.7f, 2.9f) EnderReaper.java:26; addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<EnderReaper>> ENDER_REAPER = ENTITY_TYPES.register("ender_reaper",
            () -> EntityType.Builder.<EnderReaper>of(EnderReaper::new, MobCategory.AMBIENT).sized(0.7f, 2.9f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("ender_reaper"));
    /** "Beaver" :3805 registerModEntity 64/1/false; setSize(0.6f, 0.8f) Beaver.java:32; addSpawn type creature. */
    public static final DeferredHolder<EntityType<?>, EntityType<Beaver>> BEAVER = ENTITY_TYPES.register("beaver",
            () -> EntityType.Builder.<Beaver>of(Beaver::new, MobCategory.CREATURE).sized(0.6f, 0.8f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("beaver"));

    /** W05 - "Termite" :3809-3813 (tracking 32 -> 2 chunks), setSize(0.2f, 0.2f) Termite.java:29. */
    public static final DeferredHolder<EntityType<?>, EntityType<Termite>> TERMITE = ENTITY_TYPES.register("termite",
            () -> EntityType.Builder.<Termite>of(Termite::new, MobCategory.CREATURE).sized(0.2f, 0.2f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("termite"));

    /** W07 - "Fairy" :3821 registerModEntity 32/1/false; setSize(0.4f, 0.8f) Fairy.java:47; addSpawn type ambient. Read by FairySword and the crystalfairy block. */
    public static final DeferredHolder<EntityType<?>, EntityType<Fairy>> FAIRY = ENTITY_TYPES.register("fairy",
            () -> EntityType.Builder.<Fairy>of(Fairy::new, MobCategory.AMBIENT).sized(0.4f, 0.8f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("fairy"));

    // W06, :3829-:3937 (between the termite and Boyfriend).

    /** "Peacock" :3829 registerModEntity 64/1/false; setSize(0.65f, 1.2f) Peacock.java:31; addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Peacock>> PEACOCK = ENTITY_TYPES.register("peacock",
            () -> EntityType.Builder.<Peacock>of(Peacock::new, MobCategory.AMBIENT).sized(0.65f, 1.2f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("peacock"));
    /** W08 - "Rotator" :3837 registerModEntity 64/1/false; setSize(1.0f, 2.0f) Rotator.java:30; isImmuneToFire (:32); no addSpawn, EntityMob -> MONSTER (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<Rotator>> ROTATOR = ENTITY_TYPES.register("rotator",
            () -> EntityType.Builder.<Rotator>of(Rotator::new, MobCategory.MONSTER).sized(1.0f, 2.0f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("rotator"));
    /** W08 - "Vortex" :3845 registerModEntity 64/1/false; setSize(2.0f, 4.0f) Vortex.java:30; isImmuneToFire (:32); no addSpawn, EntityMob -> MONSTER (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<Vortex>> VORTEX = ENTITY_TYPES.register("vortex",
            () -> EntityType.Builder.<Vortex>of(Vortex::new, MobCategory.MONSTER).sized(2.0f, 4.0f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("vortex"));
    /** W08 - "Dungeon Beast" :3853 registerModEntity 64/1/false; setSize(1.15f, 1.1f) DungeonBeast.java:26; addSpawn ambient (roofedForest) -> AMBIENT (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<DungeonBeast>> DUNGEON_BEAST = ENTITY_TYPES.register("dungeon_beast",
            () -> EntityType.Builder.<DungeonBeast>of(DungeonBeast::new, MobCategory.AMBIENT).sized(1.15f, 1.1f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("dungeon_beast"));
    /** W08 - "Rat" :3861 registerModEntity 32/1/false; setSize(0.25f, 0.5f) Rat.java:28; addSpawn type ambient (R18). RatSword and CRYSTALRAT read this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<Rat>> RAT = ENTITY_TYPES.register("rat",
            () -> EntityType.Builder.<Rat>of(Rat::new, MobCategory.AMBIENT).sized(0.25f, 0.5f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("rat"));
    /** "Flounder" :3869, 32/1/false; size 0.55 x 0.25 (Flounder.java:28); only water-creature spawn lists -> WATER_CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<Flounder>> FLOUNDER = ENTITY_TYPES.register("flounder",
            () -> EntityType.Builder.<Flounder>of(Flounder::new, MobCategory.WATER_CREATURE).sized(0.55f, 0.25f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("flounder"));
    /** "Whale" :3877, 64/1/false; size 1.5 x 2.5 (Whale.java:33); addSpawn waterCreature. */
    public static final DeferredHolder<EntityType<?>, EntityType<Whale>> WHALE = ENTITY_TYPES.register("whale",
            () -> EntityType.Builder.<Whale>of(Whale::new, MobCategory.WATER_CREATURE).sized(1.5f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("whale"));
    /** W08 - "Irukandji" :3885 registerModEntity 32/1/false; setSize(0.25f, 0.25f) Irukandji.java:33; Crystal waterCreature list (BiomeGenUtopianPlains.java:176). */
    public static final DeferredHolder<EntityType<?>, EntityType<Irukandji>> IRUKANDJI = ENTITY_TYPES.register("irukandji",
            () -> EntityType.Builder.<Irukandji>of(Irukandji::new, MobCategory.WATER_CREATURE).sized(0.25f, 0.25f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("irukandji"));
    /** W08 - "Skate" :3893 registerModEntity 32/1/false; setSize(0.75f, 0.25f) Skate.java:33; waterCreature list (BiomeGenUtopianPlains.java:178-180). */
    public static final DeferredHolder<EntityType<?>, EntityType<Skate>> SKATE = ENTITY_TYPES.register("skate",
            () -> EntityType.Builder.<Skate>of(Skate::new, MobCategory.WATER_CREATURE).sized(0.75f, 0.25f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("skate"));
    /** W08 - "Crystal Urchin" (class Urchin) :3901 registerModEntity 64/1/false; setSize(1.35f, 2.1f) Urchin.java:28; isImmuneToFire (:32); Crystal/Chaos monster lists. */
    public static final DeferredHolder<EntityType<?>, EntityType<Urchin>> CRYSTAL_URCHIN = ENTITY_TYPES.register("crystal_urchin",
            () -> EntityType.Builder.<Urchin>of(Urchin::new, MobCategory.MONSTER).sized(1.35f, 2.1f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("crystal_urchin"));
    /** W08 - "Mantis" :3909 registerModEntity 64/1/false; setSize(2.5f, 3.25f) Mantis.java:32; addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Mantis>> MANTIS = ENTITY_TYPES.register("mantis",
            () -> EntityType.Builder.<Mantis>of(Mantis::new, MobCategory.AMBIENT).sized(2.5f, 3.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("mantis"));
    /** W07 - "Hercules Beetle" :3917 registerModEntity 64/1/false; setSize(3.25f, 2.75f) HerculesBeetle.java:28; isImmuneToFire :32; addSpawn ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<HerculesBeetle>> HERCULES_BEETLE = ENTITY_TYPES.register("hercules_beetle",
            () -> EntityType.Builder.<HerculesBeetle>of(HerculesBeetle::new, MobCategory.AMBIENT).sized(3.25f, 2.75f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("hercules_beetle"));
    /** W07 - "T. Rex" :3925 registerModEntity 64/1/false; setSize(2.0f, 4.2f) TRex.java:27; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<TRex>> T_REX = ENTITY_TYPES.register("t_rex",
            () -> EntityType.Builder.<TRex>of(TRex::new, MobCategory.MONSTER).sized(2.0f, 4.2f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("t_rex"));
    /** W09 - "Stinky" :3933 registerModEntity 64/1/false; setSize(0.75f, 0.75f) Stinky.java:43; isImmuneToFire :46; addSpawn types hell monster, mesa ambient, Islands cave list -> AMBIENT (majority; the spawn table adds each entry under its own category, R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Stinky>> STINKY = ENTITY_TYPES.register("stinky",
            () -> EntityType.Builder.<Stinky>of(Stinky::new, MobCategory.AMBIENT).sized(0.75f, 0.75f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("stinky"));
    /** "Coin" :3937-3941 registerModEntity 64/1/false; size 1.5 (Coin.java:19); addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Coin>> COIN = ENTITY_TYPES.register("coin",
            () -> EntityType.Builder.<Coin>of(Coin::new, MobCategory.AMBIENT).sized(1.5f, 1.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("coin"));

    /** Boyfriend: "Boyfriend" 64/1/false (:3973-3977), size 0.5x1.6 (Boyfriend.java:101), {@code isImmuneToFire} (:102). */
    public static final DeferredHolder<EntityType<?>, EntityType<Boyfriend>> BOYFRIEND = ENTITY_TYPES.register("boyfriend",
            () -> EntityType.Builder.<Boyfriend>of(Boyfriend::new, MobCategory.CREATURE).sized(0.5f, 1.6f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("boyfriend"));

    /** W07 - "Molenoid" :3993 registerModEntity 64/1/false; setSize(3.9f, 2.6f) Molenoid.java:25; addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<Molenoid>> MOLENOID = ENTITY_TYPES.register("molenoid",
            () -> EntityType.Builder.<Molenoid>of(Molenoid::new, MobCategory.AMBIENT).sized(3.9f, 2.6f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("molenoid"));
    /** W08 - "Sea Monster" :4001 registerModEntity 64/1/false; setSize(1.25f, 2.5f) SeaMonster.java:39; addSpawn type waterCreature. */
    public static final DeferredHolder<EntityType<?>, EntityType<SeaMonster>> SEA_MONSTER = ENTITY_TYPES.register("sea_monster",
            () -> EntityType.Builder.<SeaMonster>of(SeaMonster::new, MobCategory.WATER_CREATURE).sized(1.25f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("sea_monster"));
    /** W08 - "Sea Viper" :4009 registerModEntity 64/1/false; setSize(1.5f, 2.5f) SeaViper.java:42; addSpawn type waterCreature. */
    public static final DeferredHolder<EntityType<?>, EntityType<SeaViper>> SEA_VIPER = ENTITY_TYPES.register("sea_viper",
            () -> EntityType.Builder.<SeaViper>of(SeaViper::new, MobCategory.WATER_CREATURE).sized(1.5f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("sea_viper"));
    /** W08 - "CaterKiller" :4025 registerModEntity 64/1/false; setSize(2.9f, 4.6f) CaterKiller.java:39 (PlayNicely 1.45x2.3 via getDefaultDimensions, R9); addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<CaterKiller>> CATER_KILLER = ENTITY_TYPES.register("cater_killer",
            () -> EntityType.Builder.<CaterKiller>of(CaterKiller::new, MobCategory.AMBIENT).sized(2.9f, 4.6f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cater_killer"));

    /** W06 - "Crystal Apple Cow" :4033, 64/1/false; EntityCow size; Crystal creature list -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalCow>> CRYSTAL_APPLE_COW = ENTITY_TYPES.register("crystal_apple_cow",
            () -> EntityType.Builder.<CrystalCow>of(CrystalCow::new, MobCategory.CREATURE).sized(0.9f, 1.3f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("crystal_apple_cow"));

    /** W09 - "Leonopteryx" (class Leon) :4041 registerModEntity 64/1/false; setSize(3.5f, 8.25f) Leon.java:63; Chaos addSpawn monster (BiomeGenUtopianPlains.java:425). */
    public static final DeferredHolder<EntityType<?>, EntityType<Leon>> LEONOPTERYX = ENTITY_TYPES.register("leonopteryx",
            () -> EntityType.Builder.<Leon>of(Leon::new, MobCategory.MONSTER).sized(3.5f, 8.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("leonopteryx"));
    /** W07 - "Hammerhead" :4049 registerModEntity 64/1/false; setSize(3.0f, 5.0f) Hammerhead.java:27; no addSpawn, EntityMob -> MONSTER (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<Hammerhead>> HAMMERHEAD = ENTITY_TYPES.register("hammerhead",
            () -> EntityType.Builder.<Hammerhead>of(Hammerhead::new, MobCategory.MONSTER).sized(3.0f, 5.0f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("hammerhead"));
    /** W09 - "Rubber Ducky" :4057 registerModEntity 64/1/false; setSize(0.33f, 0.5f) RubberDucky.java:44; addSpawn type waterCreature. RubberDucky.spawnCreature and spawnBabyAnimal read this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<RubberDucky>> RUBBER_DUCKY = ENTITY_TYPES.register("rubber_ducky",
            () -> EntityType.Builder.<RubberDucky>of(RubberDucky::new, MobCategory.WATER_CREATURE).sized(0.33f, 0.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("rubber_ducky"));
    /** W07 - "Criminal" (class BandP) :4073 registerModEntity 64/1/false; setSize(0.75f, 1.75f) BandP.java:34; addSpawn type ambient (R18). */
    public static final DeferredHolder<EntityType<?>, EntityType<BandP>> CRIMINAL = ENTITY_TYPES.register("criminal",
            () -> EntityType.Builder.<BandP>of(BandP::new, MobCategory.AMBIENT).sized(0.75f, 1.75f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("criminal"));

    /** RockBase "Rock" (:4077-4081), tracking 32/1/false; size 0.25x0.15 and fire immunity (RockBase.java:19-21). */
    public static final DeferredHolder<EntityType<?>, EntityType<RockBase>> ROCK = ENTITY_TYPES.register("rock",
            () -> EntityType.Builder.<RockBase>of(RockBase::new, MobCategory.MISC)
                    .sized(0.25f, 0.15f).fireImmune().clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false)
                    .build("rock"));
    /** W08 - "Brutalfly" :4089 registerModEntity 128/1/false; setSize(5.0f, 2.0f) Brutalfly.java:40; isImmuneToFire (:43); addSpawn type ambient (R18). CaterKiller spawns this holder. */
    public static final DeferredHolder<EntityType<?>, EntityType<Brutalfly>> BRUTALFLY = ENTITY_TYPES.register("brutalfly",
            () -> EntityType.Builder.<Brutalfly>of(Brutalfly::new, MobCategory.AMBIENT).sized(5.0f, 2.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("brutalfly"));

    /** W07 - "Nastysaurus" :4097 registerModEntity 128/1/false -> 8 chunks; setSize(2.2f, 4.6f) Nastysaurus.java:29; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<Nastysaurus>> NASTYSAURUS = ENTITY_TYPES.register("nastysaurus",
            () -> EntityType.Builder.<Nastysaurus>of(Nastysaurus::new, MobCategory.MONSTER).sized(2.2f, 4.6f)
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("nastysaurus"));
    /** W07 - "Pointysaurus" :4105 registerModEntity 64/1/false; setSize(2.9f, 2.9f) Pointysaurus.java:27; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<Pointysaurus>> POINTYSAURUS = ENTITY_TYPES.register("pointysaurus",
            () -> EntityType.Builder.<Pointysaurus>of(Pointysaurus::new, MobCategory.MONSTER).sized(2.9f, 2.9f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("pointysaurus"));

    // W06, :4109-:4129 (between the rock and the shoes).

    /** "Cricket" :4109-4113 registerModEntity 32/1/false -> 2 chunks; size 0.1 (Cricket.java:20); addSpawn type ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<Cricket>> CRICKET = ENTITY_TYPES.register("cricket",
            () -> EntityType.Builder.<Cricket>of(Cricket::new, MobCategory.AMBIENT).sized(0.1f, 0.1f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("cricket"));
    /** "Frog" :4129, 32/1/false; size 0.75 (Frog.java:27); first and heaviest addSpawn is waterCreature (river, swampland 20). */
    public static final DeferredHolder<EntityType<?>, EntityType<Frog>> FROG = ENTITY_TYPES.register("frog",
            () -> EntityType.Builder.<Frog>of(Frog::new, MobCategory.WATER_CREATURE).sized(0.75f, 0.75f)
                    .clientTrackingRange(2).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("frog"));

    /** W09 - "Robot Spider" (class SpiderRobot) :4141/:4145 registerModEntity 128/1/false; setSize(3.25f, 2.25f) SpiderRobot.java:52; isImmuneToFire :57; EntityLiving without addSpawn -> MISC (R9, Elevator precedent). */
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderRobot>> ROBOT_SPIDER = ENTITY_TYPES.register("robot_spider",
            () -> EntityType.Builder.<SpiderRobot>of(SpiderRobot::new, MobCategory.MISC).sized(3.25f, 2.25f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("robot_spider"));
    /** W09 - "Spider Driver" (class SpiderDriver) :4149/:4153 registerModEntity 64/1/false; EntitySpider setSize(1.4f, 0.9f) (javap yn.<init>); BiomeGenUtopianPlains monster list -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderDriver>> SPIDER_DRIVER = ENTITY_TYPES.register("spider_driver",
            () -> EntityType.Builder.<SpiderDriver>of(SpiderDriver::new, MobCategory.MONSTER).sized(1.4f, 0.9f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("spider_driver"));

    /** W07 - "Jeffery" (class GiantRobot) :4161 registerModEntity 128/1/false; setSize(3.0f, 9.75f) GiantRobot.java:28; isImmuneToFire :32; BiomeGenUtopianPlains.java:209 monster -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<GiantRobot>> JEFFERY = ENTITY_TYPES.register("jeffery",
            () -> EntityType.Builder.<GiantRobot>of(GiantRobot::new, MobCategory.MONSTER).sized(3.0f, 9.75f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("jeffery"));
    /** W09 - "Robot Red Ant" (class AntRobot) :4163-4169 registerModEntity 128/1/false; setSize(2.75f, 1.25f) AntRobot.java:41; isImmuneToFire :46; EntityLiving without addSpawn -> MISC (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<AntRobot>> ROBOT_RED_ANT = ENTITY_TYPES.register("robot_red_ant",
            () -> EntityType.Builder.<AntRobot>of(AntRobot::new, MobCategory.MISC).sized(2.75f, 1.25f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("robot_red_ant"));
    /** W08 - "Crab" :4177 registerModEntity 64/1/false; setSize(1.25f, 2.5f) Crab.java:38 (runtime rescale in getDefaultDimensions); addSpawn type waterCreature. */
    public static final DeferredHolder<EntityType<?>, EntityType<Crab>> CRAB = ENTITY_TYPES.register("crab",
            () -> EntityType.Builder.<Crab>of(Crab::new, MobCategory.WATER_CREATURE).sized(1.25f, 2.5f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("crab"));

    /** Shoes: "Shoes" 64/1/true (:4646-4648); EntityThrowable setSize(0.25f, 0.25f). */
    public static final DeferredHolder<EntityType<?>, EntityType<Shoes>> SHOES = ENTITY_TYPES.register("shoes",
            () -> EntityType.Builder.<Shoes>of(Shoes::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("shoes"));

    /** :5016; vanilla EntityArrow tracker 64/20/false (bytecode mn.a), EntityArrow size 0.5. */
    public static final DeferredHolder<EntityType<?>, EntityType<UltimateArrow>> ULTIMATE_ARROW = ENTITY_TYPES.register("ultimate_arrow",
            () -> EntityType.Builder.<UltimateArrow>of(UltimateArrow::new, MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(4).updateInterval(20).setShouldReceiveVelocityUpdates(false).build("ultimate_arrow"));
    /** :5020, same tracker values as {@link #ULTIMATE_ARROW}. */
    public static final DeferredHolder<EntityType<?>, EntityType<IrukandjiArrow>> IRUKANDJI_ARROW = ENTITY_TYPES.register("irukandji_arrow",
            () -> EntityType.Builder.<IrukandjiArrow>of(IrukandjiArrow::new, MobCategory.MISC).sized(0.5f, 0.5f)
                    .clientTrackingRange(4).updateInterval(20).setShouldReceiveVelocityUpdates(false).build("irukandji_arrow"));

    // Unregistered in 1.7.10 (catalogue 6.6), ids fixed by the W04 wave plan. BetterFireball fell under the vanilla
    // EntityTracker rule for EntityFireball (64, 10, no velocity) with EntityFireball's 1.0 size; ThunderBolt as the throwables.
    public static final DeferredHolder<EntityType<?>, EntityType<BetterFireball>> BETTER_FIREBALL = ENTITY_TYPES.register("better_fireball",
            () -> EntityType.Builder.<BetterFireball>of(BetterFireball::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10).setShouldReceiveVelocityUpdates(false).build("better_fireball"));
    public static final DeferredHolder<EntityType<?>, EntityType<ThunderBolt>> THUNDER_BOLT = ENTITY_TYPES.register("thunder_bolt",
            () -> EntityType.Builder.<ThunderBolt>of(ThunderBolt::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("thunder_bolt"));

    // W10 - bosses and royals, in registerModEntity line order of the original (OreSpawnMain.java).

    /** W10 - "PurplePower" :3114 registerModEntity 64/1/true; setSize(0.75f, 0.75f) PurplePower.java:27; isImmuneToFire :29; EntityLiving without addSpawn -> MISC (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<PurplePower>> PURPLE_POWER = ENTITY_TYPES.register("purple_power",
            () -> EntityType.Builder.<PurplePower>of(PurplePower::new, MobCategory.MISC).sized(0.75f, 0.75f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("purple_power"));
    /** W10 - "The Kraken" :3551 registerModEntity 128/1/false; setSize(4.0f, 15.0f) Kraken.java:50 (PlayNicely 1.3333334x5 via getDefaultDimensions, R9); no addSpawn, EntityMob -> MONSTER; isImmuneToFire (:58). */
    public static final DeferredHolder<EntityType<?>, EntityType<Kraken>> THE_KRAKEN = ENTITY_TYPES.register("the_kraken",
            () -> EntityType.Builder.<Kraken>of(Kraken::new, MobCategory.MONSTER).sized(4.0f, 15.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_kraken"));
    /** W10 - "Mobzilla" (class Godzilla) :3703 registerModEntity 128/1/false; setSize(9.9f, 25.0f) Godzilla.java:48 (PlayNicely 2.475x6.25 via getDefaultDimensions, R9); isImmuneToFire :64; BiomeGenUtopianPlains village list type monster -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<Godzilla>> MOBZILLA = ENTITY_TYPES.register("mobzilla",
            () -> EntityType.Builder.<Godzilla>of(Godzilla::new, MobCategory.MONSTER).sized(9.9f, 25.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("mobzilla"));
    /** W10 - "MobzillaHead" (class GodzillaHead) :3781 registerModEntity 128/10/true; setSize(9.9f, 10.0f) GodzillaHead.java:23; isImmuneToFire :26; EntityLiving without addSpawn -> MISC (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<GodzillaHead>> MOBZILLA_HEAD = ENTITY_TYPES.register("mobzilla_head",
            () -> EntityType.Builder.<GodzillaHead>of(GodzillaHead::new, MobCategory.MISC).sized(9.9f, 10.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(10).setShouldReceiveVelocityUpdates(true).build("mobzilla_head"));
    /** W10 - "The King" :3949 registerModEntity 128/1/false; setSize(22.0f, 24.0f) TheKing.java:64 (PlayNicely 5.5 x 6 via getDefaultDimensions); isImmuneToFire :71; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<TheKing>> THE_KING = ENTITY_TYPES.register("the_king",
            () -> EntityType.Builder.<TheKing>of(TheKing::new, MobCategory.MONSTER).sized(22.0f, 24.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_king"));
    /** W10 - "KingHead" :3955 registerModEntity 128/10/true; setSize(19.9f, 10.0f) KingHead.java:23; isImmuneToFire :26; EntityLiving without addSpawn -> MISC (R9). */
    public static final DeferredHolder<EntityType<?>, EntityType<KingHead>> KING_HEAD = ENTITY_TYPES.register("king_head",
            () -> EntityType.Builder.<KingHead>of(KingHead::new, MobCategory.MISC).sized(19.9f, 10.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(10).setShouldReceiveVelocityUpdates(true).build("king_head"));
    /** W10 - "The Queen" :3963 registerModEntity 128/1/false; setSize(22, 24) TheQueen.java:66 (PlayNicely size via getDefaultDimensions); isImmuneToFire; EntityMob without addSpawn -> MONSTER. */
    public static final DeferredHolder<EntityType<?>, EntityType<TheQueen>> THE_QUEEN = ENTITY_TYPES.register("the_queen",
            () -> EntityType.Builder.<TheQueen>of(TheQueen::new, MobCategory.MONSTER).sized(22.0f, 24.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_queen"));
    /** W10 - "QueenHead" :3969 registerModEntity 128/10/true; setSize(19.9, 10) QueenHead.java:23; isImmuneToFire; EntityLiving -> MISC. */
    public static final DeferredHolder<EntityType<?>, EntityType<QueenHead>> QUEEN_HEAD = ENTITY_TYPES.register("queen_head",
            () -> EntityType.Builder.<QueenHead>of(QueenHead::new, MobCategory.MISC).sized(19.9f, 10.0f).fireImmune()
                    .clientTrackingRange(8).updateInterval(10).setShouldReceiveVelocityUpdates(true).build("queen_head"));
    /** W10 - "The Prince" :3985 registerModEntity 64/1/false; setSize(0.75f, 1.25f) ThePrince.java:54; isImmuneToFire (:57); EntityTameable without addSpawn -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<ThePrince>> THE_PRINCE = ENTITY_TYPES.register("the_prince",
            () -> EntityType.Builder.<ThePrince>of(ThePrince::new, MobCategory.CREATURE).sized(0.75f, 1.25f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_prince"));
    /** W10 - "The Young Prince" (ThePrinceTeen) :4065 registerModEntity 64/1/false; setSize(3.25f, 4.25f) ThePrinceTeen.java:77; isImmuneToFire (:81); no addSpawn -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<ThePrinceTeen>> THE_YOUNG_PRINCE = ENTITY_TYPES.register("the_young_prince",
            () -> EntityType.Builder.<ThePrinceTeen>of(ThePrinceTeen::new, MobCategory.CREATURE).sized(3.25f, 4.25f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_young_prince"));
    /** W10 - "The Princess" :4121 registerModEntity 64/1/false; setSize(0.75f, 1.25f) ThePrincess.java:58; isImmuneToFire (:61); no addSpawn -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<ThePrincess>> THE_PRINCESS = ENTITY_TYPES.register("the_princess",
            () -> EntityType.Builder.<ThePrincess>of(ThePrincess::new, MobCategory.CREATURE).sized(0.75f, 1.25f).fireImmune()
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_princess"));
    /** W10 - "The Young Adult Prince" (class ThePrinceAdult) OreSpawnMain.java:4137 registerModEntity 128/1/false; setSize(6.25f, 10.25f) ThePrinceAdult.java:73; isImmuneToFire (:77); EntityTameable without addSpawn -> CREATURE. */
    public static final DeferredHolder<EntityType<?>, EntityType<ThePrinceAdult>> THE_YOUNG_ADULT_PRINCE = ENTITY_TYPES.register("the_young_adult_prince",
            () -> EntityType.Builder.<ThePrinceAdult>of(ThePrinceAdult::new, MobCategory.CREATURE).sized(6.25f, 10.25f).fireImmune()
                    .clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("the_young_adult_prince"));

    // W11 EntityCage (OreSpawnMain.java:4779-4781: registerModEntity 64, 1, true); EntityThrowable setSize 0.25.
    public static final DeferredHolder<EntityType<?>, EntityType<EntityCage>> ENTITY_CAGE = ENTITY_TYPES.register("entity_cage",
            () -> EntityType.Builder.<EntityCage>of(EntityCage::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("entity_cage"));
    /** W11 - "Easter Bunny" :4013 global, "EasterBunny" :4017 registerModEntity 64/1/false; setSize(0.5f, 0.75f) EasterBunny.java:20; addSpawn type ambient (:4342-4348). */
    public static final DeferredHolder<EntityType<?>, EntityType<EasterBunny>> EASTER_BUNNY = ENTITY_TYPES.register("easter_bunny",
            () -> EntityType.Builder.<EasterBunny>of(EasterBunny::new, MobCategory.AMBIENT).sized(0.5f, 0.75f)
                    .clientTrackingRange(4).updateInterval(1).setShouldReceiveVelocityUpdates(false).build("easter_bunny"));

    private ModEntities() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
