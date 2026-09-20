# Verhalten: world-04

Dieser Batch umfasst fünf Generatorklassen ohne eigene Registry-ID. Sie schreiben Blöcke direkt in die Welt. `Trees` sammelt sieben Baumgeneratoren und die gemeinsame Kristall-Lootliste:
- Wind Tree und Sky Tree (Utopia-Populate)
- Duplicator Tree und Experience Tree (Wachstum per Random Tick eines Blocks)
- Small Tree (Entity `island_too`)
- Fairy Tree und Fairy Castle Tree (Crystal-Populate und Random-Dungeon-Block)
- ein toter Scraggly Tree

`BasiliskMaze` baut eine bis 73 Blöcke lange unterirdische Anlage: Sandsteinpyramide, Wendelschacht, Vorkammer, Obsidian-Irrgarten und Halle mit drei persistenten Basilisk-Entities. `CrystalMaze` legt beim Erzeugen **jedes** Chunks der Crystal Dimension ein 16x5x16-Bedrock-Labyrinth auf Y 24..28. `RubyBirdDungeon` ist ein 10x5x10-Raum mit Ruby-Bird-Spawner in Utopia und Islands. `NightmareDungeon` ist **toter Code**. Für die Portierung heißt das:
- **Structure:** Sky Tree, Wind Tree, Fairy Castle Tree und Basilisk Maze sprengen das 3x3-Chunk-Fenster; der Fairy Tree knapp um 1-2 Blöcke.
- **Feature:** Ruby-Bird-Dungeon (und ein eventueller Nightmare Dungeon) passen hinein.
- **Java-ChunkGenerator:** das Crystal Maze.

---

## Gemeinsame Grundlagen

**Block-Schreibwege**

| Helfer | Verhalten | Herkunft |
|---|---|---|
| `OreSpawnMain.setBlockFast(world,x,y,z,block,meta,flags)` | Verwirft Y<0 und Y>=256. Holt den Chunk per `world.getChunkFromChunkCoords` und **lädt bzw. generiert dabei Nachbarchunks**. Schreibt über `setBlockIDWithMetadataFast` direkt in `ExtendedBlockStorage#func_150818_a` + `setExtBlockMetadata`: kein Licht-Update, kein `onBlockAdded`, keine Tile-Entity-Anlage, keine Stützprüfung. Flag 2 → `markBlockForUpdate` (Client-Sync). Flag 1 → `notifyBlockChange` (Nachbar-Updates). | OreSpawnMain.java:5499-5524, 5559-5582 |
| `OreSpawnMain.setBlockIDWithMetadataInChunk(chunk,…)` | Wie oben, aber ohne Welt. Koordinaten außerhalb des übergebenen Chunks werden **verworfen**. | OreSpawnMain.java:5600-5631 |
| `world.setBlock(x,y,z,block,meta,2)` | Vanilla-Weg. Wird nur für Spawner und Truhen benutzt, weil die eine Tile-Entity brauchen. | z. B. Trees.java:499, 504 |

**Loot:** `WeightedRandomChestContent(item, damage, min, max, weight)`. `generateChestContents(rand, liste, truhe, n)` heißt laut MCP `func_76293_a` (methods.csv:3646). Vanilla-Semantik 1.7.10: n gewichtete Züge, Stapel min..max, zufälliger Slot. Diese Semantik ist nicht im OreSpawn-Code und nicht am Jar nachgeprüft (siehe unresolved). „% je Zug" = Gewicht/Summe.

**SRG:**
- `func_145881_a()` = `TileEntityMobSpawner` → `MobSpawnerBaseLogic` (joined.srg:11848)
- `func_110163_bv()` = `enablePersistence` (methods.csv:32)

**Spawner-Strings → 1.21.1-IDs (manifest):**

| String | ID | max_health / attack_damage |
|---|---|---|
| "Fairy" | `orespawn:fairy` | 40 / 3.0 |
| "Basilisk" | `orespawn:basilisk` | 200 / 24 |
| "Emperor Scorpion" | `orespawn:emperor_scorpion` | 350 / 35 |
| "Nightmare" (Klasse `PitchBlack`) | `orespawn:nightmare` | `PitchBlack_stats.health * getPitchBlackScale()`; Config-Default 250 / 30 (OreSpawnMain.java:6189) |
| "Ruby Bird" | `orespawn:ruby_bird` | 2 / 1.0 |

Alle Werte liegen unter den 1.21.1-Klemmen. Offen bleibt nur, ob die Nightmare-Skalierung 1024 überschreitet (Faktor in `PitchBlack`, anderer Batch).

**Dimensionen** (manifest `dimensions`; IDs OreSpawnMain.java:1266-1271, `BaseDimensionID` Default 80):

| Feld | Name | ChunkProvider |
|---|---|---|
| `DimensionID` | Dimension-Utopia | `ChunkProviderOreSpawn` |
| `DimensionID2` | Dimension-Extreme (Mining) | `ChunkProviderOreSpawn2` |
| `DimensionID4` | Dimension-Islands (Danger) | `ChunkProviderOreSpawn4` |
| `DimensionID5` | Dimension-Crystal | `ChunkProviderOreSpawn5` |

**Config** (manifest):
- `OreSpawnTWEAKS.LessLag`, Default 0, geklemmt auf 0..2 (OreSpawnMain.java:1221-1226)
- `OreSpawnTWEAKS.DuplicatorTreeEnable`, Default 1 → `OreSpawnMain.enableduplicatortree` (OreSpawnMain.java:1153)

**`OreSpawnWorld.recently_placed`:** statisches `int` (OreSpawnWorld.java:18), nicht gespeichert. Jeder `generate()`-Aufruf in jeder Dimension senkt es um 1 (OreSpawnWorld.java:25-27). Große Strukturen setzen es auf 50 und sperren damit andere Großstrukturen für die nächsten 50 generierten Chunks, egal wo.

**Singletons:** In `postInit` werden `BMaze = new BasiliskMaze()`, `RubyDungeon = new RubyBirdDungeon()`, `MyDungeon = new GenericDungeon()` und `OreSpawnTrees = new Trees()` angelegt (OreSpawnMain.java:5442-5445; calls_OreSpawnMain.txt:9602-9608).

---

### Trees

**Rolle:** Singleton `OreSpawnMain.OreSpawnTrees` (OreSpawnMain.java:5445), keine Registry-ID.

