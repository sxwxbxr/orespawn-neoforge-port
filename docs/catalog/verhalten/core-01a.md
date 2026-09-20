# Verhalten: core-01a

`OreSpawnMain` (6575 Zeilen, `OreSpawnMain.java:1-6575`) ist die `@Mod`-Klasse von OreSpawn 1.7.10 Build 20.3 (`@Mod(modid = "OreSpawn", name = "OreSpawn", version = "1.7.10.20.3")`, `OreSpawnMain.java:24`). Sie liest die komplette Konfiguration (628 Schlüssel laut manifest, sechs Kategorien), baut und registriert in `preInit` **alles**: 211 Blöcke, 473 Items, 134 Entities (131 davon als Mod-Entity), 348 natürliche Spawn-Einträge für 55 Entity-Klassen, sechs Dimensionen, den Welt-Generator, TileEntity, GUI-Handler und 134 Dispenser-Verhalten. `load` ist leer, `postInit` erzeugt nur sechs Welt-/Hilfsobjekte. Die Klasse selbst hat **keinen** `@SubscribeEvent`-Handler, keinen Tick-Handler, keine Spieler-, Login- oder Chat-Logik; die einzigen Event- und Tick-Listener des Mods (`RiderControl` am `ClientTickEvent`, `KeyHandler`, `GirlfriendOverlayGui`) sind clientseitig und werden über `ClientProxyOreSpawn` angemeldet (§8.1). Im ganzen Quellbaum gibt es keine Login-, Spieler-, Welt- oder Server-Tick-Events und keine Befehle. Zusätzlich hält sie globale Laufzeit-Statics (Feiertagsflags, Reittier-Tastenzustand, ein geteiltes `Random`) und fünf Hilfsfunktionen zum schnellen Blocksetzen, die Welt-Generator und Strukturen benutzen. Rezepte (365 `addRecipe`/`addShapelessRecipe`-Zeilen), Schmelzrezepte (16, `OreSpawnMain.java:2751-2776`) und Kistenbeute (`OreSpawnMain.java:5051-5062`) sind **nicht** Teil dieses Katalogs.

---

## 1. Lebenszyklus und Aufrufreihenfolge

| Schritt | Was passiert | Quelle |
|---|---|---|
| Klassen-Initialisierer | setzt alle Statics auf Defaults, `OreSpawnRand = new Random(151L)` | `OreSpawnMain.java:6203-6574`, `:6366` |
| Proxy | `@SidedProxy(clientSide = "danger.orespawn.ClientProxyOreSpawn", serverSide = "danger.orespawn.CommonProxyOreSpawn")` | `OreSpawnMain.java:27` |
| Konstruktor | `OreSpawnGen = new OreSpawnWorld()` | `OreSpawnMain.java:1122-1124` |
| `preInit` 1 | `Configuration` aus `event.getSuggestedConfigurationFile()`, `config.load()` | `OreSpawnMain.java:1128-1134` |
| `preInit` 2 | IDS lesen → `getMobs` (Enable-Flags + 59 Mob-Stats) → TWEAKS → 14 Rüstungs-Stats → 15 Waffen-Stats → 2 Waffen-Direktwerte → Klemmungen → 13 Erz-Stats | `OreSpawnMain.java:1135-1256` |
| `preInit` 3 | `config.save()` — **vor** `disableAllMobs`, die Datei behält also die Nutzerwerte | `OreSpawnMain.java:1257` |
| `preInit` 4 | `AllMobsDisable != 0` → `disableAllMobs()` | `OreSpawnMain.java:1258-1260` |
| `preInit` 5 | Biom-IDs `BaseBiomeID+0..4`, Dimensions-IDs `BaseDimensionID+0..5` | `OreSpawnMain.java:1261-1271` |
| `preInit` 6 | `proxy.registerSoundThings()` | `OreSpawnMain.java:1272` |
| `preInit` 7 | `laySomeEggs()`: 119 Spawn-Erz-Blöcke (`OreGenericEgg`) + 6 Ameisenblöcke konstruieren | `OreSpawnMain.java:1273`, `:5907-6033` |
| `preInit` 8 | Blöcke, Items, Tool-Materialien (`:1292-1306`), Armor-Materialien (`:1432-1445`), Rüstungen (`:1446-1501`) konstruieren; `initializeCagesAndEggs()` bei `:1546` | `OreSpawnMain.java:1274-1635` |
| `preInit` 9 | `make_some_more_things()` | `OreSpawnMain.java:1636`, `:1639-5066` |
| ↳ | `GameRegistry.registerBlock` ×211 / `registerItem` ×473 | `OreSpawnMain.java:1640-2323` |
| ↳ | Rezepte + Sprachnamen (außerhalb des Fokus) | `OreSpawnMain.java:2324-3056` |
| ↳ | Entity-Registrierung (zuerst 11 Projektil-/Effekt-Entities `UltimateFishHook` … `EntityThrownRock` bei `:3057-3120`, dann Mobs) | `OreSpawnMain.java:3057-4177` |
| ↳ | Datumsprüfung + 348× `EntityRegistry.addSpawn` | `OreSpawnMain.java:4178-4642` |
| ↳ | Entities `Shoes` (`:4643-4648`), `EntityCage` (`:4776-4781`), `UltimateArrow`/`IrukandjiArrow` nur global (`:5013-5020`) | |
| ↳ | `GameRegistry.registerWorldGenerator(OreSpawnGen, 10)` | `OreSpawnMain.java:5035` |
| ↳ | `proxy.registerRenderThings()`, `registerKeyboardInput()`, `registerNetworkStuff()` | `OreSpawnMain.java:5036-5038` |
| ↳ | 6 Dimensionen registrieren | `OreSpawnMain.java:5039-5050` |
| ↳ | Kistenbeute (außerhalb des Fokus) | `OreSpawnMain.java:5051-5062` |
| ↳ | `registerTileEntity(TileEntityCrystalFurnace, "TileEntityCrystalFurnace")` | `OreSpawnMain.java:5063` |
| ↳ | `NetworkRegistry.INSTANCE.registerGuiHandler(this, new OreSpawnGUIHandler())` | `OreSpawnMain.java:5064` |
| ↳ | `DoDispenserRegistrations()` | `OreSpawnMain.java:5065`, `:5299-5435` |
| `load` | leer | `OreSpawnMain.java:5437-5439` |
| `postInit` (static) | `BMaze = new BasiliskMaze()`, `RubyDungeon = new RubyBirdDungeon()`, `MyDungeon = new GenericDungeon()`, `OreSpawnTrees = new Trees()`, `OreSpawnUtils = new MyUtils()`, `Chunker = new ChunkOreGenerator()` | `OreSpawnMain.java:5441-5448` |

Weitere Methoden ohne Lebenszyklusrolle: `spawnEntity(...)` ist `@SideOnly(Side.CLIENT)` und gibt immer `null` zurück (`OreSpawnMain.java:5450-5453`), `getVersion()` liefert `"1.7.10.20.3"` (`OreSpawnMain.java:6199-6201`).

**Folge für den Port:** Das Original verbraucht Config-Werte schon beim Registrieren (Tool-/Armor-Materialien, Spawn-Guards, Mob-Stats). Unter NeoForge 1.21.1 laufen `DeferredRegister`-Einträge vor dem Laden normaler COMMON/SERVER-Configs. `ModConfig.Type.STARTUP` existiert: `loader-4.0.43.jar` (FML des Packs, `neo_version=21.1.248` laut `mods/orespawn/gradle.properties:27`) enthält in `net/neoforged/fml/config/ModConfig$Type.class` die Konstanten `CLIENT`, `COMMON`, `SERVER`, `STARTUP`, und `ConfigTracker.registerConfig` referenziert `STARTUP`. offen: dass STARTUP-Werte bereits gelesen sind, wenn die `DeferredRegister`-Supplier laufen, ist nur aus Bytecode-Konstanten abgeleitet, nicht aus dem Methodenrumpf; trifft es nicht zu, müssen Materialwerte zur Laufzeit (Attribute, Tier-Abfrage) statt beim Registrieren gelesen werden.

---

## 2. Konfiguration

Datei: `event.getSuggestedConfigurationFile()` (`OreSpawnMain.java:1128`). Der Dateiname steht nicht im OreSpawn-Code; die Recherche nennt `OreSpawn.cfg` unter `config/` (`docs/research/02-dimensions-worldgen.md:790`, `docs/research/01-mobs.md:13`, geringere Vertrauensstufe), passend zur modid `OreSpawn` (`OreSpawnMain.java:24`). Kategorien als Stringkonstanten: `OreSpawnIDS`, `OreSpawnMOBS`, `OreSpawnTWEAKS`, `OreSpawnWEAPONS`, `OreSpawnORES` (`OreSpawnMain.java:1129-1133`), `OreSpawnARMOR` in `get_armorstats` (`OreSpawnMain.java:5635`). Alle Werte sind `int` (`getInt()`). Gesamtzahl 628 (manifest: 4 IDS, 20 TWEAKS, 2+75 WEAPONS, 102+177 MOBS, 196 ARMOR, 52 ORES). Der Abgleich Quelltext gegen Bytecode zeigt keine Abweichung (`reference/jar/diff_20.2_vs_20.3.txt:141-146`: 128 direkte Schlüssel, 59/15/13/14 Stats-Aufrufe, keine Default-Abweichung).

### 2.1 Direkte Schlüssel: IDS, TWEAKS, WEAPONS

| Kategorie | Schlüssel | Default | Feld | Klemmung / Kopplung | Zeile | gelesen von |
|---|---|---|---|---|---|---|
| `OreSpawnIDS` | `BaseBlockID` | 2700 | `BaseBlockID` | — | `:1135` | nur `OreSpawnMain` (Block-Konstruktoren) |
| `OreSpawnIDS` | `BaseItemID` | 9000 | `BaseItemID` | — | `:1136` | `OreSpawnMain` (Item-Konstruktoren); `ItemSpiderRobotKit` (`ItemSpiderRobotKit.java:16`, siehe §3.1) |
| `OreSpawnIDS` | `BaseBiomeID` | 120 | `BaseBiomeID` | — | `:1137` | nur `OreSpawnMain` (`:1261-1265`) |
| `OreSpawnIDS` | `BaseDimensionID` | 80 | `BaseDimensionID` | — | `:1138` | nur `OreSpawnMain` (`:1266-1271`) |
| `OreSpawnTWEAKS` | `AllMobsDisable` | 0 | `AllMobsDisable` | ≠ 0 → `disableAllMobs()` (`:1258-1260`) | `:1140` | nur `OreSpawnMain` |
| `OreSpawnTWEAKS` | `LessOre` | 0 | `LessOre` | wird 1 bei LessLag = 2 (`:1242`) | `:1141` | `ChunkOreGenerator`, `ChunkProviderOreSpawn2`, `OreSpawnWorld` |
| `OreSpawnTWEAKS` | `LessLag` | 0 | `LessLag` | 0..2 (`:1221-1226`); siehe Kopplung | `:1142` | `ChunkProviderOreSpawn4`, `ChunkProviderOreSpawn5`, `ChunkProviderOreSpawn6`, `OreSpawnWorld`, `Trees` |
| `OreSpawnTWEAKS` | `RatPlayerFriendly` | 1 | `RatPlayerFriendly` | — | `:1143` | `Rat` |
| `OreSpawnTWEAKS` | `RatPetFriendly` | 1 | `RatPetFriendly` | — | `:1144` | `Rat` |
| `OreSpawnTWEAKS` | `NightmareSize` | 0 | `NightmareSize` | 0..5 (`:1215-1220`) | `:1145` | `PitchBlack` |
| `OreSpawnTWEAKS` | `IslandSpeedFactor` | 2 | `IslandSpeedFactor` | 1..5 (`:1203-1208`); LessLag-Kopplung | `:1146` | `Island`, `IslandToo` |
| `OreSpawnTWEAKS` | `IslandSizeFactor` | 2 | `IslandSizeFactor` | 1..5 (`:1209-1214`); LessLag-Kopplung | `:1147` | `IslandBlock`, `IslandToo` |
| `OreSpawnTWEAKS` | `GinormousEmeraldTreeEnable` | 1 | `GinormousEmeraldTreeEnable` | — | `:1148` | `ItemMagicApple` |
| `OreSpawnTWEAKS` | `GuiOverlayEnable` | 1 | `GuiOverlayEnable` | — | `:1149` | `GirlfriendOverlayGui` |
| `OreSpawnTWEAKS` | `UltimateSwordPvp` | 0 | `ultimate_sword_pvp` | — | `:1150` | `IrukandjiArrow`, `UltimateArrow`, `UltimateAxe`, `UltimatePickaxe`, `UltimateShovel`, `UltimateSword` |
| `OreSpawnTWEAKS` | `BigBerthaPvp` | 0 | `big_bertha_pvp` | — | `:1151` | `Bertha`, `BerthaHit` |
| `OreSpawnTWEAKS` | `BoyfriendBroMode` | 0 | `bro_mode` | — | `:1152` | `Boyfriend` |
| `OreSpawnTWEAKS` | `DuplicatorTreeEnable` | 1 | `enableduplicatortree` | — | `:1153` | `BlockDuplicatorLog`, `OreSpawnWorld` |
| `OreSpawnTWEAKS` | `RoyalGlideEnable` | 1 | `RoyalGlideEnable` | — | `:1154` | `ItemOreSpawnArmor` |
| `OreSpawnTWEAKS` | `DragonflyHorseFriendly` | 0 | `DragonflyHorseFriendly` | — | `:1155` | `Dragonfly` |
| `OreSpawnTWEAKS` | `PlayNicely` | 0 | `PlayNicely` | — | `:1156` | `Alien`, `Alosaurus`, `AntRobot`, `AttackSquid`, `BandP`, `Baryonyx`, `Basilisk`, `Beaver` … (+77) |
| `OreSpawnTWEAKS` | `MinersDreamExpensive` | 0 | `MinersDreamExpensive` | — | `:1157` | nur `OreSpawnMain`: Rezeptauswahl (`:3018-3022`) |
| `OreSpawnTWEAKS` | `DisableOverworldDungeons` | 0 | `DisableOverworldDungeons` | — | `:1158` | `OreSpawnWorld` |
| `OreSpawnTWEAKS` | `FullPowerKingEnable` | 0 | `FullPowerKingEnable` | — | `:1159` | `ThePrinceAdult` |
| `OreSpawnWEAPONS` | `UltimateSwordEnchantmentLevel` | 5 | `UltimateSwordMagic` | 1..10 (`:1191-1196`) | `:1189` | `UltimateSword` |
| `OreSpawnWEAPONS` | `UltimateBowDamage` | 10 | `UltimateBowDamage` | 2..20 (`:1197-1202`) | `:1190` | `UltimateArrow` |

Kopplung `LessLag` (`OreSpawnMain.java:1227-1243`): `LessLag == 1` begrenzt `IslandSizeFactor` und `IslandSpeedFactor` auf höchstens 2; `LessLag == 2` begrenzt beide auf höchstens 1 **und** setzt `LessOre = 1`. Die Klemmungen laufen **nach** dem Lesen, aber der Wert wird nicht in die Datei zurückgeschrieben (`config.save()` bei `:1257` speichert die Property-Objekte, nicht die geklemmten Felder).

### 2.2 Mob-Schalter (`getMobs`, Kategorie `OreSpawnMOBS`)

