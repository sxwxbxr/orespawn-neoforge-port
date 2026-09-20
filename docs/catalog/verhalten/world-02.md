# Verhalten: world-02

Dieser Batch umfasst die Weltgenerierung von OreSpawn, soweit sie nicht in den Bauklassen
(`GenericDungeon` = world-01, `Trees`/`BasiliskMaze`/`RubyBirdDungeon` = world-04) oder in den
Dimensions-Providern (world-03) steckt. `OreSpawnWorld` ist der einzige `IWorldGenerator` des Mods
und verteilt pro Chunk-Populate Pflanzen, Ameisennester, Erze, Felsen und rund 60 Strukturaufrufe auf
Oberwelt, Nether, End und die sechs eigenen Dimensionen. `ChunkOreGenerator` schreibt Erze direkt in
das Block-Array eines frisch erzeugten Chunks (nur in vier eigenen Dimensionen). `MapGenMoreVillages`
verdichtet Vanilla-Dörfer für die VillageMania-Dimension auf ein 9er-Raster. `OreSpawnTeleporter` ist
die portallose Landelogik, die alle sechs Ameisen/Schmetterlings-Reisen benutzt und zahme Begleiter
mitnimmt. `BiomeGenUtopianPlains` ist die eine Biomklasse aller fünf Einbiom-Dimensionen und trägt deren
Spawnlisten und Dekorationszahlen. Portierungskern: kleine Dinge werden Java-`Feature`s über Biome
Modifier, alles mit Prüfflächen jenseits der 3×3-Chunk-Region wird `Structure`, der globale Zähler
`recently_placed` ist nicht 1:1 übertragbar, und die Spawnlisten werden Biom-JSON plus
konfigurationsabhängiger Java-`BiomeModifier`.

Allgemeine Konstanten, die alle Abschnitte benutzen:

| Größe | Wert | Herkunft |
|---|---|---|
| `BaseDimensionID` | 80 → Utopia 80, Extreme/Mining 81, VillageMania 82, Islands 83, Crystal 84, Chaos 85 | (manifest), (OreSpawnMain.java:1138, 1266-1271) |
| `BaseBiomeID` | 120 → Utopia 120, Islands 121, Crystal 122, Villages 123, Chaos 124 | (manifest), (OreSpawnMain.java:1137, 1261-1265) |
| Dimensionsnamen | `Dimension-Utopia`, `-Extreme`, `-VillageMania`, `-Islands`, `-Crystal`, `-Chaos` | (manifest dimensions) |
| `LessOre`, `LessLag`, `DisableOverworldDungeons` | Default 0, 0, 0; `LessLag` wird auf 0..2 geklemmt, `LessLag == 2` erzwingt `LessOre = 1` | (manifest), (OreSpawnMain.java:1221-1226, 1235-1242) |
| Registrierung `OreSpawnWorld` | `GameRegistry.registerWorldGenerator(..., 10)` | (OreSpawnMain.java:5035) |
| `OreSpawnMain.OreSpawnRand` | `new Random(151L)` | (OreSpawnMain.java:6366) |

---

### OreSpawnWorld

**Rolle.** `implements IWorldGenerator`; Forge ruft `generate()` beim Populate jedes Chunks in jeder
Dimension auf, nach der Vanilla-Dekoration (Gewicht 10, OreSpawnMain.java:5035). Nur serverseitig
(`world.isRemote` → return, OreSpawnWorld.java:21). Alle Koordinaten sind Blockkoordinaten der
Chunk-Ecke (`chunkX * 16`), Zufallspositionen liegen bei `chunk + nextInt(16)` **ohne** den
Vanilla-Versatz +8; Bauten ragen deshalb nach +X/+Z in Nachbar-Chunks.

#### Der globale Sperrzähler `recently_placed`

- `public static int`, beim Klassenladen 50 (OreSpawnWorld.java:2894-2896).
- Jeder `generate()`-Aufruf in **jeder** Dimension (auch fremde Mod-Dimensionen) zieht 1 ab, solange > 0 (OreSpawnWorld.java:25-27).
- Große Bauten setzen ihn auf 50, King/Queen Altar auf 100 (OreSpawnWorld.java:2743). Viele Aufrufer prüfen `recently_placed == 0`.
- Nicht persistent, nicht pro Dimension, abhängig von der Reihenfolge der Chunk-Erzeugung.

#### Verteiler `generate()` je Dimension (OreSpawnWorld.java:20-213)

| Dimension | Ablauf | Zeilen |
|---|---|---|
| Utopia (`DimensionID`) | `generateSurface`; dann `addHugeTree`. Nur wenn kein Riesenbaum: `addAppleTrees` → sonst `addOtherTrees` → sonst, falls `recently_placed == 0`, `addKingAltar`; danach `addVeggies` (zweiter Aufruf, der erste steckt in `generateSurface`). Immer: `addRubyDungeon`, bei Misserfolg `addGenericDungeon` | 28-42 |
| Extreme/Mining (`DimensionID2`) | `generateRuby` 1×, mit `LessOre == 0` weitere 2× plus Lapis (45 Adern Größe 7, 25 Adern Größe 4, jeweils nur bei Y < 50, `WorldGenMinable`). Dann: `recently_placed == 0 && nextInt(95) == 1` → `nextInt(7)` wählt Basilisk Maze / Kyuubi / Bee Hive / Shadow / Alien WTF / Ender Knight / Leon Nest; sonst `addGenericDungeon`. Immer: `addLavaAndWater`, `addAnts(2)` 2×, `addMosquitos` 2×, `addVeggies`, `addRocks` | 43-100 |
| VillageMania (`DimensionID3`) | wenn `MosquitoEnable`: `addMosquitos(world, random, chunkX, chunkZ)` — **Fehler im Original: Chunk-Index statt Blockkoordinate**, die Pflanzen landen nahe dem Ursprung. Dann `addAnts(4)`, `addAppleTrees`, `addGenericDungeon`, und jeweils bei `recently_placed == 0`: `addDamselInDistress`, `addSpiderHangout`, `addRedAntHangout` | 101-118 |
| Islands (`DimensionID4`) | `recently_placed == 0 && nextInt(100) == 0 && D4BigSpaceCheck(chunkEcke, 7)` → `nextInt(19)`: 0-2 `addD4Castle`, 3-6 `addD4GenericDungeon`, 7 `addD4EnderCastle`, 8 `addD4IncaPyramid`, 9 `addD4RobotLab`, 10 `addD4Mini`, 11 `addD4RubyDungeon`, 12 `addD4CephadromeAltar`, 13 `addD4Greenhouse`, 14 `addD4NightmareRookery`, 15 `addD4StinkyHouse`, 16 `addD4WhiteHouse`, 17 `addPumpkin`, 18 `addD4Rainbow`. Unabhängig davon `nextInt(300) == 0` → `addD4CloudShark`. Immer: `addUnstableAnts`, `addIslands`, `addD4Rocks` | 119-175 |
| Crystal (`DimensionID5`) | `addFairyTree`; nur wenn er `false` liefert: `addCrystalTermites`, und bei `recently_placed == 0` die Kette `addRotatorStation` → `addUrchinSpawner` → `addCrystalHauntedHouse` → `addRoundRotator` → `addCrystalBattleTower` (erster Erfolg bricht ab), danach `addIrukandji`. Immer: `addCrystalChestsAndSpawners`; `world.rand.nextInt(4) == 1` → `addRocks` | 176-191 |
| Chaos (`DimensionID6`) | `addButterfliesAndMoths`, `addVeggies`, `addAnts(2)` | 192-197 |
| Nether (-1) | `generateNether` | 199-202, 232-257 |
| Oberwelt (0) | `generateSurface`, `generateOres` | 203-207 |
| End (1) | `generateEnd`: `addEndAnts` (leer, 1555-1556), dann `world.rand.nextInt(4)`: 0 `addEndKnights`, 1 `addEndReapers`, 2 `addHospital`, 3 `addEnderCastle` | 208-230 |

`generateSurface` (259-314): `addStrawberries`, `addCorn`, `addTomatoes`, `addVeggies`,
`addButterfliesAndMoths`, `addMosquitos` (wenn `MosquitoEnable`). Nur für **Dimension 0** und
`DisableOverworldDungeons == 0` und `recently_placed == 0`: `world.rand.nextInt(6)` wählt einen von
PlayPool / WaterDragonLair / GoldFishBowl / GirlfriendIsland / MonsterIsland / FrogPond, und
**zusätzlich** läuft die Kette `addANest` → `addHauntedHouse` → `addLeafMonster` → `addSpitBug` →
`addIgloo` → `addBouncyCastle` → `addRubberDuckyPond` bis zum ersten Erfolg (269-308). Danach
`addAnts(4)`; `addRocks` nur in Biomen namens `River`, `Extreme Hills`, `Desert` (309-313).

Abweichung zur Recherche: 02-dimensions-worldgen.md:115-117 zählt für Utopia „Ozean-/Plains-Minidungeons“
und „Steine“ auf; der Code sperrt die Minidungeons auf `dimensionId == 0` (OreSpawnWorld.java:269) und
die Steine auf drei Vanilla-Biomnamen (311), in Utopia entsteht beides nicht.

#### Kleine Features (Pflanzen, Nester, Einzelblöcke, Entities)

Spalte „Suche“: Startet am oberen Y und läuft abwärts; „solange Luft“ heißt, die Schleife bricht am
ersten Nicht-Luft-Block ab. Platziert wird mit `setBlockFast(..., meta 0, flag 2)`, falls nicht anders
genannt. Biomnamen sind die 1.7.10-`biomeName`-Strings, per `equals` verglichen (Mutationen wie
„... M“ fallen nie darunter).

