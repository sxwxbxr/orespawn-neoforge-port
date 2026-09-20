# OreSpawn 1.7.10: Dimensionen, Weltgenerierung, Strukturen, Erze, Blöcke, Pflanzen

Rechercheschnitt 2 von mehreren. Ziel: Grundlage für einen Nachbau in einer aktuellen
Minecraft-Version. Stand der Recherche: 10.09.2026.

## Wie die Angaben belegt sind

Zwei Arten von Belegen, und sie sind unterschiedlich viel wert:

1. **Webquellen** (`[S#]`, Liste am Ende): hauptsächlich der vollständige Spiegel der
   offiziellen Seite orespawn.com (Stand 2021, von TheyCallMeDanger/MeganLorraine), dazu
   Wayback-Kopien der Seite von 2014/2015, das Fan-Wiki orespawnmod.fandom.com, NamuWiki
   und der Crazy-Craft-Wiki. **orespawn.fandom.com (das in der Aufgabe genannte Wiki)
   existiert nicht mehr** (HTTP 404/402); laut dem Nachfolge-Wiki wurde das offizielle
   Wiki gelöscht [S37]. orespawn.wikia.com hat im Wayback nur zwei URLs.
2. **Bytecode des Original-Jars** (`[JAR: Klasse.methode]`):
   `orespawn-1.7.10-20.3.jar` von archive.org [S34], SHA-1
   `d43dbe9a400dc8df06418da3e04d36422b2176d7`, disassembliert mit `javap -c -p`
   (JDK 17). Gelesen wurden nur Konstanten, Feldzugriffe und Kontrollfluss, **nicht**
   dekompiliert. Zahlen aus dem Jar sind damit die Default-Werte der Weltgenerierung. Sie
   sind aber aus Bytecode gelesen und **nicht im Spiel gegengeprüft**. Wo eine Deutung
   nötig war, steht „(Deutung)“ dabei.

Vanilla-Blocknamen im Bytecode stehen als SRG-Namen (z. B. `field_150369_x`). Die
Zuordnung zu Klarnamen (Lapislazuli-Erz usw.) stammt aus der bekannten MCP-1.7.10-Zuordnung
und wurde hier **nicht** gegen die Mappings geprüft.

Versionskonflikt: Die Datei heißt `1.7.10-20.3`, das `mcmod.info` im selben Jar sagt
`"version": "1.7.10-20.2"` [JAR: mcmod.info].

Namensverwechslung: Die GitHub-Repos „OreSpawn“ (DrPlantabyte / MinecraftModDevelopment)
sind eine **andere Mod**, eine JSON-Erzgenerierungsbibliothek ohne Bezug zu
TheyCallMeDanger [S36][S30].

---

## Dimensions

### Gemeinsame Mechanik (alle sechs)

- **Kein Portal.** Man betritt eine Dimension, indem man eine bestimmte Kreatur **mit
  leerer Hand rechtsklickt**. Dieselbe Kreatur bringt einen wieder nach Hause [S1][S17].
- Code-Logik [JAR: EntityAnt/EntityRedAnt/EntityRainbowAnt/EntityUnstableAnt/Termite/
  EntityButterfly.func_70085_c]:
  - Nur wenn die Hand leer ist (bzw. der Stack die Größe 0 hat).
  - Ist der Spieler **in** der Zieldimension dieser Kreatur, geht es nach Dimension 0
    (Oberwelt). Sonst geht es **in** die Zieldimension, egal aus welcher Dimension man
    kommt.
  - Eigener `OreSpawnTeleporter`, `transferPlayerToDimension`.
  - Folge: Eine Rote Ameise in der Chaos-Dimension bringt einen in die Mining-Dimension,
    nicht nach Hause. Die Aussage der Website „Ants work to get home as well“ in der Chaos
    Dimension [S7] stimmt so nur eingeschränkt (Deutung).
- Haustiere und Girlfriends/Boyfriends teleportieren mit, außer sie sitzen [S1][S17].
- **IDs** [JAR: OreSpawnMain, Config-Kategorie `orespawnids`]:
  - `BaseDimensionID` Default **80**. Die Dimensionen bekommen Base+0 … Base+5, also
    **80–85**.
  - `BaseBiomeID` Default **120**: Utopia 120, Islands 121, Crystal 122, Village 123,
    Chaos 124.
  - Die Config-Doku sagt „BaseDimensionID … size 5“ [S12], die Technical-Help-Seite
    „around 80 to 85“ [S15]. Der Code vergibt **6** IDs, „size 5“ ist also veraltet.
  - Der Spiegel nennt als Bereich 10–250 (Dimension) und 50–250 (Biome) [S12].
- **Tag/Nacht:** Alle sechs WorldProvider überschreiben nur `setWorldTime`: Schlafen alle
  Spieler, wird auf den nächsten Morgen gesprungen [JAR: WorldProviderOreSpawn*.
  setWorldTime]. Es gibt also einen normalen Tag-Nacht-Zyklus. Die Website bestätigt
  gefährliche Nächte in Crystal, Village und Danger [S3][S4][S6].
- `func_76567_e` (vermutlich `canRespawnHere`) liefert in allen sechs Providern `true`
  (Deutung der SRG-Methode).

| # | Name (Website) | interner Name `getDimensionName` | Default-ID | Zugang | Biom (eigen) | ChunkProvider |
|---|---|---|---|---|---|---|
| 1 | Utopia Dimension | `Dimension-Utopia` | 80 | Brown Ant (`EntityAnt`) | „Utopia“ (120) | `ChunkProviderOreSpawn` |
| 2 | Mining Dimension | `Dimension-Extreme` | 81 | Red Ant | keins | `ChunkProviderOreSpawn2` |
| 3 | Village Dimension | `Dimension-VillageMania` | 82 | Rainbow Ant | „Villages“ (123) | `ChunkProviderOreSpawn3` |
| 4 | Danger Dimension („Island World“) | `Dimension-Islands` | 83 | Unstable Ant | „Islands“ (121) | `ChunkProviderOreSpawn4` |
| 5 | Crystal Dimension | `Dimension-Crystal` | 84 | Termite (mit leerem Inventar) | „Crystal“ (122) | `ChunkProviderOreSpawn5` |
| 6 | Chaos Dimension | `Dimension-Chaos` | 85 | Butterfly | „Chaos“ (124) | `ChunkProviderOreSpawn6` |

Quellen der Tabelle: [JAR: WorldProviderOreSpawn1–6, OreSpawnMain, Entity-Klassen], [S1].

Fallstrick für den Nachbau: Die internen `makeString`-Namen der ChunkProvider sind
vertauscht. `ChunkProviderOreSpawn2` (Mining) meldet „VillageDimension“,
`ChunkProviderOreSpawn3` (Village) meldet „MiningDimension“. Maßgeblich ist, welcher
WorldProvider welchen ChunkProvider erzeugt [JAR: WorldProviderOreSpawn2/3.func_76555_c,
ChunkProviderOreSpawn2/3.func_73148_d].

Es gibt in 1.7.10 **keine** eigenen King-/Queen-Arena-Dimensionen und keine separate
„Islands Dimension“:

- King und Queen haben Altäre und Bäume als **Strukturen**.
- „Islands“ ist der interne Name der Danger Dimension.

Dimensionen wie Misfit, Doom, Dragon's Lair, Kraken King, Mashup, Zoo und Robot stehen nur
auf der WordPress-Seite von 2021. Sie gehören zu OreSpawn für DangerZone bzw. zum
1.12-Port, siehe *Removed/old content* [S32][JAR: nur 6 WorldProvider-Klassen].

---

### 1. Utopia Dimension (Brown Ant)

- **Zugang:** Brown Ant mit leerer Hand rechtsklicken, zurück genauso [S2][S17]. Nester:
  „Ant Nest“ (`MyAntBlock`, Config `BlackAntEnable`) [JAR].
- **Terrain:** Vanilla-ähnliches Rauschterrain aus Stein und Wasser, Meeresspiegel 63,
  mit Höhlen und Schluchten [JAR: ChunkProviderOreSpawn].
  - Eigenes Biom „Utopia“ (Temperatur/Niederschlag 0.7/0.5) über `WorldChunkManagerHell`,
    also **ein einziges Biom** [JAR: WorldProviderOreSpawn].
  - Die Erzgenerierung läuft einmal pro Chunk über `ChunkOreGenerator` [JAR].
- **Charakter laut Website:** „a beautiful world without hostile mobs“ oberirdisch,
  einige Feinde unterirdisch.
  - Wind Trees zeigen immer in dieselbe Richtung („kein Kompass nötig“).
  - Sky Trees „look like they grow forever and touch the sky“.
  - Außerdem Apple Trees und Ginormous Trees [S2][S31].
  - NamuWiki: sehr lagintensiv wegen der vielen großen Bäume; manche Riesenbäume tragen
    Eisengolems [S30].
- **Mobs laut Website:** friedlich Gazelle, Chipmunk, Kühe; feindlich The King [S2].
  - Laut Jar enthält die Spawnliste des Biom-Grundkonstruktors (Deutung: gilt für Utopia,
    da der Provider keine `set…Creatures`-Methode aufruft): Gazelle, Firefly, Girlfriend,
    Boyfriend, Red/Gold/Enchanted Cow, Butterfly, Luna Moth, Chipmunk, Cockateil (Bird),
    Gold Fish, Whale, Flounder, Coin, Cricket, Frog [JAR: BiomeGenUtopianPlains.<init>].
- **Weltgenerierung** [JAR: OreSpawnWorld.generate, Zweig `DimensionID`]:
  1. `generateSurface`, also dieselben Oberflächen-Extras wie in der Oberwelt: Erdbeeren,
     Mais, Tomaten, Gemüse, Schmetterlings-/Motten-/Moskito-Pflanzen, Ozean-/Plains-
     Minidungeons, Nester, Ameisen, Steine (siehe *Structures*).
  2. `addHugeTree`: pro Chunk Chance 1/50, bis zu 3 Versuche; `LessLag` halbiert bzw.
     viertelt. Erzeugt OMG-Magic-Apple-Riesenbäume, und zwar in drei Formen:
     `MakeBigSquareTree`, `MakeBigCircularTree`, `MakeBigRoundTree`.
     - Eine Square-Variante aus Gold-, Smaragd- und Diamantblöcken ist der **Tree of
       Goodness**; der String „The King“ steht in `MakeBigSquareTree`.
     - Eine Variante aus Obsidian, Ruby- und Amethyst-Block ist der **Queen's Tree**
       („The Queen“).
  3. Falls kein Riesenbaum entstand: `addAppleTrees` (Apple/Cherry/Peach), sonst
     `addOtherTrees` (**Wind Tree**, **Sky Tree**, nur in Utopia).
  4. `addKingAltar`: Chance **1/2000** pro Chunk, dann 50/50 **King's Altar** oder
     **Queen's Altar**. Danach 100 Chunks Sperre (`recently_placed = 100`).
  5. `addVeggies` (inkl. seltenem Duplicator-Tree-Stamm, siehe *Crops & trees*).
  6. `addRubyDungeon` (Ruby Bird Dungeon), sonst `addGenericDungeon`.