| Schlüssel | Default | Feld | Spawn-Guard in `OreSpawnMain` | gelesen von (andere Klassen) | Zeile |
|---|---|---|---|---|---|
| `MosquitoEnable` | 1 | `MosquitoEnable` | ja `:4437` | `BlockMosquitoPlant`, `OreSpawnWorld` | `:6036` |
| `RockEnable` | 1 | `RockEnable` | nein | `OreSpawnWorld` | `:6037` |
| `GhostEnable` | 1 | `GhostEnable` | ja `:4443` | — | `:6038` |
| `GhostSkellyEnable` | 1 | `GhostSkellyEnable` | ja `:4450` | — | `:6039` |
| `SpiderDriverEnable` | 1 | `SpiderDriverEnable` | nein | `BiomeGenUtopianPlains`, `OreSpawnWorld` | `:6040` |
| `JefferyEnable` | 1 | `JefferyEnable` | nein | `BiomeGenUtopianPlains` | `:6041` |
| `MothraEnable` | 1 | `MothraEnable` | ja `:4494` | `BiomeGenUtopianPlains` | `:6042` |
| `BrutalflyEnable` | 1 | `BrutalflyEnable` | ja `:4498` | `BiomeGenUtopianPlains` | `:6043` |
| `NastysaurusEnable` | 1 | `NastysaurusEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6044` |
| `PointysaurusEnable` | 1 | `PointysaurusEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6045` |
| `CricketEnable` | 1 | `CricketEnable` | ja `:4609` | `BiomeGenUtopianPlains` | `:6046` |
| `FrogEnable` | 1 | `FrogEnable` | ja `:4622` | `BiomeGenUtopianPlains` | `:6047` |
| `MothraPeaceful` | 0 | `MothraPeaceful` | nein | `Mothra` | `:6048` |
| `BlackAntEnable` | 1 | `BlackAntEnable` | nein | `AntBlock`, `CrystalAntBlock`, `OreSpawnWorld` | `:6049` |
| `RedAntEnable` | 1 | `RedAntEnable` | nein | `AntBlock`, `CrystalAntBlock`, `OreSpawnWorld` | `:6050` |
| `TermiteEnable` | 1 | `TermiteEnable` | nein | `AntBlock`, `CrystalAntBlock`, `OreSpawnWorld` | `:6051` |
| `UnstableAntEnable` | 1 | `UnstableAntEnable` | nein | `AntBlock`, `CrystalAntBlock`, `OreSpawnWorld` | `:6052` |
| `RainbowedAntEnable` | 1 | `RainbowAntEnable` | nein | `AntBlock`, `CrystalAntBlock`, `OreSpawnWorld` | `:6053` |
| `AlosaurusEnable` | 1 | `AlosaurusEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6054` |
| `HammerheadEnable` | 1 | `HammerheadEnable` | nein | `BiomeGenUtopianPlains` | `:6055` |
| `LeonEnable` | 1 | `LeonEnable` | nein | `BiomeGenUtopianPlains` | `:6056` |
| `CaterKillerEnable` | 1 | `CaterKillerEnable` | ja `:4405` | `BiomeGenUtopianPlains` | `:6057` |
| `MolenoidEnable` | 1 | `MolenoidEnable` | ja `:4400` | `BiomeGenUtopianPlains` | `:6058` |
| `TRexEnable` | 1 | `TRexEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6059` |
| `CriminalEnable` | 1 | `CriminalEnable` | ja `:4285` | `BiomeGenUtopianPlains` | `:6060` |
| `CryolophosaurusEnable` | 1 | `CryolophosaurusEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6061` |
| `RatEnable` | 1 | `RatEnable` | ja `:4636` | `BiomeGenUtopianPlains` | `:6062` |
| `UrchinEnable` | 1 | `UrchinEnable` | nein | `BiomeGenUtopianPlains`, `OreSpawnWorld` | `:6063` |
| `CamarasaurusEnable` | 1 | `CamarasaurusEnable` | nein | `ChunkProviderOreSpawn2` | `:6064` |
| `VelocityRaptorEnable` | 1 | `VelocityRaptorEnable` | nein | `ChunkProviderOreSpawn2` | `:6065` |
| `HydroliscEnable` | 1 | `HydroliscEnable` | ja `:4488` | — | `:6066` |
| `SpyroEnable` | 1 | `SpyroEnable` | nein | `ChunkProviderOreSpawn2` | `:6067` |
| `BaryonyxEnable` | 1 | `BaryonyxEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6068` |
| `BirdEnable` | 1 | `CockateilEnable` | ja `:4470` | `BiomeGenUtopianPlains` | `:6069` |
| `CassowaryEnable` | 1 | `CassowaryEnable` | ja `:4330` | `BiomeGenUtopianPlains` | `:6070` |
| `EasterBunnyEnable` | 1 | `EasterBunnyEnable` | ja `:4341` | — | `:6071` |
| `PeacockEnable` | 1 | `PeacockEnable` | ja `:4629` | `BiomeGenUtopianPlains` | `:6072` |
| `KyuubiEnable` | 1 | `KyuubiEnable` | ja `:4461` | — | `:6073` |
| `CephadromeEnable` | 1 | `CephadromeEnable` | ja `:4433` | — | `:6074` |
| `DragonEnable` | 1 | `DragonEnable` | nein | `BiomeGenUtopianPlains` | `:6075` |
| `GammaMetroidEnable` | 1 | `GammaMetroidEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6076` |
| `BasiliskEnable` | 1 | `BasiliskEnable` | ja `:4536` | `BiomeGenUtopianPlains` | `:6077` |
| `DragonflyEnable` | 1 | `DragonflyEnable` | ja `:4457` | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6078` |
| `EmperorScorpionEnable` | 1 | `EmperorScorpionEnable` | ja `:4542` | `BiomeGenUtopianPlains` | `:6079` |
| `TrooperBugEnable` | 1 | `TrooperBugEnable` | ja `:4546` | `BiomeGenUtopianPlains` | `:6080` |
| `SpitBugEnable` | 1 | `SpitBugEnable` | ja `:4550` | `BiomeGenUtopianPlains` | `:6081` |
| `StinkBugEnable` | 1 | `StinkBugEnable` | ja `:4553` | `BiomeGenUtopianPlains` | `:6082` |
| `ScorpionEnable` | 1 | `ScorpionEnable` | ja `:4560` | `BiomeGenUtopianPlains` | `:6083` |
| `CaveFisherEnable` | 1 | `CaveFisherEnable` | nein | `BiomeGenUtopianPlains`, `ChunkProviderOreSpawn2` | `:6084` |
| `AlienEnable` | 1 | `AlienEnable` | nein | `ChunkProviderOreSpawn2` | `:6085` |
| `WaterDragonEnable` | 1 | `WaterDragonEnable` | ja `:4503` | — | `:6086` |
| `SeaMonsterEnable` | 1 | `SeaMonsterEnable` | ja `:4509` | — | `:6087` |
| `SeaViperEnable` | 1 | `SeaViperEnable` | ja `:4513` | — | `:6088` |
| `AttackSquidEnable` | 1 | `AttackSquidEnable` | ja `:4522` | — | `:6089` |
| `Robot1Enable` | 1 | `Robot1Enable` | nein | `BiomeGenUtopianPlains` | `:6090` |
| `Robot2Enable` | 1 | `Robot2Enable` | nein | `BiomeGenUtopianPlains` | `:6091` |
| `Robot3Enable` | 1 | `Robot3Enable` | nein | `BiomeGenUtopianPlains` | `:6092` |
| `Robot4Enable` | 1 | `Robot4Enable` | nein | `BiomeGenUtopianPlains` | `:6093` |
| `Robot5Enable` | 1 | `Robot5Enable` | nein | `BiomeGenUtopianPlains` | `:6094` |
| `RotatorEnable` | 1 | `RotatorEnable` | nein | `BiomeGenUtopianPlains`, `OreSpawnWorld` | `:6095` |
| `VortexEnable` | 1 | `VortexEnable` | nein | `BiomeGenUtopianPlains` | `:6096` |
| `DungeonBeastEnable` | 1 | `DungeonBeastEnable` | ja `:4640` | `BiomeGenUtopianPlains` | `:6097` |
| `KrakenEnable` | 1 | `KrakenEnable` | nein | `AttackSquid` | `:6098` |
| `LizardEnable` | 1 | `LizardEnable` | ja `:4527` | — | `:6099` |
| `RubberDuckyEnable` | 1 | `RubberDuckyEnable` | ja `:4532` | — | `:6100` |
| `GirlfriendEnable` | 1 | `GirlfriendEnable` | ja `:4233` | `BiomeGenUtopianPlains` | `:6101` |
| `BoyfriendEnable` | 0 | `BoyfriendEnable` | ja `:4247` | `BiomeGenUtopianPlains` | `:6102` |
| `FireflyEnable` | 1 | `FireflyEnable` | ja `:4350` | `BiomeGenUtopianPlains`, `BlockFireflyPlant` | `:6103` |
| `FairyEnable` | 1 | `FairyEnable` | ja `:4633` | `BiomeGenUtopianPlains` | `:6104` |
| `BeeEnable` | 1 | `BeeEnable` | ja `:4368` | `BiomeGenUtopianPlains` | `:6105` |
| `TheKingEnable` | 1 | `TheKingEnable` | nein | `KingSpawnerBlock` | `:6106` |
| `TheQueenEnable` | 1 | `TheQueenEnable` | nein | `QueenSpawnerBlock` | `:6107` |
| `MantisEnable` | 1 | `MantisEnable` | ja `:4380` | `BiomeGenUtopianPlains` | `:6108` |
| `StinkyEnable` | 1 | `StinkyEnable` | ja `:4464` | `BiomeGenUtopianPlains` | `:6109` |
| `HerculesBeetleEnable` | 1 | `HerculesBeetleEnable` | ja `:4391` | `BiomeGenUtopianPlains` | `:6110` |
| `ChipmunkEnable` | 1 | `ChipmunkEnable` | ja `:4416` | `BiomeGenUtopianPlains` | `:6111` |
| `OstrichEnable` | 1 | `OstrichEnable` | ja `:4427` | `BiomeGenUtopianPlains` | `:6112` |
| `GazelleEnable` | 1 | `GazelleEnable` | nein | `BiomeGenUtopianPlains` | `:6113` |
| `CowEnable` | 1 | `CowEnable` | ja `:4269` | `BiomeGenUtopianPlains` | `:6114` |
| `ButterflyEnable` | 1 | `ButterflyEnable` | ja `:4295` | `BiomeGenUtopianPlains`, `BlockButterflyPlant` | `:6115` |
| `MothEnable` | 1 | `MothEnable` | ja `:4313` | `BiomeGenUtopianPlains`, `BlockMothPlant` | `:6116` |
| `TshirtEnable` | 1 | `TshirtEnable` | nein | `BiomeGenUtopianPlains` | `:6117` |
| `CoinEnable` | 1 | `CoinEnable` | ja `:4601` | `BiomeGenUtopianPlains` | `:6118` |
| `CreepingHorrorEnable` | 1 | `CreepingHorrorEnable` | nein | `BiomeGenUtopianPlains` | `:6119` |
| `TerribleTerrorEnable` | 1 | `TerribleTerrorEnable` | nein | `BiomeGenUtopianPlains` | `:6120` |
| `CliffRacerEnable` | 1 | `CliffRacerEnable` | nein | `BiomeGenUtopianPlains` | `:6121` |
| `TriffidEnable` | 1 | `TriffidEnable` | nein | — | `:6122` |
| `WormEnable` | 1 | `WormEnable` | ja `:4290` | — | `:6123` |
| `CloudSharkEnable` | 1 | `CloudSharkEnable` | nein | `BiomeGenUtopianPlains` | `:6124` |
| `GoldFishEnable` | 1 | `GoldFishEnable` | nein | `BiomeGenUtopianPlains` | `:6125` |
| `LeafMonsterEnable` | 1 | `LeafMonsterEnable` | ja `:4569` | `BiomeGenUtopianPlains` | `:6126` |
| `EnderKnightEnable` | 1 | `EnderKnightEnable` | ja `:4579` | `BiomeGenUtopianPlains` | `:6127` |
| `EnderReaperEnable` | 1 | `EnderReaperEnable` | ja `:4590` | `BiomeGenUtopianPlains` | `:6128` |
| `BeaverEnable` | 1 | `BeaverEnable` | ja `:4261` | `BiomeGenUtopianPlains` | `:6129` |
| `IrukandjiEnable` | 1 | `IrukandjiEnable` | nein | `BiomeGenUtopianPlains`, `OreSpawnWorld` | `:6130` |
| `SkateEnable` | 1 | `SkateEnable` | nein | `BiomeGenUtopianPlains` | `:6131` |
| `WhaleEnable` | 1 | `WhaleEnable` | ja `:4365` | `BiomeGenUtopianPlains` | `:6132` |
| `FlounderEnable` | 1 | `FlounderEnable` | nein | `BiomeGenUtopianPlains` | `:6133` |
| `NightmareEnable` | 1 | `PitchBlackEnable` | nein | `BiomeGenUtopianPlains` | `:6134` |
| `LurkingTerrorEnable` | 1 | `LurkingTerrorEnable` | nein | `BiomeGenUtopianPlains` | `:6135` |
| `GodzillaEnable` | 1 | `GodzillaEnable` | nein | `BiomeGenUtopianPlains` | `:6136` |
| `CrabEnable` | 1 | `CrabEnable` | ja `:4517` | `BiomeGenUtopianPlains` | `:6137` |

Auffälligkeiten in `getMobs`/`disableAllMobs`/Klassen-Initialisierer:

- Schlüssel ≠ Feldname: `RainbowedAntEnable` → `RainbowAntEnable` (`:6053`), `BirdEnable` → `CockateilEnable` (`:6069`), `NightmareEnable` → `PitchBlackEnable` (`:6134`).
- `BoyfriendEnable` ist als einziger Mob-Schalter per Default **0** (`OreSpawnMain.java:6102`, `:6324`).
- `MothraPeaceful` Default 0 (`:6048`), wird von `disableAllMobs` ebenfalls auf 0 gesetzt (`:5815`).
- `disableAllMobs()` (`OreSpawnMain.java:5804-5905`) setzt **nicht** zurück: `RockEnable`, `CricketEnable`, `FrogEnable`. `CrabEnable` wird doppelt gesetzt (`:5809`, `:5904`). Grille, Frosch und Steine spawnen also trotz `AllMobsDisable = 1`.
- `ThePrinceEnable`, `ThePrincessEnable` sind deklariert (`:157-158`), static auf 1 (`:6330-6331`), werden aber nie aus der Config gelesen und nirgends im Mod gelesen.
- Klassen-Initialisierer weicht vom Config-Default ab: `RatPlayerFriendly` static 0 / Config 1 (`:6277` vs `:1143`), `RatPetFriendly` static 0 / Config 1 (`:6278` vs `:1144`), `UltimateSwordMagic` static 10 / Config 5 (`:6344` vs `:1189`). Nach `preInit` gilt immer der Config-Wert.
- `FastGraphicsLeaves` ist kein Config-Wert; es wird clientseitig in `GirlfriendOverlayGui.java:47/50` gesetzt (siehe §9).

### 2.3 Rüstungswerte (`get_armorstats`, Kategorie `OreSpawnARMOR`)

Schlüssel je Präfix `s` (`OreSpawnMain.java:5633-5699`): `s_durability`, `s_head_damage_reduce`, `s_chest_damage_reduce`, `s_leggings_damage_reduce`, `s_boots_damage_reduce`, `s_enchantability`, `s_enchant_respiration`, `s_enchant_aquaaffinity`, `s_enchant_protection`, `s_enchant_fireprotection`, `s_enchant_blastprotection`, `s_enchant_projectileprotection`, `s_enchant_unbreaking`, `s_enchant_featherfalling`.

Klemmungen: `durability` und `enchantability` auf [Default/2, Default×2] (`:5637-5642`, `:5660-5665`); die vier `*_damage_reduce` nur nach unten auf Default−2, **ohne Obergrenze** (`:5644-5658`); alle acht `enchant_*` nur nach unten auf Default/2 (Integer-Division), ohne Obergrenze (`:5667-5697`).

| Präfix | Feld | durability | head | chest | leg | boots | enchantability | resp | aqua | prot | fire | blast | proj | unbreak | feather | Zeile | Material | gelesen von |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `Amethyst` | `Amethyst_armorstats` | 100 | 4 | 8 | 7 | 3 | 40 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | `:1160` | `AMETHYST` (`:1439`) | `ItemOreSpawnArmor` |
| `Emerald` | `Emerald_armorstats` | 60 | 3 | 8 | 6 | 3 | 40 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | `:1161` | `EMERALD` (`:1436`) | `ItemOreSpawnArmor` |
| `Experience` | `Experience_armorstats` | 70 | 5 | 9 | 7 | 4 | 50 | 0 | 0 | 2 | 0 | 1 | 0 | 0 | 1 | `:1162` | `EXPERIENCE` (`:1437`) | `ItemOreSpawnArmor` |
| `MothScale` | `MothScale_armorstats` | 50 | 2 | 7 | 5 | 2 | 50 | 0 | 0 | 3 | 3 | 3 | 0 | 0 | 5 | `:1163` | `MOTHSCALE` (`:1435`) | `ItemOreSpawnArmor` |
| `LavaEel` | `LavaEel_armorstats` | 40 | 2 | 7 | 5 | 2 | 35 | 1 | 2 | 3 | 2 | 10 | 0 | 0 | 2 | `:1164` | `LAVAEEL` (`:1434`) | `ItemOreSpawnArmor` |
| `Ultimate` | `Ultimate_armorstats` | 200 | 6 | 12 | 10 | 6 | 100 | 2 | 3 | 5 | 5 | 5 | 5 | 0 | 3 | `:1165` | `ULTIMATE` (`:1432`) | `ItemOreSpawnArmor` |
| `Pink` | `Pink_armorstats` | 50 | 3 | 7 | 5 | 2 | 40 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | `:1166` | `PINK` (`:1440`) | `ItemOreSpawnArmor` |
| `TigersEye` | `TigersEye_armorstats` | 80 | 4 | 8 | 7 | 4 | 55 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | `:1167` | `TIGERSEYE` (`:1441`) | `ItemOreSpawnArmor` |
| `Peacock` | `Peacock_armorstats` | 40 | 2 | 5 | 4 | 2 | 30 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 10 | `:1168` | `PEACOCK` (`:1442`) | `ItemOreSpawnArmor` |
| `Mobzilla` | `Mobzilla_armorstats` | 1000 | 7 | 13 | 11 | 7 | 150 | 0 | 0 | 10 | 10 | 10 | 10 | 5 | 10 | `:1169` | `MOBZILLA` (`:1433`) | `ItemOreSpawnArmor` |
| `Ruby` | `Ruby_armorstats` | 90 | 4 | 9 | 8 | 4 | 40 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | `:1170` | `RUBY` (`:1438`) | `ItemOreSpawnArmor` |
| `Royal` | `Royal_armorstats` | 2000 | 8 | 14 | 12 | 8 | 200 | 1 | 2 | 10 | 10 | 10 | 10 | 5 | 10 | `:1171` | `ROYAL` (`:1443`) | `ItemOreSpawnArmor` |
| `Lapis` | `Lapis_armorstats` | 60 | 2 | 7 | 5 | 2 | 60 | 1 | 1 | 1 | 0 | 0 | 1 | 0 | 0 | `:1172` | `LAPIS` (`:1444`) | `ItemOreSpawnArmor` |
| `Queen` | `Queen_armorstats` | 1500 | 9 | 16 | 14 | 9 | 150 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | `:1173` | `QUEEN` (`:1445`) | `ItemOreSpawnArmor` |

### 2.4 Waffen-/Werkzeugwerte (`get_weaponstats`, Kategorie `OreSpawnWEAPONS`)

Schlüssel je Präfix (`OreSpawnMain.java:5701-5736`): `s_harvestlevel`, `s_maxuses`, `s_efficiency`, `s_damage`, `s_enchantability`. Klemmungen: `maxuses`, `efficiency`, `damage`, `enchantability` auf [Default/2, Default×2]; `harvestlevel` wird nur dann auf den Default zurückgesetzt, wenn der Wert **kleiner als Default−1** ist (`:5704-5706`) — Default−1 selbst ist erlaubt, nach oben gibt es keine Grenze. Die Bedeutung von `damage` (Aufschlag im Item) gehört in die Item-Kataloge.

| Präfix | Feld | harvestlevel | maxuses | efficiency | damage | enchantability | Zeile | ToolMaterial | gelesen von |
|---|---|---|---|---|---|---|---|---|---|
| `Ultimate` | `ultimate_stats` | 10 | 3000 | 15 | 36 | 100 | `:1174` | `ULTIMATE` (`:1292`) | — |
| `Nightmare` | `nightmare_stats` | 3 | 1800 | 12 | 26 | 60 | `:1175` | `NIGHTMARE` (`:1293`) | — |
| `Bertha` | `bertha_stats` | 3 | 9000 | 15 | 496 | 100 | `:1176` | `BERTHA` (`:1297`) | `BerthaHit` |
| `CrystalWood` | `crystalwood_stats` | 2 | 300 | 3 | 2 | 15 | `:1177` | `CRYSTALWOOD` (`:1298`) | — |
| `CrystalStone` | `crystalstone_stats` | 3 | 800 | 6 | 5 | 45 | `:1178` | `CRYSTALSTONE` (`:1299`) | — |
| `Pink` | `crystalpink_stats` | 4 | 1100 | 10 | 7 | 65 | `:1179` | `CRYSTALPINK` (`:1300`) | — |
| `TigersEye` | `tigerseye_stats` | 4 | 1600 | 12 | 8 | 75 | `:1180` | `TIGERSEYE` (`:1301`) | — |
| `Ruby` | `ruby_stats` | 5 | 1500 | 11 | 16 | 85 | `:1181` | `RUBY` (`:1295`) | — |
| `Amethyst` | `amethyst_stats` | 4 | 2000 | 11 | 11 | 70 | `:1182` | `AMETHYST` (`:1296`) | — |
| `Emerald` | `emerald_stats` | 3 | 1300 | 10 | 6 | 75 | `:1183` | `REALEMERALD` (`:1294`) | — |
| `Royal` | `royal_stats` | 3 | 10000 | 15 | 746 | 150 | `:1184` | `ROYAL` (`:1302`) | `BerthaHit` |
| `Attitude` | `hammy_stats` | 5 | 2000 | 15 | 82 | 100 | `:1185` | `HAMMY` (`:1303`) | `BerthaHit` |
| `BattleAxe` | `battleaxe_stats` | 3 | 1500 | 15 | 46 | 75 | `:1186` | `BATTLE` (`:1304`) | — |
| `Chainsaw` | `chainsaw_stats` | 3 | 1500 | 10 | 56 | 75 | `:1187` | `CHAINSAW` (`:1305`) | `UltimateSword` |
| `QueenBattleAxe` | `queenbattleaxe_stats` | 3 | 2200 | 15 | 662 | 100 | `:1188` | `QUEENBATTLE` (`:1306`) | — |

### 2.5 Mob-Werte (`get_mobstats`, Kategorie `OreSpawnMOBS`)

Schlüssel je Präfix (`OreSpawnMain.java:5738-5768`): `s_health`, `s_attack`, `s_defense`. Klemmungen: `health` und `attack` auf [Default/2, Default×2]; `defense` erst auf [Default−4, Default+4], dann absolut auf [0, 22] (`:5755-5766`). Die Obergrenze 22 liegt unter der 1.21.1-Rüstungsklemmung 30; wie `defense` im Entity wirkt, steht in den Entity-Klassen (offen für diesen Batch).

| Präfix | Feld | health | attack | defense | Zeile | health max (×2) | Hinweis 1.21.1 | gelesen von |
|---|---|---|---|---|---|---|---|---|
| `Bee` | `Bee_stats` | 80 | 12 | 5 | `:6138` | 160 | — | `Bee` |
| `Mantis` | `Mantis_stats` | 120 | 16 | 10 | `:6139` | 240 | — | `Mantis` |
| `HerculesBeetle` | `HerculesBeetle_stats` | 250 | 30 | 19 | `:6140` | 500 | — | `HerculesBeetle` |
| `Mothra` | `Mothra_stats` | 150 | 12 | 8 | `:6141` | 300 | — | `Mothra` |
| `Brutalfly` | `Brutalfly_stats` | 110 | 10 | 6 | `:6142` | 220 | — | `Brutalfly` |
| `Nastysaurus` | `Nastysaurus_stats` | 200 | 32 | 17 | `:6143` | 400 | — | `Nastysaurus` |
| `Pointysaurus` | `Pointysaurus_stats` | 80 | 10 | 16 | `:6144` | 160 | — | `Pointysaurus` |
| `Alosaurus` | `Alosaurus_stats` | 110 | 18 | 8 | `:6145` | 220 | — | `Alosaurus` |
| `SpiderRobot` | `SpiderRobot_stats` | 1500 | 100 | 16 | `:6146` | 3000 | **virtuelles Leben** (Original 1500) | `ItemSpiderRobotKit`, `SpiderRobot` |
| `AntRobot` | `AntRobot_stats` | 300 | 30 | 16 | `:6147` | 600 | — | `AntRobot`, `ItemSpiderRobotKit` |
| `Jeffery` | `Jeffery_stats` | 550 | 40 | 18 | `:6148` | 1100 | Config-Max 1100 > 1024 | `GiantRobot` |
| `Hammerhead` | `Hammerhead_stats` | 240 | 75 | 20 | `:6149` | 480 | — | `Hammerhead` |
| `Molenoid` | `Molenoid_stats` | 200 | 18 | 12 | `:6150` | 400 | — | `Molenoid` |
| `TRex` | `TRex_stats` | 160 | 22 | 14 | `:6151` | 320 | — | `TRex` |
| `BandP` | `BandP_stats` | 100 | 1 | 18 | `:6152` | 200 | — | `BandP` |
| `CaterKiller` | `CaterKiller_stats` | 450 | 32 | 19 | `:6153` | 900 | — | `CaterKiller` |
| `Cryolophosaurus` | `Cryolophosaurus_stats` | 10 | 3 | 1 | `:6154` | 20 | — | `Cryolophosaurus` |
| `Rat` | `Rat_stats` | 5 | 3 | 1 | `:6155` | 10 | — | `Rat` |
| `Urchin` | `Urchin_stats` | 25 | 10 | 4 | `:6156` | 50 | — | `Urchin` |
| `Kyuubi` | `Kyuubi_stats` | 125 | 10 | 10 | `:6157` | 250 | — | `Kyuubi` |
| `GammaMetroid` | `GammaMetroid_stats` | 100 | 10 | 12 | `:6158` | 200 | — | `GammaMetroid` |
| `Basilisk` | `Basilisk_stats` | 200 | 24 | 15 | `:6159` | 400 | — | `Basilisk` |
| `EmperorScorpion` | `EmperorScorpion_stats` | 350 | 35 | 20 | `:6160` | 700 | — | `EmperorScorpion` |
| `TrooperBug` | `TrooperBug_stats` | 200 | 20 | 15 | `:6161` | 400 | — | `TrooperBug` |
| `SpitBug` | `SpitBug_stats` | 100 | 10 | 12 | `:6162` | 200 | — | `SpitBug` |
| `Alien` | `Alien_stats` | 100 | 12 | 8 | `:6163` | 200 | — | `Alien` |
| `WaterDragon` | `WaterDragon_stats` | 150 | 20 | 8 | `:6164` | 300 | — | `WaterDragon` |
| `SeaMonster` | `SeaMonster_stats` | 110 | 14 | 8 | `:6165` | 220 | — | `SeaMonster` |
| `SeaViper` | `SeaViper_stats` | 160 | 22 | 12 | `:6166` | 320 | — | `SeaViper` |
| `Robot2` | `Robot2_stats` | 200 | 22 | 18 | `:6167` | 400 | — | `Robot2` |
| `Robot3` | `Robot3_stats` | 80 | 16 | 14 | `:6168` | 160 | — | `Robot3` |
| `Robot4` | `Robot4_stats` | 170 | 12 | 18 | `:6169` | 340 | — | `Robot4` |
| `Robot5` | `Robot5_stats` | 20 | 5 | 6 | `:6170` | 40 | — | `Robot5` |
| `Rotator` | `Rotator_stats` | 35 | 10 | 8 | `:6171` | 70 | — | `Rotator` |
| `Vortex` | `Vortex_stats` | 150 | 26 | 10 | `:6172` | 300 | — | `Vortex` |
| `DungeonBeast` | `DungeonBeast_stats` | 65 | 12 | 6 | `:6173` | 130 | — | `DungeonBeast` |
| `Triffid` | `Triffid_stats` | 100 | 20 | 12 | `:6174` | 200 | — | `Triffid` |
| `LurkingTerror` | `LurkingTerror_stats` | 30 | 6 | 5 | `:6175` | 60 | — | `LurkingTerror` |
| `WormSmall` | `WormSmall_stats` | 10 | 3 | 0 | `:6176` | 20 | — | `WormSmall` |
| `WormMedium` | `WormMedium_stats` | 30 | 10 | 8 | `:6177` | 60 | — | `WormMedium` |
| `WormLarge` | `WormLarge_stats` | 90 | 18 | 14 | `:6178` | 180 | — | `WormLarge` |
| `EnderKnight` | `EnderKnight_stats` | 60 | 12 | 6 | `:6179` | 120 | — | `EnderKnight` |
| `EnderReaper` | `EnderReaper_stats` | 90 | 18 | 8 | `:6180` | 180 | — | `EnderReaper` |
| `Irukandji` | `Irukandji_stats` | 1 | 20 | 0 | `:6181` | 2 | — | `Irukandji` |
| `AttackSquid` | `AttackSquid_stats` | 10 | 8 | 0 | `:6182` | 20 | — | `AttackSquid` |
| `CaveFisher` | `CaveFisher_stats` | 10 | 4 | 4 | `:6183` | 20 | — | `CaveFisher` |
| `CloudShark` | `CloudShark_stats` | 15 | 6 | 5 | `:6184` | 30 | — | `CloudShark` |
| `CreepingHorror` | `CreepingHorror_stats` | 10 | 3 | 2 | `:6185` | 20 | — | `CreepingHorror` |
| `Mobzilla` | `Godzilla_stats` | 4000 | 175 | 21 | `:6186` | 8000 | **virtuelles Leben** (Original 4000) | `Godzilla`, `GodzillaHead` |
| `Kraken` | `Kraken_stats` | 1000 | 40 | 10 | `:6187` | 2000 | Config-Max 2000 > 1024 | `Kraken` |
| `LeafMonster` | `LeafMonster_stats` | 6 | 2 | 1 | `:6188` | 12 | — | `LeafMonster` |
| `Nightmare` | `PitchBlack_stats` | 250 | 30 | 10 | `:6189` | 500 | — | `Crab`, `PitchBlack` |
| `Scorpion` | `Scorpion_stats` | 15 | 4 | 10 | `:6190` | 30 | — | `Scorpion` |
| `Skate` | `Skate_stats` | 8 | 8 | 4 | `:6191` | 16 | — | `Skate` |
| `TerribleTerror` | `TerribleTerror_stats` | 10 | 5 | 3 | `:6192` | 20 | — | `TerribleTerror` |
| `TheKing` | `TheKing_stats` | 7000 | 350 | 21 | `:6193` | 14000 | **virtuelles Leben** (Original 7000) | `KingHead`, `TheKing` |
| `TheQueen` | `TheQueen_stats` | 6000 | 225 | 21 | `:6194` | 12000 | **virtuelles Leben** (Original 6000) | `QueenHead`, `TheQueen` |
| `Leonopteryx` | `Leon_stats` | 150 | 20 | 8 | `:6195` | 300 | — | — |
| `Crab` | `Crab_stats` | 180 | 24 | 16 | `:6196` | 360 | — | `Crab` |

### 2.6 Erzwerte (`get_orestats`, Kategorie `OreSpawnORES`)

Schlüssel je Präfix (`OreSpawnMain.java:5770-5802`): `s_rate`, `s_clumpsize`, `s_mindepth`, `s_maxdepth`. Klemmungen: `rate` auf [Default/2, Default×2]; `clumpsize` auf [Default/2, Default×2] und mindestens 1; `mindepth` und `maxdepth` mindestens 0; ist `maxdepth − mindepth < 10`, werden **beide** auf die Defaults zurückgesetzt (`:5797-5800`).

| Präfix | Feld | rate | clumpsize | mindepth | maxdepth | Zeile | gelesen von |
|---|---|---|---|---|---|---|---|
| `Ruby` | `Ruby_stats` | 10 | 1 | 0 | 50 | `:1244` | `OreSpawnWorld` |
| `BlockRuby` | `BlkRuby_stats` | 1 | 2 | 0 | 15 | `:1245` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Uranium` | `Uranium_stats` | 3 | 4 | 0 | 30 | `:1246` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Titanium` | `Titanium_stats` | 3 | 4 | 0 | 20 | `:1247` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Amethyst` | `Amethyst_stats` | 2 | 6 | 0 | 25 | `:1248` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Salt` | `Salt_stats` | 5 | 12 | 50 | 128 | `:1249` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `SpawnOres` | `SpawnOres_stats` | 28 | 4 | 50 | 128 | `:1250` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Diamond` | `Diamond_stats` | 4 | 6 | 0 | 30 | `:1251` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `BlockDiamond` | `BlkDiamond_stats` | 2 | 4 | 0 | 20 | `:1252` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Emerald` | `Emerald_stats` | 4 | 6 | 0 | 40 | `:1253` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `BlockEmerald` | `BlkEmerald_stats` | 2 | 4 | 0 | 20 | `:1254` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `Gold` | `Gold_stats` | 4 | 8 | 0 | 40 | `:1255` | `ChunkOreGenerator`, `OreSpawnWorld` |
| `BlockGold` | `BlkGold_stats` | 2 | 4 | 0 | 25 | `:1256` | `ChunkOreGenerator`, `OreSpawnWorld` |

---

## 3. Registrierung von Blöcken und Items

### 3.1 ID-Schema und Namen

- Numerische IDs: Blöcke `BaseBlockID + N` (Default 2700, `:1135`), Items `BaseItemID + N` (Default 9000, `:1136`). Die Zahlen sind nur Konstruktor-Argumente aus der Vor-1.7-Zeit und für 1.21.1 bedeutungslos; das manifest führt sie als `ctor_args`. **Eine Ausnahme wertet die Zahl zur Laufzeit aus:** `ItemSpiderRobotKit` prüft `i == OreSpawnMain.BaseItemID + 471` (`ItemSpiderRobotKit.java:16`) und unterscheidet so `SpiderRobotKit` (`BaseItemID + 471`, `:1385`) von `AntRobotKit` (`BaseItemID + 473`, `:1386`), die dieselbe Klasse haben. Im Port über die Item-Instanz oder einen Konstruktorparameter lösen.
- Registry-Namen: `GameRegistry.registerBlock(block, "OreSpawn_<Name>")` bzw. `registerItem(item, "OreSpawn_<Name>")`. Die neuen 1.21.1-Ids stehen im manifest (`legacy_registry` → `id`).
- Zählung: 211 `registerBlock` (`:1640-1852`) und 473 `registerItem` (`:1775-2323`), identisch mit manifest `counts` (211 Blöcke, 473 Items).
- Sprachnamen: 678× `LanguageRegistry.addNameForObject(..., "en_US", ...)` und 232× `addStringLocalization` (Entity-Namen), verteilt über `:2325-5033`. Nur `en_US`. Die Namen stehen im manifest (`name`).
- manifest-Probleme, die hier ihren Ursprung haben: `CrystalFurnaceBlock` und `CrystalFurnaceOnBlock` tragen denselben `setBlockName("crystalfurnace")` (`:1533-1534`); `MyPizzaItem`/`MyPizzaBlock` (`:1288-1289`) und `MyDuctTapeItem`/`MyDuctTapeBlock` (`:1290-1291`) teilen je einen unlokalisierten Namen, deshalb im manifest `pizza_item`/`ducttape_item`.
- Creative-Tab wird in `OreSpawnMain` nur für `MyPizzaItem` (`tabFood`, `:1289`) und `MyDuctTapeItem` (`tabTools`, `:1291`) gesetzt, beide mit `setMaxStackSize(1)`. Alle anderen Tabs setzen die Item-/Blockklassen selbst (116 weitere Quelldateien rufen `setCreativeTab` auf).

### 3.2 Reihenfolge der Registrierung

Blöcke in Aufrufreihenfolge (`Zeile: OreSpawn_<Name> → manifest-id`):

211 Einträge.

- **#1-40** (`:1640`-1679): 1640: `SpiderSpawnBlock`→`orespider`, 1641: `BatSpawnBlock`→`orebat`, 1642: `CowSpawnBlock`→`orecow`, 1643: `PigSpawnBlock`→`orepig`, 1644: `SquidSpawnBlock`→`oresquid`, 1645: `ChickenSpawnBlock`→`orechicken`, 1646: `CreeperSpawnBlock`→`orecreeper`, 1647: `SkeletonSpawnBlock`→`oreskeleton`, 1648: `ZombieSpawnBlock`→`orezombie`, 1649: `SlimeSpawnBlock`→`oreslime`, 1650: `GhastSpawnBlock`→`oreghast`, 1651: `ZombiePigmanSpawnBlock`→`orezombiepigman`, 1652: `EndermanSpawnBlock`→`oreenderman`, 1653: `CaveSpiderSpawnBlock`→`orecavespider`, 1654: `SilverfishSpawnBlock`→`oresilverfish`, 1655: `MagmaCubeSpawnBlock`→`oremagmacube`, 1656: `WitchSpawnBlock`→`orewitch`, 1657: `SheepSpawnBlock`→`oresheep`, 1658: `WolfSpawnBlock`→`orewolf`, 1659: `MooshroomSpawnBlock`→`oremooshroom`, 1660: `WitherBossSpawnBlock`→`orewitherboss`, 1661: `GirlfriendSpawnBlock`→`oregirlfriend`, 1662: `BoyfriendSpawnBlock`→`oreboyfriend`, 1663: `RedCowSpawnBlock`→`oreredcow`, 1664: `CrystalCowSpawnBlock`→`orecrystalcow`, 1665: `VillagerSpawnBlock`→`orevillager`, 1666: `GoldCowSpawnBlock`→`oregoldcow`, 1667: `EnchantedCowSpawnBlock`→`oreenchantedcow`, 1668: `MOTHRASpawnBlock`→`oremothra`, 1669: `AloSpawnBlock`→`orealosaurus`, 1670: `CryoSpawnBlock`→`orecryolophosaurus`, 1671: `CamaSpawnBlock`→`orecamarasaurus`, 1672: `VeloSpawnBlock`→`orevelocityraptor`, 1673: `HydroSpawnBlock`→`orehydrolisc`, 1674: `BasilSpawnBlock`→`orebasilisc`, 1675: `DragonflySpawnBlock`→`oredragonfly`, 1676: `EmperorScorpionSpawnBlock`→`oreemperorscorpion`, 1677: `ScorpionSpawnBlock`→`orescorpion`, 1678: `CaveFisherSpawnBlock`→`orecavefisher`, 1679: `SpyroSpawnBlock`→`orespyro`
- **#41-80** (`:1680`-1719): 1680: `BaryonyxSpawnBlock`→`orebaryonyx`, 1681: `GammaMetroidSpawnBlock`→`oregammametroid`, 1682: `CockateilSpawnBlock`→`orecockateil`, 1683: `KyuubiSpawnBlock`→`orekyuubi`, 1684: `AlienSpawnBlock`→`orealien`, 1685: `IronGolemSpawnBlock`→`oreirongolem`, 1686: `SnowGolemSpawnBlock`→`oresnowgolem`, 1687: `EnderDragonSpawnBlock`→`oreenderdragon`, 1688: `OcelotSpawnBlock`→`oreocelot`, 1689: `WitherSkeletonSpawnBlock`→`orewitherskeleton`, 1690: `BlazeSpawnBlock`→`oreblaze`, 1691: `AttackSquidSpawnBlock`→`oreattacksquid`, 1692: `WaterDragonSpawnBlock`→`orewaterdragon`, 1693: `CephadromeSpawnBlock`→`orecephadrome`, 1694: `KrakenSpawnBlock`→`orekraken`, 1695: `LizardSpawnBlock`→`orelizard`, 1696: `DragonSpawnBlock`→`oredragon`, 1697: `BeeSpawnBlock`→`orebee`, 1698: `HorseSpawnBlock`→`orehorse`, 1699: `TrooperBugSpawnBlock`→`oretrooper`, 1700: `SpitBugSpawnBlock`→`orespit`, 1701: `StinkBugSpawnBlock`→`orestink`, 1702: `OstrichSpawnBlock`→`oreostrich`, 1703: `GazelleSpawnBlock`→`oregazelle`, 1704: `ChipmunkSpawnBlock`→`orechipmunk`, 1705: `CreepingHorrorSpawnBlock`→`orecreepinghorror`, 1706: `TerribleTerrorSpawnBlock`→`oreterribleterror`, 1707: `CliffRacerSpawnBlock`→`orecliffracer`, 1708: `TriffidSpawnBlock`→`oretriffid`, 1709: `PitchBlackSpawnBlock`→`orenightmare`, 1710: `LurkingTerrorSpawnBlock`→`orelurkingterror`, 1711: `GodzillaPartSpawnBlock`→`oregodzillapart`, 1712: `GodzillaSpawnBlock`→`oregodzilla`, 1713: `TheKingPartSpawnBlock`→`orethekingpart`, 1714: `TheKingSpawnBlock`→`oretheking`, 1715: `TheQueenPartSpawnBlock`→`orethequeenpart`, 1716: `TheQueenSpawnBlock`→`orethequeen`, 1717: `SmallWormSpawnBlock`→`oresmallworm`, 1718: `MediumWormSpawnBlock`→`oremediumworm`, 1719: `LargeWormSpawnBlock`→`orelargeworm`
- **#81-120** (`:1720`-1759): 1720: `CassowarySpawnBlock`→`orecassowary`, 1721: `CloudSharkSpawnBlock`→`orecloudshark`, 1722: `GoldFishSpawnBlock`→`oregoldfish`, 1723: `LeafMonsterSpawnBlock`→`oreleafmonster`, 1724: `TshirtSpawnBlock`→`oretshirt`, 1725: `EnderKnightSpawnBlock`→`oreenderknight`, 1726: `EnderReaperSpawnBlock`→`oreenderreaper`, 1727: `BeaverSpawnBlock`→`orebeaver`, 1728: `UrchinSpawnBlock`→`oreurchin`, 1729: `FlounderSpawnBlock`→`oreflounder`, 1730: `SkateSpawnBlock`→`oreskate`, 1731: `RotatorSpawnBlock`→`orerotator`, 1732: `PeacockSpawnBlock`→`orepeacock`, 1733: `FairySpawnBlock`→`orefairy`, 1734: `DungeonBeastSpawnBlock`→`oredungeonbeast`, 1735: `VortexSpawnBlock`→`orevortex`, 1736: `RatSpawnBlock`→`orerat`, 1737: `WhaleSpawnBlock`→`orewhale`, 1738: `IrukandjiSpawnBlock`→`oreirukandji`, 1739: `TRexSpawnBlock`→`oretrex`, 1740: `HerculesSpawnBlock`→`orehercules`, 1741: `MantisSpawnBlock`→`oremantis`, 1742: `StinkySpawnBlock`→`orestinky`, 1743: `EasterBunnySpawnBlock`→`oreeasterbunny`, 1744: `CaterKillerSpawnBlock`→`orecaterkiller`, 1745: `MolenoidSpawnBlock`→`oremolenoid`, 1746: `SeaMonsterSpawnBlock`→`oreseamonster`, 1747: `SeaViperSpawnBlock`→`oreseaviper`, 1748: `LeonSpawnBlock`→`oreleon`, 1749: `HammerheadSpawnBlock`→`orehammerhead`, 1750: `RubberDuckySpawnBlock`→`orerubberducky`, 1751: `CriminalSpawnBlock`→`orecriminal`, 1752: `BrutalflySpawnBlock`→`orebrutalfly`, 1753: `NastysaurusSpawnBlock`→`orenastysaurus`, 1754: `PointysaurusSpawnBlock`→`orepointysaurus`, 1755: `CricketSpawnBlock`→`orecricket`, 1756: `FrogSpawnBlock`→`orefrog`, 1757: `SpiderDriverSpawnBlock`→`orespiderdriver`, 1758: `CrabSpawnBlock`→`orecrab`, 1759: `OreSaltBlock`→`oresalt`
- **#121-160** (`:1760`-1801): 1760: `RTPBlock`→`blockteleport`, 1761: `MoleDirtBlock`→`moledirt`, 1762: `OreTitaniumBlock`→`oretitanium`, 1763: `OreUraniumBlock`→`oreuranium`, 1764: `BlockTitaniumBlock`→`blocktitanium`, 1765: `BlockMobzillaScaleBlock`→`blockmobzillascale`, 1766: `BlockUraniumBlock`→`blockuranium`, 1767: `LavafoamBlock`→`lavafoam`, 1768: `OreRubyBlock`→`oreruby`, 1769: `BlockRubyBlock`→`blockruby`, 1770: `OreAmethystBlock`→`oreamethyst`, 1771: `BlockAmethystBlock`→`blockamethyst`, 1772: `CrystalPinkBlock`→`crystalpink_block`, 1773: `TigersEyeBlock`→`tigerseye_block`, 1774: `PizzaBlock`→`pizza`, 1776: `DuctTapeBlock`→`ducttape`, 1778: `OreCrystalStoneBlock`→`crystalstone`, 1779: `OreCrystalRatBlock`→`crystalrat`, 1780: `OreRedAntTrollBlock`→`redanttroll`, 1781: `OreTermiteTrollBlock`→`termitetroll`, 1782: `OreCrystalFairyBlock`→`crystalfairy`, 1783: `OreCrystalCoalBlock`→`crystalcoal`, 1784: `OreCrystalGrassBlock`→`crystalgrass`, 1785: `OreCrystalCrystalBlock`→`crystalcrystal`, 1786: `OreTigersEyeBlock`→`tigerseye`, 1787: `CrystalPlanksBlock`→`crystalplanks`, 1788: `CrystalWorkbenchBlock`→`crystalworkbench`, 1789: `CrystalFurnaceBlock`→`crystalfurnace`, 1790: `CrystalFurnaceOnBlock`→`crystalfurnace`, 1791: `StrawberryPlant`→`strawberry_plant`, 1792: `RadishPlant`→`radish_plant`, 1793: `RicePlant`→`rice_plant`, 1794: `ButterflyPlant`→`butterfly_plant`, 1795: `MothPlant`→`moth_plant`, 1796: `MosquitoPlant`→`mosquito_plant`, 1797: `FireflyPlant`→`firefly_plant`, 1798: `CornPlant1`→`corn_0`, 1799: `CornPlant2`→`corn_1`, 1800: `CornPlant3`→`corn_2`, 1801: `CornPlant4`→`corn_3`
- **#161-200** (`:1802`-1841): 1802: `QuinoaPlant1`→`quinoa_0`, 1803: `QuinoaPlant2`→`quinoa_1`, 1804: `QuinoaPlant3`→`quinoa_2`, 1805: `QuinoaPlant4`→`quinoa_3`, 1806: `TomatoPlant1`→`tomato_0`, 1807: `TomatoPlant2`→`tomato_1`, 1808: `TomatoPlant3`→`tomato_2`, 1809: `TomatoPlant4`→`tomato_3`, 1810: `LettucePlant1`→`lettuce_0`, 1811: `LettucePlant2`→`lettuce_1`, 1812: `LettucePlant3`→`lettuce_2`, 1813: `LettucePlant4`→`lettuce_3`, 1814: `AppleLeaves`→`leaves_apple`, 1815: `ExperienceLeaves`→`leaves_experience`, 1816: `ScaryLeaves`→`leaves_scary`, 1817: `CherryLeaves`→`leaves_cherry`, 1818: `PeachLeaves`→`leaves_peach`, 1819: `SkyTreeLog`→`skytreelog`, 1820: `DuplicatorLog`→`duplicatortreelog`, 1821: `ExperiencePlant`→`experiencesapling`, 1822: `CrystalPlant`→`crystalsapling`, 1823: `CrystalPlant2`→`crystalsapling2`, 1824: `CrystalPlant3`→`crystalsapling3`, 1825: `FlowerPink`→`flower_pink`, 1826: `FlowerBlue`→`flower_blue`, 1827: `FlowerBlack`→`flower_black`, 1828: `FlowerScary`→`flower_scary`, 1829: `CrystalFlowerRed`→`crystalflower_red`, 1830: `CrystalFlowerGreen`→`crystalflower_green`, 1831: `CrystalFlowerBlue`→`crystalflower_blue`, 1832: `CrystalFlowerYellow`→`crystalflower_yellow`, 1833: `CrystalLeaves`→`crystaltreeleaves`, 1834: `CrystalLeaves2`→`crystaltreeleaves2`, 1835: `CrystalLeaves3`→`crystaltreeleaves3`, 1836: `CrystalTreeLog`→`crystaltreelog`, 1837: `ExtremeTorch`→`extremetorch`, 1838: `CrystalTorch`→`crystaltorch`, 1839: `KrakenRepellent`→`krakenrepellent`, 1840: `CreeperRepellent`→`creeperrepellent`, 1841: `Island`→`island`
- **#201-211** (`:1842`-1852): 1842: `KingSpawner`→`kingspawner`, 1843: `QueenSpawner`→`queenspawner`, 1844: `DungeonSpawner`→`dungeonspawner`, 1845: `EnderPearlBlock`→`blockenderpearl`, 1846: `EyeOfEnderBlock`→`blockeyeofender`, 1847: `AntBlock`→`antblock`, 1848: `RedAntBlock`→`redantblock`, 1849: `TermiteBlock`→`termiteblock`, 1850: `CrystalTermiteBlock`→`crystaltermiteblock`, 1851: `RainbowAntBlock`→`rainbowantblock`, 1852: `UnstableAntBlock`→`unstableantblock`

Items in Aufrufreihenfolge:

473 Einträge.

- **#1-40** (`:1775`-1890): 1775: `PizzaItem`→`pizza_item`, 1777: `DuctTapeItem`→`ducttape_item`, 1853: `IngotUranium`→`ingoturanium`, 1854: `CrystalPinkIngot`→`crystalpink_ingot`, 1855: `TigersEyeIngot`→`tigerseye_ingot`, 1856: `IngotTitanium`→`ingottitanium`, 1857: `UltimateSword`→`ultimatesword`, 1858: `NightmareSword`→`nightmaresword`, 1859: `Bertha`→`berthasmall`, 1860: `Hammy`→`hammysmall`, 1861: `Slice`→`slicesmall`, 1862: `Royal`→`royalsmall`, 1863: `BattleAxe`→`battleaxesmall`, 1864: `QueenBattleAxe`→`queenbattleaxesmall`, 1865: `Chainsaw`→`chainsawsmall`, 1866: `UltimatePickaxe`→`ultimatepickaxe`, 1867: `UltimateShovel`→`ultimateshovel`, 1868: `UltimateHoe`→`ultimatehoe`, 1869: `UltimateAxe`→`ultimateaxe`, 1870: `EmeraldSword`→`emeraldsword`, 1871: `RoseSword`→`rosesword`, 1872: `ExperienceSword`→`experiencesword`, 1873: `PoisonSword`→`poisonsword`, 1874: `RatSword`→`ratsword`, 1875: `FairySword`→`fairysword`, 1876: `MantisClaw`→`mantisclaw`, 1877: `BigHammer`→`bighammer`, 1878: `EmeraldPickaxe`→`emeraldpickaxe`, 1879: `EmeraldShovel`→`emeraldshovel`, 1880: `EmeraldHoe`→`emeraldhoe`, 1881: `EmeraldAxe`→`emeraldaxe`, 1882: `CrystalWoodSword`→`crystalwoodsword`, 1883: `CrystalWoodPickaxe`→`crystalwoodpickaxe`, 1884: `CrystalWoodShovel`→`crystalwoodshovel`, 1885: `CrystalWoodHoe`→`crystalwoodhoe`, 1886: `CrystalWoodAxe`→`crystalwoodaxe`, 1887: `CrystalPinkSword`→`crystalpinksword`, 1888: `CrystalPinkPickaxe`→`crystalpinkpickaxe`, 1889: `CrystalPinkShovel`→`crystalpinkshovel`, 1890: `CrystalPinkHoe`→`crystalpinkhoe`
- **#41-80** (`:1891`-1930): 1891: `CrystalPinkAxe`→`crystalpinkaxe`, 1892: `TigersEyeSword`→`tigerseye_sword`, 1893: `TigersEyePickaxe`→`tigerseye_pickaxe`, 1894: `TigersEyeShovel`→`tigerseye_shovel`, 1895: `TigersEyeHoe`→`tigerseye_hoe`, 1896: `TigersEyeAxe`→`tigerseye_axe`, 1897: `CrystalStoneSword`→`crystalstonesword`, 1898: `CrystalStonePickaxe`→`crystalstonepickaxe`, 1899: `CrystalStoneShovel`→`crystalstoneshovel`, 1900: `CrystalStoneHoe`→`crystalstonehoe`, 1901: `CrystalStoneAxe`→`crystalstoneaxe`, 1902: `RubySword`→`rubysword`, 1903: `RubyPickaxe`→`rubypickaxe`, 1904: `RubyShovel`→`rubyshovel`, 1905: `RubyHoe`→`rubyhoe`, 1906: `RubyAxe`→`rubyaxe`, 1907: `AmethystSword`→`amethystsword`, 1908: `AmethystPickaxe`→`amethystpickaxe`, 1909: `AmethystShovel`→`amethystshovel`, 1910: `AmethystHoe`→`amethysthoe`, 1911: `AmethystAxe`→`amethystaxe`, 1912: `RedHeels`→`redheels`, 1913: `BlackHeels`→`blackheels`, 1914: `Slippers`→`slippers`, 1915: `Boots`→`boots`, 1916: `GameController`→`gamecontroller`, 1917: `UltimateBow`→`ultimatebow`, 1918: `SkateBow`→`skatebow`, 1919: `UltimateFishingRod`→`ultimatefishingrod`, 1920: `FireFish`→`firefish`, 1921: `SunFish`→`sunfish`, 1922: `LavaEel`→`lavaeel`, 1923: `MothScale`→`mothscale`, 1924: `QueenScale`→`queenscale`, 1925: `NightmareScale`→`nightmarescale`, 1926: `EmperorScorpionScale`→`emperorscorpionscale`, 1927: `BasiliskScale`→`basiliskscale`, 1928: `WaterDragonScale`→`waterdragonscale`, 1929: `PeacockFeather`→`peacockfeather`, 1930: `JumpyBugScale`→`jumpybugscale`
- **#81-120** (`:1931`-1970): 1931: `KrakenTooth`→`krakentooth`, 1932: `GodzillaScale`→`godzillascale`, 1933: `GreenGoo`→`greengoo`, 1934: `SpiderRobotKit`→`spiderrobotkit`, 1935: `AntRobotKit`→`antrobotkit`, 1936: `ZooKeeper`→`zookeeper`, 1937: `CreeperLauncher`→`creeperlauncher`, 1938: `NetherLost`→`netherlost`, 1939: `CrystalSticks`→`crystalsticks`, 1940: `Sifter`→`sifter`, 1941: `SunspotUrchin`→`sunspoturchin`, 1942: `WaterBall`→`waterball`, 1943: `LaserBall`→`laserball`, 1944: `IceBall`→`iceball`, 1945: `SmallRock`→`rocksmall`, 1946: `Rock`→`rock`, 1947: `RedRock`→`rockred`, 1948: `CrystalRedRock`→`rockcrystalred`, 1949: `CrystalGreenRock`→`rockcrystalgreen`, 1950: `CrystalBlueRock`→`rockcrystalblue`, 1951: `CrystalTNTRock`→`rockcrystaltnt`, 1952: `GreenRock`→`rockgreen`, 1953: `BlueRock`→`rockblue`, 1954: `PurpleRock`→`rockpurple`, 1955: `SpikeyRock`→`rockspikey`, 1956: `TNTRock`→`rocktnt`, 1957: `Acid`→`acid`, 1958: `Irukandji`→`deadirukandji`, 1959: `IrukandjiArrow`→`irukandjiarrow`, 1960: `RayGun`→`raygun`, 1961: `SquidZooka`→`squidzookasmall`, 1962: `SparkFish`→`sparkfish`, 1963: `Salt`→`salt`, 1964: `Popcorn`→`popcorn`, 1965: `ButteredPopcorn`→`popcorn_buttered`, 1966: `ButteredSaltedPopcorn`→`popcorn_buttered_salted`, 1967: `PopcornBag`→`popcorn_bag`, 1968: `Butter`→`butter`, 1969: `CornDog`→`corndog_cooked`, 1970: `Cheese`→`cheese`
- **#121-160** (`:1971`-2010): 1971: `RawCornDog`→`corndog_raw`, 1972: `Peacock`→`cookedpeacock`, 1973: `RawPeacock`→`rawpeacock`, 1974: `Ruby`→`ruby`, 1975: `Amethyst`→`amethyst`, 1976: `ThunderStaff`→`thunderstaff`, 1977: `Wrench`→`wrench`, 1978: `RawBacon`→`bacon`, 1979: `Bacon`→`cookedbacon`, 1980: `RawCrabMeat`→`crabmeat`, 1981: `CrabMeat`→`cookedcrabmeat`, 1982: `ButterCandy`→`buttercandy`, 1983: `UraniumNugget`→`uranium_nugget`, 1984: `TitaniumNugget`→`titanium_nugget`, 1985: `GreenFish`→`greenfish`, 1986: `BlueFish`→`bluefish`, 1987: `PinkFish`→`pinkfish`, 1988: `RockFish`→`rockfish`, 1989: `WoodFish`→`woodfish`, 1990: `GreyFish`→`greyfish`, 1991: `Salad`→`salad`, 1992: `BLT`→`blt_sandwich`, 1993: `CrabbyPatty`→`crabbypatty`, 1994: `BerthaHandle`→`bbhandle`, 1995: `BerthaGuard`→`bbguard`, 1996: `BerthaBlade`→`bbblade`, 1997: `MolenoidNose`→`molenoidnose`, 1998: `SeaMonsterScale`→`seamonsterscale`, 1999: `WormTooth`→`wormtooth`, 2000: `TRexTooth`→`trextooth`, 2001: `CaterKillerJaw`→`caterkillerjaw`, 2002: `SeaViperTongue`→`seavipertongue`, 2003: `VortexEye`→`vortexeye`, 2004: `WitherSkeletonEgg`→`eggwitherskeleton`, 2005: `EnderDragonEgg`→`eggenderdragon`, 2006: `SnowGolemEgg`→`eggsnowgolem`, 2007: `IronGolemEgg`→`eggirongolem`, 2008: `WitherBossEgg`→`eggwitherboss`, 2009: `GirlfriendEgg`→`egggirlfriend`, 2010: `BoyfriendEgg`→`eggboyfriend`
- **#161-200** (`:2011`-2050): 2011: `TheKingEgg`→`eggtheking`, 2012: `TheQueenEgg`→`eggthequeen`, 2013: `ThePrinceEgg`→`eggtheprince`, 2014: `RedCowEgg`→`eggredcow`, 2015: `CrystalCowEgg`→`eggcrystalcow`, 2016: `GoldCowEgg`→`egggoldcow`, 2017: `EnchantedCowEgg`→`eggenchantedcow`, 2018: `MOTHRAEgg`→`eggmothra`, 2019: `AloEgg`→`eggalosaurus`, 2020: `CryoEgg`→`eggcryolophosaurus`, 2021: `CamaEgg`→`eggcamarasaurus`, 2022: `VeloEgg`→`eggvelocityraptor`, 2023: `HydroEgg`→`egghydrolisc`, 2024: `BasilEgg`→`eggbasilisc`, 2025: `DragonflyEgg`→`eggdragonfly`, 2026: `EmperorScorpionEgg`→`eggemperorscorpion`, 2027: `ScorpionEgg`→`eggscorpion`, 2028: `CaveFisherEgg`→`eggcavefisher`, 2029: `SpyroEgg`→`eggspyro`, 2030: `BaryonyxEgg`→`eggbaryonyx`, 2031: `GammaMetroidEgg`→`egggammametroid`, 2032: `CockateilEgg`→`eggcockateil`, 2033: `KyuubiEgg`→`eggkyuubi`, 2034: `AlienEgg`→`eggalien`, 2035: `AttackSquidEgg`→`eggattacksquid`, 2036: `WaterDragonEgg`→`eggwaterdragon`, 2037: `CephadromeEgg`→`eggcephadrome`, 2038: `KrakenEgg`→`eggkraken`, 2039: `LizardEgg`→`egglizard`, 2040: `DragonEgg`→`eggdragon`, 2041: `BeeEgg`→`eggbee`, 2042: `TrooperBugEgg`→`eggtrooper`, 2043: `SpitBugEgg`→`eggspit`, 2044: `StinkBugEgg`→`eggstink`, 2045: `OstrichEgg`→`eggostrich`, 2046: `GazelleEgg`→`egggazelle`, 2047: `ChipmunkEgg`→`eggchipmunk`, 2048: `CreepingHorrorEgg`→`eggcreepinghorror`, 2049: `TerribleTerrorEgg`→`eggterribleterror`, 2050: `CliffRacerEgg`→`eggcliffracer`
- **#201-240** (`:2051`-2090): 2051: `TriffidEgg`→`eggtriffid`, 2052: `PitchBlackEgg`→`eggnightmare`, 2053: `LurkingTerrorEgg`→`egglurkingterror`, 2054: `GodzillaEgg`→`egggodzilla`, 2055: `SmallWormEgg`→`eggsmallworm`, 2056: `MediumWormEgg`→`eggmediumworm`, 2057: `LargeWormEgg`→`egglargeworm`, 2058: `CassowaryEgg`→`eggcassowary`, 2059: `CloudSharkEgg`→`eggcloudshark`, 2060: `GoldFishEgg`→`egggoldfish`, 2061: `LeafMonsterEgg`→`eggleafmonster`, 2062: `TshirtEgg`→`eggtshirt`, 2063: `EnderKnightEgg`→`eggenderknight`, 2064: `EnderReaperEgg`→`eggenderreaper`, 2065: `BeaverEgg`→`eggbeaver`, 2066: `DungeonBeastEgg`→`eggdungeonbeast`, 2067: `RotatorEgg`→`eggrotator`, 2068: `VortexEgg`→`eggvortex`, 2069: `PeacockEgg`→`eggpeacock`, 2070: `FairyEgg`→`eggfairy`, 2071: `RatEgg`→`eggrat`, 2072: `FlounderEgg`→`eggflounder`, 2073: `WhaleEgg`→`eggwhale`, 2074: `IrukandjiEgg`→`eggirukandji`, 2075: `SkateEgg`→`eggskate`, 2076: `UrchinEgg`→`eggurchin`, 2077: `Robot1Egg`→`eggrobot1`, 2078: `Robot2Egg`→`eggrobot2`, 2079: `Robot3Egg`→`eggrobot3`, 2080: `Robot4Egg`→`eggrobot4`, 2081: `GhostEgg`→`eggghost`, 2082: `GhostSkellyEgg`→`eggghostskelly`, 2083: `BrownAntEgg`→`eggbrownant`, 2084: `RedAntEgg`→`eggredant`, 2085: `RainbowAntEgg`→`eggrainbowant`, 2086: `UnstableAntEgg`→`eggunstableant`, 2087: `TermiteEgg`→`eggtermite`, 2088: `ButterflyEgg`→`eggbutterfly`, 2089: `MothEgg`→`eggmoth`, 2090: `MosquitoEgg`→`eggmosquito`
- **#241-280** (`:2091`-2130): 2091: `FireflyEgg`→`eggfirefly`, 2092: `TRexEgg`→`eggtrex`, 2093: `HerculesEgg`→`egghercules`, 2094: `MantisEgg`→`eggmantis`, 2095: `StinkyEgg`→`eggstinky`, 2096: `Robot5Egg`→`eggrobot5`, 2097: `CoinEgg`→`eggcoin`, 2098: `EasterBunnyEgg`→`eggeasterbunny`, 2099: `MolenoidEgg`→`eggmolenoid`, 2100: `SeaMonsterEgg`→`eggseamonster`, 2101: `SeaViperEgg`→`eggseaviper`, 2102: `CaterKillerEgg`→`eggcaterkiller`, 2103: `RubberDuckyEgg`→`eggrubberducky`, 2104: `HammerheadEgg`→`egghammerhead`, 2105: `LeonEgg`→`eggleon`, 2106: `CriminalEgg`→`eggcriminal`, 2107: `BrutalflyEgg`→`eggbrutalfly`, 2108: `NastysaurusEgg`→`eggnastysaurus`, 2109: `PointysaurusEgg`→`eggpointysaurus`, 2110: `CricketEgg`→`eggcricket`, 2111: `ThePrincessEgg`→`eggtheprincess`, 2112: `FrogEgg`→`eggfrog`, 2113: `JefferyEgg`→`eggrobot6`, 2114: `AntRobotEgg`→`eggantrobot`, 2115: `SpiderRobotEgg`→`eggspiderrobot`, 2116: `SpiderDriverEgg`→`eggspiderdriver`, 2117: `CrabEgg`→`eggcrab`, 2118: `CageEmpty`→`cageempty`, 2119: `CagedSpider`→`cagespider`, 2120: `CagedBat`→`cagebat`, 2121: `CagedCow`→`cagecow`, 2122: `CagePig`→`cagepig`, 2123: `CagedSquid`→`cagesquid`, 2124: `CagedChicken`→`cagechicken`, 2125: `CagedCreeper`→`cagecreeper`, 2126: `CagedSkeleton`→`cageskeleton`, 2127: `CagedZombie`→`cagezombie`, 2128: `CagedSlime`→`cageslime`, 2129: `CagedGhast`→`cageghast`, 2130: `CagedZombiePigman`→`cagezombiepigman`
- **#281-320** (`:2131`-2170): 2131: `CagedEnderman`→`cageenderman`, 2132: `CagedCaveSpider`→`cagecavespider`, 2133: `CagedSilverfish`→`cagesilverfish`, 2134: `CagedMagmaCube`→`cagemagmacube`, 2135: `CagedWitch`→`cagewitch`, 2136: `CagedSheep`→`cagesheep`, 2137: `CagedWolf`→`cagewolf`, 2138: `CagedMooshroom`→`cagemooshroom`, 2139: `CagedOcelot`→`cageocelot`, 2140: `CagedBlaze`→`cageblaze`, 2141: `CagedGirlfriend`→`cagegirlfriend`, 2142: `CagedBoyfriend`→`cageboyfriend`, 2143: `CagedWitherSkeleton`→`cagewitherskeleton`, 2144: `CagedEnderDragon`→`cageenderdragon`, 2145: `CagedSnowGolem`→`cagesnowgolem`, 2146: `CagedIronGolem`→`cageirongolem`, 2147: `CagedWitherBoss`→`cagewitherboss`, 2148: `CagedRedCow`→`cageredcow`, 2149: `CagedCrystalCow`→`cagecrystalcow`, 2150: `CagedVillager`→`cagevillager`, 2151: `CagedGoldCow`→`cagegoldcow`, 2152: `CagedEnchantedCow`→`cageenchantedcow`, 2153: `CagedMOTHRA`→`cagemothra`, 2154: `CagedAlo`→`cagealosaurus`, 2155: `CagedCryo`→`cagecryolophosaurus`, 2156: `CagedCama`→`cagecamarasaurus`, 2157: `CagedVelo`→`cagevelocityraptor`, 2158: `CagedHydro`→`cagehydrolisc`, 2159: `CagedBasil`→`cagebasilisc`, 2160: `CagedDragonfly`→`cagedragonfly`, 2161: `CagedEmperorScorpion`→`cageemperorscorpion`, 2162: `CagedScorpion`→`cagescorpion`, 2163: `CagedCaveFisher`→`cagecavefisher`, 2164: `CagedSpyro`→`cagespyro`, 2165: `CagedBaryonyx`→`cagebaryonyx`, 2166: `CagedGammaMetroid`→`cagegammametroid`, 2167: `CagedCockateil`→`cagecockateil`, 2168: `CagedKyuubi`→`cagekyuubi`, 2169: `CagedAlien`→`cagealien`, 2170: `Elevator`→`elevator`
- **#321-360** (`:2171`-2210): 2171: `CagedAttackSquid`→`cageattacksquid`, 2172: `CagedWaterDragon`→`cagewaterdragon`, 2173: `CagedCephadrome`→`cagecephadrome`, 2174: `CagedKraken`→`cagekraken`, 2175: `CagedLizard`→`cagelizard`, 2176: `CagedDragon`→`cagedragon`, 2177: `CagedBee`→`cagebee`, 2178: `CagedHorse`→`cagehorse`, 2179: `CagedFirefly`→`cagefirefly`, 2180: `CagedChipmunk`→`cagechipmunk`, 2181: `CagedGazelle`→`cagegazelle`, 2182: `CagedOstrich`→`cageostrich`, 2183: `CagedTrooper`→`cagetrooper`, 2184: `CagedSpit`→`cagespit`, 2185: `CagedStink`→`cagestink`, 2186: `CagedCreepingHorror`→`cagecreepinghorror`, 2187: `CagedTerribleTerror`→`cageterribleterror`, 2188: `CagedCliffRacer`→`cagecliffracer`, 2189: `CagedTriffid`→`cagetriffid`, 2190: `CagedPitchBlack`→`cagenightmare`, 2191: `CagedLurkingTerror`→`cagelurkingterror`, 2192: `CagedSmallWorm`→`cagesmallworm`, 2193: `CagedMediumWorm`→`cagemediumworm`, 2194: `CagedLargeWorm`→`cagelargeworm`, 2195: `CagedCassowary`→`cagecassowary`, 2196: `CagedCloudShark`→`cagecloudshark`, 2197: `CagedGoldFish`→`cagegoldfish`, 2198: `CagedLeafMonster`→`cageleafmonster`, 2199: `CagedEnderKnight`→`cageenderknight`, 2200: `CagedEnderReaper`→`cageenderreaper`, 2201: `CagedBeaver`→`cagebeaver`, 2202: `CagedUrchin`→`cageurchin`, 2203: `CagedFlounder`→`cageflounder`, 2204: `CagedSkate`→`cageskate`, 2205: `CagedRotator`→`cagerotator`, 2206: `CagedPeacock`→`cagepeacock`, 2207: `CagedFairy`→`cagefairy`, 2208: `CagedDungeonBeast`→`cagedungeonbeast`, 2209: `CagedVortex`→`cagevortex`, 2210: `CagedRat`→`cagerat`
- **#361-400** (`:2211`-2250): 2211: `CagedWhale`→`cagewhale`, 2212: `CagedIrukandji`→`cageirukandji`, 2213: `CagedTRex`→`cagetrex`, 2214: `CagedHercules`→`cagehercules`, 2215: `CagedMantis`→`cagemantis`, 2216: `CagedStinky`→`cagestinky`, 2217: `CagedEasterBunny`→`cageeasterbunny`, 2218: `CagedCaterKiller`→`cagecaterkiller`, 2219: `CagedMolenoid`→`cagemolenoid`, 2220: `CagedSeaMonster`→`cageseamonster`, 2221: `CagedSeaViper`→`cageseaviper`, 2222: `CagedLeon`→`cageleon`, 2223: `CagedHammerhead`→`cagehammerhead`, 2224: `CagedRubberDucky`→`cagerubberducky`, 2225: `CagedCriminal`→`cagecriminal`, 2226: `CagedBrutalfly`→`cagebrutalfly`, 2227: `CagedNastysaurus`→`cagenastysaurus`, 2228: `CagedPointysaurus`→`cagepointysaurus`, 2229: `CagedCricket`→`cagecricket`, 2230: `CagedFrog`→`cagefrog`, 2231: `CagedSpiderDriver`→`cagespiderdriver`, 2232: `CagedCrab`→`cagecrab`, 2233: `Strawberry`→`strawberry`, 2234: `CrystalApple`→`crystalapple`, 2235: `Love`→`heart`, 2236: `Cherry`→`cherries`, 2237: `Peach`→`peach`, 2238: `Radish`→`radish`, 2239: `Rice`→`rice`, 2240: `CornCob`→`corn_seed`, 2241: `Quinoa`→`quinoa`, 2242: `Tomato`→`tomato_seed`, 2243: `Lettuce`→`lettuce_seed`, 2244: `StrawberrySeed`→`strawberry_seed`, 2245: `ButterflySeed`→`butterfly_seed`, 2246: `MothSeed`→`moth_seed`, 2247: `MosquitoSeed`→`mosquito_seed`, 2248: `FireflySeed`→`firefly_seed`, 2249: `MagicApple`→`magicapple`, 2250: `RandomDungeon`→`randomdungeon`
- **#401-440** (`:2251`-2290): 2251: `MinersDream`→`minersdream`, 2252: `UltimateHelmet`→`ultimate_helmet`, 2253: `UltimateBody`→`ultimate_chest`, 2254: `UltimateLegs`→`ultimate_leggings`, 2255: `UltimateBoots`→`ultimate_boots`, 2256: `LavaEelHelmet`→`lavaeel_helmet`, 2257: `LavaEelBody`→`lavaeel_chest`, 2258: `LavaEelLegs`→`lavaeel_leggings`, 2259: `LavaEelBoots`→`lavaeel_boots`, 2260: `MothScaleHelmet`→`mothscale_helmet`, 2261: `MothScaleBody`→`mothscale_chest`, 2262: `MothScaleLegs`→`mothscale_leggings`, 2263: `MothScaleBoots`→`mothscale_boots`, 2264: `AppleSeed`→`appletree_seed`, 2265: `CherrySeed`→`cherrytree_seed`, 2266: `PeachSeed`→`peachtree_seed`, 2267: `StepUp`→`step_up`, 2268: `StepDown`→`step_down`, 2269: `StepAccross`→`step_accross`, 2270: `EmeraldHelmet`→`emerald_helmet`, 2271: `EmeraldBody`→`emerald_chest`, 2272: `EmeraldLegs`→`emerald_leggings`, 2273: `EmeraldBoots`→`emerald_boots`, 2274: `ExperienceCatcher`→`experiencecatcher`, 2275: `DeadStinkBug`→`deadstinkbug`, 2276: `ExperienceTreeSeed`→`experiencetree_seed`, 2277: `ExperienceHelmet`→`experience_helmet`, 2278: `ExperienceBody`→`experience_chest`, 2279: `ExperienceLegs`→`experience_leggings`, 2280: `ExperienceBoots`→`experience_boots`, 2281: `RubyHelmet`→`ruby_helmet`, 2282: `RubyBody`→`ruby_chest`, 2283: `RubyLegs`→`ruby_leggings`, 2284: `RubyBoots`→`ruby_boots`, 2285: `AmethystHelmet`→`amethyst_helmet`, 2286: `AmethystBody`→`amethyst_chest`, 2287: `AmethystLegs`→`amethyst_leggings`, 2288: `AmethystBoots`→`amethyst_boots`, 2289: `ZooCage2`→`zoo2`, 2290: `ZooCage4`→`zoo4`
- **#441-473** (`:2291`-2323): 2291: `ZooCage6`→`zoo6`, 2292: `ZooCage8`→`zoo8`, 2293: `ZooCage10`→`zoo10`, 2294: `InstantShelter`→`instantshelter`, 2295: `InstantGarden`→`instantgarden`, 2296: `CrystalPinkHelmet`→`pink_helmet`, 2297: `CrystalPinkBody`→`pink_chest`, 2298: `CrystalPinkLegs`→`pink_leggings`, 2299: `CrystalPinkBoots`→`pink_boots`, 2300: `TigersEyeHelmet`→`tigerseye_helmet`, 2301: `TigersEyeBody`→`tigerseye_chest`, 2302: `TigersEyeLegs`→`tigerseye_leggings`, 2303: `TigersEyeBoots`→`tigerseye_boots`, 2304: `PeacockFeatherBoots`→`peacock_boots`, 2305: `PeacockFeatherHelmet`→`peacock_helmet`, 2306: `PeacockFeatherBody`→`peacock_chest`, 2307: `PeacockFeatherLegs`→`peacock_leggings`, 2308: `MobzillaHelmet`→`mobzilla_helmet`, 2309: `MobzillaBody`→`mobzilla_chest`, 2310: `MobzillaLegs`→`mobzilla_leggings`, 2311: `MobzillaBoots`→`mobzilla_boots`, 2312: `RoyalHelmet`→`royal_helmet`, 2313: `RoyalBody`→`royal_chest`, 2314: `RoyalLegs`→`royal_leggings`, 2315: `RoyalBoots`→`royal_boots`, 2316: `LapisHelmet`→`lapis_helmet`, 2317: `LapisBody`→`lapis_chest`, 2318: `LapisLegs`→`lapis_leggings`, 2319: `LapisBoots`→`lapis_boots`, 2320: `QueenHelmet`→`queen_helmet`, 2321: `QueenBody`→`queen_chest`, 2322: `QueenLegs`→`queen_leggings`, 2323: `QueenBoots`→`queen_boots`

### 3.3 Blockkonstruktion (Eigenschaften, die `OreSpawnMain` setzt)

Konstruktor-Zusatzargumente sind klassenspezifisch (z. B. Härte/Resistenz bei `OreBasicStone`, `OreCrystal`; Brennstatus bei `CrystalFurnace`); ihre Bedeutung steht in der jeweiligen Blockklasse.

211 Blockkonstruktionen (inkl. `laySomeEggs`).

| Zeile | Feld | Klasse | BaseBlockID + | Ctor-Zusatz | unlocalized | weitere Aufrufe | manifest-id |
|---|---|---|---|---|---|---|---|
| `:1274` | `MyOreUraniumBlock` | `OreUranium` | 101 | — | `oreuranium` | — | `oreuranium` |
| `:1275` | `MyOreTitaniumBlock` | `OreTitanium` | 102 | — | `oretitanium` | — | `oretitanium` |
| `:1278` | `MyBlockUraniumBlock` | `BlockUranium` | 107 | — | `blockuranium` | — | `blockuranium` |
| `:1279` | `MyBlockTitaniumBlock` | `BlockTitanium` | 108 | — | `blocktitanium` | — | `blocktitanium` |
| `:1280` | `MyBlockMobzillaScaleBlock` | `BlockRuby` | 124 | — | `blockmobzillascale` | — | `blockmobzillascale` |
| `:1281` | `MyLavafoamBlock` | `Lavafoam` | 106 | — | `lavafoam` | — | `lavafoam` |
| `:1282` | `MyBlockRubyBlock` | `BlockRuby` | 109 | — | `blockruby` | — | `blockruby` |
| `:1283` | `MyBlockAmethystBlock` | `BlockRuby` | 110 | — | `blockamethyst` | — | `blockamethyst` |
| `:1284` | `MyCrystalPinkBlock` | `BlockCrystal` | 216 | — | `crystalpink_block` | — | `crystalpink_block` |
| `:1286` | `MyTigersEyeBlock` | `BlockCrystal` | 218 | — | `tigerseye_block` | — | `tigerseye_block` |
| `:1288` | `MyPizzaBlock` | `BlockPizza` | 194 | — | `pizza` | — | `pizza` |
| `:1290` | `MyDuctTapeBlock` | `BlockDuctTape` | 198 | — | `ducttape` | — | `ducttape` |
| `:1502` | `MyOreSaltBlock` | `OreSalt` | 100 | — | `oresalt` | — | `oresalt` |
| `:1520` | `MyOreRubyBlock` | `OreRuby` | 104 | — | `oreruby` | — | `oreruby` |
| `:1522` | `MyOreAmethystBlock` | `OreAmethyst` | 103 | — | `oreamethyst` | — | `oreamethyst` |
| `:1526` | `CrystalStone` | `OreBasicStone` | 200 | 2.0f, 10.0f | `crystalstone` | — | `crystalstone` |
| `:1527` | `CrystalCoal` | `OreCrystal` | 201 | 0.6f, 6.0f, 20.0f | `crystalcoal` | — | `crystalcoal` |
| `:1528` | `CrystalGrass` | `CrystalGrass` | 202 | 0.6f, 2.0f | `crystalgrass` | — | `crystalgrass` |
| `:1529` | `CrystalCrystal` | `OreCrystalCrystal` | 209 | 0.4f, 12.0f, 40.0f | `crystalcrystal` | — | `crystalcrystal` |
| `:1530` | `TigersEye` | `OreCrystalCrystal` | 217 | 0.5f, 15.0f, 60.0f | `tigerseye` | — | `tigerseye` |
| `:1531` | `CrystalPlanksBlock` | `CrystalWood` | 210 | 1.5f, 4.0f | `crystalplanks` | — | `crystalplanks` |
| `:1532` | `CrystalWorkbenchBlock` | `CrystalWorkbench` | 211 | 1.0f, 5.0f | `crystalworkbench` | — | `crystalworkbench` |
| `:1533` | `CrystalFurnaceBlock` | `CrystalFurnace` | 212 | false, 2.0f, 10.0f | `crystalfurnace` | — | `crystalfurnace` |
| `:1534` | `CrystalFurnaceOnBlock` | `CrystalFurnace` | 213 | true, 2.0f, 10.0f | `crystalfurnace` | — | `crystalfurnace` |
| `:1537` | `CrystalRat` | `OreBasicStone` | 219 | 2.5f, 14.0f | `crystalrat` | — | `crystalrat` |
| `:1538` | `CrystalFairy` | `OreBasicStone` | 220 | 2.5f, 14.0f | `crystalfairy` | — | `crystalfairy` |
| `:1539` | `RedAntTroll` | `OreBasicStone` | 225 | 2.5f, 14.0f | `redanttroll` | — | `redanttroll` |
| `:1540` | `TermiteTroll` | `OreBasicStone` | 226 | 2.5f, 14.0f | `termitetroll` | — | `termitetroll` |
| `:1541` | `MyRTPBlock` | `RTPBlock` | 105 | — | `blockteleport` | `setStepSound(Block.soundTypeStone)` | `blockteleport` |
| `:1545` | `MyMoleDirtBlock` | `MoleDirtBlock` | 123 | — | `moledirt` | `setHardness(0.6f).setStepSound(new Block.SoundType("gravel", 1.0f, 1.0f))` | `moledirt` |
| `:1548` | `MyStrawberryPlant` | `BlockStrawberry` | 153 | — | `—` | — | `strawberry_plant` |
| `:1550` | `MyButterflyPlant` | `BlockButterflyPlant` | 155 | — | `—` | — | `butterfly_plant` |
| `:1552` | `MyMothPlant` | `BlockMothPlant` | 156 | — | `—` | — | `moth_plant` |
| `:1554` | `MyMosquitoPlant` | `BlockMosquitoPlant` | 157 | — | `—` | — | `mosquito_plant` |
| `:1556` | `MyFireflyPlant` | `BlockFireflyPlant` | 154 | — | `—` | — | `firefly_plant` |
| `:1558` | `MyRadishPlant` | `BlockRadish` | 175 | — | `—` | — | `radish_plant` |
| `:1564` | `MyRicePlant` | `BlockRice` | 178 | — | `—` | — | `rice_plant` |
| `:1567` | `MyCornPlant1` | `BlockCorn` | 163 | — | `corn_0` | — | `corn_0` |
| `:1568` | `MyCornPlant2` | `BlockCorn` | 164 | — | `corn_1` | — | `corn_1` |
| `:1569` | `MyCornPlant3` | `BlockCorn` | 165 | — | `corn_2` | — | `corn_2` |
| `:1570` | `MyCornPlant4` | `BlockCorn` | 166 | — | `corn_3` | — | `corn_3` |
| `:1572` | `MyQuinoaPlant1` | `BlockQuinoa` | 179 | — | `quinoa_0` | — | `quinoa_0` |
| `:1573` | `MyQuinoaPlant2` | `BlockQuinoa` | 180 | — | `quinoa_1` | — | `quinoa_1` |
| `:1574` | `MyQuinoaPlant3` | `BlockQuinoa` | 181 | — | `quinoa_2` | — | `quinoa_2` |
| `:1575` | `MyQuinoaPlant4` | `BlockQuinoa` | 182 | — | `quinoa_3` | — | `quinoa_3` |
| `:1577` | `MyTomatoPlant1` | `BlockTomato` | 167 | — | `tomato_0` | — | `tomato_0` |
| `:1578` | `MyTomatoPlant2` | `BlockTomato` | 168 | — | `tomato_1` | — | `tomato_1` |
| `:1579` | `MyTomatoPlant3` | `BlockTomato` | 169 | — | `tomato_2` | — | `tomato_2` |
| `:1580` | `MyTomatoPlant4` | `BlockTomato` | 170 | — | `tomato_3` | — | `tomato_3` |
| `:1582` | `MyLettucePlant1` | `BlockLettuce` | 171 | — | `lettuce_0` | — | `lettuce_0` |
| `:1583` | `MyLettucePlant2` | `BlockLettuce` | 172 | — | `lettuce_1` | — | `lettuce_1` |
| `:1584` | `MyLettucePlant3` | `BlockLettuce` | 173 | — | `lettuce_2` | — | `lettuce_2` |
| `:1585` | `MyLettucePlant4` | `BlockLettuce` | 174 | — | `lettuce_3` | — | `lettuce_3` |
| `:1589` | `ExtremeTorch` | `BlockExtremeTorch` | 192 | — | `extremetorch` | `setLightLevel(1.0f)` | `extremetorch` |
| `:1590` | `KrakenRepellent` | `KrakenRepellent` | 190 | — | `krakenrepellent` | `setLightLevel(0.8f)` | `krakenrepellent` |
| `:1591` | `MyIslandBlock` | `IslandBlock` | 193 | — | `island` | `setLightLevel(0.9f)` | `island` |
| `:1592` | `CreeperRepellent` | `CreeperRepellent` | 191 | — | `creeperrepellent` | `setLightLevel(0.8f)` | `creeperrepellent` |
| `:1600` | `CrystalTorch` | `BlockCrystalTorch` | 214 | — | `crystaltorch` | `setLightLevel(0.99f)` | `crystaltorch` |
| `:1601` | `MyKingSpawnerBlock` | `KingSpawnerBlock` | 195 | — | `kingspawner` | `setLightLevel(0.9f)` | `kingspawner` |
| `:1602` | `MyQueenSpawnerBlock` | `QueenSpawnerBlock` | 197 | — | `queenspawner` | `setLightLevel(0.9f)` | `queenspawner` |
| `:1604` | `MyDungeonSpawnerBlock` | `DungeonSpawnerBlock` | 196 | — | `dungeonspawner` | `setLightLevel(0.9f)` | `dungeonspawner` |
| `:1605` | `MyAppleLeaves` | `BlockAppleLeaves` | 150 | — | `leaves_apple` | `setHardness(0.2f).setLightOpacity(1).setStepSound(Block.soundTypeGrass)` | `leaves_apple` |
| `:1607` | `MySkyTreeLog` | `BlockSkyTreeLog` | 113 | 20 | `skytreelog` | `setHardness(0.2f).setStepSound(Block.soundTypeWood)` | `skytreelog` |
| `:1608` | `MyDT` | `BlockDuplicatorLog` | 114 | — | `duplicatortreelog` | `setHardness(0.2f).setStepSound(Block.soundTypeWood)` | `duplicatortreelog` |
| `:1609` | `MyExperienceLeaves` | `BlockExperienceLeaves` | 151 | — | `leaves_experience` | `setHardness(0.2f).setLightOpacity(1).setStepSound(Block.soundTypeGrass)` | `leaves_experience` |
| `:1612` | `MyExperiencePlant` | `BlockExperiencePlant` | 158 | — | `experiencesapling` | — | `experiencesapling` |
| `:1614` | `MyFlowerPinkBlock` | `MyBlockFlower` | 159 | — | `flower_pink` | `setHardness(0.0f).setStepSound(Block.soundTypeGrass)` | `flower_pink` |
| `:1615` | `MyFlowerBlueBlock` | `MyBlockFlower` | 160 | — | `flower_blue` | `setHardness(0.0f).setStepSound(Block.soundTypeGrass)` | `flower_blue` |
| `:1616` | `MyFlowerBlackBlock` | `MyBlockFlower` | 161 | — | `flower_black` | `setHardness(0.0f).setStepSound(Block.soundTypeGrass)` | `flower_black` |
| `:1617` | `MyFlowerScaryBlock` | `MyBlockFlower` | 162 | — | `flower_scary` | `setHardness(0.0f).setStepSound(Block.soundTypeGrass)` | `flower_scary` |
| `:1618` | `MyScaryLeaves` | `BlockScaryLeaves` | 152 | — | `leaves_scary` | `setHardness(0.2f).setLightOpacity(1).setStepSound(Block.soundTypeGrass)` | `leaves_scary` |
| `:1619` | `MyCherryLeaves` | `BlockScaryLeaves` | 176 | — | `leaves_cherry` | `setHardness(0.15f).setLightOpacity(1).setStepSound(Block.soundTypeGrass)` | `leaves_cherry` |
| `:1620` | `MyPeachLeaves` | `BlockScaryLeaves` | 177 | — | `leaves_peach` | `setHardness(0.15f).setLightOpacity(1).setStepSound(Block.soundTypeGrass)` | `leaves_peach` |
| `:1623` | `CrystalFlowerRedBlock` | `MyBlockFlower` | 203 | — | `crystalflower_red` | `setHardness(0.0f).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystalflower_red` |
| `:1624` | `CrystalFlowerGreenBlock` | `MyBlockFlower` | 204 | — | `crystalflower_green` | `setHardness(0.0f).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystalflower_green` |
| `:1625` | `CrystalFlowerBlueBlock` | `MyBlockFlower` | 205 | — | `crystalflower_blue` | `setHardness(0.0f).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystalflower_blue` |
| `:1626` | `CrystalFlowerYellowBlock` | `MyBlockFlower` | 206 | — | `crystalflower_yellow` | `setHardness(0.0f).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystalflower_yellow` |
| `:1627` | `MyCrystalLeaves` | `BlockCrystalLeaves` | 208 | — | `crystaltreeleaves` | `setHardness(0.2f).setLightOpacity(1).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystaltreeleaves` |
| `:1628` | `MyCrystalTreeLog` | `BlockCrystalTreeLog` | 207 | 20 | `crystaltreelog` | `setHardness(0.2f).setStepSound(new Block.SoundType("wood", 1.0f, 1.0f))` | `crystaltreelog` |
| `:1629` | `MyCrystalLeaves2` | `BlockCrystalLeaves` | 215 | — | `crystaltreeleaves2` | `setHardness(0.25f).setLightOpacity(1).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystaltreeleaves2` |
| `:1630` | `MyCrystalLeaves3` | `BlockCrystalLeaves` | 221 | — | `crystaltreeleaves3` | `setHardness(0.25f).setLightOpacity(1).setStepSound(new Block.SoundType("grass", 1.0f, 1.0f))` | `crystaltreeleaves3` |
| `:1631` | `MyCrystalPlant` | `BlockCrystalPlant` | 222 | — | `crystalsapling` | — | `crystalsapling` |
| `:1632` | `MyCrystalPlant2` | `BlockCrystalPlant` | 223 | — | `crystalsapling2` | — | `crystalsapling2` |
| `:1633` | `MyCrystalPlant3` | `BlockCrystalPlant` | 224 | — | `crystalsapling3` | — | `crystalsapling3` |
| `:1634` | `MyEnderPearlBlock` | `OreGenericEgg` | 111 | — | `blockenderpearl` | — | `blockenderpearl` |
| `:1635` | `MyEyeOfEnderBlock` | `OreGenericEgg` | 112 | — | `blockeyeofender` | — | `blockeyeofender` |
| `:5908` | `MySpiderSpawnBlock` | `OreGenericEgg` | 0 | — | `orespider` | — | `orespider` |
| `:5909` | `MyBatSpawnBlock` | `OreGenericEgg` | 1 | — | `orebat` | — | `orebat` |
| `:5910` | `MyCowSpawnBlock` | `OreGenericEgg` | 2 | — | `orecow` | — | `orecow` |
| `:5911` | `MyPigSpawnBlock` | `OreGenericEgg` | 3 | — | `orepig` | — | `orepig` |
| `:5912` | `MySquidSpawnBlock` | `OreGenericEgg` | 4 | — | `oresquid` | — | `oresquid` |
| `:5913` | `MyChickenSpawnBlock` | `OreGenericEgg` | 5 | — | `orechicken` | — | `orechicken` |
| `:5914` | `MyCreeperSpawnBlock` | `OreGenericEgg` | 6 | — | `orecreeper` | — | `orecreeper` |
| `:5915` | `MySkeletonSpawnBlock` | `OreGenericEgg` | 7 | — | `oreskeleton` | — | `oreskeleton` |
| `:5916` | `MyZombieSpawnBlock` | `OreGenericEgg` | 8 | — | `orezombie` | — | `orezombie` |
| `:5917` | `MySlimeSpawnBlock` | `OreGenericEgg` | 9 | — | `oreslime` | — | `oreslime` |
| `:5918` | `MyGhastSpawnBlock` | `OreGenericEgg` | 10 | — | `oreghast` | — | `oreghast` |
| `:5919` | `MyZombiePigmanSpawnBlock` | `OreGenericEgg` | 11 | — | `orezombiepigman` | — | `orezombiepigman` |
| `:5920` | `MyEndermanSpawnBlock` | `OreGenericEgg` | 12 | — | `oreenderman` | — | `oreenderman` |
| `:5921` | `MyCaveSpiderSpawnBlock` | `OreGenericEgg` | 13 | — | `orecavespider` | — | `orecavespider` |
| `:5922` | `MySilverfishSpawnBlock` | `OreGenericEgg` | 14 | — | `oresilverfish` | — | `oresilverfish` |
| `:5923` | `MyMagmaCubeSpawnBlock` | `OreGenericEgg` | 15 | — | `oremagmacube` | — | `oremagmacube` |
| `:5924` | `MyWitchSpawnBlock` | `OreGenericEgg` | 16 | — | `orewitch` | — | `orewitch` |
| `:5925` | `MySheepSpawnBlock` | `OreGenericEgg` | 17 | — | `oresheep` | — | `oresheep` |
| `:5926` | `MyWolfSpawnBlock` | `OreGenericEgg` | 18 | — | `orewolf` | — | `orewolf` |
| `:5927` | `MyMooshroomSpawnBlock` | `OreGenericEgg` | 19 | — | `oremooshroom` | — | `oremooshroom` |
| `:5928` | `MyOcelotSpawnBlock` | `OreGenericEgg` | 20 | — | `oreocelot` | — | `oreocelot` |
| `:5929` | `MyBlazeSpawnBlock` | `OreGenericEgg` | 21 | — | `oreblaze` | — | `oreblaze` |
| `:5930` | `MyWitherSkeletonSpawnBlock` | `OreGenericEgg` | 22 | — | `orewitherskeleton` | — | `orewitherskeleton` |
| `:5931` | `MyEnderDragonSpawnBlock` | `OreGenericEgg` | 23 | — | `oreenderdragon` | — | `oreenderdragon` |
| `:5932` | `MySnowGolemSpawnBlock` | `OreGenericEgg` | 24 | — | `oresnowgolem` | — | `oresnowgolem` |
| `:5933` | `MyIronGolemSpawnBlock` | `OreGenericEgg` | 25 | — | `oreirongolem` | — | `oreirongolem` |
| `:5934` | `MyWitherBossSpawnBlock` | `OreGenericEgg` | 26 | — | `orewitherboss` | — | `orewitherboss` |
| `:5935` | `MyGirlfriendSpawnBlock` | `OreGenericEgg` | 27 | — | `oregirlfriend` | — | `oregirlfriend` |
| `:5936` | `MyBoyfriendSpawnBlock` | `OreGenericEgg` | 84 | — | `oreboyfriend` | — | `oreboyfriend` |
| `:5937` | `MyRedCowSpawnBlock` | `OreGenericEgg` | 28 | — | `oreredcow` | — | `oreredcow` |
| `:5938` | `MyCrystalCowSpawnBlock` | `OreGenericEgg` | 261 | — | `orecrystalcow` | — | `orecrystalcow` |
| `:5939` | `MyVillagerSpawnBlock` | `OreGenericEgg` | 94 | — | `orevillager` | — | `orevillager` |
| `:5940` | `MyGoldCowSpawnBlock` | `OreGenericEgg` | 29 | — | `oregoldcow` | — | `oregoldcow` |
| `:5941` | `MyEnchantedCowSpawnBlock` | `OreGenericEgg` | 30 | — | `oreenchantedcow` | — | `oreenchantedcow` |
| `:5942` | `MyMOTHRASpawnBlock` | `OreGenericEgg` | 31 | — | `oreMOTHRA` | — | `oremothra` |
| `:5943` | `MyAntBlock` | `AntBlock` | 115 | — | `AntBlock` | — | `antblock` |
| `:5944` | `MyRedAntBlock` | `AntBlock` | 116 | — | `RedAntBlock` | — | `redantblock` |
| `:5945` | `TermiteBlock` | `AntBlock` | 120 | — | `TermiteBlock` | — | `termiteblock` |
| `:5946` | `CrystalTermiteBlock` | `CrystalAntBlock` | 121 | — | `CrystalTermiteBlock` | — | `crystaltermiteblock` |
| `:5947` | `MyRainbowAntBlock` | `AntBlock` | 117 | — | `RainbowAntBlock` | — | `rainbowantblock` |
| `:5948` | `MyUnstableAntBlock` | `AntBlock` | 118 | — | `UnstableAntBlock` | — | `unstableantblock` |
| `:5949` | `MyAloSpawnBlock` | `OreGenericEgg` | 32 | — | `orealosaurus` | — | `orealosaurus` |
| `:5950` | `MyCryoSpawnBlock` | `OreGenericEgg` | 33 | — | `orecryolophosaurus` | — | `orecryolophosaurus` |
| `:5951` | `MyCamaSpawnBlock` | `OreGenericEgg` | 34 | — | `orecamarasaurus` | — | `orecamarasaurus` |
| `:5952` | `MyVeloSpawnBlock` | `OreGenericEgg` | 35 | — | `orevelocityraptor` | — | `orevelocityraptor` |
| `:5953` | `MyHydroSpawnBlock` | `OreGenericEgg` | 36 | — | `orehydrolisc` | — | `orehydrolisc` |
| `:5954` | `MyBasilSpawnBlock` | `OreGenericEgg` | 37 | — | `orebasilisc` | — | `orebasilisc` |
| `:5955` | `MyDragonflySpawnBlock` | `OreGenericEgg` | 38 | — | `oredragonfly` | — | `oredragonfly` |
| `:5956` | `MyEmperorScorpionSpawnBlock` | `OreGenericEgg` | 39 | — | `oreemperorscorpion` | — | `oreemperorscorpion` |
| `:5957` | `MyScorpionSpawnBlock` | `OreGenericEgg` | 40 | — | `orescorpion` | — | `orescorpion` |
| `:5958` | `MyCaveFisherSpawnBlock` | `OreGenericEgg` | 41 | — | `orecavefisher` | — | `orecavefisher` |
| `:5959` | `MySpyroSpawnBlock` | `OreGenericEgg` | 42 | — | `orespyro` | — | `orespyro` |
| `:5960` | `MyBaryonyxSpawnBlock` | `OreGenericEgg` | 43 | — | `orebaryonyx` | — | `orebaryonyx` |
| `:5961` | `MyGammaMetroidSpawnBlock` | `OreGenericEgg` | 44 | — | `oregammametroid` | — | `oregammametroid` |
| `:5962` | `MyCockateilSpawnBlock` | `OreGenericEgg` | 45 | — | `orecockateil` | — | `orecockateil` |
| `:5963` | `MyKyuubiSpawnBlock` | `OreGenericEgg` | 46 | — | `orekyuubi` | — | `orekyuubi` |
| `:5964` | `MyAlienSpawnBlock` | `OreGenericEgg` | 47 | — | `orealien` | — | `orealien` |
| `:5965` | `MyAttackSquidSpawnBlock` | `OreGenericEgg` | 48 | — | `oreattacksquid` | — | `oreattacksquid` |
| `:5966` | `MyWaterDragonSpawnBlock` | `OreGenericEgg` | 49 | — | `orewaterdragon` | — | `orewaterdragon` |
| `:5967` | `MyCephadromeSpawnBlock` | `OreGenericEgg` | 52 | — | `orecephadrome` | — | `orecephadrome` |
| `:5968` | `MyDragonSpawnBlock` | `OreGenericEgg` | 53 | — | `oredragon` | — | `oredragon` |
| `:5969` | `MyKrakenSpawnBlock` | `OreGenericEgg` | 50 | — | `orekraken` | — | `orekraken` |
| `:5970` | `MyLizardSpawnBlock` | `OreGenericEgg` | 51 | — | `orelizard` | — | `orelizard` |
| `:5971` | `MyBeeSpawnBlock` | `OreGenericEgg` | 54 | — | `orebee` | — | `orebee` |
| `:5972` | `MyHorseSpawnBlock` | `OreGenericEgg` | 55 | — | `orehorse` | — | `orehorse` |
| `:5973` | `MyTrooperBugSpawnBlock` | `OreGenericEgg` | 56 | — | `oretrooper` | — | `oretrooper` |
| `:5974` | `MySpitBugSpawnBlock` | `OreGenericEgg` | 57 | — | `orespit` | — | `orespit` |
| `:5975` | `MyStinkBugSpawnBlock` | `OreGenericEgg` | 58 | — | `orestink` | — | `orestink` |
| `:5976` | `MyOstrichSpawnBlock` | `OreGenericEgg` | 59 | — | `oreostrich` | — | `oreostrich` |
| `:5977` | `MyGazelleSpawnBlock` | `OreGenericEgg` | 60 | — | `oregazelle` | — | `oregazelle` |
| `:5978` | `MyChipmunkSpawnBlock` | `OreGenericEgg` | 61 | — | `orechipmunk` | — | `orechipmunk` |
| `:5979` | `MyCreepingHorrorSpawnBlock` | `OreGenericEgg` | 62 | — | `orecreepinghorror` | — | `orecreepinghorror` |
| `:5980` | `MyTerribleTerrorSpawnBlock` | `OreGenericEgg` | 63 | — | `oreterribleterror` | — | `oreterribleterror` |
| `:5981` | `MyCliffRacerSpawnBlock` | `OreGenericEgg` | 64 | — | `orecliffracer` | — | `orecliffracer` |
| `:5982` | `MyTriffidSpawnBlock` | `OreGenericEgg` | 65 | — | `oretriffid` | — | `oretriffid` |
| `:5983` | `MyPitchBlackSpawnBlock` | `OreGenericEgg` | 66 | — | `orenightmare` | — | `orenightmare` |
| `:5984` | `MyLurkingTerrorSpawnBlock` | `OreGenericEgg` | 67 | — | `orelurkingterror` | — | `orelurkingterror` |
| `:5985` | `MyGodzillaPartSpawnBlock` | `OreGenericEgg` | 68 | — | `oregodzillapart` | — | `oregodzillapart` |
| `:5986` | `MyGodzillaSpawnBlock` | `OreGenericEgg` | 119 | — | `oregodzilla` | — | `oregodzilla` |
| `:5987` | `MySmallWormSpawnBlock` | `OreGenericEgg` | 69 | — | `oresmallworm` | — | `oresmallworm` |
| `:5988` | `MyMediumWormSpawnBlock` | `OreGenericEgg` | 70 | — | `oremediumworm` | — | `oremediumworm` |
| `:5989` | `MyLargeWormSpawnBlock` | `OreGenericEgg` | 71 | — | `orelargeworm` | — | `orelargeworm` |
| `:5990` | `MyCassowarySpawnBlock` | `OreGenericEgg` | 72 | — | `orecassowary` | — | `orecassowary` |
| `:5991` | `MyCloudSharkSpawnBlock` | `OreGenericEgg` | 73 | — | `orecloudshark` | — | `orecloudshark` |
| `:5992` | `MyGoldFishSpawnBlock` | `OreGenericEgg` | 74 | — | `oregoldfish` | — | `oregoldfish` |
| `:5993` | `MyLeafMonsterSpawnBlock` | `OreGenericEgg` | 75 | — | `oreleafmonster` | — | `oreleafmonster` |
| `:5994` | `MyTshirtSpawnBlock` | `OreGenericEgg` | 76 | — | `oretshirt` | — | `oretshirt` |
| `:5995` | `MyEnderKnightSpawnBlock` | `OreGenericEgg` | 77 | — | `oreenderknight` | — | `oreenderknight` |
| `:5996` | `MyEnderReaperSpawnBlock` | `OreGenericEgg` | 78 | — | `oreenderreaper` | — | `oreenderreaper` |
| `:5997` | `MyBeaverSpawnBlock` | `OreGenericEgg` | 79 | — | `orebeaver` | — | `orebeaver` |
| `:5998` | `MyUrchinSpawnBlock` | `OreGenericEgg` | 250 | — | `oreurchin` | — | `oreurchin` |
| `:5999` | `MyFlounderSpawnBlock` | `OreGenericEgg` | 251 | — | `oreflounder` | — | `oreflounder` |
| `:6000` | `MySkateSpawnBlock` | `OreGenericEgg` | 252 | — | `oreskate` | — | `oreskate` |
| `:6001` | `MyRotatorSpawnBlock` | `OreGenericEgg` | 253 | — | `orerotator` | — | `orerotator` |
| `:6002` | `MyPeacockSpawnBlock` | `OreGenericEgg` | 254 | — | `orepeacock` | — | `orepeacock` |
| `:6003` | `MyFairySpawnBlock` | `OreGenericEgg` | 255 | — | `orefairy` | — | `orefairy` |
| `:6004` | `MyDungeonBeastSpawnBlock` | `OreGenericEgg` | 256 | — | `oredungeonbeast` | — | `oredungeonbeast` |
| `:6005` | `MyVortexSpawnBlock` | `OreGenericEgg` | 257 | — | `orevortex` | — | `orevortex` |
| `:6006` | `MyRatSpawnBlock` | `OreGenericEgg` | 258 | — | `orerat` | — | `orerat` |
| `:6007` | `MyWhaleSpawnBlock` | `OreGenericEgg` | 259 | — | `orewhale` | — | `orewhale` |
| `:6008` | `MyIrukandjiSpawnBlock` | `OreGenericEgg` | 260 | — | `oreirukandji` | — | `oreirukandji` |
| `:6009` | `MyTRexSpawnBlock` | `OreGenericEgg` | 80 | — | `oretrex` | — | `oretrex` |
| `:6010` | `MyHerculesSpawnBlock` | `OreGenericEgg` | 81 | — | `orehercules` | — | `orehercules` |
| `:6011` | `MyMantisSpawnBlock` | `OreGenericEgg` | 82 | — | `oremantis` | — | `oremantis` |
| `:6012` | `MyStinkySpawnBlock` | `OreGenericEgg` | 83 | — | `orestinky` | — | `orestinky` |
| `:6013` | `MyTheKingPartSpawnBlock` | `OreGenericEgg` | 85 | — | `orethekingpart` | — | `orethekingpart` |
| `:6014` | `MyTheKingSpawnBlock` | `OreGenericEgg` | 122 | — | `oretheking` | — | `oretheking` |
| `:6015` | `MyTheQueenPartSpawnBlock` | `OreGenericEgg` | 96 | — | `orethequeenpart` | — | `orethequeenpart` |
| `:6016` | `MyTheQueenSpawnBlock` | `OreGenericEgg` | 125 | — | `orethequeen` | — | `orethequeen` |
| `:6017` | `MyEasterBunnySpawnBlock` | `OreGenericEgg` | 86 | — | `oreeasterbunny` | — | `oreeasterbunny` |
| `:6018` | `MyCaterKillerSpawnBlock` | `OreGenericEgg` | 87 | — | `orecaterkiller` | — | `orecaterkiller` |
| `:6019` | `MyMolenoidSpawnBlock` | `OreGenericEgg` | 88 | — | `oremolenoid` | — | `oremolenoid` |
| `:6020` | `MySeaMonsterSpawnBlock` | `OreGenericEgg` | 89 | — | `oreseamonster` | — | `oreseamonster` |
| `:6021` | `MySeaViperSpawnBlock` | `OreGenericEgg` | 90 | — | `oreseaviper` | — | `oreseaviper` |
| `:6022` | `MyLeonSpawnBlock` | `OreGenericEgg` | 91 | — | `oreleon` | — | `oreleon` |
| `:6023` | `MyHammerheadSpawnBlock` | `OreGenericEgg` | 92 | — | `orehammerhead` | — | `orehammerhead` |
| `:6024` | `MyRubberDuckySpawnBlock` | `OreGenericEgg` | 93 | — | `orerubberducky` | — | `orerubberducky` |
| `:6025` | `MyCriminalSpawnBlock` | `OreGenericEgg` | 95 | — | `orecriminal` | — | `orecriminal` |
| `:6026` | `MyBrutalflySpawnBlock` | `OreGenericEgg` | 300 | — | `orebrutalfly` | — | `orebrutalfly` |
| `:6027` | `MyNastysaurusSpawnBlock` | `OreGenericEgg` | 301 | — | `orenastysaurus` | — | `orenastysaurus` |
| `:6028` | `MyPointysaurusSpawnBlock` | `OreGenericEgg` | 302 | — | `orepointysaurus` | — | `orepointysaurus` |
| `:6029` | `MyCricketSpawnBlock` | `OreGenericEgg` | 303 | — | `orecricket` | — | `orecricket` |
| `:6030` | `MyFrogSpawnBlock` | `OreGenericEgg` | 304 | — | `orefrog` | — | `orefrog` |
| `:6031` | `MySpiderDriverSpawnBlock` | `OreGenericEgg` | 305 | — | `orespiderdriver` | — | `orespiderdriver` |
| `:6032` | `MyCrabSpawnBlock` | `OreGenericEgg` | 306 | — | `orecrab` | — | `orecrab` |

### 3.4 Werkzeugmaterialien (`EnumHelper.addToolMaterial`)

Alle Materialien übernehmen `harvestlevel`, `maxuses`, `efficiency` (float), `damage` (float), `enchantability` aus dem jeweiligen `WeaponStats` (`OreSpawnMain.java:1292-1306`).

| ToolMaterial | Feld | Stats-Feld | Zeile | Items (Feld : Klasse : manifest-id, `setHarvestLevel`) |
|---|---|---|---|---|
| `ULTIMATE` | `toolULTIMATE` | `ultimate_stats` | `:1292` | `MyUltimateSword` : `UltimateSword` : `ultimatesword` `:1307`; `MyUltimatePickaxe` : `UltimatePickaxe` : `ultimatepickaxe` (`pickaxe`) `:1308`; `MyUltimateShovel` : `UltimateShovel` : `ultimateshovel` (`shovel`) `:1309`; `MyUltimateHoe` : `UltimateHoe` : `ultimatehoe` `:1310`; `MyUltimateAxe` : `UltimateAxe` : `ultimateaxe` (`axe`) `:1311` |
| `NIGHTMARE` | `toolNIGHTMARE` | `nightmare_stats` | `:1293` | `MyNightmareSword` : `NightmareSword` : `nightmaresword` `:1312` |
| `REALEMERALD` | `toolEMERALD` | `emerald_stats` | `:1294` | `MyEmeraldSword` : `EmeraldSword` : `emeraldsword` `:1320`; `MyEmeraldPickaxe` : `EmeraldPickaxe` : `emeraldpickaxe` `:1321`; `MyEmeraldShovel` : `EmeraldShovel` : `emeraldshovel` `:1322`; `MyEmeraldHoe` : `EmeraldHoe` : `emeraldhoe` `:1323`; `MyEmeraldAxe` : `EmeraldAxe` : `emeraldaxe` `:1324`; `MyExperienceSword` : `ExperienceSword` : `experiencesword` `:1325`; `MyPoisonSword` : `PoisonSword` : `poisonsword` `:1326`; `MyRatSword` : `RatSword` : `ratsword` `:1327`; `MyFairySword` : `FairySword` : `fairysword` `:1328`; `MyMantisClaw` : `MantisClaw` : `mantisclaw` `:1329`; `MyRoseSword` : `EmeraldSword` : `rosesword` `:1361` |
| `RUBY` | `toolRUBY` | `ruby_stats` | `:1295` | `MyRubySword` : `RubySword` : `rubysword` `:1331`; `MyRubyPickaxe` : `RubyPickaxe` : `rubypickaxe` (`pickaxe`) `:1332`; `MyRubyShovel` : `RubyShovel` : `rubyshovel` (`shovel`) `:1333`; `MyRubyHoe` : `RubyHoe` : `rubyhoe` `:1334`; `MyRubyAxe` : `RubyAxe` : `rubyaxe` (`axe`) `:1335` |
| `AMETHYST` | `toolAMETHYST` | `amethyst_stats` | `:1296` | `MyBigHammer` : `BigHammer` : `bighammer` `:1330`; `MyAmethystSword` : `AmethystSword` : `amethystsword` `:1336`; `MyAmethystPickaxe` : `AmethystPickaxe` : `amethystpickaxe` (`pickaxe`) `:1337`; `MyAmethystShovel` : `AmethystShovel` : `amethystshovel` (`shovel`) `:1338`; `MyAmethystHoe` : `AmethystHoe` : `amethysthoe` `:1339`; `MyAmethystAxe` : `AmethystAxe` : `amethystaxe` (`axe`) `:1340` |
| `BERTHA` | `toolBERTHA` | `bertha_stats` | `:1297` | `MyBertha` : `Bertha` : `berthasmall` `:1313`; `MySlice` : `Bertha` : `slicesmall` `:1314` |
| `CRYSTALWOOD` | `toolCRYSTALWOOD` | `crystalwood_stats` | `:1298` | `MyCrystalWoodSword` : `CrystalSword` : `crystalwoodsword` `:1341`; `MyCrystalWoodPickaxe` : `CrystalPickaxe` : `crystalwoodpickaxe` `:1342`; `MyCrystalWoodShovel` : `CrystalShovel` : `crystalwoodshovel` `:1343`; `MyCrystalWoodHoe` : `CrystalHoe` : `crystalwoodhoe` `:1344`; `MyCrystalWoodAxe` : `CrystalAxe` : `crystalwoodaxe` `:1345` |
| `CRYSTALSTONE` | `toolCRYSTALSTONE` | `crystalstone_stats` | `:1299` | `MyCrystalStoneSword` : `CrystalSword` : `crystalstonesword` `:1351`; `MyCrystalStonePickaxe` : `CrystalPickaxe` : `crystalstonepickaxe` `:1352`; `MyCrystalStoneShovel` : `CrystalShovel` : `crystalstoneshovel` `:1353`; `MyCrystalStoneHoe` : `CrystalHoe` : `crystalstonehoe` `:1354`; `MyCrystalStoneAxe` : `CrystalAxe` : `crystalstoneaxe` `:1355` |
| `CRYSTALPINK` | `toolCRYSTALPINK` | `crystalpink_stats` | `:1300` | `MyCrystalPinkSword` : `CrystalSword` : `crystalpinksword` `:1346`; `MyCrystalPinkPickaxe` : `CrystalPickaxe` : `crystalpinkpickaxe` `:1347`; `MyCrystalPinkShovel` : `CrystalShovel` : `crystalpinkshovel` `:1348`; `MyCrystalPinkHoe` : `CrystalHoe` : `crystalpinkhoe` `:1349`; `MyCrystalPinkAxe` : `CrystalAxe` : `crystalpinkaxe` `:1350` |
| `TIGERSEYE` | `toolTIGERSEYE` | `tigerseye_stats` | `:1301` | `MyTigersEyeSword` : `CrystalSword` : `tigerseye_sword` `:1356`; `MyTigersEyePickaxe` : `CrystalPickaxe` : `tigerseye_pickaxe` `:1357`; `MyTigersEyeShovel` : `CrystalShovel` : `tigerseye_shovel` `:1358`; `MyTigersEyeHoe` : `CrystalHoe` : `tigerseye_hoe` `:1359`; `MyTigersEyeAxe` : `CrystalAxe` : `tigerseye_axe` `:1360` |
| `ROYAL` | `toolROYAL` | `royal_stats` | `:1302` | `MyRoyal` : `Bertha` : `royalsmall` `:1315` |
| `HAMMY` | `toolHAMMY` | `hammy_stats` | `:1303` | `MyHammy` : `Bertha` : `hammysmall` `:1316` |
| `BATTLE` | `toolBATTLE` | `battleaxe_stats` | `:1304` | `MyBattleAxe` : `UltimateSword` : `battleaxesmall` `:1317` |
| `CHAINSAW` | `toolCHAINSAW` | `chainsaw_stats` | `:1305` | `MyChainsaw` : `UltimateSword` : `chainsawsmall` `:1318` |
| `QUEENBATTLE` | `toolQUEENBATTLE` | `queenbattleaxe_stats` | `:1306` | `MyQueenBattleAxe` : `UltimateSword` : `queenbattleaxesmall` `:1319` |

### 3.5 Rüstungsmaterialien (`EnumHelper.addArmorMaterial`)

Jedes Material: `durability`, `{head, chest, leg, boot}_protection`, `enchantability` aus `ArmorStats` (`OreSpawnMain.java:1432-1445`). Die Rüstungsitems werden als `new ItemOreSpawnArmor(BaseItemID + N, material, proxy.setArmorPrefix("<präfix>"), slot)` gebaut (`:1446-1501`); `setArmorPrefix` liefert serverseitig 0 (`CommonProxyOreSpawn.java:28-30`) und clientseitig `RenderingRegistry.addNewArmourRendererPrefix(string)` (`ClientProxyOreSpawn.java:177-180`). Die `e_*`-Verzauberungswerte aus `ArmorStats` liest `ItemOreSpawnArmor` (siehe Verbraucher in §2.3).

| ArmorMaterial | Feld | Stats-Feld | Zeile | Items (Slot 0-3 : Feld : manifest-id : Präfix) |
|---|---|---|---|---|
| `ULTIMATE` | `armorULTIMATE` | `Ultimate_armorstats` | `:1432` | 0 : `UltimateHelmet` : `ultimate_helmet` : `ultimate` `:1446`; 1 : `UltimateBody` : `ultimate_chest` : `ultimate` `:1447`; 2 : `UltimateLegs` : `ultimate_leggings` : `ultimate` `:1448`; 3 : `UltimateBoots` : `ultimate_boots` : `ultimate` `:1449` |
| `MOBZILLA` | `armorMOBZILLA` | `Mobzilla_armorstats` | `:1433` | 0 : `MobzillaHelmet` : `mobzilla_helmet` : `mobzilla` `:1486`; 1 : `MobzillaBody` : `mobzilla_chest` : `mobzilla` `:1487`; 2 : `MobzillaLegs` : `mobzilla_leggings` : `mobzilla` `:1488`; 3 : `MobzillaBoots` : `mobzilla_boots` : `mobzilla` `:1489` |
| `LAVAEEL` | `armorLAVAEEL` | `LavaEel_armorstats` | `:1434` | 0 : `LavaEelHelmet` : `lavaeel_helmet` : `lavaeel` `:1450`; 1 : `LavaEelBody` : `lavaeel_chest` : `lavaeel` `:1451`; 2 : `LavaEelLegs` : `lavaeel_leggings` : `lavaeel` `:1452`; 3 : `LavaEelBoots` : `lavaeel_boots` : `lavaeel` `:1453` |
| `MOTHSCALE` | `armorMOTHSCALE` | `MothScale_armorstats` | `:1435` | 0 : `MothScaleHelmet` : `mothscale_helmet` : `mothscale` `:1454`; 1 : `MothScaleBody` : `mothscale_chest` : `mothscale` `:1455`; 2 : `MothScaleLegs` : `mothscale_leggings` : `mothscale` `:1456`; 3 : `MothScaleBoots` : `mothscale_boots` : `mothscale` `:1457` |
| `EMERALD` | `armorEMERALD` | `Emerald_armorstats` | `:1436` | 0 : `EmeraldHelmet` : `emerald_helmet` : `emerald` `:1458`; 1 : `EmeraldBody` : `emerald_chest` : `emerald` `:1459`; 2 : `EmeraldLegs` : `emerald_leggings` : `emerald` `:1460`; 3 : `EmeraldBoots` : `emerald_boots` : `emerald` `:1461` |
| `EXPERIENCE` | `armorEXPERIENCE` | `Experience_armorstats` | `:1437` | 0 : `ExperienceHelmet` : `experience_helmet` : `experience` `:1462`; 1 : `ExperienceBody` : `experience_chest` : `experience` `:1463`; 2 : `ExperienceLegs` : `experience_leggings` : `experience` `:1464`; 3 : `ExperienceBoots` : `experience_boots` : `experience` `:1465` |
| `RUBY` | `armorRUBY` | `Ruby_armorstats` | `:1438` | 0 : `RubyHelmet` : `ruby_helmet` : `ruby` `:1466`; 1 : `RubyBody` : `ruby_chest` : `ruby` `:1467`; 2 : `RubyLegs` : `ruby_leggings` : `ruby` `:1468`; 3 : `RubyBoots` : `ruby_boots` : `ruby` `:1469` |
| `AMETHYST` | `armorAMETHYST` | `Amethyst_armorstats` | `:1439` | 0 : `AmethystHelmet` : `amethyst_helmet` : `amethyst` `:1470`; 1 : `AmethystBody` : `amethyst_chest` : `amethyst` `:1471`; 2 : `AmethystLegs` : `amethyst_leggings` : `amethyst` `:1472`; 3 : `AmethystBoots` : `amethyst_boots` : `amethyst` `:1473` |
| `PINK` | `armorPINK` | `Pink_armorstats` | `:1440` | 0 : `CrystalPinkHelmet` : `pink_helmet` : `pink` `:1474`; 1 : `CrystalPinkBody` : `pink_chest` : `pink` `:1475`; 2 : `CrystalPinkLegs` : `pink_leggings` : `pink` `:1476`; 3 : `CrystalPinkBoots` : `pink_boots` : `pink` `:1477` |
| `TIGERSEYE` | `armorTIGERSEYE` | `TigersEye_armorstats` | `:1441` | 0 : `TigersEyeHelmet` : `tigerseye_helmet` : `tigerseye` `:1478`; 1 : `TigersEyeBody` : `tigerseye_chest` : `tigerseye` `:1479`; 2 : `TigersEyeLegs` : `tigerseye_leggings` : `tigerseye` `:1480`; 3 : `TigersEyeBoots` : `tigerseye_boots` : `tigerseye` `:1481` |
| `PEACOCK` | `armorPEACOCK` | `Peacock_armorstats` | `:1442` | 3 : `PeacockFeatherBoots` : `peacock_boots` : `peacock` `:1482`; 0 : `PeacockFeatherHelmet` : `peacock_helmet` : `peacock` `:1483`; 1 : `PeacockFeatherBody` : `peacock_chest` : `peacock` `:1484`; 2 : `PeacockFeatherLegs` : `peacock_leggings` : `peacock` `:1485` |
| `ROYAL` | `armorROYAL` | `Royal_armorstats` | `:1443` | 0 : `RoyalHelmet` : `royal_helmet` : `royal` `:1490`; 1 : `RoyalBody` : `royal_chest` : `royal` `:1491`; 2 : `RoyalLegs` : `royal_leggings` : `royal` `:1492`; 3 : `RoyalBoots` : `royal_boots` : `royal` `:1493` |
| `LAPIS` | `armorLAPIS` | `Lapis_armorstats` | `:1444` | 0 : `LapisHelmet` : `lapis_helmet` : `lapis` `:1494`; 1 : `LapisBody` : `lapis_chest` : `lapis` `:1495`; 2 : `LapisLegs` : `lapis_leggings` : `lapis` `:1496`; 3 : `LapisBoots` : `lapis_boots` : `lapis` `:1497` |
| `QUEEN` | `armorQUEEN` | `Queen_armorstats` | `:1445` | 0 : `QueenHelmet` : `queen_helmet` : `queen` `:1498`; 1 : `QueenBody` : `queen_chest` : `queen` `:1499`; 2 : `QueenLegs` : `queen_leggings` : `queen` `:1500`; 3 : `QueenBoots` : `queen_boots` : `queen` `:1501` |

### 3.6 Käfige und Spawn-Eier (`initializeCagesAndEggs`, `OreSpawnMain.java:5068-5297`)

114× `CritterCage(BaseItemID + N, arg2)` und 114× `ItemSpawnEgg(BaseItemID + N, arg2)`. offen: Bedeutung von `arg2` (vermutlich Textur- oder Entity-Index) steht in `CritterCage`/`ItemSpawnEgg`, nicht hier. `Elevator` (Hoverboard-Item) steht in der Registrierungsliste mitten zwischen den Käfigen (§3.2), wird aber bei `:1566` konstruiert.

| Zeile | Feld | Klasse | BaseItemID + | arg2 | unlocalized | manifest-id |
|---|---|---|---|---|---|---|
| `:5069` | `CageEmpty` | `CritterCage` | 0 | 160 | `cageempty` | `cageempty` |
| `:5070` | `CagedSpider` | `CritterCage` | 1 | 161 | `cagespider` | `cagespider` |
| `:5071` | `CagedBat` | `CritterCage` | 2 | 162 | `cagebat` | `cagebat` |
| `:5072` | `CagedCow` | `CritterCage` | 3 | 163 | `cagecow` | `cagecow` |
| `:5073` | `CagedPig` | `CritterCage` | 4 | 164 | `cagepig` | `cagepig` |
| `:5074` | `CagedSquid` | `CritterCage` | 5 | 165 | `cagesquid` | `cagesquid` |
| `:5075` | `CagedChicken` | `CritterCage` | 6 | 166 | `cagechicken` | `cagechicken` |
| `:5076` | `CagedCreeper` | `CritterCage` | 7 | 167 | `cagecreeper` | `cagecreeper` |
| `:5077` | `CagedSkeleton` | `CritterCage` | 8 | 168 | `cageskeleton` | `cageskeleton` |
| `:5078` | `CagedZombie` | `CritterCage` | 9 | 169 | `cagezombie` | `cagezombie` |
| `:5079` | `CagedSlime` | `CritterCage` | 10 | 170 | `cageslime` | `cageslime` |
| `:5080` | `CagedGhast` | `CritterCage` | 11 | 171 | `cageghast` | `cageghast` |
| `:5081` | `CagedZombiePigman` | `CritterCage` | 12 | 172 | `cagezombiepigman` | `cagezombiepigman` |
| `:5082` | `CagedEnderman` | `CritterCage` | 13 | 173 | `cageenderman` | `cageenderman` |
| `:5083` | `CagedCaveSpider` | `CritterCage` | 14 | 174 | `cagecavespider` | `cagecavespider` |
| `:5084` | `CagedSilverfish` | `CritterCage` | 15 | 175 | `cagesilverfish` | `cagesilverfish` |
| `:5085` | `CagedMagmaCube` | `CritterCage` | 16 | 176 | `cagemagmacube` | `cagemagmacube` |
| `:5086` | `CagedWitch` | `CritterCage` | 17 | 177 | `cagewitch` | `cagewitch` |
| `:5087` | `CagedSheep` | `CritterCage` | 18 | 178 | `cagesheep` | `cagesheep` |
| `:5088` | `CagedWolf` | `CritterCage` | 19 | 179 | `cagewolf` | `cagewolf` |
| `:5089` | `CagedMooshroom` | `CritterCage` | 20 | 180 | `cagemooshroom` | `cagemooshroom` |
| `:5090` | `CagedOcelot` | `CritterCage` | 21 | 181 | `cageocelot` | `cageocelot` |
| `:5091` | `CagedBlaze` | `CritterCage` | 22 | 182 | `cageblaze` | `cageblaze` |
| `:5092` | `CagedGirlfriend` | `CritterCage` | 23 | 183 | `cagegirlfriend` | `cagegirlfriend` |
| `:5093` | `CagedBoyfriend` | `CritterCage` | 95 | 215 | `cageboyfriend` | `cageboyfriend` |
| `:5094` | `CagedWitherSkeleton` | `CritterCage` | 24 | 188 | `cagewitherskeleton` | `cagewitherskeleton` |
| `:5095` | `CagedEnderDragon` | `CritterCage` | 25 | 184 | `cageenderdragon` | `cageenderdragon` |
| `:5096` | `CagedSnowGolem` | `CritterCage` | 26 | 185 | `cagesnowgolem` | `cagesnowgolem` |
| `:5097` | `CagedIronGolem` | `CritterCage` | 27 | 186 | `cageirongolem` | `cageirongolem` |
| `:5098` | `CagedWitherBoss` | `CritterCage` | 28 | 187 | `cagewitherboss` | `cagewitherboss` |
| `:5099` | `CagedRedCow` | `CritterCage` | 29 | 189 | `cageredcow` | `cageredcow` |
| `:5100` | `CagedGoldCow` | `CritterCage` | 30 | 190 | `cagegoldcow` | `cagegoldcow` |
| `:5101` | `CagedEnchantedCow` | `CritterCage` | 31 | 191 | `cageenchantedcow` | `cageenchantedcow` |
| `:5102` | `CagedMOTHRA` | `CritterCage` | 32 | 208 | `cageMOTHRA` | `cagemothra` |
| `:5103` | `CagedAlo` | `CritterCage` | 33 | 209 | `cagealosaurus` | `cagealosaurus` |
| `:5104` | `CagedCryo` | `CritterCage` | 34 | 210 | `cagecryolophosaurus` | `cagecryolophosaurus` |
| `:5105` | `CagedCama` | `CritterCage` | 35 | 211 | `cagecamarasaurus` | `cagecamarasaurus` |
| `:5106` | `CagedVelo` | `CritterCage` | 36 | 212 | `cagevelocityraptor` | `cagevelocityraptor` |
| `:5107` | `CagedHydro` | `CritterCage` | 37 | 213 | `cagehydrolisc` | `cagehydrolisc` |
| `:5108` | `CagedBasil` | `CritterCage` | 38 | 214 | `cagebasilisc` | `cagebasilisc` |
| `:5109` | `CagedDragonfly` | `CritterCage` | 39 | 220 | `cagedragonfly` | `cagedragonfly` |
| `:5110` | `CagedEmperorScorpion` | `CritterCage` | 41 | 222 | `cageemperorscorpion` | `cageemperorscorpion` |
| `:5111` | `CagedScorpion` | `CritterCage` | 40 | 224 | `cagescorpion` | `cagescorpion` |
| `:5112` | `CagedCaveFisher` | `CritterCage` | 45 | 226 | `cagecavefisher` | `cagecavefisher` |
| `:5113` | `CagedSpyro` | `CritterCage` | 42 | 228 | `cagespyro` | `cagespyro` |
| `:5114` | `CagedBaryonyx` | `CritterCage` | 43 | 230 | `cagebaryonyx` | `cagebaryonyx` |
| `:5115` | `CagedGammaMetroid` | `CritterCage` | 44 | 232 | `cagegammametroid` | `cagegammametroid` |
| `:5116` | `CagedCockateil` | `CritterCage` | 46 | 234 | `cagecockateil` | `cagecockateil` |
| `:5117` | `CagedKyuubi` | `CritterCage` | 47 | 236 | `cagekyuubi` | `cagekyuubi` |
| `:5118` | `CagedAlien` | `CritterCage` | 48 | 238 | `cagealien` | `cagealien` |
| `:5119` | `CagedAttackSquid` | `CritterCage` | 49 | 240 | `cageattacksquid` | `cageattacksquid` |
| `:5120` | `CagedWaterDragon` | `CritterCage` | 50 | 242 | `cagewaterdragon` | `cagewaterdragon` |
| `:5121` | `CagedCephadrome` | `CritterCage` | 53 | 248 | `cagecephadrome` | `cagecephadrome` |
| `:5122` | `CagedKraken` | `CritterCage` | 51 | 244 | `cagekraken` | `cagekraken` |
| `:5123` | `CagedLizard` | `CritterCage` | 52 | 246 | `cagelizard` | `cagelizard` |
| `:5124` | `CagedDragon` | `CritterCage` | 54 | 250 | `cagedragon` | `cagedragon` |
| `:5125` | `CagedBee` | `CritterCage` | 55 | 252 | `cagebee` | `cagebee` |
| `:5126` | `CagedHorse` | `CritterCage` | 56 | 253 | `cagehorse` | `cagehorse` |
| `:5127` | `CagedFirefly` | `CritterCage` | 57 | 255 | `cagefirefly` | `cagefirefly` |
| `:5128` | `CagedChipmunk` | `CritterCage` | 58 | 256 | `cagechipmunk` | `cagechipmunk` |
| `:5129` | `CagedGazelle` | `CritterCage` | 59 | 257 | `cagegazelle` | `cagegazelle` |
| `:5130` | `CagedOstrich` | `CritterCage` | 60 | 258 | `cageostrich` | `cageostrich` |
| `:5131` | `CagedTrooper` | `CritterCage` | 61 | 259 | `cagetrooper` | `cagetrooper` |
| `:5132` | `CagedSpit` | `CritterCage` | 62 | 260 | `cagespit` | `cagespit` |
| `:5133` | `CagedStink` | `CritterCage` | 63 | 261 | `cagestink` | `cagestink` |
| `:5134` | `CagedCreepingHorror` | `CritterCage` | 64 | 268 | `cagecreepinghorror` | `cagecreepinghorror` |
| `:5135` | `CagedTerribleTerror` | `CritterCage` | 65 | 269 | `cageterribleterror` | `cageterribleterror` |
| `:5136` | `CagedCliffRacer` | `CritterCage` | 66 | 270 | `cagecliffracer` | `cagecliffracer` |
| `:5137` | `CagedTriffid` | `CritterCage` | 67 | 271 | `cagetriffid` | `cagetriffid` |
| `:5138` | `CagedPitchBlack` | `CritterCage` | 68 | 272 | `cagenightmare` | `cagenightmare` |
| `:5139` | `CagedLurkingTerror` | `CritterCage` | 69 | 273 | `cagelurkingterror` | `cagelurkingterror` |
| `:5140` | `CagedSmallWorm` | `CritterCage` | 70 | 281 | `cagesmallworm` | `cagesmallworm` |
| `:5141` | `CagedMediumWorm` | `CritterCage` | 71 | 282 | `cagemediumworm` | `cagemediumworm` |
| `:5142` | `CagedLargeWorm` | `CritterCage` | 72 | 283 | `cagelargeworm` | `cagelargeworm` |
| `:5143` | `CagedCassowary` | `CritterCage` | 73 | 284 | `cagecassowary` | `cagecassowary` |
| `:5144` | `CagedCloudShark` | `CritterCage` | 74 | 285 | `cagecloudshark` | `cagecloudshark` |
| `:5145` | `CagedGoldFish` | `CritterCage` | 75 | 286 | `cagegoldfish` | `cagegoldfish` |
| `:5146` | `CagedLeafMonster` | `CritterCage` | 76 | 287 | `cageleafmonster` | `cageleafmonster` |
| `:5147` | `CagedEnderKnight` | `CritterCage` | 77 | 296 | `cageenderknight` | `cageenderknight` |
| `:5148` | `CagedEnderReaper` | `CritterCage` | 78 | 297 | `cageenderreaper` | `cageenderreaper` |
| `:5149` | `CagedBeaver` | `CritterCage` | 79 | 300 | `cagebeaver` | `cagebeaver` |
| `:5150` | `CagedUrchin` | `CritterCage` | 80 | 323 | `cageurchin` | `cageurchin` |
| `:5151` | `CagedFlounder` | `CritterCage` | 81 | 319 | `cageflounder` | `cageflounder` |
| `:5152` | `CagedSkate` | `CritterCage` | 82 | 322 | `cageskate` | `cageskate` |
| `:5153` | `CagedRotator` | `CritterCage` | 83 | 313 | `cagerotator` | `cagerotator` |
| `:5154` | `CagedPeacock` | `CritterCage` | 84 | 315 | `cagepeacock` | `cagepeacock` |
| `:5155` | `CagedFairy` | `CritterCage` | 85 | 316 | `cagefairy` | `cagefairy` |
| `:5156` | `CagedDungeonBeast` | `CritterCage` | 86 | 317 | `cagedungeonbeast` | `cagedungeonbeast` |
| `:5157` | `CagedVortex` | `CritterCage` | 87 | 314 | `cagevortex` | `cagevortex` |
| `:5158` | `CagedRat` | `CritterCage` | 88 | 318 | `cagerat` | `cagerat` |
| `:5159` | `CagedWhale` | `CritterCage` | 89 | 320 | `cagewhale` | `cagewhale` |
| `:5160` | `CagedIrukandji` | `CritterCage` | 90 | 321 | `cageirukandji` | `cageirukandji` |
| `:5161` | `CagedTRex` | `CritterCage` | 91 | 345 | `cagetrex` | `cagetrex` |
| `:5162` | `CagedHercules` | `CritterCage` | 92 | 346 | `cagehercules` | `cagehercules` |
| `:5163` | `CagedMantis` | `CritterCage` | 93 | 347 | `cagemantis` | `cagemantis` |
| `:5164` | `CagedStinky` | `CritterCage` | 94 | 348 | `cagestinky` | `cagestinky` |
| `:5165` | `CagedEasterBunny` | `CritterCage` | 96 | 150 | `cageeasterbunny` | `cageeasterbunny` |
| `:5166` | `CagedCaterKiller` | `CritterCage` | 97 | 151 | `cagecaterkiller` | `cagecaterkiller` |
| `:5167` | `CagedMolenoid` | `CritterCage` | 98 | 152 | `cagemolenoid` | `cagemolenoid` |
| `:5168` | `CagedSeaMonster` | `CritterCage` | 99 | 153 | `cageseamonster` | `cageseamonster` |
| `:5169` | `CagedSeaViper` | `CritterCage` | 174 | 154 | `cageseaviper` | `cageseaviper` |
| `:5170` | `CagedLeon` | `CritterCage` | 423 | 357 | `cageleon` | `cageleon` |
| `:5171` | `CagedHammerhead` | `CritterCage` | 425 | 359 | `cagehammerhead` | `cagehammerhead` |
| `:5172` | `CagedRubberDucky` | `CritterCage` | 427 | 361 | `cagerubberducky` | `cagerubberducky` |
| `:5173` | `CagedCrystalCow` | `CritterCage` | 429 | 216 | `cagecrystalcow` | `cagecrystalcow` |
| `:5174` | `CagedVillager` | `CritterCage` | 430 | 217 | `cagevillager` | `cagevillager` |
| `:5175` | `CagedCriminal` | `CritterCage` | 433 | 218 | `cagecriminal` | `cagecriminal` |
| `:5176` | `CagedBrutalfly` | `CritterCage` | 465 | 373 | `cagebrutalfly` | `cagebrutalfly` |
| `:5177` | `CagedNastysaurus` | `CritterCage` | 466 | 374 | `cagenastysaurus` | `cagenastysaurus` |
| `:5178` | `CagedPointysaurus` | `CritterCage` | 467 | 375 | `cagepointysaurus` | `cagepointysaurus` |
| `:5179` | `CagedCricket` | `CritterCage` | 468 | 376 | `cagecricket` | `cagecricket` |
| `:5180` | `CagedFrog` | `CritterCage` | 469 | 377 | `cagefrog` | `cagefrog` |
| `:5181` | `CagedSpiderDriver` | `CritterCage` | 478 | 382 | `cagespiderdriver` | `cagespiderdriver` |
| `:5182` | `CagedCrab` | `CritterCage` | 483 | 384 | `cagecrab` | `cagecrab` |
| `:5183` | `WitherSkeletonEgg` | `ItemSpawnEgg` | 100 | 192 | `eggwitherskeleton` | `eggwitherskeleton` |
| `:5184` | `EnderDragonEgg` | `ItemSpawnEgg` | 101 | 193 | `eggenderdragon` | `eggenderdragon` |
| `:5185` | `SnowGolemEgg` | `ItemSpawnEgg` | 102 | 194 | `eggsnowgolem` | `eggsnowgolem` |
| `:5186` | `IronGolemEgg` | `ItemSpawnEgg` | 103 | 195 | `eggirongolem` | `eggirongolem` |
| `:5187` | `WitherBossEgg` | `ItemSpawnEgg` | 104 | 196 | `eggwitherboss` | `eggwitherboss` |
| `:5188` | `GirlfriendEgg` | `ItemSpawnEgg` | 105 | 197 | `egggirlfriend` | `egggirlfriend` |
| `:5189` | `RedCowEgg` | `ItemSpawnEgg` | 106 | 198 | `eggredcow` | `eggredcow` |
| `:5190` | `CrystalCowEgg` | `ItemSpawnEgg` | 431 | 363 | `eggcrystalcow` | `eggcrystalcow` |
| `:5191` | `GoldCowEgg` | `ItemSpawnEgg` | 107 | 199 | `egggoldcow` | `egggoldcow` |
| `:5192` | `EnchantedCowEgg` | `ItemSpawnEgg` | 108 | 200 | `eggenchantedcow` | `eggenchantedcow` |
| `:5193` | `MOTHRAEgg` | `ItemSpawnEgg` | 109 | 201 | `eggMOTHRA` | `eggmothra` |
| `:5194` | `AloEgg` | `ItemSpawnEgg` | 110 | 202 | `eggalosaurus` | `eggalosaurus` |
| `:5195` | `CryoEgg` | `ItemSpawnEgg` | 111 | 203 | `eggcryolophosaurus` | `eggcryolophosaurus` |
| `:5196` | `CamaEgg` | `ItemSpawnEgg` | 112 | 204 | `eggcamarasaurus` | `eggcamarasaurus` |
| `:5197` | `VeloEgg` | `ItemSpawnEgg` | 113 | 205 | `eggvelocityraptor` | `eggvelocityraptor` |
| `:5198` | `HydroEgg` | `ItemSpawnEgg` | 114 | 206 | `egghydrolisc` | `egghydrolisc` |
| `:5199` | `BasilEgg` | `ItemSpawnEgg` | 115 | 207 | `eggbasilisc` | `eggbasilisc` |
| `:5200` | `DragonflyEgg` | `ItemSpawnEgg` | 116 | 221 | `eggdragonfly` | `eggdragonfly` |
| `:5201` | `EmperorScorpionEgg` | `ItemSpawnEgg` | 117 | 223 | `eggemperorscorpion` | `eggemperorscorpion` |
| `:5202` | `ScorpionEgg` | `ItemSpawnEgg` | 118 | 225 | `eggscorpion` | `eggscorpion` |
| `:5203` | `CaveFisherEgg` | `ItemSpawnEgg` | 119 | 227 | `eggcavefisher` | `eggcavefisher` |
| `:5204` | `SpyroEgg` | `ItemSpawnEgg` | 120 | 229 | `eggspyro` | `eggspyro` |
| `:5205` | `BaryonyxEgg` | `ItemSpawnEgg` | 121 | 231 | `eggbaryonyx` | `eggbaryonyx` |
| `:5206` | `GammaMetroidEgg` | `ItemSpawnEgg` | 122 | 233 | `egggammametroid` | `egggammametroid` |
| `:5207` | `CockateilEgg` | `ItemSpawnEgg` | 123 | 235 | `eggcockateil` | `eggcockateil` |
| `:5208` | `KyuubiEgg` | `ItemSpawnEgg` | 124 | 237 | `eggkyuubi` | `eggkyuubi` |
| `:5209` | `AlienEgg` | `ItemSpawnEgg` | 125 | 239 | `eggalien` | `eggalien` |
| `:5210` | `AttackSquidEgg` | `ItemSpawnEgg` | 126 | 241 | `eggattacksquid` | `eggattacksquid` |
| `:5211` | `WaterDragonEgg` | `ItemSpawnEgg` | 127 | 243 | `eggwaterdragon` | `eggwaterdragon` |
| `:5212` | `CephadromeEgg` | `ItemSpawnEgg` | 130 | 249 | `eggcephadrome` | `eggcephadrome` |
| `:5213` | `KrakenEgg` | `ItemSpawnEgg` | 128 | 245 | `eggkraken` | `eggkraken` |
| `:5214` | `LizardEgg` | `ItemSpawnEgg` | 129 | 247 | `egglizard` | `egglizard` |
| `:5215` | `DragonEgg` | `ItemSpawnEgg` | 131 | 251 | `eggdragon` | `eggdragon` |
| `:5216` | `BeeEgg` | `ItemSpawnEgg` | 132 | 254 | `eggbee` | `eggbee` |
| `:5217` | `TrooperBugEgg` | `ItemSpawnEgg` | 133 | 262 | `eggtrooper` | `eggtrooper` |
| `:5218` | `SpitBugEgg` | `ItemSpawnEgg` | 134 | 263 | `eggspit` | `eggspit` |
| `:5219` | `StinkBugEgg` | `ItemSpawnEgg` | 135 | 264 | `eggstink` | `eggstink` |
| `:5220` | `OstrichEgg` | `ItemSpawnEgg` | 136 | 265 | `eggostrich` | `eggostrich` |
| `:5221` | `GazelleEgg` | `ItemSpawnEgg` | 137 | 266 | `egggazelle` | `egggazelle` |
| `:5222` | `ChipmunkEgg` | `ItemSpawnEgg` | 138 | 267 | `eggchipmunk` | `eggchipmunk` |
| `:5223` | `CreepingHorrorEgg` | `ItemSpawnEgg` | 139 | 274 | `eggcreepinghorror` | `eggcreepinghorror` |
| `:5224` | `TerribleTerrorEgg` | `ItemSpawnEgg` | 140 | 275 | `eggterribleterror` | `eggterribleterror` |
| `:5225` | `CliffRacerEgg` | `ItemSpawnEgg` | 141 | 276 | `eggcliffracer` | `eggcliffracer` |
| `:5226` | `TriffidEgg` | `ItemSpawnEgg` | 142 | 277 | `eggtriffid` | `eggtriffid` |
| `:5227` | `PitchBlackEgg` | `ItemSpawnEgg` | 143 | 278 | `eggnightmare` | `eggnightmare` |
| `:5228` | `LurkingTerrorEgg` | `ItemSpawnEgg` | 144 | 279 | `egglurkingterror` | `egglurkingterror` |
| `:5229` | `GodzillaEgg` | `ItemSpawnEgg` | 145 | 280 | `egggodzilla` | `egggodzilla` |
| `:5230` | `SmallWormEgg` | `ItemSpawnEgg` | 146 | 288 | `eggsmallworm` | `eggsmallworm` |
| `:5231` | `MediumWormEgg` | `ItemSpawnEgg` | 147 | 289 | `eggmediumworm` | `eggmediumworm` |
| `:5232` | `LargeWormEgg` | `ItemSpawnEgg` | 148 | 290 | `egglargeworm` | `egglargeworm` |
| `:5233` | `CassowaryEgg` | `ItemSpawnEgg` | 149 | 291 | `eggcassowary` | `eggcassowary` |
| `:5234` | `CloudSharkEgg` | `ItemSpawnEgg` | 165 | 292 | `eggcloudshark` | `eggcloudshark` |
| `:5235` | `GoldFishEgg` | `ItemSpawnEgg` | 166 | 293 | `egggoldfish` | `egggoldfish` |
| `:5236` | `LeafMonsterEgg` | `ItemSpawnEgg` | 167 | 294 | `eggleafmonster` | `eggleafmonster` |
| `:5237` | `TshirtEgg` | `ItemSpawnEgg` | 168 | 295 | `eggtshirt` | `eggtshirt` |
| `:5238` | `EnderKnightEgg` | `ItemSpawnEgg` | 169 | 298 | `eggenderknight` | `eggenderknight` |
| `:5239` | `EnderReaperEgg` | `ItemSpawnEgg` | 170 | 299 | `eggenderreaper` | `eggenderreaper` |
| `:5240` | `BeaverEgg` | `ItemSpawnEgg` | 171 | 301 | `eggbeaver` | `eggbeaver` |
| `:5241` | `RotatorEgg` | `ItemSpawnEgg` | 219 | 302 | `eggrotator` | `eggrotator` |
| `:5242` | `VortexEgg` | `ItemSpawnEgg` | 223 | 303 | `eggvortex` | `eggvortex` |
| `:5243` | `PeacockEgg` | `ItemSpawnEgg` | 220 | 304 | `eggpeacock` | `eggpeacock` |
| `:5244` | `FairyEgg` | `ItemSpawnEgg` | 221 | 305 | `eggfairy` | `eggfairy` |
| `:5245` | `DungeonBeastEgg` | `ItemSpawnEgg` | 222 | 306 | `eggdungeonbeast` | `eggdungeonbeast` |
| `:5246` | `RatEgg` | `ItemSpawnEgg` | 374 | 307 | `eggrat` | `eggrat` |
| `:5247` | `FlounderEgg` | `ItemSpawnEgg` | 375 | 308 | `eggflounder` | `eggflounder` |
| `:5248` | `WhaleEgg` | `ItemSpawnEgg` | 376 | 309 | `eggwhale` | `eggwhale` |
| `:5249` | `IrukandjiEgg` | `ItemSpawnEgg` | 377 | 310 | `eggirukandji` | `eggirukandji` |
| `:5250` | `SkateEgg` | `ItemSpawnEgg` | 378 | 311 | `eggskate` | `eggskate` |
| `:5251` | `UrchinEgg` | `ItemSpawnEgg` | 379 | 312 | `eggurchin` | `eggurchin` |
| `:5252` | `Robot1Egg` | `ItemSpawnEgg` | 380 | 324 | `eggrobot1` | `eggrobot1` |
| `:5253` | `Robot2Egg` | `ItemSpawnEgg` | 381 | 325 | `eggrobot2` | `eggrobot2` |
| `:5254` | `Robot3Egg` | `ItemSpawnEgg` | 382 | 326 | `eggrobot3` | `eggrobot3` |
| `:5255` | `Robot4Egg` | `ItemSpawnEgg` | 383 | 327 | `eggrobot4` | `eggrobot4` |
| `:5256` | `GhostEgg` | `ItemSpawnEgg` | 384 | 328 | `eggghost` | `eggghost` |
| `:5257` | `GhostSkellyEgg` | `ItemSpawnEgg` | 385 | 329 | `eggghostskelly` | `eggghostskelly` |
| `:5258` | `BrownAntEgg` | `ItemSpawnEgg` | 386 | 330 | `eggbrownant` | `eggbrownant` |
| `:5259` | `RedAntEgg` | `ItemSpawnEgg` | 387 | 331 | `eggredant` | `eggredant` |
| `:5260` | `RainbowAntEgg` | `ItemSpawnEgg` | 388 | 332 | `eggrainbowant` | `eggrainbowant` |
| `:5261` | `UnstableAntEgg` | `ItemSpawnEgg` | 389 | 333 | `eggunstableant` | `eggunstableant` |
| `:5262` | `TermiteEgg` | `ItemSpawnEgg` | 390 | 334 | `eggtermite` | `eggtermite` |
| `:5263` | `ButterflyEgg` | `ItemSpawnEgg` | 391 | 335 | `eggbutterfly` | `eggbutterfly` |
| `:5264` | `MothEgg` | `ItemSpawnEgg` | 392 | 336 | `eggmoth` | `eggmoth` |
| `:5265` | `MosquitoEgg` | `ItemSpawnEgg` | 393 | 337 | `eggmosquito` | `eggmosquito` |
| `:5266` | `FireflyEgg` | `ItemSpawnEgg` | 394 | 338 | `eggfirefly` | `eggfirefly` |
| `:5267` | `TRexEgg` | `ItemSpawnEgg` | 225 | 339 | `eggtrex` | `eggtrex` |
| `:5268` | `HerculesEgg` | `ItemSpawnEgg` | 226 | 340 | `egghercules` | `egghercules` |
| `:5269` | `MantisEgg` | `ItemSpawnEgg` | 227 | 341 | `eggmantis` | `eggmantis` |
| `:5270` | `StinkyEgg` | `ItemSpawnEgg` | 228 | 342 | `eggstinky` | `eggstinky` |
| `:5271` | `Robot5Egg` | `ItemSpawnEgg` | 172 | 343 | `eggrobot5` | `eggrobot5` |
| `:5272` | `CoinEgg` | `ItemSpawnEgg` | 173 | 344 | `eggcoin` | `eggcoin` |
| `:5273` | `BoyfriendEgg` | `ItemSpawnEgg` | 399 | 349 | `eggboyfriend` | `eggboyfriend` |
| `:5274` | `TheKingEgg` | `ItemSpawnEgg` | 400 | 350 | `eggtheking` | `eggtheking` |
| `:5275` | `TheQueenEgg` | `ItemSpawnEgg` | 448 | 366 | `eggthequeen` | `eggthequeen` |
| `:5276` | `ThePrinceEgg` | `ItemSpawnEgg` | 401 | 351 | `eggtheprince` | `eggtheprince` |
| `:5277` | `EasterBunnyEgg` | `ItemSpawnEgg` | 416 | 352 | `eggeasterbunny` | `eggeasterbunny` |
| `:5278` | `MolenoidEgg` | `ItemSpawnEgg` | 417 | 353 | `eggmolenoid` | `eggmolenoid` |
| `:5279` | `SeaMonsterEgg` | `ItemSpawnEgg` | 418 | 354 | `eggseamonster` | `eggseamonster` |
| `:5280` | `SeaViperEgg` | `ItemSpawnEgg` | 419 | 355 | `eggseaviper` | `eggseaviper` |
| `:5281` | `CaterKillerEgg` | `ItemSpawnEgg` | 420 | 356 | `eggcaterkiller` | `eggcaterkiller` |
| `:5282` | `RubberDuckyEgg` | `ItemSpawnEgg` | 428 | 362 | `eggrubberducky` | `eggrubberducky` |
| `:5283` | `HammerheadEgg` | `ItemSpawnEgg` | 426 | 360 | `egghammerhead` | `egghammerhead` |
| `:5284` | `LeonEgg` | `ItemSpawnEgg` | 424 | 358 | `eggleon` | `eggleon` |
| `:5285` | `CriminalEgg` | `ItemSpawnEgg` | 434 | 365 | `eggcriminal` | `eggcriminal` |
| `:5286` | `BrutalflyEgg` | `ItemSpawnEgg` | 459 | 367 | `eggbrutalfly` | `eggbrutalfly` |
| `:5287` | `NastysaurusEgg` | `ItemSpawnEgg` | 460 | 368 | `eggnastysaurus` | `eggnastysaurus` |
| `:5288` | `PointysaurusEgg` | `ItemSpawnEgg` | 461 | 369 | `eggpointysaurus` | `eggpointysaurus` |
| `:5289` | `CricketEgg` | `ItemSpawnEgg` | 462 | 370 | `eggcricket` | `eggcricket` |
| `:5290` | `ThePrincessEgg` | `ItemSpawnEgg` | 463 | 371 | `eggtheprincess` | `eggtheprincess` |
| `:5291` | `FrogEgg` | `ItemSpawnEgg` | 464 | 372 | `eggfrog` | `eggfrog` |
| `:5292` | `JefferyEgg` | `ItemSpawnEgg` | 474 | 378 | `eggrobot6` | `eggrobot6` |
| `:5293` | `AntRobotEgg` | `ItemSpawnEgg` | 475 | 379 | `eggantrobot` | `eggantrobot` |
| `:5294` | `SpiderRobotEgg` | `ItemSpawnEgg` | 476 | 380 | `eggspiderrobot` | `eggspiderrobot` |
| `:5295` | `SpiderDriverEgg` | `ItemSpawnEgg` | 477 | 381 | `eggspiderdriver` | `eggspiderdriver` |
| `:5296` | `CrabEgg` | `ItemSpawnEgg` | 482 | 383 | `eggcrab` | `eggcrab` |

### 3.7 Dispenser-Verhalten (`DoDispenserRegistrations`, `OreSpawnMain.java:5299-5435`)

- 134 Einträge über `BlockDispenser.dispenseBehaviorRegistry.putObject(item, behaviour)`.
- Verhalten: `DispenserBehaviorOreSpawnEgg` ×115, `MyDispenserBehaviorRock` ×12, `MyDispenserBehaviorArrow` ×1, `MyDispenserBehaviorWDCharge` ×1, `MyDispenserBehaviorSunspotUrchin` ×1, `MyDispenserBehaviorAcid` ×1, `MyDispenserBehaviorIceball` ×1, `MyDispenserBehaviorDeadIrukandji` ×1, `MyDispenserBehaviorLaserball` ×1.
- Alle 114 `ItemSpawnEgg` aus §3.6 haben einen Eintrag; doppelt: `LizardEgg` (`:5300`, `:5337`).

| Zeile | Item-Feld | Verhalten | manifest-id |
|---|---|---|---|
| `:5415` | `MyIrukandjiArrow` | `MyDispenserBehaviorArrow` | `irukandjiarrow` |
| `:5416` | `MyWaterBall` | `MyDispenserBehaviorWDCharge` | `waterball` |
| `:5417` | `MySunspotUrchin` | `MyDispenserBehaviorSunspotUrchin` | `sunspoturchin` |
| `:5418` | `MyAcid` | `MyDispenserBehaviorAcid` | `acid` |
| `:5419` | `MyIceBall` | `MyDispenserBehaviorIceball` | `iceball` |
| `:5420` | `MyIrukandji` | `MyDispenserBehaviorDeadIrukandji` | `deadirukandji` |
| `:5421` | `MyLaserBall` | `MyDispenserBehaviorLaserball` | `laserball` |
| `:5422` | `MySmallRock` | `MyDispenserBehaviorRock` | `rocksmall` |
| `:5423` | `MyRock` | `MyDispenserBehaviorRock` | `rock` |
| `:5424` | `MyRedRock` | `MyDispenserBehaviorRock` | `rockred` |
| `:5425` | `MyCrystalRedRock` | `MyDispenserBehaviorRock` | `rockcrystalred` |
| `:5426` | `MyCrystalGreenRock` | `MyDispenserBehaviorRock` | `rockcrystalgreen` |
| `:5427` | `MyCrystalBlueRock` | `MyDispenserBehaviorRock` | `rockcrystalblue` |
| `:5428` | `MyCrystalTNTRock` | `MyDispenserBehaviorRock` | `rockcrystaltnt` |
| `:5429` | `MyBlueRock` | `MyDispenserBehaviorRock` | `rockblue` |
| `:5430` | `MyGreenRock` | `MyDispenserBehaviorRock` | `rockgreen` |
| `:5431` | `MyPurpleRock` | `MyDispenserBehaviorRock` | `rockpurple` |
| `:5432` | `MySpikeyRock` | `MyDispenserBehaviorRock` | `rockspikey` |
| `:5433` | `MyTNTRock` | `MyDispenserBehaviorRock` | `rocktnt` |

---

## 4. Entity-Registrierung

Muster je Entity (`OreSpawnMain.java:3057-4177`, `:4643-4648`, `:4776-4781`, `:5013-5020`): `id = EntityRegistry.findGlobalUniqueEntityId()`, `registerGlobalEntityID(class, name, id)`, danach `registerModEntity(class, name, id, this, trackingRange, updateFrequency, sendsVelocityUpdates)`. Die globale ID ist dynamisch und für den Port ohne Bedeutung.

Sonderfälle:
- **Nur global, ohne `registerModEntity`:** `UltimateFishHook` (`:3058-3060`), `UltimateArrow` (`:5014-5016`), `IrukandjiArrow` (`:5018-5020`). offen: Tracking-Parameter für diese drei stehen nicht im OreSpawn-Code (1.7.10 behandelt Nicht-Mod-Entities in `EntityTracker` über `instanceof`-Zweige).
- **Einziges Vanilla-Spawn-Ei:** `RockBase` wird mit Eifarben `1118481, 16777215` registriert (`:4077`).
- **Namensabweichung:** `EasterBunny` global `"Easter Bunny"` (`:4013`), als Mod-Entity `"EasterBunny"` (`:4017`).
- **Teil-Entities** `GodzillaHead`, `KingHead`, `QueenHead`: Update-Frequenz 10 statt 1, Geschwindigkeits-Updates an (`:3781`, `:3955`, `:3969`).
- `Girlfriend`, `RedCow`, `GoldCow`, `EnchantedCow` usw. bekommen zusätzlich `addStringLocalization(name)` und `entity.<name>.name` (z. B. `:3236-3237`).

134 global registriert, 131 davon zusätzlich als Mod-Entity.

| # | Klasse | manifest-id | 1.7.10-Name | trackingRange | updateFrequency | velocityUpdates | global / mod |
|---|---|---|---|---|---|---|---|
| 1 | `UltimateFishHook` | `ultimate_fish_hook` | UltimateFishHook | offen | offen | offen | `:3060` / — |
| 2 | `SunspotUrchin` | `sunspot_urchin` | SunspotUrchin | 64 | 1 | true | `:3064` / `:3066` |
| 3 | `WaterBall` | `water_ball` | WaterBall | 64 | 1 | true | `:3070` / `:3072` |
| 4 | `InkSack` | `ink_sack` | InkSack | 64 | 1 | true | `:3076` / `:3078` |
| 5 | `LaserBall` | `laser_ball` | LaserBall | 64 | 1 | true | `:3082` / `:3084` |
| 6 | `IceBall` | `ice_ball` | IceBall | 64 | 1 | true | `:3088` / `:3090` |
| 7 | `Acid` | `acid` | Acid | 64 | 1 | true | `:3094` / `:3096` |
| 8 | `DeadIrukandji` | `dead_irukandji` | DeadIrukandji | 64 | 1 | true | `:3100` / `:3102` |
| 9 | `BerthaHit` | `bertha_hit` | BerthaHit | 64 | 1 | true | `:3106` / `:3108` |
| 10 | `PurplePower` | `purple_power` | PurplePower | 64 | 1 | true | `:3112` / `:3114` |
| 11 | `EntityThrownRock` | `entity_thrown_rock` | EntityThrownRock | 64 | 1 | true | `:3118` / `:3120` |
| 12 | `Girlfriend` | `girlfriend` | Girlfriend | 64 | 1 | false | `:3235` / `:3239` |
| 13 | `RedCow` | `apple_cow` | Apple Cow | 64 | 1 | false | `:3243` / `:3247` |
| 14 | `GoldCow` | `golden_apple_cow` | Golden Apple Cow | 64 | 1 | false | `:3251` / `:3255` |
| 15 | `EnchantedCow` | `enchanted_golden_apple_cow` | Enchanted Golden Apple Cow | 64 | 1 | false | `:3259` / `:3263` |
| 16 | `EntityButterfly` | `butterfly` | Butterfly | 32 | 1 | false | `:3267` / `:3271` |
| 17 | `EntityLunaMoth` | `moth` | Moth | 32 | 1 | false | `:3275` / `:3279` |
| 18 | `EntityMosquito` | `mosquito` | Mosquito | 16 | 1 | false | `:3283` / `:3287` |
| 19 | `Firefly` | `firefly` | Firefly | 64 | 1 | false | `:3291` / `:3295` |
| 20 | `Bee` | `bee` | Bee | 64 | 1 | false | `:3299` / `:3303` |
| 21 | `Mothra` | `mothra` | Mothra | 128 | 1 | false | `:3307` / `:3311` |
| 22 | `EntityAnt` | `ant` | Ant | 16 | 1 | false | `:3315` / `:3319` |
| 23 | `EntityRedAnt` | `red_ant` | Red Ant | 16 | 1 | false | `:3323` / `:3327` |
| 24 | `EntityRainbowAnt` | `rainbow_ant` | Rainbow Ant | 16 | 1 | false | `:3331` / `:3335` |
| 25 | `EntityUnstableAnt` | `unstable_ant` | Unstable Ant | 16 | 1 | false | `:3339` / `:3343` |
| 26 | `Robot1` | `bomb_omb` | Bomb-Omb | 32 | 1 | false | `:3347` / `:3351` |
| 27 | `Robot2` | `robo_pounder` | Robo-Pounder | 64 | 1 | false | `:3355` / `:3359` |
| 28 | `Robot3` | `robo_gunner` | Robo-Gunner | 64 | 1 | false | `:3363` / `:3367` |
| 29 | `Robot4` | `robo_warrior` | Robo-Warrior | 64 | 1 | false | `:3371` / `:3375` |
| 30 | `Robot5` | `robo_sniper` | Robo-Sniper | 64 | 1 | false | `:3379` / `:3383` |
| 31 | `Alosaurus` | `alosaurus` | Alosaurus | 64 | 1 | false | `:3387` / `:3391` |
| 32 | `Cryolophosaurus` | `cryolophosaurus` | Cryolophosaurus | 64 | 1 | false | `:3395` / `:3399` |
| 33 | `Basilisk` | `basilisk` | Basilisk | 64 | 1 | false | `:3403` / `:3407` |
| 34 | `Camarasaurus` | `camarasaurus` | Camarasaurus | 64 | 1 | false | `:3411` / `:3415` |
| 35 | `Hydrolisc` | `hydrolisc` | Hydrolisc | 64 | 1 | false | `:3419` / `:3423` |
| 36 | `VelocityRaptor` | `velocity_raptor` | Velocity Raptor | 64 | 1 | false | `:3427` / `:3431` |
| 37 | `Dragonfly` | `dragonfly` | Dragonfly | 64 | 1 | false | `:3435` / `:3439` |
| 38 | `EmperorScorpion` | `emperor_scorpion` | Emperor Scorpion | 64 | 1 | false | `:3443` / `:3447` |
| 39 | `Scorpion` | `scorpion` | Scorpion | 32 | 1 | false | `:3451` / `:3455` |
| 40 | `CaveFisher` | `cave_fisher` | CaveFisher | 32 | 1 | false | `:3459` / `:3463` |
| 41 | `Spyro` | `baby_dragon` | Baby Dragon | 64 | 1 | false | `:3467` / `:3471` |
| 42 | `Baryonyx` | `baryonyx` | Baryonyx | 64 | 1 | false | `:3475` / `:3479` |
| 43 | `GammaMetroid` | `wtf` | WTF? | 64 | 1 | false | `:3483` / `:3487` |
| 44 | `Cockateil` | `bird` | Bird | 32 | 1 | false | `:3491` / `:3495` |
| 45 | `RubyBird` | `ruby_bird` | Ruby Bird | 32 | 1 | false | `:3499` / `:3503` |
| 46 | `Kyuubi` | `kyuubi` | Kyuubi | 64 | 1 | false | `:3507` / `:3511` |
| 47 | `WaterDragon` | `water_dragon` | Water Dragon | 64 | 1 | false | `:3515` / `:3519` |
| 48 | `AttackSquid` | `attack_squid` | Attack Squid | 32 | 1 | false | `:3523` / `:3527` |
| 49 | `Alien` | `alien` | Alien | 64 | 1 | false | `:3531` / `:3535` |
| 50 | `Elevator` | `hoverboard` | Hoverboard | 128 | 1 | true | `:3539` / `:3543` |
| 51 | `Kraken` | `the_kraken` | The Kraken | 128 | 1 | false | `:3547` / `:3551` |
| 52 | `Lizard` | `lizard` | Lizard | 64 | 1 | false | `:3555` / `:3559` |
| 53 | `Cephadrome` | `cephadrome` | Cephadrome | 128 | 1 | true | `:3563` / `:3567` |
| 54 | `Dragon` | `dragon` | Dragon | 128 | 1 | true | `:3571` / `:3575` |
| 55 | `Chipmunk` | `chipmunk` | Chipmunk | 32 | 1 | false | `:3579` / `:3583` |
| 56 | `Gazelle` | `gazelle` | Gazelle | 64 | 1 | false | `:3587` / `:3591` |
| 57 | `Ostrich` | `ostrich` | Ostrich | 64 | 1 | true | `:3595` / `:3599` |
| 58 | `TrooperBug` | `jumpy_bug` | Jumpy Bug | 64 | 1 | false | `:3603` / `:3607` |
| 59 | `SpitBug` | `spit_bug` | Spit Bug | 64 | 1 | false | `:3611` / `:3615` |
| 60 | `StinkBug` | `stink_bug` | Stink Bug | 32 | 1 | false | `:3619` / `:3623` |
| 61 | `Tshirt` | `t_shirt` | T-Shirt | 32 | 1 | false | `:3627` / `:3631` |
| 62 | `Island` | `island` | Island | 64 | 1 | false | `:3635` / `:3639` |
| 63 | `IslandToo` | `island_too` | IslandToo | 64 | 1 | false | `:3643` / `:3647` |
| 64 | `CreepingHorror` | `creeping_horror` | Creeping Horror | 64 | 1 | false | `:3651` / `:3655` |
| 65 | `TerribleTerror` | `terrible_terror` | Terrible Terror | 64 | 1 | false | `:3659` / `:3663` |
| 66 | `CliffRacer` | `cliff_racer` | Cliff Racer | 32 | 1 | false | `:3667` / `:3671` |
| 67 | `Triffid` | `triffid` | Triffid | 64 | 1 | false | `:3675` / `:3679` |
| 68 | `PitchBlack` | `nightmare` | Nightmare | 64 | 1 | false | `:3683` / `:3687` |
| 69 | `LurkingTerror` | `lurking_terror` | Lurking Terror | 64 | 1 | false | `:3691` / `:3695` |
| 70 | `Godzilla` | `mobzilla` | Mobzilla | 128 | 1 | false | `:3699` / `:3703` |
| 71 | `Ghost` | `ghost` | Ghost | 32 | 1 | false | `:3707` / `:3711` |
| 72 | `GhostSkelly` | `ghost_pumpkin_skelly` | Ghost Pumpkin Skelly | 64 | 1 | false | `:3715` / `:3719` |
| 73 | `WormSmall` | `small_worm` | Small Worm | 32 | 1 | false | `:3723` / `:3727` |
| 74 | `WormMedium` | `medium_worm` | Medium Worm | 64 | 1 | false | `:3731` / `:3735` |
| 75 | `WormLarge` | `large_worm` | Large Worm | 64 | 1 | false | `:3739` / `:3743` |
| 76 | `Cassowary` | `cassowary` | Cassowary | 64 | 1 | false | `:3747` / `:3751` |
| 77 | `CloudShark` | `cloud_shark` | Cloud Shark | 64 | 1 | false | `:3755` / `:3759` |
| 78 | `GoldFish` | `gold_fish` | Gold Fish | 32 | 1 | false | `:3763` / `:3767` |
| 79 | `LeafMonster` | `leaf_monster` | Leaf Monster | 64 | 1 | false | `:3771` / `:3775` |
| 80 | `GodzillaHead` | `mobzilla_head` | MobzillaHead | 128 | 10 | true | `:3779` / `:3781` |
| 81 | `EnderKnight` | `ender_knight` | Ender Knight | 64 | 1 | false | `:3785` / `:3789` |
| 82 | `EnderReaper` | `ender_reaper` | Ender Reaper | 64 | 1 | false | `:3793` / `:3797` |
| 83 | `Beaver` | `beaver` | Beaver | 64 | 1 | false | `:3801` / `:3805` |
| 84 | `Termite` | `termite` | Termite | 32 | 1 | false | `:3809` / `:3813` |
| 85 | `Fairy` | `fairy` | Fairy | 32 | 1 | false | `:3817` / `:3821` |
| 86 | `Peacock` | `peacock` | Peacock | 64 | 1 | false | `:3825` / `:3829` |
| 87 | `Rotator` | `rotator` | Rotator | 64 | 1 | false | `:3833` / `:3837` |
| 88 | `Vortex` | `vortex` | Vortex | 64 | 1 | false | `:3841` / `:3845` |
| 89 | `DungeonBeast` | `dungeon_beast` | Dungeon Beast | 64 | 1 | false | `:3849` / `:3853` |
| 90 | `Rat` | `rat` | Rat | 32 | 1 | false | `:3857` / `:3861` |
| 91 | `Flounder` | `flounder` | Flounder | 32 | 1 | false | `:3865` / `:3869` |
| 92 | `Whale` | `whale` | Whale | 64 | 1 | false | `:3873` / `:3877` |
| 93 | `Irukandji` | `irukandji` | Irukandji | 32 | 1 | false | `:3881` / `:3885` |
| 94 | `Skate` | `skate` | Skate | 32 | 1 | false | `:3889` / `:3893` |
| 95 | `Urchin` | `crystal_urchin` | Crystal Urchin | 64 | 1 | false | `:3897` / `:3901` |
| 96 | `Mantis` | `mantis` | Mantis | 64 | 1 | false | `:3905` / `:3909` |
| 97 | `HerculesBeetle` | `hercules_beetle` | Hercules Beetle | 64 | 1 | false | `:3913` / `:3917` |
| 98 | `TRex` | `t_rex` | T. Rex | 64 | 1 | false | `:3921` / `:3925` |
| 99 | `Stinky` | `stinky` | Stinky | 64 | 1 | false | `:3929` / `:3933` |
| 100 | `Coin` | `coin` | Coin | 64 | 1 | false | `:3937` / `:3941` |
| 101 | `TheKing` | `the_king` | The King | 128 | 1 | false | `:3945` / `:3949` |
| 102 | `KingHead` | `king_head` | KingHead | 128 | 10 | true | `:3953` / `:3955` |
| 103 | `TheQueen` | `the_queen` | The Queen | 128 | 1 | false | `:3959` / `:3963` |
| 104 | `QueenHead` | `queen_head` | QueenHead | 128 | 10 | true | `:3967` / `:3969` |
| 105 | `Boyfriend` | `boyfriend` | Boyfriend | 64 | 1 | false | `:3973` / `:3977` |
| 106 | `ThePrince` | `the_prince` | The Prince | 64 | 1 | false | `:3981` / `:3985` |
| 107 | `Molenoid` | `molenoid` | Molenoid | 64 | 1 | false | `:3989` / `:3993` |
| 108 | `SeaMonster` | `sea_monster` | Sea Monster | 64 | 1 | false | `:3997` / `:4001` |
| 109 | `SeaViper` | `sea_viper` | Sea Viper | 64 | 1 | false | `:4005` / `:4009` |
| 110 | `EasterBunny` | `easter_bunny` | Easter Bunny | 64 | 1 | false | `:4013` / `:4017` |
| 111 | `CaterKiller` | `cater_killer` | CaterKiller | 64 | 1 | false | `:4021` / `:4025` |
| 112 | `CrystalCow` | `crystal_apple_cow` | Crystal Apple Cow | 64 | 1 | false | `:4029` / `:4033` |
| 113 | `Leon` | `leonopteryx` | Leonopteryx | 64 | 1 | false | `:4037` / `:4041` |
| 114 | `Hammerhead` | `hammerhead` | Hammerhead | 64 | 1 | false | `:4045` / `:4049` |
| 115 | `RubberDucky` | `rubber_ducky` | Rubber Ducky | 64 | 1 | false | `:4053` / `:4057` |
| 116 | `ThePrinceTeen` | `the_young_prince` | The Young Prince | 64 | 1 | false | `:4061` / `:4065` |
| 117 | `BandP` | `criminal` | Criminal | 64 | 1 | false | `:4069` / `:4073` |
| 118 | `RockBase` | `rock` | Rock | 32 | 1 | false | `:4077` / `:4081` |
| 119 | `Brutalfly` | `brutalfly` | Brutalfly | 128 | 1 | false | `:4085` / `:4089` |
| 120 | `Nastysaurus` | `nastysaurus` | Nastysaurus | 128 | 1 | false | `:4093` / `:4097` |
| 121 | `Pointysaurus` | `pointysaurus` | Pointysaurus | 64 | 1 | false | `:4101` / `:4105` |
| 122 | `Cricket` | `cricket` | Cricket | 32 | 1 | false | `:4109` / `:4113` |
| 123 | `ThePrincess` | `the_princess` | The Princess | 64 | 1 | false | `:4117` / `:4121` |
| 124 | `Frog` | `frog` | Frog | 32 | 1 | false | `:4125` / `:4129` |
| 125 | `ThePrinceAdult` | `the_young_adult_prince` | The Young Adult Prince | 128 | 1 | false | `:4133` / `:4137` |
| 126 | `SpiderRobot` | `robot_spider` | Robot Spider | 128 | 1 | false | `:4141` / `:4145` |
| 127 | `SpiderDriver` | `spider_driver` | Spider Driver | 64 | 1 | false | `:4149` / `:4153` |
| 128 | `GiantRobot` | `jeffery` | Jeffery | 128 | 1 | false | `:4157` / `:4161` |
| 129 | `AntRobot` | `robot_red_ant` | Robot Red Ant | 128 | 1 | false | `:4165` / `:4169` |
| 130 | `Crab` | `crab` | Crab | 64 | 1 | false | `:4173` / `:4177` |
| 131 | `Shoes` | `shoes` | Shoes | 64 | 1 | true | `:4646` / `:4648` |
| 132 | `EntityCage` | `entity_cage` | EntityCage | 64 | 1 | true | `:4779` / `:4781` |
| 133 | `UltimateArrow` | `ultimate_arrow` | UltimateArrow | offen | offen | offen | `:5016` / — |
| 134 | `IrukandjiArrow` | `irukandji_arrow` | IrukandjiArrow | offen | offen | offen | `:5020` / — |

**Port:** `EntityType.Builder.clientTrackingRange(...)` zählt in 1.21.1 in Chunks: `ChunkMap` rechnet `clientTrackingRange() * 16` (`neoforge-21.1.248-sources.jar`, `net/minecraft/server/level/ChunkMap.java:1113`, `:1355`). Umrechnung der Originalwerte in Blöcken: 16 → 1, 32 → 2, 64 → 4, 128 → 8. `updateInterval` entspricht `updateFrequency`, `setShouldReceiveVelocityUpdates` entspricht dem letzten Argument.

---

## 5. Natürliche Spawns (`EntityRegistry.addSpawn`)

- 348 Aufrufe für 55 Klassen, alle in `make_some_more_things` (`OreSpawnMain.java:4182-4641`), jeder Aufruf genau ein Biom. Werte stimmen zu 348/348 mit manifest `entities[].spawns` überein.
- **manifest-Lücke:** Die 44 Halloween-Zeilen (`Ghost` und `GhostSkelly`, je 22 Biome, `:4182-4225`) stehen dort mit leerem `guards`. Die echte Bedingung ist das Systemdatum 31. Oktober beim Start (§6), **nicht** `GhostEnable`/`GhostSkellyEnable`.
- Guards sind `if (<Flag> != 0)`-Blöcke direkt um die Aufrufe; einziger zusammengesetzter Guard: `EasterBunnyEnable != 0 && easter_day != 0` (`:4341`).
- Typ → `MobCategory` nach `docs/catalog/biome_map.json` (`spawn_type_to_mob_category`): `creature` → `CREATURE`, `ambient` → `AMBIENT`, `monster` → `MONSTER`, `waterCreature` → `WATER_CREATURE`. Die meisten feindlichen OreSpawn-Mobs (Scorpion, EnderReaper, Basilisk, CaterKiller …) spawnen als **ambient**, nur `Kyuubi` und `Stinky` (Nether) als `monster`.
- Biom-Zuordnung 1.7.10 → 1.21.1 nach `docs/catalog/biome_map.json`. Mehrere 1.7.10-Biome fallen auf ein 1.21.1-Biom (`forest`+`forestHills` → `minecraft:forest`, `jungle`+`jungleHills` → `minecraft:jungle`, `taiga`+`taigaHills`, `birchForest`+`birchForestHills`, `megaTaiga`+`megaTaigaHills`, `coldTaiga`+`coldTaigaHills`, `extremeHills`+`extremeHillsEdge`, `mesa`+`mesaPlateau`). offen: Ob der Port solche Doppel-Einträge addiert oder zusammenfasst, ist eine Designentscheidung; das Original hatte zwei getrennte Biome mit je eigenem Eintrag.
- Die Spawns in OreSpawn-Dimensionen stehen nicht hier, sondern in den Biomklassen (z. B. `BiomeGenUtopianPlains`, siehe Verbraucher in §2.2).

348 Aufrufe, 55 Klassen, 202 Gruppen gleicher Parameter. Eine Zeile fasst aufeinanderfolgende Aufrufe mit identischem Guard, Typ, Gewicht und Gruppengröße zusammen.

| Klasse | manifest-id | Guard (Zeile) | Kategorie | Gewicht | Gruppe | 1.7.10-Biome | 1.21.1-Biome | Zeilen |
|---|---|---|---|---|---|---|---|---|
| `GhostSkelly` | `ghost_pumpkin_skelly` | 31.10. (Datum beim Start) (`:4181`) | `ambient` → `AMBIENT` | 15 | 3-6 | beach, extremeHills, extremeHillsEdge, forest, forestHills, jungle, jungleHills, plains, river, desert, birchForest, birchForestHills, megaTaiga, taiga, savanna, savannaPlateau, mesaPlateau_F, mesaPlateau, mesa, coldTaigaHills, coldTaiga, roofedForest | minecraft:beach, minecraft:windswept_hills, minecraft:forest, minecraft:jungle, minecraft:plains, minecraft:river, minecraft:desert, minecraft:birch_forest, minecraft:old_growth_pine_taiga, minecraft:taiga, minecraft:savanna, minecraft:savanna_plateau, minecraft:wooded_badlands, minecraft:badlands, minecraft:snowy_taiga, minecraft:dark_forest | `:4182-4203` |
| `Ghost` | `ghost` | 31.10. (Datum beim Start) (`:4181`) | `ambient` → `AMBIENT` | 15 | 3-6 | beach, extremeHills, extremeHillsEdge, forest, forestHills, jungle, jungleHills, plains, river, desert, birchForest, birchForestHills, megaTaiga, taiga, savanna, savannaPlateau, mesaPlateau_F, mesaPlateau, mesa, coldTaigaHills, coldTaiga, roofedForest | minecraft:beach, minecraft:windswept_hills, minecraft:forest, minecraft:jungle, minecraft:plains, minecraft:river, minecraft:desert, minecraft:birch_forest, minecraft:old_growth_pine_taiga, minecraft:taiga, minecraft:savanna, minecraft:savanna_plateau, minecraft:wooded_badlands, minecraft:badlands, minecraft:snowy_taiga, minecraft:dark_forest | `:4204-4225` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 30 | 8-15 | beach | minecraft:beach | `:4234` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 10 | 3-6 | forest, river, stoneBeach | minecraft:forest, minecraft:river, minecraft:stony_shore | `:4235-4239` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 8 | 2-5 | forestHills | minecraft:forest | `:4236` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 5 | 2-3 | plains | minecraft:plains | `:4237` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 5 | 2-4 | birchForest | minecraft:birch_forest | `:4240` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 5 | 2-5 | birchForestHills, megaTaiga, taiga | minecraft:birch_forest, minecraft:old_growth_pine_taiga, minecraft:taiga | `:4241-4243` |
| `Girlfriend` | `girlfriend` | `GirlfriendEnable != 0` (`:4233`) | `creature` → `CREATURE` | 2 | 1-3 | savanna, savannaPlateau | minecraft:savanna, minecraft:savanna_plateau | `:4244-4245` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 30 | 8-15 | beach | minecraft:beach | `:4248` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 10 | 3-6 | forest, river, stoneBeach | minecraft:forest, minecraft:river, minecraft:stony_shore | `:4249-4253` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 8 | 2-5 | forestHills | minecraft:forest | `:4250` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 5 | 2-3 | plains | minecraft:plains | `:4251` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 5 | 2-4 | birchForest | minecraft:birch_forest | `:4254` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 5 | 2-5 | birchForestHills, megaTaiga, taiga | minecraft:birch_forest, minecraft:old_growth_pine_taiga, minecraft:taiga | `:4255-4257` |
| `Boyfriend` | `boyfriend` | `BoyfriendEnable != 0` (`:4247`) | `creature` → `CREATURE` | 2 | 1-3 | savanna, savannaPlateau | minecraft:savanna, minecraft:savanna_plateau | `:4258-4259` |
| `Beaver` | `beaver` | `BeaverEnable != 0` (`:4261`) | `creature` → `CREATURE` | 10 | 2-4 | river | minecraft:river | `:4262` |
| `Beaver` | `beaver` | `BeaverEnable != 0` (`:4261`) | `creature` → `CREATURE` | 3 | 2-4 | forest | minecraft:forest | `:4263` |
| `Beaver` | `beaver` | `BeaverEnable != 0` (`:4261`) | `creature` → `CREATURE` | 2 | 2-4 | birchForest | minecraft:birch_forest | `:4264` |
| `Beaver` | `beaver` | `BeaverEnable != 0` (`:4261`) | `creature` → `CREATURE` | 2 | 2-5 | birchForestHills | minecraft:birch_forest | `:4265` |
| `Beaver` | `beaver` | `BeaverEnable != 0` (`:4261`) | `creature` → `CREATURE` | 5 | 2-5 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4266-4267` |
| `RedCow` | `apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 8 | 4-8 | plains, forest | minecraft:plains, minecraft:forest | `:4270-4271` |
| `RedCow` | `apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 5 | 2-5 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4272-4273` |
| `RedCow` | `apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 8 | 1-3 | savanna | minecraft:savanna | `:4274` |
| `RedCow` | `apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 2 | 1-3 | savannaPlateau | minecraft:savanna_plateau | `:4275` |
| `GoldCow` | `golden_apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 5 | 2-6 | plains, forest | minecraft:plains, minecraft:forest | `:4276-4277` |
| `GoldCow` | `golden_apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 5 | 2-5 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4278-4279` |
| `EnchantedCow` | `enchanted_golden_apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 3 | 2-4 | forest, plains | minecraft:forest, minecraft:plains | `:4280-4281` |
| `EnchantedCow` | `enchanted_golden_apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 5 | 2-5 | megaTaiga | minecraft:old_growth_pine_taiga | `:4282` |
| `EnchantedCow` | `enchanted_golden_apple_cow` | `CowEnable != 0` (`:4269`) | `creature` → `CREATURE` | 15 | 3-6 | mushroomIsland | minecraft:mushroom_fields | `:4283` |
| `BandP` | `criminal` | `CriminalEnable != 0` (`:4285`) | `ambient` → `AMBIENT` | 20 | 1-2 | plains, desert, savanna | minecraft:plains, minecraft:desert, minecraft:savanna | `:4286-4288` |
| `WormLarge` | `large_worm` | `WormEnable != 0` (`:4290`) | `creature` → `CREATURE` | 25 | 1-1 | plains | minecraft:plains | `:4291` |
| `WormLarge` | `large_worm` | `WormEnable != 0` (`:4290`) | `creature` → `CREATURE` | 15 | 1-1 | savanna | minecraft:savanna | `:4292` |
| `WormLarge` | `large_worm` | `WormEnable != 0` (`:4290`) | `creature` → `CREATURE` | 10 | 1-1 | savannaPlateau | minecraft:savanna_plateau | `:4293` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 8 | 5-15 | beach | minecraft:beach | `:4296` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 5 | 1-2 | extremeHills, extremeHillsEdge | minecraft:windswept_hills | `:4297-4298` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 30 | 3-6 | forest | minecraft:forest | `:4299` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 20 | 2-5 | forestHills, jungle, river | minecraft:forest, minecraft:jungle, minecraft:river | `:4300-4304` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 10 | 2-5 | jungleHills | minecraft:jungle | `:4302` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 20 | 3-6 | plains | minecraft:plains | `:4303` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 20 | 4-10 | swampland | minecraft:swamp | `:4305` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 15 | 2-4 | birchForest | minecraft:birch_forest | `:4306` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 15 | 2-5 | birchForestHills, megaTaiga, taiga | minecraft:birch_forest, minecraft:old_growth_pine_taiga, minecraft:taiga | `:4307-4309` |
| `EntityButterfly` | `butterfly` | `ButterflyEnable != 0` (`:4295`) | `ambient` → `AMBIENT` | 10 | 1-5 | savanna, savannaPlateau | minecraft:savanna, minecraft:savanna_plateau | `:4310-4311` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 8 | 1-2 | extremeHills, extremeHillsEdge | minecraft:windswept_hills | `:4314-4315` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 10 | 2-5 | forestHills, jungleHills | minecraft:forest, minecraft:jungle | `:4316-4319` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 20 | 3-6 | forest, jungle | minecraft:forest, minecraft:jungle | `:4317-4318` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 20 | 2-5 | swampland | minecraft:swamp | `:4320` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 10 | 1-5 | plains, savanna, savannaPlateau | minecraft:plains, minecraft:savanna, minecraft:savanna_plateau | `:4321-4328` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 15 | 2-4 | birchForest | minecraft:birch_forest | `:4322` |
| `EntityLunaMoth` | `moth` | `MothEnable != 0` (`:4313`) | `ambient` → `AMBIENT` | 15 | 2-5 | birchForestHills, roofedForest, megaTaiga, taiga | minecraft:birch_forest, minecraft:dark_forest, minecraft:old_growth_pine_taiga, minecraft:taiga | `:4323-4326` |
| `Cassowary` | `cassowary` | `CassowaryEnable != 0` (`:4330`) | `ambient` → `AMBIENT` | 5 | 1-2 | extremeHills, extremeHillsEdge, extremeHillsPlus | minecraft:windswept_hills, minecraft:windswept_forest | `:4331-4333` |
| `Cassowary` | `cassowary` | `CassowaryEnable != 0` (`:4330`) | `ambient` → `AMBIENT` | 5 | 2-4 | birchForest | minecraft:birch_forest | `:4334` |
| `Cassowary` | `cassowary` | `CassowaryEnable != 0` (`:4330`) | `ambient` → `AMBIENT` | 5 | 2-5 | birchForestHills | minecraft:birch_forest | `:4335` |
| `Cassowary` | `cassowary` | `CassowaryEnable != 0` (`:4330`) | `ambient` → `AMBIENT` | 15 | 2-5 | megaTaiga, megaTaigaHills | minecraft:old_growth_pine_taiga | `:4336-4337` |
| `Cassowary` | `cassowary` | `CassowaryEnable != 0` (`:4330`) | `ambient` → `AMBIENT` | 3 | 1-5 | savanna | minecraft:savanna | `:4338` |
| `Cassowary` | `cassowary` | `CassowaryEnable != 0` (`:4330`) | `ambient` → `AMBIENT` | 10 | 1-5 | savannaPlateau | minecraft:savanna_plateau | `:4339` |
| `EasterBunny` | `easter_bunny` | `EasterBunnyEnable != 0 && easter_day != 0` (`:4341`) | `ambient` → `AMBIENT` | 10 | 1-2 | plains, forest, forestHills | minecraft:plains, minecraft:forest | `:4342-4344` |
| `EasterBunny` | `easter_bunny` | `EasterBunnyEnable != 0 && easter_day != 0` (`:4341`) | `ambient` → `AMBIENT` | 5 | 1-2 | birchForest, birchForestHills, megaTaiga | minecraft:birch_forest, minecraft:old_growth_pine_taiga | `:4345-4347` |
| `EasterBunny` | `easter_bunny` | `EasterBunnyEnable != 0 && easter_day != 0` (`:4341`) | `ambient` → `AMBIENT` | 8 | 1-2 | taiga | minecraft:taiga | `:4348` |
| `Firefly` | `firefly` | `FireflyEnable != 0` (`:4350`) | `ambient` → `AMBIENT` | 15 | 5-10 | forest, forestHills, jungle, jungleHills | minecraft:forest, minecraft:jungle | `:4351-4355` |
| `Firefly` | `firefly` | `FireflyEnable != 0` (`:4350`) | `ambient` → `AMBIENT` | 10 | 4-8 | swampland | minecraft:swamp | `:4353` |
| `Firefly` | `firefly` | `FireflyEnable != 0` (`:4350`) | `ambient` → `AMBIENT` | 10 | 3-6 | stoneBeach | minecraft:stony_shore | `:4356` |
| `Firefly` | `firefly` | `FireflyEnable != 0` (`:4350`) | `ambient` → `AMBIENT` | 15 | 3-10 | birchForest, birchForestHills | minecraft:birch_forest | `:4357-4358` |
| `Firefly` | `firefly` | `FireflyEnable != 0` (`:4350`) | `ambient` → `AMBIENT` | 15 | 2-10 | megaTaiga, taiga, megaTaigaHills | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4359-4361` |
| `Firefly` | `firefly` | `FireflyEnable != 0` (`:4350`) | `ambient` → `AMBIENT` | 10 | 2-8 | savanna, savannaPlateau | minecraft:savanna, minecraft:savanna_plateau | `:4362-4363` |
| `Whale` | `whale` | `WhaleEnable != 0` (`:4365`) | `waterCreature` → `WATER_CREATURE` | 1 | 1-2 | deepOcean | minecraft:deep_ocean | `:4366` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 2 | 1-2 | forest, forestHills | minecraft:forest | `:4369-4370` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 5 | 3-5 | jungle | minecraft:jungle | `:4371` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 5 | 2-5 | jungleHills | minecraft:jungle | `:4372` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 3 | 2-4 | birchForest, birchForestHills | minecraft:birch_forest | `:4373-4374` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 5 | 1-2 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4375-4376` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 3 | 1-1 | savanna | minecraft:savanna | `:4377` |
| `Bee` | `bee` | `BeeEnable != 0` (`:4368`) | `ambient` → `AMBIENT` | 2 | 1-1 | savannaPlateau | minecraft:savanna_plateau | `:4378` |
| `Mantis` | `mantis` | `MantisEnable != 0` (`:4380`) | `ambient` → `AMBIENT` | 1 | 1-1 | forest, forestHills, savanna, savannaPlateau | minecraft:forest, minecraft:savanna, minecraft:savanna_plateau | `:4381-4389` |
| `Mantis` | `mantis` | `MantisEnable != 0` (`:4380`) | `ambient` → `AMBIENT` | 5 | 1-2 | jungle | minecraft:jungle | `:4383` |
| `Mantis` | `mantis` | `MantisEnable != 0` (`:4380`) | `ambient` → `AMBIENT` | 1 | 1-2 | plains, swampland, megaTaiga | minecraft:plains, minecraft:swamp, minecraft:old_growth_pine_taiga | `:4384-4387` |
| `Mantis` | `mantis` | `MantisEnable != 0` (`:4380`) | `ambient` → `AMBIENT` | 1 | 2-4 | birchForest | minecraft:birch_forest | `:4386` |
| `HerculesBeetle` | `hercules_beetle` | `HerculesBeetleEnable != 0` (`:4391`) | `ambient` → `AMBIENT` | 2 | 1-2 | forestHills, jungleHills, taigaHills, megaTaigaHills | minecraft:forest, minecraft:jungle, minecraft:taiga, minecraft:old_growth_pine_taiga | `:4392-4398` |
| `HerculesBeetle` | `hercules_beetle` | `HerculesBeetleEnable != 0` (`:4391`) | `ambient` → `AMBIENT` | 5 | 1-2 | extremeHillsEdge | minecraft:windswept_hills | `:4394` |
| `HerculesBeetle` | `hercules_beetle` | `HerculesBeetleEnable != 0` (`:4391`) | `ambient` → `AMBIENT` | 5 | 1-1 | birchForestHills | minecraft:birch_forest | `:4396` |
| `HerculesBeetle` | `hercules_beetle` | `HerculesBeetleEnable != 0` (`:4391`) | `ambient` → `AMBIENT` | 2 | 1-1 | coldTaigaHills | minecraft:snowy_taiga | `:4397` |
| `Molenoid` | `molenoid` | `MolenoidEnable != 0` (`:4400`) | `ambient` → `AMBIENT` | 2 | 1-2 | plains | minecraft:plains | `:4401` |
| `Molenoid` | `molenoid` | `MolenoidEnable != 0` (`:4400`) | `ambient` → `AMBIENT` | 2 | 1-1 | savanna, savannaPlateau | minecraft:savanna, minecraft:savanna_plateau | `:4402-4403` |
| `CaterKiller` | `cater_killer` | `CaterKillerEnable != 0` (`:4405`) | `ambient` → `AMBIENT` | 2 | 1-1 | forest | minecraft:forest | `:4406` |
| `CaterKiller` | `cater_killer` | `CaterKillerEnable != 0` (`:4405`) | `ambient` → `AMBIENT` | 2 | 1-2 | jungle, birchForestHills, megaTaiga, taiga | minecraft:jungle, minecraft:birch_forest, minecraft:old_growth_pine_taiga, minecraft:taiga | `:4407-4413` |
| `CaterKiller` | `cater_killer` | `CaterKillerEnable != 0` (`:4405`) | `ambient` → `AMBIENT` | 4 | 1-2 | forestHills, jungleHills | minecraft:forest, minecraft:jungle | `:4408-4409` |
| `CaterKiller` | `cater_killer` | `CaterKillerEnable != 0` (`:4405`) | `ambient` → `AMBIENT` | 6 | 1-2 | birchForest | minecraft:birch_forest | `:4410` |
| `CaterKiller` | `cater_killer` | `CaterKillerEnable != 0` (`:4405`) | `ambient` → `AMBIENT` | 10 | 1-2 | roofedForest | minecraft:dark_forest | `:4414` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 8 | 3-6 | forest | minecraft:forest | `:4417` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 5 | 3-6 | forestHills, birchForest | minecraft:forest, minecraft:birch_forest | `:4418-4421` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 4 | 3-6 | jungle, birchForestHills | minecraft:jungle, minecraft:birch_forest | `:4419-4422` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 2 | 1-2 | plains | minecraft:plains | `:4420` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 10 | 2-5 | roofedForest | minecraft:dark_forest | `:4423` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 2 | 2-5 | megaTaiga | minecraft:old_growth_pine_taiga | `:4424` |
| `Chipmunk` | `chipmunk` | `ChipmunkEnable != 0` (`:4416`) | `ambient` → `AMBIENT` | 6 | 2-5 | taiga | minecraft:taiga | `:4425` |
| `Ostrich` | `ostrich` | `OstrichEnable != 0` (`:4427`) | `ambient` → `AMBIENT` | 1 | 1-1 | desert, stoneBeach, savanna, savannaPlateau | minecraft:desert, minecraft:stony_shore, minecraft:savanna, minecraft:savanna_plateau | `:4428-4431` |
| `Cephadrome` | `cephadrome` | `CephadromeEnable != 0` (`:4433`) | `ambient` → `AMBIENT` | 1 | 1-1 | icePlains, coldTaiga | minecraft:snowy_plains, minecraft:snowy_taiga | `:4434-4435` |
| `EntityMosquito` | `mosquito` | `MosquitoEnable != 0` (`:4437`) | `ambient` → `AMBIENT` | 30 | 5-10 | swampland | minecraft:swamp | `:4438` |
| `EntityMosquito` | `mosquito` | `MosquitoEnable != 0` (`:4437`) | `ambient` → `AMBIENT` | 20 | 5-10 | jungle, jungleHills | minecraft:jungle | `:4439-4440` |
| `EntityMosquito` | `mosquito` | `MosquitoEnable != 0` (`:4437`) | `ambient` → `AMBIENT` | 15 | 2-5 | roofedForest | minecraft:dark_forest | `:4441` |
| `Ghost` | `ghost` | `GhostEnable != 0` (`:4443`) | `ambient` → `AMBIENT` | 15 | 5-10 | coldTaiga | minecraft:snowy_taiga | `:4444` |
| `Ghost` | `ghost` | `GhostEnable != 0` (`:4443`) | `ambient` → `AMBIENT` | 10 | 5-10 | taigaHills | minecraft:taiga | `:4445` |
| `Ghost` | `ghost` | `GhostEnable != 0` (`:4443`) | `ambient` → `AMBIENT` | 6 | 4-6 | frozenRiver | minecraft:frozen_river | `:4446` |
| `Ghost` | `ghost` | `GhostEnable != 0` (`:4443`) | `ambient` → `AMBIENT` | 2 | 1-4 | jungle | minecraft:jungle | `:4447` |
| `Ghost` | `ghost` | `GhostEnable != 0` (`:4443`) | `ambient` → `AMBIENT` | 15 | 2-5 | roofedForest | minecraft:dark_forest | `:4448` |
| `GhostSkelly` | `ghost_pumpkin_skelly` | `GhostSkellyEnable != 0` (`:4450`) | `ambient` → `AMBIENT` | 15 | 5-10 | coldTaiga | minecraft:snowy_taiga | `:4451` |
| `GhostSkelly` | `ghost_pumpkin_skelly` | `GhostSkellyEnable != 0` (`:4450`) | `ambient` → `AMBIENT` | 10 | 5-10 | taigaHills | minecraft:taiga | `:4452` |
| `GhostSkelly` | `ghost_pumpkin_skelly` | `GhostSkellyEnable != 0` (`:4450`) | `ambient` → `AMBIENT` | 6 | 4-6 | frozenRiver | minecraft:frozen_river | `:4453` |
| `GhostSkelly` | `ghost_pumpkin_skelly` | `GhostSkellyEnable != 0` (`:4450`) | `ambient` → `AMBIENT` | 2 | 1-4 | jungle | minecraft:jungle | `:4454` |
| `GhostSkelly` | `ghost_pumpkin_skelly` | `GhostSkellyEnable != 0` (`:4450`) | `ambient` → `AMBIENT` | 15 | 2-5 | roofedForest | minecraft:dark_forest | `:4455` |
| `Dragonfly` | `dragonfly` | `DragonflyEnable != 0` (`:4457`) | `ambient` → `AMBIENT` | 5 | 3-5 | swampland | minecraft:swamp | `:4458` |
| `Dragonfly` | `dragonfly` | `DragonflyEnable != 0` (`:4457`) | `ambient` → `AMBIENT` | 4 | 1-2 | river | minecraft:river | `:4459` |
| `Kyuubi` | `kyuubi` | `KyuubiEnable != 0` (`:4461`) | `monster` → `MONSTER` | 10 | 1-1 | hell | minecraft:nether_wastes | `:4462` |
| `Stinky` | `stinky` | `StinkyEnable != 0` (`:4464`) | `monster` → `MONSTER` | 2 | 1-1 | hell | minecraft:nether_wastes | `:4465` |
| `Stinky` | `stinky` | `StinkyEnable != 0` (`:4464`) | `ambient` → `AMBIENT` | 1 | 1-1 | mesa, mesaPlateau, mesaPlateau_F | minecraft:badlands, minecraft:wooded_badlands | `:4466-4468` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 10 | 2-5 | beach | minecraft:beach | `:4471` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 10 | 1-2 | extremeHills | minecraft:windswept_hills | `:4472` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 10 | 2-4 | extremeHillsEdge, plains | minecraft:windswept_hills, minecraft:plains | `:4473-4478` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 25 | 5-10 | forest, jungleHills | minecraft:forest, minecraft:jungle | `:4474-4477` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 20 | 3-6 | forestHills | minecraft:forest | `:4475` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 35 | 5-10 | jungle | minecraft:jungle | `:4476` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 15 | 3-6 | river, birchForest | minecraft:river, minecraft:birch_forest | `:4479-4481` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 5 | 3-6 | stoneBeach, birchForestHills | minecraft:stony_shore, minecraft:birch_forest | `:4480-4482` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 15 | 2-5 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4483-4484` |
| `Cockateil` | `bird` | `CockateilEnable != 0` (`:4470`) | `ambient` → `AMBIENT` | 11 | 1-5 | savanna, savannaPlateau | minecraft:savanna, minecraft:savanna_plateau | `:4485-4486` |
| `Hydrolisc` | `hydrolisc` | `HydroliscEnable != 0` (`:4488`) | `creature` → `CREATURE` | 25 | 3-6 | swampland | minecraft:swamp | `:4489` |
| `Hydrolisc` | `hydrolisc` | `HydroliscEnable != 0` (`:4488`) | `creature` → `CREATURE` | 15 | 2-5 | jungle | minecraft:jungle | `:4490` |
| `Hydrolisc` | `hydrolisc` | `HydroliscEnable != 0` (`:4488`) | `creature` → `CREATURE` | 10 | 1-3 | jungleHills | minecraft:jungle | `:4491` |
| `Hydrolisc` | `hydrolisc` | `HydroliscEnable != 0` (`:4488`) | `creature` → `CREATURE` | 5 | 3-6 | stoneBeach | minecraft:stony_shore | `:4492` |
| `Mothra` | `mothra` | `MothraEnable != 0` (`:4494`) | `ambient` → `AMBIENT` | 2 | 1-1 | extremeHills, extremeHillsPlus | minecraft:windswept_hills, minecraft:windswept_forest | `:4495-4496` |
| `Brutalfly` | `brutalfly` | `BrutalflyEnable != 0` (`:4498`) | `ambient` → `AMBIENT` | 2 | 1-1 | megaTaigaHills, extremeHillsPlus, mesaPlateau | minecraft:old_growth_pine_taiga, minecraft:windswept_forest, minecraft:badlands | `:4499-4501` |
| `WaterDragon` | `water_dragon` | `WaterDragonEnable != 0` (`:4503`) | `waterCreature` → `WATER_CREATURE` | 5 | 1-1 | river | minecraft:river | `:4504` |
| `WaterDragon` | `water_dragon` | `WaterDragonEnable != 0` (`:4503`) | `waterCreature` → `WATER_CREATURE` | 3 | 1-1 | swampland | minecraft:swamp | `:4505` |
| `WaterDragon` | `water_dragon` | `WaterDragonEnable != 0` (`:4503`) | `waterCreature` → `WATER_CREATURE` | 2 | 1-1 | ocean, stoneBeach | minecraft:ocean, minecraft:stony_shore | `:4506-4507` |
| `SeaMonster` | `sea_monster` | `SeaMonsterEnable != 0` (`:4509`) | `waterCreature` → `WATER_CREATURE` | 4 | 1-1 | ocean | minecraft:ocean | `:4510` |
| `SeaMonster` | `sea_monster` | `SeaMonsterEnable != 0` (`:4509`) | `waterCreature` → `WATER_CREATURE` | 2 | 1-1 | swampland | minecraft:swamp | `:4511` |
| `SeaViper` | `sea_viper` | `SeaViperEnable != 0` (`:4513`) | `waterCreature` → `WATER_CREATURE` | 3 | 1-1 | ocean | minecraft:ocean | `:4514` |
| `SeaViper` | `sea_viper` | `SeaViperEnable != 0` (`:4513`) | `waterCreature` → `WATER_CREATURE` | 2 | 1-1 | stoneBeach | minecraft:stony_shore | `:4515` |
| `Crab` | `crab` | `CrabEnable != 0` (`:4517`) | `waterCreature` → `WATER_CREATURE` | 2 | 3-6 | ocean | minecraft:ocean | `:4518` |
| `Crab` | `crab` | `CrabEnable != 0` (`:4517`) | `waterCreature` → `WATER_CREATURE` | 1 | 3-6 | swampland | minecraft:swamp | `:4519` |
| `Crab` | `crab` | `CrabEnable != 0` (`:4517`) | `waterCreature` → `WATER_CREATURE` | 1 | 2-4 | stoneBeach | minecraft:stony_shore | `:4520` |
| `AttackSquid` | `attack_squid` | `AttackSquidEnable != 0` (`:4522`) | `waterCreature` → `WATER_CREATURE` | 12 | 6-10 | river | minecraft:river | `:4523` |
| `AttackSquid` | `attack_squid` | `AttackSquidEnable != 0` (`:4522`) | `waterCreature` → `WATER_CREATURE` | 10 | 5-9 | swampland | minecraft:swamp | `:4524` |
| `AttackSquid` | `attack_squid` | `AttackSquidEnable != 0` (`:4522`) | `waterCreature` → `WATER_CREATURE` | 7 | 4-8 | ocean | minecraft:ocean | `:4525` |
| `Lizard` | `lizard` | `LizardEnable != 0` (`:4527`) | `waterCreature` → `WATER_CREATURE` | 5 | 2-4 | river | minecraft:river | `:4528` |
| `Lizard` | `lizard` | `LizardEnable != 0` (`:4527`) | `waterCreature` → `WATER_CREATURE` | 4 | 2-4 | swampland | minecraft:swamp | `:4529` |
| `Lizard` | `lizard` | `LizardEnable != 0` (`:4527`) | `waterCreature` → `WATER_CREATURE` | 2 | 2-4 | ocean | minecraft:ocean | `:4530` |
| `RubberDucky` | `rubber_ducky` | `RubberDuckyEnable != 0` (`:4532`) | `waterCreature` → `WATER_CREATURE` | 10 | 10-20 | river | minecraft:river | `:4533` |
| `RubberDucky` | `rubber_ducky` | `RubberDuckyEnable != 0` (`:4532`) | `waterCreature` → `WATER_CREATURE` | 4 | 4-6 | stoneBeach | minecraft:stony_shore | `:4534` |
| `Basilisk` | `basilisk` | `BasiliskEnable != 0` (`:4536`) | `ambient` → `AMBIENT` | 3 | 1-1 | jungle | minecraft:jungle | `:4537` |
| `Basilisk` | `basilisk` | `BasiliskEnable != 0` (`:4536`) | `ambient` → `AMBIENT` | 2 | 1-1 | jungleHills | minecraft:jungle | `:4538` |
| `Basilisk` | `basilisk` | `BasiliskEnable != 0` (`:4536`) | `ambient` → `AMBIENT` | 4 | 1-2 | birchForestHills | minecraft:birch_forest | `:4539` |
| `Basilisk` | `basilisk` | `BasiliskEnable != 0` (`:4536`) | `ambient` → `AMBIENT` | 15 | 1-2 | roofedForest | minecraft:dark_forest | `:4540` |
| `EmperorScorpion` | `emperor_scorpion` | `EmperorScorpionEnable != 0` (`:4542`) | `ambient` → `AMBIENT` | 1 | 1-1 | desert | minecraft:desert | `:4543` |
| `EmperorScorpion` | `emperor_scorpion` | `EmperorScorpionEnable != 0` (`:4542`) | `ambient` → `AMBIENT` | 2 | 1-1 | savanna | minecraft:savanna | `:4544` |
| `TrooperBug` | `jumpy_bug` | `TrooperBugEnable != 0` (`:4546`) | `ambient` → `AMBIENT` | 3 | 1-2 | swampland | minecraft:swamp | `:4547` |
| `TrooperBug` | `jumpy_bug` | `TrooperBugEnable != 0` (`:4546`) | `ambient` → `AMBIENT` | 1 | 1-1 | mesa | minecraft:badlands | `:4548` |
| `SpitBug` | `spit_bug` | `SpitBugEnable != 0` (`:4550`) | `ambient` → `AMBIENT` | 6 | 1-2 | swampland | minecraft:swamp | `:4551` |
| `StinkBug` | `stink_bug` | `StinkBugEnable != 0` (`:4553`) | `ambient` → `AMBIENT` | 10 | 2-4 | forest | minecraft:forest | `:4554` |
| `StinkBug` | `stink_bug` | `StinkBugEnable != 0` (`:4553`) | `ambient` → `AMBIENT` | 8 | 2-4 | jungle | minecraft:jungle | `:4555` |
| `StinkBug` | `stink_bug` | `StinkBugEnable != 0` (`:4553`) | `ambient` → `AMBIENT` | 6 | 2-4 | forestHills | minecraft:forest | `:4556` |
| `StinkBug` | `stink_bug` | `StinkBugEnable != 0` (`:4553`) | `ambient` → `AMBIENT` | 4 | 2-4 | jungleHills | minecraft:jungle | `:4557` |
| `StinkBug` | `stink_bug` | `StinkBugEnable != 0` (`:4553`) | `ambient` → `AMBIENT` | 8 | 2-5 | savanna | minecraft:savanna | `:4558` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 15 | 3-6 | desert | minecraft:desert | `:4561` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 28 | 2-4 | roofedForest | minecraft:dark_forest | `:4562` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 15 | 3-5 | savanna | minecraft:savanna | `:4563` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 15 | 2-4 | savannaPlateau | minecraft:savanna_plateau | `:4564` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 6 | 1-3 | mesa | minecraft:badlands | `:4565` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 4 | 1-3 | mesaPlateau | minecraft:badlands | `:4566` |
| `Scorpion` | `scorpion` | `ScorpionEnable != 0` (`:4560`) | `ambient` → `AMBIENT` | 5 | 3-6 | mesaPlateau_F | minecraft:wooded_badlands | `:4567` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 5 | 2-6 | jungle | minecraft:jungle | `:4570` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 5 | 1-2 | forest | minecraft:forest | `:4571` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 3 | 2-4 | jungleHills | minecraft:jungle | `:4572` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 3 | 1-2 | forestHills | minecraft:forest | `:4573` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 3 | 3-6 | birchForest | minecraft:birch_forest | `:4574` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 2 | 3-6 | birchForestHills | minecraft:birch_forest | `:4575` |
| `LeafMonster` | `leaf_monster` | `LeafMonsterEnable != 0` (`:4569`) | `ambient` → `AMBIENT` | 2 | 2-5 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4576-4577` |
| `EnderKnight` | `ender_knight` | `EnderKnightEnable != 0` (`:4579`) | `ambient` → `AMBIENT` | 4 | 2-4 | extremeHills, extremeHillsEdge, forest, forestHills, jungleHills | minecraft:windswept_hills, minecraft:forest, minecraft:jungle | `:4580-4584` |
| `EnderKnight` | `ender_knight` | `EnderKnightEnable != 0` (`:4579`) | `ambient` → `AMBIENT` | 2 | 2-4 | plains, river, desert | minecraft:plains, minecraft:river, minecraft:desert | `:4585-4587` |
| `EnderKnight` | `ender_knight` | `EnderKnightEnable != 0` (`:4579`) | `ambient` → `AMBIENT` | 20 | 2-4 | roofedForest | minecraft:dark_forest | `:4588` |
| `EnderReaper` | `ender_reaper` | `EnderReaperEnable != 0` (`:4590`) | `ambient` → `AMBIENT` | 2 | 1-2 | extremeHills, extremeHillsEdge, forestHills, jungleHills | minecraft:windswept_hills, minecraft:forest, minecraft:jungle | `:4591-4595` |
| `EnderReaper` | `ender_reaper` | `EnderReaperEnable != 0` (`:4590`) | `ambient` → `AMBIENT` | 1 | 1-2 | forest, plains, river, desert | minecraft:forest, minecraft:plains, minecraft:river, minecraft:desert | `:4593-4598` |
| `EnderReaper` | `ender_reaper` | `EnderReaperEnable != 0` (`:4590`) | `ambient` → `AMBIENT` | 38 | 2-4 | roofedForest | minecraft:dark_forest | `:4599` |
| `Coin` | `coin` | `CoinEnable != 0` (`:4601`) | `ambient` → `AMBIENT` | 2 | 1-1 | taiga, forest, jungle, birchForest, coldTaiga, megaTaiga | minecraft:taiga, minecraft:forest, minecraft:jungle, minecraft:birch_forest, minecraft:snowy_taiga, minecraft:old_growth_pine_taiga | `:4602-4607` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 3 | 2-4 | forest, jungle | minecraft:forest, minecraft:jungle | `:4610-4612` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 2 | 2-4 | forestHills | minecraft:forest | `:4611` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 2 | 3-5 | jungleHills | minecraft:jungle | `:4613` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 3 | 4-8 | plains | minecraft:plains | `:4614` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 2 | 2-6 | birchForest, birchForestHills | minecraft:birch_forest | `:4615-4616` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 3 | 1-4 | roofedForest | minecraft:dark_forest | `:4617` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 2 | 1-6 | megaTaiga, taiga | minecraft:old_growth_pine_taiga, minecraft:taiga | `:4618-4619` |
| `Cricket` | `cricket` | `CricketEnable != 0` (`:4609`) | `ambient` → `AMBIENT` | 1 | 1-4 | savannaPlateau | minecraft:savanna_plateau | `:4620` |
| `Frog` | `frog` | `FrogEnable != 0` (`:4622`) | `waterCreature` → `WATER_CREATURE` | 20 | 3-6 | river | minecraft:river | `:4623` |
| `Frog` | `frog` | `FrogEnable != 0` (`:4622`) | `ambient` → `AMBIENT` | 3 | 3-6 | river, jungle | minecraft:river, minecraft:jungle | `:4624-4625` |
| `Frog` | `frog` | `FrogEnable != 0` (`:4622`) | `waterCreature` → `WATER_CREATURE` | 20 | 2-6 | swampland | minecraft:swamp | `:4626` |
| `Frog` | `frog` | `FrogEnable != 0` (`:4622`) | `ambient` → `AMBIENT` | 2 | 2-6 | swampland | minecraft:swamp | `:4627` |
| `Peacock` | `peacock` | `PeacockEnable != 0` (`:4629`) | `ambient` → `AMBIENT` | 1 | 1-3 | mesa, mesaPlateau | minecraft:badlands | `:4630-4631` |
| `Fairy` | `fairy` | `FairyEnable != 0` (`:4633`) | `ambient` → `AMBIENT` | 25 | 2-4 | roofedForest | minecraft:dark_forest | `:4634` |
| `Rat` | `rat` | `RatEnable != 0` (`:4636`) | `ambient` → `AMBIENT` | 35 | 10-20 | roofedForest | minecraft:dark_forest | `:4637` |
| `Rat` | `rat` | `RatEnable != 0` (`:4636`) | `ambient` → `AMBIENT` | 25 | 2-8 | taiga | minecraft:taiga | `:4638` |
| `DungeonBeast` | `dungeon_beast` | `DungeonBeastEnable != 0` (`:4640`) | `ambient` → `AMBIENT` | 20 | 2-4 | roofedForest | minecraft:dark_forest | `:4641` |

