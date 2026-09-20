package com.swbr.orespawn.platform;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.dispenser.ProjectileDispenserSetup;
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
import com.swbr.orespawn.entity.easter.EasterBunny;
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
import com.swbr.orespawn.entity.rider.Ostrich;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
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
import com.swbr.orespawn.item.bow.MyDispenserBehaviorArrow;
import com.swbr.orespawn.item.rock.MyDispenserBehaviorRock;
import com.swbr.orespawn.registry.ModBlockEntities;
import com.swbr.orespawn.registry.ModCreativeTabs;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/**
 * Mod-bus setup shared by both sides - the successor of {@code CommonProxyOreSpawn} and of the
 * registration tail of {@code OreSpawnMain.make_some_more_things} (OreSpawnMain.java:5035-5065).
 *
 * <p>Each handler is a hub that later waves extend through the wave's {@code registry_entries};
 * the integrator owns this file. The network channel {@code "RiderControls"} of the common proxy
 * (CommonProxyOreSpawn.java:24-26) is registered by the network port on
 * {@code RegisterPayloadHandlersEvent}, not here.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CommonSetup {

    private CommonSetup() {}

    /**
     * {@code DoDispenserRegistrations} (OreSpawnMain.java:5299-5435): 134 dispenser behaviours,
     * registered inside {@code enqueueWork} because {@code DispenserBlock.registerBehavior} writes
     * to a plain map.
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Spawn eggs (all 114, whichever wave adds them): item.spawnegg.SpawnEggSetup walks
            // ItemSpawnEgg.all() in its own FMLCommonSetupEvent listener - nothing to add here.

            // W04: the Irukandji Arrow (:5415) with the 1.7.10 BehaviorProjectileDispense numbers.
            DispenserBlock.registerBehavior(ModItems.IRUKANDJI_ARROW.get(), new MyDispenserBehaviorArrow());
            // W04: laser ball, ice ball, water ball, acid, sunspot urchin, dead irukandji (:5416-5421).
            ProjectileDispenserSetup.register();
            // W04: the twelve rocks (:5422-5433).
            MyDispenserBehaviorRock.registerAll();
        });
    }

    /**
     * Attribute suppliers for every living entity type. The original set attributes per instance
     * in {@code applyEntityAttributes}; the port keeps the base values there too (they come from
     * {@code MobStats} at runtime, after the config is loaded) and registers only the attribute
     * set here (verhalten/core-02.md, MobStats).
     */
    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        // W04 - Girlfriend/Boyfriend (health 80, speed 0.3, attack 8), hoverboard (60/1.33/0), rock (1 + type/4 at spawn).
        event.put(ModEntities.GIRLFRIEND.get(), Girlfriend.createAttributes().build());
        event.put(ModEntities.HOVERBOARD.get(), Elevator.createAttributes().build());
        event.put(ModEntities.BOYFRIEND.get(), Boyfriend.createAttributes().build());
        event.put(ModEntities.ROCK.get(), RockBase.createAttributes().build());
        // W05 flyers - butterfly/moth health 2, mosquito 2, firefly 1; speed 0.1, attack 0 (applyEntityAttributes).
        event.put(ModEntities.BUTTERFLY.get(), EntityButterfly.createAttributes().build());
        event.put(ModEntities.MOTH.get(), EntityLunaMoth.createAttributes().build());
        event.put(ModEntities.MOSQUITO.get(), EntityMosquito.createAttributes().build());
        event.put(ModEntities.FIREFLY.get(), Firefly.createAttributes().build());
        // W05 ants - health 1/2/1/1/5, speed 0.15/0.2/0.15/0.15/0.2, attack 0/1/0/0/2.
        event.put(ModEntities.ANT.get(), EntityAnt.createAttributes().build());
        event.put(ModEntities.RED_ANT.get(), EntityRedAnt.createAttributes().build());
        event.put(ModEntities.RAINBOW_ANT.get(), EntityRainbowAnt.createAttributes().build());
        event.put(ModEntities.UNSTABLE_ANT.get(), EntityUnstableAnt.createAttributes().build());
        event.put(ModEntities.TERMITE.get(), Termite.createAttributes().build());
        // W06 cows (EntityCow health 10, speed 0.2) and water animals: gold fish 6/0.22/1, flounder 5/0.25/0,
        // whale 100/0.35/0, frog 8/0.1/0.
        event.put(ModEntities.APPLE_COW.get(), RedCow.createAttributes().build());
        event.put(ModEntities.GOLDEN_APPLE_COW.get(), RedCow.createAttributes().build());
        event.put(ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), RedCow.createAttributes().build());
        event.put(ModEntities.CRYSTAL_APPLE_COW.get(), RedCow.createAttributes().build());
        event.put(ModEntities.GOLD_FISH.get(), GoldFish.createAttributes().build());
        event.put(ModEntities.FLOUNDER.get(), Flounder.createAttributes().build());
        event.put(ModEntities.WHALE.get(), Whale.createAttributes().build());
        event.put(ModEntities.FROG.get(), Frog.createAttributes().build());
        // W06 critters - bird/ruby bird 2/0.33/1, cliff racer 5/0.33/1, dragonfly 10/0.33/2, cricket 3/0.15/0,
        // coin and t-shirt 1/0/0.
        event.put(ModEntities.DRAGONFLY.get(), Dragonfly.createAttributes().build());
        event.put(ModEntities.BIRD.get(), Cockateil.createAttributes().build());
        event.put(ModEntities.RUBY_BIRD.get(), RubyBird.createAttributes().build());
        event.put(ModEntities.T_SHIRT.get(), Tshirt.createAttributes().build());
        event.put(ModEntities.CLIFF_RACER.get(), CliffRacer.createAttributes().build());
        event.put(ModEntities.COIN.get(), Coin.createAttributes().build());
        event.put(ModEntities.CRICKET.get(), Cricket.createAttributes().build());
        // W06 herbivores - Baryonyx 40/0.25/8, Cassowary 10/0.25/8, Camarasaurus 20/0.2/1, Beaver 15/0.2/1,
        // Peacock 15/0.38/4, Stink Bug 5/0.15/0, Hydrolisc 100/0.25/1 plus armor 10.
        event.put(ModEntities.CAMARASAURUS.get(), Camarasaurus.createAttributes().build());
        event.put(ModEntities.HYDROLISC.get(), Hydrolisc.createAttributes().build());
        event.put(ModEntities.BARYONYX.get(), Baryonyx.createAttributes().build());
        event.put(ModEntities.STINK_BUG.get(), StinkBug.createAttributes().build());
        event.put(ModEntities.CASSOWARY.get(), Cassowary.createAttributes().build());
        // W11 Easter Bunny 10/0.45/8.
        event.put(ModEntities.EASTER_BUNNY.get(), EasterBunny.createAttributes().build());
        event.put(ModEntities.BEAVER.get(), Beaver.createAttributes().build());
        event.put(ModEntities.PEACOCK.get(), Peacock.createAttributes().build());
        // W06 cannon fodder - lizard 30/0.3/6, chipmunk 5/0.38/1, gazelle 15/0.3/0.
        event.put(ModEntities.LIZARD.get(), Lizard.createAttributes().build());
        event.put(ModEntities.CHIPMUNK.get(), Chipmunk.createAttributes().build());
        event.put(ModEntities.GAZELLE.get(), Gazelle.createAttributes().build());
        // W06 riders/ghosts - Velocity Raptor 10/0.55/2, Ostrich 25/0.38/6, Ghost 2/0.1/0, Ghost Pumpkin Skelly 5/0.1/0.
        event.put(ModEntities.VELOCITY_RAPTOR.get(), VelocityRaptor.createAttributes().build());
        event.put(ModEntities.OSTRICH.get(), Ostrich.createAttributes().build());
        event.put(ModEntities.GHOST.get(), Ghost.createAttributes().build());
        event.put(ModEntities.GHOST_PUMPKIN_SKELLY.get(), GhostSkelly.createAttributes().build());
        // W07 dinosaurs - manifest defaults (Alosaurus 110/0.35/18/8, Cryolophosaurus 10/0.25/3/1, T. Rex 160/0.38/22/14,
        // Nastysaurus 200/0.35/32/17, Pointysaurus 80/0.35/10/16, Basilisk 200/0.4/24/15); the constructors write X_stats over them.
        event.put(ModEntities.ALOSAURUS.get(), Alosaurus.createAttributes().build());
        event.put(ModEntities.CRYOLOPHOSAURUS.get(), Cryolophosaurus.createAttributes().build());
        event.put(ModEntities.BASILISK.get(), Basilisk.createAttributes().build());
        event.put(ModEntities.T_REX.get(), TRex.createAttributes().build());
        event.put(ModEntities.NASTYSAURUS.get(), Nastysaurus.createAttributes().build());
        event.put(ModEntities.POINTYSAURUS.get(), Pointysaurus.createAttributes().build());
        // W07 arthropods - Scorpion 15/0.2/4, Emperor Scorpion 350/0.35/35, Cave Fisher 10/0.2/4, Bee 80/0.32/12,
        // Hercules Beetle 250/0.25/30, Spit Bug 100/0.33/10, Jumpy Bug 200/0.4/20; the constructors write the MobStats values.
        event.put(ModEntities.SCORPION.get(), Scorpion.createAttributes().build());
        event.put(ModEntities.EMPEROR_SCORPION.get(), EmperorScorpion.createAttributes().build());
        event.put(ModEntities.CAVE_FISHER.get(), CaveFisher.createAttributes().build());
        event.put(ModEntities.BEE.get(), Bee.createAttributes().build());
        event.put(ModEntities.HERCULES_BEETLE.get(), HerculesBeetle.createAttributes().build());
        event.put(ModEntities.SPIT_BUG.get(), SpitBug.createAttributes().build());
        event.put(ModEntities.JUMPY_BUG.get(), TrooperBug.createAttributes().build());
        // W07 monsters - Alien 100/0.65/12/8, Molenoid 200/0.35/18/12, Kyuubi 125/0.25/10/10, Criminal 100/0.32/1/18,
        // Leaf Monster 6/0.25/2/1, Hammerhead 240/0.35/75/20.
        event.put(ModEntities.KYUUBI.get(), Kyuubi.createAttributes().build());
        event.put(ModEntities.ALIEN.get(), Alien.createAttributes().build());
        event.put(ModEntities.LEAF_MONSTER.get(), LeafMonster.createAttributes().build());
        event.put(ModEntities.MOLENOID.get(), Molenoid.createAttributes().build());
        event.put(ModEntities.HAMMERHEAD.get(), Hammerhead.createAttributes().build());
        event.put(ModEntities.CRIMINAL.get(), BandP.createAttributes().build());
        // W07 ender mobs and fairy - health 60/90/40.
        event.put(ModEntities.ENDER_KNIGHT.get(), EnderKnight.createAttributes().build());
        event.put(ModEntities.ENDER_REAPER.get(), EnderReaper.createAttributes().build());
        event.put(ModEntities.FAIRY.get(), Fairy.createAttributes().build());
        // W07 robots - manifest defaults; each constructor overwrites MAX_HEALTH/ATTACK_DAMAGE from MobStats.
        event.put(ModEntities.BOMB_OMB.get(), Robot1.createAttributes().build());
        event.put(ModEntities.ROBO_POUNDER.get(), Robot2.createAttributes().build());
        event.put(ModEntities.ROBO_GUNNER.get(), Robot3.createAttributes().build());
        event.put(ModEntities.ROBO_WARRIOR.get(), Robot4.createAttributes().build());
        event.put(ModEntities.ROBO_SNIPER.get(), Robot5.createAttributes().build());
        event.put(ModEntities.JEFFERY.get(), GiantRobot.createAttributes().build());
        // W07 worms - small 10/0.1/3, medium 30/0.1/10, large 90/0.2/18 (constructors re-read WormX_stats).
        event.put(ModEntities.SMALL_WORM.get(), WormSmall.createAttributes().build());
        event.put(ModEntities.MEDIUM_WORM.get(), WormMedium.createAttributes().build());
        event.put(ModEntities.LARGE_WORM.get(), WormLarge.createAttributes().build());
        // W08 water mobs - Attack Squid, Cloud Shark, Irukandji, Skate, Crystal Urchin, Sea Monster, Sea Viper.
        event.put(ModEntities.ATTACK_SQUID.get(), AttackSquid.createAttributes().build());
        event.put(ModEntities.CLOUD_SHARK.get(), CloudShark.createAttributes().build());
        event.put(ModEntities.IRUKANDJI.get(), Irukandji.createAttributes().build());
        event.put(ModEntities.SKATE.get(), Skate.createAttributes().build());
        event.put(ModEntities.CRYSTAL_URCHIN.get(), Urchin.createAttributes().build());
        event.put(ModEntities.SEA_MONSTER.get(), SeaMonster.createAttributes().build());
        event.put(ModEntities.SEA_VIPER.get(), SeaViper.createAttributes().build());
        // W08 terrors - Creeping Horror, Terrible Terror, Lurking Terror, Rat, Mantis, Crab (Crab health from getCrabScale at runtime).
        event.put(ModEntities.CREEPING_HORROR.get(), CreepingHorror.createAttributes().build());
        event.put(ModEntities.TERRIBLE_TERROR.get(), TerribleTerror.createAttributes().build());
        event.put(ModEntities.LURKING_TERROR.get(), LurkingTerror.createAttributes().build());
        event.put(ModEntities.RAT.get(), Rat.createAttributes().build());
        event.put(ModEntities.MANTIS.get(), Mantis.createAttributes().build());
        event.put(ModEntities.CRAB.get(), Crab.createAttributes().build());
        // W08 crystal mobs - Rotator 35/0.25/10, Vortex 150/0.35/26, Dungeon Beast 65/0.29/12 (constructors write the config values).
        event.put(ModEntities.ROTATOR.get(), Rotator.createAttributes().build());
        event.put(ModEntities.VORTEX.get(), Vortex.createAttributes().build());
        event.put(ModEntities.DUNGEON_BEAST.get(), DungeonBeast.createAttributes().build());
        // W08 Triffid and Nightmare (PitchBlack; the constructor overwrites the bases with the scaled config values).
        event.put(ModEntities.TRIFFID.get(), Triffid.createAttributes().build());
        event.put(ModEntities.NIGHTMARE.get(), PitchBlack.createAttributes().build());
        // W08 islands - Mob.createMobAttributes (1.7.10 EntityLiving defaults, no applyEntityAttributes).
        event.put(ModEntities.ISLAND.get(), Island.createAttributes().build());
        event.put(ModEntities.ISLAND_TOO.get(), IslandToo.createAttributes().build());
        // W08 Mothra, CaterKiller, Brutalfly.
        event.put(ModEntities.MOTHRA.get(), Mothra.createAttributes().build());
        event.put(ModEntities.CATER_KILLER.get(), CaterKiller.createAttributes().build());
        event.put(ModEntities.BRUTALFLY.get(), Brutalfly.createAttributes().build());
        // W09 - dragons, Cephadrome, Leonopteryx, Water Dragon and WTF?, Stinky and Rubber Ducky, the robots and the Spider Driver.
        event.put(ModEntities.BABY_DRAGON.get(), Spyro.createAttributes().build());
        event.put(ModEntities.DRAGON.get(), Dragon.createAttributes().build());
        event.put(ModEntities.CEPHADROME.get(), Cephadrome.createAttributes().build());
        event.put(ModEntities.LEONOPTERYX.get(), Leon.createAttributes().build());
        event.put(ModEntities.WTF.get(), GammaMetroid.createAttributes().build());
        event.put(ModEntities.WATER_DRAGON.get(), WaterDragon.createAttributes().build());
        event.put(ModEntities.STINKY.get(), Stinky.createAttributes().build());
        event.put(ModEntities.RUBBER_DUCKY.get(), RubberDucky.createAttributes().build());
        event.put(ModEntities.ROBOT_RED_ANT.get(), AntRobot.createAttributes().build());
        event.put(ModEntities.ROBOT_SPIDER.get(), SpiderRobot.createAttributes().build());
        event.put(ModEntities.SPIDER_DRIVER.get(), SpiderDriver.createAttributes().build());
        // W10 - bosses and royals (MAX_HEALTH through LegacyCombatMath.attributeMaxHealth, R4; constructors overwrite with runtime config).
        event.put(ModEntities.PURPLE_POWER.get(), PurplePower.createAttributes().build());
        event.put(ModEntities.THE_KRAKEN.get(), Kraken.createAttributes().build());
        event.put(ModEntities.MOBZILLA.get(), Godzilla.createAttributes().build());
        event.put(ModEntities.MOBZILLA_HEAD.get(), GodzillaHead.createAttributes().build());
        event.put(ModEntities.THE_KING.get(), TheKing.createAttributes().build());
        event.put(ModEntities.KING_HEAD.get(), KingHead.createAttributes().build());
        event.put(ModEntities.THE_QUEEN.get(), TheQueen.createAttributes().build());
        event.put(ModEntities.QUEEN_HEAD.get(), QueenHead.createAttributes().build());
        event.put(ModEntities.THE_PRINCE.get(), ThePrince.createAttributes().build());
        event.put(ModEntities.THE_YOUNG_PRINCE.get(), ThePrinceTeen.createAttributes().build());
        event.put(ModEntities.THE_PRINCESS.get(), ThePrincess.createAttributes().build());
        event.put(ModEntities.THE_YOUNG_ADULT_PRINCE.get(), ThePrinceAdult.createAttributes().build());
    }

    /**
     * {@code getCanSpawnHere} of each mob as a spawn predicate (DECISIONS R11).
     */
    @SubscribeEvent
    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // W04 - a matching mob spawner within x/z -3..2, y 0..4 always allows the spawn, otherwise the animal rule.
        // The biome entries themselves (GirlfriendEnable, BoyfriendEnable) come with the W12 biome modifier.
        event.register(ModEntities.GIRLFRIEND.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Girlfriend::checkGirlfriendSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BOYFRIEND.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Boyfriend::checkBoyfriendSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W05 flyers - getCanSpawnHere as predicates; ON_GROUND is the 1.7.10 canCreatureTypeSpawnAtLocation rule for
        // ambient mobs (vanilla bat uses the same). The biome entries come with W12. The ants get no placement: they
        // have no biome spawns, their getCanSpawnHere is the instance override checkSpawnRules.
        event.register(ModEntities.BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityButterfly::checkButterflySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.MOTH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityLunaMoth::checkMothSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.MOSQUITO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityMosquito::checkMosquitoSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.FIREFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Firefly::checkFireflySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W06 - getCanSpawnHere as predicates; ON_GROUND is the 1.7.10 canCreatureTypeSpawnAtLocation rule for creature
        // and ambient mobs, IN_WATER the one for waterCreature. The biome entries themselves come with W12.
        // Cows and Hydrolisc have no getCanSpawnHere of their own: the EntityAnimal rule (grass below, light > 8).
        event.register(ModEntities.APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRYSTAL_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.GOLD_FISH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GoldFish::checkGoldFishSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.FLOUNDER.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Flounder::checkFlounderSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.WHALE.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Whale::checkWhaleSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.FROG.get(), Frog.WATER_OR_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Frog::checkFrogSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.DRAGONFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Dragonfly::checkDragonflySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BIRD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Cockateil::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.RUBY_BIRD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                RubyBird::checkRubyBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.T_SHIRT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Tshirt::checkTshirtSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CLIFF_RACER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CliffRacer::checkCliffRacerSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.COIN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Coin::checkCoinSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRICKET.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Cricket::checkCricketSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CAMARASAURUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Camarasaurus::checkCamarasaurusSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.HYDROLISC.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BARYONYX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Baryonyx::checkBaryonyxSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.STINK_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                StinkBug::checkStinkBugSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CASSOWARY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Cassowary::checkCassowarySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W11 Easter Bunny: getCanSpawnHere (Y>=50, daytime, no other bunny in 32/8/32).
        event.register(ModEntities.EASTER_BUNNY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EasterBunny::checkEasterBunnySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BEAVER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Beaver::checkBeaverSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.PEACOCK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Peacock::checkPeacockSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.LIZARD.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Lizard::checkLizardSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CHIPMUNK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Chipmunk::checkChipmunkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.GAZELLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Gazelle::checkGazelleSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.VELOCITY_RAPTOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VelocityRaptor::checkVelocityRaptorSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.OSTRICH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Ostrich::checkOstrichSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.GHOST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Ghost::checkGhostSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.GHOST_PUMPKIN_SKELLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GhostSkelly::checkGhostSkellySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W07 - getCanSpawnHere as predicates (spawner scan = MobSpawnType.SPAWNER); ON_GROUND is the 1.7.10
        // canCreatureTypeSpawnAtLocation rule for monster, ambient and creature mobs. Biome and dimension entries come
        // with W12. Small and Medium Worm have no natural spawns: their night-only rule is the instance checkSpawnRules,
        // which spawners ask through EventHooks.checkSpawnPositionSpawner.
        event.register(ModEntities.ALOSAURUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Alosaurus::checkAlosaurusSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRYOLOPHOSAURUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Cryolophosaurus::checkCryolophosaurusSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BASILISK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Basilisk::checkBasiliskSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.T_REX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TRex::checkTRexSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.NASTYSAURUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Nastysaurus::checkNastysaurusSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.POINTYSAURUS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Pointysaurus::checkPointysaurusSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Scorpion::checkScorpionSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.EMPEROR_SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EmperorScorpion::checkEmperorScorpionSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CAVE_FISHER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CaveFisher::checkCaveFisherSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BEE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Bee::checkBeeSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.HERCULES_BEETLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HerculesBeetle::checkHerculesBeetleSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SPIT_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpitBug::checkSpitBugSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.JUMPY_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TrooperBug::checkTrooperBugSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.KYUUBI.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Kyuubi::checkKyuubiSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ALIEN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Alien::checkAlienSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.LEAF_MONSTER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                LeafMonster::checkLeafMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.MOLENOID.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Molenoid::checkMolenoidSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.HAMMERHEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Hammerhead::checkHammerheadSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRIMINAL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BandP::checkBandPSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ENDER_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EnderKnight::checkEnderKnightSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ENDER_REAPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EnderReaper::checkEnderReaperSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.FAIRY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Fairy::checkFairySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BOMB_OMB.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Robot1::checkRobot1SpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ROBO_POUNDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Robot2::checkRobot2SpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ROBO_GUNNER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Robot3::checkRobot3SpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ROBO_WARRIOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Robot4::checkRobot4SpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ROBO_SNIPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Robot5::checkRobot5SpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.JEFFERY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GiantRobot::checkGiantRobotSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.LARGE_WORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WormLarge::checkWormLargeSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W08 - getCanSpawnHere as predicates; IN_WATER for the waterCreature mobs (Flounder/Whale precedent), ON_GROUND for
        // monster and ambient lists. Biome and dimension entries come with W12. No placement for the islands (spawned only by
        // IslandBlock) and Triffid (no natural spawn, W07 worm precedent). PitchBlack's static predicate is always true; its
        // full rule is the instance checkSpawnRules (it needs the rolled scale).
        event.register(ModEntities.ATTACK_SQUID.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AttackSquid::checkAttackSquidSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CLOUD_SHARK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CloudShark::checkCloudSharkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.IRUKANDJI.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Irukandji::checkIrukandjiSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SKATE.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Skate::checkSkateSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRYSTAL_URCHIN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Urchin::checkUrchinSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SEA_MONSTER.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SeaMonster::checkSeaMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SEA_VIPER.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SeaViper::checkSeaViperSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CREEPING_HORROR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CreepingHorror::checkCreepingHorrorSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.TERRIBLE_TERROR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TerribleTerror::checkTerribleTerrorSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.LURKING_TERROR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                LurkingTerror::checkLurkingTerrorSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.RAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Rat::checkRatSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.MANTIS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mantis::checkMantisSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CRAB.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Crab::checkCrabSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.ROTATOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Rotator::checkRotatorSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.VORTEX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Vortex::checkVortexSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.DUNGEON_BEAST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                DungeonBeast::checkDungeonBeastSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PitchBlack::checkPitchBlackSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.MOTHRA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mothra::checkMothraSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CATER_KILLER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CaterKiller::checkCaterKillerSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.BRUTALFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Brutalfly::checkBrutalflySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W09 - the robots have no natural spawn and get no placement.
        event.register(ModEntities.BABY_DRAGON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Spyro::checkSpyroSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.DRAGON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Dragon::checkDragonSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.CEPHADROME.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Cephadrome::checkCephadromeSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.LEONOPTERYX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Leon::checkLeonSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.WTF.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GammaMetroid::checkGammaMetroidSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.WATER_DRAGON.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterDragon::checkWaterDragonSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.STINKY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Stinky::checkStinkySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.RUBBER_DUCKY.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                RubberDucky::checkRubberDuckySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SPIDER_DRIVER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpiderDriver::checkSpiderDriverSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // W10 - Mobzilla spawns naturally (VillageMania). Kraken and the royals have no addSpawn; their ported
        // getCanSpawnHere is registered anyway because 1.7.10 mob spawners asked it too (porter predicates). King, Queen,
        // the heads, PurplePower and the Young Adult Prince get none: no natural spawn and no porter predicate (W09 robots precedent).
        event.register(ModEntities.MOBZILLA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Godzilla::checkGodzillaSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.THE_KRAKEN.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Kraken::checkKrakenSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.THE_PRINCE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ThePrince::checkThePrinceSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.THE_YOUNG_PRINCE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ThePrinceTeen::checkThePrinceTeenSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.THE_PRINCESS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ThePrincess::checkThePrincessSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /**
     * The Crystal Furnace was an {@code ISidedInventory} (TileEntityCrystalFurnace.java:15,
     * :292-302): hoppers and pipes reach it through the item-handler capability with the original
     * side rules (W03).
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRYSTAL_FURNACE.get(),
                (furnace, side) -> new SidedInvWrapper(furnace, side));
    }

    /** The vanilla-tab placement of {@link ModCreativeTabs}. */
    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        ModCreativeTabs.onBuildContents(event);
    }
}
