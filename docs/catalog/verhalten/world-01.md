# Verhalten: world-01

Der Batch besteht aus einer einzigen, aber sehr großen Klasse: `GenericDungeon` (7111 Zeilen) ist
der Baukasten für fast alle handgebauten OreSpawn-Strukturen. Die Klasse hat keinen eigenen
Registry-Eintrag und keinen Zustand außer 38 fest verdrahteten Loot-Listen und drei Bild-Arrays.
Sie wird als Singleton `OreSpawnMain.MyDungeon` angelegt (OreSpawnMain.java:5444) und von zwei
Stellen aufgerufen: von der Weltgenerierung `OreSpawnWorld` (Overworld, End, Utopia, Mining,
VillageMania, Islands/Danger, Crystal) und vom Block `DungeonSpawnerBlock`, der eine zufällige
Struktur baut. Insgesamt enthält sie 47 öffentliche `make…`-Strukturen, von Einzelspawnern
(1×5×1) bis zum King-/Queen-Altar (61×68×61). Alle Blöcke werden direkt in Chunks geschrieben
(`OreSpawnMain.setBlockFast`), ohne Rücksicht auf Chunkgrenzen. Für die Portierung heißt das:
Etwa drei Viertel passen als `Feature`, die großen Burgen, Pyramide, Labor, Altäre, Kyuubi- und
Alien-Dungeon müssen `Structure`s mit Teilen werden, die nur innerhalb ihrer Bounding Box schreiben.

### GenericDungeon

#### Rolle

- Instanz: `OreSpawnMain.MyDungeon = new GenericDungeon()` (OreSpawnMain.java:5444), Feld (OreSpawnMain.java:198).
- Aufruf 1, Weltgenerierung: `OreSpawnWorld implements IWorldGenerator` ruft pro Chunk-Populate die
  `add…`-Methoden auf, die Ursprung, Oberfläche und Seltenheit bestimmen und dann `MyDungeon.make…`
  aufrufen (Tabellen unten). Globale Sperre `OreSpawnWorld.recently_placed`: wird bei jedem
  `generate`-Aufruf um 1 verringert (OreSpawnWorld.java:25-27) und nach einer Platzierung auf 50
  gesetzt, beim King-/Queen-Altar auf 100 (OreSpawnWorld.java:2743). Statisch, gilt über alle
  Dimensionen hinweg, wird nicht gespeichert.
- Aufruf 2, Item/Block: `ItemRandomDungeon.onItemUse` setzt `MyDungeonSpawnerBlock` auf den
  angeklickten Block, wenn dieser `stone`, `cobblestone`, `grass` oder `dirt` ist und `clickedY >= 40`
  (ItemRandomDungeon.java:40-54). `DungeonSpawnerBlock.onBlockAdded` plant ein Update nach 400 Ticks
  (DungeonSpawnerBlock.java:33); `updateTick` löscht sich und den Block darüber
  (DungeonSpawnerBlock.java:44-45) und würfelt `nextInt(50)` (DungeonSpawnerBlock.java:46):
  0 `Trees.FairyTree`, 1 `Trees.FairyCastleTree`, 22 `RubyDungeon.makeDungeon`,
  23 `BMaze.buildBasiliskMaze` (alle nicht in dieser Klasse), sonst eine der Methoden hier
  (Zuordnung in der Übersichtstabelle, Spalte „Spawner-Block“). `makeFrogPond`, `makePumpkin` und
  `makeRoundRotator` bekommen `clickedY + 1` (DungeonSpawnerBlock.java:177-183).
- Kein Client-Code, keine Ticks, keine NBT. Einige Methoden prüfen `world.isRemote`, andere nicht
  (z. B. `makeAlienWTFDungeon`, `makeEnderKnightDungeon`); für den Port egal, weil Worldgen
  serverseitig läuft.

#### Gemeinsame Mechanik

| Helfer | Verhalten |
|---|---|
| `FastSetBlock(world,x,y,z,block)` (:219-221) | `OreSpawnMain.setBlockFast(world,x,y,z,block,0,2)`: schreibt direkt in `Chunk`, verwirft x/z außerhalb ±30 000 000 und `y < 0` bzw. `y >= 256`, ruft bei Flag 2 nur `markBlockForUpdate`, keine Nachbar-Updates, kein Licht-Neuberechnen (OreSpawnMain.java:5499-5524) |
| `world.setBlock(...)` | normaler Vanilla-Pfad, benutzt für Spawner, Truhen, Türen, Wasser und einige Deko-Blöcke |
| `setThisBlock` (:101-108) | 50/50 `mossy_cobblestone` oder `cobblestone` |
| `getChestTileEntity` / `getSpawnerTileEntity` (:110-130) | holt die TileEntity, `null` wenn falscher Typ |
| Spawner | `TileEntityMobSpawner.func_145881_a()` liefert `MobSpawnerBaseLogic` (joined.srg:11848, SRG-Name, Bedeutung aus MCP-Signatur), darauf `setEntityName(String)` (`func_98272_a`, methods.csv:4808). Der String ist der Legacy-`EntityList`-Name, Zuordnung zu Registry-IDs unten |
| Truhen-Loot | `WeightedRandomChestContent.generateChestContents(rand, liste, truhe, n)`; Eintrag = (Item, Meta, min, max, Gewicht) |
| Feste Slots | `chest.setInventorySlotContents(slot, stack)` bei Haunted House, Igloo, Rotator Station, Urchin Spawner, Altären und Stufe-6-Belohnungen |
| Kreise | `(int)(cpos + r*cos + 0.5f)`: Java-Cast schneidet Richtung 0 ab, bei negativen Weltkoordinaten verschieben sich Kreise daher um einen Block. Für 1:1 den Cast nachbauen, nicht `Mth.floor` |
| Metadaten | Truhen `setBlockMetadataWithNotify(..., 2..5, 3)` = Ausrichtung; `stained_hardened_clay`-Meta = Farbe; `stonebrick` Meta 3; `quartz_block` Meta 2 (Säule); Fackel-, Leiter-, Hebel-, Knopf-, Treppen- und Kolben-Meta stehen jeweils in der Zeile |

#### Aufrufer und Platzierungsregeln

Koordinaten der `add…`-Methoden sind Chunk-Minimum `chunkX*16` plus Offset. `posY` ist bei den
meisten Oberflächen-Suchen der Luftblock über der geforderten Oberfläche.

**Overworld (Dimension 0)**: nur wenn `DisableOverworldDungeons == 0` (Default 0, manifest) und
`recently_placed == 0` (OreSpawnWorld.java:269). Wassergruppe: genau eine von sechs per
`world.rand.nextInt(6)` (OreSpawnWorld.java:270-288), danach die Landkette, die beim ersten Erfolg
abbricht (OreSpawnWorld.java:289-306).

| Struktur | `add…` | Chance pro Aufruf | Biom (1.7.10-Name) | Suche | übergebenes y |
|---|---|---|---|---|---|
| Play Pool | `addPlayPool` | 1/350 (OreSpawnWorld.java:1160) | `Ocean` | 4 Versuche, y 100→41, Luft über `water` (:1165-1171) | `posY` |
| Water Dragon Lair | `addWaterDragonLair` | 1/350 (:1393) | `Ocean` | wie oben (:1403) | `posY-1` |
| Gold Fish Bowl | `addGoldFishBowl` | 1/350 (:1202) | `Ocean` | wie oben (:1212) | `posY-1` |
| Girlfriend Island | `addGirlfriendIsland` | 1/300 (:1414) | `Ocean` | wie oben (:1424) | `posY-1` |
| Monster Island | `addMonsterIsland` | 1/300 (:1435) | `Ocean` | wie oben (:1445) | `posY-1` |
| Frog Pond | `addFrogPond` | 1/350 (:1181) | `Plains` | Luft über `grass` (:1191) | `posY-1` |
| Small Bee Hive / Mantis Hive (50/50, :1008) | `addANest` | 1/230 (:1000) | `Forest`, `ForestHills`, `Jungle`, `JungleHills`, `Birch Forest`, `Birch Forest Hills` (:1004) | 5 Versuche, y 128→41 solange Luft, darunter `grass` (:1005-1007) | `posY` |
| Haunted House | `addHauntedHouse` | 1/285 (:981) | `Plains`, `Taiga`, `Swampland` (:985) | 5 Versuche, y 100→41 (:986-988) | `posY` |
| Leaf Monster | `addLeafMonster` | 1/275 (:1223) | `Plains` | 4 Versuche, Luft über `grass` (:1233) | `posY` |
| Spit Bug Lair | `addSpitBug` | 1/190 (:1267) | `Swampland` | Luft über `grass` (:1277) | `posY` |
| Igloo | `addIgloo` | 1/220 (:1289) | `Ice Plains` | Luft über `Blocks.snow` (:1299) | `posY-2` |
| Bouncy Castle | `addBouncyCastle` | 1/230 (:1311) | `Desert` | Luft über `sand` (:1321) | `posY-1` |
| Rubber Ducky Pond | `addRubberDuckyPond` | 1/275 (:1245) | `Plains` | Luft über `grass` (:1255) | `posY` |

Alle setzen `recently_placed = 50`.

**End (Dimension 1)**: `generateEnd` wählt `world.rand.nextInt(4)` (OreSpawnWorld.java:217-229). Keine
Sperre. 3 Versuche, y 90→11, Luft über `end_stone`.

| Struktur | Chance | Platzprüfung |
|---|---|---|
| Ender Knight Outpost (`addEndKnights`) | 1/25 (:1559) | `quickSpaceCheck`: x/z −2..9 bei y+4 Luft (:2820-2829) |
| Ender Reaper Graveyard (`addEndReapers`) | 1/25 (:1575) | `quickSpaceCheck` |
| Ender Dragon Hospital (`addHospital`) | 1/25 (:1591) | `quickSpaceCheck` |
| Ender Castle (`addEnderCastle`) | 1/50 (:1607) | `quickBigSpaceCheck`: x/z −5..24 bei y+8 Luft (:2831-2840) |

**Utopia (`DimensionID`, „Dimension-Utopia“, manifest)**:
- King-/Queen-Altar (`addKingAltar`): nur wenn weder Huge Tree noch Apple Trees noch Other Trees
  gesetzt wurden und `recently_placed == 0` (OreSpawnWorld.java:30-32). Chance `nextInt(2000) == 1`
  (:2725), 8 Versuche mit x/z = 3 + `nextInt(10)` (:2729-2730), y 100→51, Luft über `grass`; scheitert
  `quickReallyBigSpaceCheck` (x/z −5..54 bei y+8 Luft, :2842-2851), wird abgebrochen (:2734). 50/50
  King/Queen (:2737), übergeben `posY-1`, Sperre 100 (:2743).
- Danach `addRubyDungeon`, bei Misserfolg `addGenericDungeon` (:37-39).
- `addGenericDungeon`: 1/16 (:2101); `LessLag == 1` zusätzlich 1/2, `LessLag == 2` zusätzlich 1/4
  (:2104-2109); x/z = Chunk + `nextInt(4)`, y = 5 + `nextInt(40)` ohne jede Oberflächenprüfung,
  keine Sperre (:2110-2113).

**Mining (`DimensionID2`, „Dimension-Extreme“, manifest)**: bei `recently_placed == 0 && nextInt(95) == 1`
(OreSpawnWorld.java:65) wählt `nextInt(7)` (:66): 0 Basilisk Maze (andere Klasse), 1 Kyuubi, 2 Bee Hive,
3 Shadow, 4 Alien/WTF, 5 Ender Knight, 6 Leonopteryx Nest (:67-86); sonst `addGenericDungeon` (:90).
Suche über ein 6×6-Raster (x/z 0,3,…,15) von y 128 abwärts:

| Struktur | Kriterium | übergebenes y |
|---|---|---|
| Bee Hive (`addBeeHive`) | tiefster Punkt Luft über `grass`, Suche bis y 31, Bedingung `lowestY > 40` (:2117-2148) | `lowestY + 3` |
| Alien/WTF (`addAlienWTF`) | wie Bee Hive (:2152-2183) | `lowestY` |
| Ender Knight (`addEnderKnight`) | wie Bee Hive (:2187-2218) | `lowestY` |
| Shadow (`addShadowDungeon`) | wie Bee Hive (:2257-2288) | `lowestY` |
| Kyuubi (`addKyuubiDungeon`) | tiefster Punkt Luft über beliebigem Nicht-Luft-Block, `lowestY > 40` (:2787-2817) | `lowestY - 2` |
| Leonopteryx Nest (`addLeonNest`) | höchster Punkt Luft über `grass` in y 128→81, `highestY = posY + 1`, Bedingung `> 80` (:2222-2253) | `highestY` |

Alle setzen `recently_placed = 50`.

**VillageMania (`DimensionID3`)**: `addGenericDungeon` immer (OreSpawnWorld.java:107), dann jeweils bei
`recently_placed == 0` (:108-116):

| Struktur | Chance | Suche | y |
|---|---|---|---|
| Damsel in Distress | 1/250 (:1333) | 4 Versuche, y 100→41, Luft über `grass`, `quickSpaceCheck` (:1341) | `posY-1` |
| Spider Hangout | 1/350 (:1352), nur bei `SpiderDriverEnable != 0` (Default 1, manifest) (:1355) | wie oben (:1363) | `posY-1` |
| Red Ant Hangout | 1/250 (:1374) | wie oben (:1382) | `posY-1` |

**Islands (`DimensionID4`, „Dimension-Islands“ im manifest, in der Recherche „Danger Dimension“)**:
Bedingung `recently_placed == 0 && nextInt(100) == 0 && D4BigSpaceCheck(chunkX*16, 7, chunkZ*16)`
(OreSpawnWorld.java:120); `D4BigSpaceCheck` verlangt bei y 11 in x −25..39, z −25..29 nur Luft, `log`,
`MyAppleLeaves` oder `MyScaryLeaves` (:2853-2869). Dann `nextInt(19)` (:121), Fälle bis :163:

| i | Struktur | Zusatzprüfung bei y+18 muss Luft sein | y |
|---|---|---|---|
| 0-2 | `makeEnormousCastle` / `makeEnormousCastleQ` 50/50 (:2343) | x −20..32, z −4..32 (:2335-2336) | `posY` |
| 3-6 | `makeDungeon` (`addD4GenericDungeon`; `LessLag != 0` → nur 1/4, :2589) | keine | `posY` |
| 7 | `makeEnderCastle` | x/z −5..24 (:2465-2466) | `posY` |
| 8 | `makeIncaPyramid` | x −10..49, z −10..39 (:2490-2491) | `posY` |
| 9 | `makeRobotLab` | x −5..59, z −5..69 bei **y+4**, `log`/Apple-/Scary-Leaves erlaubt (:2515-2523) | `posY` |
| 10 | `makeMiniDungeon` | keine | `posY` |
| 11 | Ruby Bird Dungeon (andere Klasse) | – | – |
| 12 | `makeCephadromeAltar` | keine | `posY` |
| 13 | `makeGreenhouseDungeon` | x −2..24, z −4..24 (:2365-2366) | `posY` |
| 14 | `makeNightmareRookery` | x −5..24, z −4..4 (:2390-2391) | `posY` |
| 15 | `makeStinkyHouse` | x/z −8..19 (:2415-2416) | `posY` |
| 16 | `makeWhiteHouse` | x −20..29, **z −20..299** (:2440-2441) | `posY` |
| 17 | `makePumpkin` (`addPumpkin`) | keine | `posY+1` |
| 18 | `makeRainbow` (`addD4Rainbow`) | keine, x/z = 4 + `nextInt(8)`, y = 70 + `nextInt(20)` (:2580-2585) | – |

Für i 0–17 (außer 11): Ursprung x/z = Chunk + `nextInt(8)`, y 20→5 bis zum ersten `grass`-Block;
`y` ist der Grasblock selbst (:2313-2317 u. a.); bei `LessLag != 0` zusätzlich 1/2. Sperre 50.
Unabhängig davon pro Chunk `nextInt(300) == 0` → `makeCloudSharkDungeon` bei x/z = 4 + `nextInt(8)`,
y = 150 + `world.rand.nextInt(10)`, ohne Sperre (:167-169, :2573-2578).

**Crystal (`DimensionID5`)**: nur wenn kein Fairy Tree gesetzt wurde und `recently_placed == 0`, Kette bis
zum ersten Erfolg (OreSpawnWorld.java:177-181). 3 Versuche, y 100→51, Luft über `CrystalGrass`, Sperre 50.

| Struktur | Chance | Config |
|---|---|---|
| Rotator Station | 1/150 (:1662) | `RotatorEnable` (Default 1, manifest) (:1659) |
| Urchin Spawner | 1/180 (:1704) | `UrchinEnable` (Default 1, manifest) (:1701) |
| Crystal Haunted House | 1/230 (:1722) | – |
| Round Rotator | 1/150 (:1683) | `RotatorEnable` (:1680) |
| Crystal Battle Tower | 1/280 (:1740) | – |

#### Übersicht aller Strukturen

Ausdehnung relativ zum übergebenen Ursprung (x, y, z inklusive), aus den Schleifengrenzen abgeleitet.
Spalte „Spawner-Block“ = Fall von `nextInt(50)` in DungeonSpawnerBlock.java.