---

## 6. Datumslogik (Feiertage)

`GregorianCalendar` wird **einmal** in `make_some_more_things` gelesen (`OreSpawnMain.java:4178-4180`). `get(2)` ist der Monat (0-basiert), `get(5)` der Tag. Es gibt keine spätere Neuprüfung: ein Server, der am 30. Oktober startet und durchläuft, hat keine Halloween-Spawns.

| Bedingung im Code | Datum | Wirkung | Quelle |
|---|---|---|---|
| `nowmonth == 9 && nowday == 31` | 31. Oktober | zusätzliche Spawns `GhostSkelly` und `Ghost`, je Gewicht 15, Gruppe 3-6, `ambient`, 22 Biome; unabhängig von `GhostEnable`/`GhostSkellyEnable` | `OreSpawnMain.java:4181-4226` |
| `nowmonth == 1 && nowday == 14` | 14. Februar | `valentines_day = 1`; gelesen von `Girlfriend`, `MyEntityAIFollowOwner`, `MyEntityAITarget`, `MyValentineTarget`, `RenderGirlfriend`, `Shoes` | `OreSpawnMain.java:4227-4229` |
| `nowmonth == 3 && nowday == 20` | 20. April | `easter_day = 1`; einzige Verwendung ist der Spawn-Guard von `EasterBunny` | `OreSpawnMain.java:4230-4232`, `:4341` |

