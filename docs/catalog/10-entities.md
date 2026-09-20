# OreSpawn-Katalog

Erzeugt von `tools/catalog.py` aus dem Original-Jar (`orespawn-1.7.10-20.3.jar`, SHA-1 `d43dbe9a400dc8df06418da3e04d36422b2176d7`). Alle Zahlen stammen aus dem Jar; `?` heißt: statisch nicht auflösbar, nicht geraten.

Mehrere Werte in einer Zelle bedeuten Verzweigungen im Originalcode (zahm/wild, PlayNicely, Größenstufen).

## Entities

| id | Name | Klasse | Basis | HP | Angriff | Tempo | Rüstung | Hitbox b×h | Modell | Spawnregeln |
|---|---|---|---|---|---|---|---|---|---|---|
| `the_king` | The King | TheKing | EntityMob | 7000 | 250 | 0.62 | 25 / 22 / 23 / 24 / 21 | 22 / 24 / 5.5 / 6 | ModelTheKing | - |
| `king_head` | KingHead | KingHead | EntityLiving | 7000 | 0 | 1.33 | ? | 19.9 / 10 | ? | - |
| `the_queen` | The Queen | TheQueen | EntityMob | 6000 | 250 | 0.62 | 23 / 24 / 26 / 21 | 22 / 24 / 5.5 / 6 | ModelTheQueen | - |
| `queen_head` | QueenHead | QueenHead | EntityLiving | 6000 | 0 | 1.33 | ? | 19.9 / 10 | ? | - |
| `mobzilla` | Mobzilla | Godzilla | EntityMob | 4000 | 175 | 0.75 | 25 / 21 | 9.9 / 25 / 2.475 / 6.25 | ModelGodzilla | - |
| `mobzilla_head` | MobzillaHead | GodzillaHead | EntityLiving | 4000 | 0 | 1.33 | ? | 9.9 / 10 | ? | - |
| `the_young_adult_prince` | The Young Adult Prince | ThePrinceAdult | EntityTameable | 3000 | 100 | 0.36 | 20 | 6.25 / 10.25 | ModelThePrinceAdult | - |
| `the_young_prince` | The Young Prince | ThePrinceTeen | EntityTameable | 1500 | 50 | 0.32 | 18 | 3.25 / 4.25 | ModelThePrinceTeen | - |
| `robot_spider` | Robot Spider | SpiderRobot | EntityLiving | 1500 | 100 | 0.35 | 16 | 3.25 / 2.25 | ModelSpiderRobot | - |
| `purple_power` | PurplePower | PurplePower | EntityLiving | 1000 | 500 | 0.25 | 25 | 0.75 / 0.75 | ModelPurplePower | - |
| `the_kraken` | The Kraken | Kraken | EntityMob | 1000 | 40 | 0.37 | 10 | 4 / 15 / 1.3333 / 5 | ModelKraken | - |
| `girlfriend` | Girlfriend | Girlfriend | EntityTameable | 800 / 80 | 8 | 0.3 | 23 | 0.5 / 1.6 / 2.5 / 8 | ModelBiped | 12 |
| `jeffery` | Jeffery | GiantRobot | EntityMob | 550 | 40 | 0.55 | 18 | 3 / 9.75 | ModelGiantRobot | - |
| `the_prince` | The Prince | ThePrince | EntityTameable | 500 | 10 | 0.3 / 0.32 | 16 | 0.75 / 1.25 | ModelThePrince | - |
| `cater_killer` | CaterKiller | CaterKiller | EntityMob | 450 | 32 | 0.35 | 19 | 2.9 / 4.6 / 1.45 / 2.3 | ModelCaterKiller | 9 |
| `the_princess` | The Princess | ThePrincess | EntityTameable | 400 | 10 | 0.3 / 0.32 | 14 | 0.75 / 1.25 | ModelThePrincess | - |
| `emperor_scorpion` | Emperor Scorpion | EmperorScorpion | EntityMob | 350 | 35 | 0.35 | 20 | 3.5 / 3 | ModelEmperorScorpion | 2 |
| `cephadrome` | Cephadrome | Cephadrome | EntityCreature | 300 | 70 | 0.25 | 16 | 2.5 / 2.25 | ModelCephadrome | 2 |
| `robot_red_ant` | Robot Red Ant | AntRobot | EntityLiving | 300 | 30 | 0.3 | 16 | 2.75 / 1.25 | ModelAntRobot | - |
| `hercules_beetle` | Hercules Beetle | HerculesBeetle | EntityMob | 250 | 30 | 0.25 | 19 | 3.25 / 2.75 | ModelHerculesBeetle | 7 |
| `leonopteryx` | Leonopteryx | Leon | EntityTameable | 250 | 55 | 0.25 | 16 | 3.5 / 8.25 | ModelLeon | - |
| `hammerhead` | Hammerhead | Hammerhead | EntityMob | 240 | 75 | 0.35 | 20 | 3 / 5 | ModelHammerhead | - |
| `robo_pounder` | Robo-Pounder | Robot2 | EntityMob | 200 | 22 | 0.3 | 18 | 3 / 6.2 | ModelRobot2 | - |
| `basilisk` | Basilisk | Basilisk | EntityMob | 200 | 24 | 0.4 | 15 | 1.6 / 3.5 | ModelBasilisk | 4 |
| `baby_dragon` | Baby Dragon | Spyro | EntityTameable | 200 | 5 | 0.3 | 5 | 0.5 / 0.5 | ModelSpyro | - |
| `dragon` | Dragon | Dragon | EntityTameable | 200 | 35 | 0.32 | 14 | 1.5 / 1.25 | ModelDragon | - |
| `jumpy_bug` | Jumpy Bug | TrooperBug | EntityMob | 200 | 20 | 0.4 | 15 | 3 / 3.5 | ModelTrooperBug | 2 |
| `molenoid` | Molenoid | Molenoid | EntityMob | 200 | 18 | 0.35 | 12 | 3.9 / 2.6 | ModelMolenoid | 3 |
| `nastysaurus` | Nastysaurus | Nastysaurus | EntityMob | 200 | 32 | 0.35 | 17 | 2.2 / 4.6 | ModelNastysaurus | - |
| `robo_warrior` | Robo-Warrior | Robot4 | EntityMob | 170 | 12 | 0.34 | 18 | 2.5 / 4 | ModelRobot4 | - |
| `t_rex` | T. Rex | TRex | EntityMob | 160 | 22 | 0.38 | 14 | 2 / 4.2 | ModelTRex | - |
| `sea_viper` | Sea Viper | SeaViper | EntityMob | 160 | 22 | 0.25 / 0.35 | 12 | 1.5 / 2.5 | ModelSeaViper | 2 |
| `mothra` | Mothra | Mothra | EntityAmbientCreature | 150 | 12 | 0.35 | 8 | 5 / 2 | ModelButterfly | 2 |
| `water_dragon` | Water Dragon | WaterDragon | EntityTameable | 150 | 20 | 0.25 | 8 | 1.25 / 1.9 | ModelWaterDragon | 4 |
| `vortex` | Vortex | Vortex | EntityMob | 150 | 26 | 0.35 | 10 | 2 / 4 | ModelVortex | - |
| `kyuubi` | Kyuubi | Kyuubi | EntityMob | 125 | 10 | 0.25 | 10 | 0.5 / 1.25 | ModelKyuubi | 1 |
| `mantis` | Mantis | Mantis | EntityMob | 120 | 16 | 0.32 | 10 | 2.5 / 3.25 | ModelMantis | 9 |
| `alosaurus` | Alosaurus | Alosaurus | EntityMob | 110 | 18 | 0.35 | 8 | 1.9 / 3.6 | ModelAlosaurus | - |
| `sea_monster` | Sea Monster | SeaMonster | EntityMob | 110 | 14 | 0.25 | 8 | 1.25 / 2.5 | ModelSeaMonster | 2 |
| `brutalfly` | Brutalfly | Brutalfly | EntityMob | 110 | 10 | 0.35 | 6 | 5 / 2 | ModelBrutalfly | 3 |
| `hydrolisc` | Hydrolisc | Hydrolisc | EntityTameable | 100 | 1 | 0.25 | 10 | 0.5 / 0.5 | ModelHydrolisc | 4 |
| `wtf` | WTF? | GammaMetroid | EntityTameable | 100 | 10 | 0.15 | 12 | 1.5 / 1.5 | ModelGammaMetroid | - |
| `alien` | Alien | Alien | EntityMob | 100 | 12 | 0.65 | 8 | 1.1 / 3.25 | ModelAlien | - |
| `spit_bug` | Spit Bug | SpitBug | EntityMob | 100 | 10 | 0.33 | 12 | 2 / 2 | ModelSpitBug | 1 |
| `triffid` | Triffid | Triffid | EntityMob | 100 | 20 | 0.13 | 12 | 2 / 4 | ModelTriffid | - |
| `whale` | Whale | Whale | EntityAnimal | 100 | 0 | 0.35 | ? | 1.5 / 2.5 | ModelWhale | 1 |
| `stinky` | Stinky | Stinky | EntityTameable | 100 | 10 | 0.3 | 6 | 0.75 / 0.75 | ModelStinky | 4 |
| `criminal` | Criminal | BandP | EntityMob | 100 | 1 | 0.32 | 18 | 0.75 / 1.75 | ModelBandP | 3 |
| `large_worm` | Large Worm | WormLarge | EntityMob | 90 | 18 | 0.2 | 14 | 1.55 / 2.5 | ModelWormLarge | 3 |
| `ender_reaper` | Ender Reaper | EnderReaper | EntityMob | 90 | 18 | 0.37 | 8 | 0.7 / 2.9 | ModelEnderReaper | 9 |
| `bee` | Bee | Bee | EntityMob | 80 | 12 | 0.32 | 5 | 1.5 / 2.5 | ModelBee | 10 |
| `robo_gunner` | Robo-Gunner | Robot3 | EntityMob | 80 | 16 | 0.35 | 14 | 2.5 / 5 | ModelRobot3 | - |
| `boyfriend` | Boyfriend | Boyfriend | EntityTameable | 80 | 8 | 0.3 | 23 | 0.5 / 1.6 | ModelBiped | 12 |
| `pointysaurus` | Pointysaurus | Pointysaurus | EntityMob | 80 | 10 | 0.35 | 16 | 2.9 / 2.9 | ModelPointysaurus | - |
| `dungeon_beast` | Dungeon Beast | DungeonBeast | EntityMob | 65 | 12 | 0.29 | 6 | 1.15 / 1.1 | ModelDungeonBeast | 1 |
| `hoverboard` | Hoverboard | Elevator | EntityLiving | 60 | 0 | 1.33 | ? | 1.25 / 1 | ? | - |
| `ender_knight` | Ender Knight | EnderKnight | EntityMob | 60 | 12 | 0.32 | 6 | 0.6 / 2.9 | ModelEnderKnight | 9 |
| `baryonyx` | Baryonyx | Baryonyx | EntityAnimal | 40 | 8 | 0.25 | ? | 1.5 / 2.8 | ModelBaryonyx | - |
| `fairy` | Fairy | Fairy | EntityAmbientCreature | 40 | 3 | 0.1 | 4 | 0.4 / 0.8 | ModelFairy | 1 |
| `rotator` | Rotator | Rotator | EntityMob | 35 | 10 | 0.25 | 8 | 1 / 2 | ModelRotator | - |
| `lizard` | Lizard | Lizard | EntityTameable | 30 | 6 | 0.3 | 5 | 1.5 / 1.25 | ModelLizard | 3 |
| `lurking_terror` | Lurking Terror | LurkingTerror | EntityMob | 30 | 6 | 0.25 | 5 | 1.75 / 1.25 | ModelLurkingTerror | - |
| `medium_worm` | Medium Worm | WormMedium | EntityMob | 30 | 10 | 0.1 | 8 | 0.5 / 2 | ModelWormMedium | - |
| `ostrich` | Ostrich | Ostrich | EntityTameable | 25 | 6 | 0.2 / 0.38 | 3 / 0 | 0.85 / 2.1 | ModelOstrich | 4 |
| `crystal_urchin` | Crystal Urchin | Urchin | EntityMob | 25 | 10 | 0.3 | 4 | 1.35 / 2.1 | ModelUrchin | - |
| `robo_sniper` | Robo-Sniper | Robot5 | EntityMob | 20 | 5 | 0.3 | 6 | 1 / 2.25 | ModelRobot5 | - |
| `camarasaurus` | Camarasaurus | Camarasaurus | EntityTameable | 20 | 1 | 0.2 | ? | 0.5 / 1.2 | ModelCamarasaurus | - |
| `scorpion` | Scorpion | Scorpion | EntityMob | 15 | 4 | 0.2 | 10 | 0.85 / 0.55 | ModelScorpion | 7 |
| `gazelle` | Gazelle | Gazelle | EntityTameable | 15 | 0 | 0.2 / 0.3 | ? | 0.6 / 1.8 | ModelGazelle | - |
| `cloud_shark` | Cloud Shark | CloudShark | EntityMob | 15 | 6 | 0.3 | 5 | 1 / 0.75 | ModelCloudShark | - |
| `beaver` | Beaver | Beaver | EntityAnimal | 15 | 1 | 0.15 / 0.2 | ? | 0.6 / 0.8 | ModelBeaver | 6 |
| `peacock` | Peacock | Peacock | EntityAnimal | 15 | 4 | 0.38 | ? | 0.65 / 1.2 | ModelPeacock | 2 |
| `cryolophosaurus` | Cryolophosaurus | Cryolophosaurus | EntityMob | 10 | 3 | 0.25 | 1 | 0.75 / 0.75 | ModelCryolophosaurus | - |
| `velocity_raptor` | Velocity Raptor | VelocityRaptor | EntityTameable | 10 | 2 | 0.55 | 3 / 0 | 0.5 / 0.6 | ModelVelocityRaptor | - |
| `dragonfly` | Dragonfly | Dragonfly | EntityAnimal | 10 | 2 | 0.33 | ? | 1.5 / 0.5 | ModelDragonfly | 2 |
| `cave_fisher` | CaveFisher | CaveFisher | EntityMob | 10 | 4 | 0.2 | 4 | 1.35 / 0.75 | ModelCaveFisher | - |
| `attack_squid` | Attack Squid | AttackSquid | EntityMob | 10 | 8 | 0.25 | 0 | 1 / 1.25 | ModelAttackSquid | 3 |
| `creeping_horror` | Creeping Horror | CreepingHorror | EntityMob | 10 | 3 | 0.25 | 2 | 0.75 / 0.5 | ModelCreepingHorror | - |
| `terrible_terror` | Terrible Terror | TerribleTerror | EntityMob | 10 | 5 | 0.1 | 3 | 1 / 0.75 | ModelTerribleTerror | - |
| `small_worm` | Small Worm | WormSmall | EntityMob | 10 | 3 | 0.1 | 0 | 0.25 / 1 | ModelWormSmall | - |
| `cassowary` | Cassowary | Cassowary | EntityAnimal | 10 | 8 | 0.25 | ? | 0.5 / 1.2 | ModelCassowary | 9 |
| `easter_bunny` | EasterBunny | EasterBunny | EntityAnimal | 10 | 8 | 0.45 | ? | 0.5 / 0.75 | ModelEasterBunny | 7 |
| `skate` | Skate | Skate | EntityMob | 8 | 8 | 0.25 | 4 | 0.75 / 0.25 | ModelSkate | - |
| `frog` | Frog | Frog | EntityAnimal | 8 | 0 | 0.1 | ? | 0.75 / 0.75 | ModelFrog | 5 |
| `gold_fish` | Gold Fish | GoldFish | EntityAnimal | 6 | 1 | 0.22 | ? | 0.75 / 0.5 | ModelGoldFish | - |
| `leaf_monster` | Leaf Monster | LeafMonster | EntityMob | 6 | 2 | 0.25 | 1 | 1 / 2.5 | ModelLeafMonster | 8 |
| `bomb_omb` | Bomb-Omb | Robot1 | EntityMob | 5 | 4 | 0.2 | 2 | 0.5 / 0.5 | ModelRobot1 | - |
| `chipmunk` | Chipmunk | Chipmunk | EntityTameable | 5 | 1 | 0.38 | 3 / 0 | 0.35 / 0.35 | ModelChipmunk | 9 |
| `stink_bug` | Stink Bug | StinkBug | EntityAnimal | 5 | 0 | 0.15 | ? | 0.55 / 0.55 | ModelStinkBug | 5 |
| `cliff_racer` | Cliff Racer | CliffRacer | EntityAnimal | 5 | 1 | 0.33 | ? | 0.75 / 0.5 | ModelCliffRacer | - |
| `ghost_pumpkin_skelly` | Ghost Pumpkin Skelly | GhostSkelly | EntityAmbientCreature | 5 | 0 | 0.1 | ? | 1.5 / 2 | ModelGhostSkelly | 27 |
| `termite` | Termite | Termite | EntityAnimal | 5 | 2 | 0.2 | ? | 0.2 / 0.2 | ModelAnt | - |
| `rat` | Rat | Rat | EntityMob | 5 | 3 | 0.25 | 1 | 0.25 / 0.5 | ModelRat | 2 |
| `flounder` | Flounder | Flounder | EntityAnimal | 5 | 0 | 0.25 | ? | 0.55 / 0.25 | ModelFlounder | - |
| `rubber_ducky` | Rubber Ducky | RubberDucky | EntityTameable | 5 | 6 | 0.22 | 1 | 0.33 / 0.5 | ModelRubberDucky | 2 |
| `cricket` | Cricket | Cricket | EntityAnimal | 3 | 0 | 0.15 | ? | 0.1 / 0.1 | ModelCricket | 11 |
| `butterfly` | Butterfly | EntityButterfly | EntityAmbientCreature | 2 | 0 | 0.1 | ? | 0.4 / 0.4 | ModelButterfly | 16 |
| `moth` | Moth | EntityLunaMoth | EntityAmbientCreature | 2 | 0 | 0.1 | ? | 0.5 / 0.5 | ModelButterfly | 15 |
| `mosquito` | Mosquito | EntityMosquito | EntityAmbientCreature | 2 | 0 | 0.1 | ? | 0.2 / 0.2 | ModelMosquito | 4 |
| `red_ant` | Red Ant | EntityRedAnt | EntityAnimal | 2 | 1 | 0.2 | ? | 0.2 / 0.2 | ModelAnt | - |
| `bird` | Bird | Cockateil | EntityAnimal | 2 | 1 | 0.33 | ? | 0.5 / 0.5 | ModelCockateil | 16 |
| `ruby_bird` | Ruby Bird | RubyBird | EntityAnimal | 2 | 1 | 0.33 | ? | 0.5 / 0.5 | ModelCockateil | - |
| `ghost` | Ghost | Ghost | EntityAmbientCreature | 2 | 0 | 0.1 | ? | 0.5 / 1.5 | ModelGhost | 27 |
| `firefly` | Firefly | Firefly | EntityAmbientCreature | 1 | 0 | 0.1 | ? | 0.4 / 0.8 | ModelFirefly | 13 |
| `ant` | Ant | EntityAnt | EntityAnimal | 1 | 0 | 0.15 | ? | 0.1 / 0.1 | ModelAnt | - |
| `rainbow_ant` | Rainbow Ant | EntityRainbowAnt | EntityAnimal | 1 | 0 | 0.15 | ? | 0.1 / 0.1 | ModelAnt | - |
| `unstable_ant` | Unstable Ant | EntityUnstableAnt | EntityAnimal | 1 | 0 | 0.15 | ? | 0.1 / 0.1 | ModelAnt | - |
| `t_shirt` | T-Shirt | Tshirt | EntityAnimal | 1 | 0 | 0 | 0 | 4 / 4 | ModelTshirt | - |
| `irukandji` | Irukandji | Irukandji | EntityMob | 1 | 20 | 0.15 | 0 | 0.25 / 0.25 | ModelIrukandji | - |
| `coin` | Coin | Coin | EntityAnimal | 1 | 0 | 0 | 0 | 1.5 / 1.5 | ModelCoin | 6 |
| `ultimate_fish_hook` | ? | UltimateFishHook | EntityFishHook | ? | ? | ? | ? | 0.25 / 0.25 | ? | - |
| `sunspot_urchin` | SunspotUrchin | SunspotUrchin | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `water_ball` | WaterBall | WaterBall | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `ink_sack` | InkSack | InkSack | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `laser_ball` | LaserBall | LaserBall | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `ice_ball` | IceBall | IceBall | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `acid` | Acid | Acid | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `dead_irukandji` | DeadIrukandji | DeadIrukandji | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `bertha_hit` | BerthaHit | BerthaHit | EntityThrowable | ? | ? | ? | ? | 0.33 / 0.33 | ? | - |
| `entity_thrown_rock` | EntityThrownRock | EntityThrownRock | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `apple_cow` | Apple Cow | RedCow | EntityCow | ? | ? | ? | ? | ? | ModelCow | 6 |
| `golden_apple_cow` | Golden Apple Cow | GoldCow | EntityCow | ? | ? | ? | ? | ? | ModelCow | 4 |
| `enchanted_golden_apple_cow` | Enchanted Golden Apple Cow | EnchantedCow | EntityCow | ? | ? | ? | ? | ? | ModelCow | 4 |
| `island` | Island | Island | EntityAnimal | ? | ? | ? | ? | 0.5 / 0.5 | ModelIsland | - |
| `island_too` | IslandToo | IslandToo | EntityAnimal | ? | ? | ? | ? | 0.5 / 0.5 | ModelIsland | - |
| `nightmare` | Nightmare | PitchBlack | EntityMob | (OreSpawnMain.PitchBlack_stats.health * this->PitchBlack.getPitchBlackScale()) | (this->PitchBlack.getPitchBlackScale() * OreSpawnMain.PitchBlack_stats.attack) | (this.MyMoveSpeed + (0.10000000149011612 * this->PitchBlack.getPitchBlackScale())) | (OreSpawnMain.PitchBlack_stats.defense + (2.0 * this->PitchBlack.getPitchBlackScale())) | 2 / 3 | ModelPitchBlack | - |
| `crystal_apple_cow` | Crystal Apple Cow | CrystalCow | EntityCow | ? | ? | ? | ? | ? | ModelCow | - |
| `rock` | Rock | RockBase | EntityLiving | ? | ? | ? | 0 | 0.25 / 0.15 | ModelRockBase | - |
| `spider_driver` | Spider Driver | SpiderDriver | EntitySpider | ? | ? | ? | 8 / 20 | ? | ModelSpider | - |
| `crab` | Crab | Crab | EntityMob | (OreSpawnMain.PitchBlack_stats.health * this->Crab.getCrabScale()) | (OreSpawnMain.Crab_stats.attack * this->Crab.getCrabScale()) | (this.moveSpeed * this->Crab.getCrabScale()) | (OreSpawnMain.Crab_stats.defense + (2.0 * this->Crab.getCrabScale())) | 1.25 / 2.5 | ModelCrab | 3 |
| `shoes` | Shoes | Shoes | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `entity_cage` | EntityCage | EntityCage | EntityThrowable | ? | ? | ? | ? | ? | ? | - |
| `ultimate_arrow` | ? | UltimateArrow | EntityArrow | ? | ? | ? | ? | ? | ? | - |
| `irukandji_arrow` | ? | IrukandjiArrow | EntityArrow | ? | ? | ? | ? | ? | ? | - |