| Methode (Zeilen) | x | y | z | Größe x×y×z | Welt | Spawner-Block |
|---|---|---|---|---|---|---|
| `makeDungeon` (:132-217) | 0..11 | 0..5 | 0..11 | 12×6×12 | Utopia, Mining, Village, Islands | 21 |
| `makeEnormousCastle` (:223-417) | −37..55 | −1..80 | −28..55 | 93×82×84 (Stufen 1-5: x −37..31, y 0..62, z −4..31) | Islands | 2 |
| `makeRotatorStation` (:834-857) | 0 | 4..8 | 0 | 1×5×1 | Crystal | 3 |
| `makeBeeHive` (:859-904) | 0..9 | −30..0 | 0..9 | 10×31×10 | Mining | 4 |
| `makeHauntedHouse` (:938-1053) | −3..3 | 0..4 | −3..3 | 7×5×7 | Overworld | 5 |
| `makeMantisHive` (:1055-1104) | 0..12 | −6..19 | 0..12 | 13×26×13 | Overworld | 6 |
| `makeKyuubiDungeon` (:1137-1253) | 0..34 | −22..5 | −15..14 | 35×28×30 | Mining | 7 |
| `makeSmallBeeHive` (:1406-1494) | −3..9 | 1..21 | −3..9 | 13×21×13 | Overworld | 8 |
| `makeShadowDungeon` (:1496-1577) | 0..18 | −9..9 | 0..18 | 19×19×19 | Mining | 9 |
| `makeAlienWTFDungeon` (:1610-1732) | −19..17 | −17..2 | −21..15 | 37×20×37 | Mining | 10 |
| `makeEnderKnightDungeon` (:1832-1940) | 0..12 | 0..5 | −2..6 | 13×6×9 | End, Mining | 11 |
| `makePlayPool` (:1969-1992) | −1..4 | 16..18 | 0 | 6×3×1 | Overworld | 12 |
| `makeWaterDragonLair` (:1994-2084) | −10..10 | −1..7 | −10..10 | 21×9×21 | Overworld | 13 |
| `makeCloudSharkDungeon` (:2086-2118) | −1..1 | −1..1 | −1..1 | 3×3×3 | Islands | 14 |
| `makeLeafMonsterDungeon` (:2120-2260) | −3..6 | −4..16 | −3..6 | 10×21×10 | Overworld | 15 |
| `makeMiniDungeon` (:2262-2433) | −6..9 | 0..11 | 0..9 | 16×12×10 | Islands | 16 |
| `makeGoldFishBowl` (:2435-2513) | −1..5 | 1..8 | −1..5 | 7×8×7 | Overworld | 17 |
| `makeEnderReaperGraveyard` (:2515-2586) | 0..10 | −4..4 | 0..12 | 11×9×13 | End | 18 |
| `makeUrchinSpawner` (:2601-2658) | max. −17..18 | −1..17 | max. −17..18 | bis 36×19×36, zufällig | Crystal | – |
| `makeSpitBugLair` (:2660-2718) | −8..8 | 0..12 | −8..8 | 17×13×17 | Overworld | 19 |
| `makeIgloo` (:2720-2832) | −6..6 | 0..5 | −6..6 | 13×6×13 | Overworld | 20 |
| `makeEnderDragonHospital` (:2834-3006) | −6..9 | 0..9 | 0..9 | 16×10×10 | End | 24 |
| `makeCrystalHauntedHouse` (:3008-3115) | −3..3 | 0..4 | −3..3 | 7×5×7 | Crystal | 25 |
| `makeBouncyCastle` (:3117-3216) | −4..4 | 0..4 | −4..4 | 9×5×9 | Overworld | 26 |
| `makeEnderCastle` (:3218-3502) | −4..26 | 0..16 | −4..26 | 31×17×31 | End, Islands | 27 |
| `makeDamselInDistress` (:3630-3735) | −4..4 | 0..9 | −4..4 | 9×10×9 | Village | 28 |
| `makeIncaPyramid` (:3737-3994) | −10..50 | 0..19 | −10..40 | 61×20×51 | Islands | 29 |
| `makeRobotLab` (:4085-4132) | −10..19 | 0..43 | −1..48 | 30×44×50 | Islands | 30 |
| `makeKingAltar` (:4391-4441) | −5..55 | −9..58 | −5..55 | 61×68×61 | Utopia | 31 |
| `makeLeonNest` (:4716-4767) | −10..10 | −10..5 | −10..10 | 21×16×21 | Mining | 32 |
| `makeCephadromeAltar` (:4769-4865) | −4..4 | 0..4 | −4..4 | 9×5×9 | Islands | 34 |
| `makeCrystalBattleTower` (:4867-4991) | −10..10 | 0..23 | −10..10 | 21×24×21 | Crystal | 33 |
| `makeGirlfriendIsland` (:4993-5057) | −5..5 | −1..4 | −3..3 | 11×6×7 | Overworld | 35 |
| `makeGreenhouseDungeon` (:5059-5194) | 0..22 | 0..13 | −1..14 | 23×14×16 | Islands | 36 |
| `makeMonsterIsland` (:5196-5264) | −5..5 | −1..4 | −3..3 | 11×6×7 | Overworld | 37 |
| `makeNightmareRookery` (:5266-5348) | −6..21 | 0..21 | Zufallsweg, max. −53..53 | 28×22×bis 107 | Islands | 38 |
| `makeStinkyHouse` (:5350-5414) | −5..19 | 1..3 | −4..12 | 25×3×17 | Islands | 39 |
| `makeRubberDuckyPond` (:5416-5454) | −5..6 | 0..6 | −5..5 | 12×7×11 | Overworld | 40 |
| `makeWhiteHouse` (:5456-5467) | −5..21 | 0..20 | −15..18 | 27×21×34 | Islands | 41 |
| `makeQueenAltar` (:5734-5784) | −5..55 | −9..58 | −5..55 | 61×68×61 | Utopia | 42 |
| `makeFrogPond` (:6056-6077) | −3..3 | 0..2 | −3..3 | 7×3×7 | Overworld | 43 |
| `makePumpkin` (:6079-6217) | 0..13 | 0..17 | 0..11 | 14×18×12 | Islands | 44 |
| `makeRoundRotator` (:6219-6289) | −6..6 | 0..12 | 0 | 13×13×1 | Crystal | 45 |
| `makeRainbow` (:6291-6419) | −14..13 | 26..40 | −3..3 | 28×15×7 | Islands | 46 |
| `makeEnormousCastleQ` (:6421-6615) | wie King | wie King | wie King | 93×82×84 | Islands | 47 |
| `makeSpiderHangout` (:7032-7084) | 0..19 | −1..19 | 0..19 | 20×21×20 | Village | 48 |
| `makeRedAntHangout` (:7086-7110) | 0..15 | −1..15 | 0..15 | 16×17×16 | Village | 49 |

#### Strukturen im Detail

Positionen relativ zum Ursprung als (x, y, z). „n Ziehungen“ ist der Wert `n` für
`generateChestContents`. Die Listen selbst stehen im Abschnitt Loot-Tabellen.

##### makeDungeon (Generic Dungeon)

- Innenraum 12×6×12 Luft (:135-141); Boden `mossy_cobblestone` (:142-147); Decke und vier Wände je Block
  `setThisBlock`, also 50/50 `mossy_cobblestone`/`cobblestone` (:148-169).
- Spawner (6,1,6), Mob per `nextInt(12)` gleichverteilt (:170-209): Scorpion, Alien, Cryolophosaurus, WTF?,
  Kyuubi, Bee, Cloud Shark, Lurking Terror, Terrible Terror, Rotator, Rat, Dungeon Beast.
- Truhe (6,1,1) ohne gesetzte Ausrichtung, `chestContentsList`, 5 + `nextInt(7)` = 5..11 Ziehungen (:212-216).

##### makeEnormousCastle (Challenge Dungeon, King) und makeEnormousCastleQ (Queen)

Stufenzahl: `level = 1 + nextInt(6)`, bei `level <= 3` mit `nextInt(3) != 1` noch +3 (:231-234).
Abgeleitet: P(1)=P(2)=P(3)=1/18, P(4)=P(5)=P(6)=5/18.

Grundbau King (:235-321):
- Luft x −20..31, y 1..25, z −4..31 (:235-241).
- Boden `stone` 28×28 bei y 0 (:242-247), Decke `bedrock` bei y 16 (:248-253), Wände `iron_bars` y 1..15 (:254-269),
  `ExtremeTorch` in den vier Innenecken y 1 per `world.setBlock` (:270-273).
- Außenring y 0 `stone` x/z −4..31, am Rand `nether_brick_fence` bei y 1 (:274-283).
- Spawner Terrible Terror in vier Ecksäulen (−3,·,−3), (−3,·,30), (30,·,−3), (30,·,30), je y 1..4, zusammen 16 (:285-306).
- Spawner Emperor Scorpion (14, 2..4, 14), 3 Stück (:307-321).

Etagen per `buildLevel` (:322-344):

| Etage | Ursprung | width | height | pw | Eck-Spawner (King) | Eck-Spawner (Queen, :6521-6540) | stepside | stepoff | holelen | decor | Bedingung |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | (1,16,1) | 26 | 10 | 4 | Cloud Shark | Rotator | 1 | −1 | 5 | 1 | immer |
| 2 | (1,26,1) | 26 | 10 | 4 | Lurking Terror | Bee | 0 | 0 | 4 | 2 | `level >= 2` |
| 3 | (2,36,2) | 24 | 9 | 4 | Rotator | Mantis | 1 | 1 | 4 | 3 | `level >= 3` |
| 4 | (2,45,2) | 24 | 9 | 3 | Bee | Mothra | 0 | 0 | 4 | 4 | `level >= 4` |
| 5 | (3,54,3) | 22 | 8 | 3 | Mantis | Brutalfly | 1 | 1 | 4 | 5 | `level >= 5` |
| 6 | (3,62,3) | 22 | 16 | 3 | Mothra | Vortex | 0 | 0 | 3 | 6 | `level >= 6` |

`buildLevel` (:419-522):
- Luft −pw..width+pw−1 × y 1..height−1 (:420-426); `bedrock`-Boden y 0 und -Decke y height (:427-438).
- Wände z=0 und z=width−1 aus `bedrock` (:439-446); Wände x=0 und x=width−1 aus `bedrock`, deren Eckspalten
  `gold_block` (:447-458).
- Außenring bei y 0 `stone`, äußerster Rand y 1 `nether_brick_fence` (:459-468).
- Außentreppe: je ein `stone`-Block pro Höhe 1..height−1 diagonal, x ab width/2 − height/2, an z = −1
  (stepside ≠ 0) oder z = width (:469-481).
- Bei `stepoff >= 0` ein Loch von `holelen` Blöcken ab x = width/2 im Außenring bei z = −1−stepoff bzw.
  width+stepoff (:482-497).
- Eck-Spawner mit `critter` in 4 Säulen bei (−(pw−1), ·, −(pw−1)) usw., je y 1..4, also 16 (:498-520);
  danach `addLevelDecorations(decor, level)` (:521).

`addLevelDecorations` (:524-768): Für decor 1–5 zwei Spawner bei (w/2, 2..3, w/2), umgeben von
`bedrock`-Kreuz y 1..4. Der Mob hängt von `level − decor` ab, die Belohnungsstufe ist `level − decor + 1`:

| level − decor | King-Mob | Queen-Mob (:6725-6940) | Belohnung |
|---|---|---|---|
| 0 | Alosaurus | T. Rex | `level1ContentsList` |
| 1 | T. Rex | Nastysaurus | `level2ContentsList` |
| 2 | Basilisk | Basilisk | `level3ContentsList` |
| 3 | Hercules Beetle | Hercules Beetle | `level4ContentsList` |
| 4 | Jumpy Bug | Jumpy Bug | `level5ContentsList` |
| 5 (nur decor 1, level 6) | Hammerhead | CaterKiller | feste Royal- bzw. Queen-Ausrüstung |

- Durchstiege: ungerades decor Boden-Loch (w−2, 0, w−2) und Decken-Loch (1, h, 1); gerades decor umgekehrt
  (:610-611, :643-644, :680-681, :721-722, :765).
- decor 1 zusätzlich `MyRTPBlock` (Random Teleport Block) auf den vier Diagonalen bei y 1 (:761-764).
- decor 6 (:528-584):
  - `netherrack` mit `fire` auf den vier Dachecken, Dachmitte Luft.
  - 4 Spawner Nightmare bei y height+2 um die Mitte.
  - Innenraum x/z 1..w−2, y 1..4 mit `dirt` gefüllt, darin 3 Spawner Large Worm (w/2, 2..4, w/2).
  - Schacht Luft (1, 0..9, 1); Truhen mit Belohnungsstufe 1 bei y+4.

`fill_chests` (:770-832): vier Truhen bei (1, 1, w/2) Meta 5, (w−2, 1, w/2) Meta 4, (w/2, 1, 1) Meta 3,
(w/2, 1, w−2) Meta 2, je 5 + `nextInt(7)` = 5..11 Ziehungen (:794). Bei Belohnung 6 statt Loot feste Slots:

| Truhe | King (`fill_chests`) | Queen (`fill_chestsQ`, :6968-7030) |
|---|---|---|
| 1 | Slot 1 `ThePrinceEgg` (:791) | Slot 1 `ThePrincessEgg` (:6989) |
| 2 | Slot 1 `RoyalHelmet`, Slot 2 `RoyalBody` (:802-803) | `QueenHelmet`, `QueenBody` (:7000-7001) |
| 3 | `RoyalLegs`, `RoyalBoots` (:814-815) | `QueenLegs`, `QueenBoots` (:7012-7013) |
| 4 | `MyRoyal` (:826) | `MyRoyal` (:7024) |

`fill_chestsQ` wird nur von decor 1 aufgerufen (:6964). Queen-decor 2–6 nutzt `fill_chests` mit denselben
Stufenlisten.

Zugang King (:345-399):
- Plattform `quartz_block` 11×11 bei x −20..−10, z 9..19, y 16, Zaun ringsum außer drei Blöcken (:345-353).
- Brücke x −9..−4, z 12..16, y 16; bei x −10 und −3 Tore aus `netherrack`-Säulen mit `fire` (:354-374).
- Treppe 5 breit (z 12..16) diagonal von (−21,16) bis (−37,0), 6 Blöcke Luft darüber, am Fuß Feuersäulen (:375-399).

Stufe 6 zusätzlich 100 Versuche: Spawner Large Worm bei y −1, x/z = `nextInt(84)` − 42 + 14, nur außerhalb des
mittleren Quadrats (Index 21..63) (:400-416).

Queen-Unterschiede:
- Boden und Außenring `obsidian` statt `stone` (:6443, :6475).
- Eck-Spawner unten Lurking Terror (:6484-6503), Mitte Emperor Scorpion (:6505-6519).
- Plattform, Brücke und Treppe `MyBlockAmethystBlock` (:6546, :6566, :6590).
- `buildLevelQ`: Eckspalten `MyBlockRubyBlock` statt Gold (:6647-6650), Außenring und Stufen `obsidian` (:6660, :6672).

##### makeRotatorStation

- (0,4,0) `CrystalStone`, (0,5,0) und (0,6,0) Spawner Rotator, (0,7,0) `CrystalStone` (:837-848).
- Truhe (0,8,0) Meta 2 (:849-856):
  - Slot 1 `RotatorEgg` × 1 + `nextInt(5)` = 1..5.
  - Slot 2 und Slot 3 je `CrystalCoal` × 4 + `nextInt(16)` = 4..19.

##### makeBeeHive (groß, Mining)

- Luft x/z 0..9, y 0..−4 (:865-871); Boden `coal_ore` bei y −30 (:872-877).
- Wände y −1..−29 im Ring: ungerade Tiefe `gold_ore`, gerade `coal_ore`; innen Luft (:878-894).
- Spawner Bee (5, −2 − j·7, 5) für j 0..3, also y −2, −9, −16, −23 (:896-902).
- Truhen `fill_beehive_chests` (:906-936): für Tiefe j = 2, 4, …, 28 je 4 Truhen bei (1,−j,5) Meta 5,
  (8,−j,5) Meta 4, (5,−j,1) Meta 3, (5,−j,8) Meta 2, zusammen 56 Truhen.
  `beeContentsList`, je 1 + `nextInt(5)` = 1..5 Ziehungen.

##### makeHauntedHouse und makeCrystalHauntedHouse

- 7×5×7 per `world.setBlock` (:957-982).
  - Boden y 0 `cobblestone` (Crystal: `CrystalStone`).
  - Dach y 4 `planks` (Crystal: `CrystalPlanksBlock`).
  - Wände `planks` mit Glasreihe y 3.
  - Tür-Öffnung (3, 1..2, 0).
- Einrichtung:
  - Normal: `furnace` (2,1,2) Meta 2, `crafting_table` (1,1,2), Truhe (0,1,2) Meta 2 (:983-993).
  - Crystal: `CrystalFurnaceBlock`, `CrystalWorkbenchBlock` (:3056-3059).
- Spawner (0,1,0) Rat, (0,2,0) Ghost, (0,3,0) Ghost Pumpkin Skelly (:1038-1052, :3100-3114).

Truheninhalt, feste Slots, jeweils eigene Wahrscheinlichkeit:

| Slot | Haunted House (:994-1036) | Crystal Haunted House (:3064-3098) |
|---|---|---|
| 0 | `compass` 1/2 | `compass` 1/2 |
| 1 | `map` 1/2 | – |
| 2 | `cooked_porkchop`×8 1/2 | `MyPeacock`×8 2/3 |
| 3 | `torch`×32 1/2 | `CrystalTorch`×32 2/3 |
| 4 | `coal`×16 1/2 | `CrystalCoal`×16 1/2 |
| 5, 6 | je `bed` 1/2 | je `bed` 1/2 |
| 7 | `wooden_door` 1/2 | `wooden_door` 1/2 |
| 8 | `iron_pickaxe` 1/2 | `MyCrystalPinkPickaxe` 1/2 |
| 9 | `iron_sword` 1/2 | `MyCrystalPinkSword` 1/2 |
| 10 | `iron_axe` 1/2 | `MyCrystalPinkAxe` 1/2 |
| 11 | `bucket` 1/2 | `KrakenRepellent` immer |
| 12 | `MyOreSaltBlock`×4 1/2 | – |
| 13 | `chest` 1/2 | `chest` 1/2 |

##### makeMantisHive

- Luft x/z 0..12, y 0..19 (:1061-1067).
- Umgekehrte Stufenpyramide: Lagen mit width 13, 11, …, 1 bei y 0..−6, Versatz +1 je Lage. Ring gerade Lage
  `gold_ore`, ungerade `emerald_ore`, innen Luft (:1071-1093).
- Lagen width 11, 9, 7 je 4 Truhen `mantisContentsList`, 3 + `nextInt(7)` = 3..9 Ziehungen (:1086-1088, :1106-1135),
  zusammen 12.
- Spawner Mantis (6, −2..0, 6), 3 Stück (:1097-1103).

##### makeKyuubiDungeon

- Eingang (:1148-1200):
  - Luft 5×5 y 0..−4.
  - `sandstone`-Deckel y 5 mit Mittelloch, `sandstone`-Ring y 0..4.
  - `stone`-Schacht y −1..−19 (Ring 5×5).
  - `water` 3×3 bei y −20..−21, `stone` darunter bei y −22.
- Halle (:1201-1216): x 15..34, y −20..−3, z −15..14 (20×18×30), Hülle `netherrack`, innen Luft.
- Tunnel (:1217-1235): x 4..15, y −20..−16, z 0..4.
  - Boden und Decke `stone`.
  - Seitenwände unten und oben `stone`, mittig y −19..−17 `lava`.
  - Innen Luft; öffnet Schacht und Hallenwand.
