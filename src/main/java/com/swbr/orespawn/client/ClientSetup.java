package com.swbr.orespawn.client;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.PurplePowerModel;
import com.swbr.orespawn.client.model.geom.PurplePowerGeometry;
import com.swbr.orespawn.client.model.KrakenModel;
import com.swbr.orespawn.client.model.geom.KrakenGeometry;
import com.swbr.orespawn.client.model.GodzillaModel;
import com.swbr.orespawn.client.model.geom.GodzillaGeometry;
import com.swbr.orespawn.client.model.TheKingModel;
import com.swbr.orespawn.client.model.geom.TheKingGeometry;
import com.swbr.orespawn.client.model.TheQueenModel;
import com.swbr.orespawn.client.model.geom.TheQueenGeometry;
import com.swbr.orespawn.client.model.ThePrinceModel;
import com.swbr.orespawn.client.model.geom.ThePrinceGeometry;
import com.swbr.orespawn.client.model.ThePrinceTeenModel;
import com.swbr.orespawn.client.model.geom.ThePrinceTeenGeometry;
import com.swbr.orespawn.client.model.ThePrincessModel;
import com.swbr.orespawn.client.model.geom.ThePrincessGeometry;
import com.swbr.orespawn.client.model.ThePrinceAdultModel;
import com.swbr.orespawn.client.model.geom.ThePrinceAdultGeometry;
import com.swbr.orespawn.client.renderer.boss.king.RenderPurplePower;
import com.swbr.orespawn.client.renderer.boss.king.RenderTheKing;
import com.swbr.orespawn.client.renderer.boss.king.RenderKingHead;
import com.swbr.orespawn.client.renderer.boss.kraken.RenderKraken;
import com.swbr.orespawn.client.renderer.boss.mobzilla.RenderGodzilla;
import com.swbr.orespawn.client.renderer.boss.mobzilla.RenderGodzillaHead;
import com.swbr.orespawn.client.renderer.boss.queen.RenderTheQueen;
import com.swbr.orespawn.client.renderer.boss.queen.RenderQueenHead;
import com.swbr.orespawn.client.renderer.boss.prince.RenderThePrince;
import com.swbr.orespawn.client.renderer.boss.prince.RenderThePrinceTeen;
import com.swbr.orespawn.client.renderer.boss.prince.RenderThePrincess;
import com.swbr.orespawn.client.renderer.boss.princeadult.RenderThePrinceAdult;
import com.swbr.orespawn.client.model.SpyroModel;
import com.swbr.orespawn.client.model.geom.SpyroGeometry;
import com.swbr.orespawn.client.model.DragonModel;
import com.swbr.orespawn.client.model.geom.DragonGeometry;
import com.swbr.orespawn.client.model.CephadromeModel;
import com.swbr.orespawn.client.model.geom.CephadromeGeometry;
import com.swbr.orespawn.client.model.LeonModel;
import com.swbr.orespawn.client.model.geom.LeonGeometry;
import com.swbr.orespawn.client.model.GammaMetroidModel;
import com.swbr.orespawn.client.model.geom.GammaMetroidGeometry;
import com.swbr.orespawn.client.model.WaterDragonModel;
import com.swbr.orespawn.client.model.geom.WaterDragonGeometry;
import com.swbr.orespawn.client.model.StinkyModel;
import com.swbr.orespawn.client.model.geom.StinkyGeometry;
import com.swbr.orespawn.client.model.RubberDuckyModel;
import com.swbr.orespawn.client.model.geom.RubberDuckyGeometry;
import com.swbr.orespawn.client.model.AntRobotModel;
import com.swbr.orespawn.client.model.geom.AntRobotGeometry;
import com.swbr.orespawn.client.model.SpiderRobotModel;
import com.swbr.orespawn.client.model.geom.SpiderRobotGeometry;
import com.swbr.orespawn.client.renderer.dragon.RenderSpyro;
import com.swbr.orespawn.client.renderer.dragon.RenderDragon;
import com.swbr.orespawn.client.renderer.cephadrome.RenderCephadrome;
import com.swbr.orespawn.client.renderer.leon.RenderLeon;
import com.swbr.orespawn.client.renderer.waterdragon.RenderGammaMetroid;
import com.swbr.orespawn.client.renderer.waterdragon.RenderWaterDragon;
import com.swbr.orespawn.client.renderer.pet.RenderStinky;
import com.swbr.orespawn.client.renderer.pet.RenderRubberDucky;
import com.swbr.orespawn.client.renderer.antrobot.RenderAntRobot;
import com.swbr.orespawn.client.renderer.spiderrobot.RenderSpiderRobot;
import com.swbr.orespawn.client.renderer.spiderrobot.RenderSpiderDriver;
import com.swbr.orespawn.client.item.BigWeaponRenderers;
import com.swbr.orespawn.client.item.bertha.ArrowRenderers;
import com.swbr.orespawn.client.item.bertha.BerthaRenderers;
import com.swbr.orespawn.client.model.AntModel;
import com.swbr.orespawn.client.model.ButterflyModel;
import com.swbr.orespawn.client.model.ElevatorModel;
import com.swbr.orespawn.client.model.FireflyModel;
import com.swbr.orespawn.client.model.MosquitoModel;
import com.swbr.orespawn.client.model.RockBaseModel;
import com.swbr.orespawn.client.model.geom.AntGeometry;
import com.swbr.orespawn.client.model.geom.ButterflyGeometry;
import com.swbr.orespawn.client.model.geom.ElevatorGeometry;
import com.swbr.orespawn.client.model.geom.FireflyGeometry;
import com.swbr.orespawn.client.model.geom.MosquitoGeometry;
import com.swbr.orespawn.client.model.geom.RockBaseGeometry;
import com.swbr.orespawn.client.model.BaryonyxModel;
import com.swbr.orespawn.client.model.BeaverModel;
import com.swbr.orespawn.client.model.CamarasaurusModel;
import com.swbr.orespawn.client.model.CassowaryModel;
import com.swbr.orespawn.client.model.EasterBunnyModel;
import com.swbr.orespawn.client.gui.GirlfriendOverlayGui;
import com.swbr.orespawn.client.model.ChipmunkModel;
import com.swbr.orespawn.client.model.CliffRacerModel;
import com.swbr.orespawn.client.model.CockateilModel;
import com.swbr.orespawn.client.model.CoinModel;
import com.swbr.orespawn.client.model.CricketModel;
import com.swbr.orespawn.client.model.DragonflyModel;
import com.swbr.orespawn.client.model.FlounderModel;
import com.swbr.orespawn.client.model.FrogModel;
import com.swbr.orespawn.client.model.GazelleModel;
import com.swbr.orespawn.client.model.GhostModel;
import com.swbr.orespawn.client.model.GhostSkellyModel;
import com.swbr.orespawn.client.model.GoldFishModel;
import com.swbr.orespawn.client.model.HydroliscModel;
import com.swbr.orespawn.client.model.LizardModel;
import com.swbr.orespawn.client.model.OstrichModel;
import com.swbr.orespawn.client.model.PeacockModel;
import com.swbr.orespawn.client.model.StinkBugModel;
import com.swbr.orespawn.client.model.TshirtModel;
import com.swbr.orespawn.client.model.VelocityRaptorModel;
import com.swbr.orespawn.client.model.WhaleModel;
import com.swbr.orespawn.client.model.geom.BaryonyxGeometry;
import com.swbr.orespawn.client.model.geom.BeaverGeometry;
import com.swbr.orespawn.client.model.geom.CamarasaurusGeometry;
import com.swbr.orespawn.client.model.geom.CassowaryGeometry;
import com.swbr.orespawn.client.model.geom.EasterBunnyGeometry;
import com.swbr.orespawn.client.model.geom.ChipmunkGeometry;
import com.swbr.orespawn.client.model.geom.CliffRacerGeometry;
import com.swbr.orespawn.client.model.geom.CockateilGeometry;
import com.swbr.orespawn.client.model.geom.CoinGeometry;
import com.swbr.orespawn.client.model.geom.CricketGeometry;
import com.swbr.orespawn.client.model.geom.DragonflyGeometry;
import com.swbr.orespawn.client.model.geom.FlounderGeometry;
import com.swbr.orespawn.client.model.geom.FrogGeometry;
import com.swbr.orespawn.client.model.geom.GazelleGeometry;
import com.swbr.orespawn.client.model.geom.GhostGeometry;
import com.swbr.orespawn.client.model.geom.GhostSkellyGeometry;
import com.swbr.orespawn.client.model.geom.GoldFishGeometry;
import com.swbr.orespawn.client.model.geom.HydroliscGeometry;
import com.swbr.orespawn.client.model.geom.LizardGeometry;
import com.swbr.orespawn.client.model.geom.OstrichGeometry;
import com.swbr.orespawn.client.model.geom.PeacockGeometry;
import com.swbr.orespawn.client.model.geom.StinkBugGeometry;
import com.swbr.orespawn.client.model.geom.TshirtGeometry;
import com.swbr.orespawn.client.model.geom.VelocityRaptorGeometry;
import com.swbr.orespawn.client.model.geom.WhaleGeometry;
import com.swbr.orespawn.client.renderer.aquatic.RenderFlounder;
import com.swbr.orespawn.client.renderer.aquatic.RenderFrog;
import com.swbr.orespawn.client.renderer.aquatic.RenderGoldFish;
import com.swbr.orespawn.client.renderer.aquatic.RenderWhale;
import com.swbr.orespawn.client.renderer.cannonfodder.RenderChipmunk;
import com.swbr.orespawn.client.renderer.cannonfodder.RenderGazelle;
import com.swbr.orespawn.client.renderer.cannonfodder.RenderLizard;
import com.swbr.orespawn.client.renderer.cow.RenderEnchantedCow;
import com.swbr.orespawn.client.renderer.critter.RenderCliffRacer;
import com.swbr.orespawn.client.renderer.critter.RenderCockateil;
import com.swbr.orespawn.client.renderer.critter.RenderCoin;
import com.swbr.orespawn.client.renderer.critter.RenderCricket;
import com.swbr.orespawn.client.renderer.critter.RenderDragonfly;
import com.swbr.orespawn.client.renderer.critter.RenderTshirt;
import com.swbr.orespawn.client.renderer.ghost.RenderGhost;
import com.swbr.orespawn.client.renderer.ghost.RenderGhostSkelly;
import com.swbr.orespawn.client.renderer.herbivore.RenderBaryonyx;
import com.swbr.orespawn.client.renderer.herbivore.RenderBeaver;
import com.swbr.orespawn.client.renderer.herbivore.RenderCamarasaurus;
import com.swbr.orespawn.client.renderer.herbivore.RenderCassowary;
import com.swbr.orespawn.client.renderer.easter.RenderEasterBunny;
import com.swbr.orespawn.client.renderer.cage.RenderCage;
import com.swbr.orespawn.client.renderer.herbivore.RenderHydrolisc;
import com.swbr.orespawn.client.renderer.herbivore.RenderPeacock;
import com.swbr.orespawn.client.renderer.herbivore.RenderStinkBug;
import com.swbr.orespawn.client.renderer.insect.RenderButterfly;
import com.swbr.orespawn.client.renderer.insect.RenderFirefly;
import com.swbr.orespawn.client.renderer.insect.RenderMosquito;
import com.swbr.orespawn.client.renderer.portal.RenderAnt;
import com.swbr.orespawn.client.renderer.rider.RenderOstrich;
import com.swbr.orespawn.client.renderer.rider.RenderVelocityRaptor;
import com.swbr.orespawn.client.model.AlienModel;
import com.swbr.orespawn.client.model.AlosaurusModel;
import com.swbr.orespawn.client.model.BandPModel;
import com.swbr.orespawn.client.model.BasiliskModel;
import com.swbr.orespawn.client.model.BeeModel;
import com.swbr.orespawn.client.model.CaveFisherModel;
import com.swbr.orespawn.client.model.CryolophosaurusModel;
import com.swbr.orespawn.client.model.EmperorScorpionModel;
import com.swbr.orespawn.client.model.EnderKnightModel;
import com.swbr.orespawn.client.model.EnderReaperModel;
import com.swbr.orespawn.client.model.FairyModel;
import com.swbr.orespawn.client.model.GiantRobotModel;
import com.swbr.orespawn.client.model.HammerheadModel;
import com.swbr.orespawn.client.model.HerculesBeetleModel;
import com.swbr.orespawn.client.model.KyuubiModel;
import com.swbr.orespawn.client.model.LeafMonsterModel;
import com.swbr.orespawn.client.model.MolenoidModel;
import com.swbr.orespawn.client.model.NastysaurusModel;
import com.swbr.orespawn.client.model.PointysaurusModel;
import com.swbr.orespawn.client.model.Robot1Model;
import com.swbr.orespawn.client.model.Robot2Model;
import com.swbr.orespawn.client.model.Robot3Model;
import com.swbr.orespawn.client.model.Robot4Model;
import com.swbr.orespawn.client.model.Robot5Model;
import com.swbr.orespawn.client.model.ScorpionModel;
import com.swbr.orespawn.client.model.SpitBugModel;
import com.swbr.orespawn.client.model.TRexModel;
import com.swbr.orespawn.client.model.TrooperBugModel;
import com.swbr.orespawn.client.model.WormLargeModel;
import com.swbr.orespawn.client.model.WormMediumModel;
import com.swbr.orespawn.client.model.WormSmallModel;
import com.swbr.orespawn.client.model.geom.AlienGeometry;
import com.swbr.orespawn.client.model.geom.AlosaurusGeometry;
import com.swbr.orespawn.client.model.geom.BandPGeometry;
import com.swbr.orespawn.client.model.geom.BasiliskGeometry;
import com.swbr.orespawn.client.model.geom.BeeGeometry;
import com.swbr.orespawn.client.model.geom.CaveFisherGeometry;
import com.swbr.orespawn.client.model.geom.CryolophosaurusGeometry;
import com.swbr.orespawn.client.model.geom.EmperorScorpionGeometry;
import com.swbr.orespawn.client.model.geom.EnderKnightGeometry;
import com.swbr.orespawn.client.model.geom.EnderReaperGeometry;
import com.swbr.orespawn.client.model.geom.FairyGeometry;
import com.swbr.orespawn.client.model.geom.GiantRobotGeometry;
import com.swbr.orespawn.client.model.geom.HammerheadGeometry;
import com.swbr.orespawn.client.model.geom.HerculesBeetleGeometry;
import com.swbr.orespawn.client.model.geom.KyuubiGeometry;
import com.swbr.orespawn.client.model.geom.LeafMonsterGeometry;
import com.swbr.orespawn.client.model.geom.MolenoidGeometry;
import com.swbr.orespawn.client.model.geom.NastysaurusGeometry;
import com.swbr.orespawn.client.model.geom.PointysaurusGeometry;
import com.swbr.orespawn.client.model.geom.Robot1Geometry;
import com.swbr.orespawn.client.model.geom.Robot2Geometry;
import com.swbr.orespawn.client.model.geom.Robot3Geometry;
import com.swbr.orespawn.client.model.geom.Robot4Geometry;
import com.swbr.orespawn.client.model.geom.Robot5Geometry;
import com.swbr.orespawn.client.model.geom.ScorpionGeometry;
import com.swbr.orespawn.client.model.geom.SpitBugGeometry;
import com.swbr.orespawn.client.model.geom.TRexGeometry;
import com.swbr.orespawn.client.model.geom.TrooperBugGeometry;
import com.swbr.orespawn.client.model.geom.WormLargeGeometry;
import com.swbr.orespawn.client.model.geom.WormMediumGeometry;
import com.swbr.orespawn.client.model.geom.WormSmallGeometry;
import com.swbr.orespawn.client.renderer.arthropod.RenderBee;
import com.swbr.orespawn.client.renderer.arthropod.RenderCaveFisher;
import com.swbr.orespawn.client.renderer.arthropod.RenderEmperorScorpion;
import com.swbr.orespawn.client.renderer.arthropod.RenderHerculesBeetle;
import com.swbr.orespawn.client.renderer.arthropod.RenderScorpion;
import com.swbr.orespawn.client.renderer.arthropod.RenderSpitBug;
import com.swbr.orespawn.client.renderer.arthropod.RenderTrooperBug;
import com.swbr.orespawn.client.renderer.dino.RenderAlosaurus;
import com.swbr.orespawn.client.renderer.dino.RenderBasilisk;
import com.swbr.orespawn.client.renderer.dino.RenderCryolophosaurus;
import com.swbr.orespawn.client.renderer.dino.RenderNastysaurus;
import com.swbr.orespawn.client.renderer.dino.RenderPointysaurus;
import com.swbr.orespawn.client.renderer.dino.RenderTRex;
import com.swbr.orespawn.client.renderer.ender.RenderEnderKnight;
import com.swbr.orespawn.client.renderer.ender.RenderEnderReaper;
import com.swbr.orespawn.client.renderer.ender.RenderFairy;
import com.swbr.orespawn.client.renderer.monster.RenderAlien;
import com.swbr.orespawn.client.renderer.monster.RenderBandP;
import com.swbr.orespawn.client.renderer.monster.RenderHammerhead;
import com.swbr.orespawn.client.renderer.monster.RenderKyuubi;
import com.swbr.orespawn.client.renderer.monster.RenderLeafMonster;
import com.swbr.orespawn.client.renderer.monster.RenderMolenoid;
import com.swbr.orespawn.client.renderer.robot.RenderGiantRobot;
import com.swbr.orespawn.client.renderer.robot.RenderRobot1;
import com.swbr.orespawn.client.renderer.robot.RenderRobot2;
import com.swbr.orespawn.client.renderer.robot.RenderRobot3;
import com.swbr.orespawn.client.renderer.robot.RenderRobot4;
import com.swbr.orespawn.client.renderer.robot.RenderRobot5;
import com.swbr.orespawn.client.renderer.worm.RenderWormLarge;
import com.swbr.orespawn.client.renderer.worm.RenderWormMedium;
import com.swbr.orespawn.client.renderer.worm.RenderWormSmall;
import com.swbr.orespawn.entity.critter.Cockateil;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityLunaMoth;
import com.swbr.orespawn.client.renderer.companion.RenderBoyfriend;
import com.swbr.orespawn.client.renderer.companion.RenderGirlfriend;
import com.swbr.orespawn.client.renderer.companion.RenderShoe;
import com.swbr.orespawn.client.renderer.projectile.ProjectileRenderers;
import com.swbr.orespawn.client.renderer.rock.RenderRockBase;
import com.swbr.orespawn.client.renderer.rock.RenderThrownRock;
import com.swbr.orespawn.client.renderer.vehicle.RenderElevator;
import com.swbr.orespawn.client.screen.CrystalFurnaceGUI;
import com.swbr.orespawn.client.screen.CrystalWorkbenchGUI;
import com.swbr.orespawn.client.model.AttackSquidModel;
import com.swbr.orespawn.client.model.CloudSharkModel;
import com.swbr.orespawn.client.model.IrukandjiModel;
import com.swbr.orespawn.client.model.SkateModel;
import com.swbr.orespawn.client.model.UrchinModel;
import com.swbr.orespawn.client.model.SeaMonsterModel;
import com.swbr.orespawn.client.model.SeaViperModel;
import com.swbr.orespawn.client.model.CreepingHorrorModel;
import com.swbr.orespawn.client.model.TerribleTerrorModel;
import com.swbr.orespawn.client.model.LurkingTerrorModel;
import com.swbr.orespawn.client.model.RatModel;
import com.swbr.orespawn.client.model.MantisModel;
import com.swbr.orespawn.client.model.CrabModel;
import com.swbr.orespawn.client.model.RotatorModel;
import com.swbr.orespawn.client.model.VortexModel;
import com.swbr.orespawn.client.model.DungeonBeastModel;
import com.swbr.orespawn.client.model.TriffidModel;
import com.swbr.orespawn.client.model.PitchBlackModel;
import com.swbr.orespawn.client.model.IslandModel;
import com.swbr.orespawn.client.model.CaterKillerModel;
import com.swbr.orespawn.client.model.BrutalflyModel;
import com.swbr.orespawn.client.model.geom.AttackSquidGeometry;
import com.swbr.orespawn.client.model.geom.CloudSharkGeometry;
import com.swbr.orespawn.client.model.geom.IrukandjiGeometry;
import com.swbr.orespawn.client.model.geom.SkateGeometry;
import com.swbr.orespawn.client.model.geom.UrchinGeometry;
import com.swbr.orespawn.client.model.geom.SeaMonsterGeometry;
import com.swbr.orespawn.client.model.geom.SeaViperGeometry;
import com.swbr.orespawn.client.model.geom.CreepingHorrorGeometry;
import com.swbr.orespawn.client.model.geom.TerribleTerrorGeometry;
import com.swbr.orespawn.client.model.geom.LurkingTerrorGeometry;
import com.swbr.orespawn.client.model.geom.RatGeometry;
import com.swbr.orespawn.client.model.geom.MantisGeometry;
import com.swbr.orespawn.client.model.geom.CrabGeometry;
import com.swbr.orespawn.client.model.geom.RotatorGeometry;
import com.swbr.orespawn.client.model.geom.VortexGeometry;
import com.swbr.orespawn.client.model.geom.DungeonBeastGeometry;
import com.swbr.orespawn.client.model.geom.TriffidGeometry;
import com.swbr.orespawn.client.model.geom.PitchBlackGeometry;
import com.swbr.orespawn.client.model.geom.IslandGeometry;
import com.swbr.orespawn.client.model.geom.CaterKillerGeometry;
import com.swbr.orespawn.client.model.geom.BrutalflyGeometry;
import com.swbr.orespawn.client.renderer.sea.RenderAttackSquid;
import com.swbr.orespawn.client.renderer.sea.RenderCloudShark;
import com.swbr.orespawn.client.renderer.sea.RenderIrukandji;
import com.swbr.orespawn.client.renderer.sea.RenderSkate;
import com.swbr.orespawn.client.renderer.sea.RenderUrchin;
import com.swbr.orespawn.client.renderer.sea.RenderSeaMonster;
import com.swbr.orespawn.client.renderer.sea.RenderSeaViper;
import com.swbr.orespawn.client.renderer.terror.RenderCreepingHorror;
import com.swbr.orespawn.client.renderer.terror.RenderTerribleTerror;
import com.swbr.orespawn.client.renderer.terror.RenderLurkingTerror;
import com.swbr.orespawn.client.renderer.terror.RenderRat;
import com.swbr.orespawn.client.renderer.terror.RenderMantis;
import com.swbr.orespawn.client.renderer.terror.RenderCrab;
import com.swbr.orespawn.client.renderer.crystal.RenderRotator;
import com.swbr.orespawn.client.renderer.crystal.RenderVortex;
import com.swbr.orespawn.client.renderer.crystal.RenderDungeonBeast;
import com.swbr.orespawn.client.renderer.nightmare.RenderTriffid;
import com.swbr.orespawn.client.renderer.nightmare.RenderPitchBlack;
import com.swbr.orespawn.client.renderer.island.RenderIsland;
import com.swbr.orespawn.client.renderer.island.RenderIslandToo;
import com.swbr.orespawn.client.renderer.moth.RenderCaterKiller;
import com.swbr.orespawn.client.renderer.moth.RenderBrutalfly;
import com.swbr.orespawn.client.item.squidzooka.SquidZookaRenderers;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Client-side mod-bus wiring - the successor of {@code ClientProxyOreSpawn}
 * (ClientProxyOreSpawn.java:11-181).
 *
 * <p>{@code value = Dist.CLIENT} is not cosmetic: everything under {@code client} touches classes
 * a dedicated server does not have, and one reachable reference from common code is a
 * {@code NoClassDefFoundError} at first use. The proxy pattern that did this job in 1.7.10 is
 * gone; the dist marker plus the package boundary replace it (DECISIONS R1, STYLE.md).
 *
 * <p>What the proxy registered and where it now goes:
 * <ul>
 *   <li>133 entity renderers (:16-148) - {@link #onRegisterRenderers}; the three head parts
 *       (GodzillaHead, KingHead, QueenHead, :93-95) had a null model and a 0/0 renderer and
 *       become {@code NoopRenderer}s.</li>
 *   <li>the model geometry - {@link #onRegisterLayerDefinitions}, from the generated
 *       {@code client.model.geom.*Geometry} classes (R8).</li>
 *   <li>8 item renderers (:149-156, Bertha, Slice, Royal, SquidZooka, Hammy, BattleAxe, Chainsaw,
 *       QueenBattleAxe) - {@link #onRegisterClientExtensions} with a
 *       {@code BlockEntityWithoutLevelRenderer} each (R8).</li>
 *   <li>{@code OreSpawnGUIHandler.getClientGuiElement} (OreSpawnGUIHandler.java:26-40) -
 *       {@link #onRegisterMenuScreens} (R15).</li>
 *   <li>{@code KeyHandler} (:165-169) - {@code client.input.KeyHandler}, which registers its own
 *       {@code KeyMapping} on {@code RegisterKeyMappingsEvent}; {@code RiderControl} next to it
 *       polls the key on {@code ClientTickEvent.Post} (R15). Neither has a hub here: a key
 *       registered twice appears twice in the controls screen.</li>
 *   <li>{@code GirlfriendOverlayGui} (:15) - {@link #onRegisterGuiLayers} (R15).</li>
 *   <li>{@code registerSoundThings} (:160-162) registered a listener without a subscribe
 *       annotation; it never ran and has no successor.</li>
 *   <li>{@code setArmorPrefix} (:178-180) - gone; armor textures resolve through the single
 *       {@code ArmorMaterial.Layer} of {@code item.armor.OreSpawnArmorMaterials} (W03).</li>
 * </ul>
 * Each handler is a hub that later waves extend through the wave's {@code registry_entries}.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientSetup {

    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Nothing here yet. Screens go through RegisterMenuScreensEvent (NeoForge 21.1), not
        // FMLClientSetupEvent; W02 cutout blocks declare their layer as "render_type" in each
        // block model JSON; the leaf tints sit in client.BlockRenderTypes.
    }

    /** {@code OreSpawnGUIHandler.getClientGuiElement} (OreSpawnGUIHandler.java:26-40) as screen registration (DECISIONS R15). */
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CRYSTAL_FURNACE.get(), CrystalFurnaceGUI::new);
        event.register(ModMenus.CRYSTAL_WORKBENCH.get(), CrystalWorkbenchGUI::new);
    }

    /** {@code RenderingRegistry.registerEntityRenderingHandler} x133 (ClientProxyOreSpawn.java:16-148). */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // W04 - Girlfriend (shadow 0.5) and Boyfriend (0.55), :16-17.
        event.registerEntityRenderer(ModEntities.GIRLFRIEND.get(), RenderGirlfriend::new);
        event.registerEntityRenderer(ModEntities.BOYFRIEND.get(), RenderBoyfriend::new);
        event.registerEntityRenderer(ModEntities.SHOES.get(), RenderShoe::new);
        // W04 - the seven RenderItemUrchin spinners (:23-29), a fire-charge sprite for better_fireball and a
        // NoopRenderer for thunder_bolt.
        ProjectileRenderers.register(event);
        // W04 - vanilla arrow renderer for both arrows, FishingHookRenderer for the hook, NoopRenderer for BerthaHit
        // (replaces the dropped BerthaHit branch of RenderItemUrchin, :30).
        ArrowRenderers.registerRenderers(event, ModEntities.ULTIMATE_ARROW.get(), ModEntities.IRUKANDJI_ARROW.get(),
                ModEntities.ULTIMATE_FISH_HOOK.get(), ModEntities.BERTHA_HIT.get());
        // W04 - thrown and placed rocks; RenderRockBase(model, shadow 0.0, scale 1.0) per manifest renderer_args.
        event.registerEntityRenderer(ModEntities.ENTITY_THROWN_ROCK.get(), RenderThrownRock::new);
        event.registerEntityRenderer(ModEntities.ROCK.get(), ctx -> new RenderRockBase(ctx, 0.0f, 1.0f));
        // W04 - the hoverboard.
        event.registerEntityRenderer(ModEntities.HOVERBOARD.get(), RenderElevator::new);
        // W05 - flyers (ClientProxyOreSpawn RenderButterfly(new ModelButterfly(w), shadow, scale) etc., manifest renderer_args).
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), ctx -> new RenderButterfly<EntityButterfly>(ctx, 0.3f, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.MOTH.get(), ctx -> new RenderButterfly<EntityLunaMoth>(ctx, 0.4f, 1.5f, 0.75f));
        event.registerEntityRenderer(ModEntities.MOSQUITO.get(), ctx -> new RenderMosquito(ctx, 0.3f, 0.5f));
        event.registerEntityRenderer(ModEntities.FIREFLY.get(), ctx -> new RenderFirefly(ctx, 0.2f, 0.75f, 2.5f));
        // W05 - ants with the original shadow/scale pairs (:42-45, :106); one RenderAnt fits every EntityAnt subtype.
        event.registerEntityRenderer(ModEntities.ANT.get(), ctx -> new RenderAnt(ctx, 0.1f, 0.25f));
        event.registerEntityRenderer(ModEntities.RED_ANT.get(), ctx -> new RenderAnt(ctx, 0.15f, 0.35f));
        event.registerEntityRenderer(ModEntities.RAINBOW_ANT.get(), ctx -> new RenderAnt(ctx, 0.1f, 0.25f));
        event.registerEntityRenderer(ModEntities.UNSTABLE_ANT.get(), ctx -> new RenderAnt(ctx, 0.1f, 0.25f));
        event.registerEntityRenderer(ModEntities.TERMITE.get(), ctx -> new RenderAnt(ctx, 0.15f, 0.35f));
        // W06 cows - the four share RenderEnchantedCow(new ModelCow(), 0.7f) (ClientProxyOreSpawn.java:18-21) on the vanilla cow layer.
        event.registerEntityRenderer(ModEntities.APPLE_COW.get(), RenderEnchantedCow::new);
        event.registerEntityRenderer(ModEntities.GOLDEN_APPLE_COW.get(), RenderEnchantedCow::new);
        event.registerEntityRenderer(ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), RenderEnchantedCow::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_APPLE_COW.get(), RenderEnchantedCow::new);
        // W06 water animals (:100, :113, :114, :142).
        event.registerEntityRenderer(ModEntities.GOLD_FISH.get(), ctx -> new RenderGoldFish(ctx, 0.2f, 1.0f, 0.7f));
        event.registerEntityRenderer(ModEntities.FLOUNDER.get(), ctx -> new RenderFlounder(ctx, 0.1f, 1.0f));
        event.registerEntityRenderer(ModEntities.WHALE.get(), ctx -> new RenderWhale(ctx, 0.1f, 1.0f));
        event.registerEntityRenderer(ModEntities.FROG.get(), ctx -> new RenderFrog(ctx, 0.35f, 1.0f, 1.0f));
        // W06 critters - (model wingspeed, shadow, scale): Tshirt :48, Dragonfly :54, Bird/Ruby Bird :60-61, Cliff Racer :88,
        // Coin :121, Cricket :140. The type witness <Cockateil> lets the Cockateil renderer take the RubyBird type.
        event.registerEntityRenderer(ModEntities.T_SHIRT.get(), ctx -> new RenderTshirt(ctx, 1.0f, 0.33f, 0.22f));
        event.registerEntityRenderer(ModEntities.DRAGONFLY.get(), ctx -> new RenderDragonfly(ctx, 0.3f, 1.5f, 2.0f));
        event.registerEntityRenderer(ModEntities.BIRD.get(), ctx -> new RenderCockateil(ctx, 0.3f, 0.75f, 1.0f));
        event.<Cockateil>registerEntityRenderer(ModEntities.RUBY_BIRD.get(), ctx -> new RenderCockateil(ctx, 0.3f, 0.75f, 1.0f));
        event.registerEntityRenderer(ModEntities.CLIFF_RACER.get(), ctx -> new RenderCliffRacer(ctx, 0.3f, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.COIN.get(), ctx -> new RenderCoin(ctx, 0.75f, 0.125f, 0.22f));
        event.registerEntityRenderer(ModEntities.CRICKET.get(), ctx -> new RenderCricket(ctx, 0.15f, 0.5f, 2.5f));
        // W06 herbivores - Render<X>(new Model<X>(wingspeed), shadow, scale); the model argument is fixed inside each renderer.
        event.registerEntityRenderer(ModEntities.CAMARASAURUS.get(), ctx -> new RenderCamarasaurus(ctx, 0.65f, 0.65f));
        event.registerEntityRenderer(ModEntities.HYDROLISC.get(), ctx -> new RenderHydrolisc(ctx, 0.65f, 0.65f));
        event.registerEntityRenderer(ModEntities.BARYONYX.get(), ctx -> new RenderBaryonyx(ctx, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.STINK_BUG.get(), ctx -> new RenderStinkBug(ctx, 0.35f, 0.85f));
        event.registerEntityRenderer(ModEntities.CASSOWARY.get(), ctx -> new RenderCassowary(ctx, 0.5f, 1.0f));
        event.registerEntityRenderer(ModEntities.BEAVER.get(), ctx -> new RenderBeaver(ctx, 0.15f, 0.75f));
        event.registerEntityRenderer(ModEntities.PEACOCK.get(), ctx -> new RenderPeacock(ctx, 0.25f, 1.0f));
        // W06 cannon fodder (:75, :78, :79).
        event.registerEntityRenderer(ModEntities.LIZARD.get(), ctx -> new RenderLizard(ctx, 0.65f, 0.75f, 1.0f));
        event.registerEntityRenderer(ModEntities.CHIPMUNK.get(), ctx -> new RenderChipmunk(ctx, 1.0f, 0.15f, 0.9f));
        event.registerEntityRenderer(ModEntities.GAZELLE.get(), ctx -> new RenderGazelle(ctx, 0.65f, 0.45f, 1.0f));
        // W06 riders and ghosts (:53, :80, :39, :40).
        event.registerEntityRenderer(ModEntities.VELOCITY_RAPTOR.get(), ctx -> new RenderVelocityRaptor(ctx, 0.55f, 0.75f, 1.25f));
        event.registerEntityRenderer(ModEntities.OSTRICH.get(), ctx -> new RenderOstrich(ctx, 0.55f, 1.0f, 0.65f));
        event.registerEntityRenderer(ModEntities.GHOST.get(), ctx -> new RenderGhost(ctx, 0.0f, 0.65f));
        event.registerEntityRenderer(ModEntities.GHOST_PUMPKIN_SKELLY.get(), ctx -> new RenderGhostSkelly(ctx, 0.0f, 1.05f));
        // W07 dinosaurs - Render<X>(new Model<X>(wingspeed), shadow, scale) (:46, :47, :49, :50, :138, :139); the model
        // argument is fixed inside each renderer.
        event.registerEntityRenderer(ModEntities.ALOSAURUS.get(), ctx -> new RenderAlosaurus(ctx, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.T_REX.get(), ctx -> new RenderTRex(ctx, 1.0f, 1.2f));
        event.registerEntityRenderer(ModEntities.CRYOLOPHOSAURUS.get(), ctx -> new RenderCryolophosaurus(ctx, 0.75f, 0.5f));
        event.registerEntityRenderer(ModEntities.BASILISK.get(), ctx -> new RenderBasilisk(ctx, 0.5f, 1.25f));
        event.registerEntityRenderer(ModEntities.NASTYSAURUS.get(), ctx -> new RenderNastysaurus(ctx, 1.0f, 1.5f));
        event.registerEntityRenderer(ModEntities.POINTYSAURUS.get(), ctx -> new RenderPointysaurus(ctx, 1.0f, 1.0f));
        // W07 arthropods - (shadow, scale) per manifest renderer_args.
        event.registerEntityRenderer(ModEntities.SCORPION.get(), ctx -> new RenderScorpion(ctx, 0.35f, 0.75f));
        event.registerEntityRenderer(ModEntities.EMPEROR_SCORPION.get(), ctx -> new RenderEmperorScorpion(ctx, 0.95f, 1.5f));
        event.registerEntityRenderer(ModEntities.CAVE_FISHER.get(), ctx -> new RenderCaveFisher(ctx, 0.35f, 0.75f));
        event.registerEntityRenderer(ModEntities.BEE.get(), ctx -> new RenderBee(ctx, 0.9f, 1.1f));
        event.registerEntityRenderer(ModEntities.HERCULES_BEETLE.get(), ctx -> new RenderHerculesBeetle(ctx, 0.99f, 1.1f));
        event.registerEntityRenderer(ModEntities.SPIT_BUG.get(), ctx -> new RenderSpitBug(ctx, 0.55f, 0.75f));
        event.registerEntityRenderer(ModEntities.JUMPY_BUG.get(), ctx -> new RenderTrooperBug(ctx, 0.95f, 1.1f));
        // W07 monsters (:62, :65, :102, :125, :131, :134).
        event.registerEntityRenderer(ModEntities.KYUUBI.get(), ctx -> new RenderKyuubi(ctx, 0.1f, 1.0f));
        event.registerEntityRenderer(ModEntities.ALIEN.get(), ctx -> new RenderAlien(ctx, 0.35f, 1.1f));
        event.registerEntityRenderer(ModEntities.LEAF_MONSTER.get(), ctx -> new RenderLeafMonster(ctx, 0.65f, 1.0f));
        event.registerEntityRenderer(ModEntities.MOLENOID.get(), ctx -> new RenderMolenoid(ctx, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.HAMMERHEAD.get(), ctx -> new RenderHammerhead(ctx, 1.0f, 2.5f));
        event.registerEntityRenderer(ModEntities.CRIMINAL.get(), ctx -> new RenderBandP(ctx, 1.0f, 1.0f));
        // W07 ender mobs and fairy - (shadow, scale, model wingspeed).
        event.registerEntityRenderer(ModEntities.ENDER_KNIGHT.get(), ctx -> new RenderEnderKnight(ctx, 0.3f, 1.0f, 0.21f));
        event.registerEntityRenderer(ModEntities.ENDER_REAPER.get(), ctx -> new RenderEnderReaper(ctx, 0.2f, 1.0f, 0.23f));
        event.registerEntityRenderer(ModEntities.FAIRY.get(), ctx -> new RenderFairy(ctx, 0.1f, 0.35f, 1.5f));
        // W07 robots - (model wingspeed, shadow factor, scale): shadow = a*b, glScalef(b).
        event.registerEntityRenderer(ModEntities.BOMB_OMB.get(), ctx -> new RenderRobot1(ctx, 2.0f, 0.3f, 1.0f));
        event.registerEntityRenderer(ModEntities.ROBO_POUNDER.get(), ctx -> new RenderRobot2(ctx, 1.0f, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.ROBO_GUNNER.get(), ctx -> new RenderRobot3(ctx, 1.0f, 1.0f, 0.5f));
        event.registerEntityRenderer(ModEntities.ROBO_WARRIOR.get(), ctx -> new RenderRobot4(ctx, 1.0f, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.ROBO_SNIPER.get(), ctx -> new RenderRobot5(ctx, 1.0f, 0.5f, 1.0f));
        event.registerEntityRenderer(ModEntities.JEFFERY.get(), ctx -> new RenderGiantRobot(ctx, 0.25f, 0.99f, 1.0f));
        // W07 worms (:96-98).
        event.registerEntityRenderer(ModEntities.SMALL_WORM.get(), ctx -> new RenderWormSmall(ctx, 0.1f, 1.0f));
        event.registerEntityRenderer(ModEntities.MEDIUM_WORM.get(), ctx -> new RenderWormMedium(ctx, 0.25f, 1.0f));
        event.registerEntityRenderer(ModEntities.LARGE_WORM.get(), ctx -> new RenderWormLarge(ctx, 0.9f, 1.0f));
        // W08 water mobs (ClientProxyOreSpawn :67, :101, :115-117, :126-127) - (shadow, scale, model wingspeed).
        event.registerEntityRenderer(ModEntities.ATTACK_SQUID.get(), ctx -> new RenderAttackSquid(ctx, 0.25f, 0.9f, 1.0f));
        event.registerEntityRenderer(ModEntities.CLOUD_SHARK.get(), ctx -> new RenderCloudShark(ctx, 0.5f, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.IRUKANDJI.get(), ctx -> new RenderIrukandji(ctx, 0.1f, 0.25f, 1.0f));
        event.registerEntityRenderer(ModEntities.SKATE.get(), ctx -> new RenderSkate(ctx, 0.1f, 0.75f, 1.0f));
        event.registerEntityRenderer(ModEntities.CRYSTAL_URCHIN.get(), ctx -> new RenderUrchin(ctx, 0.35f, 1.25f, 1.0f));
        event.registerEntityRenderer(ModEntities.SEA_MONSTER.get(), ctx -> new RenderSeaMonster(ctx, 1.0f, 1.0f, 0.5f));
        event.registerEntityRenderer(ModEntities.SEA_VIPER.get(), ctx -> new RenderSeaViper(ctx, 1.0f, 1.0f, 0.5f));
        // W08 terrors - (shadow, scale) per manifest renderer_args.
        event.registerEntityRenderer(ModEntities.CREEPING_HORROR.get(), ctx -> new RenderCreepingHorror(ctx, 0.45f, 0.75f));
        event.registerEntityRenderer(ModEntities.TERRIBLE_TERROR.get(), ctx -> new RenderTerribleTerror(ctx, 0.45f, 0.75f));
        event.registerEntityRenderer(ModEntities.LURKING_TERROR.get(), ctx -> new RenderLurkingTerror(ctx, 0.45f, 0.85f));
        event.registerEntityRenderer(ModEntities.RAT.get(), ctx -> new RenderRat(ctx, 0.1f, 0.75f));
        event.registerEntityRenderer(ModEntities.MANTIS.get(), ctx -> new RenderMantis(ctx, 0.9f, 1.1f));
        event.registerEntityRenderer(ModEntities.CRAB.get(), ctx -> new RenderCrab(ctx, 0.99f, 1.0f));
        // W08 crystal mobs (:109-111).
        event.registerEntityRenderer(ModEntities.ROTATOR.get(), ctx -> new RenderRotator(ctx, 0.1f, 1.0f));
        event.registerEntityRenderer(ModEntities.VORTEX.get(), ctx -> new RenderVortex(ctx, 0.1f, 1.0f));
        event.registerEntityRenderer(ModEntities.DUNGEON_BEAST.get(), ctx -> new RenderDungeonBeast(ctx, 0.25f, 1.0f));
        // W08 Triffid and Nightmare (:89-90; shadow par2*par3, model args fixed inside).
        event.registerEntityRenderer(ModEntities.TRIFFID.get(), ctx -> new RenderTriffid(ctx, 0.3f, 1.0f));
        event.registerEntityRenderer(ModEntities.NIGHTMARE.get(), ctx -> new RenderPitchBlack(ctx, 1.25f, 1.0f));
        // W08 islands (model 1.0, shadow 0.25, scale 1.0); both share IslandModel.LAYER.
        event.registerEntityRenderer(ModEntities.ISLAND.get(), ctx -> new RenderIsland(ctx, 0.25f, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.ISLAND_TOO.get(), ctx -> new RenderIslandToo(ctx, 0.25f, 1.0f, 1.0f));
        // W08 Mothra reuses the W05 RenderButterfly (shadow 0.75, scale 10.0, model wingspeed 0.2; glow pass, eyemoth.png);
        // the Brutalfly overlay layer is added inside RenderBrutalfly.
        event.registerEntityRenderer(ModEntities.MOTHRA.get(), ctx -> new RenderButterfly<Mothra>(ctx, 0.75f, 10.0f, 0.2f));
        event.registerEntityRenderer(ModEntities.CATER_KILLER.get(), ctx -> new RenderCaterKiller(ctx, 1.0f, 1.25f));
        event.registerEntityRenderer(ModEntities.BRUTALFLY.get(), ctx -> new RenderBrutalfly(ctx, 0.75f, 9.0f));
        // W09 - renderer args from ClientProxyOreSpawn / manifest renderer_args (model args are fixed inside the renderers).
        event.registerEntityRenderer(ModEntities.BABY_DRAGON.get(), ctx -> new RenderSpyro(ctx, 0.65f, 0.75f));
        event.registerEntityRenderer(ModEntities.DRAGON.get(), ctx -> new RenderDragon(ctx, 1.25f, 1.0f));
        event.registerEntityRenderer(ModEntities.CEPHADROME.get(), ctx -> new RenderCephadrome(ctx, 1.25f, 1.0f, 0.55f));
        event.registerEntityRenderer(ModEntities.LEONOPTERYX.get(), ctx -> new RenderLeon(ctx, 1.0f, 1.75f));
        event.registerEntityRenderer(ModEntities.WTF.get(), ctx -> new RenderGammaMetroid(ctx, 0.75f, 0.9f));
        event.registerEntityRenderer(ModEntities.WATER_DRAGON.get(), ctx -> new RenderWaterDragon(ctx, 0.85f, 1.1f));
        event.registerEntityRenderer(ModEntities.STINKY.get(), ctx -> new RenderStinky(ctx, 0.75f, 1.0f));
        event.registerEntityRenderer(ModEntities.RUBBER_DUCKY.get(), ctx -> new RenderRubberDucky(ctx, 0.15f, 0.75f));
        event.registerEntityRenderer(ModEntities.ROBOT_RED_ANT.get(), ctx -> new RenderAntRobot(ctx, 1.0f, 0.99f, 1.0f));
        event.registerEntityRenderer(ModEntities.ROBOT_SPIDER.get(), ctx -> new RenderSpiderRobot(ctx, 0.99f, 1.0f));
        event.registerEntityRenderer(ModEntities.SPIDER_DRIVER.get(), ctx -> new RenderSpiderDriver(ctx, 0.5f));
        // W10 - renderer args from ClientProxyOreSpawn / manifest renderer_args; translucent layers are added inside the renderers.
        event.registerEntityRenderer(ModEntities.PURPLE_POWER.get(), ctx -> new RenderPurplePower(ctx, 0.3f, 2.75f));
        event.registerEntityRenderer(ModEntities.THE_KRAKEN.get(), ctx -> new RenderKraken(ctx, 1.0f, 1.0f));
        event.registerEntityRenderer(ModEntities.MOBZILLA.get(), ctx -> new RenderGodzilla(ctx, 1.0f, 2.0f));
        event.registerEntityRenderer(ModEntities.MOBZILLA_HEAD.get(), RenderGodzillaHead::new);
        event.registerEntityRenderer(ModEntities.THE_KING.get(), ctx -> new RenderTheKing(ctx, 1.9f, 2.1f));
        event.registerEntityRenderer(ModEntities.KING_HEAD.get(), RenderKingHead::new);
        event.registerEntityRenderer(ModEntities.THE_QUEEN.get(), ctx -> new RenderTheQueen(ctx, 1.9f, 2.0f));
        event.registerEntityRenderer(ModEntities.QUEEN_HEAD.get(), RenderQueenHead::new);
        event.registerEntityRenderer(ModEntities.THE_PRINCE.get(), ctx -> new RenderThePrince(ctx, 0.75f, 0.75f));
        event.registerEntityRenderer(ModEntities.THE_YOUNG_PRINCE.get(), ctx -> new RenderThePrinceTeen(ctx, 1.0f, 1.25f));
        event.registerEntityRenderer(ModEntities.THE_PRINCESS.get(), ctx -> new RenderThePrincess(ctx, 0.7f, 0.7f));
        event.registerEntityRenderer(ModEntities.THE_YOUNG_ADULT_PRINCE.get(), ctx -> new RenderThePrinceAdult(ctx, 1.2f, 1.0f));
        // W11.
        event.registerEntityRenderer(ModEntities.ENTITY_CAGE.get(), RenderCage::new); // ClientProxyOreSpawn.java:31
        event.registerEntityRenderer(ModEntities.EASTER_BUNNY.get(), ctx -> new RenderEasterBunny(ctx, 0.5f, 1.0f)); // ClientProxyOreSpawn.java:128
    }

    /** One {@code LayerDefinition} per generated geometry class (R8). */
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // W03: the three big-weapon models (BattleAxe, Chainsaw, QueenBattleAxe).
        BigWeaponRenderers.registerLayers(event);
        // W04: Bertha, Slice (shared by Slice and Royal), Hammy.
        BerthaRenderers.registerLayers(event);
        // W04: the vanilla ModelBiped as a 64x32 HumanoidModel layer; armor layers reuse the player armor layers.
        event.registerLayerDefinition(RenderGirlfriend.LAYER, RenderGirlfriend::createBodyLayer);
        event.registerLayerDefinition(RenderBoyfriend.LAYER, RenderBoyfriend::createBodyLayer);
        // W04: placed rock and hoverboard.
        event.registerLayerDefinition(RockBaseModel.LAYER, RockBaseGeometry::createBodyLayer);
        event.registerLayerDefinition(ElevatorModel.LAYER, ElevatorGeometry::createBodyLayer);
        // W05: ant (shared by all five ant types), butterfly (shared by butterfly, moth and later Mothra), mosquito, firefly.
        event.registerLayerDefinition(AntModel.LAYER, AntGeometry::createBodyLayer);
        event.registerLayerDefinition(ButterflyModel.LAYER, ButterflyGeometry::createBodyLayer);
        event.registerLayerDefinition(MosquitoModel.LAYER, MosquitoGeometry::createBodyLayer);
        event.registerLayerDefinition(FireflyModel.LAYER, FireflyGeometry::createBodyLayer);
        // W06: gold fish, whale, flounder, frog (the cows use the vanilla cow layer).
        event.registerLayerDefinition(GoldFishModel.LAYER, GoldFishGeometry::createBodyLayer);
        event.registerLayerDefinition(WhaleModel.LAYER, WhaleGeometry::createBodyLayer);
        event.registerLayerDefinition(FlounderModel.LAYER, FlounderGeometry::createBodyLayer);
        event.registerLayerDefinition(FrogModel.LAYER, FrogGeometry::createBodyLayer);
        // W06: bird (shared by bird and ruby bird), cliff racer, cricket, dragonfly, coin, t-shirt.
        event.registerLayerDefinition(CockateilModel.LAYER, CockateilGeometry::createBodyLayer);
        event.registerLayerDefinition(CliffRacerModel.LAYER, CliffRacerGeometry::createBodyLayer);
        event.registerLayerDefinition(CricketModel.LAYER, CricketGeometry::createBodyLayer);
        event.registerLayerDefinition(DragonflyModel.LAYER, DragonflyGeometry::createBodyLayer);
        event.registerLayerDefinition(CoinModel.LAYER, CoinGeometry::createBodyLayer);
        event.registerLayerDefinition(TshirtModel.LAYER, TshirtGeometry::createBodyLayer);
        // W06: herbivores.
        event.registerLayerDefinition(BaryonyxModel.LAYER, BaryonyxGeometry::createBodyLayer);
        event.registerLayerDefinition(CassowaryModel.LAYER, CassowaryGeometry::createBodyLayer);
        event.registerLayerDefinition(EasterBunnyModel.LAYER, EasterBunnyGeometry::createBodyLayer); // W11
        event.registerLayerDefinition(CamarasaurusModel.LAYER, CamarasaurusGeometry::createBodyLayer);
        event.registerLayerDefinition(BeaverModel.LAYER, BeaverGeometry::createBodyLayer);
        event.registerLayerDefinition(PeacockModel.LAYER, PeacockGeometry::createBodyLayer);
        event.registerLayerDefinition(StinkBugModel.LAYER, StinkBugGeometry::createBodyLayer);
        event.registerLayerDefinition(HydroliscModel.LAYER, HydroliscGeometry::createBodyLayer);
        // W06: cannon fodder.
        event.registerLayerDefinition(LizardModel.LAYER, LizardGeometry::createBodyLayer);
        event.registerLayerDefinition(ChipmunkModel.LAYER, ChipmunkGeometry::createBodyLayer);
        event.registerLayerDefinition(GazelleModel.LAYER, GazelleGeometry::createBodyLayer);
        // W06: velocity raptor, ostrich, ghost, ghost pumpkin skelly.
        event.registerLayerDefinition(VelocityRaptorModel.LAYER, VelocityRaptorGeometry::createBodyLayer);
        event.registerLayerDefinition(OstrichModel.LAYER, OstrichGeometry::createBodyLayer);
        event.registerLayerDefinition(GhostModel.LAYER, GhostGeometry::createBodyLayer);
        event.registerLayerDefinition(GhostSkellyModel.LAYER, GhostSkellyGeometry::createBodyLayer);
        // W07: dinosaurs and basilisk.
        event.registerLayerDefinition(AlosaurusModel.LAYER, AlosaurusGeometry::createBodyLayer);
        event.registerLayerDefinition(CryolophosaurusModel.LAYER, CryolophosaurusGeometry::createBodyLayer);
        event.registerLayerDefinition(TRexModel.LAYER, TRexGeometry::createBodyLayer);
        event.registerLayerDefinition(NastysaurusModel.LAYER, NastysaurusGeometry::createBodyLayer);
        event.registerLayerDefinition(PointysaurusModel.LAYER, PointysaurusGeometry::createBodyLayer);
        event.registerLayerDefinition(BasiliskModel.LAYER, BasiliskGeometry::createBodyLayer);
        // W07: arthropods.
        event.registerLayerDefinition(ScorpionModel.LAYER, ScorpionGeometry::createBodyLayer);
        event.registerLayerDefinition(EmperorScorpionModel.LAYER, EmperorScorpionGeometry::createBodyLayer);
        event.registerLayerDefinition(CaveFisherModel.LAYER, CaveFisherGeometry::createBodyLayer);
        event.registerLayerDefinition(BeeModel.LAYER, BeeGeometry::createBodyLayer);
        event.registerLayerDefinition(HerculesBeetleModel.LAYER, HerculesBeetleGeometry::createBodyLayer);
        event.registerLayerDefinition(SpitBugModel.LAYER, SpitBugGeometry::createBodyLayer);
        event.registerLayerDefinition(TrooperBugModel.LAYER, TrooperBugGeometry::createBodyLayer);
        // W07: monsters.
        event.registerLayerDefinition(KyuubiModel.LAYER, KyuubiGeometry::createBodyLayer);
        event.registerLayerDefinition(AlienModel.LAYER, AlienGeometry::createBodyLayer);
        event.registerLayerDefinition(LeafMonsterModel.LAYER, LeafMonsterGeometry::createBodyLayer);
        event.registerLayerDefinition(MolenoidModel.LAYER, MolenoidGeometry::createBodyLayer);
        event.registerLayerDefinition(HammerheadModel.LAYER, HammerheadGeometry::createBodyLayer);
        event.registerLayerDefinition(BandPModel.LAYER, BandPGeometry::createBodyLayer);
        // W07: ender mobs and fairy.
        event.registerLayerDefinition(EnderKnightModel.LAYER, EnderKnightGeometry::createBodyLayer);
        event.registerLayerDefinition(EnderReaperModel.LAYER, EnderReaperGeometry::createBodyLayer);
        event.registerLayerDefinition(FairyModel.LAYER, FairyGeometry::createBodyLayer);
        // W07: robots.
        event.registerLayerDefinition(Robot1Model.LAYER, Robot1Geometry::createBodyLayer);
        event.registerLayerDefinition(Robot2Model.LAYER, Robot2Geometry::createBodyLayer);
        event.registerLayerDefinition(Robot3Model.LAYER, Robot3Geometry::createBodyLayer);
        event.registerLayerDefinition(Robot4Model.LAYER, Robot4Geometry::createBodyLayer);
        event.registerLayerDefinition(Robot5Model.LAYER, Robot5Geometry::createBodyLayer);
        event.registerLayerDefinition(GiantRobotModel.LAYER, GiantRobotGeometry::createBodyLayer);
        // W07: worms.
        event.registerLayerDefinition(WormSmallModel.LAYER, WormSmallGeometry::createBodyLayer);
        event.registerLayerDefinition(WormMediumModel.LAYER, WormMediumGeometry::createBodyLayer);
        event.registerLayerDefinition(WormLargeModel.LAYER, WormLargeGeometry::createBodyLayer);
        // W08: water mobs, terrors, crystal mobs, Triffid, Nightmare, islands, CaterKiller, Brutalfly (Mothra uses ButterflyModel.LAYER).
        event.registerLayerDefinition(AttackSquidModel.LAYER, AttackSquidGeometry::createBodyLayer);
        event.registerLayerDefinition(CloudSharkModel.LAYER, CloudSharkGeometry::createBodyLayer);
        event.registerLayerDefinition(IrukandjiModel.LAYER, IrukandjiGeometry::createBodyLayer);
        event.registerLayerDefinition(SkateModel.LAYER, SkateGeometry::createBodyLayer);
        event.registerLayerDefinition(UrchinModel.LAYER, UrchinGeometry::createBodyLayer);
        event.registerLayerDefinition(SeaMonsterModel.LAYER, SeaMonsterGeometry::createBodyLayer);
        event.registerLayerDefinition(SeaViperModel.LAYER, SeaViperGeometry::createBodyLayer);
        event.registerLayerDefinition(CreepingHorrorModel.LAYER, CreepingHorrorGeometry::createBodyLayer);
        event.registerLayerDefinition(TerribleTerrorModel.LAYER, TerribleTerrorGeometry::createBodyLayer);
        event.registerLayerDefinition(LurkingTerrorModel.LAYER, LurkingTerrorGeometry::createBodyLayer);
        event.registerLayerDefinition(RatModel.LAYER, RatGeometry::createBodyLayer);
        event.registerLayerDefinition(MantisModel.LAYER, MantisGeometry::createBodyLayer);
        event.registerLayerDefinition(CrabModel.LAYER, CrabGeometry::createBodyLayer);
        event.registerLayerDefinition(RotatorModel.LAYER, RotatorGeometry::createBodyLayer);
        event.registerLayerDefinition(VortexModel.LAYER, VortexGeometry::createBodyLayer);
        event.registerLayerDefinition(DungeonBeastModel.LAYER, DungeonBeastGeometry::createBodyLayer);
        event.registerLayerDefinition(TriffidModel.LAYER, TriffidGeometry::createBodyLayer);
        event.registerLayerDefinition(PitchBlackModel.LAYER, PitchBlackGeometry::createBodyLayer);
        event.registerLayerDefinition(IslandModel.LAYER, IslandGeometry::createBodyLayer);
        event.registerLayerDefinition(CaterKillerModel.LAYER, CaterKillerGeometry::createBodyLayer);
        event.registerLayerDefinition(BrutalflyModel.LAYER, BrutalflyGeometry::createBodyLayer);
        // W08: the SquidZooka item model.
        SquidZookaRenderers.registerLayers(event);
        // W09
        event.registerLayerDefinition(SpyroModel.LAYER, SpyroGeometry::createBodyLayer);
        event.registerLayerDefinition(DragonModel.LAYER, DragonGeometry::createBodyLayer);
        event.registerLayerDefinition(CephadromeModel.LAYER, CephadromeGeometry::createBodyLayer);
        event.registerLayerDefinition(LeonModel.LAYER, LeonGeometry::createBodyLayer);
        event.registerLayerDefinition(GammaMetroidModel.LAYER, GammaMetroidGeometry::createBodyLayer);
        event.registerLayerDefinition(WaterDragonModel.LAYER, WaterDragonGeometry::createBodyLayer);
        event.registerLayerDefinition(StinkyModel.LAYER, StinkyGeometry::createBodyLayer);
        event.registerLayerDefinition(RubberDuckyModel.LAYER, RubberDuckyGeometry::createBodyLayer);
        event.registerLayerDefinition(AntRobotModel.LAYER, AntRobotGeometry::createBodyLayer);
        event.registerLayerDefinition(SpiderRobotModel.LAYER, SpiderRobotGeometry::createBodyLayer);
        // W10
        event.registerLayerDefinition(PurplePowerModel.LAYER, PurplePowerGeometry::createBodyLayer);
        event.registerLayerDefinition(KrakenModel.LAYER, KrakenGeometry::createBodyLayer);
        event.registerLayerDefinition(GodzillaModel.LAYER, GodzillaGeometry::createBodyLayer);
        event.registerLayerDefinition(TheKingModel.LAYER, TheKingGeometry::createBodyLayer);
        event.registerLayerDefinition(TheQueenModel.LAYER, TheQueenGeometry::createBodyLayer);
        event.registerLayerDefinition(ThePrinceModel.LAYER, ThePrinceGeometry::createBodyLayer);
        event.registerLayerDefinition(ThePrinceTeenModel.LAYER, ThePrinceTeenGeometry::createBodyLayer);
        event.registerLayerDefinition(ThePrincessModel.LAYER, ThePrincessGeometry::createBodyLayer);
        event.registerLayerDefinition(ThePrinceAdultModel.LAYER, ThePrinceAdultGeometry::createBodyLayer);
    }

    /** The eight {@code IItemRenderer}s (ClientProxyOreSpawn.java:149-156) as {@code IClientItemExtensions}. */
    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // MinecraftForgeClient.registerItemRenderer(MyBattleAxe / MyChainsaw / MyQueenBattleAxe, ...) (:154-156).
        BigWeaponRenderers.registerItemExtensions(event, ModItems.BATTLE_AXE, ModItems.CHAINSAW, ModItems.QUEEN_BATTLE_AXE);
        // Bertha, Slice, Royal, Hammy (:149-151, :153).
        BerthaRenderers.registerItemExtensions(event, ModItems.BERTHA, ModItems.SLICE, ModItems.ROYAL, ModItems.HAMMY);
        // MinecraftForgeClient.registerItemRenderer(MySquidZooka, new RenderSquidZooka()) (:152).
        SquidZookaRenderers.registerItemExtensions(event, ModItems.SQUID_ZOOKA);
    }

    /** {@code GirlfriendOverlayGui} (ClientProxyOreSpawn.java:15) as a {@code LayeredDraw.Layer}. */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // The original filtered RenderGameOverlayEvent for ElementType.HOTBAR, so the bar draws right after it.
        event.registerAbove(VanillaGuiLayers.HOTBAR, GirlfriendOverlayGui.ID, new GirlfriendOverlayGui(Minecraft.getInstance()));
    }
}
