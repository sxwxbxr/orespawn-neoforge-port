# Verhalten: world-03

Dieser Batch umfasst die sechs Dimensionen von OreSpawn. Zu jeder gehört ein `WorldProvider` (Biom, Schlaf-Hook, Respawn, Wahl des Generators) und ein `IChunkProvider` (Terrain und die Dekorationen, die direkt im Generator stecken). Vier Generatoren (Utopia, Mining, Village, Crystal) sind Kopien des Vanilla-1.7.10-`ChunkProviderGenerate` mit demselben 5x33x5-Dichtefeld. Mining und Village haben zusätzlich Vanilla-Seen, Lavaseen, Dungeons und Strukturgeneratoren, Village einen dichteren Dorfgenerator (`MapGenMoreVillages`). Crystal ersetzt Stein durch Kyanite, baut in **jedem** Chunk ein Bedrock-Labyrinth auf Y 24-28 und platziert Kristalle, Bäume, Blumen und Spawn-Erze direkt im Chunk. Danger (intern „Islands") ist ein flacher 8-Schichten-Boden mit krummen Apfelbäumen. Chaos ist ein Nether-Generator mit invertierter Dichte, ohne Bedrock und ohne Lavameer: schwebende Steinlagen über dem Void. Die großen Strukturen, Inseln und Bosse jeder Dimension kommen **nicht** aus diesen Klassen, sondern aus `OreSpawnWorld.generate` (anderer Batch). Alles, was die Klassen dieses Batches selbst schreiben, bleibt im aktuellen Chunk: `OreSpawnMain.setBlockIDWithMetadataInChunk` und `getBlockIDInChunk` verwerfen fremde Chunk-Koordinaten (OreSpawnMain.java:5584-5612). Ein Chunkgrenzen-Risiko gibt es deshalb nur bei den Vanilla-`MapGen`-Strukturen. Die Kehrseite: Erzadern und Bäume werden am Chunkrand **abgeschnitten**, und ein Port ohne diesen Clip ändert Erzdichte und Baumformen messbar.

---

## Gemeinsame Grundlagen (gilt für alle zwölf Klassen)

### Registrierung und IDs

| Punkt | Wert | Herkunft |
|---|---|---|
| `BaseDimensionID` | Default 80 | OreSpawnMain.java:1138, (manifest) |
| Dimension-IDs | `DimensionID` = Base+0 … `DimensionID6` = Base+5 | OreSpawnMain.java:1266-1271 |
| `BaseBiomeID` | Default 120; Utopia +0, Islands +1, Crystal +2, Village +3, Chaos +4 | OreSpawnMain.java:1261-1265, (manifest) |
| Registrierung | `registerProviderType(id, WorldProviderOreSpawnN.class, true)`: drittes Argument `keepLoaded = true` (Deutung aus Forge-Signatur), alle sechs Welten bleiben geladen | OreSpawnMain.java:5039-5050 |
| Port-Ids | `orespawn:utopia`, `:mining`, `:village`, `:danger`, `:crystal`, `:chaos` | docs/DECISIONS.md R13 |

**Zugang** (anderer Batch, nur zur Einordnung): Rechtsklick mit leerer Hand; ist der Spieler nicht in der Zieldimension, geht es hinein, sonst zurück. `EntityAnt` → Utopia (EntityAnt.java:80-81), `EntityRedAnt` → Mining (EntityRedAnt.java:70-71), `EntityRainbowAnt` → Village (EntityRainbowAnt.java:45-46), `EntityUnstableAnt` → Danger (EntityUnstableAnt.java:45-46), `Termite` → Crystal (Termite.java:82-95), `EntityButterfly` → Chaos (EntityButterfly.java:264-265). Alle über `OreSpawnTeleporter`.

**Zusatz-Generierung pro Dimension** (anderer Batch, `OreSpawnWorld.generate`): Utopia ab OreSpawnWorld.java:28, Mining ab :43, Village ab :101, Danger ab :119, Crystal ab :176, Chaos ab :192.

### Config-Schalter, die dieser Batch liest

| Key (Kategorie) | Default | Wirkung hier | Herkunft |
|---|---|---|---|
| `LessOre` (OreSpawnTWEAKS) | 0 | Mining: `ChunkOreGenerator` 1× statt 3× | OreSpawnMain.java:1141, ChunkProviderOreSpawn2.java:173-176, (manifest) |
| `LessLag` (OreSpawnTWEAKS) | 0; geklemmt auf 0..2; `LessLag == 2` setzt `LessOre = 1` | Baumanzahl Danger/Chaos, Höhe Tall Crystal Tree | OreSpawnMain.java:1142, 1221-1243, (manifest) |
| `AlosaurusEnable` … `BaryonyxEnable` (OreSpawnMOBS) | alle 1 | Mining-Mobliste | ChunkProviderOreSpawn2.java:357-404, (manifest) |
| alle `*Enable` in `BiomeGenUtopianPlains` | 1 laut manifest-Stichprobe | Biom-Spawnlisten | BiomeGenUtopianPlains.java:10-436 |

### SRG-Namen in diesem Batch

| SRG | Bedeutung | Herkunft |
|---|---|---|
| `func_147424_a` | Dichtefeld in Blöcke umsetzen (Stein/Wasser/Luft) | joined.srg:12203 `ChunkProviderGenerate/func_147424_a`, in MCP unbenannt, Bedeutung aus Nutzung |
| `func_147423_a` | Dichtefeld 5x33x5 berechnen | joined.srg:12202, unbenannt, aus Nutzung |
| `func_147419_a` | Nether-Dichtefeld in Blöcke | joined.srg:12178 `ChunkProviderHell/func_147419_a`, unbenannt |
| `func_147422_a` | `replaceBlocksForBiome` | methods.csv:780 |
| `func_150573_a` | `genTerrainBlocks` | methods.csv:1629 |
| `func_151539_a` | `MapGenBase.generate` (Strukturstarts bzw. Höhlen) | methods.csv:1839 |
| `func_147416_a` | `findClosestStructure` | methods.csv:779 |
| `func_147478_e` | `canSnowAt` | methods.csv:814 |
| `func_150818_a` | `setExtBlockID` | methods.csv:1687 |
| `func_151599_a` | `NoiseGeneratorPerlin`, 2D-Region füllen | unbenannt, aus Nutzung |
| `func_143030_a` | `MapGenScatteredFeature`: Position liegt in einer Hexenhütte | unbenannt, aus Nutzung |
| `field_147431_j` … `field_147426_g`, `field_147434_q` | Rauschgeneratoren und -puffer, Dichtefeld | fields.csv ohne Treffer, aus Nutzung |
| `WorldChunkManagerHell` 2. Argument | `rainfall` (`field_76946_f`) | fields.csv:3866 |

### Vanilla-1.7.10-Grundspawns jedes `BiomeGenBase`

Der 1.7.10-Konstruktor `BiomeGenBase(int)` füllt jede neue Biominstanz mit Vanilla-Mobs vor (client-1.7.10.jar `ahu.<init>(int)` @171-461, javap; Klassen über joined.srg aufgelöst):

| Liste | Einträge (Gewicht/min-max) |
|---|---|
| creature | Sheep 12/4-4, Pig 10/4-4, Chicken 10/4-4, Cow 8/4-4 |
| monster | Spider 100/4-4, Zombie 100/4-4, Skeleton 100/4-4, Creeper 100/4-4, Slime 100/4-4, Enderman 10/1-4, Witch 5/1-1 |
| waterCreature | Squid 10/4-4 |
| ambient (`spawnableCaveCreatureList`) | Bat 10/8-8 |

`BiomeGenUtopianPlains` fügt seine Mobs **zusätzlich** hinzu (BiomeGenUtopianPlains.java:10-56). `setIslandCreatures`, `setCrystalCreatures` und `setChaosCreatures` ersetzen die Listen komplett, `setVillageCreatures` hängt nur an (BiomeGenUtopianPlains.java:62-70, 121-129, 192-252, 254-262). Fast alle OreSpawn-Flugtiere und Kleintiere stehen in `spawnableCaveCreatureList`, also in der Kategorie `ambient`. Im Port landen sie in `MobCategory.AMBIENT` mit Obergrenze 15 (MobCategory.java:10). Diese Grenze teilen sie sich mit allen anderen Ambient-Mobs der Dimension.

### Referenz: das 1.7.10-Rauschterrain (Utopia, Mining, Village, Crystal)

Die vier Generatoren enthalten bis auf den Füllblock identischen Code. Zeilen hier aus ChunkProviderOreSpawn.java, in den anderen drei um wenige Zeilen verschoben.

| Schritt | Detail | Zeile |
|---|---|---|
| Rauschquellen | `field_147431_j` 16 Oktaven, `field_147432_k` 16, `field_147429_l` 8, `field_147430_m` Perlin 4, `noiseGen5` 10 (ungenutzt), `noiseGen6` 16, `mobSpawnerNoise` 8 (ungenutzt); alle aus `new Random(weltseed)` in dieser Reihenfolge | 60-67 |
| Forge-Hook | `TerrainGen.getModdedNoiseGenerators` darf die Generatoren ersetzen | 76-84 |
| Biomgewichtung | `parabolicField[5x5] = 10 / sqrt(j²+k²+0.2)`, geteilt durch `rootHeight + 2`; halbiert, wenn Nachbar höher | 70-75, 205-208 |
| Gitter | 5x33x5 Stützstellen, Abstand 4 horizontal, 8 vertikal | 90, 180-183 |
| Tiefenrauschen | `noiseGen6`, Skala 200/200/0.5; `/8000`, negativ: Betrag·0.3; dann `*3-2`; negativ: `/2`, min -1, `/1.4/2`; positiv: max 1, `/8` | 180, 218-236 |
| Hauptrauschen | zwei 16-Oktaven-Felder, Skala 684.412, je `/512`; Mischfeld 8 Oktaven, Skala 8.55515/4.277575/8.55515, `(n/10+1)/2`, `denormalizeClamp` | 181-183, 248-251 |
| Grundhöhe | `f = heightVariation·0.9+0.1`, `f2 = (rootHeight·4-1)/8`, `d7 = (f2 + tiefe·0.2)·8.5/8`, `d9 = 8.5 + d7·4` (in Zellen, Y = 8·Zelle) | 216-217, 238-242 |
| Dichte | `dichte = rauschen - (j - d9)·12·128/256/f`, unterhalb der Grundhöhe ×4 | 244-251 |
| Deckel | Zellen 30-32 linear gegen -10 | 252-255 |
| AMPLIFIED | ist der Welttyp AMPLIFIED und `rootHeight > 0`: `root = 1+2·root`, `var = 1+4·var` | 59, 201-204 |
| Blöcke | Dichte > 0 → Füllblock; sonst Y < 63 → Wasser; sonst Luft (Wasser bis einschließlich Y 62) | 88, 123-131 |
| Interpolation | trilinear: 8 Blöcke je Zelle vertikal, 4×4 horizontal | 99-141 |
| Chunk-Seed | `x·341873128712 + z·132897987541` | 162 |

Höhen je Biom (Rechnung aus der Formel ohne Tiefenrauschen; das Tiefenrauschen verschiebt um -2.4 bis +0.85 Blöcke, Rechnung aus 218-242):

| Dimension | `rootHeight` / `heightVariation` | Herkunft | `d9` | Grundhöhe Y | Streckung `f` |
|---|---|---|---|---|---|
| Utopia, Village | 0.1 / 0.2 (`height_Default`, nie überschrieben) | client-1.7.10.jar `ahu.<clinit>` @6-17, `ahu.<init>` @30-46; MCP-Name über joined.srg `ahu/a` → `field_150596_a` | 8.18 | ≈ 65 | 0.28 |
| Mining (Extreme Hills) | 1.0 / 0.5 (`height_MidHills`) | `ahu.<clinit>` @117-127 und @302-305; `ahu/i` → `field_150603_i` | 10.09 | ≈ 81 | 0.55 |
| Crystal | 0.1 / 0.5 | WorldProviderOreSpawn5.java:28 | 8.18 | ≈ 65 | 0.55 |

Jede Dimension hat genau ein Biom, die Gewichtung ist also konstant und die Tabelle exakt. Crystal ist bei gleicher Grundhöhe etwa doppelt so hügelig wie Utopia.

---

### ChunkProviderOreSpawn

- **Rolle:** Generator der **Utopia**-Dimension. `WorldProviderOreSpawn.createChunkGenerator` erzeugt ihn mit `mapFeaturesEnabled = true` (WorldProviderOreSpawn.java:57-59). `makeString` liefert „UtopiaDimension" (ChunkProviderOreSpawn.java:297-299). `provideChunk` läuft beim Erzeugen jedes Chunks, `populate` beim Dekorieren.

- **Terrain** (`provideChunk`, 161-173):

  | Schritt | Detail | Zeile |
  |---|---|---|
  | Dichtefeld | Referenz oben, Füllblock `minecraft:stone`, Wasser bis Y 62 | 87-144, 175-261 |
  | Oberfläche | `genTerrainBlocks` des Utopia-Bioms (Vanilla-`BiomeGenBase`-Oberfläche, nicht in diesem Batch gelesen), Tiefenrauschen `field_147430_m`, Skala 0.0625 | 146-155 |
  | Höhlen | `MapGenCaves` | 49, 167 |
  | Schluchten | `MapGenRavine` | 55, 168 |
  | Erze | `OreSpawnMain.Chunker.generateOresInChunk` 1× (`ChunkOreGenerator`, OreSpawnMain.java:5447, anderer Batch) | 170 |
  | Chunkhöhe | `Block[65536]` → 256 | 163 |

- **Dekoration** (`populate`, 267-280): Biom an Chunk+16/+16 (271), Populate-Seed aus Weltseed (272-275), `biomegenbase.decorate` mit dem Utopia-Decorator (`treesPerChunk = -999`, `flowersPerChunk = 4`, `grassPerChunk = 6`; BiomeGenUtopianPlains.java:57-59; übrige Decorator-Defaults Vanilla, nicht gelesen), danach `SpawnerAnimals.performWorldGenSpawning` (278). `BlockFalling.fallInstantly` wird vorher und nachher auf `false` gesetzt (268, 279), bei Mining/Village/Crystal vorher auf `true`.

- **Keine Vanilla-Strukturen:** `strongholdGenerator`, `mineshaftGenerator` und `scatteredFeatureGenerator` werden angelegt (52-54), aber weder in `provideChunk` noch in `populate` benutzt. Nur `recreateStructures` ruft sie (358-364), und das platziert keine Blöcke. `func_147416_a` liefert trotzdem eine Stronghold-Position (350-352). Deutung aus dem Vanilla-Ablauf: Ein Eye of Ender zeigt auf eine berechnete, nie gebaute Position (nicht im Spiel belegt).

- **Mob-Spawns** (`getPossibleCreatures`, 301-348):

  | Kategorie | Ergebnis | Zeile |
  |---|---|---|
  | `monster` | `null`, also **keine** natürlichen Monster in Utopia, auch nicht die Vanilla-Grundmonster des Bioms | 315-317 |
  | `ambient` | Biomliste ohne `EntityHorse`, beim ersten Aufruf gecacht (`MyAmbientList`) | 318-334 |
  | `creature`, `waterCreature` | Biomliste ohne `EntityHorse`, bei jedem Aufruf neu gefiltert | 335-347 |

  Biomlisten: Vanilla-Grundspawns (Tabelle oben) plus Utopia-Grundliste:

  | Liste | OreSpawn-Einträge (Gewicht/min-max) | Zeile BiomeGenUtopianPlains.java |
  |---|---|---|
  | creature | Gazelle 10/2-4, Girlfriend 5/2-3, Boyfriend 5/2-3, RedCow 10/4-8, GoldCow 8/2-6, EnchantedCow 5/2-4 | 11, 17, 20, 23-25 |
  | ambient | Firefly 15/3-6, EntityButterfly 20/3-6, EntityLunaMoth 10/1-5, Chipmunk 3/1-2, Cockateil 10/2-4, GoldFish 1/1-1, Coin 2/1-1, Cricket 5/4-6 | 14, 28, 31, 34, 37, 40, 49, 52 |
  | waterCreature | Whale 1/1-1, Flounder 2/2-4, Frog 5/4-6 | 43, 46, 55 |

  Der `EntityHorse`-Filter ist wirkungslos, weil der `BiomeGenBase`-Grundkonstruktor kein Pferd einträgt (Tabelle oben). Die Recherche nennt „einige Feinde unterirdisch" (02-dimensions-worldgen.md:107-108). Natürlich spawnen sie laut Code nicht; Feinde kommen nur aus Spawnern und Strukturen.

- **Portierung 1.21.1:**
  - Java-`ChunkGenerator` mit eigenem `MapCodec` (DECISIONS R13). Das Dichtefeld lässt sich nicht als `noise_settings`-JSON nachbilden, weil dort das 1.7.10-Tiefenrauschen und die Parabelgewichtung fehlen. Die 1.7.10-`NoiseGeneratorOctaves`/`NoiseGeneratorImproved`/`NoiseGeneratorPerlin` 1:1 portieren, Seed-Reihenfolge wie in Zeile 60-67, sonst ändert sich jede Geländeform. Der `TerrainGen`-Hook entfällt.
  - Abstrakte Methoden, die der Generator liefern muss: `codec`, `applyCarvers`, `buildSurface`, `spawnOriginalMobs`, `getGenDepth`, `fillFromNoise`, `getSeaLevel`, `getMinY`, `getBaseHeight`, `getBaseColumn`, `addDebugScreenInfo` (ChunkGenerator.java:126-624). `getSeaLevel` = 63 (Wasser bis Y 62, Zeile 88/126).
  - Oberfläche in `buildSurface` als Port von `BiomeGenBase.genBiomeTerrain` (offen: nicht in diesem Batch gelesen, aus client-1.7.10.jar portieren).
  - Höhlen und Schluchten: als Port von `MapGenCaves`/`MapGenRavine` innerhalb des Chunks in `applyCarvers`. Alternativ Vanilla-Carver im Biom-JSON, das ist eine sichtbare Formabweichung.
  - `ChunkOreGenerator` schreibt nur in den Chunk → im Generator nach der Oberfläche aufrufen, mit Chunk-Clip.
  - `spawnOriginalMobs`: Anfangs-Spawns wie `performWorldGenSpawning`. Offen: die passende `NaturalSpawner`-Methode im sources-Jar belegen.
  - Utopia-Biom `orespawn:utopia` als Datapack-JSON: Temperatur 0.7, Downfall 0.5 (WorldProviderOreSpawn.java:15, 53), Blumen und Gras als Features, Spawnlisten ohne `monster`.
  - Chunkgrenzen-Risiko: keins, alles chunk-lokal.

### ChunkProviderOreSpawn2

- **Rolle:** Generator der **Mining**-Dimension („Dimension-Extreme"). Erzeugt von `WorldProviderOreSpawn2.createChunkGenerator` (WorldProviderOreSpawn2.java:51-53). `makeString` liefert irreführend „VillageDimension" (ChunkProviderOreSpawn2.java:345-347). Das deckt sich mit dem Hinweis der Recherche (02-dimensions-worldgen.md:80-84).

- **Terrain** (`provideChunk`, 163-179): wie Utopia (89-157, 181-267), Füllblock `minecraft:stone`, Biom Extreme Hills (Grundhöhe ≈ Y 81, Tabelle oben), Höhlen und Schluchten (169-170).
  - Erze: `ChunkOreGenerator.generateOresInChunk` 1×, bei `LessOre == 0` zwei weitere Male, also **3×** (172-176). Alle drei Läufe teilen `this.rand`, sind also verschieden.

- **Dekoration** (`populate`, 273-328), in dieser Reihenfolge:

  | # | Feature | Bedingung | Position | Zeile |
  |---|---|---|---|---|
  | 1 | Vanilla-Mineshaft, Stronghold, Scattered Feature (`generateStructuresInChunk`) | `mapFeaturesEnabled` (immer true) | Starts aus der Strukturkarte | 283-287 |
  | 2 | Wassersee `WorldGenLakes(water)` | Biom nicht Desert/DesertHills, 1/4 | x,z: Chunk + 8 + rand(16); Y rand(256) | 288-293 |
  | 3 | Lavasee `WorldGenLakes(lava)` | 1/8, dann Y < 63 oder 1/10 | Y = rand(rand(248)+8) | 294-301 |
  | 4 | Vanilla-Dungeon `WorldGenDungeons` | 8 Versuche | x,z Chunk + 8 + rand(16); Y rand(256) | 306-311 |
  | 5 | `biomegenbase.decorate` (Decorator von `BiomeGenHills`, Vanilla, nicht gelesen) | immer | | 312 |
  | 6 | Anfangs-Spawns `performWorldGenSpawning` | immer | | 313 |
  | 7 | Eis auf gefrierbarem Wasser, Schneeschicht (`canSnowAt`) | pro Spalte des um +8 versetzten 16x16-Feldes | `getPrecipitationHeight` | 316-326 |

  `BlockFalling.fallInstantly = true` während des Populates (274, 327). Seen und Dungeons liegen im um +8 versetzten Populate-Fenster, also im Vanilla-2x2-Chunk-Bereich.

- **Strukturfehler** (Deutung aus dem Vanilla-1.7.10-Ablauf, nicht im Spiel belegt): Die drei `MapGenStructure`-Generatoren werden in `provideChunk` **nicht** aufgerufen, nur in `populate` (284-286) und `recreateStructures` (423-429). Strukturstarts entstehen in 1.7.10 in `func_151539_a` (`generate`). Ohne diesen Aufruf beim Erzeugen gibt es Starts nur über `recreateStructures`, also für Chunks, die von der Platte geladen werden. Mineshafts, Strongholds und Tempel dürften damit fast nie oder nur bruchstückhaft entstehen. Tempel sind in Extreme Hills ohnehin nicht zulässig.

- **Mob-Spawns** (`getPossibleCreatures`, 349-413), `monster` und `ambient` beim ersten Aufruf gecacht:

  | Kategorie | Mob (Klasse) | Gewicht | Gruppe | Config | Zeile |
  |---|---|---|---|---|---|
  | monster | `Alosaurus` | 8 | 1-2 | `AlosaurusEnable` | 357-359 |
  | monster | `TRex` | 6 | 1-2 | `TRexEnable` | 360-362 |
  | monster | `Nastysaurus` | 6 | 1-2 | `NastysaurusEnable` | 363-365 |
  | monster | `Pointysaurus` | 10 | 4-8 | `PointysaurusEnable` | 366-368 |
  | monster | `GammaMetroid` | 35 | 4-7 | `GammaMetroidEnable` | 369-371 |
  | monster | `Alien` | 35 | 2-3 | `AlienEnable` | 372-374 |
  | monster | `CaveFisher` | 35 | 4-8 | `CaveFisherEnable` | 375-377 |
  | monster | `Cryolophosaurus` | 26 | 4-7 | `CryolophosaurusEnable` | 378-380 |
  | monster | `Spyro` | 5 | 1-2 | `SpyroEnable` | 381-383 |
  | monster | + Extreme-Hills-Monsterliste (Vanilla-Grundmonster, Tabelle oben) | | | | 384-386 |
  | ambient | `VelocityRaptor` | 1 | 2-4 | `VelocityRaptorEnable` | 393-395 |
  | ambient | `Dragonfly` | 2 | 1-3 | `DragonflyEnable` | 396-398 |
  | ambient | `Camarasaurus` | 1 | 2-4 | `CamarasaurusEnable` | 399-401 |
  | ambient | `Baryonyx` | 2 | 4-8 | `BaryonyxEnable` | 402-404 |
  | ambient | + Extreme-Hills-Ambientliste: Bat 10/8-8 plus die Oberwelt-`addSpawn`-Einträge von OreSpawn für `extremeHills`: butterfly 5/1-2, moth 8/1-2, mothra 2/1-1, bird 10/1-2, ghost 15/3-6, ghost_pumpkin_skelly 15/3-6, cassowary 5/1-2, ender_knight 4/2-4, ender_reaper 2/1-2 | | | | 405-407; (manifest, `make_some_more_things`) |
  | creature, waterCreature | unverändert Extreme Hills (Vanilla-Grundspawns) | | | | 412 |

  Zwei Abweichungen von der Recherche:
  - Sie ordnet die vier Mining-Mobs „ambient/creature" zu (02-dimensions-worldgen.md:165); laut Code sind es nur `ambient`.
  - Die Website nennt Mothra als feindlich in Mining. Mothra stammt nicht aus dieser Klasse, sondern aus der geerbten Extreme-Hills-Ambientliste (manifest).

  Nur `BiomeGenBase.extremeHills` zählt, nicht `extremeHillsEdge`/`Plus`: Der Provider nutzt genau dieses Objekt (WorldProviderOreSpawn2.java:21).

- **Portierung 1.21.1:**
  - Java-`ChunkGenerator` wie Utopia, Grundwerte `height_MidHills` fest im Codec.
  - **Eigenes Biom** `orespawn:mining` als Kopie von `minecraft:windswept_hills` (biome_map.json: `extremeHills` → `minecraft:windswept_hills`) mit Temperatur 0.8, Downfall 0.01 (WorldProviderOreSpawn2.java:22). Die Spawnliste enthält die Dino-Einträge, die `extremeHills`-Regeln aus R11 und die Vanilla-Grundspawns. Die Dino-Spawns dürfen **nicht** an `windswept_hills` selbst, sonst spawnen sie in der Oberwelt.
  - Seen, Lavaseen, Monster-Rooms: `PlacedFeature` im Biom-JSON mit `rarity_filter` 4 bzw. 8 und Höhenverteilung wie oben. Offen: ob 1.21.1 noch ein Wassersee-Feature hat (`ConfiguredFeature`-Registry im sources-Jar prüfen); sonst eigenes Feature als Port von `WorldGenLakes`.
  - 3× Erzlauf: Placement-Modifier aus der COMMON-Config (R12), Faktor 3 bzw. 1 nach `LessOre`.
  - Vanilla-Strukturen: 1:1 hieße „praktisch keine". Entscheidung offen (siehe `unresolved`).
  - Chunkgrenzen: Generatorteil chunk-lokal; Seen/Dungeons als Vanilla-Features im 3x3-Bereich.

### ChunkProviderOreSpawn3

- **Rolle:** Generator der **Village**-Dimension („Dimension-VillageMania"). Erzeugt von `WorldProviderOreSpawn3.createChunkGenerator` (WorldProviderOreSpawn3.java:59-61). `makeString` liefert „MiningDimension" (ChunkProviderOreSpawn3.java:341-343).

- **Terrain** (`provideChunk`, 158-174): wie Utopia (84-152, 176-262), `minecraft:stone`, Grundhöhe ≈ Y 65, Höhlen und Schluchten (164-165), Erze 1× (171).
  - Anders als Mining laufen `villageGenerator` (`MapGenMoreVillages`) und `scatteredFeatureGenerator` **in `provideChunk`** (166-169). Ihre Starts entstehen also korrekt. Mineshaft und Stronghold laufen nur in `populate` (279, 281), dieselbe Lücke wie bei Mining (Deutung).

- **Dörfer** (`MapGenMoreVillages`, Unterklasse von `MapGenVillage`, anderer Batch):

  | Parameter | Wert | Vanilla 1.7.10 | Herkunft |
  |---|---|---|---|
  | Rasterabstand | 9 Chunks | 32 | MapGenMoreVillages.java:12; client-1.7.10.jar `avn.<init>` @5 bipush 32 |
  | Mindestabstand | 7 → Versatz `nextInt(9-7)` = 0..1 Chunk | 8 | MapGenMoreVillages.java:13, 30-31; `avn.<init>` @11 bipush 8 |
  | Salz | 10387312 | | MapGenMoreVillages.java:27 |
  | Biomprüfung | `areBiomesViable` wird berechnet, das Ergebnis aber **ignoriert**: jede Rasterzelle bekommt einen Dorfstart | | MapGenMoreVillages.java:32-35 |
  | Dorfgröße | Default-Konstruktor, keine Größenangabe | | ChunkProviderOreSpawn3.java:49 |

- **Dekoration** (`populate`, 268-324): identisch zu Mining (Tabelle dort), mit einem Unterschied: `flag = villageGenerator.generateStructuresInChunk(...)` (280). Steht ein Dorfteil im Chunk, entfallen Wassersee und Lavasee (284, 290). Danach Dungeons (302-307), `decorate` (308), Anfangs-Spawns (309), Eis/Schnee (312-322).
  - Das Villages-Biom hat den Decorator des Grundkonstruktors: Bäume -999, Blumen 4, Gras 6 (BiomeGenUtopianPlains.java:57-59).

- **Mob-Spawns** (`getPossibleCreatures`, 345-348): `monster` innerhalb einer Hexenhütte → Hexenliste, sonst Biomliste. Das Villages-Biom steht nicht in der Biomliste von `MapGenScatteredFeature` → Hütten praktisch ausgeschlossen (Deutung). Biomliste siehe `WorldProviderOreSpawn3`.

- **Portierung 1.21.1:**
  - Java-`ChunkGenerator` wie Utopia.
  - Dörfer: **Structure**, kein Feature, weil ein Dorf viele Chunks überspannt. Eigenes `structure_set` `orespawn:dense_village` mit `random_spread`, `spacing` 9, `separation` 7, `salt` 10387312. Die Struktur ist eine Kopie von `minecraft:village_plains`, ihr Biom-Tag enthält `orespawn:villages`. Den Vanilla-Tag nicht verändern, sonst ändert sich die Oberwelt. Die JSON-Schlüssel gegen den Codec im sources-Jar prüfen. Abweichung: 1.21.1-Jigsaw-Dörfer sehen anders aus als 1.7.10-Dörfer.
  - Seen/Dungeons wie Mining; die Unterdrückung im Dorf-Chunk über einen eigenen Placement-Filter „kein Strukturstart in der Nähe" oder hinnehmen (Entscheidung offen).
  - Chunkgrenzen: Dörfer sind Structure; Rest chunk-lokal.

### ChunkProviderOreSpawn4

- **Rolle:** Generator der **Danger**-Dimension („Dimension-Islands"). Erzeugt von `WorldProviderOreSpawn4.createChunkGenerator` (WorldProviderOreSpawn4.java:58-60). `makeString` „DangerDimension" (ChunkProviderOreSpawn4.java:94-96). Kein Rauschen, keine Erze, keine Höhlen.

- **Terrain** (`provideChunk`, 42-64): flacher Boden, direkt in `ExtendedBlockStorage` geschrieben.

  | Y | Block | Zeile |
  |---|---|---|
  | 0 | `minecraft:bedrock` | 26-28 |
  | 1-6 | `minecraft:dirt` | 32-34 |
  | 7 | `minecraft:grass` (1.21.1 `minecraft:grass_block`) | 29-31 |
  | ab 8 | Luft | 21, 44-60 |

  Metadaten alle 0 (22, 56). Die Recherche nennt die Schichthöhe „nicht ausgelesen" (02-dimensions-worldgen.md:215); hiermit belegt.

- **Scraggly Trees** (`addScragglyTrees` → `ScragglyTreeWithBranches` → `makeScragglyBranch`), aufgerufen in `provideChunk` (61):

  | Punkt | Wert | Zeile |
  |---|---|---|
  | Anzahl pro Chunk | `1 + rand(10)` = 1..10; `LessLag 1`: /2, `LessLag 2`: /4 (Ganzzahl, 0 → keine) | 115-124 |
  | Position | x,z = Chunk + 2 + rand(12) | 126-127 |
  | Wurzelsuche | Y 20 abwärts bis 3, erstes Gras darunter → Stamm ab Y 8 | 128-133 |
  | Stamm | `i = 1 + rand(3)` Blöcke `minecraft:log` Meta 0 (1.21.1 `minecraft:oak_log`), senkrecht | 182, 184-190 |
  | Zufallsweg | bis `j = i + rand(12)`; je Schritt x,z ∈ {-1,0,+1} (`rand(2)-rand(2)`), 3/4 aufwärts | 183, 192-203 |
  | Abbruch | Zielblock nicht Luft/Log/Apple Leaves | 199-202 |
  | Ast | Chance 1/4 je Schritt, Länge `rand(1 + j - k)`, Richtung Bias ±1 geklemmt, 2/3 aufwärts | 204-206, 137-161 |
  | Laub | je Weg- und Astblock: jedes Feld des 3x3-Rings 50 %, darüber 50 %; nur in Luft; Block `orespawn:leaves_apple` (`MyAppleLeaves`, (manifest)) | 207-222, 162-177 |
  | Größe | höchster Log etwa 14 Blöcke über dem Boden, Laub +1; seitlich unbegrenzt, **am Chunkrand abgeschnitten** | Rechnung aus 182-183, 191-198, 205 |

- **Populate** (70-77): setzt nur den Populate-Seed, sonst nichts. Also **kein** Biom-Decorator und **keine** Anfangs-Spawns.
- **Mob-Spawns** (98-101): Biomliste Islands (`setIslandCreatures`, siehe `WorldProviderOreSpawn4`). `func_147416_a` → null (103-105).
- **Determinismus:** `this.random` wird im Konstruktor (24) und in `populate` (73-76) geseedet, nicht in `provideChunk`. Die Bäume hängen damit von der Reihenfolge der Chunk-Erzeugung ab und sind **nicht** seed-reproduzierbar.

- **Portierung 1.21.1:**
  - Kleiner Java-`ChunkGenerator` mit eigenem Codec (R13), Schichten wie oben. `FlatLevelSource.spawnOriginalMobs` ist leer (FlatLevelSource.java:137-138), das passt zum leeren `populate`. Datapack-`minecraft:flat` wäre technisch möglich, R13 verlangt aber einen Codec je Dimension.
  - Bäume: Feature pro Chunk, das nur in den Ursprungs-Chunk schreibt (Clip wie im Original, sonst sehen die Bäume anders aus). Seed pro Chunk, eine harmlose Abweichung vom nicht-deterministischen Original.
  - Inseln und D4-Strukturen gehören zu `OreSpawnWorld` (anderer Batch). Dessen `D4BigSpaceCheck(world, x, 7, z)` (OreSpawnWorld.java:120) setzt die Grasebene Y 7 voraus → Schichthöhen exakt übernehmen.
  - Chunkgrenzen-Risiko: keins.

### ChunkProviderOreSpawn5

- **Rolle:** Generator der **Crystal**-Dimension. Erzeugt von `WorldProviderOreSpawn5.createChunkGenerator` (WorldProviderOreSpawn5.java:59-61). `makeString` „CrystalDimension" (ChunkProviderOreSpawn5.java:345-347). Fast alle Dekoration läuft in `provideChunk`, nicht in `populate`.

- **Terrain:** Dichtefeld wie Utopia (71-128, 223-309) mit Füllblock `orespawn:crystalstone` („Kyanite", `CrystalStone`, 108, (manifest)), Wasser bis Y 62 (110-111), Biomhöhe 0.1/0.5 (Tabelle oben). Keine Höhlen, keine Schluchten, keine `ChunkOreGenerator`-Erze.

- **Oberfläche** (`MygenBiomeTerrain`, 144-198, eigener Ersatz für `genTerrainBlocks`; Tiefe `l = stoneNoise/3 + 3 + rand·0.25`, 150):

  | Fall | Ergebnis | Zeile |
  |---|---|---|
  | Y ≤ rand(5) (also Y 0..4, je Block neu gewürfelt) | `minecraft:bedrock` | 156-157 |
  | oberster Kyanite-Block einer Säule, `l ≤ 0` | Deckblock entfällt, Füllung Kyanite | 164-168 |
  | oberster Kyanite-Block in Y 59..64 | Deck `orespawn:crystalgrass`, Füllung Kyanite | 169-173 |
  | Deckblock leer und Y < 63 | Deck = Wasser | 174-177 |
  | Y ≥ 62 | Deckblock setzen, sonst Füllblock | 179-185 |
  | darunter `l` Blöcke | Füllblock Kyanite (optisch unverändert) | 187-190 |

  Ergebnis: Land ab Y 62 trägt Crystal Grass, Seegrund ist Kyanite. Erde gibt es nicht.

- **Features in `provideChunk`** (204-221), in dieser Reihenfolge:

  | # | Feature / Methode | Chance | Anzahl | Ort und Suche | Blöcke | Größe | Zeile |
  |---|---|---|---|---|---|---|---|
  | 1 | Crystal Maze `CrystalMaze.buildCrystalMaze(world, cx, 25, cz, chunk)` | jeder Chunk | 1 | Chunk-Ursprung, Y 24-28 | Wände, Boden und Decke `minecraft:bedrock`; 4 Kyanite in der Decke, 1 im Boden | 16x5x16 | 211-212 |
  | 2 | Pink Tourmaline `addPinkTourmaline` | 1/30 | 1..10 Säulen | Start x,z Chunk+3+rand(10), Y 30..34; ersetzt **jeden** Block, auch Luft/Wasser | `orespawn:crystalcrystal` | Querschnitt 1x1 oder 2x2 (`width = rand(2)`), 2..19 Lagen, je Lage dy 0.5..1, dx/dz -1..1 | 366-393 |
  | 3 | Tiger's Eye `addTigersEye` | 1/30 | 1..5 Säulen | Start x,z Chunk+3+rand(10), Y 5..9; ersetzt jeden Block | `orespawn:tigerseye` | 1x1, 1..6 Lagen | 395-422 |
  | 4 | Kristallbäume `addCrystalTrees` | 1/5 | Typ `rand(5)`: 0 → Tall (`rand(8)` = 0..7 Stück), sonst Scraggly (`rand(8)·2` = 0..14) | x,z Chunk+4+rand(8); Y 128 abwärts bis 41: Luft über Crystal Grass | siehe unten | | 424-452 |
  | 5 | Kristallerze `generateCrystalOres` | jeder Chunk | Tabelle unten | | | | 610-696 |
  | 6 | Blumen `addCrystalFlowers` | 1/3 | 1..13, **eine** Farbe pro Chunk (`rand(4)`) | x,z Chunk+rand(16); Y 128→41 über Crystal Grass | `orespawn:crystalflower_red`/`_green`/`_blue`/`_yellow` | 1 Block | 772-808 |
  | 7 | Reis `addRice` | 1/10 | 5 Versuche | wie Blumen | `orespawn:rice_plant` Meta 0 | 1 Block | 740-754 |
  | 8 | Quinoa `addQuinoa` | 1/20 | 5 Versuche | wie Blumen | `orespawn:quinoa_0` Meta 0 | 1 Block | 756-770 |

  Registry-Ids und Namen aus dem manifest: `crystalstone` „Kyanite", `crystalgrass` „Crystal Grass", `crystalcrystal` „Pink Tourmaline", `tigerseye` „Tiger's Eye", `crystaltreelog` „Crystal Tree Wood", `crystaltreeleaves`/`crystaltreeleaves2` „Crystal Tree Leaves", `quinoa_0` „Quinoa Plant", `rice_plant` ohne Lang-Namen (manifest).

- **Crystal Maze im Detail** (CrystalMaze.java, anderer Batch; hier nur die Wirkung):

  | Schritt | Detail | Zeile CrystalMaze.java |
  |---|---|---|
  | Freiräumen | Luft 16x3x16 auf Y 25-27 | 18-24 |
  | Labyrinth | 4×4 Zellen à 4 Blöcke (Prim-artig, Zellwände doppelt), Wände `minecraft:bedrock` (`b = 1`) | 25, 60-130, 132-136 |
  | Öffnen | Außenreihen x=0, z=0, x=15, z=15 wieder Luft → offen zu den Nachbar-Chunks | 29-43 |
  | Boden/Decke | Y 24 und Y 28 vollflächig Bedrock | 44-49 |
  | Durchbrüche | 4 zufällige Kyanite-Blöcke in der Decke, 1 im Boden (Positionen können sich decken) | 50-57 |
  | Zufall | Muster aus `Math.random()`, Löcher aus `world.rand` → nicht reproduzierbar | 274-276, 51-56 |

  Folge: Unter der gesamten Dimension liegt eine durchgehende Labyrinthebene Y 25-27 zwischen zwei Bedrock-Lagen. Nach unten kommt man nur durch die Kyanite-Löcher (Deutung). Die Recherche nennt Wände aus „Obsidian/Bedrock" (02-dimensions-worldgen.md:269). Obsidian wird nur bei `b == 0` gesetzt, und dieser Aufruf übergibt `b = 1` (CrystalMaze.java:25, 133-136), also nur Bedrock.

- **Tall Crystal Tree** (`TallCrystalTree`, 543-608; Formen aus `world.rand`):

  | Punkt | Wert | Zeile |
  |---|---|---|
  | Grundstamm | `i = 10 + rand(12)` = 10..21; `LessLag 1`: -2, `LessLag 2`: -4 | 544-550 |
  | Gesamtstamm | `j = i + rand(18 - LessLag·2)` → bis 38 Logs | 551-566 |
  | Block | `orespawn:crystaltreelog` | 557, 566 |
  | Zwischenlaub | bei `k % 4 == 0`: 3x3-Ring je Feld 50 % `orespawn:crystaltreeleaves` | 567-578 |
  | Krone | eine Ebene über dem letzten Log: 3x3 Log je 50 %, auf derselben Ebene 7x7 Laub, wo Luft; darüber 3x3 Laub | 580-607 |
  | Footprint | 7 x ≤ 40 x 7; bei x,z 4..11 im Chunk vollständig innen | Rechnung aus 435-436, 544-607 |

- **Scraggly Crystal Tree** (`ScragglyCrystalTreeWithBranches`, 498-541; `makeScragglyCrystalBranch`, 454-496): gleiches Muster wie der Danger-Baum, aber Stamm `1 + rand(2)` (499), Weg bis `i + rand(8)` (500), Log `crystaltreelog`, Laub `orespawn:crystaltreeleaves2` (`MyCrystalLeaves2`), Formen aus `world.rand`, am Chunkrand abgeschnitten.

- **Kristallerze** (`generateCrystalOres`): alle über `generateOre` (698-738), einen Port von `WorldGenMinable`, der **nur Kyanite ersetzt** (727-729). Das Aderzentrum liegt bei Start **+8** in x und z (700-703), also bei Chunk-Offset 10..21. `LessOre` wirkt hier **nicht**.

  | Erz | Versuche pro Chunk | Y | Größe | Blöcke | Zeile |
  |---|---|---|---|---|---|
  | Ancient Dried Spawn Eggs | `25 + rand(30)`, bei 1/20 zusätzlich +30 | Y = rand(128), nur Y > 45 | 4 | gleichverteilt `rand(11)`: `oreurchin`, `oreflounder`, `oreskate`, `orerotator`, `orepeacock`, `orefairy`, `oredungeonbeast`, `orevortex`, `orerat`, `orewhale`, `oreirukandji` | 611-670 |
  | Crystal Energy `crystalcoal` | `3 + rand(8)` | 0..127 | 6 | | 671-677 |
  | Crystalized Rats `crystalrat` | `15 + rand(20)` | nur Y < 25 | 6 | | 678-686 |
  | Crystalized Fairies `crystalfairy` | `12 + rand(20)` | nur Y < 25 | 6 | | 687-695 |

  **Clip-Wirkung** (Rechnung: Monte-Carlo über 40 000 Adern je Größe, exakter Nachbau von `generateOre` 698-738, alle Zielblöcke als Kyanite angenommen): Größe 4 setzt ungeclippt im Mittel 2.38 Blöcke, im Chunk bleiben 0.70. Größe 6: 4.41 → 1.30. In beiden Fällen bleiben **etwa 29 %**. Ein Port ohne Clip würde die Crystal-Erze gut dreimal so dicht setzen.

- **Populate** (315-328): Populate-Seed, `decorate` mit dem Crystal-Decorator (Blumen, Gras, Bäume, Pilze, Riesenpilze, Zuckerrohr alle -999; BiomeGenUtopianPlains.java:184-189), Anfangs-Spawns. Die übrigen Vanilla-Decorator-Schritte (Erze, Quellen) zielen auf Stein und finden hier keinen (Deutung, Vanilla-Decorator nicht gelesen).
- **Mob-Spawns** (349-352): Biomliste Crystal (siehe `WorldProviderOreSpawn5`). `func_147416_a` → null (810-812).
- **Determinismus:** Positionen und Chancen aus `this.rand` (pro Chunk geseedet, 205) sind reproduzierbar; Labyrinth, Maze-Löcher und Baumformen nicht.

- **Portierung 1.21.1:**
  - Java-`ChunkGenerator`: Dichtefeld mit Kyanite, eigene Oberfläche, dann alle acht Features in fester Reihenfolge direkt über `ChunkAccess.setBlockState` im Generator (z. B. am Ende von `buildSurface`). Sie schreiben ausschließlich in den eigenen Chunk und hängen von der Reihenfolge ab: Labyrinth vor Kristallen, Kristalle vor Bäumen, Erze nur in verbliebenem Kyanite.
  - Kein Datapack-Feature nötig. Als `PlacedFeature` ginge es auch, dann muss aber der Chunk-Clip nachgebaut werden (siehe Clip-Wirkung).
  - Zufall: pro Chunk seeden, auch wo das Original `world.rand`/`Math.random()` nutzt (Abweichung ohne Spielwirkung).
  - Die Bedrock-Doppellage Y 24/28 unbedingt übernehmen: Rats/Fairies-Erze und Tiger's Eye liegen darunter.
  - Chunkgrenzen-Risiko: keins. Das Labyrinth ist 16x5x16 und chunk-bündig.

### ChunkProviderOreSpawn6

- **Rolle:** Generator der **Chaos**-Dimension. Erzeugt von `WorldProviderOreSpawn6.createChunkGenerator` ohne `mapFeaturesEnabled` (WorldProviderOreSpawn6.java:59-61). `makeString` „ChaosDimension" (ChunkProviderOreSpawn6.java:323-325). Code-Basis ist der Vanilla-`ChunkProviderHell` (`func_147419_a`, joined.srg:12178), mit invertierter Dichte-Logik.

- **Rauschen** (`initializeNoiseField`, 200-293):

  | Punkt | Wert | Zeile |
  |---|---|---|
  | Generatoren | `netherNoiseGen1` 16 Oktaven, `2` 16, `3` 8, `slowsandGravelNoiseGen` 4, `netherrackExculsivityNoiseGen` 4, `6` 10, `7` 16; alle aus `hellRNG = new Random(weltseed)` | 43-51 |
  | Gitter | 5x17x5, 4 Blöcke horizontal, 8 vertikal → Generierung nur Y 0..127 | 64-70, 189 |
  | Skalen | d0 684.412, d2 2053.236; Mischfeld `d0/80`, `d2/60`, `d0/80` | 207-213 |
  | Hauptdichte | `lerp(n1/512, n2/512, clamp((n3/10+1)/2)) - profil[j]` | 257-272 |
  | Profil | `cos(j·π·6/17)·2`; in den äußeren vier Zellen (Randabstand `d3` < 4) zusätzlich `-(4-d3)³·10` → Zelle 0/16: -640, 1/15: -270, 2/14: -80, 3/13: -10 | 214-226 |
  | Deckel | Zellen 14-16 linear gegen -10 | 273-276 |
  | Bodenrampe | `d5 = 0.0`, Bedingung `j2 < d5` nie wahr → toter Code | 233, 277-286 |
  | ungenutzt | `noiseData4`, `noiseData5` und daraus `d4`, `d6` fließen nirgends ein | 209-210, 229-255 |
  | **Blockwahl** | Dichte > 0 → **Luft**, sonst `minecraft:stone`. Der Vanilla-Nether setzt bei > 0 den Füllblock; hier ist es invertiert | 96-100 |
  | Lavameer | `b2 = 32` wird deklariert, aber nie abgefragt → **kein Lavameer** | 65 |

  Deutung (gerechnet, nicht visuell belegt): Die Randterme machen die Zellen 0-2 und 14-16 stark positiv, dort ist fast immer Luft. In den Zellen 3-13, etwa Y 24 bis 111, entscheidet das Rauschen; Stein entsteht dort, wo es negativ ist. Das passt zur Website-Aussage „nothing underneath" (02-dimensions-worldgen.md:300-302).

- **Oberfläche** (`replaceBiomeBlocks`, 117-181; Referenz-Y `b0 = 64`):

  | Fall | Ergebnis | Zeile |
  |---|---|---|
  | Y ≥ 127 - rand(5) oder Y ≤ rand(5) (je Block neu gewürfelt) | Luft → **kein Bedrock**, ausgefranster Rand oben und unten | 133, 175-177 |
  | Startwerte je Spalte | Deck `minecraft:grass`, Füllung `minecraft:dirt` | 129-130 |
  | Deckschicht einer Steinsäule, Tiefe `i1 ≤ 0` | Deck Luft, Füllung Stein | 138-141 |
  | Deckschicht in Y 60..65 | Stein/Stein; ist Kies- **oder** Seelensand-Rauschen > 0 → Gras/Erde | 142-153 |
  | Y ≥ 63 | Deckblock, darunter Füllblock | 157-163 |
  | Tiefe | `i1 = rauschen/3 + 3 + rand·0.25` Blöcke Füllung | 127, 165-168 |
  | Wasser | Bedingung `k2 < 64 && block == Blocks.air`; `block` ist nur null, Stein oder Gras, nie `Blocks.air` → **nie Wasser** | 154-156 |

  Deck- und Füllblock bleiben über die ganze Spalte gesetzt (129-130). Deckschichten außerhalb Y 60..65 tragen deshalb den zuletzt gewählten Wert. Die Recherche nennt Wasser im Chaos-Terrain (02-dimensions-worldgen.md:298-299); der Terrain-Code setzt keins, Wasser kann nur aus dem Vanilla-Biom-Decorator stammen.

- **Weitere Schritte in `provideChunk`** (187-198): `hellRNG` pro Chunk geseedet (188), `Block[32768]` (189), `ChunkOreGenerator` 1× mit **`worldObj.rand`** (194), Scraggly Trees (195).

- **Scraggly Trees** (343-456): Code identisch zu `ChunkProviderOreSpawn4` (Stamm `1+rand(3)`, Weg bis `+rand(12)`, `minecraft:log`, `orespawn:leaves_apple`), mit diesen Unterschieden:

  | Punkt | Wert | Zeile |
  |---|---|---|
  | Chance | 1/4 pro Chunk (`world.rand`), nach dem Würfeln der Anzahl | 344-347 |
  | Anzahl | `1 + rand(5)`; `LessLag` /2 bzw. /4 | 344, 348-356 |
  | Position | Chunk+2+rand(12) aus `this.random` (nie neu geseedet) | 358-359 |
  | Wurzelsuche | Y 120 abwärts bis 51, Gras darunter | 360-365 |

- **Populate** (299-306): kein Seed, `decorate` mit Chaos-Decorator (Blumen 2, Gras 4, Bäume 1, Pilze/Riesenpilze/Zuckerrohr -999; BiomeGenUtopianPlains.java:263-268), Anfangs-Spawns, beides mit `worldObj.rand`. `BlockFalling.fallInstantly = false` (300).
- **Mob-Spawns** (327-330): Biomliste Chaos (siehe `WorldProviderOreSpawn6`). `func_147416_a` → null (332-334).
- **Determinismus:** Terrain reproduzierbar; Erze, Bäume und Decorator nicht.

- **Portierung 1.21.1:**
  - Java-`ChunkGenerator` mit Port von `initializeNoiseField` und `func_147419_a` inklusive Inversion und ohne Lava. Der Generator füllt nur Y 0..127, die Welthöhe bleibt trotzdem 0..256 (das Original-Chunkformat hat 256 Blöcke, nur das Generator-Array ist halb so hoch).
  - Vanilla-Baum des Decorators (1 pro Chunk) → Vanilla-Baum-`PlacedFeature` im Biom `orespawn:chaos`; Blumen und Gras ebenso. Scraggly Trees wie bei Danger als chunk-lokales Feature.
  - `dimension_type` ohne Decke, Void darunter. Spieler fallen ins Void wie im Original.
  - Chunkgrenzen-Risiko: keins.

---

### WorldProviderOreSpawn

- **Rolle:** Provider der **Utopia**-Dimension, registriert unter `DimensionID` (OreSpawnMain.java:5039-5040). Forge instanziiert ihn beim Laden der Welt; wegen `keepLoaded` geschieht das beim Serverstart (Deutung).
- **Biom:** Der Konstruktor legt `new BiomeGenUtopianPlains(BiomeUtopiaID)` an: Kartenfarbe 353825 (dieselbe Zahl wie das Vanilla-Biom mit ID 4, `BiomeGenForest`: client-1.7.10.jar `ahu.<clinit>` @318-329, joined.srg `aif`), Name „Utopia", Temperatur 0.7, Regen 0.5 (WorldProviderOreSpawn.java:15). `registerWorldChunkManager`: `WorldChunkManagerHell(MyPlains, 0.5)`, also ein einziges Biom mit `rainfall` 0.5 (52; fields.csv:3866); dann erneut 0.7/0.5 (53) und `dimensionId = DimensionID` (54).
  - Jede Provider-Instanz erzeugt ein **neues** Biomobjekt unter derselben ID (Deutung: der 1.7.10-Biomkonstruktor trägt sich in die globale Biomliste ein, `ahu.<init>` @156-167).
  - Spawnlisten: Vanilla-Grundspawns plus Utopia-Grundliste (Tabellen bei `ChunkProviderOreSpawn`); `monster` filtert der Generator weg.
- **Name:** `getDimensionName` „Dimension-Utopia" (18-20).
- **Respawn:** `canRespawnHere` = true (22-24). MCP-Doku: „True if the player can respawn in this dimension (true = overworld, false = nether)" (methods.csv:3748). Deutung aus dem 1.7.10-Forge-Ablauf: Betten funktionieren, und wer hier stirbt, respawnt in dieser Dimension, nicht in der Oberwelt.
- **Tag/Nacht** (`setWorldTime`, 26-49): Ist `time % 24000 > 12000` und schlafen alle Spieler **dieser** Dimension, wird für **alle** geladenen Welten die Zeit auf das nächste Vielfache von 24000 gesetzt (31-37). Sonst Standardverhalten (39, 43, 47). Es gibt also einen normalen Tag-Nacht-Zyklus. **Korrigiert (Fixwelle W05, 2026-09-11):** Schlafen erreicht diesen Zweig **nie**. `WorldServer.tick` (client-1.7.10.jar `mt.b()`) setzt beim Schlafen zuerst `i - i % 24000`, ein Vielfaches von 24000, also ist `time % 24000 > 12000` falsch und der Aufruf endet im leeren `super.setWorldTime` des `DerivedWorldInfo`; danach weckt `mt.d()` alle Spieler, und das `time + 1` des nächsten Schritts sieht keine Schläfer mehr. Schlafen in der Dimension änderte also keine Uhr. Erreichbar war der Zweig nur über ein externes `setWorldTime` (`/time set`, `/time add`) bei schlafenden Spielern. Port: kein Handler, siehe `world/gen/LegacyWorldProvider.java`.
- **Himmel, Nebel, Wolken, Sterne:** nicht überschrieben → `WorldProvider`-Standard der Oberwelt. Die Himmelsfarbe folgt der Biomtemperatur; die Konstante 0.62222224 steht im 1.7.10-Biom (client-1.7.10.jar `ahu` @566).
- **Generator:** `new ChunkProviderOreSpawn(world, seed, true)` (57-59).

- **Portierung 1.21.1:**
  - `data/orespawn/dimension/utopia.json`: Generator `orespawn:utopia` (eigener Codec), Biomquelle `fixed` → `orespawn:utopia`.
  - `data/orespawn/dimension_type/utopia.json`. Record-Felder laut DimensionType.java:28-44: `fixedTime` leer, `hasSkyLight` true, `hasCeiling` false, `ultraWarm` false, `natural` true, `coordinateScale` 1.0 (Deutung: im Original nicht überschrieben), `bedWorks` true, `respawnAnchorWorks` false, `minY` 0, `height` 256, `logicalHeight` 256 (256 aus `Block[65536]`, ChunkProviderOreSpawn.java:163), Effekte der Oberwelt. Die JSON-Schlüssel sind **nicht** die Record-Feldnamen: aus dem Codec derselben Datei ablesen.
  - Biom-JSON: `temperature` 0.7, `downfall` 0.5, `has_precipitation` true. `sky_color` = `OverworldBiomes.calculateSkyColor(0.7F)` (OverworldBiomes.java:33-36, `Mth.hsvToArgb` Mth.java:543-588) = **7972607** (Rechnung, exakter Nachbau der beiden Methoden). `fog_color` 12638463, `water_color` 4159204 und `water_fog_color` 329011 wie bei Vanilla-Oberweltbiomen (OverworldBiomes.java:63, 596). Abweichung: 1.7.10 berechnet die Himmelsfarbe positionsabhängig, 1.21.1 hat einen festen Wert.
  - Schlaf-Hook: `ServerLevel.tick` feuert `SleepFinishedTimeEvent` auf dem Level, in dem geschlafen wird, und setzt dann `setDayTime` auf **diesem** Level (ServerLevel.java:348-353; Event-Klasse `net.neoforged.neoforge.event.level.SleepFinishedTimeEvent`, `getNewTime()`/`setTimeAddition`). Port: Handler für die sechs OreSpawn-Level, der bei `dayTime % 24000 > 12000` `server.overworld().setDayTime(event.getNewTime())` setzt. Offen: belegen, dass `setDayTime` auf einem abgeleiteten Level wirkungslos ist (`DerivedLevelData`), sonst genügt Vanilla. Abweichung: 1.21.1 prüft `playersSleepingPercentage` statt „alle". **Überholt (Fixwelle W05):** der Handler bildete einen Nachtsprung nach, den das Original beim Schlafen nie ausführte (siehe „Tag/Nacht" oben); `DerivedLevelData.setDayTime` ist leer (DerivedLevelData.java:90-91), Vanilla genügt, der Handler ist entfernt.
  - Respawn: `PlayerRespawnPositionEvent` (NeoForge, gefeuert von `PlayerList#respawn`, kann über `setRespawnLevel` die Zieldimension ändern). Stirbt ein Spieler ohne Bett in einer OreSpawn-Dimension, dort respawnen lassen, wenn der 1:1-Nachbau das verlangt. Position offen (siehe `unresolved`).
  - Keine Mixins nötig, vollständig serverseitig.

### WorldProviderOreSpawn2

- **Rolle:** Provider der **Mining**-Dimension, `DimensionID2` (OreSpawnMain.java:5041-5042).
- **Biom:** `WorldChunkManagerHell(BiomeGenBase.extremeHills, 0.01)` (WorldProviderOreSpawn2.java:21). Danach `getBiomeGenAt(0,0).setTemperatureRainfall(0.8, 0.01)` (22) auf dem **globalen Vanilla-Biomobjekt**. Die Oberwelt-Extreme-Hills verlieren damit ihre Vanilla-Werte 0.2/0.3 (client-1.7.10.jar `ahu.<clinit>` @308-312), sobald der Provider initialisiert ist; durch `keepLoaded` also beim Serverstart. Nebenwirkung auf die Oberwelt (Deutung: kein Schnee in großen Höhen).
  - Kein eigenes OreSpawn-Biom; Höhe `height_MidHills` 1.0/0.5 (`ahu.<clinit>` @117-127, @302-305).
  - Spawnlisten: Vanilla-Grundspawns, OreSpawn-Oberwelt-Regeln für `extremeHills` und die Dino-Liste des Generators (siehe `ChunkProviderOreSpawn2`).
- **Name:** „Dimension-Extreme" (12-14). **Respawn:** true (16-18). **dimensionId** `DimensionID2` (23). **Tag/Nacht:** identischer Schlaf-Hook (26-49).
- **Generator:** `new ChunkProviderOreSpawn2(world, seed, true)` (51-53).
- **Recherche-Abgleich:** 02-dimensions-worldgen.md:148-149 nennt das Biom „nicht ermittelt"; laut Code ist es Extreme Hills.

- **Portierung 1.21.1:**
  - Dimension `orespawn:mining`, Biom `orespawn:mining` als Kopie von `minecraft:windswept_hills` mit 0.8/0.01 und den Spawns wie oben. `sky_color` = `calculateSkyColor(0.8F)` = **7907327** (Rechnung wie bei Utopia).
  - Die globale Mutation des Oberweltbioms **nicht** nachbauen (Datapack-Biome sind unveränderlich, und die Wirkung war ein Nebeneffekt).
  - `dimension_type`, Schlaf- und Respawn-Hook wie Utopia.

### WorldProviderOreSpawn3

- **Rolle:** Provider der **Village**-Dimension, `DimensionID3` (OreSpawnMain.java:5043-5044).
- **Biom:** `new BiomeGenUtopianPlains(BiomeVillageID)`, Farbe 353825, Name „Villages", 0.7/0.5 (WorldProviderOreSpawn3.java:15). In `registerWorldChunkManager`:
  1. `setVillageCreatures()` (27) **ergänzt** die Listen, ersetzt sie nicht (BiomeGenUtopianPlains.java:192-252). Die Monsterliste enthält damit die Vanilla-Grundmonster **und** die Roboter:

     | Liste | Zusatzeinträge (Gewicht/min-max) |
     |---|---|
     | monster | Robot1 25/4-8, Robot2 16/2-8, Robot3 12/2-4, Robot4 8/1-2, Robot5 20/4-8, GiantRobot 8/1-2 (`JefferyEnable`), SpiderDriver 20/3-5, Godzilla 2/1-1 |
     | ambient | Firefly 10/3-6, EntityButterfly 25/3-6, EntityLunaMoth 20/1-5, Chipmunk 5/1-2, Cockateil 15/2-4, Tshirt 2/1-1, Coin 2/1-1, BandP 15/1-2 (`CriminalEnable`) |
     | creature | Girlfriend 1/2-3, Boyfriend 1/2-3, RedCow 8/4-8, GoldCow 6/2-6, EnchantedCow 4/2-4 |

     Mehrere Mobs stehen dadurch doppelt in einer Liste (Grund- plus Village-Eintrag), und ihre Gewichte addieren sich.
  2. `WorldChunkManagerHell(MyPlains, 0.5)` (28), 0.7/0.5 (29), `dimensionId` (30).
  3. `BiomeManager.addVillageBiome(MyPlains, true)` (31) trägt das Biom in die Vanilla-Dorfbiomliste ein, bei jeder Provider-Instanz ein neues Biomobjekt (Deutung). Das wirkt ohnehin nicht, weil `MapGenMoreVillages` die Biomprüfung ignoriert (MapGenMoreVillages.java:32-35). Nebenwirkung (Deutung): Die Oberwelt-`MapGenVillage` akzeptiert dieses Biom, das in der Oberwelt aber nie vorkommt.
- **Name:** „Dimension-VillageMania" (18-20). **Respawn:** true (22-24). **Tag/Nacht:** Schlaf-Hook (34-57).
- **Generator:** `new ChunkProviderOreSpawn3(world, seed, true)` (59-61).

- **Portierung 1.21.1:**
  - Dimension `orespawn:village`, Biom `orespawn:villages` (0.7/0.5, `sky_color` 7972607) mit Vanilla-Grundspawns, Utopia-Grundliste und Village-Liste. Die doppelten Einträge als summierte Gewichte oder als zwei Einträge übernehmen, beides ist gleichwertig.
  - Der Dorfbiom-Eintrag wird zum Biom-Tag der eigenen Dorfstruktur (siehe `ChunkProviderOreSpawn3`).
  - `dimension_type`, Schlaf- und Respawn-Hook wie Utopia.

### WorldProviderOreSpawn4

- **Rolle:** Provider der **Danger**-Dimension („Islands"), `DimensionID4` (OreSpawnMain.java:5045-5046).
- **Biom:** `new BiomeGenUtopianPlains(BiomeIslandsID)`, Farbe 353825, Name „Islands", 0.7/0.5 (WorldProviderOreSpawn4.java:15). Dann `setIslandCreatures()` (27), das alle vier Listen **ersetzt** (keine Vanilla-Mobs). `WorldChunkManagerHell(MyPlains, 0.01)` (28), Temperatur/Regen 0.8/0.01 (29), `dimensionId` (30).

  | Liste | Einträge (Gewicht/min-max) | Zeile BiomeGenUtopianPlains.java |
  |---|---|---|
  | ambient | EntityButterfly 5/2-6, Cockateil 4/1-2, EntityLunaMoth 5/2-4, Firefly 10/4-8, Dragon 1/1-2, Stinky 2/1-2, CliffRacer 20/3-6, CloudShark 1/1-1, GoldFish 5/2-4 | 71-97 |
  | monster | CreepingHorror 60/4-8, TerribleTerror 25/3-6, LurkingTerror 1/1-1, PitchBlack 15/3-6, LeafMonster 35/2-4, EnderReaper 25/2-4, HerculesBeetle 5/1-2 | 98-118 |
  | creature, waterCreature | leer | 67, 69 |

  - Der Decorator bleibt der des Grundkonstruktors, wird aber nie aufgerufen, weil `ChunkProviderOreSpawn4.populate` leer ist.
  - Viele Mobs und Blöcke fragen die Dimension-ID ab, z. B. Tagesbedingung von `Cockateil` ohne Höhenprüfung (Cockateil.java:234) und Nachtverhalten von `BlockAppleLeaves` (BlockAppleLeaves.java:50, 68). Die Id `orespawn:danger` muss im Port zentral abfragbar sein.
- **Name:** „Dimension-Islands" (18-20). **Respawn:** true (22-24). **Tag/Nacht:** Schlaf-Hook (33-56).
- **Generator:** `new ChunkProviderOreSpawn4(world, seed, true)` (58-60).

- **Portierung 1.21.1:**
  - Dimension `orespawn:danger`, Biom `orespawn:islands` (0.8/0.01, `sky_color` 7907327) ohne Features, Spawnliste wie oben.
  - `dimension_type`, Schlaf- und Respawn-Hook wie Utopia. Fallschaden normal (Website: „Death by falling is common", 02-dimensions-worldgen.md:251).

### WorldProviderOreSpawn5

- **Rolle:** Provider der **Crystal**-Dimension, `DimensionID5` (OreSpawnMain.java:5047-5048).
- **Biom:** `new BiomeGenUtopianPlains(BiomeCrystalID)`, Farbe 353825, Name „Crystal", 0.7/0.5 (WorldProviderOreSpawn5.java:15). Dann `setCrystalCreatures()` (27): ersetzt die Listen und schaltet den Decorator ab (BiomeGenUtopianPlains.java:121-190). `setHeight(new Height(0.1, 0.5))` (28) steuert das Rauschterrain, `WorldChunkManagerHell(MyPlains, 0.01)` (29), 0.8/0.01 (30), `dimensionId` (31).

  | Liste | Einträge (Gewicht/min-max) | Zeile BiomeGenUtopianPlains.java |
  |---|---|---|
  | creature | CrystalCow 1/1-4 | 131 |
  | ambient | Fairy 10/4-8, Peacock 5/4-8, Mantis 1/1-1, EntityButterfly 10/2-4, Cockateil 4/1-2, EntityLunaMoth 4/1-2 | 134-164 |
  | monster | Rotator 4/1-2, Vortex 3/1-2, Urchin 15/2-4, DungeonBeast 30/4-6, Rat 40/4-6 | 143-155 |
  | waterCreature | Whale 1/1-2, Crab 1/1-2, Flounder 5/6-8, Irukandji 4/2-3, Skate 2/3-6, Frog 1/3-5 | 167-182 |

  - Client-Seite: `CrystalGrass` und `OreBasicStone` sind nur in dieser Dimension undurchsichtig (`current_dimension == DimensionID5`, CrystalGrass.java:63, 67; OreBasicStone.java:51, 55).
- **Name:** „Dimension-Crystal" (18-20). **Respawn:** true (22-24). **Tag/Nacht:** Schlaf-Hook (34-57).
- **Generator:** `new ChunkProviderOreSpawn5(world, seed, true)` (59-61).

- **Portierung 1.21.1:**
  - Dimension `orespawn:crystal`, Biom `orespawn:crystal` (0.8/0.01, `sky_color` 7907327) ohne Vanilla-Features. Die Höhe 0.1/0.5 gehört in den Generator-Codec, weil Datapack-Biome keine Höhe mehr kennen.
  - Die dimensionsabhängige Undurchsichtigkeit ist Client-Rendering (`com.swbr.orespawn.client`) und liest dort die Dimension des Client-Levels, keinen globalen Zähler.
  - `dimension_type`, Schlaf- und Respawn-Hook wie Utopia.

### WorldProviderOreSpawn6

- **Rolle:** Provider der **Chaos**-Dimension, `DimensionID6` (OreSpawnMain.java:5049-5050).
- **Biom:** `new BiomeGenUtopianPlains(BiomeChaosID)`, Farbe 353825, Name „Chaos", 0.7/0.5 (WorldProviderOreSpawn6.java:15). Dann `setChaosCreatures()` (27): ersetzt die Listen (keine Vanilla-Mobs), Decorator Blumen 2, Gras 4, Bäume 1 (BiomeGenUtopianPlains.java:254-436). Umfang: ambient 16 Einträge, creature 4, monster 37, waterCreature 0 (Zählung BiomeGenUtopianPlains.java:269-435). Das deckt sich mit der Website: „Almost all the mobs spawn here, with the exception of the water-based creatures" (02-dimensions-worldgen.md:315-316). `WorldChunkManagerHell(MyPlains, 0.01)` (28), 0.8/0.01 (29).
  - `dimensionId` wird zuerst auf `DimensionID4` und direkt danach auf `DimensionID6` gesetzt (30-31). Die erste Zuweisung ist tot und wirkungslos (auch im manifest sichtbar).
- **Name:** „Dimension-Chaos" (18-20). **Respawn:** true (22-24) → Respawn über dem Void möglich (Deutung). **Tag/Nacht:** Schlaf-Hook (34-57).
- **Generator:** `new ChunkProviderOreSpawn6(world, seed)` ohne Strukturflag (59-61).

- **Portierung 1.21.1:**
  - Dimension `orespawn:chaos`, Biom `orespawn:chaos` (0.8/0.01, `sky_color` 7907327) mit Vanilla-Blumen, Gras und einem Baum pro Chunk; Spawnlisten 1:1 aus `setChaosCreatures`.
  - `dimension_type` wie Utopia. Die tote `DimensionID4`-Zuweisung entfällt.
  - Respawn-Hook: ein Respawn ohne Bett in Chaos kann über dem Void landen. 1:1 übernehmen oder für Chaos auf die Oberwelt umleiten (Entscheidung offen).

---

## Querschnitt für den Port

| Klasse | Umsetzung 1.21.1 | Chunkgrenze | Nicht-deterministisch im Original |
|---|---|---|---|
| ChunkProviderOreSpawn | Java-`ChunkGenerator` (Rauschen, Oberfläche, Höhlen, Erze) + Biom-JSON | chunk-lokal | keine |
| ChunkProviderOreSpawn2 | wie oben + Vanilla-Features (Seen, Monster-Rooms) im eigenen Biom | chunk-lokal / Vanilla-Feature | keine |
| ChunkProviderOreSpawn3 | wie oben + eigenes `structure_set` für Dörfer (Structure) | Dörfer mehrchunkig → Structure | keine |
| ChunkProviderOreSpawn4 | kleiner Java-`ChunkGenerator` + chunk-geclipptes Baum-Feature | chunk-lokal | Baumpositionen und -formen |
| ChunkProviderOreSpawn5 | Java-`ChunkGenerator` mit allen Features im Generator, Clip beachten | chunk-lokal | Labyrinth, Baumformen |
| ChunkProviderOreSpawn6 | Java-`ChunkGenerator` (invertierter Nether ohne Lava) + Baum-Feature | chunk-lokal | Erze, Bäume, Decorator |
| WorldProviderOreSpawn1-6 | Datapack `dimension` + `dimension_type` + Biom-JSON; `PlayerRespawnPositionEvent` (kein `SleepFinishedTimeEvent`, Fixwelle W05) | – | – |

Recherche gegen Code, kurz:

| Recherche sagt | Code sagt | Herkunft |
|---|---|---|
| Mining-Biom „nicht ermittelt" | Extreme Hills | WorldProviderOreSpawn2.java:21 |
| Mining-Mobs „ambient/creature" | nur `ambient` | ChunkProviderOreSpawn2.java:390-410 |
| Utopia „einige Feinde unterirdisch" | `monster` → null, keine natürlichen Monster | ChunkProviderOreSpawn.java:315-317 |
| Crystal-Maze-Wände „Obsidian/Bedrock" | nur Bedrock | CrystalMaze.java:25, 133-136 |
| Chaos-Terrain mit Wasser | Terrain setzt nie Wasser | ChunkProviderOreSpawn6.java:154-156 |
| Danger-Schichthöhe „nicht ausgelesen" | Bedrock Y 0, Erde 1-6, Gras 7 | ChunkProviderOreSpawn4.java:25-35 |

Allgemeine Risiken:
- Die 1.7.10-Rauschklassen gibt es in 1.21.1 nicht in dieser Form. Sie müssen als eigene Klassen portiert werden, sonst ändert sich jede Geländeform.
- Der Chunk-Clip ist Spielverhalten: Crystal-Erze behalten etwa 29 % ihrer Blöcke (Rechnung oben), Bäume in Danger/Chaos/Crystal enden am Chunkrand.
- Die Spawnlisten-Sonderlogik (Utopia ohne Monster, Mining Dino-Liste + Biomliste, Village Liste + Zusatz) lässt sich vollständig über eigene Biom-JSONs abbilden. `getMobsAt` muss nur überschrieben werden, wenn Vanilla-Biome wiederverwendet würden.
- Keine Mixins nötig. Alles läuft serverseitig, außer der Crystal-Undurchsichtigkeit (Client-Rendering).