| Methode | Aufrufer | Wann |
|---|---|---|
| `WindTree` | `OreSpawnWorld.addOtherTrees` (OreSpawnWorld.java:2701) | Chunk-Populate Utopia |
| `SkyTree` | `OreSpawnWorld.addOtherTrees` (OreSpawnWorld.java:2708) | Chunk-Populate Utopia |
| `DuplicatorTree` | `BlockDuplicatorLog.updateTick`, nur wenn `enableduplicatortree != 0` (BlockDuplicatorLog.java:24-31) | Random Tick (`setTickRandomly(true)`, BlockDuplicatorLog.java:17) jedes `duplicatortreelog`. Den Stamm setzt u. a. `addVeggies` (OreSpawnWorld.java:1997, anderer Batch). |
| `ExperienceTree` | `BlockExperiencePlant.updateTick` (BlockExperiencePlant.java:33-42) | Random Tick des Setzlings `experiencesapling`, 1/10 (`nextInt(10) != 1` → return). Der Setzling wird Luft; Aufruf mit `y-1`, also auf dem Bodenblock. |
| `SmallTree` | `IslandToo` (IslandToo.java:209, 451) | Entity `orespawn:island_too` legt Gras. Pro neuem Grasblock: 1/30 Blume, sonst 1/100 `SmallTree` auf dem Block darüber, wenn dort Luft ist. |
| `FairyTree` / `FairyCastleTree` | `OreSpawnWorld.addFairyTree` (OreSpawnWorld.java:2071, 2074) | Chunk-Populate Crystal |
| `FairyTree` / `FairyCastleTree` | `DungeonSpawnerBlock.updateTick` (DungeonSpawnerBlock.java:40-52) | Block `dungeonspawner` (Drop/Item `randomdungeon`, DungeonSpawnerBlock.java:204-206). `updateTick` kommt 400 Ticks nach `onBlockAdded` (DungeonSpawnerBlock.java:33) **oder** früher per Random Tick (DungeonSpawnerBlock.java:16). Der Block und der darüber werden Luft, dann `nextInt(50)`: 0 → FairyTree, 1 → FairyCastleTree, je 1/50. |
| `ScragglyTreeWithBranches`, `makeScragglyBranch` | kein externer Aufrufer | `ChunkProviderOreSpawn4/6` haben eigene Kopien mit `Chunk`-Parameter (ChunkProviderOreSpawn4.java:181, ChunkProviderOreSpawn6.java:413) → in `Trees` toter Code |
| `CrystalChestContentsList` (static) | `FairyTree`, `addSomething`, `OreSpawnWorld.addCrystalChest` (OreSpawnWorld.java:1820-1830: 1/3 Truhe, 1-3 Züge) | |

#### Platzierung in der Weltgenerierung

| Baum | Dimension | Bedingung | Herkunft |
|---|---|---|---|
| Wind / Sky | Utopia | `addOtherTrees` läuft nur, wenn `addHugeTree` und `addAppleTrees` false liefern | OreSpawnWorld.java:30-31 |
| | | 1/30 je Chunk | OreSpawnWorld.java:2675 |
| | | `LessLag` 1: zusätzlich 1/2, nc=4; `LessLag` 2: zusätzlich 1/4, nc=3; sonst nc=5 | OreSpawnWorld.java:2673-2689 |
| | | `what = nextInt(2)`: 0 → nur Wind Trees, 1 → nur Sky Trees | OreSpawnWorld.java:2692 |
| | | nc Versuche an X/Z = Chunk+3..12 | OreSpawnWorld.java:2694-2695 |
| | | Y 100 abwärts bis 51, solange Luft. Gras unter der Luft → Baum bei `posY-1`, also auf dem Grasblock. | OreSpawnWorld.java:2696-2698 |
| | | Abbruch nach 4 Wind bzw. 3 Sky Trees | OreSpawnWorld.java:2702, 2709 |
| | | `dir` fest 0 → alle Wind Trees zeigen nach +X (Ost) | OreSpawnWorld.java:2691 |
| | | Liefert ein Baum `true`, entfällt der King/Queen-Altar dieses Chunks | OreSpawnWorld.java:31-32, 2721 |
| Fairy / Fairy Castle | Crystal | Position fest Chunk+8/+8; 1/5 | OreSpawnWorld.java:2047-2051 |
| | | Y 128→41: Luft mit `crystalgrass` darunter | OreSpawnWorld.java:2052-2053 |
| | | 17x17 auf posY komplett Luft, sonst `return false` | OreSpawnWorld.java:2054-2061 |
| | | 5x5 auf posY-1 komplett `crystalgrass`, sonst `return false` | OreSpawnWorld.java:2062-2069 |
| | | `nextInt(5) != 1` (4/5) → `FairyTree(posY-1)`, sonst (1/5) `FairyCastleTree(posY)`; `recently_placed = 50` | OreSpawnWorld.java:2070-2076 |
| | | **Eigenheit:** findet die Y-Schleife keine Stelle, liefert die Methode trotzdem `true`. Damit entfallen Termiten und alle Crystal-Strukturen dieses Chunks. | OreSpawnWorld.java:2080, 177-184 |

#### Generatoren: Maße und Palette

Die Maße sind aus den Schleifengrenzen berechnet (Maximalwerte der Zufallszahlen) und gelten relativ zum Aufrufpunkt (x,y,z). Palette in 1.21.1-IDs (manifest): `Blocks.log` meta 0 = `minecraft:oak_log`, `Blocks.leaves` meta 0 = `minecraft:oak_leaves`.