- Hallenboden y −19, relativ zur Hallenecke (15,·,−15):
  - `addlavasquare` bei (2,2), (4,6), (12,10), (6,15), (3,22): `lava` mit 4 `netherrack`-Nachbarn (:1240-1244, :1255-1261).
  - `fire` bei (7,1), (5,9), (2,12), (16,18), (2,27), (18,28) (:1247-1252).
- `addkyuubi` bei Hallenecke + (5, 19) (:1245, :1263-1303):
  - Ring `nether_brick` 9×9 mit `lava` innen, darüber Ring 7×7 mit `lava`.
  - Spawner Kyuubi (4, 2..4, 4), 3 Stück.
  - Truhe (4,5,4) Meta 2, `kyuubiContentsList`, 7 + `nextInt(7)` = 7..13 Ziehungen.
- `addblaze` bei Hallenecke + (10, 5) (:1246, :1305-1404):
  - `obsidian`-Stufenturm: 7×4×7, 5×1×5, 3×6×3, 1×5×1 (Höhe 16).
  - 8 Spawner `Blaze` (Vanilla) um die Spitze bei relativ y 13..14.
  - 4 Truhen bei y 4, `blazeContentsList`:

    | Truhe | Meta | Ziehungen |
    |---|---|---|
    | (0,4,3) | 4 | 4..8 |
    | (3,4,0) | 2 | 3..7 |
    | (3,4,6) | 3 | 5..9 |
    | (6,4,3) | 5 | 6..10 |

##### makeSmallBeeHive

- Luft x/z −3..9, y 14..20 (:1415-1421).
- Unterbau (:1422-1440):
  - `sponge` 7×7 bei y 14.
  - Darunter `mossy_cobblestone`-Säulen der Länge `nextInt(7)·2 − |i−3| − |k−3|`, mindestens 1.
  - Mittelsäule 14 lang, reicht bis y 1; überschreibt den Schwamm bei y 14.
- Stock (:1441-1471):
  - 3× abwechselnd `sponge`-Ring 7×7 und 9×9, y 15..20.
  - Dach `sponge` 7×7 bei y 21.
- Eingang Luft x −1..0, z 2..3, y 15..17 (:1472-1479).
- Spawner Bee (1, 15..17, 1), 3 Stück (:1480-1486).
- Truhe (3,15,3) Meta 5, `beeContentsList`, 7 + `nextInt(5)` = 7..11 Ziehungen (:1487-1493).

##### makeShadowDungeon

- Untere umgekehrte Pyramide, Lagen width 19, 17, …, 1 bei y 0..−9 (:1506-1556):
  - Ring gerade Lage `obsidian`, ungerade `bedrock`.
  - Wo i oder k in w/2 ± 1 liegt: `soul_sand`.
  - Innen Luft.
- Lagen width 15..9 (y −2..−5):
  - Ungerade Tiefe (y −3, −5): 4 Truhen `shadowContentsList`, 3..9 Ziehungen (:1579-1608), plus 4 Spawner Ender Reaper.
  - Gerade Tiefe (y −2, −4): 4 Spawner Nightmare.
  - Spawner stehen in den Innenecken (:1524-1552).
  - Zusammen 16 Spawner, 8 Truhen.
- Obere Pyramide gespiegelt y 0..9, `obsidian`/`bedrock` ohne Seelensand, ohne Inhalt (:1557-1576).

##### makeAlienWTFDungeon

- Ursprung um 17 abgesenkt (`cposy -= depth − 3`, :1616).
- Kern (:1617-1628): hohler `lapis_ore`-Würfel 5×5×5 bei x/z −2..2, y 0..4.
- Schacht (:1629-1663): 4×4-Ring `lapis_ore` bei x/z −1..2, y 3..19; pro Höhe ein `stone`-Trittblock im
  Uhrzeigersinn, also eine Wendeltreppe.
- Vier Räume `makePart` mit Gängen 3 breit, 4 hoch (Ring `lapis_ore`, :1667-1731):

| Raum | Ursprung | Richtung (dx,dz) | width | height | difficulty | Spawner | Truhen |
|---|---|---|---|---|---|---|---|
| 1 | (0,0,7) | (1,1) | 9 | 5 | 1 | 2 | 1 |
| 2 | (7,0,0) | (1,−1) | 11 | 6 | 2 | 4 | 2 |
| 3 | (−7,0,0) | (−1,1) | 13 | 7 | 3 | 6 | 3 |
| 4 | (0,0,−7) | (−1,−1) | 15 | 8 | 4 | 8 | 4 |

`makePart` (:1734-1830):
- Luft; Boden `quartz_block` mit `obsidian`-Kreuz in der Mitte; Decke und Wände `obsidian`.
- Pro difficulty zwei Spawner übereinander ab y 2 an der Mitte und der Mitte+(dx,dz), je 50/50 Alien oder WTF?.
- Bis zu 4 Truhen `AlienWTFContentsList`, 3 + `nextInt(5)` = 3..7 Ziehungen.

Zusammen 20 Spawner, 10 Truhen.

##### makeEnderKnightDungeon (Ender Knight Outpost)

Querschnitte in x-Richtung, Höhe 6 (:1836-1939):
- x 0..3 Luft 5×5.
- x 4 `obsidian`-Wand mit Durchgang (2, 1..3).
- x 5 Breite 7 (z −1..5); x 6..10 Breite 9 (z −2..6); x 11 Breite 7 (z −1..5):
  - Boden innen `end_stone`, Rand `obsidian`, Decke `obsidian`.
- x 12 massive `obsidian`-Wand.

Einrichtung:
- In jedem Querschnitt an den Positionen k 1, 2, w−3, w−2 `makeShelves` (:1942-1967), zusammen 28 Plätze, `nextInt(4)`:
  - 0: Truhe `KnightContentsList`, 3..7 Ziehungen.
  - 1: `bookshelf`-Säule 1..4 hoch.
  - 2: `web`-Säule 1..4 hoch.
  - 3: nichts.
- Spawner Ender Knight (8, 2..3, 2) (:1896-1907).

##### makePlayPool

- 4 Spawner Attack Squid (0..3, 16, 0) (:1974-1980).
- Doppeltruhe (1,17,0)+(2,17,0), nur (1,17,0) gefüllt: `SquidContentsList`, 3..7 Ziehungen (:1981-1986).
- `water` (0..3, 18, 0), `flowing_water` bei (−1,18,0) und (4,18,0) (:1987-1991). Schwebt 16 Blöcke über der Wasseroberfläche.

##### makeWaterDragonLair

- Scheibe bei y 7 (:1999-2020):
  - Radius < 10 aus `bedrock`, Ring 5 < r < 6 `iron_block`.
  - Speichen `iron_block` entlang der Achsen; Mitte Luft, 4 `glowstone` daneben.
- Kreis r = 10 (:2021-2039):
  - y 1 `glowstone`; y 2 und y 3 je 50/50 `lapis_block` oder `MyWaterDragonSpawnBlock`; y 4 `glowstone`; y 5..6 `bedrock`.
- Insel `sand` 7×7 bei y 0, `stone` darunter (:2040-2045).
- Palme: `leaves` 5×5 y 3, `log`-Stamm y 1..3, diagonale `log` (:2046-2058).
- Spawner Water Dragon (±1,3,0), (0,3,±1) (:2059-2078).
- Truhe (0,1,−1), `WaterDragonContentsList`, 4..8 Ziehungen (:2079-2083).

##### makeCloudSharkDungeon

- `glowstone` (0,0,0) und (0,−1,0).
- 4 Spawner Cloud Shark (±1,0,0), (0,0,±1).
- Truhe (0,1,0), `CloudSharkContentsList`, 4..8 Ziehungen (:2091-2117).

##### makeLeafMonsterDungeon

- Luft x −2..5, z −3..1, y 0..3 (:2125-2131); `log` in Luft/`tallgrass` unter dem Stamm y −1..−4 (:2132-2142).
- Stamm 4×4 `log` y 0..9, hohl, Eingang unten; Leiter Meta 2 bei (1..2, 0..9, 2) (:2143-2174).
- Plattform y 9 (x/z −3..6) `log` mit `leaves`-Rand (:2177-2188).
- Krone:
  - `leaves`-Wände y 10..12 (:2189-2199).
  - y 13 `log`-/`leaves`-Ringe (:2200-2212).
  - `leaves` y 14, `log` y 15, `leaves` y 16 (:2213-2233).
- Spawner Leaf Monster auf der Plattform bei (−2,10,−2), (5,10,5), (−2,10,5), (5,10,−2) (:2234-2253).
- Doppeltruhe (1,10,5)+(2,10,5), nur erste gefüllt: `LeafMonsterContentsList`, 12 + `nextInt(5)` = 12..16 Ziehungen (:2254-2259).

##### makeMiniDungeon und makeEnderDragonHospital

Gemeinsames Gerüst 10×7×10 (:2267-2295, :2839-2867):
- Wände `iron_bars`, Ecksäulen und Oberkante y 6 (Mini `cobblestone`, Hospital `obsidian` bzw. `end_stone`).
- Boden y 0 (Mini `cobblestone`, Hospital `end_stone`).
- Außentreppe 4 breit bei z 3..6 von x −6 bis −1, y 1..6, Rand mit Geländer und Licht:
  - Mini: `planks`, `fence`, `torch` (:2316-2330).
  - Hospital: `end_stone`, `iron_bars`, `glowstone` (:2898-2912).

Mini:
- Dachringe `grass` bei y 7 und y 8 (:2296-2315).
- Spawner:
  - 12 Butterfly auf dem Ring 3..6 bei y 9 (:2331-2343).
  - Ecksäulen `cobblestone` y 7..10 mit Spawner bei y 11: (0,0) Terrible Terror, (9,9) Butterfly, (0,9) Terrible Terror, (9,0) Butterfly (:2344-2381).
  - Boden y 1: (1,1) und (8,8) Terrible Terror, (8,1) und (1,8) Butterfly, (4,4) und (5,5) Lurking Terror (:2382-2427).
- Truhe (3,1,3), `MiniContentsList`, 4..8 Ziehungen (:2428-2432).

Hospital:
- Dachringe `MyEyeOfEnderBlock` bei y 7 (1..8), y 8 (2..7), y 9 (3..6) (:2868-2897).
- `obsidian` an den Ecken y 7..8 (:2913-2920).
- 4 `EntityEnderCrystal` bei (0.5,9,0.5), (0.5,9,9.5), (9.5,9,0.5), (9.5,9,9.5), zufällige Gierung; im selben Block y 9 wird `bedrock` gesetzt (:2921-2936).
- Spawner Ender Reaper bei (3,9,3), (3,9,6), (6,9,3), (6,9,6) auf dem Dachring (:2937-2968); Nightmare bei (1,1,1), (1,1,8), (8,1,1), (8,1,8) (:2969-3000).
- Truhe (4,1,4), `HospitalContentsList`, 6..10 Ziehungen (:3001-3005).

##### makeGoldFishBowl

- y 1 `glass` 5×5; y 2 `sand` mit `glass`-Ring (−1..5).
- y 3 und y 4 `water` mit `glass`-Ring; bei y 3 `glowstone` in den vier Innenecken.
- y 5..7 Luft mit `glass`-Ring, y 8 `glass`-Dach 5×5.
- Spawner Gold Fish (2,6,2) (:2437-2512).

##### makeEnderReaperGraveyard

- `end_stone` füllt Luft unter der Fläche y −1..−4 (:2519-2527).
- Boden `end_stone` 11×13 y 0; `iron_bars`-Zaun y 1..4 am Rand, innen Luft (:2528-2545).
- Spawner Ender Reaper in den vier Innenecken y 1 (:2546-2577).
- 8 Gräber bei (1,6), (3,4), (5,4), (7,4), (3,8), (5,8), (7,8), (9,6) (:2578-2585). `makeAGrave` (:2588-2599):
  - Grabstein `obsidian` (x,1,z−1), `obsidian` (x,0,z+1).
  - Truhe im Boden (x,0,z), `GraveContentsList`, 3 + `nextInt(3)` = 3..5 Ziehungen.

##### makeUrchinSpawner

- Drei Kristallstrahlen, Material `CrystalStone`, `CrystalCrystal`, `TigersEye` (:2604-2633). Je Strahl:
  - Richtung dx, dz = `nextFloat() − nextFloat()`, dy = 0.5 + `nextFloat()/2`.
  - Dicke `nextInt(2)` + 1.
  - Länge 10 + Dicke·3 + `nextInt(5)`, bei Strahl 2 und 3 halbiert.
- Spawner Crystal Urchin (0, 1..3, 0); (0,0,0) Luft (:2634-2649).
- Truhe (0,−1,0) Meta 2 (:2650-2657):
  - Slot 1 `UrchinEgg` 1..5.
  - Slot 2 und 3 je `CrystalCoal` 4..19.

##### makeSpitBugLair

- First-Grat in x-Richtung bei z 0, für i 0..8 bei (±i, 9−i, 0):
  - `mossy_cobblestone`, darüber zwei `stained_hardened_clay` Meta 13 (:2668-2675).
- `emerald_ore` (0, 10..12, 0) (:2676-2678).
- Spawner Spit Bug (0, 7..9, 0) (:2679-2693).
- Rautenboden, halbe Diagonale 8 (:2695-2712):
  - y 0 `stained_hardened_clay` Meta 5.
  - Rand y 1 Meta 13, y 2 `stonebrick` Meta 3; innen Luft.
- Truhe (0,1,0), `SpitBugContentsList`, 4 + `nextInt(4)` = 4..7 Ziehungen (:2713-2717).

##### makeIgloo

- Kuppel aus Kreisen (:2723-2760):
  - r 6: y 1 `Blocks.snow` (Vollblock), y 2 `ice`, y 3 `snow`.
  - r 5: y 4 `ice`.
  - y 5: r 4 `snow`, r 3 `ice`, r 2 `snow`, r 1 `ice`.
  - Innenraum wird nicht geleert.
- Tür: `planks` (−6,0,0), Luft darüber, `ItemDoor.placeDoorBlock` Holztür Richtung 2 (:2761-2764).
- Spawner Rat (2,1,−4), Ghost (−1,1,1), Ghost Pumpkin Skelly (3,1,4) (:2765-2779).
- Truhe (−3,1,−3) Meta 2, jeder Slot mit 1/2 (:2780-2831):

| Slot | Inhalt |
|---|---|
| 0 | `compass` |
| 1 | `map` |
| 2 | `cooked_porkchop`×8 |
| 3 | `torch`×32 |
| 4 | `coal`×16 |
| 5, 6 | je `bed` |
| 7 | `wooden_door` |
| 8 | `iron_pickaxe` |
| 9 | `iron_sword` |
| 10 | `iron_axe` |
| 11 | `bucket` |
| 13 | `chest` |
| 14 | `gold_nugget`×6 |
| 15 | `gold_nugget`×8 |
| 16 | `gold_nugget`×10 |

##### makeBouncyCastle

- 9×5×9 (:3140-3165):
  - Boden, Dach und Wände `MyLavafoamBlock`.
  - Ecksäulen `stained_hardened_clay` Meta 14.
  - Tür Luft (0, 1..2, −4).
- Spawner in drei Reihen bei y 3 (:3166-3210):

| x | z = −1 | z = 0 | z = +1 |
|---|---|---|---|
| −1..1 bei z = 3 | Silverfish (−1) | Rat (0) | Scorpion (+1) |
| 3 | Silverfish | Rat | Scorpion |
| −3 | Silverfish | Rat | Scorpion |

- Truhe (3,3,3) Meta 2, `BouncyContentsList`, 6..10 Ziehungen (:3211-3215).

##### makeEnderCastle

Außen und Wände:
- Sockel `obsidian` x/z −3..25, y 0; `iron_bars` am Rand y 1 (:3226-3239).
- Kernmauer x/z 0..22, y 1..12 aus `bedrock` (:3240-3271):
  - Zinnen y 12 bei geradem (i+k) Luft.
  - y 10 bei geradem (i+k) je 1/4 `MyEnderKnightSpawnBlock`, `MyEnderReaperSpawnBlock`, `MyEndermanSpawnBlock`, `MyEnderDragonSpawnBlock`.
  - y 7 bei ungeradem (i+k) `MyEyeOfEnderBlock`.
- Außenmantel x/z −1..23 `bedrock` bei y 6 und y 9..11, bei y 7 nur ungerades (i+k) (:3272-3300).
  - An jedem y-6-Block 1/2 `MyEnderPearlBlock` darunter (y 5), davon 1/3 noch einer bei y 4.

Ecktürme `makeAColumn` bei (−2,0,−2), (20,0,−2), (−2,0,20), (20,0,20), Höhe 13, dir 0..3 (:3301-3304, :3504-3628):
- Plattform `obsidian` 9×9 bei y 15, Zinnenkranz y 16.
- Schaft 5×5 `obsidian` y 1..15; `iron_bars` in Flächenmitte wo `j%3` 0 oder 1.
- Türöffnungen y 1..2 und y 9..10 an der Innenecke.
- Innen Wendel aus `nether_brick`-Stufen.

Innenebenen:
- y 8 Boden 21×21 `obsidian`, Mittelkreuz und Diagonalen `bedrock` (:3305-3314).
- y 9 Mitte (:3315-3346):
  - `lava` 5×5 mit `bedrock`-Rahmen.
  - Darüber Säule: `bedrock`, `ender_chest` Meta 2 (y 10), `obsidian`, `bedrock` mit Kreuz, 4 `torch`, `bedrock` ×2, `dragon_egg` bei y 15.
- Spawner bei (11±5, 9, 11±5) Ender Reaper und darüber y 10 Ender Knight, zusammen 8 (:3347-3386).
- y 4 Zwischenboden `bedrock` bis 5 Blöcke vom Rand; Innenkante mit `iron_bars` 3 hoch (:3387-3418).
  - Treppe `bedrock` (16,3,10..12), (15,2,…), (14,1,…), Lücke in den Gittern (17, 5..7, 10..12) (:3419-3444).
- Unten Mitte Spawner Ender Reaper (11,1,11), Ender Knight (11,2,11) (:3445-3455).
- y 5 drei Nischen mit je 2 Spawnern CaveFisher und einer Truhe `EnderCastleContentsList`, 6..10 Ziehungen (:3456-3501):

| Nische | Truhe | Meta | CaveFisher |
|---|---|---|---|
| 1 | (1,5,11) | 2 | (1,5,10), (1,5,12) |
| 2 | (11,5,1) | 3 | (10,5,1), (12,5,1) |
| 3 | (11,5,21) | 4 | (10,5,21), (12,5,21) |

##### makeDamselInDistress