Abweichung zur Recherche: `docs/research/01-mobs.md:891` und `:1106` sagen „nur an Ostern“. Der Code prüft das feste Datum 20. April beim Start; der Code gilt.

**Port:** Datumsprüfung beim Serverstart (`ServerAboutToStartEvent`) auswerten, weil Biome-Modifier ebenfalls dort angewendet werden. Das entspricht dem Original, das auch nur beim Start prüft.

---

## 7. Dimensionen und Biom-IDs

| Feld | Wert | Provider (`registerProviderType(id, class, true)`) | Name (manifest) | Quelle |
|---|---|---|---|---|
| `DimensionID` | `BaseDimensionID + 0` = 80 | `WorldProviderOreSpawn` | `Dimension-Utopia` | `:1266`, `:5039-5040` |
| `DimensionID2` | `+1` = 81 | `WorldProviderOreSpawn2` | `Dimension-Extreme` | `:1267`, `:5041-5042` |
| `DimensionID3` | `+2` = 82 | `WorldProviderOreSpawn3` | `Dimension-VillageMania` | `:1268`, `:5043-5044` |
| `DimensionID4` | `+3` = 83 | `WorldProviderOreSpawn4` | `Dimension-Islands` | `:1269`, `:5045-5046` |
| `DimensionID5` | `+4` = 84 | `WorldProviderOreSpawn5` | `Dimension-Crystal` | `:1270`, `:5047-5048` |
| `DimensionID6` | `+5` = 85 | `WorldProviderOreSpawn6` | `Dimension-Chaos` | `:1271`, `:5049-5050` |