| Methode | Maße x*y*z (max.) | Palette | Aufbau |
|---|---|---|---|
| `WindTree(x,y,z,dir)` | 38*48*3 bei dir 0 (x 0..+37, y 0..+47, z -1..+1) | oak_log, oak_leaves | Nur auf `grass`/`dirt` (Trees.java:59-62). Höhe h = 40-47 (Trees.java:63). `width` 8-11 wird berechnet, aber nie benutzt (Trees.java:64). Stamm j=0..h-1, **ersetzt den Bodenblock**. Ab j > h/5 ein Blatt auf der +dir-Seite. Bei j > h/4 und j%4==0 ein Ast der Länge h-j in dir (Trees.java:65-72). Astblöcke i=1..len: Log, Blatt darüber; für i < len/3 zweite Blattlage; für i > len/3 Blätter seitlich; zwei Endblätter bei len+1/len+2 (Trees.java:15-39). Spitzenblatt bei y+h (Trees.java:74). Reichweite: h=47 → erster Ast j=12, Länge 35, +2 Blätter = 37. |
| `SkyTree(x,y,z)` | 69*(206-y)*69, Kreuzform | `orespawn:skytreelog`, oak_leaves | Nur auf `grass`/`dirt` (Trees.java:96-99). **Absolute** Stammspitze Y 190-204 (Trees.java:100); Abbruch, wenn sie weniger als 20 über y liegt (Trees.java:101-103). Stamm y..Spitze, ersetzt den Bodenblock; Blatt bei Spitze+1 (Trees.java:105-108). 4 Äste auf Spitzenhöhe, Länge 25-34 (Trees.java:104, 109-112). Zweite Lage 5-8 tiefer mit Länge/3 = 8-11 (Trees.java:113-119). Ast: Log i=1..len-1 mit Blatt oben und beidseitig, Blatt bei len (Trees.java:77-93) → Reichweite 34. |
| `DuplicatorTree(x,y,z)` | 5*4*5 inkl. Kopierfeld | `orespawn:duplicatortreelog`, `orespawn:leaves_apple` | Siehe „Duplicator-Mechanik". |
| `ExperienceTree(x,y,z)` | 56*30*54 (x -27..+28, y +1..+30, z -26..+27) | oak_log, `orespawn:leaves_experience` | Nur auf `grass`/`dirt`/`farmland` (Trees.java:295-298). 2x2-Stamm y+1..y+5 (Trees.java:299-305). 4× `grow_branch` bei y+6 (Trees.java:306-309). Stamm y+7..y+18 (Trees.java:310-316). 4× `grow_small_branch` bei y+19 (Trees.java:317-320). Krone 2x2 ab y+19, Höhe 5-10, jeder Block mit Blätterwolke (Trees.java:321-328). Reichweite Ast 8+10+7-1 = 24, +3 Blätter. |
| `SmallTree(x,y,z)` | 3*4*3 | `orespawn:skytreelog`, `orespawn:leaves_apple` | Siehe unten. |
| `FairyTree(x,y,z)` | `LessLag` 0: 50*15*48 (x -24..+25, y +1..+15, z -23..+24); `LessLag` 1: 44*15*42; `LessLag` 2: 38*15*36 | `orespawn:crystaltreelog`, `orespawn:crystaltreeleaves3`, `minecraft:spawner`, `minecraft:chest` | **Keine Bodenprüfung.** 2x2-Stamm y+1..y+5 (Trees.java:476-482). 8× `grow_crystal_branch` mit ydir -1: 4 bei y+5, 4 bei y+6 (Trees.java:483-490). Krone ab y+6, Höhe 5-9 (Trees.java:491-498). Spawner "Fairy" bei (x-1, y+1, z) (Trees.java:499-503). Truhe bei (x+2, y+1, z) mit 1-5 Zügen (Trees.java:504-508). Reichweite 7+9+7-1 = 22, +2 Blätter. |
| `FairyCastleTree(x,y,z)` | `LessLag` 0: 85*36*85 (±42, y +2..+37); `LessLag` 1: 71*30*71; `LessLag` 2: 57*24*57 | crystaltreelog, `orespawn:crystaltreeleaves2` (untere Blattlage), crystaltreeleaves3 (obere), `orespawn:crystaltorch`, Spawner, Truhen | Siehe unten. |
| `ScragglyTreeWithBranches` / `makeScragglyBranch` | Stamm 1-3, dann Zufallsweg mit bis zu 11 Schritten | oak_log, leaves_apple | Toter Code (Trees.java:386-473). |

**Astgeometrie** (`grow_branch` Trees.java:249-292, `grow_small_branch` Trees.java:204-247, `grow_crystal_branch` Trees.java:542-620). `(xdir,zdir)` ist die Hauptrichtung, `(xxdir,zzdir)` die Nebenrichtung.

| Phase | Verlauf | `grow_branch` | `grow_small_branch` | `grow_crystal_branch` |
|---|---|---|---|---|
| 1 | diagonal aufwärts in Hauptrichtung | 5-8 | 4-5 | 4-7 |
| 2 | waagrecht weiter in Hauptrichtung | 6-10 | 4-6 | 5-9 |
| 3 | waagrecht in Nebenrichtung, ab Ende Phase 1 | 6-10 | 4-6 | 5-9 |
| 4 | ab Ende Phase 2 eine Ebene tiefer, je Schritt -1 Y (`m += ydir`) | 4-7 | 3-5 | 4-7 |
| 5 | ab Ende Phase 3 eine Ebene tiefer, je Schritt -1 Y | 4-7 | 3-5 | 4-7 |

Bei `grow_crystal_branch` wird jede Phase bei `LessLag` 1 um 1 und bei `LessLag` 2 um 2 kürzer (Trees.java:550-555 usw.). Jeder Log-Block bekommt eine Blätterwolke, die nur Luft ersetzt:

| Methode | Wolke (x*y*z, ab Log-Höhe aufwärts) | Block |
|---|---|---|
| `make_leaves` (Trees.java:191-202) | 7*3*7 | `leaves_experience` |
| `make_crystal_leaves` (Trees.java:511-522) | 5*2*5 | `crystaltreeleaves3` |
| `make_crystal_castle_leaves` (Trees.java:524-540) | 3*2*3: Lage 0 `crystaltreeleaves2`, Lage 1 `crystaltreeleaves3` | |

**Fairy Castle Tree** (Trees.java:640-814): **kein Stamm**, nur schwebende quadratische Log-Plattformen.
- `nc` = 6, bei `LessLag` 1 5, bei `LessLag` 2 4 (Trees.java:641-647). Start `j` = 3-5, `spread` = 0 (Trees.java:648-649).
- Pro Iteration `grow` = 4-6 (Trees.java:651), dann:
  - Plattform (+spread, 0): Halbbreite 1-3, Y-Versatz -1..+1 (Trees.java:652-670)
  - ab Iteration 1 zusätzlich (-spread,0), (0,+spread), (0,-spread), Halbbreite `1+nextInt(3+iter)` (Trees.java:671-729)
  - ab Iteration 2 zusätzlich die vier Diagonalen (±spread, ±spread) (Trees.java:730-807)
- `j += grow`. Nach Iteration 0 wird `spread = 3`, danach immer `spread += grow` (Trees.java:808-812).
- Jede Plattform: Randfelder mit `make_crystal_castle_leaves`, auf den 4 Ecken eine `crystaltorch` eine Ebene höher. Im Zentrum `addSomething`, außer bei der allerersten Plattform (Trees.java:660).
- `addSomething` (Trees.java:622-638), `nextInt(3)`: 1 → Spawner "Fairy" auf Plattform+1; 2 → Truhe mit 1-5 Zügen; 0 → nichts.
- Maximalmaße: `spread` in Iteration 5 höchstens 3+5·6 = 33, Halbbreite 8, +1 Blatt = 42. Y höchstens 5+5·6+1+1 = y+37, mindestens 3-1 = y+2.

**Duplicator-Mechanik** (Trees.java:122-189). Ein Aufruf = genau ein Schritt.
1. Ist der Block bei y-1 nicht `grass`/`dirt`/`farmland`, endet die Methode **immer**. Die Zuweisungen `realy = y-2/y-3` sind wirkungslos (Trees.java:124-138). Nur das unterste Log direkt auf Erde wächst.
2. `realy = y-1`. Pro Aufruf nur der erste fehlende Block, in dieser Reihenfolge: `duplicatortreelog` bei realy+1, +2, +3; `leaves_apple` bei realy+4; dann die 8 Ringplätze um realy+3 (Trees.java:139-170). Ausgewachsen: 3 Logs, 9 Blätter. Deckt sich mit research „3 high and 9 total leaf blocks".
3. Ausgewachsen, bis zu 20 Versuche (Trees.java:171-188): ein Zufallsfeld im 5x5 (-2..+2) auf Höhe realy+1. Ist es weder Luft noch Duplicator-Log, sucht die Methode bis zu 20-mal ein Luftfeld im selben 5x5 und setzt dort **denselben Block mit demselben Metadatum** (`world.setBlock`, Flag 2).
   - Tile-Entity-Inhalte werden nicht kopiert.
   - Es gibt keine Sperrliste; Bedrock und Spawner werden mitkopiert.
   - Ohne freies Luftfeld endet der Aufruf trotzdem, weil `bidm` jetzt nicht mehr Luft ist.

