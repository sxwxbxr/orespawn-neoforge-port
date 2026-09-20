# OreSpawn-Katalog

Erzeugt von `tools/catalog.py` aus dem Original-Jar (`orespawn-1.7.10-20.3.jar`, SHA-1 `d43dbe9a400dc8df06418da3e04d36422b2176d7`). Alle Zahlen stammen aus dem Jar; `?` heißt: statisch nicht auflösbar, nicht geraten.

Mehrere Werte in einer Zelle bedeuten Verzweigungen im Originalcode (zahm/wild, PlayNicely, Größenstufen).

## Dimensionen

| Provider | Name | Chunk-Generator | Biome-Manager |
|---|---|---|---|
| WorldProviderOreSpawn | Dimension-Utopia | new ChunkProviderOreSpawn(this.field_76579_a, this.field_76579_a->World.func_72905_C(), 1) | WorldChunkManagerHell(this.MyPlains, 0.5) |
| WorldProviderOreSpawn2 | Dimension-Extreme | new ChunkProviderOreSpawn2(this.field_76579_a, this.field_76579_a->World.func_72905_C(), 1) | WorldChunkManagerHell(BiomeGenBase.field_76770_e, 0.009999999776482582) |
| WorldProviderOreSpawn3 | Dimension-VillageMania | new ChunkProviderOreSpawn3(this.field_76579_a, this.field_76579_a->World.func_72905_C(), 1) | WorldChunkManagerHell(this.MyPlains, 0.5) |
| WorldProviderOreSpawn4 | Dimension-Islands | new ChunkProviderOreSpawn4(this.field_76579_a, this.field_76579_a->World.func_72905_C(), 1) | WorldChunkManagerHell(this.MyPlains, 0.009999999776482582) |
| WorldProviderOreSpawn5 | Dimension-Crystal | new ChunkProviderOreSpawn5(this.field_76579_a, this.field_76579_a->World.func_72905_C(), 1) | BiomeGenBase$Height(0.10000000149011612, 0.5) / WorldChunkManagerHell(this.MyPlains, 0.009999999776482582) |
| WorldProviderOreSpawn6 | Dimension-Chaos | new ChunkProviderOreSpawn6(this.field_76579_a, this.field_76579_a->World.func_72905_C()) | WorldChunkManagerHell(this.MyPlains, 0.009999999776482582) |

## Sounds