`BaseDimensionID` Default 80 (`:1138`). Jede Dimension: `DimensionManager.registerProviderType(id, provider, true)` (drittes Argument `keepLoaded`), dann `DimensionManager.registerDimension(id, id)`.

Biom-IDs (`:1261-1265`, `BaseBiomeID` Default 120 bei `:1137`): `BiomeUtopiaID` = 120, `BiomeIslandsID` = 121, `BiomeCrystalID` = 122, `BiomeVillageID` = 123, `BiomeChaosID` = 124. Die Biome selbst werden in `OreSpawnMain` nicht konstruiert (Verbraucher siehe §9).

**Port:** sechs Datapack-Dimensionen mit `dimension_type` und Java-`ChunkGenerator`; die Zahlen 80-85 und 120-124 entfallen. offen: 1.21.1 hat kein direktes Gegenstück zu `keepLoaded = true`.

---

## 8. Welt-Generator, Proxies, Netzwerk, GUI, TileEntity

| Aufruf | Server (`CommonProxyOreSpawn`) | Client (`ClientProxyOreSpawn`) | Quelle |
|---|---|---|---|
| `registerWorldGenerator(OreSpawnGen, 10)` | `OreSpawnWorld`, Gewicht 10 | gleich | `OreSpawnMain.java:5035` |
| `proxy.registerSoundThings()` | leer | `MinecraftForge.EVENT_BUS.register(new OreSpawnSounds())` | `OreSpawnMain.java:1272`, `ClientProxyOreSpawn.java:160-162` |
| `proxy.registerRenderThings()` | leer | `EVENT_BUS.register(new GirlfriendOverlayGui(...))` + alle Renderer | `OreSpawnMain.java:5036`, `ClientProxyOreSpawn.java:14-157` |
| `proxy.registerKeyboardInput()` | leer | `KeyHandler` auf dem FML-Bus, `OreSpawnMain.MyKeyhandler = k` | `OreSpawnMain.java:5037`, `ClientProxyOreSpawn.java:165-169` |
| `proxy.registerNetworkStuff()` | Kanal `"RiderControls"`, `RiderControlMessage` mit Discriminator 0, verarbeitet von `RiderControlMessageHandler` auf `Side.SERVER` | `super` + `RiderControl` auf dem FML-Bus | `OreSpawnMain.java:5038`, `CommonProxyOreSpawn.java:24-26`, `ClientProxyOreSpawn.java:172-175` |
| `registerTileEntity` | `TileEntityCrystalFurnace` als `"TileEntityCrystalFurnace"` | gleich | `OreSpawnMain.java:5063` |
| `registerGuiHandler` | `OreSpawnGUIHandler` | gleich | `OreSpawnMain.java:5064` |