**SmallTree** (Trees.java:331-384): gleiche Bodenlogik wie der Duplicator. Ohne Erde darunter wird (x,y,z) zu Luft (Trees.java:338-341). Stamm und Blätter ersetzen nur Luft.

| Zweig | Chance | Stamm `skytreelog` | Apfelblätter |
|---|---|---|---|
| A | 1/2 | y | Spitze y+1, 3x3 bei y |
| B | 1/4 | y..y+1 | Spitze y+2, 3x3 bei y+1 |
| C | 1/4 | y..y+2 | Spitze y+3, 3x3 bei y+2 |

**Scraggly** (tot, Trees.java:430-473): Stamm 1-3 hoch, dann Zufallsweg bis `j = i + nextInt(12)`. Pro Schritt ±1 in X/Z, +1 Y mit 3/4. Mit 1/4 ein Seitenast (`makeScragglyBranch`, Trees.java:386-428; dort +1 Y mit 2/3). Blätter im 3x3 je 1/2, darüber 1/2. Abbruch an jedem Block außer Luft, Log und Apfelblatt.

#### Truhe `CrystalChestContentsList`

82 Einträge, Gewichtssumme 755 (Trees.java:817). Züge: FairyTree 1-5 (Trees.java:507), `addSomething` 1-5 (Trees.java:635), `addCrystalChest` 1-3 (OreSpawnWorld.java:1829).

| Item (Registry-ID, manifest) | Menge | Gewicht je | % je Zug |
|---|---|---|---|
| `crystaltermiteblock` | 1-5 | 10 | 1,3 |
| `crystalflower_red`, `crystalflower_blue`, `crystalflower_green`, `crystalflower_yellow`, `crystalplanks` | 1-10 | 10 | 1,3 |
| `crystalworkbench`, `crystalfurnace` | 1 | 10 | 1,3 |
| `tigerseye_block` | 1-10 | 5 | 0,7 |
| `crystalstone`, `crystalrat`, `crystalfairy`, `crystalcoal`, `crystalgrass`, `crystalcrystal`, `crystaltorch`, `crystaltreeleaves`, `crystaltreeleaves2`, `crystaltreeleaves3`, `crystaltreelog` | 1-10 | 10 | 1,3 |
| `tigerseye` | 1-10 | 5 | 0,7 |
| `crystalwoodsword/axe/shovel/pickaxe/hoe` | 1 | 10 | 1,3 |
| `crystalpinksword/axe/shovel/pickaxe/hoe` | 1 | 10 | 1,3 |
| `tigerseye_sword/axe/shovel/pickaxe/hoe` | 1 | 5 | 0,7 |
| `crystalstonesword/axe/shovel/pickaxe/hoe` | 1 | 10 | 1,3 |
| `tigerseye_ingot` | 1-5 | 5 | 0,7 |
| `crystalpink_ingot`, `crystalapple`, `peacockfeather` | 1-5 | 10 | 1,3 |
| `cookedpeacock`, `rawpeacock`, `rice`, `quinoa` | 1-10 | 20 | 2,6 |
| `pink_helmet/chest/leggings/boots` | 1 | 10 | 1,3 |
| `tigerseye_helmet/chest/leggings/boots` | 1 | 5 | 0,7 |
| `peacock_helmet/chest/leggings/boots` | 1 | 10 | 1,3 |
| `eggrotator`, `eggvortex`, `eggpeacock`, `eggdungeonbeast`, `eggfairy`, `eggrat`, `eggflounder`, `eggwhale`, `eggirukandji`, `eggskate`, `eggurchin`, `eggghost`, `eggghostskelly` | 1-5 | 10 | 1,3 |
| `skatebow` | 1 | 2 | 0,3 |
| `irukandjiarrow` | 5-10 | 2 | 0,3 |
| `deadirukandji` | 2-8 | 5 | 0,7 |
| `ultimatebow`, `ultimatesword` | 1 | 2 | 0,3 |
| `minecraft:iron_ingot` | 1-4 | 10 | 1,3 |
| `minecraft:oak_log` (`Blocks.log` meta 0) | 1-4 | 10 | 1,3 |
| `minecraft:golden_apple` | 1-5 | 2 | 0,3 |

**Research:** 02-dimensions-worldgen.md:461 schreibt der Castle-Variante eigene Loot zu. Laut Code nutzen Fairy Tree, Castle Tree und Crystal-Truhen dieselbe Liste.

#### Portierung 1.21.1

| Generator | Ziel | Chunk-Grenze / Begründung |
|---|---|---|
| Sky Tree | **Structure**; eine Start = Gruppe bis 3 Bäume eines Chunks, ein Piece je Baum | Reichweite ±34 bei Ursprung Chunk+3..12 → Chunk-31..+46, über dem 3x3-Fenster (-16..+31). Spitze bis Y 205 → `dimension_type` Utopia mit `min_y` 0, `height` 256. |
| Wind Tree | **Structure** (Gruppe bis 4 Bäume) | +37 in +X ab Chunk+3..12 → bis +49 |
| Fairy Castle Tree | **Structure** | 85x85. Die Prüfung (17x17 Luft, 5x5 `crystalgrass`) gehört in `findGenerationPoint`. |
| Fairy Tree | **Structure** (empfohlen) | Ursprung Chunk+8: x -16..+33, z -15..+32 → 1-2 Blöcke über +31. Ein Feature mit Abschneiden wäre eine Abweichung. |
| Experience / Duplicator / Small Tree | kein Worldgen; Java im `randomTick` des Blocks bzw. im Entity-Tick über `ServerLevel#setBlock` | Experience Tree schreibt bis 28 Blöcke weit und kann Nachbarchunks laden, wie das Original über `getChunkFromChunkCoords` |
| Scraggly | nicht portieren | tot |