- **Bosse:** The King oben auf dem Tree of Goodness, The Queen oben auf dem Queen's Tree
  [S10][S28 The_Tree_of_Goodness, The_Queen_Tree].
  - Laut Website ist der Goodness Tree „1 out of every 100 giant trees“ [S10]. Die exakte
    Code-Wahrscheinlichkeit wurde nicht vollständig ausgewertet (siehe *Open questions*).
- **To-do laut Website:** Magic Apple Trees erklettern, Sky Trees fällen, „rare Golden Ore
  Trees“ finden, Obst ernten, The King besiegen [S2].

### 2. Mining Dimension (Red Ant)

- **Zugang:** Red Ant (feindlich) mit leerer Hand [S5][S17].
- **Terrain:** Vanilla-ähnlich mit Höhlen, Schluchten, Seen, Lavaseen, Vanilla-Dungeons,
  Mineshaft-, Stronghold- und Scattered-Feature-Generatoren [JAR: ChunkProviderOreSpawn2].
  Kein eigenes OreSpawn-Biom; welches Vanilla-Biom per `WorldChunkManagerHell` gesetzt
  wird, ist nicht ermittelt.
- **Erze, der Kern der Dimension:**
  - `ChunkOreGenerator.generateOresInChunk` läuft **dreimal pro Chunk**, mit `LessOre=1`
    nur einmal [JAR: ChunkProviderOreSpawn2.func_73154_d]. Crazy Craft Wiki: „ores (and
    dried spawn eggs) generate 3x as often“ [S29], passt dazu.
  - Jeder Durchlauf enthält alle OreSpawn-Erze, Ancient Dried Eggs mit höherer Rate als in
    der Oberwelt (28+rand(30) statt 28+rand(20)) sowie zusätzlich Diamant-, Smaragd- und
    Golderz plus natürliche Diamant-, Smaragd-, Gold- und Ruby-Blöcke (siehe *Ores*)
    [JAR: ChunkOreGenerator].
  - Ruby: `generateRuby` 3× pro Chunk (1× mit `LessOre`) [JAR: OreSpawnWorld.generate].
  - **Lapislazuli**, nur ohne `LessOre`: 45 Adern Größe 7 und 25 Adern Größe 4, jeweils
    unter Y 50 [JAR: OreSpawnWorld.generate, `field_150369_x` = Lapis-Erz]. Website:
    „especially if you like Lapis Lazuli“ [S31].
- **Mobs** [JAR: ChunkProviderOreSpawn2.func_73155_a]:
  - monster: Alosaurus, T-Rex, Nastysaurus, Pointysaurus, WTF? (`GammaMetroid`), Alien,
    Cave Fisher, Cryolophosaurus, Baby Dragon (`Spyro`).
  - ambient/creature: Velocity Raptor, Dragonfly, Camarasaurus, Baryonyx.
  - Website: feindlich Mothra, Alosaurus, WTF?, Alien, Cave Fisher, Cryolophosaurus, T-Rex;
    friedlich Baby Dragon, Camarasaurus, Velocity Raptor [S5].
- **Strukturen:**
  - Pro Chunk Chance **1/95**, dann gleichverteilt `nextInt(7)`: **Basilisk Maze**,
    **Kyuubi Dungeon**, **Bee Hive**, **Shadow Dungeon**, **Alien/WTF? Dungeon**, **Ender
    Knight Outpost**, **Leonopteryx Nest**.
  - Sonst Generic Dungeon (1/16). Außerdem Lava- und Wasserquellen, Ameisennester (2
    Durchläufe), Moskito-Pflanzen (2 Durchläufe), Gemüse/Duplicator Tree, Steine [JAR:
    OreSpawnWorld.generate].
  - Website nennt: Alien/WTF dungeon, Basilisk Lair, Shadow Dungeons, Ender Knight
    Outpost, Bee Hives, Kyuubi Dungeon [S5]. Leonopteryx-Nest laut Wiki [S28 Leonopteryx].
- **To-do laut Website:** Miner's Dream benutzen, Mothra-Schuppen, Lava-Angeln mit
  Ultimate Fishing Rod, Ancient Dried Eggs sammeln, Baby Dragon großziehen [S5].

### 3. Village Dimension (Rainbow Ant)

- **Zugang:** Rainbow Ant mit leerer Hand [S3][S17].
- **Terrain:** wie die Oberwelt, aber mit `MapGenMoreVillages`, also deutlich mehr
  Vanilla-Dörfer [JAR: ChunkProviderOreSpawn3, MapGenMoreVillages]. Eigenes Biom
  „Villages“.
  - Blogpost vom 26.01.2014: Das eigene Biom wurde nötig, weil Roboter mit Plains als
    Standardbiom nicht mehr spawnten [S26].
  - Erzgenerierung 1× pro Chunk.
- **Mobs** [JAR: BiomeGenUtopianPlains.setVillageCreatures]:
  - Robot1–5 (Bomb-Omb, Robo-Pounder, Robo-Gunner, Robo-Warrior, Robo-Sniper; Zuordnung
    laut Config-Doku [S12]).
  - GiantRobot, Spider Driver, **Godzilla = Mobzilla**.
  - Außerdem Firefly, Girlfriend, Boyfriend, Red/Gold/Enchanted Cow, Butterfly, Luna Moth,
    Chipmunk, Cockateil, T-Shirt, Coin, `BandP` (unbekannt).
  - Website: feindlich Mobzilla, Robo Warrior, Robo Gunner, Robo Pounder, Bomb-omb;
    friedlich Villagers. „Utter chaos at night“ [S3]; Roboter erscheinen nach
    Sonnenuntergang [S31].
- **Weltgenerierung** [JAR: OreSpawnWorld.generate, Zweig `DimensionID3`]:
  Moskito-Pflanzen, Ameisennester (4 Durchläufe), Apple/Cherry/Peach Trees, Generic
  Dungeon, **Damsel in Distress**, **Spider Hangout** (Spawner Spider Driver + Robot
  Spider), **Red Ant Hangout** (Spawner Robot Red Ant + Red Ant Nests).
  - Mais und Tomaten werden auch hier platziert (`addCorn`/`addTomatoes` prüfen
    `DimensionID3`; Bedingungsdetails nicht ausgewertet).
- Website: Cherry and Peach trees [S3]. NamuWiki: schwebende Coins und T-Shirts, die man
  für nützliche Items schlagen kann [S30].

### 4. Danger Dimension (Unstable Ant), intern „Islands“

- **Zugang:** Unstable Ant mit leerer Hand [S6][S17].
  - Die Items-Seite nennt sie „Danger Dimension (Island World)“ [S13], die
    Technical-Help-Seite „Island Dimension“ [S15].
- **Terrain:** eigener, sehr einfacher ChunkProvider. Grundschicht direkt in
  `ExtendedBlockStorage` geschrieben aus Bedrock, Erde und Gras, dazu „Scraggly Trees“
  (Vanilla-Stamm + Apple Leaves) [JAR: ChunkProviderOreSpawn4].
  - Deutung: ein flacher Boden. Die genaue Schichthöhe wurde nicht ausgelesen.
  - Kein `ChunkOreGenerator`. Erze stecken in den schwebenden Inseln.
