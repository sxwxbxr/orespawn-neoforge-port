# OreSpawn-Katalog

Erzeugt von `tools/catalog.py` aus dem Original-Jar (`orespawn-1.7.10-20.3.jar`, SHA-1 `d43dbe9a400dc8df06418da3e04d36422b2176d7`). Alle Zahlen stammen aus dem Jar; `?` heißt: statisch nicht auflösbar, nicht geraten.

Mehrere Werte in einer Zelle bedeuten Verzweigungen im Originalcode (zahm/wild, PlayNicely, Größenstufen).

## Modelle (Geometrie aus dem Bytecode)

Jedes Teil ist genau eine Box. Pivot, Box und UV gehen unverändert in `PartPose`/`CubeListBuilder`; die Konventionen sind in `docs/research/06-models-design.md` belegt.

| Modell | benutzt von | Teile | Boxen | Textur | animierte Teile | Anim-Bytes | GL |
|---|---|---|---|---|---|---|---|
| ModelTriffid | triffid | 178 | 178 | 532 / 715 | 21 | 4264 | {'glPushMatrix': 1, 'glEnable': 1, 'glTranslatef': 2, 'glRotatef': 1, 'glPopMatrix': 1} |
| ModelTrooperBug | jumpy_bug | 134 | 134 | 512 / 256 | 69 | 4218 | {} |
| ModelTheQueen | the_queen | 130 | 130 | 2048 / 2048 | 116 | 11611 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 2, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelTheKing | the_king | 119 | 119 | 2048 / 2048 | 113 | 11048 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelThePrinceAdult | the_young_adult_prince | 119 | 119 | 2048 / 2048 | 113 | 10599 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelKraken | the_kraken | 111 | 111 | 512 / 512 | 62 | 4390 | {'glPushMatrix': 1, 'glTranslatef': 2, 'glRotatef': 1, 'glPopMatrix': 1} |
| ModelPitchBlack | nightmare | 101 | 101 | 512 / 256 | 96 | 5526 | {} |
| ModelLeon | leonopteryx | 98 | 98 | 256 / 256 | 93 | 5831 | {} |
| ModelSpitBug | spit_bug | 93 | 93 | 512 / 256 | 39 | 3308 | {} |
| ModelEmperorScorpion | emperor_scorpion | 78 | 78 | 256 / 128 | 28 | 3267 | {} |
| ModelCaveFisher | cave_fisher | 75 | 75 | 64 / 32 | 53 | 1588 | {} |
| ModelGodzilla | mobzilla | 71 | 71 | 1024 / 1024 | 58 | 4094 | {} |
| ModelLizard | lizard | 71 | 71 | 128 / 128 | 54 | 2684 | {} |
| ModelThePrinceTeen | the_young_prince | 71 | 71 | 512 / 256 | 65 | 5339 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelEnderReaper | ender_reaper | 66 | 66 | 512 / 512 | 12 | 1008 | {} |
| ModelDungeonBeast | dungeon_beast | 64 | 64 | 128 / 64 | 40 | 2340 | {'glRotatef': 1} |
| ModelLurkingTerror | lurking_terror | 59 | 59 | 256 / 64 | 58 | 1944 | {} |
| ModelNastysaurus | nastysaurus | 59 | 59 | 512 / 256 | 52 | 2301 | {} |
| ModelRobot4 | robo_warrior | 56 | 56 | 512 / 512 | 40 | 1515 | {} |
| ModelAlien | alien | 55 | 55 | 256 / 128 | 54 | 3593 | {} |
| ModelDragon | dragon | 55 | 55 | 256 / 128 | 50 | 3241 | {} |
| ModelBaryonyx | baryonyx | 52 | 52 | 128 / 128 | 9 | 689 | {} |
| ModelCephadrome | cephadrome | 50 | 50 | 512 / 256 | 44 | 2406 | {} |
| ModelStinkBug | stink_bug | 50 | 50 | 64 / 32 | 34 | 1030 | {} |
| ModelKyuubi | kyuubi | 42 | 42 | 512 / 256 | 38 | 3451 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glTranslatef': 2, 'glRotatef': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelEnderKnight | ender_knight | 40 | 40 | 512 / 512 | 25 | 1165 | {} |
| ModelHydrolisc | hydrolisc | 40 | 40 | 64 / 128 | 31 | 1061 | {} |
| ModelSpiderRobot | robot_spider | 39 | 39 | 256 / 512 | 26 | 2165 | {} |
| ModelOstrich | ostrich | 38 | 38 | 256 / 128 | 34 | 1425 | {} |
| ModelHammerhead | hammerhead | 37 | 37 | 222 / 256 | 31 | 1162 | {} |
| ModelHerculesBeetle | hercules_beetle | 37 | 37 | 256 / 256 | 28 | 786 | {} |
| ModelMolenoid | molenoid | 37 | 37 | 256 / 256 | 31 | 1760 | {} |
| ModelSpyro | baby_dragon | 37 | 37 | 64 / 64 | 32 | 1403 | {} |
| ModelThePrincess | the_princess | 37 | 37 | 128 / 128 | 32 | 2881 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelMantis | mantis | 36 | 36 | 256 / 256 | 11 | 886 | {} |
| ModelThePrince | the_prince | 35 | 35 | 128 / 128 | 29 | 2428 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelGazelle | gazelle | 34 | 34 | 64 / 64 | 31 | 837 | {} |
| ModelSeaViper | sea_viper | 34 | 34 | 128 / 128 | 14 | 1430 | {} |
| ModelVelocityRaptor | velocity_raptor | 34 | 34 | 128 / 128 | 27 | 910 | {} |
| ModelHammy | item:hammysmall | 33 | 33 | 128 / 256 | 1 | 284 | {} |
| ModelCaterKiller | cater_killer | 31 | 31 | 256 / 512 | 32 | 1666 | {} |
| ModelPointysaurus | pointysaurus | 30 | 30 | 128 / 128 | 28 | 1151 | {} |
| ModelAntRobot | robot_red_ant | 27 | 27 | 128 / 256 | 17 | 1616 | {} |
| ModelRobot1 | bomb_omb | 27 | 27 | 64 / 32 | 8 | 432 | {} |
| ModelTRex | t_rex | 27 | 27 | 128 / 128 | 12 | 537 | {} |
| ModelCreepingHorror | creeping_horror | 26 | 26 | 128 / 128 | 21 | 920 | {} |
| ModelDragonfly | dragonfly | 26 | 26 | 64 / 64 | 7 | 406 | {} |
| ModelCrab | crab | 25 | 25 | 256 / 512 | 16 | 1723 | {} |
| ModelWaterDragon | water_dragon | 24 | 24 | 128 / 128 | 19 | 1419 | {} |
| ModelBee | bee | 23 | 23 | 256 / 256 | 21 | 1127 | {} |
| ModelSeaMonster | sea_monster | 23 | 23 | 256 / 128 | 22 | 1646 | {} |
| ModelWormLarge | large_worm | 23 | 23 | 256 / 256 | 20 | 1846 | {} |
| ModelRockBase | rock | 22 | 22 | 64 / 64 | 1 | 392 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelScorpion | scorpion | 22 | 22 | 88 / 24 | 21 | 1285 | {} |
| ModelAlosaurus | alosaurus | 21 | 21 | 128 / 128 | 12 | 483 | {} |
| ModelBasilisk | basilisk | 21 | 21 | 256 / 64 | 12 | 1230 | {} |
| ModelCamarasaurus | camarasaurus | 21 | 21 | 256 / 256 | 18 | 951 | {} |
| ModelGammaMetroid | wtf | 21 | 21 | 256 / 64 | 10 | 1242 | {'glEnable': 2, 'glBlendFunc': 1, 'glDisable': 1} |
| ModelTerribleTerror | terrible_terror | 21 | 21 | 119 / 72 | 16 | 829 | {} |
| ModelAnt | ant / red_ant / rainbow_ant / unstable_ant / termite | 20 | 20 | 64 / 32 | 15 | 463 | {} |
| ModelCryolophosaurus | cryolophosaurus | 20 | 20 | 128 / 128 | 10 | 400 | {} |
| ModelStinky | stinky | 20 | 20 | 128 / 64 | 17 | 863 | {} |
| ModelRobot3 | robo_gunner | 19 | 19 | 512 / 512 | 12 | 512 | {} |
| ModelChipmunk | chipmunk | 18 | 18 | 64 / 32 | 16 | 554 | {} |
| ModelGiantRobot | jeffery | 18 | 18 | 256 / 512 | 19 | 2222 | {} |
| ModelUrchin | crystal_urchin | 17 | 17 | 128 / 128 | 18 | 1006 | {} |
| ModelCockateil | bird / ruby_bird | 16 | 16 | 64 / 32 | 11 | 413 | {} |
| ModelGoldFish | gold_fish | 16 | 16 | 64 / 64 | 8 | 415 | {} |
| ModelPeacock | peacock | 16 | 16 | 128 / 128 | 13 | 560 | {} |
| ModelBattleAxe | item:battleaxesmall | 15 | 15 | 128 / 64 | 1 | 140 | {} |
| ModelFairy | fairy | 15 | 15 | 64 / 64 | 8 | 533 | {'glColor4f': 1} |
| ModelRobot2 | robo_pounder | 15 | 15 | 256 / 512 | 12 | 581 | {} |
| ModelBrutalfly | brutalfly | 14 | 14 | 64 / 32 | 13 | 376 | {} |
| ModelSlice | item:slicesmall / item:royalsmall | 14 | 14 | 64 / 128 | 1 | 132 | {} |
| ModelWhale | whale | 14 | 14 | 256 / 256 | 10 | 562 | {} |
| ModelEasterBunny | easter_bunny | 13 | 13 | 64 / 128 | 7 | 334 | {} |
| ModelBertha | item:berthasmall | 12 | 12 | 64 / 128 | 1 | 116 | {} |
| ModelCassowary | cassowary | 12 | 12 | 64 / 32 | 10 | 392 | {} |
| ModelFirefly | firefly | 12 | 12 | 64 / 128 | 3 | 257 | {'glColor4f': 1} |
| ModelRat | rat | 12 | 12 | 64 / 64 | 7 | 394 | {} |
| ModelSquidZooka | item:squidzookasmall | 12 | 12 | 128 / 128 | 1 | 130 | {'glPushMatrix': 1, 'glRotatef': 1, 'glPopMatrix': 1} |
| ModelCricket | cricket | 11 | 11 | 64 / 64 | 9 | 412 | {} |
| ModelRobot5 | robo_sniper | 11 | 11 | 128 / 128 | 8 | 286 | {} |
| ModelButterfly | butterfly / moth / mothra | 10 | 10 | 64 / 32 | 9 | 282 | {} |
| ModelFrog | frog | 10 | 10 | 64 / 64 | 8 | 473 | {} |
| ModelGhostSkelly | ghost_pumpkin_skelly | 10 | 10 | 128 / 64 | 8 | 480 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelAttackSquid | attack_squid | 9 | 9 | 64 / 32 | 10 | 770 | {} |
| ModelBeaver | beaver | 9 | 9 | 64 / 32 | 7 | 266 | {} |
| ModelIrukandji | irukandji | 9 | 9 | 64 / 32 | 9 | 966 | {} |
| ModelQueenBattleAxe | item:queenbattleaxesmall | 9 | 9 | 128 / 64 | 1 | 92 | {} |
| ModelChainsaw | item:chainsawsmall | 8 | 8 | 64 / 64 | 3 | 908 | {} |
| ModelCliffRacer | cliff_racer | 8 | 8 | 64 / 64 | 3 | 179 | {} |
| ModelCloudShark | cloud_shark | 8 | 8 | 64 / 64 | 5 | 268 | {} |
| ModelRubberDucky | rubber_ducky | 8 | 8 | 64 / 64 | 5 | 462 | {} |
| ModelWormMedium | medium_worm | 8 | 8 | 64 / 32 | 9 | 999 | {} |
| ModelBandP | criminal | 7 | 7 | 64 / 128 | 7 | 339 | {} |
| ModelFlounder | flounder | 6 | 6 | 64 / 32 | 5 | 253 | {} |
| ModelElevator |  | 5 | 5 | 64 / 64 | 1 | 109 | {} |
| ModelLeafMonster | leaf_monster | 5 | 5 | 128 / 128 | 6 | 346 | {} |
| ModelMosquito | mosquito | 5 | 5 | 32 / 32 | 5 | 174 | {} |
| ModelGhost | ghost | 3 | 3 | 64 / 64 | 3 | 231 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 1, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelIsland | island / island_too | 3 | 3 | 64 / 32 | 4 | 337 | {} |
| ModelPurplePower | purple_power | 3 | 3 | 64 / 32 | 4 | 362 | {'glPushMatrix': 1, 'glEnable': 2, 'glBlendFunc': 1, 'glColor4f': 2, 'glRotatef': 6, 'glDisable': 1, 'glPopMatrix': 1} |
| ModelRotator | rotator | 3 | 3 | 64 / 32 | 4 | 318 | {'glRotatef': 6} |
| ModelSkate | skate | 3 | 3 | 64 / 32 | 2 | 158 | {} |
| ModelWormSmall | small_worm | 3 | 3 | 64 / 32 | 4 | 464 | {} |
| ModelTshirt | t_shirt | 2 | 2 | 512 / 256 | 3 | 121 | {} |
| ModelCoin | coin | 1 | 1 | 512 / 512 | 2 | 103 | {} |
| ModelVortex | vortex | 1 | 1 | 256 / 128 | 1 | 73 | {} |