- **Placement Utopia:** Die Kette `addHugeTree` → `addAppleTrees` → `addOtherTrees` hängt von anderen Generatoren ab (anderer Batch). Der Wurf 1/30 × `LessLag` ist config-abhängig. Beides braucht Java in `findGenerationPoint`; ein `StructureSet` mit `random_spread` spacing 1 / separation 0 prüft jeden Chunk. Die Grasprüfung Y 100→51 muss der Utopia-ChunkGenerator beantworten, weil Surface-Blöcke vor der Surface-Stufe nicht existieren.
- **`recently_placed`** hängt von der Generierungsreihenfolge ab und ist nicht reproduzierbar. Näherung über `exclusion_zone` oder Spacing der StructureSets (Abweichung).
- **Blätter:** 1.21.1-Blätter zerfallen ohne korrekte `distance`. Entweder `persistent=true` (kein Zerfall; ob das Original zerfällt, steht in den Blattklassen, anderer Batch) oder Distanzen nachrechnen. `skytreelog`, `duplicatortreelog`, `crystaltreelog` gehören in `#minecraft:logs`.
- **Spawner:** `SpawnerBlockEntity#setEntityId(EntityType, RandomSource)`.
- **Loot:** Loot-Table `orespawn:chests/crystal`; die Rollzahl setzt der Aufrufer (1-5 bzw. 1-3), also zwei Tabellen oder `rolls` im Code.
- **Duplicator:** kopiert `BlockState`, ohne Block-Entity-Daten. Eine Sperrliste wäre eine bewusste Abweichung.
- **Random-Dungeon-Block:** Fairy Tree und Castle Tree zur Laufzeit über dieselbe Structure platzieren, analog `PlaceCommand#placeStructure`: Start erzeugen, dann `placeInChunk` je betroffenem Chunk.
- **Server:** keine Client-Klassen beteiligt. `java.awt` kommt in `Trees` nicht vor.

---

### BasiliskMaze

**Rolle:** Singleton `OreSpawnMain.BMaze` (OreSpawnMain.java:5442), keine Registry-ID.

| Aufrufer | Bedingung | Herkunft |
|---|---|---|
| Mining-Populate (`DimensionID2`) | `recently_placed == 0` und `nextInt(95) == 1`, dann `nextInt(7) == 0` → 1/665 je Chunk vor der Höhenprüfung. Nur wenn der 1/95-Wurf scheitert, läuft `addGenericDungeon`; andere Werte von `nextInt(7)` wählen andere Strukturen. | OreSpawnWorld.java:65-91 |
| `addBasiliskMaze` | 6x6-Raster X/Z = Chunk+0,3,…,15. Je Spalte von Y 128 abwärts bis >30: erster Nicht-Luft-Block mit Luft darüber; gemerkt wird die **niedrigste** Oberfläche. Liegt sie über 40: `buildBasiliskMaze(lowestX, lowestY-2, lowestZ)`, `recently_placed = 50`. | OreSpawnWorld.java:2754-2785 |
| Block `dungeonspawner` | `nextInt(50) == 23` → 1/50, an der Blockposition | DungeonSpawnerBlock.java:116-118 |

**Ursprung:** `depth` = 20-29 (BasiliskMaze.java:27). Die Halle hat den Ursprung **O = (x+3, y-depth-4, z-20)**. Aufrufreihenfolge (BasiliskMaze.java:28-32): `clearArea(O)` → `makeMaze(O.x, O.y+1, O.z, 10, 10, 3, b=0)` → `openMaze` → `buildCastle(O)` → `makeEntrance(x, y, z, depth)`.

#### Bauteile (alle über `setBlockFast` Flag 2; Truhen ebenfalls)

| Teil | Methode / Zeilen | Lage relativ zu O | Maße x*y*z | Blöcke |
|---|---|---|---|---|
| Aushub Irrgarten | `clearArea` BasiliskMaze.java:272-282 | x 0..29, y 0..4, z 0..29 | 30*5*30 | Luft |
| Aushub Halle | dto. | x 30..59, y 0..6, z 0..29 | 30*7*30 | Luft |
| Aushub Vorkammer | BasiliskMaze.java:283-289 | x -4..0, y 0..5, z 0..29 | 5*6*30 | Luft |
| Irrgarten | `makeMaze` BasiliskMaze.java:35-105 | x 0..29, y 1..3, z 0..29 | 30*3*30 | `minecraft:obsidian` |
| Eingang | `openMaze` BasiliskMaze.java:293-301 | x 0 an der ersten Z-Zeile mit Luft bei x 1, 3 hoch. Weil Zelle (0,0) bei z 0 immer Randwand und bei (1,1) immer Gang hat, liegt der Eingang immer bei z 1 (berechnet). | 1*3*1 | Luft |
| Ausgang | BasiliskMaze.java:302-310 | x 29 an der letzten Z-Zeile mit Luft bei x 28 → immer z 28 (berechnet) | 1*3*1 | Luft |
| Boden | `buildCastle` BasiliskMaze.java:314-318 | x 0..59, y 0, z 0..29 | 60*1*30 | obsidian |
| Lava | BasiliskMaze.java:319-321 | 80 Zufallsfelder x 1..28, z 1..28, y 0 (Duplikate möglich) | – | `minecraft:lava` |
| Teleportfallen | BasiliskMaze.java:322-324 | 20 Zufallsfelder x 31..58, z 1..28, y 0 | – | `orespawn:blockteleport`: versetzt einen Spieler beim Betreten um ±16±7 in X und Z, Y ±4, bis 1000 Versuche (RTPBlock.java:20-57) |
| Decke Irrgarten | BasiliskMaze.java:325-329 | x 0..29, y 4 | 30*1*30 | `minecraft:bedrock` |
| Decke Halle | BasiliskMaze.java:330-334 | x 30..59, y 6 | 30*1*30 | bedrock |
| Rückwand | BasiliskMaze.java:335-341 | x 59 obsidian, x 60/61 bedrock; y 1..5, z 0..29 | 3*5*30 | obsidian, bedrock |
| Seitenwand N | BasiliskMaze.java:342-348 | z 0 obsidian, z -1/-2 bedrock; x 30..59, y 1..5 | 30*5*3 | obsidian, bedrock |
| Seitenwand S | BasiliskMaze.java:349-355 | z 29 obsidian, z 30/31 bedrock; x 30..59, y 1..5 | 30*5*3 | obsidian, bedrock |
| Sturz | BasiliskMaze.java:356-358 | x 30, y 5, z 0..29 | 1*1*30 | obsidian |
| Vorkammer | BasiliskMaze.java:359-395 | Boden x -4..-1 y 0; Decke x -4..-1 y 5 | 4*6*30 | `minecraft:sandstone` / obsidian |
| | | Westwand x -5, y 1..4; Stirnwände z -1 und z 30 (x -4..0) | | `minecraft:iron_ore` |
| | | Säulen x -4 bei z 0/15/29, y 1..4 | | sandstone |
| | | Fackeln (x -3, y 3, z 0/15/29) | | `orespawn:extremetorch` |
| Hallenfackeln | BasiliskMaze.java:396-398 | (x 30, y 4, z 2/15/27) | – | `minecraft:redstone_torch`, frei schwebend |
| Schatz | BasiliskMaze.java:399-407 | 2-4 Truhen bei (x 58, y 1, z 2+2k); `minecraft:torch` darüber bei y 4, frei schwebend | – | chest, torch |
| Wächter | BasiliskMaze.java:408-423 | 3 **Entities** "Basilisk" bei (x 45/46/47, y 1.01, z 15), zufälliger Yaw, `enablePersistence`, `playLivingSound` (BasiliskMaze.java:260-269) | – | kein Spawner |
| Pyramide | `makeEntrance` BasiliskMaze.java:429-436 | relativ zu (x,y,z): 9 hohle Ringe j=8..0 auf Höhe y+8-j, X/Z von -j bis +j+3 | 20*9*20 | sandstone, nur Außenkante, Inneres unberührt |
| Wendelschacht | BasiliskMaze.java:438-471 | y+8 abwärts bis y-depth+1: Bedrock-Ring 4x4 (x..x+3, z..z+3), Kern 2x2 Luft, pro Ebene ein Obsidian-Tritt im Umlauf (x+1,z+1) → (x+2,z+1) → (x+2,z+2) → (x+1,z+2) | 4*(depth+8)*4 | bedrock, obsidian |