Event-Handler: `OreSpawnMain` enthält keinen `@SubscribeEvent` und keinen `FMLCommonHandler`-Aufruf (0 Treffer); die einzigen `@SubscribeEvent`-Klassen des Mods sind `GirlfriendOverlayGui`, `KeyHandler`, `RiderControl` (alle client, §8.1). Chat-Ausgaben (`ChatComponentText`, 19 Treffer) gibt es nur in 10 Entity-Klassen (`Boyfriend`, `Dragon`, `Girlfriend`, `Spyro`, `Termite`, `TheKing`, `ThePrince`, `ThePrinceAdult`, `ThePrincess`, `ThePrinceTeen`), keine in `OreSpawnMain`. Im ganzen Quellbaum (`reference/src-20.2`) gibt es 0 Treffer für `PlayerLoggedInEvent`, `PlayerEvent`, `LivingDeathEvent`, `LivingHurtEvent`, `EntityJoinWorldEvent`, `WorldEvent`, `ServerTickEvent`, `WorldTickEvent`, `PlayerTickEvent`, `FMLServerStartingEvent`, `registerCommand`, `ICommand`: keine Login-Nachricht, kein Server-Tick-Handler, keine Befehle.

### 8.1 Event-, Tick- und Tastenhandler (über die Proxies angemeldet)