| id | Dateien | benutzt von |
|---|---|---|
| alien_hurt | alien_hurt | Alien |
| alien_death | alien_death | Alien |
| alien_living | alien_living | Alien |
| alo_hurt | alo_hurt | Alosaurus / Basilisk / Cephadrome / Dragon / EmperorScorpion / Godzilla / Hammerhead / HerculesBeetle / Kyuubi / Lizard / Nastysaurus / Pointysaurus / TRex / ThePrinceTeen |
| alo_death | alo_death | Alosaurus / Bee / Cephadrome / Dragon / GammaMetroid / Kraken / Kyuubi / Lizard / Mantis / Nastysaurus / Pointysaurus / ThePrinceTeen / WormLarge |
| alo_living | alo_living | Alosaurus / Nastysaurus / Pointysaurus |
| b_dark | b_dark | Boyfriend |
| b_death_boyfriend | b_death_boyfriend | Boyfriend |
| b_death_single | b_death_single1 / b_death_single2 | Boyfriend |
| b_fight | b_fight1 / b_fight2 / b_fight3 / b_fight4 / b_fight5 / b_fight6 / b_fight7 | Boyfriend |
| b_happy | b_happy1 / b_happy2 / b_happy3 / b_happy4 / b_happy5 / b_happy6 / b_happy7 / b_happy8 | Boyfriend |
| b_hurt | b_hurt1 / b_hurt2 / b_hurt3 / b_hurt4 / b_hurt5 / b_hurt6 / b_hurt7 / b_hurt8 / b_hurt9 / b_hurt10 | Boyfriend |
| b_ow | b_ow1 / b_ow2 / b_ow3 / b_ow4 / b_ow5 / b_ow6 / b_ow7 / b_ow8 / b_ow9 | Boyfriend |
| b_rain | b_rain1 / b_rain2 | Boyfriend |
| b_taunt | b_taunt1 / b_taunt2 / b_taunt3 / b_taunt4 / b_taunt5 / b_taunt6 | Boyfriend |
| b_thunder | b_thunder | Boyfriend |
| b_water | b_water1 / b_water2 | Boyfriend |
| b_woohoo | b_woohoo1 / b_woohoo2 / b_woohoo3 / b_woohoo4 | Boyfriend |
| basilisk_living | basilisk_living | Basilisk |
| bb_happy | bb_happy1 / bb_happy2 / bb_happy3 / bb_happy4 / bb_happy5 / bb_happy6 | Boyfriend |
| beebuzz | beebuzz | Bee / Mantis |
| big_splat | big_splat | CloudShark / Fairy / Frog / Whale / WormLarge / WormMedium |
| birds | birds1 / birds2 / birds3 / birds4 / birds5 / birds6 / birds7 / birds8 / birds9 / birds10 / birds11 / birds12 / birds13 / birds14 / birds15 / birds16 / birds17 / birds18 / birds19 / birds20 / birds21 / birds22 / birds23 | Cockateil |
| caterkiller_death | caterkiller_death | CaterKiller |
| caterkiller_hit | caterkiller_hit1 / caterkiller_hit2 / caterkiller_hit3 / caterkiller_hit4 | CaterKiller |
| caterkiller_living | caterkiller_living1 / caterkiller_living2 / caterkiller_living3 / caterkiller_living4 | CaterKiller |
| chain_rattles | chain_rattles | GhostSkelly |
| chainsaw | chainsaw | Beaver |
| chainsawshort | chainsawshort | UltimateSword |
| clatter | clatter1 / clatter2 | SpitBug / TrooperBug |
| cliffracer | cliffracer | CliffRacer |
| creepinghorror_dead | creepinghorror_dead | CreepingHorror |
| creepinghorror_hit | creepinghorror_hit | CreepingHorror |
| creepinghorror_living | creepinghorror_living | CreepingHorror |
| cricket | cricket | Cricket |
| crunch | crunch | SpitBug / TrooperBug |
| cryo_death | cryo_death | Beaver / Camarasaurus / CaveFisher / Chipmunk / Cryolophosaurus / Gazelle / Hydrolisc / Ostrich / Scorpion / Spyro / Stinky / ThePrince / ThePrincess / VelocityRaptor |
| cryo_hurt | cryo_hurt | Camarasaurus / CaveFisher / Cryolophosaurus / Hydrolisc / Ostrich / VelocityRaptor |
| cryo_living | cryo_living | Cryolophosaurus |
| dbdead | dbdead | DungeonBeast |
| dbhit | dbhit1 / dbhit2 / dbhit3 | DungeonBeast |
| dragonfly_death | dragonfly_death | Dragonfly |
| dragonfly_hurt | dragonfly_hurt | Bee / Dragonfly / Mantis |
| dragonfly_living | dragonfly_living | Dragonfly |
| duck_hurt | duck_hurt | Baryonyx / Cassowary / Cockateil / EasterBunny / GammaMetroid / RubberDucky / Spyro / Stinky / ThePrince / ThePrincess |
| emperorscorpion_death | emperorscorpion_death | Basilisk / EmperorScorpion / SpitBug / TrooperBug |
| fart | fart1 / fart2 / fart3 / fart4 / fart5 / fart6 / fart7 / fart8 / fart9 | StinkBug / Stinky |
| ghost_sound | ghost_sound | Ghost |
| glassdead | glassdead1 / glassdead2 | EntityThrownRock / Rotator / Urchin |
| glasshit | glasshit1 / glasshit2 / glasshit3 / glasshit4 / glasshit5 | Rotator / Urchin |
| godzilla_death | godzilla_death | Godzilla |
| godzilla_living | godzilla_living | Godzilla |
| hammerhead_death | hammerhead_death | Hammerhead |
| hammerhead_living | hammerhead_living1 / hammerhead_living2 | Hammerhead |
| hercules_death | hercules_death | HerculesBeetle |
| hover | hover1 / hover2 / hover3 / hover4 / hover5 / hover6 | Elevator |
| king_hit | king_hit | TheKing / ThePrinceAdult / TheQueen |
| king_living | king_living | TheKing / ThePrinceAdult / TheQueen |
| kraken_living | kraken_living | Kraken |
| kyuubi_living | kyuubi_living | Kyuubi / Robot1 / Urchin |
| leaves_death | leaves_death | LeafMonster |
| leaves_hit | leaves_hit | Crab / LeafMonster |
| leon_death | leon_death | Leon |
| leon_hit | leon_hit1 / leon_hit2 / leon_hit3 | Leon |
| leon_living | leon_living | Leon |
| little_splat | little_splat | CloudShark / GoldFish / Whale / WormMedium / WormSmall |
| lurkinghorror_dead | lurkinghorror_dead | LurkingTerror |
| lurkinghorror_hit | lurkinghorror_hit | LurkingTerror |
| lurkinghorror_living | lurkinghorror_living | LurkingTerror |
| molenoid_death | molenoid_death | Molenoid |
| molenoid_hit | molenoid_hit1 / molenoid_hit2 / molenoid_hit3 / molenoid_hit4 / molenoid_hit5 / molenoid_hit6 | Molenoid |
| molenoid_living | molenoid_living1 / molenoid_living2 / molenoid_living3 | Molenoid |
| mosquito | mosquito | EntityMosquito |
| mothrawings | mothrawings1 / mothrawings2 / mothrawings3 | Brutalfly / Cephadrome / Dragon / Leon / Mothra / PitchBlack / TheKing / ThePrinceAdult / ThePrinceTeen / TheQueen |
| o_dark | o_dark | Girlfriend |
| o_death_girlfriend | o_death_girlfriend | Girlfriend |
| o_death_single | o_death_single | Girlfriend |
| o_fight | o_fight1 / o_fight2 / o_fight3 / o_fight4 / o_fight5 / o_fight6 / o_fight7 | Girlfriend |
| o_happy | o_happy1 / o_happy2 / o_happy3 / o_happy4 / o_happy5 / o_happy6 / o_happy7 | Girlfriend |
| o_hurt | o_hurt1 / o_hurt2 / o_hurt3 / o_hurt4 / o_hurt5 / o_hurt6 / o_hurt7 / o_hurt8 / o_hurt9 | Girlfriend |
| o_ow | o_ow1 / o_ow2 / o_ow3 / o_ow4 / o_ow5 / o_ow6 / o_ow7 / o_ow8 | Girlfriend |
| o_taunt | o_taunt1 / o_taunt2 / o_taunt3 / o_taunt4 | Girlfriend |
| o_thunder | o_thunder | Girlfriend |
| o_water | o_water1 / o_water2 | Girlfriend |
| o_woohoo | o_woohoo1 / o_woohoo2 / o_woohoo3 / o_woohoo4 | Girlfriend |
| peacockdead | peacockdead | Peacock |
| peacockhit | peacockhit | Peacock |
| peacocklive | peacocklive | Peacock |
| pitchblack_dead | pitchblack_dead | PitchBlack |
| pitchblack_hit | pitchblack_hit | PitchBlack |
| pitchblack_living | pitchblack_living | PitchBlack |
| ratdead | ratdead1 / ratdead2 / ratdead3 | Flounder / Irukandji / Rat / Skate |
| rathit | rathit | Rat |
| ratlive | ratlive | Rat |
| roar | roar1 / roar2 / roar3 / roar4 / roar5 / roar6 | Dragon / Spyro / ThePrince / ThePrinceTeen / ThePrincess |
| robot_death | robot_death1 / robot_death2 / robot_death3 / robot_death4 / robot_death5 | GiantRobot / Robot2 / Robot3 / Robot4 / Robot5 |
| robot_hurt | robot_hurt1 / robot_hurt2 / robot_hurt3 / robot_hurt4 / robot_hurt5 / robot_hurt6 / robot_hurt7 / robot_hurt8 | GiantRobot / Robot2 / Robot3 / Robot4 / Robot5 |
| robot_living | robot_living1 / robot_living2 / robot_living3 / robot_living4 | GiantRobot / Robot2 / Robot3 / Robot4 / Robot5 |
| robot1_death | robot1_death | Robot1 |
| robotspider | robotspider1 / robotspider2 / robotspider3 / robotspider4 / robotspider5 / robotspider6 / robotspider7 / robotspider8 / robotspider9 / robotspider10 / robotspider11 | AntRobot / SpiderRobot |
| robotspidermount | robotspidermount | AntRobot / SpiderRobot |
| rubybird | rubybird | RubyBird |
| scorpion_attack | scorpion_attack | Crab / EmperorScorpion / HerculesBeetle / Scorpion / TrooperBug |
| scorpion_hit | scorpion_hit | Beaver / Chipmunk / Frog / Gazelle / Robot1 / Scorpion |
| scorpion_living | scorpion_living | Crab / EmperorScorpion / HerculesBeetle |
| seamonster_death | seamonster_death | SeaMonster |
| seamonster_hit | seamonster_hit | SeaMonster |
| seamonster_living | seamonster_living1 / seamonster_living2 | SeaMonster |
| seaviper_death | seaviper_death | SeaViper |
| seaviper_hit | seaviper_hit1 / seaviper_hit2 / seaviper_hit3 | SeaViper |
| seaviper_living | seaviper_living | SeaViper |
| squid_death | squid_death1 / squid_death2 | AttackSquid |
| squid_hurt | squid_hurt1 / squid_hurt2 / squid_hurt3 / squid_hurt4 | AttackSquid |
| terribleterror_dead | terribleterror_dead | TerribleTerror |
| terribleterror_hit | terribleterror_hit | TerribleTerror |
| terribleterror_living | terribleterror_living | TerribleTerror |
| trex_death | trex_death | TRex / TheKing / ThePrinceAdult / TheQueen |
| trex_living | trex_living | TRex |
| triffid_dead | triffid_dead | Triffid |
| triffid_hit | triffid_hit | Triffid |
| triffid_living | triffid_living | Triffid |
| vortexlive | vortexlive | Vortex |
| waterdragon_death | waterdragon_death | WaterDragon |
| waterdragon_hurt | waterdragon_hurt1 / waterdragon_hurt2 / waterdragon_hurt3 | WaterDragon |
| wtf_hurt | wtf_hurt |  |
| wtf_living | wtf_living | GammaMetroid |