- **Schwebende Inseln (Kernmechanik):**
  - `addIslands` setzt auf Gras **Island Blocks**. Ein Island Block erzeugt beim Tick eine
    Insel-Entity [JAR: IslandBlock.updateTick, Island, IslandToo]:
    - `Island` (runde Insel; Blöcke u. a. Lava, Myzel, Pilze, Bedrock, Diamanterz,
      Endstein), auf ihr **Triffids**.
    - `IslandToo` (Grasinsel mit Wasser, rosa/blauen Blumen, Bäumen). Im Inneren Stein,
      Kohle-, Eisen-, Smaragd-, Titan-, Uran-, Ruby-, Amethyst- und Golderz,
      Ender-Pearl- und Eye-of-Ender-Block, Amethyst-, Ruby-, Uran-, Titan-, Gold- und
      Diamantblock [JAR: IslandToo.mySetBlock]. NamuWiki: „minerals distributed like
      skyblocks“ [S30].
  - Inseln **bewegen sich**; die NBT-Felder heißen `Ispeed`/`Idir` [JAR]. Größe und
    Tempo über Config `IslandSizeFactor` und `IslandSpeedFactor` (Default 2, Bereich 1–5)
    [S12][JAR].
  - Technical Help: Lag durch Insel-Spawns; man kann die Insel-Entities töten („the round
    Islands … then the Triffids won't spawn“) [S15].
  - Island Blocks kann man vor dem Spawnen ausgraben und woanders pflanzen [S13].
- **Blumen:** Pink Flower (nachts schwarz) und Blue Flower (nachts „Dead Flower“)
  [S10][JAR: MyBlockFlower.checkFlowerChange].
- **Strukturen** [JAR: OreSpawnWorld.generate, Zweig `DimensionID4`]:
  - Pro Chunk Chance **1/100** plus Platzprüfung, dann `nextInt(19)`:
    - 0–2 **Challenge-Burg**: 50/50 King-Version (`makeEnormousCastle`) oder
      Queen-Version (`makeEnormousCastleQ`) [JAR: addD4Castle].
    - 3–6 Generic Dungeon
    - 7 Ender Castle · 8 Inca Pyramid · 9 Robot Lab · 10 Mini Dungeon · 11 Ruby Bird
      Dungeon · 12 Cephadrome Altar · 13 Greenhouse · 14 Nightmare Rookery · 15 Stinky
      House · 16 White House · 17 Pumpkin · 18 Rainbow Dungeon.
  - **Cloud Shark Dungeon** zusätzlich mit Chance 1/300 pro Chunk.
  - Außerdem Unstable Ant Nests, Inseln, Steine.
- **Mobs** [JAR: BiomeGenUtopianPlains.setIslandCreatures]: Butterfly, Cockateil, Luna
  Moth, Firefly, Dragon, Stinky, Cliff Racer, Cloud Shark, Gold Fish, Creeping Horror,
  Terrible Terror, Lurking Terror, Nightmare (`PitchBlack`), Leaf Monster, Ender Reaper,
  Hercules Beetle.
  - Website: feindlich Nightmare, Terrible Terror, Creeping Horror, Triffid, Lurking
    Terror, Vampire Butterfly, Cloud Shark; friedlich Dragon, Gold Fish.
  - Website-Besonderheiten: „Death by falling is common“, Inseln bewegen sich, Szenerie
    wechselt nachts [S6]. Criminals in „quartz prison“-Strukturen (White House) [S28
    Criminal].
- **Website-Dungeons:** Mini Dungeons, The Challenge Dungeons, Cloud Shark Dungeon [S6].
  Laut Dungeon-Seite außerdem Cephadrome Altars, Nightmare Rookeries und Queen's Challenge
  Dungeon [S8].

### 5. Crystal Dimension (Termite)

- **Zugang:** Termite (feindlich) rechtsklicken [JAR: Termite.func_70085_c].
  - Die Hand muss leer sein (sonst Chat „Empty your hand!“).
  - Das **Hauptinventar** muss komplett leer sein („Empty your inventory!“).
  - Es darf **keine Rüstung** getragen werden („Take off your armor!“).
  - Zurück: Termite rechtsklicken, ohne Inventarprüfung.
  - Website: „EMPTY YOUR INVENTORY BEFORE ENTERING“, „Use your girlfriend to transport one
    item in“ [S4].
- **Terrain** [JAR: ChunkProviderOreSpawn5]: Rauschterrain, aber mit **Kyanite**
  (`CrystalStone`) statt Stein, **Crystal Grass** als Oberfläche, Wasser, Bedrock.
  - Pro Chunk wird ein **Crystal Maze** gebaut (Wände aus Obsidian/Bedrock) [JAR:
    CrystalMaze]. Das ist das „dungeon maze“ tief unten [S4][S11].
  - Oberirdisch: Crystal Trees (hoch, verzweigt, blau), Crystal Flowers, Rice, Quinoa.
  - Crystal Grass und Kyanite sind nur in dieser Dimension undurchsichtig (Render-Abfrage
    `current_dimension == DimensionID5`) [JAR: CrystalGrass, OreBasicStone].
  - Blogpost 01.01.2014: „may look a little tame … during the day … at night …
    mind-blowing! And deadly!“ [S25].
- **Erze:** siehe *Ores*. Tiger's Eye und Pink Tourmaline sitzen tief, Crystalized
  Rats/Fairies unter Y 25 [JAR].
- **Weltgenerierung** [JAR: OreSpawnWorld.generate, Zweig `DimensionID5`]:
  - **Fairy Tree** oder **Fairy Castle Tree** (Fairy-Spawner, Truhen). Wenn keiner
    entsteht: Crystal Termite Nests und genau eine der Strukturen **Rotator Power
    Station**, **Crystal Urchin Spawner**, **Crystal Haunted House**, **Round Rotator**,
    **Crystal Battle Tower**.
  - Außerdem Irukandji im Wasser, Kristalltruhen und Spawner (Strings „Dungeon Beast“,
    „Rat“), Steine/Kristalle (1/4).
- **Mobs** [JAR: setCrystalCreatures]: Crystal Cow, Fairy, Peacock, Mantis, Rotator,
  Vortex, Urchin, Dungeon Beast, Rat, Butterfly, Cockateil, Luna Moth, Whale, Crab,
  Flounder, Irukandji, Skate, Frog.
  - Website: feindlich Vortex, Skate, Irukandji, Rotator, Dungeon Beast, Crystal Urchin,
    Rat, Mantis; friedlich Fairy, Whale, Peacock, Flounder, Termite, Crystal Cow [S4].
- Kein normaler Stein und kein Holz. Deshalb Crystal Workbench, Crystal Furnace (Kyanite,
  „Crystal Energy“ als Brennstoff), Bett aus Crystal Planks + Peacock Feathers [S13].

### 6. Chaos Dimension (Butterfly)

- **Zugang:** Butterfly mit leerer Hand [S7][JAR: EntityButterfly]. Hinzugefügt mit
  **V19, 03.08.2014** („make a new dimension, toss all the critters into it“) [S20].
  Auf der Seite von 2014 fehlt sie noch (nur 5 Dimensionen) [S19].
- **Terrain:** eigenes Rauschfeld (`initializeNoiseField` mit `Math.cos`) aus Stein,
  Gras, Erde und Wasser bis Y 127 [JAR: ChunkProviderOreSpawn6].
  - **Keine Bedrock-Referenz** im ChunkProvider. Website: „There's nothing underneath. One
    false move and you could end up falling into the void!“ [S7]. NamuWiki: kein Bedrock
    [S30]. Deutung: schwebende Landmassen über dem Void.
  - Erzgenerierung 1× pro Chunk, Scraggly Trees.
  - WordPress-Seite 2021: „Yes of course that is a volcano in the background“ [S32]. Ob das
    für 1.7.10 gilt, ist unklar.
- **Mobs** [JAR: setChaosCreatures], praktisch alle Landmobs inklusive Bossen:
  Butterfly, Luna Moth, Cockateil, Firefly, Cliff Racer, Cloud Shark, Gold Fish, Fairy,
  Baryonyx, Bee, Cassowary, Dragonfly, Peacock, Stink Bug, Ostrich, Chipmunk, Beaver,
  Red/Gold/Enchanted Cow, Vortex, Nightmare, Terrible Terror, Alosaurus, Basilisk,
  Robot1–5, CaterKiller, Cave Fisher, Creeping Horror, Cryolophosaurus, Urchin, Dungeon
  Beast, Emperor Scorpion, Ender Knight, Ender Reaper, Hammerhead, Hercules Beetle, Jumpy
  Bug (`TrooperBug`), Molenoid, Mothra, Brutalfly, Rat, Rotator, Scorpion, Spit Bug,
  Nastysaurus, T-Rex, Leaf Monster, Pointysaurus, Leonopteryx, Mantis, Lurking Terror,
  WTF?.
  - Website: „Almost all the mobs spawn here, with the exception of the water-based
    creatures“ [S7]. NamuWiki: ohne King, Mobzilla, Triffid, Meeresbosse, Worms, Criminal
    [S30]; deckt sich mit der Liste.
- **Weltgenerierung:** Butterfly-/Moth-Pflanzen, Gemüse/Duplicator Tree, Ameisennester
  [JAR: OreSpawnWorld.generate]. Keine eigenen Dungeons gefunden.

---

## Structures

Legende „Wo“: aus `OreSpawnWorld` gelesen [JAR]. Spawner-Mobs: aus den String-Konstanten
der jeweiligen `GenericDungeon.make…`-Methode [JAR]. Beschreibungen und Konzept-Autoren
von der Dungeon-Seite [S8] und den Blogposts [S21–S24].

### Oberwelt

Nur wenn Config `DisableOverworldDungeons = 0`, sonst entfallen alle [JAR:
generateSurface].

- **Ozean/Wasser-Gruppe** (`nextInt(6)`):

  | Struktur | Wo | Spawner / Inhalt |
  |---|---|---|
  | Play Pool | Ocean | Attack Squid, Truhe („only fun until someone accidentally summons a Kraken“) [S8] |
  | Water Dragon Lair | Ocean | Water Dragon, dazu Water-Dragon-Dried-Egg-Blöcke, Eisen-/Lapis-Block, Glowstone |
  | Gold Fish Bowl | Ocean | Gold Fish |
  | Girlfriend/Boyfriend Island | Ocean | Girlfriend, Boyfriend, Gold Fish, Truhe |
  | **Monster Island** | Ocean | Sea Viper, Sea Monster, Truhe. **Nicht auf der Website** |
  | Frog Pond | Plains | Frog, Seerosen |

- **Land-Gruppe** (nacheinander versucht):

  | Struktur | Wo | Spawner / Inhalt |
  |---|---|---|
  | Small Bee Hive / Mantis Hive | Forest, ForestHills, Jungle, JungleHills, Birch Forest (+Hills) | Bee (Schwamm, Bemooster Bruchstein) bzw. Mantis (Gold- und Smaragderz) |
  | Haunted House | Plains, Taiga, Swampland | Rat, Ghost, Ghost Pumpkin Skelly; Truhe u. a. mit Salt Ore |
  | Leaf Monster Dungeon | Plains | Leaf Monster |
  | Spit Bug Lair | Swampland | Spit Bug („a few emeralds and a chest full of loot“ [S8]) |
  | Abandoned Igloo | Ice Plains | Rat, Ghost, Ghost Pumpkin Skelly |
  | Bouncy Castle | Desert | aus **Lava Foam**; Silverfish, Rat, Scorpion |
  | Rubber Ducky Pond | Plains | Rubber Ducky |

### Nether

Keine Strukturen, nur Red Ant Nests, Moskito-Pflanzen, Lava Foam und Ruby Ore [JAR:
generateNether].

### End

[JAR: generateEnd] End-Ameisen (`addEndAnts`), dann `nextInt(4)`:

| Struktur | Spawner / Inhalt |
|---|---|
| Ender Knight Outpost | Ender Knight (Obsidian, Endstein, Bücherregale) |
| Ender Reaper Graveyard | Ender Reaper, Gräber mit Truhen |
| Ender Dragon Hospital | Ender Reaper, Nightmare, Eye-of-Ender-Block |
| Ender Castle | Ender Reaper, Ender Knight, CaveFisher, Dried-Egg-Blöcke von Ender Knight/Reaper/Enderman/Ender Dragon, Eye-of-Ender- und Ender-Pearl-Blöcke, Ender Chest |

- Website: Ender Knight Outposts „in the Mining Dimension and The End“, Ender Castles „best
  place to find Ender Chests“ [S8]. Gerüchteweise eine Red-Ant-Kolonie im End als Rückweg
  [S17].

### Utopia

| Struktur | Inhalt |
|---|---|
| **King's Altar** | Quarz, Gold- und Smaragdblock; Truhe mit **The King Spawn Egg** |
| **Queen's Altar** | Obsidian, Redstone-, Amethyst- und Ruby-Block; Truhe mit **The Queen Spawn Egg**. Website: „beating her is the only way to get the Queen's Armor“ [S8] |
| **Tree of Goodness** („Tree of EPIC PROPORTIONS“) | hohler Goldstamm, Smaragd-Äste, Diamanttreppe; Uran-, Ruby-, Amethyst-, Titan-, Gold-, Diamant-, Redstone- und Smaragdblöcke; Truhen; manchmal Hebel-Fallen; The King oben [S28 The_Tree_of_Goodness][S30] |
| **Queen's Tree** | Ruby-/Amethyst-Blöcke mit Titan-, Uran-, Redstone-„Ornamenten“; The Queen oben [S28 The_Queen_Tree][S10] |
| **Ruby Bird Dungeon** | Ruby Ore, Bemooster Bruchstein; Spawner Ruby Bird; Loot: Ruby-Werkzeuge und -Rüstung, **Thunder Staff**, Bacon, Butter Candy, Critter Cage [JAR: RubyBirdDungeon]. Website: „only in the Utopia Dimension“ [S16]. Code platziert ihn **auch** in der Danger Dimension |
| Generic Dungeon | — |

Altäre: [JAR: GenericDungeon.makeKingAltar/makeQueenAltar,
makekingcenteraltar/makequeencenteraltar].

Beim Queen's Tree liefert der Code nur Obsidian, Ruby-Block, Amethyst-Block (siehe
Utopia-Weltgenerierung); die Ornamente stammen aus dem Wiki.

### Mining Dimension

| Struktur | Inhalt |
|---|---|
| **Basilisk Maze** („Basilisc Lair“) | Irrgarten aus Obsidian/Bedrock, Burg mit Basilisk-Spawner, Lava, Random Teleport Blocks, Extreme Torches. Loot u. a. Ultimate-Rüstung/-Werkzeuge/-Bogen/-Angel, Uran-/Titanbarren, Caged Girlfriend, Lava Eel, Magic Apple, Ruby, Thunder Staff [JAR: BasiliskMaze] |
| **Kyuubi Dungeon** | Sandstein, Netherrack, Lava-Quadrate; Spawner Kyuubi und Blaze |
| **Bee Hive** (groß) | Kohle- und Golderz-Blöcke; Bee |
| **Shadow Dungeon** | Obsidian, Bedrock, Seelensand; Ender Reaper, Nightmare. „viciously difficult … If you're not sporting Big Bertha and Mobzilla armor“ [S8] |
| **Alien/WTF? Dungeon** | Lapis-Erz, Quarz, Obsidian; Alien, WTF?. Wiki: 11×11, 4 hoch [S28 The_WTF?_Beetle] |
| **Ender Knight Outpost** | siehe End |
| **Leonopteryx Nest** | Blätter, Stämme, Planken; Leonopteryx |