- Haus 9×5×9 (:3645-3669):
  - Boden und Wände `cobblestone`, je Block 1/8 `mossy_cobblestone`.
  - Tor 3 breit, 3 hoch bei z −4.
- Dach y 5 und y 6 abgestuft, Giebel über z −4 bis y 9 (:3670-3707).
- Zelle: `iron_bars`-Wand bei z 1, x −3..3, y 1..4 (:3708-3713).
- Spawner Scorpion (−3,1,−3), (3,1,−3) (:3714-3723).
- Truhe (3,1,3) Meta 2, `DamselContentsList`, 10..14 Ziehungen (:3724-3728).
- Entity `Girlfriend` bei (−2,1,3) in der Zelle, zufällige Gierung (:3729-3734).

##### makeIncaPyramid

Pyramide und Treppen:
- Stufenpyramide mit 10 Lagen (:3752-3780):
  - Lage j hat (41−2j) × (31−2j), Versatz (j,j,j).
  - Rand `stone`, 1/2 `cobblestone`, danach 1/4 `mossy_cobblestone`; Lage 0 ganz `stonebrick`; innen Luft.
  - Fackeln Meta 3/4 an den Innenwänden jeder dritten Lage.
- Vier Treppen je 5 breit, 19 Stufen, Mitte `stone_slab`, Rand `stonebrick`, Fackeln oben und unten; füllen `stone` bis zum Boden (:3781-3908):
  - −x: x −10..8 bei z 13..17.
  - +x: x 32..50.
  - −z: z −10..8 bei x 18..22.
  - +z: z 22..40.

Tempel und Einrichtung:
- Tempel auf (10,10,10), 21×9×11 (:3909-3960):
  - Wände wie oben, Boden und Decke `stonebrick`.
  - Durchgänge 3 breit an allen Seiten mit `fence`-Sturz.
  - Bei j = 6 und ungeradem (i+k) `lit_redstone_lamp`, bei j = 7 Luft.
- Kranz `stone_slab` bei y 19 (:3961-3970).
- 5 Becken `makepoolalter`: 3×3 `cobblestone` mit `water` in der Mitte (:3971-3975, :3996-4003).
- 4 `CreeperRepellent` bei (19,12,14), (21,12,16), (19,12,16), (21,12,14) (:3976-3979).
- Spawner Molenoid (18,11,15) (:3980-3984).
- Falltür (22,11,15) Meta 3 über Leiterschacht (22, 1..9, 15) mit `cobblestone`-Rückwand (:3985-3992).

Gräber `makeincagraves` in der Pyramide bei y 0 (:3993, :4005-4018):
- x 5, 11, 17, 23, 29, 35 × z 5, 10 (dir 1) und z 20, 25 (dir 3), also 24 Gräber.
- `makeincagrave` (:4020-4083):
  - `grass` mit `red_flower`/`yellow_flower` links und rechts, Grabstein `stone` plus zwei `stone_slab`.
  - 1/3 Spawner Ghost (x,2,z).
  - Truhe Meta 2 vor bzw. hinter dem Grab, `IncaPyramidContentsList`, 10..14 Ziehungen.

##### makeRobotLab

Labor und Halle:
- Labor 10×6×20 (x 0..9, z 0..19) (:4094-4124):
  - `quartz_block`, Boden-Mittelstreifen `iron_block`, Dach bündig ohne Rand.
  - Doppel-Eisentür (4..5, 1, 0) Richtung 3, `stone_button` Meta 4 bei (3,2,−1) und (6,2,−1).
- 6 Säulen `makerobopillar` bei (0,0,6), (0,0,13), (0,0,19) dir 0 und (9,0,6), (9,0,13), (9,0,19) dir 1 (:4126-4131). Säule (:4134-4166):
  - 3×3×5 `quartz_block`, `redstone_block` mittig an jeder Seite y 2..3.
  - Spawner Robo-Sniper seitlich (x±1, y+1).
- Haupthalle `makerobomain` bei (−10,0,19): 30×10×30, x −10..19, z 19..48, Front offen x 0..9, y 1..3 (:4168-4205).

Einbauten der Halle:
- `makeroboaltar` (1,0,25) (:4263-4296):
  - `iron_block` 8×8, `quartz_block` 6×6, 4 `redstone_block` mit `torch`.
  - Spawner Robo-Pounder (4,2,28), (5,2,29).
- `makeroborailway` (−7,0,29): zwei `rail`-Spuren z 29..41 bei x −7 und −4, `golden_rail` bei z 31/35/39 mit `lever` Meta 5 dazwischen (:4298-4331).
- `makeroboassemblyline` (16,0,23), 24 lang (:4333-4346):
  - `quartz_block` y 1 bei x 16..17.
  - Jede dritte Position `quartz_stairs` Meta 1 (x 14), `sticky_piston` Meta 3 und `carpet`.
  - Dazwischen `lever` Meta 13.
- `makerobotreasureroom` (−1,0,37), 12×7×8 (:4348-4389):
  - Wände `quartz_block`, Reihe y 3 `iron_bars`, Tür (0..1, 1..3, 37).
  - Spawner Robo-Warrior (9,1,38).
  - Truhen (7,1,38) und (5,1,38) Meta 2, `RobotContentsList`, je 10..14 Ziehungen.
- `makerobotower` (−1,9,28) (:4207-4261):
  - Dachplatte `quartz_block` 12×12, `iron_bars`-Ring mit `redstone_block`-Ecken.
  - 4 Säulen mit Robo-Sniper.
  - Mast 2×3 bei x 4..5, z 33..35, y 14..43; unten massiv, darüber Gitter.

Zusammen 10 Robo-Sniper, 2 Robo-Pounder, 1 Robo-Warrior.

##### makeKingAltar und makeQueenAltar

Gelände und Säulen:
- Luft x/z −5..55, y 0..58 (:4399-4406).
- `grass` 51×51 bei y 0; darunter bis 9 Blöcke tief `dirt`, wo Luft, `tallgrass` oder `water` ist (:4407-4420).
- 4 Säulen bei (1,1,1), (43,1,43), (1,1,43), (43,1,1) (:4421-4424):
  - `makekingcolumn` (:4443-4508): 7×7-Platten `quartz_block` unten und oben, Schaft 5×5 hohl, 44 hoch aus `quartz_block` Meta 2.
  - Muster je `j%4` aus `gold_block` und `emerald_block`.
  - Queen: `obsidian`, `redstone_block`, `MyBlockAmethystBlock` (:5786-5848).
- Decke `quartz_block` 51×51 bei y 47 und 53×53 bei y 48 (:4425-4438); Queen `obsidian` (:5768-5781).

Bildwand `makekingbackground` in der Ebene x = 4 (:4510-4562):
- Rahmen `gold_block` von y 9 bis y 43 und z 8 bis z 42.
- `diamond_block` mit `CrystalTorch` an den Ecken.
- Queen: Rahmen `diamond_block`, Bild `MyBlockRubyBlock` statt `quartz_block` (:5850-5902).

Das Bild ist ein Lauflängen-Array: `king` und `queen` sind identisch, 123 Werte, 33 Zeilen (:96-97).
Jede Zeile beginnt mit `stone`, danach wechselt jeder Lauf das Material; −1 füllt den Rest der Zeile mit
`stone`. Dekodiert, oben = y 42, links = z 9, `#` = `quartz_block` (Queen: `blockruby`), `.` = `stone`:

```
.................................
.................................
...............##................
........##.....##................
........##.....##................
........##.....##................
........##......#................
.........##.....##......####.....
.........##.....##....######.....
..........##....##...##..........
..........###..##...##...........
...........###.##.####...........
............#########............
.............######..............
#################################
.###############################.
..#############################..
...###########################...
.....#######################.....
........#################........
...........###########...........
............#########............
..............#####..............
..............#####..............
..............######....###......
...............######...#####....
...............##############....
................###############..
.................############....
........................#####....
........................###......
.................................
.................................
```

Mittelaltar `makekingcenteraltar` bei (25,0,25) (:4564-4714), gestufte Kreuzform aus `quartz_block`
(Queen `obsidian`, :5904-6054):

| y | Flächen | Extras |
|---|---|---|
| 0 | 21×21, 13×41, 41×13 | – |
| 1 | 17×17, 9×37, 37×9 | Enden `lapis_block` (Queen `MyBlockAmethystBlock`) |
| 2 | 15×15, 7×35, 35×7 | `CrystalTorch` an den Ecken bei y 3 |
| 3 | 13×13, 5×33, 33×5 | – |
| 4 | 5×5 | `CrystalTorch` an den Ecken bei y 5 |

- Truhe (25,4,25) Meta 2, Slot 13 `TheKingEgg` (:4708-4713) bzw. `TheQueenEgg` (:6048-6053).

##### makeLeonNest

- Halbkugel r 10 nach unten (Abstand `(int)sqrt(j²+i²+k²)`) (:4721-4753):
  - Schale ab Abstand 8 je Block zufällig 1/6 `leaves`, `log`, `planks`, `dirt`, `cobblestone`, `mossy_cobblestone`.
  - Innen Luft.
- Luft 21×21 bei y 1..5 (:4754-4761).
- Spawner Leonopteryx (0,−6,0) (:4762-4766).

##### makeCephadromeAltar

| y | Belegung |
|---|---|
| 0 | `cobblestone` 9×9 |
| 1 | 7×7 `cobblestone` mit `stonebrick`-Kreuz und -Ecken |
| 2 | Außen nur Eckpfeiler `stonebrick`; innen 5×5 `cobblestone` mit `stonebrick`-Kreuz und -Ecken |
| 3 | Eckpfeiler `end_stone`; 3×3 `cobblestone` mit `MyEyeOfEnderBlock` in der Mitte und `end_stone`-Ecken |
| 4 | `ExtremeTorch` auf den äußeren Eckpfeilern |

Keine Spawner, keine Truhen (:4769-4865).

##### makeCrystalBattleTower

- y 0..20: jede fünfte Höhe (0, 5, 10, 15, 20) volle Scheibe r < 10 `CrystalStone`; sonst Ring r 10 (:4872-4895).
  - Öffnung Richtung +x (Winkel < 10° oder > 350°) bei `j%5` 1..3.
- Ring `CrystalCrystal` bei y 21..22 (:4897-4905).
- Die Zwischenböden haben keine Öffnungen.
- Stockwerke (Truhe auf der Scheibe, zwei Spawner darüber in der Mitte):

| Truhe y | Spawner y | Mob | Liste | Ziehungen | Zeilen |
|---|---|---|---|---|---|
| 1 | 2..3 | Rat | `CrystalBattleTowerRatContentsList` | 5..9 | :4906-4922 |
| 6 | 7..8 | Dungeon Beast | `CrystalBattleTowerDungeonBeastContentsList` | 5..9 | :4923-4939 |
| 11 | 12..13 | Crystal Urchin | `CrystalBattleTowerUrchinContentsList` | 5..9 | :4940-4956 |
| 16 | 17..18 | Rotator | `CrystalBattleTowerRotatorContentsList` | 5..9 | :4957-4973 |
| 21 | 22..23 | Vortex | `CrystalBattleTowerVortexContentsList` | 6..11 | :4974-4990 |

##### makeGirlfriendIsland und makeMonsterIsland

- Insel (:4998-5013, :5205-5220): Zeilen x −5..5 mit halber Breite 1 (±5), 2 (±4, ±3), sonst 3; `sand` bei y 0, `stone` bei y −1.
- Palme wie beim Water Dragon Lair (:5014-5026, :5221-5233).
- Spawner:
  - Girlfriend Island: Girlfriend (1,3,0), Boyfriend (−1,3,0), Gold Fish (0,3,±1) (:5027-5046).
  - Monster Island: alle vier derselbe Mob, 50/50 Sea Viper oder Sea Monster (:5200-5204, :5234-5253).
- Zwei Truhen (0,1,±1), je 4..8 Ziehungen: `DamselContentsList` (:5047-5056) bzw. `MonsterIslandContentsList` (:5254-5263).

##### makeGreenhouseDungeon

- 23 (x) × 7 × 15 (z) (:5068-5156):
  - Wände `glass`.
  - Dach y 6 `iron_block`; `glowstone` bei i%4 = 3 und k%4 = 3; `glass` bei k%4 = 1.
  - Boden `grass`; Reihen mit i%3 = 2 `water`.
- Beete: im Inneren sonst mit 2/3 `farmland` plus Pflanze bei y 1, gewählt über `nextInt(20)` (:5090-5152):

| Wert | Pflanze | Wert | Pflanze |
|---|---|---|---|
| 0 | `yellow_flower` | 10 | `MyTomatoPlant1` |
| 1 | `red_flower` | 11 | `MyStrawberryPlant` |
| 2 | `brown_mushroom` | 12 | `MyButterflyPlant` |
| 3 | `red_mushroom` | 13 | `MyMothPlant` |
| 4 | `wheat` | 14 | `MyRadishPlant` |
| 5 | `carrots` | 15 | `MyLettucePlant1` |
| 6 | `potatoes` | 16 | `MyFlowerPinkBlock` |
| 7 | `reeds` | 17 | `MyFlowerBlueBlock` |
| 8 | Luft | 18 | `MyQuinoaPlant1` |
| 9 | `MyCornPlant1` | 19 | `MyRicePlant` |

- Luft y 7..13 über der ganzen Fläche (:5157-5163).
- Eingang an z = 0 bei x 6..7 (nutzt width/2 = 7 auf der x-Achse): Doppel-Eisentür Richtung 3, `stone` daneben, `stone_button` Meta 4 bei z −1 (:5164-5173).
- Spawner Triffid (11,8,7) und (11,9,7) über dem Dach (:5174-5187).
- Truhe (11,7,7) auf dem Dach, `GreenhouseContentsList`, 5..9 Ziehungen (:5188-5193).

##### makeNightmareRookery

- Zwei Durchläufe über x −5..20 (:5274-5347). Pro Spalte:
  - z wandert um `nextInt(3) − 1`; der Wert läuft über beide Durchläufe weiter.
  - Höhe h = 1 + `nextInt(20)`.
  - Säule `stone` von y 0 bis h−1, je Höhe j mit Wahrscheinlichkeit 1/(j+5) je Seite ein Anbau.
- Erreicht eine Säule j = 18 (nur bei h ≥ 19):
  - Truhe bei y 19 `NightmareRookeryContentsList`, 4..8 Ziehungen.
  - Spawner Nightmare bei y 20; Säule endet.
- Decompiler-Hinweis `//TODO` in :5274 betrifft nur die Initialisierung.

##### makeStinkyHouse

- Hof x −5..19, z −4..12 bei y 1 (:5359-5375):
  - Rand `fence` mit 1/3 Lücken.
  - Luftfelder mit 1/10 `deadbush`.
- Haus 13×3×10 (x 0..12, z 0..9) bei y 1..3 (:5376-5398):
  - Wände und Dach `planks`, `glass_pane` in Wandnähe der Ecken bei y 2.
  - Jeder Block mit 1/10 Luft (verfallen).
  - Tür (0, 1..2, 4..5).
- Spawner Stink Bug (2,1,2), Stinky (10,1,7) (:5399-5408).
- Truhe (6,1,4), `StinkyHouseContentsList`, 8..12 Ziehungen (:5409-5413).

##### makeRubberDuckyPond

- Spawner Rubber Ducky (0..1, 6, 0) (:5422-5428).
- Doppeltruhe (0..1, 5, 0), nur (1,5,0) gefüllt: `RubberDuckyContentsList`, 8..12 Ziehungen (:5429-5434).
- `glass` darunter (:5435-5436).
- `water` (0..1, 3, 0) mit `flowing_water` seitlich (:5437-5441).
- Teich 12×11 `water` mit `sand`-Rand bei y 0, darüber 2 Luft (:5442-5453).

##### makeWhiteHouse

- Zwei Brunnen `makefountain` bei (−5,0,−15) und (15,0,−15) (:5469-5497):
  - 7×5, Becken `quartz_block` mit `water`, `glowstone` in der Mitte.
  - Säule `quartz_block` y 2..4 mit `water`-Quelle oben; Luft bis y 14.
- Weg `makewalkway` (7,0,−15), 3×10 `quartz_block` mit Stufe (:5499-5518).
- Sockel `makewhbase` (−4,0,−6): 25×25 `quartz_block` y 1 mit `CrystalTorch`-Ecken, 23×23 y 2 (:5520-5537).
- Wände `makewhwalls` (−3,2,−5): 23×23, 6 hoch `quartz_block` mit `glass_pane`-Fenstermustern (:5539-5589).
  - Eisentür (8, 2..3, −5), `stone_button` (9,3,−6).
- Dach `makewhroof` (−4,0,−6): hohle Stufenpyramide y 8..20 (:5591-5635).
  - Rand der untersten Lage schachbrett `emerald_block`, Spitze `emerald_block`.
  - `CrystalTorch` an allen Lagen-Ecken; `fence`-Mast in der Mitte mit 4 `CrystalTorch`.
- Innen `makewhinterior` (−1,2,−3) (:5637-5732):
  - 6 Bankreihen je 8 lang aus `quartz_stairs` (Meta 3/2) mit zwei Reihen `piston_extension` Meta 1 dazwischen.
  - 4 Zellen bei (1,3,15), (5,3,15), (11,3,15), (15,3,15): Spawner Criminal, darunter Truhe `WhiteHouseContentsList`, 3..7 Ziehungen.

##### makeFrogPond

- Spawner Frog (0,2,0).
- `water` 7×7 bei y 0; `water` (0,1,0) mit 4 `flowing_water`.
- `waterlily` auf (±1,2,0), (0,2,±1) (:6058-6076).

##### makePumpkin

- Hohlkörper 14×14×12 aus `stained_hardened_clay` Meta 1 (:6088-6108).
- Gesicht in der Wand z = 0 aus Luftblöcken, Augen y 7..11, Mund y 1..4 (:6109-6178).
- Stiel Meta 13 schräg bei y 14..17 (:6179-6184).
- Innen `planks` 2×2 y 1..5, `netherrack` y 6, `fire` (6..7, 7, 5) (:6185-6202).
- Spawner Ghost Pumpkin Skelly (6,7,6), (7,7,6) (:6203-6216).

##### makeRoundRotator

- Senkrechter Ring in der x-y-Ebene bei z 0 um (0,6): r 6 `bedrock`, r 2 `MyCrystalPinkBlock` (:6223-6236).
- Spawner Rotator auf den Diagonalen (±1, 6±1, 0) (:6237-6256).
- Spawner Dungeon Beast bei (±5,6,0), (0,1,0), (0,11,0) (:6257-6276).
- `CrystalCoal` (±1,6,0), (0,6±1,0) (:6277-6281).
- Truhe (0,6,0) Meta 2, `CrystalBattleTowerVortexContentsList`, 6..11 Ziehungen (:6282-6288).