## Config-Schlüssel

| Kategorie | Schlüssel | Default | Typ | Kommentar |
|---|---|---|---|---|
| OreSpawnIDS | BaseBlockID | 2700 | I | ? |
| OreSpawnIDS | BaseItemID | 9000 | I | ? |
| OreSpawnIDS | BaseBiomeID | 120 | I | ? |
| OreSpawnIDS | BaseDimensionID | 80 | I | ? |
| OreSpawnTWEAKS | AllMobsDisable | 0 | I | ? |
| OreSpawnTWEAKS | LessOre | 0 | I | ? |
| OreSpawnTWEAKS | LessLag | 0 | I | ? |
| OreSpawnTWEAKS | RatPlayerFriendly | 1 | I | ? |
| OreSpawnTWEAKS | RatPetFriendly | 1 | I | ? |
| OreSpawnTWEAKS | NightmareSize | 0 | I | ? |
| OreSpawnTWEAKS | IslandSpeedFactor | 2 | I | ? |
| OreSpawnTWEAKS | IslandSizeFactor | 2 | I | ? |
| OreSpawnTWEAKS | GinormousEmeraldTreeEnable | 1 | I | ? |
| OreSpawnTWEAKS | GuiOverlayEnable | 1 | I | ? |
| OreSpawnTWEAKS | UltimateSwordPvp | 0 | I | ? |
| OreSpawnTWEAKS | BigBerthaPvp | 0 | I | ? |
| OreSpawnTWEAKS | BoyfriendBroMode | 0 | I | ? |
| OreSpawnTWEAKS | DuplicatorTreeEnable | 1 | I | ? |
| OreSpawnTWEAKS | RoyalGlideEnable | 1 | I | ? |
| OreSpawnTWEAKS | DragonflyHorseFriendly | 0 | I | ? |
| OreSpawnTWEAKS | PlayNicely | 0 | I | ? |
| OreSpawnTWEAKS | MinersDreamExpensive | 0 | I | ? |
| OreSpawnTWEAKS | DisableOverworldDungeons | 0 | I | ? |
| OreSpawnTWEAKS | FullPowerKingEnable | 0 | I | ? |
| OreSpawnWEAPONS | UltimateSwordEnchantmentLevel | 5 | I | ? |
| OreSpawnWEAPONS | UltimateBowDamage | 10 | I | ? |
| OreSpawnMOBS | MosquitoEnable | 1 | I | ? |
| OreSpawnMOBS | RockEnable | 1 | I | ? |
| OreSpawnMOBS | GhostEnable | 1 | I | ? |
| OreSpawnMOBS | GhostSkellyEnable | 1 | I | ? |
| OreSpawnMOBS | SpiderDriverEnable | 1 | I | ? |
| OreSpawnMOBS | JefferyEnable | 1 | I | ? |
| OreSpawnMOBS | MothraEnable | 1 | I | ? |
| OreSpawnMOBS | BrutalflyEnable | 1 | I | ? |
| OreSpawnMOBS | NastysaurusEnable | 1 | I | ? |
| OreSpawnMOBS | PointysaurusEnable | 1 | I | ? |
| OreSpawnMOBS | CricketEnable | 1 | I | ? |
| OreSpawnMOBS | FrogEnable | 1 | I | ? |
| OreSpawnMOBS | MothraPeaceful | 0 | I | ? |
| OreSpawnMOBS | BlackAntEnable | 1 | I | ? |
| OreSpawnMOBS | RedAntEnable | 1 | I | ? |
| OreSpawnMOBS | TermiteEnable | 1 | I | ? |
| OreSpawnMOBS | UnstableAntEnable | 1 | I | ? |
| OreSpawnMOBS | RainbowedAntEnable | 1 | I | ? |
| OreSpawnMOBS | AlosaurusEnable | 1 | I | ? |
| OreSpawnMOBS | HammerheadEnable | 1 | I | ? |
| OreSpawnMOBS | LeonEnable | 1 | I | ? |
| OreSpawnMOBS | CaterKillerEnable | 1 | I | ? |
| OreSpawnMOBS | MolenoidEnable | 1 | I | ? |
| OreSpawnMOBS | TRexEnable | 1 | I | ? |
| OreSpawnMOBS | CriminalEnable | 1 | I | ? |
| OreSpawnMOBS | CryolophosaurusEnable | 1 | I | ? |
| OreSpawnMOBS | RatEnable | 1 | I | ? |
| OreSpawnMOBS | UrchinEnable | 1 | I | ? |
| OreSpawnMOBS | CamarasaurusEnable | 1 | I | ? |
| OreSpawnMOBS | VelocityRaptorEnable | 1 | I | ? |
| OreSpawnMOBS | HydroliscEnable | 1 | I | ? |
| OreSpawnMOBS | SpyroEnable | 1 | I | ? |
| OreSpawnMOBS | BaryonyxEnable | 1 | I | ? |
| OreSpawnMOBS | BirdEnable | 1 | I | ? |
| OreSpawnMOBS | CassowaryEnable | 1 | I | ? |
| OreSpawnMOBS | EasterBunnyEnable | 1 | I | ? |
| OreSpawnMOBS | PeacockEnable | 1 | I | ? |
| OreSpawnMOBS | KyuubiEnable | 1 | I | ? |
| OreSpawnMOBS | CephadromeEnable | 1 | I | ? |
| OreSpawnMOBS | DragonEnable | 1 | I | ? |
| OreSpawnMOBS | GammaMetroidEnable | 1 | I | ? |
| OreSpawnMOBS | BasiliskEnable | 1 | I | ? |
| OreSpawnMOBS | DragonflyEnable | 1 | I | ? |
| OreSpawnMOBS | EmperorScorpionEnable | 1 | I | ? |
| OreSpawnMOBS | TrooperBugEnable | 1 | I | ? |
| OreSpawnMOBS | SpitBugEnable | 1 | I | ? |
| OreSpawnMOBS | StinkBugEnable | 1 | I | ? |
| OreSpawnMOBS | ScorpionEnable | 1 | I | ? |
| OreSpawnMOBS | CaveFisherEnable | 1 | I | ? |
| OreSpawnMOBS | AlienEnable | 1 | I | ? |
| OreSpawnMOBS | WaterDragonEnable | 1 | I | ? |
| OreSpawnMOBS | SeaMonsterEnable | 1 | I | ? |
| OreSpawnMOBS | SeaViperEnable | 1 | I | ? |
| OreSpawnMOBS | AttackSquidEnable | 1 | I | ? |
| OreSpawnMOBS | Robot1Enable | 1 | I | ? |
| OreSpawnMOBS | Robot2Enable | 1 | I | ? |
| OreSpawnMOBS | Robot3Enable | 1 | I | ? |
| OreSpawnMOBS | Robot4Enable | 1 | I | ? |
| OreSpawnMOBS | Robot5Enable | 1 | I | ? |
| OreSpawnMOBS | RotatorEnable | 1 | I | ? |
| OreSpawnMOBS | VortexEnable | 1 | I | ? |
| OreSpawnMOBS | DungeonBeastEnable | 1 | I | ? |
| OreSpawnMOBS | KrakenEnable | 1 | I | ? |
| OreSpawnMOBS | LizardEnable | 1 | I | ? |
| OreSpawnMOBS | RubberDuckyEnable | 1 | I | ? |
| OreSpawnMOBS | GirlfriendEnable | 1 | I | ? |
| OreSpawnMOBS | BoyfriendEnable | 0 | I | ? |
| OreSpawnMOBS | FireflyEnable | 1 | I | ? |
| OreSpawnMOBS | FairyEnable | 1 | I | ? |
| OreSpawnMOBS | BeeEnable | 1 | I | ? |
| OreSpawnMOBS | TheKingEnable | 1 | I | ? |
| OreSpawnMOBS | TheQueenEnable | 1 | I | ? |
| OreSpawnMOBS | MantisEnable | 1 | I | ? |
| OreSpawnMOBS | StinkyEnable | 1 | I | ? |
| OreSpawnMOBS | HerculesBeetleEnable | 1 | I | ? |
| OreSpawnMOBS | ChipmunkEnable | 1 | I | ? |
| OreSpawnMOBS | OstrichEnable | 1 | I | ? |
| OreSpawnMOBS | GazelleEnable | 1 | I | ? |
| OreSpawnMOBS | CowEnable | 1 | I | ? |
| OreSpawnMOBS | ButterflyEnable | 1 | I | ? |
| OreSpawnMOBS | MothEnable | 1 | I | ? |
| OreSpawnMOBS | TshirtEnable | 1 | I | ? |
| OreSpawnMOBS | CoinEnable | 1 | I | ? |
| OreSpawnMOBS | CreepingHorrorEnable | 1 | I | ? |
| OreSpawnMOBS | TerribleTerrorEnable | 1 | I | ? |
| OreSpawnMOBS | CliffRacerEnable | 1 | I | ? |
| OreSpawnMOBS | TriffidEnable | 1 | I | ? |
| OreSpawnMOBS | WormEnable | 1 | I | ? |
| OreSpawnMOBS | CloudSharkEnable | 1 | I | ? |
| OreSpawnMOBS | GoldFishEnable | 1 | I | ? |
| OreSpawnMOBS | LeafMonsterEnable | 1 | I | ? |
| OreSpawnMOBS | EnderKnightEnable | 1 | I | ? |
| OreSpawnMOBS | EnderReaperEnable | 1 | I | ? |
| OreSpawnMOBS | BeaverEnable | 1 | I | ? |
| OreSpawnMOBS | IrukandjiEnable | 1 | I | ? |
| OreSpawnMOBS | SkateEnable | 1 | I | ? |
| OreSpawnMOBS | WhaleEnable | 1 | I | ? |
| OreSpawnMOBS | FlounderEnable | 1 | I | ? |
| OreSpawnMOBS | NightmareEnable | 1 | I | ? |
| OreSpawnMOBS | LurkingTerrorEnable | 1 | I | ? |
| OreSpawnMOBS | GodzillaEnable | 1 | I | ? |
| OreSpawnMOBS | CrabEnable | 1 | I | ? |
| OreSpawnMOBS | Bee_health | 80 | I | ? |
| OreSpawnMOBS | Bee_attack | 12 | I | ? |
| OreSpawnMOBS | Bee_defense | 5 | I | ? |
| OreSpawnMOBS | Mantis_health | 120 | I | ? |
| OreSpawnMOBS | Mantis_attack | 16 | I | ? |
| OreSpawnMOBS | Mantis_defense | 10 | I | ? |
| OreSpawnMOBS | HerculesBeetle_health | 250 | I | ? |
| OreSpawnMOBS | HerculesBeetle_attack | 30 | I | ? |
| OreSpawnMOBS | HerculesBeetle_defense | 19 | I | ? |
| OreSpawnMOBS | Mothra_health | 150 | I | ? |
| OreSpawnMOBS | Mothra_attack | 12 | I | ? |
| OreSpawnMOBS | Mothra_defense | 8 | I | ? |
| OreSpawnMOBS | Brutalfly_health | 110 | I | ? |
| OreSpawnMOBS | Brutalfly_attack | 10 | I | ? |
| OreSpawnMOBS | Brutalfly_defense | 6 | I | ? |
| OreSpawnMOBS | Nastysaurus_health | 200 | I | ? |
| OreSpawnMOBS | Nastysaurus_attack | 32 | I | ? |
| OreSpawnMOBS | Nastysaurus_defense | 17 | I | ? |
| OreSpawnMOBS | Pointysaurus_health | 80 | I | ? |
| OreSpawnMOBS | Pointysaurus_attack | 10 | I | ? |
| OreSpawnMOBS | Pointysaurus_defense | 16 | I | ? |
| OreSpawnMOBS | Alosaurus_health | 110 | I | ? |
| OreSpawnMOBS | Alosaurus_attack | 18 | I | ? |
| OreSpawnMOBS | Alosaurus_defense | 8 | I | ? |
| OreSpawnMOBS | SpiderRobot_health | 1500 | I | ? |
| OreSpawnMOBS | SpiderRobot_attack | 100 | I | ? |
| OreSpawnMOBS | SpiderRobot_defense | 16 | I | ? |
| OreSpawnMOBS | AntRobot_health | 300 | I | ? |
| OreSpawnMOBS | AntRobot_attack | 30 | I | ? |
| OreSpawnMOBS | AntRobot_defense | 16 | I | ? |
| OreSpawnMOBS | Jeffery_health | 550 | I | ? |
| OreSpawnMOBS | Jeffery_attack | 40 | I | ? |
| OreSpawnMOBS | Jeffery_defense | 18 | I | ? |
| OreSpawnMOBS | Hammerhead_health | 240 | I | ? |
| OreSpawnMOBS | Hammerhead_attack | 75 | I | ? |
| OreSpawnMOBS | Hammerhead_defense | 20 | I | ? |
| OreSpawnMOBS | Molenoid_health | 200 | I | ? |
| OreSpawnMOBS | Molenoid_attack | 18 | I | ? |
| OreSpawnMOBS | Molenoid_defense | 12 | I | ? |
| OreSpawnMOBS | TRex_health | 160 | I | ? |
| OreSpawnMOBS | TRex_attack | 22 | I | ? |
| OreSpawnMOBS | TRex_defense | 14 | I | ? |
| OreSpawnMOBS | BandP_health | 100 | I | ? |
| OreSpawnMOBS | BandP_attack | 1 | I | ? |
| OreSpawnMOBS | BandP_defense | 18 | I | ? |
| OreSpawnMOBS | CaterKiller_health | 450 | I | ? |
| OreSpawnMOBS | CaterKiller_attack | 32 | I | ? |
| OreSpawnMOBS | CaterKiller_defense | 19 | I | ? |
| OreSpawnMOBS | Cryolophosaurus_health | 10 | I | ? |
| OreSpawnMOBS | Cryolophosaurus_attack | 3 | I | ? |
| OreSpawnMOBS | Cryolophosaurus_defense | 1 | I | ? |
| OreSpawnMOBS | Rat_health | 5 | I | ? |
| OreSpawnMOBS | Rat_attack | 3 | I | ? |
| OreSpawnMOBS | Rat_defense | 1 | I | ? |
| OreSpawnMOBS | Urchin_health | 25 | I | ? |
| OreSpawnMOBS | Urchin_attack | 10 | I | ? |
| OreSpawnMOBS | Urchin_defense | 4 | I | ? |
| OreSpawnMOBS | Kyuubi_health | 125 | I | ? |
| OreSpawnMOBS | Kyuubi_attack | 10 | I | ? |
| OreSpawnMOBS | Kyuubi_defense | 10 | I | ? |
| OreSpawnMOBS | GammaMetroid_health | 100 | I | ? |
| OreSpawnMOBS | GammaMetroid_attack | 10 | I | ? |
| OreSpawnMOBS | GammaMetroid_defense | 12 | I | ? |
| OreSpawnMOBS | Basilisk_health | 200 | I | ? |
| OreSpawnMOBS | Basilisk_attack | 24 | I | ? |
| OreSpawnMOBS | Basilisk_defense | 15 | I | ? |
| OreSpawnMOBS | EmperorScorpion_health | 350 | I | ? |
| OreSpawnMOBS | EmperorScorpion_attack | 35 | I | ? |
| OreSpawnMOBS | EmperorScorpion_defense | 20 | I | ? |
| OreSpawnMOBS | TrooperBug_health | 200 | I | ? |
| OreSpawnMOBS | TrooperBug_attack | 20 | I | ? |
| OreSpawnMOBS | TrooperBug_defense | 15 | I | ? |
| OreSpawnMOBS | SpitBug_health | 100 | I | ? |
| OreSpawnMOBS | SpitBug_attack | 10 | I | ? |
| OreSpawnMOBS | SpitBug_defense | 12 | I | ? |
| OreSpawnMOBS | Alien_health | 100 | I | ? |
| OreSpawnMOBS | Alien_attack | 12 | I | ? |
| OreSpawnMOBS | Alien_defense | 8 | I | ? |
| OreSpawnMOBS | WaterDragon_health | 150 | I | ? |
| OreSpawnMOBS | WaterDragon_attack | 20 | I | ? |
| OreSpawnMOBS | WaterDragon_defense | 8 | I | ? |
| OreSpawnMOBS | SeaMonster_health | 110 | I | ? |
| OreSpawnMOBS | SeaMonster_attack | 14 | I | ? |
| OreSpawnMOBS | SeaMonster_defense | 8 | I | ? |
| OreSpawnMOBS | SeaViper_health | 160 | I | ? |
| OreSpawnMOBS | SeaViper_attack | 22 | I | ? |
| OreSpawnMOBS | SeaViper_defense | 12 | I | ? |
| OreSpawnMOBS | Robot2_health | 200 | I | ? |
| OreSpawnMOBS | Robot2_attack | 22 | I | ? |
| OreSpawnMOBS | Robot2_defense | 18 | I | ? |
| OreSpawnMOBS | Robot3_health | 80 | I | ? |
| OreSpawnMOBS | Robot3_attack | 16 | I | ? |
| OreSpawnMOBS | Robot3_defense | 14 | I | ? |
| OreSpawnMOBS | Robot4_health | 170 | I | ? |
| OreSpawnMOBS | Robot4_attack | 12 | I | ? |
| OreSpawnMOBS | Robot4_defense | 18 | I | ? |
| OreSpawnMOBS | Robot5_health | 20 | I | ? |
| OreSpawnMOBS | Robot5_attack | 5 | I | ? |
| OreSpawnMOBS | Robot5_defense | 6 | I | ? |
| OreSpawnMOBS | Rotator_health | 35 | I | ? |
| OreSpawnMOBS | Rotator_attack | 10 | I | ? |
| OreSpawnMOBS | Rotator_defense | 8 | I | ? |
| OreSpawnMOBS | Vortex_health | 150 | I | ? |
| OreSpawnMOBS | Vortex_attack | 26 | I | ? |
| OreSpawnMOBS | Vortex_defense | 10 | I | ? |
| OreSpawnMOBS | DungeonBeast_health | 65 | I | ? |
| OreSpawnMOBS | DungeonBeast_attack | 12 | I | ? |
| OreSpawnMOBS | DungeonBeast_defense | 6 | I | ? |
| OreSpawnMOBS | Triffid_health | 100 | I | ? |
| OreSpawnMOBS | Triffid_attack | 20 | I | ? |
| OreSpawnMOBS | Triffid_defense | 12 | I | ? |
| OreSpawnMOBS | LurkingTerror_health | 30 | I | ? |
| OreSpawnMOBS | LurkingTerror_attack | 6 | I | ? |
| OreSpawnMOBS | LurkingTerror_defense | 5 | I | ? |
| OreSpawnMOBS | WormSmall_health | 10 | I | ? |
| OreSpawnMOBS | WormSmall_attack | 3 | I | ? |
| OreSpawnMOBS | WormSmall_defense | 0 | I | ? |
| OreSpawnMOBS | WormMedium_health | 30 | I | ? |
| OreSpawnMOBS | WormMedium_attack | 10 | I | ? |
| OreSpawnMOBS | WormMedium_defense | 8 | I | ? |
| OreSpawnMOBS | WormLarge_health | 90 | I | ? |
| OreSpawnMOBS | WormLarge_attack | 18 | I | ? |
| OreSpawnMOBS | WormLarge_defense | 14 | I | ? |
| OreSpawnMOBS | EnderKnight_health | 60 | I | ? |
| OreSpawnMOBS | EnderKnight_attack | 12 | I | ? |
| OreSpawnMOBS | EnderKnight_defense | 6 | I | ? |
| OreSpawnMOBS | EnderReaper_health | 90 | I | ? |
| OreSpawnMOBS | EnderReaper_attack | 18 | I | ? |
| OreSpawnMOBS | EnderReaper_defense | 8 | I | ? |
| OreSpawnMOBS | Irukandji_health | 1 | I | ? |
| OreSpawnMOBS | Irukandji_attack | 20 | I | ? |
| OreSpawnMOBS | Irukandji_defense | 0 | I | ? |
| OreSpawnMOBS | AttackSquid_health | 10 | I | ? |
| OreSpawnMOBS | AttackSquid_attack | 8 | I | ? |
| OreSpawnMOBS | AttackSquid_defense | 0 | I | ? |
| OreSpawnMOBS | CaveFisher_health | 10 | I | ? |
| OreSpawnMOBS | CaveFisher_attack | 4 | I | ? |
| OreSpawnMOBS | CaveFisher_defense | 4 | I | ? |
| OreSpawnMOBS | CloudShark_health | 15 | I | ? |
| OreSpawnMOBS | CloudShark_attack | 6 | I | ? |
| OreSpawnMOBS | CloudShark_defense | 5 | I | ? |
| OreSpawnMOBS | CreepingHorror_health | 10 | I | ? |
| OreSpawnMOBS | CreepingHorror_attack | 3 | I | ? |
| OreSpawnMOBS | CreepingHorror_defense | 2 | I | ? |
| OreSpawnMOBS | Mobzilla_health | 4000 | I | ? |
| OreSpawnMOBS | Mobzilla_attack | 175 | I | ? |
| OreSpawnMOBS | Mobzilla_defense | 21 | I | ? |
| OreSpawnMOBS | Kraken_health | 1000 | I | ? |
| OreSpawnMOBS | Kraken_attack | 40 | I | ? |
| OreSpawnMOBS | Kraken_defense | 10 | I | ? |
| OreSpawnMOBS | LeafMonster_health | 6 | I | ? |
| OreSpawnMOBS | LeafMonster_attack | 2 | I | ? |
| OreSpawnMOBS | LeafMonster_defense | 1 | I | ? |
| OreSpawnMOBS | Nightmare_health | 250 | I | ? |
| OreSpawnMOBS | Nightmare_attack | 30 | I | ? |
| OreSpawnMOBS | Nightmare_defense | 10 | I | ? |
| OreSpawnMOBS | Scorpion_health | 15 | I | ? |
| OreSpawnMOBS | Scorpion_attack | 4 | I | ? |
| OreSpawnMOBS | Scorpion_defense | 10 | I | ? |
| OreSpawnMOBS | Skate_health | 8 | I | ? |
| OreSpawnMOBS | Skate_attack | 8 | I | ? |
| OreSpawnMOBS | Skate_defense | 4 | I | ? |
| OreSpawnMOBS | TerribleTerror_health | 10 | I | ? |
| OreSpawnMOBS | TerribleTerror_attack | 5 | I | ? |
| OreSpawnMOBS | TerribleTerror_defense | 3 | I | ? |
| OreSpawnMOBS | TheKing_health | 7000 | I | ? |
| OreSpawnMOBS | TheKing_attack | 350 | I | ? |
| OreSpawnMOBS | TheKing_defense | 21 | I | ? |
| OreSpawnMOBS | TheQueen_health | 6000 | I | ? |
| OreSpawnMOBS | TheQueen_attack | 225 | I | ? |
| OreSpawnMOBS | TheQueen_defense | 21 | I | ? |
| OreSpawnMOBS | Leonopteryx_health | 150 | I | ? |
| OreSpawnMOBS | Leonopteryx_attack | 20 | I | ? |
| OreSpawnMOBS | Leonopteryx_defense | 8 | I | ? |
| OreSpawnMOBS | Crab_health | 180 | I | ? |
| OreSpawnMOBS | Crab_attack | 24 | I | ? |
| OreSpawnMOBS | Crab_defense | 16 | I | ? |
| OreSpawnARMOR | Amethyst_durability | 100 | I | ? |
| OreSpawnARMOR | Amethyst_head_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | Amethyst_chest_damage_reduce | 8 | I | ? |
| OreSpawnARMOR | Amethyst_leggings_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | Amethyst_boots_damage_reduce | 3 | I | ? |
| OreSpawnARMOR | Amethyst_enchantability | 40 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Amethyst_enchant_featherfalling | 0 | I | ? |
| OreSpawnARMOR | Emerald_durability | 60 | I | ? |
| OreSpawnARMOR | Emerald_head_damage_reduce | 3 | I | ? |
| OreSpawnARMOR | Emerald_chest_damage_reduce | 8 | I | ? |
| OreSpawnARMOR | Emerald_leggings_damage_reduce | 6 | I | ? |
| OreSpawnARMOR | Emerald_boots_damage_reduce | 3 | I | ? |
| OreSpawnARMOR | Emerald_enchantability | 40 | I | ? |
| OreSpawnARMOR | Emerald_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Emerald_enchant_featherfalling | 0 | I | ? |
| OreSpawnARMOR | Experience_durability | 70 | I | ? |
| OreSpawnARMOR | Experience_head_damage_reduce | 5 | I | ? |
| OreSpawnARMOR | Experience_chest_damage_reduce | 9 | I | ? |
| OreSpawnARMOR | Experience_leggings_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | Experience_boots_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | Experience_enchantability | 50 | I | ? |
| OreSpawnARMOR | Experience_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Experience_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Experience_enchant_protection | 2 | I | ? |
| OreSpawnARMOR | Experience_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Experience_enchant_blastprotection | 1 | I | ? |
| OreSpawnARMOR | Experience_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Experience_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Experience_enchant_featherfalling | 1 | I | ? |
| OreSpawnARMOR | MothScale_durability | 50 | I | ? |
| OreSpawnARMOR | MothScale_head_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | MothScale_chest_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | MothScale_leggings_damage_reduce | 5 | I | ? |
| OreSpawnARMOR | MothScale_boots_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | MothScale_enchantability | 50 | I | ? |
| OreSpawnARMOR | MothScale_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | MothScale_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | MothScale_enchant_protection | 3 | I | ? |
| OreSpawnARMOR | MothScale_enchant_fireprotection | 3 | I | ? |
| OreSpawnARMOR | MothScale_enchant_blastprotection | 3 | I | ? |
| OreSpawnARMOR | MothScale_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | MothScale_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | MothScale_enchant_featherfalling | 5 | I | ? |
| OreSpawnARMOR | LavaEel_durability | 40 | I | ? |
| OreSpawnARMOR | LavaEel_head_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | LavaEel_chest_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | LavaEel_leggings_damage_reduce | 5 | I | ? |
| OreSpawnARMOR | LavaEel_boots_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | LavaEel_enchantability | 35 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_respiration | 1 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_aquaaffinity | 2 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_protection | 3 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_fireprotection | 2 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_blastprotection | 10 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | LavaEel_enchant_featherfalling | 2 | I | ? |
| OreSpawnARMOR | Ultimate_durability | 200 | I | ? |
| OreSpawnARMOR | Ultimate_head_damage_reduce | 6 | I | ? |
| OreSpawnARMOR | Ultimate_chest_damage_reduce | 12 | I | ? |
| OreSpawnARMOR | Ultimate_leggings_damage_reduce | 10 | I | ? |
| OreSpawnARMOR | Ultimate_boots_damage_reduce | 6 | I | ? |
| OreSpawnARMOR | Ultimate_enchantability | 100 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_respiration | 2 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_aquaaffinity | 3 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_protection | 5 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_fireprotection | 5 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_blastprotection | 5 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_projectileprotection | 5 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Ultimate_enchant_featherfalling | 3 | I | ? |
| OreSpawnARMOR | Pink_durability | 50 | I | ? |
| OreSpawnARMOR | Pink_head_damage_reduce | 3 | I | ? |
| OreSpawnARMOR | Pink_chest_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | Pink_leggings_damage_reduce | 5 | I | ? |
| OreSpawnARMOR | Pink_boots_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | Pink_enchantability | 40 | I | ? |
| OreSpawnARMOR | Pink_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Pink_enchant_featherfalling | 0 | I | ? |
| OreSpawnARMOR | TigersEye_durability | 80 | I | ? |
| OreSpawnARMOR | TigersEye_head_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | TigersEye_chest_damage_reduce | 8 | I | ? |
| OreSpawnARMOR | TigersEye_leggings_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | TigersEye_boots_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | TigersEye_enchantability | 55 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | TigersEye_enchant_featherfalling | 0 | I | ? |
| OreSpawnARMOR | Peacock_durability | 40 | I | ? |
| OreSpawnARMOR | Peacock_head_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | Peacock_chest_damage_reduce | 5 | I | ? |
| OreSpawnARMOR | Peacock_leggings_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | Peacock_boots_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | Peacock_enchantability | 30 | I | ? |
| OreSpawnARMOR | Peacock_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Peacock_enchant_featherfalling | 10 | I | ? |
| OreSpawnARMOR | Mobzilla_durability | 1000 | I | ? |
| OreSpawnARMOR | Mobzilla_head_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | Mobzilla_chest_damage_reduce | 13 | I | ? |
| OreSpawnARMOR | Mobzilla_leggings_damage_reduce | 11 | I | ? |
| OreSpawnARMOR | Mobzilla_boots_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | Mobzilla_enchantability | 150 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_protection | 10 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_fireprotection | 10 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_blastprotection | 10 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_projectileprotection | 10 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_unbreaking | 5 | I | ? |
| OreSpawnARMOR | Mobzilla_enchant_featherfalling | 10 | I | ? |
| OreSpawnARMOR | Ruby_durability | 90 | I | ? |
| OreSpawnARMOR | Ruby_head_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | Ruby_chest_damage_reduce | 9 | I | ? |
| OreSpawnARMOR | Ruby_leggings_damage_reduce | 8 | I | ? |
| OreSpawnARMOR | Ruby_boots_damage_reduce | 4 | I | ? |
| OreSpawnARMOR | Ruby_enchantability | 40 | I | ? |
| OreSpawnARMOR | Ruby_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Ruby_enchant_featherfalling | 0 | I | ? |
| OreSpawnARMOR | Royal_durability | 2000 | I | ? |
| OreSpawnARMOR | Royal_head_damage_reduce | 8 | I | ? |
| OreSpawnARMOR | Royal_chest_damage_reduce | 14 | I | ? |
| OreSpawnARMOR | Royal_leggings_damage_reduce | 12 | I | ? |
| OreSpawnARMOR | Royal_boots_damage_reduce | 8 | I | ? |
| OreSpawnARMOR | Royal_enchantability | 200 | I | ? |
| OreSpawnARMOR | Royal_enchant_respiration | 1 | I | ? |
| OreSpawnARMOR | Royal_enchant_aquaaffinity | 2 | I | ? |
| OreSpawnARMOR | Royal_enchant_protection | 10 | I | ? |
| OreSpawnARMOR | Royal_enchant_fireprotection | 10 | I | ? |
| OreSpawnARMOR | Royal_enchant_blastprotection | 10 | I | ? |
| OreSpawnARMOR | Royal_enchant_projectileprotection | 10 | I | ? |
| OreSpawnARMOR | Royal_enchant_unbreaking | 5 | I | ? |
| OreSpawnARMOR | Royal_enchant_featherfalling | 10 | I | ? |
| OreSpawnARMOR | Lapis_durability | 60 | I | ? |
| OreSpawnARMOR | Lapis_head_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | Lapis_chest_damage_reduce | 7 | I | ? |
| OreSpawnARMOR | Lapis_leggings_damage_reduce | 5 | I | ? |
| OreSpawnARMOR | Lapis_boots_damage_reduce | 2 | I | ? |
| OreSpawnARMOR | Lapis_enchantability | 60 | I | ? |
| OreSpawnARMOR | Lapis_enchant_respiration | 1 | I | ? |
| OreSpawnARMOR | Lapis_enchant_aquaaffinity | 1 | I | ? |
| OreSpawnARMOR | Lapis_enchant_protection | 1 | I | ? |
| OreSpawnARMOR | Lapis_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Lapis_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Lapis_enchant_projectileprotection | 1 | I | ? |
| OreSpawnARMOR | Lapis_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Lapis_enchant_featherfalling | 0 | I | ? |
| OreSpawnARMOR | Queen_durability | 1500 | I | ? |
| OreSpawnARMOR | Queen_head_damage_reduce | 9 | I | ? |
| OreSpawnARMOR | Queen_chest_damage_reduce | 16 | I | ? |
| OreSpawnARMOR | Queen_leggings_damage_reduce | 14 | I | ? |
| OreSpawnARMOR | Queen_boots_damage_reduce | 9 | I | ? |
| OreSpawnARMOR | Queen_enchantability | 150 | I | ? |
| OreSpawnARMOR | Queen_enchant_respiration | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_aquaaffinity | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_protection | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_fireprotection | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_blastprotection | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_projectileprotection | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_unbreaking | 0 | I | ? |
| OreSpawnARMOR | Queen_enchant_featherfalling | 0 | I | ? |
| OreSpawnWEAPONS | Ultimate_harvestlevel | 10 | I | ? |
| OreSpawnWEAPONS | Ultimate_maxuses | 3000 | I | ? |
| OreSpawnWEAPONS | Ultimate_efficiency | 15 | I | ? |
| OreSpawnWEAPONS | Ultimate_damage | 36 | I | ? |
| OreSpawnWEAPONS | Ultimate_enchantability | 100 | I | ? |
| OreSpawnWEAPONS | Nightmare_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | Nightmare_maxuses | 1800 | I | ? |
| OreSpawnWEAPONS | Nightmare_efficiency | 12 | I | ? |
| OreSpawnWEAPONS | Nightmare_damage | 26 | I | ? |
| OreSpawnWEAPONS | Nightmare_enchantability | 60 | I | ? |
| OreSpawnWEAPONS | Bertha_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | Bertha_maxuses | 9000 | I | ? |
| OreSpawnWEAPONS | Bertha_efficiency | 15 | I | ? |
| OreSpawnWEAPONS | Bertha_damage | 496 | I | ? |
| OreSpawnWEAPONS | Bertha_enchantability | 100 | I | ? |
| OreSpawnWEAPONS | CrystalWood_harvestlevel | 2 | I | ? |
| OreSpawnWEAPONS | CrystalWood_maxuses | 300 | I | ? |
| OreSpawnWEAPONS | CrystalWood_efficiency | 3 | I | ? |
| OreSpawnWEAPONS | CrystalWood_damage | 2 | I | ? |
| OreSpawnWEAPONS | CrystalWood_enchantability | 15 | I | ? |
| OreSpawnWEAPONS | CrystalStone_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | CrystalStone_maxuses | 800 | I | ? |
| OreSpawnWEAPONS | CrystalStone_efficiency | 6 | I | ? |
| OreSpawnWEAPONS | CrystalStone_damage | 5 | I | ? |
| OreSpawnWEAPONS | CrystalStone_enchantability | 45 | I | ? |
| OreSpawnWEAPONS | Pink_harvestlevel | 4 | I | ? |
| OreSpawnWEAPONS | Pink_maxuses | 1100 | I | ? |
| OreSpawnWEAPONS | Pink_efficiency | 10 | I | ? |
| OreSpawnWEAPONS | Pink_damage | 7 | I | ? |
| OreSpawnWEAPONS | Pink_enchantability | 65 | I | ? |
| OreSpawnWEAPONS | TigersEye_harvestlevel | 4 | I | ? |
| OreSpawnWEAPONS | TigersEye_maxuses | 1600 | I | ? |
| OreSpawnWEAPONS | TigersEye_efficiency | 12 | I | ? |
| OreSpawnWEAPONS | TigersEye_damage | 8 | I | ? |
| OreSpawnWEAPONS | TigersEye_enchantability | 75 | I | ? |
| OreSpawnWEAPONS | Ruby_harvestlevel | 5 | I | ? |
| OreSpawnWEAPONS | Ruby_maxuses | 1500 | I | ? |
| OreSpawnWEAPONS | Ruby_efficiency | 11 | I | ? |
| OreSpawnWEAPONS | Ruby_damage | 16 | I | ? |
| OreSpawnWEAPONS | Ruby_enchantability | 85 | I | ? |
| OreSpawnWEAPONS | Amethyst_harvestlevel | 4 | I | ? |
| OreSpawnWEAPONS | Amethyst_maxuses | 2000 | I | ? |
| OreSpawnWEAPONS | Amethyst_efficiency | 11 | I | ? |
| OreSpawnWEAPONS | Amethyst_damage | 11 | I | ? |
| OreSpawnWEAPONS | Amethyst_enchantability | 70 | I | ? |
| OreSpawnWEAPONS | Emerald_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | Emerald_maxuses | 1300 | I | ? |
| OreSpawnWEAPONS | Emerald_efficiency | 10 | I | ? |
| OreSpawnWEAPONS | Emerald_damage | 6 | I | ? |
| OreSpawnWEAPONS | Emerald_enchantability | 75 | I | ? |
| OreSpawnWEAPONS | Royal_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | Royal_maxuses | 10000 | I | ? |
| OreSpawnWEAPONS | Royal_efficiency | 15 | I | ? |
| OreSpawnWEAPONS | Royal_damage | 746 | I | ? |
| OreSpawnWEAPONS | Royal_enchantability | 150 | I | ? |
| OreSpawnWEAPONS | Attitude_harvestlevel | 5 | I | ? |
| OreSpawnWEAPONS | Attitude_maxuses | 2000 | I | ? |
| OreSpawnWEAPONS | Attitude_efficiency | 15 | I | ? |
| OreSpawnWEAPONS | Attitude_damage | 82 | I | ? |
| OreSpawnWEAPONS | Attitude_enchantability | 100 | I | ? |
| OreSpawnWEAPONS | BattleAxe_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | BattleAxe_maxuses | 1500 | I | ? |
| OreSpawnWEAPONS | BattleAxe_efficiency | 15 | I | ? |
| OreSpawnWEAPONS | BattleAxe_damage | 46 | I | ? |
| OreSpawnWEAPONS | BattleAxe_enchantability | 75 | I | ? |
| OreSpawnWEAPONS | Chainsaw_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | Chainsaw_maxuses | 1500 | I | ? |
| OreSpawnWEAPONS | Chainsaw_efficiency | 10 | I | ? |
| OreSpawnWEAPONS | Chainsaw_damage | 56 | I | ? |
| OreSpawnWEAPONS | Chainsaw_enchantability | 75 | I | ? |
| OreSpawnWEAPONS | QueenBattleAxe_harvestlevel | 3 | I | ? |
| OreSpawnWEAPONS | QueenBattleAxe_maxuses | 2200 | I | ? |
| OreSpawnWEAPONS | QueenBattleAxe_efficiency | 15 | I | ? |
| OreSpawnWEAPONS | QueenBattleAxe_damage | 662 | I | ? |
| OreSpawnWEAPONS | QueenBattleAxe_enchantability | 100 | I | ? |
| OreSpawnORES | Ruby_rate | 10 | I | ? |
| OreSpawnORES | Ruby_clumpsize | 1 | I | ? |
| OreSpawnORES | Ruby_mindepth | 0 | I | ? |
| OreSpawnORES | Ruby_maxdepth | 50 | I | ? |
| OreSpawnORES | BlockRuby_rate | 1 | I | ? |
| OreSpawnORES | BlockRuby_clumpsize | 2 | I | ? |
| OreSpawnORES | BlockRuby_mindepth | 0 | I | ? |
| OreSpawnORES | BlockRuby_maxdepth | 15 | I | ? |
| OreSpawnORES | Uranium_rate | 3 | I | ? |
| OreSpawnORES | Uranium_clumpsize | 4 | I | ? |
| OreSpawnORES | Uranium_mindepth | 0 | I | ? |
| OreSpawnORES | Uranium_maxdepth | 30 | I | ? |
| OreSpawnORES | Titanium_rate | 3 | I | ? |
| OreSpawnORES | Titanium_clumpsize | 4 | I | ? |
| OreSpawnORES | Titanium_mindepth | 0 | I | ? |
| OreSpawnORES | Titanium_maxdepth | 20 | I | ? |
| OreSpawnORES | Amethyst_rate | 2 | I | ? |
| OreSpawnORES | Amethyst_clumpsize | 6 | I | ? |
| OreSpawnORES | Amethyst_mindepth | 0 | I | ? |
| OreSpawnORES | Amethyst_maxdepth | 25 | I | ? |
| OreSpawnORES | Salt_rate | 5 | I | ? |
| OreSpawnORES | Salt_clumpsize | 12 | I | ? |
| OreSpawnORES | Salt_mindepth | 50 | I | ? |
| OreSpawnORES | Salt_maxdepth | 128 | I | ? |
| OreSpawnORES | SpawnOres_rate | 28 | I | ? |
| OreSpawnORES | SpawnOres_clumpsize | 4 | I | ? |
| OreSpawnORES | SpawnOres_mindepth | 50 | I | ? |
| OreSpawnORES | SpawnOres_maxdepth | 128 | I | ? |
| OreSpawnORES | Diamond_rate | 4 | I | ? |
| OreSpawnORES | Diamond_clumpsize | 6 | I | ? |
| OreSpawnORES | Diamond_mindepth | 0 | I | ? |
| OreSpawnORES | Diamond_maxdepth | 30 | I | ? |
| OreSpawnORES | BlockDiamond_rate | 2 | I | ? |
| OreSpawnORES | BlockDiamond_clumpsize | 4 | I | ? |
| OreSpawnORES | BlockDiamond_mindepth | 0 | I | ? |
| OreSpawnORES | BlockDiamond_maxdepth | 20 | I | ? |
| OreSpawnORES | Emerald_rate | 4 | I | ? |
| OreSpawnORES | Emerald_clumpsize | 6 | I | ? |
| OreSpawnORES | Emerald_mindepth | 0 | I | ? |
| OreSpawnORES | Emerald_maxdepth | 40 | I | ? |
| OreSpawnORES | BlockEmerald_rate | 2 | I | ? |
| OreSpawnORES | BlockEmerald_clumpsize | 4 | I | ? |
| OreSpawnORES | BlockEmerald_mindepth | 0 | I | ? |
| OreSpawnORES | BlockEmerald_maxdepth | 20 | I | ? |
| OreSpawnORES | Gold_rate | 4 | I | ? |
| OreSpawnORES | Gold_clumpsize | 8 | I | ? |
| OreSpawnORES | Gold_mindepth | 0 | I | ? |
| OreSpawnORES | Gold_maxdepth | 40 | I | ? |
| OreSpawnORES | BlockGold_rate | 2 | I | ? |
| OreSpawnORES | BlockGold_clumpsize | 4 | I | ? |
| OreSpawnORES | BlockGold_mindepth | 0 | I | ? |
| OreSpawnORES | BlockGold_maxdepth | 25 | I | ? |

## Offene Punkte des Generators

- block id used 2 times: crystalfurnace
- item id equals a block id, renamed to pizza_item: MyPizzaItem
- item id equals a block id, renamed to ducttape_item: MyDuctTapeItem
- model not used by any renderer: ModelElevator
- sound event renamed: Beebuzz -> beebuzz
- sound event renamed: MothraWings -> mothrawings