### Village Dimension

| Struktur | Inhalt |
|---|---|
| Villages | `MapGenMoreVillages`, mehr Vanilla-Dörfer |
| **Damsel in Distress** | Scorpion, Girlfriend („beautiful girl just waiting to be saved. Plus some pretty good food“ [S8]) |
| **Spider Hangout** | Spider Driver, Robot Spider (nur bei `SpiderDriverEnable`) |
| **Red Ant Hangout** | Robot Red Ant, Red Ant Nests |

Spider Hangout und Red Ant Hangout stehen nicht auf der Website.

### Danger Dimension

- **Challenge Dungeon** (`makeEnormousCastle`) [JAR]:
  - Bau: Stein, Bedrock, Eisengitter, Extreme Torches, oben Netherrack-Feuer.
  - Spawner: Terrible Terror, Emperor Scorpion, Cloud Shark, Lurking Terror, Rotator, Bee,
    Mantis, Mothra, Large Worm.
  - Etagen-Deko: Alosaurus, Nightmare, Large Worm, T. Rex, Basilisk, Hercules Beetle,
    Jumpy Bug, **Hammerhead**, dazu Random Teleport Blocks.
  - Schatztruhe: **The Prince Spawn Egg, Royal Guardian Helmet/Chest/Legs/Boots, Royal
    Guardian Sword** (`fill_chests`).
  - Website: Stufen 1 (leicht) bis 6 (brutal); die höchste erkennt man am Netherrack-Feuer
    oben; Stufe 6 enthält Prince-Ei, Royal-Guardian-Rüstung und -Schwert [S8]. NamuWiki:
    auf Etage 6 Large Worm [S30]. Die Stufenlogik im Code (`buildLevel`) wurde nicht
    ausgewertet.
- **Queen's Challenge Dungeon** (`makeEnormousCastleQ`):
  - Bau: Obsidian, Ruby-Blöcke, Amethyst-Blöcke.
  - Spawner: Lurking Terror, Emperor Scorpion, Rotator, Bee, Mantis, Mothra, Brutalfly,
    Vortex, Large Worm.
  - Etagen: T. Rex, Nightmare, Large Worm, Nastysaurus, Basilisk, Hercules Beetle, Jumpy
    Bug, CaterKiller.
  - Truhe: **The Princess Spawn Egg, Queen-Scale-Rüstung (4 Teile), Royal Guardian Sword**
    [JAR: fill_chestsQ]. Website: Belohnung „Your very own Princess!“ [S8].

| Struktur | Inhalt |
|---|---|
| **Ender Castle** | siehe End |
| **Inca Pyramid** | Stein, Bruchstein; Spawner **Molenoid**; Gräber mit Ghost-Spawnern; Creeper Repellent. Website: „filled with ghosts and gold, and of course, a BIG mob“ [S8] |
| **Robot Lab** | Quarz, Eisenblöcke, Schienen, „Fließband“; Säulen mit Robo-Sniper, Altar mit Robo-Pounder, Schatzraum mit Robo-Warrior |
| **Mini Dungeon** | Eisengitter, Bruchstein, Zaun; Butterfly, Terrible Terror, Lurking Terror („Great dungeon practice for noobs“ [S8]) |
| **Ruby Bird Dungeon** | siehe Utopia |
| **Cephadrome Altar** | Bruchstein, Steinziegel, Endstein, **Extreme Torch auf Eye-of-Ender-Block**, also der Beschwörungsaufbau (siehe *Blocks*) |
| **Greenhouse** | Glas, Eisen, Glowstone; Spawner **Triffid**; voller Pflanzen (Mais, Tomate, Erdbeere, Butterfly-/Moth-Plant, Radieschen, Salat, Blumen, Quinoa, Reis, Karotte, Kartoffel, Pilze u. a.) |
| **Nightmare Rookery** | Stein; Nightmare |
| **Stinky House** | Stink Bug, Stinky |
| **White House** | Quarz, Brunnen, Walkway; innen **Criminal**-Spawner. Wiki: „quarts ‚prison‘ structures“ [S28] |
| **Pumpkin** | Kürbisse, Netherrack-Feuer; Ghost Pumpkin Skelly |
| **Rainbow Dungeon** | gefärbter Ton, Wasser; Cloud Shark |
| **Cloud Shark Dungeon** | Glowstone; Cloud Shark; in der Luft [S8] |

### Crystal Dimension

| Struktur | Inhalt |
|---|---|
| **Crystal Maze** | pro Chunk, unterirdisch |
| **Fairy Tree / Fairy Castle Tree** | Crystal Tree Wood, Fairy-Spawner, Truhen. Die Castle-Variante enthält u. a. Crystal-/Tiger's-Eye-/Pink-Tourmaline-/Kyanite-Werkzeuge, -Barren und Crystal Apples [JAR: Trees.FairyCastleTree] |
| **Rotator Power Station** | Kyanite; Rotator-Spawner; Rotator-Ei, Crystal Energy |
| **Crystal Urchin Spawner** | Kyanite, Pink Tourmaline, Tiger's Eye; Crystal Urchin; Urchin-Ei |
| **Crystal Haunted House** | Crystal Planks/Kyanite; Rat, Ghost, Ghost Pumpkin Skelly; Loot Peacock, Crystal Torch, Crystal Energy, Pink-Tourmaline-Werkzeuge, Kraken Repellent |
| **Round Rotator** („Rotator Dungeon“, V19) | Bedrock, Pink-Tourmaline-Blöcke; Rotator, Dungeon Beast |
| **Crystal Battle Tower** | Kyanite, Pink Tourmaline; Rat, Dungeon Beast, Crystal Urchin, Rotator, **Vortex**. „The higher you go, the better the loot“ [S8]; NamuWiki: Vortex-Spawner ganz oben [S30] |

Die Deutung „Round Rotator = Rotator Dungeon“ stammt aus den Spawnern [S24][JAR].

### Nicht aus der Weltgenerierung

- **Generic Dungeon** (`makeDungeon`): Bemooster Bruchstein, ein Spawner mit Zufallsmob aus
  Scorpion, Alien, Cryolophosaurus, WTF?, Kyuubi, Bee, Cloud Shark, Lurking Terror,
  Terrible Terror, Rotator, Rat, Dungeon Beast; eine Truhe [JAR].
- **Random Dungeon Spawner** (Block/Item): würfelt beim Tick `nextInt(50)` und baut eine
  Struktur [JAR: DungeonSpawnerBlock.updateTick].
  - Gelesene Fälle 0–18: Fairy Tree, Fairy Castle Tree, Challenge-Burg, Rotator Station,
    Bee Hive, Haunted House, Mantis Hive, Kyuubi, Small Bee Hive, Shadow, Alien/WTF, Ender
    Knight, Play Pool, Water Dragon Lair, Cloud Shark, Leaf Monster, Mini, Gold Fish Bowl,
    Ender Reaper Graveyard. Der Rest wurde nicht ausgelesen.
  - Website: „Place it on stone or cobblestone to activate it!“ [S8][S13]. NamuWiki: „can
    only be placed on dirt“ [S30]. **Konflikt**, im Code nicht geprüft.
- **Nightmare Dungeon** (eigene Klasse): Bedrock, Obsidian, Random Teleport Blocks;
  Spawner Emperor Scorpion, Nightmare.
  - Loot: Experience-Rüstung/-Schwert, Ultimate-Rüstung/-Werkzeuge/-Bogen, **Big Bertha,
    Slice**, Amethyst-Ausrüstung, Thunder Staff, Bacon, Butter Candy, Critter Cage [JAR:
    NightmareDungeon].
  - **Aufrufer in der Weltgenerierung nicht gefunden** (offen).
- **Golden Ore Tree**: Wiki beschreibt einen Baum „made completly out of gold ore“ mit 8
  Etagen und je 3 Spawnern (Spit Bug, Sea Viper, Water Dragon, WTF?, Kyuubi, Sea Monster,
  Alien, Leonopteryx) [S28 Golden_Ore_Tree(Structure)].
  - Die Utopia-Seite erwähnt „rare Golden Ore Trees“ [S2]; der Config-Schalter
    `GinormousEmeraldTreeEnable` beschreibt „the rare gold/diamond/emerald OMG Magic Apple
    tree“ [S12].
  - Ein Baum aus Gold**erz** mit Etagen-Spawnern wurde im Jar **nicht gefunden**.
    Möglicherweise ist die Wiki-Seite eine Verwechslung (Lucky-Block-Addon?). Unbelegt.

---

## Ores

### Wie gezählt wird

- Für jede Erzart gibt es `<name>_rate`, `_clumpsize`, `_mindepth`, `_maxdepth` in der
  Config-Kategorie `orespawnores` [S12].
- `get_orestats` klemmt `rate` und `clumpsize` auf ½× bis 2× des Defaults [JAR:
  OreSpawnMain.get_orestats][S12].
- Pro Chunk werden **Versuche** gewürfelt, jeweils mit Y = `rand(128)`. Nur Versuche mit
  `mindepth ≤ Y ≤ maxdepth` setzen eine `WorldGenMinable`-Ader der Größe `clumpsize`. Die
  effektive Aderzahl ist also Versuche × Bereichsanteil (Deutung aus Kontrollfluss).
- `LessOre = 1` teilt die Versuche der Hauptliste durch 3 (Troll-Blöcke durch 2) und
  **streicht** die Zusatz-Diamant-, Smaragd-, Gold- und Block-Adern komplett [JAR].