##### makeRainbow

Wolke (Meta 0):
- y 35: 24×3 `stained_hardened_clay`, `water` alle 3 Blöcke mit `flowing_water` darunter (:6299-6311).
- y 26..28: Ringe 26×5, 28×7, 26×5 (:6312-6356).
- y 29: Platte 24×3 (:6357-6364).

Regenbogen:
- Ab y 30 acht rechteckige Bögen m = 3..10 in der Ebene z 0: Beine bei x = m und x = −(m+1), Scheitel bei y 30+m (:6365-6375).
- Farben aus `blkcolors` = 14, 1, 4, 5, 3, 11, 10, 6 (:98).

Inhalt:
- Spawner Cloud Shark (2, 30..32, 0), (−3, 30..32, 0) (:6376-6405).
- Zwei Truhen (0,30,0) und (−1,30,0) Meta 2, beide `RainbowContentsList`, je 10..14 Ziehungen (:6406-6418).

##### makeSpiderHangout und makeRedAntHangout

Spider Hangout (:7032-7084):
- 20×21×20: y −1 `stone`, y 0 `gravel`, darüber Luft bis y 19.
- 12 Spawner Spider Driver in den vier Ecken y 1..3.
- Entity „Robot Spider“ bei (10,1,10).

Red Ant Hangout (:7086-7110):
- 16×17×16: y −1 `stone`, y 0 `gravel` mit `MyRedAntBlock` in den vier 3×3-Ecken.
- Entity „Robot Red Ant“ bei (8,1,8).

#### Spawner- und Entity-Namen → Registry-IDs

Legacy-Name → `orespawn:<id>` aus manifest `entities[].name`. `Blaze` und `Silverfish` sind Vanilla
(`minecraft:blaze`, `minecraft:silverfish`); `EntityEnderCrystal` wird `minecraft:end_crystal`.

| Name | ID | Name | ID | Name | ID |
|---|---|---|---|---|---|
| Alien | `alien` | Alosaurus | `alosaurus` | Attack Squid | `attack_squid` |
| Basilisk | `basilisk` | Bee | `bee` | Boyfriend | `boyfriend` |
| Brutalfly | `brutalfly` | Butterfly | `butterfly` | CaterKiller | `cater_killer` |
| CaveFisher | `cave_fisher` | Cloud Shark | `cloud_shark` | Criminal | `criminal` (Klasse `BandP`) |
| Cryolophosaurus | `cryolophosaurus` | Crystal Urchin | `crystal_urchin` | Dungeon Beast | `dungeon_beast` |
| Emperor Scorpion | `emperor_scorpion` | Ender Knight | `ender_knight` | Ender Reaper | `ender_reaper` |
| Frog | `frog` | Ghost | `ghost` | Ghost Pumpkin Skelly | `ghost_pumpkin_skelly` |
| Girlfriend | `girlfriend` | Gold Fish | `gold_fish` | Hammerhead | `hammerhead` |
| Hercules Beetle | `hercules_beetle` | Jumpy Bug | `jumpy_bug` (Klasse `TrooperBug`) | Kyuubi | `kyuubi` |
| Large Worm | `large_worm` | Leaf Monster | `leaf_monster` | Leonopteryx | `leonopteryx` |
| Lurking Terror | `lurking_terror` | Mantis | `mantis` | Molenoid | `molenoid` |
| Mothra | `mothra` | Nastysaurus | `nastysaurus` | Nightmare | `nightmare` (Klasse `PitchBlack`) |
| Rat | `rat` | Robo-Pounder | `robo_pounder` | Robo-Sniper | `robo_sniper` |
| Robo-Warrior | `robo_warrior` | Robot Red Ant | `robot_red_ant` | Robot Spider | `robot_spider` |
| Rotator | `rotator` | Rubber Ducky | `rubber_ducky` | Scorpion | `scorpion` |
| Sea Monster | `sea_monster` | Sea Viper | `sea_viper` | Spider Driver | `spider_driver` |
| Spit Bug | `spit_bug` | Stink Bug | `stink_bug` | Stinky | `stinky` |
| T. Rex | `t_rex` | Terrible Terror | `terrible_terror` | Triffid | `triffid` |
| Vortex | `vortex` | WTF? | `wtf` (Klasse `GammaMetroid`) | Water Dragon | `water_dragon` |

#### Loot-Tabellen

Semantik von 1.7.10 `generateChestContents`: n Ziehungen, je Ziehung ein Eintrag nach Gewicht,
Stapelgröße gleichverteilt min..max, in einen zufälligen Slot. Vanilla-Verhalten, im Repo nicht
nachgelesen, siehe „Offen“. Vanilla-Items auf 1.21.1-Namen gemappt (z. B. `dye@0` → `ink_sac`,
`fish@0` → `cod`, `spawn_egg@61` → `blaze_spawn_egg`); OreSpawn-Felder über manifest `field`.

**RainbowContentsList** (GenericDungeon.java:58) - 6 Einträge, Gewichtssumme 150

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MagicApple | `orespawn:magicapple` | 1-1 | 25 | 16.7 % |
| CloudSharkEgg | `orespawn:eggcloudshark` | 4-10 | 25 | 16.7 % |
| Items.bone | `minecraft:bone` | 2-16 | 25 | 16.7 % |
| Items.string | `minecraft:string` | 2-16 | 25 | 16.7 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 3-10 | 25 | 16.7 % |
| Items.experience_bottle | `minecraft:experience_bottle` | 4-10 | 25 | 16.7 % |

**WhiteHouseContentsList** (GenericDungeon.java:59) - 11 Einträge, Gewichtssumme 325

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyCornDog | `orespawn:corndog_cooked` | 6-12 | 35 | 10.8 % |
| UraniumNugget | `orespawn:uranium_nugget` | 2-6 | 10 | 3.1 % |
| TitaniumNugget | `orespawn:titanium_nugget` | 2-6 | 10 | 3.1 % |
| MyAmethyst | `orespawn:amethyst` | 2-6 | 35 | 10.8 % |
| MyRuby | `orespawn:ruby` | 2-6 | 25 | 7.7 % |
| CriminalEgg | `orespawn:eggcriminal` | 4-10 | 35 | 10.8 % |
| Items.emerald | `minecraft:emerald` | 6-16 | 35 | 10.8 % |
| Items.porkchop | `minecraft:porkchop` | 6-16 | 35 | 10.8 % |
| Items.cooked_porkchop | `minecraft:cooked_porkchop` | 6-16 | 35 | 10.8 % |
| Items.diamond | `minecraft:diamond` | 6-16 | 35 | 10.8 % |
| Items.gold_ingot | `minecraft:gold_ingot` | 6-16 | 35 | 10.8 % |

**RubberDuckyContentsList** (GenericDungeon.java:60) - 13 Einträge, Gewichtssumme 455

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyDeadStinkBug | `orespawn:deadstinkbug` | 4-10 | 35 | 7.7 % |
| MyFireFish | `orespawn:firefish` | 4-10 | 35 | 7.7 % |
| MySunFish | `orespawn:sunfish` | 4-10 | 35 | 7.7 % |
| MySparkFish | `orespawn:sparkfish` | 4-10 | 35 | 7.7 % |
| MyGreenFish | `orespawn:greenfish` | 4-10 | 35 | 7.7 % |
| MyBlueFish | `orespawn:bluefish` | 4-10 | 35 | 7.7 % |
| MyPinkFish | `orespawn:pinkfish` | 4-10 | 35 | 7.7 % |
| MyRockFish | `orespawn:rockfish` | 4-10 | 35 | 7.7 % |
| MyWoodFish | `orespawn:woodfish` | 4-10 | 35 | 7.7 % |
| MyGreyFish | `orespawn:greyfish` | 4-10 | 35 | 7.7 % |
| RubberDuckyEgg | `orespawn:eggrubberducky` | 4-10 | 35 | 7.7 % |
| MyPeacockFeather | `orespawn:peacockfeather` | 4-10 | 35 | 7.7 % |
| Items.feather | `minecraft:feather` | 6-16 | 35 | 7.7 % |

**StinkyHouseContentsList** (GenericDungeon.java:61) - 7 Einträge, Gewichtssumme 215

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyDeadStinkBug | `orespawn:deadstinkbug` | 4-10 | 35 | 16.3 % |
| StinkyEgg | `orespawn:eggstinky` | 4-10 | 35 | 16.3 % |
| StinkBugEgg | `orespawn:eggstink` | 4-10 | 35 | 16.3 % |
| Items.bone | `minecraft:bone` | 6-16 | 25 | 11.6 % |
| Items.coal | `minecraft:coal` | 6-16 | 25 | 11.6 % |
| Items.string | `minecraft:string` | 6-16 | 25 | 11.6 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 3-10 | 35 | 16.3 % |

**NightmareRookeryContentsList** (GenericDungeon.java:62) - 10 Einträge, Gewichtssumme 270

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyDeadStinkBug | `orespawn:deadstinkbug` | 4-10 | 35 | 13.0 % |
| MyFlowerBlackBlock | `orespawn:flower_black` | 4-10 | 35 | 13.0 % |
| MyFlowerScaryBlock | `orespawn:flower_scary` | 4-10 | 35 | 13.0 % |
| PitchBlackEgg | `orespawn:eggnightmare` | 4-10 | 25 | 9.3 % |
| AntRobotKit | `orespawn:antrobotkit` | 1-1 | 10 | 3.7 % |
| SpiderRobotKit | `orespawn:spiderrobotkit` | 1-1 | 10 | 3.7 % |
| Items.bone | `minecraft:bone` | 6-16 | 25 | 9.3 % |
| Items.string | `minecraft:string` | 6-16 | 25 | 9.3 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 3-10 | 35 | 13.0 % |
| Items.experience_bottle | `minecraft:experience_bottle` | 4-10 | 35 | 13.0 % |

**MonsterIslandContentsList** (GenericDungeon.java:63) - 14 Einträge, Gewichtssumme 450

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| CreeperRepellent | `orespawn:creeperrepellent` | 4-10 | 35 | 7.8 % |
| KrakenRepellent | `orespawn:krakenrepellent` | 4-10 | 35 | 7.8 % |
| Items.dye | `minecraft:ink_sac` | 6-16 | 25 | 5.6 % |
| Items.bone | `minecraft:bone` | 6-16 | 25 | 5.6 % |
| Items.string | `minecraft:string` | 6-16 | 25 | 5.6 % |
| Items.porkchop | `minecraft:porkchop` | 3-10 | 35 | 7.8 % |
| Items.beef | `minecraft:beef` | 3-10 | 35 | 7.8 % |
| Items.chicken | `minecraft:chicken` | 3-10 | 35 | 7.8 % |
| Items.fish | `minecraft:cod` | 3-10 | 35 | 7.8 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 3-10 | 35 | 7.8 % |
| Items.experience_bottle | `minecraft:experience_bottle` | 4-10 | 35 | 7.8 % |
| MyRawBacon | `orespawn:bacon` | 6-16 | 35 | 7.8 % |
| MyRawPeacock | `orespawn:rawpeacock` | 6-16 | 35 | 7.8 % |
| Blocks.log | `minecraft:oak_log` | 6-16 | 25 | 5.6 % |

**GreenhouseContentsList** (GenericDungeon.java:64) - 7 Einträge, Gewichtssumme 215

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| GreenGoo | `orespawn:greengoo` | 4-10 | 35 | 16.3 % |
| CreeperRepellent | `orespawn:creeperrepellent` | 4-10 | 35 | 16.3 % |
| Items.flower_pot | `minecraft:flower_pot` | 6-16 | 35 | 16.3 % |
| Blocks.sapling | `minecraft:oak_sapling` | 6-16 | 35 | 16.3 % |
| Blocks.leaves | `minecraft:oak_leaves` | 6-16 | 25 | 11.6 % |
| Blocks.dirt | `minecraft:dirt` | 6-16 | 25 | 11.6 % |
| Blocks.log | `minecraft:oak_log` | 6-16 | 25 | 11.6 % |

**CrystalBattleTowerRatContentsList** (GenericDungeon.java:65) - 7 Einträge, Gewichtssumme 245

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.cooked_porkchop | `minecraft:cooked_porkchop` | 3-10 | 35 | 14.3 % |
| Items.beef | `minecraft:beef` | 3-10 | 35 | 14.3 % |
| Items.cooked_chicken | `minecraft:cooked_chicken` | 3-10 | 35 | 14.3 % |
| Items.cooked_fished | `minecraft:cooked_cod` | 3-10 | 35 | 14.3 % |
| MyBLT | `orespawn:blt_sandwich` | 4-10 | 35 | 14.3 % |
| MySalad | `orespawn:salad` | 4-10 | 35 | 14.3 % |
| MyCornDog | `orespawn:corndog_cooked` | 4-10 | 35 | 14.3 % |

**CrystalBattleTowerDungeonBeastContentsList** (GenericDungeon.java:66) - 4 Einträge, Gewichtssumme 90

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.dye | `minecraft:ink_sac` | 6-16 | 25 | 27.8 % |
| MySquidZooka | `orespawn:squidzookasmall` | 1-1 | 25 | 27.8 % |
| Items.gold_nugget | `minecraft:gold_nugget` | 5-15 | 15 | 16.7 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 27.8 % |

**CrystalBattleTowerUrchinContentsList** (GenericDungeon.java:67) - 5 Einträge, Gewichtssumme 55

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| CrystalPinkHelmet | `orespawn:pink_helmet` | 1-1 | 10 | 18.2 % |
| CrystalPinkBody | `orespawn:pink_chest` | 1-1 | 10 | 18.2 % |
| CrystalPinkLegs | `orespawn:pink_leggings` | 1-1 | 10 | 18.2 % |
| CrystalPinkBoots | `orespawn:pink_boots` | 1-1 | 10 | 18.2 % |
| MyFairySword | `orespawn:fairysword` | 1-1 | 15 | 27.3 % |

**CrystalBattleTowerRotatorContentsList** (GenericDungeon.java:68) - 5 Einträge, Gewichtssumme 55

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| TigersEyeHelmet | `orespawn:tigerseye_helmet` | 1-1 | 10 | 18.2 % |
| TigersEyeBody | `orespawn:tigerseye_chest` | 1-1 | 10 | 18.2 % |
| TigersEyeLegs | `orespawn:tigerseye_leggings` | 1-1 | 10 | 18.2 % |
| TigersEyeBoots | `orespawn:tigerseye_boots` | 1-1 | 10 | 18.2 % |
| MyRatSword | `orespawn:ratsword` | 1-1 | 15 | 27.3 % |

**CrystalBattleTowerVortexContentsList** (GenericDungeon.java:69) - 5 Einträge, Gewichtssumme 60

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| CrystalCoal | `orespawn:crystalcoal` | 6-10 | 10 | 16.7 % |
| CrystalCoal | `orespawn:crystalcoal` | 6-10 | 10 | 16.7 % |
| MyTigersEyeSword | `orespawn:tigerseye_sword` | 1-1 | 10 | 16.7 % |
| MyTigersEyeBlock | `orespawn:tigerseye_block` | 4-8 | 15 | 25.0 % |
| MyPoisonSword | `orespawn:poisonsword` | 1-1 | 15 | 25.0 % |

**RobotContentsList** (GenericDungeon.java:70) - 23 Einträge, Gewichtssumme 755

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.redstone | `minecraft:redstone` | 1-10 | 35 | 4.6 % |
| Items.repeater | `minecraft:repeater` | 1-10 | 35 | 4.6 % |
| Items.minecart | `minecraft:minecart` | 1-1 | 35 | 4.6 % |
| Items.fire_charge | `minecraft:fire_charge` | 1-10 | 35 | 4.6 % |
| Items.hopper_minecart | `minecraft:hopper_minecart` | 1-1 | 35 | 4.6 % |
| Blocks.redstone_block | `minecraft:redstone_block` | 1-10 | 35 | 4.6 % |
| Blocks.rail | `minecraft:rail` | 1-10 | 35 | 4.6 % |
| Blocks.detector_rail | `minecraft:detector_rail` | 1-10 | 35 | 4.6 % |
| Blocks.sticky_piston | `minecraft:sticky_piston` | 1-10 | 35 | 4.6 % |
| Blocks.piston | `minecraft:piston` | 1-10 | 35 | 4.6 % |
| Blocks.redstone_torch | `minecraft:redstone_torch` | 1-10 | 35 | 4.6 % |
| Blocks.tnt | `minecraft:tnt` | 1-10 | 35 | 4.6 % |
| Blocks.rail | `minecraft:rail` | 1-10 | 35 | 4.6 % |
| Blocks.lever | `minecraft:lever` | 1-10 | 35 | 4.6 % |
| AntRobotKit | `orespawn:antrobotkit` | 1-1 | 10 | 1.3 % |
| SpiderRobotKit | `orespawn:spiderrobotkit` | 1-1 | 10 | 1.3 % |
| Items.iron_door | `minecraft:iron_door` | 1-10 | 35 | 4.6 % |
| Blocks.redstone_torch | `minecraft:redstone_torch` | 1-10 | 35 | 4.6 % |
| Blocks.wooden_button | `minecraft:oak_button` | 1-10 | 35 | 4.6 % |
| Blocks.iron_bars | `minecraft:iron_bars` | 1-10 | 35 | 4.6 % |
| Items.comparator | `minecraft:comparator` | 1-10 | 35 | 4.6 % |
| Blocks.activator_rail | `minecraft:activator_rail` | 1-10 | 35 | 4.6 % |
| MyRayGun | `orespawn:raygun` | 1-1 | 35 | 4.6 % |

**IncaPyramidContentsList** (GenericDungeon.java:71) - 14 Einträge, Gewichtssumme 480

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.golden_sword | `minecraft:golden_sword` | 1-1 | 35 | 7.3 % |
| Items.golden_boots | `minecraft:golden_boots` | 1-1 | 35 | 7.3 % |
| Items.golden_leggings | `minecraft:golden_leggings` | 1-1 | 35 | 7.3 % |
| Items.golden_helmet | `minecraft:golden_helmet` | 1-1 | 35 | 7.3 % |
| Items.golden_chestplate | `minecraft:golden_chestplate` | 1-1 | 35 | 7.3 % |
| Blocks.yellow_flower | `minecraft:dandelion` | 3-10 | 35 | 7.3 % |
| Blocks.red_flower | `minecraft:poppy` | 3-10 | 35 | 7.3 % |
| Items.gold_nugget | `minecraft:gold_nugget` | 3-10 | 35 | 7.3 % |
| Items.gold_ingot | `minecraft:gold_ingot` | 3-10 | 35 | 7.3 % |
| Items.experience_bottle | `minecraft:experience_bottle` | 4-10 | 35 | 7.3 % |
| MyCornCob | `orespawn:corn_seed` | 4-10 | 35 | 7.3 % |
| MyExperienceCatcher | `orespawn:experiencecatcher` | 4-10 | 25 | 5.2 % |
| Items.bone | `minecraft:bone` | 4-10 | 35 | 7.3 % |
| Blocks.gold_block | `minecraft:gold_block` | 4-10 | 35 | 7.3 % |