| Methode (Zeile) | Chance/Chunk | Versuche | Suche | Platzierung | Bedingung / Ort |
|---|---|---|---|---|---|
| `addStrawberries` (963) | `nextInt(20) == 0` | 5 | Y 100→41 solange Luft | `strawberry_plant` auf `grass` | Utopia; Biome `Forest`, `ForestHills`, `Birch Forest`, `Birch Forest Hills` |
| `addCorn` (1023) | `nextInt(35) == 1` | 6 (`LessLag` 1: 5, 2: 3) | Y 100→41 solange Luft, 9 Luftblöcke darüber | Höhe `nextInt(5)+1`: 1 → `corn_0`; 2 → `corn_1` + `corn_0`; >2 → `corn_1`, `corn_3` bis h-1, `corn_0` oben | Utopia, `DimensionID3` (tot, dort kein `generateSurface`), Biom `Plains` |
| `addTomatoes` (1078) | `nextInt(70) == 1` | 5 | wie Corn | Höhe `nextInt(3)+1`: 1 → `tomato_0`; 2 → `tomato_1` + `tomato_0`; 3 → `tomato_2`, `tomato_3`, `tomato_0` | wie Corn |
| `addButterfliesAndMoths` (1126) | `nextInt(10 + 2·LessLag) == 0` | 4 | Y 100→41 solange Luft, auf `grass` | `nextInt(3)`: `butterfly_plant` / `moth_plant` / `firefly_plant` | Utopia, Chaos; Biome `Forest`, `ForestHills`, `River`, `Jungle`, `JungleHills`, `Swampland`, `Birch Forest`, `Birch Forest Hills`, `Roofed Forest` |
| `addMosquitos` (1455) | `nextInt(25 + 2·LessLag) == 0`; in Utopia/VillageMania zusätzlich nur 1/3 | 2 | Y 100→41 solange Luft | `mosquito_plant` auf `grass` | Utopia, Mining, VillageMania; Biome `Jungle`, `Swampland` |
| `addNetherMosquitos` (1475) | `nextInt(25) == 0` | 3 | Y 90→21 | `mosquito_plant` auf `netherrack` | Nether, `MosquitoEnable` |
| `addNetherAnts` (1491) | `nextInt(25) == 0` | 3 | Y 90→21 | ersetzt `netherrack` durch `redantblock` | Nether, `RedAntEnable` |
| `addAnts(redfreq)` (1510) | `nextInt(30 + 4·LessLag) == 0` | 4 | Y 100→41 solange Luft | ersetzt `grass`: mit 1/`redfreq` (min. 2) `nextInt(4)` → `redantblock` / `rainbowantblock` / `unstableantblock` / `termiteblock` (nichts, wenn der Typ abgeschaltet ist), sonst `antblock` (`BlackAntEnable`) | Oberwelt/Utopia/VillageMania `redfreq` 4, Mining/Chaos 2; Abbruch, wenn Red/Black/Rainbow/Unstable alle aus |
| `addUnstableAnts` (1622) | `nextInt(30) == 0` | 3 | Y 20→3 solange Luft | ersetzt `grass` durch `unstableantblock` | Islands, `UnstableAntEnable` |
| `addCrystalTermites` (1639) | `nextInt(40) == 0` | 3 | Y 100→51 | ersetzt `crystalgrass` durch `crystaltermiteblock` | Crystal, `TermiteEnable` |
| `addIrukandji` (1757) | `nextInt(80) == 0` | 3 | Y 100→51, Luft über `water` | `mob_spawner` mit Entity `"Irukandji"` (→ `irukandji`, manifest) im Luftblock | Crystal, `IrukandjiEnable` |
| `addCrystalChestsAndSpawners` (1780) + `addCrystalChest` (1820) | immer | bis 3 Proben bei Y = 25, erste Luftposition zählt | Position `1+nextInt(14)`; braucht einen luftigen Nachbarn in +X/−X/+Z/−Z (Richtung → meta 5/4/2/3) | `world.rand.nextInt(3)`: 0 → Truhe mit `1 + world.rand.nextInt(3)` Ziehungen aus `Trees.CrystalChestContentsList`; sonst `mob_spawner` 50:50 `"Dungeon Beast"` (→ `dungeon_beast`) / `"Rat"` (→ `rat`) | Crystal (Y 25 ist Höhlenniveau) |
| `addIslands` (1847) | `nextInt(10 + 2·LessLag) == 1` | 1, Position `2+nextInt(12)` | Y 20→3 solange Luft | `island` auf `grass` | Islands |
| `addVeggies` (1959) | `nextInt(15) == 0` | 8 | Y 100→41 solange Luft | `nextInt(6)`: `carrots`, `potatoes`, `radish_plant`, `lettuce_0`, (4) mit 1/10 `melon_stem`, (5) mit 1/50 `duplicatortreelog` falls `enableduplicatortree` | Utopia, Mining, Chaos; Biome `River`, `Swampland` |
| `addRocks` (2011) | `nextInt(5) == 0` | `3 + nextInt(10)` | Y 110→41 solange Luft, auf `grass`/`sand`/`crystalgrass` | Entity `"Rock"` (→ `rock`, Klasse `RockBase`) bei x+0.5, y+0.01, z+0.5, zufällige Blickrichtung, `playLivingSound` (2871-2892) | `RockEnable`; Oberwelt (3 Biome), Mining, Crystal (1/4) |
| `addD4Rocks` (2029) | `nextInt(7) == 0` | `3 + nextInt(10)` | Y 20→6 solange Luft, auf `grass` | wie oben | Islands |
| `addLavaAndWater` (2605) | `nextInt(5) == 0` | 6, erster Erfolg beendet | Y 128→76 solange Luft; `grass` unten, darunter `dirt`/`stone`; im Ring um den Grasblock mind. ein Luft- **und** ein `dirt`/`stone`/`grass`-Nachbar | 50:50 Wasser (`flowing_water` oben, 2× `water` darunter) oder Lava analog, flag 3 | Mining |
| `generateRuby` (316) | `rate + nextInt(7)` Versuche | — | `Y = nextInt(128)` im Band `mindepth..maxdepth`; von Y abwärts bis 6 die erste `lava`/`flowing_lava` mit `stone` darunter | ersetzt diesen Stein durch `oreruby` (Einzelblock); Suche läuft weiter, wenn unter der Lava kein Stein liegt | Mining (1× bzw. 3×) |

`Trees.CrystalChestContentsList` (Trees.java:817), Format min–max Stückzahl, Gewicht; eine Ziehung
wählt nach Gewicht und legt einen Stapel in einen Zufallsslot:

| Einträge | min–max | Gewicht |
|---|---|---|
| `crystaltermiteblock` | 1–5 | 10 |
| `crystalflower_red`, `_blue`, `_green`, `_yellow`, `crystalplanks`, `crystalstone`, `crystalrat`, `crystalfairy`, `crystalcoal`, `crystalgrass`, `crystalcrystal`, `crystaltorch`, `crystaltreeleaves`, `crystaltreeleaves2`, `crystaltreeleaves3`, `crystaltreelog` | je 1–10 | je 10 |
| `crystalworkbench`, `crystalfurnace` | 1–1 | 10 |
| `tigerseye_block`, `tigerseye` | 1–10 | 5 |
| `crystalwood*`, `crystalpink*`, `crystalstone*` (je sword/axe/shovel/pickaxe/hoe) | 1–1 | 10 |
| `tigerseye_sword/_axe/_shovel/_pickaxe/_hoe` | 1–1 | 5 |
| `tigerseye_ingot` | 1–5 | 5 |
| `crystalpink_ingot`, `crystalapple`, `peacockfeather` | 1–5 | 10 |
| `cookedpeacock`, `rawpeacock`, `rice`, `quinoa` | 1–10 | 20 |
| `pink_helmet/_chest/_leggings/_boots`, `peacock_helmet/_chest/_leggings/_boots` | 1–1 | 10 |
| `tigerseye_helmet/_chest/_leggings/_boots` | 1–1 | 5 |
| `eggrotator`, `eggvortex`, `eggpeacock`, `eggdungeonbeast`, `eggfairy`, `eggrat`, `eggflounder`, `eggwhale`, `eggirukandji`, `eggskate`, `eggurchin`, `eggghost`, `eggghostskelly` | 1–5 | 10 |
| `skatebow`, `ultimatebow`, `ultimatesword` | 1–1 | 2 |
| `irukandjiarrow` | 5–10 | 2 |
| `deadirukandji` | 2–8 | 5 |
| `minecraft:iron_ingot`, `Blocks.log` meta 0 (Eiche) | 1–4 | 10 |
| `minecraft:golden_apple` | 1–5 | 2 |

#### Bäume (Utopia, VillageMania, Crystal)