| Erz (Anzeigename) | Config-Name | Default rate / clump / min–max Y | Versuche pro Chunk (Code) | Wo | Drop / Verhalten | Quelle |
|---|---|---|---|---|---|---|
| Salt Ore | `Salt` | 5 / 12 / **50–128** | rate + rand(9) | Oberwelt, alle `ChunkOreGenerator`-Dimensionen (Utopia, Mining ×3, Village, Chaos) | droppt sich selbst; Schmelzen → Salt (Wiki: 8 Salt). Fügt beim Betreten/Berühren **5 Schaden** zu, nur für bestimmte Entities (Website: tötet Ameisen). Härte 5 | [JAR: OreSalt][S11][S28 Salt_ore] |
| Titanium Ore | `Titanium` | 3 / 4 / **0–20** | rate + rand(9) | wie Salt | Block; Schmelzen → Titanium Nugget, 9 → Ingot [S11][S30]. Härte 15, Resistenz 5; Partikel „reddust“; XP beim Abbau | [JAR: OreTitanium][S11] |
| Uranium Ore | `Uranium` | 3 / 4 / **0–30** | rate + rand(9) | wie Salt | Block; Schmelzen → Uranium Nugget; Härte 10; leuchtet/funkelt | [JAR: OreUranium][S11] |
| Amethyst Ore | `Amethyst` | 2 / 6 / **0–25** | rate + rand(12) | wie Salt | droppt **Amethyst** (Menge mit Fortune-Bonus); Härte 10, Res. 4. Website: „found, rarely, deep underground“ | [JAR: OreAmethyst][S11] |
| Ruby Ore | `Ruby` | 10 / 1 / **0–50** (Code zusätzlich Y > 5) | Oberwelt rate + rand(5); Mining `generateRuby` rate + rand(7), 3× | **nur dort, wo Stein direkt unter (fließender) Lava liegt**; Einzelblöcke | droppt **Ruby**; Härte 10, Res. 4. Website: „only appear directly under a lava pool … the lava lets loose“ | [JAR: OreSpawnWorld.generateOres/generateRuby, OreRuby][S11] |
| Ruby Ore (Nether) | — (fest) | Größe 2, Y 10–117 | 5 + rand(5), mit LessOre /3 | Nether, ersetzt Netherrack | wie oben | [JAR: generateNether] |
| Lava Foam | — (fest) | Größe 6, Y 10–117 | 15 + rand(10), LessOre /3 | Nether, ersetzt Netherrack | Rutschigkeit 1.1; Hitbox leicht verkleinert (Deutung: Abprall); 0.5 Schaden bei Kontakt; Härte 5. Website: „Slipperier than snot on a doorknob“ | [JAR: Lavafoam, generateNether][S11] |
| Red Ant Troll Block | — (fest) | Größe 4, **Y 5–50** | 4 + rand(4), LessOre /2 | Oberwelt + `ChunkOreGenerator` | spawnt beim Abbauen **Red Ants** (Anzahl im Code 15 bzw. rand(6), Formel nicht ausgewertet). Website: „full of red ants“ | [JAR: OreBasicStone, generateOres][S11] |
| Termite Troll Block | — (fest) | Größe 4, Y 5–50 | 4 + rand(4) | wie oben | spawnt **Termites**; „Looks just like an emerald ore block!“ | [JAR][S11] |
| Diamond Ore (Vanilla, zusätzlich) | `Diamond` | 4 / 6 / 0–30 | genau `rate` | Oberwelt + `ChunkOreGenerator`, nur ohne LessOre | Vanilla | [JAR] |
| Block of Diamond (natürlich) | `BlockDiamond` | 2 / 4 / 0–20 | `rate` | wie oben | Vanilla | [JAR] |
| Emerald Ore (zusätzlich) | `Emerald` | 4 / 6 / 0–40 | `rate` | wie oben | Vanilla | [JAR] |
| Block of Emerald (natürlich) | `BlockEmerald` | 2 / 4 / 0–20 | `rate` | wie oben | Vanilla | [JAR] |
| Gold Ore (zusätzlich) | `Gold` | 4 / 8 / 0–40 | `rate` | wie oben | Vanilla | [JAR] |
| Block of Gold (natürlich) | `BlockGold` | 2 / 4 / 0–25 | `rate` | wie oben | Vanilla | [JAR] |
| Ruby Block (natürlich) | `BlockRuby` | 1 / 2 / 0–15 | `rate` | wie oben | Ruby Block | [JAR] |
| Ancient Dried Spawn Eggs (alle) | `SpawnOres` | 28 / 4 / **50–128** | Oberwelt 28 + rand(20), `ChunkOreGenerator` 28 + rand(30) | alle ore-generierenden Dimensionen, Mining ×3 | siehe *Spawn-egg/ore mechanic* | [JAR][S9] |
| Lapis Lazuli Ore (Vanilla) | — (fest) | Größe 7 (45×) und 4 (25×), Y < 50 | fest | **nur Mining Dimension**, nicht mit LessOre | Vanilla | [JAR: generate] |

**Crystal Dimension** (feste Werte, nicht konfigurierbar) [JAR:
ChunkProviderOreSpawn5.generateCrystalOres/addPinkTourmaline/addTigersEye]:

| Erz | Menge / Größe / Y | Drop / Verhalten | Quelle |
|---|---|---|---|
| Kyanite (`CrystalStone`) | Grundgestein; zusätzlich Adern Größe 4 | „Similar to stone … Kyanite tools and weapons, but not armor … Crystal Furnace“ | [S11] |
| Crystal Energy (`CrystalCoal`) | 3 + rand(8) Adern, Größe 6, beliebige Y | leuchtet; beim Abbau Explosion 1.5 (mit mobGriefing-Abfrage, Wahrscheinlichkeit nicht ausgewertet). Website (ohne Namen, Bild): „Similar to coal, but lasts much longer … tends to explode and catch things on fire“ | [JAR: OreCrystal][S11] |
| Crystalized Rats (`CrystalRat`) | 15 + rand(20) Adern, Größe 6, **Y < 25** | spawnt beim Abbau Rats (Zufall 1/10 im Code, Deutung). Material für das Rat Sword | [JAR: OreBasicStone][S11] |
| Crystalized Fairies (`CrystalFairy`) | 12 + rand(20) Adern, Größe 6, **Y < 25** | spawnt Fairies (rand(6)); Material für das Fairy Sword | [JAR][S11] |
| Pink Tourmaline (`CrystalCrystal`) | in ~1 von 30 Chunks; Y ≈ 30 + rand(5); 1–10 Klumpen | → Pink Tourmaline Ingot („second-best armor in the Crystal Dimension“) | [JAR][S11] |
| Tiger's Eye (`TigersEye`) | in ~1 von 30 Chunks; **Y ≈ 5 + rand(5)**, also nahe Bedrock; 1–5 Klumpen | „not every block will drop an item“ (Zufallsmenge im Code); nach dem Schmelzen bestes Crystal-Material | [JAR: OreCrystalCrystal][S11] |
| Crystal-Dried-Eggs | 25 + rand(30) Versuche, Größe 4, **Y > 45**, in Kyanite: Urchin, Flounder, Skate, Rotator, Peacock, Fairy, Dungeon Beast, Vortex, Rat, Whale, Irukandji | siehe Egg-Mechanik | [JAR] |

Konflikte und Hinweise:

- **Wiki: „5 different types of ore in OreSpawn: Salt, Titanium, Uranium, Ruby and
  Amethyst“** [S28 Ores]. Das stimmt nur für die Oberwelt-Eigenerze. Crystal-Erze, Troll-
  Blöcke, Lava Foam und die Dried-Egg-Blöcke fehlen dort.
- Website-Crystal-Seite: wertvolle Erze bei **Y < 20** nahe Bedrock unter dem Maze [S4].
  Laut Code liegt das nur für Tiger's Eye (≈5–9) und die Rat/Fairy-Adern (< 25); Pink
  Tourmaline liegt bei ≈30–34.
- Titanium „around Y 40 to 60 in Extreme Hills“ (SEO-Seite titaniumtrusted.com): **durch
  den Code widerlegt**, Default 0–20, keine Biombedingung [JAR]. Die Quelle wird deshalb
  nicht verwendet.
- Die WebFetch-Zusammenfassung der Materials-Seite nannte ein Material „Dynamite“. Auf der
  Seite steht kein solcher Name; der Text gehört zu Crystal Energy.

---

## Spawn-egg/ore mechanic

- **Namensgeber:** „This is how the mod started and got its name!“ Ancient Dried OreSpawn
  Eggs werden als Erzblöcke abgebaut und auf der Werkbank mit einem **Wassereimer
  rehydriert** → normales Spawn-Ei [S9][S13].
  - Wiki-Variante: Dried Egg + Wassereimer **+ Steinblock** [S28 Dried_Spawn_Eggs]. Das
    Rezept gehört in den Items-Schnitt und wurde hier nicht geprüft.
- **Block:** Klasse `OreGenericEgg` [JAR]:
  - Material Erde-artig (`field_151578_c`), Härte 0.5 (Wiki: „best mined with a shovel“
    [S28]).
  - Droppt sich selbst, dazu gelegentlich XP (`rand`-Abfrage).
- **Fundort:** „only at shallow depths and are very, very common in the Mining Dimension“
  [S9]. Code: Y 50–128 (`SpawnOres` Default) [JAR]. Wiki: „in caves and in the sides of
  mountains … near the top of the world“ [S28].
- **Auswahl pro Ader** [JAR: OreSpawnWorld.generateOres, ChunkOreGenerator]:
  - Mit Wahrscheinlichkeit **7/104** eines der 7 „neueren“ Eier: Brutalfly, Nastysaurus,
    Pointysaurus, Cricket, Frog, Spider Driver, Crab.
  - Sonst `nextInt(98)` über die übrigen Oberwelt-Eier.
  - Nicht natürlich in der Oberwelt: Crystal Cow, das volle King- und Queen-Ei sowie die 11
    Crystal-Eier (die gibt es nur in der Crystal Dimension).
  - Das volle Mobzilla-Ei **und** das Mobzilla-Teil generieren beide.
- **Teile-Mechanik:** Für King, Queen und Mobzilla gibt es „Ancient Dried … Spawn Egg
  Part“ [JAR: Anzeigenamen]. Wiki: 9 Teile ergeben das ganze Ei [S28 King, Mobzilla].
- **Anzahl:** 119 Anzeigenamen „Ancient Dried …“ im Jar, inklusive 3 Parts und der drei
  vollen Boss-Eier [JAR: LanguageRegistry.addNameForObject]. Wiki: „128 different dried
  spawn eggs“ [S28]; **Konflikt**, möglicherweise zählt das Wiki eine andere Version. Die
  Website-Galerie listet 78 Einträge mit Sammelbegriffen (Cows, Worms, Criminals …) [S9].
- **Vollständige Liste** (Anzeigenamen ohne „Ancient Dried … Spawn Egg“) [JAR]:
  - *Vanilla:* Spider, Bat, Cow, Pig, Squid, Chicken, Creeper, Skeleton, Zombie, Slime,
    Ghast, Zombie Pigman, Enderman, Cave Spider, Silverfish, Magma Cube, Witch, Sheep,
    Wolf, Mooshroom, Ocelot, Blaze, Wither Skeleton, Ender Dragon, Snow Golem, Iron Golem,
    Wither Boss, Villager, Horse.
  - *OreSpawn:* Girlfriend, Boyfriend, Apple Cow, Crystal Cow, Golden Apple Cow, Enchanted
    Golden Apple Cow, MOTHRA, Alosaurus, Cryolophosaurus, Camarasaurus, Velocity Raptor,
    Hydrolisc, Basilisk, Dragonfly, Emperor Scorpion, Scorpion, Cave Fisher, Baby Dragon,
    Baryonyx, WTF?, Bird, Kyuubi, Alien, Attack Squid, WaterDragon, Kraken, Lizard,
    Cephadrome, Dragon, Bee, Jumpy Bug, Spit Bug, Stink Bug, Ostrich, Gazelle, Chipmunk,
    Creeping Horror, Terrible Terror, Cliff Racer, Triffid, Nightmare, Lurking Terror,
    Ender Knight, Ender Reaper, Mobzilla (Part + ganz), The King (Part + ganz), The Queen
    (Part + ganz), Small/Medium/Large Worm, Cassowary, Cloud Shark, Gold Fish, Leaf
    Monster, T-Shirt, Beaver, Urchin, Flounder, Skate, Rotator, Peacock, Fairy, Dungeon
    Beast, Vortex, Rat, Whale, Irukandji, T. Rex, Hercules Beetle, Mantis, Stinky, Easter
    Bunny, Criminal, Brutalfly, Nastysaurus, Pointysaurus, Cricket, Frog, Spider Driver,
    Crab, CaterKiller, Molenoid, Sea Monster, Sea Viper, Rubber Ducky, Hammerhead,
    Leonopteryx.