**Aus den Koordinaten berechnet:**
- Der Schachtkern (x+1..x+2, z+1..z+2) liegt in der Vorkammer (Welt-x x-1..x+2, Welt-z z-20..z+9).
- Die letzte Schachtebene y-depth+1 ist genau die Vorkammerdecke O.y+5. `makeEntrance` läuft zuletzt und bricht dort durch; bis zum Boden sind es 4 Blöcke Fall.
- Die Pyramide beginnt 2 Blöcke unter der gemessenen Oberfläche (Aufruf mit `lowestY-2`).
- Weg: Schacht → Vorkammer → Eingang Irrgarten bei z 1 → Ausgang bei x 29 / z 28 → Halle mit Basilisken, Fallen und Truhen an der Rückwand.
- **Gesamtmaße:** X x-8..x+64 = **73**, Z z-22..z+11 = **34**, Y y-depth-4..y+8 = **33-42** hoch (bis 5x3 Chunks).

**Irrgarten-Algorithmus** (`makeMaze`, identisch in `CrystalMaze`):
- Randomisierter Prim-Algorithmus auf dem Zellraster. Wandbits 1/2/4/8 = oben/rechts/unten/links, Randbits 16/32/64/128 (BasiliskMaze.java:43-68).
- Frontliste über `java.util.Vector`, `java.awt.Point` (BasiliskMaze.java:69-86, 190-247).
- Zufall aus **`Math.random()`** (BasiliskMaze.java:249-258), nicht aus Welt-Seed oder `world.rand` → Layout nicht reproduzierbar.
- `drawSide` zeichnet jede Wand 3 hoch, links bei Offset 0, rechts bei Offset `cellsize-1`, beschnitten auf `cellsize*grid` (BasiliskMaze.java:88-142).
- Bei Zellgröße 3: Gänge 1 breit und 3 hoch, Wände zwischen Nachbarzellen 2 dick (berechnet).

#### Truhe `chestContentsList`

31 Einträge, Gewichtssumme 495, 5-10 Züge je Truhe (BasiliskMaze.java:23, 405).

| Item | Menge | Gewicht | % je Zug |
|---|---|---|---|
| `minecraft:ender_pearl` | 3-6 | 15 | 3,0 |
| `minecraft:diamond` | 15-25 | 20 | 4,0 |
| `minecraft:blaze_rod` | 4-12 | 15 | 3,0 |
| `orespawn:cageempty` | 3-10 | 20 | 4,0 |
| `orespawn:cagegirlfriend` | 2-4 | 15 | 3,0 |
| `minecraft:iron_ingot` | 2-20 | 20 | 4,0 |
| `minecraft:gold_ingot` | 4-16 | 20 | 4,0 |
| `orespawn:ingoturanium` | 2-8 | 20 | 4,0 |
| `orespawn:ingottitanium` | 2-6 | 20 | 4,0 |
| `orespawn:sunfish` | 2-8 | 20 | 4,0 |
| `orespawn:firefish` | 3-8 | 20 | 4,0 |
| `orespawn:lavaeel` | 5-24 | 20 | 4,0 |
| `orespawn:corndog_cooked` | 6-12 | 20 | 4,0 |
| `minecraft:diamond_pickaxe`, `minecraft:diamond_sword` | je 1 | je 15 | je 3,0 |
| `orespawn:ultimatepickaxe`, `ultimatesword`, `ultimatefishingrod`, `ultimatebow` | je 1 | je 15 | je 3,0 |
| `minecraft:diamond_chestplate/helmet/leggings/boots` | je 1 | je 15 | je 3,0 |
| `orespawn:ultimate_chest/leggings/helmet/boots` | je 1 | je 15 | je 3,0 |
| `orespawn:ruby` | 1 | 5 | 1,0 |
| `orespawn:thunderstaff` | 1 | 5 | 1,0 |
| `orespawn:magicapple` | 1 | 15 | 3,0 |
| `minecraft:golden_apple` | 2-4 | 15 | 3,0 |

**Research:** 02-dimensions-worldgen.md:398 nennt eine „Burg mit Basilisk-Spawner". Der Code setzt keinen Spawner, sondern spawnt drei persistente Basilisk-Entities (BasiliskMaze.java:408-423).

#### Portierung 1.21.1

- **Structure mit mehreren StructurePieces.** 73x42x34 liegt weit über 3x3 Chunks; jedes Piece schreibt in `postProcess` nur in seine `BoundingBox`.

  | Piece | Inhalt |
  |---|---|
  | `EntrancePiece` | Pyramide + Schacht, Höhe mit `depth` |
  | `AntechamberPiece` | Vorkammer |
  | `MazePiece` | 30x5x30; Zellbits im Konstruktor aus dem Structure-`RandomSource` erzeugen und in NBT speichern, damit alle Chunks dasselbe Layout sehen |
  | `HallPiece` | Halle, Fallen, Truhen, Basilisken |

- **Zufall:** `Math.random()` durch den Structure-Random ersetzen. Das ist eine Abweichung, aber zwingend, sonst zerschneiden Chunk-Grenzen die Wände. `java.awt.Point` durch ein int-Paar ersetzen.
- **Placement:** StructureSet in der Mining Dimension. 1/95 × 1/7 lässt sich über `frequency` bzw. Java in `findGenerationPoint` abbilden. Die Auswahl gegen die sechs anderen Mining-Strukturen braucht eine gemeinsame Auswahl im Code oder getrennte Sets mit gleicher Rate (Abweichung). `recently_placed` nur näherungsweise über `exclusion_zone`. Die Oberflächensuche im 6x6-Raster (niedrigste über 40) über `ChunkGenerator#getBaseHeight`.
- **Basilisken:** in `HallPiece#postProcess` über `EntityType#create(level.getLevel())`, `setPersistenceRequired()`, `addFreshEntityWithPassengers`; `playLivingSound` im Worldgen weglassen. HP 200 < 1024, keine Virtual-Health-Frage.
- **Fackeln:** Standfackeln ohne Unterlage (y 4 über den Truhen, Redstone-Fackeln bei x 30) fallen in 1.21.1 beim ersten Nachbar-Update ab. Entweder Wandfackeln (Abweichung) oder Ausnahme hinnehmen.
- **Lava:** fließt nach dem Setzen erst bei einem Fluid-Tick; ob sie wie im Original zunächst steht, entscheidet `markPosForPostprocessing`.
- **Loot:** `orespawn:chests/basilisk_maze`, `rolls` uniform 5-10.
- **Random-Dungeon-Block:** dieselbe Structure zur Laufzeit platzieren (siehe Trees).