| Methode (Zeile) | Chance | Versuche / Position | Auswahl | Bauaufruf |
|---|---|---|---|---|
| `addHugeTree` (1902) | `nextInt(50) == 0`; `LessLag` 1: zusätzlich 1/2, 2: 1/4 | 3; `4+nextInt(8)`; Y 127→51, Luft über `grass` | `tree_type = nextInt(4)`, Radius `6 - nextInt(2)`, `no_critters` bei `nextInt(100) > 25`; `rand_treetype = nextInt(100)`: **>75** eckig (Blätter `leaves`, mit `tree_type != 3` und 1/20 `leaves_apple`); **==0** Radius 6, keine Critter, 50:50 Gold/Smaragd/Diamant (Tree of Goodness) oder Obsidian/`blockruby`/`blockamethyst` (Queen's Tree), `tree_type -1`; **16–75** kreisförmig, Radius `6 - nextInt(3)`; **1–15** rund, Radius `6 - nextInt(3)` | `ItemMagicApple.MakeBigSquareTree` (251) / `MakeBigCircularTree` (569) / `MakeBigRoundTree` (676), Stufenblock `mossy_cobblestone`, an `y-1` |
| `addAppleTrees` (1861) | `freq = (|cx|+|cz|) % 15` (Chunk-Index), Chance `1/(15+freq)` | `howmany = 2 + nextInt(2 + (15-freq)/2)`, `LessLag` 1: /2, 2: /4 (< 1 → nichts); Position `2+nextInt(12)`, Y 100→51 solange Luft | `which = nextInt(10)` **einmal pro Chunk**: <8 `leaves_apple`, 8 `leaves_cherry`, 9 `leaves_peach` | `ItemAppleSeed.makeTree` (ItemAppleSeed.java:42) an `y-1` |
| `addOtherTrees` (2672) | `nextInt(30) == 0`; `LessLag` 1: 1/2 und 4 Versuche, 2: 1/4 und 3 | 5; `3+nextInt(10)`; Y 100→51 solange Luft | `nextInt(2)` einmal: `WindTree(dir 0)` bis 4 Stück, sonst `SkyTree` bis 3 | `Trees.WindTree` (Trees.java:41), `Trees.SkyTree` (Trees.java:95); nur Utopia |
| `addFairyTree` (2046) | `nextInt(5) == 0` | Mitte `chunk+8`; Y 128→41 Luft über `crystalgrass`; 17×17 Luft auf Y, 5×5 `crystalgrass` auf Y-1 | 4/5 `FairyTree` (an Y-1), 1/5 `FairyCastleTree` (an Y) | `Trees.FairyTree` (475), `FairyCastleTree` (640); setzt `recently_placed = 50`. Liefert `true` auch dann, wenn keine Oberfläche gefunden wurde, und blockiert damit Termiten und Crystal-Bauten dieses Chunks |

Tree of Goodness und Queen's Tree: zusammen 1/100 aller Riesenbäume, jeder einzeln 1/200
(OreSpawnWorld.java:1933-1941). Recherche (02-dimensions-worldgen.md:139-141) hält die Wahrscheinlichkeit für offen;
die Website nennt „1 out of every 100 giant trees“ für den Goodness Tree allein.

Baumgrößen: offen, die Maße stehen in `ItemMagicApple`/`ItemAppleSeed`/`Trees` (world-04 bzw. Items-Batch).

#### Strukturen (Bauaufrufe in `GenericDungeon` u. a.)

Grundmaße: offen, sie stehen in den Bauklassen (world-01/world-04). Hier stehen Platzierung,
Prüfflächen und die Spawner-Namen, die ein Literal-Scan von `GenericDungeon.java` im jeweiligen
Methodenrumpf fand (Zeilenbereich angegeben). Spalte „Prüffläche“: Luftprüfung relativ zum
Ursprung, Breite X × Tiefe Z und Prüfhöhe.

**Oberwelt** (nur `dimensionId == 0`, `DisableOverworldDungeons == 0`, `recently_placed == 0`; alle
setzen danach `recently_placed = 50`). Die ersten sechs werden zusätzlich über `world.rand.nextInt(6)`
ausgewählt, ihre wirksame Chance ist also Eigenchance × 1/6.

| Methode (Zeile) | Chance | Biom | Oberflächenbedingung (4 Versuche, sofern nicht anders) | Bau an | Bauaufruf (GenericDungeon.java) | Spawner / Truhe |
|---|---|---|---|---|---|---|
| `addPlayPool` (1159) | 1/350 · 1/6 | `Ocean` | Y 100→41: Luft über `water` | Y | `makePlayPool` (1969-1993) | Attack Squid; Truhe |
| `addWaterDragonLair` (1392) | 1/350 · 1/6 | `Ocean` | Luft über `water` | Y-1 | `makeWaterDragonLair` (1994-2085) | Water Dragon; Truhe |
| `addGoldFishBowl` (1201) | 1/350 · 1/6 | `Ocean` | Luft über `water` | Y-1 | `makeGoldFishBowl` (2435-2514) | Gold Fish |
| `addGirlfriendIsland` (1413) | 1/300 · 1/6 | `Ocean` | Luft über `water` | Y-1 | `makeGirlfriendIsland` (4993-5058) | Boyfriend, Girlfriend, Gold Fish; Truhe |
| `addMonsterIsland` (1434) | 1/300 · 1/6 | `Ocean` | Luft über `water` | Y-1 | `makeMonsterIsland` (5196-5265) | Truhe |
| `addFrogPond` (1180) | 1/350 · 1/6 | `Plains` | Luft über `grass` | Y-1 | `makeFrogPond` (6056-6078) | Frog |
| `addANest` (999) | 1/230 | `Forest`, `ForestHills`, `Jungle`, `JungleHills`, `Birch Forest`, `Birch Forest Hills` | 5 Versuche, Y 128→41 solange Luft, `grass` | Y | 50:50 `makeSmallBeeHive` (1406-1495) / `makeMantisHive` (1055-1105) | Bee bzw. Mantis |
| `addHauntedHouse` (980) | 1/285 | `Plains`, `Taiga`, `Swampland` | 5 Versuche, Y 100→41 solange Luft, `grass` | Y | `makeHauntedHouse` (938-1054) | Ghost, Ghost Pumpkin Skelly, Rat; Truhe |
| `addLeafMonster` (1222) | 1/275 | `Plains` | Luft über `grass` | Y | `makeLeafMonsterDungeon` (2120-2261) | Leaf Monster; Truhe |
| `addSpitBug` (1266) | 1/190 | `Swampland` | Luft über `grass` | Y | `makeSpitBugLair` (2660-2719) | Spit Bug; Truhe |
| `addIgloo` (1288) | 1/220 | `Ice Plains` | Luft über `Blocks.snow` (Schneeblock, nicht Schneeschicht) | Y-2 | `makeIgloo` (2720-2833) | Ghost, Ghost Pumpkin Skelly, Rat; Truhe |
| `addBouncyCastle` (1310) | 1/230 | `Desert` | Luft über `sand` | Y-1 | `makeBouncyCastle` (3117-3217) | Rat, Scorpion, Silverfish; Truhe |
| `addRubberDuckyPond` (1244) | 1/275 | `Plains` | Luft über `grass` | Y | `makeRubberDuckyPond` (5416-5455) | Rubber Ducky; Truhe |

**Utopia**

| Methode (Zeile) | Chance | Bedingung | Bau an | Bauaufruf | Sperre |
|---|---|---|---|---|---|
| `addKingAltar` (2724) | `nextInt(2000) == 1`, nur ohne Apfel-/Sonderbäume im Chunk und bei `recently_placed == 0` | 8 Versuche `3+nextInt(10)`, Y 100→51 Luft über `grass`; `quickReallyBigSpaceCheck`: **60×60** Luft auf Y+7 (Achsen −5..54), Fehlschlag beendet sofort | Y-1 | 50:50 `makeKingAltar` (4391-4442) / `makeQueenAltar` (5734-5785) | 100 |
| `addRubyDungeon` (2083) | `nextInt(15) == 0` | 8 Versuche `chunk+nextInt(8)`, Y 50→6, erster `lava`-Quellblock | Lavablock | `RubyBirdDungeon.makeDungeon` (RubyBirdDungeon.java:31) | keine |
| `addGenericDungeon` (2100) | `nextInt(16) == 0`; `LessLag` 1: 1/2, 2: 1/4 | Position `chunk+nextInt(4)`, `Y = 5+nextInt(40)`, **keine Prüfung** | Y | `makeDungeon` (132-218), Spawner-Pool Alien, Bee, Cloud Shark, Cryolophosaurus, Dungeon Beast, Kyuubi, Lurking Terror, Rat, Rotator, Scorpion, Terrible Terror, WTF?; Truhe | keine |

`addGenericDungeon` läuft ebenso in Mining (als Alternative), VillageMania (immer) und im Utopia-Zweig.

**Extreme/Mining** (1/95 bei `recently_placed == 0`, dann je 1/7; alle setzen 50). Suche: 6×6
Rasterpunkte `chunk + {0,3,6,9,12,15}`, je Y 128→31.

| Methode (Zeile) | Oberflächenwahl | Mindesthöhe | Bau an | Bauaufruf |
|---|---|---|---|---|
| `addBasiliskMaze` (2754) | niedrigster Nicht-Luft-Block mit Luft darüber | > 40 | Y-2 | `BasiliskMaze.buildBasiliskMaze` (BasiliskMaze.java:26) |
| `addKyuubiDungeon` (2787) | wie oben | > 40 | Y-2 | `makeKyuubiDungeon` (1137-1254) |
| `addBeeHive` (2117) | niedrigstes `grass` mit Luft darüber | > 40 | Y+3 | `makeBeeHive` (859-905), Bee |
| `addShadowDungeon` (2257) | wie Bee Hive | > 40 | Y | `makeShadowDungeon` (1496-1578) |
| `addAlienWTF` (2152) | wie Bee Hive | > 40 | Y | `makeAlienWTFDungeon` (1610-1733) |
| `addEnderKnight` (2187) | wie Bee Hive | > 40 | Y | `makeEnderKnightDungeon` (1832-1941), Ender Knight |
| `addLeonNest` (2222) | höchstes `grass` mit Luft darüber, Y 128→81 | > 80 | Y+1 | `makeLeonNest` (4716-4768), Leonopteryx |

**VillageMania** (je bei `recently_placed == 0`; setzen 50). 4 Versuche, Y 100→41: Luft über `grass`
und `quickSpaceCheck`: **12×12** Luft auf Y+3 (Achsen −2..9).

| Methode (Zeile) | Chance | Zusatz | Bau an | Bauaufruf |
|---|---|---|---|---|
| `addDamselInDistress` (1332) | 1/250 | — | Y-1 | `makeDamselInDistress` (3630-3736), Scorpion; Truhe |
| `addSpiderHangout` (1351) | 1/350 | `SpiderDriverEnable` | Y-1 | `makeSpiderHangout` (7032-7085), Spider Driver; Schleifen 0..19 |
| `addRedAntHangout` (1373) | 1/250 | — | Y-1 | `makeRedAntHangout` (7086-7112); Schleifen 0..15 |

**Islands.** Vorbedingung `D4BigSpaceCheck` (2853): Chunk-Ecke, Y 11, Achsen X −25..39, Z −25..29
(**65×55**), toleriert `log`, `leaves_apple`, `leaves_scary`. Jede D4-Methode: bei `LessLag != 0`
nur 1/2 (Generic: 1/4); Position `chunk+nextInt(8)`; erster `grass`-Block Y 20→5 (keine Luftprüfung);
Prüfflächen auf Y+18 (RobotLab Y+4); setzt 50.

| Methode (Zeile) | Anteil in `nextInt(19)` | Prüffläche (X-Achse × Z-Achse) | Bau an | Bauaufruf |
|---|---|---|---|---|
| `addD4Castle` (2326) | 3/19 | −20..32 × −4..32 = **53×37** | Y | 50:50 `makeEnormousCastle` (223-418) / `makeEnormousCastleQ` (6421-6616); Emperor Scorpion, Large Worm, Terrible Terror bzw. Lurking Terror |
| `addD4GenericDungeon` (2588) | 4/19 | keine | Y | `makeDungeon` |
| `addD4EnderCastle` (2456) | 1/19 | −5..24 × −5..24 = 30×30 | Y | `makeEnderCastle` (3218-3503); CaveFisher, Ender Knight, Ender Reaper; Truhe |
| `addD4IncaPyramid` (2481) | 1/19 | −10..49 × −10..39 = **60×50** | Y | `makeIncaPyramid` (3737-3995); Molenoid; Truhe |
| `addD4RobotLab` (2506) | 1/19 | −5..59 × −5..69 = **65×75** auf Y+4, toleriert Holz/Apfel-/Scary-Blätter | Y | `makeRobotLab` (4085-4133) |
| `addD4Mini` (2539) | 1/19 | keine | Y | `makeMiniDungeon` (2262-2434); Butterfly, Lurking Terror, Terrible Terror; Truhe |
| `addD4RubyDungeon` (2292) | 1/19 | keine | Y | `RubyBirdDungeon.makeDungeon` |
| `addD4CephadromeAltar` (2309) | 1/19 | keine | Y | `makeCephadromeAltar` (4769-4866) |
| `addD4Greenhouse` (2356) | 1/19 | −2..24 × −4..24 = 27×29 | Y | `makeGreenhouseDungeon` (5059-5195); Triffid; Truhe |
| `addD4NightmareRookery` (2381) | 1/19 | −5..24 × −4..4 = 30×9 | Y | `makeNightmareRookery` (5266-5349); Truhe |
| `addD4StinkyHouse` (2406) | 1/19 | −8..19 × −8..19 = 28×28 | Y | `makeStinkyHouse` (5350-5415); Stink Bug, Stinky; Truhe |
| `addD4WhiteHouse` (2431) | 1/19 | −20..29 × **−20..299** = **50×320** | Y | `makeWhiteHouse` (5456-5468); Truhe |
| `addPumpkin` (2556) | 1/19 | keine | Y+1 | `makePumpkin` (6079-6218); Ghost Pumpkin Skelly |
| `addD4Rainbow` (2580) | 1/19 | keine, Position `4+nextInt(8)` | `Y = 70 + world.rand.nextInt(20)` | `makeRainbow` (6291-6420); Cloud Shark; Truhe |
| `addD4CloudShark` (2573) | eigene 1/300, ohne Sperrprüfung, setzt keine Sperre | keine, `4+nextInt(8)` | `Y = 150 + world.rand.nextInt(10)` | `makeCloudSharkDungeon` (2086-2119); Cloud Shark; Truhe |

**Crystal** (bei `recently_placed == 0`, erster Erfolg beendet; 3 Versuche, Y 100→51, Luft über
`crystalgrass`, Bau an Y; setzen 50).

| Methode (Zeile) | Chance | Flag | Bauaufruf |
|---|---|---|---|
| `addRotatorStation` (1658) | 1/150 | `RotatorEnable` | `makeRotatorStation` (834-858); Rotator; Truhe |
| `addUrchinSpawner` (1700) | 1/180 | `UrchinEnable` | `makeUrchinSpawner` (2601-2659); Crystal Urchin; Truhe |
| `addCrystalHauntedHouse` (1721) | 1/230 | — | `makeCrystalHauntedHouse` (3008-3116); Ghost, Ghost Pumpkin Skelly, Rat; Truhe |
| `addRoundRotator` (1679) | 1/150 | `RotatorEnable` | `makeRoundRotator` (6219-6290); Dungeon Beast, Rotator; Truhe |
| `addCrystalBattleTower` (1739) | 1/280 | — | `makeCrystalBattleTower` (4867-4992); Crystal Urchin, Dungeon Beast, Rat, Rotator, Vortex; Truhe |

**End** (Auswahl `world.rand.nextInt(4)`, keine Sperre gelesen oder gesetzt). 3 Versuche,
Y 90→11, Luft über `end_stone`, Bau an Y.

| Methode (Zeile) | Chance | Prüffläche | Bauaufruf |
|---|---|---|---|
| `addEndKnights` (1558) | 1/25 · 1/4 | `quickSpaceCheck` 12×12 auf Y+4 | `makeEnderKnightDungeon` |
| `addEndReapers` (1574) | 1/25 · 1/4 | wie oben | `makeEnderReaperGraveyard` (2515-2587); Ender Reaper |
| `addHospital` (1590) | 1/25 · 1/4 | wie oben | `makeEnderDragonHospital` (2834-3007); Ender Reaper, Nightmare; Truhe |
| `addEnderCastle` (1606) | 1/50 · 1/4 | `quickBigSpaceCheck` 30×30 auf Y+8 (−5..24) | `makeEnderCastle` |

#### Erze der Oberwelt und des Nethers

`generateOres` (339-961), Oberwelt; Adern mit Vanilla-`WorldGenMinable(block, clumpsize)`, das Stein
ersetzt (javap `client-1.7.10.jar`: 2-Arg-Konstruktor setzt Ziel `ajn.b`, nicht per SRG
gegengeprüft). Position `3 + chunk + nextInt(10)`, `Y = nextInt(128)`, nur im Band
`mindepth ≤ Y ≤ maxdepth`. Ablauf und Blockliste der Spawn-Erze sind identisch mit
`ChunkOreGenerator` (siehe dort), mit drei Unterschieden:

| Punkt | `generateOres` | `ChunkOreGenerator` |
|---|---|---|
| SpawnOres-Versuche | `rate + nextInt(20)` (341) | `rate + nextInt(30)` |
| Ruby Ore | `rate + nextInt(5)` Versuche, Lava-Suche wie `generateRuby` (870-888) | fehlt |
| Schreibweg | `world.setBlock` über `WorldGenMinable`, schreibt in Nachbar-Chunks | nur in den übergebenen Chunk |

`generateNether` (232-257): Position `3 + chunk + nextInt(13)`, `Y = nextInt(108) + 10` (10..117),
`WorldGenMinable(..., netherrack)`.

| Block | Versuche | Adergröße | Zeile |
|---|---|---|---|
| `lavafoam` | `15 + nextInt(10)`, mit `LessOre` /3 | 6 | 237-246 |
| `oreruby` | `5 + nextInt(5)`, mit `LessOre` /3 | 2 | 247-256 |

#### Portierung 1.21.1

- **Aufteilung.** Kein zentraler Generator, sondern: (a) Oberwelt/Nether/End als `PlacedFeature`s über NeoForge-Biome-Modifier (`neoforge:add_features`); (b) Dimensionsdekoration direkt in den Biom-JSONs der fünf Einbiom-Dimensionen (world-03) bzw. als Features im Biom `minecraft:*` der Extreme-Dimension; (c) große Bauten als `Structure` plus `StructureSet`.
- **Biomnamen → Biom-Tags.** Namensvergleiche über `biome_map.json` (docs/catalog) übersetzen: `Forest`/`ForestHills` → `minecraft:forest`, `Birch Forest`/`Birch Forest Hills` → `minecraft:birch_forest`, `Jungle`/`JungleHills` → `minecraft:jungle`, `Plains` → `minecraft:plains`, `Taiga` → `minecraft:taiga`, `Swampland` → `minecraft:swamp`, `Ocean` → `minecraft:ocean`, `River` → `minecraft:river`, `Extreme Hills` → `minecraft:windswept_hills`, `Desert` → `minecraft:desert`, `Ice Plains` → `minecraft:snowy_plains`, `Roofed Forest` → `minecraft:dark_forest`. Je Feature ein Tag `orespawn:has_feature/<name>`.
- **Config-Abhängigkeit.** Enable-Flags, `LessLag`, `LessOre`, `DisableOverworldDungeons` sind Laufzeitwerte; die Features lesen die `ModConfigSpec` in `place()`. Weil Biome Modifier beim Serverstart angewandt werden, gehören diese Schalter in eine COMMON- oder STARTUP-Config, nicht in SERVER.
- **Zufall.** `world.rand` (Auswahl `nextInt(6)`, End-Auswahl, Crystal-Truhen, Rainbow-/CloudShark-Höhe) ist im Original nicht seedgebunden; im Port die `RandomSource` des Features verwenden. Das ist eine bewusste, unschädliche Abweichung.
- **`recently_placed` ist nicht 1:1 portierbar.** Die Chunk-Erzeugung in 1.21.1 ist parallel; ein statischer, dimensionsübergreifender Zähler wäre eine Race-Condition und nicht reproduzierbar. Ersatz: `StructureSet` mit `random_spread` (`spacing`/`separation`) und `exclusion_zone` zwischen den Sets einer Dimension. Die „1 in N pro Chunk“-Chancen bildet `spacing = 1`, `separation = 0` mit `frequency = 1/N` ab; Oberflächen- und Prüfflächen-Fehlschläge lehnt `Structure.findGenerationPoint` mit `Optional.empty()` ab. Flag an den Guardrail: exakte Strukturdichte des Originals ist nicht erreichbar.
- **Fehler im Original.** `addMosquitos(chunkX, chunkZ)` in VillageMania (OreSpawnWorld.java:103) schreibt in Chunks nahe dem Ursprung. Ein Feature darf dort nicht schreiben (außerhalb der WorldGenRegion). Nicht nachbaubar; Vorschlag: Blockkoordinaten verwenden und die Abweichung dokumentieren.
- **Höhen.** Oberflächensuchen starten bei festen Y (100, 110, 127/128) und enden bei 41/51. Die Oberwelt 1.21.1 reicht bis Y 320 und unter 0; ein fester Start bei Y 100 verfehlt Berge. Vorschlag: `Heightmap.Types.WORLD_SURFACE_WG` als Start, Untergrenzen beibehalten. Offen: Entscheidung, ob Erz-Y-Bänder der Oberwelt absolut bleiben oder verschoben werden.
- **Blöcke.** `Blocks.tallgrass` → `SHORT_GRASS`; `Blocks.snow` → `SNOW_BLOCK`; `Blocks.log` meta 0 → `OAK_LOG`; Flüssigkeiten mit flag 3 → `setBlock(..., 2)` plus Fluid-Tick einplanen (`scheduleTick`), sonst fließt nichts.
- **Spawner und Truhen.** `TileEntityMobSpawner.func_145881_a()` (SRG-Name, liefert laut joined.srg `MobSpawnerBaseLogic`) `.setEntityName(name)` → `SpawnerBlockEntity.setEntityId(EntityType, RandomSource)` mit den Registry-Ids aus manifest (`Irukandji` → `irukandji`, `Dungeon Beast` → `dungeon_beast`, `Rat` → `rat`). `WeightedRandomChestContent` → Loot-Table `orespawn:chests/crystal_cave` (Pool `rolls` uniform 1–3, Einträge mit `weight` und `set_count`) über `RandomizableContainer.setBlockEntityLootTable`.
- **Entities im Worldgen.** `spawnCreature("Rock")` → `EntityType.create(ServerLevel)` und `WorldGenLevel.addFreshEntityWithPassengers`; `playLivingSound` entfällt beim Generieren (kein Spieler in der Nähe). Offen: API-Namen vor Implementierung im `neoforge-*-sources.jar` prüfen.

**Chunkgrenzen-Risiko je Bauart.** Ein Feature darf in 1.21.1 nur in der 3×3-Chunk-Region um seinen
Ursprungs-Chunk lesen und schreiben, relativ zur Chunk-Ecke also lokal −16..31 auf X und Z. Das Original
setzt Ursprünge bei `chunk + nextInt(n)`; die folgende Rechnung nimmt den größtmöglichen Ursprung und die
Prüffläche aus dem Code (abgeleitet, die Baumaße selbst sind offen):

| Bau | Ursprung lokal | Prüffläche lokal (X) | passt in −16..31? | Empfehlung |
|---|---|---|---|---|
| Pflanzen, Nester, Felsen, Einzelspawner, Crystal-Truhen, `addLavaAndWater`, Nether-/Lapis-Adern | 0..15 (Adern +8) | ≤ 1 Block um Ursprung, Adern bis +8+Größe/2 | ja | `Feature` |
| VillageMania-Bauten (`quickSpaceCheck`) | 0..15 | −2..24 | ja | `Feature`, wenn world-01 Maße ≤ 16 Überstand bestätigt; Spider Hangout schleift 0..19 → Grenzfall |
| End-Dungeons (Knight, Reaper, Hospital) | 0..15 | −2..24 | ja | wie oben |
| End `addEnderCastle` | 0..15 | −5..39 | **nein** | `Structure` |
| `addKingAltar` / Queen | 3..12 | −2..66 | **nein** | `Structure` |
| `D4BigSpaceCheck` (Vorbedingung aller Islands-Bauten) | Chunk-Ecke | −25..39 | **nein** | alle Islands-Bauten als `Structure`, die Prüfung per Base-Column-Abtastung in `findGenerationPoint` |
| `addD4Castle` | 0..7 | −20..39 | **nein** | `Structure` |
| `addD4IncaPyramid` | 0..7 | −10..56 | **nein** | `Structure` |
| `addD4RobotLab` | 0..7 | −5..66 | **nein** | `Structure` |
| `addD4WhiteHouse` | 0..7 | −20..36 × Z −20..306 | **nein** | `Structure`; die 50×320-Luftprüfung sind 16 000 Säulen, im Port stichprobenartig abtasten (Abweichung) |
| `addD4Greenhouse`, `addD4NightmareRookery`, `addD4EnderCastle` | 0..7 | −5..31 | knapp ja | wegen `D4BigSpaceCheck` trotzdem `Structure` |
| `addD4StinkyHouse` | 0..7 | −8..26 | ja | wegen `D4BigSpaceCheck` `Structure` |
| Mining-Bauten (Rastersuche im Chunk) | 0..15 | keine Prüfung | Maße offen | Basilisk Maze sicher `Structure` (Größe aus world-04); übrige nach world-01-Maßen |
| Oberwelt-Minidungeons und Haunted House/Igloo/Bouncy Castle | 0..15 | keine Prüfung | Maße offen | nach world-01-Maßen; ≤ 16 Überstand → `Feature`, sonst `Structure` |
| `addHugeTree`, `addFairyTree` (17×17-Prüfung um `chunk+8` → lokal 0..16) | 4..11 bzw. 8 | ja | Baumhöhe/-breite offen | vorläufig `Feature` (Radius ≤ 6 laut Code), Riesenbaum-Kronen in world-04 nachmessen |
| `addGenericDungeon`, `addRubyDungeon` | 0..3 bzw. 0..7 | keine | Maße offen | nach world-01/world-04 |

---

### ChunkOreGenerator

**Rolle.** Einzige Instanz `OreSpawnMain.Chunker` (OreSpawnMain.java:200, 5447). Wird **nicht** beim
Populate, sondern in `provideChunk` direkt nach Terrain, Biom-Oberfläche, Höhlen und Schluchten auf
das rohe `Chunk`-Objekt aufgerufen:

| Aufrufer | Dimension | Aufrufe pro Chunk | Zufall |
|---|---|---|---|
| ChunkProviderOreSpawn.java:170 | Utopia | 1 | `this.rand` |
| ChunkProviderOreSpawn2.java:172-176 | Extreme/Mining | 1, mit `LessOre == 0` weitere 2 | `this.rand`, vorher mit `par1·341873128712 + par2·132897987541` gesät (ChunkProviderOreSpawn2.java:164) |
| ChunkProviderOreSpawn3.java:171 | VillageMania | 1 | `this.rand` |
| ChunkProviderOreSpawn6.java:194 | Chaos | 1 | `worldObj.rand` (nicht seedgebunden) |

Islands und Crystal rufen ihn nicht auf. Deutung aus ChunkProviderOreSpawn2.java:164: der Seed dort
enthält nicht den Welt-Seed; ob die Erzlage dadurch seedunabhängig wird, hängt vom weiteren
`rand`-Verbrauch in `replaceBlocksForBiome` ab (world-03).

**Schreibweg.** `generateBlockOre` (617-657) ist eine Kopie des 1.7.10-`WorldGenMinable`-Algorithmus
(Linie zwischen zwei Punkten im Abstand `numberOfBlocks/8` um `x+8`/`z+8`, Kugeln mit Radius aus
`sin`, Y-Endpunkte `y + nextInt(3) - 2`). Gelesen und geschrieben wird nur über
`OreSpawnMain.getBlockIDInChunk` (OreSpawnMain.java:5584-5598) und `setBlockIDWithMetadataInChunk`
(5600-5631): Koordinaten außerhalb des Chunks liefern Luft bzw. werden verworfen. Ersetzt wird nur
`Blocks.stone` (647). Da der Ursprung `3 + chunk + nextInt(10)` plus internem +8 lokal bei 11..20
liegt, wird ein Teil jeder Ader an der +X/+Z-Chunkgrenze **abgeschnitten** (abgeleitet aus 22, 619-622).

**Ablauf je Aufruf.** `Y = nextInt(128)` (0..127), Ader nur bei `mindepth ≤ Y ≤ maxdepth`.

| Erz | Config-Präfix | Default rate / clump / min / max (manifest) | Versuche | Zeilen |
|---|---|---|---|---|
| Spawn-Erze (105 Blöcke) | `SpawnOres` | 28 / 4 / 50 / 128 | `rate + nextInt(30)`, mit `nextInt(20) == 0` +30, `LessOre` /3 | 13-462 |
| `oreuranium` | `Uranium` | 3 / 4 / 0 / 30 | `rate + nextInt(9)`, `LessOre` /3 | 463-476 |
| `oretitanium` | `Titanium` | 3 / 4 / 0 / 20 | `rate + nextInt(9)`, `LessOre` /3 | 477-490 |
| `oreamethyst` | `Amethyst` | 2 / 6 / 0 / 25 | `rate + nextInt(12)`, `LessOre` /3 | 491-504 |
| `oresalt` | `Salt` | 5 / 12 / 50 / 128 | `rate + nextInt(9)`, `LessOre` /3 | 505-518 |
| `redanttroll` | fest | Größe 4, Y 5..50 | `4 + nextInt(4)`, `LessOre` /2 | 519-530 |
| `termitetroll` | fest | Größe 4, Y 5..50 | `4 + nextInt(4)`, `LessOre` /2 | 531-542 |
| `minecraft:diamond_ore` | `Diamond` | 4 / 6 / 0 / 30 | genau `rate`, nur `LessOre == 0` | 544-553 |
| `minecraft:diamond_block` | `BlockDiamond` | 2 / 4 / 0 / 20 | `rate`, nur `LessOre == 0` | 554-563 |
| `minecraft:emerald_ore` | `Emerald` | 4 / 6 / 0 / 40 | `rate`, nur `LessOre == 0` | 564-573 |
| `minecraft:emerald_block` | `BlockEmerald` | 2 / 4 / 0 / 20 | `rate`, nur `LessOre == 0` | 574-583 |
| `minecraft:gold_ore` | `Gold` | 4 / 8 / 0 / 40 | `rate`, nur `LessOre == 0` | 584-593 |
| `minecraft:gold_block` | `BlockGold` | 2 / 4 / 0 / 25 | `rate`, nur `LessOre == 0` | 594-603 |
| `blockruby` | `BlockRuby` | 1 / 2 / 0 / 15 | `rate`, nur `LessOre == 0` | 604-613 |

Config-Klemmung in `get_orestats` (OreSpawnMain.java:5770-5800): `rate` und `clumpsize` auf ½× bis
2× des Defaults, `clumpsize ≥ 1`, `mindepth`/`maxdepth ≥ 0`; ist `maxdepth − mindepth < 10`, fallen
beide auf die Defaults zurück. Kategorie `OreSpawnORES`, Schlüssel `<Präfix>_rate`, `_clumpsize`,
`_mindepth`, `_maxdepth` (manifest).

**Spawn-Erz-Auswahl je Versuch** (26-458). Mit `nextInt(104) < 7` eine der 7 seltenen Sorten
(`nextInt(7)`), sonst eine von 98 (`nextInt(98)`). Jede seltene Sorte hat damit 1/104, jede andere
97/10192 (abgeleitet); beide ≈ 0,95 %. Registry-Ids aus manifest:

- **selten (0-6):** `orebrutalfly`, `orenastysaurus`, `orepointysaurus`, `orecricket`, `orefrog`, `orespiderdriver`, `orecrab`.
- **98er-Liste (0-97):** `orespider`, `orebat`, `orecow`, `orepig`, `oresquid`, `orechicken`, `orecreeper`, `oreskeleton`, `orezombie`, `oreslime`, `oreghast`, `orezombiepigman`, `oreenderman`, `orecavespider`, `oresilverfish`, `oremagmacube`, `orewitch`, `oresheep`, `orewolf`, `oremooshroom`, `oreocelot`, `oreblaze`, `orewitherskeleton`, `oreenderdragon`, `oresnowgolem`, `oreirongolem`, `orewitherboss`, `oregirlfriend`, `oreredcow`, `oregoldcow`, `oreenchantedcow`, `oremothra`, `orealosaurus`, `orecryolophosaurus`, `orecamarasaurus`, `orevelocityraptor`, `orehydrolisc`, `orebasilisc`, `oredragonfly`, `oreemperorscorpion`, `orescorpion`, `orecavefisher`, `orespyro`, `orebaryonyx`, `oregammametroid`, `orecockateil`, `orekyuubi`, `orealien`, `oreattacksquid`, `orewaterdragon`, `orekraken`, `orelizard`, `orecephadrome`, `oredragon`, `orebee`, `orehorse`, `oretrooper`, `orespit`, `orestink`, `oreostrich`, `oregazelle`, `orechipmunk`, `orecreepinghorror`, `oreterribleterror`, `orecliffracer`, `oretriffid`, `orenightmare`, `orelurkingterror`, `oregodzillapart`, `oregodzilla`, `oresmallworm`, `oremediumworm`, `orelargeworm`, `orecassowary`, `orecloudshark`, `oregoldfish`, `oreleafmonster`, `oretshirt`, `oreenderknight`, `oreenderreaper`, `orebeaver`, `oretrex`, `orehercules`, `oremantis`, `orestinky`, `oreboyfriend`, `orethekingpart`, `oreeasterbunny`, `orecaterkiller`, `oremolenoid`, `oreseamonster`, `oreseaviper`, `oreleon`, `orehammerhead`, `orerubberducky`, `orevillager`, `orecriminal`, `orethequeenpart`.

Abweichung zur Recherche: keine im Inhalt; 02-dimensions-worldgen.md:151 nennt „dreimal pro Chunk“ als Kern
der Mining-Dimension, das gilt nur für `ChunkProviderOreSpawn2`, die übrigen drei Aufrufer rufen einmal.

#### Portierung 1.21.1

- **Ort.** Die eigenen Dimensionen bekommen einen Java-`ChunkGenerator` (world-03). Der chunklokale Schreibweg passt genau auf die `ChunkAccess`-Phase: nach `super.applyCarvers(...)` im eigenen Generator die Erze direkt in den `ProtoChunk` schreiben, mit `ChunkAccess.getBlockState`/`setBlockState` und Grenzprüfung auf den Chunk. Das reproduziert das Abschneiden an der Chunkgrenze 1:1, ohne WorldGenRegion.
- **Alternative als Feature.** `Feature` im Schritt `UNDERGROUND_ORES`, das Schreibzugriffe selbst auf den Ursprungs-Chunk begrenzt. Vanilla-`minecraft:ore` ist ungeeignet: anderer Algorithmus, keine Chunk-Klemmung, und `height_range` würfelt nur innerhalb des Bandes, während das Original über 0..127 würfelt und Treffer außerhalb verwirft (Aderzahl × Bandanteil).
- **Ersetzbare Blöcke.** Nur `minecraft:stone`. Wenn world-03 Deepslate oder andere Grundgesteine einführt, bleibt Stein das einzige Ziel (1:1).
- **Zufall.** Chaos (`worldObj.rand`) im Port auf die Chunk-`RandomSource` umstellen.
- **Config.** Werte aus `ModConfigSpec` mit derselben Klemmung wie `get_orestats`.

---

### MapGenMoreVillages

**Rolle.** Unterklasse von Vanilla `MapGenVillage`; einzige Instanz in `ChunkProviderOreSpawn3`
(VillageMania), dort bei `mapFeaturesEnabled` in `provideChunk` (`func_151539_a` = `generate`,
ChunkProviderOreSpawn3.java:165-168), in `populate` (`generateStructuresInChunk`, 280) und in
`recreateStructures` (361) aufgerufen (Konstruktion: 49).

**Verhalten.** Überschreibt nur `canSpawnStructureAtCoords` (MCP `func_75047_a`, methods.csv):

| Punkt | Wert | Herkunft |
|---|---|---|
| Rastergröße (eigenes Feld `field_82665_g`) | 9 Chunks | (MapGenMoreVillages.java:12) |
| Mindestabstand (eigenes Feld `field_82666_h`) | 7 | (MapGenMoreVillages.java:13) |
| Vanilla-Werte derselben Felder | 32 und 8 | javap `avn.<init>` in client-1.7.10.jar (`bipush 32`, `bipush 8`) |
| Versatz im Raster | `nextInt(9 − 7)` = 0 oder 1 Chunk je Achse | (MapGenMoreVillages.java:30-31) |
| Salz | 10387312 | (MapGenMoreVillages.java:27) |
| Negativ-Koordinaten | Floor-Division über `par -= 9 − 1` | (MapGenMoreVillages.java:19-24) |
| Biomprüfung | `areBiomesViable(..., villageSpawnBiomes)` wird berechnet und **ignoriert**, Rückgabe immer `true` bei Rastertreffer | (MapGenMoreVillages.java:33-34) |

Die eigenen `private`-Felder verdecken die gleichnamigen Vanilla-Felder nur für diese Methode.
Dorfgröße und Dorfstil bleiben Vanilla (kein Override); das Biom „Villages“ wird in
WorldProviderOreSpawn3.java:31 per `BiomeManager.addVillageBiome(..., true)` freigegeben.
Ergebnis: pro 9×9-Chunk-Zelle genau ein Dorfversuch, Abstand zwischen Dörfern 8 bis 10 Chunks
(abgeleitet).

#### Portierung 1.21.1

- Reines Datapack. Eigene `worldgen/structure/orespawn/village_mania.json` (`type: minecraft:jigsaw`, Start-Pool `minecraft:village/plains/town_centers`, Vanilla-Werte für Größe und Höhe) mit `biomes: #orespawn:has_structure/village_mania` (enthält `orespawn:villages`).
- `worldgen/structure_set/orespawn/village_mania.json`: `random_spread`, `spacing 9`, `separation 7`, `salt 10387312`, `spread_type linear`. `RandomSpreadStructurePlacement` rechnet den Versatz ebenfalls als `nextInt(spacing − separation)`; nur die Seed-Ableitung unterscheidet sich, Positionen sind also bei gleichem Seed nicht identisch.
- **Nicht** `orespawn:villages` in `minecraft:has_structure/village_plains` eintragen: dann greift zusätzlich das Vanilla-Set `minecraft:villages` in derselben Dimension (dessen Abstandswerte nicht geprüft).
- Die ignorierte Biomprüfung ist bedeutungslos, weil die Dimension ein Einbiom ist.
- Dorfbauweise 1.21.1 weicht optisch vom 1.7.10-Dorf ab; nicht vermeidbar ohne eigene Jigsaw-Pools.
- Chunkgrenzen: Jigsaw-`Structure`, kein Risiko.

---

### OreSpawnTeleporter

**Rolle.** `extends Teleporter`. Wird bei jedem Rechtsklick mit leerer Hand auf eine Reisekreatur neu
erzeugt und an `transferPlayerToDimension` übergeben; Ziel ist die Kreatur-Dimension oder, wenn man
sich schon darin befindet, Dimension 0.

| Aufrufer | Ziel | Rückweg |
|---|---|---|
| EntityAnt.java:81 | Utopia (`DimensionID`) | :84 → 0 |
| EntityRedAnt.java:71 | Extreme/Mining (`DimensionID2`) | :74 → 0 |
| EntityRainbowAnt.java:46 | VillageMania (`DimensionID3`) | :49 → 0 |
| EntityUnstableAnt.java:46 | Islands (`DimensionID4`) | :49 → 0 |
| Termite.java:95 | Crystal (`DimensionID5`) | :98 → 0 |
| EntityButterfly.java:265 | Chaos (`DimensionID6`) | :268 → 0 |

**Kein Portal, keine Plattform.** `placeInPortal` und `placeInExistingPortal` rufen nur `justPutMe`
(28-35), `makePortal` liefert `true` ohne Bau (37-39), `removeStalePortalLocations` ist leer (188-189).
Konstruktor: eigener `Random(world.getSeed())` (24).

**Landeplatz `justPutMe`** (45-157):

1. Startsäule `(int) posX`, `(int) posZ` des Entities (46-47); der Cast rundet Richtung 0.
2. Bis zu 1000 Säulenversuche (52). Pro Säule Y von 180 abwärts bis 2 (53):
   - Erste Stelle mit Luft auf Y+1 und Y und Nicht-Luft auf Y-1 entscheidet.
   - Y-1 mit `getMaterial().isSolid()` → gefunden, Füße auf Y (62-65).
   - Y-1 ist `tallgrass` und Y-2 fest → gefunden auf Y-1 (66-70).
   - Sonst (z. B. Wasser, Lava) → Säule verworfen (71).
   - Die Zählung `inarow` für Bodenblöcke (`dirt`, `grass`, `stone`, `end_stone`, `netherrack`, `cobblestone`, `sand`, `sandstone`, `farmland`, 41-43) kann nach `airfound` nie erreicht werden, weil jede Luft-Luft-Folge den Säulendurchlauf beendet (Deutung aus Kontrollfluss 55-82): toter Code.
3. Neue Säule nach Fehlschlag (84-100): Entity-Position ± `world.rand.nextInt(3 + i/5)` (Differenz zweier Würfe); ab Versuch 101 zusätzlich ± `OreSpawnRand.nextInt(2 + i/5)`, ab 501 zusätzlich ± `this.random.nextInt(2 + i/5)`. Bei i = 999 reicht jeder Summand bis ±201 bzw. ±200 (abgeleitet), der Suchradius wächst also bis rund ±600 Blöcke.
4. Nichts gefunden: Originalsäule, Y 180→2, erste Luft-Luft über beliebigem Nicht-Luft-Block; ohne Treffer bleibt Y = 1 (103-111).
5. Ziel `x ± 0.5`, `z ± 0.5` (Vorzeichen der Koordinate), Füße auf Y, Blickrichtung behalten, Pitch 0, Bewegung 0 (115-134). **Fehler bei negativen Koordinaten:** gesucht wurde Säule `(int) x`, gelandet wird in `(int) x − 0.5`, also in der Nachbarsäule (abgeleitet aus 46, 118-120).

**Begleiter** (138-153). Nur wenn das Entity ein Spieler ist: im **alten** Level alle
`EntityTameable` in der Box ±24 X, ±12 Y, ±24 Z um die alte Position, die nicht sitzen und dem Spieler
gehören (UUID-String-Vergleich mit `func_152113_b()` oder `func_152114_e(player)`; laut joined.srg
`EntityTameable.func_152113_b ()String` und `func_152114_e (EntityLivingBase)Z`, Bedeutung aus der
Verwendung: Besitzer-UUID und Besitzerprüfung). `sendToThisDimension` (159-186) entfernt das Tier,
erzeugt per `EntityList.createEntityByName` eine Kopie im Zielwelt-Objekt, `copyDataFrom`, gleiche
Zielposition, Blickrichtung des Spielers. `Girlfriend` und `Boyfriend` sind `EntityTameable`
(Girlfriend.java:20, Boyfriend.java:19) und reisen mit. Abschluss:
`resetUpdateEntityTick()` auf alter und neuer Welt (154-155).

Abweichung zur Recherche: 02-dimensions-worldgen.md:53 nennt nur „außer sie sitzen“; der Code verlangt
zusätzlich Besitz durch den Spieler und die 48×24×48-Box.

#### Portierung 1.21.1

- Kein `Teleporter`/`ITeleporter` mehr nötig: Hilfsklasse `OreSpawnTravel.travel(ServerPlayer, ResourceKey<Level>)`, Ziel-`ServerLevel` holen, Landeplatz berechnen, `player.changeDimension(new DimensionTransition(...))`. Offen: Signatur von `DimensionTransition` und dem Post-Transition-Callback in `neoforge-21.1.*-sources.jar` prüfen, nicht aus dem Gedächtnis schreiben.
- Suche 1:1 über `level.getBlockState`; `isSolid()` des Materials → `BlockState.isSolid()` (in 1.21.1 deprecated, im Sources-Jar prüfen) oder `blocksMotion()`; `tallgrass` → `Blocks.SHORT_GRASS`.
- Y-Start 180 und Ende 2 an `level.getMinBuildHeight()`/`getMaxBuildHeight()` klemmen, sonst nichts ändern.
- Vorschlag Abweichung: `Mth.floor` statt `(int)`, damit gesuchte und besetzte Säule übereinstimmen.
- Lastrisiko: bis zu 1000 Säulen über rund ±600 Blöcke erzwingen synchrones Laden/Erzeugen von Chunks im Ziel. 1:1 beibehalten, aber im README vermerken.
- Begleiter: `getEntitiesOfClass(TamableAnimal.class, aabb)` im alten Level, Bedingung „sitzt nicht“ und `isOwnedBy(player)`; jedes Tier per eigenem `changeDimension` an die Zielposition. Offen: Methodennamen (`isOrderedToSit`/`isInSittingPose`, `isOwnedBy`) im Sources-Jar prüfen. Reihenfolge: Tiere **vor** dem Spieler versetzen, weil die Box am alten Standort gelesen wird.
- `resetUpdateEntityTick` entfällt (kein Gegenstück nötig).
- Läuft vollständig serverseitig, kein Client-Code.

---

### BiomeGenUtopianPlains

**Rolle.** `extends BiomeGenBase`, geschützter Konstruktor mit Biom-Id. Jede WorldProvider-Instanz
erzeugt beim Konstruieren ein eigenes Exemplar und ruft danach höchstens eine `set…Creatures`-Methode;
der Biom-Manager ist `WorldChunkManagerHell` mit genau diesem Biom (world-03).

| Provider | Biom-Id / Name | Farbe, Temp., Regen | Setter | Zeilen |
|---|---|---|---|---|
| WorldProviderOreSpawn | `BiomeUtopiaID` (120), „Utopia“ | 353825, 0.7, 0.5 | keiner | WorldProviderOreSpawn.java:15, 52 |
| WorldProviderOreSpawn3 | `BiomeVillageID` (123), „Villages“ | 353825, 0.7, 0.5 | `setVillageCreatures` (hängt an) | WorldProviderOreSpawn3.java:15, 27-31 |
| WorldProviderOreSpawn4 | `BiomeIslandsID` (121), „Islands“ | 353825, 0.7, 0.5 | `setIslandCreatures` (ersetzt) | WorldProviderOreSpawn4.java:15, 27-28 |
| WorldProviderOreSpawn5 | `BiomeCrystalID` (122), „Crystal“ | 353825, 0.7, 0.5; `Height(0.1, 0.5)` | `setCrystalCreatures` (ersetzt) | WorldProviderOreSpawn5.java:15, 27-29 |
| WorldProviderOreSpawn6 | `BiomeChaosID` (124), „Chaos“ | 353825, 0.7, 0.5 | `setChaosCreatures` (ersetzt) | WorldProviderOreSpawn6.java:15, 27-28 |

Die Extreme-Dimension benutzt kein OreSpawn-Biom (manifest dimensions: `BiomeGenBase.field_76770_e`).

**Listen und Kategorien.** `spawnableCreatureList` → `creature`, `spawnableMonsterList` → `monster`,
`spawnableWaterCreatureList` → `water`, `spawnableCaveCreatureList` → `ambient` (MCP fields.csv
`field_76762_K`, `field_76761_J`, `field_76755_L`, `field_82914_M`; Zuordnung zu `MobCategory` in
biome_map.json). Viele Flieger und in Chaos sogar Dinosaurier stehen in `ambient`.
Jeder Eintrag ist `SpawnListEntry(Klasse, Gewicht, min, max)` und nur aktiv, wenn das Flag ≠ 0 ist. Alle Flags haben Default 1 (manifest), Ausnahme
`BoyfriendEnable` = 0 (OreSpawnMain.java:6102). Config-Schlüssel weichen zweimal vom Feldnamen ab:
`CockateilEnable` ← `BirdEnable` (6069), `PitchBlackEnable` ← `NightmareEnable` (6134).
`AllMobsDisable` (Default 0, OreSpawnMain.java:1140) setzt über `disableAllMobs` alle Flags auf 0
(1258-1260, 5804).

**Vanilla-Grundlisten** (nur Utopia und VillageMania behalten sie; javap `ahu(int)` in
client-1.7.10.jar): creature Sheep 12/4–4, Pig 10/4–4, Chicken 10/4–4, Cow 8/4–4; monster Spider
100/4–4, Zombie 100/4–4, Skeleton 100/4–4, `xz` 100/4–4, Slime 100/4–4, Enderman 10/1–4, Witch 5/1–1;
water `ws` 10/4–4; ambient Bat 10/8–8. Offen: `xz` und `ws` nicht per joined.srg aufgelöst.

**Utopia** — Konstruktor (BiomeGenUtopianPlains.java:10-59); Dekorator: `treesPerChunk -999`,
`flowersPerChunk 4`, `grassPerChunk 6` (57-59).

| Kat. | Mob → id | Gew. | min–max | Flag | Zeile |
|---|---|---|---|---|---|
| creature | Gazelle → `gazelle` | 10 | 2–4 | GazelleEnable | 11 |
| creature | Girlfriend → `girlfriend` | 5 | 2–3 | GirlfriendEnable | 17 |
| creature | Boyfriend → `boyfriend` | 5 | 2–3 | BoyfriendEnable (0) | 20 |
| creature | RedCow → `apple_cow` | 10 | 4–8 | CowEnable | 23 |
| creature | GoldCow → `golden_apple_cow` | 8 | 2–6 | CowEnable | 24 |
| creature | EnchantedCow → `enchanted_golden_apple_cow` | 5 | 2–4 | CowEnable | 25 |
| ambient | Firefly → `firefly` | 15 | 3–6 | FireflyEnable | 14 |
| ambient | EntityButterfly → `butterfly` | 20 | 3–6 | ButterflyEnable | 28 |
| ambient | EntityLunaMoth → `moth` | 10 | 1–5 | MothEnable | 31 |
| ambient | Chipmunk → `chipmunk` | 3 | 1–2 | ChipmunkEnable | 34 |
| ambient | Cockateil → `bird` | 10 | 2–4 | BirdEnable | 37 |
| ambient | GoldFish → `gold_fish` | 1 | 1–1 | GoldFishEnable | 40 |
| ambient | Coin → `coin` | 2 | 1–1 | CoinEnable | 49 |
| ambient | Cricket → `cricket` | 5 | 4–6 | CricketEnable | 52 |
| water | Whale → `whale` | 1 | 1–1 | WhaleEnable | 43 |
| water | Flounder → `flounder` | 2 | 2–4 | FlounderEnable | 46 |
| water | Frog → `frog` | 5 | 4–6 | FrogEnable | 55 |

**VillageMania** — Konstruktorliste wie Utopia **plus** `setVillageCreatures` (192-252), die nichts
leert; Einträge gleicher Klasse stehen damit doppelt (z. B. RedCow 10/4–8 und 8/4–8). Dekorator wie Utopia.

| Kat. | Mob → id | Gew. | min–max | Flag | Zeile |
|---|---|---|---|---|---|
| monster | Robot1 → `bomb_omb` | 25 | 4–8 | Robot1Enable | 194 |
| monster | Robot2 → `robo_pounder` | 16 | 2–8 | Robot2Enable | 197 |
| monster | Robot3 → `robo_gunner` | 12 | 2–4 | Robot3Enable | 200 |
| monster | Robot4 → `robo_warrior` | 8 | 1–2 | Robot4Enable | 203 |
| monster | Robot5 → `robo_sniper` | 20 | 4–8 | Robot5Enable | 206 |
| monster | GiantRobot → `jeffery` | 8 | 1–2 | JefferyEnable | 209 |
| monster | SpiderDriver → `spider_driver` | 20 | 3–5 | SpiderDriverEnable | 212 |
| monster | Godzilla → `mobzilla` | 2 | 1–1 | GodzillaEnable | 215 |
| ambient | Firefly → `firefly` | 10 | 3–6 | FireflyEnable | 218 |
| creature | Girlfriend → `girlfriend` | 1 | 2–3 | GirlfriendEnable | 221 |
| creature | Boyfriend → `boyfriend` | 1 | 2–3 | BoyfriendEnable (0) | 224 |
| creature | RedCow → `apple_cow` | 8 | 4–8 | CowEnable | 227 |
| creature | GoldCow → `golden_apple_cow` | 6 | 2–6 | CowEnable | 228 |
| creature | EnchantedCow → `enchanted_golden_apple_cow` | 4 | 2–4 | CowEnable | 229 |
| ambient | EntityButterfly → `butterfly` | 25 | 3–6 | ButterflyEnable | 232 |
| ambient | EntityLunaMoth → `moth` | 20 | 1–5 | MothEnable | 235 |
| ambient | Chipmunk → `chipmunk` | 5 | 1–2 | ChipmunkEnable | 238 |
| ambient | Cockateil → `bird` | 15 | 2–4 | BirdEnable | 241 |
| ambient | Tshirt → `t_shirt` | 2 | 1–1 | TshirtEnable | 244 |
| ambient | Coin → `coin` | 2 | 1–1 | CoinEnable | 247 |
| ambient | BandP → `criminal` | 15 | 1–2 | CriminalEnable | 250 |

**Islands** — `setIslandCreatures` (62-119) leert alle vier Listen; Dekorator bleibt wie Konstruktor.

| Kat. | Mob → id | Gew. | min–max | Flag | Zeile |
|---|---|---|---|---|---|
| ambient | EntityButterfly → `butterfly` | 5 | 2–6 | ButterflyEnable | 72 |
| ambient | Cockateil → `bird` | 4 | 1–2 | BirdEnable | 75 |
| ambient | EntityLunaMoth → `moth` | 5 | 2–4 | MothEnable | 78 |
| ambient | Firefly → `firefly` | 10 | 4–8 | FireflyEnable | 81 |
| ambient | Dragon → `dragon` | 1 | 1–2 | DragonEnable | 84 |
| ambient | Stinky → `stinky` | 2 | 1–2 | StinkyEnable | 87 |
| ambient | CliffRacer → `cliff_racer` | 20 | 3–6 | CliffRacerEnable | 90 |
| ambient | CloudShark → `cloud_shark` | 1 | 1–1 | CloudSharkEnable | 93 |
| ambient | GoldFish → `gold_fish` | 5 | 2–4 | GoldFishEnable | 96 |
| monster | CreepingHorror → `creeping_horror` | 60 | 4–8 | CreepingHorrorEnable | 99 |
| monster | TerribleTerror → `terrible_terror` | 25 | 3–6 | TerribleTerrorEnable | 102 |
| monster | LurkingTerror → `lurking_terror` | 1 | 1–1 | LurkingTerrorEnable | 105 |
| monster | PitchBlack → `nightmare` | 15 | 3–6 | NightmareEnable | 108 |
| monster | LeafMonster → `leaf_monster` | 35 | 2–4 | LeafMonsterEnable | 111 |
| monster | EnderReaper → `ender_reaper` | 25 | 2–4 | EnderReaperEnable | 114 |
| monster | HerculesBeetle → `hercules_beetle` | 5 | 1–2 | HerculesBeetleEnable | 117 |

**Crystal** — `setCrystalCreatures` (121-190) leert alle Listen; Dekorator: flowers, grass, trees,
bigMushrooms, mushrooms, reeds jeweils `-999` (184-189).

| Kat. | Mob → id | Gew. | min–max | Flag | Zeile |
|---|---|---|---|---|---|
| creature | CrystalCow → `crystal_apple_cow` | 1 | 1–4 | CowEnable | 131 |
| ambient | Fairy → `fairy` | 10 | 4–8 | FairyEnable | 134 |
| ambient | Peacock → `peacock` | 5 | 4–8 | PeacockEnable | 137 |
| ambient | Mantis → `mantis` | 1 | 1–1 | MantisEnable | 140 |
| monster | Rotator → `rotator` | 4 | 1–2 | RotatorEnable | 143 |
| monster | Vortex → `vortex` | 3 | 1–2 | VortexEnable | 146 |
| monster | Urchin → `crystal_urchin` | 15 | 2–4 | UrchinEnable | 149 |
| monster | DungeonBeast → `dungeon_beast` | 30 | 4–6 | DungeonBeastEnable | 152 |
| monster | Rat → `rat` | 40 | 4–6 | RatEnable | 155 |
| ambient | EntityButterfly → `butterfly` | 10 | 2–4 | ButterflyEnable | 158 |
| ambient | Cockateil → `bird` | 4 | 1–2 | BirdEnable | 161 |
| ambient | EntityLunaMoth → `moth` | 4 | 1–2 | MothEnable | 164 |
| water | Whale → `whale` | 1 | 1–2 | WhaleEnable | 167 |
| water | Crab → `crab` | 1 | 1–2 | CrabEnable | 170 |
| water | Flounder → `flounder` | 5 | 6–8 | FlounderEnable | 173 |
| water | Irukandji → `irukandji` | 4 | 2–3 | IrukandjiEnable | 176 |
| water | Skate → `skate` | 2 | 3–6 | SkateEnable | 179 |
| water | Frog → `frog` | 1 | 3–5 | FrogEnable | 182 |

**Chaos** — `setChaosCreatures` (254-436) leert alle Listen; Dekorator: `flowersPerChunk 2`,
`grassPerChunk 4`, `treesPerChunk 1`, bigMushrooms/mushrooms/reeds `-999` (263-268).

| Kat. | Mob → id | Gew. | min–max | Zeile |
|---|---|---|---|---|
| ambient | EntityButterfly → `butterfly` | 20 | 3–6 | 270 |
| ambient | EntityLunaMoth → `moth` | 10 | 1–5 | 273 |
| ambient | Cockateil → `bird` | 10 | 2–4 | 276 |
| ambient | Firefly → `firefly` | 15 | 3–6 | 279 |
| ambient | CliffRacer → `cliff_racer` | 30 | 3–6 | 282 |
| ambient | CloudShark → `cloud_shark` | 2 | 1–1 | 285 |
| ambient | GoldFish → `gold_fish` | 10 | 2–4 | 288 |
| ambient | Fairy → `fairy` | 5 | 2–4 | 291 |
| ambient | Baryonyx → `baryonyx` | 2 | 2–4 | 294 |
| ambient | Bee → `bee` | 2 | 2–4 | 297 |
| ambient | Cassowary → `cassowary` | 2 | 2–4 | 300 |
| ambient | Dragonfly → `dragonfly` | 2 | 2–4 | 303 |
| ambient | Peacock → `peacock` | 2 | 2–4 | 306 |
| ambient | StinkBug → `stink_bug` | 3 | 2–4 | 309 |
| ambient | Ostrich → `ostrich` | 1 | 1–2 | 312 |
| ambient | Chipmunk → `chipmunk` | 1 | 1–2 | 315 |
| creature | Beaver → `beaver` | 1 | 1–2 | 318 |
| creature | RedCow → `apple_cow` | 3 | 2–4 | 321 |
| creature | GoldCow → `golden_apple_cow` | 2 | 2–4 | 322 |
| creature | EnchantedCow → `enchanted_golden_apple_cow` | 1 | 2–4 | 323 |
| monster | Vortex → `vortex` | 1 | 1–2 | 326 |
| monster | PitchBlack → `nightmare` | 1 | 1–2 | 329 |
| monster | TerribleTerror → `terrible_terror` | 4 | 2–6 | 332 |
| monster | Alosaurus → `alosaurus` | 1 | 1–1 | 335 |
| monster | Basilisk → `basilisk` | 1 | 1–1 | 338 |
| monster | Robot1 → `bomb_omb` | 5 | 2–8 | 341 |
| monster | Robot2 → `robo_pounder` | 2 | 1–4 | 344 |
| monster | Robot3 → `robo_gunner` | 2 | 1–4 | 347 |
| monster | Robot4 → `robo_warrior` | 1 | 1–2 | 350 |
| monster | Robot5 → `robo_sniper` | 2 | 3–5 | 353 |
| monster | CaterKiller → `cater_killer` | 1 | 1–1 | 356 |
| monster | CaveFisher → `cave_fisher` | 5 | 1–5 | 359 |
| monster | CreepingHorror → `creeping_horror` | 5 | 1–5 | 362 |
| monster | Cryolophosaurus → `cryolophosaurus` | 5 | 1–5 | 365 |
| monster | Urchin → `crystal_urchin` | 2 | 1–5 | 368 |
| monster | DungeonBeast → `dungeon_beast` | 2 | 1–5 | 371 |
| monster | EmperorScorpion → `emperor_scorpion` | 1 | 1–1 | 374 |
| monster | EnderKnight → `ender_knight` | 2 | 1–2 | 377 |
| monster | EnderReaper → `ender_reaper` | 1 | 1–1 | 380 |
| monster | Hammerhead → `hammerhead` | 1 | 1–1 | 383 |
| monster | HerculesBeetle → `hercules_beetle` | 1 | 1–1 | 386 |
| monster | TrooperBug → `jumpy_bug` | 1 | 1–1 | 389 |
| monster | Molenoid → `molenoid` | 1 | 1–1 | 392 |
| monster | Mothra → `mothra` | 1 | 1–1 | 395 |
| monster | Brutalfly → `brutalfly` | 1 | 1–1 | 398 |
| monster | Rat → `rat` | 10 | 1–10 | 401 |
| monster | Rotator → `rotator` | 1 | 1–3 | 404 |
| monster | Scorpion → `scorpion` | 2 | 1–3 | 407 |
| monster | SpitBug → `spit_bug` | 2 | 1–3 | 410 |
| monster | Nastysaurus → `nastysaurus` | 1 | 1–1 | 413 |
| monster | TRex → `t_rex` | 1 | 1–1 | 416 |
| monster | LeafMonster → `leaf_monster` | 2 | 1–4 | 419 |
| monster | Pointysaurus → `pointysaurus` | 2 | 1–4 | 422 |
| monster | Leon → `leonopteryx` | 1 | 1–1 | 425 |
| monster | Mantis → `mantis` | 1 | 1–1 | 428 |
| monster | LurkingTerror → `lurking_terror` | 1 | 1–1 | 431 |
| monster | GammaMetroid → `wtf` | 1 | 1–1 | 434 |

Flags der Chaos-Tabelle folgen dem Muster `<Klasse>Enable` (Ausnahmen wie oben: BirdEnable,
NightmareEnable; Robot1..5 → `Robot1Enable`..`Robot5Enable`, TrooperBug → `TrooperBugEnable`,
GammaMetroid → `GammaMetroidEnable`, Leon → `LeonEnable`), jeweils Default 1 (manifest).

Abweichung zur Recherche: keine; 02-dimensions-worldgen.md:115-118 deckt die Utopia-Liste korrekt ab.

#### Portierung 1.21.1

- **Fünf Biom-JSONs** `data/orespawn/worldgen/biome/{utopia,villages,islands,crystal,chaos}.json`: `temperature 0.7`, `downfall 0.5`; `has_precipitation` offen: der 1.7.10-Regen-Default von `BiomeGenBase` wurde nicht geprüft. 353825 ist die Kartenfarbe (`setColor`), keine Grasfarbe; Himmels- und Nebelfarbe kommen aus den Providern (world-03).
- **Spawner.** In JSON nur die Einträge, die kein Flag haben: das sind die Vanilla-Grundlisten von Utopia und VillageMania. Alle OreSpawn-Einträge über einen eigenen Java-`BiomeModifier` (Codec registriert, JSON-Instanz je Biom), der `ModConfigSpec`-Flags liest; Config-Typ COMMON oder STARTUP wegen Ladezeitpunkt (siehe OreSpawnWorld). `MobSpawnSettings.SpawnerData(type, weight, min, max)` hat dieselbe Bedeutung wie `SpawnListEntry`.
- **Kategorien 1:1** nach biome_map.json (`ambient` → `AMBIENT`). Risiko: große Tiere in `AMBIENT` (Chaos: Baryonyx, Cassowary, Ostrich; Islands: Dragon) teilen sich dort die Ambient-Obergrenze; die Spawnbedingungen stehen in den `SpawnPlacements` der Entity-Batches.
- **Doppelte Einträge** in VillageMania bleiben doppelt; eine gewichtete Liste erlaubt das.
- **Dekorator.** `-999` heißt „kein Feature“. Utopia/VillageMania/Islands: Blumen 4, Gras 6 → eigene `placed_feature`s mit `minecraft:count` auf Vanilla-`flower_default` und `patch_grass`. Chaos: Blumen 2, Gras 4, Bäume 1. Offen: Baumart für `treesPerChunk 1`; das bestimmt `BiomeGenBase.func_150567_a` in Vanilla, nicht diese Klasse.
- **`Height(0.1, 0.5)`** für Crystal ist ein Terrainparameter des alten Noise-Generators und gehört in den `ChunkGenerator` von world-03, nicht ins Biom.
- **Id-Konflikt im Original.** Jede Provider-Instanz legt ein neues Biom unter derselben Id an; in 1.21.1 entfällt das (Registry-Einträge sind einmalig). Kein Portierungsaufwand.
- Datapack statt Chunk-Code, also kein Chunkgrenzen-Risiko.