- Eine eigene „Kreatur spawnt beim Abbau“-Mechanik haben nur die Troll-Blöcke und die
  Crystalized-Rats/Fairies-Erze (siehe *Ores*). Die Dried-Egg-Blöcke spawnen **nichts**
  direkt [JAR: OreBasicStone vs. OreGenericEgg].

---

## Ant teleport mechanic

| Kreatur | Ziel | Nest-Block (Anzeigename) | natürlich | Verhalten | Quelle |
|---|---|---|---|---|---|
| Brown Ant (`EntityAnt`, Config `BlackAntEnable`) | Utopia | Ant Nest | Oberwelt u. a. | harmlos | [JAR][S17][S18] |
| Red Ant | Mining | Red Ant Nest | Oberwelt, **Nether** (`addNetherAnts`), Red Ant Hangout | **feindlich** | [JAR][S17] |
| Rainbow Ant | Village | Rainbow Ant Nest | Oberwelt, Village Dim | harmlos | [JAR][S17] |
| Unstable Ant | Danger | Unstable Ant Nest | Oberwelt, Danger Dim | harmlos | [JAR][S17] |
| Termite (`Termite`) | Crystal | Termite Nest / Crystal Termite Nest | forest, jungle, plains; Crystal Dim; auch in Fairy Castle Trees | **feindlich**; zerstört Holz („Anything wooden gets destroyed. And they multiply!“), Wiki: macht es zu Erde | [JAR][S17][S28 Ants] |
| Butterfly (`EntityButterfly`) | Chaos | keins; Butterfly Plant | Butterfly Plants tagsüber | harmlos; die Vampire Butterfly der Danger Dimension saugt Blut | [JAR][S17] |

- **Ameisenstatistik** (2014/2021): Attack 0/2, Defense 0/0, Health 1/2, „Found: Almost
  everywhere“, Drops nothing [S17][S18]. Termite: Attack 2, Health 5 [S17].
- **Natürliche Nester** [JAR: OreSpawnWorld.addAnts]:
  - Mehrere Versuche pro Chunk (Parameter 2 bzw. 4, `LessLag` reduziert) auf Gras bei
    Y 40–100.
  - Mit einer Zufallsabfrage entweder, gleichverteilt über `nextInt(4)`, Red, Rainbow,
    Unstable oder Termite Nest (jeweils nur bei aktivierter Config), oder ein Brown Ant
    Nest.
- **Nester spawnen Ameisen** [JAR: AntBlock.updateTick, CrystalAntBlock.updateTick]:
  - Bedingung: Luft über dem Nest (plus Wetterabfrage `func_72896_J`).
  - Dann per Zufall (`nextInt(6)`) die zum Nest passende Ameise.
  - Das Nest droppt sich selbst, ist also abbau- und platzierbar.
- **„Telepad“:** Nester ausgraben, zu Hause platzieren und mit einem **doppelten Ring aus
  Salt Blocks** umgeben, damit man nicht überrannt wird. „Now you can travel to any
  dimension, pretty much at will!“ [S13]. Wiki: Nester in eine Zoo Cage stellen [S28 Ants].
- **Salz tötet Ameisen**; einfache Reihe reicht nicht immer [S11]. Creeper Repellent und
  Kraken Repellent schützen auch vor Ameisen und Termiten [S13].
- **Troll-Blöcke** als Ameisen-/Termiten-„Fallen“, siehe *Ores*.
- Rückkehr ohne passende Kreatur: Wiki-Tipp, ein Nest der jeweiligen Dimension mitnehmen
  [S28 OreSpawn_questions]. Für die Crystal Dimension geht das nicht, da das Inventar leer
  sein muss (Deutung).

---

## Blocks

Anzeigenamen aus `LanguageRegistry.addNameForObject`, interne Registry-Namen aus
`GameRegistry.registerBlock` [JAR: OreSpawnMain]. Insgesamt **211**
`registerBlock`-Aufrufe, davon 119 Dried-Egg-Blöcke. Verhalten aus den Blockklassen [JAR]
und der Website.

### Erze und Rohstoffblöcke

Salt Ore, Titanium Ore, Uranium Ore, Amethyst Ore, Ruby Ore, Kyanite, Crystal Energy,
Crystalized Rats, Crystalized Fairies, Pink Tourmaline, Tiger's Eye, Red Ant Troll Block,
Termite Troll Block, Lava Foam. Siehe *Ores*.

### Speicher- und Dekoblöcke

| Block (intern) | Verhalten / Zweck | Quelle |
|---|---|---|
| Titanium Block (`OreSpawn_BlockTitaniumBlock`) | „This block actually sparkles!“ | [S11][JAR] |
| Uranium Block | „a nice sparkly yellow block“ | [S11] |
| Ruby Block, Amethyst Block | Deko, Bestandteil von Queen's Tree/Altar | [S11][JAR] |
| Pink Tourmaline Block, Tiger's Eye Block (`BlockCrystal`) | Härte 4, **Lichtwert 0.4** | [JAR][S11] |
| Mobzilla Scale Block | seit V18; aus Mobzilla-Schuppen, „Now you've got a really good reason to find a Duplicator Tree“ | [S27][S11] |
| Ender-Pearl Block, Eye-of-Ender Block | Eye-of-Ender Block + **Extreme Torch darauf** → **Cephadrome** spawnt (Explosionseffekt, beide Blöcke verschwinden) | [JAR: BlockExtremeTorch.func_149689_a][S11][S16] |
| Molenoid Dirt | vom Molenoid erzeugt; „self cleaning“ (verschwindet), gibt Slowness | [S11] |
| Crystal Grass, Crystal Planks, Crystal Workbench, Crystal Furnace (an/aus), Crystal Tree Wood, Crystal Tree Leaves (3 Varianten) | Crystal-Ersatz für Gras, Holz, Werkbank und Ofen. Furnace „much faster“ als Vanilla | [S13][JAR] |

### Funktionsblöcke

| Block | Verhalten | Quelle |
|---|---|---|
| **Random Teleport Block** (`RTPBlock`) | Wer darauf läuft, wird bis zu ~16 Blöcke weit zufällig versetzt (bis 1000 Versuche, sichere Landestelle: Boden fest, 2 Luft darüber); Rauch-/Explosionspartikel. Häufig in Dungeons (Challenge, Basilisk, Nightmare) | [JAR: RTPBlock.func_149724_b][S11] |
| **Extreme Torch** | „about 10% brighter than normal torches“; Partikel. Wiki: auch Drop von Fireflies | [S13][S28 Extreme_Torch] |
| **Crystal Torch** | nur auf Crystal-Blöcke setzbar (Kyanite, Crystal Grass, Crystal Tree Wood, Crystal Planks); Funken-/Flammenpartikel | [JAR: BlockCrystalTorch.canPlaceTorchOn] |
| **Creeper Repellent** (Block) | hält Creeper fern, Reichweite ~10 (Code-Konstante 10), auch Ameisen/Termiten | [S13][JAR] |
| **Kraken Repellent** (Block) | schützt ~10×10 vor Kraken beim Töten von Attack Squid, auch Ameisen/Termiten | [S13][JAR] |
| **Pizza!** | platzierbarer Kuchen-artiger Block, Scheiben essen (4 Heilung/Scheibe laut Food-Seite) | [JAR: BlockPizza][S14] |
| **Duct Tape!** | auf den Boden legen, mit beschädigtem Item rechtsklicken, repariert bis 1/6 pro Klick | [S13] |
| **Island Block** | pflanzbare Insel: wächst nach Zufall zu `Island`/`IslandToo`; Partikel „happyVillager“ | [JAR][S13] |
| **The King Spawner Block / The Queen Spawner Block** | beim Tick (Config `TheKingEnable`/`TheQueenEnable`) spawnt The King/The Queen, Block verschwindet | [JAR: KingSpawnerBlock, QueenSpawnerBlock] |
| **Random Dungeon Spawner** | siehe *Structures* | [JAR][S13] |
| **Ant Nest, Red Ant Nest, Rainbow Ant Nest, Unstable Ant Nest, Termite Nest, Crystal Termite Nest** | spawnen ihre Ameisen | [JAR] |
| **Sky Tree Wood** | Bricht man ein Stück ab, fällt der **ganze verbundene Sky Tree** (rekursiv, bis 1000 Blöcke). „Very useful for trolling 'friends'“; ergibt Oak Planks | [JAR: BlockSkyTreeLog.breakRecursor][S10][S11] |
| **Duplicator Tree Wood** | beim Tick wächst `Trees.DuplicatorTree` (Config `DuplicatorTreeEnable`); dupliziert Blöcke am Stammfuß | [JAR][S10][S11] |
| Ancient Dried … Spawn Egg (119×) | siehe *Spawn-egg/ore mechanic* | [JAR] |

Die King/Queen Spawner Blocks sind **nicht** auf der Website beschrieben.

### Pflanzenblöcke

Strawberry Plant, Radish Plant, Rice Plant, Butterfly Plant, Moth Plant, Mosquito Plant,
Firefly Plant, Corn Plant 1–4, Quinoa Plant 1–4, Tomato Plant 1–4, Lettuce Plant 1–4,
Apple/Cherry/Peach/Experience/Scary Tree Leaves, Experience Tree Sapling,
Red/Yellow/Blue Crystal Tree Sapling, Pink/Blue/Black/Dead Flower,
Red/Green/Blue/Yellow Crystal Flower. Siehe *Crops & trees*.

### Nicht als Block gefunden

- Kraken- oder Mobzilla-Spawnerblöcke gibt es nicht. Kraken kommen über getötete Attack
  Squids oder das Dried Egg [S28 The_Kraken]; Mobzilla spawnt in der Village Dimension
  [S28 Mobzilla].
- „Elevator“ ist eine Entity-Klasse, kein Block [JAR: Elevator]; Items-Schnitt.
- Treppen-Items (Stairs Up/Down/Across), Instant Garden, Instant Shelter und Miner's Dream
  sind Items, die Blöcke setzen [S13]; Items-Schnitt.

---

## Crops & trees

### Feldfrüchte

| Pflanze | natürlich (Code) | Anbau / Drop | Quelle |
|---|---|---|---|
| Strawberry | Oberwelt: Forest, ForestHills, Birch Forest (+Hills) | reife Pflanze ausgraben → Baby-Pflanze; auf Ackerboden; → Strawberries; Instant Garden | [JAR: addStrawberries][S10][S14] |
| Corn (4 Stufen, wächst nach oben) | Plains (Oberwelt), auch Utopia/Village | Ackerboden; **Knochenmehl wirkt nicht**; → Corn Cob; Popcorn, Corn Dog | [JAR: BlockCorn, addCorn][S10][S14] |
| Tomato (4 Stufen) | Plains, auch Utopia/Village | Ackerboden → Tomatoes | [JAR][S10] |
| Radish, Lettuce (4 Stufen) | `addVeggies`: Oberwelt nur River und Swampland; **überall** in Utopia, Mining und Chaos | Ackerboden | [JAR: addVeggies][S10] |
| Rice | Crystal Dim, auf Crystal Grass | Ackerboden oder Crystal Grass | [JAR: addRice][S10][S14] |
| Quinoa (4 Stufen) | Crystal Dim | Ackerboden oder Crystal Grass; auch Futter für Battle-Mobs (grünes Team) | [JAR: addQuinoa][S10] |