**DamselContentsList** (GenericDungeon.java:72) - 9 Einträge, Gewichtssumme 315

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.iron_pickaxe | `minecraft:iron_pickaxe` | 1-1 | 35 | 11.1 % |
| Items.iron_sword | `minecraft:iron_sword` | 1-1 | 35 | 11.1 % |
| Items.cooked_porkchop | `minecraft:cooked_porkchop` | 3-10 | 35 | 11.1 % |
| Items.beef | `minecraft:beef` | 3-10 | 35 | 11.1 % |
| Items.cooked_chicken | `minecraft:cooked_chicken` | 3-10 | 35 | 11.1 % |
| Items.cooked_fished | `minecraft:cooked_cod` | 3-10 | 35 | 11.1 % |
| MyBLT | `orespawn:blt_sandwich` | 4-10 | 35 | 11.1 % |
| MySalad | `orespawn:salad` | 4-10 | 35 | 11.1 % |
| MyCornDog | `orespawn:corndog_cooked` | 4-10 | 35 | 11.1 % |

**EnderCastleContentsList** (GenericDungeon.java:73) - 8 Einträge, Gewichtssumme 270

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Blocks.ender_chest | `minecraft:ender_chest` | 2-4 | 35 | 13.0 % |
| Blocks.diamond_block | `minecraft:diamond_block` | 2-4 | 35 | 13.0 % |
| Blocks.dragon_egg | `minecraft:dragon_egg` | 1-1 | 35 | 13.0 % |
| MyEnderPearlBlock | `orespawn:blockenderpearl` | 3-6 | 35 | 13.0 % |
| MyEyeOfEnderBlock | `orespawn:blockeyeofender` | 3-6 | 35 | 13.0 % |
| MyExperienceCatcher | `orespawn:experiencecatcher` | 4-10 | 25 | 9.3 % |
| Items.ender_pearl | `minecraft:ender_pearl` | 2-4 | 35 | 13.0 % |
| Items.ender_eye | `minecraft:ender_eye` | 2-4 | 35 | 13.0 % |

**BouncyContentsList** (GenericDungeon.java:74) - 7 Einträge, Gewichtssumme 180

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 35 | 19.4 % |
| Items.fish | `minecraft:cod` | 6-16 | 25 | 13.9 % |
| Items.bone | `minecraft:bone` | 6-16 | 25 | 13.9 % |
| Items.string | `minecraft:string` | 6-16 | 25 | 13.9 % |
| Blocks.red_flower | `minecraft:poppy` | 6-16 | 25 | 13.9 % |
| Blocks.yellow_flower | `minecraft:dandelion` | 6-16 | 25 | 13.9 % |
| Items.ender_pearl | `minecraft:ender_pearl` | 2-4 | 20 | 11.1 % |

**SpitBugContentsList** (GenericDungeon.java:75) - 15 Einträge, Gewichtssumme 295

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 35 | 11.9 % |
| Items.fish | `minecraft:cod` | 6-16 | 25 | 8.5 % |
| Items.bone | `minecraft:bone` | 6-16 | 25 | 8.5 % |
| Items.string | `minecraft:string` | 6-16 | 25 | 8.5 % |
| MyAmethystPickaxe | `orespawn:amethystpickaxe` | 1-1 | 15 | 5.1 % |
| MyAmethystShovel | `orespawn:amethystshovel` | 1-1 | 15 | 5.1 % |
| MyAmethystHoe | `orespawn:amethysthoe` | 1-1 | 15 | 5.1 % |
| MyAmethystAxe | `orespawn:amethystaxe` | 1-1 | 15 | 5.1 % |
| MyAmethystSword | `orespawn:amethystsword` | 1-1 | 15 | 5.1 % |
| AmethystBody | `orespawn:amethyst_chest` | 1-1 | 15 | 5.1 % |
| AmethystLegs | `orespawn:amethyst_leggings` | 1-1 | 15 | 5.1 % |
| AmethystHelmet | `orespawn:amethyst_helmet` | 1-1 | 15 | 5.1 % |
| AmethystBoots | `orespawn:amethyst_boots` | 1-1 | 15 | 5.1 % |
| InstantGarden | `orespawn:instantgarden` | 2-4 | 25 | 8.5 % |
| InstantShelter | `orespawn:instantshelter` | 2-4 | 25 | 8.5 % |

**GraveContentsList** (GenericDungeon.java:76) - 4 Einträge, Gewichtssumme 140

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.ender_eye | `minecraft:ender_eye` | 6-16 | 35 | 25.0 % |
| Blocks.red_flower | `minecraft:poppy` | 6-16 | 35 | 25.0 % |
| Blocks.yellow_flower | `minecraft:dandelion` | 6-16 | 35 | 25.0 % |
| Items.ender_pearl | `minecraft:ender_pearl` | 6-16 | 35 | 25.0 % |

**HospitalContentsList** (GenericDungeon.java:77) - 6 Einträge, Gewichtssumme 210

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Blocks.ender_chest | `minecraft:ender_chest` | 2-4 | 35 | 16.7 % |
| Blocks.diamond_block | `minecraft:diamond_block` | 2-4 | 35 | 16.7 % |
| Blocks.dragon_egg | `minecraft:dragon_egg` | 1-1 | 35 | 16.7 % |
| MyEnderPearlBlock | `orespawn:blockenderpearl` | 3-6 | 35 | 16.7 % |
| Items.ender_pearl | `minecraft:ender_pearl` | 2-4 | 35 | 16.7 % |
| Items.ender_eye | `minecraft:ender_eye` | 2-4 | 35 | 16.7 % |

**MiniContentsList** (GenericDungeon.java:78) - 6 Einträge, Gewichtssumme 190

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.golden_apple | `minecraft:golden_apple` | 6-16 | 35 | 18.4 % |
| MyCrystalApple | `orespawn:crystalapple` | 6-16 | 35 | 18.4 % |
| MyBacon | `orespawn:cookedbacon` | 6-16 | 35 | 18.4 % |
| MyFireFish | `orespawn:firefish` | 6-16 | 35 | 18.4 % |
| InstantGarden | `orespawn:instantgarden` | 2-4 | 25 | 13.2 % |
| InstantShelter | `orespawn:instantshelter` | 2-4 | 25 | 13.2 % |

**LeafMonsterContentsList** (GenericDungeon.java:79) - 9 Einträge, Gewichtssumme 255

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.flower_pot | `minecraft:flower_pot` | 6-16 | 35 | 13.7 % |
| Blocks.sapling | `minecraft:oak_sapling` | 6-16 | 35 | 13.7 % |
| Items.flower_pot | `minecraft:flower_pot` | 6-16 | 35 | 13.7 % |
| Blocks.sapling | `minecraft:oak_sapling` | 6-16 | 35 | 13.7 % |
| Blocks.leaves | `minecraft:oak_leaves` | 6-16 | 25 | 9.8 % |
| Blocks.dirt | `minecraft:dirt` | 6-16 | 25 | 9.8 % |
| Blocks.log | `minecraft:oak_log` | 6-16 | 25 | 9.8 % |
| MyPoisonSword | `orespawn:poisonsword` | 1-1 | 15 | 5.9 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 9.8 % |

**CloudSharkContentsList** (GenericDungeon.java:80) - 6 Einträge, Gewichtssumme 140

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.fish | `minecraft:cod` | 6-16 | 25 | 17.9 % |
| Items.bone | `minecraft:bone` | 6-16 | 25 | 17.9 % |
| Items.string | `minecraft:string` | 6-16 | 25 | 17.9 % |
| Items.paper | `minecraft:paper` | 6-16 | 25 | 17.9 % |
| MyExperienceTreeSeed | `orespawn:experiencetree_seed` | 1-2 | 15 | 10.7 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 17.9 % |

**WaterDragonContentsList** (GenericDungeon.java:81) - 7 Einträge, Gewichtssumme 145

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.fish | `minecraft:cod` | 6-16 | 25 | 17.2 % |
| MyUltimateAxe | `orespawn:ultimateaxe` | 1-1 | 15 | 10.3 % |
| MyUltimatePickaxe | `orespawn:ultimatepickaxe` | 1-1 | 15 | 10.3 % |
| MyUltimateShovel | `orespawn:ultimateshovel` | 1-1 | 15 | 10.3 % |
| MyExperienceCatcher | `orespawn:experiencecatcher` | 4-10 | 25 | 17.2 % |
| Blocks.iron_block | `minecraft:iron_block` | 6-16 | 25 | 17.2 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 17.2 % |

**SquidContentsList** (GenericDungeon.java:82) - 4 Einträge, Gewichtssumme 80

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.dye | `minecraft:ink_sac` | 6-16 | 25 | 31.2 % |
| MySquidZooka | `orespawn:squidzookasmall` | 1-1 | 15 | 18.8 % |
| Items.gold_nugget | `minecraft:gold_nugget` | 5-15 | 15 | 18.8 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 31.2 % |

**KnightContentsList** (GenericDungeon.java:83) - 5 Einträge, Gewichtssumme 95

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.paper | `minecraft:paper` | 2-8 | 20 | 21.1 % |
| Blocks.planks | `minecraft:oak_planks` | 4-8 | 20 | 21.1 % |
| Items.ender_eye | `minecraft:ender_eye` | 2-8 | 15 | 15.8 % |
| Items.ender_pearl | `minecraft:ender_pearl` | 2-8 | 15 | 15.8 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 26.3 % |

**AlienWTFContentsList** (GenericDungeon.java:84) - 18 Einträge, Gewichtssumme 255

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Blocks.diamond_block | `minecraft:diamond_block` | 1-2 | 15 | 5.9 % |
| MyRuby | `orespawn:ruby` | 1-1 | 20 | 7.8 % |
| MyAmethyst | `orespawn:amethyst` | 1-1 | 20 | 7.8 % |
| MyIngotUranium | `orespawn:ingoturanium` | 1-2 | 5 | 2.0 % |
| MyIngotTitanium | `orespawn:ingottitanium` | 1-2 | 5 | 2.0 % |
| UltimateHelmet | `orespawn:ultimate_helmet` | 1-1 | 10 | 3.9 % |
| UltimateBody | `orespawn:ultimate_chest` | 1-1 | 10 | 3.9 % |
| UltimateLegs | `orespawn:ultimate_leggings` | 1-1 | 10 | 3.9 % |
| UltimateBoots | `orespawn:ultimate_boots` | 1-1 | 10 | 3.9 % |
| MyUltimateBow | `orespawn:ultimatebow` | 1-1 | 15 | 5.9 % |
| MyNightmareSword | `orespawn:nightmaresword` | 1-1 | 15 | 5.9 % |
| MyExperienceCatcher | `orespawn:experiencecatcher` | 4-10 | 15 | 5.9 % |
| MyRayGun | `orespawn:raygun` | 1-1 | 10 | 3.9 % |
| CageEmpty | `orespawn:cageempty` | 1-10 | 20 | 7.8 % |
| MyCornDog | `orespawn:corndog_cooked` | 1-10 | 20 | 7.8 % |
| MyBacon | `orespawn:cookedbacon` | 1-5 | 20 | 7.8 % |
| MyPopcornBag | `orespawn:popcorn_bag` | 2-8 | 20 | 7.8 % |
| MyFireFish | `orespawn:firefish` | 2-8 | 15 | 5.9 % |

**shadowContentsList** (GenericDungeon.java:85) - 22 Einträge, Gewichtssumme 320

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.glowstone_dust | `minecraft:glowstone_dust` | 2-8 | 20 | 6.2 % |
| Items.nether_wart | `minecraft:nether_wart` | 4-8 | 20 | 6.2 % |
| Items.blaze_rod | `minecraft:blaze_rod` | 2-8 | 15 | 4.7 % |
| Items.blaze_powder | `minecraft:blaze_powder` | 2-8 | 15 | 4.7 % |
| Items.fire_charge | `minecraft:fire_charge` | 4-8 | 15 | 4.7 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 7.8 % |
| Items.dye | `minecraft:ink_sac` | 6-16 | 25 | 7.8 % |
| MyRuby | `orespawn:ruby` | 2-8 | 15 | 4.7 % |
| MyExperienceTreeSeed | `orespawn:experiencetree_seed` | 2-4 | 15 | 4.7 % |
| MyElevator | `orespawn:elevator` | 1-1 | 15 | 4.7 % |
| MyNightmareSword | `orespawn:nightmaresword` | 1-1 | 15 | 4.7 % |
| MyPoisonSword | `orespawn:poisonsword` | 1-1 | 15 | 4.7 % |
| MyRatSword | `orespawn:ratsword` | 1-1 | 10 | 3.1 % |
| MyRubySword | `orespawn:rubysword` | 1-1 | 10 | 3.1 % |
| MyBigHammer | `orespawn:bighammer` | 1-1 | 15 | 4.7 % |
| MySquidZooka | `orespawn:squidzookasmall` | 1-1 | 15 | 4.7 % |
| MyIngotTitanium | `orespawn:ingottitanium` | 1-1 | 5 | 1.6 % |
| MyIngotUranium | `orespawn:ingoturanium` | 1-1 | 5 | 1.6 % |
| MyUltimateSword | `orespawn:ultimatesword` | 1-1 | 10 | 3.1 % |
| MyUltimateBow | `orespawn:ultimatebow` | 1-1 | 10 | 3.1 % |
| EnderReaperEgg | `orespawn:eggenderreaper` | 2-8 | 15 | 4.7 % |
| PitchBlackEgg | `orespawn:eggnightmare` | 2-8 | 15 | 4.7 % |

**kyuubiContentsList** (GenericDungeon.java:86) - 7 Einträge, Gewichtssumme 110

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.redstone | `minecraft:redstone` | 2-8 | 10 | 9.1 % |
| Blocks.redstone_block | `minecraft:redstone_block` | 4-8 | 15 | 13.6 % |
| Items.quartz | `minecraft:quartz` | 2-8 | 15 | 13.6 % |
| Items.coal | `minecraft:coal` | 2-8 | 15 | 13.6 % |
| MyNightmareSword | `orespawn:nightmaresword` | 1-1 | 20 | 18.2 % |
| MyPoisonSword | `orespawn:poisonsword` | 1-1 | 20 | 18.2 % |
| KyuubiEgg | `orespawn:eggkyuubi` | 2-8 | 15 | 13.6 % |

**blazeContentsList** (GenericDungeon.java:87) - 9 Einträge, Gewichtssumme 130

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.blaze_rod | `minecraft:blaze_rod` | 2-8 | 15 | 11.5 % |
| Items.blaze_powder | `minecraft:blaze_powder` | 2-8 | 15 | 11.5 % |
| Items.fire_charge | `minecraft:fire_charge` | 4-8 | 15 | 11.5 % |
| Items.flint_and_steel | `minecraft:flint_and_steel` | 1-1 | 10 | 7.7 % |
| LavaEelHelmet | `orespawn:lavaeel_helmet` | 1-1 | 15 | 11.5 % |
| LavaEelBody | `orespawn:lavaeel_chest` | 1-1 | 15 | 11.5 % |
| LavaEelLegs | `orespawn:lavaeel_leggings` | 1-1 | 15 | 11.5 % |
| LavaEelBoots | `orespawn:lavaeel_boots` | 1-1 | 15 | 11.5 % |
| Items.spawn_egg@61 | `minecraft:blaze_spawn_egg` | 2-8 | 15 | 11.5 % |

**beeContentsList** (GenericDungeon.java:88) - 12 Einträge, Gewichtssumme 150

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.sugar | `minecraft:sugar` | 2-8 | 15 | 10.0 % |
| Blocks.yellow_flower | `minecraft:dandelion` | 4-8 | 15 | 10.0 % |
| Items.gold_nugget | `minecraft:gold_nugget` | 5-15 | 15 | 10.0 % |
| Items.paper | `minecraft:paper` | 2-8 | 15 | 10.0 % |
| MyFairySword | `orespawn:fairysword` | 1-1 | 10 | 6.7 % |
| CrystalPinkHelmet | `orespawn:pink_helmet` | 1-1 | 10 | 6.7 % |
| CrystalPinkBody | `orespawn:pink_chest` | 1-1 | 10 | 6.7 % |
| CrystalPinkLegs | `orespawn:pink_leggings` | 1-1 | 10 | 6.7 % |
| CrystalPinkBoots | `orespawn:pink_boots` | 1-1 | 10 | 6.7 % |
| MyButterCandy | `orespawn:buttercandy` | 2-8 | 15 | 10.0 % |
| MyExperienceCatcher | `orespawn:experiencecatcher` | 4-10 | 10 | 6.7 % |
| BeeEgg | `orespawn:eggbee` | 2-8 | 15 | 10.0 % |

**mantisContentsList** (GenericDungeon.java:89) - 11 Einträge, Gewichtssumme 135

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyMantisClaw | `orespawn:mantisclaw` | 1-1 | 10 | 7.4 % |
| Items.gold_nugget | `minecraft:gold_nugget` | 4-8 | 15 | 11.1 % |
| UraniumNugget | `orespawn:uranium_nugget` | 1-3 | 5 | 3.7 % |
| TitaniumNugget | `orespawn:titanium_nugget` | 1-3 | 5 | 3.7 % |
| MantisEgg | `orespawn:eggmantis` | 2-4 | 20 | 14.8 % |
| TigersEyeHelmet | `orespawn:tigerseye_helmet` | 1-1 | 10 | 7.4 % |
| TigersEyeBody | `orespawn:tigerseye_chest` | 1-1 | 10 | 7.4 % |
| TigersEyeLegs | `orespawn:tigerseye_leggings` | 1-1 | 10 | 7.4 % |
| TigersEyeBoots | `orespawn:tigerseye_boots` | 1-1 | 10 | 7.4 % |
| Items.rotten_flesh | `minecraft:rotten_flesh` | 6-16 | 25 | 18.5 % |
| Items.diamond | `minecraft:diamond` | 1-3 | 15 | 11.1 % |