| Klasse | Bus / Anmeldung | Event | Verhalten | Quelle |
|---|---|---|---|---|
| `RiderControl` | FML-Bus, `ClientProxyOreSpawn.java:174` (in `registerNetworkStuff`) | `TickEvent.ClientTickEvent` | liest `KeyHandler.KEY_FLY_UP.getIsKeyPressed()` → Zustand 1, sonst 0; nur bei Änderung `network.sendToServer(RiderControlMessage)`. Keine Phasenprüfung, läuft also in START und END jedes Client-Ticks | `RiderControl.java:19-31` |
| `KeyHandler` | FML-Bus, `ClientProxyOreSpawn.java:167`; Instanz in `OreSpawnMain.MyKeyhandler` (`:168`) | `InputEvent.KeyInputEvent` | Rumpf leer (`KeyHandler.java:20-22`); Konstruktor registriert `KEY_FLY_UP = new KeyBinding("OreSpawn UP/FAST", 56, "key.categories.orespawn")` (`KeyHandler.java:17`, `:25`). Tastencode 56 ist LWJGL-2 `KEY_LMENU` (linke Alt-Taste); die Konstante ist nicht im Repo nachgesehen | `KeyHandler.java:15-26` |
| `GirlfriendOverlayGui` | `MinecraftForge.EVENT_BUS`, `ClientProxyOreSpawn.java:15` | `RenderGameOverlayEvent`, nur nicht-cancelable und `ElementType.HOTBAR` | HUD-Overlay; schreibt `current_dimension` und `FastGraphicsLeaves` (§9) | `GirlfriendOverlayGui.java:23-50` |
| `OreSpawnSounds` | `MinecraftForge.EVENT_BUS`, `ClientProxyOreSpawn.java:161` | `SoundLoadEvent` | `onSound` hat **kein** `@SubscribeEvent` und einen leeren Rumpf (`OreSpawnSounds.java:7-8`): wirkungslos, im Port entfällt die Klasse | `OreSpawnSounds.java:1-9` |
| `RiderControlMessageHandler` | Kanal `"RiderControls"`, Discriminator 0, `Side.SERVER` (`CommonProxyOreSpawn.java:25`) | `RiderControlMessage` | Nachricht = 1 Byte `keystate` (`readUnsignedByte`/`writeByte`, `RiderControlMessage.java:15-21`); auf Client-Seite `return null`, auf Server-Seite `OreSpawnMain.flyup_keystate = message.keystate` (global, siehe §9) | `RiderControlMessageHandler.java:13-19` |