Website: „Garlic … Plains plants“ [S10 laut WebFetch-Zusammenfassung]. Im Volltext der
Seite und im Jar gibt es **kein Garlic**; der Eintrag ist Salat, die Zusammenfassung war
falsch. Ebenso gibt es keine „Butter“-Pflanze; Butter entsteht aus Milch [S14].

`addVeggies` im Detail [JAR]: 1/15 Chunks, 8 Versuche auf Gras bei Y 40–100, `nextInt(6)`
wählt Karotte, Kartoffel, Radieschen, Salat, einen weiteren Vanilla-Block (1/10,
`field_150394_bc`, nicht aufgelöst) oder **Duplicator Tree Wood (1/50)**.

### Kreaturen-Pflanzen

| Pflanze | natürlich (Code) | spawnt | Quelle |
|---|---|---|---|
| Butterfly Plant | Forest, ForestHills, River, Jungle, JungleHills, Swampland, Birch Forest (+Hills), Roofed Forest; Utopia, Chaos | Butterflies tagsüber. Code: `ButterflyEnable`, Seed-Item `MyButterflySeed` | [JAR][S10] |
| Moth Plant | wie oben | Moths nachts | [S10] |
| Firefly Plant | wie oben | Fireflies nachts, „rather prolific“ | [S10] |
| Mosquito Plant | Jungle, Swampland; Nether; Village und Mining | Mosquitoes; Config `MosquitoEnable` | [JAR][S10] |

Alle vier: „Dig them up and plant them in a tilled-earth block“ [S10].

### Blumen

| Blume | Fundort | Verhalten | Quelle |
|---|---|---|---|
| Pink Flower ↔ Black Flower | Danger Dim | wechselt Tag/Nacht | [JAR: MyBlockFlower][S10] |
| Blue Flower ↔ Dead Flower | Danger Dim | wechselt Tag/Nacht | [JAR][S10] |
| Crystal Flowers (Red, Green, Blue, Yellow) | Crystal Dim | Red heilt Girlfriends, Yellow wechselt ihr Outfit | [S10] |

### Bäume

| Baum | Fundort | Pflanzbar / Besonderheit | Quelle |
|---|---|---|---|
| Apple Tree | Utopia, Village (Website: „Usually found around the Village Dimension“) | Frucht auf Werkbank → Seed → auf Gras pflanzen | [JAR: addAppleTrees][S10] |
| Cherry Tree | Village (+Utopia) | Cherry Pit | [S10][S14] |
| Peach Tree | Village (+Utopia) | Peach Pit | [S10][S14] |
| Experience Tree | nur gepflanzt | Apple Seed umgeben von Bottles o' Enchanting → Experience Tree Seed; Sapling (Partikel) → `Trees.ExperienceTree`. „Build a dance floor … get yourself a few girlfriends“ | [JAR][S10] |
| Duplicator Tree | Website: „Found rarely in the Mining Dimension“. Code: via `addVeggies` auch Utopia, Chaos und Oberwelt (River/Swamp) | Stamm auf Gras pflanzen; ausgewachsen „3 high and 9 total leaf blocks“ (Apple Leaves); dupliziert Blöcke am Fuß. Config `DuplicatorTreeEnable` | [JAR: Trees.DuplicatorTree][S10][S11] |
| Sky Tree | Utopia | **nicht pflanzbar**; ein Stamm bricht → alle fallen; Stämme → Oak Planks | [S10][JAR] |
| Wind Tree | Utopia | **nicht pflanzbar**; zeigt immer in dieselbe Richtung | [S10][JAR: Trees.WindTree] |
| Round Tree | Utopia | runde Äste, „great place to build a fort“ (Deutung: `MakeBigRoundTree`) | [S10] |
| OMG Magic Apple / Ginormous Trees | Utopia natürlich (`addHugeTree`); oder Magic Apple (Apfel + Redstone-Blöcke) auf Gras | eckig/rund/kreisförmig, innen begehbar, Wolken, „one VERY special one“. Seltene Edelsteinvariante (Gold/Diamant/Smaragd) über `GinormousEmeraldTreeEnable` | [JAR: ItemMagicApple][S10][S12][S13] |
| Tree of Goodness | Utopia, sehr selten | The King oben | siehe *Structures* |
| Queen's Tree | Utopia | The Queen oben | siehe *Structures* |
| Crystal Trees | Crystal Dim | Saplings Red/Yellow/Blue; Wuchsformen hoch, verzweigt und hoch-blau; Laub droppt Crystal Apples | [JAR][S10][S13] |
| Fairy Tree / Fairy Castle Tree | Crystal Dim | Struktur, siehe oben | [JAR] |
| Scraggly Trees | Danger und Chaos Dim | Vanilla-Stamm + Apple Leaves | [JAR] |

Zum Queen's Tree schreibt die Website: „made of more Ruby, Amethyst and obsidian than you
ever knew existed“ [S10].

Namenskonflikt bei den Crystal Trees: Die Website nennt drei Arten „Crystal Trees“,
„Silver Trees“ und „Willow Trees“ [S10], das Jar nur „Red/Yellow/Blue Crystal Tree
Sapling“. Die Zuordnung ist unklar.

**Instant Garden** (Item) enthält radish, lettuce, potato, wheat, tomato, corn, strawberry,
sugarcane und melon. Rezept 1.6.4: seeds, gunpowder, redstone block; 1.7.2: wheat statt
seeds [S13].

---

## Other mechanics

- **Config `OreSpawn.cfg`** unter `.minecraft/config/`, Kategorien `orespawnmobs`,
  `orespawnores`, `orespawnarmor`, `orespawntweaks`, `orespawnids`, `orespawnweapons`
  [S12]. Für diesen Schnitt relevant:

  | Schlüssel | Default | Wirkung |
  |---|---|---|
  | `<ore>_rate/_clumpsize/_mindepth/_maxdepth` | siehe Ores-Tabelle | Namen: `Ruby`, `BlockRuby`, `Uranium`, `Titanium`, `Amethyst`, `Salt`, `SpawnOres`, `Diamond`, `BlockDiamond`, `Emerald`, `BlockEmerald`, `Gold`, `BlockGold` [JAR] |
  | `LessOre` | 0 | Erzversuche /3, keine Zusatz-Diamant-/Smaragd-/Gold-/Block-Adern, Mining-Ores 1× statt 3×, kein Zusatz-Lapis [JAR][S12] |
  | `LessLag` | 0 | weniger Bäume, Dungeons, Inseln, Ameisen (Chancen halbiert/geviertelt, im Code auch Wert 2 abgefragt) [JAR][S12] |
  | `DisableOverworldDungeons` | ? | schaltet die Oberwelt-Minidungeons ab [JAR]. **Nicht in der Website-Doku**, Default nicht ausgelesen |
  | `IslandSizeFactor` | 2 (1–5) | Größe der schwebenden Inseln [JAR][S12] |
  | `IslandSpeedFactor` | 2 (1–5) | Tempo der Inseln. Die Website schreibt `IslandSPeedFactor` [S12], der Code `IslandSpeedFactor` [JAR] |
  | `DuplicatorTreeEnable` | 1 | Duplicator Tree an/aus [JAR][S12] |
  | `GinormousEmeraldTreeEnable` | 1 | seltene Edelstein-Variante des Magic-Apple-Baums [JAR][S12] |
  | `BaseDimensionID` / `BaseBiomeID` | 80 / 120 | siehe Dimensions [JAR][S12] |
  | `BaseBlockID` / `BaseItemID` | 2700 / 9000 | nur für die alten numerischen IDs (1.6.4-Zeit) relevant [S12][S15] |
  | `<Mob>Enable` | 1 | wirkt auf Weltgenerierung: `RedAntEnable`, `BlackAntEnable`, `RainbowAntEnable`, `UnstableAntEnable`, `TermiteEnable` (Nester), `ButterflyEnable`, `MosquitoEnable`, `RotatorEnable`/`UrchinEnable` (Crystal-Strukturen), `IrukandjiEnable`, `RockEnable` (Steine), `SpiderDriverEnable` (Spider Hangout), `TheKingEnable`/`TheQueenEnable` (Spawner Blocks) [JAR] |
  | `PlayNicely` | 0 | alle OreSpawn-Mobs friedlich und nicht zerstörerisch, Kraken/King/Mobzilla/CaterKiller verkleinert [S12] |
  | `FullPowerKingEnable` | 0 | „the full King's wrath. Backup your world first!“ [S12] |

  - Abweichende Mob-Namen in der Config: `GammaMetroid` = WTF?, `Godzilla` = Mobzilla,
    `Robot1–5` = Bomb-Omb/Robo-Pounder/Robo-Gunner/Robo-Warrior/Robo-Sniper, `TrooperBug`
    = Jumpy Bug [S12]. Im Code außerdem `PitchBlack` = Nightmare, `Spyro` = Baby Dragon,
    `Cockateil` = Bird, `Leon` = Leonopteryx (aus Dried-Egg-Anzeigenamen gegen Feldnamen
    abgeglichen) [JAR].
- **Strukturabstand:** Nach großen Strukturen setzt der Code `recently_placed` (100 nach
  King/Queen Altar, 50 nach Challenge-Burg), das weitere große Strukturen blockiert.
  Einheit vermutlich Chunks (Deutung) [JAR].
- **Performance:** eigenes `setBlock` ohne Lichtberechnung für die Weltgenerierung
  („I've optimized as much as I can, including making my own special setBlock() that
  strips out lighting“) [S15][JAR: OreSpawnMain.setBlockFast]. Empfehlungen: Fast
  Graphics, kein Smooth Lighting, mehr RAM [S15].
- **Steine und Kristalle als Welt-Objekte:**
  - Rocks (12 Sorten, Wurfwaffen) liegen „almost anywhere“, am häufigsten Desert und
    Extreme Hills. Code: `addRocks` in River, Extreme Hills und Desert sowie in Mining,
    Danger und Crystal.
  - Crystals nur in der Crystal Dimension. Seit V18 zerbrechen sie Glas, sind platzierbar
    und hüpfen über Wasser [S17 rocks-and-crystals][S27][JAR].
- **Sifter** (Item): auf Sand, Erde, Gras, Kies und Wasser anwenden → Zufallsfunde. Seltene
  Funde (Amethyst, Ruby, Titanium/Uranium Nuggets, Moth Scales, Diamanten…) nur im Wasser
  [S13][S28 Sifter].
- **Miner's Dream** (Item): entfernt Stein in großem Bereich, lässt Erze stehen, setzt
  Fackeln [S13]; NamuWiki warnt vor Alien-Nestern [S30].
- **Battle Mobs / Teams:** Ostrich, Lizard, Velocity Raptor und Chipmunk. Karotte = Rot,
  Kartoffel = Blau, **Quinoa** = Grün; **Mais** klont [S38]. Das verknüpft Crystal-
  Dimension-Quinoa und Oberwelt-Mais mit dem Spiel.