**level1ContentsList** (GenericDungeon.java:90) - 11 Einträge, Gewichtssumme 165

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.emerald | `minecraft:emerald` | 2-8 | 15 | 9.1 % |
| MinersDream | `orespawn:minersdream` | 4-8 | 15 | 9.1 % |
| MyEmeraldPickaxe | `orespawn:emeraldpickaxe` | 1-1 | 15 | 9.1 % |
| MyEmeraldShovel | `orespawn:emeraldshovel` | 1-1 | 15 | 9.1 % |
| MyEmeraldHoe | `orespawn:emeraldhoe` | 1-1 | 15 | 9.1 % |
| MyEmeraldAxe | `orespawn:emeraldaxe` | 1-1 | 15 | 9.1 % |
| MyEmeraldSword | `orespawn:emeraldsword` | 1-1 | 15 | 9.1 % |
| EmeraldBody | `orespawn:emerald_chest` | 1-1 | 15 | 9.1 % |
| EmeraldLegs | `orespawn:emerald_leggings` | 1-1 | 15 | 9.1 % |
| EmeraldHelmet | `orespawn:emerald_helmet` | 1-1 | 15 | 9.1 % |
| EmeraldBoots | `orespawn:emerald_boots` | 1-1 | 15 | 9.1 % |

**level2ContentsList** (GenericDungeon.java:91) - 17 Einträge, Gewichtssumme 235

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| Items.experience_bottle | `minecraft:experience_bottle` | 2-8 | 15 | 6.4 % |
| Items.experience_bottle | `minecraft:experience_bottle` | 2-8 | 15 | 6.4 % |
| CreeperLauncher | `orespawn:creeperlauncher` | 2-10 | 15 | 6.4 % |
| CrystalPinkHelmet | `orespawn:pink_helmet` | 1-1 | 10 | 4.3 % |
| CrystalPinkBody | `orespawn:pink_chest` | 1-1 | 10 | 4.3 % |
| CrystalPinkLegs | `orespawn:pink_leggings` | 1-1 | 10 | 4.3 % |
| CrystalPinkBoots | `orespawn:pink_boots` | 1-1 | 10 | 4.3 % |
| MyFairySword | `orespawn:fairysword` | 1-1 | 15 | 6.4 % |
| MyEmeraldPickaxe | `orespawn:emeraldpickaxe` | 1-1 | 15 | 6.4 % |
| MyEmeraldShovel | `orespawn:emeraldshovel` | 1-1 | 15 | 6.4 % |
| MyEmeraldHoe | `orespawn:emeraldhoe` | 1-1 | 15 | 6.4 % |
| MyEmeraldAxe | `orespawn:emeraldaxe` | 1-1 | 15 | 6.4 % |
| MyEmeraldSword | `orespawn:emeraldsword` | 1-1 | 15 | 6.4 % |
| ExperienceBody | `orespawn:experience_chest` | 1-1 | 15 | 6.4 % |
| ExperienceLegs | `orespawn:experience_leggings` | 1-1 | 15 | 6.4 % |
| ExperienceHelmet | `orespawn:experience_helmet` | 1-1 | 15 | 6.4 % |
| ExperienceBoots | `orespawn:experience_boots` | 1-1 | 15 | 6.4 % |

**level3ContentsList** (GenericDungeon.java:92) - 17 Einträge, Gewichtssumme 235

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MySquidZooka | `orespawn:squidzookasmall` | 1-1 | 15 | 6.4 % |
| MyRatSword | `orespawn:ratsword` | 1-1 | 15 | 6.4 % |
| MyAmethyst | `orespawn:amethyst` | 2-8 | 15 | 6.4 % |
| Items.dye | `minecraft:ink_sac` | 2-8 | 15 | 6.4 % |
| TigersEyeHelmet | `orespawn:tigerseye_helmet` | 1-1 | 10 | 4.3 % |
| TigersEyeBody | `orespawn:tigerseye_chest` | 1-1 | 10 | 4.3 % |
| TigersEyeLegs | `orespawn:tigerseye_leggings` | 1-1 | 10 | 4.3 % |
| TigersEyeBoots | `orespawn:tigerseye_boots` | 1-1 | 10 | 4.3 % |
| MyAmethystPickaxe | `orespawn:amethystpickaxe` | 1-1 | 15 | 6.4 % |
| MyAmethystShovel | `orespawn:amethystshovel` | 1-1 | 15 | 6.4 % |
| MyAmethystHoe | `orespawn:amethysthoe` | 1-1 | 15 | 6.4 % |
| MyAmethystAxe | `orespawn:amethystaxe` | 1-1 | 15 | 6.4 % |
| MyAmethystSword | `orespawn:amethystsword` | 1-1 | 15 | 6.4 % |
| AmethystBody | `orespawn:amethyst_chest` | 1-1 | 15 | 6.4 % |
| AmethystLegs | `orespawn:amethyst_leggings` | 1-1 | 15 | 6.4 % |
| AmethystHelmet | `orespawn:amethyst_helmet` | 1-1 | 15 | 6.4 % |
| AmethystBoots | `orespawn:amethyst_boots` | 1-1 | 15 | 6.4 % |

**level4ContentsList** (GenericDungeon.java:93) - 17 Einträge, Gewichtssumme 255

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyRuby | `orespawn:ruby` | 2-8 | 15 | 5.9 % |
| MagicApple | `orespawn:magicapple` | 1-1 | 15 | 5.9 % |
| MyRayGun | `orespawn:raygun` | 1-1 | 15 | 5.9 % |
| CreeperRepellent | `orespawn:creeperrepellent` | 4-10 | 15 | 5.9 % |
| KrakenRepellent | `orespawn:krakenrepellent` | 4-10 | 15 | 5.9 % |
| MyExperienceCatcher | `orespawn:experiencecatcher` | 4-10 | 15 | 5.9 % |
| ZooKeeper | `orespawn:zookeeper` | 10-16 | 15 | 5.9 % |
| MyRubyPickaxe | `orespawn:rubypickaxe` | 1-1 | 15 | 5.9 % |
| MyRubyShovel | `orespawn:rubyshovel` | 1-1 | 15 | 5.9 % |
| MyRubyHoe | `orespawn:rubyhoe` | 1-1 | 15 | 5.9 % |
| MyRubyAxe | `orespawn:rubyaxe` | 1-1 | 15 | 5.9 % |
| MyRubySword | `orespawn:rubysword` | 1-1 | 15 | 5.9 % |
| MyThunderStaff | `orespawn:thunderstaff` | 1-1 | 15 | 5.9 % |
| RubyBody | `orespawn:ruby_chest` | 1-1 | 15 | 5.9 % |
| RubyLegs | `orespawn:ruby_leggings` | 1-1 | 15 | 5.9 % |
| RubyHelmet | `orespawn:ruby_helmet` | 1-1 | 15 | 5.9 % |
| RubyBoots | `orespawn:ruby_boots` | 1-1 | 15 | 5.9 % |

**level5ContentsList** (GenericDungeon.java:94) - 87 Einträge, Gewichtssumme 1285

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyNightmareSword | `orespawn:nightmaresword` | 1-1 | 15 | 1.2 % |
| MyPoisonSword | `orespawn:poisonsword` | 1-1 | 15 | 1.2 % |
| WitherSkeletonEgg | `orespawn:eggwitherskeleton` | 1-4 | 15 | 1.2 % |
| EnderDragonEgg | `orespawn:eggenderdragon` | 1-4 | 15 | 1.2 % |
| SnowGolemEgg | `orespawn:eggsnowgolem` | 1-4 | 15 | 1.2 % |
| IronGolemEgg | `orespawn:eggirongolem` | 1-4 | 15 | 1.2 % |
| WitherBossEgg | `orespawn:eggwitherboss` | 1-4 | 15 | 1.2 % |
| RedCowEgg | `orespawn:eggredcow` | 1-4 | 15 | 1.2 % |
| GoldCowEgg | `orespawn:egggoldcow` | 1-4 | 15 | 1.2 % |
| EnchantedCowEgg | `orespawn:eggenchantedcow` | 1-4 | 15 | 1.2 % |
| MOTHRAEgg | `orespawn:eggmothra` | 1-4 | 15 | 1.2 % |
| AloEgg | `orespawn:eggalosaurus` | 1-4 | 15 | 1.2 % |
| CryoEgg | `orespawn:eggcryolophosaurus` | 1-4 | 15 | 1.2 % |
| CamaEgg | `orespawn:eggcamarasaurus` | 1-4 | 15 | 1.2 % |
| VeloEgg | `orespawn:eggvelocityraptor` | 1-4 | 15 | 1.2 % |
| HydroEgg | `orespawn:egghydrolisc` | 1-4 | 15 | 1.2 % |
| BasilEgg | `orespawn:eggbasilisc` | 1-4 | 15 | 1.2 % |
| DragonflyEgg | `orespawn:eggdragonfly` | 1-4 | 15 | 1.2 % |
| EmperorScorpionEgg | `orespawn:eggemperorscorpion` | 1-4 | 15 | 1.2 % |
| ScorpionEgg | `orespawn:eggscorpion` | 1-4 | 15 | 1.2 % |
| CaveFisherEgg | `orespawn:eggcavefisher` | 1-4 | 15 | 1.2 % |
| SpyroEgg | `orespawn:eggspyro` | 1-4 | 15 | 1.2 % |
| BaryonyxEgg | `orespawn:eggbaryonyx` | 1-4 | 15 | 1.2 % |
| CockateilEgg | `orespawn:eggcockateil` | 1-4 | 15 | 1.2 % |
| GammaMetroidEgg | `orespawn:egggammametroid` | 1-4 | 15 | 1.2 % |
| KyuubiEgg | `orespawn:eggkyuubi` | 1-4 | 15 | 1.2 % |
| AlienEgg | `orespawn:eggalien` | 1-4 | 15 | 1.2 % |
| AttackSquidEgg | `orespawn:eggattacksquid` | 1-4 | 15 | 1.2 % |
| WaterDragonEgg | `orespawn:eggwaterdragon` | 1-4 | 15 | 1.2 % |
| CephadromeEgg | `orespawn:eggcephadrome` | 1-4 | 15 | 1.2 % |
| KrakenEgg | `orespawn:eggkraken` | 1-4 | 15 | 1.2 % |
| LizardEgg | `orespawn:egglizard` | 1-4 | 15 | 1.2 % |
| DragonEgg | `orespawn:eggdragon` | 1-4 | 15 | 1.2 % |
| BeeEgg | `orespawn:eggbee` | 1-4 | 15 | 1.2 % |
| TrooperBugEgg | `orespawn:eggtrooper` | 1-4 | 15 | 1.2 % |
| SpitBugEgg | `orespawn:eggspit` | 1-4 | 15 | 1.2 % |
| StinkBugEgg | `orespawn:eggstink` | 1-4 | 15 | 1.2 % |
| OstrichEgg | `orespawn:eggostrich` | 1-4 | 15 | 1.2 % |
| GazelleEgg | `orespawn:egggazelle` | 1-4 | 15 | 1.2 % |
| ChipmunkEgg | `orespawn:eggchipmunk` | 1-4 | 15 | 1.2 % |
| CreepingHorrorEgg | `orespawn:eggcreepinghorror` | 1-4 | 15 | 1.2 % |
| TerribleTerrorEgg | `orespawn:eggterribleterror` | 1-4 | 15 | 1.2 % |
| CliffRacerEgg | `orespawn:eggcliffracer` | 1-4 | 15 | 1.2 % |
| TriffidEgg | `orespawn:eggtriffid` | 1-4 | 15 | 1.2 % |
| PitchBlackEgg | `orespawn:eggnightmare` | 1-4 | 15 | 1.2 % |
| LurkingTerrorEgg | `orespawn:egglurkingterror` | 1-4 | 15 | 1.2 % |
| SmallWormEgg | `orespawn:eggsmallworm` | 1-4 | 15 | 1.2 % |
| MediumWormEgg | `orespawn:eggmediumworm` | 1-4 | 15 | 1.2 % |
| LargeWormEgg | `orespawn:egglargeworm` | 1-4 | 15 | 1.2 % |
| TRexEgg | `orespawn:eggtrex` | 1-4 | 15 | 1.2 % |
| GodzillaEgg | `orespawn:egggodzilla` | 1-4 | 15 | 1.2 % |
| MantisEgg | `orespawn:eggmantis` | 1-4 | 15 | 1.2 % |
| HerculesEgg | `orespawn:egghercules` | 1-4 | 15 | 1.2 % |
| VortexEgg | `orespawn:eggvortex` | 1-4 | 15 | 1.2 % |
| RatEgg | `orespawn:eggrat` | 1-4 | 15 | 1.2 % |
| DungeonBeastEgg | `orespawn:eggdungeonbeast` | 1-4 | 15 | 1.2 % |
| FairyEgg | `orespawn:eggfairy` | 1-4 | 15 | 1.2 % |
| WhaleEgg | `orespawn:eggwhale` | 1-4 | 15 | 1.2 % |
| SkateEgg | `orespawn:eggskate` | 1-4 | 15 | 1.2 % |
| IrukandjiEgg | `orespawn:eggirukandji` | 1-4 | 15 | 1.2 % |
| Robot1Egg | `orespawn:eggrobot1` | 1-4 | 15 | 1.2 % |
| Robot2Egg | `orespawn:eggrobot2` | 1-4 | 15 | 1.2 % |
| Robot3Egg | `orespawn:eggrobot3` | 1-4 | 15 | 1.2 % |
| Robot4Egg | `orespawn:eggrobot4` | 1-4 | 15 | 1.2 % |
| Robot5Egg | `orespawn:eggrobot5` | 1-4 | 15 | 1.2 % |
| CriminalEgg | `orespawn:eggcriminal` | 1-4 | 15 | 1.2 % |
| CoinEgg | `orespawn:eggcoin` | 1-4 | 15 | 1.2 % |
| BoyfriendEgg | `orespawn:eggboyfriend` | 1-4 | 15 | 1.2 % |
| EasterBunnyEgg | `orespawn:eggeasterbunny` | 1-4 | 5 | 0.4 % |
| MolenoidEgg | `orespawn:eggmolenoid` | 1-4 | 15 | 1.2 % |
| SeaMonsterEgg | `orespawn:eggseamonster` | 1-4 | 15 | 1.2 % |
| SeaViperEgg | `orespawn:eggseaviper` | 1-4 | 15 | 1.2 % |
| CaterKillerEgg | `orespawn:eggcaterkiller` | 1-4 | 15 | 1.2 % |
| LeonEgg | `orespawn:eggleon` | 1-4 | 15 | 1.2 % |
| HammerheadEgg | `orespawn:egghammerhead` | 1-4 | 15 | 1.2 % |
| RubberDuckyEgg | `orespawn:eggrubberducky` | 1-4 | 15 | 1.2 % |
| NastysaurusEgg | `orespawn:eggnastysaurus` | 1-4 | 15 | 1.2 % |
| PointysaurusEgg | `orespawn:eggpointysaurus` | 1-4 | 15 | 1.2 % |
| BrutalflyEgg | `orespawn:eggbrutalfly` | 1-4 | 15 | 1.2 % |
| CricketEgg | `orespawn:eggcricket` | 1-4 | 15 | 1.2 % |
| FrogEgg | `orespawn:eggfrog` | 1-4 | 15 | 1.2 % |
| AntRobotKit | `orespawn:antrobotkit` | 1-1 | 10 | 0.8 % |
| SpiderRobotKit | `orespawn:spiderrobotkit` | 1-1 | 10 | 0.8 % |
| JefferyEgg | `orespawn:eggrobot6` | 1-4 | 15 | 1.2 % |
| SpiderDriverEgg | `orespawn:eggspiderdriver` | 1-4 | 15 | 1.2 % |
| CrabEgg | `orespawn:eggcrab` | 1-4 | 15 | 1.2 % |
| CassowaryEgg | `orespawn:eggcassowary` | 1-4 | 15 | 1.2 % |

**chestContentsList** (GenericDungeon.java:95) - 91 Einträge, Gewichtssumme 1380