---

### CrystalMaze

**Rolle:** `ChunkProviderOreSpawn5.provideChunk` legt für **jeden** Chunk der Crystal Dimension ein neues `CrystalMaze` an und ruft `buildCrystalMaze(world, chunkX*16, 25, chunkZ*16, chunk)` (ChunkProviderOreSpawn5.java:211-212).
- Der Aufruf liegt nach Rauschterrain und Oberfläche und vor `generateCrystals`, `addCrystalTrees`, `generateCrystalOres`, Blumen, Reis und Quinoa (ChunkProviderOreSpawn5.java:208-218). Alles davor Populate.
- Geschrieben wird nur über `setBlockIDWithMetadataInChunk`, also streng chunk-lokal.
- **Terrain an der Stelle:** Dichte > 0 → `crystalstone`, sonst unter Y 63 Wasser (ChunkProviderOreSpawn5.java:72, 107-115). Bedrock nur Y 0..0+nextInt(5) (ChunkProviderOreSpawn5.java:156-157). Das Labyrinth stanzt Y 24..28 aus, egal ob Gestein oder Wasser darum liegt.

#### Aufbau je Chunk (16*5*16, Welt-Y 24..28)

| Schritt | Methode / Zeilen | Wirkung | Blöcke |
|---|---|---|---|
| 1 | `buildCrystalMaze` CrystalMaze.java:18-24 | 16x3x16 auf Y 25..27 räumen | Luft |
| 2 | `makeMaze(…, 4, 4, 4, b=1)` CrystalMaze.java:25, 60-130 | Prim-Irrgarten 4x4 Zellen, Zellgröße 4, Wände 3 hoch (Y 25..27). `b=1` → nur Bedrock (CrystalMaze.java:133-136). Gänge 2 breit, Wände 2 dick (berechnet). | `minecraft:bedrock` |
| 3 | `openCrystalMaze` CrystalMaze.java:30-43 | Randspalten/-zeilen x 0, z 0, x 15, z 15 auf Y 25..27 räumen. Mit dem Nachbarchunk entsteht an jeder Chunk-Grenze ein 2 breiter Gang, das Netz ist also dimensionsweit verbunden. | Luft |
| 4 | CrystalMaze.java:44-49 | Boden Y 24 und Decke Y 28, je 16x16 | bedrock |
| 5 | CrystalMaze.java:50-57 | 4 Zufallsfelder in der Decke (Y 28), 1 im Boden (Y 24), aus `world.rand` | `orespawn:crystalstone` (Kyanite) |

- Keine Spawner, Truhen oder Entities.
- `clearArea` (CrystalMaze.java:285-304) ist eine unbenutzte Kopie aus `BasiliskMaze`.
- Layout aus `Math.random()` (CrystalMaze.java:274-283), Kyanite-Felder aus `world.rand` statt dem chunk-geseedeten `this.rand` (ChunkProviderOreSpawn5.java:205) → nicht reproduzierbar.
- **Research:** 02-dimensions-worldgen.md:269 nennt „Wände aus Obsidian/Bedrock"; der Code übergibt `b=1`, also nur Bedrock (CrystalMaze.java:25, 133-136).

#### Portierung 1.21.1

- **Java-ChunkGenerator der Crystal Dimension.** Nach dem Rauschterrain und der Oberfläche (`buildSurface`) in den `ChunkAccess` schreiben, vor der Feature-Stufe; das entspricht der Reihenfolge `provideChunk` → Populate. **Kein Chunk-Grenzen-Risiko**, da exakt ein Chunk.
- Zufall aus Seed + `ChunkPos` ableiten (Abweichung, sonst bricht die Reproduzierbarkeit bei Neugenerierung). `java.awt.Point`/`Vector` durch int-Arrays ersetzen.
- `dimension_type` mit `min_y` 0, damit Y 24..28 1:1 bleibt.
- Die Irrgarten-Logik mit `BasiliskMaze` in eine gemeinsame Hilfsklasse ziehen (Parameter Gitter, Zellgröße, Block).

---

### NightmareDungeon

**Rolle:** **keine, toter Code.**
- Im Quelltext kommt `NightmareDungeon` nur in der eigenen Datei vor (Grep über `src-20.2`). Im Jar-Dump steht die Klasse nur in `reference/jar/classes.txt`, ohne Aufruf.
- `OreSpawnMain` legt nur `BMaze`, `RubyDungeon`, `MyDungeon` (= `GenericDungeon`) und `OreSpawnTrees` an (OreSpawnMain.java:5442-5445, calls_OreSpawnMain.txt:9602-9608).
- `MyDungeon.makeDungeon` (OreSpawnWorld.java:2113, 2597; DungeonSpawnerBlock.java:111) ist **`GenericDungeon.makeDungeon`**: ein 12x6x12-Raum (GenericDungeon.java:132-134) mit `nextInt(12)`-Spawnerwahl, u. a. Scorpion, Alien, Cryolophosaurus, WTF?, Kyuubi, Bee, Cloud Shark, Lurking Terror, Terrible Terror, Rotator, Rat, Dungeon Beast (GenericDungeon.java:173-209).
- **Research:** 02-dimensions-worldgen.md:483-488 führt den Aufrufer als offen; der Code klärt das: kein Aufrufer im Build 20.3.

**Was `makeDungeon(x,y,z)` bauen würde** (NightmareDungeon.java:28-87), 25*12*25. Alle Blöcke über `FastSetBlock` = `setBlockFast` mit **Flag 3**, also inklusive Nachbar-Updates (NightmareDungeon.java:89-91).

| Teil | Lage | Blöcke |
|---|---|---|
| Aushub | 25x12x25 | Luft |
| Boden | y, 25x25 | `orespawn:blockteleport` |
| Decke | y+11, 25x25 | bedrock |
| Wände | x 0/24 und z 0/24, volle Höhe; überschreiben die Ränder von Boden und Decke | je Block 1/2 bedrock, sonst obsidian (NightmareDungeon.java:19-26) |
| Spawner | (x+12, y+1, z+12) | 1/2 "Emperor Scorpion", sonst "Nightmare" (NightmareDungeon.java:66-75) |
| Truhen | (x+13, y+1, z+13) und (x+11, y+1, z+11) | je 4-10 Züge (NightmareDungeon.java:76-86) |