**Port (8.1):** Taste über `RegisterKeyMappingsEvent` (Client, Mod-Bus), Abfrage in `ClientTickEvent.Post` (einmal pro Tick statt zweimal), Versand als C2S-`CustomPacketPayload`; serverseitig den Zustand am Spieler oder am gerittenen Entity ablegen, nicht global. Alles davon unter `com.swbr.orespawn.client`, nur Payload-Typ und Server-Handler im gemeinsamen Code.

**Port:** Kanal `RiderControls` → `CustomPacketPayload` C2S über `RegisterPayloadHandlersEvent`; `TileEntityCrystalFurnace` → `BlockEntityType`; `OreSpawnGUIHandler` → `MenuType`. `ClientProxy`-Inhalt nach `com.swbr.orespawn.client` (Renderer, Overlay, Keybinds, Sounds).

---

## 9. Globale Laufzeit-Statics und Hilfsfunktionen

| Static | Wert / Schreiber | Deklaration | gelesen von (andere Klassen) |
|---|---|---|---|
| `instance` | `@Mod.Instance("OreSpawn")` | `:29-30` | `CrystalFurnace`, `CrystalWorkbench` |
| `proxy` | `@SidedProxy` | `:27-28` | — |
| `godzilla_has_spawned` | Default 0 (`:6221`); gesetzt in `Godzilla.java:257`, `:586` | `:48` | `Godzilla` |
| `current_dimension` | Default 0 (`:6222`); gesetzt clientseitig `GirlfriendOverlayGui.java:45` | `:49` | `CrystalGrass`, `GirlfriendOverlayGui`, `OreBasicStone` |
| `valentines_day` | Default 0 (`:6223`); 1 am 14.02. (`:4228`) | `:50` | `Girlfriend`, `MyEntityAIFollowOwner`, `MyEntityAITarget`, `MyValentineTarget`, `RenderGirlfriend`, `Shoes` |
| `easter_day` | Default 0 (`:6224`); 1 am 20.04. (`:4231`) | `:51` | — |
| `flyup_keystate` | Default 0 (`:6205`); gesetzt `RiderControlMessageHandler.java:17` | `:32` | `Cephadrome`, `Dragon`, `Elevator`, `Leon`, `Ostrich`, `RiderControlMessageHandler`, `ThePrinceAdult`, `ThePrinceTeen` |
| `MyKeyhandler` | Default `null` (`:6204`); gesetzt `ClientProxyOreSpawn.java:168` | `:31` | `ClientProxyOreSpawn`, `RiderControl` |
| `FastGraphicsLeaves` | Default 0 (`:6353`); gesetzt `GirlfriendOverlayGui.java:47/50` | `:180` | `BlockAppleLeaves`, `BlockCrystalLeaves`, `BlockExperienceLeaves`, `BlockScaryLeaves`, `GirlfriendOverlayGui` |
| `OreSpawnRand` | `new Random(151L)` (`:6366`) | `:194` | `Alien`, `Alosaurus`, `AntBlock`, `AntRobot`, `AttackSquid`, `BandP`, `Basilisk`, `Beaver`, `Bee`, `BlockButterflyPlant`, `BlockCorn`, `BlockFireflyPlant` … (+62) |
| `OreSpawnTrees` | `postInit` (`:5445`) | `:195` | `BlockDuplicatorLog`, `BlockExperiencePlant`, `DungeonSpawnerBlock`, `IslandToo`, `OreSpawnWorld` |
| `BMaze` | `postInit` (`:5442`) | `:196` | `DungeonSpawnerBlock`, `OreSpawnWorld` |
| `RubyDungeon` | `postInit` (`:5443`) | `:197` | `DungeonSpawnerBlock`, `OreSpawnWorld` |
| `MyDungeon` | `postInit` (`:5444`) | `:198` | `DungeonSpawnerBlock`, `OreSpawnWorld` |
| `OreSpawnUtils` | `postInit` (`:5446`) | `:199` | `Alosaurus`, `AntRobot`, `Basilisk`, `Brutalfly`, `CaterKiller`, `CaveFisher`, `Crab`, `DungeonBeast`, `EmperorScorpion`, `GammaMetroid`, `GiantRobot`, `Godzilla` … (+33) |
| `Chunker` | `postInit` (`:5447`) | `:200` | `ChunkProviderOreSpawn`, `ChunkProviderOreSpawn2`, `ChunkProviderOreSpawn3`, `ChunkProviderOreSpawn6` |
| `UltimateFishingRod` | `new ItemStack(MyUltimateFishingRod)` (`:1370`) | `:450` | — |
| `BiomeUtopiaID` | `BaseBiomeID` (`:1261`) | `:37` | `WorldProviderOreSpawn` |
| `BiomeIslandsID` | `BaseBiomeID + 1` (`:1262`) | `:38` | `WorldProviderOreSpawn4` |
| `BiomeCrystalID` | `BaseBiomeID + 2` (`:1263`) | `:39` | `WorldProviderOreSpawn5` |
| `BiomeVillageID` | `BaseBiomeID + 3` (`:1264`) | `:40` | `WorldProviderOreSpawn3` |
| `BiomeChaosID` | `BaseBiomeID + 4` (`:1265`) | `:41` | `WorldProviderOreSpawn6` |
| `DimensionID` | `BaseDimensionID` (`:1266`) | `:42` | `EntityAnt`, `OreSpawnWorld`, `WorldProviderOreSpawn` |
| `DimensionID2` | `+1` (`:1267`) | `:43` | `EntityRedAnt`, `OreSpawnWorld`, `WorldProviderOreSpawn2` |
| `DimensionID3` | `+2` (`:1268`) | `:44` | `EntityRainbowAnt`, `OreSpawnWorld`, `WorldProviderOreSpawn3` |
| `DimensionID4` | `+3` (`:1269`) | `:45` | `Alien`, `Bee`, `BlockAppleLeaves`, `BlockCrystalLeaves`, `Cockateil`, `Dragon`, `EntityButterfly`, `EntityLunaMoth`, `EntityUnstableAnt`, `Firefly`, `GammaMetroid`, `LeafMonster` … (+3) |
| `DimensionID5` | `+4` (`:1270`) | `:46` | `AttackSquid`, `Crab`, `CrystalGrass`, `DungeonBeast`, `Frog`, `ItemMinersDream`, `OreBasicStone`, `OreSpawnWorld`, `Rat`, `RockBase`, `Termite`, `WorldProviderOreSpawn5` |
| `DimensionID6` | `+5` (`:1271`) | `:47` | `CreepingHorror`, `EntityButterfly`, `LurkingTerror`, `Mantis`, `OreSpawnWorld`, `PitchBlack`, `TerribleTerror`, `WorldProviderOreSpawn6` |

Probleme für einen Server mit mehreren Spielern (Beobachtung aus dem Code):
- `flyup_keystate` ist **ein** globaler Wert: `RiderControlMessageHandler.java:17` schreibt den Tastenzustand des zuletzt sendenden Spielers, alle Reittiere lesen ihn. Im Port pro Spieler bzw. am gerittenen Entity halten.
- `current_dimension` und `FastGraphicsLeaves` werden clientseitig in `GirlfriendOverlayGui.java:45-50` geschrieben, aber von gemeinsamem Code gelesen (`CrystalGrass`, `OreBasicStone`, Blätterklassen). Auf einem dedizierten Server bleiben sie 0. Im Port darf gemeinsamer Code keinen Client-Zustand lesen.
- `OreSpawnRand = new Random(151L)` (`:6366`) wird von 74 anderen Klassen geteilt, darunter Welt-Generator, Pflanzen und Entities. In 1.21.1 laufen Chunk-Generierung und Ticks parallel: `RandomSource` aus Level oder Kontext verwenden.

### 9.1 Hilfsfunktionen

| Methode | Verhalten | Aufrufer | Quelle |
|---|---|---|---|
| `getPointedAtEntity(world, player, dist)` | Strahl von `player.getPosition(1.0f)` entlang `getLook(1.0f) × dist`; prüft alle Entities in der um `f1 = 1.0f` erweiterten, entlang des Blickvektors gestreckten Spieler-AABB; `canBeCollidedWith()` Pflicht; AABB je Entity um `getCollisionBorderSize()` erweitert; Treffer im Inneren gewinnt mit Distanz 0, sonst die nächste `calculateIntercept`-Distanz; das eigene Reittier nur, wenn `canRiderInteract()` oder sonst nichts näher liegt | `GirlfriendOverlayGui` | `OreSpawnMain.java:5455-5497` |
| `setBlockFast(world, x, y, z, block, meta, flags)` | Abbruch außerhalb x/z ∈ [−30000000, 30000000) oder y ∉ [0, 255]; schreibt direkt in `ExtendedBlockStorage` (kein `World.setBlock`: kein Licht-Update, kein `onBlockAdded`, keine TileEntity-Behandlung); `flags & 1` → alter Block wird gelesen und serverseitig `notifyBlockChange`; `flags & 2` → `markBlockForUpdate`, außer client und `flags & 4` | `BasiliskMaze`, `BlockAppleLeaves`, `BlockCrystalPlant`, `BlockScaryLeaves`, `GenericDungeon`, `Island`, `IslandToo`, `NightmareDungeon`, `OreSpawnWorld`, `RubyBirdDungeon`, `Trees` | `OreSpawnMain.java:5499-5524` |
| `setBlockSuperFast(..., refChunk)` | wie `setBlockFast`; liegt der Zielchunk gleich `refChunk`, wird ohne jede Benachrichtigung geschrieben und `true` geliefert | `ItemAppleSeed`, `ItemMagicApple` | `OreSpawnMain.java:5526-5557` |
| `setBlockIDWithMetadataFast(chunk, lx, y, lz, block, meta)` | legt fehlende `ExtendedBlockStorage` an (Himmelslicht = `!provider.hasNoSky`), außer der Block ist Luft; dann `func_150818_a` (MCP: `setExtBlockID`, `reference/jar/mcp/methods.csv:1687`) und `setExtBlockMetadata`. Markiert den Chunk **nicht** als geändert | intern | `OreSpawnMain.java:5559-5582` |
| `getBlockIDInChunk(chunk, x, y, z)` | Luft, wenn Koordinate außerhalb der Weltgrenze, außerhalb dieses Chunks oder y ∉ [0, 255] | `ChunkOreGenerator`, `ChunkProviderOreSpawn4/5/6` | `OreSpawnMain.java:5584-5598` |
| `setBlockIDWithMetadataInChunk(chunk, x, y, z, block, meta)` | wie oben mit Chunk-Prüfung; `null` oder Luft legt keine neue Storage an | `ChunkOreGenerator`, `ChunkProviderOreSpawn4/5/6`, `CrystalMaze` | `OreSpawnMain.java:5600-5631` |

**Port:** Worldgen-Aufrufer schreiben in `ChunkAccess`/`WorldGenLevel` innerhalb der Bounding Box (Guardrail „Structure pieces“); Laufzeit-Aufrufer (`ItemMagicApple`, Bäume) verwenden `level.setBlock(pos, state, flags)` mit `Block.UPDATE_CLIENTS` bzw. `UPDATE_ALL`. Das fehlende Dirty-Markieren des Originals nicht nachbauen.

---

## 10. Port-Notizen (NeoForge 1.21.1)

- Modid `OreSpawn` → `orespawn`; Registry-Namen laut manifest; Sprachdatei `en_us.json` aus manifest `name`.
- Config → `ModConfigSpec` mit denselben Kategorien und Schlüsseln. Die relativen Klemmungen (Default/2 … Default×2, nur Untergrenze, `harvestlevel`-Sonderfall, `maxdepth−mindepth<10`-Reset, `defense`-Doppelklemmung) passen nicht in `defineInRange` mit festen Grenzen, wo die Obergrenze fehlt; beim Lesen nachbilden. `LessLag`-Kopplung beim Lesen anwenden.
- Registrierungszeit-Werte (Tool-Tier, `ArmorMaterial`) hängen im Original an der Config. `ModConfig.Type.STARTUP` existiert in FML 4.0.43 (§1); offen bleibt nur, ob die Werte vor den Registry-Suppliern verfügbar sind. Fallback: feste Defaults beim Registrieren plus Laufzeitabfrage.
- `ArmorMaterial` ist in 1.21.1 ein Registry-Eintrag; Haltbarkeit wird pro Item-Typ berechnet. In 1.21.1 gilt `ArmorItem.Type.getDurability(factor) = durability × factor` mit den Basiswerten HELMET 11, CHESTPLATE 16, LEGGINGS 15, BOOTS 13 (`net/minecraft/world/item/ArmorItem.java:150-153`, `:167-168`, neoforge-21.1.248-sources.jar). offen: dass der 1.7.10-Parameter `durability` von `EnumHelper.addArmorMaterial` derselbe Faktor mit denselben Basiswerten ist, liegt im Repo nur als Bytecode (`reference/jar/mcp/client-1.7.10.jar`) vor und ist nicht nachgesehen.
- Mob-Leben über 1024 (Guardrail): Defaults `TheKing` 7000, `TheQueen` 6000, `Godzilla`/`Mobzilla` 4000, `SpiderRobot` 1500 (alle `OreSpawnMain.java:6146-6194`); per Config verdoppelbar, dann auch `Kraken` (1000 → 2000) und `Jeffery` (550 → 1100). Virtuelles Leben nötig; die Originalzahl bleibt Referenz.
- Spawns: ein eigener Biome-Modifier-Typ (Codec + Java), der beim Anwenden Config-Guard und Datum auswertet; Datapack-JSON allein kann weder `*Enable` noch das Datum prüfen. Zusätzlich `RegisterSpawnPlacementsEvent` je Typ.
- Entities: 134 `EntityType`; drei ohne Mod-Registrierung brauchen im Port trotzdem Tracking-Werte (offen, §4).
- Dispenser: `DispenserBlock.registerBehavior` in `FMLCommonSetupEvent.enqueueWork`; doppelte `LizardEgg`-Registrierung ist harmlos (der zweite Eintrag gewinnt).
- Kristallofen an/aus → ein Block mit `LIT`-Property statt zwei Registry-Einträgen (manifest-Problem `crystalfurnace`).
- Keine Mixins nötig: nichts in `OreSpawnMain` greift in Vanilla-Klassen ein; `setBlockFast` umgeht nur öffentliche Chunk-API.
- Client-Code (`ClientProxyOreSpawn`, `KeyHandler`, `RiderControl`, `GirlfriendOverlayGui`, `OreSpawnSounds`) nach `com.swbr.orespawn.client`; gemeinsame Klassen dürfen keine Client-Statics lesen (§9).