| Item (Feld) | Registry-ID | Anzahl | Gewicht | Anteil je Ziehung |
|---|---|---|---|---|
| MyBacon | `orespawn:cookedbacon` | 6-12 | 20 | 1.4 % |
| MyButterCandy | `orespawn:buttercandy` | 6-12 | 20 | 1.4 % |
| Items.emerald | `minecraft:emerald` | 2-8 | 15 | 1.1 % |
| MyEmeraldPickaxe | `orespawn:emeraldpickaxe` | 1-1 | 15 | 1.1 % |
| MyEmeraldShovel | `orespawn:emeraldshovel` | 1-1 | 15 | 1.1 % |
| MyEmeraldHoe | `orespawn:emeraldhoe` | 1-1 | 15 | 1.1 % |
| MyEmeraldAxe | `orespawn:emeraldaxe` | 1-1 | 15 | 1.1 % |
| MyEmeraldSword | `orespawn:emeraldsword` | 1-1 | 15 | 1.1 % |
| EmeraldBody | `orespawn:emerald_chest` | 1-1 | 15 | 1.1 % |
| EmeraldLegs | `orespawn:emerald_leggings` | 1-1 | 15 | 1.1 % |
| EmeraldHelmet | `orespawn:emerald_helmet` | 1-1 | 15 | 1.1 % |
| EmeraldBoots | `orespawn:emerald_boots` | 1-1 | 15 | 1.1 % |
| MyMothScale | `orespawn:mothscale` | 2-8 | 15 | 1.1 % |
| MothScaleBody | `orespawn:mothscale_chest` | 1-1 | 15 | 1.1 % |
| MothScaleLegs | `orespawn:mothscale_leggings` | 1-1 | 15 | 1.1 % |
| MothScaleHelmet | `orespawn:mothscale_helmet` | 1-1 | 15 | 1.1 % |
| MothScaleBoots | `orespawn:mothscale_boots` | 1-1 | 15 | 1.1 % |
| MyLavaEel | `orespawn:lavaeel` | 2-8 | 15 | 1.1 % |
| LavaEelBody | `orespawn:lavaeel_chest` | 1-1 | 15 | 1.1 % |
| LavaEelLegs | `orespawn:lavaeel_leggings` | 1-1 | 15 | 1.1 % |
| LavaEelHelmet | `orespawn:lavaeel_helmet` | 1-1 | 15 | 1.1 % |
| LavaEelBoots | `orespawn:lavaeel_boots` | 1-1 | 15 | 1.1 % |
| ExperienceBody | `orespawn:experience_chest` | 1-1 | 15 | 1.1 % |
| ExperienceLegs | `orespawn:experience_leggings` | 1-1 | 15 | 1.1 % |
| ExperienceHelmet | `orespawn:experience_helmet` | 1-1 | 15 | 1.1 % |
| ExperienceBoots | `orespawn:experience_boots` | 1-1 | 15 | 1.1 % |
| MyExperienceSword | `orespawn:experiencesword` | 1-1 | 15 | 1.1 % |
| WitherSkeletonEgg | `orespawn:eggwitherskeleton` | 1-4 | 15 | 1.1 % |
| EnderDragonEgg | `orespawn:eggenderdragon` | 1-4 | 15 | 1.1 % |
| SnowGolemEgg | `orespawn:eggsnowgolem` | 1-4 | 15 | 1.1 % |
| IronGolemEgg | `orespawn:eggirongolem` | 1-4 | 15 | 1.1 % |
| WitherBossEgg | `orespawn:eggwitherboss` | 1-4 | 15 | 1.1 % |
| RedCowEgg | `orespawn:eggredcow` | 1-4 | 15 | 1.1 % |
| GoldCowEgg | `orespawn:egggoldcow` | 1-4 | 15 | 1.1 % |
| EnchantedCowEgg | `orespawn:eggenchantedcow` | 1-4 | 15 | 1.1 % |
| MOTHRAEgg | `orespawn:eggmothra` | 1-4 | 15 | 1.1 % |
| AloEgg | `orespawn:eggalosaurus` | 1-4 | 15 | 1.1 % |
| CryoEgg | `orespawn:eggcryolophosaurus` | 1-4 | 15 | 1.1 % |
| CamaEgg | `orespawn:eggcamarasaurus` | 1-4 | 15 | 1.1 % |
| VeloEgg | `orespawn:eggvelocityraptor` | 1-4 | 15 | 1.1 % |
| HydroEgg | `orespawn:egghydrolisc` | 1-4 | 15 | 1.1 % |
| BasilEgg | `orespawn:eggbasilisc` | 1-4 | 15 | 1.1 % |
| DragonflyEgg | `orespawn:eggdragonfly` | 1-4 | 15 | 1.1 % |
| EmperorScorpionEgg | `orespawn:eggemperorscorpion` | 1-4 | 15 | 1.1 % |
| ScorpionEgg | `orespawn:eggscorpion` | 1-4 | 15 | 1.1 % |
| CaveFisherEgg | `orespawn:eggcavefisher` | 1-4 | 15 | 1.1 % |
| SpyroEgg | `orespawn:eggspyro` | 1-4 | 15 | 1.1 % |
| BaryonyxEgg | `orespawn:eggbaryonyx` | 1-4 | 15 | 1.1 % |
| CockateilEgg | `orespawn:eggcockateil` | 1-4 | 15 | 1.1 % |
| GammaMetroidEgg | `orespawn:egggammametroid` | 1-4 | 15 | 1.1 % |
| KyuubiEgg | `orespawn:eggkyuubi` | 1-4 | 15 | 1.1 % |
| AlienEgg | `orespawn:eggalien` | 1-4 | 15 | 1.1 % |
| AttackSquidEgg | `orespawn:eggattacksquid` | 1-4 | 15 | 1.1 % |
| WaterDragonEgg | `orespawn:eggwaterdragon` | 1-4 | 15 | 1.1 % |
| CephadromeEgg | `orespawn:eggcephadrome` | 1-4 | 15 | 1.1 % |
| KrakenEgg | `orespawn:eggkraken` | 1-4 | 15 | 1.1 % |
| LizardEgg | `orespawn:egglizard` | 1-4 | 15 | 1.1 % |
| DragonEgg | `orespawn:eggdragon` | 1-4 | 15 | 1.1 % |
| BeeEgg | `orespawn:eggbee` | 1-4 | 15 | 1.1 % |
| TrooperBugEgg | `orespawn:eggtrooper` | 1-4 | 15 | 1.1 % |
| SpitBugEgg | `orespawn:eggspit` | 1-4 | 15 | 1.1 % |
| StinkBugEgg | `orespawn:eggstink` | 1-4 | 15 | 1.1 % |
| OstrichEgg | `orespawn:eggostrich` | 1-4 | 15 | 1.1 % |
| GazelleEgg | `orespawn:egggazelle` | 1-4 | 15 | 1.1 % |
| ChipmunkEgg | `orespawn:eggchipmunk` | 1-4 | 15 | 1.1 % |
| CreepingHorrorEgg | `orespawn:eggcreepinghorror` | 1-4 | 15 | 1.1 % |
| TerribleTerrorEgg | `orespawn:eggterribleterror` | 1-4 | 15 | 1.1 % |
| CliffRacerEgg | `orespawn:eggcliffracer` | 1-4 | 15 | 1.1 % |
| TriffidEgg | `orespawn:eggtriffid` | 1-4 | 15 | 1.1 % |
| PitchBlackEgg | `orespawn:eggnightmare` | 1-4 | 15 | 1.1 % |
| LurkingTerrorEgg | `orespawn:egglurkingterror` | 1-4 | 15 | 1.1 % |
| SmallWormEgg | `orespawn:eggsmallworm` | 1-4 | 15 | 1.1 % |
| MediumWormEgg | `orespawn:eggmediumworm` | 1-4 | 15 | 1.1 % |
| LargeWormEgg | `orespawn:egglargeworm` | 1-4 | 15 | 1.1 % |
| CassowaryEgg | `orespawn:eggcassowary` | 1-4 | 15 | 1.1 % |
| MolenoidEgg | `orespawn:eggmolenoid` | 1-4 | 15 | 1.1 % |
| SeaMonsterEgg | `orespawn:eggseamonster` | 1-4 | 15 | 1.1 % |
| SeaViperEgg | `orespawn:eggseaviper` | 1-4 | 15 | 1.1 % |
| CaterKillerEgg | `orespawn:eggcaterkiller` | 1-4 | 15 | 1.1 % |
| LeonEgg | `orespawn:eggleon` | 1-4 | 15 | 1.1 % |
| HammerheadEgg | `orespawn:egghammerhead` | 1-4 | 15 | 1.1 % |
| RubberDuckyEgg | `orespawn:eggrubberducky` | 1-4 | 15 | 1.1 % |
| NastysaurusEgg | `orespawn:eggnastysaurus` | 1-4 | 15 | 1.1 % |
| PointysaurusEgg | `orespawn:eggpointysaurus` | 1-4 | 15 | 1.1 % |
| BrutalflyEgg | `orespawn:eggbrutalfly` | 1-4 | 15 | 1.1 % |
| CricketEgg | `orespawn:eggcricket` | 1-4 | 15 | 1.1 % |
| FrogEgg | `orespawn:eggfrog` | 1-4 | 15 | 1.1 % |
| JefferyEgg | `orespawn:eggrobot6` | 1-4 | 15 | 1.1 % |
| SpiderDriverEgg | `orespawn:eggspiderdriver` | 1-4 | 15 | 1.1 % |
| CrabEgg | `orespawn:eggcrab` | 1-4 | 15 | 1.1 % |
| CageEmpty | `orespawn:cageempty` | 3-10 | 20 | 1.4 % |

#### Portierung 1.21.1

**Chunkgrenzen.** Ein `Feature` darf in 1.21.1 nur in den 3×3 Chunks um den dekorierten Chunk schreiben,
also von Chunk-Minimum −16 bis +31. Mit dem Ursprungs-Offset des Aufrufers ist eine Struktur als Feature
sicher, wenn gilt: Offset_min + x_min ≥ −16 und Offset_max + x_max ≤ 31 (für z genauso). Offsets:
`nextInt(16)` (Overworld, End, Crystal, Village), Raster 0..15 (Mining), `nextInt(8)` (Islands),
`nextInt(4)` (Generic in Utopia/Mining/Village), 3..12 (Altar).

| Struktur | Umsetzung | Grund |
|---|---|---|
| `makeDungeon`, `makeRotatorStation`, `makeBeeHive`, `makeHauntedHouse`, `makeCrystalHauntedHouse`, `makeMantisHive`, `makeSmallBeeHive`, `makeEnderKnightDungeon`, `makePlayPool`, `makeWaterDragonLair`, `makeCloudSharkDungeon`, `makeLeafMonsterDungeon`, `makeMiniDungeon`, `makeGoldFishBowl`, `makeEnderReaperGraveyard`, `makeSpitBugLair`, `makeIgloo`, `makeEnderDragonHospital`, `makeBouncyCastle`, `makeDamselInDistress`, `makeLeonNest`, `makeCephadromeAltar`, `makeCrystalBattleTower`, `makeGirlfriendIsland`, `makeMonsterIsland`, `makeFrogPond`, `makeRoundRotator`, `makeRedAntHangout`, `makeRainbow` | Java-`Feature` + `configured_feature`/`placed_feature`-JSON | Ausdehnung passt mit dem Aufrufer-Offset in −16..+31 |
| `makeGreenhouseDungeon`, `makeStinkyHouse`, `makeWhiteHouse`, `makePumpkin` | `Feature`, nur mit Islands-Offset `nextInt(8)` | x bis 22/19/21/13; mit Offset ≤ 7 bleibt es ≤ 31. Vom Spawner-Block aus nicht garantiert, siehe unten |
| `makeUrchinSpawner` | `Feature`, Strahlen am 3×3-Rand abschneiden | Grenzfall: Strahl bis ±17/+18, bei Ursprung 15 bis zu 2 Blöcke drüber |
| `makeShadowDungeon` | `Structure` (ein Teil) oder Ursprung auf ≤ 13 klemmen | 19 breit, Mining-Raster liefert Ursprung bis 15 |
| `makeSpiderHangout` | `Structure` (ein Teil) oder Ursprung ≤ 12 | 20 breit bei Offset bis 15 |
| `makeNightmareRookery` | `Structure` | z-Zufallsweg bis ±53 |
| `makeEnormousCastle`, `makeEnormousCastleQ` | `Structure`, mehrere `StructurePiece`s (Sockel, Etagen, Plattform/Treppe) | 93×82×84 |
| `makeKyuubiDungeon` | `Structure` | 35×28×30 |
| `makeAlienWTFDungeon` | `Structure` (Kern, Schacht, 4 Räume, 4 Gänge als Teile) | 37×20×37 |
| `makeEnderCastle` | `Structure` | 31×17×31 |
| `makeIncaPyramid` | `Structure` | 61×20×51 |
| `makeRobotLab` | `Structure` (Labor, Halle, Turm, Einbauten) | 30×44×50 |
| `makeKingAltar`, `makeQueenAltar` | `Structure` | 61×68×61 |

Hinweise zum Umbau:
- **Zufall in Structure-Teilen.** Das Original würfelt während des Bauens mit `world.rand`: Moos,
  Etagenzahl, Regale, Rookery-Weg, Urchin-Strahlen, Ei-Blöcke des Ender Castle. Ein `StructurePiece`
  wird pro Chunk einzeln nachgebaut. Alle Entscheidungen, die mehrere Chunks betreffen, gehören deshalb
  in `generatePieces` und ins Teil-NBT; Einzelblock-Zufall aus einem Positions-Hash
  (`RandomSource` mit `pos.asLong()` gesät). Sonst entstehen Nähte an Chunkgrenzen.
- **Seltenheit.** `nextInt(N)` pro Chunk wird zu `rarity_filter` mit `chance: N` (Features), bei
  Structures zu `structure_set` mit `random_spread` oder eigenem `Structure#findGenerationPoint`.
  `recently_placed` (globaler Zähler über Chunk-Aufrufe) ist nicht deterministisch und nicht
  thread-sicher. Nachbildung höchstens näherungsweise über `exclusion_zone`/Abstand, und genau das als
  Abweichung dokumentieren.
- **Biome.** 1.7.10-Namen per Datapack-Biomliste: `Plains` → `minecraft:plains`, `Taiga` → `taiga`,
  `Swampland` → `swamp`, `Ice Plains` → `snowy_plains`, `Desert` → `desert`, `Ocean` → `ocean`,
  `Forest` → `forest`, `Jungle` → `jungle`, `Birch Forest` → `birch_forest`. `ForestHills`,
  `JungleHills` und `Birch Forest Hills` gibt es nicht mehr. Overworld-Features über NeoForge
  `neoforge:add_features`-Biome-Modifier; die Oberflächensuche (Luft über `grass`/`water`/`sand`/`snow`)
  bleibt Java im `Feature#place`. In den eigenen Dimensionen hängen sie direkt am ChunkGenerator bzw.
  an deren Biomen.
- **Spawner.** `SpawnerBlockEntity#setEntityId(EntityType, RandomSource)` mit den IDs aus der Tabelle.
- **Truhen.** Die 38 Listen werden `data/orespawn/loot_table/chests/<liste>.json`: `rolls` =
  `uniform(min,max)` der Ziehungen je Aufruf, `weight`, `set_count`. Weil die Ziehungszahl pro Truhe
  unterschiedlich ist, die Rollen im Java-Code setzen oder je Truhentyp eine eigene Tabelle anlegen.
  Feste Slots (Haunted House, Igloo, Stufe-6-Belohnung, Altar-Eier, Rotator/Urchin) direkt über
  `ChestBlockEntity#setItem` in Slot n.
- **Doppeltruhen.** Play Pool, Leaf Monster, Rubber Ducky und Rainbow setzen zwei benachbarte Truhen,
  die in 1.7.10 automatisch verschmelzen. In 1.21.1 `ChestType.LEFT/RIGHT` explizit setzen.
- **Entities beim Bau.** Girlfriend (Damsel), Robot Spider, Robot Red Ant, 4 End-Kristalle (Hospital):
  in Feature/Piece über `WorldGenLevel#addFreshEntity` nach `EntityType.create(level.getLevel())`. Beim
  Kristall steht im Original `bedrock` im selben Block wie die Entity; 1:1 oder auf y+1 anheben ist
  eine Entscheidung.
- **Blockzustände.** `setBlockFast` mit Flag 2 wird `level.setBlock(pos, state, 2)`.
  - Meta auf BlockState: Truhen 2..5 → FACING N/S/W/E; `stained_hardened_clay` 0/1/3/4/5/6/10/11/13/14 →
    white/orange/light_blue/yellow/lime/pink/purple/blue/green/red_terracotta.
  - `stonebrick` 3 → `chiseled_stone_bricks`; `quartz_block` 2 → `quartz_pillar`; `Blocks.snow` → `snow_block`;
    `web` → `cobweb`; `reeds` → `sugar_cane`; `waterlily` → `lily_pad`; `stone_slab` 0 → `smooth_stone_slab`.
  - `flowing_water` Meta 0 als Wasserquelle mit geplantem Fluid-Tick.
- **Höhen.** `setBlockFast` verwirft `y < 0` (OreSpawnMain.java:5503-5505). Das betrifft Bee Hive (bis
  y −30 unter der Oberfläche) und die Dirt-Füllung der Altäre nur bei sehr tiefer Lage. In Dimensionen
  mit `min_y = 0` ist das identisch, in der 1.21-Overworld nicht; dort die Grenze nachbilden oder bewusst
  weglassen.
- **Guardrails.** Keine Mixins nötig, kein Client-Code, läuft auf dem dedizierten Server. Keine
  Health-/Armor-Werte in dieser Klasse.

Portrisiken, die eine Entscheidung brauchen:
1. `piston_extension` Meta 1 in den White-House-Bänken (:5641-5680) wird in 1.21.1 `moving_piston` ohne
   Block-Entity. Das ist ein ungültiger Zustand und verschwindet oder erzeugt Fehler; 1:1 nicht sinnvoll
   übertragbar.
2. `lit_redstone_lamp` (:3950) ohne Stromquelle geht in 1.21.1 beim ersten Nachbar-Update aus.
3. `addD4WhiteHouse` prüft z −20..299 (OreSpawnWorld.java:2441), vermutlich Tippfehler für 30. Es
   bedeutet 50×320 Blöcke Luftprüfung und macht das White House praktisch unmöglich. 1:1 übernehmen
   oder bewusst korrigieren.
4. `addD4RobotLab` prüft x −5..59, z −5..69 (OreSpawnWorld.java:2515-2516), die Struktur belegt aber
   x −10..19, z −1..48. Die Prüfung passt nicht zum Bau.
5. Vom `DungeonSpawnerBlock` aus wird jede Struktur ohne Oberflächen- oder Platzprüfung an beliebiger
   Position gebaut, auch die 61er-Altäre. Außerhalb der Worldgen gibt es keine Chunkgrenzen-Regel; im
   Port über `StructureTemplate`-freies Direktbauen im Server-Tick (`ServerLevel#setBlock`) machbar,
   lädt aber bis zu 5×5 Chunks.
6. Zwei Doppeltruhen-Hälften sind absichtlich leer (Play Pool, Leaf Monster, Rubber Ducky). In 1.21.1
   bleibt das so, wenn nur eine Hälfte befüllt wird.

#### Abweichungen zur Recherche (Quellcode gilt)

- 02-dimensions-worldgen.md:472 führt den Generic Dungeon unter „Nicht aus der Weltgenerierung“. Der
  Code platziert ihn in Utopia, Mining, VillageMania (`addGenericDungeon`, OreSpawnWorld.java:2100-2115)
  und Islands (`addD4GenericDungeon`, OreSpawnWorld.java:2588-2603).
- 02-dimensions-worldgen.md:481-482 nennt einen Konflikt „stone/cobblestone“ gegen „dirt“ für den Random
  Dungeon Spawner. Der Code erlaubt `stone`, `cobblestone`, `grass` und `dirt` ab y 40
  (ItemRandomDungeon.java:42-47).
- 02-dimensions-worldgen.md:447 sagt „Extreme Torch auf Eye-of-Ender-Block“ beim Cephadrome Altar. Im
  Code steht das Auge in der Mitte bei y 3 und die `ExtremeTorch`es auf den vier Eckpfeilern bei y 4
  (GenericDungeon.java:4821-4864).
- 02-dimensions-worldgen.md:477-480 hat nur die Fälle 0–18 des Spawner-Blocks gelesen; hier vollständig
  0–49.
- 02-dimensions-worldgen.md:429-430 sagt, die Stufenlogik des Challenge Dungeon sei nicht ausgewertet.
  Sie steht oben; die Belohnung Stufe 6 liegt in den Truhen der untersten Etage (decor 1).
- 02-dimensions-worldgen.md:452 „Kürbisse“ beim Pumpkin: der Code baut einen Kürbis aus orangefarbenem
  `stained_hardened_clay`, keine Kürbisblöcke (GenericDungeon.java:6088-6108).