- **Termiten zerstören Holz**, Beavers fressen Bäume. Beides wird als Anti-Lag-Tipp für
  Leaves genannt [S15][S17].
- Nicht gefunden: „Mobzilla on the loose“-Event, Wetter-Events, eine Trap-Block-Klasse
  (außer RTP- und Troll-Blöcken). Critter Cage und Zoo Cage sind Items (Items-Schnitt)
  [S13].

---

## Removed/old content

- **Dimensionen über die Zeit:**
  - Seite Januar 2014: **5 Dimensionen** (Utopia, Village, Crystal, Mining, Danger)
    [S19].
  - Crystal Dimension neu um den 01.01.2014 [S25].
  - Chaos Dimension neu mit **V19 am 03.08.2014** [S20].
  - Village Dimension bekam im Januar 2014 ein eigenes Biom; alte Chunks spawnen danach
    keine Roboter [S26].
  - Die Ameisenseite von 2014 kennt nur die vier Ant-Dimensionen [S18]; der
    Butterfly-Teleport steht erst auf der späteren Seite [S17].
- **Dungeon-Chronik** (Blog; Konzepte von Community-Einsendungen):
  - 13.04.2014 („New Dungeons“, V16/V17): Water Dragon Lair, Play Pool, Small Beehive,
    Ender Knight Outpost (auch End), Alien/WTF? Dungeon, Shadow Dungeon, Cloud Shark
    Dungeon, Leaf Monster Dungeon, Mini Dungeon, Gold Fish Bowl, Ender Reaper Graveyard
    [S21].
  - 24.04.2014 („already new in V17“): King's Altar, Robot Lab, Inca Pyramid, Ender Castle,
    Damsel in Distress, Crystal Urchin Spawner, Spit Bug Lair, Abandoned Igloo, Enderdragon
    Hospital, Crystal Haunted House, Bouncy Castle [S23].
  - 30.05.2014 (V18): White House, Rubber Ducky Pond, Stinky House, Nightmare Rookery,
    Girlfriend (and Boyfriend) Island, Greenhouse [S22].
  - 30.07.2014 (V19): Frog Pond, Pumpkin, Rotator Dungeon, Rainbow Dungeon [S24].
  - 26.03.2014: erste Konzeptskizze „Challenge Structure Version 0.0000001“
    [Wayback-Blog challenge-structure, siehe Sources].
- **V18** (21.05.2014): Rocks zerbrechen Glas, neue Crystals, Mobzilla Scale Block [S27].
- **Versionsspuren:** Instant-Garden-Rezept unterschiedlich zwischen 1.6.4 und 1.7.2
  [S13]; Versionen 11–15 für Forge 9.11.1.965 (MC 1.6.4) [S15]; Downloads für
  1.6.4/1.7.10 [S33].
- **Nicht 1.7.10, später:** OreSpawn wanderte zu TheyCallMeDanger's eigenem Spiel
  **DangerZone** [S37][S30].
  - Die WordPress-orespawn.com von 2021 listet die Dimensionen Utopia, Mining, **Zoo,
    Robot**, Crystal, **Doom** (hausgroße Würmer), Danger, Chaos, **Misfit** („Home of the
    NOPE“), **Dragon's Lair** (Element Dragons, Dargon, Altar teleportiert in seine Burg),
    **Kraken King** (Chaos Demon, Baby Sitter), **Mashup** [S32]. Diese sind im
    1.7.10-Jar **nicht** vorhanden (nur 6 WorldProvider) [JAR].
  - Der MC-1.12.2-Port sollte „in parts“ erscheinen, „First up is the Mining Dimension“;
    öffentliche Dev-Builds 0.1–0.6 [S33].
  - Das Community-Nachfolgeprojekt heißt „Chaos Awakens“ (1.16.5) [S30], das
    Remake-Projekt „AdventureKraft“ [S37]. Details gehören in den Historien-Schnitt.

---

## Open questions

1. **Riesenbaum-Wahrscheinlichkeiten:** Wie oft wird `addHugeTree` zum Tree of Goodness,
   Queen's Tree, Square/Circular/Round Tree? Die Website sagt 1/100 für Goodness [S10]; im
   Code gibt es mehrere `nextInt(100)`/`nextInt(20)`-Zweige, die nicht vollständig
   ausgewertet wurden.
2. **Danger-Dimension-Boden:** genaue Schichthöhen (Bedrock/Erde/Gras) in
   `ChunkProviderOreSpawn4.func_73154_d`; Inselhöhen (`IslandBlock.updateTick` enthält 64,
   55, 45 mit `IslandSizeFactor`) und die Auswahl `Island` vs. `IslandToo`.
3. **Chaos-Dimension-Terrain:** Höhenverteilung der Landmassen, Existenz von Seen
   („not much water“ [S7]) und Vulkanen (nur 2021 [S32]).
4. **Mining Dimension:** welches Vanilla-Biom `WorldChunkManagerHell` setzt.
5. **Drops:** Menge und XP bei Ruby, Amethyst, Tiger's Eye und Pink Tourmaline;
   Explosionschance von Crystal Energy und Pink Tourmaline; Anzahl der Ameisen aus
   Troll-Blöcken.
6. **Challenge Dungeon:** wie die Stufen 1–6 [S8] im Code gewählt werden (`buildLevel`,
   `addLevelDecorations`); vollständige Truhen-Loottabellen (`fill_chests`,
   `fill_beehive_chests`, `fill_shadow_chests` usw.) nicht aufgelistet.
7. **Nightmare Dungeon:** wer die Struktur platziert.
8. **Golden Ore Tree** (Wiki) und „Silver/Willow Trees“ (Website): Zuordnung zum Jar
   offen.
9. **Dried Eggs:** 119 (Jar) vs. 128 (Wiki); das genaue Rehydrier-Rezept (mit oder ohne
   Steinblock).
10. **Random Dungeon Spawner:** Untergrund Stein/Bruchstein [S8] vs. Erde [S30]; die
    Strukturfälle 19–49.
11. **`DisableOverworldDungeons`:** Default-Wert.
12. **`addEndAnts`:** was dort genau passiert (in der Methodenübersicht ohne Feldzugriffe).
13. **SRG→Vanilla-Zuordnung** der Blockfelder (z. B. `field_150369_x` Lapis-Erz,
    `field_150341_Y` Bemooster Bruchstein) ist nicht gegen MCP-Mappings geprüft.
14. Der Minecraft-Forum-Hauptthread mit Changelogs wurde **nicht gefunden**; nur
    Support-Threads [Suche]. Changelog-Informationen stammen aus den Blogposts von
    orespawn.com.
15. Alle Jar-Werte sind Defaults aus Bytecode, nicht im Spiel beobachtet. Ein Testlauf mit
    Java 8 und Forge 1.7.10 wäre der Gegenbeweis.

---

## Sources

**Offizielle Seite (Spiegel, Stand 2021)**, alle unter
`https://shrekleaker.github.io/orespawn.com/`:

- [S1] dimensions.html
- [S2] utopia-dimension.html
- [S3] village-dimension.html
- [S4] crystal-dimension.html
- [S5] mining-dimension.html
- [S6] danger-dimension.html
- [S7] chaos-dimension.html
- [S8] dungeons.html
- [S9] ancient-dried-eggs.html
- [S10] plants-and-trees.html
- [S11] materials.html
- [S12] orespawncfg-file.html
- [S13] items.html
- [S14] food.html
- [S15] technical-help.html
- [S16] things-to-do.html
- [S17] ants.html, butterflies.html, termite.html, rocks-and-crystals.html
- [S33] download.html, index.htm
- [S38] battle-mobs-gameplay.html

**orespawn.com im Wayback Machine:**

- [S18] https://web.archive.org/web/20140117072610/http://www.orespawn.com:80/ants.html
- [S19] https://web.archive.org/web/20140104063846/http://www.orespawn.com:80/dimensions.html
- [S20] https://web.archive.org/web/20150324075539/http://www.orespawn.com/home/v19-new-dimension
- [S21] https://web.archive.org/web/20150318011535/http://www.orespawn.com/home/new-dungeons
- [S22] https://web.archive.org/web/20150324012538/http://www.orespawn.com/home/new-dungeons-for-v18
- [S23] https://web.archive.org/web/20150324012615/http://www.orespawn.com/home/stuff-thats-already-new-in-v17
- [S24] https://web.archive.org/web/20150324075544/http://www.orespawn.com/home/v19-new-dungeons
- [S25] https://web.archive.org/web/20150324012518/http://www.orespawn.com/home/crystal-dimension-excitement
- [S26] https://web.archive.org/web/20150324013010/http://www.orespawn.com/home/village-dimension-notes
- [S27] https://web.archive.org/web/20150324012948/http://www.orespawn.com/home/v18-sneak-peek
- Challenge-Konzept: https://web.archive.org/web/20150324005927/http://www.orespawn.com/home/challenge-structure
- [S32] WordPress-orespawn.com 2021:
  - https://web.archive.org/web/20211007051924/https://www.orespawn.com/misfit/
  - …/20211007051219/https://www.orespawn.com/doom/
  - …/20211007004725/https://www.orespawn.com/dragons-lair/
  - …/20211007043341/https://www.orespawn.com/kraken-king/
  - …/20211007215609/https://www.orespawn.com/mashup/
  - …/20211007010250/https://www.orespawn.com/chaos/
  - …/20211007055800/https://www.orespawn.com/danger/

**Wikis und Sekundärquellen:**

- [S28] https://orespawnmod.fandom.com/wiki/ + Seite, Rohtext via `?action=raw`. Genutzt:
  Ores, Ants, Salt_ore, Salt, The_Tree_of_Goodness, The_Queen_Tree,
  Golden_Ore_Tree(Structure), Dried_Spawn_Eggs, Danger_Dimension, Chaos_Dimension,
  Criminal, Sifter, Extreme_Torch, The_WTF?_Beetle, Triffid, Nightmare, Green_Goo,
  Leonopteryx, The_Kraken, King, Mobzilla, OreSpawn_questions_For_people_and_myself
- [S29] https://crazy-craft-wiki.fandom.com/wiki/Orespawn_Mod
- [S30] https://en.namu.wiki/w/Orespawn
- [S31] https://minecraftology.fandom.com/wiki/List_of_Dimensions (zitiert alte
  Website-Texte zu Mining/Village/Utopia)
- [S35] https://minecraft-mods.fandom.com/wiki/OreSpawn
- [S36] https://github.com/DrPlantabyte/OreSpawn (andere Mod, nur zur Abgrenzung)
- [S37] https://orespawnmod.fandom.com/wiki/The_Ore_Spawn_mod_Wiki (Hinweis: offizielles
  Wiki gelöscht, DangerZone-Umzug)

**Jar:**

- [S34] / [JAR] https://archive.org/details/orespawn-1.7.10-20.3_202109,
  `orespawn-1.7.10-20.3.jar`, 17 541 969 Bytes, 594 Klassen im Paket `danger.orespawn`

**Nicht erreichbar / nicht verwendet:**

- orespawn.fandom.com (404/402)
- www.orespawn.com live (403)
- titaniumtrusted.com (SEO-Text, durch Jar widerlegt)
- Minecraft-Forum-Hauptthread (nicht gefunden)