#### Truhe

31 Einträge, Gewichtssumme 640 (NightmareDungeon.java:16).

| Item | Menge | Gewicht je | % je Zug |
|---|---|---|---|
| `orespawn:cageempty` | 3-10 | 20 | 3,1 |
| `experience_chest/leggings/helmet/boots`, `experiencesword` | 1 | 25 | 3,9 |
| `ultimate_chest/leggings/helmet/boots` | 1 | 25 | 3,9 |
| `ultimatesword`, `ultimatepickaxe`, `ultimateshovel`, `ultimatehoe`, `ultimateaxe`, `ultimatebow` | 1 | 25 | 3,9 |
| `berthasmall`, `slicesmall` | 1 | 25 | 3,9 |
| `amethyst` | 2-8 | 15 | 2,3 |
| `cookedbacon`, `buttercandy` | 6-12 | 20 | 3,1 |
| `amethystpickaxe/shovel/hoe/axe/sword` | 1 | 15 | 2,3 |
| `amethyst_chest/leggings/helmet/boots` | 1 | 15 | 2,3 |
| `thunderstaff` | 1 | 5 | 0,8 |

#### Portierung 1.21.1

- Für einen 1:1-Port **nicht erforderlich**, weil kein Spieler die Struktur im Original erreicht. Empfehlung: nicht registrieren und das als bewusste Lücke vermerken.
- Soll sie trotzdem existieren (Debug-Befehl): **Feature**. Ursprung Chunk+0..7 → +24 höchstens bis +31, liegt im 3x3-Fenster.
- `orespawn:chests/nightmare_dungeon` nur anlegen, wenn die Struktur registriert wird.

---

### RubyBirdDungeon

**Rolle:** Singleton `OreSpawnMain.RubyDungeon` (OreSpawnMain.java:5443), keine Registry-ID.

| Aufrufer | Dimension | Bedingung | Herkunft |
|---|---|---|---|
| `addRubyDungeon` | Utopia, jeder Chunk nach der Baumphase | 1/15 | OreSpawnWorld.java:36-37, 2084 |
| | | bis 8 Versuche an X/Z = Chunk+0..7; Y 50→6: der erste `minecraft:lava`-Block wird Ursprung | OreSpawnWorld.java:2087-2095 |
| | | `recently_placed` wird weder geprüft noch gesetzt | OreSpawnWorld.java:2083-2098 |
| | | ohne Treffer → `addGenericDungeon` | OreSpawnWorld.java:38-40 |
| `addD4RubyDungeon` | Islands (`DimensionID4`) | `recently_placed == 0` und `nextInt(100) == 0` und `D4BigSpaceCheck`, dann `nextInt(19) == 11` | OreSpawnWorld.java:120-142 |
| | | bei `LessLag != 0` zusätzlich 1/2 | OreSpawnWorld.java:2293-2295 |
| | | X/Z = Chunk+0..7 (ein Versuch), Y 20→5: der erste `grass`-Block wird Ursprung; `recently_placed = 50` | OreSpawnWorld.java:2296-2306 |
| `DungeonSpawnerBlock` | überall | `nextInt(50) == 22` → 1/50 | DungeonSpawnerBlock.java:113-115 |

**Aufbau** (`makeDungeon(x,y,z)`, RubyBirdDungeon.java:31-80), **10*5*10**. Blöcke über `setBlockFast` Flag 2 (RubyBirdDungeon.java:82-84); Spawner und Truhe über `world.setBlock`.

| Teil | Lage | Blöcke |
|---|---|---|
| Aushub | 10x5x10 ab Ursprung | Luft |
| Boden | y, 10x10 | `minecraft:mossy_cobblestone` |
| Decke | y+4, 10x10 | `setThisBlock`: 1/20 `orespawn:oreruby`, sonst 1/2 mossy_cobblestone, sonst `minecraft:cobblestone` → 5 % / 47,5 % / 47,5 % (RubyBirdDungeon.java:19-29) |
| Wände | x 0/9 und z 0/9, volle Höhe; überschreiben die Ränder von Boden und Decke | wie Decke |
| Spawner | (x+5, y+1, z+5) | "Ruby Bird" (RubyBirdDungeon.java:69-73) |
| Truhe | (x+5, y+1, z+1) | 4-10 Züge (RubyBirdDungeon.java:74-79) |

In Utopia liegt der Ursprung in einem Lavablock. Ob umliegende Lava in den Raum fließt, lässt sich aus dem Code nicht beantworten: offen, Flag 2 löst keine Nachbar-Updates aus.

**Research:**
- 01-mobs.md:956/1156 hält einen eigenen Mob „Ruby Bird" für unklar. Der Code registriert `RubyBird` als "Ruby Bird" (OreSpawnMain.java:3499-3503; manifest `ruby_bird`, Modell `ModelCockateil`).
- Die Website sagt „only in the Utopia Dimension". Der Code platziert den Dungeon auch in Islands und über den Random-Dungeon-Block (so bereits in 02-dimensions-worldgen.md:385).

#### Truhe

14 Einträge, Gewichtssumme 215 (RubyBirdDungeon.java:16).

| Item | Menge | Gewicht je | % je Zug |
|---|---|---|---|
| `orespawn:cageempty` | 3-10 | 20 | 9,3 |
| `orespawn:ruby` | 2-8 | 15 | 7,0 |
| `orespawn:cookedbacon` | 6-12 | 20 | 9,3 |
| `orespawn:buttercandy` | 6-12 | 20 | 9,3 |
| `rubypickaxe`, `rubyshovel`, `rubyhoe`, `rubyaxe`, `rubysword` | 1 | 15 | 7,0 |
| `ruby_chest`, `ruby_leggings`, `ruby_helmet`, `ruby_boots` | 1 | 15 | 7,0 |
| `thunderstaff` | 1 | 5 | 2,3 |

#### Portierung 1.21.1

- **Feature** (`Feature<NoneFeatureConfiguration>`, Java). Ursprung Chunk+0..7, Ausdehnung +9 → höchstens +16, sicher im 3x3-Fenster. Stufe `UNDERGROUND_STRUCTURES`.
- **Utopia:** `placed_feature` mit `rarity_filter` 15; die Lavasuche (8 Positionen, Y 50→6) in `place()`. Das Entweder-oder mit Generic Dungeon kennt datapack-Placement nicht → ein gemeinsames Java-Feature, das bei Fehlschlag den Generic Dungeon versucht.
- **Islands:** Aufruf aus dem Java-Auswahlfeature der D4-Strukturen (1/100 → `nextInt(19)`), Grassuche Y 20→5.
- **Spawner:** `SpawnerBlockEntity#setEntityId(orespawn:ruby_bird)`.
- **Loot:** `orespawn:chests/ruby_bird_dungeon`, `rolls` uniform 4-10.
- **Random-Dungeon-Block:** `Feature#place` direkt mit `ServerLevel` aufrufen.
